package com.ecalar.listaviva.domain.repository

import com.ecalar.listaviva.domain.model.Familia
import kotlinx.coroutines.flow.Flow

interface FamiliaRepository {
    suspend fun crearFamilia(nombre: String, uidCreador: String): Result<Familia>
    suspend fun unirseFamilia(codigo: String, uidUsuario: String): Result<Familia>
    fun getFamilia(familiaId: String): Flow<Result<Familia>>
    suspend fun actualizarMiembros(familiaId: String, nuevosMiembros: List<String>): Result<Unit>
    suspend fun borrarFamiliaCompleta(familiaId: String): Result<Unit>
    suspend fun quitarMiembro(familiaId: String, uidUsuario: String): Result<Unit>
}