package com.cometchat.uikit.compose.presentation.groups

import io.kotest.core.spec.style.FunSpec

/**
 * Test Suite that aggregates all CometChatGroups Compose JVM tests.
 *
 * Run the entire suite with:
 *   ./gradlew :chatuikit-compose:testDebugUnitTest --tests "*.groups.*"
 *
 * Individual test classes:
 * - CometChatGroupsRenderingTest — State → UIState mapping for composable rendering
 * - CometChatGroupsInteractionTest — User interactions → ViewModel state changes
 */
class CometChatGroupsTestSuite : FunSpec({
    test("Groups Compose JVM test suite marker") {
        println("=== CometChatGroups Compose JVM Test Suite ===")
        println("  - CometChatGroupsRenderingTest: State → composable rendering")
        println("  - CometChatGroupsInteractionTest: Interactions → state changes")
        println("=== Suite complete ===")
    }
})
