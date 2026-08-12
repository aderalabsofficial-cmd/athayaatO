package com.atahyaat.app.ui.tasbih

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.atahyaat.app.data.PreferencesManager
import com.atahyaat.app.databinding.FragmentTasbihBinding

class TasbihFragment : Fragment() {

    private var _binding: FragmentTasbihBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: PreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasbihBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferencesManager(requireContext())

        updateDisplay()

        binding.tasbihTapButton.setOnClickListener {
            prefs.tasbihCount += 1
            updateDisplay()
            lightTick()
        }

        binding.tasbihResetButton.setOnClickListener {
            prefs.tasbihCount = 0
            updateDisplay()
        }

        binding.tasbihCountText.setOnLongClickListener {
            cycleTarget()
            true
        }
    }

    private fun cycleTarget() {
        val options = listOf(33, 99, 100)
        val currentIndex = options.indexOf(prefs.tasbihTarget)
        prefs.tasbihTarget = options[(currentIndex + 1).mod(options.size)]
        updateDisplay()
    }

    private fun updateDisplay() {
        binding.tasbihCountText.text = prefs.tasbihCount.toString()
        binding.tasbihTargetText.text = "Target: ${prefs.tasbihTarget}"
    }

    private fun lightTick() {
        val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(15)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
