package com.cometchat.uikit.compose.presentation.messagelist.ui

import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Test suite for the Message List component UI/presentation layer.
 *
 * Runs all message-list-related instrumented tests in one go.
 *
 * Usage:
 *   ./gradlew :chatuikit-compose:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.compose.presentation.messagelist.ui.CometChatMessageListTestSuite
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    CometChatMessageListAvatarIntegrationTest::class
)
class CometChatMessageListTestSuite
