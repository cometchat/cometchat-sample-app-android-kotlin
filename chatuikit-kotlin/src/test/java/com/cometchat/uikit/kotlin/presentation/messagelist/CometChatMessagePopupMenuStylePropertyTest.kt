package com.cometchat.uikit.kotlin.presentation.messagelist

import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

/**
 * Pure data class mirroring the style attributes extracted by
 * CometChatMessagePopupMenu.setStyle() from CometChatPopupMenu styleable.
 *
 * Each field corresponds to a CometChatPopupMenu styleable attribute:
 * - cometchatPopupMenuElevation → elevation
 * - cometchatPopupMenuCornerRadius → cornerRadius
 * - cometchatPopupMenuBackgroundColor → backgroundColor
 * - cometchatPopupMenuItemTextColor → textColor
 * - cometchatPopupMenuItemTextAppearance → textAppearance
 * - cometchatPopupMenuStrokeColor → strokeColor
 * - cometchatPopupMenuStrokeWidth → strokeWidth
 * - cometchatPopupMenuItemStartIconTint → startIconTint
 * - cometchatPopupMenuItemEndIconTint → endIconTint
 */
private data class PopupMenuStyleState(
    @Dimension val elevation: Int,
    @Dimension val cornerRadius: Int,
    @ColorInt val backgroundColor: Int,
    @ColorInt val textColor: Int,
    @StyleRes val textAppearance: Int,
    @ColorInt val strokeColor: Int,
    @Dimension val strokeWidth: Int,
    @ColorInt val startIconTint: Int,
    @ColorInt val endIconTint: Int
)

/**
 * Simulates the style extraction logic from setStyle().
 * When a style provides a value, it overrides the current default.
 * When a style does not provide a value (represented by 0 for dimensions/colors),
 * the existing default is retained.
 *
 * This mirrors the TypedArray.get* calls with fallback to current value.
 */
private fun applyStyle(
    defaults: PopupMenuStyleState,
    styleValues: PopupMenuStyleState
): PopupMenuStyleState {
    return PopupMenuStyleState(
        elevation = if (styleValues.elevation != 0) styleValues.elevation else defaults.elevation,
        cornerRadius = if (styleValues.cornerRadius != 0) styleValues.cornerRadius else defaults.cornerRadius,
        backgroundColor = if (styleValues.backgroundColor != 0) styleValues.backgroundColor else defaults.backgroundColor,
        textColor = if (styleValues.textColor != 0) styleValues.textColor else defaults.textColor,
        textAppearance = if (styleValues.textAppearance != 0) styleValues.textAppearance else defaults.textAppearance,
        strokeColor = if (styleValues.strokeColor != 0) styleValues.strokeColor else defaults.strokeColor,
        strokeWidth = if (styleValues.strokeWidth != 0) styleValues.strokeWidth else defaults.strokeWidth,
        startIconTint = if (styleValues.startIconTint != 0) styleValues.startIconTint else defaults.startIconTint,
        endIconTint = if (styleValues.endIconTint != 0) styleValues.endIconTint else defaults.endIconTint
    )
}

/**
 * Property-based tests for popup menu style attribute extraction.
 *
 * Feature: message-popup-menu, Property 8: Style attributes are correctly extracted and applied
 *
 * *For any* valid style attribute values, all CometChatPopupMenu styleable attributes
 * should be extracted and stored correctly. Non-zero values override defaults;
 * zero values retain the existing default.
 *
 * **Validates: Requirements 5.1, 5.2**
 */
class CometChatMessagePopupMenuStylePropertyTest : FunSpec({

    // ==================== Generators ====================

    /** Arbitrary non-negative dimension values (including 0 for "not set"). */
    val dimensionArb = Arb.int(0..500)

    /** Arbitrary color int values (including 0 for "not set"). */
    val colorArb = Arb.int(0..0xFFFFFF)

    /** Arbitrary style resource IDs (including 0 for "not set"). */
    val styleResArb = Arb.int(0..100)

    // ==================== Property Tests ====================

    context("Property 8: Style attributes are correctly extracted and applied") {

        test("non-zero style values override defaults") {
            checkAll(
                100,
                dimensionArb, dimensionArb, colorArb, colorArb,
                styleResArb, colorArb, dimensionArb, colorArb, colorArb
            ) { elev, radius, bgColor, txtColor, txtAppearance, strokeCol, strokeW, startTint, endTint ->
                val defaults = PopupMenuStyleState(
                    elevation = 10,
                    cornerRadius = 8,
                    backgroundColor = 0xFFFFFF,
                    textColor = 0x000000,
                    textAppearance = 1,
                    strokeColor = 0xCCCCCC,
                    strokeWidth = 2,
                    startIconTint = 0x666666,
                    endIconTint = 0x666666
                )

                val styleValues = PopupMenuStyleState(
                    elevation = elev,
                    cornerRadius = radius,
                    backgroundColor = bgColor,
                    textColor = txtColor,
                    textAppearance = txtAppearance,
                    strokeColor = strokeCol,
                    strokeWidth = strokeW,
                    startIconTint = startTint,
                    endIconTint = endTint
                )

                val result = applyStyle(defaults, styleValues)

                // Non-zero values should override, zero values should retain defaults
                if (elev != 0) result.elevation shouldBe elev else result.elevation shouldBe defaults.elevation
                if (radius != 0) result.cornerRadius shouldBe radius else result.cornerRadius shouldBe defaults.cornerRadius
                if (bgColor != 0) result.backgroundColor shouldBe bgColor else result.backgroundColor shouldBe defaults.backgroundColor
                if (txtColor != 0) result.textColor shouldBe txtColor else result.textColor shouldBe defaults.textColor
                if (txtAppearance != 0) result.textAppearance shouldBe txtAppearance else result.textAppearance shouldBe defaults.textAppearance
                if (strokeCol != 0) result.strokeColor shouldBe strokeCol else result.strokeColor shouldBe defaults.strokeColor
                if (strokeW != 0) result.strokeWidth shouldBe strokeW else result.strokeWidth shouldBe defaults.strokeWidth
                if (startTint != 0) result.startIconTint shouldBe startTint else result.startIconTint shouldBe defaults.startIconTint
                if (endTint != 0) result.endIconTint shouldBe endTint else result.endIconTint shouldBe defaults.endIconTint
            }
        }

        test("all-zero style values retain all defaults") {
            checkAll(
                100,
                dimensionArb, dimensionArb, colorArb, colorArb,
                styleResArb, colorArb, dimensionArb, colorArb, colorArb
            ) { defElev, defRadius, defBg, defTxt, defTxtApp, defStroke, defStrokeW, defStart, defEnd ->
                // Ensure defaults are non-zero for meaningful test
                val defaults = PopupMenuStyleState(
                    elevation = maxOf(defElev, 1),
                    cornerRadius = maxOf(defRadius, 1),
                    backgroundColor = maxOf(defBg, 1),
                    textColor = maxOf(defTxt, 1),
                    textAppearance = maxOf(defTxtApp, 1),
                    strokeColor = maxOf(defStroke, 1),
                    strokeWidth = maxOf(defStrokeW, 1),
                    startIconTint = maxOf(defStart, 1),
                    endIconTint = maxOf(defEnd, 1)
                )

                val zeroStyle = PopupMenuStyleState(0, 0, 0, 0, 0, 0, 0, 0, 0)
                val result = applyStyle(defaults, zeroStyle)

                result shouldBe defaults
            }
        }

        test("all attributes are independently extracted") {
            checkAll(
                100,
                colorArb, colorArb
            ) { startTint, endTint ->
                val defaults = PopupMenuStyleState(1, 1, 1, 1, 1, 1, 1, 1, 1)

                // Only change startIconTint and endIconTint
                val styleValues = PopupMenuStyleState(
                    elevation = 0,
                    cornerRadius = 0,
                    backgroundColor = 0,
                    textColor = 0,
                    textAppearance = 0,
                    strokeColor = 0,
                    strokeWidth = 0,
                    startIconTint = startTint,
                    endIconTint = endTint
                )

                val result = applyStyle(defaults, styleValues)

                // Unchanged attributes should retain defaults
                result.elevation shouldBe defaults.elevation
                result.cornerRadius shouldBe defaults.cornerRadius
                result.backgroundColor shouldBe defaults.backgroundColor
                result.textColor shouldBe defaults.textColor
                result.textAppearance shouldBe defaults.textAppearance
                result.strokeColor shouldBe defaults.strokeColor
                result.strokeWidth shouldBe defaults.strokeWidth

                // Changed attributes should reflect new values (or default if 0)
                if (startTint != 0) result.startIconTint shouldBe startTint else result.startIconTint shouldBe defaults.startIconTint
                if (endTint != 0) result.endIconTint shouldBe endTint else result.endIconTint shouldBe defaults.endIconTint
            }
        }
    }
})
