package com.ecalar.listaviva.data.repository

import com.ecalar.listaviva.domain.model.PantryItem
import com.ecalar.listaviva.domain.model.ProductStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PantryRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    companion object {
        private const val COLLECTION_FAMILIES = "families"
        private const val SUBCOLLECTION_PANTRY = "pantry"
    }

    fun getPantryItems(familyId: String): Flow<List<PantryItem>> = callbackFlow {
        val subscription = firestore
            .collection(COLLECTION_FAMILIES)
            .document(familyId)
            .collection(SUBCOLLECTION_PANTRY)
            .orderBy("lastModified", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toPantryItem()
                } ?: emptyList()

                trySend(items)
            }

        awaitClose { subscription.remove() }
    }

    suspend fun addItem(familyId: String, item: PantryItem): Result<String> {
        return try {
            val data = hashMapOf(
                "name" to item.name,
                "category" to item.category,
                "subcategory" to item.subcategory,
                "format" to item.format,
                "status" to item.status.label,
                "notes" to item.notes,
                "addedBy" to item.addedBy,
                "lastModified" to System.currentTimeMillis()
            )

            val docRef = firestore
                .collection(COLLECTION_FAMILIES)
                .document(familyId)
                .collection(SUBCOLLECTION_PANTRY)
                .add(data)
                .await()

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateItemStatus(familyId: String, itemId: String, newStatus: ProductStatus): Result<Unit> {
        return try {
            firestore
                .collection(COLLECTION_FAMILIES)
                .document(familyId)
                .collection(SUBCOLLECTION_PANTRY)
                .document(itemId)
                .update(
                    "status", newStatus.label,
                    "lastModified", System.currentTimeMillis()
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateItem(familyId: String, item: PantryItem): Result<Unit> {
        return try {
            firestore
                .collection(COLLECTION_FAMILIES)
                .document(familyId)
                .collection(SUBCOLLECTION_PANTRY)
                .document(item.id)
                .update(
                    "name", item.name,
                    "category", item.category,
                    "subcategory", item.subcategory,
                    "format", item.format,
                    "notes", item.notes,
                    "lastModified", System.currentTimeMillis()
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteItem(familyId: String, itemId: String): Result<Unit> {
        return try {
            firestore
                .collection(COLLECTION_FAMILIES)
                .document(familyId)
                .collection(SUBCOLLECTION_PANTRY)
                .document(itemId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toPantryItem(): PantryItem? {
        return PantryItem(
            id = id,
            name = getString("name") ?: return null,
            category = getString("category") ?: "",
            subcategory = getString("subcategory") ?: "",
            format = getString("format") ?: "",
            status = ProductStatus.fromLabel(getString("status") ?: "Completo"),
            notes = getString("notes") ?: "",
            addedBy = getString("addedBy") ?: "",
            lastModified = getLong("lastModified") ?: System.currentTimeMillis()
        )
    }
}
