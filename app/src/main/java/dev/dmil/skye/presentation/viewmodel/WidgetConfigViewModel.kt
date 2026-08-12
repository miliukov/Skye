package dev.dmil.skye.presentation.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.dmil.skye.R
import dev.dmil.skye.domain.usecase.GetSavedCityUseCase
import dev.dmil.skye.domain.usecase.GetWeatherUseCase
import dev.dmil.skye.presentation.state.CityListItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WidgetConfigViewModel @Inject constructor(
    private val getSavedCityUseCase: GetSavedCityUseCase,
    private val getWeatherUseCase: GetWeatherUseCase,
    private val fusedLocationProviderClient: FusedLocationProviderClient,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val _cities = MutableStateFlow<List<CityListItem>>(emptyList())
    val cities = _cities.asStateFlow()

    init {
        viewModelScope.launch {
            val favorites = getSavedCityUseCase().getOrNull() ?: emptyList()
            val initialItems = favorites.map { city ->
                CityListItem(
                    name = city.name,
                    state = city.state,
                    countryCode = city.countryCode,
                    lat = city.lat,
                    lon = city.lon,
                    isCurrentLocation = false,
                    savedCity = city
                )
            }
            _cities.value = initialItems

            initialItems.forEach { item ->
                launch {
                    getWeatherUseCase(item.lat, item.lon).onSuccess { weather ->
                        _cities.value = _cities.value.map {
                            if (it.lat == item.lat && it.lon == item.lon) {
                                it.copy(
                                    temperature = weather.temperature.toInt(),
                                    icon = weather.icon,
                                    timezone = weather.timezone,
                                    description = weather.description.replaceFirstChar { c -> c.uppercase() }
                                )
                            } else it
                        }
                    }
                }
            }
        }
        loadCurrentLocation()
    }

    @SuppressLint("MissingPermission")
    private fun loadCurrentLocation() {
        runCatching {
            fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener { location ->
                    location ?: return@addOnSuccessListener
                    viewModelScope.launch {
                        getWeatherUseCase(location.latitude, location.longitude).onSuccess { weather ->
                            val currentLocationItem = CityListItem(
                                name = weather.city ?: context.getString(R.string.widget_current_location_fallback),
                                countryCode = "",
                                lat = location.latitude,
                                lon = location.longitude,
                                isCurrentLocation = true,
                                temperature = weather.temperature.toInt(),
                                icon = weather.icon,
                                description = weather.description.replaceFirstChar { c -> c.uppercase() }
                            )
                            _cities.value = listOf(currentLocationItem) + _cities.value.filterNot { it.isCurrentLocation }
                        }
                    }
                }
        }
    }
}