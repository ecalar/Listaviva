package com.ecalar.listaviva.ui.screens.shopping.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ecalar.listaviva.data.catalog.allProducts
import com.ecalar.listaviva.domain.model.PantryItem
import com.ecalar.listaviva.domain.model.ProductStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemToListScreen(
    pantryItems: List<PantryItem>,
    onAddFromPantry: (PantryItem) -> Unit,
    onAddManual: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showManualAdd by remember { mutableStateOf(false) }
    var manualName by remember { mutableStateOf("") }

    val filteredPantry = if (searchQuery.isNotBlank()) {
        pantryItems.filter {
            it.name.lowercase().contains(searchQuery.lowercase())
        }
    } else {
        pantryItems.filter { it.status == ProductStatus.AGOTADO || it.status == ProductStatus.CASI_AGOTADO }
    }

    val filteredCatalog = if (searchQuery.length >= 2) {
        allProducts.filter { it.lowercase().contains(searchQuery.lowercase()) }.take(15)
    } else emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Añadir a la lista") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    TextButton(onClick = { showManualAdd = true }) {
                        Text("Manual")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Buscar producto...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar")
                        }
                    }
                },
                singleLine = true
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Productos de la despensa (agotados o casi agotados)
                if (searchQuery.isBlank()) {
                    item {
                        Text(
                            text = "Productos por agotarse en tu despensa",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                items(filteredPantry) { item ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAddFromPantry(item) }
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = item.name, style = MaterialTheme.typography.bodyLarge)
                                Row {
                                    Text(
                                        text = item.status.label,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    if (item.format.isNotBlank()) {
                                        Text(
                                            text = " · ${item.format}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Sugerencias del catálogo
                if (filteredCatalog.isNotEmpty()) {
                    item {
                        Text(
                            text = "Sugerencias del catálogo",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(filteredCatalog) { product ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAddManual(product) }
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = product, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
        }

        // Diálogo añadir manualmente
        if (showManualAdd) {
            AlertDialog(
                onDismissRequest = { showManualAdd = false },
                title = { Text("Añadir manualmente") },
                text = {
                    OutlinedTextField(
                        value = manualName,
                        onValueChange = { manualName = it },
                        label = { Text("Nombre del producto") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (manualName.isNotBlank()) {
                                onAddManual(manualName)
                                showManualAdd = false
                            }
                        },
                        enabled = manualName.isNotBlank()
                    ) {
                        Text("Añadir")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showManualAdd = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
