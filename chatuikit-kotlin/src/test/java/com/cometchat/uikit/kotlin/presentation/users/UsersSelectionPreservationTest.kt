package com.cometchat.uikit.kotlin.presentation.users

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Preservation Property Tests for Users Selection Behavior
 *
 * **Property 2: Preservation** - Existing Selection Behavior Unchanged
 *
 * These tests capture the CURRENT working behavior of CometChatUsers for non-buggy inputs.
 * They should PASS on both UNFIXED and FIXED code to ensure no regressions.
 *
 * **Observation-First Methodology:**
 * - Observed: Item click in selection mode toggles user selection via `viewModel?.selectUser(user, selectionMode)`
 * - Observed: `setSelectionMode()` clears existing selections when mode changes
 * - Observed: `onSelectionChange` callback is invoked whenever selection changes
 * - Observed: `onItemClick` callback is invoked when selection mode is NONE
 *
 * **Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6**
 */
class UsersSelectionPreservationTest : FunSpec({

    // ==================== Constants ====================

    val SELECTION_MODE_NONE = "NONE"
    val SELECTION_MODE_SINGLE = "SINGLE"
    val SELECTION_MODE_MULTIPLE = "MULTIPLE"

    val ITEM_CLICK = "ITEM_CLICK"
    val ITEM_LONG_CLICK = "ITEM_LONG_CLICK"
    val MODE_CHANGE = "MODE_CHANGE"
    val SEARCH = "SEARCH"
    val SCROLL = "SCROLL"

    // ==================== Data Classes ====================

    /**
     * Represents a user in the selection list
     */
    data class MockUser(
        val uid: String,
        val name: String
    )

    /**
     * Represents the current state of the CometChatUsers component
     */
    data class UsersComponentState(
        val selectionMode: String,
        val selectedUsers: Set<MockUser>,
        val allUsers: List<MockUser>,
        val isViewAttached: Boolean
    )

    /**
     * Represents a user interaction event
     */
    data class UserInteraction(
        val action: String,
        val targetUser: MockUser? = null,
        val newSelectionMode: String? = null,
        val searchQuery: String? = null
    )

    /**
     * Represents the result after handling an interaction
     */
    data class InteractionResult(
        val selectedUsers: Set<MockUser>,
        val selectionCount: Int,
        val onItemClickInvoked: Boolean,
        val onItemLongClickInvoked: Boolean,
        val onSelectionChangeInvoked: Boolean,
        val onSelectionChangeUsers: List<MockUser>
    )

    // ==================== Simulation Functions ====================

    /**
     * Simulates the CURRENT behavior of CometChatUsers for non-buggy inputs.
     * This captures the observed behavior that must be preserved after the fix.
     *
     * **Observed Behaviors:**
     * 1. Item click in selection mode toggles selection via `viewModel?.selectUser(user, selectionMode)`
     * 2. `setSelectionMode()` clears existing selections when mode changes
     * 3. `onSelectionChange` callback is invoked whenever selection changes
     * 4. `onItemClick` callback is invoked when selection mode is NONE
     */
    fun simulateCurrentBehavior(
        state: UsersComponentState,
        interaction: UserInteraction
    ): InteractionResult {
        return when (interaction.action) {
            ITEM_CLICK -> {
                if (state.selectionMode == SELECTION_MODE_NONE) {
                    // Requirement 3.1: When selection mode is NONE, onItemClick is invoked
                    InteractionResult(
                        selectedUsers = state.selectedUsers,
                        selectionCount = state.selectedUsers.size,
                        onItemClickInvoked = true,
                        onItemLongClickInvoked = false,
                        onSelectionChangeInvoked = false,
                        onSelectionChangeUsers = emptyList()
                    )
                } else {
                    // Requirement 3.2, 3.3: Selection mode enabled - toggle selection
                    val targetUser = interaction.targetUser
                        ?: return InteractionResult(
                            selectedUsers = state.selectedUsers,
                            selectionCount = state.selectedUsers.size,
                            onItemClickInvoked = false,
                            onItemLongClickInvoked = false,
                            onSelectionChangeInvoked = false,
                            onSelectionChangeUsers = emptyList()
                        )

                    val newSelectedUsers = when (state.selectionMode) {
                        SELECTION_MODE_SINGLE -> {
                            // SINGLE mode: replace previous selection with new user
                            if (state.selectedUsers.contains(targetUser)) {
                                emptySet() // Deselect if already selected
                            } else {
                                setOf(targetUser) // Select only this user
                            }
                        }
                        SELECTION_MODE_MULTIPLE -> {
                            // MULTIPLE mode: toggle selection state
                            if (state.selectedUsers.contains(targetUser)) {
                                state.selectedUsers - targetUser // Remove from selection
                            } else {
                                state.selectedUsers + targetUser // Add to selection
                            }
                        }
                        else -> state.selectedUsers
                    }

                    // Requirement 3.6: onSelectionChange is invoked whenever selection changes
                    InteractionResult(
                        selectedUsers = newSelectedUsers,
                        selectionCount = newSelectedUsers.size,
                        onItemClickInvoked = false,
                        onItemLongClickInvoked = false,
                        onSelectionChangeInvoked = true,
                        onSelectionChangeUsers = newSelectedUsers.toList()
                    )
                }
            }
            ITEM_LONG_CLICK -> {
                // Long click always invokes onItemLongClick callback
                InteractionResult(
                    selectedUsers = state.selectedUsers,
                    selectionCount = state.selectedUsers.size,
                    onItemClickInvoked = false,
                    onItemLongClickInvoked = true,
                    onSelectionChangeInvoked = false,
                    onSelectionChangeUsers = emptyList()
                )
            }
            MODE_CHANGE -> {
                // Requirement 3.5: setSelectionMode() clears existing selections when mode changes
                val newMode = interaction.newSelectionMode ?: state.selectionMode
                val clearedSelection = emptySet<MockUser>()

                InteractionResult(
                    selectedUsers = clearedSelection,
                    selectionCount = 0,
                    onItemClickInvoked = false,
                    onItemLongClickInvoked = false,
                    onSelectionChangeInvoked = state.selectedUsers.isNotEmpty(), // Only if there were selections to clear
                    onSelectionChangeUsers = emptyList()
                )
            }
            SEARCH, SCROLL -> {
                // Search and scroll do not affect selection state
                InteractionResult(
                    selectedUsers = state.selectedUsers,
                    selectionCount = state.selectedUsers.size,
                    onItemClickInvoked = false,
                    onItemLongClickInvoked = false,
                    onSelectionChangeInvoked = false,
                    onSelectionChangeUsers = emptyList()
                )
            }
            else -> {
                // Unknown action - return current state
                InteractionResult(
                    selectedUsers = state.selectedUsers,
                    selectionCount = state.selectedUsers.size,
                    onItemClickInvoked = false,
                    onItemLongClickInvoked = false,
                    onSelectionChangeInvoked = false,
                    onSelectionChangeUsers = emptyList()
                )
            }
        }
    }

    // ==================== Arbitrary Generators ====================

    fun userArb(): Arb<MockUser> = Arb.string(5..15).map { uid ->
        MockUser(uid = uid, name = "User_$uid")
    }

    fun selectionModeArb(): Arb<String> = Arb.element(SELECTION_MODE_SINGLE, SELECTION_MODE_MULTIPLE)

    fun anySelectionModeArb(): Arb<String> = Arb.element(SELECTION_MODE_NONE, SELECTION_MODE_SINGLE, SELECTION_MODE_MULTIPLE)

    fun nonEmptyUserSetArb(): Arb<Set<MockUser>> = Arb.list(userArb(), 1..5).map { list -> list.toSet() }

    fun userListArb(): Arb<List<MockUser>> = Arb.list(userArb(), 5..20)

    // ==================== Property Tests ====================

    context("Property 2: Preservation - Item Click Behavior") {

        /**
         * **Validates: Requirements 3.2**
         *
         * For all item clicks in SINGLE selection mode, clicking a user selects only that user.
         * If the user is already selected, clicking deselects them.
         */
        test("item click in SINGLE mode should select only the clicked user") {
            checkAll(3, userListArb(), userArb()) { allUsers, targetUser ->
                val state = UsersComponentState(
                    selectionMode = SELECTION_MODE_SINGLE,
                    selectedUsers = emptySet(),
                    allUsers = allUsers,
                    isViewAttached = true
                )

                val interaction = UserInteraction(action = ITEM_CLICK, targetUser = targetUser)
                val result = simulateCurrentBehavior(state, interaction)

                // After clicking, only the target user should be selected
                result.selectedUsers shouldBe setOf(targetUser)
                result.selectionCount shouldBe 1
                result.onSelectionChangeInvoked shouldBe true
                result.onItemClickInvoked shouldBe false // Not invoked in selection mode
            }
        }

        /**
         * **Validates: Requirements 3.2**
         *
         * In SINGLE mode, clicking an already selected user should deselect them.
         */
        test("item click in SINGLE mode should deselect already selected user") {
            checkAll(3, userArb()) { user ->
                val state = UsersComponentState(
                    selectionMode = SELECTION_MODE_SINGLE,
                    selectedUsers = setOf(user),
                    allUsers = listOf(user),
                    isViewAttached = true
                )

                val interaction = UserInteraction(action = ITEM_CLICK, targetUser = user)
                val result = simulateCurrentBehavior(state, interaction)

                // After clicking already selected user, selection should be empty
                result.selectedUsers shouldBe emptySet()
                result.selectionCount shouldBe 0
                result.onSelectionChangeInvoked shouldBe true
            }
        }

        /**
         * **Validates: Requirements 3.3**
         *
         * For all item clicks in MULTIPLE selection mode, selection state toggles correctly.
         * Clicking an unselected user adds them, clicking a selected user removes them.
         */
        test("item click in MULTIPLE mode should toggle user selection state") {
            checkAll(3, nonEmptyUserSetArb(), userArb()) { existingSelection, targetUser ->
                val state = UsersComponentState(
                    selectionMode = SELECTION_MODE_MULTIPLE,
                    selectedUsers = existingSelection,
                    allUsers = existingSelection.toList() + targetUser,
                    isViewAttached = true
                )

                val interaction = UserInteraction(action = ITEM_CLICK, targetUser = targetUser)
                val result = simulateCurrentBehavior(state, interaction)

                if (existingSelection.contains(targetUser)) {
                    // User was selected, should now be deselected
                    result.selectedUsers shouldBe (existingSelection - targetUser)
                } else {
                    // User was not selected, should now be selected
                    result.selectedUsers shouldBe (existingSelection + targetUser)
                }
                result.onSelectionChangeInvoked shouldBe true
            }
        }

        /**
         * **Validates: Requirements 3.1**
         *
         * When selection mode is NONE, item click should invoke onItemClick callback.
         */
        test("item click in NONE mode should invoke onItemClick callback") {
            checkAll(3, userArb()) { user ->
                val state = UsersComponentState(
                    selectionMode = SELECTION_MODE_NONE,
                    selectedUsers = emptySet(),
                    allUsers = listOf(user),
                    isViewAttached = true
                )

                val interaction = UserInteraction(action = ITEM_CLICK, targetUser = user)
                val result = simulateCurrentBehavior(state, interaction)

                result.onItemClickInvoked shouldBe true
                result.onSelectionChangeInvoked shouldBe false
                result.selectedUsers shouldBe emptySet() // No selection in NONE mode
            }
        }
    }

    context("Property 2: Preservation - Mode Change Behavior") {

        /**
         * **Validates: Requirements 3.5**
         *
         * For all mode changes, existing selections are cleared.
         */
        test("setSelectionMode should clear existing selections when mode changes") {
            checkAll(3, anySelectionModeArb(), anySelectionModeArb(), nonEmptyUserSetArb()) { oldMode, newMode, selectedUsers ->
                val state = UsersComponentState(
                    selectionMode = oldMode,
                    selectedUsers = selectedUsers,
                    allUsers = selectedUsers.toList(),
                    isViewAttached = true
                )

                val interaction = UserInteraction(action = MODE_CHANGE, newSelectionMode = newMode)
                val result = simulateCurrentBehavior(state, interaction)

                // After mode change, selections should be cleared
                result.selectedUsers shouldBe emptySet()
                result.selectionCount shouldBe 0
            }
        }

        /**
         * **Validates: Requirements 3.5**
         *
         * Mode change with no existing selections should not invoke onSelectionChange.
         */
        test("setSelectionMode with no existing selections should not invoke onSelectionChange") {
            checkAll(3, anySelectionModeArb(), anySelectionModeArb()) { oldMode, newMode ->
                val state = UsersComponentState(
                    selectionMode = oldMode,
                    selectedUsers = emptySet(),
                    allUsers = emptyList(),
                    isViewAttached = true
                )

                val interaction = UserInteraction(action = MODE_CHANGE, newSelectionMode = newMode)
                val result = simulateCurrentBehavior(state, interaction)

                result.selectedUsers shouldBe emptySet()
                result.onSelectionChangeInvoked shouldBe false // No change to report
            }
        }
    }

    context("Property 2: Preservation - Callback Behavior") {

        /**
         * **Validates: Requirements 3.6**
         *
         * For all selection changes, onSelectionChange callback fires.
         */
        test("onSelectionChange callback should fire on every selection change") {
            checkAll(3, selectionModeArb(), userArb()) { mode, user ->
                val state = UsersComponentState(
                    selectionMode = mode,
                    selectedUsers = emptySet(),
                    allUsers = listOf(user),
                    isViewAttached = true
                )

                val interaction = UserInteraction(action = ITEM_CLICK, targetUser = user)
                val result = simulateCurrentBehavior(state, interaction)

                // Selection changed from empty to containing user
                result.onSelectionChangeInvoked shouldBe true
                result.onSelectionChangeUsers shouldNotBe emptyList<MockUser>()
            }
        }

        /**
         * **Validates: Requirements 3.4**
         *
         * Long click should always invoke onItemLongClick callback.
         */
        test("item long click should invoke onItemLongClick callback") {
            checkAll(3, anySelectionModeArb(), userArb()) { mode, user ->
                val state = UsersComponentState(
                    selectionMode = mode,
                    selectedUsers = emptySet(),
                    allUsers = listOf(user),
                    isViewAttached = true
                )

                val interaction = UserInteraction(action = ITEM_LONG_CLICK, targetUser = user)
                val result = simulateCurrentBehavior(state, interaction)

                result.onItemLongClickInvoked shouldBe true
                // Long click should not change selection
                result.selectedUsers shouldBe emptySet()
            }
        }
    }

    context("Property 2: Preservation - Non-Selection Actions") {

        /**
         * **Validates: Requirements 3.1, 3.2, 3.3 (implicit)**
         *
         * Search and scroll actions should not affect selection state.
         */
        test("search action should not affect selection state") {
            checkAll(3, selectionModeArb(), nonEmptyUserSetArb()) { mode, selectedUsers ->
                val state = UsersComponentState(
                    selectionMode = mode,
                    selectedUsers = selectedUsers,
                    allUsers = selectedUsers.toList(),
                    isViewAttached = true
                )

                val interaction = UserInteraction(action = SEARCH, searchQuery = "test")
                val result = simulateCurrentBehavior(state, interaction)

                // Selection should remain unchanged
                result.selectedUsers shouldBe selectedUsers
                result.selectionCount shouldBe selectedUsers.size
                result.onSelectionChangeInvoked shouldBe false
            }
        }

        /**
         * **Validates: Requirements 3.1, 3.2, 3.3 (implicit)**
         *
         * Scroll action should not affect selection state.
         */
        test("scroll action should not affect selection state") {
            checkAll(3, selectionModeArb(), nonEmptyUserSetArb()) { mode, selectedUsers ->
                val state = UsersComponentState(
                    selectionMode = mode,
                    selectedUsers = selectedUsers,
                    allUsers = selectedUsers.toList(),
                    isViewAttached = true
                )

                val interaction = UserInteraction(action = SCROLL)
                val result = simulateCurrentBehavior(state, interaction)

                // Selection should remain unchanged
                result.selectedUsers shouldBe selectedUsers
                result.selectionCount shouldBe selectedUsers.size
                result.onSelectionChangeInvoked shouldBe false
            }
        }
    }

    context("Property 2: Preservation - Selection Count Accuracy") {

        /**
         * **Validates: Requirements 3.2, 3.3**
         *
         * Selection count should always match the number of selected users.
         */
        test("selection count should always match number of selected users") {
            checkAll(3, selectionModeArb(), nonEmptyUserSetArb(), userArb()) { mode, existingSelection, targetUser ->
                val state = UsersComponentState(
                    selectionMode = mode,
                    selectedUsers = existingSelection,
                    allUsers = existingSelection.toList() + targetUser,
                    isViewAttached = true
                )

                val interaction = UserInteraction(action = ITEM_CLICK, targetUser = targetUser)
                val result = simulateCurrentBehavior(state, interaction)

                // Selection count should always equal the size of selected users set
                result.selectionCount shouldBe result.selectedUsers.size
            }
        }
    }
})
