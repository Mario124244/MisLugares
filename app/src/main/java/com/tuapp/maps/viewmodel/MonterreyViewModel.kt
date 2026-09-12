package com.tuapp.maps.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tuapp.maps.data.model.MonterreyPlace
import com.tuapp.maps.data.model.SavedPoint
import com.tuapp.maps.data.repository.MonterreyPlacesRepository
import com.tuapp.maps.data.repository.SavedPointsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MonterreyUiState(
    val places: List<MonterreyPlace> = emptyList(),
    val filteredPlaces: List<MonterreyPlace> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val searchQuery: String = "",
    val savedPointIds: Set<String> = emptySet(),
    val savedPointNames: Set<String> = emptySet()
)

class MonterreyViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val monterreyRepository = MonterreyPlacesRepository()
    private val savedPointsRepository = SavedPointsRepository(application)

    private val _defaultPlaces = monterreyRepository.getDefaultMonterreyPlaces()
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<MonterreyUiState> = combine(
        savedPointsRepository.observePoints(),
        _selectedCategory,
        _searchQuery
    ) { savedPoints, selectedCategory, searchQuery ->
        val savedIds = savedPoints.map { it.placeId }.filter { it.isNotBlank() }.toSet()
        val savedNames = savedPoints.map { it.nombre.lowercase().trim() }.toSet()

        val customPlaces = savedPoints.map { sp ->
            MonterreyPlace(
                id = if (sp.placeId.isBlank()) "custom_${sp.id}" else sp.placeId,
                nombre = sp.nombre,
                descripcion = sp.direccion,
                direccion = sp.direccion,
                categoria = if (sp.categoria.isBlank()) "Favoritos" else sp.categoria,
                lat = sp.lat,
                lng = sp.lng,
                isFavorite = true
            )
        }

        val defaultPlaceNames = _defaultPlaces.map { it.nombre.lowercase().trim() }.toSet()
        val uniqueCustomPlaces = customPlaces.filter { it.nombre.lowercase().trim() !in defaultPlaceNames }

        val allPlaces = _defaultPlaces.map { defaultPlace ->
            val isFav = defaultPlace.id in savedIds || defaultPlace.nombre.lowercase().trim() in savedNames
            val desc = if (defaultPlace.descripcionRes != 0) context.getString(defaultPlace.descripcionRes) else defaultPlace.descripcion
            val cat = if (defaultPlace.categoriaRes != 0) context.getString(defaultPlace.categoriaRes) else defaultPlace.categoria
            defaultPlace.copy(
                isFavorite = isFav,
                descripcion = desc,
                categoria = cat
            )
        } + uniqueCustomPlaces

        val categories = allPlaces.map { it.categoria }.distinct().sorted()

        val filtered = allPlaces.filter { place ->
            val matchesCategory = selectedCategory == null || place.categoria.equals(selectedCategory, ignoreCase = true)
            val matchesQuery = searchQuery.isBlank() ||
                    place.nombre.contains(searchQuery, ignoreCase = true) ||
                    place.descripcion.contains(searchQuery, ignoreCase = true) ||
                    place.direccion.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }

        MonterreyUiState(
            places = allPlaces,
            filteredPlaces = filtered,
            categories = categories,
            selectedCategory = selectedCategory,
            searchQuery = searchQuery,
            savedPointIds = savedIds,
            savedPointNames = savedNames
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MonterreyUiState()
    )

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(place: MonterreyPlace, savedPointsList: List<SavedPoint>) {
        viewModelScope.launch {
            if (place.isFavorite) {
                val matchingSavedPoint = savedPointsList.find {
                    (it.placeId.isNotBlank() && it.placeId == place.id) ||
                            it.nombre.equals(place.nombre, ignoreCase = true)
                }
                if (matchingSavedPoint != null) {
                    savedPointsRepository.deletePoint(matchingSavedPoint.id)
                }
            } else {
                savedPointsRepository.savePoint(
                    SavedPoint(
                        placeId = place.id,
                        lat = place.lat,
                        lng = place.lng,
                        nombre = place.nombre,
                        direccion = place.direccion,
                        categoria = if (place.categoria.isBlank()) "Favoritos" else place.categoria
                    )
                )
            }
        }
    }

    fun addCustomPlace(
        nombre: String,
        direccion: String,
        descripcion: String,
        categoria: String,
        lat: Double,
        lng: Double
    ) {
        viewModelScope.launch {
            savedPointsRepository.savePoint(
                SavedPoint(
                    placeId = "custom_${System.currentTimeMillis()}",
                    lat = lat,
                    lng = lng,
                    nombre = nombre,
                    direccion = if (descripcion.isNotBlank()) "$direccion · $descripcion" else direccion,
                    categoria = if (categoria.isBlank()) "Favoritos" else categoria
                )
            )
        }
    }
}
