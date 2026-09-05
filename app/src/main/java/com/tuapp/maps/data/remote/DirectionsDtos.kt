package com.tuapp.maps.data.remote

import com.google.gson.annotations.SerializedName

data class DirectionsResponseDto(
    val routes: List<RouteDto> = emptyList(),
    val status: String = "",
    @SerializedName("error_message")
    val errorMessage: String? = null
)

data class RouteDto(
    val legs: List<LegDto> = emptyList(),
    @SerializedName("overview_polyline")
    val overviewPolyline: OverviewPolylineDto? = null
)

data class LegDto(
    val distance: TextValueDto? = null,
    val duration: TextValueDto? = null
)

data class TextValueDto(
    val text: String = "",
    val value: Long = 0
)

data class OverviewPolylineDto(
    val points: String = ""
)
