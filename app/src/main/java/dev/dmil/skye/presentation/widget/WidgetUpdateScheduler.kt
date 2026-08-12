package dev.dmil.skye.presentation.widget

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Constraints
import java.util.concurrent.TimeUnit

object WidgetUpdateScheduler {

    fun schedule(context: Context, appWidgetId: Int, lat: Double, lon: Double) {
        val data = Data.Builder()
            .putInt(WeatherWidgetWorker.KEY_APP_WIDGET_ID, appWidgetId)
            .putDouble(WeatherWidgetWorker.KEY_LAT, lat)
            .putDouble(WeatherWidgetWorker.KEY_LON, lon)
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val oneTimeRequest = OneTimeWorkRequestBuilder<WeatherWidgetWorker>()
            .setInputData(data)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context).enqueue(oneTimeRequest)

        val periodicRequest = PeriodicWorkRequestBuilder<WeatherWidgetWorker>(30, TimeUnit.MINUTES)
            .setInputData(data)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            uniqueWorkName(appWidgetId),
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicRequest
        )
    }

    fun cancel(context: Context, appWidgetId: Int) {
        WorkManager.getInstance(context).cancelUniqueWork(uniqueWorkName(appWidgetId))
    }

    private fun uniqueWorkName(appWidgetId: Int) = "widget_update_$appWidgetId"
}