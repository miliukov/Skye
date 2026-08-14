package dev.dmil.skye.presentation.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.background
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.color.ColorProvider
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import dev.dmil.skye.R
import dev.dmil.skye.presentation.MainActivity

class SkyeWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val cityName = prefs[WidgetPrefKeys.CITY_NAME]
            val temperature = prefs[WidgetPrefKeys.TEMPERATURE]
            val icon = prefs[WidgetPrefKeys.ICON]
            val description = prefs[WidgetPrefKeys.DESCRIPTION]
            val lastUpdated = prefs[WidgetPrefKeys.LAST_UPDATED]

            val primaryColor = ColorProvider(day = Color(0xFF2B2829), night = Color(0xFFF5F5F5))
            val secondaryColor = ColorProvider(day = Color(0xFF6F6F6F), night = Color(0xFF9A9A9A))
            val isCompact = LocalSize.current.height < 70.dp

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(
                        day = Color(0xFFF5F5F5),
                        night = Color(0xFF2B2829)
                    )
                    .cornerRadius(20.dp)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clickable(actionStartActivity<MainActivity>())
            ) {
                when {
                    cityName == null -> {
                        Text(
                            text = context.getString(R.string.widget_tap_to_setup),
                            style = TextStyle(fontSize = 13.sp, color = primaryColor)
                        )
                    }

                    isCompact -> {
                        Column(modifier = GlanceModifier.fillMaxSize()) {
                            Text(
                                text = cityName,
                                maxLines = 1,
                                style = TextStyle(fontSize = 14.sp, color = primaryColor)
                            )
                            Row(
                                modifier = GlanceModifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                icon?.let {
                                    Image(
                                        provider = ImageProvider(getWidgetIconRes(it)),
                                        contentDescription = null,
                                        modifier = GlanceModifier.size(26.dp),
                                        colorFilter = if (it != "01d") {
                                            ColorFilter.tint(ColorProvider(day = Color(0xFF2B2829), night = Color(0xFFF5F5F5)))
                                        } else null
                                    )
                                }
                                Spacer(modifier = GlanceModifier.defaultWeight())
                                Text(
                                    text = temperature?.let { "${it.toInt()}º" } ?: "…",
                                    style = TextStyle(
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryColor
                                    )
                                )
                            }
                        }
                    }

                    else -> {
                        Text(
                            text = cityName,
                            style = TextStyle(fontSize = 15.sp, color = primaryColor)
                        )
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = GlanceModifier.defaultWeight()) {
                                icon?.let {
                                    Image(
                                        provider = ImageProvider(getWidgetIconRes(it)),
                                        contentDescription = null,
                                        modifier = GlanceModifier.size(40.dp),
                                        colorFilter = if (it != "01d") {
                                            ColorFilter.tint(ColorProvider(day = Color(0xFF2B2829), night = Color(0xFFF5F5F5)))
                                        } else null
                                    )
                                }
                                description?.let {
                                    Text(
                                        text = truncate(it, 14),
                                        style = TextStyle(fontSize = 9.sp, color = secondaryColor)
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = temperature?.let { "${it.toInt()}º" } ?: "…",
                                    style = TextStyle(
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryColor
                                    )
                                )
                                lastUpdated?.let {
                                    Text(
                                        text = formatUpdatedAgo(context, it),
                                        style = TextStyle(fontSize = 10.sp, color = secondaryColor)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun truncate(text: String, maxLength: Int): String {
    return if (text.length <= maxLength) text else text.take(maxLength).trimEnd() + "…"
}

private fun getWidgetIconRes(iconId: String): Int = when (iconId) {
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

private fun formatUpdatedAgo(context: Context, epochMillis: Long): String {
    val minutes = (System.currentTimeMillis() - epochMillis) / 60000
    return when {
        minutes < 1 -> context.getString(R.string.widget_updated_just_now)
        minutes < 60 -> context.getString(R.string.widget_updated_minutes_ago, minutes)
        else -> context.getString(R.string.widget_updated_hours_ago, minutes / 60)
    }
}