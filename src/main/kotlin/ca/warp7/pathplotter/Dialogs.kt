package ca.warp7.pathplotter

import ca.warp7.pathplotter.fx.combo
import ca.warp7.pathplotter.fx.menuItem
import ca.warp7.pathplotter.fx.observable
import ca.warp7.pathplotter.state.FieldConfig
import ca.warp7.pathplotter.state.Model
import edu.wpi.first.wpiutil.math.MathUtil
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.layout.Background
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import javafx.stage.Stage
import org.kordamp.ikonli.materialdesign.MaterialDesign

class Dialogs(val stage: Stage) {

    private val aboutButton = menuItem("About", MaterialDesign.MDI_INFORMATION_OUTLINE, combo(KeyCode.F1, shift = true)) {
        val dialog = Dialog<ButtonType>()
        dialog.title = "About PathPlotter"
        dialog.contentText = """PathPlotter version ${BuildConfig.kVersion}
Copyright (c) 2019, 2020 Team 865 WARP7
Licensed under MIT
OS:  ${System.getProperty("os.name")} ${System.getProperty("os.arch")}
Java Runtime: ${System.getProperty("java.vm.name")} ${System.getProperty("java.vm.version")}
JavaFX Build: ${System.getProperty("javafx.runtime.version")}
Kotlin Build: ${KotlinVersion.CURRENT}
Max Heap Size: ${Runtime.getRuntime().maxMemory() / (1024 * 1024)}Mb"""
        dialog.dialogPane.buttonTypes.add(ButtonType.OK)
        dialog.dialogPane.stylesheets.add("/style.css")
        dialog.initOwner(stage)
        dialog.show()
    }

    private val githubButton = menuItem("Show Project on GitHub", MaterialDesign.MDI_GITHUB_CIRCLE, null) {
        PathPlotterApplication.getHostServicesInstance()
                .showDocument("https://github.com/Team865/PathPlotter/")
    }

    val helpMenu = Menu("Help", null, githubButton, aboutButton)

    fun newFieldConfig(): FieldConfig {
        val dialog = Dialog<ButtonType>()
        dialog.title = "Choose Field"
        val cb = ChoiceBox<String>(listOf(
                "/2020-infiniterecharge.json",
                "/2019-deepspace.json",
                "/2018-powerup.json"
        ).observable())
        cb.prefWidth = 240.0
        cb.selectionModel.select(0)
        dialog.dialogPane.buttonTypes.add(ButtonType.OK)
        dialog.dialogPane.stylesheets.add("/style.css")
        dialog.dialogPane.content = VBox(cb)
        dialog.initOwner(stage)
        dialog.showAndWait()

        val si = cb.value ?: return FieldConfig.DEFAULT
        return FieldConfig.fromResources(si)
    }

    fun robotParamDialog(model: Model): Boolean {
        val dialog = Dialog<ButtonType>()
        dialog.title = "Robot Parameters"
        dialog.initOwner(stage)

        val botWidth = TextField(model.robotWidth.toString())
        val botLength = TextField(model.robotLength.toString())
        val maxVel = TextField(model.maxVelocity.toString())
        val maxAcc = TextField(model.maxAcceleration.toString())

        dialog.dialogPane.content = GridPane().apply {
            hgap = 8.0
            vgap = 8.0

            add(Label("Robot Width (m)"), 0, 0)
            add(botWidth, 1, 0)

            add(Label("Robot Length (m)"), 0, 1)
            add(botLength, 1, 1)

            add(Label("Max Velocity (m/s)"), 0, 2)
            add(maxVel, 1, 2)

            add(Label("Max Acceleration (m/s)"), 0, 3)
            add(maxAcc, 1, 3)
        }
        dialog.dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)
        dialog.dialogPane.background = Background.EMPTY
        dialog.dialogPane.stylesheets.add("/style.css")

        dialog.showAndWait()

        if (dialog.result != ButtonType.OK) {
            return false
        }

        return try {
            val bw = MathUtil.clamp(botWidth.text.toDouble(), 0.0, 10.0)
            val bl = MathUtil.clamp(botLength.text.toDouble(), 0.0, 10.0)
            val mv = MathUtil.clamp(maxVel.text.toDouble(), 0.0, 10.0)
            val ma = MathUtil.clamp(maxAcc.text.toDouble(), 0.0, 10.0)
            model.robotWidth = bw
            model.robotLength = bl
            model.maxVelocity = mv
            model.maxAcceleration = ma
            true
        } catch (e: Exception) {
            false
        }
    }
}