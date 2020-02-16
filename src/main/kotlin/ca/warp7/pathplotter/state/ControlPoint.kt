package ca.warp7.pathplotter.state

import edu.wpi.first.wpilibj.geometry.Pose2d

class ControlPoint(var pose: Pose2d) {
    var isSelected = false


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ControlPoint

        if (pose != other.pose) return false

        return true
    }

    override fun hashCode(): Int {
        return pose.hashCode()
    }

}