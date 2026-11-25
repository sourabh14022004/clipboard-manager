package com.clipboardmanager.app.service

import android.app.PendingIntent
import android.app.Service
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.clipboardmanager.app.ClipboardApp
import com.clipboardmanager.app.MainActivity
import com.clipboardmanager.app.R
import com.clipboardmanager.app.clipboard.ClipboardHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ClipboardMonitorService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var clipboardManager: ClipboardManager? = null

    private val clipListener = ClipboardManager.OnPrimaryClipChangedListener {
        val text = ClipboardHelper.readCurrentText(this) ?: return@OnPrimaryClipChangedListener
        val app = application as? ClipboardApp ?: return@OnPrimaryClipChangedListener
        serviceScope.launch {
            app.repository.captureClipboardText(text, sourceApp = "Monitor")
        }
    }

    override fun onCreate() {
        super.onCreate()
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboardManager?.addPrimaryClipChangedListener(clipListener)
        startInForeground()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                val app = application as? ClipboardApp
                serviceScope.launch {
                    app?.repository?.setMonitorNotification(false)
                }
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_CAPTURE_NOW -> {
                val openMain = Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra(MainActivity.EXTRA_CAPTURE_CLIPBOARD, true)
                }
                startActivity(openMain)
            }
        }
        return START_STICKY
    }

    private fun startInForeground() {
        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val captureIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, ClipboardMonitorService::class.java).apply { action = ACTION_CAPTURE_NOW },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this,
            2,
            Intent(this, ClipboardMonitorService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, ClipboardApp.MONITOR_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_tile_clipboard)
            .setContentTitle("Clipboard Manager Active")
            .setContentText("Tap Capture to save your current clipboard anytime")
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(openIntent)
            .addAction(0, "Capture Now", captureIntent)
            .addAction(0, "Turn Off", stopIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onDestroy() {
        clipboardManager?.removePrimaryClipChangedListener(clipListener)
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val NOTIFICATION_ID = 1042
        const val ACTION_CAPTURE_NOW = "com.clipboardmanager.app.ACTION_CAPTURE_NOW"
        const val ACTION_STOP = "com.clipboardmanager.app.ACTION_STOP"

        fun start(context: Context) {
            val intent = Intent(context, ClipboardMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, ClipboardMonitorService::class.java))
        }
    }
}
