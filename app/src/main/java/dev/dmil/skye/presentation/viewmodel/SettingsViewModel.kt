package dev.dmil.skye.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.dmil.skye.domain.model.Language
import dev.dmil.skye.domain.model.ThemeMode
import dev.dmil.skye.domain.model.Units
import dev.dmil.skye.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val themeMode = settingsRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.SYSTEM)

    val units = settingsRepository.units
        .stateIn(viewModelScope, SharingStarted.Eagerly, Units.METRIC)

    val apiKey = settingsRepository.apiKey
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val apiKeySetAt = settingsRepository.apiKeySetAt
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val language = settingsRepository.language
        .stateIn(viewModelScope, SharingStarted.Eagerly, Language.SYSTEM)

    fun setLanguage(language: Language) {
        viewModelScope.launch { settingsRepository.setLanguage(language) }
    }

    fun setApiKey(key: String?) {
        viewModelScope.launch { settingsRepository.setApiKey(key) }
    }

    suspend fun setApiKeyAwait(key: String?) {
        settingsRepository.setApiKey(key)
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun setUnits(units: Units) {
        viewModelScope.launch { settingsRepository.setUnits(units) }
    }
}