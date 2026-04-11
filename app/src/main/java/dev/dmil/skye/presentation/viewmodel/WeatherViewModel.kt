package dev.dmil.skye.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.dmil.skye.domain.usecase.GetWeatherUseCase
import dev.dmil.skye.presentation.state.WeatherUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val getWeatherUseCase: GetWeatherUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow<String>("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchError = MutableStateFlow("")
    val searchError = _searchError.asStateFlow()

    init {
        getWeather("London")
    }

    fun onSearchQueryChange(newValue: String) {
        _searchQuery.value = newValue
    }

    fun onSearch() {
        getWeather(_searchQuery.value)
    }

    fun getWeather(city: String) {
        if (_uiState.value is WeatherUiState.Success) {
            _uiState.value = WeatherUiState.Refreshing(weather = (_uiState.value as WeatherUiState.Success).weather)
        } else _uiState.value = WeatherUiState.Loading

        viewModelScope.launch {
            getWeatherUseCase(city).fold(
                onSuccess = { weather ->
                    _uiState.value = WeatherUiState.Success(weather)
                    _searchError.value = ""
                },
                onFailure = { e ->
                    if (_uiState.value is WeatherUiState.Refreshing) {
                        _searchError.value = "Ошибка в названии города"
                        val weather = (_uiState.value as WeatherUiState.Refreshing).weather
                        _uiState.value = WeatherUiState.Success(weather)
                        Log.e("WeatherViewModel.getWeather", e.message ?: "Error")
                        return@fold
                    }
                    Log.e("WeatherViewModel.getWeather", e.message ?: "Unknown error")
                    when(e) {
                        is HttpException -> {
                            _uiState.value = WeatherUiState.Error("Ошибка сервера")
                        }
                        is IOException -> {
                            _uiState.value = WeatherUiState.Error("Отсутствует подключение к интернету")
                        }
                        else -> {
                            _uiState.value = WeatherUiState.Error("Ошибка. Попробуйте позднее")
                        }
                    }
                }
            )
        }
    }

}