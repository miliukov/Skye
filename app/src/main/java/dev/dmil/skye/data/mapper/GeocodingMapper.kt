package dev.dmil.skye.data.mapper

import dev.dmil.skye.data.dto.GeocodingDto
import dev.dmil.skye.domain.model.GeocodingResult

fun GeocodingDto.toGeocodingResult(): GeocodingResult {
    return GeocodingResult(
        city = this.city,
        state = this.state,
        countryCode = this.countryCode,
        lat = this.lat,
        lon = this.lon,
    )
}