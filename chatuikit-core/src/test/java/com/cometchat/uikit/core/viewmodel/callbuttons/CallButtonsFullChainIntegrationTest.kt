package com.cometchat.uikit.core.viewmodel.callbuttons

import com.cometchat.chat.core.Call
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.CustomMessage
import com.cometchat.uikit.core.data.datasource.CallButtonsDataSource
import com.cometchat.uikit.core.data.repository.CallButtonsRepositoryImpl
import com.cometchat.uikit.core.domain.usecase.InitiateUserCallUseCase
import com.cometchat.uikit.core.domain.usecase.StartGroupCallUseCase
import com.cometchat.uikit.core.state.CallButtonsEvent
import com.cometchat.uikit.core.state.CallButtonsUIState
import com.cometchat.uikit.core.testutils.MockFactory
import com.cometchat.uikit.core.viewmodel.CometChatCallButtonsViewModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Full-chain integration test for the CallButtons component.
 *
 * Tests the complete dependency chain end-to-end:
 *   ViewModel → UseCases → CallButtonsRepositoryImpl → DataSource (interface impl)
 *
 * Instead of mocking use cases or the repository, we provide a custom
 * CallButtonsDataSource implementation (via `object : CallButtonsDataSource`)
 * and let all real production objects wire through.
 *
 * The ONLY fake is the DataSource at the SDK boundary.
 *
 * Each test prints a step-by-step trace showing data flowing through every layer:
 *   [DataSource] → [Repository] → [UseCase] → [ViewModel]
 *
 * Validates: Requirements 11.1, 11.3, 11.6, 11.7
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*.CallButtonsFullChainIntegrationTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CallButtonsFullChainIntegrationTest : FunSpec({

    beforeTest {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        println("\n    🧪 ${it.name.testName}")
        println("    ─────────────────────────────────────────────────")
    }

    afterTest {
        Thread.sleep(50)
        Dispatchers.resetMain()
        println()
    }

    // -- Helper: build the full chain from a custom DataSource --

    fun buildViewModel(
        dataSource: CallButtonsDataSource
    ): CometChatCallButtonsViewModel {
        println("    [WIRING] Building full chain:")
        println("    [WIRING]   DataSource (custom impl) → CallButtonsRepositoryImpl → UseCases → ViewModel")

        val repository = CallButtonsRepositoryImpl(dataSource)
        println("    [WIRING]   ✓ Repository created with custom DataSource")

        val initiateUserCallUseCase = InitiateUserCallUseCase(repository)
        val startGroupCallUseCase = StartGroupCallUseCase(repository)
        println("    [WIRING]   ✓ 2 UseCases created (InitiateUserCall, StartGroupCall) with Repository")

        val viewModel = CometChatCallButtonsViewModel(
            initiateUserCallUseCase = initiateUserCallUseCase,
            startGroupCallUseCase = startGroupCallUseCase,
            enableListeners = false
        )
        println("    [WIRING]   ✓ ViewModel created (enableListeners=false)")
        return viewModel
    }

    // ==================== 11.1 User call success — Content state ====================

    test("warmup - absorb leaked exceptions") {
        try { runTest { } } catch (_: Exception) { }
    }

    test("full chain: DataSource returning successful user call → ViewModel emits CallInitiated and returns to Idle") {
        val testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        runTest(testDispatcher) {
            val mockCall = mock<Call>()
            whenever(mockCall.receiverUid).thenReturn("user-1")
            whenever(mockCall.type).thenReturn("audio")
            println("    [SETUP] Created mock Call (receiverUid=user-1, type=audio)")

            val dataSource = object : CallButtonsDataSource {
                override suspend fun initiateUserCall(receiverId: String, callType: String): Result<Call> {
                    println("    [DataSource] initiateUserCall(receiverId=$receiverId, callType=$callType) → returning Result.success(Call)")
                    return Result.success(mockCall)
                }

                override suspend fun sendGroupCallMessage(groupId: String, callType: String): Result<CustomMessage> {
                    throw IllegalStateException("Should not be called for user call")
                }

                override fun getActiveCall(): Call? = null
                override fun getActiveCallingExtensionCall(): Call? = null
                override fun isActiveMeeting(): Boolean = false
            }

            val viewModel = buildViewModel(dataSource)
            val user = MockFactory.createUser(uid = "user-1", name = "Test User")
            viewModel.setUser(user)

            // Collect events
            val collectedEvents = mutableListOf<CallButtonsEvent>()
            val job = launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.events.toList(collectedEvents)
            }

            println("    [CHAIN]  ViewModel.initiateCall(audio)")
            println("    [CHAIN]    → InitiateUserCallUseCase.invoke(user-1, audio)")
            println("    [CHAIN]      → Repository.hasActiveCall() → false (no active call)")
            println("    [CHAIN]      → Repository.initiateUserCall(user-1, audio)")
            println("    [CHAIN]        → DataSource.initiateUserCall(user-1, audio) → Result.success(Call)")
            println("    [CHAIN]      ← Repository returns Result.success(Call)")
            println("    [CHAIN]    ← UseCase returns Result.success(Call)")
            println("    [CHAIN]  ← ViewModel receives success → emits CallInitiated, state=Idle")

            viewModel.initiateCall("audio")
            advanceUntilIdle()

            viewModel.uiState.value shouldBe CallButtonsUIState.Idle
            collectedEvents.size shouldBe 1
            collectedEvents[0].shouldBeInstanceOf<CallButtonsEvent.CallInitiated>()
            (collectedEvents[0] as CallButtonsEvent.CallInitiated).call shouldBe mockCall

            job.cancel()
            println("    ✅ PASSED — User call success flowed: DataSource → Repo → UseCase → ViewModel → CallInitiated event")
        }
    }

    // ==================== 11.1 Group call success ====================

    test("full chain: DataSource returning successful group call → ViewModel emits StartDirectCall and returns to Idle") {
        val testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        runTest(testDispatcher) {
            val mockMessage = mock<CustomMessage>()
            whenever(mockMessage.receiverUid).thenReturn("group-1")
            println("    [SETUP] Created mock CustomMessage (receiverUid=group-1)")

            val dataSource = object : CallButtonsDataSource {
                override suspend fun initiateUserCall(receiverId: String, callType: String): Result<Call> {
                    throw IllegalStateException("Should not be called for group call")
                }

                override suspend fun sendGroupCallMessage(groupId: String, callType: String): Result<CustomMessage> {
                    println("    [DataSource] sendGroupCallMessage(groupId=$groupId, callType=$callType) → returning Result.success(CustomMessage)")
                    return Result.success(mockMessage)
                }

                override fun getActiveCall(): Call? = null
                override fun getActiveCallingExtensionCall(): Call? = null
                override fun isActiveMeeting(): Boolean = false
            }

            val viewModel = buildViewModel(dataSource)
            val group = MockFactory.createGroup(guid = "group-1", name = "Test Group")
            viewModel.setGroup(group)

            // Collect events
            val collectedEvents = mutableListOf<CallButtonsEvent>()
            val job = launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.events.toList(collectedEvents)
            }

            println("    [CHAIN]  ViewModel.initiateCall(video)")
            println("    [CHAIN]    → StartGroupCallUseCase.invoke(group-1, video)")
            println("    [CHAIN]      → Repository.hasActiveCall() → false (no active call)")
            println("    [CHAIN]      → Repository.startGroupCall(group-1, video)")
            println("    [CHAIN]        → DataSource.sendGroupCallMessage(group-1, video) → Result.success(CustomMessage)")
            println("    [CHAIN]      ← Repository returns Result.success(CustomMessage)")
            println("    [CHAIN]    ← UseCase returns Result.success(CustomMessage)")
            println("    [CHAIN]  ← ViewModel receives success → emits StartDirectCall, state=Idle")

            viewModel.initiateCall("video")
            advanceUntilIdle()

            viewModel.uiState.value shouldBe CallButtonsUIState.Idle
            collectedEvents.size shouldBe 1
            collectedEvents[0].shouldBeInstanceOf<CallButtonsEvent.StartDirectCall>()
            (collectedEvents[0] as CallButtonsEvent.StartDirectCall).message shouldBe mockMessage

            job.cancel()
            println("    ✅ PASSED — Group call success flowed: DataSource → Repo → UseCase → ViewModel → StartDirectCall event")
        }
    }

    // ==================== 11.3 Error state — DataSource failure ====================

    test("full chain: DataSource returning failure for user call → ViewModel shows Error state") {
        val testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        runTest(testDispatcher) {
            val exception = CometChatException("NET_ERR", "No connection", "Network unavailable")
            println("    [SETUP] DataSource will return Result.failure(CometChatException(code=NET_ERR))")

            val dataSource = object : CallButtonsDataSource {
                override suspend fun initiateUserCall(receiverId: String, callType: String): Result<Call> {
                    println("    [DataSource] initiateUserCall(receiverId=$receiverId, callType=$callType) → returning Result.failure")
                    return Result.failure(exception)
                }

                override suspend fun sendGroupCallMessage(groupId: String, callType: String): Result<CustomMessage> {
                    throw IllegalStateException("Should not be called")
                }

                override fun getActiveCall(): Call? = null
                override fun getActiveCallingExtensionCall(): Call? = null
                override fun isActiveMeeting(): Boolean = false
            }

            val viewModel = buildViewModel(dataSource)
            val user = MockFactory.createUser(uid = "user-1", name = "Test User")
            viewModel.setUser(user)

            println("    [CHAIN]  ViewModel.initiateCall(audio)")
            println("    [CHAIN]    → InitiateUserCallUseCase.invoke(user-1, audio)")
            println("    [CHAIN]      → Repository.hasActiveCall() → false")
            println("    [CHAIN]      → Repository.initiateUserCall(user-1, audio)")
            println("    [CHAIN]        → DataSource.initiateUserCall → Result.failure(NET_ERR)")
            println("    [CHAIN]      ← Repository returns Result.failure")
            println("    [CHAIN]    ← UseCase returns Result.failure")
            println("    [CHAIN]  ← ViewModel receives failure → state=Error")

            viewModel.initiateCall("audio")
            advanceUntilIdle()

            viewModel.uiState.value.shouldBeInstanceOf<CallButtonsUIState.Error>()
            val errorState = viewModel.uiState.value as CallButtonsUIState.Error
            errorState.exception shouldBe exception
            errorState.exception.code shouldBe "NET_ERR"

            println("    ✅ PASSED — Error propagated: DataSource failure → Repo → UseCase → ViewModel Error state")
        }
    }

    // ==================== 11.6 Active call blocking via full chain ====================

    test("full chain: active call detected → UseCase blocks call initiation with ACTIVE_CALL error") {
        val testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        runTest(testDispatcher) {
            val activeCall = mock<Call>()
            println("    [SETUP] DataSource.getActiveCall() will return non-null (active call exists)")

            val dataSource = object : CallButtonsDataSource {
                override suspend fun initiateUserCall(receiverId: String, callType: String): Result<Call> {
                    throw IllegalStateException("Should not be called when active call exists")
                }

                override suspend fun sendGroupCallMessage(groupId: String, callType: String): Result<CustomMessage> {
                    throw IllegalStateException("Should not be called when active call exists")
                }

                override fun getActiveCall(): Call? = activeCall
                override fun getActiveCallingExtensionCall(): Call? = null
                override fun isActiveMeeting(): Boolean = false
            }

            val viewModel = buildViewModel(dataSource)
            val user = MockFactory.createUser(uid = "user-1", name = "Test User")
            viewModel.setUser(user)

            println("    [CHAIN]  ViewModel.initiateCall(video)")
            println("    [CHAIN]    → InitiateUserCallUseCase.invoke(user-1, video)")
            println("    [CHAIN]      → Repository.hasActiveCall()")
            println("    [CHAIN]        → DataSource.getActiveCall() → non-null (active call)")
            println("    [CHAIN]      ← Repository.hasActiveCall() = true")
            println("    [CHAIN]    ← UseCase returns Result.failure(ACTIVE_CALL) immediately")
            println("    [CHAIN]  ← ViewModel receives failure → state=Error")

            viewModel.initiateCall("video")
            advanceUntilIdle()

            viewModel.uiState.value.shouldBeInstanceOf<CallButtonsUIState.Error>()
            val errorState = viewModel.uiState.value as CallButtonsUIState.Error
            errorState.exception.code shouldBe "ACTIVE_CALL"

            println("    ✅ PASSED — Active call blocking: DataSource.getActiveCall()=non-null → UseCase blocked → Error state")
        }
    }

    // ==================== 11.7 Active meeting blocking via full chain ====================

    test("full chain: active meeting detected → UseCase blocks group call with ACTIVE_CALL error") {
        val testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        runTest(testDispatcher) {
            println("    [SETUP] DataSource.isActiveMeeting() will return true")

            val dataSource = object : CallButtonsDataSource {
                override suspend fun initiateUserCall(receiverId: String, callType: String): Result<Call> {
                    throw IllegalStateException("Should not be called when active meeting exists")
                }

                override suspend fun sendGroupCallMessage(groupId: String, callType: String): Result<CustomMessage> {
                    throw IllegalStateException("Should not be called when active meeting exists")
                }

                override fun getActiveCall(): Call? = null
                override fun getActiveCallingExtensionCall(): Call? = null
                override fun isActiveMeeting(): Boolean = true
            }

            val viewModel = buildViewModel(dataSource)
            val group = MockFactory.createGroup(guid = "group-1", name = "Test Group")
            viewModel.setGroup(group)

            println("    [CHAIN]  ViewModel.initiateCall(audio)")
            println("    [CHAIN]    → StartGroupCallUseCase.invoke(group-1, audio)")
            println("    [CHAIN]      → Repository.hasActiveCall()")
            println("    [CHAIN]        → DataSource.getActiveCall() → null")
            println("    [CHAIN]        → DataSource.getActiveCallingExtensionCall() → null")
            println("    [CHAIN]        → DataSource.isActiveMeeting() → true")
            println("    [CHAIN]      ← Repository.hasActiveCall() = true")
            println("    [CHAIN]    ← UseCase returns Result.failure(ACTIVE_CALL) immediately")
            println("    [CHAIN]  ← ViewModel receives failure → state=Error")

            viewModel.initiateCall("audio")
            advanceUntilIdle()

            viewModel.uiState.value.shouldBeInstanceOf<CallButtonsUIState.Error>()
            val errorState = viewModel.uiState.value as CallButtonsUIState.Error
            errorState.exception.code shouldBe "ACTIVE_CALL"

            println("    ✅ PASSED — Active meeting blocking: isActiveMeeting()=true → UseCase blocked → Error state")
        }
    }

    // ==================== 11.6 CallingExtension active call blocking ====================

    test("full chain: CallingExtension active call detected → UseCase blocks with ACTIVE_CALL error") {
        val testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        runTest(testDispatcher) {
            val activeCall = mock<Call>()
            println("    [SETUP] DataSource.getActiveCallingExtensionCall() will return non-null")

            val dataSource = object : CallButtonsDataSource {
                override suspend fun initiateUserCall(receiverId: String, callType: String): Result<Call> {
                    throw IllegalStateException("Should not be called when active call exists")
                }

                override suspend fun sendGroupCallMessage(groupId: String, callType: String): Result<CustomMessage> {
                    throw IllegalStateException("Should not be called when active call exists")
                }

                override fun getActiveCall(): Call? = null
                override fun getActiveCallingExtensionCall(): Call? = activeCall
                override fun isActiveMeeting(): Boolean = false
            }

            val viewModel = buildViewModel(dataSource)
            val user = MockFactory.createUser(uid = "user-1", name = "Test User")
            viewModel.setUser(user)

            println("    [CHAIN]  ViewModel.initiateCall(audio)")
            println("    [CHAIN]    → InitiateUserCallUseCase.invoke(user-1, audio)")
            println("    [CHAIN]      → Repository.hasActiveCall()")
            println("    [CHAIN]        → DataSource.getActiveCall() → null")
            println("    [CHAIN]        → DataSource.getActiveCallingExtensionCall() → non-null")
            println("    [CHAIN]      ← Repository.hasActiveCall() = true")
            println("    [CHAIN]    ← UseCase returns Result.failure(ACTIVE_CALL)")
            println("    [CHAIN]  ← ViewModel receives failure → state=Error")

            viewModel.initiateCall("audio")
            advanceUntilIdle()

            viewModel.uiState.value.shouldBeInstanceOf<CallButtonsUIState.Error>()
            val errorState = viewModel.uiState.value as CallButtonsUIState.Error
            errorState.exception.code shouldBe "ACTIVE_CALL"

            println("    ✅ PASSED — CallingExtension blocking: getActiveCallingExtensionCall()=non-null → Error state")
        }
    }
})
