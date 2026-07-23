package com.ecalar.listaviva.ui.listas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecalar.listaviva.domain.model.ItemLista
import com.ecalar.listaviva.domain.model.ListaCompra
import com.ecalar.listaviva.domain.repository.DespensaRepository
import com.ecalar.listaviva.domain.repository.ListaCompraRepository
import com.ecalar.listaviva.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ListasState {
    object Loading : ListasState()
    data class Success(
        val listas: List<ListaCompra>,
        val listaSeleccionada: ListaCompra?,
        val itemsPendientes: List<ItemLista>,
        val itemsMarcadosOcultos: Boolean // NUEVO: Estado para saber si estamos filtrando
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

    private val _itemsSeleccionados = MutableStateFlow<Set<String>>(emptySet())
    val itemsSeleccionados: StateFlow<Set<String>> = _itemsSeleccionados.asStateFlow()

    // --- Preferencias ---
    val vibracionEnabled = preferencesRepository.isVibracionEnabled()
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), true)

    val confirmarBorrado = preferencesRepository.isConfirmarBorrado()
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), true)

    // Estado interno para el filtro
    private var ocultarComprados = false

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
                        _uiState.value = ListasState.Success(listas, null, emptyList(), ocultarComprados)
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
                    // APLICAMOS EL FILTRO AQUÍ
                    val itemsPendientes = items
                        .filter { !it.comprado }
                        .filter { item -> if (ocultarComprados) !_itemsSeleccionados.value.contains(item.id) else true }
                        .sortedByDescending { it.fechaAñadido }

                    _uiState.value = ListasState.Success(todasLasListas, lista, itemsPendientes, ocultarComprados)
                }
            }
        }
    }

    fun toggleItemSeleccionado(itemId: String) {
        val seleccionados = _itemsSeleccionados.value.toMutableSet()
        if (seleccionados.contains(itemId)) {
            seleccionados.remove(itemId)
        } else {
            seleccionados.add(itemId)
        }
        _itemsSeleccionados.value = seleccionados

        // Recargamos los items si estamos en modo "ocultar comprados" para que desaparezca visualmente
        if (ocultarComprados) {
            val currentState = _uiState.value as? ListasState.Success
            if (currentState != null && currentState.listaSeleccionada != null) {
                val familiaId = preferencesRepository.getFamiliaId() ?: return
                cargarItems(familiaId, currentState.listaSeleccionada, currentState.listas)
            }
        }
    }

    // Función para alternar el botón del ojo (Ocultar/Mostrar comprados)
    fun toggleOcultarComprados() {
        ocultarComprados = !ocultarComprados
        val currentState = _uiState.value as? ListasState.Success
        if (currentState != null && currentState.listaSeleccionada != null) {
            val familiaId = preferencesRepository.getFamiliaId() ?: return
            cargarItems(familiaId, currentState.listaSeleccionada, currentState.listas)
        }
    }

    fun confirmarCompra() {
        val familiaId = preferencesRepository.getFamiliaId() ?: return
        val listaId = (_uiState.value as? ListasState.Success)?.listaSeleccionada?.id ?: return

        // Buscamos directamente en el repositorio (por si los tenemos ocultos visualmente)
        val itemsAComprar = _itemsSeleccionados.value

        if (itemsAComprar.isEmpty()) return

        viewModelScope.launch {
            itemsAComprar.forEach { itemId ->
                listaCompraRepository.marcarItemComprado(familiaId, listaId, itemId, true)
                // Buscamos el ID original del producto despensa
                val currentState = _uiState.value as? ListasState.Success
                val item = currentState?.itemsPendientes?.find { it.id == itemId }

                if (item?.despensaProductoId != null) {
                    despensaRepository.registrarCompra(
                        familiaId,
                        item.despensaProductoId,
                        item.cantidadAComprar
                    )
                }
            }
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

            // Si el item estaba seleccionado, lo quitamos de la cuenta
            val seleccionados = _itemsSeleccionados.value.toMutableSet()
            if (seleccionados.contains(item.id)) {
                seleccionados.remove(item.id)
                _itemsSeleccionados.value = seleccionados
            }
        }
    }
}