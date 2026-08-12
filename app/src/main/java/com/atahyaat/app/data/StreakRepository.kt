package com.atahyaat.app.data

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StreakRepository(context: Context) {
    private val dao = AppDatabase.getInstance(context).prayerLogDao()
    private val dateKeyFormat = SimpleDateFormat("yyyyMMdd", Locale.US)

    fun todayKey(): String = dateKeyFormat.format(Date())

    private fun keyForDaysAgo(days: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -days)
        return dateKeyFormat.format(cal.time)
    }

    suspend fun getToday(): PrayerLogEntity {
        return dao.getByDate(todayKey()) ?: PrayerLogEntity(dateKey = todayKey())
    }

    suspend fun setPrayerCompleted(prayer: PrayerName, completed: Boolean) {
        val current = getToday()
        val updated = when (prayer) {
            PrayerName.FAJR -> current.copy(fajr = completed)
            PrayerName.DHUHR -> current.copy(dhuhr = completed)
            PrayerName.ASR -> current.copy(asr = completed)
            PrayerName.MAGHRIB -> current.copy(maghrib = completed)
            PrayerName.ISHA -> current.copy(isha = completed)
            PrayerName.SUNRISE -> current // not obligatory, not tracked
        }
        dao.upsert(updated)
    }

    fun observeToday() = dao.observeByDate(todayKey())

    fun observeRecent(days: Int = 60) = dao.observeRecent(days)

    suspend fun getRecentList(days: Int = 60): List<PrayerLogEntity> = dao.getRecent(days)

    /** Current consecutive-day streak of fully completed (5/5) days, counting back from today or yesterday. */
    suspend fun currentStreak(): Int {
        val recent = dao.getRecent(400).associateBy { it.dateKey }
        var streak = 0
        var dayOffset = 0

        // If today isn't complete yet, still allow streak to count from yesterday backwards
        val todayEntry = recent[todayKey()]
        if (todayEntry == null || !todayEntry.isComplete) {
            dayOffset = 1
        }

        while (true) {
            val key = keyForDaysAgo(dayOffset)
            val entry = recent[key]
            if (entry != null && entry.isComplete) {
                streak++
                dayOffset++
            } else {
                break
            }
        }
        return streak
    }

    suspend fun bestStreak(): Int {
        val recent = dao.getRecent(400).filter { it.isComplete }.map { it.dateKey }.sorted()
        if (recent.isEmpty()) return 0
        var best = 1
        var current = 1
        for (i in 1 until recent.size) {
            val prevCal = Calendar.getInstance().apply { time = dateKeyFormat.parse(recent[i - 1])!! }
            prevCal.add(Calendar.DAY_OF_YEAR, 1)
            val expectedNext = dateKeyFormat.format(prevCal.time)
            current = if (expectedNext == recent[i]) current + 1 else 1
            best = maxOf(best, current)
        }
        return best
    }
}
