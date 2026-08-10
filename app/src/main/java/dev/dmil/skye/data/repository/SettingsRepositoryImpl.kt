package dev.dmil.skye.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.dmil.skye.domain.model.ThemeMode
import dev.dmil.skye.domain.model.Units
import dev.dmil.skye.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val unitsKey = stringPreferencesKey("units")

    override val themeMode = dataStore.data.map { prefs ->
        prefs[themeModeKey]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM
    }

    override val units = dataStore.data.map { prefs ->
        prefs[unitsKey]?.let { runCatching { Units.valueOf(it) }.getOrNull() } ?: Units.METRIC
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[themeModeKey] = mode.name }
    }

    override suspend fun setUnits(units: Units) {
        dataStore.edit { it[unitsKey] = units.name }
    }
}