package dev.dmil.skye.presentation.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.dmil.skye.domain.model.GeocodingResult
import dev.dmil.skye.domain.model.Weather
import dev.dmil.skye.domain.usecase.GetCitySuggestionsUseCase
import dev.dmil.skye.domain.usecase.GetForecastUseCase
import dev.dmil.skye.domain.usecase.GetWeatherUseCase
import dev.dmil.skye.presentation.screen.unixToHour
import dev.dmil.skye.presentation.state.WeatherUiState
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val getWeatherUseCase: GetWeatherUseCase,
    private val getForecastUseCase: GetForecastUseCase,
    private val getCitySuggestionsUseCase: GetCitySuggestionsUseCase,
    private val fusedLocationProviderClient: FusedLocationProviderClient
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
        viewModelScope.launch {
            @OptIn(kotlinx.coroutines.FlowPreview::class)
            _searchQuery
                .filter { it.length >= 2 }
                .debounce(500.milliseconds)
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

    @SuppressLint("MissingPermission")
    fun onLocationPermissionResult(isGranted: Boolean) {
        Log.d("WeatherViewModel", "Permission granted: $isGranted")
        if (isGranted) getCurrentLocation()
        else _uiState.value = WeatherUiState.Error("Отсутствует разрешение геолокации")
    }

    @RequiresPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    fun getCurrentLocation() { // TODO: fix the location requester
        Log.d("WeatherViewModel", "Requesting location...")
        fusedLocationProviderClient.getCurrentLocation(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            null
        )
            .addOnSuccessListener { location ->
                location ?: return@addOnSuccessListener
                Log.d("WeatherViewModel", "Location: $location")
                getWeather(
                    lat = location.latitude,
                    lon = location.longitude
                )
            }
            .addOnFailureListener { e ->
                Log.e("WeatherViewModel.getCurrentLocation", e.message ?: "Unknown error")
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
            _uiState.value = WeatherUiState.Refreshing(
                weather = (_uiState.value as WeatherUiState.Success).weather,
                forecast = (_uiState.value as WeatherUiState.Success).forecast
            )
        } else _uiState.value = WeatherUiState.Loading

        viewModelScope.launch {
            val weatherResult: Result<Weather>
            val forecastResult: Result<List<Weather>>
            coroutineScope {
                val weatherDeferred = async { getWeatherUseCase(lat, lon) }
                val forecastDeferred = async { getForecastUseCase(lat, lon) }
                weatherResult = weatherDeferred.await()
                forecastResult = forecastDeferred.await()
            }

            if (weatherResult.isSuccess) {
                val weather = weatherResult.getOrNull()!!
                val forecast = forecastResult.getOrNull() ?: emptyList()

                val filtered = forecast.filter { it.date >= System.currentTimeMillis() / 1000 }
                Log.d("Forecast", "now=${System.currentTimeMillis() / 1000}, first=${filtered.firstOrNull()?.date}, hour=${filtered.firstOrNull()?.let { unixToHour(it.date, weather.timezone) }}")

                _uiState.value = WeatherUiState.Success(weather, filtered)
                _searchError.value = ""
            } else {
                val e = weatherResult.exceptionOrNull()
                if (_uiState.value is WeatherUiState.Refreshing) {
                    _searchError.value = "Ошибка в названии города"
                    val refreshing = _uiState.value as WeatherUiState.Refreshing
                    _uiState.value = WeatherUiState.Success(refreshing.weather, refreshing.forecast)
                    Log.e("WeatherViewModel.getWeather", e?.message ?: "Error")
                    return@launch
                }
                Log.e("WeatherViewModel.getWeather", e?.message ?: "Unknown error")
                when (e) {
                    is HttpException -> _uiState.value = WeatherUiState.Error("Ошибка сервера")
                    is IOException -> _uiState.value = WeatherUiState.Error("Отсутствует подключение к интернету")
                    else -> _uiState.value = WeatherUiState.Error("Ошибка. Попробуйте позднее")
                }
            }
        }
    }
}