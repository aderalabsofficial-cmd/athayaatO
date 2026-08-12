package com.atahyaat.app.ui.today

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.atahyaat.app.R
import com.atahyaat.app.data.PrayerName
import com.atahyaat.app.data.PrayerTimeCalculator
import com.atahyaat.app.data.PreferencesManager
import com.atahyaat.app.data.StreakRepository
import com.atahyaat.app.databinding.FragmentTodayBinding
import com.atahyaat.app.databinding.ItemPrayerBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class TodayFragment : Fragment() {

    private var _binding: FragmentTodayBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefs: PreferencesManager
    private lateinit var repository: StreakRepository
    private var countDownTimer: CountDownTimer? = null

    private val arabicNames = mapOf(
        PrayerName.FAJR to "الفجر",
        PrayerName.SUNRISE to "الشروق",
        PrayerName.DHUHR to "الظهر",
        PrayerName.ASR to "العصر",
        PrayerName.MAGHRIB to "المغرب",
        PrayerName.ISHA to "العشاء"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferencesManager(requireContext())
        repository = StreakRepository(requireContext())

        binding.locationText.text = prefs.cityName
        binding.dateText.text = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Calendar.getInstance().time)

        binding.viewStatsButton.setOnClickListener {
            (activity as? com.atahyaat.app.MainActivity)?.let {
                it.supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, com.atahyaat.app.ui.streak.StreakFragment())
                    .commit()
                it.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_nav)
                    .selectedItemId = R.id.nav_streak
            }
        }

        loadPrayerTimes()
        refreshStreakSummary()
    }

    override fun onResume() {
        super.onResume()
        loadPrayerTimes()
        refreshStreakSummary()
    }

    private fun tz() = TimeZone.getTimeZone(prefs.timeZoneId)

    private fun loadPrayerTimes() {
        val calculator = PrayerTimeCalculator(prefs.latitude, prefs.longitude, tz(), prefs.calculationMethod, prefs.asrMethod)
        val times = calculator.calculateToday()

        binding.prayerListContainer.removeAllViews()
        val order = listOf(PrayerName.FAJR, PrayerName.SUNRISE, PrayerName.DHUHR, PrayerName.ASR, PrayerName.MAGHRIB, PrayerName.ISHA)
        val obligatory = setOf(PrayerName.FAJR, PrayerName.DHUHR, PrayerName.ASR, PrayerName.MAGHRIB, PrayerName.ISHA)

        lifecycleScope.launch {
            val todayLog = repository.getToday()
            for (prayer in order) {
                val cal = times[prayer] ?: continue
                val row = ItemPrayerBinding.inflate(layoutInflater, binding.prayerListContainer, false)
                row.prayerName.text = prayer.name.lowercase().replaceFirstChar { it.uppercase() }
                row.prayerNameArabic.text = arabicNames[prayer]
                row.prayerTime.text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(cal.time)

                if (!obligatory.contains(prayer)) {
                    row.prayerSubtitle.visibility = View.VISIBLE
                    row.prayerSubtitle.text = getString(R.string.optional_sunnah)
                    row.prayerCheck.visibility = View.INVISIBLE
                } else {
                    val completed = when (prayer) {
                        PrayerName.FAJR -> todayLog.fajr
                        PrayerName.DHUHR -> todayLog.dhuhr
                        PrayerName.ASR -> todayLog.asr
                        PrayerName.MAGHRIB -> todayLog.maghrib
                        PrayerName.ISHA -> todayLog.isha
                        else -> false
                    }
                    row.prayerCheck.setImageResource(if (completed) R.drawable.ic_check_circle else R.drawable.circle_outline)
                    row.prayerCheck.setOnClickListener {
                        lifecycleScope.launch {
                            repository.setPrayerCompleted(prayer, !completed)
                            loadPrayerTimes()
                            refreshStreakSummary()
                        }
                    }
                }
                binding.prayerListContainer.addView(row.root)
            }
            updateNextPrayer(times)
        }
    }

    private fun updateNextPrayer(times: Map<PrayerName, Calendar>) {
        val now = Calendar.getInstance(tz())
        val obligatoryOrder = listOf(PrayerName.FAJR, PrayerName.DHUHR, PrayerName.ASR, PrayerName.MAGHRIB, PrayerName.ISHA)

        var next: Pair<PrayerName, Calendar>? = null
        for (p in obligatoryOrder) {
            val t = times[p] ?: continue
            if (t.after(now)) {
                next = p to t
                break
            }
        }
        if (next == null) {
            // All prayers passed today — next is tomorrow's Fajr
            val calculator = PrayerTimeCalculator(prefs.latitude, prefs.longitude, tz(), prefs.calculationMethod, prefs.asrMethod)
            val tomorrow = Calendar.getInstance(tz()).apply { add(Calendar.DAY_OF_YEAR, 1) }
            val nextDayTimes = calculator.calculate(
                tomorrow.get(Calendar.YEAR), tomorrow.get(Calendar.MONTH) + 1, tomorrow.get(Calendar.DAY_OF_MONTH)
            )
            next = PrayerName.FAJR to nextDayTimes[PrayerName.FAJR]!!
        }

        val (prayer, time) = next
        binding.nextPrayerName.text = prayer.name.lowercase().replaceFirstChar { it.uppercase() }
        binding.nextPrayerNameArabic.text = arabicNames[prayer]
        binding.nextPrayerClockTime.text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(time.time)

        countDownTimer?.cancel()
        val diff = time.timeInMillis - now.timeInMillis
        if (diff > 0) {
            countDownTimer = object : CountDownTimer(diff, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val h = millisUntilFinished / 3_600_000
                    val m = (millisUntilFinished / 60_000) % 60
                    val s = (millisUntilFinished / 1000) % 60
                    if (_binding != null) {
                        binding.countdownText.text = String.format("%02dh %02dm %02ds", h, m, s)
                    }
                }
                override fun onFinish() {
                    if (_binding != null) loadPrayerTimes()
                }
            }.start()
        }
    }

    private fun refreshStreakSummary() {
        lifecycleScope.launch {
            val today = repository.getToday()
            val streak = repository.currentStreak()
            binding.streakCountText.text = "$streak ${getString(R.string.day_streak)}"
            binding.streakProgressText.text = "${today.completedCount}/5 ${getString(R.string.obligatory_logged)}"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
        _binding = null
    }
}
