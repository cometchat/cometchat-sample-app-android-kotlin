package com.cometchat.uikit.compose.presentation.groups.ui

import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Test suite for the Groups component UI/presentation layer.
 *
 * Runs all groups-related instrumented tests in one go.
 *
 * Usage:
 *   ./gradlew :chatuikit-compose:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.compose.presentation.groups.ui.CometChatGroupsTestSuite
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    CometChatGroupsInstrumentedTest::class
)
class CometChatGroupsTestSuite
