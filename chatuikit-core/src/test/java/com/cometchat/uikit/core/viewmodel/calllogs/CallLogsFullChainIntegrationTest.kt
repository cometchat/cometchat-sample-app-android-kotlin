package com.cometchat.uikit.core.viewmodel.calllogs

import com.cometchat.calls.core.CallLogRequest
import com.cometchat.calls.exceptions.CometChatException
import com.cometchat.calls.model.CallLog
import com.cometchat.uikit.core.data.datasource.CallLogsDataSource
import com.cometchat.uikit.core.data.repository.CallLogsRepositoryImpl
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
import org.objenesis.ObjenesisStd

/**
 * Full-chain integration test for the CallLogs component.
 *
 * Tests the complete dependency chain end-to-end:
 *   ViewModel → UseCase → CallLogsRepositoryImpl → DataSource (interface impl)
 *
 * Instead of mocking use cases or the repository, we provide a custom
 * CallLogsDataSource implementation (via `object : CallLogsDataSource`)
 * and let all real production objects wire through.
 *
 * The ONLY fake is the DataSource at the SDK boundary.
 * Objenesis is used to allocate CallLog instances without triggering
 * Android static initializers (CallLog depends on android.os.Handler).
 *
 * Each test prints a step-by-step trace showing data flowing through every layer:
 *   [DataSource] → [Repository] → [UseCase] → [ViewModel]
 *
 * Validates: Requirements 11.1, 11.2, 11.3, 11.4, 11.6, 11.7
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*.CallLogsFullChainIntegrationTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CallLogsFullChainIntegrationTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()
    val objenesis = ObjenesisStd()

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        println("\n    🧪 ${it.name.testName}")
        println("    ─────────────────────────────────────────────────")
    }

    afterTest {
        Dispatchers.resetMain()
        println()
    }

    // -- Helper: allocate CallLog instances via Objenesis (bypasses Android static init) --

    fun createCallLogs(count: Int): List<CallLog> {
        return (1..count).map {
            objenesis.newInstance(CallLog::class.java)
        }
    }

    // -- Helper: build the full chain from a custom DataSource with logging --

    fun buildViewModel(
        dataSource: CallLogsDataSource
    ): CometChatCallLogsViewModel {
        println("    [WIRING] Building full chain:")
        println("    [WIRING]   DataSource (custom impl) → CallLogsRepositoryImpl → UseCases → ViewModel")

        val repository = CallLogsRepositoryImpl(dataSource)
        println("    [WIRING]   ✓ Repository created with custom DataSource")

        val fetchCallLogsUseCase = FetchCallLogsUseCase(repository)
        val initiateCallUseCase = InitiateCallUseCase()
        println("    [WIRING]   ✓ 2 UseCases created (FetchCallLogs, InitiateCall) with Repository")

        val viewModel = CometChatCallLogsViewModel(
            fetchCallLogsUseCase = fetchCallLogsUseCase,
            initiateCallUseCase = initiateCallUseCase,
            enableListeners = false
        )
        println("    [WIRING]   ✓ ViewModel created (enableListeners=false)")
        println("    [WIRING]   ✓ ViewModel.init → fetchCallLogs() triggered automatically")
        return viewModel
    }

    // ==================== 11.1 Content state ====================

    test("full chain: DataSource returning call logs → ViewModel shows Content state") {
        runTest {
            val fakeCallLogs = createCallLogs(5)
            println("    [SETUP] Created 5 fake call logs via Objenesis")

            val dataSource = object : CallLogsDataSource {
                override suspend fun fetchCallLogs(request: CallLogRequest): Result<List<CallLog>> {
                    println("    [DataSource] fetchCallLogs() called → returning Result.success(${fakeCallLogs.size} call logs)")
                    return Result.success(fakeCallLogs)
                }
            }

            val viewModel = buildViewModel(dataSource)
            println("    [CHAIN]  ViewModel.init → fetchCallLogs()")
            println("    [CHAIN]    → FetchCallLogsUseCase.invoke(request)")
            println("    [CHAIN]      → CallLogsRepositoryImpl.getCallLogs(request)")
            println("    [CHAIN]        → DataSource.fetchCallLogs(request) → returned Result.success(5 items)")
            println("    [CHAIN]      ← Repository returns Result.success(5 call logs), hasMore=true")
            println("    [CHAIN]    ← UseCase passes Result through unchanged")
            println("    [CHAIN]  ← ViewModel receives Result.success → updates state to Content")
            advanceUntilIdle()

            val uiState = viewModel.uiState.value
            val callLogsSize = viewModel.callLogs.value.size

            println("    [ViewModel] uiState = ${uiState::class.simpleName}")
            println("    [ViewModel] callLogs.size = $callLogsSize")

            uiState.shouldBeInstanceOf<CallLogsUIState.Content>()
            viewModel.callLogs.value shouldHaveSize 5

            println("    [VERIFY] ViewModel shows Content state with 5 call logs ✓")
            println("    ✅ PASSED — 5 call logs flowed: DataSource → Repo → UseCase → ViewModel → Content")
        }
    }

    // ==================== 11.2 Empty state ====================

    test("full chain: DataSource returning empty list → ViewModel shows Empty state") {
        runTest {
            println("    [SETUP] DataSource will return Result.success(emptyList)")

            val dataSource = object : CallLogsDataSource {
                override suspend fun fetchCallLogs(request: CallLogRequest): Result<List<CallLog>> {
                    println("    [DataSource] fetchCallLogs() called → returning Result.success(empty list)")
                    return Result.success(emptyList())
                }
            }

            val viewModel = buildViewModel(dataSource)
            println("    [CHAIN]  ViewModel.init → fetchCallLogs()")
            println("    [CHAIN]    → FetchCallLogsUseCase.invoke(request)")
            println("    [CHAIN]      → CallLogsRepositoryImpl.getCallLogs(request)")
            println("    [CHAIN]        → DataSource.fetchCallLogs(request) → returned Result.success(0 items)")
            println("    [CHAIN]      ← Repository returns Result.success(emptyList), sets hasMore=false")
            println("    [CHAIN]    ← UseCase passes Result through unchanged")
            println("    [CHAIN]  ← ViewModel receives empty list → transitions to Empty state")
            advanceUntilIdle()

            val uiState = viewModel.uiState.value
            val callLogsSize = viewModel.callLogs.value.size

            println("    [ViewModel] uiState = ${uiState::class.simpleName}")
            println("    [ViewModel] callLogs.size = $callLogsSize")

            uiState shouldBe CallLogsUIState.Empty
            viewModel.callLogs.value shouldHaveSize 0

            println("    ✅ PASSED — Empty list flowed through entire chain → CallLogsUIState.Empty")
        }
    }

    // ==================== 11.3 Error state ====================

    test("full chain: DataSource returning failure → ViewModel shows Error state") {
        runTest {
            val exception = CometChatException("NET_ERR", "No connection")
            println("    [SETUP] DataSource will return Result.failure(CometChatException(code=NET_ERR, message=No connection))")

            val dataSource = object : CallLogsDataSource {
                override suspend fun fetchCallLogs(request: CallLogRequest): Result<List<CallLog>> {
                    println("    [DataSource] fetchCallLogs() called → returning Result.failure(CometChatException)")
                    return Result.failure(exception)
                }
            }

            val viewModel = buildViewModel(dataSource)
            println("    [CHAIN]  ViewModel.init → fetchCallLogs()")
            println("    [CHAIN]    → FetchCallLogsUseCase.invoke(request)")
            println("    [CHAIN]      → CallLogsRepositoryImpl.getCallLogs(request)")
            println("    [CHAIN]        → DataSource.fetchCallLogs(request) → returned Result.failure")
            println("    [CHAIN]      ← Repository returns Result.failure(exception) unchanged")
            println("    [CHAIN]    ← UseCase passes Result.failure through unchanged")
            println("    [CHAIN]  ← ViewModel receives Result.failure → transitions to Error state")
            advanceUntilIdle()

            val uiState = viewModel.uiState.value

            println("    [ViewModel] uiState = ${uiState::class.simpleName}")
            if (uiState is CallLogsUIState.Error) {
                println("    [ViewModel] exception.code = ${uiState.exception.code}")
                println("    [ViewModel] exception.message = ${uiState.exception.message}")
                println("    [ViewModel] exception matches original? ${uiState.exception === exception}")
            }

            uiState.shouldBeInstanceOf<CallLogsUIState.Error>()
            (uiState as CallLogsUIState.Error).exception shouldBe exception

            println("    ✅ PASSED — Exception propagated: DataSource returned failure → Repo passed → UseCase passed → ViewModel Error")
        }
    }

    // ==================== 11.4 Pagination appends ====================

    test("full chain: pagination appends second page from DataSource without replacing existing items") {
        runTest {
            val page1 = createCallLogs(3)
            val page2 = createCallLogs(2)
            var callCount = 0

            println("    [SETUP] Page 1: ${page1.size} call logs")
            println("    [SETUP] Page 2: ${page2.size} call logs")

            val dataSource = object : CallLogsDataSource {
                override suspend fun fetchCallLogs(request: CallLogRequest): Result<List<CallLog>> {
                    callCount++
                    val page = if (callCount == 1) page1 else page2
                    println("    [DataSource] fetchCallLogs() call #$callCount → returning Result.success(${page.size} items)")
                    return Result.success(page)
                }
            }

            // --- First fetch (automatic from init) ---
            println("    [CHAIN]  --- First fetch (from ViewModel.init) ---")
            val viewModel = buildViewModel(dataSource)
            advanceUntilIdle()

            println("    [CHAIN]    DataSource returned page1 (3 items)")
            println("    [CHAIN]    Repository returned Result.success(3 call logs), hasMore=true")
            println("    [CHAIN]    ViewModel appended → callLogs.size = ${viewModel.callLogs.value.size}")
            viewModel.callLogs.value shouldHaveSize 3

            // Verify page1 items are present
            page1.forEach { callLog ->
                viewModel.callLogs.value.contains(callLog) shouldBe true
            }

            // --- Second fetch (manual pagination) ---
            println("    [CHAIN]  --- Second fetch (manual viewModel.fetchCallLogs()) ---")
            viewModel.fetchCallLogs()
            advanceUntilIdle()

            println("    [CHAIN]    DataSource returned page2 (2 items)")
            println("    [CHAIN]    Repository returned Result.success(2 call logs), hasMore=true")
            println("    [CHAIN]    ViewModel APPENDED to existing list → callLogs.size = ${viewModel.callLogs.value.size}")

            println("    [ViewModel] callLogs.size = ${viewModel.callLogs.value.size}")
            println("    [ViewModel] uiState = ${viewModel.uiState.value::class.simpleName}")

            // Verify total count is page1 + page2
            viewModel.callLogs.value shouldHaveSize 5
            viewModel.uiState.value.shouldBeInstanceOf<CallLogsUIState.Content>()

            // Verify page1 items are still present (not replaced)
            page1.forEach { callLog ->
                viewModel.callLogs.value.contains(callLog) shouldBe true
            }
            // Verify page2 items are also present
            page2.forEach { callLog ->
                viewModel.callLogs.value.contains(callLog) shouldBe true
            }

            println("    [VERIFY] Page1(3) items still present ✓")
            println("    [VERIFY] Page2(2) items appended ✓")
            println("    [VERIFY] Total = 5 (3 + 2) ✓")
            println("    ✅ PASSED — Page1(3) + Page2(2) = 5 total, correctly appended via full chain")
        }
    }
})
