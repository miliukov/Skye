package dev.dmil.skye.presentation.util

import dev.dmil.skye.domain.model.Units
import kotlin.math.roundToInt

fun formatTemperature(celsius: Double, units: Units): String {
    val value = if (units == Units.IMPERIAL) celsius * 9.0 / 5.0 + 32.0 else celsius
    return "${value.roundToInt()}º"
}

fun formatTemperature(celsius: Int, units: Units): String =
    formatTemperature(celsius.toDouble(), units)

fun formatWindSpeed(metersPerSecond: Double, units: Units): String {
    return if (units == Units.IMPERIAL) {
        val mph = metersPerSecond * 2.23694
        "${mph.roundToInt()} mph"
    } else {
        "${metersPerSecond.roundToInt()} m/s"
    }
}