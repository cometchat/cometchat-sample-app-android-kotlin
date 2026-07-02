package com.cometchat.uikit.compose.presentation.groupmembers

import io.kotest.core.spec.style.FunSpec

/**
 * Test Suite that aggregates all CometChatGroupMembers Compose JVM tests.
 *
 * Run the entire suite with:
 *   ./gradlew :chatuikit-compose:testDebugUnitTest --tests "*.groupmembers.*"
 *
 * Individual test classes:
 * - CometChatGroupMembersRenderingTest — State → UIState mapping for composable rendering
 * - CometChatGroupMembersInteractionTest — User interactions → ViewModel state changes
 */
class CometChatGroupMembersTestSuite : FunSpec({
    test("GroupMembers Compose JVM test suite marker") {
        println("=== CometChatGroupMembers Compose JVM Test Suite ===")
        println("  - CometChatGroupMembersRenderingTest: State → composable rendering")
        println("  - CometChatGroupMembersInteractionTest: Interactions → state changes")
        println("=== Suite complete ===")
    }
})
