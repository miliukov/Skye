package dev.dmil.skye.domain.repository

import dev.dmil.skye.domain.model.Language
import dev.dmil.skye.domain.model.ThemeMode
import dev.dmil.skye.domain.model.Units
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val themeMode: Flow<ThemeMode>
    val units: Flow<Units>
    val apiKey: Flow<String?>
    val apiKeySetAt: Flow<Long?>
    val language: Flow<Language>
    val hasCompletedOnboarding: Flow<Boolean>
    suspend fun setOnboardingCompleted()
    suspend fun setLanguage(language: Language)
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setUnits(units: Units)
    suspend fun setApiKey(key: String?)
}