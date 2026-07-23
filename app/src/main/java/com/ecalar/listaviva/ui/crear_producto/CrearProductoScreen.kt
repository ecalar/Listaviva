package com.ecalar.listaviva.ui.crear_producto

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ecalar.listaviva.ui.theme.neoBrutalism

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearProductoScreen(
    codigoBarras: String,
    onNavigateBack: () -> Unit,
    viewModel: CrearProductoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var nombre by remember { mutableStateOf("") }
    var formato by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("Despensa") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    // Categorías extraídas de tu JSON original
    val categoriasPrincipales = listOf(
        "Despensa", "Bebidas", "Lácteos y Huevos", "Charcutería y Quesos",
        "Carnicería", "Pescadería", "Frutería y Verdulería", "Panadería y Bollería",
        "Desayuno y Dulces", "Snacks y Aperitivos", "Congelados", "Conservas",
        "Salsas y Condimentos", "Platos Preparados", "Limpieza del Hogar", "Higiene Personal", "Otros"
    )

    val backgroundColor = MaterialTheme.colorScheme.background
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val actionColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface

    LaunchedEffect(uiState) {
        when (uiState) {
            is CrearProductoState.Success -> {
                Toast.makeText(context, "Producto guardado y añadido", Toast.LENGTH_SHORT).show()
                onNavigateBack()
            }
            is CrearProductoState.Error -> {
                Toast.makeText(context, (uiState as CrearProductoState.Error).message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Producto", fontWeight = FontWeight.Black, color = onSurfaceColor) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = onSurfaceColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        },
        containerColor = backgroundColor
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {

            if (uiState is CrearProductoState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = onSurfaceColor)
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp, end = 6.dp)
                            .neoBrutalism(cornerRadius = 16.dp, shadowOffset = 6.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor),
                        color = surfaceColor,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                            Text("Producto no encontrado en la red", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                            Text("Código escaneado: $codigoBarras", style = MaterialTheme.typography.bodyMedium, color = onSurfaceColor)

                            HorizontalDivider(color = onSurfaceColor, thickness = 2.dp)

                            OutlinedTextField(
                                value = nombre,
                                onValueChange = {
                                    nombre = viewModel.sanitizarTexto(it).take(40)
                                },
                                label = { Text("Nombre del producto", fontWeight = FontWeight.Bold) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = onSurfaceColor,
                                    unfocusedBorderColor = onSurfaceColor.copy(alpha = 0.5f),
                                    focusedTextColor = onSurfaceColor
                                )
                            )

                            // Dropdown de Categorías
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { dropdownExpanded = true },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(4.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = onSurfaceColor),
                                    border = BorderStroke(1.dp, onSurfaceColor.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(categoria, fontWeight = FontWeight.Bold)
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                }
                                DropdownMenu(
                                    expanded = dropdownExpanded,
                                    onDismissRequest = { dropdownExpanded = false },
                                    modifier = Modifier.background(surfaceColor).heightIn(max = 300.dp)
                                ) {
                                    categoriasPrincipales.forEach { cat ->
                                        DropdownMenuItem(
                                            text = { Text(cat, fontWeight = FontWeight.Bold, color = onSurfaceColor) },
                                            onClick = {
                                                categoria = cat
                                                dropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = formato,
                                onValueChange = { formato = it },
                                label = { Text("Formato (Kg, L, pack...)", fontWeight = FontWeight.Bold) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = onSurfaceColor,
                                    unfocusedBorderColor = onSurfaceColor.copy(alpha = 0.5f),
                                    focusedTextColor = onSurfaceColor
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = { viewModel.guardarNuevoProducto(codigoBarras, nombre, categoria, formato) },
                        modifier = Modifier.fillMaxWidth().neoBrutalism(cornerRadius = 12.dp, shadowOffset = 6.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor),
                        colors = ButtonDefaults.buttonColors(containerColor = actionColor, contentColor = MaterialTheme.colorScheme.onPrimary),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Text("Guardar y Añadir", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}