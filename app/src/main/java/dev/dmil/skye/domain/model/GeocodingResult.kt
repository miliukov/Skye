package dev.dmil.skye.domain.model

data class GeocodingResult(
    val city: String,
    val state: String? = null,
    val countryCode: String,
    val lat: Double,
    val lon: Double
)
