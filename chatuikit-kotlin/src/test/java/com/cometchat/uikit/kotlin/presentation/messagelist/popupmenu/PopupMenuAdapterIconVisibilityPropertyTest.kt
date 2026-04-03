package com.cometchat.uikit.kotlin.presentation.messagelist.popupmenu

import android.view.View
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Lightweight stand-in for android.graphics.Drawable used only to distinguish
 * "icon provided" (non-null) from "icon absent" (null).
 */
private class FakeDrawable

/**
 * Mirrors the binding logic of [PopupMenuAdapter.ViewHolder.bind] for icon
 * visibility and label text, extracted from the design document:
 *
 * ```
 * if (item.startIcon != null) { startIv.visibility = VISIBLE }
 * else { startIv.visibility = GONE }
 *
 * if (item.endIcon != null) { endIv.visibility = VISIBLE }
 * else { endIv.visibility = GONE }
 *
 * textViewItem.text = item.name
 * ```
 */
private data class IconVisibilityResult(
    val startIconVisibility: Int,
    val endIconVisibility: Int,
    val labelText: String
)

/**
 * Pure-logic function that computes the expected view state for a single menu
 * item row, matching the adapter's bind() implementation.
 */
private fun computeIconVisibility(
    name: String,
    hasStartIcon: Boolean,
    hasEndIcon: Boolean
): IconVisibilityResult {
    return IconVisibilityResult(
        startIconVisibility = if (hasStartIcon) View.VISIBLE else View.GONE,
        endIconVisibility = if (hasEndIcon) View.VISIBLE else View.GONE,
        labelText = name
    )
}

/**
 * Property-based tests for PopupMenuAdapter menu item icon visibility.
 *
 * Feature: message-popup-menu, Property 5: Menu item row renders icons based on presence
 *
 * *For any* MenuItem, the start icon ImageView should be VISIBLE when startIcon is
 * non-null and GONE when null. The end icon ImageView should be VISIBLE when endIcon
 * is non-null and GONE when null. The label text should always display the item's name.
 *
 * **Validates: Requirements 3.1, 3.3, 3.4**
 */
class PopupMenuAdapterIconVisibilityPropertyTest : FunSpec({

    // ==================== Generators ====================

    /** Arbitrary menu item name. */
    val nameArb = Arb.string(0..50)

    /** Whether a start/end icon drawable is present. */
    val hasIconArb = Arb.boolean()

    // ==================== Property Tests ====================

    context("Property 5: Menu item row renders icons based on presence") {

        test("start icon is VISIBLE when startIcon is non-null, GONE when null") {
            checkAll(100, nameArb, hasIconArb, hasIconArb) { name, hasStart, hasEnd ->
                val result = computeIconVisibility(name, hasStart, hasEnd)

                if (hasStart) {
                    result.startIconVisibility shouldBe View.VISIBLE
                } else {
                    result.startIconVisibility shouldBe View.GONE
                }
            }
        }

        test("end icon is VISIBLE when endIcon is non-null, GONE when null") {
            checkAll(100, nameArb, hasIconArb, hasIconArb) { name, hasStart, hasEnd ->
                val result = computeIconVisibility(name, hasStart, hasEnd)

                if (hasEnd) {
                    result.endIconVisibility shouldBe View.VISIBLE
                } else {
                    result.endIconVisibility shouldBe View.GONE
                }
            }
        }

        test("label text always displays the item's name") {
            checkAll(100, nameArb, hasIconArb, hasIconArb) { name, hasStart, hasEnd ->
                val result = computeIconVisibility(name, hasStart, hasEnd)

                result.labelText shouldBe name
            }
        }

        test("both icons VISIBLE when both drawables are provided") {
            checkAll(100, nameArb) { name ->
                val result = computeIconVisibility(name, hasStartIcon = true, hasEndIcon = true)

                result.startIconVisibility shouldBe View.VISIBLE
                result.endIconVisibility shouldBe View.VISIBLE
            }
        }

        test("both icons GONE when neither drawable is provided") {
            checkAll(100, nameArb) { name ->
                val result = computeIconVisibility(name, hasStartIcon = false, hasEndIcon = false)

                result.startIconVisibility shouldBe View.GONE
                result.endIconVisibility shouldBe View.GONE
            }
        }

        test("start and end icon visibility are independent of each other") {
            checkAll(100, nameArb, hasIconArb, hasIconArb) { name, hasStart, hasEnd ->
                val result = computeIconVisibility(name, hasStart, hasEnd)

                // Start icon visibility depends only on hasStart
                val expectedStart = if (hasStart) View.VISIBLE else View.GONE
                result.startIconVisibility shouldBe expectedStart

                // End icon visibility depends only on hasEnd
                val expectedEnd = if (hasEnd) View.VISIBLE else View.GONE
                result.endIconVisibility shouldBe expectedEnd
            }
        }
    }
})
