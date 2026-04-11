package dev.dmil.skye.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dev.dmil.skye.domain.model.Weather
import dev.dmil.skye.presentation.state.WeatherUiState
import dev.dmil.skye.presentation.viewmodel.WeatherViewModel

@Composable
fun WeatherScreen(
    modifier: Modifier = Modifier,
    viewModel: WeatherViewModel = hiltViewModel()
) {

    val uiState = viewModel.uiState.collectAsState()
    val searchQuery = viewModel.searchQuery.collectAsState()
    val searchError = viewModel.searchError.collectAsState()

    Box(modifier = modifier) {
        when(val state = uiState.value) {
            WeatherUiState.Loading -> {
                LoadingContent()
            }
            is WeatherUiState.Success -> {
                WeatherContent(
                    weather = state.weather,
                    searchQuery = searchQuery.value,
                    onSearchQueryChange = viewModel::onSearchQueryChange,
                    onSearch = viewModel::onSearch,
                    searchError = searchError.value
                )
            }
            is WeatherUiState.Refreshing -> {
                Box {
                    WeatherContent(
                        weather = state.weather,
                        searchQuery = searchQuery.value,
                        onSearchQueryChange = viewModel::onSearchQueryChange,
                        onSearch = viewModel::onSearch,
                        searchError = searchError.value
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
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    searchError: String
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TextField(
            value = searchQuery,
            onValueChange = { onSearchQueryChange(it) },
            maxLines = 1,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            isError = searchError.isNotBlank(),
            supportingText = { Text(text = searchError) }
        )
        Text(
            text = weather.city
        )
        Text(
            text = "${weather.temperature}º"
        )
        Text(
            text = weather.conditions
        )
        Text(
            text = weather.description
        )
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