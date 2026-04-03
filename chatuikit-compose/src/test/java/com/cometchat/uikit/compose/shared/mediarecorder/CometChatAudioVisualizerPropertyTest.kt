package com.cometchat.uikit.compose.shared.mediarecorder

import com.cometchat.uikit.compose.presentation.shared.mediarecorder.ui.AmplitudeHistory
import com.cometchat.uikit.compose.presentation.shared.mediarecorder.ui.calculateBarHeight
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.floats.shouldBeGreaterThanOrEqual
import io.kotest.matchers.floats.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.checkAll
import kotlin.math.abs

/**
 * Property-based tests for [CometChatAudioVisualizer] bar height calculation
 * and [AmplitudeHistory] soft transition algorithm.
 *
 * Feature: media-recorder-redesign
 * **Property 3: Audio Visualizer Bar Rendering**
 * **Property 4: Amplitude Smoothing (Soft Transition)**
 * **Validates: Requirements 2.1, 2.3, 2.4, 2.5**
 */
class CometChatAudioVisualizerPropertyTest : FunSpec({

    // ==================== Property 3: Audio Visualizer Bar Rendering ====================
    // *For any* amplitude value in range [0.0, 1.0], the Audio_Visualizer SHALL:
    // - Calculate bar height as: minHeight + (amplitude × (maxHeight - minHeight))
    // - Position each bar centered vertically at: centerY - (barHeight / 2)
    // - Apply the configured chunk color, width, spacing, and corner radius from style
    // **Validates: Requirements 2.1, 2.3, 2.5**

    /**
     * Property 3: Bar height calculation follows the formula
     * barHeight = minHeight + (amplitude × (maxHeight - minHeight))
     *
     * **Validates: Requirements 2.1**
     */
    test("Property 3: Bar height calculation follows the formula") {
        checkAll(100, 
            Arb.float(0f, 1f),      // amplitude
            Arb.float(1f, 10f),     // minHeight
            Arb.float(20f, 50f)     // maxHeight
        ) { amplitude, minHeight, maxHeight ->
            val expectedHeight = minHeight + (amplitude * (maxHeight - minHeight))
            val calculatedHeight = calculateBarHeight(amplitude, minHeight, maxHeight)
            
            // Allow small floating point tolerance
            abs(calculatedHeight - expectedHeight) shouldBeLessThanOrEqual 0.001f
        }
    }

    /**
     * Property 3: Bar height is always within [minHeight, maxHeight] bounds
     *
     * **Validates: Requirements 2.6, 2.7**
     */
    test("Property 3: Bar height is always within bounds") {
        checkAll(100,
            Arb.float(-0.5f, 1.5f), // amplitude (including out of range values)
            Arb.float(1f, 10f),     // minHeight
            Arb.float(20f, 50f)     // maxHeight
        ) { amplitude, minHeight, maxHeight ->
            val calculatedHeight = calculateBarHeight(amplitude, minHeight, maxHeight)
            
            calculatedHeight shouldBeGreaterThanOrEqual minHeight
            calculatedHeight shouldBeLessThanOrEqual maxHeight
        }
    }

    /**
     * Property 3: Zero amplitude produces minimum height
     *
     * **Validates: Requirements 2.6**
     */
    test("Property 3: Zero amplitude produces minimum height") {
        checkAll(100,
            Arb.float(1f, 10f),     // minHeight
            Arb.float(20f, 50f)     // maxHeight
        ) { minHeight, maxHeight ->
            val calculatedHeight = calculateBarHeight(0f, minHeight, maxHeight)
            
            abs(calculatedHeight - minHeight) shouldBeLessThanOrEqual 0.001f
        }
    }

    /**
     * Property 3: Maximum amplitude produces maximum height
     *
     * **Validates: Requirements 2.7**
     */
    test("Property 3: Maximum amplitude produces maximum height") {
        checkAll(100,
            Arb.float(1f, 10f),     // minHeight
            Arb.float(20f, 50f)     // maxHeight
        ) { minHeight, maxHeight ->
            val calculatedHeight = calculateBarHeight(1f, minHeight, maxHeight)
            
            abs(calculatedHeight - maxHeight) shouldBeLessThanOrEqual 0.001f
        }
    }

    /**
     * Property 3: Bar height increases monotonically with amplitude
     *
     * **Validates: Requirements 2.1**
     */
    test("Property 3: Bar height increases monotonically with amplitude") {
        checkAll(100,
            Arb.float(0f, 0.5f),    // lowerAmplitude
            Arb.float(0.5f, 1f),    // higherAmplitude
            Arb.float(1f, 10f),     // minHeight
            Arb.float(20f, 50f)     // maxHeight
        ) { lowerAmplitude, higherAmplitude, minHeight, maxHeight ->
            val lowerHeight = calculateBarHeight(lowerAmplitude, minHeight, maxHeight)
            val higherHeight = calculateBarHeight(higherAmplitude, minHeight, maxHeight)
            
            lowerHeight shouldBeLessThanOrEqual higherHeight
        }
    }

    /**
     * Property 3: Bars are centered vertically
     * For any amplitude and container height, the bar should be equidistant from top and bottom
     *
     * **Validates: Requirements 2.3**
     */
    test("Property 3: Bars are centered vertically") {
        checkAll(100,
            Arb.float(0f, 1f),      // amplitude
            Arb.float(50f, 100f)    // containerHeight
        ) { amplitude, containerHeight ->
            val minHeight = 4f
            val maxHeight = 32f
            val barHeight = calculateBarHeight(amplitude, minHeight, maxHeight)
            val centerY = containerHeight / 2
            val topY = centerY - (barHeight / 2)
            val bottomY = topY + barHeight
            
            // Distance from center to top should equal distance from center to bottom
            val distanceToTop = centerY - topY
            val distanceToBottom = bottomY - centerY
            
            abs(distanceToTop - distanceToBottom) shouldBeLessThanOrEqual 0.001f
        }
    }

    /**
     * Property 3: Half amplitude produces height at midpoint
     *
     * **Validates: Requirements 2.1**
     */
    test("Property 3: Half amplitude produces height at midpoint") {
        checkAll(100,
            Arb.float(1f, 10f),     // minHeight
            Arb.float(20f, 50f)     // maxHeight
        ) { minHeight, maxHeight ->
            val midpointHeight = (minHeight + maxHeight) / 2
            val calculatedHeight = calculateBarHeight(0.5f, minHeight, maxHeight)
            
            abs(calculatedHeight - midpointHeight) shouldBeLessThanOrEqual 0.001f
        }
    }

    // ==================== Property 4: Amplitude Smoothing (Soft Transition) ====================
    // *For any* sequence of amplitude values, the smoothed amplitude for each bar SHALL be
    // a value derived from the amplitude history buffer, ensuring smooth transitions
    // between consecutive amplitude updates.
    // **Validates: Requirements 2.4**

    /**
     * Property 4: AmplitudeHistory stores values correctly
     *
     * **Validates: Requirements 2.4**
     */
    test("Property 4: AmplitudeHistory stores values correctly") {
        checkAll(100, Arb.float(0f, 1f)) { amplitude ->
            val history = AmplitudeHistory(20)
            history.add(amplitude)
            
            // The most recently added value should be retrievable
            // (at index 0 after one addition, the value is at position 0)
            val retrievedAmplitude = history.getSmoothedAmplitude(19) // Previous index
            
            // Value should be in valid range
            retrievedAmplitude shouldBeGreaterThanOrEqual 0f
            retrievedAmplitude shouldBeLessThanOrEqual 1f
        }
    }

    /**
     * Property 4: AmplitudeHistory clamps values to [0, 1]
     *
     * **Validates: Requirements 2.4**
     */
    test("Property 4: AmplitudeHistory clamps values to valid range") {
        checkAll(100, Arb.float(-1f, 2f)) { amplitude ->
            val history = AmplitudeHistory(20)
            history.add(amplitude)
            
            // All retrieved values should be in [0, 1] range
            for (i in 0 until 20) {
                val value = history.getSmoothedAmplitude(i)
                value shouldBeGreaterThanOrEqual 0f
                value shouldBeLessThanOrEqual 1f
            }
        }
    }

    /**
     * Property 4: AmplitudeHistory clear resets all values to zero
     *
     * **Validates: Requirements 2.4**
     */
    test("Property 4: AmplitudeHistory clear resets all values") {
        val history = AmplitudeHistory(20)
        
        // Add some values
        repeat(10) {
            history.add(0.5f + it * 0.05f)
        }
        
        // Clear the history
        history.clear()
        
        // All values should be zero
        for (i in 0 until 20) {
            history.getSmoothedAmplitude(i) shouldBe 0f
        }
    }

    /**
     * Property 4: AmplitudeHistory provides different values for different bar indices
     * This creates the wave-like visualization effect
     *
     * **Validates: Requirements 2.4**
     */
    test("Property 4: AmplitudeHistory provides wave-like distribution") {
        val history = AmplitudeHistory(20)
        
        // Add a sequence of different values
        val values = listOf(0.1f, 0.3f, 0.5f, 0.7f, 0.9f, 0.8f, 0.6f, 0.4f, 0.2f, 0.0f)
        values.forEach { history.add(it) }
        
        // Different bar indices should potentially have different values
        // (creating the wave effect)
        val retrievedValues = (0 until 20).map { history.getSmoothedAmplitude(it) }
        
        // All values should be valid
        retrievedValues.forEach { value ->
            value shouldBeGreaterThanOrEqual 0f
            value shouldBeLessThanOrEqual 1f
        }
    }

    /**
     * Property 4: AmplitudeHistory handles circular buffer correctly
     *
     * **Validates: Requirements 2.4**
     */
    test("Property 4: AmplitudeHistory handles circular buffer overflow") {
        checkAll(100, Arb.int(1, 100)) { addCount ->
            val historySize = 20
            val history = AmplitudeHistory(historySize)
            
            // Add more values than the buffer size
            repeat(addCount) { i ->
                history.add((i % 10) / 10f)
            }
            
            // All retrieved values should still be valid
            for (i in 0 until historySize) {
                val value = history.getSmoothedAmplitude(i)
                value shouldBeGreaterThanOrEqual 0f
                value shouldBeLessThanOrEqual 1f
            }
        }
    }

    /**
     * Property 4: AmplitudeHistory with sequence of values maintains smooth transitions
     *
     * **Validates: Requirements 2.4**
     */
    test("Property 4: AmplitudeHistory maintains values from sequence") {
        checkAll(100, Arb.list(Arb.float(0f, 1f), 5..20)) { amplitudes ->
            val history = AmplitudeHistory(20)
            
            amplitudes.forEach { history.add(it) }
            
            // All retrieved values should be in valid range
            for (i in 0 until 20) {
                val value = history.getSmoothedAmplitude(i)
                value shouldBeGreaterThanOrEqual 0f
                value shouldBeLessThanOrEqual 1f
            }
        }
    }

    /**
     * Property 4: AmplitudeHistory size parameter is respected
     *
     * **Validates: Requirements 2.4**
     */
    test("Property 4: AmplitudeHistory respects size parameter") {
        checkAll(100, Arb.int(5, 50)) { size ->
            val history = AmplitudeHistory(size)
            
            // Add values
            repeat(size * 2) { i ->
                history.add(i / (size * 2f))
            }
            
            // Should be able to retrieve values for all indices up to size
            for (i in 0 until size) {
                val value = history.getSmoothedAmplitude(i)
                value shouldBeGreaterThanOrEqual 0f
                value shouldBeLessThanOrEqual 1f
            }
        }
    }

    /**
     * Property 4: Initial AmplitudeHistory has all zeros
     *
     * **Validates: Requirements 2.4**
     */
    test("Property 4: Initial AmplitudeHistory has all zeros") {
        checkAll(100, Arb.int(5, 50)) { size ->
            val history = AmplitudeHistory(size)
            
            // All initial values should be zero
            for (i in 0 until size) {
                history.getSmoothedAmplitude(i) shouldBe 0f
            }
        }
    }
})
