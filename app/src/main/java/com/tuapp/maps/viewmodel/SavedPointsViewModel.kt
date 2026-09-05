package com.tuapp.maps.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tuapp.maps.data.model.Category
import com.tuapp.maps.data.model.SavedPoint
import com.tuapp.maps.data.repository.CategoryRepository
import com.tuapp.maps.data.repository.SavedPointsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SavedPointsUiState(
    val points: List<SavedPoint> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategory: String? = null
) {
    val filteredPoints: List<SavedPoint>
        get() = if (selectedCategory == null) points else points.filter { it.categoria == selectedCategory }
}

class SavedPointsViewModel(application: Application) : AndroidViewModel(application) {

    private val savedPointsRepository = SavedPointsRepository(application)
    private val categoryRepository = CategoryRepository(application)

    private val _uiState = MutableStateFlow(SavedPointsUiState())
    val uiState: StateFlow<SavedPointsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            savedPointsRepository.observePoints().collect { points ->
                _uiState.value = _uiState.value.copy(points = points)
            }
        }
        viewModelScope.launch {
            categoryRepository.observeCategories().collect { categories ->
                _uiState.value = _uiState.value.copy(categories = categories)
            }
        }
    }

    fun selectCategory(name: String?) {
        _uiState.value = _uiState.value.copy(selectedCategory = name)
    }

    fun createCategory(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { categoryRepository.createCategory(name) }
    }

    fun deletePoint(pointId: Long) {
        viewModelScope.launch { savedPointsRepository.deletePoint(pointId) }
    }
}
