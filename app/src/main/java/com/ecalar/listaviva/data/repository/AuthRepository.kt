package com.ecalar.listaviva.data.repository

import com.ecalar.listaviva.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth
) {

    suspend fun signInAnonymously(): Result<User> {
        return try {
            val result = auth.signInAnonymously().await()
            val user = result.user!!
            Result.success(
                User(
                    uid = user.uid,
                    isAnonymous = user.isAnonymous
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUser(): User? {
        return auth.currentUser?.let {
            User(
                uid = it.uid,
                isAnonymous = it.isAnonymous
            )
        }
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    suspend fun signOut() {
        auth.signOut()
    }
}
