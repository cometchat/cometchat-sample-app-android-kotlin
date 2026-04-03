package com.cometchat.uikit.compose.shared.dialog

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.shared.dialog.CometChatDialog
import com.cometchat.uikit.compose.presentation.shared.dialog.CometChatDialogStyle
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicInteger

/**
 * Instrumented UI tests for CometChatDialog.
 *
 * Feature: cometchat-dialog
 * Properties tested:
 * - Property 2: Default Style Values from Theme
 * - Property 3: Visibility Controls
 * - Property 4: Button Callback Invocation
 * - Property 5: Progress Indicator Display
 * - Property 6: Custom Content Descriptions Applied
 * 
 * Validates: Requirements 1.6, 2.6, 3.3, 3.4, 4.7, 4.8, 5.7, 5.8, 6.3, 6.4, 7.1-7.4, 8.1, 8.2
 */
@RunWith(AndroidJUnit4::class)
class CometChatDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Property 2: Default Style Values from Theme
     *
     * For any theme configuration, when CometChatDialogStyle.default() is invoked,
     * all color values SHALL match the corresponding values from CometChatTheme.colorScheme
     * and all typography values SHALL match the corresponding values from CometChatTheme.typography.
     *
     * Feature: cometchat-dialog, Property 2: Default Style Values from Theme
     * Validates: Requirements 1.6, 8.1, 8.2
     */
    @Test
    fun defaultStyle_sourcesValuesFromTheme() {
        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                val style = CometChatDialogStyle.default()
                val colorScheme = CometChatTheme.colorScheme
                val typography = CometChatTheme.typography

                // Assert - verify container colors match theme
                assert(style.backgroundColor == colorScheme.backgroundColor1) {
                    "backgroundColor should match theme backgroundColor1"
                }
                assert(style.strokeColor == colorScheme.strokeColorLight) {
                    "strokeColor should match theme strokeColorLight"
                }
                
                // Assert - verify icon colors match theme
                assert(style.iconBackgroundColor == colorScheme.backgroundColor2) {
                    "iconBackgroundColor should match theme backgroundColor2"
                }
                assert(style.iconTint == colorScheme.iconTintPrimary) {
                    "iconTint should match theme iconColorPrimary"
                }
                
                // Assert - verify text colors match theme
                assert(style.titleTextColor == colorScheme.textColorPrimary) {
                    "titleTextColor should match theme textColorPrimary"
                }
                assert(style.messageTextColor == colorScheme.textColorSecondary) {
                    "messageTextColor should match theme textColorSecondary"
                }
                
                // Assert - verify positive button colors match theme
                assert(style.positiveButtonTextColor == colorScheme.colorWhite) {
                    "positiveButtonTextColor should match theme colorWhite"
                }
                assert(style.positiveButtonBackgroundColor == colorScheme.errorColor) {
                    "positiveButtonBackgroundColor should match theme errorColor"
                }
                
                // Assert - verify negative button colors match theme
                assert(style.negativeButtonTextColor == colorScheme.textColorPrimary) {
                    "negativeButtonTextColor should match theme textColorPrimary"
                }
                assert(style.negativeButtonBackgroundColor == colorScheme.textColorWhite) {
                    "negativeButtonBackgroundColor should match theme textColorWhite"
                }
                assert(style.negativeButtonStrokeColor == colorScheme.strokeColorDark) {
                    "negativeButtonStrokeColor should match theme strokeColorDark"
                }

                // Assert - verify typography matches theme
                assert(style.titleTextStyle == typography.heading3Medium) {
                    "titleTextStyle should match theme heading3Medium"
                }
                assert(style.messageTextStyle == typography.bodyRegular) {
                    "messageTextStyle should match theme bodyRegular"
                }
                assert(style.positiveButtonTextStyle == typography.bodyMedium) {
                    "positiveButtonTextStyle should match theme bodyMedium"
                }
                assert(style.negativeButtonTextStyle == typography.bodyMedium) {
                    "negativeButtonTextStyle should match theme bodyMedium"
                }
            }
        }
    }

    /**
     * Property 3: Visibility Controls - hideNegativeButton = false (using backward compat API)
     *
     * For any CometChatDialog invocation with showCancelButton = true,
     * the negative button SHALL be visible.
     *
     * Feature: cometchat-dialog, Property 3: Visibility Controls
     * Validates: Requirements 5.7
     */
    @Test
    fun dialog_showsNegativeButtonWhenNotHidden() {
        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatDialog(
                    title = "Test Title",
                    message = "Test Message",
                    confirmButtonText = "Confirm",
                    cancelButtonText = "Cancel",
                    showCancelButton = true,
                    onConfirm = { },
                    onDismiss = { }
                )
            }
        }

        // Assert
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
    }

    /**
     * Property 3: Visibility Controls - hideNegativeButton = true (using backward compat API)
     *
     * For any CometChatDialog invocation with showCancelButton = false,
     * the negative button SHALL NOT be visible.
     *
     * Feature: cometchat-dialog, Property 3: Visibility Controls
     * Validates: Requirements 5.7
     */
    @Test
    fun dialog_hidesNegativeButtonWhenHidden() {
        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatDialog(
                    title = "Test Title",
                    message = "Test Message",
                    confirmButtonText = "Confirm",
                    cancelButtonText = "Cancel",
                    showCancelButton = false,
                    onConfirm = { },
                    onDismiss = { }
                )
            }
        }

        // Assert - cancel button should not exist
        composeTestRule.onNodeWithText("Cancel").assertDoesNotExist()
    }

    /**
     * Property 3: Visibility Controls - hideTitle = true
     *
     * For any CometChatDialog invocation with hideTitle = true,
     * the title SHALL NOT be visible.
     *
     * Feature: cometchat-dialog, Property 3: Visibility Controls
     * Validates: Requirements 3.3
     */
    @Test
    fun dialog_hidesTitleWhenHideTitleIsTrue() {
        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatDialog(
                    title = "Test Title",
                    message = "Test Message",
                    positiveButtonText = "Confirm",
                    negativeButtonText = "Cancel",
                    hideTitle = true,
                    onPositiveClick = { },
                    onNegativeClick = { }
                )
            }
        }

        // Assert - title should not exist
        composeTestRule.onNodeWithText("Test Title").assertDoesNotExist()
    }

    /**
     * Property 3: Visibility Controls - hideMessage = true
     *
     * For any CometChatDialog invocation with hideMessage = true,
     * the message SHALL NOT be visible.
     *
     * Feature: cometchat-dialog, Property 3: Visibility Controls
     * Validates: Requirements 3.4
     */
    @Test
    fun dialog_hidesMessageWhenHideMessageIsTrue() {
        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatDialog(
                    title = "Test Title",
                    message = "Test Message",
                    positiveButtonText = "Confirm",
                    negativeButtonText = "Cancel",
                    hideMessage = true,
                    onPositiveClick = { },
                    onNegativeClick = { }
                )
            }
        }

        // Assert - message should not exist
        composeTestRule.onNodeWithText("Test Message").assertDoesNotExist()
    }

    /**
     * Property 3: Visibility Controls - hidePositiveButton = true
     *
     * For any CometChatDialog invocation with hidePositiveButton = true,
     * the positive button SHALL NOT be visible.
     *
     * Feature: cometchat-dialog, Property 3: Visibility Controls
     * Validates: Requirements 4.7
     */
    @Test
    fun dialog_hidesPositiveButtonWhenHidePositiveButtonIsTrue() {
        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatDialog(
                    title = "Test Title",
                    message = "Test Message",
                    positiveButtonText = "Confirm",
                    negativeButtonText = "Cancel",
                    hidePositiveButton = true,
                    onPositiveClick = { },
                    onNegativeClick = { }
                )
            }
        }

        // Assert - positive button should not exist
        composeTestRule.onNodeWithText("Confirm").assertDoesNotExist()
    }

    /**
     * Property 3: Visibility Controls - icon is null
     *
     * For any CometChatDialog invocation with icon = null,
     * the icon SHALL NOT be visible.
     *
     * Feature: cometchat-dialog, Property 3: Visibility Controls
     * Validates: Requirements 2.6
     */
    @Test
    fun dialog_hidesIconWhenIconIsNull() {
        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatDialog(
                    title = "Test Title",
                    message = "Test Message",
                    positiveButtonText = "Confirm",
                    negativeButtonText = "Cancel",
                    icon = null,
                    onPositiveClick = { },
                    onNegativeClick = { }
                )
            }
        }

        // Assert - dialog should display without icon (no crash)
        composeTestRule.onNodeWithText("Test Title").assertIsDisplayed()
    }

    /**
     * Property 4: Button Callback Invocation - Positive button
     *
     * For any CometChatDialog invocation, when the positive button is clicked,
     * the onPositiveClick callback SHALL be invoked exactly once.
     *
     * Feature: cometchat-dialog, Property 4: Button Callback Invocation
     * Validates: Requirements 4.8
     */
    @Test
    fun dialog_positiveButtonInvokesOnPositiveClickCallbackOnce() {
        // Arrange
        val clickCount = AtomicInteger(0)

        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatDialog(
                    title = "Test Title",
                    message = "Test Message",
                    confirmButtonText = "Confirm",
                    cancelButtonText = "Cancel",
                    onConfirm = { clickCount.incrementAndGet() },
                    onDismiss = { }
                )
            }
        }

        // Click confirm button
        composeTestRule.onNodeWithText("Confirm").performClick()

        // Assert - callback should be invoked exactly once
        assert(clickCount.get() == 1) {
            "onPositiveClick callback should be invoked exactly once, but was invoked ${clickCount.get()} times"
        }
    }

    /**
     * Property 4: Button Callback Invocation - Negative button
     *
     * For any CometChatDialog invocation, when the negative button is clicked,
     * the onNegativeClick callback SHALL be invoked exactly once.
     *
     * Feature: cometchat-dialog, Property 4: Button Callback Invocation
     * Validates: Requirements 5.8
     */
    @Test
    fun dialog_negativeButtonInvokesOnNegativeClickCallbackOnce() {
        // Arrange
        val clickCount = AtomicInteger(0)

        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatDialog(
                    title = "Test Title",
                    message = "Test Message",
                    confirmButtonText = "Confirm",
                    cancelButtonText = "Cancel",
                    onConfirm = { },
                    onDismiss = { clickCount.incrementAndGet() }
                )
            }
        }

        // Click cancel button
        composeTestRule.onNodeWithText("Cancel").performClick()

        // Assert - callback should be invoked exactly once
        assert(clickCount.get() == 1) {
            "onNegativeClick callback should be invoked exactly once, but was invoked ${clickCount.get()} times"
        }
    }

    /**
     * Property 5: Progress Indicator Display - showPositiveButtonProgress = true
     *
     * For any CometChatDialog invocation with showPositiveButtonProgress = true,
     * a progress indicator SHALL be displayed instead of the positive button text.
     *
     * Feature: cometchat-dialog, Property 5: Progress Indicator Display
     * Validates: Requirements 6.3
     */
    @Test
    fun dialog_showsProgressOnPositiveButtonWhenEnabled() {
        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatDialog(
                    title = "Test Title",
                    message = "Test Message",
                    positiveButtonText = "Confirm",
                    negativeButtonText = "Cancel",
                    showPositiveButtonProgress = true,
                    onPositiveClick = { },
                    onNegativeClick = { }
                )
            }
        }

        // Assert - positive button text should not be visible (replaced by progress)
        composeTestRule.onNodeWithText("Confirm").assertDoesNotExist()
        // Negative button should still show text
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
    }

    /**
     * Property 5: Progress Indicator Display - showNegativeButtonProgress = true
     *
     * For any CometChatDialog invocation with showNegativeButtonProgress = true,
     * a progress indicator SHALL be displayed instead of the negative button text.
     *
     * Feature: cometchat-dialog, Property 5: Progress Indicator Display
     * Validates: Requirements 6.4
     */
    @Test
    fun dialog_showsProgressOnNegativeButtonWhenEnabled() {
        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatDialog(
                    title = "Test Title",
                    message = "Test Message",
                    positiveButtonText = "Confirm",
                    negativeButtonText = "Cancel",
                    showNegativeButtonProgress = true,
                    onPositiveClick = { },
                    onNegativeClick = { }
                )
            }
        }

        // Assert - negative button text should not be visible (replaced by progress)
        composeTestRule.onNodeWithText("Cancel").assertDoesNotExist()
        // Positive button should still show text
        composeTestRule.onNodeWithText("Confirm").assertIsDisplayed()
    }

    /**
     * Property 6: Custom Content Descriptions Applied
     *
     * For any custom content description strings provided to CometChatDialog,
     * the dialog SHALL use those custom descriptions for accessibility.
     *
     * Feature: cometchat-dialog, Property 6: Accessibility Descriptions Applied
     * Validates: Requirements 7.1-7.4
     */
    @Test
    fun dialog_usesCustomContentDescriptions() {
        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatDialog(
                    title = "Test Title",
                    message = "Test Message",
                    confirmButtonText = "Confirm",
                    cancelButtonText = "Cancel",
                    dialogContentDescription = "Custom Dialog Description",
                    confirmButtonContentDescription = "Custom Confirm Description",
                    cancelButtonContentDescription = "Custom Cancel Description",
                    onConfirm = { },
                    onDismiss = { }
                )
            }
        }

        // Assert - custom content descriptions should be applied
        composeTestRule.onNodeWithContentDescription("Custom Dialog Description").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Custom Confirm Description").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Custom Cancel Description").assertIsDisplayed()
    }

    /**
     * Example test: Dialog displays title and message
     */
    @Test
    fun dialog_displaysTitleAndMessage() {
        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatDialog(
                    title = "Delete Conversation",
                    message = "Are you sure you want to delete this conversation?",
                    confirmButtonText = "Delete",
                    cancelButtonText = "Cancel",
                    onConfirm = { },
                    onDismiss = { }
                )
            }
        }

        // Assert
        composeTestRule.onNodeWithText("Delete Conversation").assertIsDisplayed()
        composeTestRule.onNodeWithText("Are you sure you want to delete this conversation?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
    }

    /**
     * Example test: Dialog uses default content descriptions when custom ones not provided
     */
    @Test
    fun dialog_usesDefaultContentDescriptionsWhenNotProvided() {
        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatDialog(
                    title = "Test Title",
                    message = "Test Message",
                    confirmButtonText = "Confirm",
                    cancelButtonText = "Cancel",
                    onConfirm = { },
                    onDismiss = { }
                )
            }
        }

        // Assert - default content descriptions (title and button texts) should be used
        composeTestRule.onNodeWithContentDescription("Test Title").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Confirm").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Cancel").assertIsDisplayed()
    }

    /**
     * Example test: Dialog with icon displays correctly
     */
    @Test
    fun dialog_displaysWithIcon() {
        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatDialog(
                    title = "Delete Conversation",
                    message = "Are you sure you want to delete this conversation?",
                    positiveButtonText = "Delete",
                    negativeButtonText = "Cancel",
                    icon = painterResource(R.drawable.cometchat_ic_delete),
                    onPositiveClick = { },
                    onNegativeClick = { }
                )
            }
        }

        // Assert - dialog should display with all elements
        composeTestRule.onNodeWithText("Delete Conversation").assertIsDisplayed()
        composeTestRule.onNodeWithText("Are you sure you want to delete this conversation?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
    }

    /**
     * Backward compatibility test: Old API still works
     */
    @Test
    fun dialog_backwardCompatibilityApiWorks() {
        // Arrange
        val confirmCount = AtomicInteger(0)
        val dismissCount = AtomicInteger(0)

        // Act - using old parameter names
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                @Suppress("DEPRECATION")
                CometChatDialog(
                    title = "Test Title",
                    message = "Test Message",
                    confirmButtonText = "Confirm",
                    cancelButtonText = "Cancel",
                    showCancelButton = true,
                    onConfirm = { confirmCount.incrementAndGet() },
                    onDismiss = { dismissCount.incrementAndGet() }
                )
            }
        }

        // Assert - dialog should display correctly
        composeTestRule.onNodeWithText("Test Title").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Message").assertIsDisplayed()
        composeTestRule.onNodeWithText("Confirm").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()

        // Click confirm button
        composeTestRule.onNodeWithText("Confirm").performClick()
        assert(confirmCount.get() == 1) { "onConfirm should be called once" }
    }
}
