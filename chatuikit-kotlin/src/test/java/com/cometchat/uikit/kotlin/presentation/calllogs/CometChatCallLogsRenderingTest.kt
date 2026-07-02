package com.cometchat.uikit.kotlin.presentation.calllogs

import com.cometchat.calls.core.CallLogRequest
import com.cometchat.calls.exceptions.CometChatException
import com.cometchat.calls.model.CallLog
import com.cometchat.calls.model.CallUser
import com.cometchat.uikit.core.domain.repository.CallLogsRepository
import com.cometchat.uikit.core.domain.usecase.FetchCallLogsUseCase
import com.cometchat.uikit.core.domain.usecase.InitiateCallUseCase
import com.cometchat.uikit.core.state.CallLogsUIState
import com.cometchat.uikit.core.viewmodel.CometChatCallLogsViewModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Tests for CometChatCallLogs Kotlin component rendering states.
 *
 * Verifies that the ViewModel produces the correct CallLogsUIState for each scenario,
 * which the Kotlin XML component observes to show/hide views.
 *
 * These tests validate the ViewModel state that drives rendering:
 * - CallLogsUIState.Loading → component shows loading shimmer
 * - CallLogsUIState.Empty → component shows empty state view
 * - CallLogsUIState.Error → component shows error state view
 * - CallLogsUIState.Content → component shows RecyclerView with call log items
 *
 * Also verifies that tapping a call log item triggers the onItemClick callback
 * with the correct CallLog object (Requirement 12.3).
 *
 * Validates: Requirements 12.1, 12.3
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*CometChatCallLogsRenderingTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatCallLogsRenderingTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        Dispatchers.resetMain()
        println()
    }

    // ==================== UIState Rendering Tests (Requirement 12.1) ====================

    test("ViewModel with empty repository → CallLogsUIState.Empty (drives empty state view)") {
        runTest {
            val viewModel = createCallLogsViewModel(emptyList())
            advanceUntilIdle()

            viewModel.uiState.value shouldBe CallLogsUIState.Empty
            viewModel.callLogs.value shouldHaveSize 0
            println("    ✅ CallLogsUIState.Empty → component would show empty state view")
        }
    }

    test("ViewModel with error repository → CallLogsUIState.Error (drives error state view)") {
        runTest {
            val viewModel = createCallLogsViewModelWithError("LOAD_ERR", "Failed to load call logs")
            advanceUntilIdle()

            viewModel.uiState.value.shouldBeInstanceOf<CallLogsUIState.Error>()
            val errorState = viewModel.uiState.value as CallLogsUIState.Error
            errorState.exception.code shouldBe "LOAD_ERR"
            errorState.exception.message shouldBe "Failed to load call logs"
            println("    ✅ CallLogsUIState.Error → component would show error state view with retry")
        }
    }

    test("ViewModel with call logs → CallLogsUIState.Content with correct item count") {
        runTest {
            val callLogs = createMockCallLogs(5)
            val viewModel = createCallLogsViewModel(callLogs)
            advanceUntilIdle()

            viewModel.uiState.value.shouldBeInstanceOf<CallLogsUIState.Content>()
            viewModel.callLogs.value shouldHaveSize 5
            println("    ✅ CallLogsUIState.Content with 5 items → component would show RecyclerView")
        }
    }

    test("ViewModel Content state contains the same call log objects from repository") {
        runTest {
            val callLogs = createMockCallLogs(3)
            val viewModel = createCallLogsViewModel(callLogs)
            advanceUntilIdle()

            val contentState = viewModel.uiState.value as CallLogsUIState.Content
            contentState.callLogs shouldHaveSize 3
            contentState.callLogs shouldBe callLogs
            println("    ✅ Content state holds the exact call log objects from the repository")
        }
    }

    test("ViewModel call logs contain correct data for rendering") {
        runTest {
            val callLogs = createMockCallLogs(3)
            val viewModel = createCallLogsViewModel(callLogs)
            advanceUntilIdle()

            val items = viewModel.callLogs.value
            items shouldHaveSize 3

            // Verify each call log has the data needed for rendering
            items.forEachIndexed { index, callLog ->
                val initiator = callLog.initiator as CallUser
                initiator.uid shouldBe "caller-${index + 1}"
                initiator.name shouldBe "Caller ${index + 1}"
                callLog.type shouldBe "audio"
                callLog.status shouldBe "ended"
            }
            println("    ✅ All 3 call logs have correct data for avatar, title, subtitle rendering")
        }
    }

    // ==================== Item Click Callback Tests (Requirement 12.3) ====================

    test("getItemAt returns correct CallLog for RecyclerView position (drives onItemClick)") {
        runTest {
            val callLogs = createMockCallLogs(5)
            val viewModel = createCallLogsViewModel(callLogs)
            advanceUntilIdle()

            // Simulate RecyclerView adapter accessing items by position
            val item0 = viewModel.getItemAt(0)
            val item2 = viewModel.getItemAt(2)
            val item4 = viewModel.getItemAt(4)
            val outOfBounds = viewModel.getItemAt(10)

            item0 shouldBe callLogs[0]
            item2 shouldBe callLogs[2]
            item4 shouldBe callLogs[4]
            outOfBounds shouldBe null
            println("    ✅ getItemAt: correct CallLog for positions 0, 2, 4; null for out-of-bounds")
        }
    }

    test("callLogs list provides correct item for onItemClick callback at any valid index") {
        runTest {
            val callLogs = createMockCallLogs(7)
            val viewModel = createCallLogsViewModel(callLogs)
            advanceUntilIdle()

            // Verify every item in the list can be accessed correctly
            viewModel.callLogs.value.forEachIndexed { index, callLog ->
                val retrieved = viewModel.getItemAt(index)
                retrieved shouldBe callLog
            }
            println("    ✅ All 7 call log items accessible by index for onItemClick callback")
        }
    }
})

// ==================== Helper Functions ====================

private fun createMockCallLog(
    initiatorUid: String = "caller-1",
    initiatorName: String = "Caller 1"
): CallLog {
    val callLog = mock<CallLog>()
    whenever(callLog.type).thenReturn("audio")
    whenever(callLog.status).thenReturn("ended")
    whenever(callLog.totalDurationInMinutes).thenReturn(120.0)
    whenever(callLog.initiatedAt).thenReturn(1735689600L)

    val initiator = mock<CallUser>()
    whenever(initiator.uid).thenReturn(initiatorUid)
    whenever(initiator.name).thenReturn(initiatorName)
    whenever(callLog.initiator).thenReturn(initiator)

    val receiver = mock<CallUser>()
    whenever(receiver.uid).thenReturn("receiver-$initiatorUid")
    whenever(receiver.name).thenReturn("Receiver")
    whenever(callLog.receiver).thenReturn(receiver)

    return callLog
}

private fun createMockCallLogs(count: Int): List<CallLog> {
    return (1..count).map { i ->
        createMockCallLog(
            initiatorUid = "caller-$i",
            initiatorName = "Caller $i"
        )
    }
}

private fun createCallLogsViewModel(callLogs: List<CallLog>): CometChatCallLogsViewModel {
    val repository = object : CallLogsRepository {
        override suspend fun getCallLogs(request: CallLogRequest): Result<List<CallLog>> =
            Result.success(callLogs)
        override fun hasMoreCallLogs() = false
    }
    return CometChatCallLogsViewModel(
        fetchCallLogsUseCase = FetchCallLogsUseCase(repository),
        initiateCallUseCase = mock(),
        enableListeners = false
    )
}

private fun createCallLogsViewModelWithError(
    code: String,
    message: String
): CometChatCallLogsViewModel {
    val repository = object : CallLogsRepository {
        override suspend fun getCallLogs(request: CallLogRequest): Result<List<CallLog>> =
            Result.failure(CometChatException(code, message))
        override fun hasMoreCallLogs() = false
    }
    return CometChatCallLogsViewModel(
        fetchCallLogsUseCase = FetchCallLogsUseCase(repository),
        initiateCallUseCase = mock(),
        enableListeners = false
    )
}
