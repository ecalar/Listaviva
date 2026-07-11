package com.ecalar.listaviva.ui.despensa

import android.content.Context
import com.ecalar.listaviva.domain.model.ItemLista
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecalar.listaviva.domain.model.EstadoProducto
import com.ecalar.listaviva.domain.model.ListaCompra
import com.ecalar.listaviva.domain.model.ProductoDespensa
import com.ecalar.listaviva.domain.repository.CatalogoRepository
import com.ecalar.listaviva.domain.repository.DespensaRepository
import com.ecalar.listaviva.domain.repository.ListaCompraRepository
import com.ecalar.listaviva.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

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
    private val catalogoRepository: CatalogoRepository
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
        // Extraemos las categorías únicas de los productos actuales
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
            despensaRepository.updateEstadoProducto(
                familiaId,
                producto.id,
                EstadoProducto.AGOTADO.name.lowercase()
            )

            val nuevoItem = ItemLista(
                nombre = producto.nombre,
                despensaProductoId = producto.id,
                cantidad = producto.formato,
                comprado = false,
                aliasAñadidoPor = alias,
                fechaAñadido = Date()
            )
            listaCompraRepository.addItemToLista(familiaId, listaId, nuevoItem)
        }
    }

    fun comprobarEInicializarCatalogo(context: Context) {
        viewModelScope.launch {
            // Llama a la función de tu repositorio.
            // Cambia 'catalogoRepository' por el nombre de la variable que uses.
            catalogoRepository.inicializarCatalogo(context)
        }
    }
    fun eliminarProducto(productoId: String) {
        val familiaId = preferencesRepository.getFamiliaId() ?: return
        viewModelScope.launch {
            despensaRepository.deleteProducto(familiaId, productoId)
        }
    }
}