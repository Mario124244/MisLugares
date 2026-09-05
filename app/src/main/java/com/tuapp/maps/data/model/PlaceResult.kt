package com.tuapp.maps.data.model

import com.google.android.gms.maps.model.LatLng

/** Lugar seleccionado en el mapa o desde el buscador, listo para mostrar en el bottom sheet. */
data class PlaceResult(
    val placeId: String,
    val nombre: String,
    val direccion: String,
    val latLng: LatLng
)

data class RouteInfo(
    val points: List<LatLng>,
    val distanceText: String,
    val durationText: String
)
