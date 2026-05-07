package com.cometchat.uikit.compose.presentation.conversations

import com.cometchat.uikit.compose.presentation.conversations.ui.CometChatConversationListItemPropertyTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Test suite for the Conversations component (Compose) presentation layer JVM tests.
 *
 * Runs all conversation-related unit and property-based tests in one go.
 *
 * Mirrors: chatuikit-kotlin CometChatConversationsTestSuite
 *
 * Usage:
 *   ./gradlew :chatuikit-compose:testDebugUnitTest --tests "com.cometchat.uikit.compose.presentation.conversations.CometChatConversationsTestSuite"
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    // Top-level conversation tests
    CometChatConversationsRenderingTest::class,
    CometChatConversationsInteractionTest::class,
    // UI tests
    CometChatConversationListItemPropertyTest::class
)
class CometChatConversationsTestSuite
