package ca.warp7.planner2

import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.layout.GridPane
import javafx.stage.FileChooser
import javafx.stage.Stage

class PathWizard(owner: Stage?) {

    val dialog = Dialog<ButtonType>()

    init {
        if (owner != null) {
            dialog.initOwner(owner)
        } else {
            (dialog.dialogPane.scene.window as Stage).icons
                    .add(Image(PathWizard::class.java.getResourceAsStream("/icon.png")))
        }
        
        val c = Configuration()

        val maxVel = TextField(c.maxVelocity.toString())
        val maxAcc = TextField(c.maxAcceleration.toString())
        val botWidth = TextField(c.robotWidth.toString())
        val botLength = TextField(c.robotLength.toString())
        

        val choose = Button("Choose")

        choose.setOnAction {
            val chooser = FileChooser()
            chooser.title = "Choose Half Field Background"
            chooser.extensionFilters.add(FileChooser.ExtensionFilter("PNG", "*.png"))
            val f = chooser.showOpenDialog(dialog.dialogPane.scene.window)

            if (f != null && f.name.endsWith("png")) {
                try {
                    val image = Image(f.inputStream())
                    c.background = image
                    choose.text = f.name
                } catch (e: Exception) {
                }
            }
        }
        val f2020 = Button("Load 2020 Blue")
        val f20202 = Button("Load 2020 Red")

        val openJSON = Button("Open JSON File")
        openJSON.setOnAction {
            val fc = FileChooser()
            fc.showOpenDialog(dialog.dialogPane.scene.window)
        }

        dialog.dialogPane.content = GridPane().apply {
            hgap = 8.0
            vgap = 8.0
            add(openJSON, 0, 0, 3, 1)

            add(Label("Max Velocity"), 0, 2)
            add(maxVel, 1, 2)

            add(Label("Max Acceleration"), 0, 3)
            add(maxAcc, 1, 3)

            add(Label("Robot Width (m)"), 0, 4)
            add(botWidth, 1, 4)

            add(Label("Robot Length (m)"), 0, 5)
            add(botLength, 1, 5)

            add(Label("Half Field Background"), 2, 1)
            add(choose, 2, 2)

            add(f2020, 2, 3)
            add(f20202, 2, 4)
        }
        dialog.title = "New Path"
        dialog.dialogPane.buttonTypes.addAll(ButtonType.OK)
    }

    fun show() {
        dialog.showAndWait()
        if (dialog.result == ButtonType.OK) {
            Planner2().stage.show()
        }
    }
}