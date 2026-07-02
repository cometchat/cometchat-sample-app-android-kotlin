package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.cardbubble

import io.kotest.core.spec.style.FunSpec

/**
 * Test suite aggregator for CometChatCardBubble tests.
 *
 * Includes:
 * - CometChatCardBubbleRenderingTest — View rendering, fallback text, view hierarchy
 * - CometChatCardBubbleInteractionTest — Action forwarding, event bus delivery
 * - CometChatCardBubblePropertyTest — PBT invariants (routing, options, alignment)
 * - CardConversationPreviewPropertyTest — Conversation list preview text
 *
 * Run all:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*.cardbubble.*"
 */
class CometChatCardBubbleTestSuite : FunSpec({
    // Suite marker — Kotest discovers and runs all FunSpec classes in this package.
    // This file serves as documentation of the test group.

    test("Card bubble test suite marker") {
        println("  📦 CometChatCardBubble Test Suite")
        println("    → CometChatCardBubbleRenderingTest")
        println("    → CometChatCardBubbleInteractionTest")
        println("    → CometChatCardBubblePropertyTest")
        println("    → CardConversationPreviewPropertyTest")
    }
})
