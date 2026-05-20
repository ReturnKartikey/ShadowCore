package com.shadowcore.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "shadowcore_prefs")

/**
 * Persisted theme/settings preferences using DataStore.
 */
@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private val THEME_MODE_KEY = intPreferencesKey("theme_mode")
        // 0 = Auto, 1 = Light, 2 = Dark
    }

    val themeMode: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[THEME_MODE_KEY] ?: 0 // Default: Auto
    }

    suspend fun setThemeMode(mode: Int) {
        context.dataStore.edit { prefs ->
            prefs[THEME_MODE_KEY] = mode
        }
    }
}
