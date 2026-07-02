package com.cometchat.uikit.compose.presentation.messagelist.ui

import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Android instrumented test suite for the CometChatMessageList compose component.
 *
 * Aggregates all instrumented tests for the message list:
 * - Component rendering, pagination, and indicator tests
 * - Long-press popup and options tests
 * - BubbleFactory registration, resolution, and override tests
 * - Error/loading state and custom error view tests
 * - Custom view slot provider tests
 * - Avatar integration tests
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.compose.presentation.messagelist.ui.CometChatMessageListAndroidTestSuite
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    CometChatMessageListComponentTest::class,
    CometChatMessageListOptionsTest::class,
    CometChatMessageListBubbleFactoryTest::class,
    CometChatMessageListErrorStateTest::class,
    CometChatMessageListCustomViewTest::class,
    CometChatMessageListAvatarIntegrationTest::class
)
class CometChatMessageListAndroidTestSuite
