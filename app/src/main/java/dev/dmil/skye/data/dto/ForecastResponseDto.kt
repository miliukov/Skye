package dev.dmil.skye.data.dto

data class ForecastResponseDto(
    val list: List<CurrentWeatherDto>,
    val city: ForecastCityDto
)
