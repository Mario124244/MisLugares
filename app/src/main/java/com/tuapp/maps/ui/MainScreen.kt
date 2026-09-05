package com.tuapp.maps.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import com.tuapp.maps.R
import com.tuapp.maps.data.model.PlaceResult
import com.tuapp.maps.navigation.MainTab
import com.tuapp.maps.ui.screens.map.MapScreen
import com.tuapp.maps.ui.screens.savedpoints.SavedPointsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    deepLinkPlace: PlaceResult?,
    onDeepLinkConsumed: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(MainTab.MAP) }

    // Un deep link siempre debe abrir el mapa, aunque el usuario este en otra pestana.
    if (deepLinkPlace != null) selectedTab = MainTab.MAP

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = stringResource(
                                if (isDarkTheme) R.string.switch_to_light else R.string.switch_to_dark
                            )
                        )
                    }
                    IconButton(onClick = ::toggleAppLanguage) {
                        Icon(Icons.Default.Translate, contentDescription = stringResource(R.string.toggle_language))
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == MainTab.MAP,
                    onClick = { selectedTab = MainTab.MAP },
                    icon = { Icon(Icons.Default.Map, contentDescription = null) },
                    label = { Text(stringResource(R.string.map_tab)) }
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.SAVED,
                    onClick = { selectedTab = MainTab.SAVED },
                    icon = { Icon(Icons.Default.Bookmark, contentDescription = null) },
                    label = { Text(stringResource(R.string.saved_tab)) }
                )
            }
        }
    ) { padding ->
        when (selectedTab) {
            MainTab.MAP -> MapScreen(
                deepLinkPlace = deepLinkPlace,
                onDeepLinkConsumed = onDeepLinkConsumed,
                isDarkTheme = isDarkTheme,
                modifier = Modifier.padding(padding)
            )
            MainTab.SAVED -> SavedPointsScreen(modifier = Modifier.padding(padding))
        }
    }
}

/** Alterna entre espanol e ingles usando las preferencias de idioma por app (AndroidX AppCompat). */
private fun toggleAppLanguage() {
    val overridden = AppCompatDelegate.getApplicationLocales().get(0)?.language
    val current = overridden ?: java.util.Locale.getDefault().language
    val next = if (current == "en") "es" else "en"
    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(next))
}
