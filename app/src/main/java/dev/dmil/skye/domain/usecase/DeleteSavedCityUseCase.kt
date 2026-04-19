package dev.dmil.skye.domain.usecase

import dev.dmil.skye.domain.model.SavedCity
import dev.dmil.skye.domain.repository.SavedCityRepository
import javax.inject.Inject

class DeleteSavedCityUseCase @Inject constructor(
    private val repository: SavedCityRepository
) {

    suspend operator fun invoke(city: SavedCity): Result<Unit> {
        return repository.deleteSavedCity(city)
    }

}