package com.cometchat.uikit.compose.presentation.messagecomposer.ui

import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Android instrumented test suite for the Message Composer component (Compose).
 *
 * Runs all message-composer-related instrumented tests in one go.
 *
 * Usage:
 *   ./gradlew :chatuikit-compose:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.compose.presentation.messagecomposer.ui.CometChatMessageComposerAndroidTestSuite
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    CometChatMessageComposerListTest::class
)
class CometChatMessageComposerAndroidTestSuite
