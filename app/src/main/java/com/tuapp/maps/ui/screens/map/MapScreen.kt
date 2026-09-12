package com.tuapp.maps.ui.screens.map

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.tuapp.maps.BuildConfig
import com.tuapp.maps.R
import com.tuapp.maps.data.model.PlaceResult
import com.tuapp.maps.ui.components.MonterreyMapFallback
import com.tuapp.maps.ui.components.PlaceBottomSheet
import com.tuapp.maps.ui.components.PlaceSearchBar
import com.tuapp.maps.viewmodel.MapViewModel
import kotlinx.coroutines.launch

// Ubicación por defecto: Monterrey, N.L., México (Macroplaza / Centro)
private val DEFAULT_LOCATION = LatLng(25.6866, -100.3161)

@Composable
fun MapScreen(
    deepLinkPlace: PlaceResult?,
    onDeepLinkConsumed: () -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var hasLocationPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        hasLocationPermission = grants.values.any { it }
        if (hasLocationPermission) viewModel.loadUserLocation()
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    LaunchedEffect(deepLinkPlace) {
        if (deepLinkPlace != null) {
            viewModel.openSharedPlace(deepLinkPlace)
            onDeepLinkConsumed()
        }
    }

    val routeErrorText = stringResource(R.string.route_error)
    LaunchedEffect(uiState.saveMessage, uiState.routeError) {
        uiState.saveMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.consumeSaveMessage()
        }
        if (uiState.routeError) {
            Toast.makeText(context, routeErrorText, Toast.LENGTH_SHORT).show()
            viewModel.consumeRouteError()
        }
    }

    val isMapsApiKeyPlaceholder = BuildConfig.MAPS_API_KEY.isBlank() ||
            BuildConfig.MAPS_API_KEY == "TU_MAPS_API_KEY_AQUI"

    // Si tenemos API Key real, inicializamos el CameraPositionState para Google Map
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(DEFAULT_LOCATION, 13f)
    }

    if (!isMapsApiKeyPlaceholder) {
        // Mueve la cámara cuando se detecta la ubicación del usuario (si no hay lugar seleccionado)
        LaunchedEffect(uiState.userLocation) {
            val userLoc = uiState.userLocation
            if (userLoc != null && uiState.selectedPlace == null) {
                coroutineScope.launch {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(userLoc, 15f)
                    )
                }
            }
        }

        // Mueve automáticamente la cámara al lugar seleccionado (por ejemplo, desde la pantalla de Monterrey o la búsqueda)
        LaunchedEffect(uiState.selectedPlace) {
            uiState.selectedPlace?.let { place ->
                coroutineScope.launch {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(place.latLng, 15f)
                    )
                }
            }
        }

        // Ajusta el encuadre de la cámara para mostrar la ruta completa cuando se calcula
        LaunchedEffect(uiState.route) {
            val route = uiState.route
            if (route != null && route.points.isNotEmpty()) {
                val builder = LatLngBounds.builder()
                route.points.forEach { builder.include(it) }
                coroutineScope.launch {
                    try {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngBounds(builder.build(), 120)
                        )
                    } catch (_: Exception) {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(route.points.first(), 14f)
                        )
                    }
                }
            }
        }
    }

    val mapProperties = remember(hasLocationPermission, isDarkTheme) {
        MapProperties(
            isMyLocationEnabled = hasLocationPermission,
            mapStyleOptions = if (isDarkTheme) {
                try {
                    MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_night)
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (isMapsApiKeyPlaceholder) {
            // Mapa interactivo de Monterrey visible y funcional sin depender de una API Key pagada
            MonterreyMapFallback(
                selectedPlace = uiState.selectedPlace,
                routePoints = uiState.route?.points ?: emptyList(),
                onMapClick = { latLng -> viewModel.onMapTapped(latLng) }
            )
        } else {
            // Mapa nativo de Google Maps cuando hay una API Key válida
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = MapUiSettings(myLocationButtonEnabled = hasLocationPermission, zoomControlsEnabled = false),
                onMapClick = { latLng -> viewModel.onMapTapped(latLng) }
            ) {
                uiState.selectedPlace?.let { place ->
                    Marker(state = rememberMarkerState(position = place.latLng), title = place.nombre)
                }
                uiState.route?.let { route ->
                    if (route.points.isNotEmpty()) {
                        Polyline(points = route.points, color = MaterialTheme.colorScheme.primary, width = 12f)
                    }
                }
            }
        }

        PlaceSearchBar(
            query = uiState.searchQuery,
            predictions = uiState.predictions,
            onQueryChanged = viewModel::onSearchQueryChanged,
            onPredictionSelected = viewModel::onPredictionSelected,
            modifier = Modifier.padding(16.dp)
        )
    }

    uiState.selectedPlace?.let { place ->
        PlaceBottomSheet(
            place = place,
            categories = uiState.categories,
            route = uiState.route,
            isRouteLoading = uiState.isRouteLoading,
            onGetDirections = viewModel::requestRoute,
            onSave = viewModel::savePoint,
            onShare = {
                val shareUri = Uri.Builder()
                    .scheme("geopuntos")
                    .authority("punto")
                    .appendQueryParameter("lat", place.latLng.latitude.toString())
                    .appendQueryParameter("lng", place.latLng.longitude.toString())
                    .appendQueryParameter("nombre", place.nombre)
                    .appendQueryParameter("direccion", place.direccion)
                    .build()
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "${place.nombre}\n${place.direccion}\n$shareUri")
                }
                context.startActivity(Intent.createChooser(sendIntent, null))
            },
            onDismiss = viewModel::clearSelection
        )
    }
}
