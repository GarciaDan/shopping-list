package org.danigarcia.apps.simple_shopping_list.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

data class AppSettings(
    val endpoint: String = "",
    val auth: String = "",
    val username: String = ""
) {
    val isConfigured: Boolean
        get() = endpoint.isNotBlank() && username.isNotBlank()
}

/**
 * Persistencia local de la configuración (DataStore / Preferences).
 */
class SettingsRepository(private val context: Context) {

    private val endpointKey = stringPreferencesKey("firebase_endpoint")
    private val authKey = stringPreferencesKey("firebase_auth")
    private val usernameKey = stringPreferencesKey("username")

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            endpoint = prefs[endpointKey].orEmpty(),
            auth = prefs[authKey].orEmpty(),
            username = prefs[usernameKey].orEmpty()
        )
    }

    suspend fun save(settings: AppSettings) {
        context.dataStore.edit { prefs ->
            prefs[endpointKey] = settings.endpoint.trim()
            prefs[authKey] = settings.auth.trim()
            prefs[usernameKey] = settings.username.trim()
        }
    }
}
