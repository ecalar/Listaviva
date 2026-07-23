package com.ecalar.listaviva.ui.crear_unirse

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ecalar.listaviva.ui.scanner.ScannerScreen
import com.ecalar.listaviva.ui.theme.neoBrutalism

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnirseSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: CrearUnirseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var codigoInvitacion by remember { mutableStateOf("") }
    var showScanner by remember { mutableStateOf(false) }

    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val secondaryActionColor = MaterialTheme.colorScheme.tertiary

    // Si tiene éxito al unirse, cerramos la pantalla y volvemos a Ajustes
    LaunchedEffect(uiState) {
        if (uiState is CrearUnirseState.Success) {
            onNavigateBack()
        }
    }

    if (showScanner) {
        ScannerScreen(
            onNavigateBack = { showScanner = false },
            onBarcodeScanned = { codigoScaneado ->
                codigoInvitacion = codigoScaneado
                showScanner = false
            }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Cambiar de Grupo", fontWeight = FontWeight.Black, color = onSurfaceColor) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = onSurfaceColor)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth().neoBrutalism(cornerRadius = 16.dp, shadowOffset = 8.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor),
                    color = surfaceColor,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Unirse a un nuevo grupo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = onSurfaceColor)
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = codigoInvitacion,
                            onValueChange = { codigoInvitacion = it.uppercase() },
                            label = { Text("Código de 6 letras") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = onSurfaceColor,
                                unfocusedBorderColor = onSurfaceColor.copy(alpha = 0.5f),
                                focusedTextColor = onSurfaceColor
                            ),
                            trailingIcon = {
                                IconButton(onClick = { showScanner = true }) {
                                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Escanear QR", tint = onSurfaceColor)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            // Usamos el alias que el usuario ya tiene guardado
                            onClick = { viewModel.unirseGrupo(codigoInvitacion, viewModel.getAliasActual()) },
                            modifier = Modifier.fillMaxWidth().neoBrutalism(cornerRadius = 8.dp, shadowOffset = 4.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor),
                            enabled = uiState !is CrearUnirseState.Loading && codigoInvitacion.length == 6,
                            colors = ButtonDefaults.buttonColors(containerColor = secondaryActionColor, contentColor = MaterialTheme.colorScheme.onTertiary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Cambiar de Grupo", fontWeight = FontWeight.Black)
                        }
                    }
                }

                if (uiState is CrearUnirseState.Error) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = (uiState as CrearUnirseState.Error).message, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}