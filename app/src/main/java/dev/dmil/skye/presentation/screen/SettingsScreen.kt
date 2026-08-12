package dev.dmil.skye.presentation.screen

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.dmil.skye.R
import dev.dmil.skye.domain.model.Language
import dev.dmil.skye.domain.model.ThemeMode
import dev.dmil.skye.domain.model.Units
import dev.dmil.skye.presentation.ui.theme.Gray
import dev.dmil.skye.presentation.ui.theme.Orange
import kotlinx.coroutines.launch
import java.util.Locale
import androidx.core.net.toUri

@Composable
fun SettingsOverlay(
    visible: Boolean,
    themeMode: ThemeMode,
    units: Units,
    apiKey: String?,
    apiKeySetAt: Long?,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onUnitsSelected: (Units) -> Unit,
    onApiKeyChanged: suspend (String?) -> Result<Unit>,
    onDismiss: () -> Unit,
    language: Language,
    onLanguageSelected: (Language) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable(onClick = onDismiss)
            )
        }

        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(tween(300, easing = FastOutSlowInEasing)) { it },
            exit = slideOutVertically(tween(300, easing = FastOutSlowInEasing)) { it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            val onBackground = MaterialTheme.colorScheme.onBackground

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.background,
                        RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                    )
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .align(Alignment.CenterHorizontally)
                        .width(36.dp)
                        .height(4.dp)
                        .background(Gray.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(R.string.settings_title), fontSize = 24.sp, color = onBackground)
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.settings_close_content_description),
                            tint = Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .navigationBarsPadding()
                        .verticalScroll(rememberScrollState())
                ) {

                    Text(text = stringResource(R.string.settings_theme_label), fontSize = 14.sp, color = Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    SegmentedControl(
                        options = listOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK),
                        selected = themeMode,
                        labelFor = {
                            when (it) {
                                ThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
                                ThemeMode.LIGHT -> stringResource(R.string.settings_theme_light)
                                ThemeMode.DARK -> stringResource(R.string.settings_theme_dark)
                            }
                        },
                        onSelect = onThemeModeSelected
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(text = stringResource(R.string.settings_units_label), fontSize = 14.sp, color = Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    SegmentedControl(
                        options = listOf(Units.METRIC, Units.IMPERIAL),
                        selected = units,
                        labelFor = {
                            when (it) {
                                Units.METRIC -> "°C, ${stringResource(R.string.unit_meters_per_second)}"
                                Units.IMPERIAL -> "°F, mph"
                            }
                        },
                        onSelect = onUnitsSelected
                    )
                    Spacer(modifier = Modifier.height(28.dp))

                    Text(text = stringResource(R.string.settings_language_label), fontSize = 14.sp, color = Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    SegmentedControl(
                        options = listOf(Language.SYSTEM, Language.ENGLISH, Language.RUSSIAN),
                        selected = language,
                        labelFor = {
                            when (it) {
                                Language.SYSTEM -> stringResource(R.string.settings_language_system)
                                Language.ENGLISH -> "English"
                                Language.RUSSIAN -> "Русский"
                            }
                        },
                        onSelect = onLanguageSelected
                    )
                    Spacer(modifier = Modifier.height(28.dp))

                    Text(text = stringResource(R.string.settings_api_key_label), fontSize = 14.sp, color = Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.settings_api_key_description),
                        fontSize = 12.sp,
                        color = Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    var isEditingKey by remember(apiKey) { mutableStateOf(apiKey == null) }
                    var keyInput by remember(isEditingKey) { mutableStateOf("") }
                    val scope = rememberCoroutineScope()
                    var isChecking by remember { mutableStateOf(false) }
                    var errorMessage by remember { mutableStateOf<String?>(null) }
                    val apiKeyErrorText = stringResource(R.string.settings_api_key_error)

                    if (!isEditingKey && apiKey != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.5.dp, onBackground, RoundedCornerShape(14.dp))
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = maskApiKey(apiKey), fontSize = 15.sp, color = onBackground)
                                apiKeySetAt?.let {
                                    Text(
                                        text = stringResource(R.string.settings_api_key_set_on, formatKeySetDate(it)),
                                        fontSize = 12.sp,
                                        color = Gray
                                    )
                                }
                            }
                            Text(
                                text = stringResource(R.string.settings_api_key_change),
                                fontSize = 14.sp,
                                color = Orange,
                                modifier = Modifier.clickable {
                                    errorMessage = null
                                    isEditingKey = true
                                }
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.5.dp, onBackground, RoundedCornerShape(14.dp))
                        ) {
                            BasicTextField(
                                value = keyInput,
                                onValueChange = { keyInput = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 15.sp, color = onBackground),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                decorationBox = { innerTextField ->
                                    if (keyInput.isEmpty()) {
                                        Text(text = stringResource(R.string.settings_api_key_placeholder), fontSize = 15.sp, color = Gray)
                                    }
                                    innerTextField()
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (apiKey != null) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(1.5.dp, onBackground, RoundedCornerShape(14.dp))
                                        .clickable(enabled = !isChecking) {
                                            errorMessage = null
                                            isEditingKey = false
                                        }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = stringResource(R.string.settings_api_key_cancel), fontSize = 15.sp, color = onBackground)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(onBackground.copy(alpha = if (isChecking) 0.5f else 1f))
                                    .clickable(enabled = !isChecking) {
                                        scope.launch {
                                            isChecking = true
                                            errorMessage = null
                                            val result = onApiKeyChanged(keyInput.trim().ifBlank { null })
                                            isChecking = false
                                            if (result.isSuccess) {
                                                isEditingKey = false
                                            } else {
                                                errorMessage = apiKeyErrorText
                                            }
                                        }
                                    }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isChecking) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = MaterialTheme.colorScheme.background,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(text = stringResource(R.string.settings_api_key_save), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.background)
                                }
                            }
                        }
                    }

                    errorMessage?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = it, fontSize = 13.sp, color = Color(0xFFFF4430))
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(4.dp))

                    val context = LocalContext.current
                    LinkRow(
                        icon = Icons.Filled.Code,
                        label = stringResource(R.string.settings_github_link),
                        onClick = { openUrl(context, "https://github.com/miliukov/Skye") }
                    )
                    LinkRow(
                        icon = Icons.Filled.Email,
                        label = "contact@dmil.dev",
                        onClick = { sendEmail(context, "contact@dmil.dev") }
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun LinkRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    val onBackground = MaterialTheme.colorScheme.onBackground
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = onBackground,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label, fontSize = 15.sp, color = onBackground)
    }
}

private fun openUrl(context: android.content.Context, url: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    }
}

private fun sendEmail(context: android.content.Context, email: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_SENDTO, "mailto:$email".toUri()))
    }
}

@Composable
private fun <T> SegmentedControl(
    options: List<T>,
    selected: T,
    labelFor: @Composable (T) -> String,
    onSelect: (T) -> Unit
) {
    val onBackground = MaterialTheme.colorScheme.onBackground
    val background = MaterialTheme.colorScheme.background

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.5.dp, onBackground, RoundedCornerShape(14.dp))
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(if (isSelected) onBackground else Color.Transparent)
                    .clickable { onSelect(option) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = labelFor(option),
                    fontSize = 14.sp,
                    color = if (isSelected) background else onBackground
                )
            }
        }
    }
}

private fun maskApiKey(key: String): String {
    return "${key.take(4)}•••••••••••••••••"
}

private fun formatKeySetDate(epochMillis: Long): String {
    val date = java.time.Instant.ofEpochMilli(epochMillis)
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDate()
    val formatter = java.time.format.DateTimeFormatter
        .ofLocalizedDate(java.time.format.FormatStyle.MEDIUM)
        .withLocale(Locale.getDefault())
    return date.format(formatter)
}