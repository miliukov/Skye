package dev.dmil.skye.presentation.state

import dev.dmil.skye.domain.model.Weather

sealed class WeatherUiState {

    object Loading: WeatherUiState()

    data class Success(val weather: Weather): WeatherUiState()

    data class Error(val error: String): WeatherUiState()

}