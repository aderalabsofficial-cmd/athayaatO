package com.atahyaat.app.ui.streak

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.atahyaat.app.R
import com.atahyaat.app.data.PrayerName
import com.atahyaat.app.data.StreakRepository
import com.atahyaat.app.databinding.FragmentStreakBinding
import com.atahyaat.app.databinding.ItemPrayerBinding
import com.atahyaat.app.databinding.ItemWeekDayBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class StreakFragment : Fragment() {

    private var _binding: FragmentStreakBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: StreakRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStreakBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = StreakRepository(requireContext())
        loadData()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        lifecycleScope.launch {
            binding.currentStreakValue.text = repository.currentStreak().toString()
            binding.bestStreakValue.text = repository.bestStreak().toString()
            renderWeek()
            renderTodayChecklist()
        }
    }

    private suspend fun renderWeek() {
        binding.weekRowContainer.removeAllViews()
        val dayFormat = SimpleDateFormat("EEEEE", Locale.getDefault())
        val keyFormat = SimpleDateFormat("yyyyMMdd", Locale.US)

        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -6)

        val recent = repository.getRecentList(60)

        for (i in 0..6) {
            val dayCal = cal.clone() as Calendar
            dayCal.add(Calendar.DAY_OF_YEAR, i)
            val key = keyFormat.format(dayCal.time)
            val entry = recent.find { it.dateKey == key }

            val item = ItemWeekDayBinding.inflate(layoutInflater, binding.weekRowContainer, false)
            item.dayLabel.text = dayFormat.format(dayCal.time)
            val isComplete = entry?.isComplete == true
            item.dayDot.setImageResource(if (isComplete) R.drawable.ic_check_circle else R.drawable.circle_outline)
            binding.weekRowContainer.addView(item.root)
        }
    }

    private fun renderTodayChecklist() {
        lifecycleScope.launch {
            val today = repository.getToday()
            binding.todayChecklistContainer.removeAllViews()

            val labels = listOf(
                Triple(PrayerName.FAJR, "Fajr", today.fajr),
                Triple(PrayerName.DHUHR, "Dhuhr", today.dhuhr),
                Triple(PrayerName.ASR, "Asr", today.asr),
                Triple(PrayerName.MAGHRIB, "Maghrib", today.maghrib),
                Triple(PrayerName.ISHA, "Isha", today.isha)
            )

            for ((prayer, label, completed) in labels) {
                val row = ItemPrayerBinding.inflate(layoutInflater, binding.todayChecklistContainer, false)
                row.prayerName.text = label
                row.prayerNameArabic.visibility = View.GONE
                row.prayerTime.visibility = View.GONE
                row.prayerCheck.setImageResource(if (completed) R.drawable.ic_check_circle else R.drawable.circle_outline)
                row.prayerCheck.setOnClickListener {
                    lifecycleScope.launch {
                        repository.setPrayerCompleted(prayer, !completed)
                        loadData()
                    }
                }
                binding.todayChecklistContainer.addView(row.root)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
