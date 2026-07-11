package com.ecalar.listaviva.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.ecalar.listaviva.domain.model.Familia
import com.ecalar.listaviva.domain.repository.FamiliaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

class FamiliaRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : FamiliaRepository {

    private val familiasCollection = firestore.collection("familias")

    override suspend fun crearFamilia(nombre: String, uidCreador: String): Result<Familia> {
        return try {
            val docRef = familiasCollection.document()
            // Generador de código alfanumérico de 6 caracteres correcto
            val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
            val codigoGenerado = (1..6).map { chars.random() }.joinToString("")

            val nuevaFamilia = Familia(
                id = docRef.id,
                nombre = nombre,
                codigoInvitacion = codigoGenerado,
                creadoPor = uidCreador,
                miembros = listOf(uidCreador),
                creadoEn = Date()
            )

            docRef.set(nuevaFamilia).await()
            Result.success(nuevaFamilia)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unirseFamilia(codigo: String, uidUsuario: String): Result<Familia> {
        return try {
            val snapshot = familiasCollection
                .whereEqualTo("codigoInvitacion", codigo)
                .get()
                .await()

            if (snapshot.isEmpty) {
                return Result.failure(Exception("Código de invitación no válido"))
            }

            val doc = snapshot.documents.first()
            val familia = doc.toObject(Familia::class.java)?.copy(id = doc.id)
                ?: return Result.failure(Exception("Error al leer la familia"))

            // Añadir al usuario si no está en la lista
            if (!familia.miembros.contains(uidUsuario)) {
                val nuevosMiembros = familia.miembros + uidUsuario
                doc.reference.update("miembros", nuevosMiembros).await()
            }

            Result.success(familia)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getFamilia(familiaId: String): Flow<Result<Familia>> {
        return familiasCollection.document(familiaId).snapshots().map { snapshot ->
            try {
                val familia = snapshot.toObject(Familia::class.java)?.copy(id = snapshot.id)
                if (familia != null) {
                    Result.success(familia)
                } else {
                    Result.failure(Exception("Familia no encontrada"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun actualizarMiembros(familiaId: String, nuevosMiembros: List<String>): Result<Unit> {
        return try {
            familiasCollection.document(familiaId)
                .update("miembros", nuevosMiembros)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun quitarMiembro(familiaId: String, uidUsuario: String): Result<Unit> {
        return try {
            val docRef = familiasCollection.document(familiaId)
            val snapshot = docRef.get().await()
            val familia = snapshot.toObject(Familia::class.java)
                ?: return Result.failure(Exception("Familia no encontrada"))

            val nuevosMiembros = familia.miembros.filter { it != uidUsuario }
            actualizarMiembros(familiaId, nuevosMiembros)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun borrarFamiliaCompleta(familiaId: String): Result<Unit> {
        return try {
            val familiaRef = familiasCollection.document(familiaId)

            // 1. Obtenemos todos los documentos que queremos borrar ANTES del batch
            val productos = familiaRef.collection("productosDespensa").get().await()
            val listas = familiaRef.collection("listasCompra").get().await()

            // Para cada lista, obtenemos sus items (subcolección)
            val itemsPorLista = listas.documents.map { listaDoc ->
                listaDoc.reference.collection("items").get().await()
            }

            firestore.runBatch { batch ->
                // Borramos todos los productos de la despensa
                for (doc in productos) batch.delete(doc.reference)

                // Borramos todas las listas y sus items
                listas.documents.forEachIndexed { index, listaDoc ->
                    val items = itemsPorLista[index]
                    for (itemDoc in items) batch.delete(itemDoc.reference)
                    batch.delete(listaDoc.reference)
                }

                // 2. Finalmente borramos el documento de la familia
                batch.delete(familiaRef)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}