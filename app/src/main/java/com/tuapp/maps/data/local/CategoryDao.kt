package com.tuapp.maps.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.tuapp.maps.data.model.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY nombre ASC")
    fun observeAll(): Flow<List<Category>>

    @Insert
    suspend fun insert(category: Category): Long

    @Insert
    suspend fun insertAll(categories: List<Category>)

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int
}
