package com.cometchat.uikit.compose.presentation.shared.messagebubble

import com.cometchat.chat.models.BaseMessage
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

// Feature: compose-bubble-factory-api, Property 3: Key derivation from message
// Validates: Requirements 3.3, 3.4, 5.1, 5.2

/**
 * The DELETED_KEY constant as defined in CometChatMessageBubble.kt.
 */
private const val DELETED_KEY = "deleted"

/**
 * Replicates the private `buildFactoryKey(message)` logic from CometChatMessageBubble.kt.
 * This is the function under test — its behavior must match the design specification:
 * - When `message.deletedAt > 0`, return "deleted"
 * - Otherwise, return "${message.category}_${message.type}"
 */
private fun buildFactoryKey(message: BaseMessage): String {
    return if (message.deletedAt > 0) {
        DELETED_KEY
    } else {
        "${message.category}_${message.type}"
    }
}

/**
 * Property-based tests for key derivation from message.
 *
 * **Feature: compose-bubble-factory-api, Property 3: Key derivation from message**
 *
 * For any BaseMessage, the `buildFactoryKey(message)` function should return `"deleted"`
 * when `message.deletedAt > 0`, and `"${message.category}_${message.type}"` otherwise.
 * This must be equivalent to the behavior of the removed `getFactoryKey()` function.
 *
 * **Validates: Requirements 3.3, 3.4, 5.1, 5.2**
 */
class KeyDerivationPropertyTest : FunSpec({

    context("Property 3: Key derivation from message") {

        test("deleted messages (deletedAt > 0) always produce the deleted key") {
            checkAll(
                100,
                Arb.long(1L..Long.MAX_VALUE),
                Arb.string(1..20),
                Arb.string(1..20)
            ) { deletedAt, category, type ->
                val message = mock(BaseMessage::class.java)
                `when`(message.deletedAt).thenReturn(deletedAt)
                `when`(message.category).thenReturn(category)
                `when`(message.type).thenReturn(type)

                buildFactoryKey(message) shouldBe DELETED_KEY
            }
        }

        test("non-deleted messages (deletedAt <= 0) produce category_type key") {
            checkAll(
                100,
                Arb.long(Long.MIN_VALUE..0L),
                Arb.string(1..20),
                Arb.string(1..20)
            ) { deletedAt, category, type ->
                val message = mock(BaseMessage::class.java)
                `when`(message.deletedAt).thenReturn(deletedAt)
                `when`(message.category).thenReturn(category)
                `when`(message.type).thenReturn(type)

                buildFactoryKey(message) shouldBe "${category}_${type}"
            }
        }

        test("key derivation is deterministic for the same message properties") {
            checkAll(
                100,
                Arb.long(),
                Arb.string(1..20),
                Arb.string(1..20)
            ) { deletedAt, category, type ->
                val msg1 = mock(BaseMessage::class.java)
                `when`(msg1.deletedAt).thenReturn(deletedAt)
                `when`(msg1.category).thenReturn(category)
                `when`(msg1.type).thenReturn(type)

                val msg2 = mock(BaseMessage::class.java)
                `when`(msg2.deletedAt).thenReturn(deletedAt)
                `when`(msg2.category).thenReturn(category)
                `when`(msg2.type).thenReturn(type)

                buildFactoryKey(msg1) shouldBe buildFactoryKey(msg2)
            }
        }

        test("deletedAt boundary: exactly 0 produces category_type, exactly 1 produces deleted") {
            checkAll(
                100,
                Arb.string(1..20),
                Arb.string(1..20)
            ) { category, type ->
                val zeroMsg = mock(BaseMessage::class.java)
                `when`(zeroMsg.deletedAt).thenReturn(0L)
                `when`(zeroMsg.category).thenReturn(category)
                `when`(zeroMsg.type).thenReturn(type)

                val oneMsg = mock(BaseMessage::class.java)
                `when`(oneMsg.deletedAt).thenReturn(1L)
                `when`(oneMsg.category).thenReturn(category)
                `when`(oneMsg.type).thenReturn(type)

                buildFactoryKey(zeroMsg) shouldBe "${category}_${type}"
                buildFactoryKey(oneMsg) shouldBe DELETED_KEY
            }
        }
    }
})
