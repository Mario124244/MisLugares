package com.tuapp.maps.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeModeTest {

    @Test
    fun `el boton cicla auto claro oscuro y regresa a auto`() {
        assertEquals(ThemeMode.LIGHT, ThemeMode.AUTO.next())
        assertEquals(ThemeMode.DARK, ThemeMode.LIGHT.next())
        assertEquals(ThemeMode.AUTO, ThemeMode.DARK.next())
    }

    @Test
    fun `tres toques regresan al modo inicial`() {
        ThemeMode.entries.forEach { mode ->
            assertEquals(mode, mode.next().next().next())
        }
    }

    @Test
    fun `los valores guardados se leen de vuelta`() {
        ThemeMode.entries.forEach { mode ->
            assertEquals(mode, ThemeMode.fromStorageValue(mode.storageValue))
        }
    }

    @Test
    fun `un valor desconocido o ausente cae en automatico`() {
        assertEquals(ThemeMode.AUTO, ThemeMode.fromStorageValue(null))
        assertEquals(ThemeMode.AUTO, ThemeMode.fromStorageValue(""))
        assertEquals(ThemeMode.AUTO, ThemeMode.fromStorageValue("sepia"))
    }
}
