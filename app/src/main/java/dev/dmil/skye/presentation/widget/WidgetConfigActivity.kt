package dev.dmil.skye.presentation.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dev.dmil.skye.R
import dev.dmil.skye.presentation.screen.getCorrectConditionIcon
import dev.dmil.skye.presentation.state.CityListItem
import dev.dmil.skye.presentation.ui.theme.Black
import dev.dmil.skye.presentation.ui.theme.Orange
import dev.dmil.skye.presentation.ui.theme.SkyeTheme
import dev.dmil.skye.presentation.ui.theme.White
import dev.dmil.skye.presentation.viewmodel.WidgetConfigViewModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WidgetConfigActivity : ComponentActivity() {

    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        setResult(RESULT_CANCELED)

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            SkyeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WidgetConfigScreen(onCitySelected = { city -> finishWithCity(city) })
                }
            }
        }
    }

    private fun finishWithCity(city: CityListItem) {
        lifecycleScope.launch {
            val glanceId = GlanceAppWidgetManager(this@WidgetConfigActivity)
                .getGlanceIdBy(appWidgetId)

            updateAppWidgetState(this@WidgetConfigActivity, glanceId) { prefs ->
                prefs[WidgetPrefKeys.CITY_NAME] = city.name
                prefs[WidgetPrefKeys.LAT] = city.lat
                prefs[WidgetPrefKeys.LON] = city.lon
                city.temperature?.let { prefs[WidgetPrefKeys.TEMPERATURE] = it.toDouble() }
                city.icon?.let { prefs[WidgetPrefKeys.ICON] = it }
                city.description?.let { prefs[WidgetPrefKeys.DESCRIPTION] = it }
                prefs[WidgetPrefKeys.LAST_UPDATED] = System.currentTimeMillis()
            }

            SkyeWidget().update(this@WidgetConfigActivity, glanceId)
            WidgetUpdateScheduler.schedule(this@WidgetConfigActivity, appWidgetId, city.lat, city.lon)

            val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(RESULT_OK, resultValue)
            finish()
        }
    }
}

@Composable
private fun WidgetConfigScreen(onCitySelected: (CityListItem) -> Unit) {
    val viewModel: WidgetConfigViewModel = hiltViewModel()
    val cities = viewModel.cities.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.widget_config_title),
            fontSize = 34.sp,
            letterSpacing = (-1).sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(R.string.widget_config_subtitle),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (cities.value.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Orange)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(cities.value) { city ->
                    WidgetCityRow(city = city, onClick = { onCitySelected(city) })
                }
            }
        }
    }
}

@Composable
private fun WidgetCityRow(city: CityListItem, onClick: () -> Unit) {
    val isNight = city.icon?.endsWith("n") == true
    val bg = if (isNight) Black else White
    val fg = if (isNight) White else Black

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(2.dp, fg, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = city.name, fontSize = 20.sp, color = fg, modifier = Modifier.weight(1f))

        if (city.temperature != null && city.icon != null) {
            Icon(
                painter = painterResource(getCorrectConditionIcon(city.icon)),
                contentDescription = null,
                tint = if (isNight) White else androidx.compose.ui.graphics.Color.Unspecified,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = "${city.temperature}º", fontSize = 22.sp, color = fg)
        } else {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = if (isNight) White else Orange,
                strokeWidth = 2.dp
            )
        }
    }
}