package com.cometchat.sampleapp.kotlin.chats

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.forAll
import java.io.File

/**
 * Bug Condition Exploration Test for Conversations Search Icon Parity
 * 
 * **Validates: Requirements 1.1, 1.2, 1.3**
 * 
 * Property 1: Bug Condition - Overflow Menu Contains Search Icon
 * 
 * This test MUST FAIL on unfixed code - failure confirms the bug exists.
 * DO NOT attempt to fix the test or the code when it fails.
 * 
 * GOAL: Surface counterexamples that demonstrate the bug exists:
 * - `createOverflowMenuView()` returns a LinearLayout with search icon instead of CometChatAvatar directly
 * 
 * The bug manifests when ChatsFragment creates its overflow menu view:
 * - Current (Buggy): Returns LinearLayout container with search ImageView + CometChatAvatar
 * - Expected (Correct): Returns CometChatAvatar directly (matching master-app-kotlin logoutView)
 */
class ChatsFragmentBugConditionTest : FunSpec({

    val projectRoot = File(System.getProperty("user.dir")).parentFile
    val sampleAppKotlinPath = File(projectRoot, "sample-app-kotlin")
    val chatsFragmentPath = File(
        sampleAppKotlinPath, 
        "src/main/java/com/cometchat/sampleapp/kotlin/ui/chats/ChatsFragment.kt"
    )

    /**
     * Bug condition specification based on design.md:
     * 
     * FUNCTION isBugCondition(overflowMenuView)
     *   INPUT: overflowMenuView of type View
     *   OUTPUT: boolean
     *   
     *   RETURN overflowMenuView IS LinearLayout
     *          AND overflowMenuView.childCount > 1
     *          AND overflowMenuView.containsSearchIcon()
     * END FUNCTION
     */
    data class BugIndicator(
        val pattern: String,
        val description: String,
        val requirement: String
    )

    // Define bug indicators - patterns that indicate the bug exists
    val bugIndicators = listOf(
        // Bug indicator 1: LinearLayout container creation
        BugIndicator(
            pattern = "LinearLayout(context)",
            description = "createOverflowMenuView() creates a LinearLayout container instead of returning CometChatAvatar directly",
            requirement = "1.1"
        ),
        // Bug indicator 2: Search icon ImageView creation
        BugIndicator(
            pattern = "ImageView(context)",
            description = "createOverflowMenuView() creates a search ImageView that doesn't exist in master-app-kotlin",
            requirement = "1.2"
        ),
        // Bug indicator 3: Search icon resource reference
        BugIndicator(
            pattern = "cometchat_ic_search",
            description = "createOverflowMenuView() references search icon drawable",
            requirement = "1.2"
        ),
        // Bug indicator 4: Container addView for search icon
        BugIndicator(
            pattern = "container.addView(searchIcon)",
            description = "createOverflowMenuView() adds search icon to container",
            requirement = "1.3"
        ),
        // Bug indicator 5: Container addView for avatar
        BugIndicator(
            pattern = "container.addView(avatar)",
            description = "createOverflowMenuView() adds avatar to container instead of returning directly",
            requirement = "1.3"
        ),
        // Bug indicator 6: Return container instead of avatar
        BugIndicator(
            pattern = "return container",
            description = "createOverflowMenuView() returns container instead of avatar directly",
            requirement = "1.1"
        ),
        // Bug indicator 7: navigateToSearch function exists
        BugIndicator(
            pattern = "navigateToSearch()",
            description = "navigateToSearch() function exists for search icon click handler",
            requirement = "1.2"
        )
    )

    /**
     * Extract the createOverflowMenuView() function content from ChatsFragment.kt
     */
    fun extractCreateOverflowMenuViewFunction(): String? {
        if (!chatsFragmentPath.exists()) return null
        
        val content = chatsFragmentPath.readText()
        
        // Find the function start
        val functionStart = content.indexOf("private fun createOverflowMenuView()")
        if (functionStart == -1) return null
        
        // Find the function body by counting braces
        var braceCount = 0
        var functionStarted = false
        var functionEnd = functionStart
        
        for (i in functionStart until content.length) {
            when (content[i]) {
                '{' -> {
                    braceCount++
                    functionStarted = true
                }
                '}' -> {
                    braceCount--
                    if (functionStarted && braceCount == 0) {
                        functionEnd = i + 1
                        break
                    }
                }
            }
        }
        
        return content.substring(functionStart, functionEnd)
    }

    /**
     * Check if a bug indicator pattern exists in the function
     */
    fun bugIndicatorExists(indicator: BugIndicator, functionContent: String): Boolean {
        return functionContent.contains(indicator.pattern)
    }

    /**
     * Check if the function returns CometChatAvatar directly (expected behavior)
     */
    fun returnsAvatarDirectly(functionContent: String): Boolean {
        // Expected pattern: return avatar (or return cometchatAvatar)
        // Bug pattern: return container
        val hasReturnAvatar = functionContent.contains("return avatar") || 
                              functionContent.contains("return cometchatAvatar")
        val hasReturnContainer = functionContent.contains("return container")
        
        // Returns avatar directly if it returns avatar AND does NOT return container
        return hasReturnAvatar && !hasReturnContainer
    }

    /**
     * Check if the function creates a LinearLayout container
     */
    fun createsLinearLayoutContainer(functionContent: String): Boolean {
        return functionContent.contains("LinearLayout(context)") ||
               functionContent.contains("LinearLayout(requireContext())")
    }

    /**
     * Check if the function creates a search ImageView
     */
    fun createsSearchImageView(functionContent: String): Boolean {
        return functionContent.contains("ImageView(context)") ||
               functionContent.contains("ImageView(requireContext())")
    }

    // ========================================================================
    // PROPERTY 1: Bug Condition - Overflow Menu Contains Search Icon
    // ========================================================================

    test("Property 1: Bug Condition - createOverflowMenuView() should return CometChatAvatar directly (not LinearLayout)") {
        /**
         * **Validates: Requirements 1.1, 1.2, 1.3**
         * 
         * This test verifies that createOverflowMenuView() returns a CometChatAvatar directly
         * without any container LinearLayout or search icon.
         * 
         * EXPECTED: This test FAILS on unfixed code, confirming the bug exists.
         * 
         * Bug Condition: createOverflowMenuView() returns LinearLayout with search icon
         * Expected Behavior: createOverflowMenuView() returns CometChatAvatar directly
         */
        val functionContent = extractCreateOverflowMenuViewFunction()
        
        functionContent shouldNotBe null
        
        val bugIndicatorsFound = mutableListOf<BugIndicator>()
        
        bugIndicators.forEach { indicator ->
            if (bugIndicatorExists(indicator, functionContent!!)) {
                bugIndicatorsFound.add(indicator)
            }
        }
        
        // Print detailed counterexamples for documentation
        if (bugIndicatorsFound.isNotEmpty()) {
            println("\n=== BUG CONDITION CONFIRMED: Search Icon Parity Bug Found ===")
            println("Total bug indicators found: ${bugIndicatorsFound.size} out of ${bugIndicators.size}")
            println("\nCounterexamples (bug indicators in createOverflowMenuView()):")
            bugIndicatorsFound.forEachIndexed { index, indicator ->
                println("  ${index + 1}. [Req ${indicator.requirement}] ${indicator.description}")
                println("     Pattern found: '${indicator.pattern}'")
            }
            println("\n=== EXPECTED BEHAVIOR ===")
            println("createOverflowMenuView() should return CometChatAvatar directly:")
            println("  - NO LinearLayout container")
            println("  - NO search ImageView")
            println("  - NO container.addView() calls")
            println("  - Return avatar directly (not container)")
            println("\n=== END COUNTEREXAMPLES ===\n")
        }
        
        // This assertion should FAIL on unfixed code
        // Bug condition: bugIndicatorsFound is NOT empty (bug exists)
        // Expected behavior: bugIndicatorsFound IS empty (no bug)
        bugIndicatorsFound.isEmpty() shouldBe true
    }

    test("Property 1.1: createOverflowMenuView() should NOT create a LinearLayout container") {
        /**
         * **Validates: Requirement 1.1**
         * 
         * Verifies that createOverflowMenuView() does NOT create a LinearLayout container.
         * The function should return CometChatAvatar directly.
         * 
         * EXPECTED: This test FAILS on unfixed code.
         */
        val functionContent = extractCreateOverflowMenuViewFunction()
        
        functionContent shouldNotBe null
        
        val createsContainer = createsLinearLayoutContainer(functionContent!!)
        
        if (createsContainer) {
            println("\nCounterexample: createOverflowMenuView() creates LinearLayout container")
            println("  Found: LinearLayout(context) or LinearLayout(requireContext())")
            println("  Expected: No LinearLayout container, return CometChatAvatar directly")
        }
        
        // This assertion should FAIL on unfixed code
        createsContainer shouldBe false
    }

    test("Property 1.2: createOverflowMenuView() should NOT create a search ImageView") {
        /**
         * **Validates: Requirement 1.2**
         * 
         * Verifies that createOverflowMenuView() does NOT create a search ImageView.
         * The master-app-kotlin reference only has the avatar, no search icon.
         * 
         * EXPECTED: This test FAILS on unfixed code.
         */
        val functionContent = extractCreateOverflowMenuViewFunction()
        
        functionContent shouldNotBe null
        
        val createsSearchIcon = createsSearchImageView(functionContent!!)
        
        if (createsSearchIcon) {
            println("\nCounterexample: createOverflowMenuView() creates search ImageView")
            println("  Found: ImageView(context) or ImageView(requireContext())")
            println("  Expected: No ImageView, only CometChatAvatar")
        }
        
        // This assertion should FAIL on unfixed code
        createsSearchIcon shouldBe false
    }

    test("Property 1.3: createOverflowMenuView() should NOT have multiple children (ViewGroup)") {
        /**
         * **Validates: Requirement 1.3**
         * 
         * Verifies that createOverflowMenuView() does NOT add multiple views to a container.
         * The function should return a single CometChatAvatar, not a ViewGroup with children.
         * 
         * EXPECTED: This test FAILS on unfixed code.
         */
        val functionContent = extractCreateOverflowMenuViewFunction()
        
        functionContent shouldNotBe null
        
        // Check for container.addView() calls which indicate multiple children
        val hasAddViewCalls = functionContent!!.contains("container.addView")
        
        if (hasAddViewCalls) {
            println("\nCounterexample: createOverflowMenuView() adds multiple views to container")
            println("  Found: container.addView() calls")
            println("  Expected: No addView() calls, return CometChatAvatar directly")
        }
        
        // This assertion should FAIL on unfixed code
        hasAddViewCalls shouldBe false
    }

    test("Property 1.4: createOverflowMenuView() should return avatar directly (not container)") {
        /**
         * **Validates: Requirements 1.1, 1.3**
         * 
         * Verifies that createOverflowMenuView() returns the avatar directly,
         * not a container that wraps the avatar.
         * 
         * EXPECTED: This test FAILS on unfixed code.
         */
        val functionContent = extractCreateOverflowMenuViewFunction()
        
        functionContent shouldNotBe null
        
        val returnsDirectly = returnsAvatarDirectly(functionContent!!)
        
        if (!returnsDirectly) {
            println("\nCounterexample: createOverflowMenuView() returns container instead of avatar")
            println("  Found: 'return container' instead of 'return avatar'")
            println("  Expected: 'return avatar' (CometChatAvatar returned directly)")
        }
        
        // This assertion should FAIL on unfixed code
        returnsDirectly shouldBe true
    }

    test("Property 1.5: navigateToSearch() function should NOT exist") {
        /**
         * **Validates: Requirement 1.2**
         * 
         * Verifies that the navigateToSearch() function does NOT exist in ChatsFragment.
         * This function is only needed for the search icon click handler, which should be removed.
         * 
         * EXPECTED: This test FAILS on unfixed code.
         */
        if (!chatsFragmentPath.exists()) {
            println("ChatsFragment.kt not found")
            true shouldBe false
            return@test
        }
        
        val content = chatsFragmentPath.readText()
        val hasNavigateToSearch = content.contains("private fun navigateToSearch()")
        
        if (hasNavigateToSearch) {
            println("\nCounterexample: navigateToSearch() function exists")
            println("  Found: 'private fun navigateToSearch()'")
            println("  Expected: Function should not exist (search icon removed)")
        }
        
        // This assertion should FAIL on unfixed code
        hasNavigateToSearch shouldBe false
    }

    // ========================================================================
    // PROPERTY-BASED TEST: All Bug Indicators
    // ========================================================================

    test("Property-based: For all bug indicators, none should exist in createOverflowMenuView()") {
        /**
         * **Validates: Requirements 1.1, 1.2, 1.3**
         * 
         * Property-based test that generates random bug indicators
         * and verifies none of them exist in createOverflowMenuView().
         * 
         * EXPECTED: This test FAILS on unfixed code.
         */
        val functionContent = extractCreateOverflowMenuViewFunction()
        
        functionContent shouldNotBe null
        
        val indicatorArb = Arb.element(bugIndicators)
        
        forAll(indicatorArb) { indicator ->
            val exists = bugIndicatorExists(indicator, functionContent!!)
            if (exists) {
                println("Bug indicator found: ${indicator.description}")
                println("  Pattern '${indicator.pattern}' exists in createOverflowMenuView()")
            }
            !exists // Should NOT exist for expected behavior
        }
    }

    // ========================================================================
    // COMPREHENSIVE BUG CONDITION CHECK
    // ========================================================================

    test("Property 1: Comprehensive bug condition check - overflow menu should contain only avatar") {
        /**
         * **Validates: Requirements 1.1, 1.2, 1.3**
         * 
         * Comprehensive test that verifies ALL bug condition requirements:
         * - No LinearLayout container
         * - No search ImageView
         * - No multiple children (addView calls)
         * - Returns avatar directly
         * - No navigateToSearch() function
         * 
         * EXPECTED: This test FAILS on unfixed code, confirming the bug exists.
         */
        val functionContent = extractCreateOverflowMenuViewFunction()
        
        functionContent shouldNotBe null
        
        val violations = mutableListOf<String>()
        
        // Check for LinearLayout container
        if (createsLinearLayoutContainer(functionContent!!)) {
            violations.add("[Req 1.1] createOverflowMenuView() creates LinearLayout container")
        }
        
        // Check for search ImageView
        if (createsSearchImageView(functionContent)) {
            violations.add("[Req 1.2] createOverflowMenuView() creates search ImageView")
        }
        
        // Check for addView calls
        if (functionContent.contains("container.addView")) {
            violations.add("[Req 1.3] createOverflowMenuView() adds multiple views to container")
        }
        
        // Check return statement
        if (!returnsAvatarDirectly(functionContent)) {
            violations.add("[Req 1.1] createOverflowMenuView() returns container instead of avatar")
        }
        
        // Check for navigateToSearch function
        val fullContent = chatsFragmentPath.readText()
        if (fullContent.contains("private fun navigateToSearch()")) {
            violations.add("[Req 1.2] navigateToSearch() function exists (search icon handler)")
        }
        
        // Print summary
        if (violations.isNotEmpty()) {
            println("\n=== BUG CONDITION CONFIRMED ===")
            println("Total violations: ${violations.size}")
            println("\nCounterexamples found:")
            violations.forEachIndexed { index, violation ->
                println("  ${index + 1}. $violation")
            }
            println("\n=== EXPECTED BEHAVIOR ===")
            println("createOverflowMenuView() should:")
            println("  1. NOT create LinearLayout container")
            println("  2. NOT create search ImageView")
            println("  3. NOT add multiple views to container")
            println("  4. Return CometChatAvatar directly")
            println("  5. NOT have navigateToSearch() function")
            println("\nReference: master-app-kotlin logoutView property")
            println("=== END BUG CONDITION CHECK ===\n")
        } else {
            println("\n=== BUG CONDITION NOT FOUND ===")
            println("createOverflowMenuView() correctly returns CometChatAvatar directly.")
            println("No search icon or container found.")
            println("=== END CHECK ===\n")
        }
        
        // This assertion should FAIL on unfixed code
        violations.isEmpty() shouldBe true
    }
})
