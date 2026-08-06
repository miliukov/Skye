package dev.dmil.skye.domain.model

data class SavedCity(
    val name: String,
    val state: String? = null,
    val countryCode: String,
    val lat: Double,
    val lon: Double,
    val tag: String? = null
)
