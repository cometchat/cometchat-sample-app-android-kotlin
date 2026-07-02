package com.cometchat.uikit.compose.presentation.shared.messagebubble.cardbubble

import io.kotest.core.spec.style.FunSpec

/**
 * Test suite aggregator for CometChatCardBubble composable tests.
 *
 * Includes:
 * - CometChatCardBubbleComposeRenderingTest — Card JSON extraction, fallback logic, width constraint
 * - CometChatCardBubbleComposeInteractionTest — Action forwarding, dual-channel, PBT
 *
 * Run all:
 *   ./gradlew :chatuikit-compose:testDebugUnitTest --tests "*.cardbubble.*"
 */
class CometChatCardBubbleComposeTestSuite : FunSpec({

    test("Compose Card bubble test suite marker") {
        println("  📦 CometChatCardBubble Compose Test Suite")
        println("    → CometChatCardBubbleComposeRenderingTest")
        println("    → CometChatCardBubbleComposeInteractionTest")
    }
})
