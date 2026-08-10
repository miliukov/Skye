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
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dev.dmil.skye.R
import dev.dmil.skye.domain.model.DailyForecast
import dev.dmil.skye.domain.model.Weather
import dev.dmil.skye.presentation.state.WeatherUiState
import dev.dmil.skye.presentation.ui.theme.Black
import dev.dmil.skye.presentation.ui.theme.Gray
import dev.dmil.skye.presentation.viewmodel.WeatherViewModel
import kotlin.collections.forEachIndexed
import kotlin.collections.lastIndex

@Composable
fun WeatherScreen(
    modifier: Modifier = Modifier,
    viewModel: WeatherViewModel = hiltViewModel()
) {

    val uiState = viewModel.uiState.collectAsState()
    var showCities by remember { mutableStateOf(false) }
    val cities = viewModel.cities.collectAsState()
    BackHandler(enabled = showCities) {
        showCities = false
    }
    var selectedDayIndex by remember { mutableStateOf(0) }
    var showDayDetail by remember { mutableStateOf(false) }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !showDayDetail
                isAppearanceLightNavigationBars = !showDayDetail
            }
        }
    }

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

    AnimatedContent(
        targetState = showCities,
        modifier = modifier,
        transitionSpec = {
            if (targetState) {
                (slideInVertically(tween(320, easing = FastOutSlowInEasing)) { it } + fadeIn(tween(320)))
                    .togetherWith(fadeOut(tween(200)) + scaleOut(targetScale = 0.94f, animationSpec = tween(320)))
            } else {
                (fadeIn(tween(280)) + scaleIn(initialScale = 0.94f, animationSpec = tween(320)))
                    .togetherWith(slideOutVertically(tween(320, easing = FastOutSlowInEasing)) { it } + fadeOut(tween(200)))
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
                searchError = searchError.value,
                searchResult = searchResult.value
            )
        } else {
            val weeklyForecastForOverlay = (uiState.value as? WeatherUiState.Success)?.weeklyForecast
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
                            }
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
                                }
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
                DayDetailOverlay(
                    visible = showDayDetail,
                    days = weeklyForecastForOverlay,
                    selectedIndex = selectedDayIndex,
                    onDaySelected = { selectedDayIndex = it },
                    onDismiss = { showDayDetail = false }
                )
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
    weeklyForecast: List<DailyForecast>,
    onOpenCities: () -> Unit,
    onDayClick: (Int) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val maxHeightPx = constraints.maxHeight
        var contentHeightPx by remember { mutableStateOf(0) }
        val needsScroll = contentHeightPx > maxHeightPx
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 15.dp)
                .then(if (needsScroll) Modifier.verticalScroll(scrollState) else Modifier),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!needsScroll) {
                Spacer(modifier = Modifier.weight(1f))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { contentHeightPx = it.height },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Header(
                    city = weather.city!!,
                    temp = weather.temperature.toInt(),
                    iconId = weather.icon,
                    onSwipeDown = onOpenCities
                )
                Spacer(modifier = Modifier.height(12.dp))
                ForecastCarousel(weather = weather, forecast = forecast)
                Spacer(modifier = Modifier.height(16.dp))
                WeeklyForecastList(forecast = weeklyForecast, onDayClick = onDayClick)
                Spacer(modifier = Modifier.height(14.dp))
                IconButton(
                    onClick = onOpenCities,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(
                        imageVector = Icons.Filled.List,
                        contentDescription = "Города",
                        tint = Gray
                    )
                }
            }

            if (needsScroll) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun Header(
    city: String,
    temp: Int,
    iconId: String,
    onSwipeDown: () -> Unit
) {
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
                    contentDescription = "weather icon",
                    modifier = Modifier.size(130.dp),
                    tint = Color.Unspecified
                )
                Text(text = cityName, fontSize = 40.sp)
                Text(text = "${temperature}º", fontSize = 80.sp) // TODO: make Celsius symbol not centered
            }
        }
    }
}

@Composable
fun ForecastCarousel(weather: Weather, forecast: List<Weather>) {
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
}

@Composable
fun WeeklyForecastList(forecast: List<DailyForecast>, onDayClick: (Int) -> Unit) {
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
                .padding(horizontal = 16.dp)
        ) {
            days.forEachIndexed { index, day ->
                DailyForecastRow(day, onClick = { onDayClick(index) })
                if (index != days.lastIndex) {
                    HorizontalDivider(color = Gray.copy(alpha = 0.8f))
                }
            }
        }
    }
}

@Composable
private fun DailyForecastRow(day: DailyForecast, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = day.dayLabel,
            fontSize = 22.sp,
            fontWeight = FontWeight.Normal,
            color = Black,
            modifier = Modifier.width(78.dp)
        )
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(getCorrectConditionIcon(day.icon)),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = Color.Unspecified
            )
            Text(
                text = "${day.minTemp}º – ${day.maxTemp}º",
                fontSize = 22.sp,
                fontWeight = FontWeight.Normal,
                color = Black
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .border(1.5.dp, Black, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowUpward,
                        contentDescription = null,
                        tint = Black,
                        modifier = Modifier
                            .size(16.dp)
                            .rotate(day.windDegree.toFloat())
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${day.windSpeed.toInt()} m/s",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = Black
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