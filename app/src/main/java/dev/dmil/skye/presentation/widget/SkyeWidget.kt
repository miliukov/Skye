package dev.dmil.skye.presentation.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
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

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(
                        day = Color(0xFFF5F5F5),
                        night = Color(0xFFF5F5F5)
                    )
                    .cornerRadius(20.dp)
                    .padding(12.dp)
                    .clickable(actionStartActivity<MainActivity>())
            ) {
                if (cityName == null) {
                    Text(
                        text = context.getString(R.string.widget_tap_to_setup),
                        style = TextStyle(fontSize = 13.sp, color = ColorProvider(day = Color(0xFF2B2829), night = Color(0xFF2B2829)))
                    )
                } else {
                    Text(
                        text = cityName,
                        style = TextStyle(fontSize = 15.sp, color = ColorProvider(day = Color(0xFF2B2829), night = Color(0xFF2B2829)))
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
                                    modifier = GlanceModifier.size(40.dp)
                                )
                            }
                            description?.let {
                                Text(
                                    text = truncate(it, 14),
                                    style = TextStyle(fontSize = 9.sp, color = ColorProvider(day = Color(0xFF6F6F6F), night = Color(0xFF6F6F6F)))
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = temperature?.let { "${it.toInt()}º" } ?: "…",
                                style = TextStyle(
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorProvider(day = Color(0xFF2B2829), night = Color(0xFF2B2829))
                                )
                            )
                            lastUpdated?.let {
                                Text(
                                    text = formatUpdatedAgo(context, it),
                                    style = TextStyle(fontSize = 10.sp, color = ColorProvider(day = Color(0xFF6F6F6F), night = Color(0xFF6F6F6F)))
                                )
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
    "03d", "03n", "04d", "04n" -> R.drawable.wi_cloudy
    "10d" -> R.drawable.wi_day_rain
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