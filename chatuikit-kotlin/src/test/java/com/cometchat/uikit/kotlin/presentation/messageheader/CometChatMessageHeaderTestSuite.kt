package com.cometchat.uikit.kotlin.presentation.messageheader

import io.kotest.core.spec.style.FunSpec

/**
 * Test suite aggregating all JVM tests for CometChatMessageHeader (chatuikit-kotlin).
 *
 * Run all message header JVM tests:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*.messageheader.*"
 *
 * Individual test classes:
 *   - CometChatMessageHeaderRenderingTest: ViewModel states → UIState for View rendering
 *   - CometChatMessageHeaderInteractionTest: User interactions → ViewModel state changes
 *   - CometChatMessageHeaderStyleTest: Style data class defaults, copy, equality
 *   - CometChatMessageHeaderScreenshotTest: Roborazzi visual regression tests
 */
class CometChatMessageHeaderTestSuite : FunSpec({
    test("suite marker - all message header JVM tests are in this package") {
        println("  📦 CometChatMessageHeader Test Suite (chatuikit-kotlin JVM)")
        println("    → CometChatMessageHeaderRenderingTest")
        println("    → CometChatMessageHeaderInteractionTest")
        println("    → CometChatMessageHeaderStyleTest")
        println("    → CometChatMessageHeaderScreenshotTest")
    }
})
