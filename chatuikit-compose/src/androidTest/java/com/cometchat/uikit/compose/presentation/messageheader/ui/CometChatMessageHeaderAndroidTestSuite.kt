package com.cometchat.uikit.compose.presentation.messageheader.ui

import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Test suite aggregating all instrumented tests for CometChatMessageHeader (chatuikit-compose).
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.compose.presentation.messageheader.ui.CometChatMessageHeaderAndroidTestSuite
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    CometChatMessageHeaderListTest::class,
    CometChatMessageHeaderIntegrationTest::class,
    CometChatMessageHeaderCustomViewTest::class
)
class CometChatMessageHeaderAndroidTestSuite
