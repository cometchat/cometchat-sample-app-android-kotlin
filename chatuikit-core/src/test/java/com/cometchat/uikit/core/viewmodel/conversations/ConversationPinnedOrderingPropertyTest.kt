package com.cometchat.uikit.core.viewmodel.conversations

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Property-based tests for pin-aware ordering in the conversation list.
 *
 * Since CometChatConversationsViewModel depends on SDK classes with private constructors,
 * we test the core behavior using a test ViewModel that mirrors the production implementation
 * of updateConversation() / unpinConversation():
 * - Pinned conversations form a block at the head of the list and are never displaced by
 *   realtime activity (new/updated conversations surface at the top of the unpinned section).
 * - A pinned conversation receiving a message updates in place, keeping its slot and its pin
 *   attributes (conversations built from messages carry no per-user pin state).
 * - Unpinning moves the conversation to the top of the unpinned section.
 */
class ConversationPinnedOrderingPropertyTest : FunSpec({

    // ==================== Test Data Classes ====================

    /**
     * Test data class that simulates Conversation, including the SDK's pin attributes where
     * isPinned is derived as pinnedAt > 0.
     */
    data class TestConversation(
        val conversationId: String,
        var pinnedAt: Long = 0L,
        var pinnedBy: String? = null,
        var unreadMessageCount: Int = 0,
        var lastMessageId: Long = 0L
    ) {
        val isPinned: Boolean get() = pinnedAt > 0
        fun clone(): TestConversation = copy()
    }

    /**
     * Test ViewModel mirroring the production pin-aware ordering in
     * CometChatConversationsViewModel.updateConversation() and unpinConversation().
     */
    class TestConversationListViewModel {
        private val _conversations = MutableStateFlow<List<TestConversation>>(emptyList())
        val conversations = _conversations
        var scrollToTopEmitted = false
            private set

        fun setConversations(list: List<TestConversation>) {
            _conversations.value = list
        }

        private fun firstUnpinnedIndex(list: List<TestConversation>): Int {
            val index = list.indexOfFirst { !it.isPinned }
            return if (index >= 0) index else list.size
        }

        /**
         * Mirrors updateConversation() for a received message. The incoming conversation is
         * built from the message (CometChatHelper.getConversationFromMessage), so it never
         * carries pin attributes — production copies them from the old list entry.
         */
        fun onMessageReceived(conversationId: String, messageId: Long) {
            val incoming = TestConversation(conversationId = conversationId, lastMessageId = messageId)
            val currentList = _conversations.value
            val existingIndex = currentList.indexOfFirst { it.conversationId == incoming.conversationId }

            if (existingIndex >= 0) {
                val oldConversation = currentList[existingIndex]
                val updatedConversation = incoming.clone().apply {
                    pinnedAt = oldConversation.pinnedAt
                    pinnedBy = oldConversation.pinnedBy
                    unreadMessageCount = oldConversation.unreadMessageCount + 1
                }
                val newList = currentList.toMutableList().apply {
                    removeAt(existingIndex)
                    val targetIndex =
                        if (updatedConversation.isPinned) existingIndex else firstUnpinnedIndex(this)
                    add(targetIndex, updatedConversation)
                }
                _conversations.value = newList
                if (!updatedConversation.isPinned) scrollToTopEmitted = true
            } else {
                val updatedConversation = incoming.clone().apply { unreadMessageCount = 1 }
                val newList = currentList.toMutableList().apply {
                    add(firstUnpinnedIndex(this), updatedConversation)
                }
                _conversations.value = newList
                scrollToTopEmitted = true
            }
        }

        /**
         * Mirrors unpinConversation() success: pin attributes cleared and the conversation moved
         * to the top of the unpinned section.
         */
        fun unpinConversation(conversationId: String) {
            val list = _conversations.value.toMutableList()
            val idx = list.indexOfFirst { it.conversationId == conversationId }
            if (idx >= 0) {
                val updated = list.removeAt(idx).copy(pinnedAt = 0L, pinnedBy = null)
                list.add(firstUnpinnedIndex(list), updated)
                _conversations.value = list
            }
        }
    }

    // ==================== Generators ====================

    /**
     * Generator for a list with a pinned block at the head followed by unpinned conversations,
     * matching the backend's fetch ordering. Pinned IDs are "pinned_i", unpinned "conv_i".
     */
    fun pinnedListArb(maxPinned: Int = 4, maxUnpinned: Int = 6): Arb<List<TestConversation>> {
        return Arb.bind(Arb.int(0, maxPinned), Arb.int(1, maxUnpinned)) { pinnedCount, unpinnedCount ->
            val pinned = (0 until pinnedCount).map { i ->
                TestConversation(
                    conversationId = "pinned_$i",
                    pinnedAt = 1000L - i,
                    pinnedBy = "me"
                )
            }
            val unpinned = (0 until unpinnedCount).map { i ->
                TestConversation(conversationId = "conv_$i")
            }
            pinned + unpinned
        }
    }

    /** Returns true when no unpinned conversation appears above any pinned one. */
    fun pinnedBlockIntact(list: List<TestConversation>): Boolean {
        val firstUnpinned = list.indexOfFirst { !it.isPinned }
        return firstUnpinned < 0 || list.drop(firstUnpinned).none { it.isPinned }
    }

    // ==================== Property Tests ====================

    /**
     * Property 1: A message on an unpinned conversation surfaces it at the top of the
     * unpinned section — directly below the pinned block, never above it.
     */
    test("Property 1: message on unpinned conversation lands below the pinned block").config(invocations = 20) {
        checkAll(pinnedListArb(), Arb.int(0, 100)) { conversations, pick ->
            val unpinned = conversations.filter { !it.isPinned }
            val target = unpinned[pick % unpinned.size]
            val pinnedIds = conversations.filter { it.isPinned }.map { it.conversationId }

            val viewModel = TestConversationListViewModel()
            viewModel.setConversations(conversations)
            viewModel.onMessageReceived(target.conversationId, messageId = 999L)

            val result = viewModel.conversations.value
            // Pinned block untouched, target right below it
            result.take(pinnedIds.size).map { it.conversationId } shouldBe pinnedIds
            result[pinnedIds.size].conversationId shouldBe target.conversationId
            pinnedBlockIntact(result) shouldBe true
            viewModel.scrollToTopEmitted shouldBe true
        }
    }

    /**
     * Property 2: A message on a pinned conversation updates it in place — same index, pin
     * attributes retained even though the incoming conversation carries no pin state — and
     * does not emit scroll-to-top.
     */
    test("Property 2: message on pinned conversation keeps its slot and pin state").config(invocations = 20) {
        checkAll(pinnedListArb(maxPinned = 4), Arb.int(0, 100)) { conversations, pick ->
            val pinned = conversations.filter { it.isPinned }
            if (pinned.isNotEmpty()) {
                val target = pinned[pick % pinned.size]
                val targetIndex = conversations.indexOfFirst { it.conversationId == target.conversationId }

                val viewModel = TestConversationListViewModel()
                viewModel.setConversations(conversations)
                viewModel.onMessageReceived(target.conversationId, messageId = 999L)

                val result = viewModel.conversations.value
                result.map { it.conversationId } shouldBe conversations.map { it.conversationId }
                result[targetIndex].isPinned shouldBe true
                result[targetIndex].pinnedAt shouldBe target.pinnedAt
                result[targetIndex].pinnedBy shouldBe target.pinnedBy
                result[targetIndex].lastMessageId shouldBe 999L
                viewModel.scrollToTopEmitted shouldBe false
            }
        }
    }

    /**
     * Property 3: A message for a conversation not yet in the list inserts it at the top of
     * the unpinned section, below the pinned block.
     */
    test("Property 3: new conversation is inserted below the pinned block").config(invocations = 20) {
        checkAll(pinnedListArb()) { conversations ->
            val pinnedIds = conversations.filter { it.isPinned }.map { it.conversationId }

            val viewModel = TestConversationListViewModel()
            viewModel.setConversations(conversations)
            viewModel.onMessageReceived("brand_new_conv", messageId = 1L)

            val result = viewModel.conversations.value
            result.size shouldBe conversations.size + 1
            result.take(pinnedIds.size).map { it.conversationId } shouldBe pinnedIds
            result[pinnedIds.size].conversationId shouldBe "brand_new_conv"
            result[pinnedIds.size].unreadMessageCount shouldBe 1
        }
    }

    /**
     * Property 4: Invariant — after any sequence of message events (existing or new
     * conversations), no unpinned conversation ever sits above a pinned one.
     */
    test("Property 4: pinned block invariant holds across arbitrary message sequences").config(invocations = 20) {
        checkAll(pinnedListArb(), Arb.int(1, 15)) { conversations, eventCount ->
            val viewModel = TestConversationListViewModel()
            viewModel.setConversations(conversations)

            (0 until eventCount).forEach { i ->
                val current = viewModel.conversations.value
                // Alternate between messages on existing conversations and brand-new ones
                val targetId = if (i % 3 == 2) "new_conv_$i"
                else current[i % current.size].conversationId
                viewModel.onMessageReceived(targetId, messageId = i.toLong())
            }

            pinnedBlockIntact(viewModel.conversations.value) shouldBe true
        }
    }

    /**
     * Property 5: Unpinning moves the conversation to the top of the unpinned section and the
     * pinned block invariant still holds.
     */
    test("Property 5: unpin moves conversation to top of unpinned section").config(invocations = 20) {
        checkAll(pinnedListArb(maxPinned = 4), Arb.int(0, 100)) { conversations, pick ->
            val pinned = conversations.filter { it.isPinned }
            if (pinned.isNotEmpty()) {
                val target = pinned[pick % pinned.size]

                val viewModel = TestConversationListViewModel()
                viewModel.setConversations(conversations)
                viewModel.unpinConversation(target.conversationId)

                val result = viewModel.conversations.value
                val remainingPinnedCount = pinned.size - 1
                result[remainingPinnedCount].conversationId shouldBe target.conversationId
                result[remainingPinnedCount].isPinned shouldBe false
                pinnedBlockIntact(result) shouldBe true
            }
        }
    }
})
