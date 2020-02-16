package ca.warp7.pathplotter

import ca.warp7.pathplotter.fx.combo
import ca.warp7.pathplotter.fx.menuItem
import ca.warp7.pathplotter.remote.RemoteListener
import ca.warp7.pathplotter.state.Constants
import ca.warp7.pathplotter.state.ControlPoint
import ca.warp7.pathplotter.state.PixelReference
import ca.warp7.pathplotter.state.getDefaultPath
import ca.warp7.pathplotter.ui.*
import edu.wpi.first.wpilibj.geometry.Pose2d
import edu.wpi.first.wpilibj.geometry.Rotation2d
import edu.wpi.first.wpilibj.geometry.Translation2d
import javafx.animation.AnimationTimer
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.input.MouseEvent
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.Stage
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign.*
import kotlin.math.min
import kotlin.system.exitProcess

class PathPlotter {

    val stage = Stage()

    private val menuBar = MenuBar()

    private val canvas: Canvas = Canvas()
    private val canvasContainer = Pane(canvas)

    private val infoBar = InfoBar()
    private val controlBar = ControlBar()

    private val view = BorderPane().apply {
        top = menuBar
        center = canvasContainer
        bottom = VBox(controlBar.top, infoBar.container)
    }

    private val remoteListener = RemoteListener()

    init {
        remoteListener.addConnectionListener {
            infoBar.setConnection(it)
        }
        menuBar.isUseSystemMenuBar = true
        canvas.isFocusTraversable = true
        canvas.addEventFilter(MouseEvent.MOUSE_CLICKED) { canvas.requestFocus() }
        view.stylesheets.add("/style.css")
        stage.scene = Scene(view)
        stage.title = "PathPlotter 2020.2.0"
        stage.width = 1000.0
        stage.height = 600.0
        stage.fullScreenExitKeyCombination = KeyCodeCombination(KeyCode.F11)
        stage.icons.add(Image(PathPlotter::class.java.getResourceAsStream("/icon.png")))
    }

    private val dialogs = Dialogs(stage)
    private val gc: GraphicsContext = canvas.graphicsContext2D

    private var controlDown = false
    private var isFullScreen = false

    private val model = getDefaultPath()
    private val ref = PixelReference()

    private val fileMenu = Menu("File", null,
            menuItem("Configure Field Background", MDI_IMAGE, combo(KeyCode.F, control = true)) {
                val fc = dialogs.newFieldConfig()
                model.fieldConfig = fc
                redrawScreen()
            },
            menuItem("Robot Parameters", MDI_TUNE, combo(KeyCode.COMMA, control = true)) {
            },
            SeparatorMenuItem(),
            menuItem("Open Path", MDI_FOLDER_OUTLINE, null) {},
            menuItem("Open Playback", MDI_PLAY_BOX_OUTLINE, null) {},
            SeparatorMenuItem(),
            menuItem("Exit", null, null) {
                exitProcess(0)
            }
    )

    private fun transformItem(name: String, combo: KeyCombination, x: Double,
                              y: Double, theta: Double, fieldRelative: Boolean): MenuItem {
        return menuItem(name, null, combo) {
            transformSelected(x, y, theta, fieldRelative)
        }
    }

    private val transformMenu = Menu("Transform Point(s)", FontIcon.of(MDI_CURSOR_MOVE, 15),
            transformItem("Rotate 1 degree counter-clockwise", combo(KeyCode.Q), 0.0, 0.0, 1.0, false),
            transformItem("Rotate 1 degree clockwise", combo(KeyCode.W), 0.0, 0.0, -1.0, false),
            transformItem("Move up 0.01 metres", combo(KeyCode.UP), 0.0, 0.01, 0.0, true),
            transformItem("Move down 0.01 metres", combo(KeyCode.DOWN), 0.0, -0.01, 0.0, true),
            transformItem("Move left 0.01 metres", combo(KeyCode.LEFT), -0.01, 0.0, 0.0, true),
            transformItem("Move right 0.01 metres", combo(KeyCode.RIGHT), 0.01, 0.0, 0.0, true),
            transformItem("Move forward 0.01 metres", combo(KeyCode.UP, shift = true), 0.01, 0.0, 0.0, false),
            transformItem("Move reverse 0.01 metres", combo(KeyCode.DOWN, shift = true), -0.01, 0.0, 0.0, false),
            transformItem("Move left-normal 0.01 metres", combo(KeyCode.LEFT, shift = true), 0.0, 0.01, 0.0, false),
            transformItem("Move right-normal 0.01 metres", combo(KeyCode.RIGHT, shift = true), 0.0, -0.01, 0.0, false)
    )

    private val pathMenu = Menu(
            "Path",
            null,
            menuItem("Insert Control Point", MDI_PLUS, combo(KeyCode.N)) {
                model.controlPoints.withIndex().firstOrNull { it.value.isSelected }?.let { cp ->
                    val ps = cp.value.pose
                    cp.value.isSelected = false
                    val transform = ps.rotation.translation().times(1.5)
                    val newPose = Pose2d(ps.translation + transform, ps.rotation)
                    val newCp = ControlPoint(newPose)
                    newCp.isSelected = true
                    model.controlPoints.add(cp.index + 1, newCp)
                }
                regenerate()
            },
            menuItem("Insert Reverse Direction", MDI_SWAP_VERTICAL, combo(KeyCode.R, control = true)) {

            },
            menuItem("Insert Quick Turn", MDI_SYNC, combo(KeyCode.T, control = true)) {

            },
            SeparatorMenuItem(),
            menuItem("Select All", null, combo(KeyCode.A, control = true)) {
                for (cp in model.controlPoints) cp.isSelected = true
                redrawScreen()
            },
            menuItem("Delete Point(s)", MDI_DELETE, combo(KeyCode.D)) {
                if (model.controlPoints.count { !it.isSelected } >= 2) {
                    model.controlPoints.removeIf { it.isSelected }
                    regenerate()
                }
            },
            transformMenu,
            SeparatorMenuItem(),
            menuItem("Optimize Path", MDI_MATRIX, null) {

            }
    )

    private val constraintsMenu = Menu("Timing Constraints", FontIcon.of(MDI_GAUGE, 15))

    private val trajectoryMenu = Menu("Trajectory", null,
            menuItem("Start/Pause Playback", MDI_PLAY, combo(KeyCode.SPACE)) { onSpacePressed() },
            menuItem("Stop Playback", MDI_STOP, combo(KeyCode.ESCAPE)) { stopSimulation() },
            menuItem("Timing Graph", MDI_CHART_LINE, combo(KeyCode.G, control = true)) { showGraphs() },
            SeparatorMenuItem(),
            menuItem("Start Live Recording", MDI_RECORD, null) {},
            SeparatorMenuItem(),
            constraintsMenu
    )

    private val viewMenu = Menu("View", null,
            CheckMenuItem("Curvature Gradient").apply { isSelected = true },
            CheckMenuItem("Dual Offset Paths").apply { isSelected = true },
            menuItem("Toggle Fullscreen", null, combo(KeyCode.F11)) {
                isFullScreen = !isFullScreen
                stage.isFullScreen = isFullScreen
            }
    )

    init {
        for (handler in constraintHandlers) {
            constraintsMenu.items.add(MenuItem(handler.getName()).apply {
                this.setOnAction { handler.editConstraint(stage) }
            })
        }
        menuBar.menus.addAll(
                fileMenu,
                pathMenu,
                trajectoryMenu,
                viewMenu,
                dialogs.helpMenu
        )
        canvas.setOnMousePressed {
            onMousePressed(it.x, it.y)
            it.consume()
        }
        canvas.setOnMouseDragged {
            drag(it.x, it.y)
            it.consume()
        }
        stage.scene.setOnKeyPressed {
            if (it.isShortcutDown) {
                controlDown = true
            }
        }
        stage.scene.setOnKeyReleased {
            if (!it.isShortcutDown) {
                controlDown = false
            }
        }
    }

    private var isDraggingAngle = false

    private fun onMousePressed(x: Double, y: Double) {
        if (simulating) return
        val mouseOnField = ref.inverseTransform(Translation2d(x, y))

        var selectionChanged = false

        isDraggingAngle = false

        for (controlPoint in model.controlPoints) {

            val posInRange = controlPoint.pose.translation
                    .getDistance(mouseOnField) < Constants.kControlPointCircleSize

            val headingInRange = controlPoint.pose.translation.plus(controlPoint.pose.rotation
                    .translation().times(Constants.kArrowLength))
                    .getDistance(mouseOnField) < Constants.kArrowTipLength

            // Make sure draggingAngle is not overpowered by other points
            isDraggingAngle = isDraggingAngle || headingInRange

            val inRange = posInRange || headingInRange
            if (controlPoint.isSelected) {
                if (controlDown) {
                    if (inRange) {
                        controlPoint.isSelected = false
                        selectionChanged = true
                    }
                } else {
                    if (!inRange) {
                        controlPoint.isSelected = false
                        selectionChanged = true
                    }
                }
            } else {
                if (inRange) {
                    controlPoint.isSelected = true
                    selectionChanged = true
                }
            }
        }

        if (selectionChanged) {
            redrawScreen()
        }
    }

    private fun drag(x: Double, y: Double) {
        if (simulating) return
        val mouseOnField = ref.inverseTransform(Translation2d(x, y))
        for (controlPoint in model.controlPoints) {
            if (controlPoint.isSelected) {
                if (isDraggingAngle) {
                    controlPoint.pose = Pose2d(controlPoint.pose.translation,
                            mouseOnField.minus(controlPoint.pose.translation).direction())
                } else {
                    controlPoint.pose = Pose2d(mouseOnField, controlPoint.pose.rotation)
                }
                regenerate()
                return
            }
        }
    }

    private val graphWindow = GraphWindow(stage, model)

    private fun showGraphs() {
        graphWindow.show()
    }

    private fun transformSelected(x: Double, y: Double, theta: Double, fieldRelative: Boolean) {
        val delta = Translation2d(x, y)
        val rotation = Rotation2d.fromDegrees(theta)
        var offset = delta
        if (!fieldRelative) {
            for (controlPoint in model.controlPoints) {
                if (controlPoint.isSelected) {
                    offset = delta.rotateBy(controlPoint.pose.rotation)
                }
            }
        }
        for (controlPoint in model.controlPoints) {
            if (controlPoint.isSelected) {
                val oldPose = controlPoint.pose
                val newPose = Pose2d(snap(oldPose.translation + offset), oldPose.rotation + rotation)
                controlPoint.pose = newPose
            }
        }
        regenerate()
    }

    fun show() {
        Platform.runLater {
            regenerate()
            stage.show()
            val resizeListener = ChangeListener<Number> { _, _, _ ->
                redrawScreen()
            }

            canvas.widthProperty().bind(canvasContainer.widthProperty())
            canvas.heightProperty().bind(canvasContainer.heightProperty())
            canvas.widthProperty().addListener(resizeListener)
            canvas.heightProperty().addListener(resizeListener)
        }
    }

    private fun regenerate() {

        model.regenerateAll()

        infoBar.setDist(model.totalDist)
        infoBar.setTime(0.0, model.totalTime)
        infoBar.setCurve(0.0, 0.0, model.totalSumOfCurvature)
        controlBar.setTotalTime(model.totalTime)

        redrawScreen()
    }

    private fun redrawScreen() {
        val bg = model.fieldConfig.image
        val imageWidthToHeight = bg.width / bg.height

        var w = canvas.width - 8
        var h = canvas.height - 8

        // fit the image onto the canvas, preserving the ratio
        w = min(w, h * imageWidthToHeight)
        h = min(w * imageWidthToHeight, w / imageWidthToHeight)
        w = h * imageWidthToHeight

        val offsetX = (canvas.width - w) / 2.0
        val offsetY = (canvas.height - h) / 2.0

        ref.set(model.fieldConfig, w, h, offsetX, offsetY)

        gc.fill = Color.WHITE
        gc.fillRect(0.0, 0.0, canvas.width, canvas.height)
        gc.drawImage(bg, offsetX, offsetY, w, h)


        for ((index, trajectory) in model.trajectoryList.withIndex()) {
            drawSplines(ref, trajectory, index % 2 == 1, gc, model.robotWidth, model.robotLength)
        }
        drawMeasuredStates(ref, model.measuredStates, gc, model.robotWidth, model.robotLength)
        if (!simulating) {
            val firstState = model.trajectoryList.first().states.first().poseMeters
            drawRobot(ref, gc, model.robotWidth, model.robotLength, firstState)
            updateSelectedPointInfo()
        }

        drawAllControlPoints()
        graphWindow.drawGraph()
    }

    private fun updateSelectedPointInfo() {
        for (cp in model.controlPoints) {
            if (cp.isSelected) {
                controlBar.setPose(cp.pose)
                return
            }
        }
        controlBar.clearPose()
    }

    private fun drawAllControlPoints() {
        if (simulating) return
        for (controlPoint in model.controlPoints) {
            gc.stroke = when {
                controlPoint.isSelected -> Color.rgb(0, 255, 255)
                else -> Color.WHITE
            }
            drawArrowForPose(ref, gc, controlPoint.pose)
        }
    }

    private val simulationTimer = object : AnimationTimer() {
        override fun handle(now: Long) {
            handleSimulation()
        }
    }

    private var simulating = false
    private var simPaused = false
    private var simElapsed = 0.0
    private var simElapsedChanged = false
    private var lastTime = 0.0
    private var prevK = 0.0
    private var prevW = 0.0

    private fun onSpacePressed() {
        if (simulating) {
            simPaused = !simPaused
        } else {
            simulating = true
            simElapsed = 0.0
            simPaused = false
            prevK = 0.0
            prevW = 0.0
            lastTime = System.currentTimeMillis() / 1000.0
            redrawScreen()
            simulationTimer.start()
        }
    }

    private fun stopSimulation() {
        if (!simulating) return
        simulating = false
        simPaused = false
        redrawScreen()
        controlBar.setTime(0.0)
        simulationTimer.stop()
    }

    fun handleSimulation() {
        val nt = System.currentTimeMillis() / 1000.0
        val dt = nt - lastTime
        lastTime = nt

        if (simPaused) {
            if (!simElapsedChanged) return
            simElapsedChanged = false
        } else {
            simElapsed += dt
        }

        val t = simElapsed
        if (t > model.totalTime) {
            stopSimulation()
            return
        }

        var trackedTime = 0.0
        var currentTrajectory = model.trajectoryList.first()
        for (seg in model.trajectoryList) {
            if ((trackedTime + seg.totalTimeSeconds) > t) {
                currentTrajectory = seg
                break
            }
            trackedTime += seg.totalTimeSeconds
        }
        val relativeTime = t - trackedTime

        val sample = currentTrajectory.sample(relativeTime)

        redrawScreen()

        val v = sample.velocityMetersPerSecond
        val k = sample.curvatureRadPerMeter
        val w = v * k

        val dkShim = (k - prevK) / dt
        val dwShim = (w - prevW) / dt

        prevK = k
        prevW = w

        infoBar.setVel(v, w, sample.accelerationMetersPerSecondSq, dwShim)
        infoBar.setCurve(k, dkShim, model.totalSumOfCurvature)
        controlBar.setPose(sample.poseMeters)
        controlBar.setTime(t)
        infoBar.setTime(t, model.totalTime)

        drawRobot(ref, gc, model.robotWidth, model.robotLength, sample.poseMeters)
        gc.stroke = Color.YELLOW
        drawArrowForPose(ref, gc, sample.poseMeters)
    }
}