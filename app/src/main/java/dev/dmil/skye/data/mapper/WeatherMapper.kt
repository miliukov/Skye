package dev.dmil.skye.data.mapper

import dev.dmil.skye.data.dto.CurrentWeatherDto
import dev.dmil.skye.domain.model.Weather
import kotlin.math.round

fun CurrentWeatherDto.toWeather(timezone: Int = this.timezone): Weather {
    return Weather(
        id = this.weather.first().id,
        temperature = round(this.main.temp),
        conditions = this.weather.first().main,
        description = this.weather.first().description,
        icon = this.weather.first().icon,
        visibility = this.visibility,
        windSpeed = this.wind.speed,
        windDegree = this.wind.deg,
        windGust = this.wind.gust,
        clouds = this.clouds.all,
        date = this.dt,
        timezone = timezone,
        city = this.cityName,
        feelsLike = this.main.feelsLike,
        pressure = this.main.pressure,
        humidity = this.main.humidity
    )
}