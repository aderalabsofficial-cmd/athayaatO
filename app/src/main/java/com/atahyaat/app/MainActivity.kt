package com.atahyaat.app

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.atahyaat.app.alarm.AlarmScheduler
import com.atahyaat.app.databinding.ActivityMainBinding
import com.atahyaat.app.ui.schedule.ScheduleFragment
import com.atahyaat.app.ui.settings.SettingsFragment
import com.atahyaat.app.ui.streak.StreakFragment
import com.atahyaat.app.ui.tasbih.TasbihFragment
import com.atahyaat.app.ui.today.TodayFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestRuntimePermissionsIfNeeded()

        if (savedInstanceState == null) {
            switchFragment(TodayFragment())
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_today -> TodayFragment()
                R.id.nav_schedule -> ScheduleFragment()
                R.id.nav_streak -> StreakFragment()
                R.id.nav_tasbih -> TasbihFragment()
                R.id.nav_settings -> SettingsFragment()
                else -> TodayFragment()
            }
            switchFragment(fragment)
            true
        }

        AlarmScheduler.scheduleAll(this)
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun requestRuntimePermissionsIfNeeded() {
        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), 101)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                } catch (_: Exception) {
                }
            }
        }
    }
}
