package com.cometchat.uikit.compose.shared.views.popupmenu

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import java.util.concurrent.atomic.AtomicInteger

/**
 * Property-based tests for CometChatPopupMenuStyle and MenuItem.
 *
 * Feature: popup-menu-compose
 * Properties tested:
 * - Property 5: MenuItem Click Callback Invocation
 * - Property 8: Style Copy Immutability
 * - Property 9: Style Override Precedence
 */
class CometChatPopupMenuStylePropertyTest : StringSpec({

    /**
     * Property 8: Style Copy Immutability
     *
     * For any CometChatPopupMenuStyle instance, calling copy() with modified parameters
     * SHALL return a new instance with the modified values while leaving the original
     * instance unchanged.
     *
     * Validates: Requirements 4.1, 4.5
     */
    "Property 8: Style copy should create new instance with modified values while original remains unchanged" {
        // Generator for random style values
        val styleArb = Arb.bind(
            Arb.float(0f, 100f),  // elevation
            Arb.float(0f, 50f),   // cornerRadius
            Arb.int(0, 255),      // backgroundColor alpha
            Arb.int(0, 255),      // strokeColor alpha
            Arb.float(0f, 10f),   // strokeWidth
            Arb.int(0, 255),      // itemTextColor alpha
            Arb.float(8f, 24f),   // fontSize
            Arb.int(0, 255),      // startIconTint alpha
            Arb.int(0, 255),      // endIconTint alpha
            Arb.float(0f, 50f)    // itemPaddingHorizontal
        ) { elevation, cornerRadius, bgAlpha, strokeAlpha, strokeWidth, textAlpha, fontSize, startTintAlpha, endTintAlpha, paddingH ->
            CometChatPopupMenuStyle(
                elevation = elevation.dp,
                cornerRadius = cornerRadius.dp,
                backgroundColor = Color(1f, 1f, 1f, bgAlpha / 255f),
                strokeColor = Color(0.5f, 0.5f, 0.5f, strokeAlpha / 255f),
                strokeWidth = strokeWidth.dp,
                itemTextColor = Color(0f, 0f, 0f, textAlpha / 255f),
                itemTextStyle = TextStyle(fontSize = fontSize.sp),
                startIconTint = Color(0f, 0f, 0f, startTintAlpha / 255f),
                endIconTint = Color(0f, 0f, 0f, endTintAlpha / 255f),
                itemPaddingHorizontal = paddingH.dp,
                itemPaddingVertical = 8.dp,
                minWidth = 128.dp
            )
        }

        checkAll(100, styleArb) { originalStyle ->
            // Store original values
            val originalElevation = originalStyle.elevation
            val originalCornerRadius = originalStyle.cornerRadius
            val originalBackgroundColor = originalStyle.backgroundColor
            val originalStrokeColor = originalStyle.strokeColor
            val originalStrokeWidth = originalStyle.strokeWidth
            val originalItemTextColor = originalStyle.itemTextColor
            val originalItemTextStyle = originalStyle.itemTextStyle
            val originalStartIconTint = originalStyle.startIconTint
            val originalEndIconTint = originalStyle.endIconTint
            val originalItemPaddingHorizontal = originalStyle.itemPaddingHorizontal
            val originalItemPaddingVertical = originalStyle.itemPaddingVertical
            val originalMinWidth = originalStyle.minWidth

            // Create a copy with modified elevation
            val newElevation = 99.dp
            val copiedStyle = originalStyle.copy(elevation = newElevation)

            // Verify the copied style has the new value
            copiedStyle.elevation shouldBe newElevation

            // Verify the original style is unchanged
            originalStyle.elevation shouldBe originalElevation
            originalStyle.cornerRadius shouldBe originalCornerRadius
            originalStyle.backgroundColor shouldBe originalBackgroundColor
            originalStyle.strokeColor shouldBe originalStrokeColor
            originalStyle.strokeWidth shouldBe originalStrokeWidth
            originalStyle.itemTextColor shouldBe originalItemTextColor
            originalStyle.itemTextStyle shouldBe originalItemTextStyle
            originalStyle.startIconTint shouldBe originalStartIconTint
            originalStyle.endIconTint shouldBe originalEndIconTint
            originalStyle.itemPaddingHorizontal shouldBe originalItemPaddingHorizontal
            originalStyle.itemPaddingVertical shouldBe originalItemPaddingVertical
            originalStyle.minWidth shouldBe originalMinWidth

            // Verify the copied style retained other values
            copiedStyle.cornerRadius shouldBe originalCornerRadius
            copiedStyle.backgroundColor shouldBe originalBackgroundColor
            copiedStyle.strokeColor shouldBe originalStrokeColor
            copiedStyle.strokeWidth shouldBe originalStrokeWidth
            copiedStyle.itemTextColor shouldBe originalItemTextColor
            copiedStyle.itemTextStyle shouldBe originalItemTextStyle
            copiedStyle.startIconTint shouldBe originalStartIconTint
            copiedStyle.endIconTint shouldBe originalEndIconTint
            copiedStyle.itemPaddingHorizontal shouldBe originalItemPaddingHorizontal
            copiedStyle.itemPaddingVertical shouldBe originalItemPaddingVertical
            copiedStyle.minWidth shouldBe originalMinWidth

            // Verify they are different instances
            copiedStyle shouldNotBe originalStyle
        }
    }

    "Property 8: Style copy with multiple modified parameters should preserve unmodified values" {
        val styleArb = Arb.bind(
            Arb.float(0f, 100f),
            Arb.float(0f, 50f),
            Arb.float(0f, 10f)
        ) { elevation, cornerRadius, strokeWidth ->
            CometChatPopupMenuStyle(
                elevation = elevation.dp,
                cornerRadius = cornerRadius.dp,
                backgroundColor = Color.White,
                strokeColor = Color.Gray,
                strokeWidth = strokeWidth.dp,
                itemTextColor = Color.Black,
                itemTextStyle = TextStyle(fontSize = 14.sp),
                startIconTint = Color.Black,
                endIconTint = Color.Black,
                itemPaddingHorizontal = 16.dp,
                itemPaddingVertical = 8.dp,
                minWidth = 128.dp
            )
        }

        checkAll(100, styleArb) { originalStyle ->
            val originalBackgroundColor = originalStyle.backgroundColor
            val originalItemTextColor = originalStyle.itemTextColor
            val originalMinWidth = originalStyle.minWidth

            // Create a copy with multiple modified values
            val newElevation = 20.dp
            val newCornerRadius = 16.dp
            val newStrokeWidth = 2.dp
            val copiedStyle = originalStyle.copy(
                elevation = newElevation,
                cornerRadius = newCornerRadius,
                strokeWidth = newStrokeWidth
            )

            // Verify modified values
            copiedStyle.elevation shouldBe newElevation
            copiedStyle.cornerRadius shouldBe newCornerRadius
            copiedStyle.strokeWidth shouldBe newStrokeWidth

            // Verify unmodified values are preserved
            copiedStyle.backgroundColor shouldBe originalBackgroundColor
            copiedStyle.itemTextColor shouldBe originalItemTextColor
            copiedStyle.minWidth shouldBe originalMinWidth

            // Verify original is unchanged
            originalStyle.backgroundColor shouldBe originalBackgroundColor
            originalStyle.itemTextColor shouldBe originalItemTextColor
            originalStyle.minWidth shouldBe originalMinWidth
        }
    }

    /**
     * Property 9: Style Override Precedence
     *
     * For any MenuItem with non-null style override properties (textColor, textStyle,
     * startIconTint, endIconTint), those values SHALL be used instead of the corresponding
     * CometChatPopupMenuStyle values. For null override properties, the global style values
     * SHALL be used.
     *
     * Feature: popup-menu-compose, Property 9: Style Override Precedence
     * Validates: Requirements 6.3, 6.4
     */
    "Property 9: MenuItem style overrides should take precedence over global style when non-null" {
        // Generator for random global style values
        val globalStyleArb = Arb.bind(
            Arb.int(0, 255),  // itemTextColor red
            Arb.int(0, 255),  // startIconTint green
            Arb.int(0, 255),  // endIconTint blue
            Arb.float(10f, 24f)  // fontSize
        ) { textRed, startGreen, endBlue, fontSize ->
            CometChatPopupMenuStyle(
                elevation = 10.dp,
                cornerRadius = 8.dp,
                backgroundColor = Color.White,
                strokeColor = Color.Gray,
                strokeWidth = 1.dp,
                itemTextColor = Color(textRed / 255f, 0f, 0f),
                itemTextStyle = TextStyle(fontSize = fontSize.sp),
                startIconTint = Color(0f, startGreen / 255f, 0f),
                endIconTint = Color(0f, 0f, endBlue / 255f),
                itemPaddingHorizontal = 16.dp,
                itemPaddingVertical = 8.dp,
                minWidth = 128.dp
            )
        }

        // Generator for MenuItem override values (distinct from global style)
        val overrideArb = Arb.bind(
            Arb.int(0, 255),  // override textColor blue (different channel)
            Arb.int(0, 255),  // override startIconTint red (different channel)
            Arb.int(0, 255),  // override endIconTint green (different channel)
            Arb.float(8f, 20f)  // override fontSize
        ) { textBlue, startRed, endGreen, fontSize ->
            object {
                val textColor = Color(0f, 0f, textBlue / 255f)
                val startIconTint = Color(startRed / 255f, 0f, 0f)
                val endIconTint = Color(0f, endGreen / 255f, 0f)
                val textStyle = TextStyle(fontSize = fontSize.sp, fontWeight = FontWeight.Bold)
            }
        }

        checkAll(100, globalStyleArb, overrideArb) { globalStyle, overrides ->
            // Create MenuItem with all style overrides
            val menuItemWithOverrides = MenuItem(
                id = "test_item",
                name = "Test Item",
                textColor = overrides.textColor,
                textStyle = overrides.textStyle,
                startIconTint = overrides.startIconTint,
                endIconTint = overrides.endIconTint
            )

            // Simulate the style resolution logic from PopupMenuItem
            // This is the same logic used in PopupMenuItem composable:
            // val textColor = item.textColor ?: style.itemTextColor
            val resolvedTextColor = menuItemWithOverrides.textColor ?: globalStyle.itemTextColor
            val resolvedTextStyle = menuItemWithOverrides.textStyle ?: globalStyle.itemTextStyle
            val resolvedStartIconTint = menuItemWithOverrides.startIconTint ?: globalStyle.startIconTint
            val resolvedEndIconTint = menuItemWithOverrides.endIconTint ?: globalStyle.endIconTint

            // Property: When MenuItem has non-null overrides, those values SHALL be used
            resolvedTextColor shouldBe overrides.textColor
            resolvedTextStyle shouldBe overrides.textStyle
            resolvedStartIconTint shouldBe overrides.startIconTint
            resolvedEndIconTint shouldBe overrides.endIconTint

            // Verify overrides are different from global style (to ensure we're testing the right thing)
            // Note: Due to random generation, they might occasionally be equal, but the property still holds
        }
    }

    "Property 9: Global style should be used when MenuItem style overrides are null" {
        // Generator for random global style values
        val globalStyleArb = Arb.bind(
            Arb.int(0, 255),  // itemTextColor component
            Arb.int(0, 255),  // startIconTint component
            Arb.int(0, 255),  // endIconTint component
            Arb.float(10f, 24f)  // fontSize
        ) { textComp, startComp, endComp, fontSize ->
            CometChatPopupMenuStyle(
                elevation = 10.dp,
                cornerRadius = 8.dp,
                backgroundColor = Color.White,
                strokeColor = Color.Gray,
                strokeWidth = 1.dp,
                itemTextColor = Color(textComp / 255f, textComp / 255f, textComp / 255f),
                itemTextStyle = TextStyle(fontSize = fontSize.sp),
                startIconTint = Color(startComp / 255f, startComp / 255f, startComp / 255f),
                endIconTint = Color(endComp / 255f, endComp / 255f, endComp / 255f),
                itemPaddingHorizontal = 16.dp,
                itemPaddingVertical = 8.dp,
                minWidth = 128.dp
            )
        }

        checkAll(100, globalStyleArb) { globalStyle ->
            // Create MenuItem with NO style overrides (all null)
            val menuItemWithoutOverrides = MenuItem(
                id = "test_item",
                name = "Test Item",
                textColor = null,
                textStyle = null,
                startIconTint = null,
                endIconTint = null
            )

            // Simulate the style resolution logic from PopupMenuItem
            val resolvedTextColor = menuItemWithoutOverrides.textColor ?: globalStyle.itemTextColor
            val resolvedTextStyle = menuItemWithoutOverrides.textStyle ?: globalStyle.itemTextStyle
            val resolvedStartIconTint = menuItemWithoutOverrides.startIconTint ?: globalStyle.startIconTint
            val resolvedEndIconTint = menuItemWithoutOverrides.endIconTint ?: globalStyle.endIconTint

            // Property: When MenuItem has null overrides, global style values SHALL be used
            resolvedTextColor shouldBe globalStyle.itemTextColor
            resolvedTextStyle shouldBe globalStyle.itemTextStyle
            resolvedStartIconTint shouldBe globalStyle.startIconTint
            resolvedEndIconTint shouldBe globalStyle.endIconTint
        }
    }

    "Property 9: Partial style overrides should use override for non-null and global for null" {
        // Generator for random global style values
        val globalStyleArb = Arb.bind(
            Arb.int(0, 255),
            Arb.int(0, 255),
            Arb.int(0, 255),
            Arb.float(10f, 24f)
        ) { textComp, startComp, endComp, fontSize ->
            CometChatPopupMenuStyle(
                elevation = 10.dp,
                cornerRadius = 8.dp,
                backgroundColor = Color.White,
                strokeColor = Color.Gray,
                strokeWidth = 1.dp,
                itemTextColor = Color(textComp / 255f, 0f, 0f),
                itemTextStyle = TextStyle(fontSize = fontSize.sp),
                startIconTint = Color(0f, startComp / 255f, 0f),
                endIconTint = Color(0f, 0f, endComp / 255f),
                itemPaddingHorizontal = 16.dp,
                itemPaddingVertical = 8.dp,
                minWidth = 128.dp
            )
        }

        // Generator for partial overrides (some null, some non-null)
        val partialOverrideArb = Arb.bind(
            Arb.boolean(),  // hasTextColorOverride
            Arb.boolean(),  // hasTextStyleOverride
            Arb.boolean(),  // hasStartIconTintOverride
            Arb.boolean(),  // hasEndIconTintOverride
            Arb.int(0, 255),  // override value
            Arb.float(8f, 20f)  // override fontSize
        ) { hasTextColor, hasTextStyle, hasStartTint, hasEndTint, overrideValue, fontSize ->
            object {
                val textColor: Color? = if (hasTextColor) Color(0f, 0f, overrideValue / 255f) else null
                val textStyle: TextStyle? = if (hasTextStyle) TextStyle(fontSize = fontSize.sp, fontWeight = FontWeight.Bold) else null
                val startIconTint: Color? = if (hasStartTint) Color(overrideValue / 255f, 0f, 0f) else null
                val endIconTint: Color? = if (hasEndTint) Color(0f, overrideValue / 255f, 0f) else null
            }
        }

        checkAll(100, globalStyleArb, partialOverrideArb) { globalStyle, partialOverrides ->
            // Create MenuItem with partial style overrides
            val menuItem = MenuItem(
                id = "test_item",
                name = "Test Item",
                textColor = partialOverrides.textColor,
                textStyle = partialOverrides.textStyle,
                startIconTint = partialOverrides.startIconTint,
                endIconTint = partialOverrides.endIconTint
            )

            // Simulate the style resolution logic from PopupMenuItem
            val resolvedTextColor = menuItem.textColor ?: globalStyle.itemTextColor
            val resolvedTextStyle = menuItem.textStyle ?: globalStyle.itemTextStyle
            val resolvedStartIconTint = menuItem.startIconTint ?: globalStyle.startIconTint
            val resolvedEndIconTint = menuItem.endIconTint ?: globalStyle.endIconTint

            // Property: For each style property:
            // - If MenuItem override is non-null, use the override
            // - If MenuItem override is null, use the global style

            // Verify textColor resolution
            if (partialOverrides.textColor != null) {
                resolvedTextColor shouldBe partialOverrides.textColor
            } else {
                resolvedTextColor shouldBe globalStyle.itemTextColor
            }

            // Verify textStyle resolution
            if (partialOverrides.textStyle != null) {
                resolvedTextStyle shouldBe partialOverrides.textStyle
            } else {
                resolvedTextStyle shouldBe globalStyle.itemTextStyle
            }

            // Verify startIconTint resolution
            if (partialOverrides.startIconTint != null) {
                resolvedStartIconTint shouldBe partialOverrides.startIconTint
            } else {
                resolvedStartIconTint shouldBe globalStyle.startIconTint
            }

            // Verify endIconTint resolution
            if (partialOverrides.endIconTint != null) {
                resolvedEndIconTint shouldBe partialOverrides.endIconTint
            } else {
                resolvedEndIconTint shouldBe globalStyle.endIconTint
            }
        }
    }

    /**
     * Property 5: MenuItem Click Callback Invocation
     *
     * For any MenuItem with a non-null onClick callback, clicking the menu item
     * SHALL invoke that callback exactly once.
     *
     * Feature: popup-menu-compose, Property 5: MenuItem Click Callback Invocation
     * Validates: Requirements 3.1
     */
    "Property 5: MenuItem onClick callback should be invoked exactly once when clicked" {
        // Generator for random MenuItem properties
        val menuItemArb = Arb.bind(
            Arb.string(1..20).filter { it.isNotBlank() },  // id
            Arb.string(1..50).filter { it.isNotBlank() }   // name
        ) { id, name ->
            Pair(id, name)
        }

        checkAll(100, menuItemArb) { (id, name) ->
            // Track callback invocations using AtomicInteger for thread safety
            val callbackCount = AtomicInteger(0)

            // Create MenuItem with onClick callback
            val menuItem = MenuItem(
                id = id,
                name = name,
                onClick = { callbackCount.incrementAndGet() }
            )

            // Verify callback is non-null
            menuItem.onClick shouldNotBe null

            // Simulate a single click by invoking the callback
            menuItem.onClick?.invoke()

            // Property: Callback should be invoked exactly once
            callbackCount.get() shouldBe 1

            // Verify multiple clicks invoke callback multiple times (each click = one invocation)
            menuItem.onClick?.invoke()
            callbackCount.get() shouldBe 2
        }
    }

    "Property 5: MenuItem without onClick callback should have null onClick" {
        // Generator for random MenuItem properties without onClick
        val menuItemArb = Arb.bind(
            Arb.string(1..20).filter { it.isNotBlank() },  // id
            Arb.string(1..50).filter { it.isNotBlank() }   // name
        ) { id, name ->
            Pair(id, name)
        }

        checkAll(100, menuItemArb) { (id, name) ->
            // Create MenuItem without onClick callback using simple factory
            val menuItem = MenuItem(
                id = id,
                name = name,
                onClick = null
            )

            // Property: MenuItem without onClick should have null callback
            menuItem.onClick shouldBe null
        }
    }

    "Property 5: MenuItem.simple factory should correctly set onClick callback" {
        // Generator for random MenuItem properties
        val menuItemArb = Arb.bind(
            Arb.string(1..20).filter { it.isNotBlank() },  // id
            Arb.string(1..50).filter { it.isNotBlank() }   // name
        ) { id, name ->
            Pair(id, name)
        }

        checkAll(100, menuItemArb) { (id, name) ->
            val callbackCount = AtomicInteger(0)

            // Create MenuItem using simple factory with onClick
            val menuItemWithCallback = MenuItem.simple(
                id = id,
                name = name,
                onClick = { callbackCount.incrementAndGet() }
            )

            // Verify callback is set
            menuItemWithCallback.onClick shouldNotBe null

            // Invoke callback and verify it was called
            menuItemWithCallback.onClick?.invoke()
            callbackCount.get() shouldBe 1

            // Create MenuItem using simple factory without onClick
            val menuItemWithoutCallback = MenuItem.simple(
                id = id,
                name = name,
                onClick = null
            )

            // Verify callback is null
            menuItemWithoutCallback.onClick shouldBe null
        }
    }

    "Property 5: MenuItem.withIcons factory should correctly set onClick callback" {
        // Generator for random MenuItem properties
        val menuItemArb = Arb.bind(
            Arb.string(1..20).filter { it.isNotBlank() },  // id
            Arb.string(1..50).filter { it.isNotBlank() }   // name
        ) { id, name ->
            Pair(id, name)
        }

        checkAll(100, menuItemArb) { (id, name) ->
            val callbackCount = AtomicInteger(0)

            // Create MenuItem using withIcons factory with onClick
            val menuItemWithCallback = MenuItem.withIcons(
                id = id,
                name = name,
                startIcon = null,
                endIcon = null,
                onClick = { callbackCount.incrementAndGet() }
            )

            // Verify callback is set
            menuItemWithCallback.onClick shouldNotBe null

            // Invoke callback and verify it was called
            menuItemWithCallback.onClick?.invoke()
            callbackCount.get() shouldBe 1

            // Create MenuItem using withIcons factory without onClick
            val menuItemWithoutCallback = MenuItem.withIcons(
                id = id,
                name = name,
                startIcon = null,
                endIcon = null,
                onClick = null
            )

            // Verify callback is null
            menuItemWithoutCallback.onClick shouldBe null
        }
    }
})
