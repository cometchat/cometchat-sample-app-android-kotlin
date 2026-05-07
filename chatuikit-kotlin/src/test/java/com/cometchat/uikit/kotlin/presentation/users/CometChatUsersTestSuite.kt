package com.cometchat.uikit.kotlin.presentation.users

import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Test suite for the Users component (Kotlin/XML) presentation layer.
 *
 * Runs all users-related unit tests in one go.
 *
 * Usage:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "com.cometchat.uikit.kotlin.presentation.users.CometChatUsersTestSuite"
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    UsersSelectionPreservationTest::class,
    UsersSelectionResetBugExplorationTest::class
)
class CometChatUsersTestSuite
