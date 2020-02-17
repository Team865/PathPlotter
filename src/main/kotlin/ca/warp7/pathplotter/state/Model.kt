package ca.warp7.pathplotter.state

import ca.warp7.frc2020.lib.trajectory.QuinticHermiteSpline
import ca.warp7.pathplotter.remote.MeasuredState
import ca.warp7.pathplotter.util.translation
import edu.wpi.first.wpilibj.geometry.Pose2d
import edu.wpi.first.wpilibj.kinematics.DifferentialDriveKinematics
import edu.wpi.first.wpilibj.trajectory.Trajectory
import edu.wpi.first.wpilibj.trajectory.TrajectoryParameterizer
import edu.wpi.first.wpilibj.trajectory.constraint.DifferentialDriveKinematicsConstraint

class Model {

    var fieldConfig = FieldConfig.fromResources("/2020-infiniterecharge.json")!!

    val controlPoints: MutableList<ControlPoint> = ArrayList()

    var maxAngular = 0.0
    var maxAngularAcc = 0.0

    var totalTime = 0.0
    var totalSumOfCurvature = 0.0
    var totalDist = 0.0

    var robotWidth = 0.7
    var robotLength = 0.8

    var maxVelocity = 2.1
    var maxAcceleration = 2.8

    var optimizing = false

    val trajectoryList: MutableList<Trajectory> = ArrayList()

    val measuredStates: MutableList<MeasuredState> = ArrayList()

    fun regenerateAll() {
        val paths = controlPoints.zipWithNext { a, b ->
            QuinticHermiteSpline.fromPose(a.pose, b.pose, a.magMultiplier, b.magMultiplier)
        }
        if (optimizing) {
            QuinticHermiteSpline.optimizeSpline(paths.toMutableList())
        }
        val poseStates = QuinticHermiteSpline.parameterize(paths)
        trajectoryList.clear()
        trajectoryList.add(TrajectoryParameterizer.timeParameterizeTrajectory(poseStates, listOf(
                DifferentialDriveKinematicsConstraint(
                        DifferentialDriveKinematics(0.701),
                        3.0
                )
        ),
                0.0, 0.0, maxVelocity,
                maxAcceleration, false))
        totalTime = trajectoryList.sumByDouble { it.totalTimeSeconds }

        totalDist = 0.0
        for (tr in trajectoryList) {
            for (i in 0 until tr.states.size - 1) {
                totalDist += tr.states[i].poseMeters.translation
                        .getDistance(tr.states[i + 1].poseMeters.translation)
            }
        }

        totalSumOfCurvature = QuinticHermiteSpline.sum_dCurvature_squared(paths)
    }

    fun addPoint() {
        (controlPoints
                .withIndex()
                .firstOrNull { it.value.isSelected }
                ?: IndexedValue(controlPoints.lastIndex, controlPoints.last()))
                .let { cp ->
            val ps = cp.value.pose
            cp.value.isSelected = false
            val transform = ps.rotation.translation().times(1.5)
            val newPose = Pose2d(ps.translation + transform, ps.rotation)
            val newCp = ControlPoint(newPose)
            newCp.isSelected = true
            controlPoints.add(cp.index + 1, newCp)
        }
    }
}