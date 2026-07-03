package com.ecalar.listaviva.ui.screens.settings

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ecalar.listaviva.ui.components.QRCodeImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToAuth: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Solo navegar si el usuario EXPLÍCITAMENTE sale del grupo
    LaunchedEffect(state.showLeaveDialog) {
        // No hacemos nada automático, el usuario debe confirmar
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Sección Perfil
                SettingsSection(title = "Perfil") {
                    ListItem(
                        headlineContent = { Text("Tu nombre en el grupo") },
                        supportingContent = { Text(state.userAlias.ifEmpty { "Sin alias" }) },
                        leadingContent = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.clickable { viewModel.showAliasDialog() }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // Sección Grupo
                SettingsSection(title = "Grupo: ${state.familyName}") {
                    ListItem(
                        headlineContent = { Text("Código de invitación") },
                        supportingContent = {
                            Column {
                                Text(
                                    text = state.familyCode,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                QRCodeImage(
                                    content = state.familyCode,
                                    size = 120.dp
                                )
                            }
                        },
                        leadingContent = {
                            Icon(
                                Icons.Default.QrCode,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )

                    ListItem(
                        headlineContent = { Text("Compartir código") },
                        supportingContent = { Text("Invita a más miembros al grupo") },
                        leadingContent = {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.clickable {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Únete a nuestra despensa en Listaviva con el código: ${state.familyCode}"
                                )
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Compartir código"))
                        }
                    )

                    if (state.isCreator) {
                        ListItem(
                            headlineContent = { Text("Regenerar código") },
                            supportingContent = { Text("El código actual dejará de funcionar") },
                            leadingContent = {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            modifier = Modifier.clickable { viewModel.showRegenerateDialog() }
                        )
                    }

                    ListItem(
                        headlineContent = { Text("Miembros del grupo") },
                        supportingContent = { Text("${state.members.size} miembros") },
                        leadingContent = {
                            Icon(
                                Icons.Default.Group,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )

                    ListItem(
                        headlineContent = {
                            Text(
                                "Salir del grupo",
                                color = MaterialTheme.colorScheme.error
                            )
                        },
                        supportingContent = { Text("Dejarás de ver la despensa compartida") },
                        leadingContent = {
                            Icon(
                                Icons.Default.ExitToApp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        modifier = Modifier.clickable { viewModel.showLeaveDialog() }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // Sección Preferencias
                SettingsSection(title = "Preferencias") {
                    ListItem(
                        headlineContent = { Text("Notificaciones") },
                        supportingContent = {
                            Text(
                                if (state.notificationsEnabled) "Activadas" else "Desactivadas"
                            )
                        },
                        leadingContent = {
                            Icon(
                                if (state.notificationsEnabled) Icons.Default.Notifications
                                else Icons.Default.NotificationsOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingContent = {
                            Switch(
                                checked = state.notificationsEnabled,
                                onCheckedChange = { viewModel.toggleNotifications() }
                            )
                        }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // Sección Acerca de
                SettingsSection(title = "Acerca de") {
                    ListItem(
                        headlineContent = { Text("Versión") },
                        supportingContent = { Text("1.0.0 (MVP)") },
                        leadingContent = {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )

                    ListItem(
                        headlineContent = { Text("Listaviva") },
                        supportingContent = { Text("Tu despensa, siempre lista") },
                        leadingContent = {
                            Icon(
                                Icons.Default.Checklist,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Diálogo cambiar alias
        if (state.showAliasDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.hideAliasDialog() },
                title = { Text("Cambiar alias") },
                text = {
                    Column {
                        Text(
                            text = "Así te verán los demás miembros del grupo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = state.newAlias,
                            onValueChange = viewModel::onNewAliasChange,
                            label = { Text("Alias") },
                            placeholder = { Text("Ej: Mamá, Juan, Pepito...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.saveAlias() },
                        enabled = state.newAlias.isNotBlank()
                    ) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.hideAliasDialog() }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // Diálogo regenerar código
        if (state.showRegenerateDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.hideRegenerateDialog() },
                title = { Text("Regenerar código") },
                text = {
                    Text("El código actual dejará de funcionar. Los miembros que no se hayan unido con el nuevo código no podrán acceder. ¿Estás seguro?")
                },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.regenerateCode() },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Regenerar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.hideRegenerateDialog() }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // Diálogo salir del grupo
        if (state.showLeaveDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.hideLeaveDialog() },
                title = { Text("Salir del grupo") },
                text = {
                    Text("Dejarás de ver la despensa compartida. ¿Estás seguro de que quieres salir?")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.leaveFamily()
                            viewModel.hideLeaveDialog()
                            onNavigateToAuth()
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Salir")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.hideLeaveDialog() }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
    }
}
