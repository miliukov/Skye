package dev.dmil.skye.domain.usecase

import dev.dmil.skye.domain.model.SavedCity
import dev.dmil.skye.domain.repository.SavedCityRepository
import javax.inject.Inject

class AddSavedCityUseCase @Inject constructor(
    private val repository: SavedCityRepository
) {

    suspend operator fun invoke(city: SavedCity): Result<Unit> {
        return repository.addSavedCity(city)
    }

}