package ca.warp7.pathplotter.state

import ca.warp7.pathplotter.degrees
import edu.wpi.first.wpilibj.geometry.Pose2d

fun getDefaultPath(): Model {
    val path = Model()



    path.apply {
        maxVelocity = 3.0
        maxAcceleration = 3.0
        robotLength = 15 * 0.0254
        robotWidth = 13 * 0.0254
//        wheelbaseRadius = 0.5
//        maxJerk = 5.0
    }
    val pts = listOf(
            Pose2d(3.1, 1.7, 0.degrees),
            Pose2d(5.85, 3.38, 0.degrees),
            Pose2d(7.7, 3.38, 0.degrees)
    )
    path.controlPoints.addAll(pts.map { pose2d -> ControlPoint(pose2d) })
//    path.segments.add(Segment().apply {
//        waypoints = listOf(
//
//        )
//    })
//    val p = Pose2d(5.120, 3.414, (180 + 32).degrees).plus(Pose2d(2.3, -0.2, 0.degrees).minus(Pose2d()))
//    path.segments.add(Segment().apply {
//        inverted = true
//        waypoints = listOf(
//                Pose2d(5.120, 3.414, (180 + 32).degrees),
//                p
//        )
//    })
//    val p2 = Pose2d(p.translation, p.rotation + Rotation2D(-1.0, 0.0))
//    path.segments.add(Segment().apply {
//        waypoints = listOf(
//                p2,
//                p2 + Pose2d(0.0, 0.0, 110.degrees)
//        )
//    })
//    path.segments.add(Segment().apply {
//        waypoints = listOf(
//                p2 + Pose2d(0.0, 0.0, 110.degrees),
//                Pose2d(0.914, 3.505, (-180).degrees)
//        )
//    })
//
//    path.segments.add(Segment().apply {
//        inverted = true
//        waypoints = listOf(
//                Pose2d(0.914, 3.505, 0.degrees),
//                Pose2d(7.315, 2.133, 0.degrees)
//        )
//    })
//
//    path.segments.add(Segment().apply {
//        waypoints = listOf(
//                Pose2d(7.315, 2.133, 180.degrees),
//                Pose2d(7.315, 2.133, (180 - 90).degrees)
//        )
//    })
//
//    path.segments.add(Segment().apply {
//        waypoints = listOf(
//                Pose2d(7.315, 2.133, (180 - 90).degrees),
//                Pose2d(6.888, 3.413, (180 - 32).degrees)
//        )
//    })
//
//    for ((i, s) in path.segments.withIndex()) {
//        for ((j, point) in s.c.withIndex()) {
//            path.controlPoints.add(ControlPoint(point, i))
//        }
//    }

    return path
}