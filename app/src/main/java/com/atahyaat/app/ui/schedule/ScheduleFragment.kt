package com.atahyaat.app.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.atahyaat.app.R
import com.atahyaat.app.data.PrayerName
import com.atahyaat.app.data.PrayerTimeCalculator
import com.atahyaat.app.data.PreferencesManager
import com.atahyaat.app.databinding.FragmentScheduleBinding
import com.atahyaat.app.databinding.ItemPrayerBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefs: PreferencesManager
    private var dayOffset = 0

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
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferencesManager(requireContext())

        binding.prevDayButton.setOnClickListener {
            dayOffset -= 1
            renderDay()
        }
        binding.nextDayButton.setOnClickListener {
            dayOffset += 1
            renderDay()
        }

        renderDay()
    }

    private fun renderDay() {
        val tz = TimeZone.getTimeZone(prefs.timeZoneId)
        val cal = Calendar.getInstance(tz).apply { add(Calendar.DAY_OF_YEAR, dayOffset) }

        binding.scheduleDateText.text = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(cal.time)

        val calculator = PrayerTimeCalculator(prefs.latitude, prefs.longitude, tz, prefs.calculationMethod, prefs.asrMethod)
        val times = calculator.calculate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))

        binding.scheduleListContainer.removeAllViews()
        val order = listOf(PrayerName.FAJR, PrayerName.SUNRISE, PrayerName.DHUHR, PrayerName.ASR, PrayerName.MAGHRIB, PrayerName.ISHA)
        val obligatory = setOf(PrayerName.FAJR, PrayerName.DHUHR, PrayerName.ASR, PrayerName.MAGHRIB, PrayerName.ISHA)

        for (prayer in order) {
            val time = times[prayer] ?: continue
            val row = ItemPrayerBinding.inflate(layoutInflater, binding.scheduleListContainer, false)
            row.prayerName.text = prayer.name.lowercase().replaceFirstChar { it.uppercase() }
            row.prayerNameArabic.text = arabicNames[prayer]
            row.prayerTime.text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(time.time)
            row.prayerCheck.visibility = View.INVISIBLE
            if (!obligatory.contains(prayer)) {
                row.prayerSubtitle.visibility = View.VISIBLE
                row.prayerSubtitle.text = getString(R.string.optional_sunnah)
            }
            binding.scheduleListContainer.addView(row.root)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
