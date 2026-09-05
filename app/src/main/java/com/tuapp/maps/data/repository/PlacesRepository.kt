package com.tuapp.maps.data.repository

import android.content.Context
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.tuapp.maps.data.model.PlaceResult
import kotlinx.coroutines.tasks.await
import java.util.Locale
import java.util.UUID

class PlacesRepository(
    private val placesClient: PlacesClient,
    private val appContext: Context
) {
    private var sessionToken = com.google.android.libraries.places.api.model.AutocompleteSessionToken.newInstance()

    data class Prediction(val placeId: String, val primaryText: String, val secondaryText: String)

    suspend fun autocomplete(query: String): List<Prediction> {
        if (query.isBlank()) return emptyList()
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setSessionToken(sessionToken)
            .build()
        return try {
            val response = placesClient.findAutocompletePredictions(request).await()
            response.autocompletePredictions.map {
                Prediction(
                    placeId = it.placeId,
                    primaryText = it.getPrimaryText(null).toString(),
                    secondaryText = it.getSecondaryText(null).toString()
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun fetchPlace(placeId: String): PlaceResult? {
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
        val request = FetchPlaceRequest.newInstance(placeId, fields)
        return try {
            val response = placesClient.fetchPlace(request).await()
            val place = response.place
            val latLng = place.latLng ?: return null
            sessionToken = com.google.android.libraries.places.api.model.AutocompleteSessionToken.newInstance()
            PlaceResult(
                placeId = place.id ?: placeId,
                nombre = place.name ?: "",
                direccion = place.address ?: "",
                latLng = latLng
            )
        } catch (e: Exception) {
            null
        }
    }

    /** Para un toque directo en el mapa: no hay placeId real, se genera uno local y se resuelve la direccion por Geocoder. */
    suspend fun resolveTappedLocation(latLng: LatLng): PlaceResult {
        val direccion = try {
            @Suppress("DEPRECATION")
            Geocoder(appContext, Locale.getDefault())
                .getFromLocation(latLng.latitude, latLng.longitude, 1)
                ?.firstOrNull()
                ?.getAddressLine(0)
        } catch (e: Exception) {
            null
        } ?: "${latLng.latitude}, ${latLng.longitude}"

        return PlaceResult(
            placeId = "tap:${UUID.randomUUID()}",
            nombre = direccion.substringBefore(","),
            direccion = direccion,
            latLng = latLng
        )
    }
}
