package com.ecalar.listaviva.domain.repository

import android.content.Context
import com.ecalar.listaviva.domain.model.ProductoCatalogo
import kotlinx.coroutines.flow.Flow

interface CatalogoRepository {
    fun buscarProductos(query: String): Flow<List<ProductoCatalogo>>
    suspend fun inicializarCatalogo(context: Context)
    fun obtenerTodoElCatalogo(): Flow<List<ProductoCatalogo>>

}