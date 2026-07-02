package com.cometchat.uikit.compose.presentation.threadheader

import io.kotest.core.spec.style.FunSpec

/**
 * Test suite aggregating all JVM tests for CometChatThreadHeader (chatuikit-compose).
 *
 * Includes:
 * - CometChatThreadHeaderRenderingTest: ViewModel states → correct data for composable rendering
 * - CometChatThreadHeaderInteractionTest: User interactions → correct ViewModel state changes
 * - CometChatThreadHeaderScreenshotTest: Visual regression (Roborazzi, in screenshots/ package)
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:testDebugUnitTest --tests "*.threadheader.*"
 */
class CometChatThreadHeaderTestSuite : FunSpec({
    // This suite serves as documentation of all test classes in this package.
    // Individual test classes are discovered automatically by the test runner.
    // Run all threadheader tests with: --tests "*.threadheader.*"

    test("suite marker - all threadheader tests are in this package") {
        // Marker test to ensure the suite file is valid
        println("  📦 CometChatThreadHeader Compose Test Suite")
        println("    → CometChatThreadHeaderRenderingTest")
        println("    → CometChatThreadHeaderInteractionTest")
        println("    → CometChatThreadHeaderScreenshotTest (Roborazzi, in screenshots/)")
    }
})
