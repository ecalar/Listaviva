package com.ecalar.listaviva.data.repository

import com.ecalar.listaviva.domain.model.Family
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FamilyRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    companion object {
        private const val COLLECTION_FAMILIES = "families"
        private const val CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    }

    fun generateInviteCode(): String {
        return (1..6).map { CHARS.random() }.joinToString("")
    }

    suspend fun createFamily(name: String, createdByUid: String): Result<Family> {
        return try {
            val inviteCode = generateInviteCode()
            val family = hashMapOf(
                "name" to name,
                "inviteCode" to inviteCode,
                "createdBy" to createdByUid,
                "members" to listOf(createdByUid),
                "createdAt" to System.currentTimeMillis()
            )

            val docRef = firestore.collection(COLLECTION_FAMILIES)
                .add(family)
                .await()

            Result.success(
                Family(
                    id = docRef.id,
                    name = name,
                    inviteCode = inviteCode,
                    createdBy = createdByUid,
                    members = listOf(createdByUid)
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinFamily(inviteCode: String, uid: String): Result<Family> {
        return try {
            val snapshot = firestore.collection(COLLECTION_FAMILIES)
                .whereEqualTo("inviteCode", inviteCode)
                .get()
                .await()

            if (snapshot.isEmpty) {
                return Result.failure(Exception("Código no válido. Comprueba que sea correcto."))
            }

            val document = snapshot.documents.first()
            val familyId = document.id

            // Añadir miembro al array sin duplicar
            firestore.collection(COLLECTION_FAMILIES)
                .document(familyId)
                .update("members", com.google.firebase.firestore.FieldValue.arrayUnion(uid))
                .await()

            val updatedDoc = firestore.collection(COLLECTION_FAMILIES)
                .document(familyId)
                .get()
                .await()

            Result.success(
                Family(
                    id = familyId,
                    name = updatedDoc.getString("name") ?: "",
                    inviteCode = updatedDoc.getString("inviteCode") ?: "",
                    createdBy = updatedDoc.getString("createdBy") ?: "",
                    members = (updatedDoc.get("members") as? List<*>)?.map { it.toString() } ?: emptyList()
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFamily(familyId: String): Result<Family> {
        return try {
            val doc = firestore.collection(COLLECTION_FAMILIES)
                .document(familyId)
                .get()
                .await()

            if (!doc.exists()) {
                return Result.failure(Exception("La despensa no existe."))
            }

            Result.success(
                Family(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    inviteCode = doc.getString("inviteCode") ?: "",
                    createdBy = doc.getString("createdBy") ?: "",
                    members = (doc.get("members") as? List<*>)?.map { it.toString() } ?: emptyList()
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
