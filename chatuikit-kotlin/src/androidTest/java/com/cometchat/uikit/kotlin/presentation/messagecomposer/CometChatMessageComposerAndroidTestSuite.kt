package com.cometchat.uikit.kotlin.presentation.messagecomposer

import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Android instrumented test suite for the Message Composer component (Kotlin/XML).
 *
 * Runs all message-composer-related instrumented tests in one go.
 *
 * Usage:
 *   ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.kotlin.presentation.messagecomposer.CometChatMessageComposerAndroidTestSuite
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    CometChatMessageComposerViewIntegrationTest::class
)
class CometChatMessageComposerAndroidTestSuite
