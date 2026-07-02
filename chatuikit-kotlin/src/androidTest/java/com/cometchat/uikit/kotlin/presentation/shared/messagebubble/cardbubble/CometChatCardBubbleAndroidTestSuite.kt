package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.cardbubble

import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Aggregates all instrumented tests for the CometChatCardBubble component.
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.kotlin.presentation.shared.messagebubble.cardbubble.CometChatCardBubbleAndroidTestSuite
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    CometChatCardBubbleViewIntegrationTest::class
)
class CometChatCardBubbleAndroidTestSuite
