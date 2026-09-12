package com.tuapp.maps.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.tuapp.maps.data.model.PlaceResult

// Coordenadas de los límites de Monterrey para la proyección en el lienzo de dibujo
private const val MIN_LAT = 25.5800
private const val MAX_LAT = 25.8200
private const val MIN_LNG = -100.4500
private const val MAX_LNG = -100.1800

@Composable
fun MonterreyMapFallback(
    selectedPlace: PlaceResult?,
    routePoints: List<LatLng>,
    onMapClick: (LatLng) -> Unit,
    modifier: Modifier = Modifier
) {
    var zoomScale by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val gridLineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val mapBgColor = Color(0xFFE8ECEF)
    val riverColor = Color(0xFF81D4FA)
    val mountainColor = Color(0xFFB0BEC5)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(mapBgColor)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        zoomScale = (zoomScale * zoom).coerceIn(0.5f, 3.5f)
                        panOffset += pan
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        // Convertir la posición tocada en el lienzo a coordenadas LatLng
                        val width = size.width.toFloat()
                        val height = size.height.toFloat()
                        val normX = (tapOffset.x - panOffset.x) / (width * zoomScale)
                        val normY = (tapOffset.y - panOffset.y) / (height * zoomScale)

                        val lng = MIN_LNG + normX * (MAX_LNG - MIN_LNG)
                        val lat = MAX_LAT - normY * (MAX_LAT - MIN_LAT)

                        onMapClick(LatLng(lat, lng))
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            // Función auxiliar para convertir LatLng a píxeles en pantalla
            fun latLngToOffset(latLng: LatLng): Offset {
                val normX = ((latLng.longitude - MIN_LNG) / (MAX_LNG - MIN_LNG)).toFloat()
                val normY = ((MAX_LAT - latLng.latitude) / (MAX_LAT - MIN_LAT)).toFloat()
                val x = normX * width * zoomScale + panOffset.x
                val y = normY * height * zoomScale + panOffset.y
                return Offset(x, y)
            }

            // 1. Dibujar silueta del Cerro de la Silla y montañas
            val mountainPath = Path().apply {
                val p1 = latLngToOffset(LatLng(25.6500, -100.2700))
                val p2 = latLngToOffset(LatLng(25.6322, -100.2312))
                val p3 = latLngToOffset(LatLng(25.6100, -100.2000))
                moveTo(p1.x, p1.y)
                lineTo(p2.x, p2.y)
                lineTo(p3.x, p3.y)
            }
            drawPath(mountainPath, color = mountainColor, style = Stroke(width = 16f * zoomScale))

            // 2. Dibujar Río Santa Catarina (atraviesa la ciudad de OESTE a ESTE)
            val riverPath = Path().apply {
                val r1 = latLngToOffset(LatLng(25.6700, -100.4000))
                val r2 = latLngToOffset(LatLng(25.6680, -100.3100))
                val r3 = latLngToOffset(LatLng(25.6720, -100.2400))
                val r4 = latLngToOffset(LatLng(25.6800, -100.1900))
                moveTo(r1.x, r1.y)
                lineTo(r2.x, r2.y)
                lineTo(r3.x, r3.y)
                lineTo(r4.x, r4.y)
            }
            drawPath(riverPath, color = riverColor, style = Stroke(width = 12f * zoomScale))

            // 3. Dibujar red vial principal de Monterrey
            val roads = listOf(
                listOf(LatLng(25.7500, -100.3100), LatLng(25.6400, -100.3100)), // Av. Universidad / Juárez
                listOf(LatLng(25.6800, -100.3800), LatLng(25.6800, -100.2200)), // Av. Constitución / Morones Prieto
                listOf(LatLng(25.7200, -100.3600), LatLng(25.6500, -100.3600))  // Av. Gonzalitos
            )

            for (road in roads) {
                val p1 = latLngToOffset(road[0])
                val p2 = latLngToOffset(road[1])
                drawLine(
                    color = gridLineColor,
                    start = p1,
                    end = p2,
                    strokeWidth = 6f * zoomScale
                )
            }

            // 4. Dibujar la ruta en color primario si está calculada
            if (routePoints.size >= 2) {
                val routePath = Path()
                routePoints.forEachIndexed { i, pt ->
                    val offset = latLngToOffset(pt)
                    if (i == 0) routePath.moveTo(offset.x, offset.y) else routePath.lineTo(offset.x, offset.y)
                }
                drawPath(
                    path = routePath,
                    color = primaryColor,
                    style = Stroke(width = 12f * zoomScale)
                )
            }

            // 5. Dibujar el marcador del lugar seleccionado
            selectedPlace?.let { place ->
                val markerOffset = latLngToOffset(place.latLng)
                drawCircle(
                    color = primaryColor,
                    radius = 18f * zoomScale,
                    center = markerOffset
                )
                drawCircle(
                    color = Color.White,
                    radius = 8f * zoomScale,
                    center = markerOffset
                )
            }
        }

        // Etiqueta indicadora de la ciudad en la esquina superior izquierda
        Surface(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart),
            color = surfaceColor.copy(alpha = 0.9f),
            shape = RoundedCornerShape(8.dp),
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                Text(
                    text = "Monterrey, N.L., México",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Mapa interactivo",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Botones de control de Zoom y Centrado en la derecha
        Card(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column {
                IconButton(onClick = { zoomScale = (zoomScale * 1.3f).coerceAtMost(3.5f) }) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom +")
                }
                IconButton(onClick = { zoomScale = (zoomScale / 1.3f).coerceAtLeast(0.5f) }) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom -")
                }
                IconButton(onClick = {
                    zoomScale = 1f
                    panOffset = Offset.Zero
                }) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Centrar")
                }
            }
        }
    }
}
