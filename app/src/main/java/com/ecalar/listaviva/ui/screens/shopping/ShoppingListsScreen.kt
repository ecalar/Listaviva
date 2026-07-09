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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ecalar.listaviva.domain.model.ShoppingItem
import com.ecalar.listaviva.domain.model.ShoppingList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListsScreen(
    viewModel: ShoppingListsViewModel = hiltViewModel(),
    onNavigateToAddItem: () -> Unit,
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
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.lists.isEmpty()) {
                EmptyLists(onCreateClick = { viewModel.showCreateDialog() })
            } else {
                ListsSelector(
                    lists = state.lists,
                    selectedListId = state.selectedListId,
                    onListSelected = { viewModel.selectList(it) }
                )

                HorizontalDivider()

                if (state.selectedListId != null) {
                    val pendingItems = state.items.filter { !it.purchased }
                    val purchasedItems = state.items.filter { it.purchased }

                    if (state.items.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Añade productos a esta lista", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(pendingItems, key = { it.id }) { item ->
                                ShoppingItemCard(
                                    item = item,
                                    onPurchased = { viewModel.markAsPurchased(item) },
                                    onDelete = { viewModel.deleteItem(item) }
                                )
                            }
                            if (purchasedItems.isNotEmpty()) {
                                item {
                                    Text("Comprado", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
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

        // Diálogos (crear, eliminar, renombrar)
        if (state.showCreateDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.hideCreateDialog() },
                title = { Text("Nueva lista") },
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
                    TextButton(onClick = { viewModel.createList() }, enabled = state.newListName.isNotBlank()) {
                        Text("Crear")
                    }
                },
                dismissButton = { TextButton(onClick = { viewModel.hideCreateDialog() }) { Text("Cancelar") } }
            )
        }
        // ... otros diálogos similares
    }

    if (showTopBar) {
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
                    FloatingActionButton(onClick = onNavigateToAddItem, containerColor = MaterialTheme.colorScheme.primary) {
                        Icon(Icons.Default.Add, contentDescription = "Añadir producto")
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding -> content(padding) }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            content(PaddingValues(0.dp))
            if (state.selectedListId != null) {
                FloatingActionButton(
                    onClick = onNavigateToAddItem,
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
}

// Funciones auxiliares (ListsSelector, EmptyLists, ShoppingItemCard) se mantienen
