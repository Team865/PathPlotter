package ca.warp7.pathplotter.constraint

import javafx.scene.control.*
import javafx.scene.layout.VBox
import javafx.stage.Window

class DifferentialDriveVoltageHandler : ConstraintHandler {
    override fun getName(): String {
        return "Differential Drive Voltage Constraint"
    }

    override fun editConstraint(owner: Window) {
        val dialog = Dialog<ButtonType>()
        dialog.title = " Differential Drive Voltage Constraint"
        dialog.initOwner(owner)
        dialog.dialogPane.content = VBox().apply {
            spacing = 4.0
            children.add(CheckBox("Enable constraint").apply { prefWidth = 200.0 })
            children.add(Label("Effective wheelbase radius in metres (track width / 2 * scrub factor)"))
            children.add(TextField())
            children.add(Label("Max Voltage"))
            children.add(TextField())
            children.add(Label("Ks"))
            children.add(TextField())
            children.add(Label("Kv"))
            children.add(TextField())
            children.add(Label("Ka"))
            children.add(TextField())
        }
        dialog.dialogPane.stylesheets.add("/style.css")
        dialog.dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)
        dialog.show()
    }
}