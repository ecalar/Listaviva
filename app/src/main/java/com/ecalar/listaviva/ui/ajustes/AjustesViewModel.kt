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
    data class Success(val familia: Familia, val alias: String) : AjustesState()
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
    // Estados observables para las preferencias
    val modoOscuro = preferencesRepository.isModoOscuro()
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), false)

    val notificaciones = preferencesRepository.isNotificacionesEnabled()
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), true)

    fun setModoOscuro(activado: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setModoOscuro(activado)
        }
    }

    fun setNotificaciones(activado: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setNotificacionesEnabled(activado)
        }
    }

    init {
        cargarAjustes()
    }

    private fun cargarAjustes() {
        val familiaId = preferencesRepository.getFamiliaId()
        val alias = preferencesRepository.getAlias() ?: "Usuario"

        if (familiaId == null) {
            _uiState.value = AjustesState.Error("No estás en ningún grupo")
            return
        }

        viewModelScope.launch {
            familiaRepository.getFamilia(familiaId).collect { result ->
                result.onSuccess { familia ->
                    _uiState.value = AjustesState.Success(familia, alias)
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
            // 1. Obtenemos la familia para ver los miembros
            familiaRepository.getFamilia(familiaId).collect { result ->
                result.onSuccess { familia ->
                    val esElUltimo = familia.miembros.size <= 1

                    if (esElUltimo) {
                        // BORRADO EN CASCADA: Borramos la familia y todo lo que contiene
                        // Nota: Deberías añadir una función 'borrarFamiliaCompleta' en FamiliaRepository
                        familiaRepository.borrarFamiliaCompleta(familiaId)
                    } else {
                        // Solo nos quitamos de la lista
                        familiaRepository.quitarMiembro(familiaId, uidUsuario)
                    }

                    preferencesRepository.clear()
                    onLogoutComplete()
                }
            }
        }
    }
}