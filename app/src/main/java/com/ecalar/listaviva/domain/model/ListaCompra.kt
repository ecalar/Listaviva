package com.ecalar.listaviva.domain.model

import java.util.Date

data class ListaCompra(
    val id: String = "",
    val nombre: String = "",
    val color: String = "", // Ej: "#FF8C42"
    val icono: String = "",
    val creadoPor: String = "",
    val creadoEn: Date = Date()
)