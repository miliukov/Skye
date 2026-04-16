package dev.dmil.skye.data.repository

import dev.dmil.skye.data.mapper.toGeocodingResult
import dev.dmil.skye.data.mapper.toWeather
import dev.dmil.skye.data.remote.WeatherApi
import dev.dmil.skye.domain.model.GeocodingResult
import dev.dmil.skye.domain.model.Weather
import dev.dmil.skye.domain.repository.WeatherRepository
import java.util.Locale
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val api: WeatherApi
) : WeatherRepository {

    override suspend fun getWeatherForCoordinates(lat: Double, lon: Double): Result<Weather> {
        return try {
            Result.success(api.getWeather(
                lat = lat,
                lon = lon,
                lang = Locale.getDefault().language,
                units = "metric"
            ).toWeather())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLocationByName(query: String): Result<List<GeocodingResult>> {
        return try {
            Result.success(api.searchCity(query).map { it.toGeocodingResult() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}