package com.tuapp.maps.data.local

import android.content.Context

/** Preferencias simples de UI que no ameritan Room (tema claro/oscuro elegido manualmente). */
object AppPreferences {
    private const val PREFS_NAME = "geopuntos_settings"
    private const val KEY_DARK_MODE = "dark_mode"

    fun isDarkMode(context: Context, defaultValue: Boolean): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return if (prefs.contains(KEY_DARK_MODE)) prefs.getBoolean(KEY_DARK_MODE, defaultValue) else defaultValue
    }

    fun setDarkMode(context: Context, isDark: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DARK_MODE, isDark)
            .apply()
    }
}
