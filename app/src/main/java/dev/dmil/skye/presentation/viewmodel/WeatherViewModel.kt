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
import dev.dmil.skye.domain.model.SavedCity
import dev.dmil.skye.domain.model.Weather
import dev.dmil.skye.domain.usecase.AddSavedCityUseCase
import dev.dmil.skye.domain.usecase.DeleteSavedCityUseCase
import dev.dmil.skye.domain.usecase.GetCitySuggestionsUseCase
import dev.dmil.skye.domain.usecase.GetForecastUseCase
import dev.dmil.skye.domain.usecase.GetSavedCityUseCase
import dev.dmil.skye.domain.usecase.GetWeatherUseCase
import dev.dmil.skye.domain.usecase.GetWeeklyForecastUseCase
import dev.dmil.skye.domain.usecase.TestApiKeyUseCase
import dev.dmil.skye.presentation.screen.unixToHour
import dev.dmil.skye.presentation.state.CityListItem
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
    private val getWeeklyForecastUseCase: GetWeeklyForecastUseCase,
    private val getCitySuggestionsUseCase: GetCitySuggestionsUseCase,
    private val getSavedCityUseCase: GetSavedCityUseCase,
    private val addSavedCityUseCase: AddSavedCityUseCase,
    private val deleteSavedCityUseCase: DeleteSavedCityUseCase,
    private val fusedLocationProviderClient: FusedLocationProviderClient,
    private val testApiKeyUseCase: TestApiKeyUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchError = MutableStateFlow("")
    val searchError = _searchError.asStateFlow()

    private val _searchResult = MutableStateFlow<List<GeocodingResult>>(emptyList())
    val searchResult = _searchResult.asStateFlow()

    private val _cities = MutableStateFlow<List<CityListItem>>(emptyList())
    val cities = _cities.asStateFlow()

    private var currentLocationLat: Double? = null
    private var currentLocationLon: Double? = null
    private var currentLocationWeather: Weather? = null

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
                currentLocationLat = location.latitude
                currentLocationLon = location.longitude
                getWeather(
                    lat = location.latitude,
                    lon = location.longitude,
                    onWeatherLoaded = { weather ->
                        currentLocationWeather = weather
                        refreshCities()
                    }
                )
            }
            .addOnFailureListener { e ->
                Log.e("WeatherViewModel.getCurrentLocation", e.message ?: "Unknown error")
            }
    }

    fun onAddToFavorites(result: GeocodingResult) {
        val alreadySaved = _cities.value.any {
            !it.isCurrentLocation && it.lat == result.lat && it.lon == result.lon
        }
        if (!alreadySaved) {
            viewModelScope.launch {
                val city = SavedCity(
                    name = result.city,
                    state = result.state,
                    countryCode = result.countryCode,
                    lat = result.lat,
                    lon = result.lon
                )
                addSavedCityUseCase(city)
                refreshCities()
            }
        }
        _searchQuery.value = ""
        onDismissSearch()
    }

    fun onDeleteFavorite(city: SavedCity) {
        viewModelScope.launch {
            deleteSavedCityUseCase(city)
            refreshCities()
        }
    }

    fun onSelectCity(item: CityListItem) {
        getWeather(item.lat, item.lon, displayName = item.name)
    }

    fun onDismissSearch() {
        _searchResult.value = emptyList()
    }

    fun onSearchQueryChange(newValue: String) {
        _searchQuery.value = newValue
    }

    fun onSearch() {
        val first = searchResult.value.firstOrNull() ?: return
        onAddToFavorites(first)
    }

    fun getWeather(lat: Double, lon: Double, displayName: String? = null, onWeatherLoaded: ((Weather) -> Unit)? = null) {
        if (_uiState.value is WeatherUiState.Success) {
            val current = _uiState.value as WeatherUiState.Success
            _uiState.value = WeatherUiState.Refreshing(
                weather = current.weather,
                forecast = current.forecast,
                weeklyForecast = current.weeklyForecast
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
                val weather = weatherResult.getOrNull()!!.let { w ->
                    if (displayName != null) w.copy(city = displayName) else w
                }
                val forecast = forecastResult.getOrNull() ?: emptyList()

                val filtered = forecast.filter { it.date >= System.currentTimeMillis() / 1000 }
                Log.d("Forecast", "now=${System.currentTimeMillis() / 1000}, first=${filtered.firstOrNull()?.date}, hour=${filtered.firstOrNull()?.let { unixToHour(it.date, weather.timezone) }}")

                val weeklyForecast = getWeeklyForecastUseCase(filtered)
                _uiState.value = WeatherUiState.Success(weather, filtered, weeklyForecast)
                onWeatherLoaded?.invoke(weather)
                _searchError.value = ""
            } else {
                val e = weatherResult.exceptionOrNull()
                if (_uiState.value is WeatherUiState.Refreshing) {
                    _searchError.value = "Ошибка в названии города"
                    val refreshing = _uiState.value as WeatherUiState.Refreshing
                    _uiState.value = WeatherUiState.Success(refreshing.weather, refreshing.forecast, refreshing.weeklyForecast)
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

    suspend fun testApiKey(key: String): Result<Unit> {
        val lat = currentLocationLat
        val lon = currentLocationLon
        if (lat == null || lon == null) {
            return Result.failure(IllegalStateException("Location not available yet"))
        }
        return testApiKeyUseCase(key, lat, lon)
    }

    fun refreshAfterKeyChange() {
        val lat = currentLocationLat
        val lon = currentLocationLon
        if (lat == null || lon == null) return
        getWeather(lat, lon)
        refreshCities()
    }

    private fun refreshCities() {
        viewModelScope.launch {
            val favorites = getSavedCityUseCase().getOrNull() ?: emptyList()
            val previousByCoords = _cities.value.associateBy { "${it.lat}_${it.lon}" }

            val currentLocationItem = if (currentLocationLat != null && currentLocationLon != null) {
                listOf(
                    CityListItem(
                        name = currentLocationWeather?.city ?: "…",
                        countryCode = "",
                        lat = currentLocationLat!!,
                        lon = currentLocationLon!!,
                        isCurrentLocation = true,
                        temperature = currentLocationWeather?.temperature?.toInt(),
                        icon = currentLocationWeather?.icon,
                        timezone = currentLocationWeather?.timezone
                    )
                )
            } else emptyList()

            val favoriteItems = favorites.map { city ->
                val previous = previousByCoords["${city.lat}_${city.lon}"]
                CityListItem(
                    name = city.name,
                    state = city.state,
                    countryCode = city.countryCode,
                    lat = city.lat,
                    lon = city.lon,
                    isCurrentLocation = false,
                    savedCity = city,
                    temperature = previous?.temperature,
                    icon = previous?.icon,
                    timezone = previous?.timezone
                )
            }

            _cities.value = currentLocationItem + favoriteItems

            _cities.value.filter { it.temperature == null }.forEach { item ->
                launch {
                    getWeatherUseCase(item.lat, item.lon).onSuccess { weather ->
                        updateCityWeather(item.lat, item.lon, weather)
                    }
                }
            }
        }
    }

    private fun updateCityWeather(lat: Double, lon: Double, weather: Weather) {
        _cities.value = _cities.value.map { item ->
            if (item.lat == lat && item.lon == lon) {
                item.copy(
                    name = if (item.isCurrentLocation) weather.city ?: item.name else item.name,
                    temperature = weather.temperature.toInt(),
                    icon = weather.icon,
                    timezone = weather.timezone
                )
            } else item
        }
    }
}