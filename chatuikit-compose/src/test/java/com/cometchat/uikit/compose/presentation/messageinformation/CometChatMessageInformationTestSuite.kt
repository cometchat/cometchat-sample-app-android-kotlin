package com.cometchat.uikit.compose.presentation.messageinformation

import io.kotest.core.spec.style.FunSpec

/**
 * Test Suite for CometChatMessageInformation Compose JVM tests.
 *
 * Run all MessageInformation Compose JVM tests:
 *   ./gradlew :chatuikit-compose:testDebugUnitTest --tests "*.messageinformation.*"
 *
 * Individual test classes:
 * - CometChatMessageInformationRenderingTest: UIState → composable rendering
 * - CometChatMessageInformationInteractionTest: Interactions → state changes
 */
class CometChatMessageInformationTestSuite : FunSpec({

    test("Suite marker — all MessageInformation Compose JVM tests") {
        println("=== CometChatMessageInformation Compose JVM Test Suite ===")
        println("  • CometChatMessageInformationRenderingTest — 6 tests (states, PBT)")
        println("  • CometChatMessageInformationInteractionTest — 5 tests (setMessage, conversationType, PBT)")
        println("  Total: 11 JVM tests")
    }
})
