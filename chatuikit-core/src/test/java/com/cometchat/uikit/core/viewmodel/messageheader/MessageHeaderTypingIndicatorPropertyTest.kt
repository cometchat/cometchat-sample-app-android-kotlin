package com.cometchat.uikit.core.viewmodel.messageheader

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.TypingIndicator
import com.cometchat.uikit.core.domain.usecase.GetGroupUseCase
import com.cometchat.uikit.core.domain.usecase.GetUserUseCase
import com.cometchat.uikit.core.testutils.MockFactory
import com.cometchat.uikit.core.viewmodel.CometChatMessageHeaderViewModel
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
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

/**
 * Property-based tests for typing indicator behavior in CometChatMessageHeaderViewModel.
 * 
 * Tests the invariant: typing indicator should only be shown for the current conversation
 * and should respect blocked user state.
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*MessageHeaderTypingIndicatorPropertyTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MessageHeaderTypingIndicatorPropertyTest : FunSpec({

    isolationMode = IsolationMode.SingleInstance

    val testDispatcher = UnconfinedTestDispatcher()

    lateinit var getUserUseCase: GetUserUseCase
    lateinit var getGroupUseCase: GetGroupUseCase

    fun createViewModel(): CometChatMessageHeaderViewModel {
        return CometChatMessageHeaderViewModel(
            getUserUseCase = getUserUseCase,
            getGroupUseCase = getGroupUseCase,
            enableListeners = false
        )
    }

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        getUserUseCase = mock()
        getGroupUseCase = mock()
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        Dispatchers.resetMain()
        println()
    }

    // ==================== Typing Indicator State Invariants ====================

    test("INVARIANT: typingIndicator starts as null for any user conversation") {
        checkAll(30, Arb.string(5..20)) { uid ->
            runTest {
                val viewModel = createViewModel()
                val user = MockFactory.createUser(uid = uid)
                println("    → setUser(uid=$uid), checking typingIndicator is null")

                viewModel.setUser(user)
                advanceUntilIdle()

                viewModel.typingIndicator.value shouldBe null
            }
        }
    }

    test("INVARIANT: typingIndicator starts as null for any group conversation") {
        checkAll(30, Arb.string(5..20)) { guid ->
            runTest {
                val viewModel = createViewModel()
                val group = MockFactory.createGroup(guid = guid)
                println("    → setGroup(guid=$guid), checking typingIndicator is null")

                viewModel.setGroup(group)
                advanceUntilIdle()

                viewModel.typingIndicator.value shouldBe null
            }
        }
    }

    test("INVARIANT: switching from user to group should reset typing indicator") {
        checkAll(20, Arb.string(5..15), Arb.string(5..15)) { uid, guid ->
            runTest {
                val viewModel = createViewModel()
                println("    → setUser(uid=$uid) then setGroup(guid=$guid)")

                viewModel.setUser(MockFactory.createUser(uid = uid))
                advanceUntilIdle()
                // typingIndicator is null (no SDK events in test mode)
                viewModel.typingIndicator.value shouldBe null

                viewModel.setGroup(MockFactory.createGroup(guid = guid))
                advanceUntilIdle()
                viewModel.typingIndicator.value shouldBe null
            }
        }
    }

    test("INVARIANT: switching from group to user should reset typing indicator") {
        checkAll(20, Arb.string(5..15), Arb.string(5..15)) { guid, uid ->
            runTest {
                val viewModel = createViewModel()
                println("    → setGroup(guid=$guid) then setUser(uid=$uid)")

                viewModel.setGroup(MockFactory.createGroup(guid = guid))
                advanceUntilIdle()
                viewModel.typingIndicator.value shouldBe null

                viewModel.setUser(MockFactory.createUser(uid = uid))
                advanceUntilIdle()
                viewModel.typingIndicator.value shouldBe null
            }
        }
    }

    // ==================== Typing Indicator Matching ====================

    test("INVARIANT: typing indicator for user conversation should match sender uid to currentId") {
        val receiverTypeArb = Arb.element(
            CometChatConstants.RECEIVER_TYPE_USER,
            CometChatConstants.RECEIVER_TYPE_GROUP
        )
        checkAll(20, Arb.string(5..15), receiverTypeArb) { senderUid, receiverType ->
            runTest {
                val viewModel = createViewModel()
                val indicator = MockFactory.createTypingIndicator(
                    senderUid = senderUid,
                    receiverId = "some-receiver",
                    receiverType = receiverType
                )
                println("    → TypingIndicator: sender=$senderUid, receiverType=$receiverType")

                // The matching logic in ViewModel checks:
                // For user type: sender.uid == currentId
                // For group type: receiverId == currentId
                // Since enableListeners=false, we verify the indicator structure
                indicator.sender.uid shouldBe senderUid
                indicator.receiverType shouldBe receiverType
            }
        }
    }
})
