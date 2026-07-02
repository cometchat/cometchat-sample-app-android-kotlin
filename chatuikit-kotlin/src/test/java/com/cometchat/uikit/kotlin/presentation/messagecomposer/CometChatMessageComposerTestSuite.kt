package com.cometchat.uikit.kotlin.presentation.messagecomposer

import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Test suite for the Message Composer component (Kotlin/XML) presentation layer.
 *
 * Runs all message-composer-related unit tests in one go.
 *
 * Usage:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "com.cometchat.uikit.kotlin.presentation.messagecomposer.CometChatMessageComposerTestSuite"
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    CometChatMessageComposerCallbackTest::class,
    CometChatMessageComposerStyleTest::class,
    CometChatMessageComposerRenderingTest::class,
    CometChatMessageComposerInteractionTest::class,
    CometChatMessageComposerScreenshotTest::class
)
class CometChatMessageComposerTestSuite
