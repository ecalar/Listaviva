package com.ecalar.listaviva.ui.listas

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ecalar.listaviva.R
import com.ecalar.listaviva.domain.model.ItemLista
import com.ecalar.listaviva.domain.model.ListaCompra
import com.ecalar.listaviva.ui.theme.neoBrutalism
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ListasScreen(viewModel: ListasViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val itemsSeleccionados by viewModel.itemsSeleccionados.collectAsState()
    val vibracionEnabled by viewModel.vibracionEnabled.collectAsState()
    val confirmarBorrado by viewModel.confirmarBorrado.collectAsState()

    val backgroundColor = MaterialTheme.colorScheme.background
    val actionColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    var listaParaEditar by remember { mutableStateOf<ListaCompra?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<ItemLista?>(null) }

    // --- CORRECCIÓN: Mantener pantalla encendida de forma segura ---
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Listas", fontWeight = FontWeight.Black, color = onSurfaceColor) },
                actions = {
                    // Botón para ocultar/mostrar productos comprados (solo visible si hay una lista cargada)
                    if (uiState is ListasState.Success) {
                        val state = uiState as ListasState.Success
                        IconButton(onClick = { viewModel.toggleOcultarComprados() }) {
                            Icon(
                                imageVector = if (state.itemsMarcadosOcultos) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Ocultar/Mostrar marcados",
                                tint = onSurfaceColor
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = actionColor,
                contentColor = onPrimaryColor,
                modifier = Modifier.neoBrutalism(cornerRadius = 16.dp, shadowOffset = 6.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Lista")
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

                        if (state.itemsPendientes.isEmpty() && state.itemsMarcadosOcultos && itemsSeleccionados.isNotEmpty()) {
                            // Mensaje si los ocultamos todos
                            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text("Todos los productos están marcados (y ocultos)", fontWeight = FontWeight.Bold, color = onSurfaceColor.copy(alpha = 0.6f))
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(state.itemsPendientes, key = { it.id }) { item ->
                                    val isSeleccionado = itemsSeleccionados.contains(item.id)

                                    Box(modifier = Modifier.animateItemPlacement()) {
                                        ItemListaNeoCard(
                                            item = item,
                                            isSelected = isSeleccionado,
                                            confirmarBorrado = confirmarBorrado,
                                            onToggleSelect = {
                                                if (vibracionEnabled) {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                }
                                                viewModel.toggleItemSeleccionado(item.id)
                                            },
                                            onCantidadChange = { incremento ->
                                                viewModel.cambiarCantidadItem(item, incremento)
                                            },
                                            onDelete = {
                                                if (confirmarBorrado) {
                                                    itemToDelete = item
                                                } else {
                                                    viewModel.eliminarItemDeLista(item)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        if (itemsSeleccionados.isNotEmpty()) {
                            val marcados = itemsSeleccionados.size
                            val totales = state.itemsPendientes.size + (if (state.itemsMarcadosOcultos) marcados else 0)

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp)
                                    .neoBrutalism(cornerRadius = 16.dp, shadowOffset = 6.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor),
                                color = actionColor,
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("En el carro:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = onPrimaryColor)
                                        Text(
                                            text = "$marcados / $totales",
                                            fontWeight = FontWeight.Black,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = onPrimaryColor
                                        )
                                    }

                                    Button(
                                        onClick = { showConfirmDialog = true },
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

    // --- DIÁLOGO DE CONFIRMACIÓN DE BORRADO INDIVIDUAL ---
    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            containerColor = surfaceColor,
            modifier = Modifier.neoBrutalism(cornerRadius = 16.dp, borderWidth = 2.dp, shadowOffset = 6.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor),
            title = { Text("Eliminar Producto", fontWeight = FontWeight.Black, color = onSurfaceColor) },
            text = { Text("¿Deseas eliminar '${itemToDelete?.nombre}' de la lista de la compra?", color = onSurfaceColor) },
            confirmButton = {
                Button(
                    onClick = {
                        itemToDelete?.let { viewModel.eliminarItemDeLista(it) }
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) { Text("Cancelar", color = onSurfaceColor) }
            }
        )
    }

    // --- RESTO DE DIÁLOGOS ORIGINALES ---
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
                        scope.launch {
                            snackbarHostState.showSnackbar("¡$marcados productos enviados a la despensa!")
                        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemListaNeoCard(
    item: ItemLista,
    isSelected: Boolean,
    confirmarBorrado: Boolean,
    onToggleSelect: () -> Unit,
    onCantidadChange: (Int) -> Unit,
    onDelete: () -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                !confirmarBorrado
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                Color(0xFFEF4444)
            } else {
                Color.Transparent
            }

            Box(
                Modifier
                    .fillMaxSize()
                    .padding(vertical = 6.dp)
                    .background(color, RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        content = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onToggleSelect() }
                    .animateContentSize()
                    .neoBrutalism(cornerRadius = 12.dp, shadowOffset = 4.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor),
                color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else surfaceColor,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = isSelected, onCheckedChange = { onToggleSelect() })
                    Spacer(Modifier.width(8.dp))
                    AsyncImage(
                        model = item.imageUrl.ifBlank { null },
                        contentDescription = item.nombre,
                        placeholder = painterResource(R.drawable.logo),
                        error = painterResource(R.drawable.logo),
                        fallback = painterResource(R.drawable.logo),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(2.dp, onSurfaceColor, RoundedCornerShape(10.dp))
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${item.nombre} - ${item.cantidad}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { onCantidadChange(-1) }) { Icon(Icons.Default.Remove, null) }
                        Text("${item.cantidadAComprar}", fontWeight = FontWeight.Bold)
                        IconButton(onClick = { onCantidadChange(1) }) { Icon(Icons.Default.Add, null) }
                    }
                }
            }
        }
    )
}