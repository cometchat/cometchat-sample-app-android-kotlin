package com.cometchat.uikit.compose.presentation.messagelist.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * Unit tests for MessageListHelpers functions.
 *
 * **Validates: Requirements 1.2, 1.7, 8.3, 8.4**
 *
 * Tests:
 * - formatDateSeparator returns correct values for today, yesterday, older dates
 * - Date formatting is consistent
 * - getDateId returns correct YYYYMMDD format for various timestamps
 */
class MessageListHelpersTest {

    // ========================================
    // formatDateSeparator Tests
    // ========================================

    @Test
    fun `formatDateSeparator returns Today for current timestamp`() {
        val now = System.currentTimeMillis() / 1000

        val result = formatDateSeparator(now)

        assertEquals("Today", result)
    }

    @Test
    fun `formatDateSeparator returns Yesterday for yesterday timestamp`() {
        val yesterday = (System.currentTimeMillis() / 1000) - (24 * 3600)

        val result = formatDateSeparator(yesterday)

        assertEquals("Yesterday", result)
    }

    @Test
    fun `formatDateSeparator returns formatted date for two days ago`() {
        val twoDaysAgo = (System.currentTimeMillis() / 1000) - (2 * 24 * 3600)

        val result = formatDateSeparator(twoDaysAgo)

        // Should not be "Today" or "Yesterday"
        assertTrue(result != "Today")
        assertTrue(result != "Yesterday")
        // Should contain year
        assertTrue(result.contains(","))
    }

    @Test
    fun `formatDateSeparator returns formatted date for specific date`() {
        // Set a specific date: March 20, 2023
        val calendar = Calendar.getInstance()
        calendar.set(2023, Calendar.MARCH, 20, 12, 0, 0)
        val timestamp = calendar.timeInMillis / 1000

        val result = formatDateSeparator(timestamp)

        assertTrue(result.contains("March"))
        assertTrue(result.contains("20"))
        assertTrue(result.contains("2023"))
    }

    @Test
    fun `formatDateSeparator handles midnight boundary correctly`() {
        // Get today at 00:01
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 1)
        calendar.set(Calendar.SECOND, 0)
        val earlyToday = calendar.timeInMillis / 1000

        val result = formatDateSeparator(earlyToday)

        assertEquals("Today", result)
    }

    @Test
    fun `formatDateSeparator handles late night correctly`() {
        // Get today at 23:59
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 0)
        val lateToday = calendar.timeInMillis / 1000

        val result = formatDateSeparator(lateToday)

        assertEquals("Today", result)
    }

    @Test
    fun `formatDateSeparator handles year boundary correctly`() {
        // Set to January 1, 2022
        val calendar = Calendar.getInstance()
        calendar.set(2022, Calendar.JANUARY, 1, 12, 0, 0)
        val timestamp = calendar.timeInMillis / 1000

        val result = formatDateSeparator(timestamp)

        assertTrue(result.contains("January"))
        assertTrue(result.contains("1"))
        assertTrue(result.contains("2022"))
    }

    @Test
    fun `formatDateSeparator handles different months correctly`() {
        val calendar = Calendar.getInstance()
        
        // Test December
        calendar.set(2023, Calendar.DECEMBER, 25, 12, 0, 0)
        val december = formatDateSeparator(calendar.timeInMillis / 1000)
        assertTrue(december.contains("December"))
        
        // Test June
        calendar.set(2023, Calendar.JUNE, 15, 12, 0, 0)
        val june = formatDateSeparator(calendar.timeInMillis / 1000)
        assertTrue(june.contains("June"))
    }

    // ========================================
    // Edge Cases
    // ========================================

    @Test
    fun `formatDateSeparator handles zero timestamp`() {
        // Unix epoch: January 1, 1970
        val result = formatDateSeparator(0)

        assertTrue(result.contains("January"))
        assertTrue(result.contains("1970"))
    }

    @Test
    fun `formatDateSeparator handles very old timestamp`() {
        // January 1, 2000
        val calendar = Calendar.getInstance()
        calendar.set(2000, Calendar.JANUARY, 1, 12, 0, 0)
        val timestamp = calendar.timeInMillis / 1000

        val result = formatDateSeparator(timestamp)

        assertTrue(result.contains("January"))
        assertTrue(result.contains("2000"))
    }

    // ========================================
    // getDateId Tests
    // ========================================

    @Test
    fun `getDateId returns correct YYYYMMDD format for known date`() {
        // December 25, 2024 at 12:00:00
        val calendar = Calendar.getInstance(Locale.ENGLISH).apply {
            timeZone = TimeZone.getDefault()
            set(2024, Calendar.DECEMBER, 25, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val timestamp = calendar.timeInMillis / 1000

        val result = getDateId(timestamp)

        assertEquals(20241225L, result)
    }

    @Test
    fun `getDateId returns correct format for January date`() {
        // January 1, 2024
        val calendar = Calendar.getInstance(Locale.ENGLISH).apply {
            timeZone = TimeZone.getDefault()
            set(2024, Calendar.JANUARY, 1, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val timestamp = calendar.timeInMillis / 1000

        val result = getDateId(timestamp)

        assertEquals(20240101L, result)
    }

    @Test
    fun `getDateId handles midnight boundary - start of day`() {
        // March 15, 2024 at 00:00:00 (midnight start)
        val calendar = Calendar.getInstance(Locale.ENGLISH).apply {
            timeZone = TimeZone.getDefault()
            set(2024, Calendar.MARCH, 15, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val timestamp = calendar.timeInMillis / 1000

        val result = getDateId(timestamp)

        assertEquals(20240315L, result)
    }

    @Test
    fun `getDateId handles end of day - 23 59 59`() {
        // March 15, 2024 at 23:59:59
        val calendar = Calendar.getInstance(Locale.ENGLISH).apply {
            timeZone = TimeZone.getDefault()
            set(2024, Calendar.MARCH, 15, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val timestamp = calendar.timeInMillis / 1000

        val result = getDateId(timestamp)

        assertEquals(20240315L, result)
    }

    @Test
    fun `getDateId handles year boundary - December 31 to January 1`() {
        // December 31, 2023 at 23:59:59
        val dec31Calendar = Calendar.getInstance(Locale.ENGLISH).apply {
            timeZone = TimeZone.getDefault()
            set(2023, Calendar.DECEMBER, 31, 23, 59, 59)
            set(Calendar.MILLISECOND, 0)
        }
        val dec31Timestamp = dec31Calendar.timeInMillis / 1000

        // January 1, 2024 at 00:00:00
        val jan1Calendar = Calendar.getInstance(Locale.ENGLISH).apply {
            timeZone = TimeZone.getDefault()
            set(2024, Calendar.JANUARY, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val jan1Timestamp = jan1Calendar.timeInMillis / 1000

        val dec31Result = getDateId(dec31Timestamp)
        val jan1Result = getDateId(jan1Timestamp)

        assertEquals(20231231L, dec31Result)
        assertEquals(20240101L, jan1Result)
        assertTrue("Year boundary should produce different date IDs", dec31Result != jan1Result)
    }

    @Test
    fun `getDateId handles month boundary`() {
        // January 31, 2024
        val jan31Calendar = Calendar.getInstance(Locale.ENGLISH).apply {
            timeZone = TimeZone.getDefault()
            set(2024, Calendar.JANUARY, 31, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val jan31Timestamp = jan31Calendar.timeInMillis / 1000

        // February 1, 2024
        val feb1Calendar = Calendar.getInstance(Locale.ENGLISH).apply {
            timeZone = TimeZone.getDefault()
            set(2024, Calendar.FEBRUARY, 1, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val feb1Timestamp = feb1Calendar.timeInMillis / 1000

        val jan31Result = getDateId(jan31Timestamp)
        val feb1Result = getDateId(feb1Timestamp)

        assertEquals(20240131L, jan31Result)
        assertEquals(20240201L, feb1Result)
        assertTrue("Month boundary should produce different date IDs", jan31Result != feb1Result)
    }

    @Test
    fun `getDateId handles leap year date - February 29`() {
        // February 29, 2024 (2024 is a leap year)
        val calendar = Calendar.getInstance(Locale.ENGLISH).apply {
            timeZone = TimeZone.getDefault()
            set(2024, Calendar.FEBRUARY, 29, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val timestamp = calendar.timeInMillis / 1000

        val result = getDateId(timestamp)

        assertEquals(20240229L, result)
    }

    @Test
    fun `getDateId handles different years`() {
        // Test multiple years
        val testCases = listOf(
            Triple(2020, Calendar.JUNE, 15) to 20200615L,
            Triple(2021, Calendar.SEPTEMBER, 30) to 20210930L,
            Triple(2022, Calendar.APRIL, 1) to 20220401L,
            Triple(2023, Calendar.NOVEMBER, 11) to 20231111L,
            Triple(2025, Calendar.AUGUST, 20) to 20250820L
        )

        testCases.forEach { (dateTriple, expectedId) ->
            val (year, month, day) = dateTriple
            val calendar = Calendar.getInstance(Locale.ENGLISH).apply {
                timeZone = TimeZone.getDefault()
                set(year, month, day, 12, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val timestamp = calendar.timeInMillis / 1000

            val result = getDateId(timestamp)

            assertEquals("Year $year, month $month, day $day should return $expectedId", expectedId, result)
        }
    }

    @Test
    fun `getDateId handles single digit month and day with proper padding`() {
        // May 5, 2024 (single digit month and day)
        val calendar = Calendar.getInstance(Locale.ENGLISH).apply {
            timeZone = TimeZone.getDefault()
            set(2024, Calendar.MAY, 5, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val timestamp = calendar.timeInMillis / 1000

        val result = getDateId(timestamp)

        // Should be 20240505, not 2024055 or 202455
        assertEquals(20240505L, result)
    }

    @Test
    fun `getDateId handles double digit month with single digit day`() {
        // October 3, 2024
        val calendar = Calendar.getInstance(Locale.ENGLISH).apply {
            timeZone = TimeZone.getDefault()
            set(2024, Calendar.OCTOBER, 3, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val timestamp = calendar.timeInMillis / 1000

        val result = getDateId(timestamp)

        assertEquals(20241003L, result)
    }

    @Test
    fun `getDateId handles single digit month with double digit day`() {
        // March 25, 2024
        val calendar = Calendar.getInstance(Locale.ENGLISH).apply {
            timeZone = TimeZone.getDefault()
            set(2024, Calendar.MARCH, 25, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val timestamp = calendar.timeInMillis / 1000

        val result = getDateId(timestamp)

        assertEquals(20240325L, result)
    }

    @Test
    fun `getDateId handles Unix epoch`() {
        // Unix epoch: January 1, 1970 00:00:00 UTC
        // Note: Result depends on local timezone
        val result = getDateId(0)

        // The result should be a valid date ID in YYYYMMDD format
        // For UTC, this would be 19700101
        assertTrue("Date ID should be positive", result > 0)
        assertTrue("Date ID should be in valid range", result >= 19700101L)
    }

    @Test
    fun `getDateId returns same value for same day different times`() {
        // Morning: 8:00 AM
        val morningCalendar = Calendar.getInstance(Locale.ENGLISH).apply {
            timeZone = TimeZone.getDefault()
            set(2024, Calendar.JULY, 4, 8, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val morningTimestamp = morningCalendar.timeInMillis / 1000

        // Afternoon: 3:30 PM
        val afternoonCalendar = Calendar.getInstance(Locale.ENGLISH).apply {
            timeZone = TimeZone.getDefault()
            set(2024, Calendar.JULY, 4, 15, 30, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val afternoonTimestamp = afternoonCalendar.timeInMillis / 1000

        // Evening: 9:45 PM
        val eveningCalendar = Calendar.getInstance(Locale.ENGLISH).apply {
            timeZone = TimeZone.getDefault()
            set(2024, Calendar.JULY, 4, 21, 45, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val eveningTimestamp = eveningCalendar.timeInMillis / 1000

        val morningResult = getDateId(morningTimestamp)
        val afternoonResult = getDateId(afternoonTimestamp)
        val eveningResult = getDateId(eveningTimestamp)

        assertEquals("All times on same day should return same date ID", morningResult, afternoonResult)
        assertEquals("All times on same day should return same date ID", afternoonResult, eveningResult)
        assertEquals(20240704L, morningResult)
    }

    @Test
    fun `getDateId returns different values for consecutive days`() {
        // July 4, 2024
        val day1Calendar = Calendar.getInstance(Locale.ENGLISH).apply {
            timeZone = TimeZone.getDefault()
            set(2024, Calendar.JULY, 4, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val day1Timestamp = day1Calendar.timeInMillis / 1000

        // July 5, 2024
        val day2Calendar = Calendar.getInstance(Locale.ENGLISH).apply {
            timeZone = TimeZone.getDefault()
            set(2024, Calendar.JULY, 5, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val day2Timestamp = day2Calendar.timeInMillis / 1000

        val day1Result = getDateId(day1Timestamp)
        val day2Result = getDateId(day2Timestamp)

        assertEquals(20240704L, day1Result)
        assertEquals(20240705L, day2Result)
        assertTrue("Consecutive days should have different date IDs", day1Result != day2Result)
    }
}
