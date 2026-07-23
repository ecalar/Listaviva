package com.ecalar.listaviva.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    fun getFamiliaId(): String?
    fun setFamiliaId(familiaId: String)
    fun getAlias(): String?
    fun setAlias(alias: String)
    fun clear()
    fun clearFamiliaData()
    fun isFirstTime(): Boolean
    fun setFirstTimeCompleted()

    fun isModoOscuro(): Flow<Boolean>
    suspend fun setModoOscuro(enabled: Boolean)

    // --- NUEVAS NOTIFICACIONES ---
    fun isNotifCaducidadEnabled(): Flow<Boolean>
    suspend fun setNotifCaducidadEnabled(enabled: Boolean)

    fun isNotifDespensaVaciaEnabled(): Flow<Boolean>
    suspend fun setNotifDespensaVaciaEnabled(enabled: Boolean)

    fun isNotifCompraEnabled(): Flow<Boolean>
    suspend fun setNotifCompraEnabled(enabled: Boolean)

    fun isNotifCambiosGrupoEnabled(): Flow<Boolean>
    suspend fun setNotifCambiosGrupoEnabled(enabled: Boolean)

    // --- ACCESIBILIDAD ---
    fun getTamañoTexto(): Flow<String>
    suspend fun setTamañoTexto(tamaño: String)

    fun getColorPrincipal(): Flow<String>
    suspend fun setColorPrincipal(color: String)

    fun isConfirmarBorrado(): Flow<Boolean>
    suspend fun setConfirmarBorrado(enabled: Boolean)

    fun isVibracionEnabled(): Flow<Boolean>
    suspend fun setVibracionEnabled(enabled: Boolean)
}