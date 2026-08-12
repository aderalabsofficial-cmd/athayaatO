package com.atahyaat.app.data

import java.util.Calendar

enum class PrayerName {
    FAJR, SUNRISE, DHUHR, ASR, MAGHRIB, ISHA
}

data class PrayerTime(
    val name: PrayerName,
    val calendar: Calendar,
    val isObligatory: Boolean
)

/** Angle-based calculation methods (offline, no internet required). */
enum class CalculationMethod(val fajrAngle: Double, val ishaAngle: Double, val label: String) {
    MWL(18.0, 17.0, "Muslim World League"),
    ISNA(15.0, 15.0, "Islamic Society of North America"),
    EGYPT(19.5, 17.5, "Egyptian General Authority"),
    KARACHI(18.0, 18.0, "University of Islamic Sciences, Karachi"),
    UMM_AL_QURA(18.5, 90.0, "Umm al-Qura, Makkah") // Isha = Maghrib + 90 min, handled specially
}

enum class AsrMethod(val shadowFactor: Int) {
    STANDARD(1), // Shafi'i, Maliki, Hanbali
    HANAFI(2)
}
