package com.tuapp.maps.data.repository

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.tuapp.maps.BuildConfig
import com.tuapp.maps.data.model.RouteInfo
import com.tuapp.maps.data.remote.NetworkModule

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

            if (response.status != "OK" || route == null || leg == null) {
                // Si esto imprime REQUEST_DENIED, casi siempre es porque DIRECTIONS_API_KEY
                // esta restringida a "Apps para Android" (esa restriccion no aplica a esta llamada REST).
                Log.e(TAG, "Directions API status=${response.status} error=${response.errorMessage}")
                return Result.failure(IllegalStateException(response.errorMessage ?: response.status))
            }

            val points = route.overviewPolyline?.points?.let { PolylineDecoder.decode(it) } ?: emptyList()

            Result.success(
                RouteInfo(
                    points = points,
                    distanceText = leg.distance?.text ?: "",
                    durationText = leg.duration?.text ?: ""
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Fallo al obtener la ruta", e)
            Result.failure(e)
        }
    }
}
