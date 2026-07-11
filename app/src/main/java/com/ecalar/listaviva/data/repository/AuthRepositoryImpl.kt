package com.ecalar.listaviva.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.ecalar.listaviva.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {

    // Escuchamos los cambios de estado de Firebase Auth en tiempo real
    override val currentUserUid: Flow<String?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser?.uid)
        }
        auth.addAuthStateListener(authStateListener)
        awaitClose {
            auth.removeAuthStateListener(authStateListener)
        }
    }

    override fun getCurrentUserUid(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    override suspend fun signInAnonymously(): Result<String> {
        return try {
            val result = auth.signInAnonymously().await()
            val uid = result.user?.uid ?: throw Exception("UID es nulo tras autenticación")
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun signOut() {
        auth.signOut()
    }
}