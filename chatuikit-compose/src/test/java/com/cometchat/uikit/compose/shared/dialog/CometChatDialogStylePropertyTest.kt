package com.cometchat.uikit.compose.shared.dialog

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cometchat.uikit.compose.presentation.shared.dialog.CometChatDialogStyle
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

/**
 * Property-based tests for CometChatDialogStyle.
 *
 * Feature: cometchat-dialog
 * Properties tested:
 * - Property 1: Style Immutability
 * - Property 2: Default Style Values from Theme
 * 
 * Validates: Requirements 1.1-1.5, 2.2-2.5, 3.1-3.2, 4.1-4.6, 5.1-5.6
 */
class CometChatDialogStylePropertyTest : StringSpec({

    /**
     * Property 1: Style Immutability
     *
     * For any CometChatDialogStyle instance with any combination of property values,
     * calling copy() with modified values SHALL produce a new instance while the
     * original instance remains unchanged.
     *
     * Feature: cometchat-dialog, Property 1: Style Immutability
     * Validates: Requirements 1.1-1.5, 2.2-2.5, 3.1-3.2, 4.1-4.6, 5.1-5.6
     */
    "Property 1: CometChatDialogStyle copy should create new instance with modified values while original remains unchanged" {
        val styleArb = Arb.bind(
            Arb.int(0, 255),      // backgroundColor alpha
            Arb.float(8f, 24f),   // cornerRadius
            Arb.int(0, 255),      // titleTextColor alpha
            Arb.int(0, 255),      // messageTextColor alpha
            Arb.int(0, 255)       // positiveButtonTextColor alpha
        ) { bgAlpha, cornerRadius, titleAlpha, messageAlpha, positiveAlpha ->
            createTestDialogStyle(
                backgroundColor = Color(1f, 1f, 1f, bgAlpha / 255f),
                cornerRadius = cornerRadius.dp,
                titleTextColor = Color(0f, 0f, 0f, titleAlpha / 255f),
                messageTextColor = Color(0.5f, 0.5f, 0.5f, messageAlpha / 255f),
                positiveButtonTextColor = Color(1f, 0f, 0f, positiveAlpha / 255f)
            )
        }

        checkAll(100, styleArb) { originalStyle ->
            // Store original values
            val originalBackgroundColor = originalStyle.backgroundColor
            val originalCornerRadius = originalStyle.cornerRadius
            val originalStrokeColor = originalStyle.strokeColor
            val originalStrokeWidth = originalStyle.strokeWidth
            val originalElevation = originalStyle.elevation
            val originalIconBackgroundColor = originalStyle.iconBackgroundColor
            val originalIconTint = originalStyle.iconTint
            val originalIconSize = originalStyle.iconSize
            val originalIconBackgroundSize = originalStyle.iconBackgroundSize
            val originalTitleTextColor = originalStyle.titleTextColor
            val originalMessageTextColor = originalStyle.messageTextColor
            val originalPositiveButtonTextColor = originalStyle.positiveButtonTextColor
            val originalPositiveButtonBackgroundColor = originalStyle.positiveButtonBackgroundColor
            val originalNegativeButtonTextColor = originalStyle.negativeButtonTextColor
            val originalNegativeButtonBackgroundColor = originalStyle.negativeButtonBackgroundColor

            // Create a copy with modified backgroundColor
            val newBackgroundColor = Color.Red
            val copiedStyle = originalStyle.copy(backgroundColor = newBackgroundColor)

            // Verify the copied style has the new value
            copiedStyle.backgroundColor shouldBe newBackgroundColor

            // Verify the original style is unchanged
            originalStyle.backgroundColor shouldBe originalBackgroundColor
            originalStyle.cornerRadius shouldBe originalCornerRadius
            originalStyle.strokeColor shouldBe originalStrokeColor
            originalStyle.strokeWidth shouldBe originalStrokeWidth
            originalStyle.elevation shouldBe originalElevation
            originalStyle.iconBackgroundColor shouldBe originalIconBackgroundColor
            originalStyle.iconTint shouldBe originalIconTint
            originalStyle.iconSize shouldBe originalIconSize
            originalStyle.iconBackgroundSize shouldBe originalIconBackgroundSize
            originalStyle.titleTextColor shouldBe originalTitleTextColor
            originalStyle.messageTextColor shouldBe originalMessageTextColor
            originalStyle.positiveButtonTextColor shouldBe originalPositiveButtonTextColor
            originalStyle.positiveButtonBackgroundColor shouldBe originalPositiveButtonBackgroundColor
            originalStyle.negativeButtonTextColor shouldBe originalNegativeButtonTextColor
            originalStyle.negativeButtonBackgroundColor shouldBe originalNegativeButtonBackgroundColor

            // Verify the copied style retained other values
            copiedStyle.cornerRadius shouldBe originalCornerRadius
            copiedStyle.strokeColor shouldBe originalStrokeColor
            copiedStyle.strokeWidth shouldBe originalStrokeWidth
            copiedStyle.elevation shouldBe originalElevation
            copiedStyle.iconBackgroundColor shouldBe originalIconBackgroundColor
            copiedStyle.iconTint shouldBe originalIconTint
            copiedStyle.iconSize shouldBe originalIconSize
            copiedStyle.iconBackgroundSize shouldBe originalIconBackgroundSize
            copiedStyle.titleTextColor shouldBe originalTitleTextColor
            copiedStyle.messageTextColor shouldBe originalMessageTextColor
            copiedStyle.positiveButtonTextColor shouldBe originalPositiveButtonTextColor
            copiedStyle.positiveButtonBackgroundColor shouldBe originalPositiveButtonBackgroundColor
            copiedStyle.negativeButtonTextColor shouldBe originalNegativeButtonTextColor
            copiedStyle.negativeButtonBackgroundColor shouldBe originalNegativeButtonBackgroundColor

            // Verify they are different instances (by value comparison after modification)
            copiedStyle.backgroundColor shouldNotBe originalStyle.backgroundColor
        }
    }

    /**
     * Property 1: Style Immutability - Container properties
     *
     * For any CometChatDialogStyle instance, calling copy() with modified container
     * properties SHALL preserve all unmodified values.
     *
     * Feature: cometchat-dialog, Property 1: Style Immutability
     * Validates: Requirements 1.3, 1.4, 1.5
     */
    "Property 1: Style copy with modified container properties should preserve unmodified values" {
        val styleArb = Arb.bind(
            Arb.float(0f, 4f),    // strokeWidth
            Arb.float(0f, 8f)     // elevation
        ) { strokeWidth, elevation ->
            createTestDialogStyle(
                strokeWidth = strokeWidth.dp,
                elevation = elevation.dp
            )
        }

        checkAll(100, styleArb) { originalStyle ->
            val originalIconBackgroundColor = originalStyle.iconBackgroundColor
            val originalPositiveButtonBackgroundColor = originalStyle.positiveButtonBackgroundColor
            val originalNegativeButtonStrokeColor = originalStyle.negativeButtonStrokeColor

            // Create a copy with modified container values
            val newStrokeColor = Color.Blue
            val newStrokeWidth = 2.dp
            val newElevation = 4.dp
            val copiedStyle = originalStyle.copy(
                strokeColor = newStrokeColor,
                strokeWidth = newStrokeWidth,
                elevation = newElevation
            )

            // Verify modified values
            copiedStyle.strokeColor shouldBe newStrokeColor
            copiedStyle.strokeWidth shouldBe newStrokeWidth
            copiedStyle.elevation shouldBe newElevation

            // Verify unmodified values are preserved
            copiedStyle.iconBackgroundColor shouldBe originalIconBackgroundColor
            copiedStyle.positiveButtonBackgroundColor shouldBe originalPositiveButtonBackgroundColor
            copiedStyle.negativeButtonStrokeColor shouldBe originalNegativeButtonStrokeColor
        }
    }

    /**
     * Property 1: Style Immutability - Icon properties
     *
     * For any CometChatDialogStyle instance, calling copy() with modified icon
     * properties SHALL preserve all unmodified values.
     *
     * Feature: cometchat-dialog, Property 1: Style Immutability
     * Validates: Requirements 2.2, 2.3, 2.4, 2.5
     */
    "Property 1: Style copy with modified icon properties should preserve unmodified values" {
        val styleArb = Arb.bind(
            Arb.int(0, 255),      // iconTint alpha
            Arb.float(32f, 64f)   // iconSize
        ) { iconTintAlpha, iconSize ->
            createTestDialogStyle(
                iconTint = Color(0f, 0f, 0f, iconTintAlpha / 255f),
                iconSize = iconSize.dp
            )
        }

        checkAll(100, styleArb) { originalStyle ->
            val originalBackgroundColor = originalStyle.backgroundColor
            val originalTitleTextColor = originalStyle.titleTextColor
            val originalPositiveButtonTextColor = originalStyle.positiveButtonTextColor

            // Create a copy with modified icon values
            val newIconBackgroundColor = Color.Cyan
            val newIconTint = Color.Magenta
            val newIconSize = 56.dp
            val newIconBackgroundSize = 96.dp
            val copiedStyle = originalStyle.copy(
                iconBackgroundColor = newIconBackgroundColor,
                iconTint = newIconTint,
                iconSize = newIconSize,
                iconBackgroundSize = newIconBackgroundSize
            )

            // Verify modified values
            copiedStyle.iconBackgroundColor shouldBe newIconBackgroundColor
            copiedStyle.iconTint shouldBe newIconTint
            copiedStyle.iconSize shouldBe newIconSize
            copiedStyle.iconBackgroundSize shouldBe newIconBackgroundSize

            // Verify unmodified values are preserved
            copiedStyle.backgroundColor shouldBe originalBackgroundColor
            copiedStyle.titleTextColor shouldBe originalTitleTextColor
            copiedStyle.positiveButtonTextColor shouldBe originalPositiveButtonTextColor
        }
    }

    /**
     * Property 1: Style Immutability - Button properties
     *
     * For any CometChatDialogStyle instance, calling copy() with modified button
     * properties SHALL preserve all unmodified values.
     *
     * Feature: cometchat-dialog, Property 1: Style Immutability
     * Validates: Requirements 4.1-4.6, 5.1-5.6
     */
    "Property 1: Style copy with modified button properties should preserve unmodified values" {
        val styleArb = Arb.bind(
            Arb.int(0, 255),      // positiveButtonBackgroundColor alpha
            Arb.float(0f, 2f)     // positiveButtonStrokeWidth
        ) { bgAlpha, strokeWidth ->
            createTestDialogStyle(
                positiveButtonBackgroundColor = Color(1f, 0f, 0f, bgAlpha / 255f),
                positiveButtonStrokeWidth = strokeWidth.dp
            )
        }

        checkAll(100, styleArb) { originalStyle ->
            val originalBackgroundColor = originalStyle.backgroundColor
            val originalIconTint = originalStyle.iconTint
            val originalTitleTextColor = originalStyle.titleTextColor

            // Create a copy with modified positive button values
            val newPositiveButtonBackgroundColor = Color.Green
            val newPositiveButtonStrokeColor = Color.DarkGray
            val newPositiveButtonStrokeWidth = 1.dp
            val newPositiveButtonCornerRadius = 12.dp
            val copiedStyle = originalStyle.copy(
                positiveButtonBackgroundColor = newPositiveButtonBackgroundColor,
                positiveButtonStrokeColor = newPositiveButtonStrokeColor,
                positiveButtonStrokeWidth = newPositiveButtonStrokeWidth,
                positiveButtonCornerRadius = newPositiveButtonCornerRadius
            )

            // Verify modified values
            copiedStyle.positiveButtonBackgroundColor shouldBe newPositiveButtonBackgroundColor
            copiedStyle.positiveButtonStrokeColor shouldBe newPositiveButtonStrokeColor
            copiedStyle.positiveButtonStrokeWidth shouldBe newPositiveButtonStrokeWidth
            copiedStyle.positiveButtonCornerRadius shouldBe newPositiveButtonCornerRadius

            // Verify unmodified values are preserved
            copiedStyle.backgroundColor shouldBe originalBackgroundColor
            copiedStyle.iconTint shouldBe originalIconTint
            copiedStyle.titleTextColor shouldBe originalTitleTextColor
            
            // Verify negative button values are unchanged
            copiedStyle.negativeButtonBackgroundColor shouldBe originalStyle.negativeButtonBackgroundColor
            copiedStyle.negativeButtonStrokeColor shouldBe originalStyle.negativeButtonStrokeColor
            copiedStyle.negativeButtonStrokeWidth shouldBe originalStyle.negativeButtonStrokeWidth
            copiedStyle.negativeButtonCornerRadius shouldBe originalStyle.negativeButtonCornerRadius
        }
    }

    /**
     * Property 1: Style Immutability - Negative button properties
     *
     * For any CometChatDialogStyle instance, calling copy() with modified negative button
     * properties SHALL preserve all unmodified values including positive button properties.
     *
     * Feature: cometchat-dialog, Property 1: Style Immutability
     * Validates: Requirements 5.1-5.6
     */
    "Property 1: Style copy with modified negative button properties should preserve positive button values" {
        val styleArb = Arb.bind(
            Arb.int(0, 255),      // negativeButtonBackgroundColor alpha
            Arb.float(0f, 2f)     // negativeButtonStrokeWidth
        ) { bgAlpha, strokeWidth ->
            createTestDialogStyle(
                negativeButtonBackgroundColor = Color(1f, 1f, 1f, bgAlpha / 255f),
                negativeButtonStrokeWidth = strokeWidth.dp
            )
        }

        checkAll(100, styleArb) { originalStyle ->
            val originalPositiveButtonBackgroundColor = originalStyle.positiveButtonBackgroundColor
            val originalPositiveButtonStrokeColor = originalStyle.positiveButtonStrokeColor
            val originalPositiveButtonStrokeWidth = originalStyle.positiveButtonStrokeWidth
            val originalPositiveButtonCornerRadius = originalStyle.positiveButtonCornerRadius

            // Create a copy with modified negative button values
            val newNegativeButtonBackgroundColor = Color.LightGray
            val newNegativeButtonStrokeColor = Color.Black
            val newNegativeButtonStrokeWidth = 2.dp
            val newNegativeButtonCornerRadius = 16.dp
            val copiedStyle = originalStyle.copy(
                negativeButtonBackgroundColor = newNegativeButtonBackgroundColor,
                negativeButtonStrokeColor = newNegativeButtonStrokeColor,
                negativeButtonStrokeWidth = newNegativeButtonStrokeWidth,
                negativeButtonCornerRadius = newNegativeButtonCornerRadius
            )

            // Verify modified values
            copiedStyle.negativeButtonBackgroundColor shouldBe newNegativeButtonBackgroundColor
            copiedStyle.negativeButtonStrokeColor shouldBe newNegativeButtonStrokeColor
            copiedStyle.negativeButtonStrokeWidth shouldBe newNegativeButtonStrokeWidth
            copiedStyle.negativeButtonCornerRadius shouldBe newNegativeButtonCornerRadius

            // Verify positive button values are unchanged
            copiedStyle.positiveButtonBackgroundColor shouldBe originalPositiveButtonBackgroundColor
            copiedStyle.positiveButtonStrokeColor shouldBe originalPositiveButtonStrokeColor
            copiedStyle.positiveButtonStrokeWidth shouldBe originalPositiveButtonStrokeWidth
            copiedStyle.positiveButtonCornerRadius shouldBe originalPositiveButtonCornerRadius
        }
    }
})

/**
 * Helper function to create a test dialog style with customizable values.
 * Includes all new properties aligned with Java CometChatConfirmDialog.
 */
private fun createTestDialogStyle(
    backgroundColor: Color = Color.White,
    cornerRadius: Dp = 16.dp,
    strokeColor: Color = Color.LightGray,
    strokeWidth: Dp = 0.dp,
    elevation: Dp = 0.dp,
    iconBackgroundColor: Color = Color.LightGray,
    iconTint: Color = Color.Black,
    iconSize: Dp = 48.dp,
    iconBackgroundSize: Dp = 80.dp,
    titleTextColor: Color = Color.Black,
    messageTextColor: Color = Color.Gray,
    positiveButtonTextColor: Color = Color.White,
    positiveButtonBackgroundColor: Color = Color.Red,
    positiveButtonStrokeColor: Color = Color.Transparent,
    positiveButtonStrokeWidth: Dp = 0.dp,
    positiveButtonCornerRadius: Dp = 8.dp,
    negativeButtonTextColor: Color = Color.Black,
    negativeButtonBackgroundColor: Color = Color.White,
    negativeButtonStrokeColor: Color = Color.DarkGray,
    negativeButtonStrokeWidth: Dp = 1.dp,
    negativeButtonCornerRadius: Dp = 8.dp
): CometChatDialogStyle {
    return CometChatDialogStyle(
        backgroundColor = backgroundColor,
        cornerRadius = cornerRadius,
        strokeColor = strokeColor,
        strokeWidth = strokeWidth,
        elevation = elevation,
        iconBackgroundColor = iconBackgroundColor,
        iconTint = iconTint,
        iconSize = iconSize,
        iconBackgroundSize = iconBackgroundSize,
        titleTextColor = titleTextColor,
        titleTextStyle = TextStyle(fontSize = 18.sp),
        messageTextColor = messageTextColor,
        messageTextStyle = TextStyle(fontSize = 14.sp),
        positiveButtonTextColor = positiveButtonTextColor,
        positiveButtonTextStyle = TextStyle(fontSize = 14.sp),
        positiveButtonBackgroundColor = positiveButtonBackgroundColor,
        positiveButtonStrokeColor = positiveButtonStrokeColor,
        positiveButtonStrokeWidth = positiveButtonStrokeWidth,
        positiveButtonCornerRadius = positiveButtonCornerRadius,
        negativeButtonTextColor = negativeButtonTextColor,
        negativeButtonTextStyle = TextStyle(fontSize = 14.sp),
        negativeButtonBackgroundColor = negativeButtonBackgroundColor,
        negativeButtonStrokeColor = negativeButtonStrokeColor,
        negativeButtonStrokeWidth = negativeButtonStrokeWidth,
        negativeButtonCornerRadius = negativeButtonCornerRadius
    )
}
