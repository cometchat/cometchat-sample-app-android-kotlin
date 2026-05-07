package com.cometchat.uikit.compose.presentation

import com.cometchat.uikit.compose.presentation.calllogs.ui.CometChatCallLogsTestSuite
import com.cometchat.uikit.compose.presentation.conversations.ui.CometChatConversationsAndroidTestSuite
import com.cometchat.uikit.compose.presentation.groups.ui.CometChatGroupsTestSuite
import com.cometchat.uikit.compose.presentation.messagelist.ui.CometChatMessageListTestSuite
import com.cometchat.uikit.compose.presentation.reactionlist.ui.CometChatReactionListTestSuite
import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Master test suite that runs ALL presentation/UI layer instrumented tests
 * by aggregating each component's individual test suite.
 *
 * Components included:
 * - Conversations
 * - Groups
 * - Call Logs
 * - Message List
 * - Reaction List
 *
 * Usage:
 *   ./gradlew :chatuikit-compose:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.compose.presentation.PresentationLayerTestSuite
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    CometChatConversationsAndroidTestSuite::class,
    CometChatGroupsTestSuite::class,
    CometChatCallLogsTestSuite::class,
    CometChatMessageListTestSuite::class,
    CometChatReactionListTestSuite::class
)
class PresentationLayerTestSuite
