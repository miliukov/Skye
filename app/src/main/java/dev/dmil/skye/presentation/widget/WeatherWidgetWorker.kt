package dev.dmil.skye.presentation.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.dmil.skye.domain.usecase.GetWeatherUseCase

@HiltWorker
class WeatherWidgetWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val getWeatherUseCase: GetWeatherUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val appWidgetId = inputData.getInt(KEY_APP_WIDGET_ID, -1)
        val lat = inputData.getDouble(KEY_LAT, Double.NaN)
        val lon = inputData.getDouble(KEY_LON, Double.NaN)

        if (appWidgetId == -1 || lat.isNaN() || lon.isNaN()) return Result.failure()

        val glanceId = runCatching {
            GlanceAppWidgetManager(applicationContext).getGlanceIdBy(appWidgetId)
        }.getOrNull() ?: return Result.failure()

        val weatherResult = getWeatherUseCase(lat, lon)
        val weather = weatherResult.getOrNull() ?: return Result.retry()

        updateAppWidgetState(applicationContext, glanceId) { prefs ->
            prefs[WidgetPrefKeys.TEMPERATURE] = weather.temperature
            prefs[WidgetPrefKeys.ICON] = weather.icon
            prefs[WidgetPrefKeys.DESCRIPTION] = weather.description.replaceFirstChar { it.uppercase() }
            prefs[WidgetPrefKeys.LAST_UPDATED] = System.currentTimeMillis()
        }

        SkyeWidget().update(applicationContext, glanceId)

        return Result.success()
    }

    companion object {
        const val KEY_APP_WIDGET_ID = "app_widget_id"
        const val KEY_LAT = "lat"
        const val KEY_LON = "lon"
    }
}