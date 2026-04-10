package dev.dmil.skye.presentation.viewmodel

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

    fun getWeather(city: String) {
        _uiState.value = WeatherUiState.Loading
        viewModelScope.launch {
            getWeatherUseCase(city).fold(
                onSuccess = { weather ->
                    _uiState.value = WeatherUiState.Success(weather)
                },
                onFailure = { e ->
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