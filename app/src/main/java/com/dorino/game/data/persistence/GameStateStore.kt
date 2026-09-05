package com.dorino.game.data.persistence

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dorino.game.data.model.GameHistoryEntry
import com.dorino.game.data.model.GameSettings
import com.dorino.game.data.model.GameState
import com.dorino.game.data.model.PlayerProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "dorino_prefs")

/**
 * لایه‌ی Persistence برای قابلیت «ادامه بازی»، تنظیمات و تاریخچه.
 * از JSON برای ذخیره‌سازی ساختارهای پیچیده در DataStore استفاده می‌شود.
 */
class GameStateStore(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private object Keys {
        val SAVED_GAME = stringPreferencesKey("saved_game_state")
        val SETTINGS = stringPreferencesKey("game_settings")
        val HISTORY = stringPreferencesKey("game_history")
        val PLAYER_PROFILES = stringPreferencesKey("player_profiles")
    }

    val settingsFlow: Flow<GameSettings> = context.dataStore.data.map { prefs ->
        prefs[Keys.SETTINGS]?.let {
            runCatching { json.decodeFromString<GameSettings>(it) }.getOrNull()
        } ?: GameSettings()
    }

    val savedGameFlow: Flow<GameState?> = context.dataStore.data.map { prefs ->
        prefs[Keys.SAVED_GAME]?.let {
            runCatching { json.decodeFromString<GameState>(it) }.getOrNull()
        }
    }

    val historyFlow: Flow<List<GameHistoryEntry>> = context.dataStore.data.map { prefs ->
        prefs[Keys.HISTORY]?.let {
            runCatching { json.decodeFromString<List<GameHistoryEntry>>(it) }.getOrNull()
        } ?: emptyList()
    }

    val playerProfilesFlow: Flow<List<PlayerProfile>> = context.dataStore.data.map { prefs ->
        prefs[Keys.PLAYER_PROFILES]?.let {
            runCatching { json.decodeFromString<List<PlayerProfile>>(it) }.getOrNull()
        } ?: emptyList()
    }

    suspend fun saveSettings(settings: GameSettings) {
        context.dataStore.edit { it[Keys.SETTINGS] = json.encodeToString(settings) }
    }

    suspend fun saveGameState(state: GameState?) {
        context.dataStore.edit { prefs ->
            if (state == null) {
                prefs.remove(Keys.SAVED_GAME)
            } else {
                prefs[Keys.SAVED_GAME] = json.encodeToString(state)
            }
        }
    }

    suspend fun clearGameState() = saveGameState(null)

    suspend fun addHistoryEntry(entry: GameHistoryEntry) {
        val current = historyFlow.first()
        val updated = (listOf(entry) + current).take(50)
        context.dataStore.edit { it[Keys.HISTORY] = json.encodeToString(updated) }
    }

    suspend fun savePlayerProfiles(profiles: List<PlayerProfile>) {
        context.dataStore.edit { it[Keys.PLAYER_PROFILES] = json.encodeToString(profiles) }
    }
}
