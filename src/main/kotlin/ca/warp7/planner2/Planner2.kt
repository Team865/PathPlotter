package ca.warp7.planner2

import ca.warp7.planner2.fx.combo
import ca.warp7.planner2.fx.menuItem
import ca.warp7.planner2.state.Constants
import ca.warp7.planner2.state.PixelReference
import ca.warp7.planner2.state.getDefaultPath
import edu.wpi.first.wpilibj.geometry.Pose2d
import edu.wpi.first.wpilibj.geometry.Rotation2d
import edu.wpi.first.wpilibj.geometry.Translation2d
import javafx.animation.AnimationTimer
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.collections.MapChangeListener
import javafx.collections.ObservableMap
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCombination
import javafx.scene.input.MouseEvent
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.Stage
import kotlin.math.min
import kotlin.system.measureNanoTime

class Planner2 {

    val stage = Stage()

    val menuBar = MenuBar()

    val canvas: Canvas = Canvas()
    val canvasContainer = Pane(canvas)


    val pathStatus: ObservableMap<String, String> = FXCollections
            .observableMap<String, String>(LinkedHashMap())

    private val pathStatusLabel = Label().apply {
        style = "-fx-text-fill: white"
    }

    val pointStatus: ObservableMap<String, String> = FXCollections
            .observableMap<String, String>(LinkedHashMap())

    private val pointStatusLabel = Label()

    val view = BorderPane().apply {
        top = menuBar
        center = canvasContainer
        bottom = VBox().apply {
            children.addAll(
                    HBox().apply {
                        style = "-fx-background-color: white"
                        padding = Insets(4.0, 16.0, 4.0, 16.0)
                        children.add(pointStatusLabel)
                    },
                    HBox().apply {
                        style = "-fx-background-color: #1e2e4a"
                        padding = Insets(4.0, 16.0, 4.0, 16.0)
                        children.add(pathStatusLabel)
                    }
            )
        }
    }

    init {
        menuBar.isUseSystemMenuBar = true
        pathStatus.addListener(MapChangeListener {
            pathStatusLabel.text = pathStatus.entries
                    .joinToString("   ") { it.key + ": " + it.value }
        })
        pointStatus.addListener(MapChangeListener {
            pointStatusLabel.text = pointStatus.entries
                    .joinToString("   ") { it.key + ": " + it.value }
        })
        canvas.isFocusTraversable = true
        canvas.addEventFilter(MouseEvent.MOUSE_CLICKED) { canvas.requestFocus() }
        stage.scene = Scene(view)
        stage.title = "FRC Drive Path Planner"
        stage.width = 1000.0
        stage.height = 600.0
        stage.icons.add(Image(Planner2::class.java.getResourceAsStream("/icon.png")))
    }

    private val dialogs = Dialogs(stage)
    private val gc: GraphicsContext = canvas.graphicsContext2D

    private var controlDown = false

    private val path = getDefaultPath()
    private val ref = PixelReference()

    private val fileMenu = Menu("File", null,
            menuItem("New/Open Trajectory", combo(KeyCode.N, control = true)) {
                PathWizard(stage).show()
            },
            menuItem("Save as", combo(KeyCode.S, control = true)) {

            },
            menuItem("Configure Path", combo(KeyCode.COMMA, control = true)) {
                //                config.showSettings(stage)
//                regenerate()
            },
            MenuItem("Generate WPILib Java function"),
            MenuItem("Generate WPILib C++ function")
    )

    private val editMenu = Menu(
            "Path",
            null,
            MenuItem("Insert Spline Control Point"),
            MenuItem("Insert Reverse Direction"),
            MenuItem("Insert Quick Turn"),
            MenuItem("Reverse Point(s)"),
            menuItem("Select All", combo(KeyCode.A, control = true)) {
                for (cp in path.controlPoints) cp.isSelected = true
                redrawScreen()
            }
    )

    private fun transformItem(name: String, combo: KeyCombination, x: Double,
                              y: Double, theta: Double, fieldRelative: Boolean): MenuItem {
        return menuItem(name, combo) {
            transformSelected(x, y, theta, fieldRelative)
        }
    }

    private val stopMenu = Menu("Transform Point(s) by Steps", null,
            transformItem("Rotate 1 degree counter-clockwise", combo(KeyCode.Q), 0.0, 0.0, 1.0, false),
            transformItem("Rotate 1 degree clockwise", combo(KeyCode.W), 0.0, 0.0, -1.0, false),
            transformItem("Move up 0.01 metres", combo(KeyCode.UP), 0.0, 0.01, 0.0, true),
            transformItem("Move down 0.01 metres", combo(KeyCode.DOWN), 0.0, -0.01, 0.0, true),
            transformItem("Move left 0.01 metres", combo(KeyCode.LEFT), -0.01, 0.0, 0.0, true),
            transformItem("Move right 0.01 metres", combo(KeyCode.RIGHT),  0.01, 0.0, 0.0, true),
            transformItem("Move forward 0.01 metres", combo(KeyCode.UP, shift = true), 0.01, 0.0, 0.0, false),
            transformItem("Move reverse 0.01 metres", combo(KeyCode.DOWN, shift = true), -0.01, 0.0, 0.0, false),
            transformItem("Move left-normal 0.01 metres", combo(KeyCode.LEFT, shift = true), 0.0, 0.01, 0.0, false),
            transformItem("Move right-normal 0.01 metres", combo(KeyCode.RIGHT, shift = true), 0.0, -0.01, 0.0, false)
    )

    private val pointMenu = Menu(
            "Selection",
            null,
            menuItem("Delete Point(s)", combo(KeyCode.D)) {
                if (path.controlPoints.count { !it.isSelected } >= 2) {
                    path.controlPoints.removeIf { it.isSelected }
                    regenerate()
                }
            },
            menuItem("Edit Point(s)", combo(KeyCode.E)) {

            },
            menuItem("Transform Point(s)", combo(KeyCode.T)) {

            },
            stopMenu)

    private val trajectoryMenu = Menu("Trajectory", null,
            menuItem("Start/Pause Simulation", combo(KeyCode.SPACE)) { onSpacePressed() },
            menuItem("Stop Simulation", combo(KeyCode.DIGIT0)) { stopSimulation() },
            menuItem("Graphs", combo(KeyCode.G, control = true)) { showGraphs() },
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
                pointMenu,
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

    var isDraggingAngle = false

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

    private fun drag(x:Double, y: Double) {
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

    fun regenerate() {

        val time = measureNanoTime {
            path.regenerateAll()
        } / 1E6

        pathStatus.putAll(mapOf(
                "∫(dξ)" to "${path.totalDist.f2}m",
                "∫(dt)" to "${path.totalTime.f2}s",
                "Σ(dCurvature²)" to path.totalSumOfCurvature.f2,
                "Optimize" to path.optimizing.toString(),
                "MaxVel" to "", //state.maxVelString(),
                "MaxAcc" to "", // state.maxAccString(),
                "MaxCAcc" to "", // state.maxAcString(),
                "ComputeTime" to "${time.f2}ms"
        ))
        pointStatus.putAll(mapOf(
                "t" to "0.0s",
                "x" to "0.0m",
                "y" to "0.0m",
                "heading" to "0.0deg",
                "curvature" to "0.0rad/m",
                "v" to "0.0m/s",
                "ω" to "0.0rad/s",
                "dv/dt" to "0.0m/s^2"
        ))

        redrawScreen()
    }

    private fun redrawScreen() {
        val bg = path.background
        val imageWidthToHeight = bg.width / bg.height

        var w = canvas.width - 32
        var h = canvas.height - 32

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
                pointStatus.putAll(mapOf(
                        "t" to "---s",
                        "x" to "${cp.pose.translation.x.f}m",
                        "y" to "${cp.pose.translation.y.f}m",
                        "heading" to "${cp.pose.rotation.degrees.f}deg",
                        "curvature" to "---rad/m",
                        "v" to "---m/s",
                        "ω" to "---rad/s",
                        "dv/dt" to "---m/s^2"
                ))
                return
            }
        }
        pointStatus.clear()
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

    var simulating = false
    var simPaused = false
    var simElapsed = 0.0
    var simElapsedChanged = false
    var lastTime = 0.0

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

        val w = sample.velocityMetersPerSecond * sample.curvatureRadPerMeter

        pointStatus.putAll(mapOf(
                "t" to "${sample.timeSeconds.f2}s",
                "x" to "${sample.poseMeters.translation.x.f2}m",
                "y" to "${sample.poseMeters.translation.y.f2}m",
                "heading" to "${sample.poseMeters.rotation.degrees.f2}deg",
                "curvature" to "${sample.curvatureRadPerMeter.f2}rad/m",
                "v" to "${sample.velocityMetersPerSecond.f2}m/s",
                "ω" to "${w.f2}rad/s",
                "dv/dt" to "${sample.accelerationMetersPerSecondSq.f2}m/s^2"
        ))
        drawRobot(ref, gc, path.robotWidth, path.robotLength, sample.poseMeters)
        gc.stroke = Color.YELLOW
        drawArrowForPose(ref, gc, sample.poseMeters)
    }
}