package com.cometchat.uikit.kotlin.presentation.conversations

import com.cometchat.uikit.kotlin.presentation.conversations.style.CometChatConversationListItemStyleTest
import com.cometchat.uikit.kotlin.presentation.conversations.ui.CometChatConversationListItemPropertyTest
import com.cometchat.uikit.kotlin.presentation.conversations.ui.CometChatConversationListItemTest
import com.cometchat.uikit.kotlin.presentation.conversations.ui.ConversationsAdapterTest
import com.cometchat.uikit.kotlin.presentation.conversations.utils.ConversationsDiffCallbackTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Test suite for the Conversations component (Kotlin/XML) presentation layer.
 *
 * Runs all conversation-related unit tests in one go.
 *
 * Usage:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "com.cometchat.uikit.kotlin.presentation.conversationlist.CometChatConversationsTestSuite"
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    // Top-level conversation tests
    CometChatConversationsInteractionTest::class,
    CometChatConversationsRenderingTest::class,
    // Style tests
    CometChatConversationListItemStyleTest::class,
    // UI tests
    CometChatConversationListItemPropertyTest::class,
    CometChatConversationListItemTest::class,
    ConversationsAdapterTest::class,
    // Utils tests
    ConversationsDiffCallbackTest::class
)
class CometChatConversationsTestSuite
