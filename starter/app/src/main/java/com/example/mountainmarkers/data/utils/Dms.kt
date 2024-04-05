package com.example.mountainmarkers.data.utils

enum class Direction(val sign: Int) {
    NORTH(1),
    EAST(1),
    SOUTH(-1),
    WEST(-1)
}

/**
 * Degrees, minutes, seconds utility class
 */
data class DMS(
    val direction: Direction,
    val degrees: Double,
    val minutes: Double = 0.0,
    val seconds: Double = 0.0,
)

fun DMS.toDecimalDegrees(): Double =
    (degrees + (minutes / 60) + (seconds / 3600)) * direction.sign