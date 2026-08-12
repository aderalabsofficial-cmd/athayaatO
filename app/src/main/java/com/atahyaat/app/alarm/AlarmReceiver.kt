package com.atahyaat.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.atahyaat.app.data.AlertMode
import com.atahyaat.app.data.PrayerName
import com.atahyaat.app.data.PreferencesManager

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_PRAYER = "extra_prayer"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val prayerRaw = intent.getStringExtra(EXTRA_PRAYER) ?: return
        val prayer = PrayerName.valueOf(prayerRaw)
        val prefs = PreferencesManager(context)
        val mode = prefs.alertModeFor(prayer)

        if (mode != AlertMode.SILENT) {
            val serviceIntent = Intent(context, AlarmRingService::class.java).apply {
                putExtra(EXTRA_PRAYER, prayer.name)
            }
            ContextCompat.startForegroundService(context, serviceIntent)
        }

        // Re-arm tomorrow's alarm for this same prayer so the schedule keeps going offline.
        AlarmScheduler.scheduleAll(context)
    }
}
