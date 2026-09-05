package com.tuapp.maps.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.tuapp.maps.data.model.SavedPoint
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedPointDao {

    @Query("SELECT * FROM saved_points ORDER BY fechaCreacion DESC")
    fun observeAll(): Flow<List<SavedPoint>>

    @Insert
    suspend fun insert(point: SavedPoint): Long

    @Query("DELETE FROM saved_points WHERE id = :id")
    suspend fun deleteById(id: Long)
}
