package com.ecalar.listaviva.domain.repository

import com.ecalar.listaviva.domain.model.ItemLista
import com.ecalar.listaviva.domain.model.ListaCompra
import kotlinx.coroutines.flow.Flow

interface ListaCompraRepository {
    // Operaciones para las Listas (Ej: Mercadona, LIDL)
    fun getListas(familiaId: String): Flow<Result<List<ListaCompra>>>
    suspend fun crearLista(familiaId: String, lista: ListaCompra): Result<Unit>

    // Operaciones para los Items dentro de una Lista
    fun getItemsLista(familiaId: String, listaId: String): Flow<Result<List<ItemLista>>>
    suspend fun addItemToLista(familiaId: String, listaId: String, item: ItemLista): Result<Unit>
    suspend fun marcarItemComprado(familiaId: String, listaId: String, itemId: String, comprado: Boolean): Result<Unit>

    suspend fun deleteLista(familiaId: String, listaId: String): Result<Unit>
    suspend fun updateNombreLista(familiaId: String, listaId: String, nuevoNombre: String): Result<Unit>
}