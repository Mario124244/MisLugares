package com.tuapp.maps.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.tuapp.maps.data.model.ThemeMode
import com.tuapp.maps.util.SolarCalculator
import kotlinx.coroutines.delay
import java.time.Instant

/**
 * Resuelve si el tema debe verse oscuro.
 *
 * En [ThemeMode.AUTO] sigue el amanecer y el ocaso reales de [location] (Monterrey por defecto):
 * se reprograma sola para el siguiente cambio en vez de sondear el reloj, y se revalida cada vez
 * que la app vuelve al primer plano, por si el reloj avanzo con el proceso dormido.
 *
 * [clock] existe para poder inyectar una hora fija en pruebas y previews.
 */
@Composable
fun rememberIsDarkTheme(
    themeMode: ThemeMode,
    location: SolarCalculator.Location = SolarCalculator.MONTERREY,
    clock: () -> Instant = { Instant.now() }
): Boolean {
    var isNight by remember(location) { mutableStateOf(!SolarCalculator.isDaytime(clock(), location)) }

    LaunchedEffect(themeMode, location) {
        if (themeMode != ThemeMode.AUTO) return@LaunchedEffect
        while (true) {
            val now = clock()
            isNight = !SolarCalculator.isDaytime(now, location)

            val nextChange = SolarCalculator.nextTransition(now, location)
            // Minimo un segundo para no girar en vacio si el calculo cae justo en el limite.
            delay((nextChange.toEpochMilli() - now.toEpochMilli()).coerceAtLeast(1_000L))
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, themeMode, location) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && themeMode == ThemeMode.AUTO) {
                isNight = !SolarCalculator.isDaytime(clock(), location)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    return when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.AUTO -> isNight
    }
}
