package com.ecalar.listaviva.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ecalar.listaviva.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore(name = "ajustes_app")

class UserPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UserPreferencesRepository {

    private val prefs: SharedPreferences = context.getSharedPreferences("listaviva_prefs", Context.MODE_PRIVATE)

    private val COLOR_PRINCIPAL = stringPreferencesKey("color_principal")
    private val CONFIRMAR_BORRADO = booleanPreferencesKey("confirmar_borrado")
    private val VIBRACION_ENABLED = booleanPreferencesKey("vibracion_enabled")

    // Nuevas claves
    private val TAMANO_TEXTO = stringPreferencesKey("tamano_texto")
    private val NOTIF_CADUCIDAD = booleanPreferencesKey("notif_caducidad")
    private val NOTIF_DESPENSA = booleanPreferencesKey("notif_despensa")
    private val NOTIF_COMPRA = booleanPreferencesKey("notif_compra")
    private val NOTIF_GRUPO = booleanPreferencesKey("notif_grupo")

    override fun getFamiliaId(): String? = prefs.getString("familia_id", null)
    override fun setFamiliaId(familiaId: String) { prefs.edit().putString("familia_id", familiaId).apply() }
    override fun getAlias(): String? = prefs.getString("alias_usuario", null)
    override fun setAlias(alias: String) { prefs.edit().putString("alias_usuario", alias).apply() }
    override fun clear() { prefs.edit().clear().apply() }
    override fun isFirstTime(): Boolean = prefs.getBoolean("is_first_time", true)
    override fun setFirstTimeCompleted() { prefs.edit().putBoolean("is_first_time", false).apply() }
    override fun clearFamiliaData() { prefs.edit().remove("familia_id").remove("alias_usuario").apply() }

    override fun isModoOscuro(): Flow<Boolean> = context.dataStore.data.map { it[booleanPreferencesKey("modo_oscuro")] ?: false }
    override suspend fun setModoOscuro(enabled: Boolean) { context.dataStore.edit { it[booleanPreferencesKey("modo_oscuro")] = enabled } }

    // --- Notificaciones ---
    override fun isNotifCaducidadEnabled(): Flow<Boolean> = context.dataStore.data.map { it[NOTIF_CADUCIDAD] ?: true }
    override suspend fun setNotifCaducidadEnabled(enabled: Boolean) { context.dataStore.edit { it[NOTIF_CADUCIDAD] = enabled } }

    override fun isNotifDespensaVaciaEnabled(): Flow<Boolean> = context.dataStore.data.map { it[NOTIF_DESPENSA] ?: true }
    override suspend fun setNotifDespensaVaciaEnabled(enabled: Boolean) { context.dataStore.edit { it[NOTIF_DESPENSA] = enabled } }

    override fun isNotifCompraEnabled(): Flow<Boolean> = context.dataStore.data.map { it[NOTIF_COMPRA] ?: true }
    override suspend fun setNotifCompraEnabled(enabled: Boolean) { context.dataStore.edit { it[NOTIF_COMPRA] = enabled } }

    override fun isNotifCambiosGrupoEnabled(): Flow<Boolean> = context.dataStore.data.map { it[NOTIF_GRUPO] ?: true }
    override suspend fun setNotifCambiosGrupoEnabled(enabled: Boolean) { context.dataStore.edit { it[NOTIF_GRUPO] = enabled } }

    // --- Accesibilidad ---
    override fun getTamañoTexto(): Flow<String> = context.dataStore.data.map { it[TAMANO_TEXTO] ?: "Normal" }
    override suspend fun setTamañoTexto(tamaño: String) { context.dataStore.edit { it[TAMANO_TEXTO] = tamaño } }

    // --- Otros ---
    override fun getColorPrincipal(): Flow<String> = context.dataStore.data.map { it[COLOR_PRINCIPAL] ?: "Verde" }
    override suspend fun setColorPrincipal(color: String) { context.dataStore.edit { it[COLOR_PRINCIPAL] = color } }

    override fun isConfirmarBorrado(): Flow<Boolean> = context.dataStore.data.map { it[CONFIRMAR_BORRADO] ?: true }
    override suspend fun setConfirmarBorrado(enabled: Boolean) { context.dataStore.edit { it[CONFIRMAR_BORRADO] = enabled } }

    override fun isVibracionEnabled(): Flow<Boolean> = context.dataStore.data.map { it[VIBRACION_ENABLED] ?: true }
    override suspend fun setVibracionEnabled(enabled: Boolean) { context.dataStore.edit { it[VIBRACION_ENABLED] = enabled } }
}