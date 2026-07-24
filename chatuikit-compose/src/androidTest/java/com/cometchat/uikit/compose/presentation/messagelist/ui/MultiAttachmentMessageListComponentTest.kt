package com.cometchat.uikit.compose.presentation.messagelist.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.chat.models.Attachment
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.model.SurroundingMessagesResult
import com.cometchat.uikit.core.domain.repository.MessageListRepository
import com.cometchat.uikit.core.viewmodel.CometChatMessageListViewModel
import com.cometchat.uikit.compose.presentation.shared.messagebubble.ui.CometChatFilesBubble
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * ENG-36737 — Layer 1 component tests for multi-attachment rendering in the Compose
 * [CometChatMessageList]: fake repository → real ViewModel → real composable, per TEST-SETUP.md.
 *
 * Covers the `enableMultipleAttachments` flag end-to-end in the real list (new per-type bubbles
 * vs deprecated single bubbles) and the files card-stack expand/collapse interaction — both in
 * the list and on the bare [CometChatFilesBubble] composable.
 *
 * Run: ./gradlew :chatuikit-compose:connectedDebugAndroidTest \
 *   -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.compose.presentation.messagelist.ui.MultiAttachmentMessageListComponentTest
 */
@RunWith(AndroidJUnit4::class)
class MultiAttachmentMessageListComponentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        // Message alignment resolves via CometChat.getLoggedInUser().
        MessageListComposeTestHelper.ensureInitialized()
    }

    // ==================== Helpers ====================

    private fun createUser(uid: String, name: String): User = User().apply {
        this.uid = uid
        this.name = name
        this.status = CometChatConstants.USER_STATUS_ONLINE
    }

    private fun attachment(name: String, mime: String) = Attachment().apply {
        fileUrl = "https://cdn.example.com/$name"
        fileName = name
        fileMimeType = mime
        fileExtension = name.substringAfterLast('.', "")
        fileSize = 3_200_000
    }

    private fun fiveFileAttachments() = listOf(
        attachment("report.pdf", "application/pdf"),
        attachment("notes.docx", "application/msword"),
        attachment("data.xlsx", "application/vnd.ms-excel"),
        attachment("slides.pptx", "application/vnd.ms-powerpoint"),
        attachment("archive.zip", "application/zip")
    )

    private fun mediaMessage(
        id: Long,
        type: String,
        attachments: List<Attachment>,
        caption: String? = null
    ): MediaMessage {
        val sender = createUser("user-1", "Alice")
        return MediaMessage("user-1", type, CometChatConstants.RECEIVER_TYPE_USER).apply {
            this.id = id
            this.sender = sender
            this.receiverUid = MessageListComposeTestHelper.LOGGED_IN_USER_UID
            this.sentAt = 1_700_000_000L + id
            this.category = CometChatConstants.CATEGORY_MESSAGE
            this.attachment = attachments.first()
            this.attachments = attachments
            caption?.let { this.caption = it }
        }
    }

    private fun createViewModelWithMessages(messages: List<BaseMessage>): CometChatMessageListViewModel {
        val repository = object : MessageListRepository {
            override suspend fun fetchPreviousMessages() = Result.success(messages)
            override suspend fun fetchNextMessages(fromMessageId: Long) = Result.success(emptyList<BaseMessage>())
            override suspend fun getConversation(id: String, type: String): Result<Conversation> =
                Result.failure(Exception("Not configured"))
            override suspend fun getMessage(messageId: Long): Result<BaseMessage> =
                messages.find { it.id == messageId }?.let { Result.success(it) }
                    ?: Result.failure(Exception("Not found"))
            override suspend fun deleteMessage(message: BaseMessage) = Result.success(message)
            override suspend fun flagMessage(messageId: Long, reason: String, remark: String) = Result.success(Unit)
            override suspend fun addReaction(messageId: Long, emoji: String): Result<BaseMessage> =
                messages.find { it.id == messageId }?.let { Result.success(it) }
                    ?: Result.failure(Exception("Not found"))
            override suspend fun removeReaction(messageId: Long, emoji: String): Result<BaseMessage> =
                messages.find { it.id == messageId }?.let { Result.success(it) }
                    ?: Result.failure(Exception("Not found"))
            override suspend fun markAsDelivered(message: BaseMessage) = Result.success(Unit)
            override suspend fun markAsRead(message: BaseMessage) = Result.success(Unit)
            override suspend fun markAsUnread(message: BaseMessage): Result<Conversation> =
                Result.failure(Exception("Not configured"))
            override fun hasMorePreviousMessages() = false
            override fun resetRequest() {}
            override fun configureForUser(user: User, messagesTypes: List<String>, messagesCategories: List<String>, parentMessageId: Long, messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder?) {}
            override fun configureForGroup(group: Group, messagesTypes: List<String>, messagesCategories: List<String>, parentMessageId: Long, messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder?) {}
            override suspend fun fetchSurroundingMessages(messageId: Long): Result<SurroundingMessagesResult> =
                Result.failure(Exception("Not configured"))
            override suspend fun fetchActionMessages(fromMessageId: Long) = Result.success(emptyList<BaseMessage>())
            override fun rebuildRequestFromMessageId(messageId: Long) {}
            override fun getLatestMessageId() = messages.lastOrNull()?.id ?: -1L
            override fun setLatestMessageId(messageId: Long) {}
        }
        return CometChatMessageListViewModel(repository = repository, enableListeners = false)
    }

    private fun setListContent(messages: List<BaseMessage>, enableMultipleAttachments: Boolean) {
        val viewModel = createViewModelWithMessages(messages)
        val user = createUser("user-1", "Alice")
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageList(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    user = user,
                    enableMultipleAttachments = enableMultipleAttachments
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    // ==================== Flag routing in the real list ====================

    @Test
    fun filesMessage_flagOn_rendersCardStack_collapsedWithMoreToggle() {
        setListContent(
            listOf(mediaMessage(1L, CometChatConstants.MESSAGE_TYPE_FILE, fiveFileAttachments())),
            enableMultipleAttachments = true
        )

        composeTestRule.onNodeWithText("report.pdf").assertExists()
        composeTestRule.onNodeWithText("data.xlsx").assertExists()
        composeTestRule.onNodeWithText("archive.zip").assertDoesNotExist()
        composeTestRule.onNodeWithText("+2 more", substring = true).assertExists()
    }

    @Test
    fun filesMessage_flagOff_rendersDeprecatedSingleBubble_noMoreToggle() {
        setListContent(
            listOf(mediaMessage(1L, CometChatConstants.MESSAGE_TYPE_FILE, fiveFileAttachments())),
            enableMultipleAttachments = false
        )

        // The deprecated single bubble shows only the legacy single attachment — no card stack.
        composeTestRule.onNodeWithText("+2 more", substring = true).assertDoesNotExist()
        composeTestRule.onNodeWithText("archive.zip").assertDoesNotExist()
    }

    @Test
    fun imagesMessage_flagOn_showsCaptionUnderGrid() {
        setListContent(
            listOf(
                mediaMessage(
                    1L,
                    CometChatConstants.MESSAGE_TYPE_IMAGE,
                    listOf(attachment("a.jpg", "image/jpeg"), attachment("b.jpg", "image/jpeg")),
                    caption = "Weekend trip photos"
                )
            ),
            enableMultipleAttachments = true
        )

        composeTestRule.onNodeWithText("Weekend trip photos").assertExists()
    }

    // ==================== Files expand/collapse interaction (bare bubble) ====================

    @Test
    fun filesBubble_expandsAndCollapsesOnToggleTap() {
        val message = mediaMessage(1L, CometChatConstants.MESSAGE_TYPE_FILE, fiveFileAttachments())
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatFilesBubble(
                    message = message,
                    alignment = UIKitConstants.MessageBubbleAlignment.LEFT
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("archive.zip").assertDoesNotExist()

        composeTestRule.onNodeWithText("+2 more", substring = true).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("slides.pptx").assertExists()
        composeTestRule.onNodeWithText("archive.zip").assertExists()

        composeTestRule.onNodeWithText("Show less", substring = true).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("archive.zip").assertDoesNotExist()
    }
}
