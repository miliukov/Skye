package dev.dmil.skye.domain.model

data class DailyForecast(
    val dayLabel: String,
    val icon: String,
    val minTemp: Int,
    val maxTemp: Int,
    val windSpeed: Double,
    val windDegree: Int
)