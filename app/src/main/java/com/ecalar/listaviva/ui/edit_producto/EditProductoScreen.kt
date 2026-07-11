package com.ecalar.listaviva.ui.edit_producto

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ecalar.listaviva.ui.theme.neoBrutalism

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductoScreen(
    productoId: String,
    onNavigateBack: () -> Unit,
    viewModel: EditProductoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var nombre by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var formato by remember { mutableStateOf("") }
    var datosCargados by remember { mutableStateOf(false) }

    val backgroundColor = MaterialTheme.colorScheme.background
    val actionColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary

    LaunchedEffect(productoId) {
        viewModel.cargarProducto(productoId)
    }

    if (uiState is EditProductoState.Success && !datosCargados) {
        viewModel.productoActual?.let {
            nombre = it.nombre
            categoria = it.categoria
            formato = it.formato
            datosCargados = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Producto", fontWeight = FontWeight.Black, color = onSurfaceColor) },
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
        if (uiState is EditProductoState.Loading && !datosCargados) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = onSurfaceColor)
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp, end = 6.dp).neoBrutalism(cornerRadius = 16.dp, shadowOffset = 6.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor),
                    color = surfaceColor,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            label = { Text("Nombre del producto*", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = onSurfaceColor,
                                unfocusedBorderColor = onSurfaceColor.copy(alpha = 0.5f),
                                focusedTextColor = onSurfaceColor,
                                unfocusedTextColor = onSurfaceColor
                            )
                        )
                        OutlinedTextField(
                            value = categoria,
                            onValueChange = { categoria = it },
                            label = { Text("Categoría (Ej: Lácteos)", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = onSurfaceColor,
                                unfocusedBorderColor = onSurfaceColor.copy(alpha = 0.5f),
                                focusedTextColor = onSurfaceColor,
                                unfocusedTextColor = onSurfaceColor
                            )
                        )
                        OutlinedTextField(
                            value = formato,
                            onValueChange = { formato = it },
                            label = { Text("Formato (Ej: 1 Litro)", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = onSurfaceColor,
                                unfocusedBorderColor = onSurfaceColor.copy(alpha = 0.5f),
                                focusedTextColor = onSurfaceColor,
                                unfocusedTextColor = onSurfaceColor
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        viewModel.guardarCambios(nombre, categoria, formato)
                        onNavigateBack()
                    },
                    modifier = Modifier.fillMaxWidth().neoBrutalism(cornerRadius = 12.dp, shadowOffset = 6.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor),
                    colors = ButtonDefaults.buttonColors(containerColor = actionColor, contentColor = onPrimaryColor),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Text("Guardar Cambios", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}