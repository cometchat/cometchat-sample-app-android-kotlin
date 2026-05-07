package com.cometchat.uikit.kotlin.presentation

import com.cometchat.uikit.kotlin.presentation.calls.CometChatCallsTestSuite
import com.cometchat.uikit.kotlin.presentation.conversations.CometChatConversationsTestSuite
import com.cometchat.uikit.kotlin.presentation.groupmembers.CometChatGroupMembersTestSuite
import com.cometchat.uikit.kotlin.presentation.messagecomposer.CometChatMessageComposerTestSuite
import com.cometchat.uikit.kotlin.presentation.messagelist.CometChatMessageListTestSuite
import com.cometchat.uikit.kotlin.presentation.ongoingcall.CometChatOngoingCallTestSuite
import com.cometchat.uikit.kotlin.presentation.search.CometChatSearchTestSuite
import com.cometchat.uikit.kotlin.presentation.users.CometChatUsersTestSuite
import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Master test suite that runs ALL presentation layer unit tests for chatuikit-kotlin.
 *
 * Components included:
 * - Conversations
 * - Message List
 * - Message Composer
 * - Calls
 * - Ongoing Call
 * - Group Members
 * - Users
 * - Search
 *
 * Usage:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "com.cometchat.uikit.kotlin.presentation.PresentationLayerTestSuite"
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    CometChatConversationsTestSuite::class,
    CometChatMessageListTestSuite::class,
    CometChatMessageComposerTestSuite::class,
    CometChatCallsTestSuite::class,
    CometChatOngoingCallTestSuite::class,
    CometChatGroupMembersTestSuite::class,
    CometChatUsersTestSuite::class,
    CometChatSearchTestSuite::class
)
class PresentationLayerTestSuite
