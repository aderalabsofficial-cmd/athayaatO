package com.atahyaat.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.atahyaat.app.data.PreferencesManager

class AtahyaatApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val prefs = PreferencesManager(this)
        AppCompatDelegate.setDefaultNightMode(
            if (prefs.darkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
