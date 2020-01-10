package ca.warp7.planner2

import ca.warp7.planner2.state.Path
import javafx.beans.value.ChangeListener
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.Image
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.text.FontSmoothingType
import javafx.stage.Stage
import javafx.stage.StageStyle

class GraphWindow(owner: Stage, val path: Path) {
    private val stage = Stage()
    private val canvas = Canvas()
    private val gc: GraphicsContext = canvas.graphicsContext2D
    private val cont = Pane(canvas)
    private val listener = ChangeListener<Number> { _, _, _ -> drawGraph() }

    init {
        stage.title = "Graphs"
        stage.initOwner(owner)
        stage.icons.add(Image(GraphWindow::class.java.getResourceAsStream("/icon.png")))
        val scene = Scene(cont)
        cont.prefWidth = 400.0
        cont.prefHeight = 400.0
        stage.minHeight = 300.0
        stage.minWidth = 300.0

        stage.scene = scene

        stage
    }

    fun show() {
        if (stage.isShowing) {
            stage.requestFocus()
            return
        }
        stage.show()
        canvas.widthProperty().addListener(listener)
        canvas.heightProperty().addListener(listener)
        canvas.widthProperty().bind(cont.widthProperty())
        canvas.heightProperty().bind(cont.heightProperty())
    }

    fun drawGraph() {

        if (!stage.isShowing) return

        val pad = 16.0

        val width = gc.canvas.width - pad * 2.0
        val height = gc.canvas.height - pad * 2.0

        val timeHeight = 16.0
        val textHeight = 20.0
        val spaceHeight = 16.0
        val graphHeight = (height - textHeight * 3 - timeHeight - 2 * spaceHeight) / 4.0

        val timeTop = pad + textHeight
        val timeBottom = timeTop + timeHeight

        val velTop = timeBottom + textHeight + spaceHeight
        val velMid = velTop + graphHeight
        val velBottom = velMid + graphHeight

        val accTop = velBottom + textHeight + spaceHeight
        val accMid = accTop + graphHeight
        val accBottom = accMid + graphHeight

        gc.fill = Color.BLACK
        gc.fillRect(0.0, 0.0, gc.canvas.width, gc.canvas.height)

        gc.fontSmoothingType = FontSmoothingType.LCD
        gc.lineWidth = 1.0
        gc.fill = Color.WHITE
        gc.fillText("Time Steps (Total: ${path.totalTime.f}s)", pad, timeTop - 10.0)
        gc.fillText("Velocity vs Time (±${path.maxVelocity.f1}m/s)", pad, velTop - 10.0)
        gc.fillText("Acceleration vs Time (±${path.maxAcceleration.f1}m/s^2)", pad, accTop - 10.0)

        gc.stroke = Color.ORANGE
        var trackedTime = 0.0
        for (trajectory in path.trajectoryList) {
            for (trajectoryState in trajectory.states) {
                val progress = (trajectoryState.timeSeconds + trackedTime) / path.totalTime
                val x = (pad + progress * width)
                gc.strokeLine(x, timeTop, x, timeBottom)
            }
            trackedTime += trajectory.totalTimeSeconds
        }
        gc.stroke = Color.GRAY
        gc.lineWidth = 1.0

        val velPxPerM = graphHeight / path.maxVelocity
        gc.strokeLine(pad, velTop, pad + width, velTop)
        gc.strokeLine(pad, velBottom, pad + width, velBottom)
        var velStep = -path.maxVelocity.toInt()
        if (velStep.toDouble() == -path.maxVelocity) velStep++
        while (velStep < path.maxVelocity) {
            val h = velTop + (path.maxVelocity - velStep) * velPxPerM
            gc.strokeLine(pad, h, pad + width, h)
            velStep++
        }

        val accPxPerM = graphHeight / path.maxAcceleration
        gc.strokeLine(pad, accTop, pad + width, accTop)
        gc.strokeLine(pad, accBottom, pad + width, accBottom)
        var accStep = -path.maxAcceleration.toInt()
        if (accStep.toDouble() == -path.maxAcceleration) accStep++
        while (accStep < path.maxAcceleration) {
            val h = accTop + (path.maxAcceleration - accStep) * accPxPerM
            gc.strokeLine(pad, h, pad + width, h)
            accStep++
        }

        gc.lineWidth = 2.0
        gc.stroke = Color.rgb(128, 128, 255)
        trackedTime = 0.0
        gc.beginPath()
        for (trajectory in path.trajectoryList) {
            for (trajectoryState in trajectory.states) {
                val progress = (trajectoryState.timeSeconds + trackedTime) / path.totalTime
                val v = trajectoryState.velocityMetersPerSecond

                gc.lineTo(pad + progress * width, velMid - v / path.maxVelocity * graphHeight)
            }
            trackedTime += trajectory.totalTimeSeconds
        }
        gc.stroke()

        gc.lineWidth = 2.0
        gc.stroke = Color.rgb(0, 128, 192)
        trackedTime = 0.0
        gc.beginPath()
        for (trajectory in path.trajectoryList) {
            for (trajectoryState in trajectory.states) {
                val progress = (trajectoryState.timeSeconds + trackedTime) / path.totalTime
                val dv = trajectoryState.accelerationMetersPerSecondSq

                gc.lineTo(pad + progress * width, accMid - dv / path.maxAcceleration * graphHeight)
            }
            trackedTime += trajectory.totalTimeSeconds
        }
        gc.stroke()

    }
}