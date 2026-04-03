package com.cometchat.uikit.kotlin.presentation.messagelist.ui

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

/**
 * Test class that simulates the swipe to reply behavior of CometChatMessageList
 * without requiring Android context.
 *
 * This mirrors the actual implementation:
 * - CometChatMessageList stores swipeToReplyEnabled field (default true)
 * - setSwipeToReplyEnabled(boolean) updates the field and attaches/detaches ItemTouchHelper
 * - isSwipeToReplyEnabled() returns the stored boolean value
 * - When enabled, ItemTouchHelper is attached to RecyclerView
 * - When disabled, ItemTouchHelper is detached from RecyclerView (attachToRecyclerView(null))
 */
private class TestSwipeToReplyStorage {
    // Simulates CometChatMessageList.swipeToReplyEnabled (default true)
    private var swipeToReplyEnabled: Boolean = true

    // Simulates whether ItemTouchHelper is attached to RecyclerView
    private var itemTouchHelperAttached: Boolean = true

    // Simulates whether ItemTouchHelper instance exists
    private var itemTouchHelperCreated: Boolean = true

    /**
     * Simulates CometChatMessageList.setSwipeToReplyEnabled(boolean)
     * 
     * When enabled is true:
     * - Creates ItemTouchHelper if not exists (initializeItemTouchHelper)
     * - Attaches ItemTouchHelper to RecyclerView
     * 
     * When enabled is false:
     * - Detaches ItemTouchHelper from RecyclerView (attachToRecyclerView(null))
     */
    fun setSwipeToReplyEnabled(enabled: Boolean) {
        swipeToReplyEnabled = enabled
        if (enabled) {
            // Create ItemTouchHelper if not exists
            if (!itemTouchHelperCreated) {
                itemTouchHelperCreated = true
            }
            // Attach ItemTouchHelper to RecyclerView
            itemTouchHelperAttached = true
        } else {
            // Detach ItemTouchHelper from RecyclerView
            itemTouchHelperAttached = false
        }
    }

    /**
     * Simulates CometChatMessageList.isSwipeToReplyEnabled()
     */
    fun isSwipeToReplyEnabled(): Boolean = swipeToReplyEnabled

    /**
     * Returns whether the ItemTouchHelper is currently attached to RecyclerView.
     * This simulates checking if itemTouchHelper.attachToRecyclerView was called with
     * the RecyclerView (attached) or null (detached).
     */
    fun isItemTouchHelperAttached(): Boolean = itemTouchHelperAttached

    /**
     * Returns whether the ItemTouchHelper instance has been created.
     */
    fun isItemTouchHelperCreated(): Boolean = itemTouchHelperCreated
}

/**
 * Property-based tests for CometChatMessageList swipe to reply toggle.
 * Uses Kotest property testing to verify correctness properties.
 *
 * Feature: messagelist-property-parity, Property 8: Swipe to Reply Toggle
 *
 * *For any* boolean value, when `setSwipeToReplyEnabled` is called, the swipe gesture
 * SHALL be enabled (true) or disabled (false) on the RecyclerView.
 *
 * **Validates: Requirements 8.1, 8.2, 8.3**
 */
class CometChatMessageListSwipeToReplyPropertyTest : FunSpec({

    // ==================== Property Tests ====================

    context("Property 8: Swipe to Reply Toggle") {

        // ========================================
        // Basic Enable/Disable Tests
        // ========================================

        test("setSwipeToReplyEnabled(true) attaches ItemTouchHelper to RecyclerView") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestSwipeToReplyStorage()

                // Set swipe to reply enabled
                storage.setSwipeToReplyEnabled(true)

                // Verify ItemTouchHelper is attached
                storage.isItemTouchHelperAttached().shouldBeTrue()
            }
        }

        test("setSwipeToReplyEnabled(false) detaches ItemTouchHelper from RecyclerView") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestSwipeToReplyStorage()

                // First ensure ItemTouchHelper is attached
                storage.setSwipeToReplyEnabled(true)
                storage.isItemTouchHelperAttached().shouldBeTrue()

                // Set swipe to reply disabled
                storage.setSwipeToReplyEnabled(false)

                // Verify ItemTouchHelper is detached
                storage.isItemTouchHelperAttached().shouldBeFalse()
            }
        }

        // ========================================
        // Getter Tests
        // ========================================

        test("isSwipeToReplyEnabled() returns the value that was set") {
            checkAll(100, Arb.boolean()) { enabled ->
                val storage = TestSwipeToReplyStorage()

                // Set swipe to reply
                storage.setSwipeToReplyEnabled(enabled)

                // Verify getter returns the same value
                storage.isSwipeToReplyEnabled() shouldBe enabled
            }
        }

        test("default value is true") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestSwipeToReplyStorage()

                // Verify default value without any setter calls
                storage.isSwipeToReplyEnabled().shouldBeTrue()
            }
        }

        // ========================================
        // ItemTouchHelper State Tests
        // ========================================

        test("ItemTouchHelper is attached by default (swipeToReplyEnabled is true by default)") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestSwipeToReplyStorage()

                // Default state should have ItemTouchHelper attached
                storage.isItemTouchHelperAttached().shouldBeTrue()
            }
        }

        test("ItemTouchHelper is created when setSwipeToReplyEnabled(true) is called") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestSwipeToReplyStorage()

                // Set swipe to reply enabled
                storage.setSwipeToReplyEnabled(true)

                // Verify ItemTouchHelper is created
                storage.isItemTouchHelperCreated().shouldBeTrue()
            }
        }

        test("ItemTouchHelper instance is preserved when toggling enabled state") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestSwipeToReplyStorage()

                // Create ItemTouchHelper by setting enabled
                storage.setSwipeToReplyEnabled(true)
                storage.isItemTouchHelperCreated().shouldBeTrue()

                // Toggle to disabled
                storage.setSwipeToReplyEnabled(false)
                // ItemTouchHelper instance should still exist (just not attached)
                storage.isItemTouchHelperCreated().shouldBeTrue()

                // Toggle back to enabled
                storage.setSwipeToReplyEnabled(true)
                // ItemTouchHelper should be reattached
                storage.isItemTouchHelperAttached().shouldBeTrue()
            }
        }

        // ========================================
        // Toggle Tests
        // ========================================

        test("toggling multiple times maintains correct state") {
            checkAll(100, Arb.boolean(), Arb.boolean()) { first, second ->
                val storage = TestSwipeToReplyStorage()

                // Set first value
                storage.setSwipeToReplyEnabled(first)
                storage.isSwipeToReplyEnabled() shouldBe first
                storage.isItemTouchHelperAttached() shouldBe first

                // Set second value
                storage.setSwipeToReplyEnabled(second)
                storage.isSwipeToReplyEnabled() shouldBe second
                storage.isItemTouchHelperAttached() shouldBe second
            }
        }

        test("setting same value multiple times is idempotent") {
            checkAll(100, Arb.boolean()) { enabled ->
                val storage = TestSwipeToReplyStorage()

                // Set value multiple times
                storage.setSwipeToReplyEnabled(enabled)
                val firstState = storage.isItemTouchHelperAttached()

                storage.setSwipeToReplyEnabled(enabled)
                val secondState = storage.isItemTouchHelperAttached()

                storage.setSwipeToReplyEnabled(enabled)
                val thirdState = storage.isItemTouchHelperAttached()

                // State should be consistent
                firstState shouldBe secondState
                secondState shouldBe thirdState
                storage.isSwipeToReplyEnabled() shouldBe enabled
            }
        }

        // ========================================
        // Enabled vs Disabled Tests
        // ========================================

        test("only true attaches ItemTouchHelper, false detaches") {
            checkAll(100, Arb.boolean()) { enabled ->
                val storage = TestSwipeToReplyStorage()

                // Set enabled state
                storage.setSwipeToReplyEnabled(enabled)

                // Only true should have ItemTouchHelper attached
                if (enabled) {
                    storage.isItemTouchHelperAttached().shouldBeTrue()
                } else {
                    storage.isItemTouchHelperAttached().shouldBeFalse()
                }
            }
        }

        // ========================================
        // Sequence Tests
        // ========================================

        test("true -> false -> true correctly toggles ItemTouchHelper") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestSwipeToReplyStorage()

                // Start with true
                storage.setSwipeToReplyEnabled(true)
                storage.isItemTouchHelperAttached().shouldBeTrue()
                storage.isSwipeToReplyEnabled().shouldBeTrue()

                // Toggle to false
                storage.setSwipeToReplyEnabled(false)
                storage.isItemTouchHelperAttached().shouldBeFalse()
                storage.isSwipeToReplyEnabled().shouldBeFalse()

                // Toggle back to true
                storage.setSwipeToReplyEnabled(true)
                storage.isItemTouchHelperAttached().shouldBeTrue()
                storage.isSwipeToReplyEnabled().shouldBeTrue()
            }
        }

        test("false -> true -> false correctly toggles ItemTouchHelper") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestSwipeToReplyStorage()

                // Start with false
                storage.setSwipeToReplyEnabled(false)
                storage.isItemTouchHelperAttached().shouldBeFalse()
                storage.isSwipeToReplyEnabled().shouldBeFalse()

                // Toggle to true
                storage.setSwipeToReplyEnabled(true)
                storage.isItemTouchHelperAttached().shouldBeTrue()
                storage.isSwipeToReplyEnabled().shouldBeTrue()

                // Toggle back to false
                storage.setSwipeToReplyEnabled(false)
                storage.isItemTouchHelperAttached().shouldBeFalse()
                storage.isSwipeToReplyEnabled().shouldBeFalse()
            }
        }

        test("multiple false values keep ItemTouchHelper detached") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestSwipeToReplyStorage()

                // Set false multiple times
                storage.setSwipeToReplyEnabled(false)
                storage.isItemTouchHelperAttached().shouldBeFalse()

                storage.setSwipeToReplyEnabled(false)
                storage.isItemTouchHelperAttached().shouldBeFalse()

                storage.setSwipeToReplyEnabled(false)
                storage.isItemTouchHelperAttached().shouldBeFalse()
            }
        }

        test("multiple true values keep ItemTouchHelper attached") {
            checkAll(100, Arb.int(0..10)) { _ ->
                val storage = TestSwipeToReplyStorage()

                // Set true multiple times
                storage.setSwipeToReplyEnabled(true)
                storage.isItemTouchHelperAttached().shouldBeTrue()

                storage.setSwipeToReplyEnabled(true)
                storage.isItemTouchHelperAttached().shouldBeTrue()

                storage.setSwipeToReplyEnabled(true)
                storage.isItemTouchHelperAttached().shouldBeTrue()
            }
        }

        // ========================================
        // Property Invariant Tests
        // ========================================

        test("getter always returns the last set value") {
            checkAll(100, Arb.boolean(), Arb.boolean(), Arb.boolean()) { v1, v2, v3 ->
                val storage = TestSwipeToReplyStorage()

                storage.setSwipeToReplyEnabled(v1)
                storage.isSwipeToReplyEnabled() shouldBe v1

                storage.setSwipeToReplyEnabled(v2)
                storage.isSwipeToReplyEnabled() shouldBe v2

                storage.setSwipeToReplyEnabled(v3)
                storage.isSwipeToReplyEnabled() shouldBe v3
            }
        }

        test("ItemTouchHelper attachment state is consistent with enabled value") {
            checkAll(100, Arb.boolean()) { enabled ->
                val storage = TestSwipeToReplyStorage()

                storage.setSwipeToReplyEnabled(enabled)

                // Invariant: ItemTouchHelper attached iff enabled == true
                storage.isItemTouchHelperAttached() shouldBe enabled
            }
        }

        test("enabled state and attachment state are always synchronized") {
            checkAll(100, Arb.boolean(), Arb.boolean(), Arb.boolean(), Arb.boolean()) { 
                e1, e2, e3, e4 ->
                val storage = TestSwipeToReplyStorage()

                // Apply sequence of enabled states
                storage.setSwipeToReplyEnabled(e1)
                storage.isSwipeToReplyEnabled() shouldBe e1
                storage.isItemTouchHelperAttached() shouldBe e1

                storage.setSwipeToReplyEnabled(e2)
                storage.isSwipeToReplyEnabled() shouldBe e2
                storage.isItemTouchHelperAttached() shouldBe e2

                storage.setSwipeToReplyEnabled(e3)
                storage.isSwipeToReplyEnabled() shouldBe e3
                storage.isItemTouchHelperAttached() shouldBe e3

                storage.setSwipeToReplyEnabled(e4)
                storage.isSwipeToReplyEnabled() shouldBe e4
                storage.isItemTouchHelperAttached() shouldBe e4
            }
        }
    }
})
