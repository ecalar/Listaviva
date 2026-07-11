package com.ecalar.listaviva.ui.ajustes

import android.Manifest
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ecalar.listaviva.ui.theme.neoBrutalism

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: AjustesViewModel = hiltViewModel(),
    onNavigateToCrearUnirse: () -> Unit,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var expandidoGrupo by remember { mutableStateOf(true) }
    var expandidoPreferencias by remember { mutableStateOf(false) }

    val backgroundColor = MaterialTheme.colorScheme.background
    val actionColor = MaterialTheme.colorScheme.primary
    val destructiveColor = MaterialTheme.colorScheme.error
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes", fontWeight = FontWeight.Black, color = onSurfaceColor) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        },
        containerColor = backgroundColor
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is AjustesState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = onSurfaceColor)
                is AjustesState.Error -> Text(text = state.message, color = destructiveColor, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
                is AjustesState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp, end = 6.dp).neoBrutalism(cornerRadius = 16.dp, shadowOffset = 6.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor),
                            color = MaterialTheme.colorScheme.secondary,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Tu alias en el grupo", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
                                    Text(state.alias, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSecondary, fontWeight = FontWeight.Black)
                                }
                            }
                        }

                        NeoAccordion(
                            title = "Grupo: ${state.familia.nombre}",
                            icon = Icons.Default.Group,
                            expanded = expandidoGrupo,
                            onHeaderClick = { expandidoGrupo = !expandidoGrupo }
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Código de Invitación", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = onSurfaceColor)
                                Spacer(modifier = Modifier.height(8.dp))

                                Surface(
                                    color = actionColor,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.neoBrutalism(cornerRadius = 12.dp, borderWidth = 2.dp, shadowOffset = 4.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor)
                                ) {
                                    Text(
                                        text = state.familia.codigoInvitacion,
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = onPrimaryColor,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                                        letterSpacing = 4.run { sp }
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedButton(
                                    onClick = {
                                        val codigoGrupo = state.familia.codigoInvitacion
                                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, "¡Únete a mi despensa en ListaViva! El código de mi grupo es: $codigoGrupo")
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Compartir código"))
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = onSurfaceColor),
                                    border = BorderStroke(width = 2.dp, color = onSurfaceColor),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Compartir Código", fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedButton(
                                    onClick = onNavigateToCrearUnirse,
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = onSurfaceColor),
                                    border = BorderStroke(width = 2.dp, color = onSurfaceColor),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Unirse a otro grupo", fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = { showDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = destructiveColor, contentColor = MaterialTheme.colorScheme.onError),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Salir del grupo", fontWeight = FontWeight.Black)
                                }
                            }
                        }

                        NeoAccordion(
                            title = "Preferencias",
                            icon = Icons.Default.Settings,
                            expanded = expandidoPreferencias,
                            onHeaderClick = { expandidoPreferencias = !expandidoPreferencias }
                        ) {
                            val isModoOscuro by viewModel.modoOscuro.collectAsState()
                            val isNotificaciones by viewModel.notificaciones.collectAsState()

                            val permissionLauncher = rememberLauncherForActivityResult(
                                ActivityResultContracts.RequestPermission()
                            ) { isGranted: Boolean ->
                                if (isGranted) {
                                    viewModel.setNotificaciones(true)
                                } else {
                                    Toast.makeText(context, "Permiso denegado", Toast.LENGTH_SHORT).show()
                                    viewModel.setNotificaciones(false)
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.DarkMode, contentDescription = null, tint = onSurfaceColor)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text("Modo Oscuro", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = onSurfaceColor)
                                    }
                                    Switch(
                                        checked = isModoOscuro,
                                        onCheckedChange = { activado -> viewModel.setModoOscuro(activado) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = actionColor,
                                            checkedTrackColor = onSurfaceColor,
                                            uncheckedThumbColor = surfaceColor,
                                            uncheckedTrackColor = Color.Gray,
                                            checkedBorderColor = onSurfaceColor,
                                            uncheckedBorderColor = onSurfaceColor
                                        )
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Notifications, contentDescription = null, tint = onSurfaceColor)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text("Notificaciones", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = onSurfaceColor)
                                    }
                                    Switch(
                                        checked = isNotificaciones,
                                        onCheckedChange = { activado ->
                                            if (activado) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                                } else {
                                                    viewModel.setNotificaciones(true)
                                                }
                                            } else {
                                                viewModel.setNotificaciones(false)
                                            }
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = actionColor,
                                            checkedTrackColor = onSurfaceColor,
                                            uncheckedThumbColor = surfaceColor,
                                            uncheckedTrackColor = Color.Gray,
                                            checkedBorderColor = onSurfaceColor,
                                            uncheckedBorderColor = onSurfaceColor
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = surfaceColor,
            modifier = Modifier.neoBrutalism(cornerRadius = 16.dp, borderWidth = 2.dp, shadowOffset = 6.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor),
            title = { Text("¿Salir del grupo?", fontWeight = FontWeight.Black, color = onSurfaceColor) },
            text = { Text("Si sales de este grupo, dejarás de ver la despensa y las listas. Necesitarás un código de invitación para volver a entrar.", fontWeight = FontWeight.Bold, color = onSurfaceColor) },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        viewModel.salirDelGrupo(onLogoutComplete = onNavigateToLogin)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = destructiveColor, contentColor = MaterialTheme.colorScheme.onError),
                    modifier = Modifier.neoBrutalism(cornerRadius = 8.dp, borderWidth = 2.dp, shadowOffset = 2.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor)
                ) {
                    Text("Salir", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar", color = onSurfaceColor, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun NeoAccordion(
    title: String,
    icon: ImageVector,
    expanded: Boolean,
    onHeaderClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp, end = 6.dp)
            .neoBrutalism(cornerRadius = 16.dp, shadowOffset = 6.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onHeaderClick() }
                    .padding(16.dp)
            ) {
                Icon(icon, contentDescription = null, tint = onSurfaceColor)
                Spacer(modifier = Modifier.width(16.dp))
                Text(title, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f), color = onSurfaceColor)
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = onSurfaceColor
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).padding(bottom = 8.dp)) {
                    HorizontalDivider(color = onSurfaceColor, thickness = 2.dp, modifier = Modifier.padding(bottom = 16.dp))
                    content()
                }
            }
        }
    }
}