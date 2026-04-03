package com.cometchat.sampleapp.compose.ui.users

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.presentation.users.ui.CometChatUsers

/**
 * Users screen composable for the Users tab.
 *
 * This screen displays the list of users using the
 * CometChatUsers component from the UIKit.
 *
 * ## Features:
 * - Displays all users with avatars, names, and online status
 * - Supports alphabetical section headers
 * - Supports search functionality
 * - Handles user tap to navigate to messages
 * - Shows loading and empty states
 * - Shows toolbar with title (matching sample-app-kotlin)
 *
 * ## Usage:
 * ```kotlin
 * UsersScreen(
 *     onUserClick = { user ->
 *         // Navigate to messages screen with user
 *     }
 * )
 * ```
 *
 * @param onUserClick Callback when a user is tapped
 *
 * Validates: Requirements 8.1, 8.2, 8.3, 8.4, 8.5, 8.6, 8.7
 */
@Composable
fun UsersScreen(
    onUserClick: (User) -> Unit
) {
    CometChatUsers(
        modifier = Modifier.fillMaxSize(),
        // Show toolbar with title (matching sample-app-kotlin)
        hideToolbar = false,
        // Hide back button since this is a tab
        hideBackIcon = true,
        // Show search box for filtering users
        hideSearchBox = false,
        // Show status indicator for online/offline
        hideStatusIndicator = false,
        // Show sticky alphabetical headers
        hideStickyHeader = false,
        // Callbacks
        onItemClick = { user ->
            onUserClick(user)
        },
        onError = { exception ->
            // Error handling is done internally by the component
            // Additional error handling can be added here if needed
        }
    )
}
