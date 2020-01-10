package ca.warp7.planner2.state

import edu.wpi.first.wpilibj.geometry.Pose2d

class ControlPoint(var pose: Pose2d, val index: Int) {
    var isSelected = false


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ControlPoint

        if (pose != other.pose) return false
        if (index != other.index) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pose.hashCode()
        result = 31 * result + index
        return result
    }

}