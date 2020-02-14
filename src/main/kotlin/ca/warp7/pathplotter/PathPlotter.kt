package ca.warp7.pathplotter

import ca.warp7.pathplotter.fx.combo
import ca.warp7.pathplotter.fx.menuItem
import ca.warp7.pathplotter.remote.RemoteListener
import ca.warp7.pathplotter.state.Constants
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
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCombination
import javafx.scene.input.MouseEvent
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.Stage
import org.kordamp.ikonli.materialdesign.MaterialDesign.*
import kotlin.math.min
import kotlin.system.measureNanoTime

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
        stage.icons.add(Image(PathPlotter::class.java.getResourceAsStream("/icon.png")))
    }

    private val dialogs = Dialogs(stage)
    private val gc: GraphicsContext = canvas.graphicsContext2D

    private var controlDown = false

    private val path = getDefaultPath()
    private val ref = PixelReference()

    private val fileMenu = Menu("File", null,
            menuItem("New/Open Trajectory", MDI_FOLDER, combo(KeyCode.N, control = true)) {
            },
            menuItem("Save as", MDI_CONTENT_SAVE, combo(KeyCode.S, control = true)) {

            },
            menuItem("Configure Path", MDI_SETTINGS, combo(KeyCode.COMMA, control = true)) {
                //                config.showSettings(stage)
//                regenerate()
            }
    )

    private val editMenu = Menu(
            "Path",
            null,
            MenuItem("Insert Spline Control Point"),
            MenuItem("Insert Reverse Direction"),
            MenuItem("Insert Quick Turn"),
            MenuItem("Reverse Point(s)"),
            menuItem("Select All", null, combo(KeyCode.A, control = true)) {
                for (cp in path.controlPoints) cp.isSelected = true
                redrawScreen()
            }
    )

    private fun transformItem(name: String, combo: KeyCombination, x: Double,
                              y: Double, theta: Double, fieldRelative: Boolean): MenuItem {
        return menuItem(name, null, combo) {
            transformSelected(x, y, theta, fieldRelative)
        }
    }

    private val transformMenu = Menu("Transform Point(s) by Steps", null,
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

    private val selectionMenu = Menu(
            "Selection",
            null,
            menuItem("Delete Point(s)", MDI_DELETE, combo(KeyCode.D)) {
                if (path.controlPoints.count { !it.isSelected } >= 2) {
                    path.controlPoints.removeIf { it.isSelected }
                    regenerate()
                }
            },
            menuItem("Edit Point(s)", MDI_PEN, combo(KeyCode.E)) {

            },
            menuItem("Transform Point(s)", MDI_CURSOR_MOVE, combo(KeyCode.T)) {

            },
            transformMenu)

    private val trajectoryMenu = Menu("Trajectory", null,
            menuItem("Start/Pause Simulation", MDI_PLAY, combo(KeyCode.SPACE)) { onSpacePressed() },
            menuItem("Stop Simulation", MDI_STOP, combo(KeyCode.DIGIT0)) { stopSimulation() },
            menuItem("Graphs", MDI_CHART_LINE, combo(KeyCode.G, control = true)) { showGraphs() },
            SeparatorMenuItem()
    )

    init {
        for (handler in constraintHandlers) {
            trajectoryMenu.items.add(MenuItem(handler.getName()).apply {
                this.setOnAction { handler.editConstraint(stage) }
            })
        }
        menuBar.menus.addAll(
                fileMenu,
                editMenu,
                selectionMenu,
                trajectoryMenu,
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
            if (it.code == KeyCode.CONTROL) {
                controlDown = true
            }
        }
        stage.scene.setOnKeyReleased {
            if (it.code == KeyCode.CONTROL) {
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

        for (controlPoint in path.controlPoints) {

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
        for (controlPoint in path.controlPoints) {
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

    private val graphWindow = GraphWindow(stage, path)

    private fun showGraphs() {
        graphWindow.show()
    }

    private fun transformSelected(x: Double, y: Double, theta: Double, fieldRelative: Boolean) {
        val delta = Translation2d(x, y)
        val rotation = Rotation2d.fromDegrees(theta)
        var offset = delta
        if (!fieldRelative) {
            for (controlPoint in path.controlPoints) {
                if (controlPoint.isSelected) {
                    offset = delta.rotateBy(controlPoint.pose.rotation)
                }
            }
        }
        for (controlPoint in path.controlPoints) {
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

        val time = measureNanoTime {
            path.regenerateAll()
        } / 1E6

        infoBar.setDist(0.0, path.totalDist)
        infoBar.setTime(0.0, path.totalTime)
        infoBar.setCurve(0.0, 0.0, path.totalSumOfCurvature)
        infoBar.setComputeTime(time)

        redrawScreen()
    }

    private fun redrawScreen() {
        val bg = path.background
        val imageWidthToHeight = bg.width / bg.height

        var w = canvas.width - 8
        var h = canvas.height - 8

        w = min(w, h * imageWidthToHeight)
        h = min(w * imageWidthToHeight, w / imageWidthToHeight)
        w = h * imageWidthToHeight

        val offsetX = (canvas.width - w) / 2.0
        val offsetY = (canvas.height - h) / 2.0

        ref.set(w, h, offsetX, offsetY, Constants.kFieldSize * 2, Constants.kFieldSize)

        gc.fill = Color.WHITE
        gc.fillRect(0.0, 0.0, canvas.width, canvas.height)
        gc.drawImage(bg, offsetX, offsetY, w, h)


        for ((index, trajectory) in path.trajectoryList.withIndex()) {
            drawSplines(ref, trajectory, index % 2 == 1, gc, path.robotWidth, path.robotLength)
        }
        if (!simulating) {
            val firstState = path.trajectoryList.first().states.first().poseMeters
            drawRobot(ref, gc, path.robotWidth, path.robotLength, firstState)
            updateSelectedPointInfo()
        }

        drawAllControlPoints()
        graphWindow.drawGraph()
    }

    private fun updateSelectedPointInfo() {
        for (cp in path.controlPoints) {
            if (cp.isSelected) {
                controlBar.setPose(cp.pose)
                return
            }
        }
        controlBar.clearPose()
    }

    private fun drawAllControlPoints() {
        if (simulating) return
        for (controlPoint in path.controlPoints) {
            gc.stroke = when {
                controlPoint.isSelected -> Color.rgb(0, 255, 255)
                else -> Color.WHITE
            }
            drawArrowForPose(ref, gc, controlPoint.pose)
        }
    }

    var simFrameCount = 0

    private val simulationTimer = object : AnimationTimer() {
        override fun handle(now: Long) {
            if (simFrameCount % 3 == 0) {
                handleSimulation()
            }
        }
    }

    private var simulating = false
    private var simPaused = false
    private var simElapsed = 0.0
    private var simElapsedChanged = false
    private var lastTime = 0.0

    private fun onSpacePressed() {
        if (simulating) {
            simPaused = !simPaused
        } else {
            simulating = true
            simElapsed = 0.0
            simFrameCount = 0
            simPaused = false
            lastTime = System.currentTimeMillis() / 1000.0
            redrawScreen()
            simulationTimer.start()
        }
    }

    private fun stopSimulation() {
        simulating = false
        simPaused = false
        redrawScreen()
        simulationTimer.stop()
    }

    fun handleSimulation() {
        val nt = System.currentTimeMillis() / 1000.0
        val dt = nt - lastTime
        lastTime = nt
        if (simPaused) {
            if (!simElapsedChanged) return
            simElapsedChanged = false
        } else simElapsed += dt
        val t = simElapsed
        if (t > path.totalTime) {
            stopSimulation()
            return
        }
        var trackedTime = 0.0
        var simSeg = path.trajectoryList.first()
        for (seg in path.trajectoryList) {
            if ((trackedTime + seg.totalTimeSeconds) > t) {
                simSeg = seg
                break
            }
            trackedTime += seg.totalTimeSeconds
        }
        val relativeTime = t - trackedTime

        val sample = simSeg.sample(relativeTime)

        redrawScreen()

        val v = sample.velocityMetersPerSecond
        val w = v * sample.curvatureRadPerMeter

        infoBar.setVel(v, w, sample.accelerationMetersPerSecondSq, 0.0)
        infoBar.setCurve(sample.curvatureRadPerMeter, 0.0, path.totalSumOfCurvature)
        controlBar.setPose(sample.poseMeters)

        drawRobot(ref, gc, path.robotWidth, path.robotLength, sample.poseMeters)
        gc.stroke = Color.YELLOW
        drawArrowForPose(ref, gc, sample.poseMeters)
    }
}