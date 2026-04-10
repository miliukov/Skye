package dev.dmil.skye.data.remote

import dev.dmil.skye.BuildConfig
import dev.dmil.skye.data.dto.CurrentWeatherDto
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    @GET("data/2.5/weather")
    suspend fun getWeather(
        @Query("q") q: String,
        @Query("appid") apiKey: String = BuildConfig.WEATHER_API_KEY
    ): CurrentWeatherDto

}