package dev.dmil.skye.data.remote

import dev.dmil.skye.BuildConfig
import dev.dmil.skye.data.dto.CurrentWeatherDto
import dev.dmil.skye.data.dto.GeocodingDto
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    @GET("data/2.5/weather")
    suspend fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String = BuildConfig.WEATHER_API_KEY
    ): CurrentWeatherDto

    @GET("geo/1.0/direct")
    suspend fun searchCity(
        @Query("q") q: String,
        @Query("limit") limit: Int = 5,
        @Query("appid") apiKey: String = BuildConfig.WEATHER_API_KEY
    ): List<GeocodingDto>

}