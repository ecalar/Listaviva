package com.ecalar.listaviva.ui.despensa

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.ecalar.listaviva.domain.model.EstadoProducto
import com.ecalar.listaviva.domain.model.ListaCompra
import com.ecalar.listaviva.domain.model.ProductoDespensa
import com.ecalar.listaviva.ui.theme.neoBrutalism
import kotlinx.coroutines.launch


@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DespensaScreen(
    navController: NavController,
    onNavigateToAddProduct: () -> Unit,
    onNavigateToEditProduct: (String) -> Unit,
    onNavigateToScanner: () -> Unit,
    viewModel: DespensaViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val listasDisponibles by viewModel.listasDisponibles.collectAsState()
    val categoriaSeleccionada by viewModel.categoriaSeleccionada.collectAsState()

    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showDialog by remember { mutableStateOf(false) }
    var productoAAgotar by remember { mutableStateOf<ProductoDespensa?>(null) }
    var listaSeleccionada by remember { mutableStateOf<ListaCompra?>(null) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val backgroundColor = MaterialTheme.colorScheme.background
    val actionColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val codigoEscaneado by savedStateHandle?.getStateFlow<String?>("codigo_escaneado", null)?.collectAsState() ?: mutableStateOf(null)


    LaunchedEffect(Unit) {
        viewModel.comprobarEInicializarCatalogo(context)
    }

    LaunchedEffect(listasDisponibles, showDialog) {
        if (showDialog && listasDisponibles.isNotEmpty() && listaSeleccionada == null) {
            listaSeleccionada = listasDisponibles.first()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    if (isSearching) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Buscar producto...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(end = 8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = onSurfaceColor,
                                unfocusedBorderColor = onSurfaceColor,
                                focusedTextColor = onSurfaceColor,
                                unfocusedTextColor = onSurfaceColor
                            )
                        )
                    } else {
                        Text("Despensa", fontWeight = FontWeight.Black, color = onSurfaceColor)
                    }
                },
                actions = {
                    if (isSearching) {
                        IconButton(onClick = {
                            isSearching = false
                            searchQuery = ""
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar búsqueda", tint = onSurfaceColor)
                        }
                    } else {
                        IconButton(onClick = {
                            Toast.makeText(context, "Filtros próximamente", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filtrar", tint = onSurfaceColor)
                        }
                        IconButton(onClick = { isSearching = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Buscar producto", tint = onSurfaceColor)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        },
        floatingActionButton = {
            // Usamos un Row que ocupe el ancho completo para separar los botones a los extremos
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp), // Ajusta el padding para separarlos de los bordes
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingActionButton(
                    onClick = onNavigateToScanner,
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.neoBrutalism(cornerRadius = 16.dp, shadowOffset = 6.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Escanear Código")
                }

                FloatingActionButton(
                    onClick = onNavigateToAddProduct,
                    containerColor = actionColor,
                    contentColor = onPrimaryColor,
                    modifier = Modifier.neoBrutalism(cornerRadius = 16.dp, shadowOffset = 6.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir producto", modifier = Modifier.size(28.dp))
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        containerColor = backgroundColor
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            when (val state = uiState) {
                is DespensaState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = onSurfaceColor)
                    }
                }
                is DespensaState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }
                is DespensaState.Success -> {

                    if (state.categorias.size > 1) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            items(state.categorias) { categoria ->
                                val isSelected = categoria == categoriaSeleccionada
                                Surface(
                                    modifier = Modifier
                                        .clickable { viewModel.seleccionarCategoria(categoria) }
                                        .neoBrutalism(
                                            cornerRadius = 20.dp,
                                            borderWidth = 2.dp,
                                            shadowOffset = if(isSelected) 2.dp else 4.dp,
                                            borderColor = onSurfaceColor,
                                            shadowColor = onSurfaceColor
                                        ),
                                    color = if (isSelected) actionColor else surfaceColor,
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text(
                                        text = categoria,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) onPrimaryColor else onSurfaceColor,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (state.productosTotales.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Añade tu primer producto", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = onSurfaceColor)
                        }
                    } else if (state.productosFiltrados.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No hay productos en esta categoría", fontWeight = FontWeight.Bold, color = onSurfaceColor)
                        }
                    } else {
                        val productosAMostrar = if (searchQuery.isBlank()) {
                            state.productosFiltrados
                        } else {
                            state.productosFiltrados.filter {
                                it.nombre.contains(searchQuery, ignoreCase = true)
                            }
                        }

                        if (productosAMostrar.isEmpty() && searchQuery.isNotBlank()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No hay resultados para '$searchQuery'", fontWeight = FontWeight.Bold, color = onSurfaceColor)
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(productosAMostrar, key = { it.id }) { producto ->
                                    // Caja animada para que las tarjetas se deslicen suavemente
                                    Box(modifier = Modifier.animateItemPlacement()) {
                                        ProductoNeoCard(
                                            producto = producto,
                                            onCantidadChange = { incremento ->
                                                viewModel.cambiarCantidad(producto, incremento)
                                            },
                                            onAgotar = {
                                                productoAAgotar = producto
                                                showDialog = true
                                            },
                                            onEditar = { onNavigateToEditProduct(producto.id) },
                                            onEliminar = { viewModel.eliminarProducto(producto.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog && productoAAgotar != null) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                productoAAgotar = null
            },
            containerColor = surfaceColor,
            modifier = Modifier.neoBrutalism(cornerRadius = 16.dp, borderWidth = 2.dp, shadowOffset = 6.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor),
            title = { Text("¿Añadir a lista de la compra?", fontWeight = FontWeight.Black, color = onSurfaceColor) },
            text = {
                Column {
                    Text("El producto '${productoAAgotar?.nombre}' se marcará como agotado.", color = onSurfaceColor)
                    Spacer(modifier = Modifier.height(16.dp))

                    if (listasDisponibles.isEmpty()) {
                        Text(
                            "No tienes listas creadas.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        Text("Selecciona una lista:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = onSurfaceColor)
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            OutlinedButton(
                                onClick = { dropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = onSurfaceColor),
                                border = BorderStroke(width = 1.dp, color = onSurfaceColor)
                            ) {
                                Text(listaSeleccionada?.nombre ?: "Seleccionar...")
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false },
                                modifier = Modifier.background(surfaceColor)
                            ) {
                                listasDisponibles.forEach { lista ->
                                    DropdownMenuItem(
                                        text = { Text(lista.nombre, fontWeight = FontWeight.Bold, color = onSurfaceColor) },
                                        onClick = {
                                            listaSeleccionada = lista
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        listaSeleccionada?.let { lista ->
                            viewModel.agotarYAñadirALista(productoAAgotar!!, lista.id)
                            scope.launch {
                                snackbarHostState.showSnackbar("Añadido a '${lista.nombre}'")
                            }
                        }
                        showDialog = false
                        productoAAgotar = null
                    },
                    enabled = listasDisponibles.isNotEmpty() && listaSeleccionada != null,
                    colors = ButtonDefaults.buttonColors(containerColor = actionColor, contentColor = onPrimaryColor),
                    modifier = Modifier.neoBrutalism(cornerRadius = 8.dp, shadowOffset = 2.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor)
                ) {
                    Text("Añadir", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.cambiarEstadoProducto(productoAAgotar!!.id, EstadoProducto.AGOTADO.name.lowercase())
                        showDialog = false
                        productoAAgotar = null
                    }
                ) {
                    Text("Solo agotar", color = onSurfaceColor)
                }
            }
        )
    }
}

@Composable
fun ProductoNeoCard(
    producto: ProductoDespensa,
    onCantidadChange: (Int) -> Unit,
    onAgotar: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit

) {
    var menuExpanded by remember { mutableStateOf(false) }

    val (targetColor, textoEstado) = when (producto.estado.uppercase()) {
        EstadoProducto.COMPLETO.name -> Pair(Color(0xFF4ADE80), "COMPLETO")
        EstadoProducto.MITAD.name -> Pair(Color(0xFFFACC15), "MITAD")
        EstadoProducto.CASI_AGOTADO.name -> Pair(Color(0xFFF87171), "CASI AGOTADO")
        EstadoProducto.AGOTADO.name -> Pair(Color(0xFF9CA3AF), "AGOTADO")
        else -> Pair(Color.Gray, "DESCONOCIDO")
    }

    // Animación de color de la barra
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 500),
        label = "colorAnimation"
    )

    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val actionColor = MaterialTheme.colorScheme.primary

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp, end = 6.dp)
            .animateContentSize() // Expansión fluida
            .neoBrutalism(cornerRadius = 12.dp, shadowOffset = 6.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor),
        color = surfaceColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Box {
            Column(modifier = Modifier.padding(16.dp)) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    AsyncImage(
                        model = producto.imageUrl,
                        contentDescription = "Imagen del producto",
                        modifier = Modifier
                            .size(64.dp)
                            .padding(end = 8.dp) // <--- Margen para separar del nombre
                            .clip(RoundedCornerShape(8.dp)) // <--- Opcional: bordes redondeados
                    )

                    Text(
                        text = "${producto.nombre} (${producto.formato.ifEmpty { "ud" }})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        maxLines = 2,
                        color = onSurfaceColor,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Opciones",
                        tint = onSurfaceColor,
                        modifier = Modifier.clickable { menuExpanded = true }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (producto.cantidadActual == 1) {
                                onAgotar()
                            } else if (producto.cantidadActual > 0) {
                                onCantidadChange(-1)
                            }
                        },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .size(32.dp)
                            .border(1.dp, onSurfaceColor, RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.Remove, "Quitar", tint = onSurfaceColor)
                    }

                    Text(
                        text = "${producto.cantidadActual}",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = onSurfaceColor
                    )

                    IconButton(
                        onClick = { onCantidadChange(1) },
                        modifier = Modifier
                            .background(actionColor, RoundedCornerShape(8.dp))
                            .size(32.dp)
                            .border(1.dp, onSurfaceColor, RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.Add, "Añadir", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ESTADO VISUAL
                Text(
                    text = textoEstado,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = onSurfaceColor
                )
                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(animatedColor) // Color suavizado
                        .border(1.dp, onSurfaceColor, RoundedCornerShape(50))
                )
            }

            // MENÚ DESPLEGABLE
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                modifier = Modifier.background(surfaceColor)
            ) {
                DropdownMenuItem(
                    text = { Text("Editar Producto", fontWeight = FontWeight.Bold, color = onSurfaceColor) },
                    onClick = { menuExpanded = false; onEditar() }
                )
                HorizontalDivider(color = onSurfaceColor, thickness = 1.dp)
                DropdownMenuItem(
                    text = { Text("Eliminar", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) },
                    onClick = { menuExpanded = false; onEliminar() }
                )
            }
        }
    }
}