package com.cometchat.uikit.kotlin.presentation.messagelist.popupmenu

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

/**
 * Mirrors the color-fallback logic from [PopupMenuAdapter.ViewHolder.bind] as
 * described in the design document:
 *
 * ```
 * textViewItem.setTextColor(if (item.textColor == 0) textColor else item.textColor)
 * textViewItem.setTextAppearance(if (item.textAppearance == 0) textAppearance else item.textAppearance)
 * startIv.setColorFilter(if (item.startIconTint == 0) startIconColor else item.startIconTint)
 * endIv.setColorFilter(if (item.endIconTint == 0) endIconColor else item.endIconTint)
 * ```
 *
 * "0" means "no override — use adapter default".
 */
private data class ResolvedColors(
    val textColor: Int,
    val textAppearance: Int,
    val startIconTint: Int,
    val endIconTint: Int
)

/**
 * Pure-logic function that resolves the effective colors for a menu item row,
 * matching the adapter's bind() implementation.
 *
 * @param itemTextColor       item-level text color (0 = use adapter default)
 * @param itemTextAppearance  item-level text appearance (0 = use adapter default)
 * @param itemStartIconTint   item-level start icon tint (0 = use adapter default)
 * @param itemEndIconTint     item-level end icon tint (0 = use adapter default)
 * @param adapterTextColor    adapter-level default text color
 * @param adapterTextAppearance adapter-level default text appearance
 * @param adapterStartIconTint adapter-level default start icon tint
 * @param adapterEndIconTint  adapter-level default end icon tint
 */
private fun resolveColors(
    itemTextColor: Int,
    itemTextAppearance: Int,
    itemStartIconTint: Int,
    itemEndIconTint: Int,
    adapterTextColor: Int,
    adapterTextAppearance: Int,
    adapterStartIconTint: Int,
    adapterEndIconTint: Int
): ResolvedColors {
    return ResolvedColors(
        textColor = if (itemTextColor == 0) adapterTextColor else itemTextColor,
        textAppearance = if (itemTextAppearance == 0) adapterTextAppearance else itemTextAppearance,
        startIconTint = if (itemStartIconTint == 0) adapterStartIconTint else itemStartIconTint,
        endIconTint = if (itemEndIconTint == 0) adapterEndIconTint else itemEndIconTint
    )
}

/**
 * Property-based tests for PopupMenuAdapter menu item color fallback logic.
 *
 * Feature: message-popup-menu, Property 6: Menu item colors use item-level override or adapter default
 *
 * *For any* MenuItem and adapter-level default colors, the text color should be the
 * item's textColor when non-zero, otherwise the adapter's default textColor. The same
 * rule applies to startIconTint, endIconTint, and textAppearance.
 *
 * **Validates: Requirements 3.5, 3.6**
 */
class PopupMenuAdapterColorFallbackPropertyTest : FunSpec({

    // ==================== Generators ====================

    /**
     * Generator for non-zero color values (simulating a color override).
     * Uses the full Int range excluding 0.
     */
    val nonZeroColorArb = Arb.int(1..Int.MAX_VALUE)

    /**
     * Generator for color values that may be zero (no override) or non-zero.
     * 0 means "use adapter default".
     */
    val maybeZeroColorArb = Arb.int(0..Int.MAX_VALUE)

    /**
     * Generator for adapter-level default colors (always non-zero in practice,
     * since they come from theme defaults).
     */
    val adapterColorArb = Arb.int(1..Int.MAX_VALUE)

    // ==================== Property Tests ====================

    context("Property 6: Menu item colors use item-level override or adapter default") {

        test("text color uses item value when non-zero, adapter default when zero") {
            checkAll(
                100,
                maybeZeroColorArb,
                adapterColorArb
            ) { itemColor, adapterColor ->
                val resolved = resolveColors(
                    itemTextColor = itemColor,
                    itemTextAppearance = 0,
                    itemStartIconTint = 0,
                    itemEndIconTint = 0,
                    adapterTextColor = adapterColor,
                    adapterTextAppearance = 0,
                    adapterStartIconTint = 0,
                    adapterEndIconTint = 0
                )

                if (itemColor != 0) {
                    resolved.textColor shouldBe itemColor
                } else {
                    resolved.textColor shouldBe adapterColor
                }
            }
        }

        test("text appearance uses item value when non-zero, adapter default when zero") {
            checkAll(
                100,
                maybeZeroColorArb,
                adapterColorArb
            ) { itemAppearance, adapterAppearance ->
                val resolved = resolveColors(
                    itemTextColor = 0,
                    itemTextAppearance = itemAppearance,
                    itemStartIconTint = 0,
                    itemEndIconTint = 0,
                    adapterTextColor = 0,
                    adapterTextAppearance = adapterAppearance,
                    adapterStartIconTint = 0,
                    adapterEndIconTint = 0
                )

                if (itemAppearance != 0) {
                    resolved.textAppearance shouldBe itemAppearance
                } else {
                    resolved.textAppearance shouldBe adapterAppearance
                }
            }
        }

        test("start icon tint uses item value when non-zero, adapter default when zero") {
            checkAll(
                100,
                maybeZeroColorArb,
                adapterColorArb
            ) { itemTint, adapterTint ->
                val resolved = resolveColors(
                    itemTextColor = 0,
                    itemTextAppearance = 0,
                    itemStartIconTint = itemTint,
                    itemEndIconTint = 0,
                    adapterTextColor = 0,
                    adapterTextAppearance = 0,
                    adapterStartIconTint = adapterTint,
                    adapterEndIconTint = 0
                )

                if (itemTint != 0) {
                    resolved.startIconTint shouldBe itemTint
                } else {
                    resolved.startIconTint shouldBe adapterTint
                }
            }
        }

        test("end icon tint uses item value when non-zero, adapter default when zero") {
            checkAll(
                100,
                maybeZeroColorArb,
                adapterColorArb
            ) { itemTint, adapterTint ->
                val resolved = resolveColors(
                    itemTextColor = 0,
                    itemTextAppearance = 0,
                    itemStartIconTint = 0,
                    itemEndIconTint = itemTint,
                    adapterTextColor = 0,
                    adapterTextAppearance = 0,
                    adapterStartIconTint = 0,
                    adapterEndIconTint = adapterTint
                )

                if (itemTint != 0) {
                    resolved.endIconTint shouldBe itemTint
                } else {
                    resolved.endIconTint shouldBe adapterTint
                }
            }
        }

        test("all four color properties resolve independently") {
            checkAll(
                100,
                maybeZeroColorArb, maybeZeroColorArb, maybeZeroColorArb, maybeZeroColorArb,
                adapterColorArb, adapterColorArb, adapterColorArb, adapterColorArb
            ) { iText, iAppearance, iStart, iEnd,
                aText, aAppearance, aStart, aEnd ->

                val resolved = resolveColors(
                    itemTextColor = iText,
                    itemTextAppearance = iAppearance,
                    itemStartIconTint = iStart,
                    itemEndIconTint = iEnd,
                    adapterTextColor = aText,
                    adapterTextAppearance = aAppearance,
                    adapterStartIconTint = aStart,
                    adapterEndIconTint = aEnd
                )

                resolved.textColor shouldBe if (iText != 0) iText else aText
                resolved.textAppearance shouldBe if (iAppearance != 0) iAppearance else aAppearance
                resolved.startIconTint shouldBe if (iStart != 0) iStart else aStart
                resolved.endIconTint shouldBe if (iEnd != 0) iEnd else aEnd
            }
        }

        test("item-level override always wins over adapter default when non-zero") {
            checkAll(
                100,
                nonZeroColorArb, nonZeroColorArb, nonZeroColorArb, nonZeroColorArb,
                adapterColorArb, adapterColorArb, adapterColorArb, adapterColorArb
            ) { iText, iAppearance, iStart, iEnd,
                aText, aAppearance, aStart, aEnd ->

                val resolved = resolveColors(
                    itemTextColor = iText,
                    itemTextAppearance = iAppearance,
                    itemStartIconTint = iStart,
                    itemEndIconTint = iEnd,
                    adapterTextColor = aText,
                    adapterTextAppearance = aAppearance,
                    adapterStartIconTint = aStart,
                    adapterEndIconTint = aEnd
                )

                resolved.textColor shouldBe iText
                resolved.textAppearance shouldBe iAppearance
                resolved.startIconTint shouldBe iStart
                resolved.endIconTint shouldBe iEnd
            }
        }
    }
})
