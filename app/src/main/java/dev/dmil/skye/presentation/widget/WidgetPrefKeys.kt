package dev.dmil.skye.presentation.widget

import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object WidgetPrefKeys {
    val CITY_NAME = stringPreferencesKey("city_name")
    val LAT = doublePreferencesKey("lat")
    val LON = doublePreferencesKey("lon")
    val TEMPERATURE = doublePreferencesKey("temperature")
    val ICON = stringPreferencesKey("icon")
    val LAST_UPDATED = longPreferencesKey("last_updated")
    val DESCRIPTION = stringPreferencesKey("description")
}