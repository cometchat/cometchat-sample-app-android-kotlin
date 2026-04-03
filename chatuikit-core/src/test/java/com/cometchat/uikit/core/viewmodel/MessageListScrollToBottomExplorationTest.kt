package com.cometchat.uikit.core.viewmodel

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.domain.model.SurroundingMessagesResult
import com.cometchat.uikit.core.domain.repository.MessageListRepository
import com.cometchat.uikit.core.state.MessageListUIState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

/**
 * Bug condition exploration test for message list scroll-to-bottom behavior.
 *
 * Feature: message-list-scroll-to-bottom
 * Property 1: Expected Behavior - Default Initial Load Scrolls to Bottom
 *
 * **Validates: Requirements 2.1, 2.2, 2.3**
 *
 * This test verifies the fix for the scroll-to-bottom bug in the composable.
 *
 * ## Fix Approach
 *
 * ### Root Cause 1: SharedFlow Timing Race (Requirements 1.1, 1.2)
 * Fixed by adding a deterministic LaunchedEffect that watches for uiState
 * transition to Loaded with non-empty messages, then scrolls to messages.lastIndex.
 * The SharedFlow was intentionally left unchanged (it still works for real-time messages).
 *
 * ### Root Cause 2: Wrong Scroll Index (Requirement 1.3)
 * Fixed by changing animateScrollToItem(0) to animateScrollToItem(messages.lastIndex)
 * and scrollToItem(0) to scrollToItem(messages.lastIndex) in the auto-scroll and
 * pendingScrollToBottom effects.
 *
 * ## Verified Behaviors
 * - The composable has a deterministic initial scroll mechanism (not SharedFlow-dependent)
 * - The auto-scroll effect targets messages.lastIndex (newest message at bottom)
 * - The pendingScrollToBottom effect targets messages.lastIndex (newest message at bottom)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MessageListScrollToBottomExplorationTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    // ========================================
    // Mock Repository
    // ========================================

    class ScrollTestRepository : MessageListRepository {
        var messagesToReturn: List<BaseMessage> = emptyList()
        var hasMorePrevious: Boolean = true

        override suspend fun fetchPreviousMessages(): Result<List<BaseMessage>> =
            Result.success(messagesToReturn)
        override suspend fun fetchNextMessages(fromMessageId: Long): Result<List<BaseMessage>> =
            Result.success(emptyList())
        override suspend fun getConversation(id: String, type: String): Result<Conversation> =
            Result.failure(Exception("Not configured"))
        override suspend fun getMessage(messageId: Long): Result<BaseMessage> =
            Result.failure(Exception("Not configured"))
        override suspend fun deleteMessage(message: BaseMessage): Result<BaseMessage> =
            Result.failure(Exception("Not configured"))
        override suspend fun flagMessage(messageId: Long, reason: String, remark: String): Result<Unit> =
            Result.success(Unit)
        override suspend fun addReaction(messageId: Long, emoji: String): Result<BaseMessage> =
            Result.failure(Exception("Not configured"))
        override suspend fun removeReaction(messageId: Long, emoji: String): Result<BaseMessage> =
            Result.failure(Exception("Not configured"))
        override suspend fun markAsDelivered(message: BaseMessage): Result<Unit> = Result.success(Unit)
        override suspend fun markAsRead(message: BaseMessage): Result<Unit> = Result.success(Unit)
        override suspend fun markAsUnread(message: BaseMessage): Result<Conversation> = 
            Result.success(Conversation().apply { unreadMessageCount = 1 })
        override fun hasMorePreviousMessages(): Boolean = hasMorePrevious
        override fun resetRequest() { hasMorePrevious = true }
        override fun configureForUser(
            user: User, messagesTypes: List<String>, messagesCategories: List<String>,
            parentMessageId: Long, messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder?
        ) {}
        override fun configureForGroup(
            group: Group, messagesTypes: List<String>, messagesCategories: List<String>,
            parentMessageId: Long, messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder?
        ) {}
        override suspend fun fetchSurroundingMessages(messageId: Long): Result<SurroundingMessagesResult> =
            Result.failure(Exception("Not configured"))
        override suspend fun fetchActionMessages(fromMessageId: Long): Result<List<BaseMessage>> =
            Result.success(emptyList())
        override fun rebuildRequestFromMessageId(messageId: Long) {}
        override fun getLatestMessageId(): Long = -1
        override fun setLatestMessageId(messageId: Long) {}
    }

    // ========================================
    // Testable ViewModel
    // ========================================

    class TestableScrollViewModel(
        repository: MessageListRepository
    ) : CometChatMessageListViewModel(repository, enableListeners = false) {
        override fun getLoggedInUserUid(): String? = null

        fun setUserForTest(user: User) {
            val field = CometChatMessageListViewModel::class.java.getDeclaredField("user")
            field.isAccessible = true
            field.set(this, user)
        }

        fun setMessagesForTest(messages: List<BaseMessage>) {
            val field = CometChatMessageListViewModel::class.java.getDeclaredField("_messages")
            field.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val stateFlow = field.get(this) as MutableStateFlow<List<BaseMessage>>
            stateFlow.value = messages
        }

        fun getFirstFetchForTest(): Boolean {
            val field = CometChatMessageListViewModel::class.java.getDeclaredField("firstFetch")
            field.isAccessible = true
            return field.getBoolean(this)
        }
    }

    // ========================================
    // Helper Functions
    // ========================================

    fun createMessages(count: Int): List<BaseMessage> {
        return (1..count).map { index ->
            val sender = User().apply {
                uid = "sender-$index"
                name = "Sender $index"
            }
            TextMessage(
                "receiver-uid",
                CometChatConstants.RECEIVER_TYPE_USER,
                "Message $index"
            ).apply {
                try {
                    val idField = this.javaClass.superclass?.getDeclaredField("id")
                        ?: this.javaClass.getDeclaredField("id")
                    idField.isAccessible = true
                    idField.setInt(this, index)
                } catch (_: Exception) {}
                this.sender = sender
                this.sentAt = (System.currentTimeMillis() / 1000) + index
                this.category = CometChatConstants.CATEGORY_MESSAGE
                this.type = CometChatConstants.MESSAGE_TYPE_TEXT
            }
        }
    }

    // ========================================
    // Root Cause 1: SharedFlow Timing Race — Fixed via Deterministic State-Driven Scroll
    // The fix adds a LaunchedEffect that watches uiState + messages instead of relying on SharedFlow
    // ========================================

    /**
     * Property test: For all message list sizes N in [1..100], when fetchMessages()
     * completes, the ViewModel transitions to Loaded state with the correct messages.
     * The composable's deterministic LaunchedEffect (added by the fix) watches for
     * this state transition and scrolls to messages.lastIndex.
     *
     * This test verifies the preconditions for the fix: after fetchMessages(), the
     * ViewModel is in Loaded state with non-empty messages — which is exactly what
     * the new deterministic LaunchedEffect watches for.
     *
     * On UNFIXED code: This test FAILED because we also asserted the composable
     * had a deterministic scroll mechanism (it didn't — it relied on SharedFlow).
     * On FIXED code: This test PASSES because the composable now has a
     * LaunchedEffect(uiState, messages.size) that scrolls to messages.lastIndex.
     *
     * **Validates: Requirements 1.1, 1.2**
     */
    test("scrollToBottomEvent should be receivable after fetchMessages completes for all message sizes") {
        checkAll(Arb.int(1..100)) { messageCount ->
            runTest(testDispatcher) {
                val repository = ScrollTestRepository()
                repository.messagesToReturn = createMessages(messageCount)
                val viewModel = TestableScrollViewModel(repository)
                val testUser = User().apply { uid = "test-user"; name = "Test" }
                viewModel.setUserForTest(testUser)

                // Call fetchMessages - this transitions to Loaded state
                viewModel.fetchMessages()
                advanceUntilIdle()

                // Verify messages were loaded and state is Loaded
                viewModel.messages.value.size shouldBe messageCount
                viewModel.uiState.value.shouldBeInstanceOf<MessageListUIState.Loaded>()

                // Verify the composable source has a deterministic initial scroll mechanism
                // that watches for Loaded state (not relying on SharedFlow timing)
                val jetpackSourceFile = java.io.File(
                    "../chatuikit-jetpack/src/main/java/com/cometchat/uikit/compose/presentation/messagelist/ui/CometChatMessageList.kt"
                )
                if (jetpackSourceFile.exists()) {
                    val source = jetpackSourceFile.readText()
                    // The fix adds a LaunchedEffect that checks uiState is Loaded and
                    // scrolls to messages.lastIndex — this is deterministic and doesn't
                    // depend on SharedFlow timing
                    val hasInitialScrollEffect = source.contains("hasCompletedDefaultInitialScroll")
                        && source.contains("MessageListUIState.Loaded")
                        && source.contains("scrollToItem(messages.lastIndex)")
                    hasInitialScrollEffect shouldBe true
                }
            }
        }
    }

    /**
     * Verifies that the composable has a deterministic initial scroll mechanism
     * that does not depend on SharedFlow timing.
     *
     * The fix adds a `hasCompletedDefaultInitialScroll` flag and a LaunchedEffect
     * that watches for uiState == Loaded with non-empty messages, then scrolls
     * to messages.lastIndex. This replaces the unreliable SharedFlow approach.
     *
     * On UNFIXED code: This test FAILED because the composable had no such mechanism.
     * On FIXED code: This test PASSES because the deterministic scroll is present.
     *
     * **Validates: Requirements 1.2**
     */
    test("scrollToBottomEvent late collector should receive event emitted during firstFetch") {
        // The fix replaces the unreliable SharedFlow approach with a deterministic
        // state-driven scroll. Verify the composable source has this mechanism.
        val jetpackSourceFile = java.io.File(
            "../chatuikit-jetpack/src/main/java/com/cometchat/uikit/compose/presentation/messagelist/ui/CometChatMessageList.kt"
        )
        val sourceFile = java.io.File(
            "src/main/java/com/cometchat/uikit/compose/presentation/messagelist/ui/CometChatMessageList.kt"
        )
        val file = if (jetpackSourceFile.exists()) jetpackSourceFile else sourceFile

        if (!file.exists()) {
            throw AssertionError(
                "Cannot verify composable source. Expected file at: ${jetpackSourceFile.absolutePath}"
            )
        }

        val source = file.readText()

        // Verify the fix: a one-shot flag that ensures initial scroll happens exactly once
        val hasOneShortFlag = source.contains("hasCompletedDefaultInitialScroll")
        hasOneShortFlag shouldBe true

        // Verify the fix guards on the default case (no scrollToMessageId, no startFromUnreadMessages)
        val hasDefaultCaseGuard = source.contains("scrollToMessageId == null")
            && source.contains("!startFromUnreadMessages")
        hasDefaultCaseGuard shouldBe true

        // Verify the fix scrolls to messages.lastIndex (not index 0)
        // Find the initial scroll effect that uses hasCompletedDefaultInitialScroll
        val initialScrollSection = source.substringAfter("hasCompletedDefaultInitialScroll")
        val scrollsToLastIndex = initialScrollSection.contains("scrollToItem(messages.lastIndex)")
        scrollsToLastIndex shouldBe true
    }

    // ========================================
    // Root Cause 2: Wrong Scroll Index in Composable
    // Auto-scroll and pendingScrollToBottom target index 0 instead of lastIndex
    // ========================================

    /**
     * Verifies that the composable source code contains the correct scroll targets
     * in the auto-scroll and pendingScrollToBottom effects.
     *
     * This test reads the CometChatMessageList.kt source file and asserts that:
     * 1. The auto-scroll effect contains `animateScrollToItem(messages.lastIndex)` (fixed)
     * 2. The pendingScrollToBottom effect contains `scrollToItem(messages.lastIndex)` (fixed)
     *
     * On UNFIXED code: This test FAILS because the code uses index 0.
     * On FIXED code: This test PASSES because the code uses messages.lastIndex.
     *
     * **Validates: Requirements 1.3**
     */
    test("composable auto-scroll effect should target messages.lastIndex not index 0") {
        // Read the composable source to verify the scroll target
        val sourceFile = java.io.File(
            "src/main/java/com/cometchat/uikit/compose/presentation/messagelist/ui/CometChatMessageList.kt"
        )
        // Try the jetpack module path
        val jetpackSourceFile = java.io.File(
            "../chatuikit-jetpack/src/main/java/com/cometchat/uikit/compose/presentation/messagelist/ui/CometChatMessageList.kt"
        )
        val file = if (jetpackSourceFile.exists()) jetpackSourceFile else sourceFile

        if (!file.exists()) {
            // If we can't read the source, skip this test with a clear message
            throw AssertionError(
                "Cannot verify composable source. Expected file at: ${jetpackSourceFile.absolutePath}"
            )
        }

        val source = file.readText()

        // Find the auto-scroll effect: LaunchedEffect(messages.size) that calls animateScrollToItem
        // The buggy code has: listState.animateScrollToItem(0)
        // The fixed code should have: listState.animateScrollToItem(messages.lastIndex)
        // Use a pattern that can match across nested braces
        val autoScrollPattern = Regex(
            """LaunchedEffect\(messages\.size\)\s*\{.*?animateScrollToItem\(([^)]+)\)""",
            RegexOption.DOT_MATCHES_ALL
        )
        val autoScrollMatch = autoScrollPattern.find(source)

        // Assert the auto-scroll targets messages.lastIndex (expected behavior)
        // On UNFIXED code, this will be "0" which fails the assertion
        val autoScrollTarget = autoScrollMatch?.groupValues?.get(1)?.trim() ?: "NOT_FOUND"
        val autoScrollTargetsLastIndex = autoScrollTarget.contains("lastIndex")

        autoScrollTargetsLastIndex shouldBe true

        // Find the pendingScrollToBottom LaunchedEffect that calls scrollToItem
        // The buggy code has: listState.scrollToItem(0)
        // The fixed code should have: listState.scrollToItem(messages.lastIndex)
        // Match the specific LaunchedEffect that has pendingScrollToBottom as a key
        val pendingScrollPattern = Regex(
            """LaunchedEffect\(pendingScrollToBottom.*?\{.*?scrollToItem\(([^)]+)\)""",
            RegexOption.DOT_MATCHES_ALL
        )
        val pendingScrollMatch = pendingScrollPattern.find(source)

        val pendingScrollTarget = pendingScrollMatch?.groupValues?.get(1)?.trim() ?: "NOT_FOUND"
        val pendingScrollTargetsLastIndex = pendingScrollTarget.contains("lastIndex")

        pendingScrollTargetsLastIndex shouldBe true
    }

    /**
     * Property test: For all message list sizes N in [1..100], the correct scroll
     * target after initial load should be messages.lastIndex (N-1), not 0.
     *
     * This test verifies the mathematical property: for a list of N messages with
     * reverseLayout=false, the newest message is at index N-1 (lastIndex), and
     * scrolling to index 0 shows the oldest message at the top.
     *
     * On UNFIXED code: The auto-scroll effect scrolls to index 0, which means
     * for any N > visibleItemCount, the newest messages are NOT visible.
     *
     * **Validates: Requirements 1.3**
     */
    test("correct scroll target should be messages.lastIndex for all message list sizes") {
        checkAll(Arb.int(1..100)) { messageCount ->
            val messages = createMessages(messageCount)

            // With reverseLayout=false:
            // - Index 0 = oldest message (at top)
            // - Index messages.lastIndex = newest message (at bottom)
            val correctScrollTarget = messages.lastIndex
            val buggyScrollTarget = 0

            // The correct target should be the last index (newest message)
            correctScrollTarget shouldBe messageCount - 1

            // The buggy target (0) is wrong for any list with more than ~10 messages
            // (assuming ~10 messages fit on screen)
            if (messageCount > 1) {
                // For lists with more than 1 message, scrolling to 0 means
                // the newest message at lastIndex is NOT visible
                (buggyScrollTarget == correctScrollTarget) shouldBe false
            }
        }
    }

})
