package com.ecalar.listaviva.ui.crear_unirse

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ecalar.listaviva.ui.scanner.QrScannerScreen
import com.ecalar.listaviva.ui.theme.neoBrutalism

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearUnirseScreen(
    onNavigateToHome: () -> Unit,
    viewModel: CrearUnirseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var alias by remember { mutableStateOf("") }
    var nombreGrupo by remember { mutableStateOf("") }
    var codigoInvitacion by remember { mutableStateOf("") }
    var showScanner by remember { mutableStateOf(false) }

    val backgroundColor = MaterialTheme.colorScheme.background
    val actionColor = MaterialTheme.colorScheme.primary
    val secondaryActionColor = MaterialTheme.colorScheme.tertiary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary

    LaunchedEffect(uiState) {
        if (uiState is CrearUnirseState.Success) {
            onNavigateToHome()
        }
    }

    if (showScanner) {
        QrScannerScreen(
            onQrScanned = { codigoScaneado ->
                codigoInvitacion = codigoScaneado
                showScanner = false
            },
            onClose = { showScanner = false }
        )
    } else {
        Scaffold(
            topBar = { CenterAlignedTopAppBar(title = { Text("Listaviva", fontWeight = FontWeight.Black, color = onSurfaceColor) }, colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = backgroundColor)) },
            containerColor = backgroundColor
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                OutlinedTextField(
                    value = alias,
                    onValueChange = { alias = it },
                    label = { Text("Tu nombre o alias", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = onSurfaceColor,
                        unfocusedBorderColor = onSurfaceColor.copy(alpha = 0.5f),
                        focusedLabelColor = onSurfaceColor,
                        focusedTextColor = onSurfaceColor,
                        unfocusedTextColor = onSurfaceColor
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, end = 8.dp).neoBrutalism(cornerRadius = 16.dp, shadowOffset = 8.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor),
                    color = surfaceColor,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Crear nuevo grupo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = onSurfaceColor)
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = nombreGrupo,
                            onValueChange = { nombreGrupo = it },
                            label = { Text("Nombre (Ej: Casa López)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = onSurfaceColor,
                                unfocusedBorderColor = onSurfaceColor.copy(alpha = 0.5f),
                                focusedTextColor = onSurfaceColor,
                                unfocusedTextColor = onSurfaceColor
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.crearGrupo(nombreGrupo, alias) },
                            modifier = Modifier.fillMaxWidth().neoBrutalism(cornerRadius = 8.dp, shadowOffset = 4.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor),
                            enabled = uiState !is CrearUnirseState.Loading,
                            colors = ButtonDefaults.buttonColors(containerColor = actionColor, contentColor = onPrimaryColor),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Crear Grupo", fontWeight = FontWeight.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("— O —", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Black, color = onSurfaceColor)
                Spacer(modifier = Modifier.height(24.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, end = 8.dp).neoBrutalism(cornerRadius = 16.dp, shadowOffset = 8.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor),
                    color = surfaceColor,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Unirse a un grupo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = onSurfaceColor)
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
                                focusedTextColor = onSurfaceColor,
                                unfocusedTextColor = onSurfaceColor
                            ),
                            trailingIcon = {
                                IconButton(onClick = { showScanner = true }) {
                                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Escanear QR", tint = onSurfaceColor)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.unirseGrupo(codigoInvitacion, alias) },
                            modifier = Modifier.fillMaxWidth().neoBrutalism(cornerRadius = 8.dp, shadowOffset = 4.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor),
                            enabled = uiState !is CrearUnirseState.Loading,
                            colors = ButtonDefaults.buttonColors(containerColor = secondaryActionColor, contentColor = MaterialTheme.colorScheme.onTertiary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Unirme", fontWeight = FontWeight.Black)
                        }
                    }
                }

                if (uiState is CrearUnirseState.Loading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(color = onSurfaceColor)
                }
                if (uiState is CrearUnirseState.Error) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = (uiState as CrearUnirseState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}