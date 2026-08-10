package dev.dmil.skye.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import dev.dmil.skye.domain.model.GeocodingResult
import dev.dmil.skye.domain.model.SavedCity
import dev.dmil.skye.presentation.state.CityListItem
import dev.dmil.skye.presentation.ui.theme.Black
import dev.dmil.skye.presentation.ui.theme.Gray
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
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(top = 10.dp, bottom = 20.dp)
    ) {
        Text(
            text = "Skye",
            fontSize = 52.sp,
            color = Black,
            letterSpacing = (-2).sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
    val shape = RoundedCornerShape(10.dp)

    if (item.isCurrentLocation) {
        CityCardContent(item, bg, fg, shape, onClick)
    } else {
        val dismissState = rememberSwipeToDismissBoxState()

        LaunchedEffect(dismissState.currentValue) {
            if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
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
            .border(2.dp, Black, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
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
            Text(
                text = formatLocalTime(item.timezone),
                fontSize = 20.sp,
                fontWeight = FontWeight.Light,
                color = fg
            )
        }

        if (item.temperature != null && item.icon != null) {
            Icon(
                painter = painterResource(getCorrectConditionIcon(item.icon)),
                contentDescription = null,
                tint = if (isNight(item)) White else androidx.compose.ui.graphics.Color.Unspecified,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.size(14.dp))
            Text(text = "${item.temperature}º", fontSize = 36.sp, color = fg)
        } else {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = if (isNight(item)) White else Orange,
                strokeWidth = 2.dp
            )
        }
    }
}

@Composable
fun CitySearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onDismiss: () -> Unit,
    onResultClick: (GeocodingResult) -> Unit,
    error: String,
    results: List<GeocodingResult>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(White)
                .border(2.dp, Black, RoundedCornerShape(10.dp))
        ) {
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 18.sp, color = Black),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(Orange),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(text = "Search...", fontSize = 18.sp, color = Gray)
                    }
                    innerTextField()
                }
            )
        }

        if (error.isNotBlank()) {
            Text(
                text = error,
                color = Orange,
                fontSize = 13.sp,
                modifier = Modifier.padding(start = 20.dp, top = 6.dp)
            )
        }

        DropdownMenu(
            expanded = results.isNotEmpty(),
            onDismissRequest = onDismiss,
            properties = PopupProperties(focusable = false),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(White, RoundedCornerShape(12.dp))
        ) {
            results.forEach { result ->
                val subtitle = listOfNotNull(result.state, result.countryCode)
                    .filter { it.isNotBlank() }
                    .joinToString(", ")
                DropdownMenuItem(
                    text = {
                        Text(text = if (subtitle.isBlank()) result.city else "${result.city}, $subtitle")
                    },
                    onClick = { onResultClick(result) }
                )
            }
        }
    }
}

private fun formatLocalTime(timezoneOffsetSeconds: Int?): String {
    if (timezoneOffsetSeconds == null) return "--:--"
    val nowUtcSeconds = System.currentTimeMillis() / 1000
    val localSeconds = nowUtcSeconds + timezoneOffsetSeconds
    val secondsInDay = ((localSeconds % 86400) + 86400) % 86400
    val hour = (secondsInDay / 3600).toInt()
    val minute = ((secondsInDay % 3600) / 60).toInt()
    return "%02d:%02d".format(hour, minute)
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