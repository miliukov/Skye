package dev.dmil.skye.domain.usecase

import dev.dmil.skye.domain.model.WeatherResult
import dev.dmil.skye.domain.repository.WeatherCacheRepository
import dev.dmil.skye.domain.repository.WeatherRepository
import javax.inject.Inject

class GetWeatherWithCacheUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val forecastRepository: WeatherRepository,
    private val cacheRepository: WeatherCacheRepository
) {
    companion object {
        const val CACHE_TTL_MILLIS = 5 * 60 * 1000L
    }

    suspend operator fun invoke(lat: Double, lon: Double): Result<WeatherResult> {
        val cached = cacheRepository.get(lat, lon)
        val cacheAge = cached?.let { System.currentTimeMillis() - it.fetchedAt }

        if (cached != null && cacheAge != null && cacheAge < CACHE_TTL_MILLIS) {
            return Result.success(WeatherResult(cached.weather, cached.forecast, isStale = false))
        }

        val weatherResult = weatherRepository.getWeatherForCoordinates(lat, lon)
        val forecastResult = forecastRepository.getForecastForCoordinates(lat, lon)

        return if (weatherResult.isSuccess && forecastResult.isSuccess) {
            val weather = weatherResult.getOrNull()!!
            val forecast = forecastResult.getOrNull()!!
            cacheRepository.save(lat, lon, weather, forecast)
            Result.success(WeatherResult(weather, forecast, isStale = false))
        } else if (cached != null) {
            Result.success(WeatherResult(cached.weather, cached.forecast, isStale = true))
        } else {
            Result.failure(weatherResult.exceptionOrNull() ?: forecastResult.exceptionOrNull()!!)
        }
    }
}