package com.ecalar.listaviva.ui.crear_producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecalar.listaviva.domain.model.EstadoProducto
import com.ecalar.listaviva.domain.model.ProductoCatalogo
import com.ecalar.listaviva.domain.model.ProductoDespensa
import com.ecalar.listaviva.domain.repository.DespensaRepository
import com.ecalar.listaviva.domain.repository.UserPreferencesRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CrearProductoState {
    object Idle : CrearProductoState()
    object Loading : CrearProductoState()
    object Success : CrearProductoState()
    data class Error(val message: String) : CrearProductoState()
}

@HiltViewModel
class CrearProductoViewModel @Inject constructor(
    private val despensaRepository: DespensaRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow<CrearProductoState>(CrearProductoState.Idle)
    val uiState: StateFlow<CrearProductoState> = _uiState.asStateFlow()

    /**
     * Filtra caracteres no permitidos mientras el usuario escribe.
     * No hace trim() para no impedir escribir espacios.
     */
    fun sanitizarTexto(input: String): String {
        val regex = Regex("[^\\p{L}\\p{N} .,'()/%+\\-]")
        return regex.replace(input, "")
    }

    /**
     * Limpieza final antes de guardar.
     */
    private fun limpiarTextoParaGuardar(input: String): String {
        return sanitizarTexto(input)
            .replace(Regex(" {2,}"), " ") // múltiples espacios -> uno
            .trim()                       // elimina espacios al inicio y final
            .take(40)
    }

    fun guardarNuevoProducto(
        codigoBarras: String,
        nombreCrudo: String,
        categoria: String,
        formato: String
    ) {
        val familiaId = preferencesRepository.getFamiliaId() ?: return
        val alias = preferencesRepository.getAlias() ?: "Desconocido"

        if (nombreCrudo.isBlank()) {
            _uiState.value = CrearProductoState.Error("El nombre no puede estar vacío")
            return
        }

        _uiState.value = CrearProductoState.Loading

        val nombreLimpio = limpiarTextoParaGuardar(nombreCrudo)

        viewModelScope.launch {
            try {
                // Guardar en el catálogo
                val nuevoProductoCatalogo = ProductoCatalogo(
                    id = codigoBarras,
                    nombre = nombreLimpio,
                    categoria = categoria,
                    formato = formato,
                    codigoBarras = codigoBarras,
                    imageUrl = "",
                    verificado = false
                )

                firestore.collection("productosCatalogo")
                    .document(codigoBarras)
                    .set(nuevoProductoCatalogo)

                // Añadir a la despensa
                val nuevoProductoDespensa = ProductoDespensa(
                    id = "",
                    nombre = nombreLimpio,
                    categoria = categoria,
                    formato = formato.ifBlank { "1 ud" },
                    cantidadActual = 1,
                    cantidadReferencia = 1,
                    estado = EstadoProducto.COMPLETO.name.lowercase(),
                    añadidoPor = alias,
                    imageUrl = ""
                )

                despensaRepository.addProducto(familiaId, nuevoProductoDespensa)

                _uiState.value = CrearProductoState.Success

            } catch (e: Exception) {
                _uiState.value =
                    CrearProductoState.Error(e.message ?: "Error al guardar")
            }
        }
    }
}