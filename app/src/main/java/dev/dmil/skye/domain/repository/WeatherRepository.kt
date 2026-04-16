package dev.dmil.skye.domain.repository

import dev.dmil.skye.domain.model.GeocodingResult
import dev.dmil.skye.domain.model.Weather

interface WeatherRepository {

    suspend fun getWeatherForCoordinates(
        lat: Double,
        lon: Double
    ): Result<Weather>

    suspend fun getLocationByName(query: String): Result<List<GeocodingResult>>

}