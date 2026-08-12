package dev.dmil.skye.domain.model

data class CachedWeather(
    val weather: Weather,
    val forecast: List<Weather>,
    val fetchedAt: Long
)