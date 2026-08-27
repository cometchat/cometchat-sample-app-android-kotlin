package com.cometchat.uikit.core.viewmodel.conversations

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.TextMessage
import com.cometchat.uikit.core.viewmodel.mergeConversationUpdate
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * ENG-38583 — property-based coverage of the ConversationUpdated merge.
 *
 * The example-based tests pin specific combinations; these pin the rules for arbitrary
 * inputs. The invariant that matters to the reported bug is the third one: whenever the
 * caller supplies a `conversationWith`, it must survive — that is precisely what the old
 * implementation dropped.
 *
 * Layer 3 (PBT). Kotest Property with `checkAll` and `Arb` generators, no hardcoded values.
 *
 * Run: ./gradlew :chatuikit-core:testDebugUnitTest --tests "*ConversationUpdateMergePropertyTest"
 */
class ConversationUpdateMergePropertyTest : StringSpec({

    fun group(description: String) =
        Group("guid", "name", CometChatConstants.GROUP_TYPE_PUBLIC, null)
            .apply { setDescription(description) }

    fun conversation(
        unread: Int = 0,
        updatedAt: Long = 0,
        withDescription: String? = null,
        messageText: String? = null
    ): Conversation = Conversation("conv-1", CometChatConstants.RECEIVER_TYPE_GROUP).apply {
        unreadMessageCount = unread
        this.updatedAt = updatedAt
        withDescription?.let { conversationWith = group(it) }
        messageText?.let {
            lastMessage = TextMessage("guid", it, CometChatConstants.RECEIVER_TYPE_GROUP)
        }
    }

    "the conversation id is never altered by a merge" {
        checkAll(Arb.int(), Arb.int()) { existingUnread, updateUnread ->
            mergeConversationUpdate(
                conversation(unread = existingUnread, withDescription = "e"),
                conversation(unread = updateUnread, withDescription = "u")
            ).conversationId shouldBe "conv-1"
        }
    }

    "the supplied unread count always wins, whatever either side holds" {
        checkAll(Arb.int(), Arb.int()) { existingUnread, updateUnread ->
            mergeConversationUpdate(
                conversation(unread = existingUnread, withDescription = "e"),
                conversation(unread = updateUnread, withDescription = "u")
            ).unreadMessageCount shouldBe updateUnread
        }
    }

    "a supplied conversationWith always survives, and its absence never clears the existing one" {
        checkAll(Arb.string(1..20), Arb.string(1..20), Arb.boolean()) { existingDesc, updateDesc, supplied ->
            val merged = mergeConversationUpdate(
                conversation(withDescription = existingDesc),
                conversation(withDescription = if (supplied) updateDesc else null)
            )
            (merged.conversationWith as Group).description shouldBe
                if (supplied) updateDesc else existingDesc
        }
    }

    "a supplied lastMessage always survives, and its absence never clears the existing one" {
        checkAll(Arb.string(1..20), Arb.string(1..20), Arb.boolean()) { existingText, updateText, supplied ->
            val merged = mergeConversationUpdate(
                conversation(withDescription = "e", messageText = existingText),
                conversation(withDescription = "u", messageText = if (supplied) updateText else null)
            )
            (merged.lastMessage as TextMessage).text shouldBe
                if (supplied) updateText else existingText
        }
    }

    "updatedAt moves only when the supplied value is positive" {
        checkAll(Arb.long(1L..4_102_444_800L), Arb.long(0L..4_102_444_800L)) { existingAt, updateAt ->
            val merged = mergeConversationUpdate(
                conversation(updatedAt = existingAt, withDescription = "e"),
                conversation(updatedAt = updateAt, withDescription = "u")
            )
            merged.updatedAt shouldBe if (updateAt > 0) updateAt else existingAt
        }
    }

    "merging never mutates the entry already in the list" {
        checkAll(Arb.string(1..20), Arb.int()) { updateDesc, updateUnread ->
            val existing = conversation(unread = 0, withDescription = "original")
            mergeConversationUpdate(existing, conversation(unread = updateUnread, withDescription = updateDesc))
            (existing.conversationWith as Group).description shouldBe "original"
            existing.unreadMessageCount shouldBe 0
        }
    }
})
