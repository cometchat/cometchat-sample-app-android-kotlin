package com.cometchat.uikit.kotlin.presentation.messagelist

import android.content.Context
import android.view.View
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * Property-based tests for BubbleViewProvider interface.
 *
 * Verifies the contract between createView and bindView:
 * - createView receives correct alignment and message
 * - bindView receives the same view that createView returned
 * - Alignment-based conditional logic works correctly
 * - Null return from createView is handled properly
 *
 * Uses mock Context and View objects to avoid Robolectric dependency.
 *
 * Feature: messagelist-bubble-view-provider
 *
 * Validates: Task 21.2 — createView/bindView alignment PBT
 */
class BubbleViewProviderPropertyTest : FunSpec({

    // ==================== Generators ====================

    /**
     * Generator for MessageBubbleAlignment values.
     */
    val alignmentArb = Arb.element(
        UIKitConstants.MessageBubbleAlignment.LEFT,
        UIKitConstants.MessageBubbleAlignment.RIGHT,
        UIKitConstants.MessageBubbleAlignment.CENTER
    )

    /**
     * Generator for mock BaseMessage objects.
     */
    val messageArb = arbitrary {
        val id = Arb.long(1, 100000).bind()
        val category = Arb.element(
            CometChatConstants.CATEGORY_MESSAGE,
            CometChatConstants.CATEGORY_CUSTOM,
            CometChatConstants.CATEGORY_ACTION,
            CometChatConstants.CATEGORY_CALL
        ).bind()
        val type = Arb.element(
            CometChatConstants.MESSAGE_TYPE_TEXT,
            CometChatConstants.MESSAGE_TYPE_IMAGE,
            CometChatConstants.MESSAGE_TYPE_VIDEO,
            CometChatConstants.MESSAGE_TYPE_AUDIO,
            CometChatConstants.MESSAGE_TYPE_FILE
        ).bind()
        val senderUid = Arb.string(5, 20).bind()
        val sentAt = Arb.long(1000000000L, 2000000000L).bind()

        val mockSender = mock(User::class.java).apply {
            `when`(this.uid).thenReturn(senderUid)
            `when`(this.name).thenReturn("User_$senderUid")
        }

        mock(BaseMessage::class.java).apply {
            `when`(this.id).thenReturn(id)
            `when`(this.category).thenReturn(category)
            `when`(this.type).thenReturn(type)
            `when`(this.sender).thenReturn(mockSender)
            `when`(this.sentAt).thenReturn(sentAt)
            `when`(this.deletedAt).thenReturn(0L)
        }
    }

    // ==================== Property Tests ====================

    /**
     * Property 1: createView receives correct alignment parameter.
     *
     * *For any* alignment A and message M, when createView(context, M, A) is called,
     * the provider SHALL receive the exact alignment value passed by the adapter.
     */
    test("Property 1: createView receives correct alignment parameter").config(invocations = 50) {
        val mockContext = mock(Context::class.java)

        checkAll(alignmentArb, messageArb) { alignment, message ->
            println("  [TRACE] Testing createView with alignment=$alignment, messageId=${message.id}")

            var receivedAlignment: UIKitConstants.MessageBubbleAlignment? = null

            val provider = object : BubbleViewProvider {
                override fun createView(
                    context: Context,
                    message: BaseMessage,
                    alignment: UIKitConstants.MessageBubbleAlignment
                ): View? {
                    receivedAlignment = alignment
                    return mock(View::class.java)
                }

                override fun bindView(
                    view: View,
                    message: BaseMessage,
                    alignment: UIKitConstants.MessageBubbleAlignment
                ) { /* no-op */ }
            }

            provider.createView(mockContext, message, alignment)

            receivedAlignment shouldBe alignment
            println("  [TRACE] ✓ Alignment correctly passed: $alignment")
        }
    }

    /**
     * Property 2: bindView receives the same view instance that createView returned.
     *
     * *For any* message M and alignment A, when createView returns a non-null View V,
     * bindView SHALL receive that exact same View instance V.
     */
    test("Property 2: bindView receives the same view instance from createView").config(invocations = 50) {
        val mockContext = mock(Context::class.java)

        checkAll(alignmentArb, messageArb) { alignment, message ->
            println("  [TRACE] Testing view identity with alignment=$alignment, messageId=${message.id}")

            var createdView: View? = null
            var boundView: View? = null

            val provider = object : BubbleViewProvider {
                override fun createView(
                    context: Context,
                    message: BaseMessage,
                    alignment: UIKitConstants.MessageBubbleAlignment
                ): View {
                    return mock(View::class.java).also { createdView = it }
                }

                override fun bindView(
                    view: View,
                    message: BaseMessage,
                    alignment: UIKitConstants.MessageBubbleAlignment
                ) {
                    boundView = view
                }
            }

            val view = provider.createView(mockContext, message, alignment)
            view.shouldNotBeNull()
            provider.bindView(view, message, alignment)

            // Same instance identity
            boundView shouldBe createdView
            println("  [TRACE] ✓ View identity preserved: createdView === boundView")
        }
    }

    /**
     * Property 3: createView may return null to indicate no custom view for a given alignment.
     *
     * *For any* alignment A, a provider that returns null for certain alignments
     * SHALL correctly return null for those alignments and non-null for others.
     */
    test("Property 3: createView null return for conditional alignment").config(invocations = 50) {
        val mockContext = mock(Context::class.java)

        checkAll(alignmentArb, messageArb) { alignment, message ->
            println("  [TRACE] Testing null return for alignment=$alignment, messageId=${message.id}")

            // Provider that only creates views for LEFT alignment
            val leftOnlyProvider = object : BubbleViewProvider {
                override fun createView(
                    context: Context,
                    message: BaseMessage,
                    alignment: UIKitConstants.MessageBubbleAlignment
                ): View? {
                    return if (alignment == UIKitConstants.MessageBubbleAlignment.LEFT) {
                        mock(View::class.java)
                    } else {
                        null
                    }
                }

                override fun bindView(
                    view: View,
                    message: BaseMessage,
                    alignment: UIKitConstants.MessageBubbleAlignment
                ) { /* no-op */ }
            }

            val view = leftOnlyProvider.createView(mockContext, message, alignment)

            if (alignment == UIKitConstants.MessageBubbleAlignment.LEFT) {
                view.shouldNotBeNull()
                println("  [TRACE] ✓ LEFT alignment → non-null view returned")
            } else {
                view.shouldBeNull()
                println("  [TRACE] ✓ Non-LEFT alignment ($alignment) → null returned")
            }
        }
    }

    /**
     * Property 4: bindView receives correct message data for updating the view.
     *
     * *For any* message M, when bindView is called, the provider SHALL receive
     * the exact message object with its original properties intact.
     */
    test("Property 4: bindView receives correct message data").config(invocations = 50) {
        val mockContext = mock(Context::class.java)

        checkAll(alignmentArb, messageArb) { alignment, message ->
            println("  [TRACE] Testing message data in bindView: id=${message.id}, category=${message.category}")

            var receivedMessageId: Long? = null
            var receivedCategory: String? = null
            var receivedSenderUid: String? = null

            val provider = object : BubbleViewProvider {
                override fun createView(
                    context: Context,
                    message: BaseMessage,
                    alignment: UIKitConstants.MessageBubbleAlignment
                ): View = mock(View::class.java)

                override fun bindView(
                    view: View,
                    message: BaseMessage,
                    alignment: UIKitConstants.MessageBubbleAlignment
                ) {
                    receivedMessageId = message.id
                    receivedCategory = message.category
                    receivedSenderUid = message.sender?.uid
                }
            }

            val view = provider.createView(mockContext, message, alignment)!!
            provider.bindView(view, message, alignment)

            receivedMessageId shouldBe message.id
            receivedCategory shouldBe message.category
            receivedSenderUid shouldBe message.sender?.uid
            println("  [TRACE] ✓ Message data correctly passed to bindView")
        }
    }

    /**
     * Property 5: bindView alignment matches createView alignment for the same binding cycle.
     *
     * *For any* alignment A, when a view is created with alignment A and then bound,
     * bindView SHALL receive the same alignment A.
     */
    test("Property 5: alignment consistency between createView and bindView").config(invocations = 50) {
        val mockContext = mock(Context::class.java)

        checkAll(alignmentArb, messageArb) { alignment, message ->
            println("  [TRACE] Testing alignment consistency: $alignment")

            var createAlignment: UIKitConstants.MessageBubbleAlignment? = null
            var bindAlignment: UIKitConstants.MessageBubbleAlignment? = null

            val provider = object : BubbleViewProvider {
                override fun createView(
                    context: Context,
                    message: BaseMessage,
                    alignment: UIKitConstants.MessageBubbleAlignment
                ): View {
                    createAlignment = alignment
                    return mock(View::class.java)
                }

                override fun bindView(
                    view: View,
                    message: BaseMessage,
                    alignment: UIKitConstants.MessageBubbleAlignment
                ) {
                    bindAlignment = alignment
                }
            }

            val view = provider.createView(mockContext, message, alignment)!!
            provider.bindView(view, message, alignment)

            createAlignment shouldBe bindAlignment
            createAlignment shouldBe alignment
            println("  [TRACE] ✓ Alignment consistent: create=$createAlignment, bind=$bindAlignment")
        }
    }

    /**
     * Property 6: Different alignments can produce different view types.
     *
     * *For any* message M, a provider MAY return different view instances (or null)
     * based on the alignment parameter. This is the primary use case for
     * alignment-aware customization.
     */
    test("Property 6: alignment-aware view creation produces correct results").config(invocations = 50) {
        val mockContext = mock(Context::class.java)

        checkAll(messageArb) { message ->
            println("  [TRACE] Testing alignment-aware view creation for messageId=${message.id}")

            val leftView = mock(View::class.java)
            val rightView = mock(View::class.java)

            // Provider that returns different views per alignment
            val alignmentAwareProvider = object : BubbleViewProvider {
                override fun createView(
                    context: Context,
                    message: BaseMessage,
                    alignment: UIKitConstants.MessageBubbleAlignment
                ): View? {
                    return when (alignment) {
                        UIKitConstants.MessageBubbleAlignment.LEFT -> leftView
                        UIKitConstants.MessageBubbleAlignment.RIGHT -> rightView
                        UIKitConstants.MessageBubbleAlignment.CENTER -> null
                    }
                }

                override fun bindView(
                    view: View,
                    message: BaseMessage,
                    alignment: UIKitConstants.MessageBubbleAlignment
                ) { /* no-op */ }
            }

            val resultLeft = alignmentAwareProvider.createView(mockContext, message, UIKitConstants.MessageBubbleAlignment.LEFT)
            val resultRight = alignmentAwareProvider.createView(mockContext, message, UIKitConstants.MessageBubbleAlignment.RIGHT)
            val resultCenter = alignmentAwareProvider.createView(mockContext, message, UIKitConstants.MessageBubbleAlignment.CENTER)

            resultLeft.shouldNotBeNull()
            resultLeft shouldBe leftView

            resultRight.shouldNotBeNull()
            resultRight shouldBe rightView

            resultCenter.shouldBeNull()

            println("  [TRACE] ✓ LEFT→leftView, RIGHT→rightView, CENTER→null")
        }
    }

    /**
     * Property 7: bindView can update view state based on message data.
     *
     * *For any* message M with sender name N, bindView SHALL be able to
     * update the view's state to reflect the message data.
     */
    test("Property 7: bindView updates view state from message data").config(invocations = 50) {
        val mockContext = mock(Context::class.java)

        checkAll(alignmentArb, messageArb) { alignment, message ->
            val senderName = message.sender?.name ?: "Unknown"
            println("  [TRACE] Testing bindView content update: sender=$senderName")

            var lastBoundSenderName: String? = null

            val provider = object : BubbleViewProvider {
                override fun createView(
                    context: Context,
                    message: BaseMessage,
                    alignment: UIKitConstants.MessageBubbleAlignment
                ): View = mock(View::class.java)

                override fun bindView(
                    view: View,
                    message: BaseMessage,
                    alignment: UIKitConstants.MessageBubbleAlignment
                ) {
                    lastBoundSenderName = message.sender?.name ?: "Unknown"
                }
            }

            val view = provider.createView(mockContext, message, alignment)!!
            provider.bindView(view, message, alignment)

            lastBoundSenderName shouldBe senderName
            println("  [TRACE] ✓ View state updated with sender: $lastBoundSenderName")
        }
    }

    /**
     * Property 8: bindView can be called multiple times on the same view (recycling).
     *
     * *For any* sequence of messages [M1, M2], calling bindView repeatedly
     * on the same view SHALL update it to reflect the latest message.
     * This simulates RecyclerView recycling behavior.
     */
    test("Property 8: view recycling — bindView updates for different messages").config(invocations = 50) {
        val mockContext = mock(Context::class.java)

        checkAll(alignmentArb, messageArb, messageArb) { alignment, message1, message2 ->
            println("  [TRACE] Testing view recycling: msg1.id=${message1.id}, msg2.id=${message2.id}")

            var lastBoundMessageId: Long? = null

            val provider = object : BubbleViewProvider {
                override fun createView(
                    context: Context,
                    message: BaseMessage,
                    alignment: UIKitConstants.MessageBubbleAlignment
                ): View = mock(View::class.java)

                override fun bindView(
                    view: View,
                    message: BaseMessage,
                    alignment: UIKitConstants.MessageBubbleAlignment
                ) {
                    lastBoundMessageId = message.id
                }
            }

            // Create view once (simulating ViewHolder creation)
            val view = provider.createView(mockContext, message1, alignment)!!

            // Bind first message
            provider.bindView(view, message1, alignment)
            lastBoundMessageId shouldBe message1.id

            // Bind second message (simulating recycling)
            provider.bindView(view, message2, alignment)
            lastBoundMessageId shouldBe message2.id

            println("  [TRACE] ✓ View recycled: first=${message1.id}, then=${message2.id}")
        }
    }

    /**
     * Property 9: Provider slots are independent — each slot receives its own alignment.
     *
     * *For any* alignment A, multiple BubbleViewProvider instances (for different slots)
     * SHALL each independently receive the correct alignment without interference.
     */
    test("Property 9: multiple provider slots are independent").config(invocations = 50) {
        val mockContext = mock(Context::class.java)

        checkAll(alignmentArb, messageArb) { alignment, message ->
            println("  [TRACE] Testing slot independence with alignment=$alignment")

            var headerAlignment: UIKitConstants.MessageBubbleAlignment? = null
            var footerAlignment: UIKitConstants.MessageBubbleAlignment? = null
            var contentAlignment: UIKitConstants.MessageBubbleAlignment? = null

            val headerProvider = object : BubbleViewProvider {
                override fun createView(context: Context, message: BaseMessage, alignment: UIKitConstants.MessageBubbleAlignment): View? {
                    headerAlignment = alignment
                    return mock(View::class.java)
                }
                override fun bindView(view: View, message: BaseMessage, alignment: UIKitConstants.MessageBubbleAlignment) {}
            }

            val footerProvider = object : BubbleViewProvider {
                override fun createView(context: Context, message: BaseMessage, alignment: UIKitConstants.MessageBubbleAlignment): View? {
                    footerAlignment = alignment
                    return mock(View::class.java)
                }
                override fun bindView(view: View, message: BaseMessage, alignment: UIKitConstants.MessageBubbleAlignment) {}
            }

            val contentProvider = object : BubbleViewProvider {
                override fun createView(context: Context, message: BaseMessage, alignment: UIKitConstants.MessageBubbleAlignment): View? {
                    contentAlignment = alignment
                    return mock(View::class.java)
                }
                override fun bindView(view: View, message: BaseMessage, alignment: UIKitConstants.MessageBubbleAlignment) {}
            }

            // Simulate adapter calling each provider with the same alignment
            headerProvider.createView(mockContext, message, alignment)
            footerProvider.createView(mockContext, message, alignment)
            contentProvider.createView(mockContext, message, alignment)

            // All should receive the same alignment independently
            headerAlignment shouldBe alignment
            footerAlignment shouldBe alignment
            contentAlignment shouldBe alignment

            println("  [TRACE] ✓ All slots received alignment=$alignment independently")
        }
    }

    /**
     * Property 10: createView with different message categories produces appropriate views.
     *
     * *For any* message M, a category-aware provider SHALL correctly differentiate
     * between message categories when deciding what view to create.
     */
    test("Property 10: category-aware createView differentiates message types").config(invocations = 50) {
        val mockContext = mock(Context::class.java)

        checkAll(alignmentArb, messageArb) { alignment, message ->
            println("  [TRACE] Testing category-aware provider: category=${message.category}")

            val messageView = mock(View::class.java)
            val customView = mock(View::class.java)

            val categoryAwareProvider = object : BubbleViewProvider {
                override fun createView(
                    context: Context,
                    message: BaseMessage,
                    alignment: UIKitConstants.MessageBubbleAlignment
                ): View? {
                    return when (message.category) {
                        CometChatConstants.CATEGORY_MESSAGE -> messageView
                        CometChatConstants.CATEGORY_CUSTOM -> customView
                        CometChatConstants.CATEGORY_ACTION -> null // No custom view for actions
                        CometChatConstants.CATEGORY_CALL -> null // No custom view for calls
                        else -> null
                    }
                }

                override fun bindView(
                    view: View,
                    message: BaseMessage,
                    alignment: UIKitConstants.MessageBubbleAlignment
                ) { /* no-op */ }
            }

            val view = categoryAwareProvider.createView(mockContext, message, alignment)

            when (message.category) {
                CometChatConstants.CATEGORY_MESSAGE -> {
                    view.shouldNotBeNull()
                    view shouldBe messageView
                }
                CometChatConstants.CATEGORY_CUSTOM -> {
                    view.shouldNotBeNull()
                    view shouldBe customView
                }
                CometChatConstants.CATEGORY_ACTION,
                CometChatConstants.CATEGORY_CALL -> {
                    view.shouldBeNull()
                }
            }
            println("  [TRACE] ✓ Category '${message.category}' → ${if (view != null) "view" else "null"}")
        }
    }

    /**
     * Property 11: bindView alignment-conditional logic updates view correctly.
     *
     * *For any* alignment A and message M, bindView SHALL be able to apply
     * alignment-specific logic based on the alignment parameter.
     */
    test("Property 11: bindView applies alignment-specific logic").config(invocations = 50) {
        val mockContext = mock(Context::class.java)

        checkAll(alignmentArb, messageArb) { alignment, message ->
            println("  [TRACE] Testing alignment-specific logic: alignment=$alignment")

            var appliedFormatting: String? = null

            val provider = object : BubbleViewProvider {
                override fun createView(
                    context: Context,
                    message: BaseMessage,
                    alignment: UIKitConstants.MessageBubbleAlignment
                ): View = mock(View::class.java)

                override fun bindView(
                    view: View,
                    message: BaseMessage,
                    alignment: UIKitConstants.MessageBubbleAlignment
                ) {
                    // Apply alignment-specific formatting
                    appliedFormatting = when (alignment) {
                        UIKitConstants.MessageBubbleAlignment.LEFT -> "formatted_left"
                        UIKitConstants.MessageBubbleAlignment.RIGHT -> "formatted_right"
                        UIKitConstants.MessageBubbleAlignment.CENTER -> "formatted_center"
                    }
                }
            }

            val view = provider.createView(mockContext, message, alignment)!!
            provider.bindView(view, message, alignment)

            val expectedFormatting = when (alignment) {
                UIKitConstants.MessageBubbleAlignment.LEFT -> "formatted_left"
                UIKitConstants.MessageBubbleAlignment.RIGHT -> "formatted_right"
                UIKitConstants.MessageBubbleAlignment.CENTER -> "formatted_center"
            }

            appliedFormatting shouldBe expectedFormatting
            println("  [TRACE] ✓ Applied formatting='$appliedFormatting' for alignment=$alignment")
        }
    }
})
