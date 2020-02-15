package ca.warp7.pathplotter.ui

import ca.warp7.pathplotter.util.f
import ca.warp7.pathplotter.util.f2
import edu.wpi.first.wpilibj.geometry.Pose2d
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.HBox

class ControlBar {

    enum class Mode {
        Planned, Actual, Error
    }

    private val timeSlider = Slider().apply {
        this.value = 0.0
        this.prefWidth = 360.0
        this.max = 1.0
        this.min = 0.0
    }

    private fun textField(): TextField {
        return TextField().apply {
            this.prefWidth = 50.0
        }
    }

    private val x = textField()
    private val y = textField()
    private val theta = textField()

    private val planned = RadioButton("Planned")
    private val actual = RadioButton("Actual")
    private val error = RadioButton("Error")

    private var mode = Mode.Planned

    private fun fixWidthLabel(t: String): Label {
        return Label(t)
    }

    private val xLabel = fixWidthLabel("X:")
    private val yLabel = fixWidthLabel("Y:")
    private val thetaLabel = fixWidthLabel("θ:")

    val top = HBox().apply {
        spacing = 8.0
        padding = Insets(4.0, 8.0, 4.0, 8.0)
        this.style = "-fx-background-color: #eee"
        alignment = Pos.CENTER
        this.children.addAll(timeSlider,
                planned, actual, error,
                xLabel, x,
                yLabel, y,
                thetaLabel, theta
        )
    }

    fun setPose(pose: Pose2d) {
        x.text = pose.translation.x.f
        y.text = pose.translation.y.f
        theta.text = pose.rotation.degrees.f2
    }

    fun clearPose() {
        x.text = ""
        y.text = ""
        theta.text = ""
    }

    init {
        val group = ToggleGroup()
        group.toggles.addAll(planned, actual, error)
        group.selectedToggleProperty().addListener { _, _, nv ->
            mode = when {
                nv === planned -> Mode.Planned
                nv === actual -> Mode.Actual
                nv === error -> Mode.Error
                else -> throw IllegalStateException()
            }
        }
        group.selectToggle(planned)
    }
}