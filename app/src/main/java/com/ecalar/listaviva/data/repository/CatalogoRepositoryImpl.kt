package com.ecalar.listaviva.data.repository

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.ecalar.listaviva.domain.model.ProductoCatalogo
import com.ecalar.listaviva.domain.repository.CatalogoRepository
import com.google.gson.reflect.TypeToken
import com.google.firebase.firestore.snapshots
import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CatalogoRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : CatalogoRepository {

    suspend fun seedCatalogoSiEstaVacio(context: Context, familiaId: String) {
        try {
            // 1. Comprobar si ya hay datos
            val snapshot = firestore.collection("productosCatalogo").limit(1).get().await()
            if (!snapshot.isEmpty) return // Ya tiene datos, no hacemos nada

            // 2. Leer el archivo JSON de assets
            val jsonString = context.assets.open("catalogo.json").bufferedReader().use { it.readText() }

            // 3. Convertir a objetos
            val listType = object : TypeToken<List<Map<String, String>>>() {}.type
            val productos: List<Map<String, String>> = Gson().fromJson(jsonString, listType)

            // 4. Subir a Firestore por lotes (batches) para no superar límites
            val batch = firestore.batch()
            productos.forEach { prod ->
                val ref = firestore.collection("productosCatalogo").document()
                batch.set(ref, prod)
            }
            batch.commit().await()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun buscarProductos(query: String): Flow<List<ProductoCatalogo>> {
        return firestore.collection("productosCatalogo")
            .orderBy("nombre")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .limit(5)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.toObject(ProductoCatalogo::class.java) }
            }

    }
    override suspend fun inicializarCatalogo(context: Context) {
        try {
            // Comprobar si ya existe para no duplicar
            val snapshot = firestore.collection("productosCatalogo").limit(1).get().await()
            if (!snapshot.isEmpty) return

            val jsonString = context.assets.open("catalogo.json").bufferedReader().use { it.readText() }
            val listType = object : TypeToken<List<Map<String, String>>>() {}.type
            val productos: List<Map<String, String>> = Gson().fromJson(jsonString, listType)

            val batch = firestore.batch()
            productos.forEach { prod ->
                val ref = firestore.collection("productosCatalogo").document()
                batch.set(ref, prod)
            }
            batch.commit().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    override fun obtenerTodoElCatalogo(): Flow<List<ProductoCatalogo>> {
        return firestore.collection("productosCatalogo")
            .orderBy("categoria")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.toObject(ProductoCatalogo::class.java) }
            }
    }
}