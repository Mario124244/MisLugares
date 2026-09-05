package com.tuapp.maps.data.repository

import android.content.Context
import com.tuapp.maps.data.local.AppDatabase
import com.tuapp.maps.data.model.Category
import com.tuapp.maps.data.model.DefaultCategories
import kotlinx.coroutines.flow.Flow

class CategoryRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).categoryDao()

    /** Crea las categorias por defecto la primera vez que se abre la app. */
    suspend fun seedDefaultCategoriesIfNeeded() {
        if (dao.count() > 0) return
        dao.insertAll(DefaultCategories.NAMES.map { Category(nombre = it) })
    }

    suspend fun createCategory(nombre: String) {
        dao.insert(Category(nombre = nombre))
    }

    fun observeCategories(): Flow<List<Category>> = dao.observeAll()
}
