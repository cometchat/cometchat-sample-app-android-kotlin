package com.cometchat.uikit.compose.presentation.conversations.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.AIAssistantMessage
import com.cometchat.uikit.core.testutils.MockFactory
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric tests for the Compose [ConversationUtils.getLastMessageText] across the
 * card + AI-agent contexts (ENG-36415, task 1.7 — conversation-list preview).
 *
 * Mirrors the kotlin-module ConversationAgenticSubtitleTest: an `agentic` (AI assistant)
 * message previews its text, else the localized "AI agent message" label; a developer
 * card previews its text, else "Card Message", identically in 1-1 and group.
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:testDebugUnitTest --tests "*ConversationAgenticSubtitleTest"
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ConversationAgenticSubtitleTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    private fun agenticMessage(text: String?, deletedAt: Long = 0L): AIAssistantMessage {
        val message = mock<AIAssistantMessage>()
        whenever(message.text).thenReturn(text)
        whenever(message.deletedAt).thenReturn(deletedAt)
        return message
    }

    // ==================== Agentic (AI assistant) subtitle ====================

    @Test
    fun agenticMessage_withText_usesMessageText() {
        val subtitle = ConversationUtils.getLastMessageText(context, agenticMessage("Your order has shipped!"))
        assertEquals("Your order has shipped!", subtitle)
    }

    @Test
    fun agenticMessage_withNullText_fallsBackToAiAgentLabel() {
        val subtitle = ConversationUtils.getLastMessageText(context, agenticMessage(null))
        assertEquals("AI agent message", subtitle)
    }

    @Test
    fun agenticMessage_withEmptyText_fallsBackToAiAgentLabel() {
        val subtitle = ConversationUtils.getLastMessageText(context, agenticMessage(""))
        assertEquals("AI agent message", subtitle)
    }

    @Test
    fun agenticMessage_whenDeleted_showsDeletedText() {
        val subtitle = ConversationUtils.getLastMessageText(
            context, agenticMessage("ignored", deletedAt = 1_700_000_000L)
        )
        assertEquals("This message was deleted", subtitle)
    }

    // ==================== Developer-card subtitle (1-1 vs group) ====================

    @Test
    fun developerCard_withText_usesCardText() {
        val card = MockFactory.createCardMessage(text = "Track your order")
        assertEquals("Track your order", ConversationUtils.getLastMessageText(context, card))
    }

    @Test
    fun developerCard_withNullText_fallsBackToCardMessageLabel() {
        val card = MockFactory.createCardMessage(text = null)
        assertEquals("Card Message", ConversationUtils.getLastMessageText(context, card))
    }

    @Test
    fun developerCard_previewIsIdentical_inOneToOneAndGroup() {
        val oneToOne = MockFactory.createCardMessage(
            text = null, receiverType = CometChatConstants.RECEIVER_TYPE_USER, receiverId = "user-2"
        )
        val group = MockFactory.createCardMessage(
            text = null, receiverType = CometChatConstants.RECEIVER_TYPE_GROUP, receiverId = "group-1"
        )
        assertEquals(
            ConversationUtils.getLastMessageText(context, oneToOne),
            ConversationUtils.getLastMessageText(context, group)
        )
    }
}
