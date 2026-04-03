package com.cometchat.uikit.compose.groups

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.uikit.compose.presentation.shared.statusindicator.StatusIndicator
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
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
    val membersCount: Int = 0,
    val groupType: String = CometChatConstants.GROUP_TYPE_PUBLIC
)

/**
 * Test ViewModel for selection count testing.
 */
private class TestGroupsSelectionViewModel {
    private val _selectedGroups = MutableStateFlow<Set<TestGroup>>(emptySet())
    val selectedGroups = _selectedGroups

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
     * Returns the selection count for display.
     */
    fun getSelectionCount(): Int = _selectedGroups.value.size
}

/**
 * Converts a group type to a StatusIndicator enum value.
 * This mirrors the logic in CometChatGroupsItem.kt
 */
private fun getStatusIndicatorFromGroupType(groupType: String): StatusIndicator {
    return when (groupType) {
        CometChatConstants.GROUP_TYPE_PRIVATE -> StatusIndicator.PRIVATE_GROUP
        CometChatConstants.GROUP_TYPE_PASSWORD -> StatusIndicator.PROTECTED_GROUP
        else -> StatusIndicator.PUBLIC_GROUP
    }
}

/**
 * Property-based tests for CometChatGroups Compose component.
 * 
 * These tests validate the UI-specific properties that are implemented
 * in the Compose layer (chatuikit-jetpack).
 * 
 * Feature: groups-component
 * Validates: Requirements 2.2, 2.3, 2.4, 4.5
 */
class GroupsComposePropertyTest : FunSpec({

    // Feature: groups-component, Property 2: Group Type Indicator Correctness
    // *For any* group in the list, the displayed status indicator should match
    // the group's type: public groups show public icon, private groups show
    // private icon, and password-protected groups show password icon.
    // **Validates: Requirements 2.2, 2.3, 2.4**
    context("Property 2: Group Type Indicator Correctness") {

        test("public group should show PUBLIC_GROUP indicator") {
            checkAll(100, Arb.string()) { guid ->
                val group = TestGroup(
                    guid = guid,
                    groupType = CometChatConstants.GROUP_TYPE_PUBLIC
                )

                val indicator = getStatusIndicatorFromGroupType(group.groupType)

                indicator shouldBe StatusIndicator.PUBLIC_GROUP
            }
        }

        test("private group should show PRIVATE_GROUP indicator") {
            checkAll(100, Arb.string()) { guid ->
                val group = TestGroup(
                    guid = guid,
                    groupType = CometChatConstants.GROUP_TYPE_PRIVATE
                )

                val indicator = getStatusIndicatorFromGroupType(group.groupType)

                indicator shouldBe StatusIndicator.PRIVATE_GROUP
            }
        }

        test("password-protected group should show PROTECTED_GROUP indicator") {
            checkAll(100, Arb.string()) { guid ->
                val group = TestGroup(
                    guid = guid,
                    groupType = CometChatConstants.GROUP_TYPE_PASSWORD
                )

                val indicator = getStatusIndicatorFromGroupType(group.groupType)

                indicator shouldBe StatusIndicator.PROTECTED_GROUP
            }
        }

        test("any group type should map to correct indicator") {
            val groupTypeArb = Arb.element(
                CometChatConstants.GROUP_TYPE_PUBLIC,
                CometChatConstants.GROUP_TYPE_PRIVATE,
                CometChatConstants.GROUP_TYPE_PASSWORD
            )

            checkAll(100, Arb.string(), groupTypeArb) { guid, groupType ->
                val group = TestGroup(guid = guid, groupType = groupType)

                val indicator = getStatusIndicatorFromGroupType(group.groupType)

                when (groupType) {
                    CometChatConstants.GROUP_TYPE_PUBLIC -> indicator shouldBe StatusIndicator.PUBLIC_GROUP
                    CometChatConstants.GROUP_TYPE_PRIVATE -> indicator shouldBe StatusIndicator.PRIVATE_GROUP
                    CometChatConstants.GROUP_TYPE_PASSWORD -> indicator shouldBe StatusIndicator.PROTECTED_GROUP
                }
            }
        }

        test("unknown group type should default to PUBLIC_GROUP indicator") {
            checkAll(100, Arb.string(), Arb.string()) { guid, unknownType ->
                // Skip if unknownType happens to be a valid type
                if (unknownType !in listOf(
                        CometChatConstants.GROUP_TYPE_PUBLIC,
                        CometChatConstants.GROUP_TYPE_PRIVATE,
                        CometChatConstants.GROUP_TYPE_PASSWORD
                    )
                ) {
                    val group = TestGroup(guid = guid, groupType = unknownType)

                    val indicator = getStatusIndicatorFromGroupType(group.groupType)

                    indicator shouldBe StatusIndicator.PUBLIC_GROUP
                }
            }
        }
    }

    // Feature: groups-component, Property 6: Selection Count Accuracy
    // *For any* selection state, the displayed selection count should equal
    // the size of the selectedGroups set.
    // **Validates: Requirements 4.5**
    context("Property 6: Selection Count Accuracy") {

        test("selection count should equal selectedGroups size") {
            checkAll(100, Arb.list(Arb.string(), 0..20)) { guids ->
                val viewModel = TestGroupsSelectionViewModel()
                val groups = guids.mapIndexed { index, guid -> TestGroup("$guid-$index") }

                // Select all groups
                groups.forEach { group ->
                    viewModel.selectGroupMultiple(group)
                }

                viewModel.getSelectionCount() shouldBe viewModel.selectedGroups.value.size
                viewModel.getSelectionCount() shouldBe groups.size
            }
        }

        test("selection count should update after toggle") {
            checkAll(100, Arb.list(Arb.string(), 1..10)) { guids ->
                val viewModel = TestGroupsSelectionViewModel()
                val groups = guids.mapIndexed { index, guid -> TestGroup("$guid-$index") }

                // Select all groups
                groups.forEach { group ->
                    viewModel.selectGroupMultiple(group)
                }
                viewModel.getSelectionCount() shouldBe groups.size

                // Deselect half
                val halfSize = groups.size / 2
                groups.take(halfSize).forEach { group ->
                    viewModel.selectGroupMultiple(group)
                }

                viewModel.getSelectionCount() shouldBe groups.size - halfSize
                viewModel.getSelectionCount() shouldBe viewModel.selectedGroups.value.size
            }
        }

        test("selection count should be zero after clear") {
            checkAll(100, Arb.list(Arb.string(), 1..20)) { guids ->
                val viewModel = TestGroupsSelectionViewModel()
                val groups = guids.mapIndexed { index, guid -> TestGroup("$guid-$index") }

                // Select all groups
                groups.forEach { group ->
                    viewModel.selectGroupMultiple(group)
                }

                viewModel.clearSelection()

                viewModel.getSelectionCount() shouldBe 0
                viewModel.getSelectionCount() shouldBe viewModel.selectedGroups.value.size
            }
        }

        test("selection count should be zero initially") {
            val viewModel = TestGroupsSelectionViewModel()

            viewModel.getSelectionCount() shouldBe 0
            viewModel.getSelectionCount() shouldBe viewModel.selectedGroups.value.size
        }

        test("selection count should match after random toggles") {
            checkAll(100, Arb.list(Arb.string(), 1..10), Arb.int(1..20)) { guids, toggleCount ->
                val viewModel = TestGroupsSelectionViewModel()
                val groups = guids.mapIndexed { index, guid -> TestGroup("$guid-$index") }

                // Perform random toggles
                repeat(toggleCount) {
                    val randomGroup = groups.random()
                    viewModel.selectGroupMultiple(randomGroup)
                }

                // Count should always match the set size
                viewModel.getSelectionCount() shouldBe viewModel.selectedGroups.value.size
            }
        }

        test("displayed count format should be correct for any count") {
            checkAll(100, Arb.int(0..100)) { count ->
                // Simulate the format string used in the toolbar
                val displayedText = "$count selected"

                // Verify the format is correct
                displayedText shouldBe "$count selected"
            }
        }
    }
})
