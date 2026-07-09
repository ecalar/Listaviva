package com.ecalar.listaviva.ui.screens.pantry

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ecalar.listaviva.domain.model.PantryItem
import com.ecalar.listaviva.domain.model.ProductStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryScreen(
    viewModel: PantryViewModel = hiltViewModel(),
    onNavigateToAdd: () -> Unit,
    showTopBar: Boolean = true
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    val content = @Composable { paddingValues: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            CategoryChips(
                selectedCategory = state.selectedCategory,
                onCategorySelected = viewModel::selectCategory
            )

            SearchBar(
                query = state.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange
            )

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                state.filteredItems.isEmpty() && state.items.isEmpty() -> {
                    EmptyPantry(onAddClick = onNavigateToAdd)
                }
                state.filteredItems.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontraron productos",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                else -> {
                    Text(
                        text = "${state.filteredItems.size} productos",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.filteredItems, key = { it.id }) { item ->
                            PantryItemCard(
                                item = item,
                                onStatusClick = { viewModel.showStatusDialog(item) },
                                onDeleteClick = { viewModel.showDeleteDialog(item) }
                            )
                        }
                    }
                }
            }
        }

        // Diálogos
        state.showStatusDialog?.let { item ->
            StatusDialog(
                item = item,
                currentStatus = item.status,
                onStatusSelected = { newStatus ->
                    viewModel.updateItemStatus(item, newStatus)
                    viewModel.showStatusDialog(null)
                },
                onDismiss = { viewModel.showStatusDialog(null) }
            )
        }

        state.showDeleteDialog?.let { item ->
            AlertDialog(
                onDismissRequest = { viewModel.showDeleteDialog(null) },
                title = { Text("Eliminar producto") },
                text = { Text("¿Estás seguro de que quieres eliminar \"${item.name}\" de la despensa?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteItem(item)
                            viewModel.showDeleteDialog(null)
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.showDeleteDialog(null) }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }

    if (showTopBar) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Despensa") },
                    actions = {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Search, contentDescription = "Buscar")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onNavigateToAdd,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir producto")
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            content(padding)
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            content(PaddingValues(0.dp))
            // FAB manual
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir producto")
            }
        }
    }
}

// ... (el resto de funciones auxiliares se mantienen igual: CategoryChips, SearchBar, EmptyPantry, PantryItemCard, StatusDialog)
