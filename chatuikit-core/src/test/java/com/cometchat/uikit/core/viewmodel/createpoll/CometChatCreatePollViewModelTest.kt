package com.cometchat.uikit.core.viewmodel.createpoll

import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.uikit.core.domain.usecase.CreatePollUseCase
import com.cometchat.uikit.core.state.CreatePollUIState
import com.cometchat.uikit.core.viewmodel.CometChatCreatePollViewModel
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.json.JSONArray
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Comprehensive property-based tests for CometChatCreatePollViewModel.
 *
 * Tests cover:
 * - Initial state verification
 * - Form data management (question, options)
 * - Form validation logic
 * - Poll creation flow (success/failure)
 * - State transitions (Idle → Submitting → Success/Error)
 * - Reset and error dismissal
 * - PBT: form validity invariants
 * - PBT: addOption size invariant
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*.CometChatCreatePollViewModelTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatCreatePollViewModelTest : FunSpec({

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

    // ==================== A. Initial State ====================

    test("initial uiState should be Idle") {
        println("    → Checking initial uiState")
        viewModel.uiState.value shouldBe CreatePollUIState.Idle
        println("    ✓ uiState is Idle")
    }

    test("initial question should be empty string") {
        println("    → Checking initial question")
        viewModel.question.value shouldBe ""
        println("    ✓ question is empty")
    }

    test("initial options should be list of 2 empty strings") {
        println("    → Checking initial options")
        viewModel.options.value shouldHaveSize 2
        viewModel.options.value[0] shouldBe ""
        viewModel.options.value[1] shouldBe ""
        println("    ✓ options are ['', '']")
    }

    // ==================== B. Form Data Management ====================

    test("setQuestion updates question StateFlow") {
        println("    → Setting question to 'What is your favorite color?'")
        viewModel.setQuestion("What is your favorite color?")
        viewModel.question.value shouldBe "What is your favorite color?"
        println("    ✓ question updated correctly")
    }

    test("updateOption updates specific option at index") {
        println("    → Updating option at index 0 to 'Red'")
        viewModel.updateOption(0, "Red")
        viewModel.options.value[0] shouldBe "Red"
        viewModel.options.value[1] shouldBe ""
        println("    ✓ option at index 0 updated, index 1 unchanged")
    }

    test("updateOption with invalid index does nothing") {
        println("    → Updating option at invalid index 5")
        viewModel.updateOption(5, "Invalid")
        viewModel.options.value shouldHaveSize 2
        viewModel.options.value[0] shouldBe ""
        viewModel.options.value[1] shouldBe ""
        println("    ✓ options unchanged for invalid index")
    }

    test("addOption adds new empty option") {
        println("    → Adding option")
        viewModel.addOption()
        viewModel.options.value shouldHaveSize 3
        viewModel.options.value[2] shouldBe ""
        println("    ✓ new empty option added at end")
    }

    test("removeOption removes option at index when more than 2 options") {
        println("    → Adding option then removing at index 1")
        viewModel.addOption()
        viewModel.updateOption(0, "A")
        viewModel.updateOption(1, "B")
        viewModel.updateOption(2, "C")
        viewModel.removeOption(1)
        viewModel.options.value shouldHaveSize 2
        viewModel.options.value[0] shouldBe "A"
        viewModel.options.value[1] shouldBe "C"
        println("    ✓ option removed, list compacted")
    }

    test("removeOption does nothing when only 2 options remain") {
        println("    → Attempting to remove option when only 2 exist")
        viewModel.updateOption(0, "A")
        viewModel.updateOption(1, "B")
        viewModel.removeOption(0)
        viewModel.options.value shouldHaveSize 2
        viewModel.options.value[0] shouldBe "A"
        viewModel.options.value[1] shouldBe "B"
        println("    ✓ minimum 2 options enforced")
    }

    test("removeOption with invalid index does nothing") {
        println("    → Attempting to remove at invalid index 10")
        viewModel.addOption()
        viewModel.removeOption(10)
        viewModel.options.value shouldHaveSize 3
        println("    ✓ options unchanged for invalid index")
    }

    // ==================== C. Form Validation ====================

    test("isFormValid returns false when question is empty") {
        println("    → Checking validity with empty question")
        viewModel.updateOption(0, "Option A")
        viewModel.updateOption(1, "Option B")
        viewModel.isFormValid() shouldBe false
        println("    ✓ form invalid with empty question")
    }

    test("isFormValid returns false when question is blank") {
        println("    → Checking validity with blank question")
        viewModel.setQuestion("   ")
        viewModel.updateOption(0, "Option A")
        viewModel.updateOption(1, "Option B")
        viewModel.isFormValid() shouldBe false
        println("    ✓ form invalid with blank question")
    }

    test("isFormValid returns false when fewer than 2 non-empty options") {
        println("    → Checking validity with only 1 non-empty option")
        viewModel.setQuestion("Question?")
        viewModel.updateOption(0, "Option A")
        viewModel.isFormValid() shouldBe false
        println("    ✓ form invalid with <2 non-empty options")
    }

    test("isFormValid returns false when all options are empty") {
        println("    → Checking validity with all empty options")
        viewModel.setQuestion("Question?")
        viewModel.isFormValid() shouldBe false
        println("    ✓ form invalid with all empty options")
    }

    test("isFormValid returns true when question and 2+ non-empty options") {
        println("    → Checking validity with valid form data")
        viewModel.setQuestion("What color?")
        viewModel.updateOption(0, "Red")
        viewModel.updateOption(1, "Blue")
        viewModel.isFormValid() shouldBe true
        println("    ✓ form valid with question + 2 non-empty options")
    }

    // ==================== D. Create Poll - Guards ====================

    test("createPoll does nothing when form is invalid") {
        runTest {
            println("    → Calling createPoll with invalid form")
            viewModel.createPoll("receiver-1", "user")
            viewModel.uiState.value shouldBe CreatePollUIState.Idle
            verify(createPollUseCase, never()).invoke(any(), any(), any(), any(), anyOrNull())
            println("    ✓ createPoll skipped for invalid form")
        }
    }

    test("createPoll does nothing when already submitting") {
        // Use StandardTestDispatcher so coroutines don't execute eagerly
        val standardDispatcher = StandardTestDispatcher()
        Dispatchers.resetMain()
        Dispatchers.setMain(standardDispatcher)

        runTest(standardDispatcher) {
            println("    → Setting up valid form and simulating submitting state")
            viewModel.setQuestion("Question?")
            viewModel.updateOption(0, "A")
            viewModel.updateOption(1, "B")

            whenever(createPollUseCase.invoke(any(), any(), any(), any(), anyOrNull()))
                .thenReturn(Result.success(Unit))

            // First call sets state to Submitting synchronously, then launches coroutine
            viewModel.createPoll("receiver-1", "user")
            // Don't advance - coroutine is pending, state should be Submitting
            viewModel.uiState.value shouldBe CreatePollUIState.Submitting

            // Second call should be ignored because state is Submitting
            viewModel.createPoll("receiver-1", "user")

            // Now advance to let the first coroutine complete
            advanceUntilIdle()

            // Use case should have been invoked only once
            verify(createPollUseCase, times(1))
                .invoke(any(), any(), any(), any(), anyOrNull())
            println("    ✓ second createPoll ignored while submitting")
        }

        // Restore UnconfinedTestDispatcher for remaining tests
        Dispatchers.resetMain()
        Dispatchers.setMain(testDispatcher)
    }

    // ==================== E. Create Poll - Success ====================

    test("createPoll success transitions Idle → Submitting → Success") {
        runTest {
            println("    → Setting up valid form and mocking success")
            viewModel.setQuestion("Favorite color?")
            viewModel.updateOption(0, "Red")
            viewModel.updateOption(1, "Blue")

            whenever(createPollUseCase.invoke(any(), any(), any(), any(), anyOrNull()))
                .thenReturn(Result.success(Unit))

            var pollCreatedInvoked = false
            viewModel.onPollCreated = { pollCreatedInvoked = true }

            viewModel.createPoll("group-1", "group")
            advanceUntilIdle()

            viewModel.uiState.value shouldBe CreatePollUIState.Success
            pollCreatedInvoked shouldBe true
            println("    ✓ state transitioned to Success, onPollCreated invoked")
        }
    }

    test("createPoll passes correct parameters to use case") {
        runTest {
            println("    → Verifying parameters passed to use case")
            viewModel.setQuestion("Best language?")
            viewModel.updateOption(0, "Kotlin")
            viewModel.updateOption(1, "Swift")

            whenever(createPollUseCase.invoke(any(), any(), any(), any(), anyOrNull()))
                .thenReturn(Result.success(Unit))

            viewModel.createPoll("user-123", "user", 42L)
            advanceUntilIdle()

            verify(createPollUseCase).invoke(
                question = org.mockito.kotlin.eq("Best language?"),
                options = any(),
                receiverId = org.mockito.kotlin.eq("user-123"),
                receiverType = org.mockito.kotlin.eq("user"),
                quotedMessageId = org.mockito.kotlin.eq(42L)
            )
            println("    ✓ correct parameters forwarded to use case")
        }
    }

    // ==================== F. Create Poll - Failure ====================

    test("createPoll failure transitions Idle → Submitting → Error") {
        runTest {
            println("    → Setting up valid form and mocking failure")
            viewModel.setQuestion("Favorite color?")
            viewModel.updateOption(0, "Red")
            viewModel.updateOption(1, "Blue")

            val exception = CometChatException("ERR_POLL", "Network error")
            whenever(createPollUseCase.invoke(any(), any(), any(), any(), anyOrNull()))
                .thenReturn(Result.failure(exception))

            var errorReceived: CometChatException? = null
            viewModel.onError = { errorReceived = it }

            viewModel.createPoll("group-1", "group")
            advanceUntilIdle()

            viewModel.uiState.value.shouldBeInstanceOf<CreatePollUIState.Error>()
            val errorState = viewModel.uiState.value as CreatePollUIState.Error
            errorState.exception.code shouldBe "ERR_POLL"
            errorReceived shouldBe exception
            println("    ✓ state transitioned to Error, onError invoked")
        }
    }

    test("createPoll wraps non-CometChatException in CometChatException") {
        runTest {
            println("    → Mocking failure with generic exception")
            viewModel.setQuestion("Question?")
            viewModel.updateOption(0, "A")
            viewModel.updateOption(1, "B")

            whenever(createPollUseCase.invoke(any(), any(), any(), any(), anyOrNull()))
                .thenReturn(Result.failure(RuntimeException("Unexpected error")))

            viewModel.createPoll("group-1", "group")
            advanceUntilIdle()

            val errorState = viewModel.uiState.value as CreatePollUIState.Error
            errorState.exception.code shouldBe "ERR_POLL_CREATION"
            println("    ✓ generic exception wrapped in CometChatException")
        }
    }

    // ==================== G. Reset ====================

    test("reset clears question, resets options to 2 empty, state to Idle") {
        runTest {
            println("    → Setting up form data then resetting")
            viewModel.setQuestion("Question?")
            viewModel.updateOption(0, "A")
            viewModel.updateOption(1, "B")
            viewModel.addOption()
            viewModel.updateOption(2, "C")

            viewModel.reset()

            viewModel.question.value shouldBe ""
            viewModel.options.value shouldHaveSize 2
            viewModel.options.value[0] shouldBe ""
            viewModel.options.value[1] shouldBe ""
            viewModel.uiState.value shouldBe CreatePollUIState.Idle
            println("    ✓ form reset to initial state")
        }
    }

    // ==================== H. Dismiss Error ====================

    test("dismissError transitions Error → Idle") {
        runTest {
            println("    → Setting up Error state then dismissing")
            viewModel.setQuestion("Q?")
            viewModel.updateOption(0, "A")
            viewModel.updateOption(1, "B")

            whenever(createPollUseCase.invoke(any(), any(), any(), any(), anyOrNull()))
                .thenReturn(Result.failure(CometChatException("ERR", "fail")))

            viewModel.createPoll("r", "user")
            advanceUntilIdle()
            viewModel.uiState.value.shouldBeInstanceOf<CreatePollUIState.Error>()

            viewModel.dismissError()
            viewModel.uiState.value shouldBe CreatePollUIState.Idle
            println("    ✓ Error dismissed, state back to Idle")
        }
    }

    test("dismissError does nothing when state is not Error") {
        println("    → Calling dismissError when state is Idle")
        viewModel.uiState.value shouldBe CreatePollUIState.Idle
        viewModel.dismissError()
        viewModel.uiState.value shouldBe CreatePollUIState.Idle
        println("    ✓ dismissError is no-op for non-Error state")
    }

    // ==================== I. PBT: Form Validity Invariant ====================

    test("PBT: for any valid question + 2+ non-blank options, isFormValid returns true") {
        val questionArb: Arb<String> = Arb.string(1..50).filter { it.isNotBlank() }
        val optionArb: Arb<String> = Arb.string(1..30).filter { it.isNotBlank() }
        checkAll(50, questionArb, optionArb, optionArb) { q, opt1, opt2 ->
            runTest {
                val vm = CometChatCreatePollViewModel(createPollUseCase)
                vm.setQuestion(q)
                vm.updateOption(0, opt1)
                vm.updateOption(1, opt2)
                vm.isFormValid() shouldBe true
            }
        }
    }

    test("PBT: for any empty/blank question, isFormValid returns false regardless of options") {
        checkAll(50, Arb.string(1..30), Arb.string(1..30)) { opt1, opt2 ->
            runTest {
                val vm = CometChatCreatePollViewModel(createPollUseCase)
                vm.setQuestion("")
                vm.updateOption(0, opt1)
                vm.updateOption(1, opt2)
                vm.isFormValid() shouldBe false
            }
        }
    }

    // ==================== J. PBT: addOption Size Invariant ====================

    test("PBT: for any count of addOption calls 0..20, options size increases by that count") {
        checkAll(50, Arb.int(0..20)) { addCount ->
            runTest {
                val vm = CometChatCreatePollViewModel(createPollUseCase)
                val initialSize = vm.options.value.size
                repeat(addCount) { vm.addOption() }
                vm.options.value shouldHaveSize (initialSize + addCount)
            }
        }
    }

    test("PBT: removeOption never reduces below 2 options") {
        checkAll(30, Arb.int(0..10), Arb.int(0..15)) { addCount, removeCount ->
            runTest {
                val vm = CometChatCreatePollViewModel(createPollUseCase)
                repeat(addCount) { vm.addOption() }
                repeat(removeCount) { vm.removeOption(0) }
                (vm.options.value.size >= 2) shouldBe true
            }
        }
    }
})
