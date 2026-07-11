package com.ecalar.listaviva.ui.estadisticas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecalar.listaviva.domain.model.EstadoProducto
import com.ecalar.listaviva.domain.repository.DespensaRepository
import com.ecalar.listaviva.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class EstadisticasState {
    object Loading : EstadisticasState()
    data class Success(
        val totalProductos: Int,
        val porEstado: Map<String, Int>,
        val porCategoria: Map<String, Int>
    ) : EstadisticasState()
    data class Error(val message: String) : EstadisticasState()
}

@HiltViewModel
class EstadisticasViewModel @Inject constructor(
    private val despensaRepository: DespensaRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<EstadisticasState>(EstadisticasState.Loading)
    val uiState: StateFlow<EstadisticasState> = _uiState.asStateFlow()

    init {
        cargarEstadisticas()
    }

    private fun cargarEstadisticas() {
        val familiaId = preferencesRepository.getFamiliaId()
        if (familiaId == null) {
            _uiState.value = EstadisticasState.Error("No se encontró el grupo familiar")
            return
        }

        viewModelScope.launch {
            despensaRepository.getProductosDespensa(familiaId).collect { result ->
                result.onSuccess { productos ->
                    val total = productos.size

                    // Agrupar por estado (Completo, Mitad, Casi Agotado, Agotado)
                    val porEstado = productos.groupingBy { it.estado.uppercase() }.eachCount()

                    // Agrupar por categoría y ordenar por los que tienen más productos
                    val porCategoria = productos.groupingBy { it.categoria }
                        .eachCount()
                        .toList()
                        .sortedByDescending { (_, value) -> value }
                        .toMap()

                    _uiState.value = EstadisticasState.Success(
                        totalProductos = total,
                        porEstado = porEstado,
                        porCategoria = porCategoria
                    )
                }.onFailure { e ->
                    _uiState.value = EstadisticasState.Error(e.message ?: "Error al cargar estadísticas")
                }
            }
        }
    }
}