package com.cometchat.sampleapp.compose.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.cometchat.sampleapp.compose.ui.calls.CallDetailsScreen
import com.cometchat.sampleapp.compose.ui.calls.toJson
import com.cometchat.sampleapp.compose.ui.conversations.getGroup
import com.cometchat.sampleapp.compose.ui.conversations.getUser
import com.cometchat.sampleapp.compose.ui.credentials.AppCredentialsScreen
import com.cometchat.sampleapp.compose.ui.groups.AddMembersScreen
import com.cometchat.sampleapp.compose.ui.groups.BannedMembersScreen
import com.cometchat.sampleapp.compose.ui.groups.GroupDetailsScreen
import com.cometchat.sampleapp.compose.ui.groups.GroupMembersScreen
import com.cometchat.sampleapp.compose.ui.home.HomeScreen
import com.cometchat.sampleapp.compose.ui.login.LoginScreen
import com.cometchat.sampleapp.compose.ui.messages.MessagesScreen
import com.cometchat.sampleapp.compose.ui.messages.ThreadMessagesScreen
import com.cometchat.sampleapp.compose.ui.newchat.NewChatScreen
import com.cometchat.sampleapp.compose.ui.search.SearchScreen
import com.cometchat.sampleapp.compose.ui.splash.SplashScreen
import com.cometchat.sampleapp.compose.ui.users.UserDetailsScreen

/**
 * Main navigation graph for the CometChat Sample App.
 *
 * This composable sets up the navigation structure using Jetpack Compose Navigation
 * with type-safe routes defined in [AppNavigation.kt].
 *
 * ## Navigation Flow:
 * ```
 * SplashRoute -> AppCredentialsRoute (if no credentials)
 *            -> LoginRoute (if not logged in)
 *            -> HomeRoute (if logged in)
 *
 * LoginRoute -> HomeRoute -> MessagesRoute
 *                        -> UserDetailsRoute
 *                        -> GroupDetailsRoute
 * ```
 *
 * ## Usage:
 * ```kotlin
 * @Composable
 * fun App() {
 *     AppNavGraph()
 * }
 * ```
 *
 * @param navController The NavHostController for navigation (defaults to a new instance)
 * @param startDestination The initial route (defaults to SplashRoute)
 *
 * Validates: Requirements 4.3, 11.1, 11.2, 11.3, 13.1
 */
@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: Any = SplashRoute
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Splash Screen - Entry point
        // Validates: Requirements 1.1, 1.2, 1.3, 1.4, 1.5, 2.1, 2.2, 2.3, 2.4, 3.1, 3.2, 3.3, 3.4, 13.1
        composable<SplashRoute> {
            SplashScreen(
                onNavigateToAppCredentials = {
                    // Navigate to AppCredentials and clear splash from back stack
                    // Validates: Requirement 2.2, 13.2
                    navController.navigate(AppCredentialsRoute) {
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    // Navigate to Login and clear splash from back stack
                    // Validates: Requirement 3.3, 3.4
                    navController.navigate(LoginRoute) {
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    // Navigate to Home and clear entire back stack
                    // Validates: Requirements 3.2, 3.4, 11.2
                    navController.navigate(HomeRoute) {
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                }
            )
        }

        // App Credentials Screen - First-time setup
        // Validates: Requirements 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 13.2
        composable<AppCredentialsRoute> {
            AppCredentialsScreen(
                onCredentialsSaved = {
                    // Navigate to Login and clear credentials from back stack
                    // Validates: Requirement 4.7
                    navController.navigate(LoginRoute) {
                        popUpTo(AppCredentialsRoute) { inclusive = true }
                    }
                }
            )
        }

        // Login Screen
        composable<LoginRoute> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(HomeRoute) {
                        // Clear the back stack so user can't go back to login
                        // Validates: Requirement 11.3
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                },
                onChangeAppCredentials = {
                    // Navigate to AppCredentials to change credentials
                    navController.navigate(AppCredentialsRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                }
            )
        }

        // Home Screen with bottom navigation
        composable<HomeRoute> {
            HomeScreen(
                onLogout = {
                    navController.navigate(LoginRoute) {
                        // Clear the entire back stack
                        popUpTo(HomeRoute) { inclusive = true }
                    }
                },
                onConversationClick = { conversation ->
                    // Navigate to messages based on conversation type
                    val user = conversation.getUser()
                    val group = conversation.getGroup()

                    when {
                        user != null -> {
                            navController.navigate(
                                MessagesRoute(userId = user.uid, groupId = null)
                            )
                        }
                        group != null -> {
                            navController.navigate(
                                MessagesRoute(userId = null, groupId = group.guid)
                            )
                        }
                    }
                },
                onCallLogClick = { callLog ->
                    // Navigate to CallDetailsScreen with serialized call log
                    // Validates: Requirement 9.2
                    navController.navigate(
                        CallDetailsRoute(callLogJson = callLog.toJson())
                    )
                },
                onUserClick = { user ->
                    navController.navigate(
                        MessagesRoute(userId = user.uid, groupId = null)
                    )
                },
                onGroupClick = { group ->
                    navController.navigate(
                        MessagesRoute(userId = null, groupId = group.guid)
                    )
                },
                onNewChatClick = {
                    // Navigate to NewChatScreen to start a new conversation
                    // Validates: Requirement 8.4
                    navController.navigate(NewChatRoute)
                }
            )
        }

        // Messages Screen
        // Validates: Requirements 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 6.8, 6.9, 7.1, 7.2, 7.3
        composable<MessagesRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<MessagesRoute>()
            MessagesScreen(
                userId = route.userId,
                groupId = route.groupId,
                onBackPress = { navController.popBackStack() },
                onUserDetailsClick = { user ->
                    navController.navigate(UserDetailsRoute(userId = user.uid))
                },
                onGroupDetailsClick = { group ->
                    navController.navigate(GroupDetailsRoute(groupId = group.guid))
                },
                onThreadClick = { message ->
                    navController.navigate(ThreadRoute(parentMessageId = message.id.toLong()))
                }
            )
        }

        // User Details Screen
        // Validates: Requirements 10.1, 10.2, 10.3, 10.4, 10.6, 10.7, 10.8
        composable<UserDetailsRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<UserDetailsRoute>()
            UserDetailsScreen(
                userId = route.userId,
                onBackPress = { navController.popBackStack() },
                onMessageClick = { user ->
                    // Navigate to messages with this user
                    navController.navigate(MessagesRoute(userId = user.uid, groupId = null))
                }
            )
        }

        // Group Details Screen
        // Validates: Requirements 7.4, 7.5, 7.6, 11.1, 11.2, 11.3, 11.4, 11.6, 11.7, 11.8, 11.9, 11.10
        composable<GroupDetailsRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<GroupDetailsRoute>()
            GroupDetailsScreen(
                groupId = route.groupId,
                onBackPress = { navController.popBackStack() },
                onLeaveGroup = {
                    // Navigate back to home after leaving group
                    navController.popBackStack(HomeRoute, inclusive = false)
                },
                onMemberClick = { member ->
                    // Navigate to user details when member is tapped
                    navController.navigate(UserDetailsRoute(userId = member.uid))
                },
                onNavigateToAddMembers = { groupId ->
                    // Navigate to AddMembersScreen
                    navController.navigate(AddMembersRoute(groupId = groupId))
                },
                onNavigateToMembers = { groupId ->
                    // Navigate to GroupMembersScreen (full-screen)
                    navController.navigate(GroupMembersRoute(groupId = groupId))
                },
                onNavigateToBannedMembers = { groupId ->
                    // Navigate to BannedMembersScreen (full-screen)
                    navController.navigate(BannedMembersRoute(groupId = groupId))
                }
            )
        }

        // Add Members Screen - Add members to a group
        // Validates: Requirements 11.5
        composable<AddMembersRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<AddMembersRoute>()
            AddMembersScreen(
                groupId = route.groupId,
                onBackPress = { navController.popBackStack() },
                onMembersAdded = { navController.popBackStack() }
            )
        }

        // Group Members Screen - View group members (full-screen)
        // Validates: Requirements 11.4
        composable<GroupMembersRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<GroupMembersRoute>()
            GroupMembersScreen(
                groupId = route.groupId,
                onBackPress = { navController.popBackStack() }
            )
        }

        // Banned Members Screen - View and manage banned members (full-screen)
        // Validates: Requirements 11.4
        composable<BannedMembersRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<BannedMembersRoute>()
            BannedMembersScreen(
                groupId = route.groupId,
                onBackPress = { navController.popBackStack() }
            )
        }

        // Thread Messages Screen
        // Validates: Requirements 6.9
        composable<ThreadRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ThreadRoute>()
            ThreadMessagesScreen(
                parentMessageId = route.parentMessageId,
                onBackPress = { navController.popBackStack() }
            )
        }

        // Search Screen - Global and contextual search
        // Validates: Requirements 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7, 6.1, 6.2, 6.3, 6.4, 13.3
        composable<SearchRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<SearchRoute>()
            SearchScreen(
                userId = route.userId,
                groupId = route.groupId,
                onBackPress = { navController.popBackStack() },
                onNavigateToMessages = { userId, groupId ->
                    // Navigate to messages screen
                    // Validates: Requirements 5.4, 5.6
                    navController.navigate(MessagesRoute(userId = userId, groupId = groupId))
                },
                onNavigateToThread = { parentMessageId ->
                    // Navigate to thread messages screen
                    // Validates: Requirement 5.5
                    navController.navigate(ThreadRoute(parentMessageId = parentMessageId))
                }
            )
        }

        // New Chat Screen - Start new conversations
        // Validates: Requirements 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 13.4
        composable<NewChatRoute> {
            NewChatScreen(
                onBackPress = {
                    // Validates: Requirement 7.6 - back button closes screen
                    navController.popBackStack()
                },
                onUserSelected = { user ->
                    // Navigate to messages with selected user
                    // Validates: Requirement 7.4
                    navController.navigate(MessagesRoute(userId = user.uid, groupId = null))
                },
                onGroupSelected = { group ->
                    // Navigate to messages with selected group
                    // Validates: Requirement 7.5
                    navController.navigate(MessagesRoute(userId = null, groupId = group.guid))
                }
            )
        }

        // Call Details Screen - Display call log details
        // Validates: Requirements 9.2, 13.5
        composable<CallDetailsRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<CallDetailsRoute>()
            CallDetailsScreen(
                callLogJson = route.callLogJson,
                onBackPress = { navController.popBackStack() },
                onUserClick = { user ->
                    // Navigate to user details when user is clicked
                    navController.navigate(UserDetailsRoute(userId = user.uid))
                }
            )
        }
    }
}
