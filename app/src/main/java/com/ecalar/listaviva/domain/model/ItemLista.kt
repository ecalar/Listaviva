package com.ecalar.listaviva.domain.model

import java.util.Date

data class ItemLista(
    val id: String = "",
    val nombre: String = "",
    val despensaProductoId: String? = null,
    val cantidad: String = "",
    val comprado: Boolean = false,
    val añadidoPor: String = "",
    val aliasAñadidoPor: String = "",
    val fechaAñadido: Date = Date()
)