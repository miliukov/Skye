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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import dev.dmil.skye.R
import dev.dmil.skye.domain.model.GeocodingResult
import dev.dmil.skye.domain.model.SavedCity
import dev.dmil.skye.domain.model.Units
import dev.dmil.skye.presentation.state.CityListItem
import dev.dmil.skye.presentation.ui.theme.Black
import dev.dmil.skye.presentation.ui.theme.Gray
import dev.dmil.skye.presentation.ui.theme.Orange
import dev.dmil.skye.presentation.ui.theme.White
import dev.dmil.skye.presentation.util.formatTemperature
import dev.dmil.skye.presentation.util.isCompactWidth

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
    onOpenSettings: () -> Unit,
    units: Units,
    modifier: Modifier = Modifier
) {
    val compact = isCompactWidth()

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = if (compact) 18.dp else 24.dp)
            .padding(top = 10.dp, bottom = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Skye",
                fontSize = if (compact) 42.sp else 52.sp,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = (-2).sp,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = stringResource(R.string.cities_settings_content_description),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(cities, key = { it.savedCity?.id?.toString() ?: "current_location" }) { city ->
                CityRow(
                    item = city,
                    onClick = { onCityClick(city) },
                    onDelete = { city.savedCity?.let(onDeleteCity) },
                    units = units
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
    onDelete: () -> Unit,
    units: Units
) {
    val isNight = item.icon?.endsWith("n") == true
    val bg = if (isNight) Black else White
    val fg = if (isNight) White else Black
    val shape = RoundedCornerShape(10.dp)

    if (item.isCurrentLocation) {
        CityCardContent(item, bg, fg, shape, onClick, units)
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
            CityCardContent(item, bg, fg, shape, onClick, units)
        }
    }
}

@Composable
private fun CityCardContent(
    item: CityListItem,
    bg: androidx.compose.ui.graphics.Color,
    fg: androidx.compose.ui.graphics.Color,
    shape: RoundedCornerShape,
    onClick: () -> Unit,
    units: Units
) {
    val compact = isCompactWidth()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(bg)
            .border(2.dp, fg, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = if (compact) 8.dp else 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = item.name, fontSize = if (compact) 22.sp else 28.sp, color = fg)
                if (item.isCurrentLocation) {
                    Spacer(modifier = Modifier.size(6.dp))
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = stringResource(R.string.cities_current_location_content_description),
                        tint = Orange,
                        modifier = Modifier.size(if (compact) 15.dp else 18.dp)
                    )
                }
                if (item.isStale) {
                    Spacer(modifier = Modifier.size(6.dp))
                    Icon(
                        imageVector = Icons.Filled.CloudOff,
                        contentDescription = stringResource(R.string.stale_data_content_description),
                        tint = fg.copy(alpha = 0.6f),
                        modifier = Modifier.size(if (compact) 13.dp else 16.dp)
                    )
                }
            }
            Text(
                text = formatLocalTime(item.timezone),
                fontSize = if (compact) 16.sp else 20.sp,
                fontWeight = FontWeight.Light,
                color = fg
            )
        }

        if (item.temperature != null && item.icon != null) {
            Icon(
                painter = painterResource(getCorrectConditionIcon(item.icon)),
                contentDescription = null,
                tint = if (isNight(item)) White else androidx.compose.ui.graphics.Color.Unspecified,
                modifier = Modifier.size(if (compact) 38.dp else 48.dp)
            )
            Spacer(modifier = Modifier.size(14.dp))
            Text(text = formatTemperature(item.temperature, units), fontSize = if (compact) 29.sp else 36.sp, color = fg)
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
        val onBackground = MaterialTheme.colorScheme.onBackground
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.background)
                .border(2.dp, onBackground, RoundedCornerShape(14.dp))
        ) {
            val compact = isCompactWidth()
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                singleLine = true,
                textStyle = TextStyle(fontSize = if (compact) 16.sp else 18.sp, color = onBackground),
                cursorBrush = SolidColor(Orange),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(text = stringResource(R.string.cities_search_placeholder), fontSize = if (compact) 16.sp else 18.sp, color = Gray)
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
            containerColor = Black,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .border(1.dp, Gray.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
        ) {
            results.forEachIndexed { index, result ->
                val subtitle = listOfNotNull(result.state, result.countryCode)
                    .filter { it.isNotBlank() }
                    .joinToString(", ")

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onResultClick(result) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Orange,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = result.city, color = White, fontSize = 16.sp)
                        if (subtitle.isNotBlank()) {
                            Text(text = subtitle, color = Gray, fontSize = 13.sp)
                        }
                    }
                }

                if (index != results.lastIndex) {
                    HorizontalDivider(color = Gray.copy(alpha = 0.12f), thickness = 0.5.dp)
                }
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
            contentDescription = stringResource(R.string.cities_delete_content_description),
            tint = White,
            modifier = Modifier.padding(end = 20.dp).size(22.dp)
        )
    }
}