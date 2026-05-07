package com.cometchat.uikit.kotlin.presentation.calls

import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Test suite for the Calls component (Kotlin/XML) presentation layer.
 *
 * Runs all call-related unit tests in one go.
 *
 * Usage:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "com.cometchat.uikit.kotlin.presentation.calls.CometChatCallsTestSuite"
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    CallFlowTransitionBugExplorationTest::class,
    CallFlowTransitionPreservationTest::class
)
class CometChatCallsTestSuite
