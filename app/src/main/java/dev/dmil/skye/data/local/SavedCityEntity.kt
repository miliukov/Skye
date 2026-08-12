package dev.dmil.skye.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["lat", "lon"], unique = true)])
data class SavedCityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val state: String? = null,
    @ColumnInfo(name = "country_code")
    val countryCode: String,
    val lat: Double,
    val lon: Double,
    val tag: String? = null
)