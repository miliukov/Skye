package dev.dmil.skye.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.dmil.skye.domain.model.GeocodingResult
import dev.dmil.skye.domain.usecase.GetCitySuggestionsUseCase
import dev.dmil.skye.domain.usecase.GetWeatherUseCase
import dev.dmil.skye.presentation.state.WeatherUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val getWeatherUseCase: GetWeatherUseCase,
    private val getCitySuggestionsUseCase: GetCitySuggestionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchError = MutableStateFlow("")
    val searchError = _searchError.asStateFlow()

    private val _searchResult = MutableStateFlow<List<GeocodingResult>>(emptyList())
    val searchResult = _searchResult.asStateFlow()

    init {
        getWeather(10.0, 10.0)
        viewModelScope.launch {
            @OptIn(kotlinx.coroutines.FlowPreview::class)
            _searchQuery
                .filter { it.length >= 2 }
                .debounce(500)
                .collect { query ->
                    getCitySuggestionsUseCase(query = query).fold(
                        onSuccess = { list ->
                            _searchResult.value = list
                        },
                        onFailure = { e ->
                            Log.e("WeatherViewModel.onSearchQuery", e.message ?: "Unknown error")
                        }
                    )
                }
        }
    }

    fun onDropdownMenuItemClick(geocodingResult: GeocodingResult) {
        getWeather(geocodingResult.lat, geocodingResult.lon)
        onDismissSearch()
    }

    fun onDismissSearch() {
        _searchResult.value = emptyList()
    }

    fun onSearchQueryChange(newValue: String) {
        _searchQuery.value = newValue
    }

    fun onSearch() {
        if (searchResult.value.isEmpty()) return
        val lat = _searchResult.value.first().lat
        val lon = _searchResult.value.first().lon
        getWeather(lat, lon)
        onDismissSearch()
    }

    fun getWeather(lat: Double, lon: Double) {
        if (_uiState.value is WeatherUiState.Success) {
            _uiState.value = WeatherUiState.Refreshing(weather = (_uiState.value as WeatherUiState.Success).weather)
        } else _uiState.value = WeatherUiState.Loading

        viewModelScope.launch {
            getWeatherUseCase(lat, lon).fold(
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