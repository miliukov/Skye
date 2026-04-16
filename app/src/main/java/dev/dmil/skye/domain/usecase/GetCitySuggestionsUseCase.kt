package dev.dmil.skye.domain.usecase

import dev.dmil.skye.domain.model.GeocodingResult
import dev.dmil.skye.domain.repository.WeatherRepository
import javax.inject.Inject

class GetCitySuggestionsUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository
) {
    suspend operator fun invoke(query: String): Result<List<GeocodingResult>> {
        return weatherRepository.getLocationByName(query)
    }
}