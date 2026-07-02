package com.cometchat.uikit.core.viewmodel.messagelist

import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.repository.MessageListRepository
import com.cometchat.uikit.core.testutils.MockFactory
import com.cometchat.uikit.core.viewmodel.CometChatMessageListViewModel
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Verifies the `isAgentChat` state machine in [CometChatMessageListViewModel] — the
 * conversation-level flag that distinguishes a 1-1 BYOA agent chat from everything else.
 *
 * Contract (see [CometChatMessageListViewModel.setUser] / [setGroup]):
 * - `setUser(peer)` sets the flag from `AgentChatDetector.isAgentChat(peer)` (role `@agentic`).
 * - `setGroup(...)` ALWAYS forces the flag false — a group is never an "agent chat",
 *   even when it contains an agent member (their messages are still attributed/rendered
 *   as agentic, but the conversation mode stays "group").
 *
 * This is the conversation-mode counterpart to the detector-level coverage in
 * CardAgentContextPropertyTest, asserting the wiring through the public getter.
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*MessageListAgentChatStatePropertyTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MessageListAgentChatStatePropertyTest : FunSpec({

    isolationMode = IsolationMode.SingleInstance

    val testDispatcher = UnconfinedTestDispatcher()
    lateinit var repository: MessageListRepository

    beforeSpec { Dispatchers.setMain(testDispatcher) }
    afterSpec { Dispatchers.resetMain() }
    beforeTest { repository = mock() }

    fun viewModel() = CometChatMessageListViewModel(
        repository = repository,
        enableListeners = false
    )

    fun agentUser() = MockFactory.createUser(uid = "agent-1", name = "Support Bot")
        .also { whenever(it.role).thenReturn(UIKitConstants.AIConstants.AGENTIC_USER) }

    fun humanUser() = MockFactory.createUser(uid = "user-2", name = "Alice")
        .also { whenever(it.role).thenReturn("default") }

    test("setUser with a human peer → isAgentChat() is false (plain 1-1)") {
        val vm = viewModel()
        vm.setUser(humanUser())
        vm.isAgentChat() shouldBe false
    }

    test("setUser with an @agentic peer → isAgentChat() is true (BYOA)") {
        val vm = viewModel()
        vm.setUser(agentUser())
        vm.isAgentChat() shouldBe true
    }

    test("setGroup → isAgentChat() is false (groups are never agent chats)") {
        val vm = viewModel()
        vm.setGroup(MockFactory.createGroup())
        vm.isAgentChat() shouldBe false
    }

    test("switching from a BYOA user to a group resets agent chat to false") {
        val vm = viewModel()
        vm.setUser(agentUser())
        vm.isAgentChat() shouldBe true

        vm.setGroup(MockFactory.createGroup())
        vm.isAgentChat() shouldBe false
    }

    test("switching from a group to a BYOA user re-enables agent chat") {
        val vm = viewModel()
        vm.setGroup(MockFactory.createGroup())
        vm.isAgentChat() shouldBe false

        vm.setUser(agentUser())
        vm.isAgentChat() shouldBe true
    }
})
