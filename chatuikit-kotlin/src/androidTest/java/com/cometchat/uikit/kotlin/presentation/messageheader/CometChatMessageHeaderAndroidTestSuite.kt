package com.cometchat.uikit.kotlin.presentation.messageheader

import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Test suite aggregating all instrumented tests for CometChatMessageHeader (chatuikit-kotlin).
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.kotlin.presentation.messageheader.CometChatMessageHeaderAndroidTestSuite
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    CometChatMessageHeaderViewIntegrationTest::class,
    CometChatMessageHeaderUITest::class,
    CometChatMessageHeaderCustomViewTest::class
)
class CometChatMessageHeaderAndroidTestSuite
