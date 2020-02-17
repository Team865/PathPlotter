package ca.warp7.pathplotter.constraint

import javafx.scene.control.*
import javafx.scene.layout.VBox
import javafx.stage.Window

class DifferentialDriveKinematicsHandler : ConstraintHandler {
    override fun getName(): String {
        return "Differential Drive Kinematics Constraint"
    }

    override fun editConstraint(owner: Window) {
        val dialog = Dialog<ButtonType>()
        dialog.title = "Differential Drive Kinematics Constraint"
        dialog.initOwner(owner)
        dialog.dialogPane.content = VBox().apply {
            spacing = 4.0
            children.add(CheckBox("Enable constraint").apply { prefWidth = 200.0 })
            children.add(Label("Effective wheelbase radius in metres (track width / 2 * scrub factor)"))
            children.add(TextField())
            children.add(Label("Max velocity in metres per second"))
            children.add(TextField())
        }
        dialog.dialogPane.stylesheets.add("/style.css")
        dialog.dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)
        dialog.show()
    }
}