package com.cometchat.uikit.kotlin.presentation.callbuttons

import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Test suite for the CallButtons component (Kotlin/XML) presentation layer.
 *
 * Aggregates all call-buttons-related JVM unit tests in one go.
 * Note: Kotest FunSpec tests (Rendering, Interaction, Property) run via JUnit Platform,
 * not via Suite runner. They are listed here for documentation purposes.
 *
 * Validates: Requirement 25.8
 *
 * Usage:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*.callbuttons.*"
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    // Kotest FunSpec tests run via JUnit Platform (not Suite):
    // CometChatCallButtonsRenderingTest::class,
    // CometChatCallButtonsInteractionTest::class,
    // CometChatCallButtonsPropertyTest::class,
)
class CometChatCallButtonsTestSuite
