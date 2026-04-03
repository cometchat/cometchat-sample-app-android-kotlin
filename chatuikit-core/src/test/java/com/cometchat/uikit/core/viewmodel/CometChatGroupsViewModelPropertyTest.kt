package com.cometchat.uikit.core.viewmodel

import com.cometchat.uikit.core.state.GroupsUIState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Test data class that simulates Group with guid-based equality.
 */
private data class TestGroup(
    val guid: String,
    val name: String = "Test Group",
    val membersCount: Int = 0
)

/**
 * Sealed class representing UI states for testing.
 */
private sealed class TestUIState {
    object Loading : TestUIState()
    object Empty : TestUIState()
    data class Error(val message: String) : TestUIState()
    data class Content(val groups: List<TestGroup>) : TestUIState()
}

/**
 * Test ViewModel that implements ListOperations using the same pattern as
 * CometChatGroupsViewModel. This validates the interface + delegation pattern.
 */
private class TestGroupsViewModel : ListOperations<TestGroup> {
    private val _groups = MutableStateFlow<List<TestGroup>>(emptyList())
    val groups = _groups

    private val _uiState = MutableStateFlow<TestUIState>(TestUIState.Loading)
    val uiState = _uiState

    private val _selectedGroups = MutableStateFlow<Set<TestGroup>>(emptySet())
    val selectedGroups = _selectedGroups

    private val listDelegate = ListOperationsDelegate(
        stateFlow = _groups,
        equalityChecker = { a, b -> a.guid == b.guid }
    )

    override fun addItem(item: TestGroup) = listDelegate.addItem(item)
    override fun addItems(items: List<TestGroup>) = listDelegate.addItems(items)
    override fun removeItem(item: TestGroup) = listDelegate.removeItem(item)
    override fun removeItemAt(index: Int) = listDelegate.removeItemAt(index)
    override fun updateItem(item: TestGroup, predicate: (TestGroup) -> Boolean) =
        listDelegate.updateItem(item, predicate)
    override fun clearItems() = listDelegate.clearItems()
    override fun getItems() = listDelegate.getItems()
    override fun getItemAt(index: Int) = listDelegate.getItemAt(index)
    override fun getItemCount() = listDelegate.getItemCount()
    override fun moveItemToTop(item: TestGroup) = listDelegate.moveItemToTop(item)
    override fun batch(operations: ListOperationsBatchScope<TestGroup>.() -> Unit) =
        listDelegate.batch(operations)

    /**
     * Simulates single selection mode - only one group can be selected.
     */
    fun selectGroupSingle(group: TestGroup) {
        _selectedGroups.value = setOf(group)
    }

    /**
     * Simulates multiple selection mode - toggles selection.
     */
    fun selectGroupMultiple(group: TestGroup) {
        val current = _selectedGroups.value.toMutableSet()
        if (current.any { it.guid == group.guid }) {
            current.removeAll { it.guid == group.guid }
        } else {
            current.add(group)
        }
        _selectedGroups.value = current
    }

    /**
     * Clears all selections.
     */
    fun clearSelection() {
        _selectedGroups.value = emptySet()
    }

    /**
     * Returns the list of selected groups.
     */
    fun getSelectedGroups(): List<TestGroup> = _selectedGroups.value.toList()

    /**
     * Updates UI state based on groups list.
     */
    fun updateUIState() {
        _uiState.value = if (_groups.value.isEmpty()) {
            TestUIState.Empty
        } else {
            TestUIState.Content(_groups.value)
        }
    }

    /**
     * Sets error state.
     */
    fun setError(message: String) {
        _uiState.value = TestUIState.Error(message)
    }

    /**
     * Sets loading state.
     */
    fun setLoading() {
        _uiState.value = TestUIState.Loading
    }
}

/**
 * Property-based tests for CometChatGroupsViewModel.
 * 
 * Since CometChatGroupsViewModel depends on SDK classes with private constructors,
 * we test the interface implementation pattern using a test ViewModel that mirrors
 * the production implementation.
 * 
 * Feature: groups-component
 * Validates: Requirements 1.9, 4.2, 4.3, 4.6, 6.2, 6.3, 6.4
 */
class CometChatGroupsViewModelPropertyTest : FunSpec({

    // Feature: groups-component, Property 1: ListOperations Consistency
    // *For any* sequence of ListOperations (addItem, removeItem, updateItem, clearItems)
    // on the Groups ViewModel, the resulting groups list should accurately reflect
    // all applied operations in order.
    // **Validates: Requirements 1.9**
    context("Property 1: ListOperations Consistency") {

        test("addItem should add group to list") {
            checkAll(100, Arb.string(), Arb.string()) { guid, name ->
                val viewModel = TestGroupsViewModel()
                val group = TestGroup(guid, name)
                val sizeBefore = viewModel.getItemCount()

                viewModel.addItem(group)

                viewModel.getItemCount() shouldBe sizeBefore + 1
                viewModel.getItems() shouldContain group
            }
        }

        test("addItems should add multiple groups") {
            checkAll(100, Arb.list(Arb.string(), 0..20)) { guids ->
                val viewModel = TestGroupsViewModel()
                val groups = guids.mapIndexed { index, guid -> TestGroup("$guid-$index") }

                viewModel.addItems(groups)

                viewModel.getItemCount() shouldBe groups.size
            }
        }

        test("removeItem should use guid equality") {
            checkAll(100, Arb.list(Arb.string(), 1..20)) { guids ->
                val viewModel = TestGroupsViewModel()
                val groups = guids.mapIndexed { index, guid -> TestGroup("$guid-$index") }
                viewModel.addItems(groups)
                val groupToRemove = groups.random()
                val sizeBefore = viewModel.getItemCount()

                // Create different instance with same guid
                val toRemove = TestGroup(groupToRemove.guid, "Different Name")
                val result = viewModel.removeItem(toRemove)

                result shouldBe true
                viewModel.getItemCount() shouldBe sizeBefore - 1
            }
        }

        test("updateItem should replace matching group") {
            checkAll(100, Arb.list(Arb.string(), 1..20), Arb.string()) { guids, newName ->
                val viewModel = TestGroupsViewModel()
                val groups = guids.mapIndexed { index, guid -> TestGroup("$guid-$index") }
                viewModel.addItems(groups)
                val groupToUpdate = groups.random()
                val sizeBefore = viewModel.getItemCount()

                val updated = TestGroup(groupToUpdate.guid, newName)
                val result = viewModel.updateItem(updated) { it.guid == groupToUpdate.guid }

                result shouldBe true
                viewModel.getItemCount() shouldBe sizeBefore
                viewModel.getItems().find { it.guid == groupToUpdate.guid }?.name shouldBe newName
            }
        }

        test("clearItems should empty the list") {
            checkAll(100, Arb.list(Arb.string(), 0..50)) { guids ->
                val viewModel = TestGroupsViewModel()
                val groups = guids.mapIndexed { index, guid -> TestGroup("$guid-$index") }
                viewModel.addItems(groups)

                viewModel.clearItems()

                viewModel.getItemCount() shouldBe 0
                viewModel.getItems() shouldBe emptyList()
            }
        }

        test("sequence of operations should reflect all changes") {
            checkAll(100, Arb.list(Arb.string(), 3..10)) { guids ->
                val viewModel = TestGroupsViewModel()
                val groups = guids.mapIndexed { index, guid -> TestGroup("$guid-$index") }

                // Add all groups
                viewModel.addItems(groups)
                viewModel.getItemCount() shouldBe groups.size

                // Remove first group
                viewModel.removeItem(groups.first())
                viewModel.getItemCount() shouldBe groups.size - 1
                viewModel.getItems() shouldNotContain groups.first()

                // Update second group (now first in list)
                val remaining = viewModel.getItems()
                if (remaining.isNotEmpty()) {
                    val toUpdate = remaining.first()
                    val updated = TestGroup(toUpdate.guid, "Updated Name")
                    viewModel.updateItem(updated) { it.guid == toUpdate.guid }
                    viewModel.getItems().find { it.guid == toUpdate.guid }?.name shouldBe "Updated Name"
                }

                // Clear all
                viewModel.clearItems()
                viewModel.getItemCount() shouldBe 0
            }
        }
    }

    // Feature: groups-component, Property 4: Single Selection Mode Exclusivity
    // *For any* sequence of group selections in SINGLE mode, the selectedGroups set
    // should contain exactly one group—the most recently selected group.
    // **Validates: Requirements 4.2**
    context("Property 4: Single Selection Mode Exclusivity") {

        test("single selection mode should only keep last selected group") {
            checkAll(100, Arb.list(Arb.string(), 1..10)) { guids ->
                val viewModel = TestGroupsViewModel()
                val groups = guids.mapIndexed { index, guid -> TestGroup("$guid-$index") }
                viewModel.addItems(groups)

                // Select each group in sequence
                groups.forEach { group ->
                    viewModel.selectGroupSingle(group)
                }

                viewModel.selectedGroups.value.size shouldBe 1
                viewModel.selectedGroups.value.first() shouldBe groups.last()
            }
        }

        test("single selection should replace previous selection") {
            checkAll(100, Arb.string(), Arb.string()) { guid1, guid2 ->
                val viewModel = TestGroupsViewModel()
                val group1 = TestGroup("$guid1-1")
                val group2 = TestGroup("$guid2-2")
                viewModel.addItems(listOf(group1, group2))

                viewModel.selectGroupSingle(group1)
                viewModel.selectedGroups.value.size shouldBe 1
                viewModel.selectedGroups.value.first() shouldBe group1

                viewModel.selectGroupSingle(group2)
                viewModel.selectedGroups.value.size shouldBe 1
                viewModel.selectedGroups.value.first() shouldBe group2
            }
        }
    }

    // Feature: groups-component, Property 5: Multiple Selection Mode Accumulation
    // *For any* sequence of group selections in MULTIPLE mode, toggling a group's
    // selection should add it to the set if not present, or remove it if already present.
    // The final set should contain exactly the groups with an odd number of toggles.
    // **Validates: Requirements 4.3**
    context("Property 5: Multiple Selection Mode Accumulation") {

        test("multiple selection mode should toggle selection") {
            checkAll(100, Arb.list(Arb.string(), 1..10)) { guids ->
                val viewModel = TestGroupsViewModel()
                val groups = guids.mapIndexed { index, guid -> TestGroup("$guid-$index") }
                viewModel.addItems(groups)

                // Select all groups
                groups.forEach { group ->
                    viewModel.selectGroupMultiple(group)
                }

                viewModel.selectedGroups.value.size shouldBe groups.size

                // Deselect all groups (toggle again)
                groups.forEach { group ->
                    viewModel.selectGroupMultiple(group)
                }

                viewModel.selectedGroups.value.size shouldBe 0
            }
        }

        test("odd number of toggles should result in selection") {
            checkAll(100, Arb.string(), Arb.int(1..5)) { guid, toggleCount ->
                val viewModel = TestGroupsViewModel()
                val group = TestGroup(guid)
                viewModel.addItem(group)

                // Toggle the specified number of times
                repeat(toggleCount) {
                    viewModel.selectGroupMultiple(group)
                }

                val expectedSelected = toggleCount % 2 == 1
                if (expectedSelected) {
                    viewModel.selectedGroups.value shouldContain group
                } else {
                    viewModel.selectedGroups.value shouldNotContain group
                }
            }
        }

        test("multiple selection should accumulate different groups") {
            checkAll(100, Arb.list(Arb.string(), 2..10)) { guids ->
                val viewModel = TestGroupsViewModel()
                val groups = guids.mapIndexed { index, guid -> TestGroup("$guid-$index") }
                viewModel.addItems(groups)

                // Select first half
                val firstHalf = groups.take(groups.size / 2)
                firstHalf.forEach { group ->
                    viewModel.selectGroupMultiple(group)
                }

                viewModel.selectedGroups.value.size shouldBe firstHalf.size
                firstHalf.forEach { group ->
                    viewModel.selectedGroups.value.any { it.guid == group.guid } shouldBe true
                }
            }
        }
    }

    // Feature: groups-component, Property 7: Clear Selection Completeness
    // *For any* selection state, after invoking clearSelection(), the selectedGroups
    // set should be empty.
    // **Validates: Requirements 4.6**
    context("Property 7: Clear Selection Completeness") {

        test("clearSelection should empty the selection set") {
            checkAll(100, Arb.list(Arb.string(), 1..20)) { guids ->
                val viewModel = TestGroupsViewModel()
                val groups = guids.mapIndexed { index, guid -> TestGroup("$guid-$index") }
                viewModel.addItems(groups)

                // Select all groups
                groups.forEach { group ->
                    viewModel.selectGroupMultiple(group)
                }

                viewModel.clearSelection()

                viewModel.selectedGroups.value shouldBe emptySet()
                viewModel.getSelectedGroups() shouldBe emptyList()
            }
        }

        test("clearSelection on empty selection should remain empty") {
            val viewModel = TestGroupsViewModel()

            viewModel.clearSelection()

            viewModel.selectedGroups.value shouldBe emptySet()
        }

        test("clearSelection should work after single selection") {
            checkAll(100, Arb.string()) { guid ->
                val viewModel = TestGroupsViewModel()
                val group = TestGroup(guid)
                viewModel.addItem(group)

                viewModel.selectGroupSingle(group)
                viewModel.selectedGroups.value.size shouldBe 1

                viewModel.clearSelection()
                viewModel.selectedGroups.value shouldBe emptySet()
            }
        }
    }

    // Feature: groups-component, Property 12: UI State Consistency
    // *For any* groups list state:
    // - If the list is empty and not loading, UI state should be Empty
    // - If an error occurred during fetch, UI state should be Error
    // - If the list has groups, UI state should be Content with those groups
    // **Validates: Requirements 6.2, 6.3, 6.4**
    context("Property 12: UI State Consistency") {

        test("empty list should result in Empty state") {
            checkAll(100, Arb.list(Arb.string(), 0..20)) { guids ->
                val viewModel = TestGroupsViewModel()
                val groups = guids.mapIndexed { index, guid -> TestGroup("$guid-$index") }
                viewModel.addItems(groups)
                viewModel.clearItems()

                viewModel.updateUIState()

                viewModel.uiState.value.shouldBeInstanceOf<TestUIState.Empty>()
            }
        }

        test("non-empty list should result in Content state") {
            checkAll(100, Arb.list(Arb.string(), 1..20)) { guids ->
                val viewModel = TestGroupsViewModel()
                val groups = guids.mapIndexed { index, guid -> TestGroup("$guid-$index") }
                viewModel.addItems(groups)

                viewModel.updateUIState()

                val state = viewModel.uiState.value
                state.shouldBeInstanceOf<TestUIState.Content>()
                (state as TestUIState.Content).groups.size shouldBe groups.size
            }
        }

        test("error should result in Error state") {
            checkAll(100, Arb.string()) { errorMessage ->
                val viewModel = TestGroupsViewModel()

                viewModel.setError(errorMessage)

                val state = viewModel.uiState.value
                state.shouldBeInstanceOf<TestUIState.Error>()
                (state as TestUIState.Error).message shouldBe errorMessage
            }
        }

        test("initial state should be Loading") {
            val viewModel = TestGroupsViewModel()

            viewModel.uiState.value.shouldBeInstanceOf<TestUIState.Loading>()
        }

        test("state transitions should be consistent with list changes") {
            checkAll(100, Arb.list(Arb.string(), 1..10)) { guids ->
                val viewModel = TestGroupsViewModel()
                val groups = guids.mapIndexed { index, guid -> TestGroup("$guid-$index") }

                // Initial state is Loading
                viewModel.uiState.value.shouldBeInstanceOf<TestUIState.Loading>()

                // Add groups -> Content
                viewModel.addItems(groups)
                viewModel.updateUIState()
                viewModel.uiState.value.shouldBeInstanceOf<TestUIState.Content>()

                // Clear groups -> Empty
                viewModel.clearItems()
                viewModel.updateUIState()
                viewModel.uiState.value.shouldBeInstanceOf<TestUIState.Empty>()

                // Set error -> Error
                viewModel.setError("Test error")
                viewModel.uiState.value.shouldBeInstanceOf<TestUIState.Error>()

                // Set loading -> Loading
                viewModel.setLoading()
                viewModel.uiState.value.shouldBeInstanceOf<TestUIState.Loading>()
            }
        }
    }
})
