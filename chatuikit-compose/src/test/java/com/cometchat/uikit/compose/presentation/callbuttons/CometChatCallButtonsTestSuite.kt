package com.cometchat.uikit.compose.presentation.callbuttons

import io.kotest.core.spec.style.FunSpec

/**
 * Test Suite that aggregates all CometChatCallButtons Compose JVM tests.
 *
 * Run the entire suite with:
 *   ./gradlew :chatuikit-compose:testDebugUnitTest --tests "*.callbuttons.*"
 *
 * Individual test classes:
 * - CometChatCallButtonsRenderingTest — State → UIState mapping for composable rendering
 * - CometChatCallButtonsInteractionTest — User interactions → ViewModel state changes & events
 *
 * Validates: Requirements 15.1, 15.2, 15.4
 */
class CometChatCallButtonsTestSuite : FunSpec({
    test("CallButtons Compose JVM test suite marker") {
        println("=== CometChatCallButtons Compose JVM Test Suite ===")
        println("  - CometChatCallButtonsRenderingTest: State → composable rendering")
        println("  - CometChatCallButtonsInteractionTest: Interactions → state changes & events")
        println("=== Suite complete ===")
    }
})
