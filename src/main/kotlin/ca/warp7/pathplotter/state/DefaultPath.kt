package ca.warp7.pathplotter.state

import ca.warp7.pathplotter.degrees
import edu.wpi.first.wpilibj.geometry.Pose2d

fun getDefaultModel(): Model {
    val path = Model()

    path.apply {
        maxVelocity = 2.0
        maxAcceleration = 1.0
        robotLength = 0.38
        robotWidth = 0.33
    }
    val pts = listOf(
            Pose2d(3.1, 1.7, 0.degrees),
            Pose2d(5.85, 3.38, 0.degrees),
            Pose2d(7.7, 3.38, 0.degrees)
    )
    path.controlPoints.addAll(pts.map { pose2d -> ControlPoint(pose2d) })

    return path
}