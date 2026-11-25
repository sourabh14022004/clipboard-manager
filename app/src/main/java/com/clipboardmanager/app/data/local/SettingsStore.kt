package com.clipboardmanager.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.clipboardmanager.app.data.model.SortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "clipboard_settings")

data class AppSettings(
    val autoCaptureEnabled: Boolean = true,
    val deduplicateClips: Boolean = true,
    val monitorNotificationEnabled: Boolean = false,
    val sortOrder: SortOrder = SortOrder.NEWEST,
    val hasPinConfigured: Boolean = false,
    val hasSeededSamples: Boolean = false,
    val nextColorIndex: Int = 0,
    val retentionLimit: Int = 500
)

interface SettingsStore {
    val settingsFlow: Flow<AppSettings>
    suspend fun setAutoCapture(enabled: Boolean)
    suspend fun setDeduplicateClips(enabled: Boolean)
    suspend fun setMonitorNotification(enabled: Boolean)
    suspend fun setSortOrder(sortOrder: SortOrder)
    suspend fun setRetentionLimit(limit: Int)
    suspend fun markSamplesSeeded()
    suspend fun consumeNextColorIndex(): Int
    suspend fun setVaultPin(pin: String)
    suspend fun verifyVaultPin(pin: String): Boolean
    suspend fun clearVaultPin()
}

class DataStoreSettingsStore(private val context: Context) : SettingsStore {
    private object Keys {
        val AUTO_CAPTURE = booleanPreferencesKey("auto_capture")
        val DEDUPLICATE = booleanPreferencesKey("deduplicate_clips")
        val MONITOR_NOTIFICATION = booleanPreferencesKey("monitor_notification")
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val PIN_HASH = stringPreferencesKey("vault_pin_hash")
        val PIN_SALT = stringPreferencesKey("vault_pin_salt")
        val HAS_SEEDED_SAMPLES = booleanPreferencesKey("has_seeded_samples")
        val NEXT_COLOR_INDEX = intPreferencesKey("next_color_index")
        val RETENTION_LIMIT = intPreferencesKey("retention_limit")
    }

    override val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            autoCaptureEnabled = prefs[Keys.AUTO_CAPTURE] ?: true,
            deduplicateClips = prefs[Keys.DEDUPLICATE] ?: true,
            monitorNotificationEnabled = prefs[Keys.MONITOR_NOTIFICATION] ?: false,
            sortOrder = runCatching {
                SortOrder.valueOf(prefs[Keys.SORT_ORDER] ?: SortOrder.NEWEST.name)
            }.getOrDefault(SortOrder.NEWEST),
            hasPinConfigured = !prefs[Keys.PIN_HASH].isNullOrBlank(),
            hasSeededSamples = prefs[Keys.HAS_SEEDED_SAMPLES] ?: false,
            nextColorIndex = prefs[Keys.NEXT_COLOR_INDEX] ?: 0,
            retentionLimit = prefs[Keys.RETENTION_LIMIT] ?: 500
        )
    }

    override suspend fun setAutoCapture(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_CAPTURE] = enabled }
    }

    override suspend fun setDeduplicateClips(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DEDUPLICATE] = enabled }
    }

    override suspend fun setMonitorNotification(enabled: Boolean) {
        context.dataStore.edit { it[Keys.MONITOR_NOTIFICATION] = enabled }
    }

    override suspend fun setSortOrder(sortOrder: SortOrder) {
        context.dataStore.edit { it[Keys.SORT_ORDER] = sortOrder.name }
    }

    override suspend fun setRetentionLimit(limit: Int) {
        context.dataStore.edit { it[Keys.RETENTION_LIMIT] = limit.coerceIn(50, 2000) }
    }

    override suspend fun markSamplesSeeded() {
        context.dataStore.edit { it[Keys.HAS_SEEDED_SAMPLES] = true }
    }

    override suspend fun consumeNextColorIndex(): Int {
        var assigned = 0
        context.dataStore.edit { prefs ->
            assigned = prefs[Keys.NEXT_COLOR_INDEX] ?: 0
            prefs[Keys.NEXT_COLOR_INDEX] = (assigned + 1) % 6
        }
        return assigned
    }

    override suspend fun setVaultPin(pin: String) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val saltEncoded = Base64.getEncoder().encodeToString(salt)
        val hash = hashPin(pin, salt)
        context.dataStore.edit { prefs ->
            prefs[Keys.PIN_SALT] = saltEncoded
            prefs[Keys.PIN_HASH] = hash
        }
    }

    override suspend fun verifyVaultPin(pin: String): Boolean {
        val prefs = context.dataStore.data.first()
        val saltEncoded = prefs[Keys.PIN_SALT] ?: return false
        val expectedHash = prefs[Keys.PIN_HASH] ?: return false
        val salt = runCatching { Base64.getDecoder().decode(saltEncoded) }.getOrNull() ?: return false
        return hashPin(pin, salt) == expectedHash
    }

    override suspend fun clearVaultPin() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.PIN_HASH)
            prefs.remove(Keys.PIN_SALT)
        }
    }

    private fun hashPin(pin: String, salt: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt)
        val hashed = digest.digest(pin.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(hashed)
    }
}
