package com.ecalar.listaviva.ui.listas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecalar.listaviva.domain.model.EstadoProducto
import com.ecalar.listaviva.domain.model.ItemLista
import com.ecalar.listaviva.domain.model.ListaCompra
import com.ecalar.listaviva.domain.repository.DespensaRepository
import com.ecalar.listaviva.domain.repository.ListaCompraRepository
import com.ecalar.listaviva.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ListasState {
    object Loading : ListasState()
    data class Success(
        val listas: List<ListaCompra>,
        val listaSeleccionada: ListaCompra?,
        val itemsPendientes: List<ItemLista>
    ) : ListasState()
    data class Error(val message: String) : ListasState()
}

@HiltViewModel
class ListasViewModel @Inject constructor(
    private val listaCompraRepository: ListaCompraRepository,
    private val despensaRepository: DespensaRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ListasState>(ListasState.Loading)
    val uiState: StateFlow<ListasState> = _uiState.asStateFlow()

    // Estado local para guardar los IDs de los productos marcados con el tick verde
    private val _itemsSeleccionados = MutableStateFlow<Set<String>>(emptySet())
    val itemsSeleccionados: StateFlow<Set<String>> = _itemsSeleccionados.asStateFlow()

    init {
        cargarListas()
    }

    private fun cargarListas() {
        val familiaId = preferencesRepository.getFamiliaId()
        if (familiaId == null) {
            _uiState.value = ListasState.Error("No se encontró el grupo familiar")
            return
        }

        viewModelScope.launch {
            listaCompraRepository.getListas(familiaId).collect { result ->
                result.onSuccess { listas ->
                    val listaActual = (_uiState.value as? ListasState.Success)?.listaSeleccionada
                        ?: listas.firstOrNull()

                    if (listaActual != null) {
                        cargarItems(familiaId, listaActual, listas)
                    } else {
                        _uiState.value = ListasState.Success(listas, null, emptyList())
                    }
                }.onFailure { e ->
                    _uiState.value = ListasState.Error(e.message ?: "Error al cargar listas")
                }
            }
        }
    }

    fun seleccionarLista(lista: ListaCompra) {
        val familiaId = preferencesRepository.getFamiliaId() ?: return
        val currentListas = (_uiState.value as? ListasState.Success)?.listas ?: return

        // Limpiamos las selecciones al cambiar de lista
        _itemsSeleccionados.value = emptySet()
        cargarItems(familiaId, lista, currentListas)
    }

    private fun cargarItems(
        familiaId: String,
        lista: ListaCompra,
        todasLasListas: List<ListaCompra>
    ) {
        viewModelScope.launch {
            listaCompraRepository.getItemsLista(familiaId, lista.id).collect { result ->
                result.onSuccess { items ->
                    val itemsPendientes =
                        items.filter { !it.comprado }.sortedByDescending { it.fechaAñadido }
                    _uiState.value = ListasState.Success(todasLasListas, lista, itemsPendientes)
                }
            }
        }
    }

    // Activa o desactiva el tick verde localmente (sin borrarlo de la lista)
    fun toggleItemSeleccionado(itemId: String) {
        val seleccionados = _itemsSeleccionados.value.toMutableSet()
        if (seleccionados.contains(itemId)) {
            seleccionados.remove(itemId)
        } else {
            seleccionados.add(itemId)
        }
        _itemsSeleccionados.value = seleccionados
    }

    // Confirma la compra enviando todos los productos marcados a la despensa
    fun confirmarCompra() {
        val familiaId = preferencesRepository.getFamiliaId() ?: return
        val listaId = (_uiState.value as? ListasState.Success)?.listaSeleccionada?.id ?: return
        val currentState = _uiState.value as? ListasState.Success ?: return

        val itemsAComprar = currentState.itemsPendientes.filter { _itemsSeleccionados.value.contains(it.id) }
        if (itemsAComprar.isEmpty()) return

        viewModelScope.launch {
            itemsAComprar.forEach { item ->
                // 1. Lo marcamos como comprado en la lista
                listaCompraRepository.marcarItemComprado(familiaId, listaId, item.id, true)

                // 2. Ejecutamos la transacción en la despensa pasándole la cantidad comprada
                if (item.despensaProductoId != null) {
                    despensaRepository.registrarCompra(
                        familiaId,
                        item.despensaProductoId,
                        item.cantidadAComprar // Aquí inyectamos el valor del stepper
                    )
                }
            }
            // Limpiamos la selección
            _itemsSeleccionados.value = emptySet()
        }
    }

    fun eliminarLista(listaId: String) {
        val familiaId = preferencesRepository.getFamiliaId() ?: return
        viewModelScope.launch {
            listaCompraRepository.deleteLista(familiaId, listaId)
        }
    }

    fun editarNombreLista(listaId: String, nuevoNombre: String) {
        val familiaId = preferencesRepository.getFamiliaId() ?: return
        viewModelScope.launch {
            listaCompraRepository.updateNombreLista(familiaId, listaId, nuevoNombre)
        }
    }

    fun crearLista(nombre: String) {
        val familiaId = preferencesRepository.getFamiliaId() ?: return
        viewModelScope.launch {
            val nuevaLista = ListaCompra(
                id = "",
                nombre = nombre
            )
            listaCompraRepository.crearLista(familiaId, nuevaLista)
        }
    }

    fun cambiarCantidadItem(item: ItemLista, incremento: Int) {
        val familiaId = preferencesRepository.getFamiliaId() ?: return
        val listaId = (_uiState.value as? ListasState.Success)?.listaSeleccionada?.id ?: return

        // No permitimos comprar menos de 1 unidad
        val nuevaCantidad = (item.cantidadAComprar + incremento).coerceAtLeast(1)

        viewModelScope.launch {
            listaCompraRepository.updateCantidadItem(familiaId, listaId, item.id, nuevaCantidad)
        }
    }

    fun eliminarItemDeLista(item: ItemLista) {
        val familiaId = preferencesRepository.getFamiliaId() ?: return
        val listaActual = uiState.value.let { if (it is ListasState.Success) it.listaSeleccionada else null } ?: return

        viewModelScope.launch {
            listaCompraRepository.eliminarItemDeLista(familiaId, listaActual.id, item.id)
        }
    }
}