package com.tuapp.maps.data.local

import android.content.Context
import com.tuapp.maps.data.model.ThemeMode

/** Preferencias simples de UI que no ameritan Room (modo de tema e idioma elegido). */
object AppPreferences {
    private const val PREFS_NAME = "geopuntos_settings"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_LANGUAGE = "language"

    /** Clave anterior (booleano claro/oscuro). Solo se lee para migrar la preferencia existente. */
    private const val LEGACY_KEY_DARK_MODE = "dark_mode"

    fun getThemeMode(context: Context): ThemeMode {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        prefs.getString(KEY_THEME_MODE, null)?.let { return ThemeMode.fromStorageValue(it) }

        // Migracion: quien ya habia elegido claro u oscuro a mano conserva esa eleccion.
        if (prefs.contains(LEGACY_KEY_DARK_MODE)) {
            val legacyMode = if (prefs.getBoolean(LEGACY_KEY_DARK_MODE, false)) ThemeMode.DARK else ThemeMode.LIGHT
            setThemeMode(context, legacyMode)
            return legacyMode
        }

        return ThemeMode.AUTO
    }

    fun setThemeMode(context: Context, mode: ThemeMode) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME_MODE, mode.storageValue)
            .remove(LEGACY_KEY_DARK_MODE)
            .apply()
    }

    fun getLanguage(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, null)
    }

    fun setLanguage(context: Context, language: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, language)
            .apply()
    }
}
