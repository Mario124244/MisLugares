package com.tuapp.maps.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationCity
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
import com.tuapp.maps.R
import com.tuapp.maps.data.model.PlaceResult
import com.tuapp.maps.data.model.ThemeMode
import com.tuapp.maps.navigation.MainTab
import com.tuapp.maps.ui.screens.map.MapScreen
import com.tuapp.maps.ui.screens.monterrey.MonterreyScreen
import com.tuapp.maps.ui.screens.savedpoints.SavedPointsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    deepLinkPlace: PlaceResult?,
    onDeepLinkConsumed: () -> Unit,
    isDarkTheme: Boolean,
    themeMode: ThemeMode,
    onCycleThemeMode: () -> Unit,
    onToggleLanguage: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(MainTab.MONTERREY) }
    var navigatePlaceForMap by remember { mutableStateOf<PlaceResult?>(null) }

    // Un deep link siempre debe abrir el mapa, aunque el usuario este en otra pestana.
    if (deepLinkPlace != null) selectedTab = MainTab.MAP

    val activeMapPlace = deepLinkPlace ?: navigatePlaceForMap

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onCycleThemeMode) {
                        // El icono muestra el modo activo; la descripcion anuncia a cual se cambia al tocar.
                        Icon(
                            imageVector = when (themeMode) {
                                ThemeMode.AUTO -> Icons.Default.BrightnessAuto
                                ThemeMode.LIGHT -> Icons.Default.LightMode
                                ThemeMode.DARK -> Icons.Default.DarkMode
                            },
                            contentDescription = stringResource(
                                when (themeMode.next()) {
                                    ThemeMode.AUTO -> R.string.switch_to_auto
                                    ThemeMode.LIGHT -> R.string.switch_to_light
                                    ThemeMode.DARK -> R.string.switch_to_dark
                                }
                            )
                        )
                    }
                    IconButton(onClick = onToggleLanguage) {
                        Icon(Icons.Default.Translate, contentDescription = stringResource(R.string.toggle_language))
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == MainTab.MONTERREY,
                    onClick = { selectedTab = MainTab.MONTERREY },
                    icon = { Icon(Icons.Default.LocationCity, contentDescription = null) },
                    label = { Text(stringResource(R.string.monterrey_tab)) }
                )
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
            MainTab.MONTERREY -> MonterreyScreen(
                onNavigateToMap = { place ->
                    navigatePlaceForMap = place
                    selectedTab = MainTab.MAP
                },
                modifier = Modifier.padding(padding)
            )
            MainTab.MAP -> MapScreen(
                deepLinkPlace = activeMapPlace,
                onDeepLinkConsumed = {
                    if (deepLinkPlace != null) onDeepLinkConsumed()
                    navigatePlaceForMap = null
                },
                isDarkTheme = isDarkTheme,
                modifier = Modifier.padding(padding)
            )
            MainTab.SAVED -> SavedPointsScreen(modifier = Modifier.padding(padding))
        }
    }
}
