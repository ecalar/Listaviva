package com.ecalar.listaviva.ui.estadisticas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ecalar.listaviva.domain.model.EstadoProducto
import com.ecalar.listaviva.ui.theme.neoBrutalism

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticasScreen(
    viewModel: EstadisticasViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val backgroundColor = MaterialTheme.colorScheme.background
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas", fontWeight = FontWeight.Black, color = onSurfaceColor) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        },
        containerColor = backgroundColor
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is EstadisticasState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = onSurfaceColor)
                }
                is EstadisticasState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is EstadisticasState.Success -> {
                    if (state.totalProductos == 0) {
                        Text(
                            text = "No hay datos suficientes.\nAñade productos a tu despensa.",
                            fontWeight = FontWeight.Bold,
                            color = onSurfaceColor,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            ResumenTotalCard(total = state.totalProductos)
                            EstadoDespensaCard(total = state.totalProductos, porEstado = state.porEstado)
                            CategoriasCard(porCategoria = state.porCategoria)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResumenTotalCard(total: Int) {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    Surface(
        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp, end = 6.dp).neoBrutalism(cornerRadius = 16.dp, shadowOffset = 6.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor),
        color = MaterialTheme.colorScheme.secondary,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("TOTAL EN DESPENSA", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondary)
            Spacer(modifier = Modifier.height(8.dp))
            Text("$total", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSecondary)
            Text("Productos registrados", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondary)
        }
    }
}

@Composable
fun EstadoDespensaCard(total: Int, porEstado: Map<String, Int>) {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    Surface(
        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp, end = 6.dp).neoBrutalism(cornerRadius = 16.dp, shadowOffset = 6.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Estado de tu comida", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = onSurfaceColor)
            Spacer(modifier = Modifier.height(16.dp))

            val estados = listOf(
                Pair(EstadoProducto.COMPLETO.name, Color(0xFF4ADE80)),
                Pair(EstadoProducto.MITAD.name, Color(0xFFFACC15)),
                Pair(EstadoProducto.CASI_AGOTADO.name, Color(0xFFF87171)),
                Pair(EstadoProducto.AGOTADO.name, Color(0xFF9CA3AF))
            )

            estados.forEach { (nombreEstado, color) ->
                val cantidad = porEstado[nombreEstado] ?: 0
                val porcentaje = if (total > 0) cantidad.toFloat() / total.toFloat() else 0f

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = nombreEstado.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.Bold,
                        color = onSurfaceColor
                    )
                    Text(
                        text = "$cantidad",
                        fontWeight = FontWeight.Black,
                        color = onSurfaceColor,
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    Box(
                        modifier = Modifier
                            .weight(1.5f)
                            .height(20.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFFE5E7EB))
                            .border(2.dp, onSurfaceColor, RoundedCornerShape(50))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(porcentaje)
                                .background(color)
                                .border(2.dp, onSurfaceColor, RoundedCornerShape(50))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoriasCard(porCategoria: Map<String, Int>) {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    Surface(
        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp, end = 6.dp).neoBrutalism(cornerRadius = 16.dp, shadowOffset = 6.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor),
        color = MaterialTheme.colorScheme.tertiary,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Top Categorías", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onTertiary)
            Spacer(modifier = Modifier.height(16.dp))

            porCategoria.forEach { (categoria, cantidad) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(categoria, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiary)
                    Text("$cantidad uds", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onTertiary)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.onTertiary, thickness = 2.dp, modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}