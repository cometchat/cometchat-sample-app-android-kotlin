package com.cometchat.uikit.compose.presentation.conversations.ui

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.Modifier
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.presentation.conversations.style.CometChatConversationsStyle
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.constants.UIKitConstants
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * ENG-35566 — the conversation list must never hand duplicate keys to its LazyColumn.
 *
 * A repeated `conversationId` reaching `itemsIndexed` throws
 * `IllegalArgumentException: Key "..." was already used` and takes the whole app down. The
 * ViewModel guards its own write paths, but this is the last line of defence: whatever the list
 * contains, the render boundary must not crash.
 *
 * Layer 1 (Component). Runs on Robolectric in `src/test`, so it executes in the JVM unit-test
 * job rather than the instrumented suite.
 *
 * Each test renders through a custom `itemView`, which replaces the whole row — this keeps the
 * assertions on LazyColumn keying rather than on avatars, receipts and image loading.
 *
 * Run: ./gradlew :chatuikit-compose:testDebugUnitTest --tests "*ConversationListDuplicateKeyTest"
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ConversationListDuplicateKeyTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun conversation(id: String?, name: String = "Test"): Conversation {
        val user = mock<User>()
        whenever(user.uid).thenReturn(name)
        whenever(user.name).thenReturn(name)
        val conversation = mock<Conversation>()
        whenever(conversation.conversationId).thenReturn(id)
        whenever(conversation.conversationType).thenReturn(UIKitConstants.ConversationType.USERS)
        whenever(conversation.conversationWith).thenReturn(user)
        whenever(conversation.unreadMessageCount).thenReturn(0)
        whenever(conversation.lastMessage).thenReturn(null)
        return conversation
    }

    private fun render(conversations: List<Conversation>) {
        composeRule.setContent {
            CometChatTheme {
                ConversationListContent(
                    conversations = conversations,
                    typingIndicators = emptyMap(),
                    selectedConversations = emptySet(),
                    selectionMode = UIKitConstants.SelectionMode.NONE,
                    style = CometChatConversationsStyle.default(),
                    hideUserStatus = true,
                    hideGroupType = true,
                    hideReceipts = true,
                    hideSeparator = true,
                    hideDeleteOption = true,
                    dateTimeFormatter = null,
                    textFormatters = emptyList(),
                    itemView = { conversation, _ ->
                        Text(
                            text = conversation.conversationId ?: "no-id",
                            modifier = Modifier.testTag("row")
                        )
                    },
                    leadingView = null,
                    titleView = null,
                    subtitleView = null,
                    trailingView = null,
                    options = null,
                    addOptions = null,
                    onItemClick = {},
                    onItemLongClick = {},
                    onDeleteConversation = {},
                    onLoadMore = {},
                    scrollToTopEvent = null
                )
            }
        }
        composeRule.waitForIdle()
    }

    // useUnmergedTree: each row sits inside a clickable Box, which merges its children's
    // semantics — the tag on the row content is not a node of its own in the merged tree.
    private fun renderedRows(): Int =
        composeRule.onAllNodesWithTag("row", useUnmergedTree = true).fetchSemanticsNodes().size

    @Test
    fun duplicateConversationId_doesNotCrash_andRendersOnce() {
        // Two distinct instances sharing an id — what a real-time event racing the fetch produces.
        render(listOf(conversation("user_alice"), conversation("user_alice")))

        assertEquals(1, renderedRows())
    }

    @Test
    fun distinctConversationIds_allRender() {
        render(listOf(conversation("user_alice"), conversation("user_bob")))

        assertEquals(2, renderedRows())
    }

    @Test
    fun conversationsWithoutAnId_doNotCollide() {
        // A null id must fall back to identity, not collapse every id-less row onto one key.
        render(listOf(conversation(null, "a"), conversation(null, "b")))

        assertEquals(2, renderedRows())
    }

    @Test
    fun blankConversationId_doesNotCollide() {
        render(listOf(conversation("", "a"), conversation("", "b")))

        assertEquals(2, renderedRows())
    }
}
