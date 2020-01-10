package ca.warp7.planner2.constraint

import edu.wpi.first.wpilibj.geometry.Translation2d
import edu.wpi.first.wpilibj.trajectory.constraint.MecanumDriveKinematicsConstraint
import javafx.scene.control.*
import javafx.scene.layout.VBox
import javafx.stage.Window

class MecanumKinematicsHandler : ConstraintHandler {
    override fun getName(): String {
        return "Mecanum Kinematics Constraint"
    }

    override fun editConstraint(owner: Window) {
        val dialog = Dialog<ButtonType>()
        dialog.title = "Mecanum Kinematics Constraint"
        dialog.initOwner(owner)
        dialog.dialogPane.content = VBox().apply {
            spacing = 4.0
            children.add(CheckBox("Enable constraint"))

            children.add(Label("Max velocity in metres per second"))
            children.add(TextField())
            children.add(Label("Wheel distance from centre"))
            children.add(ListView<Translation2d>().apply { prefHeight = 96.0 })
        }
        dialog.dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)
        dialog.show()
    }
}