package com.cometchat.uikit.compose.presentation.messageheader

import io.kotest.core.spec.style.FunSpec

/**
 * Test suite aggregating all JVM tests for CometChatMessageHeader (chatuikit-compose).
 *
 * Run all message header JVM tests:
 *   ./gradlew :chatuikit-compose:testDebugUnitTest --tests "*.messageheader.*"
 *
 * Individual test classes:
 *   - CometChatMessageHeaderRenderingTest: ViewModel states → UIState for composable rendering
 *   - CometChatMessageHeaderInteractionTest: User interactions → ViewModel state changes
 *   - CometChatMessageHeaderScreenshotTest: Roborazzi visual regression tests
 */
class CometChatMessageHeaderTestSuite : FunSpec({
    test("suite marker - all message header JVM tests are in this package") {
        println("  📦 CometChatMessageHeader Test Suite (chatuikit-compose JVM)")
        println("    → CometChatMessageHeaderRenderingTest")
        println("    → CometChatMessageHeaderInteractionTest")
        println("    → CometChatMessageHeaderScreenshotTest")
    }
})
