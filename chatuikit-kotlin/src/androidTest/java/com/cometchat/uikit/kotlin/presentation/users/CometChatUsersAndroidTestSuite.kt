package com.cometchat.uikit.kotlin.presentation.users

import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Test Suite that aggregates all CometChatUsers instrumented tests.
 *
 * Run the entire suite with:
 *   ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.kotlin.presentation.users.CometChatUsersAndroidTestSuite
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    CometChatUsersViewIntegrationTest::class,
    CometChatUsersUITest::class,
    CometChatUsersSelectionModeTest::class,
    CometChatUsersErrorStateTest::class,
    CometChatUsersCustomViewTest::class
)
class CometChatUsersAndroidTestSuite
