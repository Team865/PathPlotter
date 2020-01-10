package ca.warp7.planner2.constraint

import edu.wpi.first.wpilibj.trajectory.constraint.TrajectoryConstraint
import javafx.stage.Window

interface ConstraintHandler {
    fun getName(): String

    fun editConstraint(owner: Window) = Unit

    fun getConstraint(): TrajectoryConstraint? = null
}