package com.tuapp.maps.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String = ""
)

object DefaultCategories {
    val NAMES = listOf("Casa", "Trabajo", "Favoritos", "Restaurantes")
}
