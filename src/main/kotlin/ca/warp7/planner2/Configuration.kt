package ca.warp7.planner2

import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.layout.GridPane
import javafx.stage.FileChooser
import javafx.stage.Window

/**
 * The part of the State that the user sets in a
 * dialog box
 */
class Configuration {
    var wheelbaseRadius = 0.0
    var maxVelocity = 0.0
    var maxAcceleration = 0.0
    var maxCentripetalAcceleration = 0.0
    var maxJerk = Double.POSITIVE_INFINITY
    var robotWidth = 0.0
    var robotLength = 0.0
    var background: Image? = null

    private fun validate(str: String, old: Double): Double {
        return try {
            str.trim().toDouble()
        } catch (e: NumberFormatException) {
            val dialog = Dialog<ButtonType>()
            dialog.title = "Settings Error"
            dialog.dialogPane.buttonTypes.add(ButtonType.OK)
            dialog.dialogPane.contentText = "Cannot parse $str as a number, reverting to $old"
            dialog.showAndWait()
            old
        }
    }

    fun showSettings(owner: Window) {
        val dialog = Dialog<ButtonType>()
        dialog.title = "Configure Path"
        dialog.initOwner(owner)
        dialog.dialogPane.buttonTypes.addAll(ButtonType.CANCEL, ButtonType.OK)


        val wheelbase = TextField(wheelbaseRadius.toString())
        val maxVel = TextField(maxVelocity.toString())
        val maxAcc = TextField(maxAcceleration.toString())
        val maxCA = TextField(maxCentripetalAcceleration.toString())
        val maxJ = TextField(maxJerk.toString())
        val botWidth = TextField(robotWidth.toString())
        val botLength = TextField(robotLength.toString())

        val choose = Button("Choose")

        choose.setOnAction {
            val chooser = FileChooser()
            chooser.title = "Choose Half Field Background"
            chooser.extensionFilters.add(FileChooser.ExtensionFilter("PNG", "*.png"))
            val f = chooser.showOpenDialog(null)

            if (f != null && f.name.endsWith("png")) {
                try {
                    val image = Image(f.inputStream())
                    background = image
                    choose.text = f.name
                } catch (e: Exception) {
                }
            }
        }

        val f2020 = Button("Load 2020 Blue")
        val f20202 = Button("Load 2020 Red")

        dialog.dialogPane.content = GridPane().apply {
            hgap = 8.0
            vgap = 8.0
            add(Label("Effective Wheelbase Radius"), 0, 0)
            add(wheelbase, 1, 0)

            add(Label("Max Velocity"), 0, 1)
            add(maxVel, 1, 1)

            add(Label("Max Acceleration"), 0, 2)
            add(maxAcc, 1, 2)

            add(Label("Max Centripetal Acceleration"), 0, 3)
            add(maxCA, 1, 3)

            add(Label("Max Jerk"), 0, 4)
            add(maxJ, 1, 4)

            add(Label("Robot Width (for graphics)"), 0, 5)
            add(botWidth, 1, 5)

            add(Label("Robot Length (for graphics)"), 0, 6)
            add(botLength, 1, 6)

            add(Label("Half Field Background"), 2, 0)
            add(choose, 3, 0)

            add(f2020, 3, 1)
            add(f20202, 3, 2)
        }

        dialog.showAndWait()

        wheelbaseRadius = validate(wheelbase.text, wheelbaseRadius)
        maxVelocity = validate(maxVel.text, maxVelocity)
        maxAcceleration = validate(maxAcc.text, maxAcceleration)
        maxCentripetalAcceleration = validate(maxCA.text, maxCentripetalAcceleration)
        maxJerk = validate(maxJ.text, maxJerk)
        robotWidth = validate(botWidth.text, robotWidth)
        robotLength = validate(botLength.text, robotLength)
    }
}