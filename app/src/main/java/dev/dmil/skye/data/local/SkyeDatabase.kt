package dev.dmil.skye.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SavedCityEntity::class], version = 1, exportSchema = false)
abstract class SkyeDatabase: RoomDatabase() {

    abstract fun savedCityDao(): SavedCityDao

}