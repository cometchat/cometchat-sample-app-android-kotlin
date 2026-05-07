package com.cometchat.uikit.compose.presentation.reactionlist.ui

import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Test suite for the Reaction List component UI/presentation layer.
 *
 * Runs all reaction-list-related instrumented tests in one go.
 *
 * Usage:
 *   ./gradlew :chatuikit-compose:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.compose.presentation.reactionlist.ui.CometChatReactionListTestSuite
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    CometChatReactionListTest::class
)
class CometChatReactionListTestSuite
