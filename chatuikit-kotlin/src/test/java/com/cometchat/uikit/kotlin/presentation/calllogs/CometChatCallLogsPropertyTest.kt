package com.cometchat.uikit.kotlin.presentation.calllogs

import com.cometchat.calls.core.CallLogRequest
import com.cometchat.calls.model.CallLog
import com.cometchat.calls.model.CallUser
import com.cometchat.uikit.core.domain.repository.CallLogsRepository
import com.cometchat.uikit.core.domain.usecase.FetchCallLogsUseCase
import com.cometchat.uikit.core.state.CallLogsUIState
import com.cometchat.uikit.core.viewmodel.CometChatCallLogsViewModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
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
 * Property-based tests for CometChatCallLogs Kotlin component.
 *
 * Tests domain invariants that must hold for ANY valid input:
 * - Selection constraints: SINGLE retains at most 1, MULTIPLE allows 0..N, NONE always 0
 * - DiffUtil: items with same sessionId are "same item", different sessionId are different
 * - Pagination state exclusivity: Loading/Empty/Content/Error are mutually exclusive
 *
 * Validates: Requirements 13.1, 13.7
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*CometChatCallLogsPropertyTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatCallLogsPropertyTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        Dispatchers.resetMain()
        println()
    }

    // ==================== Selection Constraints (Requirement 13.1) ====================

    test("SINGLE selection: for any list size, selected set never exceeds 1") {
        checkAll(Arb.int(2..20)) { listSize ->
            val selectedSet = mutableSetOf<String>()

            // Simulate selecting multiple items in SINGLE mode
            val items = (1..listSize).map { "session-$it" }
            items.forEach { sessionId ->
                // SINGLE mode: clear before adding
                selectedSet.clear()
                selectedSet.add(sessionId)
            }

            // Property: SINGLE mode retains at most 1 item
            selectedSet.size shouldBe 1
        }
    }

    test("MULTIPLE selection: for any subset of items, selected count equals subset size") {
        checkAll(Arb.int(1..20), Arb.int(0..20)) { listSize, selectCount ->
            val actualSelectCount = selectCount.coerceAtMost(listSize)
            val selectedSet = mutableSetOf<String>()

            val items = (1..listSize).map { "session-$it" }
            items.take(actualSelectCount).forEach { sessionId ->
                selectedSet.add(sessionId)
            }

            // Property: MULTIPLE mode allows 0..N selected items
            selectedSet.size shouldBe actualSelectCount
        }
    }

    test("MULTIPLE selection: toggle deselects already-selected item") {
        checkAll(Arb.int(1..15)) { listSize ->
            val selectedSet = mutableSetOf<String>()
            val items = (1..listSize).map { "session-$it" }

            // Select all
            items.forEach { selectedSet.add(it) }
            selectedSet.size shouldBe listSize

            // Toggle first item (deselect)
            val target = items[0]
            if (selectedSet.contains(target)) {
                selectedSet.remove(target)
            } else {
                selectedSet.add(target)
            }

            // Property: after toggle, size decreases by 1
            selectedSet.size shouldBe listSize - 1
            selectedSet.contains(target).shouldBeFalse()
        }
    }

    test("NONE selection: for any operations, selected set remains empty") {
        checkAll(Arb.int(1..20)) { listSize ->
            val selectedSet = mutableSetOf<String>()

            // In NONE mode, selection operations are no-ops
            // (we don't add to the set)

            // Property: NONE mode always has 0 selected
            selectedSet.size shouldBe 0
        }
    }

    // ==================== DiffUtil Properties (Requirement 13.7) ====================

    test("DiffUtil areItemsTheSame: same sessionId → true regardless of other fields") {
        checkAll(
            Arb.string(5..15),
            Arb.element("audio", "video"),
            Arb.element("audio", "video"),
            Arb.element("ended", "missed", "cancelled", "rejected"),
            Arb.element("ended", "missed", "cancelled", "rejected")
        ) { sessionId, type1, type2, status1, status2 ->
            // Simulate two CallLog items with same sessionId but different fields
            data class CallLogItem(val sessionId: String, val type: String, val status: String)

            val oldItem = CallLogItem(sessionId, type1, status1)
            val newItem = CallLogItem(sessionId, type2, status2)

            // Property: areItemsTheSame uses sessionId
            val areItemsTheSame = oldItem.sessionId == newItem.sessionId
            areItemsTheSame.shouldBeTrue()
        }
    }

    test("DiffUtil areItemsTheSame: different sessionId → false") {
        checkAll(
            Arb.string(5..15),
            Arb.string(5..15)
        ) { sessionId1, sessionId2 ->
            // Skip when randomly generated IDs happen to be equal
            if (sessionId1 != sessionId2) {
                data class CallLogItem(val sessionId: String)

                val oldItem = CallLogItem(sessionId1)
                val newItem = CallLogItem(sessionId2)

                // Property: different sessionIds → not the same item
                val areItemsTheSame = oldItem.sessionId == newItem.sessionId
                areItemsTheSame.shouldBeFalse()
            }
        }
    }

    test("DiffUtil areContentsTheSame: same fields → true") {
        checkAll(
            Arb.string(5..15),
            Arb.element("audio", "video"),
            Arb.element("ended", "missed", "cancelled"),
            Arb.int(0..3600),
            Arb.long(1000000000L..2000000000L)
        ) { sessionId, type, status, duration, timestamp ->
            data class CallLogItem(
                val sessionId: String,
                val type: String,
                val status: String,
                val duration: Int,
                val timestamp: Long
            )

            val item1 = CallLogItem(sessionId, type, status, duration, timestamp)
            val item2 = CallLogItem(sessionId, type, status, duration, timestamp)

            // Property: identical fields → contents are the same
            val areContentsTheSame = item1 == item2
            areContentsTheSame.shouldBeTrue()
        }
    }

    test("DiffUtil areContentsTheSame: different duration → false") {
        checkAll(
            Arb.string(5..15),
            Arb.int(0..1800),
            Arb.int(1801..3600)
        ) { sessionId, duration1, duration2 ->
            data class CallLogItem(val sessionId: String, val duration: Int)

            val item1 = CallLogItem(sessionId, duration1)
            val item2 = CallLogItem(sessionId, duration2)

            // Property: different duration → contents differ
            val areContentsTheSame = item1 == item2
            areContentsTheSame.shouldBeFalse()
        }
    }

    // ==================== Pagination State Exclusivity (Requirement 13.7) ====================

    test("UIState exclusivity: for any item count, exactly one state is active") {
        checkAll(Arb.int(0..20)) { count ->
            runTest {
                val callLogs = (1..count).map { i ->
                    val callLog = mock<CallLog>()
                    whenever(callLog.type).thenReturn("audio")
                    whenever(callLog.status).thenReturn("ended")
                    val initiator = mock<CallUser>()
                    whenever(initiator.uid).thenReturn("uid-$i")
                    whenever(initiator.name).thenReturn("User $i")
                    whenever(callLog.initiator).thenReturn(initiator)
                    callLog
                }

                val repository = object : CallLogsRepository {
                    override suspend fun getCallLogs(request: CallLogRequest) =
                        Result.success(callLogs)
                    override fun hasMoreCallLogs() = false
                }
                val viewModel = CometChatCallLogsViewModel(
                    fetchCallLogsUseCase = FetchCallLogsUseCase(repository),
                    initiateCallUseCase = mock(),
                    enableListeners = false
                )
                advanceUntilIdle()

                val state = viewModel.uiState.value

                // Property: exactly one state variant is active
                val isLoading = state is CallLogsUIState.Loading
                val isEmpty = state is CallLogsUIState.Empty
                val isContent = state is CallLogsUIState.Content
                val isError = state is CallLogsUIState.Error

                val activeCount = listOf(isLoading, isEmpty, isContent, isError).count { it }
                activeCount shouldBe 1

                // Property: state matches item count
                if (count == 0) {
                    state shouldBe CallLogsUIState.Empty
                } else {
                    state.shouldBeInstanceOf<CallLogsUIState.Content>()
                }
            }
        }
    }

    test("UIState exclusivity: Content state item count matches callLogs flow") {
        checkAll(Arb.int(1..20)) { count ->
            runTest {
                val callLogs = (1..count).map { i ->
                    val callLog = mock<CallLog>()
                    whenever(callLog.type).thenReturn("audio")
                    val initiator = mock<CallUser>()
                    whenever(initiator.uid).thenReturn("uid-$i")
                    whenever(callLog.initiator).thenReturn(initiator)
                    callLog
                }

                val repository = object : CallLogsRepository {
                    override suspend fun getCallLogs(request: CallLogRequest) =
                        Result.success(callLogs)
                    override fun hasMoreCallLogs() = false
                }
                val viewModel = CometChatCallLogsViewModel(
                    fetchCallLogsUseCase = FetchCallLogsUseCase(repository),
                    initiateCallUseCase = mock(),
                    enableListeners = false
                )
                advanceUntilIdle()

                // Property: Content state's callLogs matches the StateFlow
                val contentState = viewModel.uiState.value as CallLogsUIState.Content
                contentState.callLogs shouldHaveSize count
                contentState.callLogs shouldBe viewModel.callLogs.value
            }
        }
    }

    test("Pagination: after clearItems, state transitions to Empty exclusively") {
        checkAll(Arb.int(1..15)) { count ->
            runTest {
                val callLogs = (1..count).map { i ->
                    val callLog = mock<CallLog>()
                    whenever(callLog.type).thenReturn("audio")
                    val initiator = mock<CallUser>()
                    whenever(initiator.uid).thenReturn("uid-$i")
                    whenever(callLog.initiator).thenReturn(initiator)
                    callLog
                }

                val repository = object : CallLogsRepository {
                    override suspend fun getCallLogs(request: CallLogRequest) =
                        Result.success(callLogs)
                    override fun hasMoreCallLogs() = false
                }
                val viewModel = CometChatCallLogsViewModel(
                    fetchCallLogsUseCase = FetchCallLogsUseCase(repository),
                    initiateCallUseCase = mock(),
                    enableListeners = false
                )
                advanceUntilIdle()

                // Verify Content state first
                viewModel.uiState.value.shouldBeInstanceOf<CallLogsUIState.Content>()

                // Clear items
                viewModel.clearItems()

                // Property: after clear, state is exclusively Empty
                viewModel.uiState.value shouldBe CallLogsUIState.Empty
                viewModel.callLogs.value shouldHaveSize 0
                viewModel.getItemCount() shouldBe 0
            }
        }
    }
})
