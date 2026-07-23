package com.ecalar.listaviva.domain.model

import java.util.Date

data class ItemLista(
    val id: String = "",
    val nombre: String = "",
    val despensaProductoId: String? = null,
    val cantidad: String = "",
    val cantidadAComprar: Int = 1,
    val comprado: Boolean = false,
    val aliasAñadidoPor: String = "",
    val fechaAñadido: java.util.Date = java.util.Date(),
    val imageUrl: String = ""
)