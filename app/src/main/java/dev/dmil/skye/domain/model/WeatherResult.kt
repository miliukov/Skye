package dev.dmil.skye.domain.model

data class WeatherResult(
    val weather: Weather,
    val forecast: List<Weather>,
    val isStale: Boolean
)