package com.ecalar.listaviva.ui.edit_producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecalar.listaviva.domain.model.EstadoProducto
import com.ecalar.listaviva.domain.model.ProductoDespensa
import com.ecalar.listaviva.domain.repository.DespensaRepository
import com.ecalar.listaviva.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class EditProductoState {
    object Loading : EditProductoState()
    object Success : EditProductoState()
    data class Error(val message: String) : EditProductoState()
}

@HiltViewModel
class EditProductoViewModel @Inject constructor(
    private val despensaRepository: DespensaRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<EditProductoState>(EditProductoState.Loading)
    val uiState: StateFlow<EditProductoState> = _uiState.asStateFlow()

    var productoActual: ProductoDespensa? = null

    fun cargarProducto(productoId: String) {
        val familiaId = preferencesRepository.getFamiliaId() ?: return
        viewModelScope.launch {
            despensaRepository.getProductosDespensa(familiaId).collect { result ->
                result.onSuccess { productos ->
                    productoActual = productos.find { it.id == productoId }
                    if (productoActual != null) {
                        _uiState.value = EditProductoState.Success
                    } else {
                        _uiState.value = EditProductoState.Error("Producto no encontrado")
                    }
                }.onFailure {
                    _uiState.value = EditProductoState.Error(it.message ?: "Error al cargar")
                }
            }
        }
    }

    fun guardarCambios(nombre: String, categoria: String, formato: String, cantidadActual: Int) {
        val familiaId = preferencesRepository.getFamiliaId() ?: return
        val prodId = productoActual?.id ?: return

        // Recalculamos el techo histórico
        val refAnterior = productoActual?.cantidadReferencia ?: 1
        val nuevaReferencia = if (cantidadActual > refAnterior) cantidadActual else refAnterior

        // Calculamos el porcentaje
        val porcentaje = if (nuevaReferencia > 0) (cantidadActual.toFloat() / nuevaReferencia.toFloat()) * 100 else 0f
        val nuevoEstado = when {
            cantidadActual <= 0 -> EstadoProducto.AGOTADO.name.lowercase()
            porcentaje >= 60f -> EstadoProducto.COMPLETO.name.lowercase()
            porcentaje >= 30f -> EstadoProducto.MITAD.name.lowercase()
            else -> EstadoProducto.CASI_AGOTADO.name.lowercase()
        }

        viewModelScope.launch {
            despensaRepository.updateProductoCompleto(
                familiaId, prodId, nombre, categoria, formato, cantidadActual, nuevaReferencia, nuevoEstado
            )
            _uiState.value = EditProductoState.Success
        }
    }
}