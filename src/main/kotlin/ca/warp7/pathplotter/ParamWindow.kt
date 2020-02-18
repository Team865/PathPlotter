package ca.warp7.pathplotter

import ca.warp7.pathplotter.fx.observable
import ca.warp7.pathplotter.state.DefaultFields
import ca.warp7.pathplotter.state.Model
import ca.warp7.pathplotter.util.f2
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.layout.Background
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.stage.Stage

class ParamWindow(owner: Stage, private val model: Model, private val callback: () -> Unit) {
    private val stage = Stage()

    private fun textField(initial: String = ""): TextField {
        return TextField(initial).apply {
            prefWidth = 50.0
            setOnAction { updateAndRegenerate() }
        }
    }

    private fun checkBox(label: String = ""): CheckBox {
        return CheckBox(label).apply {
            setOnAction { updateAndRegenerate() }
        }
    }

    private val fieldSelector = ComboBox(DefaultFields.values().toList().observable()).apply {
        selectionModel.select(0)
        valueProperty().addListener { _, _, nv ->
            model.fieldConfig = nv.createFieldConfig()
            callback()
        }
    }

    private val botWidth = textField(model.robotWidth.f2)
    private val botLength = textField(model.robotLength.f2)
    private val maxVel = textField(model.maxVelocity.f2)
    private val maxAcc = textField(model.maxAcceleration.f2)

    private val curvatureChangeOptimization = checkBox("Minimize Curvature Change")

    private val differentialDriveKinematicsConstraint = checkBox()
    private val differentialDriveVoltageConstraint = checkBox()
    private val centripetalAccelerationConstraint = checkBox()

    private val trackWidth = textField()
    private val kv = textField()
    private val ka = textField()
    private val ks = textField()
    private val maxCentripetal = textField()

    private val grid = GridPane().apply {
        this.background = Background.EMPTY
        padding = Insets(8.0)
        hgap = 8.0
        vgap = 8.0

        add(fieldSelector, 0, 0, 2, 1)

        add(Label("Robot Width (m)"), 0, 1)
        add(botWidth, 1, 1)

        add(Label("Robot Length (m)"), 0, 2)
        add(botLength, 1, 2)

        add(Label("Max Velocity (m/s)"), 0, 3)
        add(maxVel, 1, 3)

        add(Label("Max Acceleration (m/s²)"), 0, 4)
        add(maxAcc, 1, 4)

        add(curvatureChangeOptimization, 0, 5, 2, 1)

        // ############################

        add(differentialDriveKinematicsConstraint, 2, 0)
        add(Label("Differential Drive Kinematics Constraint"), 3, 0)

        add(HBox(
                Label("Effective Track Width (m)"), trackWidth
        ).apply {
            spacing = 8.0
            alignment = Pos.CENTER_LEFT
        }, 3, 1)

        add(differentialDriveVoltageConstraint, 2, 2)
        add(Label("Differential Drive Voltage Constraint"), 3, 2)

        add(HBox(
                Label("Ks"), ks,
                Label("Kv"), kv,
                Label("Ka"), ka
        ).apply {
            spacing = 8.0
            alignment = Pos.CENTER_LEFT
        }, 3, 3)

        add(centripetalAccelerationConstraint, 2, 4)
        add(Label("Centripetal Acceleration Constraint"), 3, 4)

        add(HBox(
                Label("Max Centripetal Accel. (m/s²)"),
                maxCentripetal
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

    private fun updateAndRegenerate() {
        try {
            val bw = botWidth.text.toDouble().coerceIn(0.0, 10.0)
            val bl = botLength.text.toDouble().coerceIn(0.0, 10.0)
            val mv = maxVel.text.toDouble().coerceIn(0.0, 10.0)
            val ma = maxAcc.text.toDouble().coerceIn(0.0, 10.0)
            model.robotWidth = bw
            model.robotLength = bl
            model.maxVelocity = mv
            model.maxAcceleration = ma
            model.optimizeCurvature = curvatureChangeOptimization.isSelected

            model.differentialDriveKinematicsHandler.isEnabled =
                    differentialDriveKinematicsConstraint.isSelected
            if (model.differentialDriveKinematicsHandler.isEnabled) {
                model.differentialDriveKinematicsHandler.maxSpeedMetresPerSecond =
                        model.maxVelocity
                model.differentialDriveKinematicsHandler.trackWidthMetres =
                        trackWidth.text.toDouble().coerceIn(0.1, 10.0)
            }


            model.centripetalAccelerationHandler.isEnabled =
                    centripetalAccelerationConstraint.isSelected
            if (model.centripetalAccelerationHandler.isEnabled) {

                model.centripetalAccelerationHandler.maxCentripetalAccelerationMetresPerSecondSq =
                        maxCentripetal.text.toDouble().coerceIn(0.1, 10.0)
            }

            model.differentialDriveVoltageHandler.isEnabled =
                    differentialDriveVoltageConstraint.isSelected
            if (model.differentialDriveVoltageHandler.isEnabled) {
                model.differentialDriveVoltageHandler.ks =
                        ks.text.toDouble().coerceIn(0.0, 10.0)
                model.differentialDriveVoltageHandler.kv =
                        kv.text.toDouble().coerceIn(0.0, 10.0)
                model.differentialDriveVoltageHandler.ka =
                        ka.text.toDouble().coerceIn(0.0, 10.0)
                model.differentialDriveVoltageHandler.maxVoltage = 12.0
                model.differentialDriveVoltageHandler.trackWidthMetres =
                        model.differentialDriveKinematicsHandler.trackWidthMetres
            }

            stage.title = "Path Parameters"
            callback()
        } catch (e: Exception) {
            stage.title = "Path Parameters | Error: ${e.localizedMessage}"
        }
    }
}