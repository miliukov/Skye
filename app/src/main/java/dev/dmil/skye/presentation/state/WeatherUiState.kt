package dev.dmil.skye.presentation.state

import dev.dmil.skye.domain.model.DailyForecast
import dev.dmil.skye.domain.model.Weather

sealed class WeatherUiState {

    object Loading: WeatherUiState()

    data class Success(
        val weather: Weather,
        val forecast: List<Weather>,
        val weeklyForecast: List<DailyForecast>
    ): WeatherUiState()

    data class Refreshing(
        val weather: Weather,
        val forecast: List<Weather>,
        val weeklyForecast: List<DailyForecast>
    ): WeatherUiState()

    data class Error(val error: String): WeatherUiState()

}