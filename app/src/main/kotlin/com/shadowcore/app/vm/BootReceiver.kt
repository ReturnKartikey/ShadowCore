package com.shadowcore.app.vm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint

/**
 * Receives BOOT_COMPLETED to optionally pre-warm VM resources.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Pre-warm: could start VmService or schedule WorkManager task
            // For now, this is a stub that can be enabled in settings
        }
    }
}
