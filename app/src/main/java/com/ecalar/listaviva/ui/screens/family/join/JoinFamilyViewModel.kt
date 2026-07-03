package com.ecalar.listaviva.ui.screens.family.join

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecalar.listaviva.data.local.LocalPreferences
import com.ecalar.listaviva.data.repository.AuthRepository
import com.ecalar.listaviva.data.repository.FamilyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JoinFamilyState(
    val code: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val joinedFamily: Boolean = false,
    val familyName: String = ""
)

@HiltViewModel
class JoinFamilyViewModel @Inject constructor(
    private val familyRepository: FamilyRepository,
    private val authRepository: AuthRepository,
    private val localPreferences: LocalPreferences,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(JoinFamilyState())
    val state: StateFlow<JoinFamilyState> = _state.asStateFlow()

    init {
        // Recibir código escaneado del QR Scanner
        savedStateHandle.getLiveData<String>("scanned_code").observeForever { code ->
            if (code != null && code.isNotEmpty()) {
                onCodeChange(code)
                joinFamily(code)
            }
        }
    }

    fun onCodeChange(code: String) {
        val filtered = code.uppercase().filter { it in "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" }
        if (filtered.length <= 6) {
            _state.value = _state.value.copy(code = filtered, error = null)
        }
    }

    fun joinFamily() {
        joinFamily(_state.value.code)
    }

    private fun joinFamily(code: String) {
        val trimmedCode = code.trim()
        if (trimmedCode.length != 6) {
            _state.value = _state.value.copy(error = "El código debe tener 6 caracteres")
            return
        }

        val uid = authRepository.getCurrentUser()?.uid ?: run {
            _state.value = _state.value.copy(error = "Error: usuario no autenticado")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            familyRepository.joinFamily(trimmedCode, uid)
                .onSuccess { family ->
                    localPreferences.saveFamilyInfo(family.id, family.name)
                    _state.value = _state.value.copy(
                        isLoading = false,
                        joinedFamily = true,
                        familyName = family.name
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Error al unirse a la despensa"
                    )
                }
        }
    }
}
