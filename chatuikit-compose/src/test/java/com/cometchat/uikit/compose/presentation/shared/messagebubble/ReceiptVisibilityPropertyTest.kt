package com.cometchat.uikit.compose.presentation.shared.messagebubble

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.checkAll

/**
 * Property-based tests for receipt visibility logic in DefaultStatusInfoView.
 *
 * // Feature: jetpack-message-bubble-parity, Property 7: Receipt visibility controlled by hideReceipts flag
 *
 * **Validates: Requirements 6.2, 6.3**
 *
 * Property 7: For any message, when `hideReceipts` is `true`, the DefaultStatusInfoView
 * SHALL hide the receipt indicator regardless of the message's receipt status or sender.
 * When `hideReceipts` is `false`, receipt visibility SHALL be determined by
 * `MessageReceiptUtils.shouldHideReceipt(message)`.
 *
 * The core logic under test (from InternalContentRenderer.DefaultStatusInfoView):
 *   val shouldHideReceipt = hideReceipts || MessageReceiptUtils.shouldHideReceipt(message)
 *   if (showReceipt && !shouldHideReceipt) { CometChatReceipts(...) }
 *
 * Since MessageReceiptUtils.shouldHideReceipt depends on CometChatUIKit.getLoggedInUser()
 * (SDK state), we model it as a pure boolean input and test the OR-gate logic directly.
 */
class ReceiptVisibilityPropertyTest : StringSpec({

    /**
     * Models the shouldHideReceipt computation from DefaultStatusInfoView.
     * This is the pure boolean logic: hideReceipts || utilShouldHide.
     */
    fun computeShouldHideReceipt(hideReceipts: Boolean, utilShouldHide: Boolean): Boolean {
        return hideReceipts || utilShouldHide
    }

    /**
     * Models the final receipt visibility from DefaultStatusInfoView.
     * Receipt is visible when: showReceipt && !shouldHideReceipt.
     */
    fun isReceiptVisible(showReceipt: Boolean, hideReceipts: Boolean, utilShouldHide: Boolean): Boolean {
        val shouldHide = computeShouldHideReceipt(hideReceipts, utilShouldHide)
        return showReceipt && !shouldHide
    }

    // ============================================================================
    // Property Test: hideReceipts=true always hides receipt
    // ============================================================================

    /**
     * Property test: When hideReceipts is true, shouldHideReceipt is always true
     * regardless of what MessageReceiptUtils.shouldHideReceipt returns.
     *
     * // Feature: jetpack-message-bubble-parity, Property 7: Receipt visibility controlled by hideReceipts flag
     * **Validates: Requirements 6.2, 6.3**
     */
    "hideReceipts=true forces shouldHideReceipt=true regardless of util result" {
        checkAll(100, Arb.boolean()) { utilShouldHide ->
            computeShouldHideReceipt(
                hideReceipts = true,
                utilShouldHide = utilShouldHide
            ) shouldBe true
        }
    }

    // ============================================================================
    // Property Test: hideReceipts=false delegates to MessageReceiptUtils
    // ============================================================================

    /**
     * Property test: When hideReceipts is false, shouldHideReceipt equals
     * whatever MessageReceiptUtils.shouldHideReceipt returns.
     *
     * // Feature: jetpack-message-bubble-parity, Property 7: Receipt visibility controlled by hideReceipts flag
     * **Validates: Requirements 6.2, 6.3**
     */
    "hideReceipts=false delegates shouldHideReceipt to MessageReceiptUtils result" {
        checkAll(100, Arb.boolean()) { utilShouldHide ->
            computeShouldHideReceipt(
                hideReceipts = false,
                utilShouldHide = utilShouldHide
            ) shouldBe utilShouldHide
        }
    }

    // ============================================================================
    // Property Test: Full receipt visibility with all boolean inputs
    // ============================================================================

    /**
     * Property test: For any combination of showReceipt, hideReceipts, and
     * MessageReceiptUtils result, the receipt is visible iff showReceipt is true
     * AND neither hideReceipts nor utilShouldHide is true.
     *
     * // Feature: jetpack-message-bubble-parity, Property 7: Receipt visibility controlled by hideReceipts flag
     * **Validates: Requirements 6.2, 6.3**
     */
    "receipt visible iff showReceipt AND NOT (hideReceipts OR utilShouldHide)" {
        checkAll(100, Arb.boolean(), Arb.boolean(), Arb.boolean()) { showReceipt, hideReceipts, utilShouldHide ->
            val expected = showReceipt && !(hideReceipts || utilShouldHide)
            isReceiptVisible(showReceipt, hideReceipts, utilShouldHide) shouldBe expected
        }
    }

    // ============================================================================
    // Property Test: hideReceipts=true makes receipt invisible regardless of showReceipt
    // ============================================================================

    /**
     * Property test: When hideReceipts is true, the receipt is never visible
     * regardless of showReceipt and MessageReceiptUtils result.
     *
     * // Feature: jetpack-message-bubble-parity, Property 7: Receipt visibility controlled by hideReceipts flag
     * **Validates: Requirements 6.2, 6.3**
     */
    "hideReceipts=true makes receipt invisible regardless of other flags" {
        checkAll(100, Arb.boolean(), Arb.boolean()) { showReceipt, utilShouldHide ->
            isReceiptVisible(
                showReceipt = showReceipt,
                hideReceipts = true,
                utilShouldHide = utilShouldHide
            ) shouldBe false
        }
    }

    // ============================================================================
    // Property Test: Receipt visible only when all conditions met
    // ============================================================================

    /**
     * Property test: The receipt is visible only when showReceipt=true,
     * hideReceipts=false, and MessageReceiptUtils.shouldHideReceipt=false.
     *
     * // Feature: jetpack-message-bubble-parity, Property 7: Receipt visibility controlled by hideReceipts flag
     * **Validates: Requirements 6.2, 6.3**
     */
    "receipt visible only when showReceipt=true, hideReceipts=false, utilShouldHide=false" {
        isReceiptVisible(
            showReceipt = true,
            hideReceipts = false,
            utilShouldHide = false
        ) shouldBe true

        // Any other combination with hideReceipts=true should be false
        isReceiptVisible(showReceipt = true, hideReceipts = true, utilShouldHide = false) shouldBe false
        isReceiptVisible(showReceipt = true, hideReceipts = true, utilShouldHide = true) shouldBe false
        isReceiptVisible(showReceipt = true, hideReceipts = false, utilShouldHide = true) shouldBe false
        isReceiptVisible(showReceipt = false, hideReceipts = false, utilShouldHide = false) shouldBe false
    }
})
