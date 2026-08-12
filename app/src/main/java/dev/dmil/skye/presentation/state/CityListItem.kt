package dev.dmil.skye.presentation.state

import dev.dmil.skye.domain.model.SavedCity

data class CityListItem(
    val name: String,
    val state: String? = null,
    val countryCode: String,
    val lat: Double,
    val lon: Double,
    val isCurrentLocation: Boolean,
    val savedCity: SavedCity? = null,
    val temperature: Int? = null,
    val icon: String? = null,
    val timezone: Int? = null,
    val description: String? = null,
    val isStale: Boolean = false
)