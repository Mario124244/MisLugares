package com.tuapp.maps

import android.app.LocaleManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.gms.maps.model.LatLng
import com.tuapp.maps.data.local.AppPreferences
import com.tuapp.maps.data.model.PlaceResult
import com.tuapp.maps.data.model.ThemeMode
import com.tuapp.maps.ui.MainScreen
import com.tuapp.maps.ui.theme.GeoPuntosTheme
import com.tuapp.maps.ui.theme.rememberIsDarkTheme
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private var deepLinkPlace by mutableStateOf<PlaceResult?>(null)
    private var themeMode by mutableStateOf(ThemeMode.AUTO)

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            installSplashScreen()
        } catch (_: Exception) {
            // Ignorar excepción si el tema no tiene Splash configurado
        }

        super.onCreate(savedInstanceState)

        // Aplicar idioma guardado si existe
        AppPreferences.getLanguage(this)?.let { lang ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getSystemService(LocaleManager::class.java)?.applicationLocales =
                    LocaleList.forLanguageTags(lang)
            }
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(lang))
        }

        deepLinkPlace = extractSharedPlace(intent)

        themeMode = AppPreferences.getThemeMode(this)

        setContent {
            // En modo automatico esto sigue el amanecer y el ocaso de Monterrey y cambia solo.
            val isDarkTheme = rememberIsDarkTheme(themeMode)

            GeoPuntosTheme(darkTheme = isDarkTheme) {
                MainScreen(
                    deepLinkPlace = deepLinkPlace,
                    onDeepLinkConsumed = { deepLinkPlace = null },
                    isDarkTheme = isDarkTheme,
                    themeMode = themeMode,
                    onCycleThemeMode = {
                        themeMode = themeMode.next()
                        AppPreferences.setThemeMode(this, themeMode)
                    },
                    onToggleLanguage = { toggleLanguage() }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        extractSharedPlace(intent)?.let { deepLinkPlace = it }
    }

    private fun toggleLanguage() {
        val currentTag = AppPreferences.getLanguage(this)
            ?: AppCompatDelegate.getApplicationLocales().get(0)?.language
            ?: Locale.getDefault().language

        val nextTag = if (currentTag.startsWith("en", ignoreCase = true)) "es" else "en"

        AppPreferences.setLanguage(this, nextTag)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getSystemService(LocaleManager::class.java)?.applicationLocales =
                LocaleList.forLanguageTags(nextTag)
        }
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(nextTag))
        recreate()
    }

    private fun extractSharedPlace(intent: Intent?): PlaceResult? {
        val data: Uri = intent?.data ?: return null
        val isHttpsAppLink = data.scheme == "https" && data.host == BuildConfig.APP_LINK_HOST
        val isCustomScheme = data.scheme == "geopuntos" && data.host == "punto"
        if (!isHttpsAppLink && !isCustomScheme) return null

        val lat = data.getQueryParameter("lat")?.toDoubleOrNull() ?: return null
        val lng = data.getQueryParameter("lng")?.toDoubleOrNull() ?: return null
        val nombre = data.getQueryParameter("nombre").orEmpty()
        val direccion = data.getQueryParameter("direccion").orEmpty()

        return PlaceResult(
            placeId = "shared:$lat,$lng",
            nombre = nombre,
            direccion = direccion,
            latLng = LatLng(lat, lng)
        )
    }
}
