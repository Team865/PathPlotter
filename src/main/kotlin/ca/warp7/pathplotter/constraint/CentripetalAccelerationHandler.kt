package ca.warp7.pathplotter.constraint

import javafx.scene.control.*
import javafx.scene.layout.VBox
import javafx.stage.Window

class CentripetalAccelerationHandler : ConstraintHandler {
    override fun getName(): String {
        return "Centripetal Acceleration Constraint"
    }

    override fun editConstraint(owner: Window) {
        val dialog = Dialog<ButtonType>()
        dialog.title = "Centripetal Acceleration Constraint"
        dialog.initOwner(owner)
        dialog.dialogPane.content = VBox().apply {
            spacing = 4.0
            children.add(CheckBox("Enable constraint").apply { prefWidth = 200.0 })
            children.add(Label("Maximum centripetal acceleration in metres per second squared"))
            children.add(TextField())
        }
        dialog.dialogPane.stylesheets.add("/style.css")
        dialog.dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)
        dialog.show()
    }
}