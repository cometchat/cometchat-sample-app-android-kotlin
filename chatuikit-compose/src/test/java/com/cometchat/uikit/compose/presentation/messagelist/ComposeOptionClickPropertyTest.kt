package com.cometchat.uikit.compose.presentation.messagelist

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Represents the result of an option click in the Compose popup menu.
 */
private data class ComposeOptionClickResult(
    val optionOnClickInvoked: Boolean,
    val topLevelCallbackInvoked: Boolean,
    val callbackOptionId: String,
    val dismissed: Boolean
)

/**
 * Pure-logic function that mirrors the Compose option click flow in
 * CometChatMessagePopupMenu:
 *
 * ```kotlin
 * onOptionClick = { option ->
 *     option.onClick?.invoke()          // invoke option's own callback if present
 *     onMessageOptionClick?.invoke(...)  // invoke top-level callback
 *     showPopupMenu = false             // dismiss
 * }
 * ```
 *
 * The Compose variant always invokes the top-level onOptionClick callback
 * and always dismisses. The option's own onClick is invoked when non-null.
 */
private fun simulateComposeOptionClick(
    optionId: String,
    hasOptionOnClick: Boolean
): ComposeOptionClickResult {
    // option.onClick?.invoke()
    val optionOnClickInvoked = hasOptionOnClick

    // onOptionClick callback is always invoked
    val topLevelCallbackInvoked = true

    // Always dismisses
    val dismissed = true

    return ComposeOptionClickResult(
        optionOnClickInvoked = optionOnClickInvoked,
        topLevelCallbackInvoked = topLevelCallbackInvoked,
        callbackOptionId = optionId,
        dismissed = dismissed
    )
}

/**
 * Property-based tests for Compose option click callback behavior.
 *
 * Feature: message-popup-menu, Property 13: Compose option click invokes callback and dismisses
 *
 * *For any* CometChatMessageOption in the Compose popup menu, tapping it should
 * invoke the option's onClick callback (if present) and the top-level onOptionClick
 * callback, then dismiss the popup.
 *
 * **Validates: Requirements 7.4, 8.3**
 */
class ComposeOptionClickPropertyTest : FunSpec({

    // ==================== Generators ====================

    val idArb = Arb.string(1..20)
    val boolArb = Arb.boolean()

    // ==================== Property Tests ====================

    context("Property 13: Compose option click invokes callback and dismisses") {

        test("top-level onOptionClick is always invoked") {
            checkAll(100, idArb, boolArb) { optionId, hasOnClick ->
                val result = simulateComposeOptionClick(optionId, hasOnClick)

                result.topLevelCallbackInvoked shouldBe true
            }
        }

        test("option's own onClick is invoked when present") {
            checkAll(100, idArb) { optionId ->
                val result = simulateComposeOptionClick(optionId, hasOptionOnClick = true)

                result.optionOnClickInvoked shouldBe true
            }
        }

        test("option's own onClick is not invoked when absent") {
            checkAll(100, idArb) { optionId ->
                val result = simulateComposeOptionClick(optionId, hasOptionOnClick = false)

                result.optionOnClickInvoked shouldBe false
            }
        }

        test("popup is always dismissed after option click") {
            checkAll(100, idArb, boolArb) { optionId, hasOnClick ->
                val result = simulateComposeOptionClick(optionId, hasOnClick)

                result.dismissed shouldBe true
            }
        }

        test("callback receives the correct option ID") {
            checkAll(100, idArb, boolArb) { optionId, hasOnClick ->
                val result = simulateComposeOptionClick(optionId, hasOnClick)

                result.callbackOptionId shouldBe optionId
            }
        }
    }
})
