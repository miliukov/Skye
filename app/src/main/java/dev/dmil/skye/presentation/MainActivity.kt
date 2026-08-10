package dev.dmil.skye.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import dev.dmil.skye.presentation.screen.WeatherScreen
import dev.dmil.skye.presentation.ui.theme.SkyeTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SkyeTheme {
                WeatherScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
