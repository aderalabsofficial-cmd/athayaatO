package com.atahyaat.app.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.atahyaat.app.data.PrayerName
import com.atahyaat.app.data.PrayerTimeCalculator
import com.atahyaat.app.data.PreferencesManager
import java.util.Calendar
import java.util.TimeZone

/**
 * Schedules exact alarms for the five obligatory prayers, entirely from locally
 * computed prayer times (no network calls). Alarms are re-armed each time they
 * fire and on boot, so the app keeps working fully offline.
 */
object AlarmScheduler {

    private const val REQUEST_CODE_BASE = 4200

    fun requestCodeFor(prayer: PrayerName): Int = REQUEST_CODE_BASE + prayer.ordinal

    fun scheduleAll(context: Context) {
        val prefs = PreferencesManager(context)
        val tz = TimeZone.getTimeZone(prefs.timeZoneId)
        val calculator = PrayerTimeCalculator(
            prefs.latitude, prefs.longitude, tz, prefs.calculationMethod, prefs.asrMethod
        )

        val now = Calendar.getInstance(tz)
        val today = calculator.calculateToday()

        val obligatory = listOf(
            PrayerName.FAJR, PrayerName.DHUHR, PrayerName.ASR, PrayerName.MAGHRIB, PrayerName.ISHA
        )

        for (prayer in obligatory) {
            var time = today[prayer] ?: continue
            if (time.before(now)) {
                // Already passed today — schedule for tomorrow instead
                val tomorrow = Calendar.getInstance(tz).apply { add(Calendar.DAY_OF_YEAR, 1) }
                val nextDay = calculator.calculate(
                    tomorrow.get(Calendar.YEAR),
                    tomorrow.get(Calendar.MONTH) + 1,
                    tomorrow.get(Calendar.DAY_OF_MONTH)
                )
                time = nextDay[prayer] ?: continue
            }
            scheduleOne(context, prayer, time.timeInMillis)
        }
    }

    fun scheduleOne(context: Context, prayer: PrayerName, triggerAtMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_PRAYER, prayer.name)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCodeFor(prayer),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    fun cancelAll(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (prayer in PrayerName.values()) {
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCodeFor(prayer),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }
}
