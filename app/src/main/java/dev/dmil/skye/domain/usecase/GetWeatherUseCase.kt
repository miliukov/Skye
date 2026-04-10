package dev.dmil.skye.domain.usecase

import dev.dmil.skye.domain.model.Weather
import dev.dmil.skye.domain.repository.WeatherRepository
import javax.inject.Inject

class GetWeatherUseCase @Inject constructor(private val weatherRepository: WeatherRepository) {

    suspend operator fun invoke(city: String): Result<Weather> {
        return weatherRepository.getWeatherForCity(city)
    }

}