package com.cometchat.uikit.kotlin.presentation.conversations

import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Test suite for the Conversations component (Kotlin/XML) instrumented tests.
 *
 * Includes both integration tests and UI tests that require a device/emulator.
 *
 * Usage:
 *   ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.kotlin.presentation.conversations.CometChatConversationsAndroidTestSuite
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    CometChatConversationsIntegrationTest::class,
    CometChatConversationsUITest::class,
    CometChatConversationsViewIntegrationTest::class,
    CometChatConversationsSelectionModeTest::class,
    CometChatConversationsErrorStateTest::class,
    CometChatConversationsCustomViewTest::class
)
class CometChatConversationsAndroidTestSuite
