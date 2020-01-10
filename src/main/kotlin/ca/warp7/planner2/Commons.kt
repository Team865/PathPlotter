@file:JvmName("Util")

package ca.warp7.planner2

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sign
import kotlin.math.sin

const val kFeetToMetres: Double = 0.3048

const val kInchesToMetres: Double = 0.0254

const val kMetresToFeet: Double = 1 / kFeetToMetres

const val kMetresToInches: Double = 1 / kInchesToMetres

/**
 * Check if [this] is close enough to [other] by [epsilon]
 * @return true if numbers are close
 */
fun Double.epsilonEquals(other: Double, epsilon: Double): Boolean {
    return (this - epsilon <= other) && (this + epsilon >= other)
}

/**
 * Check if a number is close enough to another by 1E-12
 * @return true if numbers are close
 */
fun Double.epsilonEquals(other: Double): Boolean {
    return epsilonEquals(other, 1E-12)
}

/**
 * Interpolate between two numbers
 *
 * This function is undefined if any of its parameters are
 * Infinity or NaN
 *
 * @param a the lower bound
 * @param b the upper bound, b >= a
 * @param x the amount to interpolate between 0 and 1
 *
 * @return the interpolation result
 */
fun linearInterpolate(a: Double, b: Double, x: Double): Double {
    if (x < 0.0) {
        return a
    }
    if (x > 1.0) {
        return b
    }
    return a + (b - a) * x
}


/**
 * Limit a value within a magnitude range
 *
 * @param max the maximum magnitude of the value. Must be positive
 * @return the value within the limit
 */
fun Double.limit(max: Double): Double {
    if (this > max) {
        return max
    }
    if (this < -max) {
        return -max
    }
    return this
}


/**
 * Apply a deadband to a value and rescales the result
 *
 * @param value the value to apply the deadband to
 * @param max the maximum magnitude of the value. Must be positive
 * @param deadband the range in which [value] is rounded to 0.
 * Must be positive and less than [max]
 *
 * @return the value within the deadband and max
 */
fun applyDeadband(value: Double, max: Double, deadband: Double): Double {
    val v = value.limit(max)
    return if (abs(v) > deadband) {
        if (v > 0.0) {
            (v - deadband) / (max - deadband)
        } else {
            (v + deadband) / (max - deadband)
        }
    } else {
        0.0
    }
}

/**
 * Steps an accumulator towards zero
 *
 * @param accumulator the current accumulator
 * @return the next accumulator
 */
fun wrapAccumulator(accumulator: Double): Double {
    return when {
        accumulator > 1 -> accumulator - 1.0
        accumulator < -1 -> accumulator + 1.0
        else -> 0.0
    }
}

/**
 * Rescales a value iteratively using a sign function
 *
 * @param v the value to scale
 * @param nonLinearity How much to deviate from a linear result
 * @param passes number of iterations to scale the number
 * @return the scaled number
 */
fun sinScale(v: Double, nonLinearity: Double, passes: Int): Double {
    var r = v
    repeat(passes) {
        r = sin(PI / 2 * nonLinearity * r) / sin(PI / 2 * nonLinearity)
    }
    return r
}

/**
 * Format a number to 3 decimal places
 */
val Double.f get() = String.format("%.3f", this)

/**
 * Format a number to 1 decimal places
 */
val Double.f1 get() = String.format("%.1f", this)

/**
 * Format a number to 2 decimal places
 */
val Double.f2 get() = String.format("%.2f", this)

/**
 * Convert a number in feet into metres
 */
val Number.feet: Double get() = this.toDouble() * kFeetToMetres

/**
 * Convert a number in inches into metres
 */
val Number.inches: Double get() = this.toDouble() * kInchesToMetres

/**
 * Square a number
 */
val Double.squared: Double get() = this * this

/**
 * Cube a number
 */
val Double.cubed: Double get() = this * this * this

/**
 * Square a number and keep the sign
 */
val Double.squaredWithSign: Double get() = this * this * sign

/**
 * Create a double sign representation of a boolean
 */
fun Boolean.toDoubleSign(): Double {
    return if (this) 1.0 else -1.0
}

/**
 * Create an integer sign representation of a boolean
 */
fun Boolean.toIntSign(): Int {
    return if (this) 1 else -1
}

/**
 * Create a double representation of a boolean
 */
fun Boolean.toDouble(): Double {
    return if (this) 1.0 else 0.0
}

/**
 * Create an integer representation of a boolean
 */
fun Boolean.toInt(): Int {
    return if (this) 1 else 0
}