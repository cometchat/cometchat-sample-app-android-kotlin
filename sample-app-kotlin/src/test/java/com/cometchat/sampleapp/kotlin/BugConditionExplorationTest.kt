package com.cometchat.sampleapp.kotlin

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.forAll
import java.io.File

/**
 * Bug Condition Exploration Test
 * 
 * **Validates: Requirements 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9, 1.10, 1.11, 1.12, 1.13, 1.14, 1.15**
 * 
 * Property 1: Bug Condition - Missing Components Detection
 * 
 * This test MUST FAIL on unfixed code - failure confirms the bug exists.
 * DO NOT attempt to fix the test or the code when it fails.
 * 
 * GOAL: Surface counterexamples that demonstrate sample-app-kotlin is missing 
 * components from master-app-kotlin2 (excluding VoIP, FCM, Google Login, showcase).
 */
class BugConditionExplorationTest : FunSpec({

    val projectRoot = File(System.getProperty("user.dir")).parentFile
    val sampleAppKotlinPath = File(projectRoot, "sample-app-kotlin")
    val sampleAppSrcPath = File(sampleAppKotlinPath, "src/main/java/com/cometchat/sampleapp/kotlin")
    val buildGradlePath = File(sampleAppKotlinPath, "build.gradle.kts")

    /**
     * Component specification for expected files/folders in sample-app-kotlin
     * Based on Clean Architecture + MVVM pattern from master-app-kotlin2
     */
    data class ExpectedComponent(
        val path: String,
        val description: String,
        val requirement: String
    )

    // Define all expected components that should exist in sample-app-kotlin
    val expectedComponents = listOf(
        // Build dependency
        ExpectedComponent(
            path = "BUILD_GRADLE_CALLS_SDK",
            description = "build.gradle.kts should contain calls.sdk.android dependency",
            requirement = "1.14"
        ),
        
        // Data layer - Repository
        ExpectedComponent(
            path = "data/repository/Repository.kt",
            description = "data/repository/ folder with Repository.kt",
            requirement = "1.5"
        ),
        
        // UI Calls folder components
        ExpectedComponent(
            path = "ui/calls/CallDetailsActivity.kt",
            description = "ui/calls/ folder with CallDetailsActivity",
            requirement = "1.7"
        ),
        ExpectedComponent(
            path = "ui/calls/CallsFragment.kt",
            description = "ui/calls/ folder with CallsFragment",
            requirement = "1.7"
        ),
        ExpectedComponent(
            path = "ui/calls/CallDetailsHistoryFragment.kt",
            description = "ui/calls/ folder with CallDetailsHistoryFragment",
            requirement = "1.10"
        ),
        ExpectedComponent(
            path = "ui/calls/CallDetailsParticipantsFragment.kt",
            description = "ui/calls/ folder with CallDetailsParticipantsFragment",
            requirement = "1.10"
        ),
        ExpectedComponent(
            path = "ui/calls/CallDetailsRecordingsFragment.kt",
            description = "ui/calls/ folder with CallDetailsRecordingsFragment",
            requirement = "1.10"
        ),
        ExpectedComponent(
            path = "ui/calls/CallDetailsHistoryViewModel.kt",
            description = "ui/calls/ folder with CallDetailsHistoryViewModel",
            requirement = "1.9"
        ),
        ExpectedComponent(
            path = "ui/calls/CallsFragmentViewModel.kt",
            description = "ui/calls/ folder with CallsFragmentViewModel",
            requirement = "1.9"
        ),
        ExpectedComponent(
            path = "ui/calls/CallDetailsHistoryAdapter.kt",
            description = "ui/calls/ folder with CallDetailsHistoryAdapter",
            requirement = "1.8"
        ),
        ExpectedComponent(
            path = "ui/calls/CallDetailsParticipantsAdapter.kt",
            description = "ui/calls/ folder with CallDetailsParticipantsAdapter",
            requirement = "1.8"
        ),
        ExpectedComponent(
            path = "ui/calls/CallDetailsTabAdapter.kt",
            description = "ui/calls/ folder with CallDetailsTabAdapter",
            requirement = "1.8"
        ),
        
        // UI Messages folder components
        ExpectedComponent(
            path = "ui/messages/MessagesActivity.kt",
            description = "ui/messages/ folder with MessagesActivity",
            requirement = "1.7"
        ),
        ExpectedComponent(
            path = "ui/messages/MessagesViewModel.kt",
            description = "ui/messages/ folder with MessagesViewModel",
            requirement = "1.9"
        ),
        ExpectedComponent(
            path = "ui/messages/ThreadMessagesActivity.kt",
            description = "ui/messages/ folder with ThreadMessagesActivity",
            requirement = "1.7"
        ),
        
        // UI Users folder components
        ExpectedComponent(
            path = "ui/users/UsersFragment.kt",
            description = "ui/users/ folder with UsersFragment",
            requirement = "1.7"
        ),
        ExpectedComponent(
            path = "ui/users/UserDetailsActivity.kt",
            description = "ui/users/ folder with UserDetailsActivity",
            requirement = "1.7"
        ),
        ExpectedComponent(
            path = "ui/users/UserDetailsViewModel.kt",
            description = "ui/users/ folder with UserDetailsViewModel",
            requirement = "1.9"
        ),
        
        // UI Groups folder components
        ExpectedComponent(
            path = "ui/groups/GroupsFragment.kt",
            description = "ui/groups/ folder with GroupsFragment",
            requirement = "1.7"
        ),
        ExpectedComponent(
            path = "ui/groups/GroupDetailsActivity.kt",
            description = "ui/groups/ folder with GroupDetailsActivity",
            requirement = "1.7"
        ),
        ExpectedComponent(
            path = "ui/groups/GroupDetailsViewModel.kt",
            description = "ui/groups/ folder with GroupDetailsViewModel",
            requirement = "1.9"
        ),
        ExpectedComponent(
            path = "ui/groups/GroupsViewModel.kt",
            description = "ui/groups/ folder with GroupsViewModel",
            requirement = "1.9"
        ),
        ExpectedComponent(
            path = "ui/groups/GroupAction.kt",
            description = "ui/groups/ folder with GroupAction enum",
            requirement = "1.11"
        ),
        
        // UI Home folder components
        ExpectedComponent(
            path = "ui/home/HomeActivity.kt",
            description = "ui/home/ folder with HomeActivity (with bottom navigation)",
            requirement = "1.2"
        ),
        
        // Shared folder components
        ExpectedComponent(
            path = "shared/AppCredentials.kt",
            description = "shared/ folder with AppCredentials.kt",
            requirement = "1.4"
        ),
        ExpectedComponent(
            path = "shared/AppPreferences.kt",
            description = "shared/ folder with AppPreferences.kt",
            requirement = "1.4"
        )
    )

    /**
     * Check if a component exists in sample-app-kotlin
     */
    fun componentExists(component: ExpectedComponent): Boolean {
        return when (component.path) {
            "BUILD_GRADLE_CALLS_SDK" -> {
                // Check if build.gradle.kts contains calls.sdk.android dependency
                if (buildGradlePath.exists()) {
                    val content = buildGradlePath.readText()
                    content.contains("calls.sdk.android") || content.contains("calls-sdk-android")
                } else {
                    false
                }
            }
            else -> {
                // Check if file exists in src/main/java/com/cometchat/sampleapp/kotlin/
                val file = File(sampleAppSrcPath, component.path)
                file.exists()
            }
        }
    }

    test("Property 1: Bug Condition - All expected components should exist in sample-app-kotlin") {
        /**
         * **Validates: Requirements 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9, 1.10, 1.11, 1.12, 1.13, 1.14, 1.15**
         * 
         * This property test verifies that sample-app-kotlin contains all expected components
         * from master-app-kotlin2 (excluding VoIP, FCM, Google Login, showcase).
         * 
         * EXPECTED: This test FAILS on unfixed code, confirming the bug exists.
         */
        
        val missingComponents = mutableListOf<ExpectedComponent>()
        
        expectedComponents.forEach { component ->
            if (!componentExists(component)) {
                missingComponents.add(component)
            }
        }
        
        // Print detailed counterexamples for documentation
        if (missingComponents.isNotEmpty()) {
            println("\n=== BUG CONDITION CONFIRMED: Missing Components Found ===")
            println("Total missing: ${missingComponents.size} out of ${expectedComponents.size}")
            println("\nCounterexamples (missing components):")
            missingComponents.forEachIndexed { index, component ->
                println("  ${index + 1}. [Req ${component.requirement}] ${component.description}")
                println("     Expected path: ${component.path}")
            }
            println("\n=== END COUNTEREXAMPLES ===\n")
        }
        
        // This assertion should FAIL on unfixed code
        missingComponents.isEmpty() shouldBe true
    }

    test("Property 1.1: build.gradle.kts should contain calls.sdk.android dependency") {
        /**
         * **Validates: Requirement 1.14**
         * 
         * Verifies that sample-app-kotlin/build.gradle.kts includes the CometChat Calls SDK
         * dependency for Calls UI features (call buttons, call logs).
         */
        val component = expectedComponents.find { it.path == "BUILD_GRADLE_CALLS_SDK" }!!
        val exists = componentExists(component)
        
        if (!exists) {
            println("\nCounterexample: ${component.description}")
            println("  build.gradle.kts is missing 'calls.sdk.android' dependency")
        }
        
        exists shouldBe true
    }

    test("Property 1.2: data/repository/ folder should exist with Repository.kt") {
        /**
         * **Validates: Requirement 1.5**
         * 
         * Verifies that sample-app-kotlin has data/repository/ folder with Repository.kt
         */
        val component = expectedComponents.find { it.path == "data/repository/Repository.kt" }!!
        val exists = componentExists(component)
        
        if (!exists) {
            println("\nCounterexample: ${component.description}")
            println("  Expected: ${sampleAppSrcPath}/data/repository/Repository.kt")
        }
        
        exists shouldBe true
    }

    test("Property 1.3: ui/calls/ folder should have all required components") {
        /**
         * **Validates: Requirements 1.7, 1.8, 1.9, 1.10**
         * 
         * Verifies that sample-app-kotlin has ui/calls/ folder with:
         * - CallDetailsActivity, CallsFragment
         * - CallDetailsHistoryFragment, CallDetailsParticipantsFragment, CallDetailsRecordingsFragment
         * - CallDetailsHistoryViewModel, CallsFragmentViewModel
         * - CallDetailsHistoryAdapter, CallDetailsParticipantsAdapter, CallDetailsTabAdapter
         */
        val callsComponents = expectedComponents.filter { it.path.startsWith("ui/calls/") }
        val missingCalls = callsComponents.filter { !componentExists(it) }
        
        if (missingCalls.isNotEmpty()) {
            println("\nCounterexamples in ui/calls/:")
            missingCalls.forEach { component ->
                println("  - Missing: ${component.path}")
            }
        }
        
        missingCalls.isEmpty() shouldBe true
    }

    test("Property 1.4: ui/messages/ folder should have all required components") {
        /**
         * **Validates: Requirements 1.7, 1.9**
         * 
         * Verifies that sample-app-kotlin has ui/messages/ folder with:
         * - MessagesActivity, MessagesViewModel, ThreadMessageActivity
         */
        val messagesComponents = expectedComponents.filter { it.path.startsWith("ui/messages/") }
        val missingMessages = messagesComponents.filter { !componentExists(it) }
        
        if (missingMessages.isNotEmpty()) {
            println("\nCounterexamples in ui/messages/:")
            missingMessages.forEach { component ->
                println("  - Missing: ${component.path}")
            }
        }
        
        missingMessages.isEmpty() shouldBe true
    }

    test("Property 1.5: ui/users/ folder should have all required components") {
        /**
         * **Validates: Requirements 1.7, 1.9**
         * 
         * Verifies that sample-app-kotlin has ui/users/ folder with:
         * - UsersFragment, UserDetailsActivity, UserDetailsViewModel
         */
        val usersComponents = expectedComponents.filter { it.path.startsWith("ui/users/") }
        val missingUsers = usersComponents.filter { !componentExists(it) }
        
        if (missingUsers.isNotEmpty()) {
            println("\nCounterexamples in ui/users/:")
            missingUsers.forEach { component ->
                println("  - Missing: ${component.path}")
            }
        }
        
        missingUsers.isEmpty() shouldBe true
    }

    test("Property 1.6: ui/groups/ folder should have all required components") {
        /**
         * **Validates: Requirements 1.7, 1.9, 1.11**
         * 
         * Verifies that sample-app-kotlin has ui/groups/ folder with:
         * - GroupsFragment, GroupDetailsActivity, GroupDetailsViewModel, GroupsViewModel, GroupAction enum
         */
        val groupsComponents = expectedComponents.filter { it.path.startsWith("ui/groups/") }
        val missingGroups = groupsComponents.filter { !componentExists(it) }
        
        if (missingGroups.isNotEmpty()) {
            println("\nCounterexamples in ui/groups/:")
            missingGroups.forEach { component ->
                println("  - Missing: ${component.path}")
            }
        }
        
        missingGroups.isEmpty() shouldBe true
    }

    test("Property 1.7: ui/home/ folder should have all required components") {
        /**
         * **Validates: Requirement 1.2**
         * 
         * Verifies that sample-app-kotlin has ui/home/ folder with:
         * - HomeActivity (with bottom navigation - no separate DefaultComponentAppFlowActivity needed)
         */
        val homeComponents = expectedComponents.filter { it.path.startsWith("ui/home/") }
        val missingHome = homeComponents.filter { !componentExists(it) }
        
        if (missingHome.isNotEmpty()) {
            println("\nCounterexamples in ui/home/:")
            missingHome.forEach { component ->
                println("  - Missing: ${component.path}")
            }
        }
        
        missingHome.isEmpty() shouldBe true
    }

    test("Property 1.8: shared/ folder should have all required components") {
        /**
         * **Validates: Requirement 1.4**
         * 
         * Verifies that sample-app-kotlin has shared/ folder with:
         * - AppCredentials.kt, AppPreferences.kt
         */
        val sharedComponents = expectedComponents.filter { it.path.startsWith("shared/") }
        val missingShared = sharedComponents.filter { !componentExists(it) }
        
        if (missingShared.isNotEmpty()) {
            println("\nCounterexamples in shared/:")
            missingShared.forEach { component ->
                println("  - Missing: ${component.path}")
            }
        }
        
        missingShared.isEmpty() shouldBe true
    }

    test("Property-based: For all expected components, they should exist in sample-app-kotlin") {
        /**
         * **Validates: Requirements 1.1-1.15**
         * 
         * Property-based test that generates random components from the expected list
         * and verifies each one exists.
         */
        val componentArb = Arb.element(expectedComponents)
        
        forAll(componentArb) { component ->
            val exists = componentExists(component)
            if (!exists) {
                println("Counterexample found: ${component.description} (${component.path})")
            }
            exists
        }
    }
})
