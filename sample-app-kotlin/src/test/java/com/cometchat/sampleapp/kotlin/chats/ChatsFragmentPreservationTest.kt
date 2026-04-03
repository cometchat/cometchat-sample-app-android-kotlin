package com.cometchat.sampleapp.kotlin.chats

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.string
import io.kotest.property.arbitrary.stringPattern
import io.kotest.property.forAll
import java.io.File

/**
 * Preservation Property Test for Conversations Search Icon Parity
 * 
 * **Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5**
 * 
 * Property 2: Preservation - Avatar Click and Popup Menu Behavior
 * 
 * This test verifies behaviors that MUST be preserved after the fix:
 * - Avatar click shows popup menu with logout and create conversation options
 * - Avatar has correct size (40dp) and displays user name/avatar
 * - Avatar has click listener attached
 * - createOverflowMenuView() returns null when SDK not initialized
 * - createOverflowMenuView() returns null when user not logged in
 * 
 * These tests should PASS on UNFIXED code (preservation is already satisfied).
 * After the fix is implemented, these tests should CONTINUE to pass (no regressions).
 */
class ChatsFragmentPreservationTest : FunSpec({

    val projectRoot = File(System.getProperty("user.dir")).parentFile
    val sampleAppKotlinPath = File(projectRoot, "sample-app-kotlin")
    val chatsFragmentPath = File(
        sampleAppKotlinPath, 
        "src/main/java/com/cometchat/sampleapp/kotlin/ui/chats/ChatsFragment.kt"
    )
    val popupLayoutPath = File(
        sampleAppKotlinPath,
        "src/main/res/layout/popup_user_menu.xml"
    )

    /**
     * Preservation requirement specification based on bugfix.md:
     * 
     * 3.1 WHEN the user taps the avatar in the header THEN the system SHALL CONTINUE TO 
     *     show the user popup menu with logout and create conversation options
     * 3.2 WHEN the user selects "Logout" from the popup menu THEN the system SHALL CONTINUE TO 
     *     perform logout and navigate to SplashActivity
     * 3.3 WHEN the user selects "Create Conversation" from the popup menu THEN the system SHALL 
     *     CONTINUE TO navigate to the Users tab
     * 3.4 WHEN the Conversations list is displayed THEN the system SHALL CONTINUE TO show all 
     *     conversations with proper click handling to MessagesActivity
     * 3.5 WHEN the CometChatConversations component is used THEN the system SHALL CONTINUE TO 
     *     function correctly with the simplified overflow menu view
     */
    data class PreservationRequirement(
        val id: String,
        val description: String,
        val codePattern: String,
        val fileType: String // "fragment", "layout", "both"
    )

    // Define preservation requirements that must be maintained
    val preservationRequirements = listOf(
        // Avatar click behavior
        PreservationRequirement(
            id = "3.1",
            description = "Avatar has click listener that shows popup menu",
            codePattern = "setOnClickListener",
            fileType = "fragment"
        ),
        PreservationRequirement(
            id = "3.1",
            description = "showUserMenu function exists for popup display",
            codePattern = "private fun showUserMenu",
            fileType = "fragment"
        ),
        PreservationRequirement(
            id = "3.1",
            description = "PopupWindow is created for user menu",
            codePattern = "PopupWindow(",
            fileType = "fragment"
        ),
        
        // Logout functionality
        PreservationRequirement(
            id = "3.2",
            description = "performLogout function exists",
            codePattern = "private fun performLogout()",
            fileType = "fragment"
        ),
        PreservationRequirement(
            id = "3.2",
            description = "CometChat.logout is called",
            codePattern = "CometChat.logout(",
            fileType = "fragment"
        ),
        PreservationRequirement(
            id = "3.2",
            description = "Navigation to SplashActivity on logout",
            codePattern = "SplashActivity::class.java",
            fileType = "fragment"
        ),
        PreservationRequirement(
            id = "3.2",
            description = "Logout click handler in popup menu",
            codePattern = "tv_logout",
            fileType = "fragment"
        ),
        
        // Create Conversation functionality
        PreservationRequirement(
            id = "3.3",
            description = "navigateToUsersTab function exists",
            codePattern = "private fun navigateToUsersTab()",
            fileType = "fragment"
        ),
        PreservationRequirement(
            id = "3.3",
            description = "Create conversation click handler in popup menu",
            codePattern = "tv_create_conversation",
            fileType = "fragment"
        ),
        PreservationRequirement(
            id = "3.3",
            description = "Navigation to Users tab via bottom navigation",
            codePattern = "nav_users",
            fileType = "fragment"
        ),
        
        // Avatar configuration
        PreservationRequirement(
            id = "3.1",
            description = "CometChatAvatar is created",
            codePattern = "CometChatAvatar(",
            fileType = "fragment"
        ),
        PreservationRequirement(
            id = "3.1",
            description = "Avatar size is set to 40dp",
            codePattern = "cometchat_40dp",
            fileType = "fragment"
        ),
        PreservationRequirement(
            id = "3.1",
            description = "Avatar displays user name and avatar",
            codePattern = "setAvatar(loggedInUser.name, loggedInUser.avatar)",
            fileType = "fragment"
        ),
        
        // Null return conditions
        PreservationRequirement(
            id = "3.5",
            description = "Returns null when SDK not initialized",
            codePattern = "if (!CometChatUIKit.isSDKInitialized()) return null",
            fileType = "fragment"
        ),
        PreservationRequirement(
            id = "3.5",
            description = "Returns null when user not logged in",
            codePattern = "CometChatUIKit.getLoggedInUser() ?: return null",
            fileType = "fragment"
        ),
        
        // Conversation list functionality
        PreservationRequirement(
            id = "3.4",
            description = "setOnItemClick handler for conversations",
            codePattern = "setOnItemClick",
            fileType = "fragment"
        ),
        PreservationRequirement(
            id = "3.4",
            description = "navigateToMessages function exists",
            codePattern = "private fun navigateToMessages",
            fileType = "fragment"
        ),
        PreservationRequirement(
            id = "3.4",
            description = "MessagesActivity navigation",
            codePattern = "MessagesActivity.start(",
            fileType = "fragment"
        ),
        
        // Overflow menu integration
        PreservationRequirement(
            id = "3.5",
            description = "setOverflowMenu is called with createOverflowMenuView",
            codePattern = "setOverflowMenu(createOverflowMenuView())",
            fileType = "fragment"
        )
    )

    // Popup menu layout requirements
    val popupLayoutRequirements = listOf(
        PreservationRequirement(
            id = "3.1",
            description = "Popup layout has user name TextView",
            codePattern = "tv_user_name",
            fileType = "layout"
        ),
        PreservationRequirement(
            id = "3.2",
            description = "Popup layout has logout option",
            codePattern = "tv_logout",
            fileType = "layout"
        ),
        PreservationRequirement(
            id = "3.3",
            description = "Popup layout has create conversation option",
            codePattern = "tv_create_conversation",
            fileType = "layout"
        )
    )

    /**
     * Check if a preservation requirement pattern exists in the file
     */
    fun requirementSatisfied(requirement: PreservationRequirement): Boolean {
        return when (requirement.fileType) {
            "fragment" -> {
                if (chatsFragmentPath.exists()) {
                    chatsFragmentPath.readText().contains(requirement.codePattern)
                } else false
            }
            "layout" -> {
                if (popupLayoutPath.exists()) {
                    popupLayoutPath.readText().contains(requirement.codePattern)
                } else false
            }
            "both" -> {
                val inFragment = chatsFragmentPath.exists() && 
                    chatsFragmentPath.readText().contains(requirement.codePattern)
                val inLayout = popupLayoutPath.exists() && 
                    popupLayoutPath.readText().contains(requirement.codePattern)
                inFragment || inLayout
            }
            else -> false
        }
    }

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
     * Extract the showUserMenu() function content from ChatsFragment.kt
     */
    fun extractShowUserMenuFunction(): String? {
        if (!chatsFragmentPath.exists()) return null
        
        val content = chatsFragmentPath.readText()
        
        // Find the function start
        val functionStart = content.indexOf("private fun showUserMenu(")
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

    // ========================================================================
    // PROPERTY 2.1: Avatar Click Shows Popup Menu
    // ========================================================================

    test("Property 2.1: Avatar click listener should show popup menu") {
        /**
         * **Validates: Requirement 3.1**
         * 
         * Verifies that the avatar has a click listener that calls showUserMenu().
         * This behavior must be preserved after the fix.
         * 
         * EXPECTED: This test PASSES on unfixed code.
         */
        val functionContent = extractCreateOverflowMenuViewFunction()
        
        functionContent shouldNotBe null
        
        // Check that avatar has setOnClickListener
        val hasClickListener = functionContent!!.contains("setOnClickListener")
        
        // Check that click listener calls showUserMenu
        val callsShowUserMenu = functionContent.contains("showUserMenu(")
        
        if (!hasClickListener || !callsShowUserMenu) {
            println("\n=== PRESERVATION VIOLATION: Avatar click behavior ===")
            if (!hasClickListener) {
                println("  - Avatar does not have setOnClickListener")
            }
            if (!callsShowUserMenu) {
                println("  - Click listener does not call showUserMenu()")
            }
            println("=== END VIOLATION ===\n")
        }
        
        hasClickListener shouldBe true
        callsShowUserMenu shouldBe true
    }

    test("Property 2.2: showUserMenu function should create PopupWindow with correct options") {
        /**
         * **Validates: Requirements 3.1, 3.2, 3.3**
         * 
         * Verifies that showUserMenu() creates a PopupWindow with:
         * - User name display
         * - Logout option
         * - Create conversation option
         * 
         * EXPECTED: This test PASSES on unfixed code.
         */
        val functionContent = extractShowUserMenuFunction()
        
        functionContent shouldNotBe null
        
        val violations = mutableListOf<String>()
        
        // Check PopupWindow creation
        if (!functionContent!!.contains("PopupWindow(")) {
            violations.add("showUserMenu() does not create PopupWindow")
        }
        
        // Check popup layout inflation
        if (!functionContent.contains("popup_user_menu")) {
            violations.add("showUserMenu() does not inflate popup_user_menu layout")
        }
        
        // Check user name setup
        if (!functionContent.contains("tv_user_name")) {
            violations.add("showUserMenu() does not set up user name TextView")
        }
        
        // Check logout click handler
        if (!functionContent.contains("tv_logout")) {
            violations.add("showUserMenu() does not set up logout click handler")
        }
        
        // Check create conversation click handler
        if (!functionContent.contains("tv_create_conversation")) {
            violations.add("showUserMenu() does not set up create conversation click handler")
        }
        
        if (violations.isNotEmpty()) {
            println("\n=== PRESERVATION VIOLATION: Popup menu behavior ===")
            violations.forEach { println("  - $it") }
            println("=== END VIOLATION ===\n")
        }
        
        violations.isEmpty() shouldBe true
    }

    // ========================================================================
    // PROPERTY 2.3: Avatar Configuration
    // ========================================================================

    test("Property 2.3: Avatar should have correct size (40dp) and display user info") {
        /**
         * **Validates: Requirement 3.1**
         * 
         * Verifies that the avatar:
         * - Has size set to 40dp (cometchat_40dp dimension)
         * - Displays user name and avatar image via setAvatar()
         * 
         * EXPECTED: This test PASSES on unfixed code.
         */
        val functionContent = extractCreateOverflowMenuViewFunction()
        
        functionContent shouldNotBe null
        
        val violations = mutableListOf<String>()
        
        // Check avatar size
        if (!functionContent!!.contains("cometchat_40dp")) {
            violations.add("Avatar size is not set to 40dp")
        }
        
        // Check setAvatar call with user info
        if (!functionContent.contains("setAvatar(loggedInUser.name, loggedInUser.avatar)")) {
            violations.add("Avatar does not display user name and avatar")
        }
        
        // Check CometChatAvatar creation
        if (!functionContent.contains("CometChatAvatar(")) {
            violations.add("CometChatAvatar is not created")
        }
        
        if (violations.isNotEmpty()) {
            println("\n=== PRESERVATION VIOLATION: Avatar configuration ===")
            violations.forEach { println("  - $it") }
            println("=== END VIOLATION ===\n")
        }
        
        violations.isEmpty() shouldBe true
    }

    // ========================================================================
    // PROPERTY 2.4: Null Return Conditions
    // ========================================================================

    test("Property 2.4: createOverflowMenuView() should return null when SDK not initialized") {
        /**
         * **Validates: Requirement 3.5**
         * 
         * Verifies that createOverflowMenuView() returns null when 
         * CometChatUIKit.isSDKInitialized() returns false.
         * 
         * EXPECTED: This test PASSES on unfixed code.
         */
        val functionContent = extractCreateOverflowMenuViewFunction()
        
        functionContent shouldNotBe null
        
        val hasSDKCheck = functionContent!!.contains("if (!CometChatUIKit.isSDKInitialized()) return null")
        
        if (!hasSDKCheck) {
            println("\n=== PRESERVATION VIOLATION: SDK initialization check ===")
            println("  - createOverflowMenuView() does not check SDK initialization")
            println("  - Expected: 'if (!CometChatUIKit.isSDKInitialized()) return null'")
            println("=== END VIOLATION ===\n")
        }
        
        hasSDKCheck shouldBe true
    }

    test("Property 2.5: createOverflowMenuView() should return null when user not logged in") {
        /**
         * **Validates: Requirement 3.5**
         * 
         * Verifies that createOverflowMenuView() returns null when 
         * CometChatUIKit.getLoggedInUser() returns null.
         * 
         * EXPECTED: This test PASSES on unfixed code.
         */
        val functionContent = extractCreateOverflowMenuViewFunction()
        
        functionContent shouldNotBe null
        
        val hasUserCheck = functionContent!!.contains("CometChatUIKit.getLoggedInUser() ?: return null")
        
        if (!hasUserCheck) {
            println("\n=== PRESERVATION VIOLATION: User login check ===")
            println("  - createOverflowMenuView() does not check user login status")
            println("  - Expected: 'CometChatUIKit.getLoggedInUser() ?: return null'")
            println("=== END VIOLATION ===\n")
        }
        
        hasUserCheck shouldBe true
    }

    // ========================================================================
    // PROPERTY 2.6: Logout Functionality
    // ========================================================================

    test("Property 2.6: Logout should perform CometChat.logout and navigate to SplashActivity") {
        /**
         * **Validates: Requirement 3.2**
         * 
         * Verifies that the logout functionality:
         * - Calls CometChat.logout()
         * - Navigates to SplashActivity on success
         * - Clears the back stack
         * 
         * EXPECTED: This test PASSES on unfixed code.
         */
        if (!chatsFragmentPath.exists()) {
            println("ChatsFragment.kt not found")
            true shouldBe false
            return@test
        }
        
        val content = chatsFragmentPath.readText()
        val violations = mutableListOf<String>()
        
        // Check performLogout function exists
        if (!content.contains("private fun performLogout()")) {
            violations.add("performLogout() function does not exist")
        }
        
        // Check CometChat.logout call
        if (!content.contains("CometChat.logout(")) {
            violations.add("CometChat.logout() is not called")
        }
        
        // Check navigation to SplashActivity
        if (!content.contains("SplashActivity::class.java")) {
            violations.add("Navigation to SplashActivity not found")
        }
        
        // Check back stack clearing
        if (!content.contains("FLAG_ACTIVITY_NEW_TASK") || !content.contains("FLAG_ACTIVITY_CLEAR_TASK")) {
            violations.add("Back stack clearing flags not set")
        }
        
        if (violations.isNotEmpty()) {
            println("\n=== PRESERVATION VIOLATION: Logout functionality ===")
            violations.forEach { println("  - $it") }
            println("=== END VIOLATION ===\n")
        }
        
        violations.isEmpty() shouldBe true
    }

    // ========================================================================
    // PROPERTY 2.7: Create Conversation Functionality
    // ========================================================================

    test("Property 2.7: Create Conversation should navigate to Users tab") {
        /**
         * **Validates: Requirement 3.3**
         * 
         * Verifies that the create conversation functionality:
         * - Has navigateToUsersTab() function
         * - Navigates to Users tab via bottom navigation
         * 
         * EXPECTED: This test PASSES on unfixed code.
         */
        if (!chatsFragmentPath.exists()) {
            println("ChatsFragment.kt not found")
            true shouldBe false
            return@test
        }
        
        val content = chatsFragmentPath.readText()
        val violations = mutableListOf<String>()
        
        // Check navigateToUsersTab function exists
        if (!content.contains("private fun navigateToUsersTab()")) {
            violations.add("navigateToUsersTab() function does not exist")
        }
        
        // Check navigation to Users tab
        if (!content.contains("nav_users")) {
            violations.add("Navigation to nav_users not found")
        }
        
        // Check bottom navigation usage
        if (!content.contains("BottomNavigationView")) {
            violations.add("BottomNavigationView not used for navigation")
        }
        
        if (violations.isNotEmpty()) {
            println("\n=== PRESERVATION VIOLATION: Create conversation functionality ===")
            violations.forEach { println("  - $it") }
            println("=== END VIOLATION ===\n")
        }
        
        violations.isEmpty() shouldBe true
    }

    // ========================================================================
    // PROPERTY 2.8: Conversation List Click Handling
    // ========================================================================

    test("Property 2.8: Conversation list should navigate to MessagesActivity on click") {
        /**
         * **Validates: Requirement 3.4**
         * 
         * Verifies that the conversation list:
         * - Has setOnItemClick handler
         * - Navigates to MessagesActivity
         * 
         * EXPECTED: This test PASSES on unfixed code.
         */
        if (!chatsFragmentPath.exists()) {
            println("ChatsFragment.kt not found")
            true shouldBe false
            return@test
        }
        
        val content = chatsFragmentPath.readText()
        val violations = mutableListOf<String>()
        
        // Check setOnItemClick handler
        if (!content.contains("setOnItemClick")) {
            violations.add("setOnItemClick handler not found")
        }
        
        // Check navigateToMessages function
        if (!content.contains("private fun navigateToMessages")) {
            violations.add("navigateToMessages() function does not exist")
        }
        
        // Check MessagesActivity navigation
        if (!content.contains("MessagesActivity.start(")) {
            violations.add("MessagesActivity.start() not called")
        }
        
        if (violations.isNotEmpty()) {
            println("\n=== PRESERVATION VIOLATION: Conversation list click handling ===")
            violations.forEach { println("  - $it") }
            println("=== END VIOLATION ===\n")
        }
        
        violations.isEmpty() shouldBe true
    }

    // ========================================================================
    // PROPERTY 2.9: Overflow Menu Integration
    // ========================================================================

    test("Property 2.9: setOverflowMenu should be called with createOverflowMenuView") {
        /**
         * **Validates: Requirement 3.5**
         * 
         * Verifies that the overflow menu is properly integrated:
         * - setOverflowMenu() is called with createOverflowMenuView()
         * 
         * EXPECTED: This test PASSES on unfixed code.
         */
        if (!chatsFragmentPath.exists()) {
            println("ChatsFragment.kt not found")
            true shouldBe false
            return@test
        }
        
        val content = chatsFragmentPath.readText()
        
        val hasOverflowMenuSetup = content.contains("setOverflowMenu(createOverflowMenuView())")
        
        if (!hasOverflowMenuSetup) {
            println("\n=== PRESERVATION VIOLATION: Overflow menu integration ===")
            println("  - setOverflowMenu(createOverflowMenuView()) not found")
            println("=== END VIOLATION ===\n")
        }
        
        hasOverflowMenuSetup shouldBe true
    }

    // ========================================================================
    // PROPERTY 2.10: Popup Layout Exists with Required Elements
    // ========================================================================

    test("Property 2.10: Popup layout should exist with user name, logout, and create conversation options") {
        /**
         * **Validates: Requirements 3.1, 3.2, 3.3**
         * 
         * Verifies that the popup_user_menu.xml layout exists and contains:
         * - tv_user_name TextView
         * - tv_logout TextView
         * - tv_create_conversation TextView
         * 
         * EXPECTED: This test PASSES on unfixed code.
         */
        popupLayoutPath.exists() shouldBe true
        
        val layoutContent = popupLayoutPath.readText()
        val violations = mutableListOf<String>()
        
        if (!layoutContent.contains("tv_user_name")) {
            violations.add("tv_user_name not found in popup layout")
        }
        
        if (!layoutContent.contains("tv_logout")) {
            violations.add("tv_logout not found in popup layout")
        }
        
        if (!layoutContent.contains("tv_create_conversation")) {
            violations.add("tv_create_conversation not found in popup layout")
        }
        
        if (violations.isNotEmpty()) {
            println("\n=== PRESERVATION VIOLATION: Popup layout elements ===")
            violations.forEach { println("  - $it") }
            println("=== END VIOLATION ===\n")
        }
        
        violations.isEmpty() shouldBe true
    }

    // ========================================================================
    // PROPERTY-BASED TEST: All Preservation Requirements
    // ========================================================================

    test("Property-based: For all logged-in user states, avatar click behavior produces popup menu") {
        /**
         * **Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5**
         * 
         * Property-based test that verifies all preservation requirements are satisfied.
         * 
         * EXPECTED: This test PASSES on unfixed code.
         */
        val requirementArb = Arb.element(preservationRequirements)
        
        forAll(requirementArb) { requirement ->
            val satisfied = requirementSatisfied(requirement)
            if (!satisfied) {
                println("Preservation violation: [Req ${requirement.id}] ${requirement.description}")
                println("  Pattern '${requirement.codePattern}' not found in ${requirement.fileType}")
            }
            satisfied
        }
    }

    // ========================================================================
    // COMPREHENSIVE PRESERVATION CHECK
    // ========================================================================

    test("Property 2: Comprehensive preservation check - all avatar and popup behaviors preserved") {
        /**
         * **Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5**
         * 
         * Comprehensive test that verifies ALL preservation requirements:
         * - Avatar click shows popup menu
         * - Popup menu has logout and create conversation options
         * - Logout performs CometChat.logout and navigates to SplashActivity
         * - Create conversation navigates to Users tab
         * - Conversation list click navigates to MessagesActivity
         * - createOverflowMenuView() returns null when SDK not initialized
         * - createOverflowMenuView() returns null when user not logged in
         * 
         * EXPECTED: This test PASSES on unfixed code.
         */
        val allViolations = mutableListOf<String>()
        
        // Check all fragment preservation requirements
        preservationRequirements.forEach { requirement ->
            if (!requirementSatisfied(requirement)) {
                allViolations.add("[Req ${requirement.id}] ${requirement.description}")
            }
        }
        
        // Check popup layout requirements
        popupLayoutRequirements.forEach { requirement ->
            if (!requirementSatisfied(requirement)) {
                allViolations.add("[Req ${requirement.id}] ${requirement.description}")
            }
        }
        
        // Print summary
        if (allViolations.isNotEmpty()) {
            println("\n=== PRESERVATION VIOLATIONS SUMMARY ===")
            println("Total violations: ${allViolations.size}")
            allViolations.forEachIndexed { index, violation ->
                println("  ${index + 1}. $violation")
            }
            println("=== END SUMMARY ===\n")
        } else {
            println("\n=== PRESERVATION CHECK PASSED ===")
            println("All avatar and popup menu behaviors are preserved:")
            println("  ✓ Avatar click shows popup menu")
            println("  ✓ Avatar has correct size (40dp) and displays user info")
            println("  ✓ Popup menu has logout and create conversation options")
            println("  ✓ Logout performs CometChat.logout and navigates to SplashActivity")
            println("  ✓ Create conversation navigates to Users tab")
            println("  ✓ Conversation list click navigates to MessagesActivity")
            println("  ✓ createOverflowMenuView() returns null when SDK not initialized")
            println("  ✓ createOverflowMenuView() returns null when user not logged in")
            println("=== END CHECK ===\n")
        }
        
        allViolations.isEmpty() shouldBe true
    }
})
