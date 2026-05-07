package com.cometchat.uikit.compose.presentation.calllogs.ui

import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Test suite for the Call Logs component UI/presentation layer.
 *
 * Runs all call-logs-related instrumented tests in one go.
 *
 * Usage:
 *   ./gradlew :chatuikit-compose:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.compose.presentation.calllogs.ui.CometChatCallLogsTestSuite
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    CometChatCallLogsTest::class
)
class CometChatCallLogsTestSuite
