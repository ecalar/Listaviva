package com.ecalar.listaviva.ui.screens.family.create

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

data class CreateFamilyState(
    val name: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val createdFamilyCode: String? = null,
    val createdFamilyName: String? = null
)

@HiltViewModel
class CreateFamilyViewModel @Inject constructor(
    private val familyRepository: FamilyRepository,
    private val authRepository: AuthRepository,
    private val localPreferences: LocalPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(CreateFamilyState())
    val state: StateFlow<CreateFamilyState> = _state.asStateFlow()

    fun onNameChange(name: String) {
        _state.value = _state.value.copy(name = name, error = null)
    }

    fun createFamily() {
        val name = _state.value.name.trim()
        if (name.isEmpty()) {
            _state.value = _state.value.copy(error = "El nombre no puede estar vacío")
            return
        }

        val uid = authRepository.getCurrentUser()?.uid ?: run {
            _state.value = _state.value.copy(error = "Error: usuario no autenticado")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            familyRepository.createFamily(name, uid)
                .onSuccess { family ->
                    localPreferences.saveFamilyInfo(family.id, family.name)
                    _state.value = _state.value.copy(
                        isLoading = false,
                        createdFamilyCode = family.inviteCode,
                        createdFamilyName = family.name
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Error al crear la despensa"
                    )
                }
        }
    }
}
