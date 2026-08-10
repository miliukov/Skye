package dev.dmil.skye.domain.model

import java.time.LocalDate

data class DailyForecast(
    val date: LocalDate,
    val dayLabel: String,
    val icon: String,
    val minTemp: Int,
    val maxTemp: Int,
    val windSpeed: Double,
    val windDegree: Int,
    val feelsLike: Int,
    val humidity: Int,
    val pressure: Int,
    val description: String,
    val hourly: List<Weather>
)