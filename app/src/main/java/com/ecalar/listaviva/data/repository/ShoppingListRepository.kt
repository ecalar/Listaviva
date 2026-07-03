package com.ecalar.listaviva.data.repository

import com.ecalar.listaviva.domain.model.ShoppingItem
import com.ecalar.listaviva.domain.model.ShoppingList
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShoppingListRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    companion object {
        private const val COLLECTION_FAMILIES = "families"
        private const val SUBCOLLECTION_LISTS = "shoppingLists"
        private const val SUBCOLLECTION_ITEMS = "items"
    }

    // Obtener todas las listas
    fun getShoppingLists(familyId: String): Flow<List<ShoppingList>> = callbackFlow {
        val subscription = firestore
            .collection(COLLECTION_FAMILIES)
            .document(familyId)
            .collection(SUBCOLLECTION_LISTS)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val lists = snapshot?.documents?.mapNotNull { doc ->
                    ShoppingList(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        color = doc.getLong("color") ?: 0xFFFF8C42,
                        createdBy = doc.getString("createdBy") ?: "",
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                    )
                } ?: emptyList()

                trySend(lists)
            }

        awaitClose { subscription.remove() }
    }

    // Crear lista
    suspend fun createList(familyId: String, name: String, color: Long, userId: String): Result<String> {
        return try {
            val data = hashMapOf(
                "name" to name,
                "color" to color,
                "createdBy" to userId,
                "createdAt" to System.currentTimeMillis()
            )

            val docRef = firestore
                .collection(COLLECTION_FAMILIES)
                .document(familyId)
                .collection(SUBCOLLECTION_LISTS)
                .add(data)
                .await()

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Actualizar nombre de lista
    suspend fun updateListName(familyId: String, listId: String, newName: String): Result<Unit> {
        return try {
            firestore
                .collection(COLLECTION_FAMILIES)
                .document(familyId)
                .collection(SUBCOLLECTION_LISTS)
                .document(listId)
                .update("name", newName)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Eliminar lista
    suspend fun deleteList(familyId: String, listId: String): Result<Unit> {
        return try {
            // Primero eliminar todos los items
            val items = firestore
                .collection(COLLECTION_FAMILIES)
                .document(familyId)
                .collection(SUBCOLLECTION_LISTS)
                .document(listId)
                .collection(SUBCOLLECTION_ITEMS)
                .get()
                .await()

            val batch = firestore.batch()
            items.documents.forEach { doc ->
                batch.delete(doc.reference)
            }

            // Eliminar la lista
            batch.delete(
                firestore
                    .collection(COLLECTION_FAMILIES)
                    .document(familyId)
                    .collection(SUBCOLLECTION_LISTS)
                    .document(listId)
            )

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener items de una lista
    fun getShoppingItems(familyId: String, listId: String): Flow<List<ShoppingItem>> = callbackFlow {
        val subscription = firestore
            .collection(COLLECTION_FAMILIES)
            .document(familyId)
            .collection(SUBCOLLECTION_LISTS)
            .document(listId)
            .collection(SUBCOLLECTION_ITEMS)
            .orderBy("addedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val items = snapshot?.documents?.mapNotNull { doc ->
                    ShoppingItem(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        pantryItemId = doc.getString("pantryItemId"),
                        quantity = doc.getString("quantity") ?: "",
                        purchased = doc.getBoolean("purchased") ?: false,
                        addedBy = doc.getString("addedBy") ?: "",
                        addedByAlias = doc.getString("addedByAlias") ?: "",
                        addedAt = doc.getLong("addedAt") ?: System.currentTimeMillis()
                    )
                } ?: emptyList()

                trySend(items)
            }

        awaitClose { subscription.remove() }
    }

    // Añadir item a la lista
    suspend fun addItem(
        familyId: String,
        listId: String,
        name: String,
        pantryItemId: String?,
        quantity: String,
        addedBy: String,
        addedByAlias: String
    ): Result<String> {
        return try {
            // Verificar si ya existe el item no comprado
            val existing = firestore
                .collection(COLLECTION_FAMILIES)
                .document(familyId)
                .collection(SUBCOLLECTION_LISTS)
                .document(listId)
                .collection(SUBCOLLECTION_ITEMS)
                .whereEqualTo("name", name)
                .whereEqualTo("purchased", false)
                .get()
                .await()

            if (!existing.isEmpty) {
                return Result.failure(Exception("Este producto ya está en la lista"))
            }

            val data = hashMapOf(
                "name" to name,
                "pantryItemId" to pantryItemId,
                "quantity" to quantity,
                "purchased" to false,
                "addedBy" to addedBy,
                "addedByAlias" to addedByAlias,
                "addedAt" to System.currentTimeMillis()
            )

            val docRef = firestore
                .collection(COLLECTION_FAMILIES)
                .document(familyId)
                .collection(SUBCOLLECTION_LISTS)
                .document(listId)
                .collection(SUBCOLLECTION_ITEMS)
                .add(data)
                .await()

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Marcar como comprado
    suspend fun markAsPurchased(familyId: String, listId: String, itemId: String): Result<Unit> {
        return try {
            firestore
                .collection(COLLECTION_FAMILIES)
                .document(familyId)
                .collection(SUBCOLLECTION_LISTS)
                .document(listId)
                .collection(SUBCOLLECTION_ITEMS)
                .document(itemId)
                .update("purchased", true)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Eliminar item
    suspend fun deleteItem(familyId: String, listId: String, itemId: String): Result<Unit> {
        return try {
            firestore
                .collection(COLLECTION_FAMILIES)
                .document(familyId)
                .collection(SUBCOLLECTION_LISTS)
                .document(listId)
                .collection(SUBCOLLECTION_ITEMS)
                .document(itemId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
