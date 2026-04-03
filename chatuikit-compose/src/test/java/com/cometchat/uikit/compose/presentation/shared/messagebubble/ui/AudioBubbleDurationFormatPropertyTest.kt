package com.cometchat.uikit.compose.presentation.shared.messagebubble.ui

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll

/**
 * Property-based tests for Audio Bubble Duration Format.
 *
 * Feature: video-audio-bubbles
 * Properties tested:
 * - Property 6: Audio Duration Format
 *
 * **Validates: Requirements 5.2**
 *
 * Tests the [formatDuration] function which formats audio playback position and total
 * duration in milliseconds to "MM:SS/MM:SS" format where:
 * - First MM:SS = floor(P / 60000) : floor((P % 60000) / 1000)
 * - Second MM:SS = floor(D / 60000) : floor((D % 60000) / 1000)
 */
class AudioBubbleDurationFormatPropertyTest : StringSpec({

    /**
     * Property 6: Audio Duration Format
     *
     * *For any* audio playback position P (in milliseconds) and total duration D (in milliseconds),
     * the displayed duration string SHALL match the format "MM:SS/MM:SS" where:
     * - First MM:SS = floor(P / 60000) : floor((P % 60000) / 1000)
     * - Second MM:SS = floor(D / 60000) : floor((D % 60000) / 1000)
     *
     * **Validates: Requirements 5.2**
     */
    "Property 6: Duration format should match MM:SS/MM:SS pattern for all valid inputs" {
        // Test with arbitrary millisecond values (0 to 1 hour in milliseconds)
        val positionArb = Arb.long(0L..3600000L)
        val durationArb = Arb.long(0L..3600000L)

        checkAll(200, positionArb, durationArb) { position, duration ->
            val formatted = formatDuration(position, duration)
            
            // Verify format matches "MM:SS/MM:SS" pattern
            formatted shouldMatch Regex("\\d{2}:\\d{2}/\\d{2}:\\d{2}")
        }
    }

    "Property 6: Duration format should correctly calculate minutes from milliseconds" {
        // Test with arbitrary millisecond values
        val positionArb = Arb.long(0L..3600000L)
        val durationArb = Arb.long(0L..3600000L)

        checkAll(200, positionArb, durationArb) { position, duration ->
            val formatted = formatDuration(position, duration)
            val parts = formatted.split("/")
            
            // Extract minutes from formatted string
            val positionMinutes = parts[0].split(":")[0].toInt()
            val durationMinutes = parts[1].split(":")[0].toInt()
            
            // Calculate expected minutes using floor(ms / 60000) % 60
            val expectedPositionMinutes = ((position / (1000 * 60)) % 60).toInt()
            val expectedDurationMinutes = ((duration / (1000 * 60)) % 60).toInt()
            
            positionMinutes shouldBe expectedPositionMinutes
            durationMinutes shouldBe expectedDurationMinutes
        }
    }

    "Property 6: Duration format should correctly calculate seconds from milliseconds" {
        // Test with arbitrary millisecond values
        val positionArb = Arb.long(0L..3600000L)
        val durationArb = Arb.long(0L..3600000L)

        checkAll(200, positionArb, durationArb) { position, duration ->
            val formatted = formatDuration(position, duration)
            val parts = formatted.split("/")
            
            // Extract seconds from formatted string
            val positionSeconds = parts[0].split(":")[1].toInt()
            val durationSeconds = parts[1].split(":")[1].toInt()
            
            // Calculate expected seconds using floor((ms / 1000) % 60)
            val expectedPositionSeconds = ((position / 1000) % 60).toInt()
            val expectedDurationSeconds = ((duration / 1000) % 60).toInt()
            
            positionSeconds shouldBe expectedPositionSeconds
            durationSeconds shouldBe expectedDurationSeconds
        }
    }

    "Property 6: Duration format should zero-pad minutes and seconds" {
        // Test with values that should produce single-digit minutes/seconds
        val smallValueArb = Arb.long(0L..9999L) // 0-9 seconds

        checkAll(100, smallValueArb, smallValueArb) { position, duration ->
            val formatted = formatDuration(position, duration)
            val parts = formatted.split("/")
            
            // Each part should be exactly 5 characters (MM:SS)
            parts[0].length shouldBe 5
            parts[1].length shouldBe 5
            
            // Minutes and seconds should be 2 digits each
            parts[0].split(":")[0].length shouldBe 2
            parts[0].split(":")[1].length shouldBe 2
            parts[1].split(":")[0].length shouldBe 2
            parts[1].split(":")[1].length shouldBe 2
        }
    }

    "Property 6: Duration format should handle zero values correctly" {
        val formatted = formatDuration(0L, 0L)
        
        formatted shouldBe "00:00/00:00"
    }

    "Property 6: Duration format should handle exact minute boundaries" {
        // Test exact minute values (60000ms = 1 minute)
        val formatted1Min = formatDuration(60000L, 120000L)
        formatted1Min shouldBe "01:00/02:00"
        
        val formatted5Min = formatDuration(300000L, 600000L)
        formatted5Min shouldBe "05:00/10:00"
    }

    "Property 6: Duration format should handle exact second boundaries" {
        // Test exact second values (1000ms = 1 second)
        val formatted1Sec = formatDuration(1000L, 2000L)
        formatted1Sec shouldBe "00:01/00:02"
        
        val formatted30Sec = formatDuration(30000L, 45000L)
        formatted30Sec shouldBe "00:30/00:45"
    }

    "Property 6: Duration format should handle mixed minute and second values" {
        // Test values with both minutes and seconds
        val formatted = formatDuration(90000L, 150000L) // 1:30 / 2:30
        formatted shouldBe "01:30/02:30"
        
        val formatted2 = formatDuration(125000L, 305000L) // 2:05 / 5:05
        formatted2 shouldBe "02:05/05:05"
    }

    "Property 6: Duration format should handle maximum 59 minutes and 59 seconds" {
        // Test maximum values within the hour (59:59)
        val maxMinutesSeconds = (59 * 60 * 1000L) + (59 * 1000L) // 59:59 in ms
        val formatted = formatDuration(maxMinutesSeconds, maxMinutesSeconds)
        
        formatted shouldBe "59:59/59:59"
    }

    "Property 6: Duration format should wrap around at 60 minutes" {
        // Test that minutes wrap around at 60 (since we use % 60)
        val oneHour = 60 * 60 * 1000L // 60 minutes in ms
        val formatted = formatDuration(oneHour, oneHour)
        
        // 60 minutes % 60 = 0 minutes
        formatted shouldBe "00:00/00:00"
    }

    "Property 6: Duration format should handle position greater than duration" {
        // The function should still format correctly even if position > duration
        // (this can happen during edge cases in playback)
        val positionArb = Arb.long(0L..3600000L)
        val durationArb = Arb.long(0L..3600000L)

        checkAll(100, positionArb, durationArb) { position, duration ->
            // Regardless of which is larger, format should still be valid
            val formatted = formatDuration(position, duration)
            formatted shouldMatch Regex("\\d{2}:\\d{2}/\\d{2}:\\d{2}")
        }
    }

    "Property 6: Duration format should produce consistent results for same inputs" {
        // Idempotency property - same inputs should always produce same output
        val positionArb = Arb.long(0L..3600000L)
        val durationArb = Arb.long(0L..3600000L)

        checkAll(100, positionArb, durationArb) { position, duration ->
            val formatted1 = formatDuration(position, duration)
            val formatted2 = formatDuration(position, duration)
            
            formatted1 shouldBe formatted2
        }
    }

    "Property 6: Duration format should correctly separate position and duration with slash" {
        val positionArb = Arb.long(0L..3600000L)
        val durationArb = Arb.long(0L..3600000L)

        checkAll(100, positionArb, durationArb) { position, duration ->
            val formatted = formatDuration(position, duration)
            
            // Should contain exactly one slash
            formatted.count { it == '/' } shouldBe 1
            
            // Should split into exactly 2 parts
            formatted.split("/").size shouldBe 2
        }
    }

    "Property 6: Duration format should handle typical audio durations" {
        // Test common audio message durations (voice messages typically 1-60 seconds)
        val typicalDurationArb = Arb.long(1000L..60000L) // 1-60 seconds
        val typicalPositionArb = Arb.long(0L..60000L)

        checkAll(100, typicalPositionArb, typicalDurationArb) { position, duration ->
            val formatted = formatDuration(position, duration)
            
            // Should match format
            formatted shouldMatch Regex("\\d{2}:\\d{2}/\\d{2}:\\d{2}")
            
            // For typical audio, minutes should be 0 or 1
            val parts = formatted.split("/")
            val positionMinutes = parts[0].split(":")[0].toInt()
            val durationMinutes = parts[1].split(":")[0].toInt()
            
            (positionMinutes in 0..1) shouldBe true
            (durationMinutes in 0..1) shouldBe true
        }
    }
})
