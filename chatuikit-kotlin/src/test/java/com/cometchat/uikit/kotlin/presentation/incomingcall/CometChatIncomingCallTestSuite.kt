package com.cometchat.uikit.kotlin.presentation.incomingcall

import com.cometchat.uikit.kotlin.presentation.incomingcall.style.CometChatIncomingCallStyleTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Test suite for the IncomingCall component (Kotlin/XML) presentation layer.
 *
 * Aggregates all incoming-call-related JVM unit tests in one go.
 * Note: Kotest FunSpec tests (Rendering, Interaction, Property) run via JUnit Platform,
 * not via Suite runner. They are listed here for documentation purposes.
 *
 * Validates: Requirement 25.8
 *
 * Usage:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*.incomingcall.*"
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    CometChatIncomingCallStyleTest::class
    // Kotest FunSpec tests run via JUnit Platform (not Suite):
    // CometChatIncomingCallRenderingTest::class,
    // CometChatIncomingCallInteractionTest::class,
    // CometChatIncomingCallPropertyTest::class,
)
class CometChatIncomingCallTestSuite
