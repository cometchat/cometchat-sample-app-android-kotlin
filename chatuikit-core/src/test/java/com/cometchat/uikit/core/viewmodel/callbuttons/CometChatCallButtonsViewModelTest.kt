package com.cometchat.uikit.core.viewmodel.callbuttons

import com.cometchat.chat.core.Call
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.CustomMessage
import com.cometchat.uikit.core.domain.usecase.InitiateUserCallUseCase
import com.cometchat.uikit.core.domain.usecase.StartGroupCallUseCase
import com.cometchat.uikit.core.state.CallButtonsEvent
import com.cometchat.uikit.core.state.CallButtonsUIState
import com.cometchat.uikit.core.testutils.MockFactory
import com.cometchat.uikit.core.viewmodel.CometChatCallButtonsViewModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Comprehensive property-based tests for CometChatCallButtonsViewModel.
 *
 * CallButtons is a call-utility component (NOT list-based). It manages:
 * - User/Group target setting
 * - Call initiation (audio/video) for user or group
 * - Active call blocking (cannot initiate if active call exists)
 * - Error state handling
 * - One-time events (CallInitiated, StartDirectCall, CallRejected)
 *
 * PBT covers:
 * - A. User/Group target setting
 * - B. Call initiation success (user and group)
 * - C. Call type (audio/video)
 * - D. Active call blocking (SDK availability)
 * - E. Error state handling
 * - F. UIState transitions (Idle → Initiating → Idle/Error)
 * - G. Event emission isolation
 * - H. Blocked user scenario
 *
 * **Validates: Requirements 7.1–7.6, 27.1–27.6**
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*CometChatCallButtonsViewModelTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatCallButtonsViewModelTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    // ==================== Arb Generators ====================

    val callTypeArb = Arb.element("audio", "video")
    val userUidArb = Arb.string(3..15)
    val groupGuidArb = Arb.string(3..15)

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        Dispatchers.resetMain()
        println()
    }

    // ==================== A. User/Group Target Setting (Requirement 7.1) ====================

    test("setUser should store user and clear group") {
        checkAll(30, userUidArb) { uid ->
            runTest {
                val initiateUseCase = mock<InitiateUserCallUseCase>()
                val groupUseCase = mock<StartGroupCallUseCase>()
                val viewModel = CometChatCallButtonsViewModel(initiateUseCase, groupUseCase, enableListeners = false)
                val user = MockFactory.createUser(uid = uid, name = "User-$uid")

                viewModel.setUser(user)

                viewModel.getUser() shouldBe user
                viewModel.getGroup() shouldBe null
                println("    → uid=$uid: user set, group=null ✅")
            }
        }
    }

    test("setGroup should store group and clear user") {
        checkAll(30, groupGuidArb) { guid ->
            runTest {
                val initiateUseCase = mock<InitiateUserCallUseCase>()
                val groupUseCase = mock<StartGroupCallUseCase>()
                val viewModel = CometChatCallButtonsViewModel(initiateUseCase, groupUseCase, enableListeners = false)
                val group = MockFactory.createGroup(guid = guid, name = "Group-$guid")

                viewModel.setGroup(group)

                viewModel.getGroup() shouldBe group
                viewModel.getUser() shouldBe null
                println("    → guid=$guid: group set, user=null ✅")
            }
        }
    }

    test("setUser after setGroup should replace group with user") {
        checkAll(20, userUidArb, groupGuidArb) { uid, guid ->
            runTest {
                val initiateUseCase = mock<InitiateUserCallUseCase>()
                val groupUseCase = mock<StartGroupCallUseCase>()
                val viewModel = CometChatCallButtonsViewModel(initiateUseCase, groupUseCase, enableListeners = false)
                val group = MockFactory.createGroup(guid = guid, name = "Group-$guid")
                val user = MockFactory.createUser(uid = uid, name = "User-$uid")

                viewModel.setGroup(group)
                viewModel.getGroup() shouldBe group

                viewModel.setUser(user)
                viewModel.getUser() shouldBe user
                viewModel.getGroup() shouldBe null
                println("    → set group($guid) then user($uid): user wins ✅")
            }
        }
    }

    test("setGroup after setUser should replace user with group") {
        checkAll(20, userUidArb, groupGuidArb) { uid, guid ->
            runTest {
                val initiateUseCase = mock<InitiateUserCallUseCase>()
                val groupUseCase = mock<StartGroupCallUseCase>()
                val viewModel = CometChatCallButtonsViewModel(initiateUseCase, groupUseCase, enableListeners = false)
                val user = MockFactory.createUser(uid = uid, name = "User-$uid")
                val group = MockFactory.createGroup(guid = guid, name = "Group-$guid")

                viewModel.setUser(user)
                viewModel.getUser() shouldBe user

                viewModel.setGroup(group)
                viewModel.getGroup() shouldBe group
                viewModel.getUser() shouldBe null
                println("    → set user($uid) then group($guid): group wins ✅")
            }
        }
    }

    // ==================== B. Call Initiation Success (Requirement 7.2) ====================

    test("initiateCall with user target should call initiateUserCallUseCase and emit CallInitiated") {
        checkAll(20, callTypeArb, userUidArb) { callType, uid ->
            runTest {
                val initiateUseCase = mock<InitiateUserCallUseCase>()
                val groupUseCase = mock<StartGroupCallUseCase>()
                val viewModel = CometChatCallButtonsViewModel(initiateUseCase, groupUseCase, enableListeners = false)
                val user = MockFactory.createUser(uid = uid, name = "User-$uid")
                viewModel.setUser(user)

                val mockCall = mock<Call>()
                whenever(initiateUseCase.invoke(uid, callType)).thenReturn(Result.success(mockCall))

                // Collect events before triggering
                val collectedEvents = mutableListOf<CallButtonsEvent>()
                val job = launch(testDispatcher) {
                    viewModel.events.toList(collectedEvents)
                }

                viewModel.initiateCall(callType)
                advanceUntilIdle()

                verify(initiateUseCase).invoke(uid, callType)
                viewModel.uiState.value shouldBe CallButtonsUIState.Idle
                collectedEvents.size shouldBe 1
                collectedEvents[0].shouldBeInstanceOf<CallButtonsEvent.CallInitiated>()
                (collectedEvents[0] as CallButtonsEvent.CallInitiated).call shouldBe mockCall

                job.cancel()
                println("    → uid=$uid, callType=$callType: CallInitiated emitted ✅")
            }
        }
    }

    test("initiateCall with group target should call startGroupCallUseCase and emit StartDirectCall") {
        checkAll(20, callTypeArb, groupGuidArb) { callType, guid ->
            runTest {
                val initiateUseCase = mock<InitiateUserCallUseCase>()
                val groupUseCase = mock<StartGroupCallUseCase>()
                val viewModel = CometChatCallButtonsViewModel(initiateUseCase, groupUseCase, enableListeners = false)
                val group = MockFactory.createGroup(guid = guid, name = "Group-$guid")
                viewModel.setGroup(group)

                val mockMessage = mock<CustomMessage>()
                whenever(groupUseCase.invoke(guid, callType)).thenReturn(Result.success(mockMessage))

                // Collect events before triggering
                val collectedEvents = mutableListOf<CallButtonsEvent>()
                val job = launch(testDispatcher) {
                    viewModel.events.toList(collectedEvents)
                }

                viewModel.initiateCall(callType)
                advanceUntilIdle()

                verify(groupUseCase).invoke(guid, callType)
                viewModel.uiState.value shouldBe CallButtonsUIState.Idle
                collectedEvents.size shouldBe 1
                collectedEvents[0].shouldBeInstanceOf<CallButtonsEvent.StartDirectCall>()
                (collectedEvents[0] as CallButtonsEvent.StartDirectCall).message shouldBe mockMessage

                job.cancel()
                println("    → guid=$guid, callType=$callType: StartDirectCall emitted ✅")
            }
        }
    }

    // ==================== C. Call Type (Requirement 7.3) ====================

    test("for any call type: user call should pass correct callType to use case") {
        checkAll(30, callTypeArb) { callType ->
            runTest {
                val initiateUseCase = mock<InitiateUserCallUseCase>()
                val groupUseCase = mock<StartGroupCallUseCase>()
                val viewModel = CometChatCallButtonsViewModel(initiateUseCase, groupUseCase, enableListeners = false)
                val user = MockFactory.createUser(uid = "user-1", name = "Test User")
                viewModel.setUser(user)

                val mockCall = mock<Call>()
                whenever(initiateUseCase.invoke(any(), any())).thenReturn(Result.success(mockCall))

                viewModel.initiateCall(callType)
                advanceUntilIdle()

                verify(initiateUseCase).invoke("user-1", callType)
                println("    → callType=$callType passed to initiateUserCallUseCase ✅")
            }
        }
    }

    test("for any call type: group call should pass correct callType to use case") {
        checkAll(30, callTypeArb) { callType ->
            runTest {
                val initiateUseCase = mock<InitiateUserCallUseCase>()
                val groupUseCase = mock<StartGroupCallUseCase>()
                val viewModel = CometChatCallButtonsViewModel(initiateUseCase, groupUseCase, enableListeners = false)
                val group = MockFactory.createGroup(guid = "group-1", name = "Test Group")
                viewModel.setGroup(group)

                val mockMessage = mock<CustomMessage>()
                whenever(groupUseCase.invoke(any(), any())).thenReturn(Result.success(mockMessage))

                viewModel.initiateCall(callType)
                advanceUntilIdle()

                verify(groupUseCase).invoke("group-1", callType)
                println("    → callType=$callType passed to startGroupCallUseCase ✅")
            }
        }
    }

    // ==================== D. Active Call Blocking / SDK Availability (Requirement 7.4) ====================

    test("initiateCall should emit error when active call blocks user call") {
        checkAll(20, callTypeArb, userUidArb) { callType, uid ->
            runTest {
                val initiateUseCase = mock<InitiateUserCallUseCase>()
                val groupUseCase = mock<StartGroupCallUseCase>()
                val viewModel = CometChatCallButtonsViewModel(initiateUseCase, groupUseCase, enableListeners = false)
                val user = MockFactory.createUser(uid = uid, name = "User-$uid")
                viewModel.setUser(user)

                val activeCallException = CometChatException(
                    "ACTIVE_CALL",
                    "Cannot initiate call while another call is active",
                    "An active call is already in progress"
                )
                whenever(initiateUseCase.invoke(uid, callType))
                    .thenReturn(Result.failure(activeCallException))

                // Collect error events
                val collectedErrors = mutableListOf<CometChatException>()
                val job = launch(testDispatcher) {
                    viewModel.errorEvent.toList(collectedErrors)
                }

                viewModel.initiateCall(callType)
                advanceUntilIdle()

                viewModel.uiState.value.shouldBeInstanceOf<CallButtonsUIState.Error>()
                val errorState = viewModel.uiState.value as CallButtonsUIState.Error
                errorState.exception.code shouldBe "ACTIVE_CALL"
                collectedErrors.size shouldBe 1
                collectedErrors[0].code shouldBe "ACTIVE_CALL"

                job.cancel()
                println("    → uid=$uid, callType=$callType: ACTIVE_CALL error emitted ✅")
            }
        }
    }

    test("initiateCall should emit error when active call blocks group call") {
        checkAll(20, callTypeArb, groupGuidArb) { callType, guid ->
            runTest {
                val initiateUseCase = mock<InitiateUserCallUseCase>()
                val groupUseCase = mock<StartGroupCallUseCase>()
                val viewModel = CometChatCallButtonsViewModel(initiateUseCase, groupUseCase, enableListeners = false)
                val group = MockFactory.createGroup(guid = guid, name = "Group-$guid")
                viewModel.setGroup(group)

                val activeCallException = CometChatException(
                    "ACTIVE_CALL",
                    "Cannot start call while another call is active",
                    "An active call is already in progress"
                )
                whenever(groupUseCase.invoke(guid, callType))
                    .thenReturn(Result.failure(activeCallException))

                // Collect error events
                val collectedErrors = mutableListOf<CometChatException>()
                val job = launch(testDispatcher) {
                    viewModel.errorEvent.toList(collectedErrors)
                }

                viewModel.initiateCall(callType)
                advanceUntilIdle()

                viewModel.uiState.value.shouldBeInstanceOf<CallButtonsUIState.Error>()
                val errorState = viewModel.uiState.value as CallButtonsUIState.Error
                errorState.exception.code shouldBe "ACTIVE_CALL"
                collectedErrors.size shouldBe 1
                collectedErrors[0].code shouldBe "ACTIVE_CALL"

                job.cancel()
                println("    → guid=$guid, callType=$callType: ACTIVE_CALL error emitted ✅")
            }
        }
    }

    // ==================== E. Error State Handling (Requirement 7.5) ====================

    test("initiateCall with no receiver set should emit INVALID_RECEIVER error") {
        checkAll(20, callTypeArb) { callType ->
            runTest {
                val initiateUseCase = mock<InitiateUserCallUseCase>()
                val groupUseCase = mock<StartGroupCallUseCase>()
                val viewModel = CometChatCallButtonsViewModel(initiateUseCase, groupUseCase, enableListeners = false)
                // Don't set user or group — receiverType will be empty

                // Collect error events
                val collectedErrors = mutableListOf<CometChatException>()
                val job = launch(testDispatcher) {
                    viewModel.errorEvent.toList(collectedErrors)
                }

                viewModel.initiateCall(callType)
                advanceUntilIdle()

                viewModel.uiState.value.shouldBeInstanceOf<CallButtonsUIState.Error>()
                val errorState = viewModel.uiState.value as CallButtonsUIState.Error
                errorState.exception.code shouldBe "INVALID_RECEIVER"
                collectedErrors.size shouldBe 1
                collectedErrors[0].code shouldBe "INVALID_RECEIVER"

                job.cancel()
                println("    → callType=$callType, no receiver: INVALID_RECEIVER error ✅")
            }
        }
    }

    test("initiateCall failure with generic exception should set Error state") {
        checkAll(20, callTypeArb, Arb.string(3..20)) { callType, errorMsg ->
            runTest {
                val initiateUseCase = mock<InitiateUserCallUseCase>()
                val groupUseCase = mock<StartGroupCallUseCase>()
                val viewModel = CometChatCallButtonsViewModel(initiateUseCase, groupUseCase, enableListeners = false)
                val user = MockFactory.createUser(uid = "user-1", name = "Test User")
                viewModel.setUser(user)

                val exception = CometChatException("ERR_GENERIC", errorMsg)
                whenever(initiateUseCase.invoke("user-1", callType))
                    .thenReturn(Result.failure(exception))

                viewModel.initiateCall(callType)
                advanceUntilIdle()

                viewModel.uiState.value.shouldBeInstanceOf<CallButtonsUIState.Error>()
                val errorState = viewModel.uiState.value as CallButtonsUIState.Error
                errorState.exception.code shouldBe "ERR_GENERIC"
                errorState.exception.message shouldBe errorMsg
                println("    → callType=$callType, error='$errorMsg': Error state set ✅")
            }
        }
    }

    // ==================== F. UIState Transitions (Requirement 7.6) ====================

    test("initial state should be Idle") {
        runTest {
            val initiateUseCase = mock<InitiateUserCallUseCase>()
            val groupUseCase = mock<StartGroupCallUseCase>()
            val viewModel = CometChatCallButtonsViewModel(initiateUseCase, groupUseCase, enableListeners = false)

            viewModel.uiState.value shouldBe CallButtonsUIState.Idle
            println("    → initial state is Idle ✅")
        }
    }

    test("successful call should end in Idle state") {
        checkAll(20, callTypeArb) { callType ->
            runTest {
                val initiateUseCase = mock<InitiateUserCallUseCase>()
                val groupUseCase = mock<StartGroupCallUseCase>()
                val viewModel = CometChatCallButtonsViewModel(initiateUseCase, groupUseCase, enableListeners = false)
                val user = MockFactory.createUser(uid = "user-1", name = "Test User")
                viewModel.setUser(user)

                val mockCall = mock<Call>()
                whenever(initiateUseCase.invoke("user-1", callType))
                    .thenReturn(Result.success(mockCall))

                // Before call
                viewModel.uiState.value shouldBe CallButtonsUIState.Idle

                viewModel.initiateCall(callType)
                advanceUntilIdle()

                // After successful call — back to Idle
                viewModel.uiState.value shouldBe CallButtonsUIState.Idle
                println("    → callType=$callType: Idle → ... → Idle ✅")
            }
        }
    }

    test("failed call should end in Error state") {
        checkAll(20, callTypeArb, Arb.string(3..10)) { callType, errCode ->
            runTest {
                val initiateUseCase = mock<InitiateUserCallUseCase>()
                val groupUseCase = mock<StartGroupCallUseCase>()
                val viewModel = CometChatCallButtonsViewModel(initiateUseCase, groupUseCase, enableListeners = false)
                val user = MockFactory.createUser(uid = "user-1", name = "Test User")
                viewModel.setUser(user)

                val exception = CometChatException(errCode, "Test error")
                whenever(initiateUseCase.invoke("user-1", callType))
                    .thenReturn(Result.failure(exception))

                // Before call
                viewModel.uiState.value shouldBe CallButtonsUIState.Idle

                viewModel.initiateCall(callType)
                advanceUntilIdle()

                // After failed call — Error state
                viewModel.uiState.value.shouldBeInstanceOf<CallButtonsUIState.Error>()
                val errorState = viewModel.uiState.value as CallButtonsUIState.Error
                errorState.exception.code shouldBe errCode
                println("    → callType=$callType, errCode=$errCode: Idle → ... → Error ✅")
            }
        }
    }

    // ==================== G. Event Emission Isolation (Requirement 27.1–27.6) ====================

    test("user call success should not invoke startGroupCallUseCase") {
        checkAll(20, callTypeArb, userUidArb) { callType, uid ->
            runTest {
                val initiateUseCase = mock<InitiateUserCallUseCase>()
                val groupUseCase = mock<StartGroupCallUseCase>()
                val viewModel = CometChatCallButtonsViewModel(initiateUseCase, groupUseCase, enableListeners = false)
                val user = MockFactory.createUser(uid = uid, name = "User-$uid")
                viewModel.setUser(user)

                val mockCall = mock<Call>()
                whenever(initiateUseCase.invoke(uid, callType))
                    .thenReturn(Result.success(mockCall))

                viewModel.initiateCall(callType)
                advanceUntilIdle()

                verify(groupUseCase, never()).invoke(any(), any())
                println("    → uid=$uid: startGroupCallUseCase NOT called ✅")
            }
        }
    }

    test("group call success should not invoke initiateUserCallUseCase") {
        checkAll(20, callTypeArb, groupGuidArb) { callType, guid ->
            runTest {
                val initiateUseCase = mock<InitiateUserCallUseCase>()
                val groupUseCase = mock<StartGroupCallUseCase>()
                val viewModel = CometChatCallButtonsViewModel(initiateUseCase, groupUseCase, enableListeners = false)
                val group = MockFactory.createGroup(guid = guid, name = "Group-$guid")
                viewModel.setGroup(group)

                val mockMessage = mock<CustomMessage>()
                whenever(groupUseCase.invoke(guid, callType))
                    .thenReturn(Result.success(mockMessage))

                viewModel.initiateCall(callType)
                advanceUntilIdle()

                verify(initiateUseCase, never()).invoke(any(), any())
                println("    → guid=$guid: initiateUserCallUseCase NOT called ✅")
            }
        }
    }

    test("removeListeners should not throw") {
        runTest {
            val initiateUseCase = mock<InitiateUserCallUseCase>()
            val groupUseCase = mock<StartGroupCallUseCase>()
            val viewModel = CometChatCallButtonsViewModel(initiateUseCase, groupUseCase, enableListeners = false)

            // Should not throw even when no listeners were added (enableListeners = false)
            viewModel.removeListeners()
            println("    → removeListeners() called without exception ✅")
        }
    }

    // ==================== H. Blocked User Scenario (Requirement 7.4) ====================

    test("initiateCall with blocked user should propagate error from use case") {
        checkAll(20, callTypeArb, Arb.boolean()) { callType, isBlockedByMe ->
            runTest {
                val initiateUseCase = mock<InitiateUserCallUseCase>()
                val groupUseCase = mock<StartGroupCallUseCase>()
                val viewModel = CometChatCallButtonsViewModel(initiateUseCase, groupUseCase, enableListeners = false)
                val user = MockFactory.createUser(
                    uid = "blocked-user",
                    name = "Blocked User",
                    isBlockedByMe = isBlockedByMe
                )
                viewModel.setUser(user)

                if (isBlockedByMe) {
                    // Simulate use case returning error for blocked user
                    val blockedException = CometChatException(
                        "ERR_BLOCKED",
                        "Cannot call blocked user"
                    )
                    whenever(initiateUseCase.invoke("blocked-user", callType))
                        .thenReturn(Result.failure(blockedException))

                    viewModel.initiateCall(callType)
                    advanceUntilIdle()

                    viewModel.uiState.value.shouldBeInstanceOf<CallButtonsUIState.Error>()
                    val errorState = viewModel.uiState.value as CallButtonsUIState.Error
                    errorState.exception.code shouldBe "ERR_BLOCKED"
                    println("    → blocked=$isBlockedByMe, callType=$callType: ERR_BLOCKED ✅")
                } else {
                    // Non-blocked user should succeed
                    val mockCall = mock<Call>()
                    whenever(initiateUseCase.invoke("blocked-user", callType))
                        .thenReturn(Result.success(mockCall))

                    viewModel.initiateCall(callType)
                    advanceUntilIdle()

                    viewModel.uiState.value shouldBe CallButtonsUIState.Idle
                    println("    → blocked=$isBlockedByMe, callType=$callType: success ✅")
                }
            }
        }
    }
})
