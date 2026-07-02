package com.cometchat.uikit.kotlin.presentation.calllogs

import com.cometchat.calls.core.CallLogRequest
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
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Tests for CometChatCallLogs Kotlin component interaction behavior.
 *
 * Verifies that ViewModel operations triggered by user interactions
 * (item click, long press, search input) produce correct state changes.
 *
 * The Kotlin XML component calls these ViewModel methods in response to
 * RecyclerView item clicks, long-press actions, and search input.
 *
 * Requirements: 12.2, 12.3
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatCallLogsInteractionTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        Dispatchers.resetMain()
        println()
    }

    test("getItemAt returns correct call log for RecyclerView position (item click)") {
        runTest {
            val callLogs = createMockCallLogs(5)
            val viewModel = createViewModel(callLogs)
            advanceUntilIdle()

            // Simulate RecyclerView adapter accessing items by position on click
            val item0 = viewModel.getItemAt(0)
            val item2 = viewModel.getItemAt(2)
            val item4 = viewModel.getItemAt(4)
            val outOfBounds = viewModel.getItemAt(10)

            item0 shouldBe callLogs[0]
            item2 shouldBe callLogs[2]
            item4 shouldBe callLogs[4]
            outOfBounds shouldBe null
            println("    ✅ getItemAt: correct items for positions 0, 2, 4; null for out-of-bounds")
        }
    }

    test("removeCall on long press → removes item from list and updates state") {
        runTest {
            val callLogs = createMockCallLogs(3)
            val viewModel = createViewModel(callLogs)
            advanceUntilIdle()

            viewModel.callLogs.value shouldHaveSize 3

            // Simulate long-press delete action
            viewModel.removeCall(callLogs[1])
            advanceUntilIdle()

            viewModel.callLogs.value shouldHaveSize 2
            viewModel.uiState.value.shouldBeInstanceOf<CallLogsUIState.Content>()
            println("    ✅ removeCall: removed from list, state remains Content")
        }
    }

    test("removeCall on last item → transitions to Empty state") {
        runTest {
            val callLogs = createMockCallLogs(1)
            val viewModel = createViewModel(callLogs)
            advanceUntilIdle()

            viewModel.callLogs.value shouldHaveSize 1

            // Simulate long-press delete on the only item
            viewModel.removeCall(callLogs[0])
            advanceUntilIdle()

            viewModel.callLogs.value shouldHaveSize 0
            viewModel.uiState.value shouldBe CallLogsUIState.Empty
            println("    ✅ removeCall on last item: transitions to Empty state")
        }
    }

    test("addToTop simulates new call log arriving → inserts at position 0") {
        runTest {
            val callLogs = createMockCallLogs(3)
            val viewModel = createViewModel(callLogs)
            advanceUntilIdle()

            val newCallLog = createMockCallLog("new-caller", "New Caller")
            viewModel.addToTop(newCallLog)
            advanceUntilIdle()

            viewModel.callLogs.value shouldHaveSize 4
            viewModel.getItemAt(0) shouldBe newCallLog
            viewModel.uiState.value.shouldBeInstanceOf<CallLogsUIState.Content>()
            println("    ✅ addToTop: new item at position 0, size increased")
        }
    }

    test("search input via setCallLogRequestBuilder + refreshCallLogs → refreshes list") {
        runTest {
            val initialCallLogs = createMockCallLogs(5)
            val searchResultCallLogs = createMockCallLogs(2, "search-result")

            val repository = object : CallLogsRepository {
                private var callCount = 0
                override suspend fun getCallLogs(request: CallLogRequest): Result<List<CallLog>> {
                    callCount++
                    return if (callCount == 1) {
                        Result.success(initialCallLogs)
                    } else {
                        Result.success(searchResultCallLogs)
                    }
                }
                override fun hasMoreCallLogs() = false
            }

            val fetchCallLogsUseCase = FetchCallLogsUseCase(repository)
            val initiateCallUseCase = mock<InitiateCallUseCase>()

            val viewModel = CometChatCallLogsViewModel(
                fetchCallLogsUseCase = fetchCallLogsUseCase,
                initiateCallUseCase = initiateCallUseCase,
                enableListeners = false
            )
            advanceUntilIdle()

            // Verify initial state
            viewModel.callLogs.value shouldHaveSize 5

            // Simulate search input: set new request builder and refresh
            val searchBuilder = mock<CallLogRequest.CallLogRequestBuilder>()
            val searchRequest = mock<CallLogRequest>()
            whenever(searchBuilder.setCallCategory(any())).thenReturn(searchBuilder)
            whenever(searchBuilder.build()).thenReturn(searchRequest)

            viewModel.setCallLogRequestBuilder(searchBuilder)
            viewModel.refreshCallLogs()
            advanceUntilIdle()

            // After search, list should contain search results
            viewModel.callLogs.value shouldHaveSize 2
            viewModel.uiState.value.shouldBeInstanceOf<CallLogsUIState.Content>()
            println("    ✅ search input: refreshCallLogs returns filtered results")
        }
    }

    test("moveToTop on long press → moves item to position 0") {
        runTest {
            val callLogs = createMockCallLogs(5)
            val viewModel = createViewModel(callLogs)
            advanceUntilIdle()

            // Simulate long-press action to move item to top
            val targetItem = callLogs[3]
            viewModel.moveToTop(targetItem)
            advanceUntilIdle()

            viewModel.callLogs.value shouldHaveSize 5
            viewModel.getItemAt(0) shouldBe targetItem
            println("    ✅ moveToTop: item moved to position 0, list size preserved")
        }
    }

    test("getItemCount returns correct count for adapter") {
        runTest {
            val callLogs = createMockCallLogs(7)
            val viewModel = createViewModel(callLogs)
            advanceUntilIdle()

            viewModel.getItemCount() shouldBe 7
            println("    ✅ getItemCount: returns 7 for adapter binding")
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

private fun createMockCallLogs(count: Int, prefix: String = "caller"): List<CallLog> {
    return (1..count).map { i ->
        createMockCallLog(
            initiatorUid = "$prefix-$i",
            initiatorName = "Caller $prefix $i"
        )
    }
}

private fun createViewModel(callLogs: List<CallLog>): CometChatCallLogsViewModel {
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
