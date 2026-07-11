package com.ecalar.listaviva.domain.model

import java.util.Date

data class Familia(
    val id: String = "",
    val nombre: String = "",
    val codigoInvitacion: String = "",
    val creadoPor: String = "",
    val miembros: List<String> = emptyList(),
    val creadoEn: Date = Date()
)