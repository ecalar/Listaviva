package com.ecalar.listaviva.data.repository

import com.ecalar.listaviva.domain.model.EstadoProducto
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

    // Helper unificado: TODOS los productos van aquí
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
            docRef.set(productoGuardar)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateEstadoProducto(familiaId: String, productoId: String, nuevoEstado: String): Result<Unit> {
        return try {
            getDespensaRef(familiaId).document(productoId).update("estado", nuevoEstado)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteProducto(familiaId: String, productoId: String): Result<Unit> {
        return try {
            val batch = firestore.batch()

            val prodRef = getDespensaRef(familiaId).document(productoId)
            batch.delete(prodRef)

            val listasRef = firestore.collection("familias").document(familiaId).collection("listasCompra")
            val listasSnapshot = listasRef.get().await()

            for (listaDoc in listasSnapshot.documents) {
                val itemsRef = listasRef.document(listaDoc.id).collection("items")
                val itemsQuery = itemsRef.whereEqualTo("despensaProductoId", productoId)
                val itemsSnapshot = itemsQuery.get().await()

                for (itemDoc in itemsSnapshot.documents) {
                    batch.delete(itemDoc.reference)
                }
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun actualizarStock(familiaId: String, productoId: String, cantidadActual: Int, cantidadReferencia: Int, estado: String): Result<Unit> {
        return try {
            getDespensaRef(familiaId).document(productoId)
                .update(
                    mapOf(
                        "cantidadActual" to cantidadActual,
                        "cantidadReferencia" to cantidadReferencia,
                        "estado" to estado
                    )
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun registrarCompra(familiaId: String, productoId: String, cantidadComprada: Int): Result<Unit> {
        return try {
            val docRef = getDespensaRef(familiaId).document(productoId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                if (snapshot.exists()) {
                    val actual = snapshot.getLong("cantidadActual")?.toInt() ?: 0

                    // 1. Sumamos la compra al stock actual
                    val nuevaCantidad = actual + cantidadComprada

                    // 2. Al comprar, la nueva cantidad SIEMPRE es nuestro nuevo 100%
                    val nuevaReferencia = nuevaCantidad

                    // 3. Por lo tanto, el estado al venir de la compra SIEMPRE es Completo
                    val nuevoEstado = EstadoProducto.COMPLETO.name.lowercase()

                    transaction.update(docRef, "cantidadActual", nuevaCantidad)
                    transaction.update(docRef, "cantidadReferencia", nuevaReferencia)
                    transaction.update(docRef, "estado", nuevoEstado)
                }
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProductoCompleto(
        familiaId: String,
        productoId: String,
        nombre: String,
        categoria: String,
        formato: String,
        cantidadActual: Int,
        cantidadReferencia: Int,
        estado: String
    ): Result<Unit> {
        return try {
            getDespensaRef(familiaId).document(productoId)
                .update(
                    mapOf(
                        "nombre" to nombre,
                        "categoria" to categoria,
                        "formato" to formato,
                        "cantidadActual" to cantidadActual,
                        "cantidadReferencia" to cantidadReferencia,
                        "estado" to estado
                    )
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}