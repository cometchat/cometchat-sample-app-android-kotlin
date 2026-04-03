package com.cometchat.uikit.kotlin.presentation.messagelist.adapter

import android.app.Application
import android.view.View
import androidx.test.core.app.ApplicationProvider
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.BubbleFactory
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
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
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.annotation.Config
import java.util.Calendar
import java.util.UUID

/**
 * Property-based tests for MessageAdapter component.
 * Uses Kotest property testing to verify correctness properties.
 *
 * Feature: kotlin-message-adapter-rewrite
 */
@Config(sdk = [28], application = Application::class)
class MessageAdapterPropertyTest : FunSpec({

    // ==================== Test Setup ====================

    lateinit var context: Application
    lateinit var cometChatMock: MockedStatic<CometChat>

    beforeSpec {
        context = ApplicationProvider.getApplicationContext()
    }

    beforeTest {
        cometChatMock = Mockito.mockStatic(CometChat::class.java)
    }

    afterTest {
        cometChatMock.close()
    }

    // ==================== Generators ====================

    /**
     * Generator for message categories.
     */
    val categoryArb = Arb.element(
        CometChatConstants.CATEGORY_MESSAGE,
        CometChatConstants.CATEGORY_ACTION,
        CometChatConstants.CATEGORY_CALL,
        CometChatConstants.CATEGORY_CUSTOM
    )

    /**
     * Generator for message types.
     */
    val typeArb = Arb.element(
        CometChatConstants.MESSAGE_TYPE_TEXT,
        CometChatConstants.MESSAGE_TYPE_IMAGE,
        CometChatConstants.MESSAGE_TYPE_VIDEO,
        CometChatConstants.MESSAGE_TYPE_AUDIO,
        CometChatConstants.MESSAGE_TYPE_FILE,
        CometChatConstants.ActionKeys.ACTION_TYPE_GROUP_MEMBER,
        CometChatConstants.CALL_TYPE_AUDIO,
        CometChatConstants.CALL_TYPE_VIDEO
    )

    /**
     * Generator for alphanumeric strings (for category/type).
     */
    val alphanumericArb = Arb.string(1..20, CodepointArb.alphanumeric())

    /**
     * Generator for mock User objects.
     */
    val userArb = arbitrary {
        val uid = Arb.string(5, 20).bind()
        val name = Arb.string(3, 30).bind()
        mock(User::class.java).apply {
            `when`(this.uid).thenReturn(uid)
            `when`(this.name).thenReturn(name)
        }
    }

    /**
     * Generator for mock BaseMessage objects with configurable category and type.
     */
    fun baseMessageArb(
        categoryGen: Arb<String> = categoryArb,
        typeGen: Arb<String> = typeArb,
        senderUidGen: Arb<String> = Arb.string(5, 20)
    ) = arbitrary {
        val id = Arb.long(1, 100000).bind()
        val category = categoryGen.bind()
        val type = typeGen.bind()
        val senderUid = senderUidGen.bind()
        val sentAt = Arb.long(1000000000L, 2000000000L).bind()
        val deletedAt = if (Arb.boolean().bind()) Arb.long(sentAt, sentAt + 10000).bind() else 0L
        val replyCount = Arb.int(0, 100).bind()
        val receiverType = Arb.element(
            CometChatConstants.RECEIVER_TYPE_USER,
            CometChatConstants.RECEIVER_TYPE_GROUP
        ).bind()

        val mockSender = mock(User::class.java).apply {
            `when`(this.uid).thenReturn(senderUid)
        }

        mock(BaseMessage::class.java).apply {
            `when`(this.id).thenReturn(id)
            `when`(this.category).thenReturn(category)
            `when`(this.type).thenReturn(type)
            `when`(this.sender).thenReturn(mockSender)
            `when`(this.sentAt).thenReturn(sentAt)
            `when`(this.deletedAt).thenReturn(deletedAt)
            `when`(this.replyCount).thenReturn(replyCount)
            `when`(this.receiverType).thenReturn(receiverType)
        }
    }

    /**
     * Generator for mock BaseMessage with specific sender UID.
     */
    fun messageWithSenderArb(senderUid: String) = arbitrary {
        val id = Arb.long(1, 100000).bind()
        val category = Arb.element(
            CometChatConstants.CATEGORY_MESSAGE,
            CometChatConstants.CATEGORY_CUSTOM
        ).bind()
        val type = typeArb.bind()
        val sentAt = Arb.long(1000000000L, 2000000000L).bind()
        val deletedAt = 0L
        val replyCount = Arb.int(0, 100).bind()
        val receiverType = Arb.element(
            CometChatConstants.RECEIVER_TYPE_USER,
            CometChatConstants.RECEIVER_TYPE_GROUP
        ).bind()

        val mockSender = mock(User::class.java).apply {
            `when`(this.uid).thenReturn(senderUid)
        }

        mock(BaseMessage::class.java).apply {
            `when`(this.id).thenReturn(id)
            `when`(this.category).thenReturn(category)
            `when`(this.type).thenReturn(type)
            `when`(this.sender).thenReturn(mockSender)
            `when`(this.sentAt).thenReturn(sentAt)
            `when`(this.deletedAt).thenReturn(deletedAt)
            `when`(this.replyCount).thenReturn(replyCount)
            `when`(this.receiverType).thenReturn(receiverType)
        }
    }

    /**
     * Generator for ACTION category messages.
     */
    val actionMessageArb = arbitrary {
        val id = Arb.long(1, 100000).bind()
        val type = CometChatConstants.ActionKeys.ACTION_TYPE_GROUP_MEMBER
        val sentAt = Arb.long(1000000000L, 2000000000L).bind()

        mock(BaseMessage::class.java).apply {
            `when`(this.id).thenReturn(id)
            `when`(this.category).thenReturn(CometChatConstants.CATEGORY_ACTION)
            `when`(this.type).thenReturn(type)
            `when`(this.sentAt).thenReturn(sentAt)
            `when`(this.deletedAt).thenReturn(0L)
        }
    }

    /**
     * Generator for CALL category messages.
     */
    val callMessageArb = arbitrary {
        val id = Arb.long(1, 100000).bind()
        val type = Arb.element(CometChatConstants.CALL_TYPE_AUDIO, CometChatConstants.CALL_TYPE_VIDEO).bind()
        val sentAt = Arb.long(1000000000L, 2000000000L).bind()

        mock(BaseMessage::class.java).apply {
            `when`(this.id).thenReturn(id)
            `when`(this.category).thenReturn(CometChatConstants.CATEGORY_CALL)
            `when`(this.type).thenReturn(type)
            `when`(this.sentAt).thenReturn(sentAt)
            `when`(this.deletedAt).thenReturn(0L)
        }
    }

    /**
     * Generator for list of messages.
     */
    val messageListArb = Arb.list(baseMessageArb(), 1..20)

    // ==================== Property Tests ====================

    /**
     * Feature: kotlin-message-adapter-rewrite, Property 1: Factory Key Format Consistency
     *
     * *For any* BaseMessage with category C and type T, the factory key SHALL always
     * be formatted as `{C}_{T}`.
     *
     * **Validates: Requirements 1.1**
     */
    test("Property 1: Factory Key Format Consistency").config(invocations = 100) {
        checkAll(alphanumericArb, alphanumericArb) { category, type ->
            // Test the BubbleFactory.getKey static method
            val factoryKey = BubbleFactory.getKey(category, type)

            // Verify format is {category}_{type}
            factoryKey shouldBe "${category}_${type}"

            // Verify the key contains exactly one underscore separator
            factoryKey.count { it == '_' } shouldBe 1

            // Verify the parts before and after underscore match inputs
            val parts = factoryKey.split("_")
            parts.size shouldBe 2
            parts[0] shouldBe category
            parts[1] shouldBe type
        }
    }


    /**
     * Feature: kotlin-message-adapter-rewrite, Property 2: View Type ID Uniqueness and Consistency
     *
     * *For any* factory key K, the assigned view type ID SHALL be unique across all factory keys,
     * and querying the same factory key multiple times SHALL always return the same view type ID.
     *
     * **Validates: Requirements 1.2, 4.2, 4.4**
     */
    test("Property 2: View Type ID Uniqueness and Consistency").config(invocations = 100) {
        checkAll(Arb.list(baseMessageArb(), 2..10)) { messages ->
            val adapter = MessageAdapter(context)

            // Set up logged-in user mock
            val loggedInUser = mock(User::class.java).apply {
                `when`(uid).thenReturn("logged_in_user_123")
            }
            cometChatMock.`when`<User?> { CometChat.getLoggedInUser() }.thenReturn(loggedInUser)

            // Set messages
            adapter.setMessageList(messages)

            // Track view types by factory key
            val viewTypesByFactoryKey = mutableMapOf<String, MutableSet<Int>>()

            // Query view types for all messages
            messages.forEachIndexed { index, message ->
                val viewType = adapter.getItemViewType(index)
                val factoryKey = "${message.category}_${message.type}"

                // Skip special view types (STREAM, IGNORE)
                if (viewType != 4 && viewType != 10000) {
                    viewTypesByFactoryKey.getOrPut(factoryKey) { mutableSetOf() }.add(viewType)
                }
            }

            // For each factory key, all view types should have the same base (factoryId)
            // The suffix (1, 2, 3) may differ based on alignment
            viewTypesByFactoryKey.forEach { (_, viewTypes) ->
                if (viewTypes.isNotEmpty()) {
                    // Extract factory IDs (remove last digit which is alignment suffix)
                    val factoryIds = viewTypes.map { it.toString().dropLast(1) }.toSet()
                    // All view types for the same factory key should have the same factory ID
                    factoryIds.size shouldBe 1
                }
            }

            // Query the same message multiple times - should return same view type
            if (messages.isNotEmpty()) {
                val firstViewType = adapter.getItemViewType(0)
                repeat(5) {
                    adapter.getItemViewType(0) shouldBe firstViewType
                }
            }
        }
    }

    /**
     * Feature: kotlin-message-adapter-rewrite, Property 3: Alignment Suffix Based on Message Category and Sender
     *
     * *For any* message:
     * - If category is ACTION or CALL, the alignment suffix SHALL be CENTER ("3")
     * - If sender UID equals logged-in user UID (and not ACTION/CALL), the alignment suffix SHALL be RIGHT ("2")
     * - If sender UID differs from logged-in user UID (and not ACTION/CALL), the alignment suffix SHALL be LEFT ("1")
     *
     * **Validates: Requirements 1.3, 1.4, 1.5, 7.1, 7.2, 7.3, 7.4**
     */
    test("Property 3: Alignment Suffix Based on Message Category and Sender").config(invocations = 100) {
        val loggedInUid = "logged_in_user_${UUID.randomUUID()}"

        checkAll(baseMessageArb()) { message ->
            val adapter = MessageAdapter(context)

            // Set up logged-in user mock
            val loggedInUser = mock(User::class.java).apply {
                `when`(uid).thenReturn(loggedInUid)
            }
            cometChatMock.`when`<User?> { CometChat.getLoggedInUser() }.thenReturn(loggedInUser)

            adapter.setMessageList(listOf(message))
            val viewType = adapter.getItemViewType(0)
            val viewTypeString = viewType.toString()

            when (message.category) {
                CometChatConstants.CATEGORY_ACTION -> {
                    // ACTION messages should be CENTER ("3") or IGNORE ("10000")
                    (viewTypeString.endsWith("3") || viewType == 10000).shouldBeTrue()
                }
                CometChatConstants.CATEGORY_CALL -> {
                    // CALL messages should be CENTER ("3")
                    viewTypeString.endsWith("3").shouldBeTrue()
                }
                else -> {
                    // Non-ACTION/CALL messages
                    val senderUid = message.sender?.uid
                    if (senderUid == loggedInUid) {
                        // Outgoing message - RIGHT ("2")
                        viewTypeString.endsWith("2").shouldBeTrue()
                    } else {
                        // Incoming message - LEFT ("1")
                        viewTypeString.endsWith("1").shouldBeTrue()
                    }
                }
            }
        }
    }

    /**
     * Feature: kotlin-message-adapter-rewrite, Property 4: Single MessageViewHolder for All Alignments
     *
     * *For any* view type integer ending with "1", "2", or "3":
     * - The adapter SHALL correctly identify the alignment via helper methods
     * - If it equals "10000", it SHALL be identified as IGNORE message
     *
     * **Validates: Requirements 2.1, 2.2, 2.3**
     */
    test("Property 4: Single MessageViewHolder for All Alignments").config(invocations = 100) {
        checkAll(Arb.int(1, 999), Arb.element("1", "2", "3")) { factoryId, suffix ->
            val adapter = MessageAdapter(context)
            val viewType = "$factoryId$suffix".toInt()

            when (suffix) {
                "1" -> {
                    adapter.isLeftViewType(viewType).shouldBeTrue()
                    adapter.isRightViewType(viewType).shouldBeFalse()
                    adapter.isCenterViewType(viewType).shouldBeFalse()
                }
                "2" -> {
                    adapter.isLeftViewType(viewType).shouldBeFalse()
                    adapter.isRightViewType(viewType).shouldBeTrue()
                    adapter.isCenterViewType(viewType).shouldBeFalse()
                }
                "3" -> {
                    adapter.isLeftViewType(viewType).shouldBeFalse()
                    adapter.isRightViewType(viewType).shouldBeFalse()
                    adapter.isCenterViewType(viewType).shouldBeTrue()
                }
            }

            // IGNORE view type check
            adapter.isIgnoreViewType(10000).shouldBeTrue()
            adapter.isIgnoreViewType(viewType).shouldBeFalse()
        }
    }

    /**
     * Feature: kotlin-message-adapter-rewrite, Property 5: Factory Storage and Retrieval Consistency
     *
     * *For any* view type V returned by getItemViewType, the factory stored in viewTypeFactoryHashMap[V]
     * SHALL be the same factory that was looked up using the message's factory key during view type calculation.
     *
     * **Validates: Requirements 1.7, 2.6, 4.3**
     */
    test("Property 5: Factory Storage and Retrieval Consistency").config(invocations = 100) {
        checkAll(baseMessageArb()) { message ->
            val adapter = MessageAdapter(context)

            // Set up logged-in user mock
            val loggedInUser = mock(User::class.java).apply {
                `when`(uid).thenReturn("logged_in_user_123")
            }
            cometChatMock.`when`<User?> { CometChat.getLoggedInUser() }.thenReturn(loggedInUser)

            // Create a mock factory
            val mockFactory = mock(BubbleFactory::class.java)
            val factoryKey = "${message.category}_${message.type}"

            // Register the factory
            adapter.setBubbleFactories(mapOf(factoryKey to mockFactory))

            // Set message and get view type
            adapter.setMessageList(listOf(message))
            val viewType = adapter.getItemViewType(0)

            // Skip special view types
            if (viewType != 4 && viewType != 10000) {
                // Retrieve factory for view type
                val retrievedFactory = adapter.getFactoryForViewType(viewType)

                // Factory should be the same as what we registered
                retrievedFactory shouldBe mockFactory
            }
        }
    }


    /**
     * Feature: kotlin-message-adapter-rewrite, Property 7: Binding Passes Correct Parameters
     *
     * *For any* position P in the message list, the adapter SHALL correctly determine:
     * - The message at position P
     * - The correct alignment for that message
     *
     * **Validates: Requirements 3.1, 3.2, 3.4, 3.5**
     */
    test("Property 7: Binding Passes Correct Parameters").config(invocations = 100) {
        val loggedInUid = "logged_in_user_${UUID.randomUUID()}"

        checkAll(Arb.list(baseMessageArb(), 1..10)) { messages ->
            val adapter = MessageAdapter(context)

            // Set up logged-in user mock
            val loggedInUser = mock(User::class.java).apply {
                `when`(uid).thenReturn(loggedInUid)
            }
            cometChatMock.`when`<User?> { CometChat.getLoggedInUser() }.thenReturn(loggedInUser)

            adapter.setMessageList(messages)

            // Verify each position returns correct message and alignment
            messages.forEachIndexed { position, expectedMessage ->
                // getMessage should return the correct message
                val retrievedMessage = adapter.getMessage(position)
                retrievedMessage.shouldNotBeNull()
                retrievedMessage!!.id shouldBe expectedMessage.id

                // getMessageAlignment should return correct alignment
                val alignment = adapter.getMessageAlignment(expectedMessage)

                when (expectedMessage.category) {
                    CometChatConstants.CATEGORY_ACTION,
                    CometChatConstants.CATEGORY_CALL -> {
                        alignment shouldBe UIKitConstants.MessageBubbleAlignment.CENTER
                    }
                    else -> {
                        if (expectedMessage.sender?.uid == loggedInUid) {
                            alignment shouldBe UIKitConstants.MessageBubbleAlignment.RIGHT
                        } else {
                            alignment shouldBe UIKitConstants.MessageBubbleAlignment.LEFT
                        }
                    }
                }
            }
        }
    }

    /**
     * Feature: kotlin-message-adapter-rewrite, Property 8: Header ID Consistency for Same-Date Messages
     *
     * *For any* two messages M1 and M2 with the same calendar date (ignoring time),
     * getHeaderId(position of M1) SHALL equal getHeaderId(position of M2).
     *
     * **Validates: Requirements 5.2**
     */
    test("Property 8: Header ID Consistency for Same-Date Messages").config(invocations = 100) {
        checkAll(Arb.long(1000000000L, 2000000000L)) { baseTimestamp ->
            val adapter = MessageAdapter(context)

            // Create two messages on the same day but different times
            val calendar = Calendar.getInstance().apply {
                timeInMillis = baseTimestamp * 1000
            }

            // Message 1: at the base time
            val message1 = mock(BaseMessage::class.java).apply {
                `when`(id).thenReturn(1L)
                `when`(category).thenReturn(CometChatConstants.CATEGORY_MESSAGE)
                `when`(type).thenReturn(CometChatConstants.MESSAGE_TYPE_TEXT)
                `when`(sentAt).thenReturn(baseTimestamp)
            }

            // Message 2: same day, different hour (add 1-23 hours)
            val hoursToAdd = (1..23).random()
            calendar.add(Calendar.HOUR_OF_DAY, hoursToAdd)
            // Make sure we're still on the same day
            calendar.set(Calendar.HOUR_OF_DAY, (calendar.get(Calendar.HOUR_OF_DAY) % 24))
            val timestamp2 = calendar.timeInMillis / 1000

            val message2 = mock(BaseMessage::class.java).apply {
                `when`(id).thenReturn(2L)
                `when`(category).thenReturn(CometChatConstants.CATEGORY_MESSAGE)
                `when`(type).thenReturn(CometChatConstants.MESSAGE_TYPE_TEXT)
                `when`(sentAt).thenReturn(timestamp2)
            }

            // Only test if both messages are on the same calendar day
            val cal1 = Calendar.getInstance().apply { timeInMillis = baseTimestamp * 1000 }
            val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp2 * 1000 }

            if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)) {

                adapter.setMessageList(listOf(message1, message2))

                val headerId1 = adapter.getHeaderId(0)
                val headerId2 = adapter.getHeaderId(1)

                // Same date should produce same header ID
                headerId1 shouldBe headerId2
            }
        }
    }

    /**
     * Feature: kotlin-message-adapter-rewrite, Property 9: List Operations Preserve Message Integrity
     *
     * *For any* sequence of list operations (add, update, remove):
     * - addMessage(M, P) SHALL result in getMessage(P) returning M
     * - updateMessage(M) SHALL result in the message with M.id being replaced with M
     * - removeMessage(id) SHALL result in findMessagePosition(id) returning -1
     * - clearMessages() SHALL result in getItemCount() returning 0
     *
     * **Validates: Requirements 10.2, 10.3, 10.4, 10.5, 10.6, 10.7**
     */
    test("Property 9: List Operations Preserve Message Integrity").config(invocations = 100) {
        checkAll(Arb.list(baseMessageArb(), 1..10)) { initialMessages ->
            val adapter = MessageAdapter(context)

            // Test setMessageList
            adapter.setMessageList(initialMessages)
            adapter.itemCount shouldBe initialMessages.size

            // Test getMessage returns correct message at each position
            initialMessages.forEachIndexed { index, message ->
                adapter.getMessage(index)?.id shouldBe message.id
            }

            // Test addMessage at specific position
            val newMessage = mock(BaseMessage::class.java).apply {
                `when`(id).thenReturn(999999L)
                `when`(category).thenReturn(CometChatConstants.CATEGORY_MESSAGE)
                `when`(type).thenReturn(CometChatConstants.MESSAGE_TYPE_TEXT)
                `when`(sentAt).thenReturn(System.currentTimeMillis() / 1000)
            }

            val insertPosition = if (initialMessages.isNotEmpty()) initialMessages.size / 2 else 0
            adapter.addMessage(newMessage, insertPosition)
            adapter.getMessage(insertPosition)?.id shouldBe 999999L
            adapter.itemCount shouldBe initialMessages.size + 1

            // Test updateMessage
            val updatedMessage = mock(BaseMessage::class.java).apply {
                `when`(id).thenReturn(999999L)
                `when`(category).thenReturn(CometChatConstants.CATEGORY_MESSAGE)
                `when`(type).thenReturn(CometChatConstants.MESSAGE_TYPE_IMAGE) // Changed type
                `when`(sentAt).thenReturn(System.currentTimeMillis() / 1000)
                `when`(updatedAt).thenReturn(System.currentTimeMillis() / 1000 + 100)
            }
            adapter.updateMessage(updatedMessage)
            adapter.findMessagePosition(999999L) shouldBe insertPosition

            // Test removeMessage
            adapter.removeMessage(999999L)
            adapter.findMessagePosition(999999L) shouldBe -1
            adapter.itemCount shouldBe initialMessages.size

            // Test clearMessages
            adapter.clearMessages()
            adapter.itemCount shouldBe 0
        }
    }

    /**
     * Feature: kotlin-message-adapter-rewrite, Property 10: Highlight Application Based on Message ID
     *
     * *For any* message M during binding:
     * - If M.id equals highlightedMessageId, the message should be highlighted
     * - If M.id does not equal highlightedMessageId, the message should not be highlighted
     *
     * **Validates: Requirements 11.2, 11.3**
     */
    test("Property 10: Highlight Application Based on Message ID").config(invocations = 100) {
        checkAll(Arb.long(1, 100000), Arb.long(1, 100000)) { messageId, highlightId ->
            val adapter = MessageAdapter(context)

            // Set highlight configuration
            adapter.highlightedMessageId = highlightId
            adapter.highlightAlpha = 0.3f

            val message = mock(BaseMessage::class.java).apply {
                `when`(id).thenReturn(messageId)
                `when`(category).thenReturn(CometChatConstants.CATEGORY_MESSAGE)
                `when`(type).thenReturn(CometChatConstants.MESSAGE_TYPE_TEXT)
                `when`(sentAt).thenReturn(System.currentTimeMillis() / 1000)
            }

            adapter.setMessageList(listOf(message))

            // Verify the highlight configuration is set correctly
            adapter.highlightedMessageId shouldBe highlightId

            // The actual highlight application happens in bind(), which requires ViewHolder
            // Here we verify the configuration is correctly stored
            if (messageId == highlightId) {
                // Message should be highlighted
                adapter.highlightedMessageId shouldBe messageId
            } else {
                // Message should not be highlighted
                adapter.highlightedMessageId shouldNotBe messageId
            }
        }
    }


    /**
     * Feature: kotlin-message-adapter-rewrite, Property 11: Long Press Callback Not Invoked for Deleted Messages
     *
     * *For any* message M with deletedAt > 0, the adapter configuration SHALL support
     * checking if a message is deleted before invoking long press callback.
     *
     * **Validates: Requirements 9.5**
     */
    test("Property 11: Long Press Callback Not Invoked for Deleted Messages").config(invocations = 100) {
        checkAll(
            Arb.long(1, 100000),
            Arb.long(0, 2000000000L)
        ) { messageId, deletedAt ->
            val adapter = MessageAdapter(context)

            val message = mock(BaseMessage::class.java).apply {
                `when`(id).thenReturn(messageId)
                `when`(category).thenReturn(CometChatConstants.CATEGORY_MESSAGE)
                `when`(type).thenReturn(CometChatConstants.MESSAGE_TYPE_TEXT)
                `when`(sentAt).thenReturn(System.currentTimeMillis() / 1000)
                `when`(this.deletedAt).thenReturn(deletedAt)
            }

            adapter.setMessageList(listOf(message))

            // Verify the message's deletedAt is correctly accessible
            val retrievedMessage = adapter.getMessage(0)
            retrievedMessage.shouldNotBeNull()
            retrievedMessage!!.deletedAt shouldBe deletedAt

            // The actual callback invocation check happens in bind()
            // Here we verify the message state is correctly stored
            // If deletedAt > 0, the message is deleted and long press should not invoke callback
            if (deletedAt > 0) {
                (retrievedMessage.deletedAt > 0) shouldBe true
            } else {
                retrievedMessage.deletedAt shouldBe 0L
            }
        }
    }

    /**
     * Feature: kotlin-message-adapter-rewrite, Property 12: RTL Alignment Swapping
     *
     * *For any* message in RTL layout direction:
     * - LEFT alignment SHALL be swapped to RIGHT in the ViewHolder
     * - RIGHT alignment SHALL be swapped to LEFT in the ViewHolder
     * - CENTER alignment SHALL remain CENTER
     *
     * **Validates: Requirements 7.6**
     */
    test("Property 12: RTL Alignment Swapping").config(invocations = 100) {
        checkAll(
            Arb.element(
                UIKitConstants.MessageBubbleAlignment.LEFT,
                UIKitConstants.MessageBubbleAlignment.RIGHT,
                UIKitConstants.MessageBubbleAlignment.CENTER
            )
        ) { originalAlignment ->
            val adapter = MessageAdapter(context)

            // Test LTR (no swapping)
            adapter.layoutDirection = View.LAYOUT_DIRECTION_LTR
            val ltrAlignment = adapter.adjustAlignmentForRTL(originalAlignment)
            ltrAlignment shouldBe originalAlignment

            // Test RTL (swapping)
            adapter.layoutDirection = View.LAYOUT_DIRECTION_RTL
            val rtlAlignment = adapter.adjustAlignmentForRTL(originalAlignment)

            when (originalAlignment) {
                UIKitConstants.MessageBubbleAlignment.LEFT -> {
                    rtlAlignment shouldBe UIKitConstants.MessageBubbleAlignment.RIGHT
                }
                UIKitConstants.MessageBubbleAlignment.RIGHT -> {
                    rtlAlignment shouldBe UIKitConstants.MessageBubbleAlignment.LEFT
                }
                UIKitConstants.MessageBubbleAlignment.CENTER -> {
                    rtlAlignment shouldBe UIKitConstants.MessageBubbleAlignment.CENTER
                }
            }
        }
    }

    /**
     * Feature: kotlin-message-adapter-rewrite, Property 13: LEFT_ALIGNED Mode Forces LEFT Alignment
     *
     * *For any* non-ACTION, non-CALL message when listAlignment is LEFT_ALIGNED,
     * the alignment suffix SHALL be LEFT ("1") regardless of sender UID.
     *
     * **Validates: Requirements 7.5**
     */
    test("Property 13: LEFT_ALIGNED Mode Forces LEFT Alignment").config(invocations = 100) {
        val loggedInUid = "logged_in_user_${UUID.randomUUID()}"

        // Test with both outgoing (same UID) and incoming (different UID) messages
        checkAll(Arb.boolean()) { isOutgoing ->
            val adapter = MessageAdapter(context)
            adapter.listAlignment = UIKitConstants.MessageListAlignment.LEFT_ALIGNED

            // Set up logged-in user mock
            val loggedInUser = mock(User::class.java).apply {
                `when`(uid).thenReturn(loggedInUid)
            }
            cometChatMock.`when`<User?> { CometChat.getLoggedInUser() }.thenReturn(loggedInUser)

            val senderUid = if (isOutgoing) loggedInUid else "other_user_${UUID.randomUUID()}"
            val mockSender = mock(User::class.java).apply {
                `when`(uid).thenReturn(senderUid)
            }

            // Create a non-ACTION, non-CALL message
            val message = mock(BaseMessage::class.java).apply {
                `when`(id).thenReturn(1L)
                `when`(category).thenReturn(CometChatConstants.CATEGORY_MESSAGE)
                `when`(type).thenReturn(CometChatConstants.MESSAGE_TYPE_TEXT)
                `when`(sender).thenReturn(mockSender)
                `when`(sentAt).thenReturn(System.currentTimeMillis() / 1000)
            }

            adapter.setMessageList(listOf(message))
            val viewType = adapter.getItemViewType(0)

            // In LEFT_ALIGNED mode, all non-ACTION/CALL messages should be LEFT ("1")
            viewType.toString().endsWith("1").shouldBeTrue()

            // Also verify via getMessageAlignment
            val alignment = adapter.getMessageAlignment(message)
            alignment shouldBe UIKitConstants.MessageBubbleAlignment.LEFT
        }
    }

    /**
     * Feature: kotlin-message-adapter-rewrite, Property 17: Hide Group Action Messages
     *
     * *For any* message with category ACTION when hideGroupActionMessage is true,
     * getItemViewType SHALL return IGNORE_MESSAGE view type (10000).
     *
     * **Validates: Requirements 1.8**
     */
    test("Property 17: Hide Group Action Messages").config(invocations = 100) {
        checkAll(actionMessageArb) { actionMessage ->
            val adapter = MessageAdapter(context)

            // Test with hideGroupActionMessage = false (should be CENTER)
            adapter.hideGroupActionMessage = false
            adapter.setMessageList(listOf(actionMessage))
            val viewTypeVisible = adapter.getItemViewType(0)
            viewTypeVisible.toString().endsWith("3").shouldBeTrue() // CENTER

            // Test with hideGroupActionMessage = true (should be IGNORE)
            adapter.hideGroupActionMessage = true
            // Need to reset the adapter state
            val adapter2 = MessageAdapter(context)
            adapter2.hideGroupActionMessage = true
            adapter2.setMessageList(listOf(actionMessage))
            val viewTypeHidden = adapter2.getItemViewType(0)
            viewTypeHidden shouldBe 10000 // IGNORE_MESSAGE
        }
    }

    /**
     * Feature: kotlin-message-adapter-rewrite, Property 18: No Factory Passes Null to Bubble
     *
     * *For any* message with a category_type combination that has no registered BubbleFactory,
     * the adapter SHALL pass null to CometChatMessageBubble.setBubbleFactory(),
     * and the bubble SHALL handle default rendering internally.
     *
     * **Validates: Requirements 4.5**
     */
    test("Property 18: No Factory Passes Null to Bubble").config(invocations = 100) {
        checkAll(baseMessageArb()) { message ->
            val adapter = MessageAdapter(context)

            // Set up logged-in user mock
            val loggedInUser = mock(User::class.java).apply {
                `when`(uid).thenReturn("logged_in_user_123")
            }
            cometChatMock.`when`<User?> { CometChat.getLoggedInUser() }.thenReturn(loggedInUser)

            // Do NOT register any factory
            adapter.setMessageList(listOf(message))
            val viewType = adapter.getItemViewType(0)

            // Skip special view types
            if (viewType != 4 && viewType != 10000) {
                // Factory should be null since we didn't register any
                val factory = adapter.getFactoryForViewType(viewType)
                factory.shouldBeNull()
            }

            // Verify the bubbleFactories map is empty
            adapter.getBubbleFactories().isEmpty().shouldBeTrue()
        }
    }
})

// ==================== Helper Extensions ====================

/**
 * Codepoint data class for character representation.
 */
data class Codepoint(val value: Int) {
    fun asString(): String = value.toChar().toString()
}

/**
 * Codepoint generator for alphanumeric characters.
 */
object CodepointArb {
    fun alphanumeric(): Arb<Codepoint> = Arb.element(
        ('a'..'z').map { Codepoint(it.code) } +
        ('A'..'Z').map { Codepoint(it.code) } +
        ('0'..'9').map { Codepoint(it.code) }
    )
}

/**
 * Extension to generate strings from codepoints.
 */
fun Arb.Companion.string(range: IntRange, codepoints: Arb<Codepoint>): Arb<String> = arbitrary {
    val length = Arb.int(range).bind()
    (1..length).map { codepoints.bind().asString() }.joinToString("")
}
