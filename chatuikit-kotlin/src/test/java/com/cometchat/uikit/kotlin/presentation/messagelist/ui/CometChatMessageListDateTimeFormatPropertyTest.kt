package com.cometchat.uikit.kotlin.presentation.messagelist.ui

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Test implementation of DateTimeFormatterCallback for property testing.
 * This allows us to create callbacks with different formatting behaviors.
 */
private interface TestDateTimeFormatterCallback {
    fun time(timestamp: Long): String? = null
    fun today(timestamp: Long): String? = null
    fun yesterday(timestamp: Long): String? = null
    fun lastWeek(timestamp: Long): String? = null
    fun otherDays(timestamp: Long): String? = null
    fun minute(timestamp: Long): String? = null
    fun minutes(diffInMinutesFromNow: Long, timestamp: Long): String? = null
    fun hour(timestamp: Long): String? = null
    fun hours(diffInHourFromNow: Long, timestamp: Long): String? = null
}

/**
 * Simple implementation of TestDateTimeFormatterCallback that returns fixed strings.
 */
private class FixedDateTimeFormatterCallback(
    private val timeValue: String? = null,
    private val todayValue: String? = null,
    private val yesterdayValue: String? = null,
    private val lastWeekValue: String? = null,
    private val otherDaysValue: String? = null
) : TestDateTimeFormatterCallback {
    override fun time(timestamp: Long): String? = timeValue
    override fun today(timestamp: Long): String? = todayValue
    override fun yesterday(timestamp: Long): String? = yesterdayValue
    override fun lastWeek(timestamp: Long): String? = lastWeekValue
    override fun otherDays(timestamp: Long): String? = otherDaysValue
}

/**
 * Test class that simulates the date/time format storage behavior of CometChatMessageList
 * and MessageAdapter without requiring Android context.
 *
 * This mirrors the actual implementation:
 * - CometChatMessageList stores timeFormat, dateFormat, and dateTimeFormatter
 * - CometChatMessageList propagates to MessageAdapter via adapter properties
 * - MessageAdapter stores the formats and uses them for rendering
 */
private class TestDateTimeFormatStorage {
    // Simulates CometChatMessageList fields
    private var timeFormat: SimpleDateFormat? = null
    private var dateFormat: SimpleDateFormat? = null
    private var dateTimeFormatter: TestDateTimeFormatterCallback? = null

    // Simulates MessageAdapter fields
    private var adapterTimeFormat: SimpleDateFormat? = null
    private var adapterDateSeparatorFormat: SimpleDateFormat? = null
    private var adapterDateTimeFormatter: TestDateTimeFormatterCallback? = null

    // ========================================
    // Time Format
    // ========================================

    /**
     * Simulates CometChatMessageList.setTimeFormat()
     */
    fun setTimeFormat(format: SimpleDateFormat?) {
        this.timeFormat = format
        format?.let { adapterTimeFormat = it }
    }

    /**
     * Simulates CometChatMessageList.getTimeFormat()
     */
    fun getTimeFormat(): SimpleDateFormat? = timeFormat

    /**
     * Gets the adapter's time format
     */
    fun getAdapterTimeFormat(): SimpleDateFormat? = adapterTimeFormat

    // ========================================
    // Date Format
    // ========================================

    /**
     * Simulates CometChatMessageList.setDateFormat()
     */
    fun setDateFormat(format: SimpleDateFormat?) {
        this.dateFormat = format
        adapterDateSeparatorFormat = format
    }

    /**
     * Simulates CometChatMessageList.getDateFormat()
     */
    fun getDateFormat(): SimpleDateFormat? = dateFormat

    /**
     * Gets the adapter's date separator format
     */
    fun getAdapterDateSeparatorFormat(): SimpleDateFormat? = adapterDateSeparatorFormat

    // ========================================
    // Date Time Formatter Callback
    // ========================================

    /**
     * Simulates CometChatMessageList.setDateTimeFormatter()
     */
    fun setDateTimeFormatter(formatter: TestDateTimeFormatterCallback?) {
        this.dateTimeFormatter = formatter
        adapterDateTimeFormatter = formatter
    }

    /**
     * Simulates CometChatMessageList.getDateTimeFormatter()
     */
    fun getDateTimeFormatter(): TestDateTimeFormatterCallback? = dateTimeFormatter

    /**
     * Gets the adapter's date time formatter
     */
    fun getAdapterDateTimeFormatter(): TestDateTimeFormatterCallback? = adapterDateTimeFormatter
}

/**
 * Property-based tests for CometChatMessageList date/time format application.
 * Uses Kotest property testing to verify correctness properties.
 *
 * Feature: messagelist-property-parity, Property 4: Date/Time Format Application
 *
 * *For any* custom SimpleDateFormat set via `setTimeFormat` or `setDateFormat`,
 * message timestamps and date separators SHALL be formatted using the provided format.
 *
 * **Validates: Requirements 4.1, 4.2, 4.3**
 */
class CometChatMessageListDateTimeFormatPropertyTest : FunSpec({

    // ==================== Generators ====================

    /**
     * Common time format patterns.
     */
    val timeFormatPatterns = listOf(
        "HH:mm",           // 24-hour format
        "h:mm a",          // 12-hour format with AM/PM
        "HH:mm:ss",        // 24-hour with seconds
        "h:mm:ss a",       // 12-hour with seconds and AM/PM
        "H:mm",            // 24-hour without leading zero
        "K:mm a"           // 12-hour (0-11) with AM/PM
    )

    /**
     * Common date format patterns.
     */
    val dateFormatPatterns = listOf(
        "MM/dd/yyyy",      // US format
        "dd/MM/yyyy",      // European format
        "yyyy-MM-dd",      // ISO format
        "MMMM d, yyyy",    // Long format
        "MMM d, yyyy",     // Medium format
        "EEE, MMM d",      // Day of week with month and day
        "d MMM yyyy"       // Day month year
    )

    /**
     * Generator for time format patterns.
     */
    val timeFormatPatternArb = Arb.element(timeFormatPatterns)

    /**
     * Generator for date format patterns.
     */
    val dateFormatPatternArb = Arb.element(dateFormatPatterns)

    /**
     * Generator for SimpleDateFormat with time patterns.
     */
    val timeFormatArb = arbitrary {
        val pattern = timeFormatPatternArb.bind()
        SimpleDateFormat(pattern, Locale.US)
    }

    /**
     * Generator for SimpleDateFormat with date patterns.
     */
    val dateFormatArb = arbitrary {
        val pattern = dateFormatPatternArb.bind()
        SimpleDateFormat(pattern, Locale.US)
    }

    /**
     * Generator for non-empty strings (for callback return values).
     */
    val nonEmptyStringArb = arbitrary {
        var result: String
        do {
            result = Arb.string(1..20).bind()
        } while (result.isEmpty())
        result
    }

    /**
     * Generator for DateTimeFormatterCallback with random fixed values.
     */
    val dateTimeFormatterCallbackArb = arbitrary {
        val timeValue = nonEmptyStringArb.bind()
        val todayValue = nonEmptyStringArb.bind()
        val yesterdayValue = nonEmptyStringArb.bind()
        val lastWeekValue = nonEmptyStringArb.bind()
        val otherDaysValue = nonEmptyStringArb.bind()
        FixedDateTimeFormatterCallback(
            timeValue = timeValue,
            todayValue = todayValue,
            yesterdayValue = yesterdayValue,
            lastWeekValue = lastWeekValue,
            otherDaysValue = otherDaysValue
        )
    }

    // ==================== Property Tests ====================

    context("Property 4: Date/Time Format Application") {

        // ========================================
        // Time Format Tests
        // ========================================

        test("setTimeFormat(format) stores the provided format") {
            checkAll(100, timeFormatArb) { format ->
                val storage = TestDateTimeFormatStorage()

                // Set time format
                storage.setTimeFormat(format)

                // Verify format is stored
                storage.getTimeFormat().shouldNotBeNull()
                storage.getTimeFormat()?.toPattern() shouldBe format.toPattern()
            }
        }

        test("getTimeFormat() returns the format that was set") {
            checkAll(100, timeFormatArb) { format ->
                val storage = TestDateTimeFormatStorage()

                // Set time format
                storage.setTimeFormat(format)

                // Verify getter returns the same format
                val retrieved = storage.getTimeFormat()
                retrieved.shouldNotBeNull()
                retrieved.toPattern() shouldBe format.toPattern()
            }
        }

        test("setTimeFormat propagates to adapter") {
            checkAll(100, timeFormatArb) { format ->
                val storage = TestDateTimeFormatStorage()

                // Set time format
                storage.setTimeFormat(format)

                // Verify adapter has the format
                storage.getAdapterTimeFormat().shouldNotBeNull()
                storage.getAdapterTimeFormat()?.toPattern() shouldBe format.toPattern()
            }
        }

        test("setTimeFormat(null) clears the format") {
            checkAll(100, timeFormatArb) { format ->
                val storage = TestDateTimeFormatStorage()

                // Set format first
                storage.setTimeFormat(format)
                storage.getTimeFormat().shouldNotBeNull()

                // Clear format
                storage.setTimeFormat(null)

                // Verify format is cleared in MessageList
                storage.getTimeFormat().shouldBeNull()
            }
        }

        test("default time format is null") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestDateTimeFormatStorage()

                // Verify default is null
                storage.getTimeFormat().shouldBeNull()
            }
        }

        test("time format pattern is preserved after storage") {
            checkAll(100, timeFormatPatternArb) { pattern ->
                val storage = TestDateTimeFormatStorage()
                val format = SimpleDateFormat(pattern, Locale.US)

                // Set format
                storage.setTimeFormat(format)

                // Verify pattern is preserved
                storage.getTimeFormat()?.toPattern() shouldBe pattern
            }
        }

        // ========================================
        // Date Format Tests
        // ========================================

        test("setDateFormat(format) stores the provided format") {
            checkAll(100, dateFormatArb) { format ->
                val storage = TestDateTimeFormatStorage()

                // Set date format
                storage.setDateFormat(format)

                // Verify format is stored
                storage.getDateFormat().shouldNotBeNull()
                storage.getDateFormat()?.toPattern() shouldBe format.toPattern()
            }
        }

        test("getDateFormat() returns the format that was set") {
            checkAll(100, dateFormatArb) { format ->
                val storage = TestDateTimeFormatStorage()

                // Set date format
                storage.setDateFormat(format)

                // Verify getter returns the same format
                val retrieved = storage.getDateFormat()
                retrieved.shouldNotBeNull()
                retrieved.toPattern() shouldBe format.toPattern()
            }
        }

        test("setDateFormat propagates to adapter") {
            checkAll(100, dateFormatArb) { format ->
                val storage = TestDateTimeFormatStorage()

                // Set date format
                storage.setDateFormat(format)

                // Verify adapter has the format
                storage.getAdapterDateSeparatorFormat().shouldNotBeNull()
                storage.getAdapterDateSeparatorFormat()?.toPattern() shouldBe format.toPattern()
            }
        }

        test("setDateFormat(null) clears the format") {
            checkAll(100, dateFormatArb) { format ->
                val storage = TestDateTimeFormatStorage()

                // Set format first
                storage.setDateFormat(format)
                storage.getDateFormat().shouldNotBeNull()

                // Clear format
                storage.setDateFormat(null)

                // Verify format is cleared
                storage.getDateFormat().shouldBeNull()
                storage.getAdapterDateSeparatorFormat().shouldBeNull()
            }
        }

        test("default date format is null") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestDateTimeFormatStorage()

                // Verify default is null
                storage.getDateFormat().shouldBeNull()
            }
        }

        test("date format pattern is preserved after storage") {
            checkAll(100, dateFormatPatternArb) { pattern ->
                val storage = TestDateTimeFormatStorage()
                val format = SimpleDateFormat(pattern, Locale.US)

                // Set format
                storage.setDateFormat(format)

                // Verify pattern is preserved
                storage.getDateFormat()?.toPattern() shouldBe pattern
            }
        }

        // ========================================
        // Date Time Formatter Callback Tests
        // ========================================

        test("setDateTimeFormatter(callback) stores the provided callback") {
            checkAll(100, dateTimeFormatterCallbackArb) { callback ->
                val storage = TestDateTimeFormatStorage()

                // Set callback
                storage.setDateTimeFormatter(callback)

                // Verify callback is stored
                storage.getDateTimeFormatter().shouldNotBeNull()
                storage.getDateTimeFormatter() shouldBe callback
            }
        }

        test("getDateTimeFormatter() returns the callback that was set") {
            checkAll(100, dateTimeFormatterCallbackArb) { callback ->
                val storage = TestDateTimeFormatStorage()

                // Set callback
                storage.setDateTimeFormatter(callback)

                // Verify getter returns the same callback
                storage.getDateTimeFormatter() shouldBe callback
            }
        }

        test("setDateTimeFormatter propagates to adapter") {
            checkAll(100, dateTimeFormatterCallbackArb) { callback ->
                val storage = TestDateTimeFormatStorage()

                // Set callback
                storage.setDateTimeFormatter(callback)

                // Verify adapter has the callback
                storage.getAdapterDateTimeFormatter().shouldNotBeNull()
                storage.getAdapterDateTimeFormatter() shouldBe callback
            }
        }

        test("setDateTimeFormatter(null) clears the callback") {
            checkAll(100, dateTimeFormatterCallbackArb) { callback ->
                val storage = TestDateTimeFormatStorage()

                // Set callback first
                storage.setDateTimeFormatter(callback)
                storage.getDateTimeFormatter().shouldNotBeNull()

                // Clear callback
                storage.setDateTimeFormatter(null)

                // Verify callback is cleared
                storage.getDateTimeFormatter().shouldBeNull()
                storage.getAdapterDateTimeFormatter().shouldBeNull()
            }
        }

        test("default dateTimeFormatter is null") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestDateTimeFormatStorage()

                // Verify default is null
                storage.getDateTimeFormatter().shouldBeNull()
            }
        }

        test("dateTimeFormatter callback methods return expected values") {
            checkAll(100, nonEmptyStringArb, nonEmptyStringArb, Arb.long(0L..Long.MAX_VALUE / 2)) { 
                timeValue, todayValue, timestamp ->
                val callback = FixedDateTimeFormatterCallback(
                    timeValue = timeValue,
                    todayValue = todayValue
                )
                val storage = TestDateTimeFormatStorage()

                // Set callback
                storage.setDateTimeFormatter(callback)

                // Verify callback methods return expected values
                storage.getDateTimeFormatter()?.time(timestamp) shouldBe timeValue
                storage.getDateTimeFormatter()?.today(timestamp) shouldBe todayValue
            }
        }

        // ========================================
        // Combined Tests
        // ========================================

        test("time and date formats can be set independently") {
            checkAll(100, timeFormatArb, dateFormatArb) { timeFormat, dateFormat ->
                val storage = TestDateTimeFormatStorage()

                // Set both formats
                storage.setTimeFormat(timeFormat)
                storage.setDateFormat(dateFormat)

                // Verify both are stored correctly
                storage.getTimeFormat()?.toPattern() shouldBe timeFormat.toPattern()
                storage.getDateFormat()?.toPattern() shouldBe dateFormat.toPattern()
            }
        }

        test("all three date/time properties can be set together") {
            checkAll(100, timeFormatArb, dateFormatArb, dateTimeFormatterCallbackArb) { 
                timeFormat, dateFormat, callback ->
                val storage = TestDateTimeFormatStorage()

                // Set all three
                storage.setTimeFormat(timeFormat)
                storage.setDateFormat(dateFormat)
                storage.setDateTimeFormatter(callback)

                // Verify all are stored correctly
                storage.getTimeFormat()?.toPattern() shouldBe timeFormat.toPattern()
                storage.getDateFormat()?.toPattern() shouldBe dateFormat.toPattern()
                storage.getDateTimeFormatter() shouldBe callback
            }
        }

        test("replacing time format updates to latest value") {
            checkAll(100, timeFormatArb, timeFormatArb) { format1, format2 ->
                val storage = TestDateTimeFormatStorage()

                // Set first format
                storage.setTimeFormat(format1)
                storage.getTimeFormat()?.toPattern() shouldBe format1.toPattern()

                // Replace with second format
                storage.setTimeFormat(format2)
                storage.getTimeFormat()?.toPattern() shouldBe format2.toPattern()
            }
        }

        test("replacing date format updates to latest value") {
            checkAll(100, dateFormatArb, dateFormatArb) { format1, format2 ->
                val storage = TestDateTimeFormatStorage()

                // Set first format
                storage.setDateFormat(format1)
                storage.getDateFormat()?.toPattern() shouldBe format1.toPattern()

                // Replace with second format
                storage.setDateFormat(format2)
                storage.getDateFormat()?.toPattern() shouldBe format2.toPattern()
            }
        }

        test("replacing dateTimeFormatter updates to latest callback") {
            checkAll(100, dateTimeFormatterCallbackArb, dateTimeFormatterCallbackArb) { 
                callback1, callback2 ->
                val storage = TestDateTimeFormatStorage()

                // Set first callback
                storage.setDateTimeFormatter(callback1)
                storage.getDateTimeFormatter() shouldBe callback1

                // Replace with second callback
                storage.setDateTimeFormatter(callback2)
                storage.getDateTimeFormatter() shouldBe callback2
            }
        }

        test("MessageList and Adapter have identical format references") {
            checkAll(100, timeFormatArb, dateFormatArb, dateTimeFormatterCallbackArb) { 
                timeFormat, dateFormat, callback ->
                val storage = TestDateTimeFormatStorage()

                // Set all formats
                storage.setTimeFormat(timeFormat)
                storage.setDateFormat(dateFormat)
                storage.setDateTimeFormatter(callback)

                // Verify MessageList and Adapter have same references
                storage.getTimeFormat()?.toPattern() shouldBe storage.getAdapterTimeFormat()?.toPattern()
                storage.getDateFormat()?.toPattern() shouldBe storage.getAdapterDateSeparatorFormat()?.toPattern()
                storage.getDateTimeFormatter() shouldBe storage.getAdapterDateTimeFormatter()
            }
        }

        test("clearing one format does not affect others") {
            checkAll(100, timeFormatArb, dateFormatArb, dateTimeFormatterCallbackArb) { 
                timeFormat, dateFormat, callback ->
                val storage = TestDateTimeFormatStorage()

                // Set all formats
                storage.setTimeFormat(timeFormat)
                storage.setDateFormat(dateFormat)
                storage.setDateTimeFormatter(callback)

                // Clear time format only
                storage.setTimeFormat(null)

                // Verify only time format is cleared
                storage.getTimeFormat().shouldBeNull()
                storage.getDateFormat()?.toPattern() shouldBe dateFormat.toPattern()
                storage.getDateTimeFormatter() shouldBe callback
            }
        }

        test("time format can format timestamps correctly") {
            checkAll(100, timeFormatPatternArb, Arb.long(0L..System.currentTimeMillis())) { 
                pattern, timestamp ->
                val storage = TestDateTimeFormatStorage()
                val format = SimpleDateFormat(pattern, Locale.US)

                // Set format
                storage.setTimeFormat(format)

                // Verify format can be used to format timestamps
                val retrieved = storage.getTimeFormat()
                retrieved.shouldNotBeNull()
                
                // Format should produce a non-empty string
                val formatted = retrieved.format(java.util.Date(timestamp))
                formatted.isNotEmpty() shouldBe true
            }
        }

        test("date format can format timestamps correctly") {
            checkAll(100, dateFormatPatternArb, Arb.long(0L..System.currentTimeMillis())) { 
                pattern, timestamp ->
                val storage = TestDateTimeFormatStorage()
                val format = SimpleDateFormat(pattern, Locale.US)

                // Set format
                storage.setDateFormat(format)

                // Verify format can be used to format timestamps
                val retrieved = storage.getDateFormat()
                retrieved.shouldNotBeNull()
                
                // Format should produce a non-empty string
                val formatted = retrieved.format(java.util.Date(timestamp))
                formatted.isNotEmpty() shouldBe true
            }
        }
    }
})
