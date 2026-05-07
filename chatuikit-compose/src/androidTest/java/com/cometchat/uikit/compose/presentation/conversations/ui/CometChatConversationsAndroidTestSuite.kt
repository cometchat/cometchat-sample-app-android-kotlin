package com.cometchat.uikit.compose.presentation.conversations.ui

import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Test suite for the Conversations component UI/presentation layer.
 *
 * Runs all conversation-related instrumented tests in one go.
 *
 * Usage:
 *   ./gradlew :chatuikit-compose:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.compose.presentation.conversations.ui.CometChatConversationsAndroidTestSuite
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    CometChatConversationListTest::class,
    CometChatConversationsCustomViewTest::class,
    CometChatConversationsErrorStateTest::class,
    CometChatConversationsSelectionModeTest::class,
    CometChatConversationsIntegrationTest::class
)
class CometChatConversationsAndroidTestSuite
