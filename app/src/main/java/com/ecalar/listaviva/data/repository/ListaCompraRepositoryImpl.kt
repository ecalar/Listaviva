package com.ecalar.listaviva.data.repository

import com.ecalar.listaviva.domain.model.ItemLista
import com.ecalar.listaviva.domain.model.ListaCompra
import com.ecalar.listaviva.domain.repository.ListaCompraRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ListaCompraRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ListaCompraRepository {

    private fun getListasRef(familiaId: String) =
        firestore.collection("familias").document(familiaId).collection("listasCompra")
    private fun getItemsRef(familiaId: String, listaId: String) =
        getListasRef(familiaId).document(listaId).collection("items")

    override fun getListas(familiaId: String): Flow<Result<List<ListaCompra>>> {
        return getListasRef(familiaId).orderBy("nombre").snapshots().map { snapshot ->
            try {
                val listas = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(ListaCompra::class.java)?.copy(id = doc.id)
                }
                Result.success(listas)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun crearLista(familiaId: String, lista: ListaCompra): Result<Unit> {
        return try {
            val ref = getListasRef(familiaId)
            val docRef = if (lista.id.isEmpty()) ref.document() else ref.document(lista.id)
            val listaGuardar = lista.copy(id = docRef.id)
            // USAMOS MERGE PARA EVITAR SOBRESCRITURAS ACCIDENTALES
            docRef.set(listaGuardar, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getItemsLista(familiaId: String, listaId: String): Flow<Result<List<ItemLista>>> {
        return getItemsRef(familiaId, listaId).snapshots().map { snapshot ->
            try {
                val items = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(ItemLista::class.java)?.copy(id = doc.id)
                }
                Result.success(items)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun addItemToLista(familiaId: String, listaId: String, item: ItemLista): Result<Unit> {
        return try {
            val ref = getItemsRef(familiaId, listaId)
            val docRef = if (item.id.isEmpty()) ref.document() else ref.document(item.id)
            val itemGuardar = item.copy(id = docRef.id)
            docRef.set(itemGuardar, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun marcarItemComprado(familiaId: String, listaId: String, itemId: String, comprado: Boolean): Result<Unit> {
        return try {
            getItemsRef(familiaId, listaId).document(itemId)
                .update("comprado", comprado).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteLista(familiaId: String, listaId: String): Result<Unit> {
        return try {
            val listaRef = getListasRef(familiaId).document(listaId)
            val itemsCollection = listaRef.collection("items")
            val items = itemsCollection.get().await()

            // Borrado en lote (Batch) seguro
            firestore.runBatch { batch ->
                for (item in items) {
                    batch.delete(item.reference)
                }
                batch.delete(listaRef)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateNombreLista(familiaId: String, listaId: String, nuevoNombre: String): Result<Unit> {
        return try {
            getListasRef(familiaId).document(listaId)
                .update("nombre", nuevoNombre).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCantidadItem(familiaId: String, listaId: String, itemId: String, nuevaCantidad: Int): Result<Unit> {
        return try {
            getItemsRef(familiaId, listaId).document(itemId)
                .update("cantidadAComprar", nuevaCantidad).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}