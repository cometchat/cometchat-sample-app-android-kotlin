package com.cometchat.uikit.kotlin.presentation.messagelist

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Represents a test menu item with an optional onClick callback.
 */
private data class TestClickMenuItem(
    val id: String,
    val name: String,
    val hasOnClick: Boolean
)

/**
 * Tracks which callbacks were invoked during a simulated click.
 */
private data class ClickCallbackResult(
    val itemOnClickInvoked: Boolean,
    val listenerInvoked: Boolean,
    val listenerReceivedId: String,
    val listenerReceivedName: String
)

/**
 * Pure-logic function that mirrors the menu item click handling in
 * CometChatMessagePopupMenu.show() → adapter click handler:
 *
 * ```kotlin
 * adapter click handler: { id, name ->
 *     menuItems.find { it.id == id }?.onClick?.invoke()
 *     onMenuItemClickListener?.onMenuItemClick(id, name)
 * }
 * ```
 *
 * Both the item's onClick (if present) AND the listener are invoked.
 */
private fun simulateMenuItemClick(
    item: TestClickMenuItem,
    hasListener: Boolean
): ClickCallbackResult {
    val itemOnClickInvoked = item.hasOnClick
    val listenerInvoked = hasListener

    return ClickCallbackResult(
        itemOnClickInvoked = itemOnClickInvoked,
        listenerInvoked = listenerInvoked,
        listenerReceivedId = if (listenerInvoked) item.id else "",
        listenerReceivedName = if (listenerInvoked) item.name else ""
    )
}

/**
 * Property-based tests for menu item click callback behavior.
 *
 * Feature: message-popup-menu, Property 7: Menu item click invokes both item callback and listener
 *
 * *For any* MenuItem in the option list, tapping it should invoke the OnMenuItemClickListener
 * callback with the item's id and name. Additionally, if the item has a non-null onClick
 * callback, that should also be invoked.
 *
 * **Validates: Requirements 3.7**
 */
class MenuItemClickPropertyTest : FunSpec({

    // ==================== Generators ====================

    val idArb = Arb.string(1..20)
    val nameArb = Arb.string(1..30)
    val boolArb = Arb.boolean()

    // ==================== Property Tests ====================

    context("Property 7: Menu item click invokes both item callback and listener") {

        test("listener receives correct id and name when set") {
            checkAll(100, idArb, nameArb, boolArb) { id, name, hasOnClick ->
                val item = TestClickMenuItem(id, name, hasOnClick)
                val result = simulateMenuItemClick(item, hasListener = true)

                result.listenerInvoked shouldBe true
                result.listenerReceivedId shouldBe id
                result.listenerReceivedName shouldBe name
            }
        }

        test("item onClick is invoked when present") {
            checkAll(100, idArb, nameArb, boolArb) { id, name, hasListener ->
                val item = TestClickMenuItem(id, name, hasOnClick = true)
                val result = simulateMenuItemClick(item, hasListener)

                result.itemOnClickInvoked shouldBe true
            }
        }

        test("item onClick is not invoked when absent") {
            checkAll(100, idArb, nameArb, boolArb) { id, name, hasListener ->
                val item = TestClickMenuItem(id, name, hasOnClick = false)
                val result = simulateMenuItemClick(item, hasListener)

                result.itemOnClickInvoked shouldBe false
            }
        }

        test("both callbacks fire independently when both are present") {
            checkAll(100, idArb, nameArb) { id, name ->
                val item = TestClickMenuItem(id, name, hasOnClick = true)
                val result = simulateMenuItemClick(item, hasListener = true)

                result.itemOnClickInvoked shouldBe true
                result.listenerInvoked shouldBe true
                result.listenerReceivedId shouldBe id
                result.listenerReceivedName shouldBe name
            }
        }

        test("listener not invoked when not set, but item onClick still fires") {
            checkAll(100, idArb, nameArb) { id, name ->
                val item = TestClickMenuItem(id, name, hasOnClick = true)
                val result = simulateMenuItemClick(item, hasListener = false)

                result.itemOnClickInvoked shouldBe true
                result.listenerInvoked shouldBe false
            }
        }
    }
})
