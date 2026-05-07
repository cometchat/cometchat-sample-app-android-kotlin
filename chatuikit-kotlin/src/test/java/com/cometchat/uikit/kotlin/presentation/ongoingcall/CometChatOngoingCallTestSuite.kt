package com.cometchat.uikit.kotlin.presentation.ongoingcall

import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Test suite for the Ongoing Call component (Kotlin/XML) presentation layer.
 *
 * Runs all ongoing-call-related unit tests in one go.
 *
 * Usage:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "com.cometchat.uikit.kotlin.presentation.ongoingcall.CometChatOngoingCallTestSuite"
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    OngoingCallBlankPageBugExplorationTest::class,
    OngoingCallPreservationPropertyTest::class
)
class CometChatOngoingCallTestSuite
