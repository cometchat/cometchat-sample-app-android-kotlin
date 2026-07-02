package com.cometchat.uikit.kotlin.presentation.messageinformation

import io.kotest.core.spec.style.FunSpec

/**
 * Test Suite for CometChatMessageInformation Kotlin JVM tests.
 *
 * Run all MessageInformation Kotlin JVM tests:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*.messageinformation.*"
 *
 * Individual test classes:
 * - CometChatMessageInformationRenderingTest: UIState → rendering states
 * - CometChatMessageInformationInteractionTest: User interactions → state changes
 */
class CometChatMessageInformationTestSuite : FunSpec({

    test("Suite marker — all MessageInformation Kotlin JVM tests") {
        println("=== CometChatMessageInformation Kotlin JVM Test Suite ===")
        println("  • CometChatMessageInformationRenderingTest — 8 tests (states, PBT, conversation type)")
        println("  • CometChatMessageInformationInteractionTest — 6 tests (setMessage, fetch, timestamps, listener)")
        println("  Total: 14 JVM tests")
    }
})
