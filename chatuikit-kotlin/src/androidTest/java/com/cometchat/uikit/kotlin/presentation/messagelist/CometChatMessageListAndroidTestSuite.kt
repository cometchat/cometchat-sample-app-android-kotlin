package com.cometchat.uikit.kotlin.presentation.messagelist

import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Test suite for the MessageList component (Kotlin/XML) instrumented tests.
 *
 * Includes integration tests and UI tests that require a device/emulator.
 *
 * Usage:
 *   ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.kotlin.presentation.messagelist.CometChatMessageListAndroidTestSuite
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    CometChatMessageListViewIntegrationTest::class,
    CometChatMessageListUITest::class,
    CometChatMessageListOptionsTest::class,
    CometChatMessageListErrorStateTest::class,
    CometChatMessageListCustomViewTest::class
)
class CometChatMessageListAndroidTestSuite
