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

    fun isNotificacionesEnabled(): Flow<Boolean>
    suspend fun setNotificacionesEnabled(enabled: Boolean)
}