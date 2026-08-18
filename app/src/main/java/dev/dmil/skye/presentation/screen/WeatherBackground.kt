package dev.dmil.skye.presentation.screen

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.dmil.skye.presentation.ui.theme.White
import kotlin.random.Random


@Composable
fun WeatherConditionBackground(icon: String, modifier: Modifier = Modifier) {
    when {
        icon.startsWith("09") || icon.startsWith("10") || icon.startsWith("11") ->
            RainBackground(modifier)
        icon.startsWith("02") || icon.startsWith("03") || icon.startsWith("04") ->
            CloudyBackground(modifier)
        icon == "01n" -> ClearNightBackground(modifier)
        else -> Unit
    }
}

private data class Raindrop(
    val xFraction: Float,
    val length: Dp,
    val fallDurationSeconds: Float,
    val alpha: Float,
    val phase: Float
)

@Composable
private fun RainBackground(modifier: Modifier = Modifier) {
    val dropColor = MaterialTheme.colorScheme.onBackground

    val drops = remember {
        List(26) {
            Raindrop(
                xFraction = Random.nextFloat(),
                length = (22 + Random.nextInt(18)).dp,
                fallDurationSeconds = 0.4f + Random.nextFloat() * 0.5f,
                alpha = 0.05f + Random.nextFloat() * 0.07f,
                phase = Random.nextFloat()
            )
        }
    }

    var elapsedSeconds by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        var lastNanos = withFrameNanos { it }
        while (true) {
            val nanos = withFrameNanos { it }
            elapsedSeconds += (nanos - lastNanos) / 1_000_000_000f
            lastNanos = nanos
        }
    }

    Canvas(modifier = modifier) {
        val h = size.height
        val w = size.width

        drops.forEach { drop ->
            val lengthPx = drop.length.toPx()
            val progress = ((elapsedSeconds / drop.fallDurationSeconds) + drop.phase) % 1f
            val y = progress * (h + lengthPx) - lengthPx
            val x = drop.xFraction * w

            drawLine(
                color = dropColor.copy(alpha = drop.alpha),
                start = Offset(x, y),
                end = Offset(x, y + lengthPx),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}


private val cloudPathData = listOf(
    // wi_cloud
    "M0,9.49C0,8.34 0.36,7.32 1.08,6.42C1.8,5.52 2.71,4.94 3.82,4.69C4.13,3.32 4.84,2.2 5.93,1.32C7.02,0.44 8.28,0 9.69,0C11.07,0 12.3,0.43 13.38,1.28C14.46,2.13 15.16,3.23 15.48,4.57H15.81C16.71,4.57 17.54,4.79 18.3,5.22C19.06,5.65 19.67,6.25 20.11,7.01C20.55,7.77 20.78,8.59 20.78,9.49C20.78,10.37 20.57,11.19 20.15,11.94C19.73,12.69 19.15,13.29 18.42,13.74C17.69,14.19 16.88,14.43 16.01,14.46H4.8C3.46,14.4 2.33,13.89 1.4,12.93C0.47,11.98 0,10.83 0,9.49ZM1.71,9.49C1.71,10.36 2.01,11.11 2.61,11.75C3.21,12.39 3.94,12.73 4.8,12.78H15.99C16.85,12.74 17.58,12.39 18.18,11.75C18.79,11.11 19.09,10.35 19.09,9.49C19.09,8.61 18.76,7.86 18.11,7.22C17.46,6.58 16.69,6.26 15.79,6.26H14.19C14.08,6.26 14.02,6.2 14.02,6.08L13.95,5.51C13.84,4.43 13.37,3.52 12.55,2.79C11.73,2.06 10.78,1.69 9.69,1.69C8.6,1.69 7.64,2.06 6.84,2.79C6.03,3.52 5.57,4.43 5.47,5.51L5.39,6.08C5.39,6.2 5.32,6.26 5.19,6.26H4.66C3.82,6.36 3.12,6.72 2.56,7.33C2,7.94 1.71,8.66 1.71,9.49Z"
            to Pair(21f, 15f),
    // wi_cloudy
    "M0,10.3C0,9.31 0.31,8.42 0.93,7.65C1.55,6.88 2.34,6.38 3.31,6.16C3.57,4.99 4.16,4.02 5.09,3.28C6.02,2.53 7.09,2.16 8.31,2.16C9.49,2.16 10.55,2.52 11.47,3.25C12.4,3.98 13,4.91 13.27,6.05H13.54C14.72,6.05 15.72,6.46 16.55,7.29C17.38,8.12 17.8,9.12 17.8,10.29C17.8,11.47 17.38,12.47 16.55,13.3C15.72,14.13 14.72,14.55 13.54,14.55H4.27C3.69,14.55 3.14,14.44 2.62,14.21C2.1,13.98 1.63,13.7 1.25,13.32C0.87,12.94 0.57,12.48 0.34,11.96C0.11,11.44 0,10.87 0,10.3ZM1.45,10.3C1.45,11.06 1.73,11.72 2.27,12.26C2.81,12.8 3.48,13.08 4.26,13.08H13.54C14.31,13.08 14.98,12.81 15.53,12.26C16.08,11.71 16.36,11.06 16.36,10.3C16.36,9.54 16.09,8.88 15.53,8.34C14.98,7.8 14.32,7.52 13.54,7.52H12.15C12.05,7.52 12,7.47 12,7.37L11.93,6.88C11.83,5.94 11.43,5.15 10.74,4.53C10.05,3.91 9.23,3.6 8.29,3.6C7.35,3.6 6.53,3.91 5.83,4.54C5.13,5.16 4.74,5.95 4.65,6.88L4.58,7.3C4.58,7.4 4.53,7.45 4.42,7.45L3.97,7.52C3.25,7.58 2.65,7.88 2.16,8.41C1.7,8.94 1.45,9.57 1.45,10.3ZM10.3,1.58C10.2,1.67 10.22,1.74 10.37,1.79C10.8,1.98 11.16,2.16 11.45,2.34C11.56,2.37 11.64,2.36 11.67,2.31C12.28,1.74 12.98,1.45 13.79,1.45C14.6,1.45 15.29,1.72 15.89,2.26C16.48,2.8 16.81,3.47 16.88,4.26L16.97,4.9H18.39C19.04,4.9 19.6,5.13 20.07,5.6C20.54,6.07 20.77,6.62 20.77,7.26C20.77,7.86 20.56,8.38 20.15,8.83C19.74,9.28 19.23,9.53 18.62,9.6C18.52,9.6 18.47,9.65 18.47,9.76V10.89C18.47,11 18.52,11.05 18.62,11.05C19.63,10.99 20.48,10.59 21.17,9.86C21.86,9.13 22.21,8.26 22.21,7.26C22.21,6.2 21.84,5.3 21.09,4.56C20.34,3.81 19.44,3.44 18.39,3.44H18.24C17.98,2.44 17.43,1.62 16.59,0.97C15.76,0.32 14.82,0 13.79,0C12.39,-0.01 11.22,0.52 10.3,1.58Z"
            to Pair(23f, 15f)
)

private data class DriftingCloud(
    val laneYFraction: Float,
    val startXFraction: Float,
    val scale: Float,
    val driftSeconds: Float,
    val alpha: Float,
    val pathIndex: Int,
    val rotationDegrees: Float
)

@Composable
private fun CloudyBackground(modifier: Modifier = Modifier) {
    val cloudColor = MaterialTheme.colorScheme.onBackground

    val laneCount = 15
    val clouds = remember {
        List(laneCount) { lane ->
            val laneHeight = 0.8f / laneCount
            DriftingCloud(
                laneYFraction = 0.1f + lane * laneHeight + Random.nextFloat() * laneHeight * 0.6f,
                startXFraction = Random.nextFloat(),
                scale = 6.5f + Random.nextFloat() * 3f,
                driftSeconds = 45f + Random.nextFloat() * 35f,
                alpha = 0.05f + Random.nextFloat() * 0.05f,
                pathIndex = Random.nextInt(cloudPathData.size),
                rotationDegrees = -8f + Random.nextFloat() * 16f
            )
        }
    }

    val parsedPaths = remember {
        cloudPathData.map { (data, _) -> PathParser().parsePathString(data).toPath() }
    }

    var elapsedSeconds by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        var lastNanos = withFrameNanos { it }
        while (true) {
            val nanos = withFrameNanos { it }
            elapsedSeconds += (nanos - lastNanos) / 1_000_000_000f
            lastNanos = nanos
        }
    }

    Canvas(modifier = modifier.blur(radius = 8.dp)) {
        val w = size.width
        val baseUnit = 6.dp.toPx()

        clouds.forEach { cloud ->
            val (_, viewport) = cloudPathData[cloud.pathIndex]
            val (viewW, viewH) = viewport
            val path = parsedPaths[cloud.pathIndex]

            val targetWidth = baseUnit * cloud.scale
            val s = targetWidth / viewW

            val progress = ((elapsedSeconds / cloud.driftSeconds) + cloud.startXFraction) % 1f
            val x = -targetWidth * 1.5f + progress * (w + targetWidth * 3f)
            val y = cloud.laneYFraction * size.height

            translate(left = x, top = y) {
                rotate(degrees = cloud.rotationDegrees, pivot = Offset(viewW * s / 2f, viewH * s / 2f)) {
                    scale(scaleX = s, scaleY = s, pivot = Offset.Zero) {
                        drawPath(path = path, color = cloudColor.copy(alpha = cloud.alpha))
                    }
                }
            }
        }
    }
}

private data class TwinklingStar(
    val xFraction: Float,
    val yFraction: Float,
    val baseRadius: Float,
    val twinkleSeconds: Float,
    val phase: Float,
    val maxAlpha: Float
)

@Composable
private fun ClearNightBackground(modifier: Modifier = Modifier) {
    val stars = remember {
        List(40) {
            TwinklingStar(
                xFraction = Random.nextFloat(),
                yFraction = Random.nextFloat(),
                baseRadius = 1f + Random.nextFloat() * 1.5f,
                twinkleSeconds = 1.5f + Random.nextFloat() * 2.5f,
                phase = Random.nextFloat(),
                maxAlpha = 0.15f + Random.nextFloat() * 0.2f
            )
        }
    }

    var elapsedSeconds by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        var lastNanos = withFrameNanos { it }
        while (true) {
            val nanos = withFrameNanos { it }
            elapsedSeconds += (nanos - lastNanos) / 1_000_000_000f
            lastNanos = nanos
        }
    }

    Canvas(modifier = modifier) {
        stars.forEach { star ->
            val cycle = ((elapsedSeconds / star.twinkleSeconds) + star.phase) % 1f
            val brightness = 1f - kotlin.math.abs(cycle * 2f - 1f)
            val alpha = brightness * star.maxAlpha

            drawCircle(
                color = White.copy(alpha = alpha),
                radius = star.baseRadius.dp.toPx(),
                center = Offset(star.xFraction * size.width, star.yFraction * size.height)
            )
        }
    }
}