package dev.dmil.skye.domain.usecase

import dev.dmil.skye.domain.model.DailyForecast
import dev.dmil.skye.domain.model.Weather
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.abs

class GetWeeklyForecastUseCase @Inject constructor() {

    operator fun invoke(forecast: List<Weather>): List<DailyForecast> {
        if (forecast.isEmpty()) return emptyList()

        return forecast
            .groupBy { localEpochDay(it) }
            .toSortedMap()
            .map { (epochDay, dayEntriesUnsorted) ->
                val dayEntries = dayEntriesUnsorted.sortedBy { it.date }
                val representative = dayEntries.minByOrNull { abs(localHour(it) - 12) } ?: dayEntries.first()
                DailyForecast(
                    date = LocalDate.ofEpochDay(epochDay),
                    icon = representative.icon,
                    minTemp = dayEntries.minOf { it.temperature.toInt() },
                    maxTemp = dayEntries.maxOf { it.temperature.toInt() },
                    windSpeed = representative.windSpeed,
                    windDegree = representative.windDegree,
                    feelsLike = representative.feelsLike.toInt(),
                    humidity = representative.humidity.toInt(),
                    pressure = representative.pressure.toInt(),
                    description = representative.description.replaceFirstChar { it.uppercase() },
                    hourly = dayEntries
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