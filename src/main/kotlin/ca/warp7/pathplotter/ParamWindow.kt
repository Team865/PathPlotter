package ca.warp7.pathplotter

import ca.warp7.pathplotter.fx.observable
import ca.warp7.pathplotter.state.Model
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.layout.Background
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.stage.Stage

class ParamWindow(owner: Stage, private val model: Model) {
    private val stage = Stage()

    private fun textField(initial: String = ""): TextField {
        return TextField(initial).apply { prefWidth = 50.0 }
    }

    private val grid = GridPane().apply {
        this.background = Background.EMPTY
        padding = Insets(8.0)
        hgap = 8.0
        vgap = 8.0

        val botWidth = textField(model.robotWidth.toString())
        val botLength = textField(model.robotLength.toString())
        val maxVel = textField(model.maxVelocity.toString())
        val maxAcc = textField(model.maxAcceleration.toString())

        add(ComboBox(listOf("Infinite Recharge", "Destination: Deep Space", "FIRST Power Up").observable()).apply {
            selectionModel.select(0)
        }, 0, 0, 2, 1)

        add(Label("Robot Width (m)"), 0, 1)
        add(botWidth, 1, 1)

        add(Label("Robot Length (m)"), 0, 2)
        add(botLength, 1, 2)

        add(Label("Max Velocity (m/s)"), 0, 3)
        add(maxVel, 1, 3)

        add(Label("Max Acceleration (m/s)"), 0, 4)
        add(maxAcc, 1, 4)

        // ############################

        add(CheckBox(), 2, 0)
        add(Label("Differential Drive Kinematics Constraint"), 3, 0)

        add(HBox(Label("Track Width (m)"), textField()).apply {
            spacing = 8.0
            alignment = Pos.CENTER_LEFT
        }, 3, 1)

        add(CheckBox(), 2, 2)
        add(Label("Differential Drive Voltage Constraint"), 3, 2)

        add(HBox(
                Label("Ks"),
                textField(),
                Label("Kv"),
                textField(),
                Label("Ka"),
                textField()
        ).apply {
            spacing = 8.0
            alignment = Pos.CENTER_LEFT
        }, 3, 3)

        add(CheckBox(), 2, 4)
        add(Label("Centripetal Acceleration Constraint"), 3, 4)

        add(HBox(
                Label("Max Centripetal Accel. (m/s²)"),
                textField()
        ).apply {
            spacing = 8.0
            alignment = Pos.CENTER_LEFT
        }, 3, 5)
    }

    init {
        stage.title = "Path Parameters"
        stage.initOwner(owner)

        stage.icons.add(Image(GraphWindow::class.java.getResourceAsStream("/icon.png")))
        stage.scene = Scene(grid)
        stage.scene.stylesheets.add("/style.css")
        stage.isResizable = false
    }

    fun show() {
        if (stage.isShowing) {
            stage.requestFocus()
            return
        }
        stage.show()
    }
}