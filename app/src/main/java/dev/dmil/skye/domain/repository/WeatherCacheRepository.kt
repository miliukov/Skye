package dev.dmil.skye.domain.repository

import dev.dmil.skye.domain.model.CachedWeather
import dev.dmil.skye.domain.model.Weather

interface WeatherCacheRepository {
    suspend fun get(lat: Double, lon: Double): CachedWeather?
    suspend fun save(lat: Double, lon: Double, weather: Weather, forecast: List<Weather>)
}