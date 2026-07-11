package com.ecalar.listaviva.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.ecalar.listaviva.domain.model.ProductoDespensa
import com.ecalar.listaviva.domain.repository.DespensaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class DespensaRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : DespensaRepository {

    // Helper para obtener la referencia de la subcolección de despensa
    private fun getDespensaRef(familiaId: String) =
        firestore.collection("familias").document(familiaId).collection("despensa")

    override fun getProductosDespensa(familiaId: String): Flow<Result<List<ProductoDespensa>>> {
        return getDespensaRef(familiaId).snapshots().map { snapshot ->
            try {
                val productos = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(ProductoDespensa::class.java)?.copy(id = doc.id)
                }
                Result.success(productos)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun addProducto(familiaId: String, producto: ProductoDespensa): Result<Unit> {
        return try {
            val collection = getDespensaRef(familiaId)
            val docRef = if (producto.id.isEmpty()) collection.document() else collection.document(producto.id)

            val productoGuardar = producto.copy(id = docRef.id)
            docRef.set(productoGuardar) // Mantenemos sin .await() para modo offline

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateEstadoProducto(familiaId: String, productoId: String, nuevoEstado: String): Result<Unit> {
        return try {
            getDespensaRef(familiaId).document(productoId).update("estado", nuevoEstado) // Mantenemos sin .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteProducto(familiaId: String, productoId: String): Result<Unit> {
        return try {
            // Creamos un lote para ejecutar múltiples operaciones juntas
            val batch = firestore.batch()

            // 1. Borrar de despensa
            val prodRef = firestore.collection("familias").document(familiaId)
                .collection("despensa").document(productoId)
            batch.delete(prodRef)

            // 2. Buscar en listas (Consulta asíncrona normal, permitida offline gracias a la caché de Firebase)
            val listasRef = firestore.collection("familias").document(familiaId).collection("listasCompra")
            val listasSnapshot = listasRef.get().await()

            for (listaDoc in listasSnapshot.documents) {
                val itemsRef = listasRef.document(listaDoc.id).collection("items")

                // 3. Consultar qué ítems están asociados a este producto
                val itemsQuery = itemsRef.whereEqualTo("despensaProductoId", productoId)
                val itemsSnapshot = itemsQuery.get().await()

                // 4. Añadimos el borrado de cada coincidencia al lote
                for (itemDoc in itemsSnapshot.documents) {
                    batch.delete(itemDoc.reference)
                }
            }

            // 5. Comprometemos todas las escrituras de forma atómica
            batch.commit().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}