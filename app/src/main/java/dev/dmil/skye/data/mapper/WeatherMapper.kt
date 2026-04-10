package dev.dmil.skye.data.mapper

import dev.dmil.skye.data.dto.CurrentWeatherDto
import dev.dmil.skye.domain.model.Weather

fun CurrentWeatherDto.toWeather(): Weather {
    return Weather(
        temperature = this.main.temp,
        conditions = this.weather.first().main,
        description = this.weather.first().description,
        icon = this.weather.first().icon,
        visibility = this.visibility,
        windSpeed = this.wind.speed,
        windDegree = this.wind.deg,
        windGust = this.wind.gust,
        clouds = this.clouds.all,
        date = this.dt,
        timezone = this.timezone,
        city = this.cityName
    )
}