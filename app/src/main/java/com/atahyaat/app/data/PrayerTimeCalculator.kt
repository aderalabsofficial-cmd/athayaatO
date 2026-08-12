package com.atahyaat.app.data

import java.util.Calendar
import java.util.TimeZone
import kotlin.math.*

/**
 * Fully offline prayer-time calculator based on standard solar-position astronomy
 * (Julian date, solar declination and equation of time). No network access required.
 */
class PrayerTimeCalculator(
    private val latitude: Double,
    private val longitude: Double,
    private val timeZone: TimeZone,
    private val method: CalculationMethod = CalculationMethod.MWL,
    private val asrMethod: AsrMethod = AsrMethod.STANDARD
) {

    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun sunPosition(jd: Double): Pair<Double, Double> {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sinD(g) + 0.020 * sinD(2 * g))
        val e = 23.439 - 0.00000036 * d
        val dec = asinD(sinD(e) * sinD(l))
        var ra = atan2D(cosD(e) * sinD(l), cosD(l)) / 15.0
        ra = fixHour(ra)
        val eqt = q / 15.0 - ra
        return Pair(dec, eqt)
    }

    private fun fixAngle(a: Double): Double {
        var v = a % 360.0
        if (v < 0) v += 360.0
        return v
    }

    private fun fixHour(h: Double): Double {
        var v = h % 24.0
        if (v < 0) v += 24.0
        return v
    }

    private fun sinD(d: Double) = sin(Math.toRadians(d))
    private fun cosD(d: Double) = cos(Math.toRadians(d))
    private fun tanD(d: Double) = tan(Math.toRadians(d))
    private fun asinD(x: Double) = Math.toDegrees(asin(x))
    private fun acosD(x: Double) = Math.toDegrees(acos(x.coerceIn(-1.0, 1.0)))
    private fun atan2D(y: Double, x: Double) = Math.toDegrees(atan2(y, x))

    private fun computeTime(jd: Double, angle: Double, dec: Double, eqt: Double, isRising: Boolean): Double {
        val numerator = -sinD(angle) - sinD(dec) * sinD(latitude)
        val denominator = cosD(dec) * cosD(latitude)
        val ratio = numerator / denominator
        if (ratio.isNaN() || abs(ratio) > 1.0) {
            // Polar edge-case fallback: approximate with 90 degree hour angle
            return 12.0
        }
        val hourAngle = acosD(ratio) / 15.0
        val noon = 12.0 - eqt
        return if (isRising) noon - hourAngle else noon + hourAngle
    }

    private fun asrTime(jd: Double, dec: Double, eqt: Double, shadowFactor: Int): Double {
        val angle = -atan2D(1.0, shadowFactor + tanD(abs(latitude - dec)))
        val numerator = sinD(angle) - sinD(dec) * sinD(latitude)
        val denominator = cosD(dec) * cosD(latitude)
        val ratio = (numerator / denominator).coerceIn(-1.0, 1.0)
        val hourAngle = acosD(ratio) / 15.0
        val noon = 12.0 - eqt
        return noon + hourAngle
    }

    /** Returns a map of prayer -> decimal hour (local, before timezone offset applied). */
    fun calculate(year: Int, month: Int, day: Int): Map<PrayerName, Calendar> {
        val jd = julianDate(year, month, day) - longitude / (15.0 * 24.0)
        val (dec, eqt) = sunPosition(jd)

        val fajrAngle = method.fajrAngle
        val ishaAngle = method.ishaAngle

        val fajr = computeTime(jd, fajrAngle, dec, eqt, true)
        val sunrise = computeTime(jd, 0.833, dec, eqt, true)
        val dhuhr = 12.0 - eqt
        val asr = asrTime(jd, dec, eqt, asrMethod.shadowFactor)
        val maghrib = computeTime(jd, 0.833, dec, eqt, false)
        val isha = if (method == CalculationMethod.UMM_AL_QURA) {
            maghrib + 90.0 / 60.0
        } else {
            computeTime(jd, ishaAngle, dec, eqt, false)
        }

        val tzOffsetHours = timeZone.getOffset(
            Calendar.getInstance(timeZone).apply { set(year, month - 1, day) }.timeInMillis
        ) / 3600000.0

        fun toCalendar(decimalHour: Double): Calendar {
            val cal = Calendar.getInstance(timeZone)
            cal.set(year, month - 1, day, 0, 0, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val adjusted = decimalHour + tzOffsetHours
            val totalMinutes = (fixHour(adjusted) * 60).roundToInt()
            cal.set(Calendar.HOUR_OF_DAY, totalMinutes / 60)
            cal.set(Calendar.MINUTE, totalMinutes % 60)
            return cal
        }

        return mapOf(
            PrayerName.FAJR to toCalendar(fajr),
            PrayerName.SUNRISE to toCalendar(sunrise),
            PrayerName.DHUHR to toCalendar(dhuhr),
            PrayerName.ASR to toCalendar(asr),
            PrayerName.MAGHRIB to toCalendar(maghrib),
            PrayerName.ISHA to toCalendar(isha)
        )
    }

    fun calculateToday(): Map<PrayerName, Calendar> {
        val now = Calendar.getInstance(timeZone)
        return calculate(
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH) + 1,
            now.get(Calendar.DAY_OF_MONTH)
        )
    }
}
