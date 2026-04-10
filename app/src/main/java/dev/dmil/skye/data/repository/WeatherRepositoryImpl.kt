package dev.dmil.skye.data.repository

import dev.dmil.skye.data.mapper.toWeather
import dev.dmil.skye.data.remote.WeatherApi
import dev.dmil.skye.domain.model.Weather
import dev.dmil.skye.domain.repository.WeatherRepository
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val api: WeatherApi
) : WeatherRepository {

    override suspend fun getWeatherForCity(city: String): Result<Weather> {
        return try {
            Result.success(api.getWeather(city).toWeather())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}