package dev.dmil.skye.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.dmil.skye.R
import dev.dmil.skye.domain.model.Units
import kotlin.math.roundToInt

fun formatTemperature(celsius: Double, units: Units): String {
    val value = if (units == Units.IMPERIAL) celsius * 9.0 / 5.0 + 32.0 else celsius
    return "${value.roundToInt()}º"
}

fun formatTemperature(celsius: Int, units: Units): String =
    formatTemperature(celsius.toDouble(), units)

@Composable
fun formatWindSpeed(metersPerSecond: Double, units: Units): String {
    return if (units == Units.IMPERIAL) {
        val mph = metersPerSecond * 2.23694
        "${mph.roundToInt()} mph"
    } else {
        val unit = stringResource(R.string.unit_meters_per_second)
        "${metersPerSecond.roundToInt()} $unit"
    }
}