package dev.dmil.skye.domain.repository

import dev.dmil.skye.domain.model.Weather

interface WeatherRepository {

    suspend fun getWeatherForCity(city: String): Result<Weather>

}