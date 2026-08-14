package dev.dmil.skye.presentation.screen

import android.Manifest
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dev.dmil.skye.R
import dev.dmil.skye.domain.model.DailyForecast
import dev.dmil.skye.domain.model.Units
import dev.dmil.skye.domain.model.Weather
import dev.dmil.skye.presentation.state.WeatherError
import dev.dmil.skye.presentation.state.WeatherUiState
import dev.dmil.skye.presentation.ui.theme.Gray
import dev.dmil.skye.presentation.ui.theme.White
import dev.dmil.skye.presentation.util.formatTemperature
import dev.dmil.skye.presentation.util.formatWindSpeed
import dev.dmil.skye.presentation.viewmodel.SettingsViewModel
import dev.dmil.skye.presentation.viewmodel.WeatherViewModel
import kotlin.collections.forEachIndexed
import kotlin.collections.lastIndex
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.style.TextAlign
import dev.dmil.skye.presentation.ui.theme.Orange
import dev.dmil.skye.presentation.util.isCompactWidth

@Composable
fun WeatherScreen(
    modifier: Modifier = Modifier,
    viewModel: WeatherViewModel = hiltViewModel()
) {

    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val hasCompletedOnboarding = settingsViewModel.hasCompletedOnboarding.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onLocationPermissionResult(isGranted)
    }

    LaunchedEffect(hasCompletedOnboarding.value) {
        if (hasCompletedOnboarding.value == true) {
            launcher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    when (hasCompletedOnboarding.value) {
        null -> return
        false -> {
            OnboardingScreen(
                onLocationRequested = { settingsViewModel.setOnboardingCompleted() },
                modifier = modifier
            )
            return
        }
        true -> Unit
    }

    val uiState = viewModel.uiState.collectAsState()
    var showCities by remember { mutableStateOf(false) }
    val cities = viewModel.cities.collectAsState()
    BackHandler(enabled = showCities) {
        showCities = false
    }
    var selectedDayIndex by remember { mutableIntStateOf(0) }
    var showDayDetail by remember { mutableStateOf(false) }

    val themeMode = settingsViewModel.themeMode.collectAsState()
    val units = settingsViewModel.units.collectAsState()
    var showSettings by remember { mutableStateOf(false) }

    BackHandler(enabled = showSettings) {
        showSettings = false
    }

    val view = LocalView.current
    val isDarkBackground = MaterialTheme.colorScheme.background.luminance() < 0.5f
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !showDayDetail && !isDarkBackground
                isAppearanceLightNavigationBars = !showDayDetail && !isDarkBackground
            }
        }
    }

    val searchQuery = viewModel.searchQuery.collectAsState()
    val searchError = viewModel.searchError.collectAsState()
    val searchResult = viewModel.searchResult.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = showCities,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = {
                if (targetState) {
                    (slideInVertically(tween(320, easing = FastOutSlowInEasing)) { it } + fadeIn(
                        tween(320)
                    ))
                        .togetherWith(
                            fadeOut(tween(200)) + scaleOut(
                                targetScale = 0.94f,
                                animationSpec = tween(320)
                            )
                        )
                } else {
                    (fadeIn(tween(280)) + scaleIn(initialScale = 0.94f, animationSpec = tween(320)))
                        .togetherWith(
                            slideOutVertically(
                                tween(
                                    320,
                                    easing = FastOutSlowInEasing
                                )
                            ) { it } + fadeOut(tween(200)))
                }.using(SizeTransform(clip = false))
            },
            label = "home_cities_transition"
        ) { citiesVisible ->
            if (citiesVisible) {
                CitiesScreen(
                    cities = cities.value,
                    onCityClick = { item ->
                        viewModel.onSelectCity(item)
                        showCities = false
                    },
                    onDeleteCity = viewModel::onDeleteFavorite,
                    searchQuery = searchQuery.value,
                    onSearchQueryChange = viewModel::onSearchQueryChange,
                    onSearch = viewModel::onSearch,
                    onDismissSearch = viewModel::onDismissSearch,
                    onAddCity = viewModel::onAddToFavorites,
                    searchError = searchError.value?.let { errorMessage(it) } ?: "",
                    searchResult = searchResult.value,
                    onOpenSettings = { showSettings = true },
                    units = units.value
                )
            } else {
                val weeklyForecastForOverlay =
                    (uiState.value as? WeatherUiState.Success)?.weeklyForecast
                        ?: (uiState.value as? WeatherUiState.Refreshing)?.weeklyForecast
                        ?: emptyList()

                Box(modifier = Modifier.fillMaxSize()) {
                    when (val state = uiState.value) {
                        WeatherUiState.Loading -> {
                            LoadingContent()
                        }

                        is WeatherUiState.Success -> {
                            WeatherContent(
                                weather = state.weather,
                                forecast = state.forecast,
                                weeklyForecast = state.weeklyForecast,
                                onOpenCities = { showCities = true },
                                onDayClick = { index ->
                                    selectedDayIndex = index
                                    showDayDetail = true
                                },
                                units = units.value,
                                isStale = state.isStale
                            )
                        }

                        is WeatherUiState.Refreshing -> {
                            Box {
                                WeatherContent(
                                    weather = state.weather,
                                    forecast = state.forecast,
                                    weeklyForecast = state.weeklyForecast,
                                    onOpenCities = { showCities = true },
                                    onDayClick = { index ->
                                        selectedDayIndex = index
                                        showDayDetail = true
                                    },
                                    units = units.value,
                                    isStale = state.isStale
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.3f))
                                )
                            }
                        }

                        is WeatherUiState.Error -> {
                            ErrorContent(
                                error = state.error,
                                onRetry = viewModel::retryLastRequest
                            )
                        }
                    }
                    DayDetailOverlay(
                        visible = showDayDetail,
                        days = weeklyForecastForOverlay,
                        selectedIndex = selectedDayIndex,
                        onDaySelected = { selectedDayIndex = it },
                        onDismiss = { showDayDetail = false },
                        units = units.value
                    )
                }
            }
        }
    }

    val apiKey = settingsViewModel.apiKey.collectAsState()
    val apiKeySetAt = settingsViewModel.apiKeySetAt.collectAsState()
    val language = settingsViewModel.language.collectAsState()

    SettingsOverlay(
        visible = showSettings,
        themeMode = themeMode.value,
        units = units.value,
        language = language.value,
        apiKey = apiKey.value,
        apiKeySetAt = apiKeySetAt.value,
        onThemeModeSelected = settingsViewModel::setThemeMode,
        onUnitsSelected = settingsViewModel::setUnits,
        onLanguageSelected = settingsViewModel::setLanguage,
        onApiKeyChanged = { key ->
            if (key == null) {
                settingsViewModel.setApiKeyAwait(null)
                viewModel.refreshAfterKeyChange()
                Result.success(Unit)
            } else {
                val testResult = viewModel.testApiKey(key)
                if (testResult.isSuccess) {
                    settingsViewModel.setApiKeyAwait(key)
                    viewModel.refreshAfterKeyChange()
                }
                testResult
            }
        },
        onDismiss = { showSettings = false }
    )
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
    weeklyForecast: List<DailyForecast>,
    onOpenCities: () -> Unit,
    onDayClick: (Int) -> Unit,
    units: Units,
    isStale: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 15.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Header(
            city = weather.city!!,
            temp = weather.temperature.toInt(),
            iconId = weather.icon,
            onSwipeDown = onOpenCities,
            units = units,
            isStale = isStale
        )
        Spacer(modifier = Modifier.height(12.dp))
        ForecastCarousel(weather = weather, forecast = forecast, units = units)
        Spacer(modifier = Modifier.height(16.dp))
        WeeklyForecastList(forecast = weeklyForecast, onDayClick = onDayClick, units = units)
        Spacer(modifier = Modifier.height(14.dp))
        IconButton(
            onClick = onOpenCities,
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(
                imageVector = Icons.Filled.List,
                contentDescription = stringResource(R.string.home_open_cities_content_description),
                tint = Gray
            )
        }
    }
}

@Composable
fun Header(
    city: String,
    temp: Int,
    iconId: String,
    onSwipeDown: () -> Unit,
    units: Units,
    isStale: Boolean
) {
    val onBackground = MaterialTheme.colorScheme.onBackground
    val isDarkBackground = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val compact = isCompactWidth()

    val iconSize = if (compact) 110.dp else 130.dp
    val cityFontSize = if (compact) 34.sp else 40.sp
    val cityLineHeight = if (compact) 38.sp else 44.sp
    val tempFontSize = if (compact) 68.sp else 80.sp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                var accumulatedDrag = 0f
                detectVerticalDragGestures(
                    onDragStart = { accumulatedDrag = 0f },
                    onVerticalDrag = { change, dragAmount ->
                        accumulatedDrag += dragAmount
                        if (accumulatedDrag > 80f) {
                            onSwipeDown()
                            accumulatedDrag = 0f
                        }
                        change.consume()
                    }
                )
            }
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        AnimatedContent(
            targetState = Triple(iconId, city, temp),
            transitionSpec = {
                (fadeIn(tween(280)) + scaleIn(initialScale = 0.92f, animationSpec = tween(280)))
                    .togetherWith(fadeOut(tween(160)))
            },
            label = "header_content"
        ) { (icon, cityName, temperature) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painter = painterResource(getCorrectConditionIcon(icon)),
                    contentDescription = stringResource(R.string.header_weather_icon_content_description),
                    modifier = Modifier.size(iconSize),
                    tint = weatherIconTint(icon, isDarkBackground)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = cityName,
                        fontSize = cityFontSize,
                        lineHeight = cityLineHeight,
                        textAlign = TextAlign.Center,
                        color = onBackground
                    )
                    if (isStale) {
                        Spacer(modifier = Modifier.size(6.dp))
                        Icon(
                            imageVector = Icons.Filled.CloudOff,
                            contentDescription = stringResource(R.string.stale_data_content_description),
                            tint = Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(text = formatTemperature(temperature, units), fontSize = tempFontSize, color = onBackground)
            }
        }
    }
}

@Composable
fun ForecastCarousel(weather: Weather, forecast: List<Weather>, units: Units) {
    val onBackground = MaterialTheme.colorScheme.onBackground
    val isDarkBackground = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val compact = isCompactWidth()

    val iconSize = if (compact) 34.dp else 40.dp
    val hourFontSize = if (compact) 13.sp else 15.sp
    val tempFontSize = if (compact) 17.sp else 20.sp

    AnimatedContent(
        targetState = listOf(weather) + forecast,
        transitionSpec = {
            fadeIn(tween(280)).togetherWith(fadeOut(tween(160)))
        },
        label = "carousel_content"
    ) { hourly ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            hourly.forEachIndexed { index, w ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (index == 0) stringResource(R.string.forecast_now) else "${unixToHour(w.date, w.timezone)}",
                        fontSize = hourFontSize,
                        color = onBackground
                    )
                    Icon(
                        painter = painterResource(getCorrectConditionIcon(w.icon)),
                        contentDescription = "",
                        modifier = Modifier.size(iconSize),
                        tint = weatherIconTint(w.icon, isDarkBackground)
                    )
                    Text(
                        text = formatTemperature(w.temperature, units),
                        fontSize = tempFontSize,
                        color = onBackground
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyForecastList(forecast: List<DailyForecast>, onDayClick: (Int) -> Unit, units: Units) {
    AnimatedContent(
        targetState = forecast,
        transitionSpec = {
            fadeIn(tween(280)).togetherWith(fadeOut(tween(160)))
        },
        label = "weekly_forecast_content"
    ) { days ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
        ) {
            days.forEachIndexed { index, day ->
                DailyForecastRow(day, onClick = { onDayClick(index) }, units = units)
                if (index != days.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                }
            }
        }
    }
}

@Composable
private fun DailyForecastRow(day: DailyForecast, onClick: () -> Unit, units: Units) {
    val onBackground = MaterialTheme.colorScheme.onBackground
    val isDarkBackground = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val compact = isCompactWidth()

    val dayLabelWidth = if (compact) 82.dp else 96.dp
    val rowFontSize = if (compact) 19.sp else 22.sp
    val windFontSize = if (compact) 15.sp else 18.sp
    val conditionIconSize = if (compact) 27.dp else 32.dp
    val windCircleSize = if (compact) 26.dp else 30.dp
    val windArrowSize = if (compact) 14.dp else 16.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = if (compact) 10.dp else 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val isToday = day.date == java.time.LocalDate.now()
        Text(
            text = if (isToday) {
                stringResource(R.string.day_today)
            } else {
                day.date.dayOfWeek
                    .getDisplayName(java.time.format.TextStyle.SHORT, LocalLocale.current.platformLocale)
                    .replaceFirstChar { it.uppercase() }
            },
            fontSize = rowFontSize,
            fontWeight = FontWeight.Normal,
            color = onBackground,
            maxLines = 1,
            modifier = Modifier.width(dayLabelWidth)
        )
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(getCorrectConditionIcon(day.icon)),
                contentDescription = null,
                modifier = Modifier.size(conditionIconSize),
                tint = weatherIconTint(day.icon, isDarkBackground)
            )
            Text(
                text = "${formatTemperature(day.minTemp, units)} – ${formatTemperature(day.maxTemp, units)}",
                fontSize = rowFontSize,
                fontWeight = FontWeight.Normal,
                color = onBackground
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(windCircleSize)
                        .border(1.5.dp, onBackground, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowUpward,
                        contentDescription = null,
                        tint = onBackground,
                        modifier = Modifier
                            .size(windArrowSize)
                            .rotate(day.windDegree.toFloat())
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatWindSpeed(day.windSpeed, units),
                    fontSize = windFontSize,
                    fontWeight = FontWeight.Normal,
                    color = onBackground
                )
            }
        }
    }
}

@Composable
fun ErrorContent(error: WeatherError, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = errorIcon(error),
                contentDescription = null,
                tint = Gray,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage(error),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.error_retry),
                color = Orange,
                fontSize = 15.sp,
                modifier = Modifier.clickable(onClick = onRetry)
            )
        }
    }
}

@Composable
fun errorMessage(error: WeatherError): String = when (error) {
    WeatherError.LocationPermissionDenied -> stringResource(R.string.error_location_permission_denied)
    WeatherError.LocationServicesDisabled -> stringResource(R.string.error_location_services_disabled)
    WeatherError.LocationUnavailable -> stringResource(R.string.error_location_unavailable)
    WeatherError.ServerError -> stringResource(R.string.error_server)
    WeatherError.NoInternet -> stringResource(R.string.error_no_internet)
    WeatherError.InvalidCityName -> stringResource(R.string.error_invalid_city_name)
    WeatherError.Unknown -> stringResource(R.string.error_unknown)
}

fun errorIcon(error: WeatherError): ImageVector = when (error) {
    WeatherError.LocationPermissionDenied -> Icons.Filled.LocationOff
    WeatherError.LocationServicesDisabled -> Icons.Filled.LocationOff
    WeatherError.LocationUnavailable -> Icons.Filled.LocationOff
    WeatherError.ServerError -> Icons.Filled.ErrorOutline
    WeatherError.NoInternet -> Icons.Filled.WifiOff
    WeatherError.InvalidCityName -> Icons.Filled.ErrorOutline
    WeatherError.Unknown -> Icons.Filled.CloudOff
}

fun getCorrectConditionIcon(id: String): Int {
    return when (id) {
        "01d" -> R.drawable.wi_day_sunny
        "01n" -> R.drawable.wi_night_clear
        "02d" -> R.drawable.wi_day_cloudy
        "02n" -> R.drawable.wi_night_cloudy
        "03d", "03n" -> R.drawable.wi_cloud
        "04d", "04n" -> R.drawable.wi_cloudy
        "09d", "09n" -> R.drawable.wi_rain
        "10d" -> R.drawable.wi_day_rain
        "10n" -> R.drawable.wi_night_rain
        "11d", "11n" -> R.drawable.wi_thunderstorm
        "13d", "13n" -> R.drawable.wi_snow
        else -> R.drawable.wi_na
    }
}

fun weatherIconTint(iconId: String, isDarkBackground: Boolean): Color {
    if (!isDarkBackground) return Color.Unspecified
    return if (iconId == "01d") Color.Unspecified else White
}

fun unixToHour(dt: Long, timezoneOffsetSeconds: Int): Int {
    val localSeconds = dt + timezoneOffsetSeconds
    val secondsInDay = localSeconds % 86400
    return (secondsInDay / 3600).toInt()
}