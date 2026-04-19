package dev.dmil.skye.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SavedCityDao {

    @Insert
    suspend fun addSavedCity(city: SavedCityEntity)

    @Query("SELECT * FROM savedcityentity")
    suspend fun getSavedCities(): List<SavedCityEntity>

    @Delete
    suspend fun deleteSavedCity(city: SavedCityEntity)

}