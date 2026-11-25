package com.clipboardmanager.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProcessTextActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val selectedText = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()
        if (!selectedText.isNullOrBlank()) {
            val app = application as ClipboardApp
            CoroutineScope(Dispatchers.IO).launch {
                app.repository.captureClipboardText(selectedText, sourceApp = "Selection")
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        applicationContext,
                        "Saved to Clipboard Manager",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        } else {
            finish()
        }
    }
}
