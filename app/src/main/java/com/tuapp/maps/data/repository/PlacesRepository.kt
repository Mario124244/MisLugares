package com.tuapp.maps.data.repository

import android.content.Context
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.tuapp.maps.BuildConfig
import com.tuapp.maps.data.model.PlaceResult
import kotlinx.coroutines.tasks.await
import java.util.Locale
import java.util.UUID

class PlacesRepository(
    private val placesClient: PlacesClient,
    private val appContext: Context
) {
    private var sessionToken = AutocompleteSessionToken.newInstance()

    data class Prediction(val placeId: String, val primaryText: String, val secondaryText: String)

    // Simulador de lugares para búsquedas sin API Key pagada
    private val mockPlaces = listOf(
        PlaceResult("mock_1", "Parque Fundidora", "Avenida Fundidora y Adolfo Prieto, Obrera, Monterrey, N.L.", LatLng(25.6780, -100.2858)),
        PlaceResult("mock_2", "Macroplaza", "Zaragoza Sur S/N, Centro, Monterrey, N.L.", LatLng(25.6689, -100.3097)),
        PlaceResult("mock_3", "Paseo Santa Lucía", "Santa Lucía, Centro, Monterrey, N.L.", LatLng(25.6718, -100.3005)),
        PlaceResult("mock_4", "Cerro de la Silla", "Guadalupe, N.L.", LatLng(25.6322, -100.2312)),
        PlaceResult("mock_5", "Barrio Antiguo", "Centro, Monterrey, N.L.", LatLng(25.6661, -100.3060)),
        PlaceResult("mock_6", "Museo MARCO", "Zuazua y Jardón S/N, Centro, Monterrey, N.L.", LatLng(25.6653, -100.3101)),
        PlaceResult("mock_7", "Mirador del Obispado", "Rafael José Verger, Obispado, Monterrey, N.L.", LatLng(25.6728, -100.3475)),
        PlaceResult("mock_8", "Estadio BBVA", "Av. Pablo Livas 2011, Guadalupe, N.L.", LatLng(25.6703, -100.2447)),
        PlaceResult("mock_9", "Estadio Universitario", "San Nicolás de los Garza, N.L.", LatLng(25.7250, -100.3142)),
        PlaceResult("mock_10", "Parque Ecológico Chipinque", "Carretera a Chipinque, San Pedro Garza García, N.L.", LatLng(25.6174, -100.3644))
    )

    private val isMapsApiKeyPlaceholder = BuildConfig.MAPS_API_KEY.isBlank() ||
            BuildConfig.MAPS_API_KEY == "TU_MAPS_API_KEY_AQUI"

    suspend fun autocomplete(query: String): List<Prediction> {
        if (query.isBlank()) return emptyList()

        // Si no hay API Key real, filtramos de la lista local (simulador)
        if (isMapsApiKeyPlaceholder) {
            val q = query.lowercase().trim()
            return mockPlaces
                .filter { it.nombre.lowercase().contains(q) || it.direccion.lowercase().contains(q) }
                .map { Prediction(placeId = it.placeId, primaryText = it.nombre, secondaryText = it.direccion) }
        }

        // Si hay API Key válida, usamos el SDK de Places
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
        if (isMapsApiKeyPlaceholder) {
            return mockPlaces.find { it.placeId == placeId }
        }

        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
        val request = FetchPlaceRequest.newInstance(placeId, fields)
        return try {
            val response = placesClient.fetchPlace(request).await()
            val place = response.place
            val latLng = place.latLng ?: return null
            sessionToken = AutocompleteSessionToken.newInstance()
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
