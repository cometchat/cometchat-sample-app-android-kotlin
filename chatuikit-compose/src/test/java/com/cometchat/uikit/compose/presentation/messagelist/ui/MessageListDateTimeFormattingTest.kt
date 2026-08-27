package com.cometchat.uikit.compose.presentation.messagelist.ui

import androidx.compose.ui.test.junit4.createComposeRule
import com.cometchat.uikit.compose.presentation.shared.baseelements.date.defaultTimePattern
import com.cometchat.uikit.compose.presentation.shared.interfaces.DateTimeFormatterCallback
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowSettings
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * ENG-38585 — the Compose message list had no date/time formatting API at all, so the XML kit's
 * `setTimeFormat` / `setDateFormat` / `setDateTimeFormatter` had no counterpart and every
 * timestamp rendered as hardcoded 12-hour `h:mm a`.
 *
 * These tests pin the two halves of the fix: the precedence rules for date separator text
 * (callback beats pattern beats default, and "today"/"yesterday" come from string resources
 * rather than English literals), and the device-aware default time pattern.
 *
 * Layer 1 (Component). Runs on Robolectric in `src/test`, so it executes in the JVM unit-test
 * job rather than the instrumented suite.
 *
 * Run: ./gradlew :chatuikit-compose:testDebugUnitTest --tests "*MessageListDateTimeFormattingTest"
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MessageListDateTimeFormattingTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val nowSeconds = System.currentTimeMillis() / 1000
    private val yesterdaySeconds = nowSeconds - 86_400
    private val lastMonthSeconds = nowSeconds - (30L * 86_400)

    private fun separatorText(
        timestamp: Long,
        dateFormat: SimpleDateFormat? = null,
        dateTimeFormatter: DateTimeFormatterCallback? = null
    ): String {
        lateinit var resolved: String
        composeRule.setContent {
            resolved = rememberDateSeparatorText(timestamp, dateFormat, dateTimeFormatter)
        }
        composeRule.waitForIdle()
        return resolved
    }

    private fun timePattern(): String {
        lateinit var resolved: String
        composeRule.setContent { resolved = defaultTimePattern() }
        composeRule.waitForIdle()
        return resolved
    }

    @Test
    fun `today comes from the string resource, not an English literal`() {
        assertEquals("Today", separatorText(nowSeconds))
    }

    @Test
    fun `the callback wins over the string resource for today`() {
        val formatter = object : DateTimeFormatterCallback {
            override fun today(timestamp: Long) = "Heute"
        }
        assertEquals("Heute", separatorText(nowSeconds, dateTimeFormatter = formatter))
    }

    @Test
    fun `the callback wins over the string resource for yesterday`() {
        val formatter = object : DateTimeFormatterCallback {
            override fun yesterday(timestamp: Long) = "Gestern"
        }
        assertEquals("Gestern", separatorText(yesterdaySeconds, dateTimeFormatter = formatter))
    }

    @Test
    fun `a callback returning null falls through to the default`() {
        val formatter = object : DateTimeFormatterCallback {
            override fun today(timestamp: Long): String? = null
        }
        assertEquals("Today", separatorText(nowSeconds, dateTimeFormatter = formatter))
    }

    @Test
    fun `dateFormat applies to dates older than yesterday`() {
        val pattern = SimpleDateFormat("dd.MM.yyyy", Locale.US)
        val expected = pattern.format(java.util.Date(lastMonthSeconds * 1000))
        assertEquals(expected, separatorText(lastMonthSeconds, dateFormat = pattern))
    }

    @Test
    fun `dateFormat does not touch today`() {
        val pattern = SimpleDateFormat("dd.MM.yyyy", Locale.US)
        assertEquals("Today", separatorText(nowSeconds, dateFormat = pattern))
    }

    @Test
    fun `the callback wins over dateFormat for older dates`() {
        val formatter = object : DateTimeFormatterCallback {
            override fun otherDays(timestamp: Long) = "long ago"
        }
        assertEquals(
            "long ago",
            separatorText(
                lastMonthSeconds,
                dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.US),
                dateTimeFormatter = formatter
            )
        )
    }

    @Test
    fun `the callback is handed milliseconds`() {
        var seen = 0L
        val formatter = object : DateTimeFormatterCallback {
            override fun today(timestamp: Long): String {
                seen = timestamp
                return "seen"
            }
        }
        separatorText(nowSeconds, dateTimeFormatter = formatter)
        assertEquals(nowSeconds * 1000, seen)
    }

    @Test
    fun `the default time pattern follows a 24-hour device`() {
        ShadowSettings.set24HourTimeFormat(true)
        assertEquals("HH:mm", timePattern())
    }

    @Test
    fun `the default time pattern follows a 12-hour device`() {
        ShadowSettings.set24HourTimeFormat(false)
        assertEquals("h:mm a", timePattern())
    }
}
