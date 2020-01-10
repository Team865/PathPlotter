package ca.warp7.planner2

import ca.warp7.planner2.fx.combo
import ca.warp7.planner2.fx.menuItem
import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog
import javafx.scene.control.Menu
import javafx.scene.control.TextArea
import javafx.scene.input.KeyCode
import javafx.stage.Stage

class Dialogs(val stage: Stage) {
    private val shortcutButton = menuItem("Keyboard Shortcuts", combo(KeyCode.F1)) {
        val dialog = Dialog<ButtonType>()
        dialog.title = "Shortcuts"
        dialog.contentText = Dialogs::class.java.getResourceAsStream("/docs.txt")
                .bufferedReader().readText()
        dialog.dialogPane.buttonTypes.add(ButtonType.OK)
        dialog.initOwner(stage)
        dialog.show()
    }

    private val aboutButton = menuItem("About", combo(KeyCode.F1, shift = true)) {
        val dialog = Dialog<ButtonType>()
        dialog.title = "About FRC Drive Path Planner"
        dialog.contentText = """FRC Drive Path Planner version 2020.1.0
Copyright (c) 2019,2020 Team 865 WARP7
Licensed under MIT
OS:  ${System.getProperty("os.name")} ${System.getProperty("os.arch")}
Java Runtime: ${System.getProperty("java.vm.name")} ${System.getProperty("java.vm.version")}
JavaFX Build: ${System.getProperty("javafx.runtime.version")}
Kotlin Build: ${KotlinVersion.CURRENT}
Max Heap Size: ${Runtime.getRuntime().maxMemory() / 1024 / 1024}Mb"""
        dialog.dialogPane.buttonTypes.add(ButtonType.OK)
        dialog.initOwner(stage)
        dialog.show()
    }

    private val githubButton = menuItem("Show Project on GitHub", null) {
        PlannerApplication.host?.showDocument("https://github.com/Team865/Path-Planner/")
    }

    val helpMenu = Menu("Help", null, shortcutButton, githubButton, aboutButton)

    fun showTextBox(title: String, content: String) {
        val dialog = Dialog<ButtonType>()
        dialog.title = title
        dialog.dialogPane.content = TextArea(content).apply {
            prefWidth = 800.0
            prefHeight = 600.0
        }
        dialog.dialogPane.buttonTypes.add(ButtonType.OK)
        dialog.initOwner(stage)
        dialog.show()
    }
}