package com.ecalar.listaviva.ui.listas

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ecalar.listaviva.domain.model.ItemLista
import com.ecalar.listaviva.domain.model.ListaCompra
import com.ecalar.listaviva.ui.theme.neoBrutalism

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ListasScreen(viewModel: ListasViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val itemsSeleccionados by viewModel.itemsSeleccionados.collectAsState()

    val backgroundColor = MaterialTheme.colorScheme.background
    val actionColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary

    var listaParaEditar by remember { mutableStateOf<ListaCompra?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Listas", fontWeight = FontWeight.Black, color = onSurfaceColor) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        },
        floatingActionButton = {
            if (itemsSeleccionados.isEmpty()) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = actionColor,
                    contentColor = onPrimaryColor,
                    modifier = Modifier.neoBrutalism(cornerRadius = 16.dp, shadowOffset = 6.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir Lista")
                }
            }
        },
        containerColor = backgroundColor
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is ListasState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = onSurfaceColor)
                is ListasState.Success -> {
                    if (state.listas.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No tienes listas. ¡Crea una!", fontWeight = FontWeight.Bold, color = onSurfaceColor.copy(alpha = 0.7f))
                        }
                    } else {
                        ScrollableTabRow(
                            selectedTabIndex = state.listas.indexOf(state.listaSeleccionada).coerceAtLeast(0),
                            containerColor = Color.Transparent,
                            edgePadding = 16.dp,
                            divider = {},
                            indicator = {}
                        ) {
                            state.listas.forEach { lista ->
                                val isSelected = state.listaSeleccionada == lista

                                Box(modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)) {
                                    Surface(
                                        color = if (isSelected) actionColor else surfaceColor,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .neoBrutalism(cornerRadius = 12.dp, borderWidth = 2.dp, shadowOffset = if (isSelected) 2.dp else 4.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor)
                                            .combinedClickable(
                                                onClick = { viewModel.seleccionarLista(lista) },
                                                onLongClick = {
                                                    listaParaEditar = lista
                                                    showEditDialog = true
                                                }
                                            )
                                    ) {
                                        Text(
                                            text = lista.nombre,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                            color = if (isSelected) onPrimaryColor else onSurfaceColor
                                        )
                                    }
                                }
                            }
                        }

                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(state.itemsPendientes, key = { it.id }) { item ->
                                val isSeleccionado = itemsSeleccionados.contains(item.id)

                                ItemListaNeoCard(
                                    item = item,
                                    isSeleccionado = isSeleccionado,
                                    onToggle = { viewModel.toggleItemSeleccionado(item.id) }
                                )
                            }
                        }

                        if (state.itemsPendientes.isNotEmpty()) {
                            val marcados = itemsSeleccionados.size
                            val totales = state.itemsPendientes.size

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .neoBrutalism(cornerRadius = 16.dp, shadowOffset = 6.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor),
                                color = if (marcados > 0) actionColor else surfaceColor,
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("En el carro:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if (marcados > 0) onPrimaryColor else onSurfaceColor)
                                        Text(
                                            text = "$marcados / $totales",
                                            fontWeight = FontWeight.Black,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = if (marcados > 0) onPrimaryColor else onSurfaceColor
                                        )
                                    }

                                    Button(
                                        onClick = { if (marcados > 0) showConfirmDialog = true },
                                        enabled = marcados > 0,
                                        colors = ButtonDefaults.buttonColors(containerColor = onSurfaceColor, contentColor = backgroundColor),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Confirmar", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }

    if (showCreateDialog) {
        var nuevaListaNombre by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = surfaceColor,
            modifier = Modifier.neoBrutalism(cornerRadius = 16.dp, borderWidth = 2.dp, shadowOffset = 6.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor),
            title = { Text("Nueva Lista", fontWeight = FontWeight.Black, color = onSurfaceColor) },
            text = {
                OutlinedTextField(
                    value = nuevaListaNombre,
                    onValueChange = { nuevaListaNombre = it },
                    label = { Text("Nombre (Ej: Mercadona)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = onSurfaceColor,
                        unfocusedBorderColor = onSurfaceColor.copy(alpha = 0.5f),
                        focusedTextColor = onSurfaceColor,
                        unfocusedTextColor = onSurfaceColor
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if(nuevaListaNombre.isNotBlank()) {
                            viewModel.crearLista(nuevaListaNombre)
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = actionColor, contentColor = onPrimaryColor)
                ) { Text("Crear", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancelar", color = onSurfaceColor) }
            }
        )
    }

    if (showEditDialog && listaParaEditar != null) {
        var nuevoNombre by remember { mutableStateOf(listaParaEditar!!.nombre) }
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            containerColor = surfaceColor,
            modifier = Modifier.neoBrutalism(cornerRadius = 16.dp, borderWidth = 2.dp, shadowOffset = 6.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor),
            title = { Text("Gestionar Lista", fontWeight = FontWeight.Black, color = onSurfaceColor) },
            text = {
                OutlinedTextField(
                    value = nuevoNombre,
                    onValueChange = { nuevoNombre = it },
                    label = { Text("Nombre") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = onSurfaceColor,
                        unfocusedBorderColor = onSurfaceColor.copy(alpha = 0.5f),
                        focusedTextColor = onSurfaceColor,
                        unfocusedTextColor = onSurfaceColor
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.editarNombreLista(listaParaEditar!!.id, nuevoNombre)
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = actionColor, contentColor = onPrimaryColor)
                ) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = {
                    listaParaEditar?.let {
                        viewModel.eliminarLista(it.id)
                        showEditDialog = false
                    }
                }) {
                    Text("Eliminar", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showConfirmDialog) {
        val marcados = itemsSeleccionados.size
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            containerColor = surfaceColor,
            modifier = Modifier.neoBrutalism(cornerRadius = 16.dp, borderWidth = 2.dp, shadowOffset = 6.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor),
            title = { Text("Confirmar Compra", fontWeight = FontWeight.Black, color = onSurfaceColor) },
            text = {
                Text(
                    "¿Has comprado los $marcados productos marcados?\n\nAl confirmar, desaparecerán de la lista y se pondrán como 'Completos' en la despensa.",
                    fontWeight = FontWeight.Bold,
                    color = onSurfaceColor
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.confirmarCompra()
                        showConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = actionColor, contentColor = onPrimaryColor)
                ) { Text("Sí, confirmar", fontWeight = FontWeight.Black) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("Aún no", color = onSurfaceColor) }
            }
        )
    }
}

@Composable
fun ItemListaNeoCard(
    item: ItemLista,
    isSeleccionado: Boolean,
    onToggle: () -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    // En modo oscuro o claro usamos una variante para los seleccionados
    val cardColor = if (isSeleccionado) MaterialTheme.colorScheme.surfaceVariant else surfaceColor

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp, end = 6.dp)
            .neoBrutalism(cornerRadius = 12.dp, shadowOffset = if (isSeleccionado) 2.dp else 6.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor)
            .clickable { onToggle() },
        color = cardColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSeleccionado,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    checkmarkColor = MaterialTheme.colorScheme.onPrimary,
                    uncheckedColor = onSurfaceColor
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = if (isSeleccionado) onSurfaceColor.copy(alpha = 0.5f) else onSurfaceColor
                )
                Text(
                    text = "${item.cantidad.ifEmpty { "1 ud" }} • Añadido por ${item.aliasAñadidoPor}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = onSurfaceColor.copy(alpha = 0.6f)
                )
            }
        }
    }
}