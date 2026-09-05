package com.tuapp.maps.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tuapp.maps.R
import com.tuapp.maps.data.model.PlaceResult
import com.tuapp.maps.data.model.RouteInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceBottomSheet(
    place: PlaceResult,
    categories: List<String>,
    route: RouteInfo?,
    isRouteLoading: Boolean,
    onGetDirections: () -> Unit,
    onSave: (categoria: String) -> Unit,
    onShare: () -> Unit,
    onDismiss: () -> Unit
) {
    var showCategoryPicker by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text(place.nombre, style = MaterialTheme.typography.titleLarge)
            if (place.direccion.isNotBlank()) {
                Text(
                    place.direccion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (route != null) {
                Text(
                    "${route.distanceText} · ${route.durationText}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = if (showCategoryPicker) 4.dp else 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(onClick = onGetDirections, modifier = Modifier.weight(1f)) {
                    if (isRouteLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp))
                    } else {
                        Icon(Icons.Default.Directions, contentDescription = null)
                        Text(" " + stringResource(R.string.how_to_get_there))
                    }
                }
                OutlinedButton(onClick = { showCategoryPicker = !showCategoryPicker }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Star, contentDescription = null)
                    Text(" " + stringResource(R.string.save))
                }
                OutlinedButton(onClick = onShare, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Text(" " + stringResource(R.string.share))
                }
            }

            if (showCategoryPicker) {
                Text(
                    stringResource(R.string.save_point_title),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { categoria ->
                        CategoryChip(
                            label = categoria,
                            selected = false,
                            onClick = {
                                onSave(categoria)
                                showCategoryPicker = false
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
