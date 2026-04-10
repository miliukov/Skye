package dev.dmil.skye.data.dto

import com.google.gson.annotations.SerializedName

data class CurrentWeatherDto(
    val weather: List<WeatherDto>,
    val main: MainDto,
    val visibility: Int,
    val wind: WindDto,
    val clouds: CloudsDto,
    val dt: Long,
    val timezone: Int,
    @SerializedName("id")
    val cityId: Int,
    val name: String
)