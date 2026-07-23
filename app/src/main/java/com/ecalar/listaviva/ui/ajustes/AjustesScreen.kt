package com.ecalar.listaviva.ui.ajustes

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
    var showAliasDialog by remember { mutableStateOf(false) }
    var nuevoAliasInput by remember { mutableStateOf("") }

    var expandidoGrupo by remember { mutableStateOf(true) }
    var expandidoPreferencias by remember { mutableStateOf(false) }
    var expandidoAcercaDe by remember { mutableStateOf(false) }

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
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // --- SECCIÓN: PERFIL ---
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp, end = 6.dp)
                                .neoBrutalism(cornerRadius = 16.dp, shadowOffset = 6.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor),
                            color = MaterialTheme.colorScheme.secondary,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(24.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Tu alias en el grupo", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
                                    Text(state.alias, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSecondary, fontWeight = FontWeight.Black)
                                }

                                IconButton(onClick = {
                                    nuevoAliasInput = state.alias
                                    showAliasDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Cambiar alias", tint = MaterialTheme.colorScheme.onSecondary)
                                }
                            }
                        }

                        // --- SECCIÓN: GRUPO Y MIEMBROS ---
                        NeoAccordion(
                            title = "Grupo: ${state.familia.nombre}",
                            icon = Icons.Default.Group,
                            expanded = expandidoGrupo,
                            onHeaderClick = { expandidoGrupo = !expandidoGrupo }
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {

                                Text("Miembros del grupo", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = onSurfaceColor, modifier = Modifier.align(Alignment.Start))

                                val isAdmin = state.currentUid == state.familia.creadoPor

                                Surface(
                                    modifier = Modifier.fillMaxWidth().neoBrutalism(cornerRadius = 12.dp, borderWidth = 2.dp, shadowOffset = 2.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor),
                                    color = surfaceColor,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        state.familia.miembros.forEach { uid ->
                                            val aliasMiembro = state.familia.aliasMiembros[uid] ?: "Usuario"
                                            val isCurrentUser = uid == state.currentUid
                                            val isThisUserAdmin = uid == state.familia.creadoPor

                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = if (isThisUserAdmin) Icons.Default.Stars else Icons.Default.Person,
                                                        contentDescription = null,
                                                        tint = if (isThisUserAdmin) actionColor else onSurfaceColor
                                                    )
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Text(
                                                        text = if (isCurrentUser) "$aliasMiembro (Tú)" else aliasMiembro,
                                                        fontWeight = FontWeight.Bold,
                                                        color = onSurfaceColor
                                                    )
                                                }
                                                if (isAdmin && !isCurrentUser) {
                                                    IconButton(onClick = { viewModel.expulsarMiembro(uid) }) {
                                                        Icon(Icons.Default.PersonRemove, contentDescription = "Expulsar", tint = destructiveColor)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                HorizontalDivider(color = onSurfaceColor.copy(alpha = 0.2f), thickness = 2.dp, modifier = Modifier.padding(vertical = 4.dp))

                                Text("Código de Invitación", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = onSurfaceColor)

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
                                        letterSpacing = 4.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Button(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Código de Invitación", state.familia.codigoInvitacion)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Código copiado", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth().neoBrutalism(cornerRadius = 12.dp, shadowOffset = 4.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor),
                                    colors = ButtonDefaults.buttonColors(containerColor = surfaceColor, contentColor = onSurfaceColor),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(16.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Copiar código", fontWeight = FontWeight.Black)
                                }

                                Button(
                                    onClick = {
                                        val codigoGrupo = state.familia.codigoInvitacion
                                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, "¡Únete a mi despensa en ListaViva! El código de mi grupo es: $codigoGrupo")
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Compartir código"))
                                    },
                                    modifier = Modifier.fillMaxWidth().neoBrutalism(cornerRadius = 12.dp, shadowOffset = 4.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor),
                                    colors = ButtonDefaults.buttonColors(containerColor = surfaceColor, contentColor = onSurfaceColor),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(16.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Compartir Código", fontWeight = FontWeight.Black)
                                }

                                Button(
                                    onClick = onNavigateToCrearUnirse,
                                    modifier = Modifier.fillMaxWidth().neoBrutalism(cornerRadius = 12.dp, shadowOffset = 4.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor),
                                    colors = ButtonDefaults.buttonColors(containerColor = surfaceColor, contentColor = onSurfaceColor),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(16.dp)
                                ) {
                                    Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Unirse a otro grupo", fontWeight = FontWeight.Black)
                                }

                                HorizontalDivider(color = onSurfaceColor.copy(alpha = 0.2f), thickness = 2.dp, modifier = Modifier.padding(vertical = 8.dp))

                                Button(
                                    onClick = { showDialog = true },
                                    modifier = Modifier.fillMaxWidth().neoBrutalism(cornerRadius = 12.dp, shadowOffset = 4.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor),
                                    colors = ButtonDefaults.buttonColors(containerColor = destructiveColor, contentColor = MaterialTheme.colorScheme.onError),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(16.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Salir del grupo", fontWeight = FontWeight.Black)
                                }
                            }
                        }

                        // --- SECCIÓN: PREFERENCIAS Y PERSONALIZACIÓN ---
                        NeoAccordion(
                            title = "Personalización",
                            icon = Icons.Default.Settings,
                            expanded = expandidoPreferencias,
                            onHeaderClick = { expandidoPreferencias = !expandidoPreferencias }
                        ) {
                            val isModoOscuro by viewModel.modoOscuro.collectAsState()
                            val colorPrincipal by viewModel.colorPrincipal.collectAsState()
                            val confirmarBorrado by viewModel.confirmarBorrado.collectAsState()
                            val vibracionEnabled by viewModel.vibracionEnabled.collectAsState()
                            val tamanoTexto by viewModel.tamanoTexto.collectAsState()

                            // Estados de Notificaciones Avanzadas
                            val notifCaducidad by viewModel.notifCaducidad.collectAsState()
                            val notifDespensaVacia by viewModel.notifDespensaVacia.collectAsState()
                            val notifCompra by viewModel.notifCompra.collectAsState()
                            val notifCambiosGrupo by viewModel.notifCambiosGrupo.collectAsState()

                            val permissionLauncher = rememberLauncherForActivityResult(
                                ActivityResultContracts.RequestPermission()
                            ) { isGranted: Boolean ->
                                if (!isGranted) {
                                    Toast.makeText(context, "Permiso de notificaciones denegado", Toast.LENGTH_SHORT).show()
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                                // Tema y Color
                                Text("Tema y Color", fontWeight = FontWeight.Black, color = onSurfaceColor)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    val paleta = listOf(
                                        "Verde" to Color(0xFF4ADE80),
                                        "Azul" to Color(0xFF3B82F6),
                                        "Naranja" to Color(0xFFF97316),
                                        "Morado" to Color(0xFFA855F7)
                                    )

                                    paleta.forEach { (nombre, color) ->
                                        val isSelected = colorPrincipal == nombre
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .background(color, CircleShape)
                                                .border(
                                                    width = if (isSelected) 4.dp else 2.dp,
                                                    color = onSurfaceColor,
                                                    shape = CircleShape
                                                )
                                                .clickable { viewModel.setColorPrincipal(nombre) }
                                        )
                                    }
                                }

                                HorizontalDivider(color = onSurfaceColor.copy(alpha = 0.2f), thickness = 2.dp)

                                // --- ACCESIBILIDAD: TAMAÑO DE TEXTO ---
                                Text("Tamaño de Texto", fontWeight = FontWeight.Black, color = onSurfaceColor)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val tamanos = listOf("Pequeño", "Normal", "Grande")
                                    tamanos.forEach { tam ->
                                        val isSelected = tamanoTexto == tam
                                        Button(
                                            onClick = { viewModel.setTamanoTexto(tam) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .neoBrutalism(cornerRadius = 8.dp, shadowOffset = 2.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isSelected) actionColor else surfaceColor,
                                                contentColor = if (isSelected) onPrimaryColor else onSurfaceColor
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(tam, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                HorizontalDivider(color = onSurfaceColor.copy(alpha = 0.2f), thickness = 2.dp)

                                // Switches de Configuración General
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    NeoSwitchRow(
                                        title = "Modo Oscuro",
                                        icon = Icons.Default.DarkMode,
                                        checked = isModoOscuro,
                                        onCheckedChange = { viewModel.setModoOscuro(it) },
                                        actionColor = actionColor,
                                        surfaceColor = surfaceColor,
                                        onSurfaceColor = onSurfaceColor
                                    )

                                    NeoSwitchRow(
                                        title = "Confirmar al borrar",
                                        icon = Icons.Default.DeleteSweep,
                                        checked = confirmarBorrado,
                                        onCheckedChange = { viewModel.setConfirmarBorrado(it) },
                                        actionColor = actionColor,
                                        surfaceColor = surfaceColor,
                                        onSurfaceColor = onSurfaceColor
                                    )

                                    NeoSwitchRow(
                                        title = "Vibración al marcar",
                                        icon = Icons.Default.Vibration,
                                        checked = vibracionEnabled,
                                        onCheckedChange = { viewModel.setVibracionEnabled(it) },
                                        actionColor = actionColor,
                                        surfaceColor = surfaceColor,
                                        onSurfaceColor = onSurfaceColor
                                    )
                                }

                                HorizontalDivider(color = onSurfaceColor.copy(alpha = 0.2f), thickness = 2.dp)

                                // --- NOTIFICACIONES AVANZADAS ---
                                Text("Notificaciones", fontWeight = FontWeight.Black, color = onSurfaceColor)

                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    NeoSwitchRow(
                                        title = "Avisos de Caducidad",
                                        icon = Icons.Default.Timer,
                                        checked = notifCaducidad,
                                        onCheckedChange = {
                                            if (it && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                            }
                                            viewModel.setNotifCaducidad(it)
                                        },
                                        actionColor = actionColor,
                                        surfaceColor = surfaceColor,
                                        onSurfaceColor = onSurfaceColor
                                    )

                                    NeoSwitchRow(
                                        title = "Despensa Vacía",
                                        icon = Icons.Default.Kitchen,
                                        checked = notifDespensaVacia,
                                        onCheckedChange = { viewModel.setNotifDespensaVacia(it) },
                                        actionColor = actionColor,
                                        surfaceColor = surfaceColor,
                                        onSurfaceColor = onSurfaceColor
                                    )

                                    NeoSwitchRow(
                                        title = "Compra sin confirmar",
                                        icon = Icons.Default.ShoppingCart,
                                        checked = notifCompra,
                                        onCheckedChange = { viewModel.setNotifCompra(it) },
                                        actionColor = actionColor,
                                        surfaceColor = surfaceColor,
                                        onSurfaceColor = onSurfaceColor
                                    )

                                    NeoSwitchRow(
                                        title = "Cambios en el Grupo",
                                        icon = Icons.Default.GroupAdd,
                                        checked = notifCambiosGrupo,
                                        onCheckedChange = { viewModel.setNotifCambiosGrupo(it) },
                                        actionColor = actionColor,
                                        surfaceColor = surfaceColor,
                                        onSurfaceColor = onSurfaceColor
                                    )
                                }
                            }
                        }

                        // --- SECCIÓN: INFORMACIÓN (ACERCA DE) ---
                        NeoAccordion(
                            title = "Acerca de",
                            icon = Icons.Default.Info,
                            expanded = expandidoAcercaDe,
                            onHeaderClick = { expandidoAcercaDe = !expandidoAcercaDe }
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("ListaViva", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = onSurfaceColor)
                                Text("Versión 1.2.0 (120)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = onSurfaceColor.copy(alpha = 0.7f))

                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Desarrollado por:\nEcalar Apps", textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontWeight = FontWeight.Bold, color = onSurfaceColor)

                                HorizontalDivider(color = onSurfaceColor.copy(alpha = 0.2f), thickness = 2.dp, modifier = Modifier.padding(vertical = 4.dp))

                                val opcionesAcercaDe = listOf(
                                    "• Novedades" to { Toast.makeText(context, "Estás en la última versión", Toast.LENGTH_SHORT).show() },
                                    "• Política de privacidad" to {
                                        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://google.com"))
                                        context.startActivity(intent)
                                    },
                                    "• Términos de uso" to {
                                        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://google.com"))
                                        context.startActivity(intent)
                                    },
                                    "• Contactar con soporte" to {
                                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                                            data = android.net.Uri.parse("mailto:soporte@ecalar.com?subject=Soporte ListaViva")
                                        }
                                        try { context.startActivity(intent) } catch (e: Exception) { Toast.makeText(context, "No hay app de correo", Toast.LENGTH_SHORT).show() }
                                    },
                                    "• Valorar la aplicación" to { Toast.makeText(context, "¡Gracias por tu apoyo!", Toast.LENGTH_SHORT).show() }
                                )

                                opcionesAcercaDe.forEach { (texto, accion) ->
                                    TextButton(
                                        onClick = accion,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(texto, fontWeight = FontWeight.Bold, color = onSurfaceColor, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Start)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- DIÁLOGOS (ALIAS Y SALIR) ---
    if (showAliasDialog) {
        AlertDialog(
            onDismissRequest = { showAliasDialog = false },
            containerColor = surfaceColor,
            modifier = Modifier.neoBrutalism(cornerRadius = 16.dp, borderWidth = 2.dp, shadowOffset = 6.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor),
            title = { Text("Cambiar tu Alias", fontWeight = FontWeight.Black, color = onSurfaceColor) },
            text = {
                OutlinedTextField(
                    value = nuevoAliasInput,
                    onValueChange = { nuevoAliasInput = it },
                    label = { Text("Nuevo alias", fontWeight = FontWeight.Bold) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = onSurfaceColor,
                        unfocusedBorderColor = onSurfaceColor.copy(alpha = 0.5f),
                        focusedTextColor = onSurfaceColor
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.actualizarAlias(nuevoAliasInput) {
                            showAliasDialog = false
                            Toast.makeText(context, "Alias actualizado", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = actionColor, contentColor = onPrimaryColor),
                    modifier = Modifier.neoBrutalism(cornerRadius = 8.dp, borderWidth = 2.dp, shadowOffset = 2.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor)
                ) {
                    Text("Guardar", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAliasDialog = false }) {
                    Text("Cancelar", color = onSurfaceColor, fontWeight = FontWeight.Bold)
                }
            }
        )
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
fun NeoSwitchRow(
    title: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    actionColor: Color,
    surfaceColor: Color,
    onSurfaceColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth().neoBrutalism(cornerRadius = 12.dp, borderWidth = 2.dp, shadowOffset = 2.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor),
        color = surfaceColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = onSurfaceColor)
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = onSurfaceColor)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
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
                Text(title, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f), color = onSurfaceColor)
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