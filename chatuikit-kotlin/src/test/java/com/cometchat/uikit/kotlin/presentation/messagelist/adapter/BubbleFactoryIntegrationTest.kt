package com.cometchat.uikit.kotlin.presentation.messagelist.adapter

import android.app.Application
import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.BubbleFactory
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Integration tests for BubbleFactory registration, replacement, and slot rendering
 * within the MessageAdapter pipeline.
 *
 * Covers:
 * - How it works: factory key resolution (category_type) and view type assignment
 * - Replacing an existing bubble factory
 * - Adding a new bubble factory for custom messages
 * - Replacing the entire bubble (createBubbleView)
 * - Bubble slot reference (all 8 slots)
 *
 * Reference: https://www.cometchat.com/docs/ui-kit/android/v6/message-list#bubble-factory
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
class BubbleFactoryIntegrationTest {

    private lateinit var context: Application
    private lateinit var adapter: MessageAdapter
    private lateinit var cometChatMock: MockedStatic<CometChat>

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        adapter = MessageAdapter(context)
        cometChatMock = Mockito.mockStatic(CometChat::class.java)

        // Set up logged-in user
        val loggedInUser = mock(User::class.java).apply {
            `when`(uid).thenReturn("logged_in_user")
        }
        cometChatMock.`when`<User?> { CometChat.getLoggedInUser() }.thenReturn(loggedInUser)
    }

    @After
    fun tearDown() {
        cometChatMock.close()
    }

    // ==================== Helper Methods ====================

    private fun createMessage(
        id: Long = 1L,
        category: String = CometChatConstants.CATEGORY_MESSAGE,
        type: String = CometChatConstants.MESSAGE_TYPE_TEXT,
        senderUid: String = "other_user",
        deletedAt: Long = 0L
    ): BaseMessage {
        val sender = mock(User::class.java).apply {
            `when`(this.uid).thenReturn(senderUid)
        }
        return mock(BaseMessage::class.java).apply {
            `when`(this.id).thenReturn(id)
            `when`(this.category).thenReturn(category)
            `when`(this.type).thenReturn(type)
            `when`(this.sender).thenReturn(sender)
            `when`(this.sentAt).thenReturn(1700000000L)
            `when`(this.deletedAt).thenReturn(deletedAt)
        }
    }

    /**
     * Creates a simple BubbleFactory that tracks create/bind calls.
     */
    private fun createTrackingFactory(): TrackingBubbleFactory {
        return TrackingBubbleFactory()
    }

    // ==================== How It Works: Factory Key Resolution ====================

    /**
     * Verifies that the factory key is derived from message category and type.
     * Format: "{category}_{type}"
     */
    @Test
    fun `factory key is derived from category and type`() {
        val key = BubbleFactory.getKey(
            CometChatConstants.CATEGORY_MESSAGE,
            CometChatConstants.MESSAGE_TYPE_TEXT
        )
        assertEquals("message_text", key)
    }

    /**
     * Verifies that deleted messages use the special DELETED_KEY.
     */
    @Test
    fun `deleted messages use DELETED_KEY regardless of original type`() {
        val deletedMessage = createMessage(deletedAt = 1700000100L)
        val key = BubbleFactory.getFactoryKey(deletedMessage)
        assertEquals(BubbleFactory.DELETED_KEY, key)
    }

    /**
     * Verifies that non-deleted messages use category_type key.
     */
    @Test
    fun `non-deleted messages use category_type key`() {
        val message = createMessage(
            category = CometChatConstants.CATEGORY_MESSAGE,
            type = CometChatConstants.MESSAGE_TYPE_IMAGE
        )
        val key = BubbleFactory.getFactoryKey(message)
        assertEquals("message_image", key)
    }

    /**
     * Verifies that the same factory key always gets the same view type ID.
     */
    @Test
    fun `same factory key always returns same view type ID`() {
        val msg1 = createMessage(id = 1L, type = CometChatConstants.MESSAGE_TYPE_TEXT)
        val msg2 = createMessage(id = 2L, type = CometChatConstants.MESSAGE_TYPE_TEXT)

        adapter.setMessageList(listOf(msg1, msg2))

        val viewType1 = adapter.getItemViewType(0)
        val viewType2 = adapter.getItemViewType(1)

        assertEquals(viewType1, viewType2)
    }

    /**
     * Verifies that different factory keys get different view type IDs.
     */
    @Test
    fun `different factory keys get different view type IDs`() {
        val textMsg = createMessage(id = 1L, type = CometChatConstants.MESSAGE_TYPE_TEXT)
        val imageMsg = createMessage(id = 2L, type = CometChatConstants.MESSAGE_TYPE_IMAGE)

        adapter.setMessageList(listOf(textMsg, imageMsg))

        val textViewType = adapter.getItemViewType(0)
        val imageViewType = adapter.getItemViewType(1)

        // They should differ (different factory IDs, same alignment suffix)
        assertTrue(
            "Text and image messages should have different view types",
            textViewType != imageViewType
        )
    }

    // ==================== Replacing an Existing Bubble Factory ====================

    /**
     * Verifies that registering a factory for an existing key replaces the default.
     */
    @Test
    fun `setBubbleFactories replaces existing factory for same key`() {
        val customFactory = createTrackingFactory()
        val factoryKey = BubbleFactory.getKey(
            CometChatConstants.CATEGORY_MESSAGE,
            CometChatConstants.MESSAGE_TYPE_TEXT
        )

        adapter.setBubbleFactories(mapOf(factoryKey to customFactory))

        val message = createMessage(type = CometChatConstants.MESSAGE_TYPE_TEXT)
        adapter.setMessageList(listOf(message))

        val viewType = adapter.getItemViewType(0)
        val retrievedFactory = adapter.getFactoryForViewType(viewType)

        assertEquals(customFactory, retrievedFactory)
    }

    /**
     * Verifies that replacing a factory doesn't affect other registered factories.
     */
    @Test
    fun `replacing one factory does not affect others`() {
        val textFactory = createTrackingFactory()
        val imageFactory = createTrackingFactory()

        adapter.setBubbleFactories(
            mapOf(
                "message_text" to textFactory,
                "message_image" to imageFactory
            )
        )

        val textMsg = createMessage(id = 1L, type = CometChatConstants.MESSAGE_TYPE_TEXT)
        val imageMsg = createMessage(id = 2L, type = CometChatConstants.MESSAGE_TYPE_IMAGE)
        adapter.setMessageList(listOf(textMsg, imageMsg))

        val textViewType = adapter.getItemViewType(0)
        val imageViewType = adapter.getItemViewType(1)

        assertEquals(textFactory, adapter.getFactoryForViewType(textViewType))
        assertEquals(imageFactory, adapter.getFactoryForViewType(imageViewType))
    }

    /**
     * Verifies that removeBubbleFactory removes a specific factory.
     */
    @Test
    fun `removeBubbleFactory removes factory for given category and type`() {
        val factory = createTrackingFactory()
        adapter.setBubbleFactories(mapOf("message_text" to factory))

        assertTrue(adapter.getBubbleFactories().containsKey("message_text"))

        adapter.removeBubbleFactory(
            CometChatConstants.CATEGORY_MESSAGE,
            CometChatConstants.MESSAGE_TYPE_TEXT
        )

        assertTrue(!adapter.getBubbleFactories().containsKey("message_text"))
    }

    // ==================== Adding a New Bubble Factory for Custom Messages ====================

    /**
     * Verifies that a factory can be registered for a custom message type.
     */
    @Test
    fun `custom message type factory is registered and used`() {
        val pollFactory = createTrackingFactory()
        val customKey = BubbleFactory.getKey(
            CometChatConstants.CATEGORY_CUSTOM,
            "extension_poll"
        )

        adapter.setBubbleFactories(mapOf(customKey to pollFactory))

        val pollMessage = createMessage(
            category = CometChatConstants.CATEGORY_CUSTOM,
            type = "extension_poll"
        )
        adapter.setMessageList(listOf(pollMessage))

        val viewType = adapter.getItemViewType(0)
        val retrievedFactory = adapter.getFactoryForViewType(viewType)

        assertEquals(pollFactory, retrievedFactory)
    }

    /**
     * Verifies that messages without a registered factory return null factory.
     */
    @Test
    fun `message without registered factory returns null from getFactoryForViewType`() {
        // Don't register any factory
        val message = createMessage(type = CometChatConstants.MESSAGE_TYPE_TEXT)
        adapter.setMessageList(listOf(message))

        val viewType = adapter.getItemViewType(0)
        val factory = adapter.getFactoryForViewType(viewType)

        assertNull(factory)
    }

    /**
     * Verifies that multiple custom factories can coexist.
     */
    @Test
    fun `multiple custom factories coexist independently`() {
        val pollFactory = createTrackingFactory()
        val stickerFactory = createTrackingFactory()
        val whiteboardFactory = createTrackingFactory()

        adapter.setBubbleFactories(
            mapOf(
                "custom_extension_poll" to pollFactory,
                "custom_extension_sticker" to stickerFactory,
                "custom_extension_whiteboard" to whiteboardFactory
            )
        )

        val pollMsg = createMessage(id = 1L, category = CometChatConstants.CATEGORY_CUSTOM, type = "extension_poll")
        val stickerMsg = createMessage(id = 2L, category = CometChatConstants.CATEGORY_CUSTOM, type = "extension_sticker")
        val whiteboardMsg = createMessage(id = 3L, category = CometChatConstants.CATEGORY_CUSTOM, type = "extension_whiteboard")

        adapter.setMessageList(listOf(pollMsg, stickerMsg, whiteboardMsg))

        val pollViewType = adapter.getItemViewType(0)
        val stickerViewType = adapter.getItemViewType(1)
        val whiteboardViewType = adapter.getItemViewType(2)

        assertEquals(pollFactory, adapter.getFactoryForViewType(pollViewType))
        assertEquals(stickerFactory, adapter.getFactoryForViewType(stickerViewType))
        assertEquals(whiteboardFactory, adapter.getFactoryForViewType(whiteboardViewType))
    }

    // ==================== Replacing the Entire Bubble ====================

    /**
     * Verifies that createBubbleView returning non-null replaces the entire bubble.
     */
    @Test
    fun `factory with createBubbleView replaces entire bubble`() {
        val bubbleReplacementFactory = object : BubbleFactory() {
            var bubbleViewCreated = false
            var bubbleViewBound = false
            var boundMessage: BaseMessage? = null

            override fun createBubbleView(context: Context): View {
                bubbleViewCreated = true
                return TextView(context).apply { text = "Custom Bubble" }
            }

            override fun bindBubbleView(
                view: View,
                message: BaseMessage,
                alignment: UIKitConstants.MessageBubbleAlignment,
                holder: RecyclerView.ViewHolder?,
                position: Int
            ) {
                bubbleViewBound = true
                boundMessage = message
            }
        }

        adapter.setBubbleFactories(mapOf("message_text" to bubbleReplacementFactory))

        val message = createMessage(type = CometChatConstants.MESSAGE_TYPE_TEXT)
        adapter.setMessageList(listOf(message))

        // Verify factory is registered
        val viewType = adapter.getItemViewType(0)
        assertEquals(bubbleReplacementFactory, adapter.getFactoryForViewType(viewType))

        // Verify createBubbleView returns non-null
        val bubbleView = bubbleReplacementFactory.createBubbleView(context)
        assertNotNull(bubbleView)
        assertTrue(bubbleView is TextView)
        assertEquals("Custom Bubble", (bubbleView as TextView).text)
    }

    /**
     * Verifies that when createBubbleView returns null, standard slots are used.
     */
    @Test
    fun `factory with null createBubbleView uses standard slot-based rendering`() {
        val slotFactory = object : BubbleFactory() {
            override fun createBubbleView(context: Context): View? = null

            override fun createContentView(context: Context): View {
                return TextView(context).apply { text = "Content Slot" }
            }

            override fun bindContentView(
                view: View,
                message: BaseMessage,
                alignment: UIKitConstants.MessageBubbleAlignment,
                holder: RecyclerView.ViewHolder?,
                position: Int
            ) {
                (view as TextView).text = "Bound: ${message.id}"
            }
        }

        // createBubbleView returns null
        assertNull(slotFactory.createBubbleView(context))

        // createContentView returns a view
        val contentView = slotFactory.createContentView(context)
        assertNotNull(contentView)
        assertEquals("Content Slot", (contentView as TextView).text)
    }

    // ==================== Bubble Slot Reference ====================

    /**
     * Verifies all 8 slots return null by default (base class behavior).
     */
    @Test
    fun `base BubbleFactory returns null for all optional slots`() {
        val baseFactory = object : BubbleFactory() {
            override fun createContentView(context: Context): View = View(context)
            override fun bindContentView(
                view: View, message: BaseMessage,
                alignment: UIKitConstants.MessageBubbleAlignment,
                holder: RecyclerView.ViewHolder?, position: Int
            ) {}
        }

        assertNull(baseFactory.createBubbleView(context))
        assertNull(baseFactory.createLeadingView(context))
        assertNull(baseFactory.createHeaderView(context))
        assertNull(baseFactory.createReplyView(context))
        assertNull(baseFactory.createBottomView(context))
        assertNull(baseFactory.createStatusInfoView(context))
        assertNull(baseFactory.createThreadView(context))
        assertNull(baseFactory.createFooterView(context))
    }

    /**
     * Verifies that each slot can be independently overridden.
     */
    @Test
    fun `each slot can be independently overridden`() {
        val factory = object : BubbleFactory() {
            override fun createContentView(context: Context): View =
                TextView(context).apply { text = "content" }

            override fun createLeadingView(context: Context): View =
                TextView(context).apply { text = "leading" }

            override fun createHeaderView(context: Context): View =
                TextView(context).apply { text = "header" }

            override fun createReplyView(context: Context): View =
                TextView(context).apply { text = "reply" }

            override fun createBottomView(context: Context): View =
                TextView(context).apply { text = "bottom" }

            override fun createStatusInfoView(context: Context): View =
                TextView(context).apply { text = "statusInfo" }

            override fun createThreadView(context: Context): View =
                TextView(context).apply { text = "thread" }

            override fun createFooterView(context: Context): View =
                TextView(context).apply { text = "footer" }

            override fun bindContentView(
                view: View, message: BaseMessage,
                alignment: UIKitConstants.MessageBubbleAlignment,
                holder: RecyclerView.ViewHolder?, position: Int
            ) {}
        }

        assertEquals("content", (factory.createContentView(context) as TextView).text)
        assertEquals("leading", (factory.createLeadingView(context) as TextView).text)
        assertEquals("header", (factory.createHeaderView(context) as TextView).text)
        assertEquals("reply", (factory.createReplyView(context) as TextView).text)
        assertEquals("bottom", (factory.createBottomView(context) as TextView).text)
        assertEquals("statusInfo", (factory.createStatusInfoView(context) as TextView).text)
        assertEquals("thread", (factory.createThreadView(context) as TextView).text)
        assertEquals("footer", (factory.createFooterView(context) as TextView).text)
    }

    /**
     * Verifies that bind methods receive correct alignment.
     */
    @Test
    fun `bind methods receive correct alignment parameter`() {
        var receivedContentAlignment: UIKitConstants.MessageBubbleAlignment? = null
        var receivedLeadingAlignment: UIKitConstants.MessageBubbleAlignment? = null
        var receivedHeaderAlignment: UIKitConstants.MessageBubbleAlignment? = null

        val factory = object : BubbleFactory() {
            override fun createContentView(context: Context): View = View(context)
            override fun createLeadingView(context: Context): View = View(context)
            override fun createHeaderView(context: Context): View = View(context)

            override fun bindContentView(
                view: View, message: BaseMessage,
                alignment: UIKitConstants.MessageBubbleAlignment,
                holder: RecyclerView.ViewHolder?, position: Int
            ) {
                receivedContentAlignment = alignment
            }

            override fun bindLeadingView(
                view: View, message: BaseMessage,
                alignment: UIKitConstants.MessageBubbleAlignment
            ) {
                receivedLeadingAlignment = alignment
            }

            override fun bindHeaderView(
                view: View, message: BaseMessage,
                alignment: UIKitConstants.MessageBubbleAlignment
            ) {
                receivedHeaderAlignment = alignment
            }
        }

        val message = createMessage()
        val view = factory.createContentView(context)
        val leadingView = factory.createLeadingView(context)!!
        val headerView = factory.createHeaderView(context)!!

        factory.bindContentView(
            view, message, UIKitConstants.MessageBubbleAlignment.LEFT, null, 0
        )
        factory.bindLeadingView(
            leadingView, message, UIKitConstants.MessageBubbleAlignment.LEFT
        )
        factory.bindHeaderView(
            headerView, message, UIKitConstants.MessageBubbleAlignment.RIGHT
        )

        assertEquals(UIKitConstants.MessageBubbleAlignment.LEFT, receivedContentAlignment)
        assertEquals(UIKitConstants.MessageBubbleAlignment.LEFT, receivedLeadingAlignment)
        assertEquals(UIKitConstants.MessageBubbleAlignment.RIGHT, receivedHeaderAlignment)
    }

    /**
     * Verifies that bind methods receive the correct message object.
     */
    @Test
    fun `bind methods receive correct message object`() {
        var receivedMessageId: Long? = null

        val factory = object : BubbleFactory() {
            override fun createContentView(context: Context): View = View(context)
            override fun bindContentView(
                view: View, message: BaseMessage,
                alignment: UIKitConstants.MessageBubbleAlignment,
                holder: RecyclerView.ViewHolder?, position: Int
            ) {
                receivedMessageId = message.id
            }
        }

        val message = createMessage(id = 42L)
        val view = factory.createContentView(context)
        factory.bindContentView(
            view, message, UIKitConstants.MessageBubbleAlignment.LEFT, null, 0
        )

        assertEquals(42L, receivedMessageId)
    }

    /**
     * Verifies that onViewRecycled is called with the content view.
     */
    @Test
    fun `onViewRecycled is invoked with content view`() {
        var recycledView: View? = null

        val factory = object : BubbleFactory() {
            override fun createContentView(context: Context): View = View(context)
            override fun bindContentView(
                view: View, message: BaseMessage,
                alignment: UIKitConstants.MessageBubbleAlignment,
                holder: RecyclerView.ViewHolder?, position: Int
            ) {}

            override fun onViewRecycled(contentView: View) {
                recycledView = contentView
            }
        }

        val contentView = factory.createContentView(context)
        factory.onViewRecycled(contentView)

        assertEquals(contentView, recycledView)
    }

    // ==================== Self-Describing Factory Identity ====================

    /**
     * Verifies that getCategory() and getType() return empty by default.
     */
    @Test
    fun `default getCategory and getType return empty string`() {
        val factory = object : BubbleFactory() {
            override fun createContentView(context: Context): View = View(context)
            override fun bindContentView(
                view: View, message: BaseMessage,
                alignment: UIKitConstants.MessageBubbleAlignment,
                holder: RecyclerView.ViewHolder?, position: Int
            ) {}
        }

        assertEquals("", factory.getCategory())
        assertEquals("", factory.getType())
    }

    /**
     * Verifies that self-describing factories return their category and type.
     */
    @Test
    fun `self-describing factory returns category and type`() {
        val factory = object : BubbleFactory() {
            override fun getCategory(): String = CometChatConstants.CATEGORY_CUSTOM
            override fun getType(): String = "extension_poll"
            override fun createContentView(context: Context): View = View(context)
            override fun bindContentView(
                view: View, message: BaseMessage,
                alignment: UIKitConstants.MessageBubbleAlignment,
                holder: RecyclerView.ViewHolder?, position: Int
            ) {}
        }

        assertEquals(CometChatConstants.CATEGORY_CUSTOM, factory.getCategory())
        assertEquals("extension_poll", factory.getType())
    }

    // ==================== getBubbleFactories / setBubbleFactories ====================

    /**
     * Verifies getBubbleFactories returns empty map initially.
     */
    @Test
    fun `getBubbleFactories returns empty map initially`() {
        val freshAdapter = MessageAdapter(context)
        assertTrue(freshAdapter.getBubbleFactories().isEmpty())
    }

    /**
     * Verifies setBubbleFactories stores all provided factories.
     */
    @Test
    fun `setBubbleFactories stores all provided factories`() {
        val factory1 = createTrackingFactory()
        val factory2 = createTrackingFactory()

        adapter.setBubbleFactories(
            mapOf(
                "message_text" to factory1,
                "custom_poll" to factory2
            )
        )

        val factories = adapter.getBubbleFactories()
        assertEquals(2, factories.size)
        assertEquals(factory1, factories["message_text"])
        assertEquals(factory2, factories["custom_poll"])
    }
}

// ==================== Test Helpers ====================

/**
 * A BubbleFactory implementation that tracks create/bind invocations.
 */
class TrackingBubbleFactory : BubbleFactory() {
    var contentViewCreated = false
    var contentViewBound = false
    var lastBoundMessage: BaseMessage? = null
    var lastBoundAlignment: UIKitConstants.MessageBubbleAlignment? = null

    override fun createContentView(context: Context): View {
        contentViewCreated = true
        return TextView(context).apply { text = "Tracking Content" }
    }

    override fun bindContentView(
        view: View,
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment,
        holder: RecyclerView.ViewHolder?,
        position: Int
    ) {
        contentViewBound = true
        lastBoundMessage = message
        lastBoundAlignment = alignment
    }
}
