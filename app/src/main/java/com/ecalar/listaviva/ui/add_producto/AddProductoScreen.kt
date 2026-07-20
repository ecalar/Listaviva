package com.ecalar.listaviva.ui.add_producto

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ecalar.listaviva.ui.theme.neoBrutalism

@SuppressLint("AutoboxingStateCreation")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductoScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddProductoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val step by viewModel.currentStep.collectAsState()
    val categoria by viewModel.categoriaSeleccionada.collectAsState()
    val producto by viewModel.productoSeleccionado.collectAsState()

    val catalogo by viewModel.catalogoCompleto.collectAsState()

    // Declaración única y anclada de las categorías
    val categorias = remember(catalogo) {
        catalogo.map { it.categoria }.distinct().sorted()
    }

    val backgroundColor = MaterialTheme.colorScheme.background
    val actionColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val surfaceColor = MaterialTheme.colorScheme.surface

    BackHandler {
        if (!viewModel.retrocederPaso()) onNavigateBack()
    }

    LaunchedEffect(uiState) {
        if (uiState is AddProductoState.Success) {
            onNavigateBack()
        }
    }

    val tituloTopBar = when (step) {
        AddStep.CATEGORIAS -> "Elegir Categoría"
        AddStep.PRODUCTOS -> categoria
        AddStep.DETALLE -> "Detalles"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tituloTopBar, fontWeight = FontWeight.Black, color = onSurfaceColor) },
                navigationIcon = {
                    IconButton(onClick = { if (!viewModel.retrocederPaso()) onNavigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = onSurfaceColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        },
        containerColor = backgroundColor
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {

            if (uiState is AddProductoState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = onSurfaceColor)
            } else {
                when (step) {
                    AddStep.CATEGORIAS -> {
                        // NUEVA LÓGICA DE UX: Mostramos un loader si aún no hay categorías
                        if (categorias.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(48.dp),
                                        color = onSurfaceColor,
                                        strokeWidth = 4.dp
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Cargando categorías...",
                                        fontWeight = FontWeight.Bold,
                                        color = onSurfaceColor.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(categorias) { cat ->
                                    SeleccionCard(
                                        texto = cat,
                                        colorFondo = MaterialTheme.colorScheme.secondary,
                                        textColor = MaterialTheme.colorScheme.onSecondary
                                    ) {
                                        viewModel.seleccionarCategoria(cat)
                                    }
                                }
                            }
                        }
                    }

                    AddStep.PRODUCTOS -> {
                        val productos = remember(catalogo, categoria) {
                            catalogo.filter { it.categoria == categoria }.sortedBy { it.nombre }
                        }
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(productos) { prod ->
                                SeleccionCard(
                                    texto = prod.nombre,
                                    colorFondo = surfaceColor,
                                    textColor = onSurfaceColor
                                ) {
                                    viewModel.seleccionarProducto(prod)
                                }
                            }
                        }
                    }

                    AddStep.DETALLE -> {
                        var formato by remember { mutableStateOf(producto?.formato ?: "") }
                        var cantidadActual by remember { mutableIntStateOf(1) }

                        Column(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp, end = 6.dp).neoBrutalism(cornerRadius = 16.dp, shadowOffset = 6.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor),
                                color = surfaceColor,
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Text("Producto Seleccionado", style = MaterialTheme.typography.labelLarge, color = onSurfaceColor.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                                    Text(producto?.nombre ?: "", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = onSurfaceColor)
                                    HorizontalDivider(color = onSurfaceColor, thickness = 2.dp)

                                    OutlinedTextField(
                                        value = formato,
                                        onValueChange = { formato = it },
                                        label = { Text("Formato (Kg, Cajas, Litros...)", fontWeight = FontWeight.Bold) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = onSurfaceColor,
                                            unfocusedBorderColor = onSurfaceColor.copy(alpha = 0.5f),
                                            focusedTextColor = onSurfaceColor
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("¿Cuántas unidades añades ahora?", fontWeight = FontWeight.Bold, color = onSurfaceColor)

                                    // Stepper
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = { if (cantidadActual > 1) cantidadActual-- },
                                            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)).size(48.dp).border(2.dp, onSurfaceColor, RoundedCornerShape(8.dp))
                                        ) {
                                            Icon(Icons.Default.Remove, "Quitar", tint = onSurfaceColor)
                                        }

                                        Text(
                                            text = "$cantidadActual",
                                            modifier = Modifier.padding(horizontal = 32.dp),
                                            style = MaterialTheme.typography.displaySmall,
                                            fontWeight = FontWeight.Black,
                                            color = onSurfaceColor
                                        )

                                        IconButton(
                                            onClick = { cantidadActual++ },
                                            modifier = Modifier.background(actionColor, RoundedCornerShape(8.dp)).size(48.dp).border(2.dp, onSurfaceColor, RoundedCornerShape(8.dp))
                                        ) {
                                            Icon(Icons.Default.Add, "Añadir", tint = MaterialTheme.colorScheme.onPrimary)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            Button(
                                // Pasamos el formato y la cantidad al ViewModel
                                onClick = { viewModel.guardarProducto(formato, cantidadActual) },
                                modifier = Modifier.fillMaxWidth().neoBrutalism(cornerRadius = 12.dp, shadowOffset = 6.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor),
                                colors = ButtonDefaults.buttonColors(containerColor = actionColor, contentColor = MaterialTheme.colorScheme.onPrimary),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(16.dp)
                            ) {
                                Text("Añadir a Despensa", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SeleccionCard(texto: String, colorFondo: Color, textColor: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() }
            .padding(bottom = 6.dp, end = 6.dp)
            .neoBrutalism(cornerRadius = 16.dp, shadowOffset = 6.dp, borderColor = MaterialTheme.colorScheme.onBackground, shadowColor = MaterialTheme.colorScheme.onBackground),
        color = colorFondo,
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(16.dp)) {
            Text(
                text = texto,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleMedium,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}