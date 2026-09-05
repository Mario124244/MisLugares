package com.tuapp.maps.data.repository

import android.content.Context
import com.tuapp.maps.data.local.AppDatabase
import com.tuapp.maps.data.model.SavedPoint
import kotlinx.coroutines.flow.Flow

class SavedPointsRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).savedPointDao()

    fun observePoints(): Flow<List<SavedPoint>> = dao.observeAll()

    suspend fun savePoint(point: SavedPoint) {
        dao.insert(point)
    }

    suspend fun deletePoint(id: Long) {
        dao.deleteById(id)
    }
}
