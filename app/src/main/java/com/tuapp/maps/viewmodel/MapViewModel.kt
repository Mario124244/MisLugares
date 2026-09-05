package com.tuapp.maps.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.tuapp.maps.data.location.LocationHelper
import com.tuapp.maps.data.model.PlaceResult
import com.tuapp.maps.data.model.RouteInfo
import com.tuapp.maps.data.model.SavedPoint
import com.tuapp.maps.data.repository.CategoryRepository
import com.tuapp.maps.data.repository.DirectionsRepository
import com.tuapp.maps.data.repository.PlacesRepository
import com.tuapp.maps.data.repository.SavedPointsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MapUiState(
    val userLocation: LatLng? = null,
    val searchQuery: String = "",
    val predictions: List<PlacesRepository.Prediction> = emptyList(),
    val selectedPlace: PlaceResult? = null,
    val route: RouteInfo? = null,
    val isRouteLoading: Boolean = false,
    val saveMessage: String? = null,
    val routeError: Boolean = false,
    val categories: List<String> = emptyList()
)

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val placesRepository = PlacesRepository(
        Places.createClient(application),
        application.applicationContext
    )
    private val directionsRepository = DirectionsRepository()
    private val savedPointsRepository = SavedPointsRepository(application)
    private val categoryRepository = CategoryRepository(application)
    private val locationHelper = LocationHelper(application)

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            categoryRepository.seedDefaultCategoriesIfNeeded()
        }
        viewModelScope.launch {
            categoryRepository.observeCategories().collect { categories ->
                _uiState.value = _uiState.value.copy(categories = categories.map { it.nombre })
            }
        }
    }

    fun loadUserLocation() {
        viewModelScope.launch {
            val location = locationHelper.getLastKnownLocation()
            _uiState.value = _uiState.value.copy(userLocation = location)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(predictions = emptyList())
            return
        }
        viewModelScope.launch {
            val predictions = placesRepository.autocomplete(query)
            _uiState.value = _uiState.value.copy(predictions = predictions)
        }
    }

    fun onPredictionSelected(placeId: String) {
        viewModelScope.launch {
            val place = placesRepository.fetchPlace(placeId)
            _uiState.value = _uiState.value.copy(
                selectedPlace = place,
                predictions = emptyList(),
                searchQuery = place?.nombre ?: "",
                route = null
            )
        }
    }

    fun onMapTapped(latLng: LatLng) {
        viewModelScope.launch {
            val place = placesRepository.resolveTappedLocation(latLng)
            _uiState.value = _uiState.value.copy(selectedPlace = place, route = null)
        }
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedPlace = null, route = null)
    }

    fun requestRoute() {
        val destination = _uiState.value.selectedPlace ?: return
        val origin = _uiState.value.userLocation
        if (origin == null) {
            _uiState.value = _uiState.value.copy(routeError = true)
            return
        }
        _uiState.value = _uiState.value.copy(isRouteLoading = true, routeError = false)
        viewModelScope.launch {
            val result = directionsRepository.getRoute(origin, destination.latLng)
            _uiState.value = _uiState.value.copy(
                isRouteLoading = false,
                route = result.getOrNull(),
                routeError = result.isFailure
            )
        }
    }

    fun consumeRouteError() {
        _uiState.value = _uiState.value.copy(routeError = false)
    }

    fun savePoint(categoria: String) {
        val place = _uiState.value.selectedPlace ?: return
        viewModelScope.launch {
            savedPointsRepository.savePoint(
                SavedPoint(
                    placeId = place.placeId,
                    lat = place.latLng.latitude,
                    lng = place.latLng.longitude,
                    nombre = place.nombre,
                    direccion = place.direccion,
                    categoria = categoria
                )
            )
            _uiState.value = _uiState.value.copy(saveMessage = "Punto guardado")
        }
    }

    fun consumeSaveMessage() {
        _uiState.value = _uiState.value.copy(saveMessage = null)
    }

    /**
     * Usado al abrir un deep link (`geopuntos://punto?...` o el App Link https equivalente).
     * El lat/lng/nombre van codificados en el propio link, asi que no depende de la Places API
     * ni de que el placeId siga siendo valido (funciona igual para lugares buscados o tocados en el mapa).
     */
    fun openSharedPlace(place: PlaceResult) {
        _uiState.value = _uiState.value.copy(selectedPlace = place, route = null)
    }
}
