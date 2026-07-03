package com.ecalar.listaviva.ui.screens.shopping

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ecalar.listaviva.domain.model.ShoppingItem
import com.ecalar.listaviva.domain.model.ShoppingList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListsScreen(
    viewModel: ShoppingListsViewModel = hiltViewModel(),
    onNavigateToAddItem: () -> Unit
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
                title = { Text("Listas de la compra") },
                actions = {
                    IconButton(onClick = { viewModel.showCreateDialog() }) {
                        Icon(Icons.Default.Add, contentDescription = "Nueva lista")
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.selectedListId != null) {
                FloatingActionButton(
                    onClick = onNavigateToAddItem,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir producto")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.lists.isEmpty()) {
                EmptyLists(onCreateClick = { viewModel.showCreateDialog() })
            } else {
                // Selector horizontal de listas
                ListsSelector(
                    lists = state.lists,
                    selectedListId = state.selectedListId,
                    onListSelected = { viewModel.selectList(it) },
                    onListLongPressed = { list ->
                        // Menú contextual
                    }
                )

                Divider()

                // Items de la lista seleccionada
                if (state.selectedListId != null) {
                    val pendingItems = state.items.filter { !it.purchased }
                    val purchasedItems = state.items.filter { it.purchased }

                    if (state.items.isEmpty()) {
                        EmptyListMessage()
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Pendientes
                            items(pendingItems, key = { it.id }) { item ->
                                ShoppingItemCard(
                                    item = item,
                                    onPurchased = { viewModel.markAsPurchased(item) },
                                    onDelete = { viewModel.deleteItem(item) }
                                )
                            }

                            // Comprados
                            if (purchasedItems.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Comprado",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                                items(purchasedItems, key = { it.id }) { item ->
                                    ShoppingItemCard(
                                        item = item,
                                        onPurchased = {},
                                        onDelete = { viewModel.deleteItem(item) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Diálogo crear lista
        if (state.showCreateDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.hideCreateDialog() },
                title = { Text("Nueva lista") },
                text = {
                    OutlinedTextField(
                        value = state.newListName,
                        onValueChange = viewModel::onNewListNameChange,
                        label = { Text("Nombre de la lista") },
                        placeholder = { Text("Ej: Mercadona, LIDL...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.createList() },
                        enabled = state.newListName.isNotBlank()
                    ) {
                        Text("Crear")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.hideCreateDialog() }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // Diálogo eliminar lista
        state.showDeleteDialog?.let { list ->
            AlertDialog(
                onDismissRequest = { viewModel.hideDeleteDialog() },
                title = { Text("Eliminar lista") },
                text = { Text("¿Estás seguro de que quieres eliminar \"${list.name}\" y todos sus productos?") },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.deleteList() },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.hideDeleteDialog() }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // Diálogo renombrar lista
        state.showRenameDialog?.let { list ->
            AlertDialog(
                onDismissRequest = { viewModel.hideRenameDialog() },
                title = { Text("Renombrar lista") },
                text = {
                    OutlinedTextField(
                        value = state.newListName,
                        onValueChange = viewModel::onNewListNameChange,
                        label = { Text("Nombre de la lista") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.renameList() },
                        enabled = state.newListName.isNotBlank()
                    ) {
                        Text("Renombrar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.hideRenameDialog() }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun ListsSelector(
    lists: List<ShoppingList>,
    selectedListId: String?,
    onListSelected: (ShoppingList) -> Unit,
    onListLongPressed: (ShoppingList) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(lists) { list ->
            val isSelected = list.id == selectedListId
            FilterChip(
                selected = isSelected,
                onClick = { onListSelected(list) },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(list.name)
                    }
                }
            )
        }
    }
}

@Composable
fun EmptyLists(onCreateClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No tienes listas aún",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Crea listas para Mercadona, LIDL...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onCreateClick) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Crear primera lista")
            }
        }
    }
}

@Composable
fun EmptyListMessage() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Añade productos a esta lista",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun ShoppingItemCard(
    item: ShoppingItem,
    onPurchased: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (item.purchased)
                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.purchased) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = item.purchased,
                onCheckedChange = { if (!item.purchased) onPurchased() }
            )
            Spacer(modifier = Modifier.width(8.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (item.purchased) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (item.purchased)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                if (item.quantity.isNotBlank()) {
                    Text(
                        text = item.quantity,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Text(
                    text = "Añadido por ${item.addedByAlias}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }

            // Eliminar
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}
