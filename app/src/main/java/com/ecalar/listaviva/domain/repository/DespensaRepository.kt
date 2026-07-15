package com.ecalar.listaviva.domain.repository

import com.ecalar.listaviva.domain.model.ProductoDespensa
import kotlinx.coroutines.flow.Flow

interface DespensaRepository {
    fun getProductosDespensa(familiaId: String): Flow<Result<List<ProductoDespensa>>>
    suspend fun addProducto(familiaId: String, producto: ProductoDespensa): Result<Unit>
    suspend fun updateEstadoProducto(familiaId: String, productoId: String, nuevoEstado: String): Result<Unit>
    suspend fun deleteProducto(familiaId: String, productoId: String): Result<Unit>
    suspend fun actualizarStock(familiaId: String, productoId: String, cantidadActual: Int, cantidadReferencia: Int, estado: String): Result<Unit>
    suspend fun registrarCompra(familiaId: String, productoId: String, cantidadComprada: Int): Result<Unit>
    suspend fun updateProductoCompleto(familiaId: String, productoId: String, nombre: String, categoria: String, formato: String, cantidadActual: Int, cantidadReferencia: Int, estado: String): Result<Unit>

}