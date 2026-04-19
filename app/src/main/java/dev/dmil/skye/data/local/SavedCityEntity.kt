package dev.dmil.skye.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SavedCityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val state: String? = null,
    @ColumnInfo(name = "country_code")
    val countryCode: String,
    val tag: String? = null
)