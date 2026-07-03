package com.ecalar.listaviva.domain.model

data class ShoppingList(
    val id: String = "",
    val name: String = "",
    val color: Long = 0xFFFF8C42,
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class ShoppingItem(
    val id: String = "",
    val name: String = "",
    val pantryItemId: String? = null, // Referencia al producto de despensa
    val quantity: String = "",
    val purchased: Boolean = false,
    val addedBy: String = "",
    val addedByAlias: String = "",
    val addedAt: Long = System.currentTimeMillis()
)
