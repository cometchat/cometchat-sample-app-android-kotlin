package com.cometchat.uikit.core.viewmodel.createpoll

import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.uikit.core.data.datasource.PollDataSource
import com.cometchat.uikit.core.data.repository.PollRepositoryImpl
import com.cometchat.uikit.core.domain.usecase.CreatePollUseCase
import com.cometchat.uikit.core.state.CreatePollUIState
import com.cometchat.uikit.core.viewmodel.CometChatCreatePollViewModel
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
import org.json.JSONArray

/**
 * Full chain integration test for CreatePoll.
 *
 * Tests the complete chain: fake DataSource → real Repository → real UseCase → real ViewModel.
 * Only the DataSource is faked — everything else is real.
 *
 * Verifies end-to-end behavior:
 * - Success path: DataSource returns success → ViewModel transitions to Success
 * - Failure path: DataSource returns failure → ViewModel transitions to Error
 * - Form validation: ViewModel correctly validates before calling chain
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*.CreatePollFullChainIntegrationTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CreatePollFullChainIntegrationTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        Dispatchers.resetMain()
    }

    /**
     * Builds a real ViewModel with the full chain using a fake DataSource.
     */
    fun buildViewModel(dataSource: PollDataSource): CometChatCreatePollViewModel {
        val repository = PollRepositoryImpl(dataSource)
        val useCase = CreatePollUseCase(repository)
        return CometChatCreatePollViewModel(useCase)
    }

    // ==================== Success Path ====================

    test("full chain: DataSource returning success → ViewModel shows Success state") {
        runTest {
            println("    → Building full chain with success DataSource")
            val dataSource = object : PollDataSource {
                override suspend fun createPoll(
                    question: String,
                    options: JSONArray,
                    receiverId: String,
                    receiverType: String,
                    quotedMessageId: Long?
                ): Result<Unit> = Result.success(Unit)
            }

            val viewModel = buildViewModel(dataSource)
            viewModel.setQuestion("Best language?")
            viewModel.updateOption(0, "Kotlin")
            viewModel.updateOption(1, "Swift")

            var pollCreated = false
            viewModel.onPollCreated = { pollCreated = true }

            viewModel.createPoll("group-1", "group")
            advanceUntilIdle()

            viewModel.uiState.value shouldBe CreatePollUIState.Success
            pollCreated shouldBe true
            println("    ✓ Full chain: Success state reached, onPollCreated invoked")
        }
    }

    // ==================== Failure Path ====================

    test("full chain: DataSource returning failure → ViewModel shows Error state") {
        runTest {
            println("    → Building full chain with failure DataSource")
            val dataSource = object : PollDataSource {
                override suspend fun createPoll(
                    question: String,
                    options: JSONArray,
                    receiverId: String,
                    receiverType: String,
                    quotedMessageId: Long?
                ): Result<Unit> = Result.failure(
                    CometChatException("ERR_NETWORK", "Connection timeout")
                )
            }

            val viewModel = buildViewModel(dataSource)
            viewModel.setQuestion("Best IDE?")
            viewModel.updateOption(0, "IntelliJ")
            viewModel.updateOption(1, "VS Code")

            var errorReceived: CometChatException? = null
            viewModel.onError = { errorReceived = it }

            viewModel.createPoll("user-1", "user")
            advanceUntilIdle()

            viewModel.uiState.value.shouldBeInstanceOf<CreatePollUIState.Error>()
            val errorState = viewModel.uiState.value as CreatePollUIState.Error
            errorState.exception.code shouldBe "ERR_NETWORK"
            errorReceived?.code shouldBe "ERR_NETWORK"
            println("    ✓ Full chain: Error state reached, onError invoked")
        }
    }

    // ==================== Form Validation Guard ====================

    test("full chain: invalid form does not trigger DataSource call") {
        runTest {
            println("    → Building full chain with tracking DataSource")
            var dataSourceCalled = false
            val dataSource = object : PollDataSource {
                override suspend fun createPoll(
                    question: String,
                    options: JSONArray,
                    receiverId: String,
                    receiverType: String,
                    quotedMessageId: Long?
                ): Result<Unit> {
                    dataSourceCalled = true
                    return Result.success(Unit)
                }
            }

            val viewModel = buildViewModel(dataSource)
            // Don't set question or options — form is invalid

            viewModel.createPoll("group-1", "group")
            advanceUntilIdle()

            viewModel.uiState.value shouldBe CreatePollUIState.Idle
            dataSourceCalled shouldBe false
            println("    ✓ Full chain: invalid form prevented DataSource call")
        }
    }

    // ==================== Parameter Forwarding ====================

    test("full chain: parameters forwarded correctly through all layers") {
        runTest {
            println("    → Verifying parameter forwarding through chain")
            var receivedQuestion: String? = null
            var receivedReceiverId: String? = null
            var receivedReceiverType: String? = null
            var receivedQuotedMessageId: Long? = null

            val dataSource = object : PollDataSource {
                override suspend fun createPoll(
                    question: String,
                    options: JSONArray,
                    receiverId: String,
                    receiverType: String,
                    quotedMessageId: Long?
                ): Result<Unit> {
                    receivedQuestion = question
                    receivedReceiverId = receiverId
                    receivedReceiverType = receiverType
                    receivedQuotedMessageId = quotedMessageId
                    return Result.success(Unit)
                }
            }

            val viewModel = buildViewModel(dataSource)
            viewModel.setQuestion("Favorite OS?")
            viewModel.updateOption(0, "Linux")
            viewModel.updateOption(1, "macOS")

            viewModel.createPoll("user-42", "user", 99L)
            advanceUntilIdle()

            receivedQuestion shouldBe "Favorite OS?"
            receivedReceiverId shouldBe "user-42"
            receivedReceiverType shouldBe "user"
            receivedQuotedMessageId shouldBe 99L
            println("    ✓ All parameters forwarded correctly: question, receiverId, receiverType, quotedMessageId")
        }
    }

    // ==================== Reset After Success ====================

    test("full chain: reset after success returns to initial state") {
        runTest {
            println("    → Testing reset after successful poll creation")
            val dataSource = object : PollDataSource {
                override suspend fun createPoll(
                    question: String,
                    options: JSONArray,
                    receiverId: String,
                    receiverType: String,
                    quotedMessageId: Long?
                ): Result<Unit> = Result.success(Unit)
            }

            val viewModel = buildViewModel(dataSource)
            viewModel.setQuestion("Q?")
            viewModel.updateOption(0, "A")
            viewModel.updateOption(1, "B")

            viewModel.createPoll("r", "user")
            advanceUntilIdle()
            viewModel.uiState.value shouldBe CreatePollUIState.Success

            viewModel.reset()

            viewModel.uiState.value shouldBe CreatePollUIState.Idle
            viewModel.question.value shouldBe ""
            viewModel.options.value.size shouldBe 2
            viewModel.options.value.all { it.isEmpty() } shouldBe true
            println("    ✓ Reset after success: form cleared, state back to Idle")
        }
    }

    // ==================== Dismiss Error ====================

    test("full chain: dismissError after failure returns to Idle") {
        runTest {
            println("    → Testing dismissError after failure")
            val dataSource = object : PollDataSource {
                override suspend fun createPoll(
                    question: String,
                    options: JSONArray,
                    receiverId: String,
                    receiverType: String,
                    quotedMessageId: Long?
                ): Result<Unit> = Result.failure(
                    CometChatException("ERR", "fail")
                )
            }

            val viewModel = buildViewModel(dataSource)
            viewModel.setQuestion("Q?")
            viewModel.updateOption(0, "A")
            viewModel.updateOption(1, "B")

            viewModel.createPoll("r", "user")
            advanceUntilIdle()
            viewModel.uiState.value.shouldBeInstanceOf<CreatePollUIState.Error>()

            viewModel.dismissError()
            viewModel.uiState.value shouldBe CreatePollUIState.Idle
            println("    ✓ dismissError: Error → Idle")
        }
    }
})
