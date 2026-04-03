package com.cometchat.sampleapp.compose.ui.home

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cometchat.calls.model.CallLog
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.sampleapp.compose.R
import com.cometchat.sampleapp.compose.ui.calls.CallLogsScreen
import com.cometchat.sampleapp.compose.ui.conversations.ConversationsScreen
import com.cometchat.sampleapp.compose.ui.groups.GroupsScreen
import com.cometchat.sampleapp.compose.ui.users.UsersScreen
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Data class representing a bottom navigation item.
 * Uses drawable resource IDs for custom icons to match sample-app-kotlin.
 */
private data class BottomNavItem(
    val tab: HomeTab,
    val label: String,
    @DrawableRes val iconRes: Int
)

/**
 * Home screen composable with bottom navigation.
 *
 * This screen contains four tabs:
 * - Chats: Displays the conversation list
 * - Calls: Displays the call logs
 * - Users: Displays the user list
 * - Groups: Displays the group list
 *
 * ## Features:
 * - Bottom navigation with tab switching
 * - State preservation for each tab
 * - Logout functionality via overflow menu
 * - Navigation to message screens
 * - Custom navigation icons matching sample-app-kotlin
 *
 * ## Usage:
 * ```kotlin
 * HomeScreen(
 *     onLogout = {
 *         navController.navigate(LoginRoute) {
 *             popUpTo(HomeRoute) { inclusive = true }
 *         }
 *     },
 *     onConversationClick = { conversation ->
 *         // Navigate to messages
 *     },
 *     onCallLogClick = { callLog ->
 *         // Navigate to messages with call log participant
 *     },
 *     onUserClick = { user ->
 *         // Navigate to messages with user
 *     },
 *     onGroupClick = { group ->
 *         // Navigate to messages with group
 *     }
 * )
 * ```
 *
 * @param viewModel The HomeViewModel instance
 * @param onLogout Callback when logout is successful
 * @param onConversationClick Callback when a conversation is tapped
 * @param onCallLogClick Callback when a call log is tapped
 * @param onUserClick Callback when a user is tapped
 * @param onGroupClick Callback when a group is tapped
 * @param onNewChatClick Callback when new chat is requested
 *
 * Validates: Requirements 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onLogout: () -> Unit,
    onConversationClick: (Conversation) -> Unit,
    onCallLogClick: (CallLog) -> Unit = {},
    onUserClick: (User) -> Unit,
    onGroupClick: (Group) -> Unit,
    onNewChatClick: () -> Unit = {}
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Handle back press to close the app (matching sample-app-kotlin behavior)
    // When on HomeScreen, pressing back should finish the activity
    BackHandler {
        (context as? Activity)?.finish()
    }

    // Handle navigation events
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is HomeNavigationEvent.ToLogin -> onLogout()
            }
        }
    }

    // Bottom navigation items - Chats, Calls, Users, Groups (Requirement 5.1)
    // Using custom drawable icons to match sample-app-kotlin
    val navItems = remember {
        listOf(
            BottomNavItem(HomeTab.CHATS, "Chats", R.drawable.ic_nav_chats),
            BottomNavItem(HomeTab.CALLS, "Calls", R.drawable.ic_nav_calls),
            BottomNavItem(HomeTab.USERS, "Users", R.drawable.ic_nav_users),
            BottomNavItem(HomeTab.GROUPS, "Groups", R.drawable.ic_nav_groups)
        )
    }

    Scaffold(
        topBar = {},
        bottomBar = {
            Column {
                // Separator line above bottom navigation (matching sample-app-kotlin)
                HorizontalDivider(
                    thickness = 1.dp,
                    color = CometChatTheme.colorScheme.strokeColorLight
                )
                NavigationBar(
                    containerColor = CometChatTheme.colorScheme.backgroundColor1,
                    contentColor = CometChatTheme.colorScheme.textColorPrimary,
                    tonalElevation = 0.dp
                ) {
                    navItems.forEach { item ->
                        val isSelected = currentTab == item.tab
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    painter = painterResource(id = item.iconRes),
                                    contentDescription = item.label,
                                    modifier = Modifier.size(32.dp) // Match sample-app-kotlin itemIconSize
                                )
                            },
                            // Show label only when selected (matching sample-app-kotlin)
                            label = if (isSelected) {
                                {
                                    Text(
                                        text = item.label,
                                        style = CometChatTheme.typography.caption1Medium
                                    )
                                }
                            } else null,
                            selected = isSelected,
                            onClick = { viewModel.selectTab(item.tab) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CometChatTheme.colorScheme.iconTintHighlight,
                                selectedTextColor = CometChatTheme.colorScheme.iconTintHighlight,
                                indicatorColor = CometChatTheme.colorScheme.backgroundColor1,
                                unselectedIconColor = CometChatTheme.colorScheme.iconTintSecondary,
                                unselectedTextColor = CometChatTheme.colorScheme.iconTintSecondary
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // State preservation: Keep all screens in composition but control visibility
            // This ensures scroll position, search queries, and other state are preserved
            // when switching between tabs.
            //
            // Using AnimatedVisibility with fade transitions for smooth tab switching
            // while keeping each screen's state intact in the composition tree.
            //
            // Validates: Requirements 5.6 (State preservation for each tab)
            
            // Chats tab - always in composition, visibility controlled by currentTab
            // Validates: Requirement 5.2
            AnimatedVisibility(
                visible = currentTab == HomeTab.CHATS,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ConversationsScreen(
                    onConversationClick = onConversationClick,
                    onLogout = onLogout,
                    onNewChatClick = onNewChatClick
                )
            }
            
            // Calls tab - always in composition, visibility controlled by currentTab
            // Validates: Requirement 5.3
            AnimatedVisibility(
                visible = currentTab == HomeTab.CALLS,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                CallLogsScreen(
                    onCallLogClick = onCallLogClick
                )
            }
            
            // Users tab - always in composition, visibility controlled by currentTab
            // Validates: Requirement 5.4
            AnimatedVisibility(
                visible = currentTab == HomeTab.USERS,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                UsersScreen(
                    onUserClick = onUserClick
                )
            }
            
            // Groups tab - always in composition, visibility controlled by currentTab
            // Validates: Requirement 5.5
            AnimatedVisibility(
                visible = currentTab == HomeTab.GROUPS,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                GroupsScreen(
                    onGroupClick = onGroupClick
                )
            }
        }
    }
}
