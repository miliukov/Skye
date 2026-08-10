package dev.dmil.skye.data.repository

import dev.dmil.skye.BuildConfig
import dev.dmil.skye.data.mapper.toGeocodingResult
import dev.dmil.skye.data.mapper.toWeather
import dev.dmil.skye.data.remote.WeatherApi
import dev.dmil.skye.domain.model.GeocodingResult
import dev.dmil.skye.domain.model.Weather
import dev.dmil.skye.domain.repository.SettingsRepository
import dev.dmil.skye.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.first
import java.util.Locale
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val api: WeatherApi,
    private val settingsRepository: SettingsRepository
) : WeatherRepository {

    private suspend fun activeApiKey(): String {
        return settingsRepository.apiKey.first() ?: BuildConfig.WEATHER_API_KEY
    }

    override suspend fun getWeatherForCoordinates(lat: Double, lon: Double): Result<Weather> {
        return try {
            Result.success(api.getWeather(
                lat = lat,
                lon = lon,
                lang = Locale.getDefault().language,
                units = "metric",
                apiKey = activeApiKey()
            ).toWeather())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getForecastForCoordinates(lat: Double, lon: Double): Result<List<Weather>> {
        return try {
            val response = api.getHourlyForecast(
                lat = lat,
                lon = lon,
                lang = Locale.getDefault().language,
                units = "metric",
                apiKey = activeApiKey()
            )
            Result.success(response.list.map { it.toWeather(timezone = response.city.timezone) })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLocationByName(query: String): Result<List<GeocodingResult>> {
        return try {
            Result.success(api.searchCity(query, apiKey = activeApiKey()).map { it.toGeocodingResult() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun testApiKey(key: String, lat: Double, lon: Double): Result<Unit> {
        return try {
            api.getWeather(
                lat = lat,
                lon = lon,
                lang = Locale.getDefault().language,
                units = "metric",
                apiKey = key
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}