package com.atahyaat.app.ui.settings

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.atahyaat.app.alarm.AlarmScheduler
import com.atahyaat.app.data.AlertMode
import com.atahyaat.app.data.CalculationMethod
import com.atahyaat.app.data.CityData
import com.atahyaat.app.data.PrayerName
import com.atahyaat.app.data.PreferencesManager
import com.atahyaat.app.databinding.FragmentSettingsBinding
import com.atahyaat.app.databinding.ItemAlertSettingBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: PreferencesManager

    private data class SoundOption(val resName: String, val label: String)

    private val soundOptions = listOf(
        SoundOption("tone_chime", "Soft Chime"),
        SoundOption("tone_bell", "Gentle Bell"),
        SoundOption("tone_soft", "Soft Ping"),
        SoundOption("tone_classic", "Classic Tone")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferencesManager(requireContext())

        setupCitySpinner()
        setupMethodSpinner()
        setupDarkModeSwitch()
        setupAlertRows()
    }

    private fun setupCitySpinner() {
        val names = CityData.PRESETS.map { "${it.name}, ${it.country}" }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, names)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.citySpinner.adapter = adapter

        val currentIndex = CityData.PRESETS.indexOfFirst { it.name == prefs.cityName }.coerceAtLeast(0)
        binding.citySpinner.setSelection(currentIndex)

        binding.citySpinner.post {
            binding.citySpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val city = CityData.PRESETS[position]
                    prefs.cityName = city.name
                    prefs.latitude = city.latitude
                    prefs.longitude = city.longitude
                    prefs.timeZoneId = city.timeZoneId
                    AlarmScheduler.scheduleAll(requireContext())
                }
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            }
        }
    }

    private fun setupMethodSpinner() {
        val methods = CalculationMethod.values()
        val labels = methods.map { it.label }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, labels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.methodSpinner.adapter = adapter
        binding.methodSpinner.setSelection(methods.indexOf(prefs.calculationMethod))

        binding.methodSpinner.post {
            binding.methodSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                    prefs.calculationMethod = methods[position]
                    AlarmScheduler.scheduleAll(requireContext())
                }
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            }
        }
    }

    private fun setupDarkModeSwitch() {
        binding.darkModeSwitch.isChecked = prefs.darkMode
        binding.darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.darkMode = isChecked
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

    private fun setupAlertRows() {
        binding.alertSettingsContainer.removeAllViews()
        val obligatory = listOf(PrayerName.FAJR, PrayerName.DHUHR, PrayerName.ASR, PrayerName.MAGHRIB, PrayerName.ISHA)
        for (prayer in obligatory) {
            val row = ItemAlertSettingBinding.inflate(layoutInflater, binding.alertSettingsContainer, false)
            bindAlertRow(row, prayer)
            row.root.setOnClickListener { showAlertPicker(prayer, row) }
            binding.alertSettingsContainer.addView(row.root)
        }
    }

    private fun bindAlertRow(row: ItemAlertSettingBinding, prayer: PrayerName) {
        row.alertPrayerName.text = prayer.name.lowercase().replaceFirstChar { it.uppercase() }
        val mode = prefs.alertModeFor(prayer)
        val soundLabel = soundOptions.find { it.resName == prefs.soundFor(prayer) }?.label ?: "Soft Chime"
        row.alertModeSummary.text = when (mode) {
            AlertMode.ALARM -> "Alarm • $soundLabel"
            AlertMode.NOTIFICATION -> "Notification • $soundLabel"
            AlertMode.VIBRATION_ONLY -> "Vibration only"
            AlertMode.SILENT -> "Silent"
        }
    }

    private fun showAlertPicker(prayer: PrayerName, row: ItemAlertSettingBinding) {
        val options = mutableListOf<String>()
        val actions = mutableListOf<() -> Unit>()

        for (sound in soundOptions) {
            options.add("Alarm • ${sound.label}")
            actions.add {
                prefs.setAlertModeFor(prayer, AlertMode.ALARM)
                prefs.setSoundFor(prayer, sound.resName)
            }
        }
        for (sound in soundOptions) {
            options.add("Notification • ${sound.label}")
            actions.add {
                prefs.setAlertModeFor(prayer, AlertMode.NOTIFICATION)
                prefs.setSoundFor(prayer, sound.resName)
            }
        }
        options.add("Vibration only")
        actions.add { prefs.setAlertModeFor(prayer, AlertMode.VIBRATION_ONLY) }

        options.add("Silent")
        actions.add { prefs.setAlertModeFor(prayer, AlertMode.SILENT) }

        AlertDialog.Builder(requireContext())
            .setTitle("${prayer.name.lowercase().replaceFirstChar { it.uppercase() }} Alert")
            .setItems(options.toTypedArray()) { _, which ->
                actions[which].invoke()
                bindAlertRow(row, prayer)
                AlarmScheduler.scheduleAll(requireContext())
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
