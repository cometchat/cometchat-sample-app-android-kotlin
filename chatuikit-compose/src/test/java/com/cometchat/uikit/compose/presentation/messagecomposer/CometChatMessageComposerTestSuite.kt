package com.cometchat.uikit.compose.presentation.messagecomposer

import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Test suite for the Message Composer component (Compose) presentation layer.
 *
 * Runs all message-composer-related JVM unit tests in one go.
 *
 * Usage:
 *   ./gradlew :chatuikit-compose:testDebugUnitTest --tests "com.cometchat.uikit.compose.presentation.messagecomposer.CometChatMessageComposerTestSuite"
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    CometChatMessageComposerRenderingTest::class,
    CometChatMessageComposerInteractionTest::class,
    CometChatMessageComposerScreenshotTest::class
)
class CometChatMessageComposerTestSuite
