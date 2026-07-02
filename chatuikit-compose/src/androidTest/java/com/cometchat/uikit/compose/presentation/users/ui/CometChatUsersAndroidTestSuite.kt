package com.cometchat.uikit.compose.presentation.users.ui

import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Test Suite that aggregates all CometChatUsers Compose instrumented tests.
 *
 * Run the entire suite with:
 *   ./gradlew :chatuikit-compose:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.compose.presentation.users.ui.CometChatUsersAndroidTestSuite
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    CometChatUsersListTest::class,
    CometChatUsersIntegrationTest::class,
    CometChatUsersSelectionModeTest::class,
    CometChatUsersErrorStateTest::class,
    CometChatUsersCustomViewTest::class
)
class CometChatUsersAndroidTestSuite
