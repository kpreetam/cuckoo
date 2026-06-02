package com.example.cuckooclock

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import androidx.preference.PreferenceManager
import java.util.Calendar

class ChimeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
    val isHalf = intent.getBooleanExtra(ChimeScheduler.EXTRA_IS_HALF_HOUR, false)
    val hourCount = intent.getIntExtra(ChimeScheduler.EXTRA_HOUR_COUNT, 1)
    
    android.util.Log.d("ChimeReceiver", "Chime received: isHalf=$isHalf, hourCount=$hourCount")

    val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    // Check if this type is enabled
    val enabled = if (isHalf) {
        prefs.getBoolean(PrefsKeys.HALF_HOUR_CHIME_ENABLED, true)
    } else {
        prefs.getBoolean(PrefsKeys.HOUR_CHIME_ENABLED, true)
    }

    android.util.Log.d("ChimeReceiver", "Enabled: $enabled, Bedtime: ${isBedtime(context)}, Silent: ${isSilentModeBlocking(context)}")

    if (enabled && !isBedtime(context) && !isSilentModeBlocking(context)) {
        val serviceIntent = Intent(context, ChimeService::class.java).apply {
            putExtra(ChimeScheduler.EXTRA_IS_HALF_HOUR, isHalf)
            putExtra(ChimeScheduler.EXTRA_HOUR_COUNT, hourCount)
        }
        context.startForegroundService(serviceIntent)
        android.util.Log.d("ChimeReceiver", "Service started")
    }

    // Schedule the next chime
    android.util.Log.d("ChimeReceiver", "Scheduling next chime...")
    ChimeScheduler.scheduleNextChime(context)
    android.util.Log.d("ChimeReceiver", "Next chime scheduled")
}

    private fun isBedtime(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        if (!prefs.getBoolean(PrefsKeys.BEDTIME_ENABLED, false)) return false

        val startStr = prefs.getString(PrefsKeys.BEDTIME_START, "22:00") ?: "22:00"
        val endStr = prefs.getString(PrefsKeys.BEDTIME_END, "07:00") ?: "07:00"

        val now = Calendar.getInstance()
        val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        val startParts = startStr.split(":")
        val endParts = endStr.split(":")
        val startMinutes = (startParts[0].toIntOrNull() ?: 22) * 60 + (startParts[1].toIntOrNull() ?: 0)
        val endMinutes = (endParts[0].toIntOrNull() ?: 7) * 60 + (endParts[1].toIntOrNull() ?: 0)

        return if (startMinutes > endMinutes) {
            // Overnight: e.g. 22:00 -> 07:00
            nowMinutes >= startMinutes || nowMinutes < endMinutes
        } else {
            nowMinutes in startMinutes until endMinutes
        }
    }

    private fun isSilentModeBlocking(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val overrideSilent = prefs.getBoolean(PrefsKeys.OVERRIDE_SILENT, false)
        if (overrideSilent) return false

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT ||
               audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE
    }
}
