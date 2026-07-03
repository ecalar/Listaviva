package com.ecalar.listaviva.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecalar.listaviva.data.local.LocalPreferences
import com.ecalar.listaviva.data.repository.AuthRepository
import com.ecalar.listaviva.data.repository.FamilyRepository
import com.ecalar.listaviva.domain.model.Family
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

data class SettingsState(
    val userAlias: String = "",
    val familyName: String = "",
    val familyCode: String = "",
    val members: List<String> = emptyList(),
    val notificationsEnabled: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showLeaveDialog: Boolean = false,
    val showRegenerateDialog: Boolean = false,
    val showAliasDialog: Boolean = false,
    val newAlias: String = "",
    val isCreator: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val localPreferences: LocalPreferences,
    private val familyRepository: FamilyRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val familyId: String
        get() = runBlocking { localPreferences.familyId.first() ?: "" }

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            // Cargar alias
            localPreferences.userAlias.first()?.let { alias ->
                _state.value = _state.value.copy(userAlias = alias)
            }

            // Cargar info de familia
            if (familyId.isNotEmpty()) {
                familyRepository.getFamily(familyId)
                    .onSuccess { family ->
                        val currentUid = authRepository.getCurrentUser()?.uid
                        _state.value = _state.value.copy(
                            familyName = family.name,
                            familyCode = family.inviteCode,
                            members = family.members,
                            isCreator = family.createdBy == currentUid,
                            isLoading = false
                        )
                    }
                    .onFailure { e ->
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = e.localizedMessage
                        )
                    }
            } else {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    fun saveAlias() {
        val alias = _state.value.newAlias.trim()
        if (alias.isEmpty()) return

        viewModelScope.launch {
            localPreferences.saveUserAlias(alias)
            _state.value = _state.value.copy(
                userAlias = alias,
                showAliasDialog = false,
                newAlias = ""
            )
        }
    }

    fun regenerateCode() {
        // Para simplificar, solo mostramos mensaje.
        // En una implementación real, se actualizaría en Firestore.
        _state.value = _state.value.copy(
            showRegenerateDialog = false,
            familyCode = FamilyRepository(
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
            ).generateInviteCode()
        )
    }

    fun leaveFamily() {
        viewModelScope.launch {
            localPreferences.clearAll()
            authRepository.signOut()
            _state.value = _state.value.copy(showLeaveDialog = false)
            // La navegación se manejará desde la UI
        }
    }

    fun toggleNotifications() {
        _state.value = _state.value.copy(
            notificationsEnabled = !_state.value.notificationsEnabled
        )
    }

    // Diálogos
    fun showLeaveDialog() { _state.value = _state.value.copy(showLeaveDialog = true) }
    fun hideLeaveDialog() { _state.value = _state.value.copy(showLeaveDialog = false) }
    fun showRegenerateDialog() { _state.value = _state.value.copy(showRegenerateDialog = true) }
    fun hideRegenerateDialog() { _state.value = _state.value.copy(showRegenerateDialog = false) }
    fun showAliasDialog() { _state.value = _state.value.copy(showAliasDialog = true, newAlias = _state.value.userAlias) }
    fun hideAliasDialog() { _state.value = _state.value.copy(showAliasDialog = false, newAlias = "") }
    fun onNewAliasChange(alias: String) { _state.value = _state.value.copy(newAlias = alias) }
    fun clearError() { _state.value = _state.value.copy(error = null) }
}
