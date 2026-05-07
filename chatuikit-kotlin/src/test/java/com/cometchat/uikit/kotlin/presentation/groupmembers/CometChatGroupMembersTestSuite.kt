package com.cometchat.uikit.kotlin.presentation.groupmembers

import com.cometchat.uikit.kotlin.presentation.groupmembers.ui.CometChatGroupMembersTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Test suite for the Group Members component (Kotlin/XML) presentation layer.
 *
 * Runs all group-members-related unit tests in one go.
 *
 * Usage:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "com.cometchat.uikit.kotlin.presentation.groupmembers.CometChatGroupMembersTestSuite"
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    CometChatGroupMembersTest::class
)
class CometChatGroupMembersTestSuite
