package ca.warp7.pathplotter

import ca.warp7.pathplotter.fx.combo
import ca.warp7.pathplotter.fx.menuItem
import ca.warp7.pathplotter.remote.RemoteListener
import ca.warp7.pathplotter.state.PixelReference
import ca.warp7.pathplotter.state.getDefaultModel
import ca.warp7.pathplotter.ui.*
import ca.warp7.pathplotter.util.direction
import ca.warp7.pathplotter.util.translation
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
import javafx.scene.input.*
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

    private val stage = Stage()

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

    private val dialogs = Dialogs(stage)
    private val gc: GraphicsContext = canvas.graphicsContext2D

    private var isFullScreen = false

    private val model = getDefaultModel()

    private val ref = PixelReference()
    private val graphWindow = GraphWindow(stage, model)
    private val paramWindow = ParamWindow(stage, model) { regenerate() }

    private val fileMenu = Menu("File", null,
            menuItem("Configure Field Background", MDI_IMAGE, combo(KeyCode.F, control = true)) {
                val fc = dialogs.newFieldConfig()
                model.fieldConfig = fc
                redrawScreen()
            },
            menuItem("Path Parameters", MDI_TUNE, combo(KeyCode.COMMA, control = true)) {
                paramWindow.show()
            },
//            SeparatorMenuItem(),
//            menuItem("Open Path", MDI_FOLDER_OUTLINE, null) {},
//            menuItem("Open Playback", MDI_PLAY_BOX_OUTLINE, null) {},
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

    private val transformMenu = Menu("Transform Selected Point(s)", FontIcon.of(MDI_CURSOR_MOVE, 15),
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

    @Suppress("unused")
    private val optimizationMenu = Menu("Optimization", FontIcon.of(MDI_MATRIX, 15),
            CheckMenuItem("Automatic Intermediate Direction"),
            CheckMenuItem("Minimize Curvature Sum")
    )

    private val pathMenu = Menu(
            "Path",
            null,
            menuItem("Insert Control Point", MDI_PLUS, combo(KeyCode.N)) {
                model.addPoint()
                regenerate()
            },
//            menuItem("Insert Reverse Direction", MDI_SWAP_VERTICAL, combo(KeyCode.R, control = true)) {
//
//            },
//            menuItem("Insert Quick Turn", MDI_SYNC, combo(KeyCode.T, control = true)) {
//
//            },
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
            transformMenu//,
//            SeparatorMenuItem(),
//            optimizationMenu
    )

    private val viewMenu = Menu("View", null,
            menuItem("Toggle Trajectory Playback", MDI_PLAY, combo(KeyCode.SPACE)) { togglePlayback() },
            menuItem("Show Graphs", MDI_CHART_LINE, combo(KeyCode.G, control = true)) { showGraphs() },
            menuItem("Toggle Fullscreen", null, combo(KeyCode.F11)) {
                isFullScreen = !isFullScreen
                stage.isFullScreen = isFullScreen
            }
    )

    init {
        remoteListener.addConnectionListener {
            infoBar.setConnection(it)
        }
        menuBar.isUseSystemMenuBar = true
        canvas.isFocusTraversable = true
        canvas.addEventFilter(MouseEvent.MOUSE_CLICKED) { canvas.requestFocus() }
        view.stylesheets.add("/style.css")
        stage.scene = Scene(view)
        stage.title = "PathPlotter ${BuildConfig.kVersion}"
        stage.width = 1000.0
        stage.height = 600.0
        stage.fullScreenExitKeyCombination = KeyCodeCombination(KeyCode.F11)
        stage.icons.add(Image(PathPlotter::class.java.getResourceAsStream("/icon.png")))


        menuBar.menus.addAll(
                fileMenu,
                pathMenu,
                viewMenu,
                dialogs.helpMenu
        )
        canvas.setOnMousePressed {
            if (it.isPrimaryButtonDown) {
                onMousePressed(it.x, it.y, it.isShortcutDown)
                it.consume()
            }
        }
        canvas.setOnMouseDragged {
            if (it.isPrimaryButtonDown) {
                drag(it.x, it.y)
                it.consume()
            }
        }

        controlBar.addTimePropertyListener {
            redrawScreen()
        }
        controlBar.addEditListener { newPose, newMag ->
            if (!autoPlayback) {
                model.controlPoints.firstOrNull { it.isSelected }?.let {
                    it.pose = newPose
                    it.magMultiplier = newMag
                    regenerate()
                }
            }
        }
    }

    private var isDraggingAngle = false

    private fun onMousePressed(x: Double, y: Double, controlDown: Boolean) {
        if (autoPlayback) {
            return
        }
        val mouseOnField = ref.inverseTransform(Translation2d(x, y))

        var selectionChanged = false

        isDraggingAngle = false

        for (controlPoint in model.controlPoints) {

            val posInRange = controlPoint.pose.translation
                    .getDistance(mouseOnField) < Constants.kControlPointCircleSize

            val headingInRange = controlPoint.pose.translation.plus(controlPoint.pose.rotation
                    .translation().times(Constants.kArrowMultiplier * controlPoint.magMultiplier))
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
        if (autoPlayback) {
            return
        }
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
                val newPose = Pose2d(oldPose.translation + offset, oldPose.rotation + rotation)
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
            drawSplines(ref, trajectory, index % 2 == 1, gc,
                    model.robotWidth / 2.0, model.robotLength / 2.0)
        }

        drawPlaybackGraphics()
        if (!autoPlayback) {
            updateSelectedPointInfo()
            drawAllControlPoints()
        }

        graphWindow.drawGraph()
    }

    private fun updateSelectedPointInfo() {
        for (cp in model.controlPoints) {
            if (cp.isSelected) {
                controlBar.setPose(cp.pose, cp.magMultiplier)
                return
            }
        }
        controlBar.clearPose()
    }

    private fun drawAllControlPoints() {
        if (autoPlayback) {
            return
        }
        var coordinateDrawn = false
        for (controlPoint in model.controlPoints) {
            if (controlPoint.isSelected) {
                if (!coordinateDrawn) {
                    drawCoordinateFrame(ref, gc, controlPoint.pose)
                    coordinateDrawn = true
                }
                gc.stroke = Color.rgb(0, 255, 255)
            } else {
                gc.stroke = Color.WHITE
            }
            drawArrowForPose(ref, gc, controlPoint.pose, controlPoint.magMultiplier)
        }
    }

    private val playbackTimer = object : AnimationTimer() {
        override fun handle(now: Long) {
            val time = System.nanoTime() / 1E9
            val dt = time - lastTime
            lastTime = time

            val elapsed = controlBar.getElapsedTime()
            val newElapsed = elapsed + dt
            if (newElapsed >= model.totalTime) {
                stopPlayback()
            } else {
                controlBar.setElapsedTime(newElapsed)
            }
        }
    }

    private var autoPlayback = false
    private var lastTime = 0.0

    private fun togglePlayback() {
        if (autoPlayback) {
            autoPlayback = false
            playbackTimer.stop()
            redrawScreen()
        } else {
            autoPlayback = true
            lastTime = System.nanoTime() / 1E9
            redrawScreen()
            playbackTimer.start()
        }
    }

    private fun stopPlayback() {
        if (!autoPlayback) {
            return
        }
        autoPlayback = false
        playbackTimer.stop()
        controlBar.setElapsedTime(0.0)
    }

    private fun drawPlaybackGraphics() {

        val t = controlBar.getElapsedTime()

        var trackedTime = 0.0
        var currentTrajectory = model.trajectoryList.first()
        for (seg in model.trajectoryList) {
            if ((trackedTime + seg.totalTimeSeconds) >= t) {
                currentTrajectory = seg
                break
            }
            trackedTime += seg.totalTimeSeconds
        }
        val relativeTime = t - trackedTime

        val sample = currentTrajectory.sample(relativeTime)

        val v = sample.velocityMetersPerSecond
        val k = sample.curvatureRadPerMeter
        val w = v * k

        val dkShim: Double
        val dwShim: Double
        if (relativeTime <= 0.2) {
            dkShim = 0.0
            dwShim = 0.0
        } else {
            val prevSample = currentTrajectory.sample(relativeTime - 0.2)
            dkShim = (k - prevSample.curvatureRadPerMeter) / 0.2
            dwShim = (w - prevSample.velocityMetersPerSecond * prevSample.curvatureRadPerMeter) / 0.2
        }

        infoBar.setVel(v, w, sample.accelerationMetersPerSecondSq, dwShim)
        infoBar.setCurve(k, dkShim, model.totalSumOfCurvature)
        controlBar.setPose(sample.poseMeters)
        controlBar.setElapsedTime(t)
        infoBar.setTime(t, model.totalTime)

        drawRobot(ref, gc, model.robotWidth / 2.0,
                model.robotLength / 2.0, sample.poseMeters)
        if (autoPlayback) {
            gc.stroke = Color.YELLOW
            drawArrowForPose(ref, gc, sample.poseMeters, 1.2)
        }
    }
}