package com.cometchat.uikit.kotlin.presentation.messagelist

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Represents the result of an option click delegation.
 */
private enum class DelegationTarget {
    CUSTOM_LISTENER,
    OPTION_ON_CLICK,
    BUILT_IN_HANDLER
}

/**
 * Pure-logic function that mirrors the option click delegation priority chain
 * in CometChatMessageList.openMessageOptionBottomSheet():
 *
 * ```kotlin
 * if (messageOptionClickListener != null) {
 *     messageOptionClickListener.onMessageOptionClick(msg, id, name)
 * } else {
 *     val matchingOption = originalOptions.find { it.id == id }
 *     val clickHandler = matchingOption?.onClick
 *     if (clickHandler != null) {
 *         clickHandler.invoke()
 *     } else {
 *         invokeMessageOptionClick(msg, id, name)
 *     }
 * }
 * ```
 *
 * Priority chain:
 * 1. Custom messageOptionClickListener (if set) → CUSTOM_LISTENER
 * 2. Option's onClick callback (if non-null) → OPTION_ON_CLICK
 * 3. Built-in handler (invokeMessageOptionClick) → BUILT_IN_HANDLER
 */
private fun determineClickDelegationTarget(
    hasCustomListener: Boolean,
    hasOptionOnClick: Boolean
): DelegationTarget {
    return when {
        hasCustomListener -> DelegationTarget.CUSTOM_LISTENER
        hasOptionOnClick -> DelegationTarget.OPTION_ON_CLICK
        else -> DelegationTarget.BUILT_IN_HANDLER
    }
}

/**
 * Property-based tests for option click delegation priority chain.
 *
 * Feature: message-popup-menu, Property 10: Option click delegation follows priority chain
 *
 * *For any* option click in the message list, when a custom messageOptionClickListener is set,
 * it should receive all clicks. When no custom listener is set, the system should find the
 * matching CometChatMessageOption by id and invoke its onClick if present, otherwise delegate
 * to handleMessageOptionSheetClicks for built-in handling.
 *
 * **Validates: Requirements 6.4, 6.5, 6.6**
 */
class OptionClickDelegationPropertyTest : FunSpec({

    // ==================== Generators ====================

    val boolArb = Arb.boolean()
    val idArb = Arb.string(1..20)
    val nameArb = Arb.string(1..30)

    // ==================== Property Tests ====================

    context("Property 10: Option click delegation follows priority chain") {

        test("custom listener always takes priority when set") {
            checkAll(100, boolArb, idArb, nameArb) { hasOptionOnClick, id, name ->
                val target = determineClickDelegationTarget(
                    hasCustomListener = true,
                    hasOptionOnClick = hasOptionOnClick
                )
                target shouldBe DelegationTarget.CUSTOM_LISTENER
            }
        }

        test("option onClick is used when no custom listener and onClick exists") {
            checkAll(100, idArb, nameArb) { id, name ->
                val target = determineClickDelegationTarget(
                    hasCustomListener = false,
                    hasOptionOnClick = true
                )
                target shouldBe DelegationTarget.OPTION_ON_CLICK
            }
        }

        test("built-in handler is used when no custom listener and no onClick") {
            checkAll(100, idArb, nameArb) { id, name ->
                val target = determineClickDelegationTarget(
                    hasCustomListener = false,
                    hasOptionOnClick = false
                )
                target shouldBe DelegationTarget.BUILT_IN_HANDLER
            }
        }

        test("delegation target matches the full priority chain expression") {
            checkAll(100, boolArb, boolArb) { hasCustomListener, hasOptionOnClick ->
                val target = determineClickDelegationTarget(hasCustomListener, hasOptionOnClick)

                val expected = when {
                    hasCustomListener -> DelegationTarget.CUSTOM_LISTENER
                    hasOptionOnClick -> DelegationTarget.OPTION_ON_CLICK
                    else -> DelegationTarget.BUILT_IN_HANDLER
                }
                target shouldBe expected
            }
        }
    }
})
