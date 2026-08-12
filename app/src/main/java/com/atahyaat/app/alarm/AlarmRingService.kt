package com.atahyaat.app.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.atahyaat.app.MainActivity
import com.atahyaat.app.R
import com.atahyaat.app.data.AlertMode
import com.atahyaat.app.data.PrayerName
import com.atahyaat.app.data.PreferencesManager

class AlarmRingService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelfSafely()
            return START_NOT_STICKY
        }

        val prayerRaw = intent?.getStringExtra(AlarmReceiver.EXTRA_PRAYER) ?: return START_NOT_STICKY
        val prayer = PrayerName.valueOf(prayerRaw)
        val prefs = PreferencesManager(this)
        val mode = prefs.alertModeFor(prayer)

        startForeground(NOTIF_ID, buildNotification(prayer, mode))

        if (mode == AlertMode.ALARM || mode == AlertMode.NOTIFICATION) {
            playSound(prefs.soundFor(prayer), mode == AlertMode.ALARM)
        }
        if (mode == AlertMode.ALARM || mode == AlertMode.VIBRATION_ONLY) {
            vibrate(mode == AlertMode.ALARM)
        }

        if (mode == AlertMode.ALARM) {
            val fullScreenIntent = Intent(this, AlarmActivity::class.java).apply {
                putExtra(AlarmReceiver.EXTRA_PRAYER, prayer.name)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(fullScreenIntent)
        } else {
            // Auto-stop non-alarm alerts after they've played once.
            android.os.Handler(mainLooper).postDelayed({ stopSelfSafely() }, 6000)
        }

        return START_NOT_STICKY
    }

    private fun buildNotification(prayer: PrayerName, mode: AlertMode): Notification {
        val channelId = "prayer_alerts"
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Prayer Alerts", NotificationManager.IMPORTANCE_HIGH)
            channel.description = "Notifications for prayer times"
            nm.createNotificationChannel(channel)
        }

        val openIntent = Intent(this, MainActivity::class.java)
        val contentPending = PendingIntent.getActivity(
            this, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, AlarmRingService::class.java).apply { action = ACTION_STOP }
        val stopPending = PendingIntent.getService(
            this, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val label = prayer.name.lowercase().replaceFirstChar { it.uppercase() }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.time_for_prayer, label))
            .setContentText("Atahyaat prayer reminder")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(contentPending)
            .addAction(0, getString(R.string.stop), stopPending)

        if (mode == AlertMode.ALARM) {
            val fullScreenIntent = Intent(this, AlarmActivity::class.java).apply {
                putExtra(AlarmReceiver.EXTRA_PRAYER, prayer.name)
            }
            val fullScreenPending = PendingIntent.getActivity(
                this, 2, fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.setFullScreenIntent(fullScreenPending, true)
        }

        return builder.build()
    }

    private fun playSound(rawName: String, loop: Boolean) {
        try {
            val resId = resources.getIdentifier(rawName, "raw", packageName)
            if (resId == 0) return
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                val afd = resources.openRawResourceFd(resId)
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                isLooping = loop
                prepare()
                start()
            }
        } catch (_: Exception) {
        }
    }

    private fun vibrate(repeating: Boolean) {
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        val pattern = longArrayOf(0, 500, 300, 500, 300, 800)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(pattern, if (repeating) 0 else -1)
            vibrator?.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, if (repeating) 0 else -1)
        }
    }

    private fun stopSelfSafely() {
        mediaPlayer?.let { try { it.stop(); it.release() } catch (_: Exception) {} }
        mediaPlayer = null
        vibrator?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopSelfSafely()
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
    }

    companion object {
        const val NOTIF_ID = 5301
        const val ACTION_STOP = "com.atahyaat.app.ACTION_STOP_ALARM"
    }
}
