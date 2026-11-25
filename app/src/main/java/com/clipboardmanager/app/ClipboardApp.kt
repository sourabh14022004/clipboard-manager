package com.clipboardmanager.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.clipboardmanager.app.data.local.ClipboardDatabase
import com.clipboardmanager.app.data.local.DataStoreSettingsStore
import com.clipboardmanager.app.data.repository.ClipRepository
import com.clipboardmanager.app.data.repository.DefaultClipRepository

class ClipboardApp : Application() {
    lateinit var repository: ClipRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = ClipboardDatabase.getInstance(this)
        val settingsStore = DataStoreSettingsStore(this)
        repository = DefaultClipRepository(
            clipDao = database.clipDao(),
            settingsStore = settingsStore
        )
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                MONITOR_CHANNEL_ID,
                getString(R.string.monitor_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.monitor_channel_desc)
                setShowBadge(false)
            }
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val MONITOR_CHANNEL_ID = "clipboard_monitor_channel"
    }
}
