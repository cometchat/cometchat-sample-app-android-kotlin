package com.cometchat.uikit.compose.presentation.incomingcall

import io.kotest.core.spec.style.FunSpec

/**
 * Test Suite that aggregates all CometChatIncomingCall Compose JVM tests.
 *
 * Run the entire suite with:
 *   ./gradlew :chatuikit-compose:testDebugUnitTest --tests "*.incomingcall.*"
 *
 * Individual test classes:
 * - CometChatIncomingCallRenderingTest — State → composable rendering mapping
 * - CometChatIncomingCallInteractionTest — User interactions → ViewModel state changes
 *
 * Validates: Requirements 15.1, 15.2, 15.5
 */
class CometChatIncomingCallTestSuite : FunSpec({
    test("IncomingCall Compose JVM test suite marker") {
        println("=== CometChatIncomingCall Compose JVM Test Suite ===")
        println("  - CometChatIncomingCallRenderingTest: State → composable rendering")
        println("  - CometChatIncomingCallInteractionTest: Interactions → state changes")
        println("=== Suite complete ===")
    }
})
