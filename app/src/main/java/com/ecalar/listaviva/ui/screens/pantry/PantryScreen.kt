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
    onNavigateToAdd: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Chips de categorías
            CategoryChips(
                selectedCategory = state.selectedCategory,
                onCategorySelected = viewModel::selectCategory
            )

            // Barra de búsqueda (siempre visible)
            SearchBar(
                query = state.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange
            )

            // Contenido
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
                    // Contador de productos
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

        // Diálogo de cambio de estado
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

        // Diálogo de confirmación de eliminación
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
}

@Composable
fun CategoryChips(
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit
) {
    val categories = listOf(
        "Lácteos", "Carnes", "Pescados", "Pastas y Arroces",
        "Verduras y Hortalizas", "Frutas", "Bebidas", "Limpieza", "Bazar", "Otros"
    )

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text("Todos") }
            )
        }
        items(categories) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = {
                    onCategorySelected(
                        if (selectedCategory == category) null else category
                    )
                },
                label = { Text(category) }
            )
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text("Buscar producto...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Limpiar búsqueda")
                }
            }
        },
        singleLine = true
    )
}

@Composable
fun EmptyPantry(onAddClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.Kitchen,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Tu despensa está vacía",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Añade productos para empezar",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Añadir producto")
            }
        }
    }
}

@Composable
fun PantryItemCard(
    item: PantryItem,
    onStatusClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val statusColor by animateColorAsState(
        targetValue = when (item.status) {
            ProductStatus.COMPLETO -> MaterialTheme.colorScheme.primary
            ProductStatus.MITAD -> MaterialTheme.colorScheme.tertiary
            ProductStatus.CASI_AGOTADO -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
            ProductStatus.AGOTADO -> MaterialTheme.colorScheme.error
        },
        label = "statusColor"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onStatusClick() }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador de estado
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = statusColor,
                        shape = MaterialTheme.shapes.small
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Información del producto
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.format.isNotBlank()) {
                    Text(
                        text = item.format,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.status.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor
                    )
                    if (item.notes.isNotBlank()) {
                        Text(
                            text = " · ${item.notes}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Botón eliminar
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar producto",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
fun StatusDialog(
    item: PantryItem,
    currentStatus: ProductStatus,
    onStatusSelected: (ProductStatus) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(item.name) },
        text = {
            Column {
                Text(
                    text = "Selecciona el estado del producto:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                ProductStatus.entries.forEach { status ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onStatusSelected(status) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = status == currentStatus,
                            onClick = { onStatusSelected(status) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = status.label,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
