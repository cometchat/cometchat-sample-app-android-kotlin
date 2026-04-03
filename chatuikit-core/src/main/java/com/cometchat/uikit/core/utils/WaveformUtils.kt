package com.cometchat.uikit.core.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile
import kotlin.math.abs
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Utility object for generating waveform amplitude data for audio bubbles.
 */
object WaveformUtils {
    private const val TAG = "WaveformUtils"
    private const val DEFAULT_BAR_COUNT = 28
    private const val MIN_AMPLITUDE = 0.15f
    private const val MAX_AMPLITUDE = 1.0f

    fun generateDeterministicWaveform(key: String, barCount: Int = DEFAULT_BAR_COUNT): List<Float> {
        val seed = key.hashCode().toLong()
        val random = Random(seed)
        return List(barCount) { index ->
            val baseAmplitude = 0.3f + random.nextFloat() * 0.7f
            val variation = sin(index * 0.5f) * 0.2f
            (baseAmplitude + variation).coerceIn(MIN_AMPLITUDE, MAX_AMPLITUDE)
        }
    }

    fun generatePlaceholder(barCount: Int = DEFAULT_BAR_COUNT): List<Float> {
        return List(barCount) { MIN_AMPLITUDE + Random.nextFloat() * 0.7f }
    }

    fun normalizeToBarCount(amplitudes: List<Float>, targetCount: Int): List<Float> {
        if (amplitudes.size == targetCount) return amplitudes
        if (amplitudes.isEmpty()) return generatePlaceholder(targetCount)
        return List(targetCount) { i ->
            val sourceIndex = i.toFloat() * amplitudes.size / targetCount
            val lowerIndex = sourceIndex.toInt().coerceIn(0, amplitudes.size - 1)
            val upperIndex = (lowerIndex + 1).coerceIn(0, amplitudes.size - 1)
            val fraction = sourceIndex - lowerIndex
            val value = amplitudes[lowerIndex] * (1 - fraction) + amplitudes[upperIndex] * fraction
            value.coerceIn(MIN_AMPLITUDE, MAX_AMPLITUDE)
        }
    }
}
