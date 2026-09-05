package com.tuapp.maps.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_points")
data class SavedPoint(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val placeId: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val nombre: String = "",
    val direccion: String = "",
    val categoria: String = "",
    val fechaCreacion: Long = System.currentTimeMillis()
)
