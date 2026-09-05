package com.tuapp.maps

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.gms.maps.model.LatLng
import com.tuapp.maps.data.local.AppPreferences
import com.tuapp.maps.data.model.PlaceResult
import com.tuapp.maps.ui.MainScreen
import com.tuapp.maps.ui.theme.GeoPuntosTheme

class MainActivity : ComponentActivity() {

    private var deepLinkPlace by mutableStateOf<PlaceResult?>(null)
    private var isDarkTheme by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        deepLinkPlace = extractSharedPlace(intent)

        val systemIsDark =
            (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        isDarkTheme = AppPreferences.isDarkMode(this, defaultValue = systemIsDark)

        setContent {
            GeoPuntosTheme(darkTheme = isDarkTheme) {
                MainScreen(
                    deepLinkPlace = deepLinkPlace,
                    onDeepLinkConsumed = { deepLinkPlace = null },
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = {
                        isDarkTheme = !isDarkTheme
                        AppPreferences.setDarkMode(this, isDarkTheme)
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        extractSharedPlace(intent)?.let { deepLinkPlace = it }
    }

    /**
     * Soporta tanto el App Link https (https://.../punto?lat=...) como el esquema propio
     * (geopuntos://punto?lat=...). El lat/lng/nombre/direccion viajan en la query del link,
     * asi que reconstruir el punto no requiere ninguna llamada de red ni un placeId valido.
     */
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
