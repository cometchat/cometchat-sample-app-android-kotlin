package com.cometchat.uikit.kotlin.presentation.outgoingcall

import com.cometchat.uikit.kotlin.presentation.outgoingcall.style.CometChatOutgoingCallStyleTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Test suite for the OutgoingCall component (Kotlin/XML) presentation layer.
 *
 * Aggregates all outgoing-call-related JVM unit tests in one go.
 * Note: Kotest FunSpec tests (Rendering, Interaction, Property) run via JUnit Platform,
 * not via Suite runner. They are listed here for documentation purposes.
 *
 * Validates: Requirements 9.1-9.14
 *
 * Usage:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*.outgoingcall.*"
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    CometChatOutgoingCallStyleTest::class
    // Kotest FunSpec tests run via JUnit Platform (not Suite):
    // CometChatOutgoingCallRenderingTest::class,
    // CometChatOutgoingCallInteractionTest::class,
    // CometChatOutgoingCallPropertyTest::class,
)
class CometChatOutgoingCallTestSuite
