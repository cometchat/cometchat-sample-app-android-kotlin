package com.cometchat.uikit.compose.components.shared.baseelements

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onChild
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.uikit.compose.presentation.shared.baseelements.badgecount.BadgeCountStyle
import com.cometchat.uikit.compose.presentation.shared.baseelements.badgecount.CometChatBadgeCount
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for CometChatBadgeCount composable.
 *
 * Tests cover:
 * - Default badge rendering
 * - Count display and formatting
 * - Overflow handling (counts >= 999)
 * - Custom styling application
 * - Various count values
 * - Theme integration
 */
@RunWith(AndroidJUnit4::class)
class CometChatBadgeCountTest {

    @get:Rule
    val composeTestRule = androidx.compose.ui.test.junit4.createComposeRule()

    @Before
    fun setUp() {
        // Setup can be used for common initialization
    }

    @Test
    fun testDefaultBadgeRendering() {
        // Arrange & Act
        composeTestRule.setContent {
            CometChatBadgeCount(
                count = 5,
                modifier = Modifier.testTag("badge_default")
            )
        }

        // Assert
        composeTestRule.onNodeWithTag("badge_default").assertIsDisplayed()
    }

    @Test
    fun testCountDisplay_SingleDigit() {
        // Arrange & Act
        val count = 5
        composeTestRule.setContent {
            CometChatBadgeCount(count = count)
        }

        // Assert
        composeTestRule.onNodeWithText(count.toString()).assertIsDisplayed()
    }

    @Test
    fun testCountDisplay_MultipleDigits() {
        // Arrange & Act
        val count = 123
        composeTestRule.setContent {
            CometChatBadgeCount(count = count)
        }

        // Assert
        composeTestRule.onNodeWithText(count.toString()).assertIsDisplayed()
    }

    @Test
    fun testCountDisplay_MaxBeforeOverflow() {
        // Arrange & Act
        val count = 998
        composeTestRule.setContent {
            CometChatBadgeCount(count = count)
        }

        // Assert
        composeTestRule.onNodeWithText(count.toString()).assertIsDisplayed()
    }

    @Test
    fun testCountOverflow_AtThreshold() {
        // Arrange & Act
        composeTestRule.setContent {
            CometChatBadgeCount(count = 999)
        }

        // Assert
        composeTestRule.onNodeWithText("+99").assertIsDisplayed()
    }

    @Test
    fun testCountOverflow_AboveThreshold() {
        // Arrange & Act
        composeTestRule.setContent {
            CometChatBadgeCount(count = 1000)
        }

        // Assert
        composeTestRule.onNodeWithText("+99").assertIsDisplayed()
    }

    @Test
    fun testCountOverflow_LargeValue() {
        // Arrange & Act
        composeTestRule.setContent {
            CometChatBadgeCount(count = 9999)
        }

        // Assert
        composeTestRule.onNodeWithText("+99").assertIsDisplayed()
    }

    @Test
    fun testZeroCount() {
        // Arrange & Act
        composeTestRule.setContent {
            CometChatBadgeCount(count = 0)
        }

        // Assert
        composeTestRule.onNodeWithText("0").assertIsDisplayed()
    }

    @Test
    fun testNegativeCount() {
        // Arrange & Act
        composeTestRule.setContent {
            CometChatBadgeCount(count = -5)
        }

        // Assert
        composeTestRule.onNodeWithText("-5").assertIsDisplayed()
    }

    @Test
    fun testCustomBackgroundColor() {
        // Arrange & Act
        composeTestRule.setContent {
            CometChatBadgeCount(
                count = 5,
                style = BadgeCountStyle(
                    backgroundColor = Color.Red
                ),
                modifier = Modifier.testTag("badge_red_bg")
            )
        }

        // Assert
        composeTestRule.onNodeWithTag("badge_red_bg").assertIsDisplayed()
    }

    @Test
    fun testCustomTextColor() {
        // Arrange & Act
        composeTestRule.setContent {
            CometChatBadgeCount(
                count = 5,
                style = BadgeCountStyle(
                    textColor = Color.Yellow
                ),
                modifier = Modifier.testTag("badge_yellow_text")
            )
        }

        // Assert
        composeTestRule.onNodeWithTag("badge_yellow_text").assertIsDisplayed()
    }

    @Test
    fun testCustomBorderColor() {
        // Arrange & Act
        composeTestRule.setContent {
            CometChatBadgeCount(
                count = 5,
                style = BadgeCountStyle(
                    borderColor = Color.Blue,
                    borderWidth = 2.dp
                ),
                modifier = Modifier.testTag("badge_blue_border")
            )
        }

        // Assert
        composeTestRule.onNodeWithTag("badge_blue_border").assertIsDisplayed()
    }

    @Test
    fun testCustomSize() {
        // Arrange & Act
        composeTestRule.setContent {
            CometChatBadgeCount(
                count = 5,
                style = BadgeCountStyle(size = 48.dp),
                modifier = Modifier.testTag("badge_large")
            )
        }

        // Assert
        composeTestRule.onNodeWithTag("badge_large").assertIsDisplayed()
    }

    @Test
    fun testCustomCornerRadius() {
        // Arrange & Act
        composeTestRule.setContent {
            CometChatBadgeCount(
                count = 5,
                style = BadgeCountStyle(cornerRadius = 12.dp),
                modifier = Modifier.testTag("badge_rounded")
            )
        }

        // Assert
        composeTestRule.onNodeWithTag("badge_rounded").assertIsDisplayed()
    }

    @Test
    fun testCircularBadge() {
        // Arrange & Act
        val size = 32.dp
        composeTestRule.setContent {
            CometChatBadgeCount(
                count = 5,
                style = BadgeCountStyle(
                    size = size,
                    cornerRadius = size / 2
                ),
                modifier = Modifier.testTag("badge_circular")
            )
        }

        // Assert
        composeTestRule.onNodeWithTag("badge_circular").assertIsDisplayed()
    }

    @Test
    fun testFullCustomization() {
        // Arrange & Act
        composeTestRule.setContent {
            CometChatBadgeCount(
                count = 42,
                style = BadgeCountStyle(
                    backgroundColor = Color.Red,
                    textColor = Color.White,
                    borderColor = Color.DarkGray,
                    borderWidth = 2.dp,
                    cornerRadius = 8.dp,
                    size = 40.dp
                ),
                modifier = Modifier.testTag("badge_custom")
            )
        }

        // Assert
        composeTestRule.onNodeWithTag("badge_custom").assertIsDisplayed()
        composeTestRule.onNodeWithText("42").assertIsDisplayed()
    }

    @Test
    fun testDefaultStyle() {
        // Arrange & Act
        composeTestRule.setContent {
            CometChatBadgeCount(
                count = 7,
                style = BadgeCountStyle(),
                modifier = Modifier.testTag("badge_default_style")
            )
        }

        // Assert
        composeTestRule.onNodeWithTag("badge_default_style").assertIsDisplayed()
        composeTestRule.onNodeWithText("7").assertIsDisplayed()
    }

    @Test
    fun testWithoutStyle() {
        // Arrange & Act
        composeTestRule.setContent {
            CometChatBadgeCount(
                count = 3,
                modifier = Modifier.testTag("badge_no_style")
            )
        }

        // Assert
        composeTestRule.onNodeWithTag("badge_no_style").assertIsDisplayed()
        composeTestRule.onNodeWithText("3").assertIsDisplayed()
    }

    @Test
    fun testCountDisplay_TransitionFromZeroToPositive() {
        // Test that badge correctly displays count transitions
        // Arrange & Act
        composeTestRule.setContent {
            androidx.compose.foundation.layout.Row {
                CometChatBadgeCount(
                    count = 0,
                    modifier = Modifier.testTag("badge_zero")
                )
                CometChatBadgeCount(
                    count = 5,
                    modifier = Modifier.testTag("badge_five")
                )
            }
        }

        // Assert - verify both displays are correct
        composeTestRule.onNodeWithTag("badge_zero").onChild().assertTextEquals("0")
        composeTestRule.onNodeWithTag("badge_five").onChild().assertTextEquals("5")
    }

    @Test
    fun testCountDisplay_TransitionToOverflow() {
        // Test that badge correctly displays normal count and overflow
        // Arrange & Act
        composeTestRule.setContent {
            androidx.compose.foundation.layout.Row {
                CometChatBadgeCount(
                    count = 50,
                    modifier = Modifier.testTag("badge_fifty")
                )
                CometChatBadgeCount(
                    count = 999,
                    modifier = Modifier.testTag("badge_overflow")
                )
            }
        }

        // Assert - verify both displays are correct
        composeTestRule.onNodeWithTag("badge_fifty").onChild().assertTextEquals("50")
        composeTestRule.onNodeWithTag("badge_overflow").onChild().assertTextEquals("+99")
    }

    @Test
    fun testAllCommonCounts() {
        // Test counts: 0, 1, 9, 10, 99, 100, 998, 999, 1000
        val testCounts = listOf(0, 1, 9, 10, 99, 100, 998, 999, 1000)

        // Act - compose all badges in a single call
        composeTestRule.setContent {
            androidx.compose.foundation.layout.Column {
                for (count in testCounts) {
                    CometChatBadgeCount(
                        count = count,
                        modifier = Modifier.testTag("badge_count_$count")
                    )
                }
            }
        }

        // Assert - verify all counts display correctly
        for (count in testCounts) {
            val expectedText = when {
                count < 999 -> count.toString()
                else -> "+99"
            }
            composeTestRule.onNodeWithTag("badge_count_$count").onChild().assertTextEquals(expectedText)
        }
    }

    @Test
    fun testBadgeVisibility_Always() {
        // Test that badge is always visible regardless of count
        val counts = listOf(-100, 0, 1, 500, 999, 10000)

        // Act - compose all badges in a single call
        composeTestRule.setContent {
            androidx.compose.foundation.layout.Column {
                for (count in counts) {
                    CometChatBadgeCount(
                        count = count,
                        modifier = Modifier.testTag("badge_$count")
                    )
                }
            }
        }

        // Assert - verify all badges are displayed
        for (count in counts) {
            composeTestRule.onNodeWithTag("badge_$count").assertIsDisplayed()
        }
    }

    @Test
    fun testMultipleBadgesWithDifferentCounts() {
        // Arrange & Act
        composeTestRule.setContent {
            androidx.compose.foundation.layout.Row {
                CometChatBadgeCount(
                    count = 1,
                    modifier = Modifier.testTag("badge_1")
                )
                CometChatBadgeCount(
                    count = 999,
                    modifier = Modifier.testTag("badge_999")
                )
            }
        }

        // Assert
        composeTestRule.onNodeWithTag("badge_1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("badge_999").assertIsDisplayed()
        composeTestRule.onNodeWithText("1").assertIsDisplayed()
        composeTestRule.onNodeWithText("+99").assertIsDisplayed()
    }

    @Test
    fun testBorderWidthVariations() {
        // Test different border widths
        val borderWidths = listOf(0.dp, 1.dp, 2.dp, 4.dp)

        // Act - compose all variations in a single call
        composeTestRule.setContent {
            androidx.compose.foundation.layout.Column {
                for ((index, width) in borderWidths.withIndex()) {
                    CometChatBadgeCount(
                        count = 5,
                        style = BadgeCountStyle(
                            borderWidth = width,
                            borderColor = Color.Black
                        ),
                        modifier = Modifier.testTag("badge_border_$index")
                    )
                }
            }
        }

        // Assert
        for (index in borderWidths.indices) {
            composeTestRule.onNodeWithTag("badge_border_$index").assertIsDisplayed()
        }
    }

    @Test
    fun testSizeVariations() {
        // Test different badge sizes
        val sizes = listOf(16.dp, 20.dp, 24.dp, 32.dp, 40.dp, 48.dp)

        // Act - compose all variations in a single call
        composeTestRule.setContent {
            androidx.compose.foundation.layout.Column {
                for ((index, size) in sizes.withIndex()) {
                    CometChatBadgeCount(
                        count = 5,
                        style = BadgeCountStyle(size = size),
                        modifier = Modifier.testTag("badge_size_$index")
                    )
                }
            }
        }

        // Assert
        for (index in sizes.indices) {
            composeTestRule.onNodeWithTag("badge_size_$index").assertIsDisplayed()
        }
    }
}
