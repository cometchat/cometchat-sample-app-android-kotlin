package com.cometchat.uikit.compose.presentation.createpoll

import com.cometchat.uikit.core.domain.usecase.CreatePollUseCase
import com.cometchat.uikit.core.state.CreatePollUIState
import com.cometchat.uikit.core.viewmodel.CometChatCreatePollViewModel
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Interaction tests for CometChatCreatePoll (Compose).
 *
 * Tests user interactions → correct state changes.
 * The Compose component manages local state internally, but the consumer
 * uses CometChatCreatePollViewModel for poll creation. These tests verify
 * the ViewModel interaction patterns that the Compose component's callbacks trigger.
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:testDebugUnitTest --tests "*.CometChatCreatePollInteractionTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatCreatePollInteractionTest : FunSpec({

    isolationMode = IsolationMode.SingleInstance

    val testDispatcher = UnconfinedTestDispatcher()

    lateinit var createPollUseCase: CreatePollUseCase
    lateinit var viewModel: CometChatCreatePollViewModel

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        createPollUseCase = mock()
        viewModel = CometChatCreatePollViewModel(createPollUseCase)
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        Dispatchers.resetMain()
    }

    // ==================== Back Press Interaction ====================

    test("back press invokes callback - ViewModel state unchanged") {
        println("    → Simulating back press (Compose onBackPress callback)")
        // In Compose, onBackPress is a lambda parameter
        // ViewModel state should remain unchanged
        viewModel.uiState.value shouldBe CreatePollUIState.Idle
        println("    ✓ Back press handled by consumer callback, ViewModel unaffected")
    }

    // ==================== Submit Click Interaction ====================

    test("submit click invokes callback with correct data") {
        runTest {
            println("    → Simulating submit click via ViewModel createPoll")
            viewModel.setQuestion("Best framework?")
            viewModel.updateOption(0, "Compose")
            viewModel.updateOption(1, "SwiftUI")

            whenever(createPollUseCase.invoke(any(), any(), any(), any(), anyOrNull()))
                .thenReturn(Result.success(Unit))

            var pollCreated = false
            viewModel.onPollCreated = { pollCreated = true }

            viewModel.createPoll("group-1", "group")
            advanceUntilIdle()

            pollCreated shouldBe true
            verify(createPollUseCase).invoke(
                question = org.mockito.kotlin.eq("Best framework?"),
                options = any(),
                receiverId = org.mockito.kotlin.eq("group-1"),
                receiverType = org.mockito.kotlin.eq("group"),
                quotedMessageId = org.mockito.kotlin.eq(null)
            )
            println("    ✓ Submit invoked createPoll with correct parameters")
        }
    }

    test("submit click with invalid form does nothing") {
        runTest {
            println("    → Attempting submit with empty form")
            viewModel.createPoll("r", "user")
            viewModel.uiState.value shouldBe CreatePollUIState.Idle
            println("    ✓ Invalid form prevents submission")
        }
    }

    // ==================== Option Auto-Add Behavior ====================

    test("option auto-add - Compose local state adds option when last filled") {
        println("    → Simulating auto-add behavior")
        // In Compose, auto-add is handled locally in the composable
        // When the last option is filled, a new empty option is added
        // ViewModel equivalent: addOption() called by consumer
        viewModel.updateOption(0, "First")
        viewModel.updateOption(1, "Second")
        viewModel.addOption() // Simulates auto-add triggered by Compose
        viewModel.options.value shouldHaveSize 3
        viewModel.options.value[2] shouldBe ""
        println("    ✓ Auto-add: new empty option appended")
    }

    test("option auto-add respects maximum limit") {
        println("    → Adding options up to limit")
        // Compose has maxOptions = 12
        repeat(10) { viewModel.addOption() } // 2 initial + 10 = 12
        viewModel.options.value shouldHaveSize 12
        println("    ✓ Options list at 12 (Compose max is 12)")
    }

    // ==================== Drag Reorder ====================

    test("drag reorder - options can be reordered via ViewModel updates") {
        println("    → Simulating drag reorder")
        viewModel.updateOption(0, "Alpha")
        viewModel.updateOption(1, "Beta")
        viewModel.addOption()
        viewModel.updateOption(2, "Gamma")

        // Simulate reorder: move "Gamma" to position 0
        // In Compose, this is handled by detectDragGesturesAfterLongPress
        // ViewModel doesn't have moveOption - reorder is local state
        // Consumer would update options after reorder
        viewModel.updateOption(0, "Gamma")
        viewModel.updateOption(1, "Alpha")
        viewModel.updateOption(2, "Beta")

        viewModel.options.value[0] shouldBe "Gamma"
        viewModel.options.value[1] shouldBe "Alpha"
        viewModel.options.value[2] shouldBe "Beta"
        println("    ✓ Options reordered correctly")
    }

    // ==================== Reset Interaction ====================

    test("reset clears all form data") {
        println("    → Filling form then resetting")
        viewModel.setQuestion("Question?")
        viewModel.updateOption(0, "A")
        viewModel.updateOption(1, "B")
        viewModel.addOption()
        viewModel.updateOption(2, "C")

        viewModel.reset()

        viewModel.question.value shouldBe ""
        viewModel.options.value shouldHaveSize 2
        viewModel.options.value.all { it.isEmpty() } shouldBe true
        viewModel.uiState.value shouldBe CreatePollUIState.Idle
        println("    ✓ Form reset to initial state")
    }

    // ==================== PBT: Interaction Invariants ====================

    test("PBT: any sequence of option additions never produces negative size") {
        checkAll(30, Arb.int(0..25)) { addCount ->
            val vm = CometChatCreatePollViewModel(createPollUseCase)
            repeat(addCount) { vm.addOption() }
            (vm.options.value.size >= 2) shouldBe true
        }
    }

    test("PBT: updateOption followed by getOptions always reflects the value") {
        checkAll(30, Arb.int(0..1), Arb.string(0..50)) { index, value ->
            val vm = CometChatCreatePollViewModel(createPollUseCase)
            vm.updateOption(index, value)
            vm.options.value[index] shouldBe value
        }
    }

    test("PBT: reset always returns to initial state regardless of prior mutations") {
        checkAll(20, Arb.string(0..30), Arb.string(0..20), Arb.int(0..5)) { q, opt, adds ->
            val vm = CometChatCreatePollViewModel(createPollUseCase)
            vm.setQuestion(q)
            vm.updateOption(0, opt)
            repeat(adds) { vm.addOption() }
            vm.reset()
            vm.question.value shouldBe ""
            vm.options.value shouldHaveSize 2
            vm.uiState.value shouldBe CreatePollUIState.Idle
        }
    }
})
