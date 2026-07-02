package com.cometchat.uikit.compose.presentation.callbuttons.ui

import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Test suite that aggregates all CometChatCallButtons Compose instrumented tests.
 *
 * Includes:
 * - ComponentTest: Real composable rendering, button clicks, visibility, callbacks, theming
 * - ErrorStateTest: Error callback invocation via real ViewModel chain with fake DataSource
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.compose.presentation.callbuttons.ui.CometChatCallButtonsAndroidTestSuite
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    CometChatCallButtonsComponentTest::class,
    CometChatCallButtonsErrorStateTest::class
)
class CometChatCallButtonsAndroidTestSuite
