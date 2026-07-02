package com.cometchat.uikit.kotlin.presentation.callbuttons

import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Test suite that aggregates all CometChatCallButtons instrumented tests.
 *
 * Includes:
 * - ViewIntegrationTest: Real view inflation with Espresso assertions on button clicks/visibility
 * - UITest: Component rendering, callbacks, visibility controls, accessibility
 * - ErrorStateTest: Error callback invocation, button resilience after errors
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.kotlin.presentation.callbuttons.CometChatCallButtonsAndroidTestSuite
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    CometChatCallButtonsViewIntegrationTest::class,
    CometChatCallButtonsUITest::class,
    CometChatCallButtonsErrorStateTest::class
)
class CometChatCallButtonsAndroidTestSuite
