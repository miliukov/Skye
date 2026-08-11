package dev.dmil.skye.presentation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.os.LocaleListCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import dev.dmil.skye.domain.model.Language
import dev.dmil.skye.domain.model.ThemeMode
import dev.dmil.skye.presentation.screen.WeatherScreen
import dev.dmil.skye.presentation.ui.theme.SkyeTheme
import dev.dmil.skye.presentation.viewmodel.SettingsViewModel

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val themeMode by settingsViewModel.themeMode.collectAsState()
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            val language by settingsViewModel.language.collectAsState()
            LaunchedEffect(language) {
                val locales = when (language) {
                    Language.SYSTEM -> LocaleListCompat.getEmptyLocaleList()
                    Language.ENGLISH -> LocaleListCompat.forLanguageTags("en")
                    Language.RUSSIAN -> LocaleListCompat.forLanguageTags("ru")
                }
                AppCompatDelegate.setApplicationLocales(locales)
            }

            SkyeTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WeatherScreen(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}
