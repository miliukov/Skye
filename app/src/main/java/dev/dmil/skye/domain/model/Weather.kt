package dev.dmil.skye.domain.model

data class Weather(
    val id: Int,
    val temperature: Double,
    val conditions: String,
    val description: String,
    val icon: String,
    val visibility: Int,
    val windSpeed: Double,
    val windDegree: Int,
    val windGust: Double,
    val clouds: Int,
    val date: Long,
    val timezone: Int,
    val city: String?,
    val feelsLike: Double,
    val pressure: Double,
    val humidity: Double
)
