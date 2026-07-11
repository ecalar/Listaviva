package com.ecalar.listaviva.domain.model

import java.util.Date

enum class EstadoProducto {
    COMPLETO,
    MITAD,
    CASI_AGOTADO,
    AGOTADO
}

data class ProductoDespensa(
    val id: String = "",
    val nombre: String = "",
    val categoria: String = "",
    val subcategoria: String = "",
    val formato: String = "",
    val estado: String = EstadoProducto.COMPLETO.name.lowercase(), // "completo", "mitad", "casi_agotado", "agotado"
    val listaAsociada: String? = null,
    val añadidoPor: String = "",
    val ultimaModificacion: Date = Date(),
    val notas: String = ""
)

