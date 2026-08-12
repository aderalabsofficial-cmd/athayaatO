package com.atahyaat.app.data

import android.content.Context
import android.content.SharedPreferences

enum class AlertMode { ALARM, NOTIFICATION, VIBRATION_ONLY, SILENT }

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("atahyaat_prefs", Context.MODE_PRIVATE)

    var cityName: String
        get() = prefs.getString("city_name", "Makkah") ?: "Makkah"
        set(v) = prefs.edit().putString("city_name", v).apply()

    var latitude: Double
        get() = prefs.getFloat("lat", 21.3891f).toDouble()
        set(v) = prefs.edit().putFloat("lat", v.toFloat()).apply()

    var longitude: Double
        get() = prefs.getFloat("lng", 39.8579f).toDouble()
        set(v) = prefs.edit().putFloat("lng", v.toFloat()).apply()

    var timeZoneId: String
        get() = prefs.getString("tz", "Asia/Riyadh") ?: "Asia/Riyadh"
        set(v) = prefs.edit().putString("tz", v).apply()

    var calculationMethod: CalculationMethod
        get() = CalculationMethod.valueOf(prefs.getString("calc_method", CalculationMethod.MWL.name)!!)
        set(v) = prefs.edit().putString("calc_method", v.name).apply()

    var asrMethod: AsrMethod
        get() = AsrMethod.valueOf(prefs.getString("asr_method", AsrMethod.STANDARD.name)!!)
        set(v) = prefs.edit().putString("asr_method", v.name).apply()

    var darkMode: Boolean
        get() = prefs.getBoolean("dark_mode", false)
        set(v) = prefs.edit().putBoolean("dark_mode", v).apply()

    fun alertModeFor(prayer: PrayerName): AlertMode {
        val raw = prefs.getString("alert_${prayer.name}", AlertMode.ALARM.name)!!
        return AlertMode.valueOf(raw)
    }

    fun setAlertModeFor(prayer: PrayerName, mode: AlertMode) {
        prefs.edit().putString("alert_${prayer.name}", mode.name).apply()
    }

    fun soundFor(prayer: PrayerName): String {
        return prefs.getString("sound_${prayer.name}", "tone_chime") ?: "tone_chime"
    }

    fun setSoundFor(prayer: PrayerName, rawResName: String) {
        prefs.edit().putString("sound_${prayer.name}", rawResName).apply()
    }

    var tasbihCount: Int
        get() = prefs.getInt("tasbih_count", 0)
        set(v) = prefs.edit().putInt("tasbih_count", v).apply()

    var tasbihTarget: Int
        get() = prefs.getInt("tasbih_target", 33)
        set(v) = prefs.edit().putInt("tasbih_target", v).apply()

    var onboardingDone: Boolean
        get() = prefs.getBoolean("onboarding_done", false)
        set(v) = prefs.edit().putBoolean("onboarding_done", v).apply()
}
