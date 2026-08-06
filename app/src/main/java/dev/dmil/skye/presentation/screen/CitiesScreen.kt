package dev.dmil.skye.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.dmil.skye.domain.model.GeocodingResult
import dev.dmil.skye.domain.model.SavedCity
import dev.dmil.skye.presentation.state.CityListItem
import dev.dmil.skye.presentation.ui.theme.Black
import dev.dmil.skye.presentation.ui.theme.Orange
import dev.dmil.skye.presentation.ui.theme.White

@Composable
fun CitiesScreen(
    cities: List<CityListItem>,
    onCityClick: (CityListItem) -> Unit,
    onDeleteCity: (SavedCity) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onDismissSearch: () -> Unit,
    onAddCity: (GeocodingResult) -> Unit,
    searchError: String,
    searchResult: List<GeocodingResult>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 15.dp)
            .padding(top = 40.dp, bottom = 20.dp)
    ) {
        Text(text = "Skye", fontSize = 48.sp, color = Black)
        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(cities, key = { "${it.lat}_${it.lon}" }) { city ->
                CityRow(
                    item = city,
                    onClick = { onCityClick(city) },
                    onDelete = { city.savedCity?.let(onDeleteCity) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        CitySearchBar(
            query = searchQuery,
            onQueryChange = onSearchQueryChange,
            onSearch = onSearch,
            onDismiss = onDismissSearch,
            onResultClick = onAddCity,
            error = searchError,
            results = searchResult
        )
    }
}

@Composable
private fun CityRow(
    item: CityListItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val isNight = item.icon?.endsWith("n") == true
    val bg = if (isNight) Black else White
    val fg = if (isNight) White else Black
    val shape = RoundedCornerShape(16.dp)

    var removed by remember { mutableStateOf(false) }
    if (removed) return

    if (item.isCurrentLocation) {
        CityCardContent(item, bg, fg, shape, onClick)
    } else {
        val dismissState = rememberSwipeToDismissBoxState()

        LaunchedEffect(dismissState.currentValue) {
            if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
                removed = true
                onDelete()
            }
        }

        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromStartToEnd = false,
            backgroundContent = { DeleteBackground(shape) }
        ) {
            CityCardContent(item, bg, fg, shape, onClick)
        }
    }
}

@Composable
private fun CityCardContent(
    item: CityListItem,
    bg: androidx.compose.ui.graphics.Color,
    fg: androidx.compose.ui.graphics.Color,
    shape: RoundedCornerShape,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(bg)
            .border(1.5.dp, Black, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = item.name, fontSize = 28.sp, color = fg)
                if (item.isCurrentLocation) {
                    Spacer(modifier = Modifier.size(6.dp))
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "Текущая геопозиция",
                        tint = Orange,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        if (item.temperature != null && item.icon != null) {
            Icon(
                painter = painterResource(getCorrectConditionIcon(item.icon)),
                contentDescription = null,
                tint = androidx.compose.ui.graphics.Color.Unspecified,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.size(10.dp))
            Text(text = "${item.temperature}º", fontSize = 34.sp, color = fg)
        } else {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = if (isNight(item)) White else Orange,
                strokeWidth = 2.dp
            )
        }
    }
}

private fun isNight(item: CityListItem) = item.icon?.endsWith("n") == true

@Composable
private fun DeleteBackground(shape: RoundedCornerShape) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape)
            .background(Orange),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Delete,
            contentDescription = "Удалить",
            tint = White,
            modifier = Modifier.padding(end = 20.dp).size(22.dp)
        )
    }
}