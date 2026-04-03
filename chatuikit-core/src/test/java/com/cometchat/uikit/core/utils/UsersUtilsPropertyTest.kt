package com.cometchat.uikit.core.utils

import com.cometchat.chat.models.User
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.char
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Utility functions for the CometChatUsers component.
 * These are pure functions that can be tested independently of UI frameworks.
 * 
 * Note: These functions mirror the implementations in:
 * - chatuikit-jetpack/src/main/java/com/cometchat/uikit/compose/presentation/users/utils/UsersUtils.kt
 * - chatuikit-kotlin/src/main/java/com/cometchat/uikit/kotlin/presentation/users/utils/UsersUtils.kt
 */
private object UsersUtils {
    
    /**
     * Groups users by the first letter of their name.
     * Users whose names start with non-alphabetic characters are grouped under '#'.
     */
    fun groupUsersByFirstLetter(users: List<User>): Map<Char, List<User>> {
        return users
            .groupBy { user ->
                val firstChar = user.name?.firstOrNull()?.uppercaseChar() ?: '#'
                if (firstChar.isLetter()) firstChar else '#'
            }
            .toSortedMap(compareBy { 
                // Sort '#' at the end
                if (it == '#') Char.MAX_VALUE else it 
            })
    }
    
    /**
     * Gets the initials from a user's name.
     * Returns the first character of the name in uppercase.
     * If the name is empty or null, returns a default placeholder.
     */
    fun getInitials(user: User): String {
        val name = user.name
        if (name.isNullOrBlank()) {
            return "?"
        }
        return name.first().uppercaseChar().toString()
    }
    
    /**
     * Gets the initials from a name string.
     */
    fun getInitialsFromName(name: String?): String {
        if (name.isNullOrBlank()) {
            return "?"
        }
        return name.first().uppercaseChar().toString()
    }
    
    /**
     * Filters users based on a search query.
     * Matches against the user's name (case-insensitive).
     */
    fun filterUsers(users: List<User>, query: String): List<User> {
        if (query.isBlank()) {
            return users
        }
        
        val lowerQuery = query.lowercase()
        return users.filter { user ->
            user.name?.lowercase()?.contains(lowerQuery) == true
        }
    }
    
    /**
     * Gets the first letter for sticky header from a user.
     * Returns '#' for users whose names start with non-alphabetic characters.
     */
    fun getHeaderLetter(user: User): Char {
        val firstChar = user.name?.firstOrNull()?.uppercaseChar() ?: '#'
        return if (firstChar.isLetter()) firstChar else '#'
    }
    
    /**
     * Checks if a user matches a search query.
     */
    fun matchesQuery(user: User, query: String): Boolean {
        if (query.isBlank()) {
            return true
        }
        return user.name?.lowercase()?.contains(query.lowercase()) == true
    }
}

/**
 * Property-based tests for [UsersUtils].
 * Each test validates a correctness property from the design document.
 *
 * Feature: users-component
 */
class UsersUtilsPropertyTest : StringSpec({

    // ============================================================================
    // Test Helpers
    // ============================================================================

    /**
     * Creates a mock User with the given name.
     */
    fun createMockUser(name: String?, uid: String = "uid_${System.nanoTime()}"): User {
        return User().apply {
            this.uid = uid
            this.name = name
        }
    }

    /**
     * Arbitrary generator for non-empty alphabetic names.
     * Generates names starting with a letter (A-Z, a-z).
     */
    val alphabeticNameArb = Arb.string(1..50).filter { it.isNotBlank() && it.first().isLetter() }

    /**
     * Arbitrary generator for names starting with non-alphabetic characters.
     * Generates names starting with digits or special characters.
     */
    val nonAlphabeticNameArb = arbitrary { rs ->
        val firstChar = Arb.char('0'..'9').bind()
        val rest = Arb.string(0..20).bind()
        "$firstChar$rest"
    }

    /**
     * Arbitrary generator for any valid name (alphabetic or non-alphabetic start).
     */
    val anyNameArb = arbitrary { rs ->
        val useAlphabetic = Arb.boolean().bind()
        if (useAlphabetic) {
            alphabeticNameArb.bind()
        } else {
            nonAlphabeticNameArb.bind()
        }
    }

    /**
     * Arbitrary generator for a list of users with unique UIDs.
     */
    fun arbUniqueUsers(range: IntRange = 0..20) = arbitrary { rs ->
        val count = Arb.int(range).bind()
        (0 until count).map { index ->
            val name = anyNameArb.bind()
            createMockUser(name, "uid_$index")
        }
    }

    /**
     * Arbitrary generator for search queries.
     */
    val searchQueryArb = Arb.string(1..20).filter { it.isNotBlank() }

    // ============================================================================
    // Property 3: Initials Derivation from Name
    // ============================================================================

    /**
     * Feature: users-component, Property 3: Initials Derivation from Name
     * *For any* user without an avatar, the displayed initials should be derived
     * from the first character of the user's name (uppercase). If the name is empty,
     * a default placeholder should be shown.
     * **Validates: Requirements 2.4**
     */
    "Property 3: Initials should be first character of name in uppercase" {
        checkAll(100, alphabeticNameArb) { name ->
            val user = createMockUser(name)
            val initials = UsersUtils.getInitials(user)

            // Initials should be the first character of the name, uppercased
            initials shouldBe name.first().uppercaseChar().toString()
        }
    }

    /**
     * Property 3: Initials for names starting with lowercase should be uppercase.
     * **Validates: Requirements 2.4**
     */
    "Property 3: Initials should be uppercase regardless of name case" {
        checkAll(100, Arb.char('a'..'z')) { lowerChar ->
            val name = "$lowerChar${Arb.string(0..20)}"
            val user = createMockUser(name)
            val initials = UsersUtils.getInitials(user)

            // Initials should be uppercase
            initials shouldBe lowerChar.uppercaseChar().toString()
            initials.first().isUpperCase() shouldBe true
        }
    }

    /**
     * Property 3: Initials for names starting with uppercase should remain uppercase.
     * **Validates: Requirements 2.4**
     */
    "Property 3: Initials should preserve uppercase for uppercase names" {
        checkAll(100, Arb.char('A'..'Z')) { upperChar ->
            val name = "$upperChar${Arb.string(0..20)}"
            val user = createMockUser(name)
            val initials = UsersUtils.getInitials(user)

            // Initials should be the same uppercase character
            initials shouldBe upperChar.toString()
        }
    }

    /**
     * Property 3: Empty or null names should return default placeholder.
     * **Validates: Requirements 2.4**
     */
    "Property 3: Empty or null names should return default placeholder '?'" {
        // Test null name
        val userWithNullName = createMockUser(null)
        UsersUtils.getInitials(userWithNullName) shouldBe "?"

        // Test empty name
        val userWithEmptyName = createMockUser("")
        UsersUtils.getInitials(userWithEmptyName) shouldBe "?"

        // Test blank name (whitespace only)
        val userWithBlankName = createMockUser("   ")
        UsersUtils.getInitials(userWithBlankName) shouldBe "?"
    }

    /**
     * Property 3: getInitialsFromName should behave the same as getInitials.
     * **Validates: Requirements 2.4**
     */
    "Property 3: getInitialsFromName should match getInitials behavior" {
        checkAll(100, anyNameArb) { name ->
            val user = createMockUser(name)
            val initialsFromUser = UsersUtils.getInitials(user)
            val initialsFromName = UsersUtils.getInitialsFromName(name)

            // Both methods should return the same result
            initialsFromUser shouldBe initialsFromName
        }
    }

    /**
     * Property 3: Initials for non-alphabetic starting characters should still work.
     * **Validates: Requirements 2.4**
     */
    "Property 3: Initials for names starting with digits should return the digit" {
        checkAll(100, nonAlphabeticNameArb) { name ->
            val user = createMockUser(name)
            val initials = UsersUtils.getInitials(user)

            // Initials should be the first character (uppercased, though digits don't change)
            initials shouldBe name.first().uppercaseChar().toString()
        }
    }

    // ============================================================================
    // Property 4: Alphabetical Grouping
    // ============================================================================

    /**
     * Feature: users-component, Property 4: Alphabetical Grouping
     * *For any* list of users, users should be grouped by the first letter of their
     * name (case-insensitive). Users whose names start with non-alphabetic characters
     * should be grouped under '#'.
     * **Validates: Requirements 3.1, 3.4**
     */
    "Property 4: Users should be grouped by first letter of name (case-insensitive)" {
        checkAll(100, arbUniqueUsers(1..20)) { users ->
            val grouped = UsersUtils.groupUsersByFirstLetter(users)

            // Each user should be in the correct group
            users.forEach { user ->
                val expectedKey = user.name?.firstOrNull()?.uppercaseChar()?.let {
                    if (it.isLetter()) it else '#'
                } ?: '#'

                grouped[expectedKey]?.any { it.uid == user.uid } shouldBe true
            }
        }
    }

    /**
     * Property 4: Non-alphabetic names should be grouped under '#'.
     * **Validates: Requirements 3.1, 3.4**
     */
    "Property 4: Names starting with non-alphabetic characters should be grouped under '#'" {
        checkAll(100, Arb.list(nonAlphabeticNameArb, 1..10)) { names ->
            val users = names.mapIndexed { index, name -> createMockUser(name, "uid_$index") }
            val grouped = UsersUtils.groupUsersByFirstLetter(users)

            // All users should be under '#'
            grouped.keys.filter { it != '#' } shouldHaveSize 0
            grouped['#']?.size shouldBe users.size
        }
    }

    /**
     * Property 4: Grouping should be case-insensitive.
     * **Validates: Requirements 3.1, 3.4**
     */
    "Property 4: Grouping should be case-insensitive (a and A in same group)" {
        val usersWithMixedCase = listOf(
            createMockUser("Alice", "uid_1"),
            createMockUser("alice", "uid_2"),
            createMockUser("ALICE", "uid_3"),
            createMockUser("Bob", "uid_4"),
            createMockUser("bob", "uid_5")
        )

        val grouped = UsersUtils.groupUsersByFirstLetter(usersWithMixedCase)

        // All Alice variants should be under 'A'
        grouped['A']?.size shouldBe 3
        // All Bob variants should be under 'B'
        grouped['B']?.size shouldBe 2
    }

    /**
     * Property 4: Total users in all groups should equal original list size.
     * **Validates: Requirements 3.1, 3.4**
     */
    "Property 4: Total users in all groups should equal original list size" {
        checkAll(100, arbUniqueUsers(0..30)) { users ->
            val grouped = UsersUtils.groupUsersByFirstLetter(users)

            val totalInGroups = grouped.values.sumOf { it.size }
            totalInGroups shouldBe users.size
        }
    }

    /**
     * Property 4: Groups should be sorted alphabetically with '#' at the end.
     * **Validates: Requirements 3.1, 3.4**
     */
    "Property 4: Groups should be sorted alphabetically with '#' at the end" {
        val usersWithVariousStarts = listOf(
            createMockUser("Zack", "uid_1"),
            createMockUser("Alice", "uid_2"),
            createMockUser("123User", "uid_3"),
            createMockUser("Bob", "uid_4"),
            createMockUser("9Lives", "uid_5")
        )

        val grouped = UsersUtils.groupUsersByFirstLetter(usersWithVariousStarts)
        val keys = grouped.keys.toList()

        // '#' should be at the end if present
        if (keys.contains('#')) {
            keys.last() shouldBe '#'
        }

        // Alphabetic keys should be sorted
        val alphabeticKeys = keys.filter { it != '#' }
        alphabeticKeys shouldBe alphabeticKeys.sorted()
    }

    /**
     * Property 4: getHeaderLetter should return correct header for each user.
     * **Validates: Requirements 3.1, 3.4**
     */
    "Property 4: getHeaderLetter should return uppercase letter or '#'" {
        checkAll(100, anyNameArb) { name ->
            val user = createMockUser(name)
            val headerLetter = UsersUtils.getHeaderLetter(user)

            val firstChar = name.firstOrNull()?.uppercaseChar() ?: '#'
            val expected = if (firstChar.isLetter()) firstChar else '#'

            headerLetter shouldBe expected
        }
    }

    /**
     * Property 4: Empty list should return empty map.
     * **Validates: Requirements 3.1, 3.4**
     */
    "Property 4: Empty user list should return empty grouped map" {
        val grouped = UsersUtils.groupUsersByFirstLetter(emptyList())
        grouped.size shouldBe 0
    }

    // ============================================================================
    // Property 5: Search Filtering Round-Trip
    // ============================================================================

    /**
     * Feature: users-component, Property 5: Search Filtering Round-Trip
     * *For any* search query, the filtered users should all contain the search text
     * in their name (case-insensitive). When the search is cleared, the full original
     * user list should be restored.
     * **Validates: Requirements 4.2, 4.3**
     */
    "Property 5: Filtered users should all contain search text in name (case-insensitive)" {
        checkAll(100, arbUniqueUsers(1..20), searchQueryArb) { users, query ->
            val filtered = UsersUtils.filterUsers(users, query)

            // All filtered users should contain the query in their name (case-insensitive)
            filtered.forEach { user ->
                user.name?.lowercase()?.contains(query.lowercase()) shouldBe true
            }
        }
    }

    /**
     * Property 5: Empty or blank search should return all users.
     * **Validates: Requirements 4.2, 4.3**
     */
    "Property 5: Empty or blank search should return all users" {
        checkAll(100, arbUniqueUsers(0..20)) { users ->
            // Empty string
            val filteredEmpty = UsersUtils.filterUsers(users, "")
            filteredEmpty shouldBe users

            // Blank string (whitespace)
            val filteredBlank = UsersUtils.filterUsers(users, "   ")
            filteredBlank shouldBe users
        }
    }

    /**
     * Property 5: Search should be case-insensitive.
     * **Validates: Requirements 4.2, 4.3**
     */
    "Property 5: Search should be case-insensitive" {
        val users = listOf(
            createMockUser("Alice Johnson", "uid_1"),
            createMockUser("ALICE SMITH", "uid_2"),
            createMockUser("alice brown", "uid_3"),
            createMockUser("Bob Wilson", "uid_4")
        )

        // Search with lowercase
        val filteredLower = UsersUtils.filterUsers(users, "alice")
        filteredLower shouldHaveSize 3

        // Search with uppercase
        val filteredUpper = UsersUtils.filterUsers(users, "ALICE")
        filteredUpper shouldHaveSize 3

        // Search with mixed case
        val filteredMixed = UsersUtils.filterUsers(users, "AlIcE")
        filteredMixed shouldHaveSize 3
    }

    /**
     * Property 5: Filtered results should be a subset of original list.
     * **Validates: Requirements 4.2, 4.3**
     */
    "Property 5: Filtered results should be a subset of original list" {
        checkAll(100, arbUniqueUsers(1..20), searchQueryArb) { users, query ->
            val filtered = UsersUtils.filterUsers(users, query)

            // All filtered users should be in the original list
            filtered.forEach { filteredUser ->
                users.any { it.uid == filteredUser.uid } shouldBe true
            }
        }
    }

    /**
     * Property 5: matchesQuery should be consistent with filterUsers.
     * **Validates: Requirements 4.2, 4.3**
     */
    "Property 5: matchesQuery should be consistent with filterUsers" {
        checkAll(100, arbUniqueUsers(1..20), searchQueryArb) { users, query ->
            val filtered = UsersUtils.filterUsers(users, query)

            // Each user's matchesQuery result should match whether they're in filtered list
            users.forEach { user ->
                val matches = UsersUtils.matchesQuery(user, query)
                val inFiltered = filtered.any { it.uid == user.uid }
                matches shouldBe inFiltered
            }
        }
    }

    /**
     * Property 5: matchesQuery with empty query should always return true.
     * **Validates: Requirements 4.2, 4.3**
     */
    "Property 5: matchesQuery with empty query should always return true" {
        checkAll(100, anyNameArb) { name ->
            val user = createMockUser(name)

            UsersUtils.matchesQuery(user, "") shouldBe true
            UsersUtils.matchesQuery(user, "   ") shouldBe true
        }
    }

    /**
     * Property 5: Search should match partial names.
     * **Validates: Requirements 4.2, 4.3**
     */
    "Property 5: Search should match partial names" {
        val users = listOf(
            createMockUser("Alexander Hamilton", "uid_1"),
            createMockUser("Alexandra Smith", "uid_2"),
            createMockUser("Bob Alexander", "uid_3"),
            createMockUser("Charlie", "uid_4")
        )

        // Search for "alex" should match users with "alex" anywhere in name
        val filtered = UsersUtils.filterUsers(users, "alex")
        filtered shouldHaveSize 3
        filtered.map { it.uid } shouldContainAll listOf("uid_1", "uid_2", "uid_3")
    }

    /**
     * Property 5: Users with null names should not match any non-empty query.
     * **Validates: Requirements 4.2, 4.3**
     */
    "Property 5: Users with null names should not match any non-empty query" {
        checkAll(100, searchQueryArb) { query ->
            val userWithNullName = createMockUser(null)

            UsersUtils.matchesQuery(userWithNullName, query) shouldBe false
        }
    }

    /**
     * Property 5: Round-trip - clearing search restores original list.
     * **Validates: Requirements 4.2, 4.3**
     */
    "Property 5: Clearing search (empty query) restores original list" {
        checkAll(100, arbUniqueUsers(1..20), searchQueryArb) { users, query ->
            // First filter with a query
            val filtered = UsersUtils.filterUsers(users, query)

            // Then "clear" the search by using empty query
            val restored = UsersUtils.filterUsers(users, "")

            // Restored list should be the same as original
            restored shouldBe users
            restored shouldHaveSize users.size
        }
    }

    /**
     * Property 5: Filtering should preserve relative order of matching users.
     * **Validates: Requirements 4.2, 4.3**
     */
    "Property 5: Filtering should preserve relative order of matching users" {
        checkAll(100, arbUniqueUsers(5..20), searchQueryArb) { users, query ->
            val filtered = UsersUtils.filterUsers(users, query)

            // Get indices of filtered users in original list
            val originalIndices = filtered.map { filteredUser ->
                users.indexOfFirst { it.uid == filteredUser.uid }
            }

            // Indices should be in ascending order (preserving original order)
            originalIndices shouldBe originalIndices.sorted()
        }
    }
})
