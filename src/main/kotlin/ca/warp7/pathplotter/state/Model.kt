package ca.warp7.pathplotter.state

import ca.warp7.frc2020.lib.trajectory.QuinticHermiteSpline
import ca.warp7.pathplotter.constraint.CentripetalAccelerationHandler
import ca.warp7.pathplotter.constraint.DifferentialDriveKinematicsHandler
import ca.warp7.pathplotter.constraint.DifferentialDriveVoltageHandler
import ca.warp7.pathplotter.util.kFlip
import ca.warp7.pathplotter.util.translation
import edu.wpi.first.wpilibj.geometry.Pose2d
import edu.wpi.first.wpilibj.geometry.Translation2d
import edu.wpi.first.wpilibj.trajectory.Trajectory
import edu.wpi.first.wpilibj.trajectory.TrajectoryParameterizer

class Model {

    var fieldConfig = FieldConfig.fromResources("/2020-infiniterecharge.json")!!

    val controlPoints: MutableList<ControlPoint> = ArrayList()

    var totalTime = 0.0
    var totalSumOfCurvature = 0.0
    var totalDist = 0.0

    var robotWidth = 0.7
    var robotLength = 0.8

    var maxVelocity = 2.1
    var maxAcceleration = 2.8

    var optimizeCurvature = false
    var reversed = false

    val trajectoryList: MutableList<Trajectory> = ArrayList()

    val differentialDriveKinematicsHandler = DifferentialDriveKinematicsHandler()
    val differentialDriveVoltageHandler = DifferentialDriveVoltageHandler()
    val centripetalAccelerationHandler = CentripetalAccelerationHandler()

    private val constraintHandlers = listOf(
            differentialDriveKinematicsHandler,
            differentialDriveVoltageHandler,
            centripetalAccelerationHandler
    )

    fun regenerateAll() {
        val points = if (reversed) {
            controlPoints.map { ControlPoint(it.pose.plus(kFlip), it.isSelected, it.magMultiplier) }.reversed()
        } else {
            controlPoints
        }
        val paths = points.zipWithNext { a, b ->
            QuinticHermiteSpline.fromPose(a.pose, b.pose, a.magMultiplier, b.magMultiplier)
        }
        if (optimizeCurvature) {
            QuinticHermiteSpline.optimizeSpline(paths.toMutableList())
        }
        val poseStates = QuinticHermiteSpline.parameterize(paths).toMutableList()
        if (reversed) {
            poseStates.forEach {
                it.poseMeters = it.poseMeters.plus(kFlip)
                it.curvatureRadPerMeter *= -1
            }
        }
        trajectoryList.clear()
        trajectoryList.add(TrajectoryParameterizer.timeParameterizeTrajectory(poseStates,
                constraintHandlers.mapNotNull { it.constraint },
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