package dev.dmil.skye.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class WeatherCacheEntity(
    @PrimaryKey
    val cacheKey: String,
    val weatherJson: String,
    val forecastJson: String,
    val fetchedAt: Long
)