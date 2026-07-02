package com.cometchat.uikit.compose.presentation.groupmembers.ui

import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Test Suite that aggregates all CometChatGroupMembers Compose instrumented tests.
 *
 * Run the entire suite with:
 *   ./gradlew :chatuikit-compose:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.compose.presentation.groupmembers.ui.CometChatGroupMembersAndroidTestSuite
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    CometChatGroupMembersListTest::class,
    CometChatGroupMembersSelectionModeTest::class,
    CometChatGroupMembersErrorStateTest::class,
    CometChatGroupMembersCustomViewTest::class
)
class CometChatGroupMembersAndroidTestSuite
