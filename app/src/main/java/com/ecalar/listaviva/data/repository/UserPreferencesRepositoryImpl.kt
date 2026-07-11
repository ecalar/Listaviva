package com.ecalar.listaviva.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.ecalar.listaviva.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// Inicializamos DataStore asociado al contexto
private val Context.dataStore by preferencesDataStore(name = "ajustes_app")

class UserPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UserPreferencesRepository {

    // --- Preferencias Síncronas (SharedPreferences) ---
    private val prefs: SharedPreferences = context.getSharedPreferences("listaviva_prefs", Context.MODE_PRIVATE)

    override fun getFamiliaId(): String? = prefs.getString("familia_id", null)

    override fun setFamiliaId(familiaId: String) {
        prefs.edit().putString("familia_id", familiaId).apply()
    }

    override fun getAlias(): String? = prefs.getString("alias_usuario", null)

    override fun setAlias(alias: String) {
        prefs.edit().putString("alias_usuario", alias).apply()
    }

    override fun clear() {
        prefs.edit().clear().apply()
    }

    override fun isFirstTime(): Boolean = prefs.getBoolean("is_first_time", true)

    override fun setFirstTimeCompleted() {
        prefs.edit().putBoolean("is_first_time", false).apply()
    }

    // --- Preferencias Reactivas (DataStore) ---

    override fun isModoOscuro(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[booleanPreferencesKey("modo_oscuro")] ?: false
        }
    }

    override suspend fun setModoOscuro(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[booleanPreferencesKey("modo_oscuro")] = enabled
        }
    }

    override fun isNotificacionesEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[booleanPreferencesKey("notificaciones_enabled")] ?: true
        }
    }

    override suspend fun setNotificacionesEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[booleanPreferencesKey("notificaciones_enabled")] = enabled
        }
    }
}