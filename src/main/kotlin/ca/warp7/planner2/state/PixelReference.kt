package ca.warp7.planner2.state

import edu.wpi.first.wpilibj.geometry.Rotation2d
import edu.wpi.first.wpilibj.geometry.Translation2d

/**
 * Defines functions to scale translations
 * to pixel sizes on the screen
 *
 * In JavaFX, x is to the right, y is down, origin at top-left
 *
 * In Path Planner coordinates, x is up, y is left,
 * origin is at the bottom-centre of the image
 *
 * The border is just extra. No calculations involved
 */
class PixelReference {

    private var pixelPerMetreWidth = 0.0
    private var pixelPerMetreHeight = 0.0

    private var originX = 0.0
    private var originY = 0.0

    fun set(
            width: Double,
            height: Double,
            offsetX: Double,
            offsetY: Double,
            fieldWidth: Double,
            fieldHeight: Double
    ) {
        pixelPerMetreWidth = width / fieldWidth
        pixelPerMetreHeight = height / fieldHeight

        originX = offsetX
        originY = offsetY + height / 2.0
    }

    fun scale(point: Translation2d) = Translation2d(
            pixelPerMetreWidth * point.x,
            -pixelPerMetreHeight * point.y
    )

    fun inverseScale(point: Translation2d) = Translation2d(
            -point.x / pixelPerMetreWidth,
            point.y / pixelPerMetreHeight
    )

    fun transform(point: Translation2d) = Translation2d(
            originX + pixelPerMetreWidth * point.x,
            originY - pixelPerMetreHeight * point.y
    )

    fun inverseTransform(point: Translation2d) = Translation2d(
            (point.x - originX) / pixelPerMetreWidth,
            -(point.y - originY) / pixelPerMetreHeight
    )
}