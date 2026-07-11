package com.ecalar.listaviva.ui.edit_producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    fun guardarCambios(nombre: String, categoria: String, formato: String) {
        val familiaId = preferencesRepository.getFamiliaId() ?: return
        val prod = productoActual ?: return
        _uiState.value = EditProductoState.Loading

        viewModelScope.launch {
            // Reutilizamos addProducto, que sobrescribe si el ID ya existe
            val productoActualizado = prod.copy(
                nombre = nombre,
                categoria = categoria,
                formato = formato
            )
            val result = despensaRepository.addProducto(familiaId, productoActualizado)

            result.onSuccess { _uiState.value = EditProductoState.Success }
            result.onFailure { _uiState.value = EditProductoState.Error("Error al guardar") }
        }
    }
}