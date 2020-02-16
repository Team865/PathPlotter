package ca.warp7.pathplotter

import ca.warp7.pathplotter.fx.combo
import ca.warp7.pathplotter.fx.menuItem
import ca.warp7.pathplotter.fx.observable
import ca.warp7.pathplotter.state.FieldConfig
import javafx.scene.control.*
import javafx.scene.input.KeyCode
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
                "/2018-powerup.json",
                "/2019-deepspace.json",
                "/2020-infiniterecharge.json").observable())
        cb.selectionModel.select(0)
        dialog.dialogPane.buttonTypes.add(ButtonType.OK)
        dialog.dialogPane.content = VBox(cb)
        dialog.initOwner(stage)
        dialog.showAndWait()

        val si = cb.value ?: return FieldConfig.DEFAULT
        return FieldConfig.fromResources(si)
    }
}