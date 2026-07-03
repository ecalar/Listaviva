package com.ecalar.listaviva.ui.screens.pantry.add

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ecalar.listaviva.data.catalog.catalogData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    onNavigateBack: () -> Unit,
    onProductAdded: (String, String, String, String, String) -> Unit
) {
    var productName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedSubcategory by remember { mutableStateOf("") }
    var format by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var showCatalogSearch by remember { mutableStateOf(false) }
    var catalogSearch by remember { mutableStateOf("") }

    val categories = catalogData.map { it.name }
    val subcategories = if (selectedCategory.isNotEmpty()) {
        catalogData.find { it.name == selectedCategory }?.subcategories?.map { it.name } ?: emptyList()
    } else emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Añadir producto") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (productName.isNotBlank() && selectedCategory.isNotBlank()) {
                                onProductAdded(productName, selectedCategory, selectedSubcategory, format, notes)
                            }
                        },
                        enabled = productName.isNotBlank() && selectedCategory.isNotBlank()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Guardar")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Nombre del producto
            item {
                Text(
                    text = "Nombre del producto",
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ej: Leche semidesnatada") },
                    singleLine = true
                )
            }

            // Sugerencias del catálogo
            if (productName.length >= 2) {
                item {
                    TextButton(onClick = { showCatalogSearch = !showCatalogSearch }) {
                        Text(
                            if (showCatalogSearch) "Ocultar sugerencias" else "Ver sugerencias del catálogo"
                        )
                    }

                    if (showCatalogSearch) {
                        val filteredProducts = catalogData
                            .flatMap { cat ->
                                cat.subcategories.flatMap { sub ->
                                    sub.products.map { Triple(it, cat.name, sub.name) }
                                }
                            }
                            .filter { it.first.lowercase().contains(productName.lowercase()) }
                            .take(10)

                        filteredProducts.forEach { (name, cat, sub) ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        productName = name
                                        selectedCategory = cat
                                        selectedSubcategory = sub
                                        showCatalogSearch = false
                                    }
                                    .padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(text = name, style = MaterialTheme.typography.bodyLarge)
                                        Text(
                                            text = "$cat > $sub",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Categoría
            item {
                Text(
                    text = "Categoría",
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                CategoryDropdown(
                    selectedCategory = selectedCategory,
                    categories = categories,
                    onCategorySelected = {
                        selectedCategory = it
                        selectedSubcategory = ""
                    }
                )
            }

            // Subcategoría (si hay categoría seleccionada)
            if (subcategories.isNotEmpty()) {
                item {
                    Text(
                        text = "Subcategoría",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CategoryDropdown(
                        selectedCategory = selectedSubcategory,
                        categories = subcategories,
                        onCategorySelected = { selectedSubcategory = it },
                        label = "Seleccionar subcategoría"
                    )
                }
            }

            // Formato / Cantidad
            item {
                Text(
                    text = "Formato o cantidad (opcional)",
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = format,
                    onValueChange = { format = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ej: 1L, 500g, 2 paquetes") },
                    singleLine = true
                )
            }

            // Notas
            item {
                Text(
                    text = "Notas (opcional)",
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ej: Comprar marca Hacendado") },
                    maxLines = 2
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(
    selectedCategory: String,
    categories: List<String>,
    onCategorySelected: (String) -> Unit,
    label: String = "Seleccionar categoría"
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedCategory,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}
