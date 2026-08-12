package dev.dmil.skye.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WeatherCacheDao {

    @Query("SELECT * FROM WeatherCacheEntity WHERE cacheKey = :cacheKey")
    suspend fun get(cacheKey: String): WeatherCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: WeatherCacheEntity)
}