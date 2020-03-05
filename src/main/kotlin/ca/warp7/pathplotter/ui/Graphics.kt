package ca.warp7.pathplotter.ui

import ca.warp7.pathplotter.remote.MeasuredState
import ca.warp7.pathplotter.state.PixelReference
import ca.warp7.pathplotter.util.*
import edu.wpi.first.wpilibj.geometry.Pose2d
import edu.wpi.first.wpilibj.geometry.Rotation2d
import edu.wpi.first.wpilibj.geometry.Translation2d
import edu.wpi.first.wpilibj.trajectory.Trajectory
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.shape.StrokeLineCap
import kotlin.math.abs

fun drawArrowForPose(ref: PixelReference, gc: GraphicsContext, point: Pose2d, mag: Double) {
    gc.lineWidth = 2.0

    val posOnCanvas = ref.transform(point.translation)

    val directionVector = point.rotation.translation()
    val arrowOffset = ref.transform(point.translation +
            directionVector.times(Constants.kArrowMultiplier * mag))

    // This is the offset from the base of the tip to the actual tip
    val r1 = directionVector.times(Constants.kArrowTipLength * Constants.k60DegreesRatio * 2)
    val r2 = r1.rotateBy(Rotation2d(0.0, 1.0)).times(Constants.k60DegreesRatio)
    val r3 = r1.rotateBy(Rotation2d(0.0, -1.0)).times(Constants.k60DegreesRatio)

    val ovalSize = ref.scale(Translation2d(Constants.kControlPointCircleSize, Constants.kControlPointCircleSize))
    val ovalWidth = abs(ovalSize.x)
    val ovalHeight = abs(ovalSize.y)
    gc.strokeOval(posOnCanvas.x - ovalWidth / 2.0,
            posOnCanvas.y - ovalHeight / 2.0, ovalWidth, ovalHeight)

    val start = posOnCanvas + ref.scale(directionVector.times(Constants.kControlPointCircleSize / 2.0))
    gc.lineTo(start, arrowOffset)

    val a1 = arrowOffset + ref.scale(r1)
    val a2 = arrowOffset + ref.scale(r2)
    val a3 = arrowOffset + ref.scale(r3)

    gc.lineCap = StrokeLineCap.ROUND
    gc.lineTo(a1, a2)
    gc.lineTo(a2, a3)
    gc.lineTo(a3, a1)
}

fun drawCoordinateFrame(ref: PixelReference, gc: GraphicsContext, point: Pose2d) {
    gc.lineWidth = 1.0

    val posOnCanvas = ref.transform(point.translation)
    val xDir = ref.scale(point.rotation.translation())
    val yDir = ref.scale(point.rotation.normal().translation())

    gc.stroke = Color.MAGENTA
    gc.lineTo(posOnCanvas.minus(xDir.times(5.0)), posOnCanvas.plus(xDir.times(5.0)))
    gc.lineTo(posOnCanvas.minus(yDir.times(5.0)), posOnCanvas.plus(yDir.times(5.0)))
}

fun drawSplines(
        ref: PixelReference,
        trajectory: Trajectory,
        odd: Boolean,
        gc: GraphicsContext,
        robotHalfWidth: Double,
        robotHalfLength: Double,
        reversed: Boolean
) {

    val reverse = (!reversed).toIntSign()
    val maxCurvature = trajectory.states.map { abs(it.curvatureRadPerMeter) }.max()!!

    gc.lineWidth = 1.5

    val s0 = trajectory.states.first()
    val t0 = s0.poseMeters.translation
    var normal = s0.poseMeters.rotation.normal().translation() * robotHalfWidth
    var left = ref.transform(t0 - normal)
    var right = ref.transform(t0 + normal)

    if (s0.curvatureRadPerMeter.isFinite()) {
        if (odd) {
            gc.stroke = Color.rgb(0, 128, 255)
        } else {
            gc.stroke = Color.rgb(0, 255, 0)
        }
        val a0 = ref.transform(t0) - ref.scale(Translation2d(robotHalfLength * reverse,
                robotHalfWidth).rotateBy(s0.poseMeters.rotation))
        val b0 = ref.transform(t0) + ref.scale(Translation2d(-robotHalfLength * reverse,
                robotHalfWidth).rotateBy(s0.poseMeters.rotation))
        gc.lineTo(a0, b0)
        gc.lineTo(left, a0)
        gc.lineTo(right, b0)
    }

    for (i in 1 until trajectory.states.size) {
        val s = trajectory.states[i]
        val t = s.poseMeters.translation
        normal = s.poseMeters.rotation.normal().translation() * robotHalfWidth
        val newLeft = ref.transform(t - normal)
        val newRight = ref.transform(t + normal)

        if (s.curvatureRadPerMeter.isFinite()) {
            val kx = abs(s.curvatureRadPerMeter) / maxCurvature
            if (odd) {
                val r = linearInterpolate(0.0, 192.0, kx) + 63
                val b = 255 - linearInterpolate(0.0, 192.0, kx)
                gc.stroke = Color.rgb(r.toInt(), 128, b.toInt())
            } else {
                val r = linearInterpolate(0.0, 192.0, kx) + 63
                val g = 255 - linearInterpolate(0.0, 192.0, kx)
                gc.stroke = Color.rgb(r.toInt(), g.toInt(), 0)
            }
        } else {
            gc.stroke = Color.MAGENTA
        }

        gc.lineTo(left, newLeft)
        gc.lineTo(right, newRight)
        left = newLeft
        right = newRight
    }

    val s1 = trajectory.states.last()

    if (s1.curvatureRadPerMeter.isFinite()) {
        val t1 = s1.poseMeters.translation
        if (odd) {
            gc.stroke = Color.rgb(0, 128, 255)
        } else {
            gc.stroke = Color.rgb(0, 255, 0)
        }
        val a1 = ref.transform(t1) - ref.scale(Translation2d(-robotHalfLength * reverse,
                robotHalfWidth).rotateBy(s1.poseMeters.rotation))
        val b1 = ref.transform(t1) + ref.scale(Translation2d(robotHalfLength * reverse,
                robotHalfWidth).rotateBy(s1.poseMeters.rotation))
        gc.lineTo(a1, b1)
        gc.lineTo(left, a1)
        gc.lineTo(right, b1)
    }
}

fun drawRuler(ref: PixelReference, gc: GraphicsContext, a: Translation2d, b: Translation2d) {
    val lw = ref.scale(Translation2d(0.2, 0.0)).x
    gc.lineWidth = lw
    gc.lineCap = StrokeLineCap.SQUARE
    gc.stroke = Color.YELLOW

    val at = ref.transform(a)
    val bt = ref.transform(b)
    gc.lineTo(at, bt)
    val diff = b.minus(a)
    val mag = diff.norm
    val norm = diff.div(mag)
    val normal4 = Translation2d(norm.y * lw * 0.5, norm.x * lw * 0.5)
    val normal2 = Translation2d(norm.y * lw * 0.3, norm.x * lw * 0.3)

    gc.stroke = Color.GRAY
    gc.lineWidth = 1.0
    var x = 0.0
    while (x < mag) {
        val k = at + ref.scale(norm.times(x))
        gc.lineTo(k.minus(normal2), k.plus(normal4))
        x += 0.1
    }

    gc.stroke = Color.BLACK
    gc.lineWidth = 2.0
    x = 0.0
    while (x < mag) {
        val k = at + ref.scale(norm.times(x))
        gc.lineTo(k.minus(normal2), k.plus(normal4))
        x += 1.0
    }

    gc.fill = Color.LIME
    val tt = ref.transform(Translation2d(0.8, -3.7))
    gc.fillText("${mag.f2}m  [${norm.direction().degrees.f1}°]\n(${diff.x.f2}m, ${diff.y.f2}m)", tt.x, tt.y)
}

fun drawMeasuredStates(
        ref: PixelReference,
        states: List<MeasuredState>,
        gc: GraphicsContext,
        robotWidth: Double,
        robotLength: Double
) {

    gc.lineWidth = 3.0

    val s0 = states.first()
    val t0 = s0.robotState.translation
    var normal = s0.robotState.rotation.normal().translation() * robotWidth
    var left = ref.transform(t0 - normal)
    var right = ref.transform(t0 + normal)

    gc.stroke = Color.WHITE
    gc.fill = Color.WHITE

    val a0 = ref.transform(t0) - ref.scale(Translation2d(robotLength,
            robotWidth).rotateBy(s0.robotState.rotation))
    val b0 = ref.transform(t0) + ref.scale(Translation2d(-robotLength,
            robotWidth).rotateBy(s0.robotState.rotation))
    gc.lineTo(a0, b0)
    gc.lineTo(left, a0)
    gc.lineTo(right, b0)

    for (i in 1 until states.size) {
        val s = states[i]
        val t = s.robotState.translation
        normal = s.robotState.rotation.normal().translation() * robotWidth
        val newLeft = ref.transform(t - normal)
        val newRight = ref.transform(t + normal)


        gc.fillOval(newLeft.x - 1.5, newLeft.y - 1.5, 3.0, 3.0)
        gc.fillOval(newRight.x - 1.5, newRight.y - 1.5, 3.0, 3.0)

        left = newLeft
        right = newRight
    }

    val s1 = states.last()
    val t1 = s1.robotState.translation
    val a1 = ref.transform(t1) - ref.scale(Translation2d(-robotLength,
            robotWidth).rotateBy(s1.robotState.rotation))
    val b1 = ref.transform(t1) + ref.scale(Translation2d(robotLength,
            robotWidth).rotateBy(s1.robotState.rotation))
    gc.lineTo(a1, b1)
    gc.lineTo(left, a1)
    gc.lineTo(right, b1)

}

fun drawRobot(
        ref: PixelReference,
        gc: GraphicsContext,
        robotHalfWidth: Double,
        robotHalfLength: Double,
        point: Pose2d
) {
    val pos = ref.transform(point.translation)
    val heading = point.rotation
    val a = ref.scale(Translation2d(robotHalfLength, robotHalfWidth).rotateBy(heading))
    val b = ref.scale(Translation2d(robotHalfLength, -robotHalfWidth).rotateBy(heading))
    val p1 = pos + a
    val p2 = pos + b
    val p3 = pos - a
    val p4 = pos - b
//    gc.stroke = Color.rgb(60, 92, 148)
    gc.lineWidth = 2.0
    gc.stroke = Color.WHITE
    gc.fill = Color.rgb(90, 138, 222)
    gc.beginPath()
    gc.vertex(p1)
    gc.vertex(p2)
    gc.vertex(p3)
    gc.vertex(p4)
    gc.vertex(p1)
    gc.closePath()
    gc.stroke()
    gc.fill()
}

fun GraphicsContext.lineTo(a: Translation2d, b: Translation2d) {
    strokeLine(a.x, a.y, b.x, b.y)
}

fun GraphicsContext.vertex(a: Translation2d) {
    lineTo(a.x, a.y)
}