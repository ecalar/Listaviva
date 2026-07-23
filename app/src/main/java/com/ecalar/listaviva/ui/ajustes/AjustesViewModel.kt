package com.ecalar.listaviva.ui.ajustes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecalar.listaviva.domain.model.Familia
import com.ecalar.listaviva.domain.repository.AuthRepository
import com.ecalar.listaviva.domain.repository.FamiliaRepository
import com.ecalar.listaviva.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AjustesState {
    object Loading : AjustesState()
    data class Success(val familia: Familia, val alias: String, val currentUid: String) : AjustesState()
    data class Error(val message: String) : AjustesState()
}

@HiltViewModel
class AjustesViewModel @Inject constructor(
    private val familiaRepository: FamiliaRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AjustesState>(AjustesState.Loading)
    val uiState: StateFlow<AjustesState> = _uiState.asStateFlow()

    val modoOscuro = preferencesRepository.isModoOscuro().stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), false)
    val colorPrincipal = preferencesRepository.getColorPrincipal().stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), "Verde")
    val confirmarBorrado = preferencesRepository.isConfirmarBorrado().stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), true)
    val vibracionEnabled = preferencesRepository.isVibracionEnabled().stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), true)

    // Accesibilidad
    val tamanoTexto = preferencesRepository.getTamañoTexto().stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), "Normal")

    // Notificaciones
    val notifCaducidad = preferencesRepository.isNotifCaducidadEnabled().stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), true)
    val notifDespensaVacia = preferencesRepository.isNotifDespensaVaciaEnabled().stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), true)
    val notifCompra = preferencesRepository.isNotifCompraEnabled().stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), true)
    val notifCambiosGrupo = preferencesRepository.isNotifCambiosGrupoEnabled().stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), true)

    init { cargarAjustes() }

    private fun cargarAjustes() {
        val familiaId = preferencesRepository.getFamiliaId()
        val alias = preferencesRepository.getAlias() ?: "Usuario"

        if (familiaId == null) {
            _uiState.value = AjustesState.Error("No estás en ningún grupo")
            return
        }

        viewModelScope.launch {
            val currentUid = authRepository.getCurrentUserUid() ?: ""
            familiaRepository.getFamilia(familiaId).collect { result ->
                result.onSuccess { familia ->
                    _uiState.value = AjustesState.Success(familia, alias, currentUid)
                }.onFailure { e ->
                    _uiState.value = AjustesState.Error(e.message ?: "Error al cargar ajustes")
                }
            }
        }
    }

    fun salirDelGrupo(onLogoutComplete: () -> Unit) {
        val familiaId = preferencesRepository.getFamiliaId() ?: return
        val uidUsuario = authRepository.getCurrentUserUid() ?: ""
        viewModelScope.launch {
            familiaRepository.getFamilia(familiaId).collect { result ->
                result.onSuccess { familia ->
                    if (familia.miembros.size <= 1) familiaRepository.borrarFamiliaCompleta(familiaId)
                    else familiaRepository.quitarMiembro(familiaId, uidUsuario)
                    preferencesRepository.clearFamiliaData()
                    onLogoutComplete()
                }
            }
        }
    }

    fun expulsarMiembro(uidAExpulsar: String) {
        val familiaId = preferencesRepository.getFamiliaId() ?: return
        viewModelScope.launch { familiaRepository.quitarMiembro(familiaId, uidAExpulsar) }
    }

    fun actualizarAlias(nuevoAlias: String, onComplete: () -> Unit) {
        if (nuevoAlias.isBlank()) return
        viewModelScope.launch {
            preferencesRepository.setAlias(nuevoAlias.trim())
            val familiaId = preferencesRepository.getFamiliaId()
            val uidUsuario = authRepository.getCurrentUserUid()
            if (familiaId != null && uidUsuario != null) {
                familiaRepository.actualizarAlias(familiaId, uidUsuario, nuevoAlias.trim())
            }
            val currentState = _uiState.value
            if (currentState is AjustesState.Success) {
                _uiState.value = currentState.copy(alias = nuevoAlias.trim())
            }
            onComplete()
        }
    }

    // Setters
    fun setModoOscuro(activado: Boolean) = viewModelScope.launch { preferencesRepository.setModoOscuro(activado) }
    fun setColorPrincipal(color: String) = viewModelScope.launch { preferencesRepository.setColorPrincipal(color) }
    fun setConfirmarBorrado(activado: Boolean) = viewModelScope.launch { preferencesRepository.setConfirmarBorrado(activado) }
    fun setVibracionEnabled(activado: Boolean) = viewModelScope.launch { preferencesRepository.setVibracionEnabled(activado) }
    fun setTamanoTexto(tamano: String) = viewModelScope.launch { preferencesRepository.setTamañoTexto(tamano) }

    fun setNotifCaducidad(activado: Boolean) = viewModelScope.launch { preferencesRepository.setNotifCaducidadEnabled(activado) }
    fun setNotifDespensaVacia(activado: Boolean) = viewModelScope.launch { preferencesRepository.setNotifDespensaVaciaEnabled(activado) }
    fun setNotifCompra(activado: Boolean) = viewModelScope.launch { preferencesRepository.setNotifCompraEnabled(activado) }
    fun setNotifCambiosGrupo(activado: Boolean) = viewModelScope.launch { preferencesRepository.setNotifCambiosGrupoEnabled(activado) }
}