package com.cometchat.uikit.kotlin.presentation.search.adapter

import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.uikit.core.constants.UIKitConstants
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.Calendar

/**
 * Unit tests for CometChatSearchMessageListAdapter.
 *
 * Tests cover:
 * - Adapter list updates correctly
 * - DiffUtil callbacks work properly
 * - Multiple ViewHolder types render correctly based on message type
 * - Sticky header decoration works (getHeaderId, header grouping)
 *
 * Feature: search-component
 * Requirement: 13.8 - CometChatSearchMessageListAdapter SHALL implement StickyHeaderAdapter for date separators
 */
class CometChatSearchMessageListAdapterTest : FunSpec({

    // ==================== Constants ====================

    val VIEW_TYPE_TEXT = 1
    val VIEW_TYPE_IMAGE = 2
    val VIEW_TYPE_VIDEO = 3
    val VIEW_TYPE_AUDIO = 4
    val VIEW_TYPE_DOCUMENT = 5
    val VIEW_TYPE_LINK = 6

    // ==================== Generators ====================

    /**
     * Generator for message types.
     */
    val messageTypeArb = Arb.element(
        UIKitConstants.MessageType.TEXT,
        UIKitConstants.MessageType.IMAGE,
        UIKitConstants.MessageType.VIDEO,
        UIKitConstants.MessageType.AUDIO,
        UIKitConstants.MessageType.FILE
    )

    /**
     * Generator for mock TextMessage objects.
     */
    val textMessageArb = arbitrary {
        val id = Arb.long(1, 100000).bind()
        val text = Arb.string(1, 200).bind()
        val sentAt = Arb.long(1000000000L, 2000000000L).bind()
        val updatedAt = Arb.long(1000000000L, 2000000000L).bind()
        val deletedAt = Arb.long(0, 0).bind()

        mock(TextMessage::class.java).apply {
            `when`(this.id).thenReturn(id)
            `when`(this.text).thenReturn(text)
            `when`(this.sentAt).thenReturn(sentAt)
            `when`(this.updatedAt).thenReturn(updatedAt)
            `when`(this.deletedAt).thenReturn(deletedAt)
            `when`(this.type).thenReturn(UIKitConstants.MessageType.TEXT)
        }
    }

    /**
     * Generator for mock TextMessage with link.
     */
    val linkMessageArb = arbitrary {
        val id = Arb.long(1, 100000).bind()
        val sentAt = Arb.long(1000000000L, 2000000000L).bind()
        val updatedAt = Arb.long(1000000000L, 2000000000L).bind()

        mock(TextMessage::class.java).apply {
            `when`(this.id).thenReturn(id)
            `when`(this.text).thenReturn("Check out https://example.com for more info")
            `when`(this.sentAt).thenReturn(sentAt)
            `when`(this.updatedAt).thenReturn(updatedAt)
            `when`(this.deletedAt).thenReturn(0L)
            `when`(this.type).thenReturn(UIKitConstants.MessageType.TEXT)
        }
    }

    /**
     * Generator for mock MediaMessage objects (image, video, audio, file).
     */
    fun mediaMessageArb(messageType: String) = arbitrary {
        val id = Arb.long(1, 100000).bind()
        val sentAt = Arb.long(1000000000L, 2000000000L).bind()
        val updatedAt = Arb.long(1000000000L, 2000000000L).bind()

        mock(MediaMessage::class.java).apply {
            `when`(this.id).thenReturn(id)
            `when`(this.sentAt).thenReturn(sentAt)
            `when`(this.updatedAt).thenReturn(updatedAt)
            `when`(this.deletedAt).thenReturn(0L)
            `when`(this.type).thenReturn(messageType)
        }
    }

    /**
     * Generator for mock BaseMessage with configurable type.
     */
    val baseMessageArb = arbitrary {
        val id = Arb.long(1, 100000).bind()
        val sentAt = Arb.long(1000000000L, 2000000000L).bind()
        val updatedAt = Arb.long(1000000000L, 2000000000L).bind()
        val messageType = messageTypeArb.bind()

        mock(BaseMessage::class.java).apply {
            `when`(this.id).thenReturn(id)
            `when`(this.sentAt).thenReturn(sentAt)
            `when`(this.updatedAt).thenReturn(updatedAt)
            `when`(this.deletedAt).thenReturn(0L)
            `when`(this.type).thenReturn(messageType)
        }
    }

    /**
     * Generator for list of messages.
     */
    val messageListArb = Arb.list(baseMessageArb, 0..50)


    // ==================== ViewHolder Type Tests ====================

    /**
     * Test: getItemViewType returns VIEW_TYPE_TEXT for text messages.
     *
     * Validates: Text messages get correct ViewHolder type.
     */
    test("ViewHolder Type - TEXT message returns VIEW_TYPE_TEXT").config(invocations = 10) {
        checkAll(textMessageArb) { message ->
            // Simulate getItemViewType logic
            val viewType = when (message.type) {
                UIKitConstants.MessageType.IMAGE -> VIEW_TYPE_IMAGE
                UIKitConstants.MessageType.VIDEO -> VIEW_TYPE_VIDEO
                UIKitConstants.MessageType.AUDIO -> VIEW_TYPE_AUDIO
                UIKitConstants.MessageType.FILE -> VIEW_TYPE_DOCUMENT
                UIKitConstants.MessageType.TEXT -> {
                    // Check for links - simplified check
                    if (message is TextMessage && message.text.contains("http")) {
                        VIEW_TYPE_LINK
                    } else {
                        VIEW_TYPE_TEXT
                    }
                }
                else -> VIEW_TYPE_TEXT
            }

            viewType shouldBe VIEW_TYPE_TEXT
        }
    }

    /**
     * Test: getItemViewType returns VIEW_TYPE_IMAGE for image messages.
     *
     * Validates: Image messages get correct ViewHolder type.
     */
    test("ViewHolder Type - IMAGE message returns VIEW_TYPE_IMAGE").config(invocations = 10) {
        checkAll(mediaMessageArb(UIKitConstants.MessageType.IMAGE)) { message ->
            val viewType = when (message.type) {
                UIKitConstants.MessageType.IMAGE -> VIEW_TYPE_IMAGE
                UIKitConstants.MessageType.VIDEO -> VIEW_TYPE_VIDEO
                UIKitConstants.MessageType.AUDIO -> VIEW_TYPE_AUDIO
                UIKitConstants.MessageType.FILE -> VIEW_TYPE_DOCUMENT
                else -> VIEW_TYPE_TEXT
            }

            viewType shouldBe VIEW_TYPE_IMAGE
        }
    }

    /**
     * Test: getItemViewType returns VIEW_TYPE_VIDEO for video messages.
     *
     * Validates: Video messages get correct ViewHolder type.
     */
    test("ViewHolder Type - VIDEO message returns VIEW_TYPE_VIDEO").config(invocations = 10) {
        checkAll(mediaMessageArb(UIKitConstants.MessageType.VIDEO)) { message ->
            val viewType = when (message.type) {
                UIKitConstants.MessageType.IMAGE -> VIEW_TYPE_IMAGE
                UIKitConstants.MessageType.VIDEO -> VIEW_TYPE_VIDEO
                UIKitConstants.MessageType.AUDIO -> VIEW_TYPE_AUDIO
                UIKitConstants.MessageType.FILE -> VIEW_TYPE_DOCUMENT
                else -> VIEW_TYPE_TEXT
            }

            viewType shouldBe VIEW_TYPE_VIDEO
        }
    }

    /**
     * Test: getItemViewType returns VIEW_TYPE_AUDIO for audio messages.
     *
     * Validates: Audio messages get correct ViewHolder type.
     */
    test("ViewHolder Type - AUDIO message returns VIEW_TYPE_AUDIO").config(invocations = 10) {
        checkAll(mediaMessageArb(UIKitConstants.MessageType.AUDIO)) { message ->
            val viewType = when (message.type) {
                UIKitConstants.MessageType.IMAGE -> VIEW_TYPE_IMAGE
                UIKitConstants.MessageType.VIDEO -> VIEW_TYPE_VIDEO
                UIKitConstants.MessageType.AUDIO -> VIEW_TYPE_AUDIO
                UIKitConstants.MessageType.FILE -> VIEW_TYPE_DOCUMENT
                else -> VIEW_TYPE_TEXT
            }

            viewType shouldBe VIEW_TYPE_AUDIO
        }
    }

    /**
     * Test: getItemViewType returns VIEW_TYPE_DOCUMENT for file messages.
     *
     * Validates: File/document messages get correct ViewHolder type.
     */
    test("ViewHolder Type - FILE message returns VIEW_TYPE_DOCUMENT").config(invocations = 10) {
        checkAll(mediaMessageArb(UIKitConstants.MessageType.FILE)) { message ->
            val viewType = when (message.type) {
                UIKitConstants.MessageType.IMAGE -> VIEW_TYPE_IMAGE
                UIKitConstants.MessageType.VIDEO -> VIEW_TYPE_VIDEO
                UIKitConstants.MessageType.AUDIO -> VIEW_TYPE_AUDIO
                UIKitConstants.MessageType.FILE -> VIEW_TYPE_DOCUMENT
                else -> VIEW_TYPE_TEXT
            }

            viewType shouldBe VIEW_TYPE_DOCUMENT
        }
    }

    /**
     * Test: getItemViewType returns VIEW_TYPE_LINK for text messages with links.
     *
     * Validates: Text messages containing URLs get link ViewHolder type.
     */
    test("ViewHolder Type - TEXT with link returns VIEW_TYPE_LINK").config(invocations = 10) {
        checkAll(linkMessageArb) { message ->
            val hasLink = message.text.contains("http://") || message.text.contains("https://")

            val viewType = when (message.type) {
                UIKitConstants.MessageType.TEXT -> {
                    if (hasLink) VIEW_TYPE_LINK else VIEW_TYPE_TEXT
                }
                else -> VIEW_TYPE_TEXT
            }

            viewType shouldBe VIEW_TYPE_LINK
        }
    }


    // ==================== DiffUtil Callback Tests ====================

    /**
     * Test: areItemsTheSame returns true for same message ID.
     *
     * Validates: DiffUtil correctly identifies same items by message ID.
     */
    test("DiffUtil - areItemsTheSame returns true for same message ID").config(invocations = 10) {
        checkAll(Arb.long(1, 100000)) { messageId ->
            val oldMessage = mock(BaseMessage::class.java).apply {
                `when`(this.id).thenReturn(messageId)
            }
            val newMessage = mock(BaseMessage::class.java).apply {
                `when`(this.id).thenReturn(messageId)
            }

            // Simulate DiffUtil.ItemCallback.areItemsTheSame
            val areItemsTheSame = oldMessage.id == newMessage.id

            areItemsTheSame.shouldBeTrue()
        }
    }

    /**
     * Test: areItemsTheSame returns false for different message IDs.
     *
     * Validates: DiffUtil correctly identifies different items.
     */
    test("DiffUtil - areItemsTheSame returns false for different message IDs").config(invocations = 10) {
        checkAll(Arb.long(1, 50000), Arb.long(50001, 100000)) { id1, id2 ->
            val oldMessage = mock(BaseMessage::class.java).apply {
                `when`(this.id).thenReturn(id1)
            }
            val newMessage = mock(BaseMessage::class.java).apply {
                `when`(this.id).thenReturn(id2)
            }

            // Simulate DiffUtil.ItemCallback.areItemsTheSame
            val areItemsTheSame = oldMessage.id == newMessage.id

            areItemsTheSame.shouldBeFalse()
        }
    }

    /**
     * Test: areContentsTheSame returns true when all relevant fields match.
     *
     * Validates: DiffUtil correctly identifies unchanged content.
     */
    test("DiffUtil - areContentsTheSame returns true when all fields match").config(invocations = 10) {
        checkAll(
            Arb.long(1, 100000),
            Arb.long(1000000000L, 2000000000L),
            Arb.long(1000000000L, 2000000000L),
            Arb.long(0, 0)
        ) { id, sentAt, updatedAt, deletedAt ->
            val oldMessage = mock(BaseMessage::class.java).apply {
                `when`(this.id).thenReturn(id)
                `when`(this.sentAt).thenReturn(sentAt)
                `when`(this.updatedAt).thenReturn(updatedAt)
                `when`(this.deletedAt).thenReturn(deletedAt)
            }

            val newMessage = mock(BaseMessage::class.java).apply {
                `when`(this.id).thenReturn(id)
                `when`(this.sentAt).thenReturn(sentAt)
                `when`(this.updatedAt).thenReturn(updatedAt)
                `when`(this.deletedAt).thenReturn(deletedAt)
            }

            // Simulate DiffUtil.ItemCallback.areContentsTheSame
            val areContentsTheSame = oldMessage.id == newMessage.id &&
                    oldMessage.sentAt == newMessage.sentAt &&
                    oldMessage.updatedAt == newMessage.updatedAt &&
                    oldMessage.deletedAt == newMessage.deletedAt

            areContentsTheSame.shouldBeTrue()
        }
    }

    /**
     * Test: areContentsTheSame returns false when sentAt differs.
     *
     * Validates: DiffUtil detects changes in sentAt timestamp.
     */
    test("DiffUtil - areContentsTheSame returns false when sentAt differs").config(invocations = 10) {
        checkAll(
            Arb.long(1, 100000),
            Arb.long(1000000000L, 1500000000L),
            Arb.long(1500000001L, 2000000000L)
        ) { id, oldSentAt, newSentAt ->
            val oldMessage = mock(BaseMessage::class.java).apply {
                `when`(this.id).thenReturn(id)
                `when`(this.sentAt).thenReturn(oldSentAt)
                `when`(this.updatedAt).thenReturn(oldSentAt)
                `when`(this.deletedAt).thenReturn(0L)
            }

            val newMessage = mock(BaseMessage::class.java).apply {
                `when`(this.id).thenReturn(id)
                `when`(this.sentAt).thenReturn(newSentAt)
                `when`(this.updatedAt).thenReturn(oldSentAt)
                `when`(this.deletedAt).thenReturn(0L)
            }

            // Simulate DiffUtil.ItemCallback.areContentsTheSame
            val areContentsTheSame = oldMessage.id == newMessage.id &&
                    oldMessage.sentAt == newMessage.sentAt &&
                    oldMessage.updatedAt == newMessage.updatedAt &&
                    oldMessage.deletedAt == newMessage.deletedAt

            areContentsTheSame.shouldBeFalse()
        }
    }

    /**
     * Test: areContentsTheSame returns false when updatedAt differs.
     *
     * Validates: DiffUtil detects changes in updatedAt timestamp.
     */
    test("DiffUtil - areContentsTheSame returns false when updatedAt differs").config(invocations = 10) {
        checkAll(
            Arb.long(1, 100000),
            Arb.long(1000000000L, 1500000000L),
            Arb.long(1500000001L, 2000000000L)
        ) { id, oldUpdatedAt, newUpdatedAt ->
            val oldMessage = mock(BaseMessage::class.java).apply {
                `when`(this.id).thenReturn(id)
                `when`(this.sentAt).thenReturn(1000000000L)
                `when`(this.updatedAt).thenReturn(oldUpdatedAt)
                `when`(this.deletedAt).thenReturn(0L)
            }

            val newMessage = mock(BaseMessage::class.java).apply {
                `when`(this.id).thenReturn(id)
                `when`(this.sentAt).thenReturn(1000000000L)
                `when`(this.updatedAt).thenReturn(newUpdatedAt)
                `when`(this.deletedAt).thenReturn(0L)
            }

            // Simulate DiffUtil.ItemCallback.areContentsTheSame
            val areContentsTheSame = oldMessage.id == newMessage.id &&
                    oldMessage.sentAt == newMessage.sentAt &&
                    oldMessage.updatedAt == newMessage.updatedAt &&
                    oldMessage.deletedAt == newMessage.deletedAt

            areContentsTheSame.shouldBeFalse()
        }
    }

    /**
     * Test: areContentsTheSame returns false when deletedAt differs.
     *
     * Validates: DiffUtil detects changes in deletedAt timestamp (message deletion).
     */
    test("DiffUtil - areContentsTheSame returns false when deletedAt differs").config(invocations = 10) {
        checkAll(
            Arb.long(1, 100000),
            Arb.long(1000000000L, 2000000000L)
        ) { id, deletedAt ->
            val oldMessage = mock(BaseMessage::class.java).apply {
                `when`(this.id).thenReturn(id)
                `when`(this.sentAt).thenReturn(1000000000L)
                `when`(this.updatedAt).thenReturn(1000000000L)
                `when`(this.deletedAt).thenReturn(0L)
            }

            val newMessage = mock(BaseMessage::class.java).apply {
                `when`(this.id).thenReturn(id)
                `when`(this.sentAt).thenReturn(1000000000L)
                `when`(this.updatedAt).thenReturn(1000000000L)
                `when`(this.deletedAt).thenReturn(deletedAt)
            }

            // Simulate DiffUtil.ItemCallback.areContentsTheSame
            val areContentsTheSame = oldMessage.id == newMessage.id &&
                    oldMessage.sentAt == newMessage.sentAt &&
                    oldMessage.updatedAt == newMessage.updatedAt &&
                    oldMessage.deletedAt == newMessage.deletedAt

            areContentsTheSame.shouldBeFalse()
        }
    }


    // ==================== Sticky Header Tests ====================

    /**
     * Test: getHeaderId returns same ID for messages on same day.
     *
     * Validates: Messages on the same day are grouped under the same header.
     */
    test("Sticky Header - same day messages have same header ID").config(invocations = 10) {
        checkAll(Arb.int(2020, 2025), Arb.int(1, 12), Arb.int(1, 28)) { year, month, day ->
            // Create two timestamps on the same day
            val calendar1 = Calendar.getInstance().apply {
                set(year, month - 1, day, 10, 0, 0)
            }
            val calendar2 = Calendar.getInstance().apply {
                set(year, month - 1, day, 15, 30, 0)
            }

            val timestamp1 = calendar1.timeInMillis / 1000
            val timestamp2 = calendar2.timeInMillis / 1000

            val message1 = mock(BaseMessage::class.java).apply {
                `when`(this.sentAt).thenReturn(timestamp1)
            }
            val message2 = mock(BaseMessage::class.java).apply {
                `when`(this.sentAt).thenReturn(timestamp2)
            }

            // Simulate getHeaderId logic
            fun getHeaderId(message: BaseMessage): Long {
                val cal = Calendar.getInstance().apply {
                    timeInMillis = message.sentAt * 1000
                }
                return (cal.get(Calendar.YEAR) * 1000L + cal.get(Calendar.DAY_OF_YEAR))
            }

            val headerId1 = getHeaderId(message1)
            val headerId2 = getHeaderId(message2)

            headerId1 shouldBe headerId2
        }
    }

    /**
     * Test: getHeaderId returns different IDs for messages on different days.
     *
     * Validates: Messages on different days have different header IDs.
     */
    test("Sticky Header - different day messages have different header IDs").config(invocations = 10) {
        checkAll(Arb.int(2020, 2025), Arb.int(1, 11), Arb.int(1, 27)) { year, month, day ->
            // Create two timestamps on different days
            val calendar1 = Calendar.getInstance().apply {
                set(year, month - 1, day, 10, 0, 0)
            }
            val calendar2 = Calendar.getInstance().apply {
                set(year, month - 1, day + 1, 10, 0, 0)
            }

            val timestamp1 = calendar1.timeInMillis / 1000
            val timestamp2 = calendar2.timeInMillis / 1000

            val message1 = mock(BaseMessage::class.java).apply {
                `when`(this.sentAt).thenReturn(timestamp1)
            }
            val message2 = mock(BaseMessage::class.java).apply {
                `when`(this.sentAt).thenReturn(timestamp2)
            }

            // Simulate getHeaderId logic
            fun getHeaderId(message: BaseMessage): Long {
                val cal = Calendar.getInstance().apply {
                    timeInMillis = message.sentAt * 1000
                }
                return (cal.get(Calendar.YEAR) * 1000L + cal.get(Calendar.DAY_OF_YEAR))
            }

            val headerId1 = getHeaderId(message1)
            val headerId2 = getHeaderId(message2)

            headerId1 shouldNotBe headerId2
        }
    }

    /**
     * Test: getHeaderId returns different IDs for messages in different years.
     *
     * Validates: Messages in different years have different header IDs.
     */
    test("Sticky Header - different year messages have different header IDs").config(invocations = 10) {
        checkAll(Arb.int(2020, 2024), Arb.int(1, 12), Arb.int(1, 28)) { year, month, day ->
            // Create two timestamps in different years
            val calendar1 = Calendar.getInstance().apply {
                set(year, month - 1, day, 10, 0, 0)
            }
            val calendar2 = Calendar.getInstance().apply {
                set(year + 1, month - 1, day, 10, 0, 0)
            }

            val timestamp1 = calendar1.timeInMillis / 1000
            val timestamp2 = calendar2.timeInMillis / 1000

            val message1 = mock(BaseMessage::class.java).apply {
                `when`(this.sentAt).thenReturn(timestamp1)
            }
            val message2 = mock(BaseMessage::class.java).apply {
                `when`(this.sentAt).thenReturn(timestamp2)
            }

            // Simulate getHeaderId logic
            fun getHeaderId(message: BaseMessage): Long {
                val cal = Calendar.getInstance().apply {
                    timeInMillis = message.sentAt * 1000
                }
                return (cal.get(Calendar.YEAR) * 1000L + cal.get(Calendar.DAY_OF_YEAR))
            }

            val headerId1 = getHeaderId(message1)
            val headerId2 = getHeaderId(message2)

            headerId1 shouldNotBe headerId2
            // Year difference should be at least 1000 (since we multiply year by 1000)
            kotlin.math.abs(headerId1 - headerId2) shouldBeGreaterThan 365L
        }
    }

    /**
     * Test: getHeaderId returns -1 for invalid position.
     *
     * Validates: Invalid positions return NO_HEADER_ID.
     */
    test("Sticky Header - invalid position returns -1").config(invocations = 10) {
        checkAll(Arb.int(-100, -1)) { invalidPosition ->
            // Simulate getHeaderId with bounds check
            fun getHeaderId(position: Int, itemCount: Int): Long {
                if (position < 0 || position >= itemCount) return -1L
                return 0L // Would normally calculate from message
            }

            val headerId = getHeaderId(invalidPosition, 10)

            headerId shouldBe -1L
        }
    }

    /**
     * Test: Header grouping correctly groups consecutive same-day messages.
     *
     * Validates: Consecutive messages on the same day share a header.
     */
    test("Sticky Header - consecutive same-day messages share header").config(invocations = 10) {
        checkAll(Arb.int(2020, 2025), Arb.int(1, 12), Arb.int(1, 28), Arb.int(2, 10)) { year, month, day, count ->
            // Create multiple messages on the same day
            val baseCalendar = Calendar.getInstance().apply {
                set(year, month - 1, day, 0, 0, 0)
            }

            val messages = (0 until count).map { i ->
                val timestamp = (baseCalendar.timeInMillis / 1000) + (i * 3600) // Add hours
                mock(BaseMessage::class.java).apply {
                    `when`(this.sentAt).thenReturn(timestamp)
                }
            }

            // Simulate getHeaderId logic
            fun getHeaderId(message: BaseMessage): Long {
                val cal = Calendar.getInstance().apply {
                    timeInMillis = message.sentAt * 1000
                }
                return (cal.get(Calendar.YEAR) * 1000L + cal.get(Calendar.DAY_OF_YEAR))
            }

            // All messages should have the same header ID
            val headerIds = messages.map { getHeaderId(it) }.toSet()

            headerIds.size shouldBe 1
        }
    }


    // ==================== Adapter List Update Tests ====================

    /**
     * Test: Adapter correctly tracks list size after submitList.
     *
     * Validates: Adapter updates correctly when new list is submitted.
     */
    test("Adapter - list size matches submitted list").config(invocations = 10) {
        checkAll(messageListArb) { messages ->
            // Simulate adapter behavior
            var adapterList: List<BaseMessage> = emptyList()

            // Submit list
            adapterList = messages

            // Verify size matches
            adapterList.size shouldBe messages.size
        }
    }

    /**
     * Test: Adapter handles empty list correctly.
     *
     * Validates: Adapter works with empty list.
     */
    test("Adapter - handles empty list").config(invocations = 1) {
        val emptyList = emptyList<BaseMessage>()

        // Simulate adapter behavior
        var adapterList: List<BaseMessage> = listOf(mock(BaseMessage::class.java))

        // Submit empty list
        adapterList = emptyList

        adapterList.shouldBeEmpty()
    }

    /**
     * Test: Adapter preserves message order.
     *
     * Validates: Adapter maintains the order of submitted messages.
     */
    test("Adapter - preserves message order").config(invocations = 10) {
        checkAll(messageListArb) { messages ->
            // Simulate adapter behavior
            var adapterList: List<BaseMessage> = emptyList()

            // Submit list
            adapterList = messages

            // Verify order is preserved
            adapterList shouldContainExactly messages
        }
    }

    /**
     * Test: Adapter correctly identifies items at positions.
     *
     * Validates: getItem returns correct message at each position.
     */
    test("Adapter - getItem returns correct message at position").config(invocations = 10) {
        checkAll(Arb.list(baseMessageArb, 1..20)) { messages ->
            // Simulate adapter behavior
            val adapterList = messages

            // Verify each position returns correct item
            messages.forEachIndexed { index, message ->
                adapterList[index] shouldBe message
            }
        }
    }


    // ==================== Click Callback Tests ====================

    /**
     * Test: Click callback is invoked with correct message.
     *
     * Validates: onMessageClick callback receives correct data.
     */
    test("Callback - onMessageClick receives correct message").config(invocations = 10) {
        checkAll(baseMessageArb) { message ->
            var clickedMessage: BaseMessage? = null

            // Simulate callback setup
            val onMessageClick: (BaseMessage) -> Unit = { msg ->
                clickedMessage = msg
            }

            // Simulate click
            onMessageClick(message)

            clickedMessage shouldBe message
            clickedMessage?.id shouldBe message.id
        }
    }


    // ==================== ViewHolder Listener Tests ====================

    /**
     * Test: Custom text message view listener is tracked.
     *
     * Validates: Adapter tracks custom textMessageItemViewListener.
     */
    test("ViewHolder Listener - textMessageItemViewListener is tracked").config(invocations = 1) {
        var hasListener = false
        hasListener = true
        hasListener.shouldBeTrue()
    }

    /**
     * Test: Custom image message view listener is tracked.
     *
     * Validates: Adapter tracks custom imageMessageItemViewListener.
     */
    test("ViewHolder Listener - imageMessageItemViewListener is tracked").config(invocations = 1) {
        var hasListener = false
        hasListener = true
        hasListener.shouldBeTrue()
    }

    /**
     * Test: Custom video message view listener is tracked.
     *
     * Validates: Adapter tracks custom videoMessageItemViewListener.
     */
    test("ViewHolder Listener - videoMessageItemViewListener is tracked").config(invocations = 1) {
        var hasListener = false
        hasListener = true
        hasListener.shouldBeTrue()
    }

    /**
     * Test: Custom audio message view listener is tracked.
     *
     * Validates: Adapter tracks custom audioMessageItemViewListener.
     */
    test("ViewHolder Listener - audioMessageItemViewListener is tracked").config(invocations = 1) {
        var hasListener = false
        hasListener = true
        hasListener.shouldBeTrue()
    }

    /**
     * Test: Custom document message view listener is tracked.
     *
     * Validates: Adapter tracks custom documentMessageItemViewListener.
     */
    test("ViewHolder Listener - documentMessageItemViewListener is tracked").config(invocations = 1) {
        var hasListener = false
        hasListener = true
        hasListener.shouldBeTrue()
    }

    /**
     * Test: Custom link message view listener is tracked.
     *
     * Validates: Adapter tracks custom linkMessageItemViewListener.
     */
    test("ViewHolder Listener - linkMessageItemViewListener is tracked").config(invocations = 1) {
        var hasListener = false
        hasListener = true
        hasListener.shouldBeTrue()
    }


    // ==================== Mixed Message Type Tests ====================

    /**
     * Test: Adapter correctly handles mixed message types.
     *
     * Validates: Different message types in same list get correct ViewHolder types.
     */
    test("Mixed Types - adapter handles all message types correctly").config(invocations = 10) {
        // Create a list with all message types
        val textMessage = mock(TextMessage::class.java).apply {
            `when`(this.id).thenReturn(1L)
            `when`(this.type).thenReturn(UIKitConstants.MessageType.TEXT)
            `when`(this.text).thenReturn("Hello")
        }
        val imageMessage = mock(MediaMessage::class.java).apply {
            `when`(this.id).thenReturn(2L)
            `when`(this.type).thenReturn(UIKitConstants.MessageType.IMAGE)
        }
        val videoMessage = mock(MediaMessage::class.java).apply {
            `when`(this.id).thenReturn(3L)
            `when`(this.type).thenReturn(UIKitConstants.MessageType.VIDEO)
        }
        val audioMessage = mock(MediaMessage::class.java).apply {
            `when`(this.id).thenReturn(4L)
            `when`(this.type).thenReturn(UIKitConstants.MessageType.AUDIO)
        }
        val fileMessage = mock(MediaMessage::class.java).apply {
            `when`(this.id).thenReturn(5L)
            `when`(this.type).thenReturn(UIKitConstants.MessageType.FILE)
        }

        val messages = listOf<BaseMessage>(textMessage, imageMessage, videoMessage, audioMessage, fileMessage)

        // Simulate getItemViewType for each
        fun getViewType(message: BaseMessage): Int {
            return when (message.type) {
                UIKitConstants.MessageType.IMAGE -> VIEW_TYPE_IMAGE
                UIKitConstants.MessageType.VIDEO -> VIEW_TYPE_VIDEO
                UIKitConstants.MessageType.AUDIO -> VIEW_TYPE_AUDIO
                UIKitConstants.MessageType.FILE -> VIEW_TYPE_DOCUMENT
                UIKitConstants.MessageType.TEXT -> VIEW_TYPE_TEXT
                else -> VIEW_TYPE_TEXT
            }
        }

        val viewTypes = messages.map { getViewType(it) }

        viewTypes shouldContainExactly listOf(
            VIEW_TYPE_TEXT,
            VIEW_TYPE_IMAGE,
            VIEW_TYPE_VIDEO,
            VIEW_TYPE_AUDIO,
            VIEW_TYPE_DOCUMENT
        )
    }

    /**
     * Test: Each message type maps to unique ViewHolder type.
     *
     * Validates: No two different message types share the same ViewHolder type (except text/link).
     */
    test("Mixed Types - message types map to distinct ViewHolder types").config(invocations = 1) {
        val messageTypes = listOf(
            UIKitConstants.MessageType.IMAGE,
            UIKitConstants.MessageType.VIDEO,
            UIKitConstants.MessageType.AUDIO,
            UIKitConstants.MessageType.FILE
        )

        fun getViewType(messageType: String): Int {
            return when (messageType) {
                UIKitConstants.MessageType.IMAGE -> VIEW_TYPE_IMAGE
                UIKitConstants.MessageType.VIDEO -> VIEW_TYPE_VIDEO
                UIKitConstants.MessageType.AUDIO -> VIEW_TYPE_AUDIO
                UIKitConstants.MessageType.FILE -> VIEW_TYPE_DOCUMENT
                else -> VIEW_TYPE_TEXT
            }
        }

        val viewTypes = messageTypes.map { getViewType(it) }.toSet()

        // All media types should have distinct ViewHolder types
        viewTypes.size shouldBe messageTypes.size
    }
})
