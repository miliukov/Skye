package dev.dmil.skye.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SavedCityEntity::class, WeatherCacheEntity::class], version = 1, exportSchema = true)
abstract class SkyeDatabase: RoomDatabase() {

    abstract fun savedCityDao(): SavedCityDao
    abstract fun weatherCacheDao(): WeatherCacheDao

}