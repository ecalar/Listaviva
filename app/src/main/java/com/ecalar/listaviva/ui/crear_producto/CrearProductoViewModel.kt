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

    // 1. SANITIZACIÓN DE TEXTO: Solo permite letras, números y espacios. Quita símbolos raros.
    fun sanitizarTexto(input: String): String {
        val regex = Regex("[^A-Za-z0-9ÁÉÍÓÚáéíóúÑñ ]")
        return regex.replace(input, "").replace(Regex(" +"), " ").trim()
    }

    // 2. CREACIÓN Y CUARENTENA
    fun guardarNuevoProducto(codigoBarras: String, nombreCrudo: String, categoria: String, formato: String) {
        val familiaId = preferencesRepository.getFamiliaId() ?: return
        val alias = preferencesRepository.getAlias() ?: "Desconocido"

        if (nombreCrudo.isBlank()) {
            _uiState.value = CrearProductoState.Error("El nombre no puede estar vacío")
            return
        }

        _uiState.value = CrearProductoState.Loading

        val nombreLimpio = sanitizarTexto(nombreCrudo)
            .replaceFirstChar { it.uppercase() }
            .take(40) // Límite de 40 caracteres

        viewModelScope.launch {
            try {
                // A) Lo guardamos en el Catálogo Global en modo "CUARENTENA"
                // Nota: Asumimos que más adelante añadirás los campos 'verificado' o 'reportes' a tu data class ProductoCatalogo
                val nuevoProductoCatalogo = ProductoCatalogo(
                    id = codigoBarras,
                    nombre = nombreLimpio,
                    categoria = categoria,
                    codigoBarras = codigoBarras,
                    imageUrl = "" // Sin imagen porque es manual
                )

                firestore.collection("catalogo_global")
                    .document(codigoBarras)
                    .set(nuevoProductoCatalogo)

                // B) Lo añadimos directamente a la Despensa del usuario
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
                _uiState.value = CrearProductoState.Error(e.message ?: "Error al guardar")
            }
        }
    }
}