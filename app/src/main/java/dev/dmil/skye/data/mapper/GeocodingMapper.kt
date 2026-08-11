package dev.dmil.skye.data.mapper

import dev.dmil.skye.data.dto.GeocodingDto
import dev.dmil.skye.domain.model.GeocodingResult

fun GeocodingDto.toGeocodingResult(): GeocodingResult {
    val language = java.util.Locale.getDefault().language
    val localizedName = this.localNames?.get(language) ?: this.city
    return GeocodingResult(
        city = localizedName,
        state = this.state,
        countryCode = this.countryCode,
        lat = this.lat,
        lon = this.lon,
    )
}