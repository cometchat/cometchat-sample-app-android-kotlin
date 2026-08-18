package com.cometchat.uikit.core.viewmodel.messagelist

import android.util.Log
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.model.SurroundingMessagesResult
import com.cometchat.uikit.core.domain.repository.MessageListRepository
import com.cometchat.uikit.core.testutils.MockFactory
import com.cometchat.uikit.core.utils.getDefaultMessagesCategories
import com.cometchat.uikit.core.viewmodel.CometChatMessageListViewModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * ENG-38259 — real-time message filtering in [CometChatMessageListViewModel].
 *
 * Reported by a customer via Nisha (SMG): a custom [MessagesRequest.MessagesRequestBuilder]
 * passed to the message list was honoured on the initial fetch but ignored for messages
 * arriving over the WebSocket. Their use case is silent "ban" messages that must never be
 * rendered — they showed up anyway when delivered live.
 *
 * The bug: [CometChatMessageListViewModel.setUser] handed the builder straight to the
 * repository (fetch path only) and stored `getDefaultMessagesTypes()` /
 * `getDefaultMessagesCategories()` as the VM's own filter, so the VM never learned what the
 * caller had asked for. `handleIncomingMessage` — the WebSocket entry point — gated only on
 * conversation and thread, never on type or category.
 *
 * The fix reads the resolved filter back off the repository in setUser/setGroup and gates
 * `handleIncomingMessage` on it, so both paths enforce the same [MessagesRequest] filter.
 *
 * The repository is mocked here, so these tests stub what a real
 * `MessageListRepositoryImpl` would resolve from the builder; that resolution step is covered
 * separately in `MessageListRepositoryImplTest`.
 *
 * The listener is captured from the mocked static `CometChat.addMessageListener` (registered
 * by setUser when enableListeners=true) and driven directly, exactly as the SDK does.
 *
 * Run: ./gradlew :chatuikit-core:testDebugUnitTest --tests "*MessageListRealtimeFilterTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MessageListRealtimeFilterTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    lateinit var repository: MessageListRepository
    lateinit var logMock: MockedStatic<Log>
    lateinit var cometChatMock: MockedStatic<CometChat>

    /**
     * Builds a VM for a 1:1 chat with "test-user" starting from an empty list, with the
     * repository reporting [effectiveTypes]/[effectiveCategories] as the filter in effect, and
     * returns it with the SDK message listener it registered.
     */
    suspend fun createViewModelWithListener(
        effectiveTypes: List<String> = emptyList(),
        effectiveCategories: List<String> = emptyList()
    ): Pair<CometChatMessageListViewModel, CometChat.MessageListener> {
        whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(emptyList()))
        whenever(repository.hasMorePreviousMessages()).thenReturn(true)
        whenever(repository.getEffectiveMessagesTypes()).thenReturn(effectiveTypes)
        whenever(repository.getEffectiveMessagesCategories()).thenReturn(effectiveCategories)

        val vm = CometChatMessageListViewModel(repository = repository, enableListeners = true)
        vm.setUser(user = MockFactory.createUser(uid = "test-user", name = "Test User"))
        vm.fetchMessages()

        val listenerCaptor = argumentCaptor<CometChat.MessageListener>()
        cometChatMock.verify({ CometChat.addMessageListener(any(), listenerCaptor.capture()) }, atLeastOnce())
        return vm to listenerCaptor.lastValue
    }

    /** An incoming text message in the current 1:1 chat — category "message", type "text". */
    fun incomingText(id: Long = 1L) = MockFactory.createTextMessage(
        id = id,
        text = "should be filtered out",
        senderUid = "test-user",
        receiverId = "logged-in-user",
        receiverType = CometChatConstants.RECEIVER_TYPE_USER
    )

    /** An incoming custom message in the current 1:1 chat — category "custom", type "extension_poll". */
    fun incomingCustom(id: Long = 1L) = MockFactory.createCustomMessage(
        id = id,
        customType = "extension_poll",
        senderUid = "test-user",
        receiverId = "logged-in-user",
        receiverType = CometChatConstants.RECEIVER_TYPE_USER
    )

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        logMock = Mockito.mockStatic(Log::class.java)
        cometChatMock = Mockito.mockStatic(CometChat::class.java)
        repository = mock()
        whenever(repository.hasMorePreviousMessages()).thenReturn(true)
        whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(emptyList()))
        whenever(repository.markAsRead(any())).thenReturn(Result.success(Unit))
        whenever(repository.markAsDelivered(any())).thenReturn(Result.success(Unit))
    }

    afterTest {
        cometChatMock.close()
        logMock.close()
        Dispatchers.resetMain()
    }

    // ==================== The filter is enforced in real time ====================

    test("a real-time message outside the configured categories is not added") {
        runTest {
            // The customer's shape, reduced: custom messages only, no plain messages.
            val (vm, listener) = createViewModelWithListener(
                effectiveCategories = listOf(CometChatConstants.CATEGORY_CUSTOM)
            )
            advanceUntilIdle()

            listener.onTextMessageReceived(incomingText(id = 1L))
            advanceUntilIdle()

            vm.getItemCount() shouldBe 0
        }
    }

    test("a real-time message outside the configured types is not added") {
        runTest {
            val (vm, listener) = createViewModelWithListener(
                effectiveTypes = listOf(CometChatConstants.MESSAGE_TYPE_TEXT)
            )
            advanceUntilIdle()

            // Category is unrestricted, but "extension_poll" is not an allowed type.
            listener.onCustomMessageReceived(incomingCustom(id = 2L))
            advanceUntilIdle()

            vm.getItemCount() shouldBe 0
        }
    }

    // ==================== Allowed traffic still flows ====================

    test("a real-time message allowed by the configured filter is added") {
        runTest {
            val (vm, listener) = createViewModelWithListener(
                effectiveCategories = listOf(CometChatConstants.CATEGORY_CUSTOM)
            )
            advanceUntilIdle()

            listener.onCustomMessageReceived(incomingCustom(id = 3L))
            advanceUntilIdle()

            vm.getItemCount() shouldBe 1
            vm.getItems().single().id shouldBe 3L
        }
    }

    test("with no custom builder a real-time text message is added as normal") {
        runTest {
            // Repository reports nothing, so the VM falls back to the defaults — which allow text.
            val (vm, listener) = createViewModelWithListener()
            advanceUntilIdle()

            listener.onTextMessageReceived(incomingText(id = 4L))
            advanceUntilIdle()

            vm.getItemCount() shouldBe 1
        }
    }

    // ==================== The VM learns the filter ====================

    test("the ViewModel adopts the filter the repository resolved") {
        runTest {
            val (vm, _) = createViewModelWithListener(
                effectiveCategories = listOf(CometChatConstants.CATEGORY_CUSTOM)
            )
            advanceUntilIdle()

            vm.getCategories() shouldBe listOf(CometChatConstants.CATEGORY_CUSTOM)
        }
    }

    test("the ViewModel falls back to the defaults when no filter is reported") {
        runTest {
            val (vm, _) = createViewModelWithListener()
            advanceUntilIdle()

            vm.getCategories() shouldBe getDefaultMessagesCategories()
        }
    }

    // ==================== goToMessage realigns latestMessageId ====================
    //
    // handleIncomingMessage's isAtLatestPosition guard compares the last *rendered* message
    // against latestMessageId. fetchMessagesWithUnreadCount seeds latestMessageId from the
    // conversation, which under a filter points at a message this list excludes — leaving the
    // guard reading "user scrolled up" forever and silently dropping every live message into
    // hasMoreNewMessages. goToMessage now realigns it whenever the server reports nothing newer.

    /** Drives the VM through goToMessage(10) with the given hasMoreNext, list ending at id 10. */
    suspend fun createViewModelAtGoToMessage(
        hasMoreNext: Boolean
    ): Pair<CometChatMessageListViewModel, CometChat.MessageListener> {
        val (vm, listener) = createViewModelWithListener()

        val older = incomingText(id = 9L)
        val target = incomingText(id = 10L)
        whenever(repository.getMessage(10L)).thenReturn(Result.success(target))
        whenever(repository.fetchSurroundingMessages(10L)).thenReturn(
            Result.success(
                SurroundingMessagesResult(
                    olderMessages = listOf(older),
                    targetMessage = target,
                    newerMessages = emptyList(),
                    hasMorePrevious = true,
                    hasMoreNext = hasMoreNext
                )
            )
        )

        vm.goToMessage(10L)
        return vm to listener
    }

    test("after goToMessage with nothing newer, a live message still reaches the list") {
        runTest {
            val (vm, listener) = createViewModelAtGoToMessage(hasMoreNext = false)
            advanceUntilIdle()
            vm.getItemCount() shouldBe 2

            listener.onTextMessageReceived(incomingText(id = 11L))
            advanceUntilIdle()

            // latestMessageId was realigned to the loaded tail (10), so the guard lets 11 in.
            vm.getItemCount() shouldBe 3
            vm.getItems().last().id shouldBe 11L
        }
    }

    test("after goToMessage with more newer messages, a live message is held back") {
        runTest {
            val (vm, listener) = createViewModelAtGoToMessage(hasMoreNext = true)
            advanceUntilIdle()
            vm.getItemCount() shouldBe 2

            listener.onTextMessageReceived(incomingText(id = 11L))
            advanceUntilIdle()

            // Genuinely not at the latest position — appending would render it out of order.
            vm.getItemCount() shouldBe 2
            vm.hasMoreNewMessages.value shouldBe true
        }
    }

    // ==================== Agent chat is exempt from the filter ====================

    test("an agent chat ignores the configured filter") {
        runTest {
            // Filter allows only "message"; the agentic message below is excluded by it.
            whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(emptyList()))
            whenever(repository.hasMorePreviousMessages()).thenReturn(true)
            whenever(repository.getEffectiveMessagesTypes()).thenReturn(emptyList())
            whenever(repository.getEffectiveMessagesCategories())
                .thenReturn(listOf(CometChatConstants.CATEGORY_MESSAGE))

            val agent = MockFactory.createUser(uid = "test-user", name = "Agent")
            whenever(agent.role).thenReturn(UIKitConstants.AIConstants.AGENTIC_USER)

            val vm = CometChatMessageListViewModel(repository = repository, enableListeners = true)
            vm.setUser(user = agent)
            vm.fetchMessages()

            val captor = argumentCaptor<CometChat.MessageListener>()
            cometChatMock.verify({ CometChat.addMessageListener(any(), captor.capture()) }, atLeastOnce())
            advanceUntilIdle()

            val agentic = incomingCustom(id = 20L)
            whenever(agentic.category).thenReturn(UIKitConstants.MessageCategory.AGENTIC)

            // Agent chats also register the AI stream service's own listener, so take the
            // ViewModel's — the first one registered — not the last.
            captor.firstValue.onCustomMessageReceived(agentic)
            advanceUntilIdle()

            // Streaming swaps placeholders in and out of the list and has no fetch filter to
            // match, so matchesConfiguredFilter short-circuits for agent chats.
            vm.getItemCount() shouldBe 1
        }
    }
})
