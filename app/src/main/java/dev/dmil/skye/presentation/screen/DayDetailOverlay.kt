package dev.dmil.skye.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.dmil.skye.R
import dev.dmil.skye.domain.model.DailyForecast
import dev.dmil.skye.domain.model.Units
import dev.dmil.skye.domain.model.Weather
import dev.dmil.skye.presentation.ui.theme.Black
import dev.dmil.skye.presentation.ui.theme.Gray
import dev.dmil.skye.presentation.ui.theme.Orange
import dev.dmil.skye.presentation.ui.theme.White
import dev.dmil.skye.presentation.util.formatTemperature
import dev.dmil.skye.presentation.util.formatWindSpeed
import java.time.format.TextStyle as DateTextStyle
import kotlin.collections.mapIndexed
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import dev.dmil.skye.presentation.util.isCompactWidth
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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
        val scope = rememberCoroutineScope()
        val offsetY = remember { Animatable(0f) }
        var containerHeightPx by remember { mutableFloatStateOf(0f) }
        val dismissThresholdPx = with(LocalDensity.current) { 120.dp.toPx() }

        val scrimAlpha by remember {
            derivedStateOf {
                val progress = if (containerHeightPx > 0f) (offsetY.value / containerHeightPx).coerceIn(0f, 1f) else 0f
                0.4f * (1f - progress)
            }
        }

        fun dragBy(delta: Float) {
            scope.launch { offsetY.snapTo((offsetY.value + delta).coerceAtLeast(0f)) }
        }

        suspend fun settleDrag() {
            if (offsetY.value > dismissThresholdPx) {
                offsetY.animateTo(
                    targetValue = if (containerHeightPx > 0f) containerHeightPx else offsetY.value + 800f,
                    animationSpec = tween(220, easing = FastOutSlowInEasing)
                )
                onDismiss()
            } else {
                offsetY.animateTo(
                    0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium)
                )
            }
        }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = scrimAlpha))
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

            LaunchedEffect(visible) {
                if (visible) offsetY.snapTo(0f)
            }

            val nestedScrollConnection = remember {
                object : NestedScrollConnection {
                    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                        if (offsetY.value > 0f && available.y < 0f) {
                            val consumed = maxOf(available.y, -offsetY.value)
                            dragBy(consumed)
                            return Offset(0f, consumed)
                        }
                        return Offset.Zero
                    }

                    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                        if (available.y > 0f) {
                            dragBy(available.y)
                            return Offset(0f, available.y)
                        }
                        return Offset.Zero
                    }

                    override suspend fun onPreFling(available: Velocity): Velocity {
                        if (offsetY.value > 0f) {
                            settleDrag()
                            return available
                        }
                        return Velocity.Zero
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned { containerHeightPx = it.size.height.toFloat() }
                    .offset { IntOffset(0, offsetY.value.roundToInt()) }
                    .background(Black, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .padding(top = 12.dp, start = 16.dp, end = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onDragEnd = { scope.launch { settleDrag() } },
                                onDragCancel = { scope.launch { settleDrag() } },
                                onVerticalDrag = { change, dragAmount ->
                                    dragBy(dragAmount)
                                    change.consume()
                                }
                            )
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(36.dp)
                            .height(4.dp)
                            .background(Gray.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            days.forEachIndexed { index, d ->
                                DatePill(
                                    day = d,
                                    isToday = d.date == java.time.LocalDate.now(),
                                    isSelected = index == selectedIndex,
                                    onClick = { onDaySelected(index) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val compact = isCompactWidth()
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(text = formatTemperature(day.maxTemp, units), fontSize = if (compact) 52.sp else 60.sp, color = White)
                        Text(text = formatTemperature(day.minTemp, units), fontSize = if (compact) 44.sp else 50.sp, color = Gray)
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .navigationBarsPadding()
                        .nestedScroll(nestedScrollConnection)
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
    val compact = isCompactWidth()
    val boxSize = if (compact) 30.dp else 34.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Text(
            text = day.date.dayOfWeek
                .getDisplayName(DateTextStyle.SHORT, LocalLocale.current.platformLocale)
                .replaceFirstChar { it.uppercase() },
            fontSize = if (compact) 14.sp else 16.sp,
            color = if (isToday) Orange else White.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .size(boxSize)
                .then(if (isSelected) Modifier.background(White, CircleShape) else Modifier),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                fontSize = if (compact) 19.sp else 22.sp,
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

    val compact = isCompactWidth()
    val textMeasurer = rememberTextMeasurer()
    val tempStyle = TextStyle(fontSize = if (compact) 12.sp else 13.sp, color = White)
    val hourStyle = TextStyle(fontSize = if (compact) 11.sp else 12.sp, color = Gray)
    val bubbleStyle = TextStyle(fontSize = if (compact) 11.sp else 12.sp, fontWeight = FontWeight.Bold, color = Black)

    val interpolated = interpolateHourly(hourly)
    var touchX by remember { mutableStateOf<Float?>(null) }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .pointerInput(interpolated) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    touchX = down.position.x
                    do {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.pressed }
                        if (change != null) {
                            touchX = change.position.x
                            change.consume()
                        }
                    } while (event.changes.any { it.pressed })
                    touchX = null
                }
            }
    ) {
        val minTemp = interpolated.minOf { it.temperature }
        val maxTemp = interpolated.maxOf { it.temperature }
        val range = (maxTemp - minTemp).let { if (it == 0.0) 1.0 else it }

        val topPadding = 52.dp.toPx()
        val bottomPadding = 24.dp.toPx()
        val graphHeight = size.height - topPadding - bottomPadding
        val stepX = size.width / (interpolated.size - 1)

        val points = interpolated.mapIndexed { index, w ->
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

        interpolated.forEachIndexed { index, w ->
            val isRealPoint = hourly.any { it.date == w.date }
            if (!isRealPoint) return@forEachIndexed

            val point = points[index]
            drawCircle(color = Orange, radius = 4.dp.toPx(), center = point)

            val tempLayout = textMeasurer.measure(formatTemperature(w.temperature, units), tempStyle)
            drawText(
                textLayoutResult = tempLayout,
                topLeft = Offset(point.x - tempLayout.size.width / 2f, point.y - tempLayout.size.height - 10.dp.toPx())
            )

            val hourLayout = textMeasurer.measure(unixToHour(w.date, w.timezone).toString(), hourStyle)
            drawText(
                textLayoutResult = hourLayout,
                topLeft = Offset(point.x - hourLayout.size.width / 2f, size.height - hourLayout.size.height)
            )
        }

        touchX?.let { tx ->
            val index = (tx / stepX).roundToInt().coerceIn(0, interpolated.size - 1)
            val point = points[index]
            val touched = interpolated[index]

            drawLine(
                color = White.copy(alpha = 0.35f),
                start = Offset(point.x, 28.dp.toPx()),
                end = Offset(point.x, size.height - bottomPadding),
                strokeWidth = 1.5.dp.toPx()
            )
            drawCircle(color = White, radius = 5.dp.toPx(), center = point)
            drawCircle(color = Orange, radius = 3.dp.toPx(), center = point)

            val bubbleText = "${formatTemperature(touched.temperature, units)} · ${unixToHour(touched.date, touched.timezone)}:00"
            val bubbleLayout = textMeasurer.measure(bubbleText, bubbleStyle)
            val paddingH = 6.dp.toPx()
            val paddingV = 3.dp.toPx()
            val bubbleWidth = bubbleLayout.size.width + paddingH * 2
            val bubbleHeight = bubbleLayout.size.height + paddingV * 2
            val bubbleX = (point.x - bubbleWidth / 2f).coerceIn(0f, size.width - bubbleWidth)
            val bubbleY = 4.dp.toPx()

            drawRoundRect(
                color = White,
                topLeft = Offset(bubbleX, bubbleY),
                size = androidx.compose.ui.geometry.Size(bubbleWidth, bubbleHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
            )
            drawText(
                textLayoutResult = bubbleLayout,
                topLeft = Offset(bubbleX + paddingH, bubbleY + paddingV)
            )
        }
    }
}

@Composable
private fun DayInfoGrid(day: DailyForecast, units: Units) {
    val items = listOf(
        stringResource(R.string.day_detail_feels_like) to formatTemperature(day.feelsLike, units),
        stringResource(R.string.day_detail_humidity) to "${day.humidity}%",
        stringResource(R.string.day_detail_pressure) to "${day.pressure} ${stringResource(R.string.unit_hectopascal)}",
        stringResource(R.string.day_detail_wind) to formatWindSpeed(day.windSpeed, units)
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
        InfoCard(
            label = stringResource(R.string.day_detail_conditions),
            value = day.description,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun InfoCard(label: String, value: String, modifier: Modifier = Modifier) {
    val compact = isCompactWidth()
    Column(
        modifier = modifier
            .border(1.5.dp, Gray.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(if (compact) 12.dp else 16.dp)
    ) {
        Text(text = label, fontSize = if (compact) 12.sp else 13.sp, color = Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontSize = if (compact) 19.sp else 22.sp, color = White)
    }
}