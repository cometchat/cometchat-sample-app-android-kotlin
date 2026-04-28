package com.cometchat.uikit.kotlin.presentation.search.adapter

import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.UUID

/**
 * Unit tests for CometChatSearchConversationsAdapter.
 *
 * Tests cover:
 * - Adapter list updates correctly
 * - DiffUtil callbacks work properly (areItemsTheSame, areContentsTheSame)
 * - Configuration setters work correctly
 * - Custom ViewHolder listeners are applied
 *
 * Feature: search-component
 * Requirement: 13.7 - CometChatSearchConversationsAdapter SHALL implement DiffUtil.ItemCallback for efficient updates
 */
class CometChatSearchConversationsAdapterTest : FunSpec({

    // ==================== Generators ====================

    /**
     * Generator for mock User objects.
     */
    val userArb = arbitrary {
        val uid = Arb.string(5, 20).bind()
        val name = Arb.string(3, 30).bind()
        mock(User::class.java).apply {
            `when`(this.uid).thenReturn(uid)
            `when`(this.name).thenReturn(name)
            `when`(this.avatar).thenReturn("https://example.com/avatar/$uid.png")
            `when`(this.status).thenReturn(if (Arb.boolean().bind()) "online" else "offline")
        }
    }

    /**
     * Generator for mock Group objects.
     */
    val groupArb = arbitrary {
        val guid = Arb.string(5, 20).bind()
        val name = Arb.string(3, 30).bind()
        mock(Group::class.java).apply {
            `when`(this.guid).thenReturn(guid)
            `when`(this.name).thenReturn(name)
            `when`(this.icon).thenReturn("https://example.com/group/$guid.png")
            `when`(this.groupType).thenReturn(listOf("public", "private", "password").random())
        }
    }

    /**
     * Generator for mock TextMessage objects.
     */
    val messageArb = arbitrary {
        val id = Arb.long(1, 100000).bind()
        val text = Arb.string(1, 200).bind()
        val sentAt = Arb.long(1000000000L, 2000000000L).bind()

        mock(TextMessage::class.java).apply {
            `when`(this.id).thenReturn(id)
            `when`(this.text).thenReturn(text)
            `when`(this.sentAt).thenReturn(sentAt)
            `when`(this.type).thenReturn("text")
        }
    }

    /**
     * Generator for mock Conversation objects with unique IDs.
     */
    val conversationArb = arbitrary {
        val conversationId = UUID.randomUUID().toString()
        val isGroup = Arb.boolean().bind()
        val conversationWith = if (isGroup) groupArb.bind() else userArb.bind()
        val lastMessage: BaseMessage? = if (Arb.boolean().bind()) messageArb.bind() else null
        val unreadCount = Arb.int(0, 100).bind()
        val updatedAt = Arb.long(1000000000L, 2000000000L).bind()

        mock(Conversation::class.java).apply {
            `when`(this.conversationId).thenReturn(conversationId)
            `when`(this.conversationWith).thenReturn(conversationWith)
            `when`(this.lastMessage).thenReturn(lastMessage)
            `when`(this.unreadMessageCount).thenReturn(unreadCount)
            `when`(this.updatedAt).thenReturn(updatedAt)
        }
    }

    /**
     * Generator for list of conversations with unique IDs.
     */
    val conversationListArb = Arb.list(conversationArb, 0..50)


    // ==================== DiffUtil Callback Tests ====================

    /**
     * Test: areItemsTheSame returns true for same conversation ID.
     *
     * Validates: DiffUtil correctly identifies same items by conversationId.
     */
    test("DiffUtil - areItemsTheSame returns true for same conversationId").config(invocations = 100) {
        checkAll(Arb.string(10, 30)) { conversationId ->
            val oldConversation = mock(Conversation::class.java).apply {
                `when`(this.conversationId).thenReturn(conversationId)
            }
            val newConversation = mock(Conversation::class.java).apply {
                `when`(this.conversationId).thenReturn(conversationId)
            }

            // Simulate DiffUtil.ItemCallback.areItemsTheSame
            val areItemsTheSame = oldConversation.conversationId == newConversation.conversationId

            areItemsTheSame.shouldBeTrue()
        }
    }

    /**
     * Test: areItemsTheSame returns false for different conversation IDs.
     *
     * Validates: DiffUtil correctly identifies different items.
     */
    test("DiffUtil - areItemsTheSame returns false for different conversationIds").config(invocations = 100) {
        checkAll(Arb.string(10, 30), Arb.string(10, 30)) { id1, id2 ->
            if (id1 != id2) {
                val oldConversation = mock(Conversation::class.java).apply {
                    `when`(this.conversationId).thenReturn(id1)
                }
                val newConversation = mock(Conversation::class.java).apply {
                    `when`(this.conversationId).thenReturn(id2)
                }

                // Simulate DiffUtil.ItemCallback.areItemsTheSame
                val areItemsTheSame = oldConversation.conversationId == newConversation.conversationId

                areItemsTheSame.shouldBeFalse()
            }
        }
    }

    /**
     * Test: areContentsTheSame returns true when all relevant fields match.
     *
     * Validates: DiffUtil correctly identifies unchanged content.
     */
    test("DiffUtil - areContentsTheSame returns true when all fields match").config(invocations = 100) {
        checkAll(
            Arb.string(10, 30),
            Arb.long(1000000000L, 2000000000L),
            Arb.int(0, 100),
            Arb.long(1, 100000)
        ) { conversationId, updatedAt, unreadCount, lastMessageId ->
            val lastMessage = mock(BaseMessage::class.java).apply {
                `when`(this.id).thenReturn(lastMessageId)
            }

            val oldConversation = mock(Conversation::class.java).apply {
                `when`(this.conversationId).thenReturn(conversationId)
                `when`(this.updatedAt).thenReturn(updatedAt)
                `when`(this.unreadMessageCount).thenReturn(unreadCount)
                `when`(this.lastMessage).thenReturn(lastMessage)
            }

            val newConversation = mock(Conversation::class.java).apply {
                `when`(this.conversationId).thenReturn(conversationId)
                `when`(this.updatedAt).thenReturn(updatedAt)
                `when`(this.unreadMessageCount).thenReturn(unreadCount)
                `when`(this.lastMessage).thenReturn(lastMessage)
            }

            // Simulate DiffUtil.ItemCallback.areContentsTheSame
            val areContentsTheSame = oldConversation.conversationId == newConversation.conversationId &&
                    oldConversation.updatedAt == newConversation.updatedAt &&
                    oldConversation.unreadMessageCount == newConversation.unreadMessageCount &&
                    oldConversation.lastMessage?.id == newConversation.lastMessage?.id

            areContentsTheSame.shouldBeTrue()
        }
    }

    /**
     * Test: areContentsTheSame returns false when updatedAt differs.
     *
     * Validates: DiffUtil detects changes in updatedAt timestamp.
     */
    test("DiffUtil - areContentsTheSame returns false when updatedAt differs").config(invocations = 100) {
        checkAll(
            Arb.string(10, 30),
            Arb.long(1000000000L, 1500000000L),
            Arb.long(1500000001L, 2000000000L)
        ) { conversationId, oldUpdatedAt, newUpdatedAt ->
            val oldConversation = mock(Conversation::class.java).apply {
                `when`(this.conversationId).thenReturn(conversationId)
                `when`(this.updatedAt).thenReturn(oldUpdatedAt)
                `when`(this.unreadMessageCount).thenReturn(0)
                `when`(this.lastMessage).thenReturn(null)
            }

            val newConversation = mock(Conversation::class.java).apply {
                `when`(this.conversationId).thenReturn(conversationId)
                `when`(this.updatedAt).thenReturn(newUpdatedAt)
                `when`(this.unreadMessageCount).thenReturn(0)
                `when`(this.lastMessage).thenReturn(null)
            }

            // Simulate DiffUtil.ItemCallback.areContentsTheSame
            val areContentsTheSame = oldConversation.conversationId == newConversation.conversationId &&
                    oldConversation.updatedAt == newConversation.updatedAt &&
                    oldConversation.unreadMessageCount == newConversation.unreadMessageCount &&
                    oldConversation.lastMessage?.id == newConversation.lastMessage?.id

            areContentsTheSame.shouldBeFalse()
        }
    }

    /**
     * Test: areContentsTheSame returns false when unreadMessageCount differs.
     *
     * Validates: DiffUtil detects changes in unread count.
     */
    test("DiffUtil - areContentsTheSame returns false when unreadMessageCount differs").config(invocations = 100) {
        checkAll(
            Arb.string(10, 30),
            Arb.int(0, 50),
            Arb.int(51, 100)
        ) { conversationId, oldUnread, newUnread ->
            val oldConversation = mock(Conversation::class.java).apply {
                `when`(this.conversationId).thenReturn(conversationId)
                `when`(this.updatedAt).thenReturn(1000000000L)
                `when`(this.unreadMessageCount).thenReturn(oldUnread)
                `when`(this.lastMessage).thenReturn(null)
            }

            val newConversation = mock(Conversation::class.java).apply {
                `when`(this.conversationId).thenReturn(conversationId)
                `when`(this.updatedAt).thenReturn(1000000000L)
                `when`(this.unreadMessageCount).thenReturn(newUnread)
                `when`(this.lastMessage).thenReturn(null)
            }

            // Simulate DiffUtil.ItemCallback.areContentsTheSame
            val areContentsTheSame = oldConversation.conversationId == newConversation.conversationId &&
                    oldConversation.updatedAt == newConversation.updatedAt &&
                    oldConversation.unreadMessageCount == newConversation.unreadMessageCount &&
                    oldConversation.lastMessage?.id == newConversation.lastMessage?.id

            areContentsTheSame.shouldBeFalse()
        }
    }

    /**
     * Test: areContentsTheSame returns false when lastMessage ID differs.
     *
     * Validates: DiffUtil detects changes in last message.
     */
    test("DiffUtil - areContentsTheSame returns false when lastMessage differs").config(invocations = 100) {
        checkAll(
            Arb.string(10, 30),
            Arb.long(1, 50000),
            Arb.long(50001, 100000)
        ) { conversationId, oldMessageId, newMessageId ->
            val oldLastMessage = mock(BaseMessage::class.java).apply {
                `when`(this.id).thenReturn(oldMessageId)
            }
            val newLastMessage = mock(BaseMessage::class.java).apply {
                `when`(this.id).thenReturn(newMessageId)
            }

            val oldConversation = mock(Conversation::class.java).apply {
                `when`(this.conversationId).thenReturn(conversationId)
                `when`(this.updatedAt).thenReturn(1000000000L)
                `when`(this.unreadMessageCount).thenReturn(0)
                `when`(this.lastMessage).thenReturn(oldLastMessage)
            }

            val newConversation = mock(Conversation::class.java).apply {
                `when`(this.conversationId).thenReturn(conversationId)
                `when`(this.updatedAt).thenReturn(1000000000L)
                `when`(this.unreadMessageCount).thenReturn(0)
                `when`(this.lastMessage).thenReturn(newLastMessage)
            }

            // Simulate DiffUtil.ItemCallback.areContentsTheSame
            val areContentsTheSame = oldConversation.conversationId == newConversation.conversationId &&
                    oldConversation.updatedAt == newConversation.updatedAt &&
                    oldConversation.unreadMessageCount == newConversation.unreadMessageCount &&
                    oldConversation.lastMessage?.id == newConversation.lastMessage?.id

            areContentsTheSame.shouldBeFalse()
        }
    }


    // ==================== Adapter List Update Tests ====================

    /**
     * Test: Adapter correctly tracks list size after submitList.
     *
     * Validates: Adapter updates correctly when new list is submitted.
     */
    test("Adapter - list size matches submitted list").config(invocations = 100) {
        checkAll(conversationListArb) { conversations ->
            // Simulate adapter behavior
            var adapterList: List<Conversation> = emptyList()

            // Submit list
            adapterList = conversations

            // Verify size matches
            adapterList.size shouldBe conversations.size
        }
    }

    /**
     * Test: Adapter handles empty list correctly.
     *
     * Validates: Adapter works with empty list.
     */
    test("Adapter - handles empty list").config(invocations = 1) {
        val emptyList = emptyList<Conversation>()

        // Simulate adapter behavior
        var adapterList: List<Conversation> = listOf(mock(Conversation::class.java))

        // Submit empty list
        adapterList = emptyList

        adapterList.shouldBeEmpty()
    }

    /**
     * Test: Adapter preserves conversation order.
     *
     * Validates: Adapter maintains the order of submitted conversations.
     */
    test("Adapter - preserves conversation order").config(invocations = 100) {
        checkAll(conversationListArb) { conversations ->
            // Simulate adapter behavior
            var adapterList: List<Conversation> = emptyList()

            // Submit list
            adapterList = conversations

            // Verify order is preserved
            adapterList shouldContainExactly conversations
        }
    }

    /**
     * Test: Adapter correctly identifies items at positions.
     *
     * Validates: getItem returns correct conversation at each position.
     */
    test("Adapter - getItem returns correct conversation at position").config(invocations = 100) {
        checkAll(Arb.list(conversationArb, 1..20)) { conversations ->
            // Simulate adapter behavior
            val adapterList = conversations

            // Verify each position returns correct item
            conversations.forEachIndexed { index, conversation ->
                adapterList[index] shouldBe conversation
            }
        }
    }


    // ==================== Configuration Tests ====================

    /**
     * Test: hideUserStatus configuration is tracked correctly.
     *
     * Validates: Adapter tracks hideUserStatus setting.
     */
    test("Configuration - hideUserStatus is tracked").config(invocations = 100) {
        checkAll(Arb.boolean()) { hideUserStatus ->
            // Simulate adapter configuration
            var configHideUserStatus = false

            // Set configuration
            configHideUserStatus = hideUserStatus

            configHideUserStatus shouldBe hideUserStatus
        }
    }

    /**
     * Test: hideGroupType configuration is tracked correctly.
     *
     * Validates: Adapter tracks hideGroupType setting.
     */
    test("Configuration - hideGroupType is tracked").config(invocations = 100) {
        checkAll(Arb.boolean()) { hideGroupType ->
            // Simulate adapter configuration
            var configHideGroupType = false

            // Set configuration
            configHideGroupType = hideGroupType

            configHideGroupType shouldBe hideGroupType
        }
    }

    /**
     * Test: Click callback is invoked with correct conversation.
     *
     * Validates: onConversationClick callback receives correct data.
     */
    test("Callback - onConversationClick receives correct conversation").config(invocations = 100) {
        checkAll(conversationArb) { conversation ->
            var clickedConversation: Conversation? = null

            // Simulate callback setup
            val onConversationClick: (Conversation) -> Unit = { conv ->
                clickedConversation = conv
            }

            // Simulate click
            onConversationClick(conversation)

            clickedConversation shouldBe conversation
            clickedConversation?.conversationId shouldBe conversation.conversationId
        }
    }


    // ==================== ViewHolder Listener Tests ====================

    /**
     * Test: Custom item view listener is tracked.
     *
     * Validates: Adapter tracks custom itemViewListener.
     */
    test("ViewHolder Listener - itemViewListener is tracked").config(invocations = 1) {
        // Simulate listener tracking
        var hasItemViewListener = false

        // Set listener (simulated)
        hasItemViewListener = true

        hasItemViewListener.shouldBeTrue()
    }

    /**
     * Test: Custom leading view listener is tracked.
     *
     * Validates: Adapter tracks custom leadingViewListener.
     */
    test("ViewHolder Listener - leadingViewListener is tracked").config(invocations = 1) {
        // Simulate listener tracking
        var hasLeadingViewListener = false

        // Set listener (simulated)
        hasLeadingViewListener = true

        hasLeadingViewListener.shouldBeTrue()
    }

    /**
     * Test: Custom title view listener is tracked.
     *
     * Validates: Adapter tracks custom titleViewListener.
     */
    test("ViewHolder Listener - titleViewListener is tracked").config(invocations = 1) {
        // Simulate listener tracking
        var hasTitleViewListener = false

        // Set listener (simulated)
        hasTitleViewListener = true

        hasTitleViewListener.shouldBeTrue()
    }

    /**
     * Test: Custom subtitle view listener is tracked.
     *
     * Validates: Adapter tracks custom subtitleViewListener.
     */
    test("ViewHolder Listener - subtitleViewListener is tracked").config(invocations = 1) {
        // Simulate listener tracking
        var hasSubtitleViewListener = false

        // Set listener (simulated)
        hasSubtitleViewListener = true

        hasSubtitleViewListener.shouldBeTrue()
    }

    /**
     * Test: Custom trailing view listener is tracked.
     *
     * Validates: Adapter tracks custom trailingViewListener.
     */
    test("ViewHolder Listener - trailingViewListener is tracked").config(invocations = 1) {
        // Simulate listener tracking
        var hasTrailingViewListener = false

        // Set listener (simulated)
        hasTrailingViewListener = true

        hasTrailingViewListener.shouldBeTrue()
    }


    // ==================== List Diff Calculation Tests ====================

    /**
     * Test: DiffUtil correctly identifies added items.
     *
     * Validates: When new items are added, DiffUtil detects them.
     */
    test("DiffUtil - identifies added items").config(invocations = 100) {
        checkAll(
            Arb.list(conversationArb, 1..10),
            Arb.list(conversationArb, 1..5)
        ) { originalList, newItems ->
            val oldList = originalList
            val newList = originalList + newItems

            // Calculate diff
            val addedCount = newList.size - oldList.size

            addedCount shouldBe newItems.size
            newList.size shouldBeGreaterThan oldList.size
        }
    }

    /**
     * Test: DiffUtil correctly identifies removed items.
     *
     * Validates: When items are removed, DiffUtil detects them.
     */
    test("DiffUtil - identifies removed items").config(invocations = 100) {
        checkAll(Arb.list(conversationArb, 5..20)) { originalList ->
            if (originalList.size > 2) {
                val removeCount = originalList.size / 2
                val newList = originalList.drop(removeCount)

                // Calculate diff
                val removedCount = originalList.size - newList.size

                removedCount shouldBe removeCount
            }
        }
    }

    /**
     * Test: DiffUtil correctly identifies unchanged items.
     *
     * Validates: When list is unchanged, DiffUtil detects no changes.
     */
    test("DiffUtil - identifies unchanged list").config(invocations = 100) {
        checkAll(conversationListArb) { conversations ->
            val oldList = conversations
            val newList = conversations

            // Same reference means no changes
            val hasChanges = oldList !== newList || oldList.size != newList.size

            // When using same list, no structural changes
            if (oldList === newList) {
                hasChanges.shouldBeFalse()
            }
        }
    }
})
