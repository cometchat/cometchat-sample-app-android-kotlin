package com.cometchat.uikit.kotlin.presentation.messagelist

import android.view.View
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Pure-logic function that mirrors the quick reactions visibility determination
 * performed by CometChatMessageList.openMessageOptionBottomSheet():
 *
 * ```kotlin
 * if (message.category == UIKitConstants.MessageCategory.INTERACTIVE
 *     || messageReactionOptionVisibility != View.VISIBLE
 *     || moderationStatus == UIKitConstants.ModerationConstants.DISAPPROVED
 *     || quickReactionsVisibility == View.GONE) {
 *     return View.GONE
 * }
 * return View.VISIBLE
 * ```
 *
 * This is the CALLER's logic (message list), not the popup menu itself.
 */
internal fun computeQuickReactionsVisibility(
    messageCategory: String,
    reactionOptionVisibility: Int,
    moderationStatus: String?,
    quickReactionsVisibility: Int
): Int {
    if (messageCategory == "interactive"
        || reactionOptionVisibility != View.VISIBLE
        || moderationStatus == "disapproved"
        || quickReactionsVisibility == View.GONE
    ) {
        return View.GONE
    }
    return View.VISIBLE
}

/**
 * Property-based tests for quick reactions visibility determination.
 *
 * Feature: message-popup-menu, Property 2: Quick reactions visibility is determined by message properties
 *
 * *For any* BaseMessage, the quick reactions bar should be hidden when:
 * (a) category is INTERACTIVE, (b) reaction option visibility is not VISIBLE,
 * or (c) moderation status is DISAPPROVED.
 *
 * **Validates: Requirements 2.6, 2.7, 2.8**
 */
class QuickReactionsVisibilityPropertyTest : FunSpec({

    // ==================== Generators ====================

    val categoryArb = Arb.element("message", "action", "call", "custom", "interactive")
    val visibilityArb = Arb.element(View.VISIBLE, View.GONE, View.INVISIBLE)
    val moderationStatusArb = Arb.element("approved", "disapproved", "pending", null)

    // ==================== Property Tests ====================

    context("Property 2: Quick reactions visibility is determined by message properties") {

        test("interactive messages always hide quick reactions") {
            checkAll(100, visibilityArb, moderationStatusArb, visibilityArb) { reactionVis, modStatus, qrVis ->
                val result = computeQuickReactionsVisibility(
                    messageCategory = "interactive",
                    reactionOptionVisibility = reactionVis,
                    moderationStatus = modStatus,
                    quickReactionsVisibility = qrVis
                )
                result shouldBe View.GONE
            }
        }

        test("non-VISIBLE reaction option visibility hides quick reactions") {
            checkAll(100, categoryArb, moderationStatusArb, visibilityArb) { category, modStatus, qrVis ->
                val nonVisibleReactionVis = if (category == "interactive") View.VISIBLE else View.GONE

                val result = computeQuickReactionsVisibility(
                    messageCategory = category,
                    reactionOptionVisibility = nonVisibleReactionVis,
                    moderationStatus = modStatus,
                    quickReactionsVisibility = qrVis
                )

                // Either category is interactive OR reactionOptionVisibility is GONE → always GONE
                result shouldBe View.GONE
            }
        }

        test("disapproved moderation status hides quick reactions") {
            checkAll(100, categoryArb, visibilityArb, visibilityArb) { category, reactionVis, qrVis ->
                val result = computeQuickReactionsVisibility(
                    messageCategory = category,
                    reactionOptionVisibility = reactionVis,
                    moderationStatus = "disapproved",
                    quickReactionsVisibility = qrVis
                )
                result shouldBe View.GONE
            }
        }

        test("quickReactionsVisibility GONE hides quick reactions") {
            checkAll(100, categoryArb, visibilityArb, moderationStatusArb) { category, reactionVis, modStatus ->
                val result = computeQuickReactionsVisibility(
                    messageCategory = category,
                    reactionOptionVisibility = reactionVis,
                    moderationStatus = modStatus,
                    quickReactionsVisibility = View.GONE
                )
                result shouldBe View.GONE
            }
        }

        test("visibility result matches the boolean expression from design doc") {
            checkAll(100, categoryArb, visibilityArb, moderationStatusArb, visibilityArb) { category, reactionVis, modStatus, qrVis ->
                val result = computeQuickReactionsVisibility(
                    messageCategory = category,
                    reactionOptionVisibility = reactionVis,
                    moderationStatus = modStatus,
                    quickReactionsVisibility = qrVis
                )

                val shouldBeGone = category == "interactive"
                    || reactionVis != View.VISIBLE
                    || modStatus == "disapproved"
                    || qrVis == View.GONE

                val expected = if (shouldBeGone) View.GONE else View.VISIBLE
                result shouldBe expected
            }
        }

        test("only non-interactive + VISIBLE reaction option + non-disapproved + VISIBLE qr shows reactions") {
            checkAll(100, Arb.element("message", "action", "call", "custom"), Arb.element("approved", "pending", null)) { category, modStatus ->
                val result = computeQuickReactionsVisibility(
                    messageCategory = category,
                    reactionOptionVisibility = View.VISIBLE,
                    moderationStatus = modStatus,
                    quickReactionsVisibility = View.VISIBLE
                )
                result shouldBe View.VISIBLE
            }
        }
    }
})
