package dev.dmil.skye.presentation.state

sealed class WeatherError {
    object LocationPermissionDenied : WeatherError()
    object LocationUnavailable : WeatherError()
    object ServerError : WeatherError()
    object NoInternet : WeatherError()
    object InvalidCityName : WeatherError()
    object Unknown : WeatherError()
}