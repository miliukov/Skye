package dev.dmil.skye.presentation.screen

import android.Manifest
import android.util.Log
import android.util.Log.w
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dev.dmil.skye.R
import dev.dmil.skye.domain.model.GeocodingResult
import dev.dmil.skye.domain.model.Weather
import dev.dmil.skye.presentation.state.WeatherUiState
import dev.dmil.skye.presentation.ui.theme.Orange
import dev.dmil.skye.presentation.viewmodel.WeatherViewModel

@Composable
fun WeatherScreen(
    modifier: Modifier = Modifier,
    viewModel: WeatherViewModel = hiltViewModel()
) {

    val uiState = viewModel.uiState.collectAsState()
    val searchQuery = viewModel.searchQuery.collectAsState()
    val searchError = viewModel.searchError.collectAsState()
    val searchResult = viewModel.searchResult.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onLocationPermissionResult(isGranted)
    }

    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    Box(modifier = modifier) {
        when(val state = uiState.value) {
            WeatherUiState.Loading -> {
                LoadingContent()
            }
            is WeatherUiState.Success -> {
                WeatherContent(
                    weather = state.weather,
                    forecast = state.forecast,
                    searchQuery = searchQuery.value,
                    onSearchQueryChange = viewModel::onSearchQueryChange,
                    onSearch = viewModel::onSearch,
                    onDismissSearch = viewModel::onDismissSearch,
                    onDropdownMenuItemClick = viewModel::onDropdownMenuItemClick,
                    searchError = searchError.value,
                    searchResult = searchResult.value
                )
            }
            is WeatherUiState.Refreshing -> {
                Box {
                    WeatherContent(
                        weather = state.weather,
                        forecast = state.forecast,
                        searchQuery = searchQuery.value,
                        onSearchQueryChange = viewModel::onSearchQueryChange,
                        onSearch = viewModel::onSearch,
                        onDismissSearch = viewModel::onDismissSearch,
                        onDropdownMenuItemClick = viewModel::onDropdownMenuItemClick,
                        searchError = searchError.value,
                        searchResult = searchResult.value
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f))
                    )
                }
            }
            is WeatherUiState.Error -> {
                ErrorContent(error = state.error)
            }
        }
    }
}

@Composable
fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun WeatherContent(
    weather: Weather,
    forecast: List<Weather>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onDismissSearch: () -> Unit,
    onDropdownMenuItemClick: (GeocodingResult) -> Unit,
    searchError: String,
    searchResult: List<GeocodingResult>
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 15.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Header(
            city = weather.city!!,
            temp = weather.temperature.toInt(),
            iconId = weather.icon
        )
        ForecastCarousel(
            weather = weather,
            forecast = forecast
        )
//        Box { // TODO: Search
//            TextField(
//                value = searchQuery,
//                onValueChange = { onSearchQueryChange(it) },
//                maxLines = 1,
//                keyboardOptions = KeyboardOptions(
//                    keyboardType = KeyboardType.Text,
//                    imeAction = ImeAction.Search
//                ),
//                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
//                isError = searchError.isNotBlank(),
//                supportingText = { Text(text = searchError) }
//            )
//            DropdownMenu(
//                expanded = searchResult.isNotEmpty(),
//                onDismissRequest = { onDismissSearch() },
//                properties = PopupProperties(focusable = false)
//            ) {
//                searchResult.forEach {
//                    DropdownMenuItem(
//                        text = {
//                            Text(text = "${it.city} ${it.state} ${it.countryCode}")
//                        },
//                        onClick = { onDropdownMenuItemClick(it) }
//                    )
//                }
//            }
//        }
    }
}

@Composable
fun Header(city: String, temp: Int, iconId: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(120.dp))
        Icon(
            painter = painterResource(getCorrectConditionIcon(iconId)),
            contentDescription = "weather icon",
            modifier = Modifier.size(130.dp),
            tint = Color.Unspecified
        )
        Text(
            text = city,
            fontSize = 40.sp
        )
        Text(
            text = "${temp}º", // TODO: make Celsius symbol not centered
            fontSize = 80.sp
        )
        Text(
            text = iconId
        )
    }
}

@Composable
fun ForecastCarousel(weather: Weather, forecast: List<Weather>) {
    val hourly = listOf(weather) + forecast
    Log.d("Carousel", "hourly size=${hourly.size}, forecast size=${forecast.size}")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        hourly.forEachIndexed { index, w ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (index == 0) "Now" else "${unixToHour(w.date, w.timezone)}",
                    fontSize = 15.sp
                )
                Icon(
                    painter = painterResource(getCorrectConditionIcon(w.icon)),
                    contentDescription = "",
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = "${w.temperature.toInt()}º",
                    fontSize = 20.sp
                )
            }
        }
    }
}

@Composable
fun ErrorContent(error: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = error
        )
    }
}

fun getCorrectConditionIcon(id: String): Int {
    return when (id) {
        "01d" -> R.drawable.wi_day_sunny
        "01n" -> R.drawable.wi_night_clear
        "02d" -> R.drawable.wi_day_cloudy
        "02n" -> R.drawable.wi_night_cloudy
        "03d", "03n", "04d", "04n" -> R.drawable.wi_cloudy
        "10d" -> R.drawable.wi_day_rain
        else -> R.drawable.wi_na
    }
}

fun unixToHour(dt: Long, timezoneOffsetSeconds: Int): Int {
    val localSeconds = dt + timezoneOffsetSeconds
    val secondsInDay = localSeconds % 86400
    return (secondsInDay / 3600).toInt()
}