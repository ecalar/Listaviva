package com.ecalar.listaviva.ui.add_producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecalar.listaviva.domain.model.EstadoProducto
import com.ecalar.listaviva.domain.model.ProductoCatalogo
import com.ecalar.listaviva.domain.model.ProductoDespensa
import com.ecalar.listaviva.domain.repository.CatalogoRepository
import com.ecalar.listaviva.domain.repository.DespensaRepository
import com.ecalar.listaviva.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.google.firebase.firestore.FirebaseFirestore

enum class AddStep { CATEGORIAS, PRODUCTOS, DETALLE }

sealed class AddProductoState {
    object Idle : AddProductoState()
    object Loading : AddProductoState()
    object Success : AddProductoState()
    data class Error(val message: String) : AddProductoState()
}

@HiltViewModel
class AddProductoViewModel @Inject constructor(
    private val despensaRepository: DespensaRepository,
    private val catalogoRepository: CatalogoRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddProductoState>(AddProductoState.Idle)
    val uiState: StateFlow<AddProductoState> = _uiState.asStateFlow()

    private val _catalogoCompleto = MutableStateFlow<List<ProductoCatalogo>>(emptyList())
    // Exponemos el catálogo para que la pantalla pueda "escucharlo"
    val catalogoCompleto: StateFlow<List<ProductoCatalogo>> = _catalogoCompleto.asStateFlow()

    // Estados para la navegación del wizard
    val currentStep = MutableStateFlow(AddStep.CATEGORIAS)
    val categoriaSeleccionada = MutableStateFlow("")
    val productoSeleccionado = MutableStateFlow<ProductoCatalogo?>(null)

    init {
        cargarCatalogo()
    }

    private fun cargarCatalogo() {
        viewModelScope.launch {
            catalogoRepository.obtenerTodoElCatalogo().collect { productos ->
                _catalogoCompleto.value = productos
            }
        }
    }

    // Obtenemos solo los nombres de las categorías únicas
    fun getCategoriasUnicas(): List<String> {
        return _catalogoCompleto.value.map { it.categoria }.distinct().sorted()
    }

    // Obtenemos los productos de una categoría concreta
    fun getProductosPorCategoria(categoria: String): List<ProductoCatalogo> {
        return _catalogoCompleto.value.filter { it.categoria == categoria }.sortedBy { it.nombre }
    }

    fun seleccionarCategoria(categoria: String) {
        categoriaSeleccionada.value = categoria
        currentStep.value = AddStep.PRODUCTOS
    }

    fun seleccionarProducto(producto: ProductoCatalogo) {
        productoSeleccionado.value = producto
        currentStep.value = AddStep.DETALLE
    }

    fun retrocederPaso(): Boolean {
        return when (currentStep.value) {
            AddStep.DETALLE -> {
                currentStep.value = AddStep.PRODUCTOS
                true
            }
            AddStep.PRODUCTOS -> {
                currentStep.value = AddStep.CATEGORIAS
                true
            }
            AddStep.CATEGORIAS -> false
        }
    }

    fun guardarProducto(formato: String, cantidadActual: Int, codigoBarras: String? = null) {
        val familiaId = preferencesRepository.getFamiliaId() ?: return
        val prod = productoSeleccionado.value ?: return

        viewModelScope.launch {
            // 1. Si hay código de barras, lo registramos en el catálogo global (Firebase)
            if (!codigoBarras.isNullOrBlank()) {
                val nuevoCatalogo = ProductoCatalogo(
                    id = codigoBarras,
                    nombre = prod.nombre,
                    categoria = categoriaSeleccionada.value,
                    codigoBarras = codigoBarras
                )
                // Ahora firestore ya es una variable conocida y no dará error
                firestore.collection("productosCatalogo").document(codigoBarras).set(nuevoCatalogo)
            }

            // 2. Guardamos el producto en la despensa
            val nuevoProducto = ProductoDespensa(
                nombre = prod.nombre,
                categoria = categoriaSeleccionada.value,
                formato = formato,
                cantidadActual = cantidadActual,
                cantidadReferencia = cantidadActual,
                estado = EstadoProducto.COMPLETO.name.lowercase(),
                imageUrl = prod.imageUrl
            )
            despensaRepository.addProducto(familiaId, nuevoProducto)
            _uiState.value = AddProductoState.Success
        }
    }
}