package com.cometchat.uikit.compose.presentation.users.utils

import com.cometchat.chat.models.User

/**
 * Utility functions for the CometChatUsers component.
 */
object UsersUtils {
    
    /**
     * Groups users by the first letter of their name.
     * Users whose names start with non-alphabetic characters are grouped under '#'.
     *
     * @param users The list of users to group
     * @return A map of first letter to list of users, sorted alphabetically
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
     *
     * @param user The user to get initials from
     * @return The initials string (single character)
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
     * Returns the first character of the name in uppercase.
     * If the name is empty or null, returns a default placeholder.
     *
     * @param name The name to get initials from
     * @return The initials string (single character)
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
     *
     * @param users The list of users to filter
     * @param query The search query
     * @return The filtered list of users
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
     *
     * @param user The user to get the header letter from
     * @return The header letter character
     */
    fun getHeaderLetter(user: User): Char {
        val firstChar = user.name?.firstOrNull()?.uppercaseChar() ?: '#'
        return if (firstChar.isLetter()) firstChar else '#'
    }
    
    /**
     * Groups users by the first letter of their name while preserving the original order.
     * Unlike [groupUsersByFirstLetter], this function does NOT sort users alphabetically.
     * It groups consecutive users with the same first letter together, maintaining
     * the order from the input list (important for online users moved to top).
     *
     * Users whose names start with non-alphabetic characters are grouped under '#'.
     *
     * @param users The list of users to group (order is preserved)
     * @return A list of pairs where each pair contains a header letter and the users in that group
     */
    fun groupUsersByFirstLetterPreservingOrder(users: List<User>): List<Pair<Char, List<User>>> {
        if (users.isEmpty()) return emptyList()
        
        val result = mutableListOf<Pair<Char, List<User>>>()
        var currentLetter: Char? = null
        var currentGroup = mutableListOf<User>()
        
        for (user in users) {
            val letter = getHeaderLetter(user)
            
            if (letter != currentLetter) {
                if (currentGroup.isNotEmpty() && currentLetter != null) {
                    result.add(currentLetter to currentGroup.toList())
                }
                currentLetter = letter
                currentGroup = mutableListOf(user)
            } else {
                currentGroup.add(user)
            }
        }
        
        // Add last group
        if (currentGroup.isNotEmpty() && currentLetter != null) {
            result.add(currentLetter to currentGroup.toList())
        }
        
        return result
    }
    
    /**
     * Checks if a user matches a search query.
     *
     * @param user The user to check
     * @param query The search query
     * @return True if the user matches the query
     */
    fun matchesQuery(user: User, query: String): Boolean {
        if (query.isBlank()) {
            return true
        }
        return user.name?.lowercase()?.contains(query.lowercase()) == true
    }
}
