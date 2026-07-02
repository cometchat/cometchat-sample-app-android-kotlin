package com.cometchat.uikit.compose.presentation.messageinformation.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.MessageReceipt
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.presentation.messageinformation.ui.CometChatMessageInformation
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import com.cometchat.uikit.core.data.datasource.MessageReceiptEventListener
import com.cometchat.uikit.core.domain.repository.MessageInformationRepository
import com.cometchat.uikit.core.viewmodel.CometChatMessageInformationViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented UI tests for CometChatMessageInformation Compose component.
 *
 * Tests verify composable rendering of receipt information for both
 * USER and GROUP conversations.
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.compose.presentation.messageinformation.ui.CometChatMessageInformationListTest
 */
@RunWith(AndroidJUnit4::class)
class CometChatMessageInformationListTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private fun createGroupMessage(messageId: Long): BaseMessage {
        return TextMessage("group-1", "Hello group!", CometChatConstants.RECEIVER_TYPE_GROUP).apply {
            id = messageId
            sender = User().apply { uid = "sender-1"; name = "Sender" }
        }
    }

    private fun createUserMessage(messageId: Long, deliveredAt: Long, readAt: Long): BaseMessage {
        val receiver = User().apply { uid = "receiver-1"; name = "Receiver" }
        return TextMessage(receiver.uid, "Hello!", CometChatConstants.RECEIVER_TYPE_USER).apply {
            id = messageId
            this.deliveredAt = deliveredAt
            this.readAt = readAt
            this.receiver = receiver
            sender = User().apply { uid = "sender-1"; name = "Sender" }
        }
    }

    private fun createMockReceipts(count: Int, messageId: Long = 100L): List<MessageReceipt> {
        return (1..count).map { i ->
            MessageReceipt().apply {
                this.messageId = messageId
                this.deliveredAt = 1000L + i * 100
                this.readAt = 2000L + i * 100
                sender = User().apply { uid = "user-$i"; name = "User $i" }
            }
        }
    }

    private fun createViewModel(
        receipts: List<MessageReceipt> = emptyList(),
        error: CometChatException? = null
    ): CometChatMessageInformationViewModel {
        val repository = object : MessageInformationRepository {
            override suspend fun fetchReceipts(messageId: Long): Result<List<MessageReceipt>> {
                return if (error != null) Result.failure(error) else Result.success(receipts)
            }
            override fun createReceiptFromMessage(message: BaseMessage): MessageReceipt {
                return MessageReceipt().apply {
                    sender = if (message.receiver is User) message.receiver as User else null
                    readAt = message.readAt
                    deliveredAt = if (message.deliveredAt == 0L) message.readAt else message.deliveredAt
                    messageId = message.id
                }
            }
        }
        return CometChatMessageInformationViewModel(
            repository = repository,
            eventListener = MessageReceiptEventListener(),
            enableListeners = false
        )
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 1: Toolbar title "Message Info" is displayed
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun messageInformation_displaysToolbarTitle() {
        println("=== TEST: messageInformation_displaysToolbarTitle ===")
        val viewModel = createViewModel()
        val message = createGroupMessage(100L)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageInformation(
                    message = message,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.waitForIdle()
        println("ACTION: Checking toolbar title is displayed")
        composeTestRule.onNodeWithText("Message Info").assertIsDisplayed()
        println("RESULT: Toolbar title 'Message Info' displayed ✅")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 2: GROUP conversation shows receipt user names
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun groupConversation_displaysReceiptUserNames() {
        println("=== TEST: groupConversation_displaysReceiptUserNames ===")
        val receipts = createMockReceipts(3, messageId = 100L)
        val viewModel = createViewModel(receipts = receipts)
        val message = createGroupMessage(100L)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageInformation(
                    message = message,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.waitForIdle()
        println("ACTION: Checking receipt user names are displayed")
        composeTestRule.onNodeWithText("User 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("User 2").assertIsDisplayed()
        composeTestRule.onNodeWithText("User 3").assertIsDisplayed()
        println("RESULT: All 3 receipt user names displayed ✅")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 3: USER conversation shows "Read" and "Delivered" labels
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun userConversation_displaysReadAndDeliveredLabels() {
        println("=== TEST: userConversation_displaysReadAndDeliveredLabels ===")
        val viewModel = createViewModel()
        val message = createUserMessage(100L, deliveredAt = 1000L, readAt = 2000L)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageInformation(
                    message = message,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.waitForIdle()
        println("ACTION: Checking Read and Delivered labels")
        composeTestRule.onNodeWithText("Read").assertIsDisplayed()
        composeTestRule.onNodeWithText("Delivered").assertIsDisplayed()
        println("RESULT: Read and Delivered labels displayed ✅")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 4: Toolbar hidden when configured
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun messageInformation_hidesToolbarWhenConfigured() {
        println("=== TEST: messageInformation_hidesToolbarWhenConfigured ===")
        val viewModel = createViewModel()
        val message = createGroupMessage(100L)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageInformation(
                    message = message,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    hideToolBar = true
                )
            }
        }

        composeTestRule.waitForIdle()
        println("ACTION: Checking toolbar title does NOT exist when hidden")
        // When toolbar is hidden, the title should not be rendered
        composeTestRule.onNodeWithText("Message Info").assertDoesNotExist()
        println("RESULT: Toolbar hidden successfully ✅")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 5: Custom toolbar title
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun messageInformation_displaysCustomToolbarTitle() {
        println("=== TEST: messageInformation_displaysCustomToolbarTitle ===")
        val viewModel = createViewModel()
        val message = createGroupMessage(100L)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatMessageInformation(
                    message = message,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    toolBarTitleText = "Receipt Details"
                )
            }
        }

        composeTestRule.waitForIdle()
        println("ACTION: Checking custom title is displayed")
        composeTestRule.onNodeWithText("Receipt Details").assertIsDisplayed()
        println("RESULT: Custom toolbar title 'Receipt Details' displayed ✅")
    }
}
