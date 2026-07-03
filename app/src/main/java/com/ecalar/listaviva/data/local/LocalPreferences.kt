package com.ecalar.listaviva.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "listaviva_prefs")

@Singleton
class LocalPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_FAMILY_ID = stringPreferencesKey("family_id")
        private val KEY_USER_ALIAS = stringPreferencesKey("user_alias")
        private val KEY_FAMILY_NAME = stringPreferencesKey("family_name")
    }

    val familyId: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_FAMILY_ID]
    }

    val userAlias: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_ALIAS]
    }

    val familyName: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_FAMILY_NAME]
    }

    suspend fun saveFamilyInfo(familyId: String, familyName: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_FAMILY_ID] = familyId
            prefs[KEY_FAMILY_NAME] = familyName
        }
    }

    suspend fun saveUserAlias(alias: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_ALIAS] = alias
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
