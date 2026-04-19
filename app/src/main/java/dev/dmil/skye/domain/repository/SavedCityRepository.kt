package dev.dmil.skye.domain.repository

import dev.dmil.skye.domain.model.SavedCity


interface SavedCityRepository {

    suspend fun addSavedCity(city: SavedCity): Result<Unit>

    suspend fun deleteSavedCity(city: SavedCity): Result<Unit>

    suspend fun getSavedCities(): Result<List<SavedCity>>

}