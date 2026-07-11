package com.ecalar.listaviva.ui.crear_unirse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecalar.listaviva.domain.model.ListaCompra
import com.ecalar.listaviva.domain.repository.AuthRepository
import com.ecalar.listaviva.domain.repository.FamiliaRepository
import com.ecalar.listaviva.domain.repository.ListaCompraRepository
import com.ecalar.listaviva.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

sealed class CrearUnirseState {
    object Idle : CrearUnirseState()
    object Loading : CrearUnirseState()
    data class Success(val familiaId: String) : CrearUnirseState()
    data class Error(val message: String) : CrearUnirseState()
}

@HiltViewModel
class CrearUnirseViewModel @Inject constructor(
    private val familiaRepository: FamiliaRepository,
    private val authRepository: AuthRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val listaCompraRepository: ListaCompraRepository // Añadido para inyección
) : ViewModel() {

    private val _uiState = MutableStateFlow<CrearUnirseState>(CrearUnirseState.Idle)
    val uiState: StateFlow<CrearUnirseState> = _uiState.asStateFlow()

    fun crearGrupo(nombreGrupo: String, alias: String) {
        if (nombreGrupo.isBlank() || alias.isBlank()) {
            _uiState.value = CrearUnirseState.Error("Rellena todos los campos")
            return
        }

        viewModelScope.launch {
            _uiState.value = CrearUnirseState.Loading
            val uid = authRepository.currentUserUid.firstOrNull() ?: return@launch

            val result = familiaRepository.crearFamilia(nombreGrupo, uid)
            result.onSuccess { familia ->

                // --- INICIO AJUSTE: Autogenerar lista por defecto ---
                val listaPorDefecto = ListaCompra(
                    nombre = "Lista General",
                    color = "#FF8C42",
                    icono = "cart",
                    creadoPor = uid,
                    creadoEn = Date()
                )
                listaCompraRepository.crearLista(familia.id, listaPorDefecto)
                // --- FIN AJUSTE ---

                preferencesRepository.setFamiliaId(familia.id)
                preferencesRepository.setAlias(alias)
                _uiState.value = CrearUnirseState.Success(familia.id)
            }.onFailure { e ->
                _uiState.value = CrearUnirseState.Error(e.message ?: "Error al crear grupo")
            }
        }
    }

    fun unirseGrupo(codigo: String, alias: String) {
        if (codigo.isBlank() || alias.isBlank()) {
            _uiState.value = CrearUnirseState.Error("Rellena todos los campos")
            return
        }

        viewModelScope.launch {
            _uiState.value = CrearUnirseState.Loading
            val uid = authRepository.currentUserUid.firstOrNull() ?: return@launch

            val result = familiaRepository.unirseFamilia(codigo.uppercase(), uid)
            result.onSuccess { familia ->
                preferencesRepository.setFamiliaId(familia.id)
                preferencesRepository.setAlias(alias)
                _uiState.value = CrearUnirseState.Success(familia.id)
            }.onFailure { e ->
                _uiState.value = CrearUnirseState.Error(e.message ?: "Error al unirse al grupo")
            }
        }
    }

    fun getAliasActual(): String = preferencesRepository.getAlias() ?: "Usuario"
}