package com.cometchat.uikit.kotlin.presentation.calllogs

import com.cometchat.uikit.kotlin.presentation.calllogs.style.CometChatCallLogsStyleTest
import com.cometchat.uikit.kotlin.presentation.calllogs.ui.CallLogsAdapterTest
import com.cometchat.uikit.kotlin.presentation.calllogs.utils.CallLogsDiffCallbackTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Test suite for the CallLogs component (Kotlin/XML) presentation layer.
 *
 * Aggregates all call-logs-related JVM unit tests in one go.
 *
 * Validates: Requirement 25.8
 *
 * Usage:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "com.cometchat.uikit.kotlin.presentation.calllogs.CometChatCallLogsTestSuite"
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    // Top-level call logs tests (Kotest — run via JUnit Platform, not Suite)
    // CometChatCallLogsRenderingTest::class,      // Kotest FunSpec
    // CometChatCallLogsInteractionTest::class,    // Kotest FunSpec
    // CometChatCallLogsPropertyTest::class,       // Kotest FunSpec
    // Style tests
    CometChatCallLogsStyleTest::class,
    // UI tests
    CallLogsAdapterTest::class,
    // Utils tests
    CallLogsDiffCallbackTest::class
)
class CometChatCallLogsTestSuite
