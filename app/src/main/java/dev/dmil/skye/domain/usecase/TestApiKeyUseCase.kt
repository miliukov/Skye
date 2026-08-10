package dev.dmil.skye.domain.usecase

import dev.dmil.skye.domain.repository.WeatherRepository
import javax.inject.Inject

class TestApiKeyUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository
) {
    suspend operator fun invoke(key: String, lat: Double, lon: Double) =
        weatherRepository.testApiKey(key, lat, lon)
}