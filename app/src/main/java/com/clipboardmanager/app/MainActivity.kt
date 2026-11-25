package com.clipboardmanager.app

import android.Manifest
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.clipboardmanager.app.clipboard.ClipboardHelper
import com.clipboardmanager.app.service.ClipboardMonitorService
import com.clipboardmanager.app.ui.ClipboardViewModel
import com.clipboardmanager.app.ui.UiEffect
import com.clipboardmanager.app.ui.screens.ClipboardHomeScreen
import com.clipboardmanager.app.ui.theme.ClipboardManagerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private val viewModel: ClipboardViewModel by viewModels {
        ClipboardViewModel.Factory((application as ClipboardApp).repository)
    }

    private var clipboardManager: ClipboardManager? = null

    private val clipChangedListener = ClipboardManager.OnPrimaryClipChangedListener {
        val text = ClipboardHelper.readCurrentText(this)
        viewModel.onSystemClipboardSeen(text)
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            ClipboardMonitorService.start(this)
        }
    }

    private val exportDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri ?: return@registerForActivityResult
        lifecycleScope.launch {
            val json = withContext(Dispatchers.IO) { viewModel.exportBackupJson() }
            withContext(Dispatchers.IO) {
                contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(json.toByteArray(Charsets.UTF_8))
                }
            }
        }
    }

    private val importDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@registerForActivityResult
        lifecycleScope.launch {
            val raw = withContext(Dispatchers.IO) {
                contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            }
            if (!raw.isNullOrBlank()) {
                viewModel.importBackupJson(raw)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        handleIncomingIntent(intent)

        setContent {
            ClipboardManagerTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(Unit) {
                    viewModel.effects.collect { effect ->
                        when (effect) {
                            is UiEffect.ShowMessage -> {
                                val result = snackbarHostState.showSnackbar(
                                    message = effect.message,
                                    actionLabel = if (effect.undoClip != null) "Undo" else null,
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed && effect.undoClip != null) {
                                    viewModel.undoDelete(effect.undoClip)
                                }
                            }
                            is UiEffect.CopyToSystemClipboard -> {
                                ClipboardHelper.writeText(this@MainActivity, effect.label, effect.text)
                            }
                            is UiEffect.ShareClip -> {
                                ClipboardHelper.shareText(this@MainActivity, effect.title, effect.text)
                            }
                            is UiEffect.OpenUrl -> {
                                ClipboardHelper.openUrl(this@MainActivity, effect.url)
                            }
                            is UiEffect.ToggleForegroundMonitor -> {
                                handleMonitorToggle(effect.enabled)
                            }
                        }
                    }
                }

                ClipboardHomeScreen(
                    uiState = uiState,
                    viewModel = viewModel,
                    snackbarHostState = snackbarHostState,
                    systemClipboardSnapshot = { ClipboardHelper.readCurrentText(this@MainActivity) },
                    onCopyDirect = { label, text ->
                        ClipboardHelper.writeText(this@MainActivity, label, text)
                    },
                    onShareDirect = { title, text ->
                        ClipboardHelper.shareText(this@MainActivity, title, text)
                    },
                    onOpenUrlDirect = { url ->
                        ClipboardHelper.openUrl(this@MainActivity, url)
                    },
                    onExportBackup = {
                        exportDocumentLauncher.launch("clipboard_backup.json")
                    },
                    onImportBackup = {
                        importDocumentLauncher.launch(arrayOf("application/json", "text/plain"))
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        clipboardManager?.addPrimaryClipChangedListener(clipChangedListener)
    }

    override fun onPause() {
        clipboardManager?.removePrimaryClipChangedListener(clipChangedListener)
        super.onPause()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // Android 10+ allows reading clipboard once the activity has window focus
            val current = ClipboardHelper.readCurrentText(this)
            val forceCapture = intent?.getBooleanExtra(EXTRA_CAPTURE_CLIPBOARD, false) == true
            if (forceCapture) {
                intent?.removeExtra(EXTRA_CAPTURE_CLIPBOARD)
            }
            viewModel.onSystemClipboardSeen(current, forceCapture = forceCapture)
        }
    }

    private fun handleIncomingIntent(intent: Intent?) {
        intent ?: return
        if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val shared = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!shared.isNullOrBlank()) {
                viewModel.onSharedTextReceived(shared)
            }
        }
    }

    private fun handleMonitorToggle(enabled: Boolean) {
        if (!enabled) {
            ClipboardMonitorService.stop(this)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (granted) {
                ClipboardMonitorService.start(this)
            } else {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            ClipboardMonitorService.start(this)
        }
    }

    companion object {
        const val EXTRA_CAPTURE_CLIPBOARD = "extra_capture_clipboard"
    }
}
