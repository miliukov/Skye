package dev.dmil.skye.domain.repository

import dev.dmil.skye.domain.model.ThemeMode
import dev.dmil.skye.domain.model.Units
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val themeMode: Flow<ThemeMode>
    val units: Flow<Units>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setUnits(units: Units)
}