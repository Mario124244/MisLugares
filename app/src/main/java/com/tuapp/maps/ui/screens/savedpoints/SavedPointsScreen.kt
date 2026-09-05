package com.tuapp.maps.ui.screens.savedpoints

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tuapp.maps.R
import com.tuapp.maps.data.model.SavedPoint
import com.tuapp.maps.ui.components.CategoryChip
import com.tuapp.maps.viewmodel.SavedPointsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedPointsScreen(
    modifier: Modifier = Modifier,
    viewModel: SavedPointsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showNewCategoryDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = { showNewCategoryDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.new_category))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Text(
                stringResource(R.string.saved_points_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                item {
                    CategoryChip(
                        label = stringResource(R.string.all_categories),
                        selected = uiState.selectedCategory == null,
                        onClick = { viewModel.selectCategory(null) }
                    )
                }
                items(uiState.categories) { category ->
                    CategoryChip(
                        label = category.nombre,
                        selected = uiState.selectedCategory == category.nombre,
                        onClick = { viewModel.selectCategory(category.nombre) }
                    )
                }
            }

            if (uiState.filteredPoints.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.no_saved_points),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
                    items(uiState.filteredPoints, key = { it.id }) { point ->
                        SavedPointRow(point = point, onDelete = { viewModel.deletePoint(point.id) })
                    }
                }
            }
        }
    }

    if (showNewCategoryDialog) {
        var newCategoryName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewCategoryDialog = false },
            title = { Text(stringResource(R.string.new_category)) },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text(stringResource(R.string.category_name_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.createCategory(newCategoryName)
                    showNewCategoryDialog = false
                }) { Text(stringResource(R.string.create)) }
            },
            dismissButton = {
                TextButton(onClick = { showNewCategoryDialog = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

@Composable
private fun SavedPointRow(point: SavedPoint, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        ListItem(
            leadingContent = { Icon(Icons.Default.Place, contentDescription = null) },
            headlineContent = { Text(point.nombre) },
            supportingContent = { Text("${point.categoria} · ${point.direccion}") },
            trailingContent = {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                }
            }
        )
    }
}
