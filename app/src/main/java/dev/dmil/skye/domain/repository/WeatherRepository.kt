package dev.dmil.skye.domain.repository

import dev.dmil.skye.domain.model.GeocodingResult
import dev.dmil.skye.domain.model.Weather

interface WeatherRepository {

    suspend fun getWeatherForCoordinates(
        lat: Double,
        lon: Double
    ): Result<Weather>

    suspend fun getForecastForCoordinates(
        lat: Double,
        lon: Double
    ): Result<List<Weather>>

    suspend fun getLocationByName(query: String): Result<List<GeocodingResult>>

    suspend fun testApiKey(key: String, lat: Double, lon: Double): Result<Unit>

}