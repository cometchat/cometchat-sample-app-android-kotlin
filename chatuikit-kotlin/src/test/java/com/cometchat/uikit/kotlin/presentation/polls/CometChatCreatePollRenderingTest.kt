package com.cometchat.uikit.kotlin.presentation.polls

import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.uikit.core.domain.usecase.CreatePollUseCase
import com.cometchat.uikit.core.state.CreatePollUIState
import com.cometchat.uikit.core.viewmodel.CometChatCreatePollViewModel
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
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
import org.mockito.kotlin.whenever

/**
 * Rendering tests for CometChatCreatePoll (Kotlin XML View).
 *
 * Tests ViewModel states → correct UIState for view rendering decisions.
 * These are JVM tests that verify the ViewModel produces the correct states
 * that the View would observe and render.
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*.CometChatCreatePollRenderingTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatCreatePollRenderingTest : FunSpec({

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

    // ==================== Initial State Rendering ====================

    test("initial state shows empty form - Idle state with empty question and 2 options") {
        println("    → Verifying initial rendering state")
        viewModel.uiState.value shouldBe CreatePollUIState.Idle
        viewModel.question.value shouldBe ""
        viewModel.options.value.size shouldBe 2
        viewModel.options.value.all { it.isEmpty() } shouldBe true
        println("    ✓ Initial state: Idle, empty question, 2 empty options")
    }

    // ==================== Question Input Rendering ====================

    test("question input updates correctly and reflects in StateFlow") {
        println("    → Setting question text")
        viewModel.setQuestion("What is your favorite programming language?")
        viewModel.question.value shouldBe "What is your favorite programming language?"
        println("    ✓ Question StateFlow reflects input")
    }

    test("question with special characters renders correctly") {
        println("    → Setting question with special chars")
        viewModel.setQuestion("What's your #1 choice? (Pick one!)")
        viewModel.question.value shouldBe "What's your #1 choice? (Pick one!)"
        println("    ✓ Special characters preserved in question")
    }

    // ==================== Options List Rendering ====================

    test("options list renders with minimum 2 items") {
        println("    → Verifying minimum options count")
        viewModel.options.value.size shouldBe 2
        println("    ✓ Minimum 2 options rendered")
    }

    test("options list grows when options are added") {
        println("    → Adding options and verifying list size")
        viewModel.addOption()
        viewModel.addOption()
        viewModel.options.value.size shouldBe 4
        println("    ✓ Options list grows to 4")
    }

    test("options display correct values after update") {
        println("    → Updating options and verifying values")
        viewModel.updateOption(0, "Kotlin")
        viewModel.updateOption(1, "Java")
        viewModel.options.value[0] shouldBe "Kotlin"
        viewModel.options.value[1] shouldBe "Java"
        println("    ✓ Options display correct values")
    }

    // ==================== Submit Button State Rendering ====================

    test("submit button disabled when form invalid - empty question") {
        println("    → Checking submit button state with empty question")
        viewModel.updateOption(0, "A")
        viewModel.updateOption(1, "B")
        viewModel.isFormValid() shouldBe false
        println("    ✓ Submit button should be disabled (empty question)")
    }

    test("submit button disabled when form invalid - insufficient options") {
        println("    → Checking submit button state with <2 options")
        viewModel.setQuestion("Question?")
        viewModel.updateOption(0, "Only one")
        viewModel.isFormValid() shouldBe false
        println("    ✓ Submit button should be disabled (<2 non-empty options)")
    }

    test("submit button enabled when form valid") {
        println("    → Checking submit button state with valid form")
        viewModel.setQuestion("Best color?")
        viewModel.updateOption(0, "Red")
        viewModel.updateOption(1, "Blue")
        viewModel.isFormValid() shouldBe true
        println("    ✓ Submit button should be enabled (valid form)")
    }



    // ==================== Error Message Rendering ====================

    test("error message displayed on error state") {
        runTest {
            println("    → Triggering error to verify Error state")
            viewModel.setQuestion("Q?")
            viewModel.updateOption(0, "A")
            viewModel.updateOption(1, "B")

            val exception = CometChatException("ERR_NET", "Network unavailable")
            whenever(createPollUseCase.invoke(any(), any(), any(), any(), anyOrNull()))
                .thenReturn(Result.failure(exception))

            viewModel.createPoll("receiver-1", "user")
            advanceUntilIdle()

            val errorState = viewModel.uiState.value
            errorState.shouldBeInstanceOf<CreatePollUIState.Error>()
            (errorState as CreatePollUIState.Error).exception.message shouldBe "Network unavailable"
            println("    ✓ Error state contains message for display")
        }
    }

    test("error message cleared after dismissError") {
        runTest {
            println("    → Dismissing error and verifying state")
            viewModel.setQuestion("Q?")
            viewModel.updateOption(0, "A")
            viewModel.updateOption(1, "B")

            whenever(createPollUseCase.invoke(any(), any(), any(), any(), anyOrNull()))
                .thenReturn(Result.failure(CometChatException("ERR", "fail")))

            viewModel.createPoll("r", "user")
            advanceUntilIdle()
            viewModel.dismissError()

            viewModel.uiState.value shouldBe CreatePollUIState.Idle
            println("    ✓ Error cleared, back to Idle → error message hidden")
        }
    }
})
