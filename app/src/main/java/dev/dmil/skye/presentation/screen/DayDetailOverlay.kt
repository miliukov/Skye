package dev.dmil.skye.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.dmil.skye.domain.model.DailyForecast
import dev.dmil.skye.domain.model.Units
import dev.dmil.skye.domain.model.Weather
import dev.dmil.skye.presentation.ui.theme.Black
import dev.dmil.skye.presentation.ui.theme.Gray
import dev.dmil.skye.presentation.ui.theme.Orange
import dev.dmil.skye.presentation.ui.theme.White
import dev.dmil.skye.presentation.util.formatTemperature
import dev.dmil.skye.presentation.util.formatWindSpeed
import java.util.Locale
import java.time.format.TextStyle as DateTextStyle
import kotlin.collections.mapIndexed

@Composable
fun DayDetailOverlay(
    visible: Boolean,
    days: List<DailyForecast>,
    selectedIndex: Int,
    onDaySelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    units: Units,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(onClick = onDismiss)
            )
        }

        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(tween(300, easing = FastOutSlowInEasing)) { it },
            exit = slideOutVertically(tween(300, easing = FastOutSlowInEasing)) { it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
        ) {
            if (days.isEmpty() || selectedIndex !in days.indices) return@AnimatedVisibility
            val day = days[selectedIndex]

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Black, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .padding(top = 12.dp, start = 16.dp, end = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(36.dp)
                        .height(4.dp)
                        .background(Gray.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                )

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        days.forEachIndexed { index, d ->
                            DatePill(
                                day = d,
                                isToday = d.dayLabel == "Today",
                                isSelected = index == selectedIndex,
                                onClick = { onDaySelected(index) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(text = formatTemperature(day.maxTemp, units), fontSize = 60.sp, color = White)
                    Text(text = formatTemperature(day.minTemp, units), fontSize = 50.sp, color = Gray)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .navigationBarsPadding()
                        .verticalScroll(rememberScrollState())
                ) {
                    DayTemperatureGraph(hourly = day.hourly, units = units)
                    Spacer(modifier = Modifier.height(28.dp))
                    DayInfoGrid(day = day, units = units)
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun DatePill(
    day: DailyForecast,
    isToday: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Text(
            text = day.date.dayOfWeek.getDisplayName(DateTextStyle.NARROW, Locale.ENGLISH),
            fontSize = 16.sp,
            color = if (isToday) Orange else White.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .size(34.dp)
                .then(if (isSelected) Modifier.background(White, CircleShape) else Modifier),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                fontSize = 22.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isSelected -> Black
                    isToday -> Orange
                    else -> White
                }
            )
        }
    }
}

@Composable
fun DayTemperatureGraph(hourly: List<Weather>, units: Units, modifier: Modifier = Modifier) {
    if (hourly.size < 2) return

    val textMeasurer = rememberTextMeasurer()
    val tempStyle = TextStyle(fontSize = 13.sp, color = White)
    val hourStyle = TextStyle(fontSize = 12.sp, color = Gray)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        val minTemp = hourly.minOf { it.temperature }
        val maxTemp = hourly.maxOf { it.temperature }
        val range = (maxTemp - minTemp).let { if (it == 0.0) 1.0 else it }

        val topPadding = 28.dp.toPx()
        val bottomPadding = 24.dp.toPx()
        val graphHeight = size.height - topPadding - bottomPadding
        val stepX = size.width / (hourly.size - 1)

        val points = hourly.mapIndexed { index, w ->
            val x = index * stepX
            val normalized = ((w.temperature - minTemp) / range).toFloat()
            val y = topPadding + (graphHeight - normalized * graphHeight)
            Offset(x, y)
        }

        for (i in 0 until points.size - 1) {
            drawLine(
                color = Orange,
                start = points[i],
                end = points[i + 1],
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        points.forEachIndexed { index, point ->
            drawCircle(color = Orange, radius = 4.dp.toPx(), center = point)

            val tempLayout = textMeasurer.measure(formatTemperature(hourly[index].temperature, units), tempStyle)
            drawText(
                textLayoutResult = tempLayout,
                topLeft = Offset(point.x - tempLayout.size.width / 2f, point.y - tempLayout.size.height - 10.dp.toPx())
            )

            val hourLayout = textMeasurer.measure(unixToHour(hourly[index].date, hourly[index].timezone).toString(), hourStyle)
            drawText(
                textLayoutResult = hourLayout,
                topLeft = Offset(point.x - hourLayout.size.width / 2f, size.height - hourLayout.size.height)
            )
        }
    }
}

@Composable
private fun DayInfoGrid(day: DailyForecast, units: Units) {
    val items = listOf(
        "Feels like" to formatTemperature(day.feelsLike, units),
        "Humidity" to "${day.humidity}%",
        "Pressure" to "${day.pressure} hPa",
        "Wind" to formatWindSpeed(day.windSpeed, units)
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.chunked(2).forEach { rowItems ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowItems.forEach { (label, value) ->
                    InfoCard(label = label, value = value, modifier = Modifier.weight(1f))
                }
            }
        }
        InfoCard(label = "Conditions", value = day.description, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun InfoCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .border(1.5.dp, Gray.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(text = label, fontSize = 13.sp, color = Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontSize = 22.sp, color = White)
    }
}