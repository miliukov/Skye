package dev.dmil.skye.domain.usecase

import dev.dmil.skye.domain.model.SavedCity
import dev.dmil.skye.domain.repository.SavedCityRepository
import javax.inject.Inject

class GetSavedCityUseCase @Inject constructor(
    private val repository: SavedCityRepository
) {

    suspend operator fun invoke(): Result<List<SavedCity>> {
        return repository.getSavedCities()
    }

}