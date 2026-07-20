package com.ecalar.listaviva.domain.model

data class ProductoCatalogo(
    val id: String = "",
    val nombre: String = "",
    val categoria: String = "",
    val formato: String = "",
    val subcategoria: String = "",
    val sugeridoPor: String = "",
    val vecesUsado: Int = 0,
    val codigoBarras: String? = null
)