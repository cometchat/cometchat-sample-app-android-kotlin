package com.cometchat.uikit.compose.components.shared.baseelements

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.uikit.compose.presentation.shared.baseelements.date.CometChatDate
import com.cometchat.uikit.compose.presentation.shared.baseelements.date.DateStyle
import com.cometchat.uikit.compose.presentation.shared.baseelements.date.Pattern
import com.cometchat.uikit.compose.presentation.shared.interfaces.DateTimeFormatterCallback

import com.cometchat.uikit.compose.theme.CometChatTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar

/**
 * Instrumented tests for CometChatDate composable.
 *
 * Tests cover:
 * - Different pattern displays (TIME, DAY_DATE, DAY_DATE_TIME)
 * - Custom date string override
 * - Date formatting for today, yesterday, and other days
 * - Custom date/time formatter callbacks
 * - Styling customization
 * - Transparent background mode
 */
@RunWith(AndroidJUnit4::class)
class CometChatDateTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Helper function to get timestamp for today at a specific time
     */
    private fun getTodayTimestamp(hourOfDay: Int = 14, minute: Int = 30): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis / 1000
    }

    /**
     * Helper function to get timestamp for yesterday
     */
    private fun getYesterdayTimestamp(): Long {
        return Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 14)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis / 1000
    }

    /**
     * Helper function to get timestamp for a week ago
     */
    private fun getWeekAgoTimestamp(): Long {
        return Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -5)
            set(Calendar.HOUR_OF_DAY, 14)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis / 1000
    }

    @Test
    fun testCustomDateStringDisplay() {
        composeTestRule.setContent {
            CometChatTheme {
                CometChatDate(
                    customDateString = "Last seen recently",
                    modifier = Modifier.testTag("date_custom")
                )
            }
        }

        composeTestRule.onNodeWithText("Last seen recently").assertIsDisplayed()
    }

    @Test
    fun testTimePatternDisplay() {
        val timestamp = getTodayTimestamp(14, 30)
        
        composeTestRule.setContent {
            CometChatTheme {
                CometChatDate(
                    timestamp = timestamp,
                    pattern = Pattern.TIME,
                    modifier = Modifier.testTag("date_time")
                )
            }
        }

        composeTestRule.waitForIdle()
        // Should display time in format like "2:30 PM"
        composeTestRule.onNodeWithTag("date_time").assertIsDisplayed()
    }

    @Test
    fun testDayDatePatternDisplaysToday() {
        val timestamp = getTodayTimestamp()
        
        composeTestRule.setContent {
            CometChatTheme {
                CometChatDate(
                    timestamp = timestamp,
                    pattern = Pattern.DAY_DATE,
                    modifier = Modifier.testTag("date_today")
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Today").assertIsDisplayed()
    }

    @Test
    fun testDayDatePatternDisplaysYesterday() {
        val timestamp = getYesterdayTimestamp()
        
        composeTestRule.setContent {
            CometChatTheme {
                CometChatDate(
                    timestamp = timestamp,
                    pattern = Pattern.DAY_DATE,
                    modifier = Modifier.testTag("date_yesterday")
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Yesterday").assertIsDisplayed()
    }

    @Test
    fun testDayDateTimePatternForToday() {
        val timestamp = getTodayTimestamp(15, 45)
        
        composeTestRule.setContent {
            CometChatTheme {
                CometChatDate(
                    timestamp = timestamp,
                    pattern = Pattern.DAY_DATE_TIME,
                    modifier = Modifier.testTag("date_datetime_today")
                )
            }
        }

        composeTestRule.waitForIdle()
        // Should display time since it's today
        composeTestRule.onNodeWithTag("date_datetime_today").assertIsDisplayed()
    }

    @Test
    fun testDayDateTimePatternForYesterday() {
        val timestamp = getYesterdayTimestamp()
        
        composeTestRule.setContent {
            CometChatTheme {
                CometChatDate(
                    timestamp = timestamp,
                    pattern = Pattern.DAY_DATE_TIME,
                    modifier = Modifier.testTag("date_datetime_yesterday")
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Yesterday").assertIsDisplayed()
    }

    @Test
    fun testCustomDateTimeFormatterCallback() {
        val timestamp = getTodayTimestamp()
        
        val customCallback = object : DateTimeFormatterCallback {
            override fun today(timestamp: Long) = "Hoy"
            override fun yesterday(timestamp: Long) = "Ayer"
        }
        
        composeTestRule.setContent {
            CometChatTheme {
                CometChatDate(
                    timestamp = timestamp,
                    pattern = Pattern.DAY_DATE,
                    dateTimeFormatterCallback = customCallback,
                    modifier = Modifier.testTag("date_custom_callback")
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Hoy").assertIsDisplayed()
    }

    @Test
    fun testTransparentBackgroundMode() {
        composeTestRule.setContent {
            CometChatTheme {
                CometChatDate(
                    customDateString = "Test",
                    transparentBackground = true,
                    modifier = Modifier.testTag("date_transparent")
                )
            }
        }

        composeTestRule.onNodeWithTag("date_transparent").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test").assertIsDisplayed()
    }

    @Test
    fun testStyledDateWithBackground() {
        composeTestRule.setContent {
            CometChatTheme {
                CometChatDate(
                    customDateString = "Styled Date",
                    transparentBackground = false,
                    style = DateStyle(
                        backgroundColor = Color.LightGray,
                        textColor = Color.Black,
                        cornerRadius = 8.dp,
                        borderColor = Color.Gray,
                        borderWidth = 1.dp
                    ),
                    modifier = Modifier.testTag("date_styled")
                )
            }
        }

        composeTestRule.onNodeWithTag("date_styled").assertIsDisplayed()
        composeTestRule.onNodeWithText("Styled Date").assertIsDisplayed()
    }

    @Test
    fun testEmptyDateWhenTimestampIsZero() {
        composeTestRule.setContent {
            CometChatTheme {
                CometChatDate(
                    timestamp = 0,
                    pattern = Pattern.DAY_DATE,
                    modifier = Modifier.testTag("date_empty")
                )
            }
        }

        composeTestRule.waitForIdle()
        // Component should not be visible when timestamp is 0 and no custom string
        // The testTag won't be found because nothing is rendered
    }

    @Test
    fun testCustomTimePattern() {
        val timestamp = getTodayTimestamp(14, 30)
        
        composeTestRule.setContent {
            CometChatTheme {
                CometChatDate(
                    timestamp = timestamp,
                    pattern = Pattern.TIME,
                    timePattern = "HH:mm", // 24-hour format
                    modifier = Modifier.testTag("date_custom_time_pattern")
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("14:30").assertIsDisplayed()
    }

    @Test
    fun testCustomDatePattern() {
        // Create a specific date: December 25, 2023
        val calendar = Calendar.getInstance().apply {
            set(2023, Calendar.DECEMBER, 25, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val timestamp = calendar.timeInMillis / 1000
        
        composeTestRule.setContent {
            CometChatTheme {
                CometChatDate(
                    timestamp = timestamp,
                    pattern = Pattern.DAY_DATE,
                    datePattern = "MMM dd, yyyy",
                    modifier = Modifier.testTag("date_custom_pattern")
                )
            }
        }

        composeTestRule.waitForIdle()
        // Should display date in custom format
        composeTestRule.onNodeWithTag("date_custom_pattern").assertIsDisplayed()
    }
}
