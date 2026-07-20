package com.ecalar.listaviva.ui.despensa

import android.content.Context
import com.ecalar.listaviva.domain.model.ItemLista
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecalar.listaviva.domain.model.EstadoProducto
import com.ecalar.listaviva.domain.model.ListaCompra
import com.ecalar.listaviva.domain.model.ProductoCatalogo
import com.ecalar.listaviva.domain.model.ProductoDespensa
import com.ecalar.listaviva.domain.repository.CatalogoRepository
import com.ecalar.listaviva.domain.repository.DespensaRepository
import com.ecalar.listaviva.domain.repository.ListaCompraRepository
import com.ecalar.listaviva.domain.repository.UserPreferencesRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

sealed class DespensaState {
    object Loading : DespensaState()
    data class Success(
        val productosTotales: List<ProductoDespensa>,
        val productosFiltrados: List<ProductoDespensa>,
        val categorias: List<String>
    ) : DespensaState()
    data class Error(val message: String) : DespensaState()
}

@HiltViewModel
class DespensaViewModel @Inject constructor(
    private val despensaRepository: DespensaRepository,
    private val listaCompraRepository: ListaCompraRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val catalogoRepository: CatalogoRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow<DespensaState>(DespensaState.Loading)
    val uiState: StateFlow<DespensaState> = _uiState.asStateFlow()

    private val _listasDisponibles = MutableStateFlow<List<ListaCompra>>(emptyList())
    val listasDisponibles: StateFlow<List<ListaCompra>> = _listasDisponibles.asStateFlow()

    private val _categoriaSeleccionada = MutableStateFlow("Todos")
    val categoriaSeleccionada: StateFlow<String> = _categoriaSeleccionada.asStateFlow()

    init {
        cargarDatos()
    }

    private fun cargarDatos() {
        val familiaId = preferencesRepository.getFamiliaId()
        if (familiaId == null) {
            _uiState.value = DespensaState.Error("No se encontró el grupo familiar")
            return
        }

        viewModelScope.launch {
            despensaRepository.getProductosDespensa(familiaId).collect { result ->
                result.onSuccess { productos ->
                    actualizarEstadoConFiltro(productos, _categoriaSeleccionada.value)
                }.onFailure { e ->
                    _uiState.value = DespensaState.Error(e.message ?: "Error al sincronizar")
                }
            }
        }

        viewModelScope.launch {
            listaCompraRepository.getListas(familiaId).collect { result ->
                result.onSuccess { listas -> _listasDisponibles.value = listas }
            }
        }
    }

    fun seleccionarCategoria(categoria: String) {
        _categoriaSeleccionada.value = categoria
        val currentState = _uiState.value
        if (currentState is DespensaState.Success) {
            actualizarEstadoConFiltro(currentState.productosTotales, categoria)
        }
    }

    private fun actualizarEstadoConFiltro(productos: List<ProductoDespensa>, categoria: String) {
        val categoriasDinamicas =
            listOf("Todos") + productos.map { it.categoria }.distinct().filter { it.isNotBlank() }
                .sorted()

        val filtrados = if (categoria == "Todos") {
            productos
        } else {
            productos.filter { it.categoria.equals(categoria, ignoreCase = true) }
        }

        _uiState.value = DespensaState.Success(
            productosTotales = productos,
            productosFiltrados = filtrados,
            categorias = categoriasDinamicas
        )
    }

    fun cambiarEstadoProducto(productoId: String, nuevoEstado: String) {
        val familiaId = preferencesRepository.getFamiliaId() ?: return
        viewModelScope.launch {
            despensaRepository.updateEstadoProducto(familiaId, productoId, nuevoEstado)
        }
    }

    fun agotarYAñadirALista(producto: ProductoDespensa, listaId: String) {
        val familiaId = preferencesRepository.getFamiliaId() ?: return
        val alias = preferencesRepository.getAlias() ?: "Desconocido"

        viewModelScope.launch {
            despensaRepository.actualizarStock(
                familiaId,
                producto.id,
                cantidadActual = 0,
                cantidadReferencia = producto.cantidadReferencia,
                estado = EstadoProducto.AGOTADO.name.lowercase()
            )

            val cantidadAComprar = 1

            val nuevoItem = ItemLista(
                nombre = producto.nombre,
                despensaProductoId = producto.id,
                cantidad = producto.formato,
                cantidadAComprar = cantidadAComprar,
                comprado = false,
                aliasAñadidoPor = alias,
                fechaAñadido = Date()
            )
            listaCompraRepository.addItemToLista(familiaId, listaId, nuevoItem)
        }
    }

    fun comprobarEInicializarCatalogo(context: Context) {
        viewModelScope.launch {
            catalogoRepository.inicializarCatalogo(context)
        }
    }

    fun eliminarProducto(productoId: String) {
        val familiaId = preferencesRepository.getFamiliaId() ?: return
        viewModelScope.launch {
            despensaRepository.deleteProducto(familiaId, productoId)
        }
    }

    private fun calcularEstado(actual: Int, referencia: Int): String {
        if (referencia <= 0 || actual <= 0) return EstadoProducto.AGOTADO.name.lowercase()
        val porcentaje = (actual.toFloat() / referencia.toFloat()) * 100

        return when {
            porcentaje >= 60f -> EstadoProducto.COMPLETO.name.lowercase()
            porcentaje >= 30f -> EstadoProducto.MITAD.name.lowercase()
            else -> EstadoProducto.CASI_AGOTADO.name.lowercase()
        }
    }

    fun cambiarCantidad(producto: ProductoDespensa, incremento: Int) {
        val familiaId = preferencesRepository.getFamiliaId() ?: return

        val nuevaCantidad = (producto.cantidadActual + incremento).coerceAtLeast(0)


        val nuevaReferencia = if (incremento > 0) {
            nuevaCantidad
        } else {
            producto.cantidadReferencia
        }

        val nuevoEstado = calcularEstado(nuevaCantidad, nuevaReferencia)

        viewModelScope.launch {
            despensaRepository.actualizarStock(
                familiaId = familiaId,
                productoId = producto.id,
                cantidadActual = nuevaCantidad,
                cantidadReferencia = nuevaReferencia,
                estado = nuevoEstado
            )
        }
    }

    fun procesarCodigoBarras(codigo: String, onProductoNoEncontrado: (String) -> Unit) {
        viewModelScope.launch {
            // 1. Buscamos en nuestro catálogo global (Crowdsourced)
            val productoEncontrado = buscarProductoEnCatalogoGlobal(codigo)

            if (productoEncontrado != null) {
                // El producto existe.
                // Lo añadimos directamente a la despensa del usuario.
                añadirProductoADespensa(productoEncontrado)
            } else {
                // NO EXISTE. Invocamos la función para que navegue a la pantalla de añadir
                // pasándole el código para que lo rellene y se guarde para todos.
                onProductoNoEncontrado(codigo)
            }
        }
    }

    private fun añadirProductoADespensa(productoCatalogo: ProductoCatalogo) {
        val familiaId = preferencesRepository.getFamiliaId() ?: return
        val alias = preferencesRepository.getAlias() ?: "Desconocido"

        // Creamos el objeto listo para tu despensa
        val nuevoProducto = ProductoDespensa(
            id = "", // Firebase le asignará un ID automáticamente al guardarlo
            nombre = productoCatalogo.nombre,
            categoria = productoCatalogo.categoria,
            formato = "1 ud",
            cantidadActual = 1,
            cantidadReferencia = 1,
            estado = EstadoProducto.COMPLETO.name.lowercase(),
            añadidoPor = alias
        )

        viewModelScope.launch {
            // Llama a la función de tu repositorio que añade un producto
            // Nota: Asegúrate de que el nombre de la función coincida con la de tu repositorio (addProducto, guardarProducto, etc.)
            despensaRepository.addProducto(familiaId, nuevoProducto)
        }
    }

    // Función auxiliar para buscar en Firestore
    private suspend fun buscarProductoEnCatalogoGlobal(codigo: String): ProductoCatalogo? {
        return try {
            val querySnapshot = firestore.collection("catalogo_global")
                .whereEqualTo("codigoBarras", codigo)
                .limit(1)
                .get()
                .await()

            if (querySnapshot.isEmpty) null
            else querySnapshot.documents.first().toObject(ProductoCatalogo::class.java)
        } catch (e: Exception) {
            null
        }
    }


}