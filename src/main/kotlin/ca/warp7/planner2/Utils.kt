package ca.warp7.planner2

import ca.warp7.planner2.constraint.*
import edu.wpi.first.wpilibj.geometry.Rotation2d
import edu.wpi.first.wpilibj.geometry.Translation2d
import javafx.scene.canvas.GraphicsContext


fun GraphicsContext.lineTo(a: Translation2d, b: Translation2d) = strokeLine(a.x, a.y, b.x, b.y)
fun GraphicsContext.vertex(a: Translation2d) = lineTo(a.x, a.y)


fun snap(t: Translation2d): Translation2d {
    return Translation2d((t.x * 1000).toInt() / 1000.0, (t.y * 1000).toInt() / 1000.0)
}

val constraintHandlers = listOf(
        DifferentialDriveKinematicsHandler(),
        DifferentialDriveVoltageHandler(),
        CentripetalAccelerationHandler(),
        MecanumKinematicsHandler(),
        SwerveKinematicsHandler()
)

val Number.degrees: Rotation2d get() = Rotation2d.fromDegrees(this.toDouble())

fun Rotation2d.translation() = Translation2d(cos, sin)

fun Translation2d.direction() = Rotation2d(x, y)

fun Rotation2d.normal() = Rotation2d(-sin, cos)
