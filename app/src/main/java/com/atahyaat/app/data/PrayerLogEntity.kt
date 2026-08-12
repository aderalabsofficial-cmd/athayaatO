package com.atahyaat.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One row per calendar day, tracking which of the 5 obligatory prayers were completed.
 * dateKey format: yyyyMMdd (local date), used as primary key so there is exactly one row per day.
 */
@Entity(tableName = "prayer_log")
data class PrayerLogEntity(
    @PrimaryKey val dateKey: String,
    val fajr: Boolean = false,
    val dhuhr: Boolean = false,
    val asr: Boolean = false,
    val maghrib: Boolean = false,
    val isha: Boolean = false
) {
    val completedCount: Int
        get() = listOf(fajr, dhuhr, asr, maghrib, isha).count { it }

    val isComplete: Boolean
        get() = completedCount == 5
}
