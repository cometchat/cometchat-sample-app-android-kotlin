package com.cometchat.uikit.kotlin.presentation.groups

import io.kotest.core.spec.style.FunSpec

/**
 * Test Suite that aggregates all CometChatGroups Kotlin JVM tests.
 *
 * Run the entire suite with:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*.groups.*"
 *
 * Individual test classes:
 * - CometChatGroupsRenderingTest — State → UIState mapping for View rendering
 * - CometChatGroupsInteractionTest — User interactions → ViewModel state changes
 * - GroupsXmlPropertyTest — PBT for group type indicators and selection count
 */
class CometChatGroupsTestSuite : FunSpec({
    test("Groups JVM test suite marker") {
        println("=== CometChatGroups Kotlin JVM Test Suite ===")
        println("  - CometChatGroupsRenderingTest: State → UIState mapping")
        println("  - CometChatGroupsInteractionTest: Interactions → state changes")
        println("  - GroupsXmlPropertyTest: Group type indicators + selection count PBT")
        println("=== Suite complete ===")
    }
})
