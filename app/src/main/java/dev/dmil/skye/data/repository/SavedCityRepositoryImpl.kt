package dev.dmil.skye.data.repository

import dev.dmil.skye.data.local.SavedCityDao
import dev.dmil.skye.data.mapper.toSavedCity
import dev.dmil.skye.data.mapper.toSavedCityEntity
import dev.dmil.skye.domain.model.SavedCity
import dev.dmil.skye.domain.repository.SavedCityRepository
import javax.inject.Inject

class SavedCityRepositoryImpl @Inject constructor(
    private val dao: SavedCityDao
): SavedCityRepository {

    override suspend fun addSavedCity(city: SavedCity): Result<Unit> {
        return try {
            Result.success(dao.addSavedCity(city.toSavedCityEntity()))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteSavedCity(city: SavedCity): Result<Unit> {
        return try {
            Result.success(dao.deleteSavedCity(city.toSavedCityEntity()))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSavedCities(): Result<List<SavedCity>> {
        return try {
            Result.success(dao.getSavedCities().map { it.toSavedCity() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}