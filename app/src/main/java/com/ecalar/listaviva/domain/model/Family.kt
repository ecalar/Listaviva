package com.ecalar.listaviva.domain.model

data class Family(
    val id: String = "",
    val name: String = "",
    val inviteCode: String = "",
    val createdBy: String = "",
    val members: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
