package com.ecalar.listaviva.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUserUid: Flow<String?>
    fun getCurrentUserUid(): String?
    suspend fun signInAnonymously(): Result<String>
    fun signOut()
}