package com.cometchat.uikit.compose.presentation.users

import io.kotest.core.spec.style.FunSpec

/**
 * Test Suite that aggregates all CometChatUsers Compose JVM tests.
 *
 * Run the entire suite with:
 *   ./gradlew :chatuikit-compose:testDebugUnitTest --tests "*.users.*"
 *
 * Individual test classes:
 * - CometChatUsersRenderingTest: UIState → composable rendering states
 * - CometChatUsersInteractionTest: User interactions → state changes
 * - CometChatUsersPropertyTest: PBT for selection, pagination, visibility invariants
 */
class CometChatUsersTestSuite : FunSpec({
    // This suite serves as documentation and a single entry point.
    // Kotest discovers and runs all FunSpec classes in this package automatically.

    test("Suite marker — all Users Compose JVM tests are in this package") {
        println("=== CometChatUsers Compose JVM Test Suite ===")
        println("  • CometChatUsersRenderingTest — 5 tests (Loading, Empty, Error, Content, data binding)")
        println("  • CometChatUsersInteractionTest — 7 tests (selection, clear, getItemAt, isSelected)")
        println("  • CometChatUsersPropertyTest — 7 PBT tests (selection invariants, pagination, slots, status)")
        println("  Total: 19 JVM tests")
    }
})
