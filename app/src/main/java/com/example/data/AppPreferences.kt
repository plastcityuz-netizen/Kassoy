package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.core.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.balansDataStore by preferencesDataStore(name = "balans_ai_prefs")

enum class ThemePreference {
    SYSTEM, DARK, LIGHT
}

data class UserPrefs(
    val language: AppLanguage = AppLanguage.UZ,
    val themePreference: ThemePreference = ThemePreference.DARK,
    val denseTableMode: Boolean = false
)

class AppPreferences(private val context: Context) {
    private val keyLang = stringPreferencesKey("app_language")
    private val keyTheme = stringPreferencesKey("theme_preference")
    private val keyDense = booleanPreferencesKey("dense_table_mode")

    val prefsFlow: Flow<UserPrefs> = context.balansDataStore.data.map { prefs ->
        val lang = if (prefs[keyLang] == AppLanguage.RU.code) AppLanguage.RU else AppLanguage.UZ
        val theme = when (prefs[keyTheme]) {
            ThemePreference.LIGHT.name -> ThemePreference.LIGHT
            ThemePreference.SYSTEM.name -> ThemePreference.SYSTEM
            else -> ThemePreference.DARK
        }
        val dense = prefs[keyDense] ?: false
        UserPrefs(lang, theme, dense)
    }

    suspend fun setLanguage(lang: AppLanguage) {
        context.balansDataStore.edit { it[keyLang] = lang.code }
    }

    suspend fun setTheme(theme: ThemePreference) {
        context.balansDataStore.edit { it[keyTheme] = theme.name }
    }

    suspend fun setDenseTableMode(dense: Boolean) {
        context.balansDataStore.edit { it[keyDense] = dense }
    }
}
