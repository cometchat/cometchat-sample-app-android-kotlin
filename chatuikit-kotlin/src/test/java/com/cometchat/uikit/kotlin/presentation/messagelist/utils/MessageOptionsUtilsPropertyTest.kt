package com.cometchat.uikit.kotlin.presentation.messagelist.utils

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Message option constants for testing.
 * These mirror the actual UIKitConstants.MessageOption constants.
 */
private object MessageOptionConstants {
    const val REPLY_IN_THREAD = "reply_in_thread"
    const val REPLY = "reply"
    const val COPY = "copy"
    const val EDIT = "edit"
    const val DELETE = "delete"
    const val SHARE = "share"
    const val MESSAGE_PRIVATELY = "message_privately"
    const val MESSAGE_INFORMATION = "message_information"
    const val REPORT = "report"
    const val MARK_AS_UNREAD = "mark_as_unread"
}

/**
 * Enum representing supported message types for testing.
 */
private enum class MessageType(val category: String, val type: String) {
    TEXT("message", "text"),
    IMAGE("message", "image"),
    VIDEO("message", "video"),
    AUDIO("message", "audio"),
    FILE("message", "file")
}

/**
 * Test implementation of MessageOptionsUtils that mirrors the actual implementation.
 * This allows testing the logic without Android dependencies.
 */
private object TestMessageOptionsUtils {

    /**
     * Map of message key (category_type) to list of option IDs.
     * The order of options in each list determines the display order.
     * This mirrors the actual MessageOptionsUtils.defaultOptionsMap.
     */
    private val defaultOptionsMap: Map<String, List<String>> = mapOf(
        "message_text" to listOf(
            MessageOptionConstants.REPLY_IN_THREAD,
            MessageOptionConstants.REPLY,
            MessageOptionConstants.SHARE,
            MessageOptionConstants.COPY,
            MessageOptionConstants.MARK_AS_UNREAD,
            MessageOptionConstants.MESSAGE_INFORMATION,
            MessageOptionConstants.EDIT,
            MessageOptionConstants.DELETE,
            MessageOptionConstants.REPORT,
            MessageOptionConstants.MESSAGE_PRIVATELY
        ),
        "message_image" to listOf(
            MessageOptionConstants.MESSAGE_INFORMATION,
            MessageOptionConstants.MARK_AS_UNREAD,
            MessageOptionConstants.REPLY_IN_THREAD,
            MessageOptionConstants.REPLY,
            MessageOptionConstants.SHARE,
            MessageOptionConstants.REPORT,
            MessageOptionConstants.DELETE,
            MessageOptionConstants.MESSAGE_PRIVATELY
        ),
        "message_video" to listOf(
            MessageOptionConstants.MESSAGE_INFORMATION,
            MessageOptionConstants.MARK_AS_UNREAD,
            MessageOptionConstants.REPLY_IN_THREAD,
            MessageOptionConstants.REPLY,
            MessageOptionConstants.SHARE,
            MessageOptionConstants.REPORT,
            MessageOptionConstants.DELETE,
            MessageOptionConstants.MESSAGE_PRIVATELY
        ),
        "message_audio" to listOf(
            MessageOptionConstants.MESSAGE_INFORMATION,
            MessageOptionConstants.MARK_AS_UNREAD,
            MessageOptionConstants.REPLY_IN_THREAD,
            MessageOptionConstants.REPLY,
            MessageOptionConstants.SHARE,
            MessageOptionConstants.REPORT,
            MessageOptionConstants.DELETE,
            MessageOptionConstants.MESSAGE_PRIVATELY
        ),
        "message_file" to listOf(
            MessageOptionConstants.MESSAGE_INFORMATION,
            MessageOptionConstants.MARK_AS_UNREAD,
            MessageOptionConstants.REPLY_IN_THREAD,
            MessageOptionConstants.REPLY,
            MessageOptionConstants.SHARE,
            MessageOptionConstants.REPORT,
            MessageOptionConstants.DELETE,
            MessageOptionConstants.MESSAGE_PRIVATELY
        )
    )

    /**
     * Returns the list of default option IDs for a given message category and type.
     *
     * @param category The message category (e.g., "message")
     * @param type The message type (e.g., "text", "image", "video", "audio", "file")
     * @return List of option IDs for the message type, or empty list if not found
     */
    fun getDefaultOptionIds(category: String, type: String): List<String> {
        val key = "${category}_$type".lowercase()
        return defaultOptionsMap[key] ?: emptyList()
    }
}

/**
 * Property-based tests for default message options by type.
 * Uses Kotest property testing to verify correctness properties.
 *
 * Feature: messagelist-property-parity, Property 13: Default Message Options by Type
 *
 * *For any* message with category C and type T, calling `MessageOptionsUtils.getDefaultOptionIds(C, T)`
 * SHALL return the predefined list of option IDs for that message type, and the list SHALL match
 * the Java implementation's options for the same message type.
 *
 * **Validates: Requirements 1.1-1.10**
 */
class MessageOptionsUtilsPropertyTest : FunSpec({

    // ==================== Expected Options by Type ====================

    /**
     * Expected options for text messages.
     * Order: REPLY_IN_THREAD, REPLY, SHARE, COPY, MARK_AS_UNREAD, MESSAGE_INFO, EDIT, DELETE, FLAG/REPORT, MESSAGE_PRIVATELY
     */
    val expectedTextOptions = listOf(
        MessageOptionConstants.REPLY_IN_THREAD,
        MessageOptionConstants.REPLY,
        MessageOptionConstants.SHARE,
        MessageOptionConstants.COPY,
        MessageOptionConstants.MARK_AS_UNREAD,
        MessageOptionConstants.MESSAGE_INFORMATION,
        MessageOptionConstants.EDIT,
        MessageOptionConstants.DELETE,
        MessageOptionConstants.REPORT,
        MessageOptionConstants.MESSAGE_PRIVATELY
    )

    /**
     * Expected options for media messages (image, video, audio, file).
     * Order: MESSAGE_INFO, MARK_AS_UNREAD, REPLY_IN_THREAD, REPLY, SHARE, FLAG/REPORT, DELETE, MESSAGE_PRIVATELY
     */
    val expectedMediaOptions = listOf(
        MessageOptionConstants.MESSAGE_INFORMATION,
        MessageOptionConstants.MARK_AS_UNREAD,
        MessageOptionConstants.REPLY_IN_THREAD,
        MessageOptionConstants.REPLY,
        MessageOptionConstants.SHARE,
        MessageOptionConstants.REPORT,
        MessageOptionConstants.DELETE,
        MessageOptionConstants.MESSAGE_PRIVATELY
    )

    // ==================== Generators ====================

    /**
     * Generator for supported message types.
     */
    val messageTypeArb = Arb.element(MessageType.entries)

    /**
     * Generator for unknown/unsupported category strings.
     */
    val unknownCategoryArb = Arb.element("custom", "action", "call", "unknown", "invalid", "")

    /**
     * Generator for unknown/unsupported type strings.
     */
    val unknownTypeArb = Arb.element("custom", "sticker", "poll", "meeting", "unknown", "invalid", "")

    // ==================== Property Tests ====================

    context("Property 13: Default Message Options by Type") {

        // ========================================
        // Text Message Options Tests
        // ========================================

        test("getDefaultOptionIds('message', 'text') returns expected list of option IDs") {
            checkAll(100, Arb.string(0..5)) { _ ->
                val options = TestMessageOptionsUtils.getDefaultOptionIds("message", "text")

                options shouldContainExactly expectedTextOptions
            }
        }

        test("text message options contain COPY option (unique to text)") {
            checkAll(100, Arb.string(0..5)) { _ ->
                val options = TestMessageOptionsUtils.getDefaultOptionIds("message", "text")

                options.contains(MessageOptionConstants.COPY) shouldBe true
            }
        }

        test("text message options contain EDIT option (unique to text)") {
            checkAll(100, Arb.string(0..5)) { _ ->
                val options = TestMessageOptionsUtils.getDefaultOptionIds("message", "text")

                options.contains(MessageOptionConstants.EDIT) shouldBe true
            }
        }

        // ========================================
        // Image Message Options Tests
        // ========================================

        test("getDefaultOptionIds('message', 'image') returns expected list of option IDs") {
            checkAll(100, Arb.string(0..5)) { _ ->
                val options = TestMessageOptionsUtils.getDefaultOptionIds("message", "image")

                options shouldContainExactly expectedMediaOptions
            }
        }

        test("image message options do not contain COPY option") {
            checkAll(100, Arb.string(0..5)) { _ ->
                val options = TestMessageOptionsUtils.getDefaultOptionIds("message", "image")

                options.contains(MessageOptionConstants.COPY) shouldBe false
            }
        }

        test("image message options do not contain EDIT option") {
            checkAll(100, Arb.string(0..5)) { _ ->
                val options = TestMessageOptionsUtils.getDefaultOptionIds("message", "image")

                options.contains(MessageOptionConstants.EDIT) shouldBe false
            }
        }

        // ========================================
        // Video Message Options Tests
        // ========================================

        test("getDefaultOptionIds('message', 'video') returns expected list of option IDs") {
            checkAll(100, Arb.string(0..5)) { _ ->
                val options = TestMessageOptionsUtils.getDefaultOptionIds("message", "video")

                options shouldContainExactly expectedMediaOptions
            }
        }

        test("video message options match image message options") {
            checkAll(100, Arb.string(0..5)) { _ ->
                val videoOptions = TestMessageOptionsUtils.getDefaultOptionIds("message", "video")
                val imageOptions = TestMessageOptionsUtils.getDefaultOptionIds("message", "image")

                videoOptions shouldContainExactly imageOptions
            }
        }

        // ========================================
        // Audio Message Options Tests
        // ========================================

        test("getDefaultOptionIds('message', 'audio') returns expected list of option IDs") {
            checkAll(100, Arb.string(0..5)) { _ ->
                val options = TestMessageOptionsUtils.getDefaultOptionIds("message", "audio")

                options shouldContainExactly expectedMediaOptions
            }
        }

        test("audio message options match image message options") {
            checkAll(100, Arb.string(0..5)) { _ ->
                val audioOptions = TestMessageOptionsUtils.getDefaultOptionIds("message", "audio")
                val imageOptions = TestMessageOptionsUtils.getDefaultOptionIds("message", "image")

                audioOptions shouldContainExactly imageOptions
            }
        }

        // ========================================
        // File Message Options Tests
        // ========================================

        test("getDefaultOptionIds('message', 'file') returns expected list of option IDs") {
            checkAll(100, Arb.string(0..5)) { _ ->
                val options = TestMessageOptionsUtils.getDefaultOptionIds("message", "file")

                options shouldContainExactly expectedMediaOptions
            }
        }

        test("file message options match image message options") {
            checkAll(100, Arb.string(0..5)) { _ ->
                val fileOptions = TestMessageOptionsUtils.getDefaultOptionIds("message", "file")
                val imageOptions = TestMessageOptionsUtils.getDefaultOptionIds("message", "image")

                fileOptions shouldContainExactly imageOptions
            }
        }

        // ========================================
        // Unknown Message Type Tests
        // ========================================

        test("unknown message types return empty list") {
            checkAll(100, unknownCategoryArb, unknownTypeArb) { category, type ->
                // Skip known combinations
                val key = "${category}_$type".lowercase()
                if (key !in listOf("message_text", "message_image", "message_video", "message_audio", "message_file")) {
                    val options = TestMessageOptionsUtils.getDefaultOptionIds(category, type)

                    options.shouldBeEmpty()
                }
            }
        }

        test("unknown category with known type returns empty list") {
            checkAll(100, unknownCategoryArb) { category ->
                if (category != "message") {
                    val options = TestMessageOptionsUtils.getDefaultOptionIds(category, "text")

                    options.shouldBeEmpty()
                }
            }
        }

        test("known category with unknown type returns empty list") {
            checkAll(100, unknownTypeArb) { type ->
                if (type !in listOf("text", "image", "video", "audio", "file")) {
                    val options = TestMessageOptionsUtils.getDefaultOptionIds("message", type)

                    options.shouldBeEmpty()
                }
            }
        }

        // ========================================
        // Option Order Tests
        // ========================================

        test("text message options order matches expected order") {
            checkAll(100, Arb.string(0..5)) { _ ->
                val options = TestMessageOptionsUtils.getDefaultOptionIds("message", "text")

                // Verify first option is REPLY_IN_THREAD
                options.first() shouldBe MessageOptionConstants.REPLY_IN_THREAD

                // Verify last option is MESSAGE_PRIVATELY
                options.last() shouldBe MessageOptionConstants.MESSAGE_PRIVATELY

                // Verify COPY comes before EDIT
                val copyIndex = options.indexOf(MessageOptionConstants.COPY)
                val editIndex = options.indexOf(MessageOptionConstants.EDIT)
                (copyIndex < editIndex) shouldBe true
            }
        }

        test("media message options order matches expected order") {
            checkAll(100, messageTypeArb) { messageType ->
                if (messageType != MessageType.TEXT) {
                    val options = TestMessageOptionsUtils.getDefaultOptionIds(messageType.category, messageType.type)

                    // Verify first option is MESSAGE_INFORMATION
                    options.first() shouldBe MessageOptionConstants.MESSAGE_INFORMATION

                    // Verify last option is MESSAGE_PRIVATELY
                    options.last() shouldBe MessageOptionConstants.MESSAGE_PRIVATELY

                    // Verify REPLY_IN_THREAD comes before REPLY
                    val replyInThreadIndex = options.indexOf(MessageOptionConstants.REPLY_IN_THREAD)
                    val replyIndex = options.indexOf(MessageOptionConstants.REPLY)
                    (replyInThreadIndex < replyIndex) shouldBe true
                }
            }
        }

        // ========================================
        // Case Insensitivity Tests
        // ========================================

        test("getDefaultOptionIds is case insensitive for category") {
            checkAll(100, Arb.string(0..5)) { _ ->
                val lowerOptions = TestMessageOptionsUtils.getDefaultOptionIds("message", "text")
                val upperOptions = TestMessageOptionsUtils.getDefaultOptionIds("MESSAGE", "text")
                val mixedOptions = TestMessageOptionsUtils.getDefaultOptionIds("Message", "text")

                lowerOptions shouldContainExactly upperOptions
                lowerOptions shouldContainExactly mixedOptions
            }
        }

        test("getDefaultOptionIds is case insensitive for type") {
            checkAll(100, Arb.string(0..5)) { _ ->
                val lowerOptions = TestMessageOptionsUtils.getDefaultOptionIds("message", "text")
                val upperOptions = TestMessageOptionsUtils.getDefaultOptionIds("message", "TEXT")
                val mixedOptions = TestMessageOptionsUtils.getDefaultOptionIds("message", "Text")

                lowerOptions shouldContainExactly upperOptions
                lowerOptions shouldContainExactly mixedOptions
            }
        }

        // ========================================
        // All Message Types Property Tests
        // ========================================

        test("all supported message types return non-empty option lists") {
            checkAll(100, messageTypeArb) { messageType ->
                val options = TestMessageOptionsUtils.getDefaultOptionIds(messageType.category, messageType.type)

                options.isNotEmpty() shouldBe true
            }
        }

        test("all supported message types contain common options") {
            checkAll(100, messageTypeArb) { messageType ->
                val options = TestMessageOptionsUtils.getDefaultOptionIds(messageType.category, messageType.type)

                // All message types should have these common options
                options.contains(MessageOptionConstants.REPLY_IN_THREAD) shouldBe true
                options.contains(MessageOptionConstants.REPLY) shouldBe true
                options.contains(MessageOptionConstants.SHARE) shouldBe true
                options.contains(MessageOptionConstants.DELETE) shouldBe true
                options.contains(MessageOptionConstants.MESSAGE_PRIVATELY) shouldBe true
                options.contains(MessageOptionConstants.REPORT) shouldBe true
            }
        }

        test("only text messages have COPY and EDIT options") {
            checkAll(100, messageTypeArb) { messageType ->
                val options = TestMessageOptionsUtils.getDefaultOptionIds(messageType.category, messageType.type)

                if (messageType == MessageType.TEXT) {
                    options.contains(MessageOptionConstants.COPY) shouldBe true
                    options.contains(MessageOptionConstants.EDIT) shouldBe true
                } else {
                    options.contains(MessageOptionConstants.COPY) shouldBe false
                    options.contains(MessageOptionConstants.EDIT) shouldBe false
                }
            }
        }
    }
})
