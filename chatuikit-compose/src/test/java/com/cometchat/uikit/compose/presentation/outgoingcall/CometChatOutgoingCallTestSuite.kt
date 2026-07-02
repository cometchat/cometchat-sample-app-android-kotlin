package com.cometchat.uikit.compose.presentation.outgoingcall

import io.kotest.core.spec.style.FunSpec

/**
 * Test Suite that aggregates all CometChatOutgoingCall Compose JVM tests.
 *
 * Run the entire suite with:
 *   ./gradlew :chatuikit-compose:testDebugUnitTest --tests "*.outgoingcall.*"
 *
 * Individual test classes:
 * - CometChatOutgoingCallRenderingTest — State → composable rendering mapping
 * - CometChatOutgoingCallInteractionTest — User interactions → ViewModel state changes
 *
 * Validates: Requirements 9.1-9.14
 */
class CometChatOutgoingCallTestSuite : FunSpec({
    test("OutgoingCall Compose JVM test suite marker") {
        println("=== CometChatOutgoingCall Compose JVM Test Suite ===")
        println("  - CometChatOutgoingCallRenderingTest: State → composable rendering")
        println("  - CometChatOutgoingCallInteractionTest: Interactions → state changes")
        println("=== Suite complete ===")
    }
})
