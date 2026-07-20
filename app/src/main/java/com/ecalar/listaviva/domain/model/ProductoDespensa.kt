package com.ecalar.listaviva.domain.model

import java.net.URL
import java.util.Date

enum class EstadoProducto {
    COMPLETO,
    MITAD,
    CASI_AGOTADO,
    AGOTADO
}

data class ProductoDespensa(
    val imageUrl: String = "",
    val id: String = "",
    val nombre: String = "",
    val categoria: String = "",
    val subcategoria: String = "",
    val formato: String = "",
    val estado: String = "", // "completo", "mitad", "casi_agotado", "agotado"
    val cantidadActual: Int = 1,
    val cantidadReferencia: Int = 1,
    val listaAsociada: String? = null,
    val añadidoPor: String = "",
    val ultimaModificacion: Date = Date(),
    val notas: String = ""
)

