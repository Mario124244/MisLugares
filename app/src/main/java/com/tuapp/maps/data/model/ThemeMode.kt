package com.tuapp.maps.data.model

/**
 * Modo de tema elegido por el usuario. [AUTO] deja que la app siga el amanecer y el ocaso de
 * Monterrey; [LIGHT] y [DARK] lo fijan manualmente.
 */
enum class ThemeMode(val storageValue: String) {
    AUTO("auto"),
    LIGHT("light"),
    DARK("dark");

    /** Siguiente modo al tocar el boton de tema: auto -> claro -> oscuro -> auto. */
    fun next(): ThemeMode = when (this) {
        AUTO -> LIGHT
        LIGHT -> DARK
        DARK -> AUTO
    }

    companion object {
        fun fromStorageValue(value: String?): ThemeMode =
            entries.firstOrNull { it.storageValue == value } ?: AUTO
    }
}
