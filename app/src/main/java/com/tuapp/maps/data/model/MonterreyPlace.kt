package com.tuapp.maps.data.model

import androidx.annotation.StringRes

data class MonterreyPlace(
    val id: String,
    val nombre: String,
    val descripcion: String = "",
    val direccion: String,
    val categoria: String = "",
    val lat: Double,
    val lng: Double,
    val isFavorite: Boolean = false,
    @StringRes val descripcionRes: Int = 0,
    @StringRes val categoriaRes: Int = 0
)
