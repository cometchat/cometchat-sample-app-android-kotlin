package com.cometchat.uikit.core.state

import com.cometchat.uikit.core.testutils.MockFactory
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Unit tests for GroupsUIState sealed class.
 *
 * Verifies construction, type checking, property access, and exhaustiveness
 * for all variants: Loading, Empty, Error, Content.
 *
 * The tests validate:
 * - Loading is singleton
 * - Empty is singleton
 * - Error holds exception with correct code
 * - Content holds groups list
 * - All 4 types are distinct
 * - PBT: any error code/message preserved
 * - when() exhaustiveness
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*.GroupsUIStateTest"
 */
class GroupsUIStateTest : FunSpec({

    beforeTest {
        println("\n    🧪 ${it.name.testName}")
        println("    ─────────────────────────────────────────────────")
    }

    // ==================== Singleton Tests ====================

    test("Loading should be a singleton instance") {
        println("    [ACTION] Creating GroupsUIState.Loading")
        val state: GroupsUIState = GroupsUIState.Loading

        println("    [RESULT] state type = ${state::class.simpleName}")
        state.shouldBeInstanceOf<GroupsUIState.Loading>()

        // Verify singleton identity
        val state2: GroupsUIState = GroupsUIState.Loading
        println("    [VERIFY] state === state2: ${state === state2}")
        (state === state2) shouldBe true
        println("    ✅ PASSED — Loading is a singleton instance")
    }

    test("Empty should be a singleton instance") {
        println("    [ACTION] Creating GroupsUIState.Empty")
        val state: GroupsUIState = GroupsUIState.Empty

        println("    [RESULT] state type = ${state::class.simpleName}")
        state.shouldBeInstanceOf<GroupsUIState.Empty>()

        // Verify singleton identity
        val state2: GroupsUIState = GroupsUIState.Empty
        println("    [VERIFY] state === state2: ${state === state2}")
        (state === state2) shouldBe true
        println("    ✅ PASSED — Empty is a singleton instance")
    }

    // ==================== Data Class Tests ====================

    test("Error should hold exception with correct code and message") {
        val exception = MockFactory.createCometChatException("ERR_NET", "Network timeout")
        println("    [ACTION] Creating GroupsUIState.Error with exception(ERR_NET, Network timeout)")

        val state = GroupsUIState.Error(exception)

        println("    [RESULT] state type = ${state::class.simpleName}")
        println("    [RESULT] state.exception.code = ${state.exception.code}")
        println("    [RESULT] state.exception.message = ${state.exception.message}")
        state.shouldBeInstanceOf<GroupsUIState.Error>()
        state.exception shouldBe exception
        state.exception.code shouldBe "ERR_NET"
        state.exception.message shouldBe "Network timeout"
        println("    ✅ PASSED — Error holds exception with correct code and message")
    }

    test("Content should hold groups list") {
        val groups = listOf(
            MockFactory.createGroup(guid = "g1", name = "Group 1"),
            MockFactory.createGroup(guid = "g2", name = "Group 2"),
            MockFactory.createGroup(guid = "g3", name = "Group 3")
        )
        println("    [ACTION] Creating GroupsUIState.Content with 3 groups")

        val state = GroupsUIState.Content(groups)

        println("    [RESULT] state type = ${state::class.simpleName}")
        println("    [RESULT] state.groups.size = ${state.groups.size}")
        println("    [RESULT] state.groups GUIDs = ${state.groups.map { it.guid }}")
        state.shouldBeInstanceOf<GroupsUIState.Content>()
        state.groups shouldBe groups
        state.groups.size shouldBe 3
        println("    ✅ PASSED — Content holds groups list correctly")
    }

    // ==================== Distinctness Tests ====================

    test("All 4 state types should be distinct") {
        val exception = MockFactory.createCometChatException("ERR", "Error")
        val groups = listOf(MockFactory.createGroup(guid = "g1", name = "Group 1"))

        val loading: GroupsUIState = GroupsUIState.Loading
        val empty: GroupsUIState = GroupsUIState.Empty
        val error: GroupsUIState = GroupsUIState.Error(exception)
        val content: GroupsUIState = GroupsUIState.Content(groups)

        println("    [ACTION] Creating all 4 state types")
        println("    [RESULT] Loading type = ${loading::class.simpleName}")
        println("    [RESULT] Empty type = ${empty::class.simpleName}")
        println("    [RESULT] Error type = ${error::class.simpleName}")
        println("    [RESULT] Content type = ${content::class.simpleName}")

        // All should be different
        loading shouldNotBe empty
        loading shouldNotBe error
        loading shouldNotBe content
        empty shouldNotBe error
        empty shouldNotBe content
        error shouldNotBe content

        println("    [VERIFY] All 6 pairwise comparisons are not-equal ✓")
        println("    ✅ PASSED — All 4 state types are distinct")
    }

    // ==================== Property-based Tests ====================

    test("PBT: any error code and message should be preserved in Error state") {
        println("    [TEST] PBT - any error code/message preserved")
        checkAll(20, Arb.string(1, 30), Arb.string(1, 100)) { code, message ->
            val exception = MockFactory.createCometChatException(code, message)
            val state = GroupsUIState.Error(exception)

            state.exception.code shouldBe code
            state.exception.message shouldBe message
        }
        println("    ✅ PASSED — PBT verified: any error code/message preserved in Error state")
    }

    // ==================== Exhaustiveness Test ====================

    test("when() expression should be exhaustive over all GroupsUIState variants") {
        val exception = MockFactory.createCometChatException("ERR", "Test")
        val groups = listOf(MockFactory.createGroup(guid = "g1", name = "Group 1"))

        val states = listOf(
            GroupsUIState.Loading,
            GroupsUIState.Empty,
            GroupsUIState.Error(exception),
            GroupsUIState.Content(groups)
        )
        println("    [ACTION] Testing when() exhaustiveness over ${states.size} states")

        val results = states.map { state ->
            when (state) {
                is GroupsUIState.Loading -> "loading"
                is GroupsUIState.Empty -> "empty"
                is GroupsUIState.Error -> "error:${state.exception.code}"
                is GroupsUIState.Content -> "content:${state.groups.size}"
            }
        }

        println("    [RESULT] when() results: $results")
        results shouldBe listOf("loading", "empty", "error:ERR", "content:1")
        println("    [VERIFY] All 4 branches matched correctly ✓")
        println("    ✅ PASSED — when() expression is exhaustive over all GroupsUIState variants")
    }

    // ==================== Data Class Equality ====================

    test("Error data class equality should work correctly") {
        val ex1 = MockFactory.createCometChatException("ERR", "msg")
        val state1 = GroupsUIState.Error(ex1)
        val state2 = GroupsUIState.Error(ex1)

        println("    [ACTION] Comparing two Error states with same exception")
        println("    [RESULT] state1 == state2: ${state1 == state2}")
        state1 shouldBe state2
        println("    ✅ PASSED — Error data class equality works correctly")
    }

    test("Content data class equality should work correctly") {
        val groups = listOf(MockFactory.createGroup(guid = "g1", name = "Group 1"))
        val state1 = GroupsUIState.Content(groups)
        val state2 = GroupsUIState.Content(groups)

        println("    [ACTION] Comparing two Content states with same groups list")
        println("    [RESULT] state1 == state2: ${state1 == state2}")
        state1 shouldBe state2
        println("    ✅ PASSED — Content data class equality works correctly")
    }
})
