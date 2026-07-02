package com.cometchat.uikit.kotlin.presentation.callbuttons

import com.cometchat.chat.core.Call
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.CustomMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.domain.usecase.InitiateUserCallUseCase
import com.cometchat.uikit.core.domain.usecase.StartGroupCallUseCase
import com.cometchat.uikit.core.state.CallButtonsUIState
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
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Property-based tests for CometChatCallButtons Kotlin component.
 *
 * Tests domain invariants that must hold for ANY valid input:
 * - Visibility exclusivity: user XOR group is set (never both)
 * - UIState exclusivity: Idle/Initiating/Error are mutually exclusive
 * - Call type correctness: audio/video forwarded correctly
 * - Target switching: last set target wins
 *
 * Validates: Requirements 13.2, 32.4
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*CometChatCallButtonsPropertyTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatCallButtonsPropertyTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

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

    // ==================== Visibility Exclusivity (Requirement 13.2) ====================

    test("for any sequence of setUser/setGroup: user and group are mutually exclusive") {
        checkAll(30, userUidArb, groupGuidArb, Arb.boolean()) { uid, guid, setUserLast ->
            runTest {
                val initiateUseCase = mock<InitiateUserCallUseCase>()
                val groupUseCase = mock<StartGroupCallUseCase>()
                val viewModel = CometChatCallButtonsViewModel(
                    initiateUseCase, groupUseCase, enableListeners = false
                )

                val user = mock<User>().also { whenever(it.uid).thenReturn(uid); whenever(it.name).thenReturn("User-$uid") }
                val group = mock<Group>().also { whenever(it.guid).thenReturn(guid); whenever(it.name).thenReturn("Group-$guid") }

                if (setUserLast) {
                    viewModel.setGroup(group)
                    viewModel.setUser(user)
                    // Property: user is set, group is null
                    viewModel.getUser() shouldBe user
                    viewModel.getGroup() shouldBe null
                } else {
                    viewModel.setUser(user)
                    viewModel.setGroup(group)
                    // Property: group is set, user is null
                    viewModel.getGroup() shouldBe group
                    viewModel.getUser() shouldBe null
                }
                println("    → setUserLast=$setUserLast: mutual exclusivity holds ✅")
            }
        }
    }

    // ==================== UIState Exclusivity (Requirement 32.4) ====================

    test("UIState exclusivity: exactly one state variant is active at any time") {
        checkAll(20, callTypeArb, Arb.boolean()) { callType, shouldSucceed ->
            runTest {
                val initiateUseCase = mock<InitiateUserCallUseCase>()
                val groupUseCase = mock<StartGroupCallUseCase>()
                val viewModel = CometChatCallButtonsViewModel(
                    initiateUseCase, groupUseCase, enableListeners = false
                )

                val user = mock<User>().also { whenever(it.uid).thenReturn("user-1"); whenever(it.name).thenReturn("Test User") }
                viewModel.setUser(user)

                if (shouldSucceed) {
                    val mockCall = mock<Call>()
                    whenever(initiateUseCase.invoke("user-1", callType))
                        .thenReturn(Result.success(mockCall))
                } else {
                    val exception = CometChatException("ERR", "Test error")
                    whenever(initiateUseCase.invoke("user-1", callType))
                        .thenReturn(Result.failure(exception))
                }

                viewModel.initiateCall(callType)
                advanceUntilIdle()

                val state = viewModel.uiState.value
                val isIdle = state is CallButtonsUIState.Idle
                val isInitiating = state is CallButtonsUIState.Initiating
                val isError = state is CallButtonsUIState.Error

                // Property: exactly one state is active
                val activeCount = listOf(isIdle, isInitiating, isError).count { it }
                activeCount shouldBe 1

                // Property: success → Idle, failure → Error
                if (shouldSucceed) {
                    state shouldBe CallButtonsUIState.Idle
                } else {
                    state.shouldBeInstanceOf<CallButtonsUIState.Error>()
                }
                println("    → callType=$callType, success=$shouldSucceed: exclusivity holds ✅")
            }
        }
    }

    // ==================== Call Type Correctness (Requirement 13.2) ====================

    test("for any call type: the correct type is forwarded to the use case") {
        checkAll(30, callTypeArb) { callType ->
            runTest {
                val initiateUseCase = mock<InitiateUserCallUseCase>()
                val groupUseCase = mock<StartGroupCallUseCase>()
                val viewModel = CometChatCallButtonsViewModel(
                    initiateUseCase, groupUseCase, enableListeners = false
                )

                val user = mock<User>().also { whenever(it.uid).thenReturn("user-1"); whenever(it.name).thenReturn("Test User") }
                viewModel.setUser(user)

                val mockCall = mock<Call>()
                whenever(initiateUseCase.invoke("user-1", callType))
                    .thenReturn(Result.success(mockCall))

                viewModel.initiateCall(callType)
                advanceUntilIdle()

                // Property: callType is forwarded unchanged
                org.mockito.kotlin.verify(initiateUseCase).invoke("user-1", callType)
                println("    → callType=$callType forwarded correctly ✅")
            }
        }
    }

    // ==================== Target Switching (Requirement 13.2) ====================

    test("for any number of target switches: last target wins") {
        checkAll(20, userUidArb, groupGuidArb) { uid, guid ->
            runTest {
                val initiateUseCase = mock<InitiateUserCallUseCase>()
                val groupUseCase = mock<StartGroupCallUseCase>()
                val viewModel = CometChatCallButtonsViewModel(
                    initiateUseCase, groupUseCase, enableListeners = false
                )

                val user = mock<User>().also { whenever(it.uid).thenReturn(uid); whenever(it.name).thenReturn("User-$uid") }
                val group = mock<Group>().also { whenever(it.guid).thenReturn(guid); whenever(it.name).thenReturn("Group-$guid") }

                // Switch multiple times
                viewModel.setUser(user)
                viewModel.setGroup(group)
                viewModel.setUser(user)
                viewModel.setGroup(group)
                viewModel.setUser(user)

                // Property: last call was setUser, so user wins
                viewModel.getUser() shouldBe user
                viewModel.getGroup() shouldBe null

                // Switch back to group
                viewModel.setGroup(group)

                // Property: last call was setGroup, so group wins
                viewModel.getGroup() shouldBe group
                viewModel.getUser() shouldBe null
                println("    → uid=$uid, guid=$guid: last target wins ✅")
            }
        }
    }

    // ==================== Error Recovery (Requirement 32.4) ====================

    test("after error state: subsequent successful call returns to Idle") {
        checkAll(20, callTypeArb) { callType ->
            runTest {
                val initiateUseCase = mock<InitiateUserCallUseCase>()
                val groupUseCase = mock<StartGroupCallUseCase>()
                val viewModel = CometChatCallButtonsViewModel(
                    initiateUseCase, groupUseCase, enableListeners = false
                )

                val user = mock<User>().also { whenever(it.uid).thenReturn("user-1"); whenever(it.name).thenReturn("Test User") }
                viewModel.setUser(user)

                // First call fails
                val exception = CometChatException("ERR", "Failed")
                whenever(initiateUseCase.invoke("user-1", callType))
                    .thenReturn(Result.failure(exception))

                viewModel.initiateCall(callType)
                advanceUntilIdle()
                viewModel.uiState.value.shouldBeInstanceOf<CallButtonsUIState.Error>()

                // Second call succeeds
                val mockCall = mock<Call>()
                whenever(initiateUseCase.invoke("user-1", callType))
                    .thenReturn(Result.success(mockCall))

                viewModel.initiateCall(callType)
                advanceUntilIdle()

                // Property: after recovery, state is Idle
                viewModel.uiState.value shouldBe CallButtonsUIState.Idle
                println("    → callType=$callType: error → success → Idle ✅")
            }
        }
    }
})
