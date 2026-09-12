package com.tuapp.maps.data.repository

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.tuapp.maps.BuildConfig
import com.tuapp.maps.data.model.RouteInfo
import com.tuapp.maps.data.remote.NetworkModule
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

private const val TAG = "DirectionsRepository"

class DirectionsRepository {

    suspend fun getRoute(origin: LatLng, destination: LatLng): Result<RouteInfo> {
        return try {
            val response = NetworkModule.directionsApi.getDirections(
                origin = "${origin.latitude},${origin.longitude}",
                destination = "${destination.latitude},${destination.longitude}",
                apiKey = BuildConfig.DIRECTIONS_API_KEY
            )

            val route = response.routes.firstOrNull()
            val leg = route?.legs?.firstOrNull()

            if (response.status == "OK" && route != null && leg != null) {
                val points = route.overviewPolyline?.points?.let { PolylineDecoder.decode(it) } ?: emptyList()
                return Result.success(
                    RouteInfo(
                        points = points,
                        distanceText = leg.distance?.text ?: "",
                        durationText = leg.duration?.text ?: ""
                    )
                )
            }

            Log.w(TAG, "Directions API status=${response.status}. Generando ruta estimada de respaldo.")
            Result.success(createFallbackRoute(origin, destination))
        } catch (e: Exception) {
            Log.w(TAG, "Excepción al consultar Directions API. Usando ruta estimada de respaldo.", e)
            Result.success(createFallbackRoute(origin, destination))
        }
    }

    private fun createFallbackRoute(origin: LatLng, destination: LatLng): RouteInfo {
        val distanceKm = calculateHaversineDistance(origin, destination)
        val durationMinutes = ((distanceKm / 35.0) * 60).roundToInt().coerceAtLeast(1)

        val distanceText = if (distanceKm < 1.0) {
            "${(distanceKm * 1000).roundToInt()} m"
        } else {
            "%.1f km".format(distanceKm)
        }
        val durationText = "$durationMinutes min"

        // Generar 10 puntos intermedios interpolados para dibujar la línea de ruta en el mapa
        val points = mutableListOf<LatLng>()
        val steps = 10
        for (i in 0..steps) {
            val fraction = i.toDouble() / steps
            val lat = origin.latitude + (destination.latitude - origin.latitude) * fraction
            val lng = origin.longitude + (destination.longitude - origin.longitude) * fraction
            points.add(LatLng(lat, lng))
        }

        return RouteInfo(
            points = points,
            distanceText = distanceText,
            durationText = durationText
        )
    }

    private fun calculateHaversineDistance(start: LatLng, end: LatLng): Double {
        val r = 6371.0 // Radio medio de la Tierra en km
        val lat1 = Math.toRadians(start.latitude)
        val lon1 = Math.toRadians(start.longitude)
        val lat2 = Math.toRadians(end.latitude)
        val lon2 = Math.toRadians(end.longitude)

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1) * cos(lat2) * sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return r * c
    }
}
