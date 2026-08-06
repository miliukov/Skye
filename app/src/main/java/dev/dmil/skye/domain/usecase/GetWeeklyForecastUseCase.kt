package dev.dmil.skye.domain.usecase

import dev.dmil.skye.domain.model.DailyForecast
import dev.dmil.skye.domain.model.Weather
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject
import kotlin.math.abs

class GetWeeklyForecastUseCase @Inject constructor() {

    operator fun invoke(forecast: List<Weather>): List<DailyForecast> {
        if (forecast.isEmpty()) return emptyList()

        val today = localEpochDay(forecast.first())

        return forecast
            .groupBy { localEpochDay(it) }
            .toSortedMap()
            .map { (epochDay, dayEntries) ->
                val representative = dayEntries.minByOrNull { abs(localHour(it) - 12) } ?: dayEntries.first()
                DailyForecast(
                    dayLabel = if (epochDay == today) {
                        "Today"
                    } else {
                        LocalDate.ofEpochDay(epochDay).dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                    },
                    icon = representative.icon,
                    minTemp = dayEntries.minOf { it.temperature.toInt() },
                    maxTemp = dayEntries.maxOf { it.temperature.toInt() },
                    windSpeed = representative.windSpeed,
                    windDegree = representative.windDegree
                )
            }
    }

    private fun localEpochDay(weather: Weather): Long {
        val localSeconds = weather.date + weather.timezone
        return localSeconds / 86400
    }

    private fun localHour(weather: Weather): Int {
        val localSeconds = weather.date + weather.timezone
        return ((localSeconds % 86400) / 3600).toInt()
    }
}