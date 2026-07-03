package com.ecalar.listaviva.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecalar.listaviva.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SplashState {
    object Loading : SplashState()
    object Authenticated : SplashState()
    object NotAuthenticated : SplashState()
    data class Error(val message: String) : SplashState()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<SplashState>(SplashState.Loading)
    val state: StateFlow<SplashState> = _state.asStateFlow()

    init {
        checkAuth()
    }

    private fun checkAuth() {
        viewModelScope.launch {
            try {
                if (authRepository.isUserLoggedIn()) {
                    _state.value = SplashState.Authenticated
                } else {
                    authRepository.signInAnonymously()
                        .onSuccess {
                            _state.value = SplashState.Authenticated
                        }
                        .onFailure { e ->
                            _state.value = SplashState.Error(
                                e.localizedMessage ?: "Error de autenticación"
                            )
                        }
                }
            } catch (e: Exception) {
                _state.value = SplashState.Error(
                    e.localizedMessage ?: "Error inesperado"
                )
            }
        }
    }
}
