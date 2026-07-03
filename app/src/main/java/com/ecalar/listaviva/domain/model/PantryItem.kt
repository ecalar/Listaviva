package com.ecalar.listaviva.domain.model

enum class ProductStatus(val label: String, val percentage: Float) {
    COMPLETO("Completo", 1.0f),
    MITAD("Mitad", 0.5f),
    CASI_AGOTADO("Casi agotado", 0.25f),
    AGOTADO("Agotado", 0.0f);

    companion object {
        fun fromLabel(label: String): ProductStatus {
            return entries.find { it.label == label } ?: COMPLETO
        }
    }
}

data class PantryItem(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val subcategory: String = "",
    val format: String = "",
    val status: ProductStatus = ProductStatus.COMPLETO,
    val notes: String = "",
    val addedBy: String = "",
    val lastModified: Long = System.currentTimeMillis()
)
