package com.ecalar.listaviva.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecalar.listaviva.domain.repository.AuthRepository
import com.ecalar.listaviva.domain.repository.FamiliaRepository
import com.ecalar.listaviva.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Loading : AuthState()
    data class Authenticated(
        val uid: String,
        val isFirstTime: Boolean,
        val hasFamilia: Boolean
    ) : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val familiaRepository: FamiliaRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkAuthStatus()
        fun verificarIntegridadSesion() {
            val familiaId = preferencesRepository.getFamiliaId()
            if (familiaId != null) {
                viewModelScope.launch {
                    familiaRepository.getFamilia(familiaId).collect { result ->
                        result.onFailure {
                            preferencesRepository.clear()
                        }
                    }
                }
            }
        }
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            authRepository.currentUserUid.collect { uid ->
                if (uid != null) {
                    val isFirstTime = preferencesRepository.isFirstTime()
                    val hasFamilia = preferencesRepository.getFamiliaId() != null
                    _authState.value = AuthState.Authenticated(uid, isFirstTime, hasFamilia)
                } else {
                    signInAnonymously()
                }
            }
        }
    }

    private fun signInAnonymously() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.signInAnonymously()
            result.onFailure { exception ->
                _authState.value = AuthState.Error(exception.message ?: "Error al autenticar")
            }
        }
    }


}