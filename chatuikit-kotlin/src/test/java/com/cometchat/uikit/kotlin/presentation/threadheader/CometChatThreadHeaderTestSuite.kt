package com.cometchat.uikit.kotlin.presentation.threadheader

import com.cometchat.uikit.kotlin.presentation.threadheader.style.CometChatThreadHeaderStyleTest
import io.kotest.core.spec.style.FunSpec

/**
 * Test suite aggregating all JVM tests for CometChatThreadHeader (chatuikit-kotlin).
 *
 * Includes:
 * - CometChatThreadHeaderRenderingTest: ViewModel states → correct data for View rendering
 * - CometChatThreadHeaderInteractionTest: User interactions → correct ViewModel state changes
 * - CometChatThreadHeaderStyleTest: Style data class defaults, copy, equals
 * - CometChatThreadHeaderScreenshotTest: Visual regression (Roborazzi, run separately)
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*.threadheader.*"
 */
class CometChatThreadHeaderTestSuite : FunSpec({
    // This suite serves as documentation of all test classes in this package.
    // Individual test classes are discovered automatically by the test runner.
    // Run all threadheader tests with: --tests "*.threadheader.*"

    test("suite marker - all threadheader tests are in this package") {
        // Marker test to ensure the suite file is valid
        println("  📦 CometChatThreadHeader Test Suite")
        println("    → CometChatThreadHeaderRenderingTest")
        println("    → CometChatThreadHeaderInteractionTest")
        println("    → CometChatThreadHeaderStyleTest")
        println("    → CometChatThreadHeaderScreenshotTest (Roborazzi)")
    }
})
