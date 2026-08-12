package com.atahyaat.app.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.atahyaat.app.data.PrayerName
import com.atahyaat.app.databinding.ActivityAlarmBinding
import java.util.Calendar

class AlarmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        turnScreenOnAndKeyguardOff()

        val prayerRaw = intent.getStringExtra(AlarmReceiver.EXTRA_PRAYER) ?: PrayerName.FAJR.name
        val prayer = PrayerName.valueOf(prayerRaw)
        val label = prayer.name.lowercase().replaceFirstChar { it.uppercase() }

        binding.prayerNameText.text = label
        binding.timeText.text = android.text.format.DateFormat.format("h:mm a", Calendar.getInstance())

        binding.stopButton.setOnClickListener {
            stopAlarmAndFinish()
        }

        binding.snoozeButton.setOnClickListener {
            snooze(prayer)
            stopAlarmAndFinish()
        }
    }

    private fun turnScreenOnAndKeyguardOff() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        val wl = pm.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "atahyaat:alarm"
        )
        wl.acquire(10_000)
    }

    private fun stopAlarmAndFinish() {
        val stopIntent = Intent(this, AlarmRingService::class.java).apply {
            action = AlarmRingService.ACTION_STOP
        }
        startService(stopIntent)
        finish()
    }

    private fun snooze(prayer: PrayerName) {
        val triggerAt = System.currentTimeMillis() + 5 * 60 * 1000L
        AlarmScheduler.scheduleOne(this, prayer, triggerAt)
    }

    override fun onBackPressed() {
        // Prevent dismissing the alarm with back button; require explicit Stop/Snooze.
    }
}
