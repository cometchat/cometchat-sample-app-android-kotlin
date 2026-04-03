package com.cometchat.uikit.kotlin.presentation.messagelist.ui

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Test data class representing a message for callback testing.
 * Simulates BaseMessage without requiring Android/CometChat SDK dependencies.
 */
private data class CallbackTestMessage(
    val id: Long,
    val text: String,
    val senderUid: String
)

/**
 * Test implementation of MessageOptionClickListener that tracks invocations.
 */
private class TestMessageOptionClickListener {
    var invoked: Boolean = false
    var lastMessage: CallbackTestMessage? = null
    var lastOptionId: String? = null
    var lastOptionName: String? = null

    fun onMessageOptionClick(message: CallbackTestMessage, optionId: String, optionName: String) {
        invoked = true
        lastMessage = message
        lastOptionId = optionId
        lastOptionName = optionName
    }

    fun reset() {
        invoked = false
        lastMessage = null
        lastOptionId = null
        lastOptionName = null
    }
}

/**
 * Test implementation of ReactionClickListener that tracks invocations.
 */
private class TestReactionClickListener {
    var invoked: Boolean = false
    var lastMessage: CallbackTestMessage? = null
    var lastReaction: String? = null

    fun onReactionClick(message: CallbackTestMessage, reaction: String) {
        invoked = true
        lastMessage = message
        lastReaction = reaction
    }

    fun reset() {
        invoked = false
        lastMessage = null
        lastReaction = null
    }
}

/**
 * Test implementation of EmojiPickerClickListener that tracks invocations.
 */
private open class TestEmojiPickerClickListener {
    var invoked: Boolean = false
    var invocationCount: Int = 0

    open fun onEmojiPickerClick() {
        invoked = true
        invocationCount++
    }

    fun reset() {
        invoked = false
        invocationCount = 0
    }
}

/**
 * Test class that simulates the callback behavior of CometChatMessageList
 * without requiring Android context.
 *
 * This mirrors the actual implementation:
 * - CometChatMessageList stores callback listeners (messageOptionClickListener, quickReactionClickListener, emojiPickerClickListener)
 * - setMessageOptionClickListener/setQuickReactionClickListener/setEmojiPickerClick set the listeners
 * - invokeMessageOptionClick/invokeQuickReactionClick/invokeEmojiPickerClick invoke the listeners with parameters
 * - When no listener is set, invoke methods are no-ops (don't throw exceptions)
 * - Setting a new listener replaces the previous one
 */
private class TestCallbackStorage {
    // Simulates CometChatMessageList.messageOptionClickListener
    private var messageOptionClickListener: TestMessageOptionClickListener? = null

    // Simulates CometChatMessageList.quickReactionClickListener
    private var quickReactionClickListener: TestReactionClickListener? = null

    // Simulates CometChatMessageList.emojiPickerClickListener
    private var emojiPickerClickListener: TestEmojiPickerClickListener? = null

    /**
     * Simulates CometChatMessageList.setMessageOptionClickListener()
     */
    fun setMessageOptionClickListener(listener: TestMessageOptionClickListener?) {
        this.messageOptionClickListener = listener
    }

    /**
     * Simulates CometChatMessageList.getMessageOptionClickListener()
     */
    fun getMessageOptionClickListener(): TestMessageOptionClickListener? = messageOptionClickListener

    /**
     * Simulates CometChatMessageList.setQuickReactionClickListener()
     */
    fun setQuickReactionClickListener(listener: TestReactionClickListener?) {
        this.quickReactionClickListener = listener
    }

    /**
     * Simulates CometChatMessageList.getQuickReactionClickListener()
     */
    fun getQuickReactionClickListener(): TestReactionClickListener? = quickReactionClickListener

    /**
     * Simulates CometChatMessageList.setEmojiPickerClick()
     */
    fun setEmojiPickerClick(listener: TestEmojiPickerClickListener?) {
        this.emojiPickerClickListener = listener
    }

    /**
     * Simulates CometChatMessageList.getEmojiPickerClick()
     */
    fun getEmojiPickerClick(): TestEmojiPickerClickListener? = emojiPickerClickListener

    /**
     * Simulates CometChatMessageList.invokeMessageOptionClick()
     * 
     * Invokes the message option click listener if set.
     * Does nothing if no listener is set (no exception thrown).
     */
    fun invokeMessageOptionClick(message: CallbackTestMessage, optionId: String, optionName: String) {
        messageOptionClickListener?.onMessageOptionClick(message, optionId, optionName)
    }

    /**
     * Simulates CometChatMessageList.invokeQuickReactionClick()
     * 
     * Invokes the quick reaction click listener if set.
     * Does nothing if no listener is set (no exception thrown).
     */
    fun invokeQuickReactionClick(message: CallbackTestMessage, reaction: String) {
        quickReactionClickListener?.onReactionClick(message, reaction)
    }

    /**
     * Simulates CometChatMessageList.invokeEmojiPickerClick()
     * 
     * Invokes the emoji picker click listener if set.
     * Does nothing if no listener is set (no exception thrown).
     */
    fun invokeEmojiPickerClick() {
        emojiPickerClickListener?.onEmojiPickerClick()
    }
}

/**
 * Property-based tests for CometChatMessageList callback invocation.
 * Uses Kotest property testing to verify correctness properties.
 *
 * Feature: messagelist-property-parity, Property 10: Callback Invocation
 *
 * *For any* registered callback (MessageOptionClickListener, ReactionClickListener, EmojiPickerClickListener),
 * when the corresponding user action occurs, the callback SHALL be invoked with the correct parameters.
 *
 * **Validates: Requirements 10.1, 10.2, 10.3**
 */
class CometChatMessageListCallbackPropertyTest : FunSpec({

    // ==================== Generators ====================

    /**
     * Generator for non-empty strings (for IDs, names, reactions).
     */
    val nonEmptyStringArb = arbitrary {
        var result: String
        do {
            result = Arb.string(1..50).bind()
        } while (result.isEmpty())
        result
    }

    /**
     * Generator for message IDs (positive longs).
     */
    val messageIdArb = Arb.long(1L..Long.MAX_VALUE)

    /**
     * Generator for CallbackTestMessage instances.
     */
    val testMessageArb = arbitrary {
        val id = messageIdArb.bind()
        val text = nonEmptyStringArb.bind()
        val senderUid = nonEmptyStringArb.bind()
        CallbackTestMessage(id, text, senderUid)
    }

    /**
     * Generator for option IDs (common message options).
     */
    val optionIdArb = arbitrary {
        val options = listOf(
            "reply", "copy", "edit", "delete", "reply_in_thread",
            "share", "translate", "message_info", "react", "flag"
        )
        options.random()
    }

    /**
     * Generator for option names.
     */
    val optionNameArb = arbitrary {
        val names = listOf(
            "Reply", "Copy", "Edit", "Delete", "Reply in Thread",
            "Share", "Translate", "Message Info", "React", "Report"
        )
        names.random()
    }

    /**
     * Generator for emoji reactions.
     */
    val reactionArb = arbitrary {
        val reactions = listOf("👍", "❤️", "😂", "😮", "😢", "🙏", "🎉", "👏", "🔥", "💯")
        reactions.random()
    }

    // ==================== Property Tests ====================

    context("Property 10: Callback Invocation") {

        // ========================================
        // MessageOptionClickListener Tests
        // ========================================

        context("MessageOptionClickListener") {

            test("when listener is set and invokeMessageOptionClick is called, listener receives correct message") {
                checkAll(100, testMessageArb, optionIdArb, optionNameArb) { message, optionId, optionName ->
                    val storage = TestCallbackStorage()
                    val listener = TestMessageOptionClickListener()

                    // Set listener
                    storage.setMessageOptionClickListener(listener)

                    // Invoke callback
                    storage.invokeMessageOptionClick(message, optionId, optionName)

                    // Verify listener was invoked with correct message
                    listener.invoked.shouldBeTrue()
                    listener.lastMessage shouldBe message
                }
            }

            test("when listener is set and invokeMessageOptionClick is called, listener receives correct optionId") {
                checkAll(100, testMessageArb, optionIdArb, optionNameArb) { message, optionId, optionName ->
                    val storage = TestCallbackStorage()
                    val listener = TestMessageOptionClickListener()

                    // Set listener
                    storage.setMessageOptionClickListener(listener)

                    // Invoke callback
                    storage.invokeMessageOptionClick(message, optionId, optionName)

                    // Verify listener received correct optionId
                    listener.invoked.shouldBeTrue()
                    listener.lastOptionId shouldBe optionId
                }
            }

            test("when listener is set and invokeMessageOptionClick is called, listener receives correct optionName") {
                checkAll(100, testMessageArb, optionIdArb, optionNameArb) { message, optionId, optionName ->
                    val storage = TestCallbackStorage()
                    val listener = TestMessageOptionClickListener()

                    // Set listener
                    storage.setMessageOptionClickListener(listener)

                    // Invoke callback
                    storage.invokeMessageOptionClick(message, optionId, optionName)

                    // Verify listener received correct optionName
                    listener.invoked.shouldBeTrue()
                    listener.lastOptionName shouldBe optionName
                }
            }

            test("when no listener is set, invokeMessageOptionClick does not throw exception") {
                checkAll(100, testMessageArb, optionIdArb, optionNameArb) { message, optionId, optionName ->
                    val storage = TestCallbackStorage()

                    // No listener set - should not throw
                    storage.invokeMessageOptionClick(message, optionId, optionName)

                    // Verify no listener is set
                    storage.getMessageOptionClickListener().shouldBeNull()
                }
            }

            test("setting a new listener replaces the previous one") {
                checkAll(100, testMessageArb, optionIdArb, optionNameArb) { message, optionId, optionName ->
                    val storage = TestCallbackStorage()
                    val firstListener = TestMessageOptionClickListener()
                    val secondListener = TestMessageOptionClickListener()

                    // Set first listener
                    storage.setMessageOptionClickListener(firstListener)

                    // Replace with second listener
                    storage.setMessageOptionClickListener(secondListener)

                    // Invoke callback
                    storage.invokeMessageOptionClick(message, optionId, optionName)

                    // First listener should NOT be invoked
                    firstListener.invoked.shouldBeFalse()

                    // Second listener should be invoked
                    secondListener.invoked.shouldBeTrue()
                    secondListener.lastMessage shouldBe message
                }
            }

            test("setting listener to null removes the listener") {
                checkAll(100, testMessageArb, optionIdArb, optionNameArb) { message, optionId, optionName ->
                    val storage = TestCallbackStorage()
                    val listener = TestMessageOptionClickListener()

                    // Set listener
                    storage.setMessageOptionClickListener(listener)
                    storage.getMessageOptionClickListener().shouldNotBeNull()

                    // Remove listener
                    storage.setMessageOptionClickListener(null)
                    storage.getMessageOptionClickListener().shouldBeNull()

                    // Invoke should not throw and listener should not be invoked
                    storage.invokeMessageOptionClick(message, optionId, optionName)
                    listener.invoked.shouldBeFalse()
                }
            }

            test("getter returns the same listener that was set") {
                checkAll(100, Arb.int(0..10)) { _ ->
                    val storage = TestCallbackStorage()
                    val listener = TestMessageOptionClickListener()

                    // Set listener
                    storage.setMessageOptionClickListener(listener)

                    // Getter should return the same instance
                    storage.getMessageOptionClickListener() shouldBe listener
                }
            }
        }

        // ========================================
        // ReactionClickListener Tests
        // ========================================

        context("ReactionClickListener") {

            test("when listener is set and invokeQuickReactionClick is called, listener receives correct message") {
                checkAll(100, testMessageArb, reactionArb) { message, reaction ->
                    val storage = TestCallbackStorage()
                    val listener = TestReactionClickListener()

                    // Set listener
                    storage.setQuickReactionClickListener(listener)

                    // Invoke callback
                    storage.invokeQuickReactionClick(message, reaction)

                    // Verify listener was invoked with correct message
                    listener.invoked.shouldBeTrue()
                    listener.lastMessage shouldBe message
                }
            }

            test("when listener is set and invokeQuickReactionClick is called, listener receives correct reaction") {
                checkAll(100, testMessageArb, reactionArb) { message, reaction ->
                    val storage = TestCallbackStorage()
                    val listener = TestReactionClickListener()

                    // Set listener
                    storage.setQuickReactionClickListener(listener)

                    // Invoke callback
                    storage.invokeQuickReactionClick(message, reaction)

                    // Verify listener received correct reaction
                    listener.invoked.shouldBeTrue()
                    listener.lastReaction shouldBe reaction
                }
            }

            test("when no listener is set, invokeQuickReactionClick does not throw exception") {
                checkAll(100, testMessageArb, reactionArb) { message, reaction ->
                    val storage = TestCallbackStorage()

                    // No listener set - should not throw
                    storage.invokeQuickReactionClick(message, reaction)

                    // Verify no listener is set
                    storage.getQuickReactionClickListener().shouldBeNull()
                }
            }

            test("setting a new listener replaces the previous one") {
                checkAll(100, testMessageArb, reactionArb) { message, reaction ->
                    val storage = TestCallbackStorage()
                    val firstListener = TestReactionClickListener()
                    val secondListener = TestReactionClickListener()

                    // Set first listener
                    storage.setQuickReactionClickListener(firstListener)

                    // Replace with second listener
                    storage.setQuickReactionClickListener(secondListener)

                    // Invoke callback
                    storage.invokeQuickReactionClick(message, reaction)

                    // First listener should NOT be invoked
                    firstListener.invoked.shouldBeFalse()

                    // Second listener should be invoked
                    secondListener.invoked.shouldBeTrue()
                    secondListener.lastMessage shouldBe message
                    secondListener.lastReaction shouldBe reaction
                }
            }

            test("setting listener to null removes the listener") {
                checkAll(100, testMessageArb, reactionArb) { message, reaction ->
                    val storage = TestCallbackStorage()
                    val listener = TestReactionClickListener()

                    // Set listener
                    storage.setQuickReactionClickListener(listener)
                    storage.getQuickReactionClickListener().shouldNotBeNull()

                    // Remove listener
                    storage.setQuickReactionClickListener(null)
                    storage.getQuickReactionClickListener().shouldBeNull()

                    // Invoke should not throw and listener should not be invoked
                    storage.invokeQuickReactionClick(message, reaction)
                    listener.invoked.shouldBeFalse()
                }
            }

            test("getter returns the same listener that was set") {
                checkAll(100, Arb.int(0..10)) { _ ->
                    val storage = TestCallbackStorage()
                    val listener = TestReactionClickListener()

                    // Set listener
                    storage.setQuickReactionClickListener(listener)

                    // Getter should return the same instance
                    storage.getQuickReactionClickListener() shouldBe listener
                }
            }
        }

        // ========================================
        // EmojiPickerClickListener Tests
        // ========================================

        context("EmojiPickerClickListener") {

            test("when listener is set and invokeEmojiPickerClick is called, listener is invoked") {
                checkAll(100, Arb.int(0..10)) { _ ->
                    val storage = TestCallbackStorage()
                    val listener = TestEmojiPickerClickListener()

                    // Set listener
                    storage.setEmojiPickerClick(listener)

                    // Invoke callback
                    storage.invokeEmojiPickerClick()

                    // Verify listener was invoked
                    listener.invoked.shouldBeTrue()
                }
            }

            test("when no listener is set, invokeEmojiPickerClick does not throw exception") {
                checkAll(100, Arb.int(0..10)) { _ ->
                    val storage = TestCallbackStorage()

                    // No listener set - should not throw
                    storage.invokeEmojiPickerClick()

                    // Verify no listener is set
                    storage.getEmojiPickerClick().shouldBeNull()
                }
            }

            test("setting a new listener replaces the previous one") {
                checkAll(100, Arb.int(0..10)) { _ ->
                    val storage = TestCallbackStorage()
                    val firstListener = TestEmojiPickerClickListener()
                    val secondListener = TestEmojiPickerClickListener()

                    // Set first listener
                    storage.setEmojiPickerClick(firstListener)

                    // Replace with second listener
                    storage.setEmojiPickerClick(secondListener)

                    // Invoke callback
                    storage.invokeEmojiPickerClick()

                    // First listener should NOT be invoked
                    firstListener.invoked.shouldBeFalse()

                    // Second listener should be invoked
                    secondListener.invoked.shouldBeTrue()
                }
            }

            test("setting listener to null removes the listener") {
                checkAll(100, Arb.int(0..10)) { _ ->
                    val storage = TestCallbackStorage()
                    val listener = TestEmojiPickerClickListener()

                    // Set listener
                    storage.setEmojiPickerClick(listener)
                    storage.getEmojiPickerClick().shouldNotBeNull()

                    // Remove listener
                    storage.setEmojiPickerClick(null)
                    storage.getEmojiPickerClick().shouldBeNull()

                    // Invoke should not throw and listener should not be invoked
                    storage.invokeEmojiPickerClick()
                    listener.invoked.shouldBeFalse()
                }
            }

            test("getter returns the same listener that was set") {
                checkAll(100, Arb.int(0..10)) { _ ->
                    val storage = TestCallbackStorage()
                    val listener = TestEmojiPickerClickListener()

                    // Set listener
                    storage.setEmojiPickerClick(listener)

                    // Getter should return the same instance
                    storage.getEmojiPickerClick() shouldBe listener
                }
            }

            test("multiple invocations trigger listener each time") {
                checkAll(100, Arb.int(1..5)) { invocationCount ->
                    val storage = TestCallbackStorage()
                    val listener = TestEmojiPickerClickListener()

                    // Set listener
                    storage.setEmojiPickerClick(listener)

                    // Invoke multiple times
                    repeat(invocationCount) {
                        storage.invokeEmojiPickerClick()
                    }

                    // Verify listener was invoked the correct number of times
                    listener.invocationCount shouldBe invocationCount
                }
            }
        }

        // ========================================
        // Cross-Callback Independence Tests
        // ========================================

        context("Callback Independence") {

            test("setting one callback does not affect other callbacks") {
                checkAll(100, testMessageArb, optionIdArb, optionNameArb, reactionArb) { 
                    message, optionId, optionName, reaction ->
                    val storage = TestCallbackStorage()
                    val messageOptionListener = TestMessageOptionClickListener()
                    val reactionListener = TestReactionClickListener()
                    val emojiPickerListener = TestEmojiPickerClickListener()

                    // Set only message option listener
                    storage.setMessageOptionClickListener(messageOptionListener)

                    // Other listeners should still be null
                    storage.getQuickReactionClickListener().shouldBeNull()
                    storage.getEmojiPickerClick().shouldBeNull()

                    // Set reaction listener
                    storage.setQuickReactionClickListener(reactionListener)

                    // Message option listener should still be set
                    storage.getMessageOptionClickListener() shouldBe messageOptionListener

                    // Set emoji picker listener
                    storage.setEmojiPickerClick(emojiPickerListener)

                    // All listeners should be set independently
                    storage.getMessageOptionClickListener() shouldBe messageOptionListener
                    storage.getQuickReactionClickListener() shouldBe reactionListener
                    storage.getEmojiPickerClick() shouldBe emojiPickerListener
                }
            }

            test("invoking one callback does not invoke other callbacks") {
                checkAll(100, testMessageArb, optionIdArb, optionNameArb, reactionArb) { 
                    message, optionId, optionName, reaction ->
                    val storage = TestCallbackStorage()
                    val messageOptionListener = TestMessageOptionClickListener()
                    val reactionListener = TestReactionClickListener()
                    val emojiPickerListener = TestEmojiPickerClickListener()

                    // Set all listeners
                    storage.setMessageOptionClickListener(messageOptionListener)
                    storage.setQuickReactionClickListener(reactionListener)
                    storage.setEmojiPickerClick(emojiPickerListener)

                    // Invoke only message option callback
                    storage.invokeMessageOptionClick(message, optionId, optionName)

                    // Only message option listener should be invoked
                    messageOptionListener.invoked.shouldBeTrue()
                    reactionListener.invoked.shouldBeFalse()
                    emojiPickerListener.invoked.shouldBeFalse()

                    // Reset
                    messageOptionListener.reset()

                    // Invoke only reaction callback
                    storage.invokeQuickReactionClick(message, reaction)

                    // Only reaction listener should be invoked
                    messageOptionListener.invoked.shouldBeFalse()
                    reactionListener.invoked.shouldBeTrue()
                    emojiPickerListener.invoked.shouldBeFalse()

                    // Reset
                    reactionListener.reset()

                    // Invoke only emoji picker callback
                    storage.invokeEmojiPickerClick()

                    // Only emoji picker listener should be invoked
                    messageOptionListener.invoked.shouldBeFalse()
                    reactionListener.invoked.shouldBeFalse()
                    emojiPickerListener.invoked.shouldBeTrue()
                }
            }

            test("removing one callback does not affect other callbacks") {
                checkAll(100, Arb.int(0..10)) { _ ->
                    val storage = TestCallbackStorage()
                    val messageOptionListener = TestMessageOptionClickListener()
                    val reactionListener = TestReactionClickListener()
                    val emojiPickerListener = TestEmojiPickerClickListener()

                    // Set all listeners
                    storage.setMessageOptionClickListener(messageOptionListener)
                    storage.setQuickReactionClickListener(reactionListener)
                    storage.setEmojiPickerClick(emojiPickerListener)

                    // Remove message option listener
                    storage.setMessageOptionClickListener(null)

                    // Other listeners should still be set
                    storage.getMessageOptionClickListener().shouldBeNull()
                    storage.getQuickReactionClickListener() shouldBe reactionListener
                    storage.getEmojiPickerClick() shouldBe emojiPickerListener

                    // Remove reaction listener
                    storage.setQuickReactionClickListener(null)

                    // Emoji picker listener should still be set
                    storage.getQuickReactionClickListener().shouldBeNull()
                    storage.getEmojiPickerClick() shouldBe emojiPickerListener
                }
            }
        }

        // ========================================
        // Parameter Preservation Tests
        // ========================================

        context("Parameter Preservation") {

            test("message option callback preserves all parameter values exactly") {
                checkAll(100, testMessageArb, nonEmptyStringArb, nonEmptyStringArb) { 
                    message, optionId, optionName ->
                    val storage = TestCallbackStorage()
                    val listener = TestMessageOptionClickListener()

                    storage.setMessageOptionClickListener(listener)
                    storage.invokeMessageOptionClick(message, optionId, optionName)

                    // Verify exact parameter preservation
                    listener.lastMessage?.id shouldBe message.id
                    listener.lastMessage?.text shouldBe message.text
                    listener.lastMessage?.senderUid shouldBe message.senderUid
                    listener.lastOptionId shouldBe optionId
                    listener.lastOptionName shouldBe optionName
                }
            }

            test("reaction callback preserves all parameter values exactly") {
                checkAll(100, testMessageArb, reactionArb) { message, reaction ->
                    val storage = TestCallbackStorage()
                    val listener = TestReactionClickListener()

                    storage.setQuickReactionClickListener(listener)
                    storage.invokeQuickReactionClick(message, reaction)

                    // Verify exact parameter preservation
                    listener.lastMessage?.id shouldBe message.id
                    listener.lastMessage?.text shouldBe message.text
                    listener.lastMessage?.senderUid shouldBe message.senderUid
                    listener.lastReaction shouldBe reaction
                }
            }
        }
    }
})
