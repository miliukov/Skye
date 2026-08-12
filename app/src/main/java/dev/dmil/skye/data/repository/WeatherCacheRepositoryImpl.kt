package dev.dmil.skye.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.dmil.skye.data.local.WeatherCacheDao
import dev.dmil.skye.data.local.WeatherCacheEntity
import dev.dmil.skye.domain.model.CachedWeather
import dev.dmil.skye.domain.model.Weather
import dev.dmil.skye.domain.repository.WeatherCacheRepository
import javax.inject.Inject

class WeatherCacheRepositoryImpl @Inject constructor(
    private val dao: WeatherCacheDao
) : WeatherCacheRepository {

    private val gson = Gson()
    private val forecastType = object : TypeToken<List<Weather>>() {}.type

    override suspend fun get(lat: Double, lon: Double): CachedWeather? {
        val entity = dao.get(cacheKey(lat, lon)) ?: return null
        return runCatching {
            CachedWeather(
                weather = gson.fromJson(entity.weatherJson, Weather::class.java),
                forecast = gson.fromJson(entity.forecastJson, forecastType),
                fetchedAt = entity.fetchedAt
            )
        }.getOrNull()
    }

    override suspend fun save(lat: Double, lon: Double, weather: Weather, forecast: List<Weather>) {
        dao.upsert(
            WeatherCacheEntity(
                cacheKey = cacheKey(lat, lon),
                weatherJson = gson.toJson(weather),
                forecastJson = gson.toJson(forecast),
                fetchedAt = System.currentTimeMillis()
            )
        )
    }

    private fun cacheKey(lat: Double, lon: Double) = "${lat}_${lon}"
}