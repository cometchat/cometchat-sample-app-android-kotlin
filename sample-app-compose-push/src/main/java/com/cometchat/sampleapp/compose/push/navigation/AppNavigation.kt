package com.cometchat.sampleapp.compose.push.navigation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.sampleapp.compose.push.MainActivity
import com.cometchat.sampleapp.compose.push.appflow.AppFlowScreen
import com.cometchat.sampleapp.compose.push.appflow.navigation.AppFlowHomeRoute
import com.cometchat.sampleapp.compose.push.appflow.navigation.AddMembersRoute
import com.cometchat.sampleapp.compose.push.appflow.navigation.CallDetailsRoute
import com.cometchat.sampleapp.compose.push.appflow.navigation.ChatHistoryRoute
import com.cometchat.sampleapp.compose.push.appflow.navigation.GroupDetailsRoute
import com.cometchat.sampleapp.compose.push.appflow.navigation.MessagesRoute
import com.cometchat.sampleapp.compose.push.appflow.navigation.NewChatRoute
import com.cometchat.sampleapp.compose.push.appflow.navigation.PinnedMessagesRoute
import com.cometchat.sampleapp.compose.push.appflow.navigation.SavedMessagesRoute
import com.cometchat.sampleapp.compose.push.appflow.navigation.SearchRoute
import com.cometchat.sampleapp.compose.push.appflow.navigation.ThreadMessageRoute
import com.cometchat.sampleapp.compose.push.appflow.navigation.UserDetailsRoute
import com.cometchat.sampleapp.compose.push.appflow.screens.AddMembersScreen
import com.cometchat.sampleapp.compose.push.appflow.screens.CallDetailsScreen
import com.cometchat.sampleapp.compose.push.appflow.screens.ChatHistoryScreen
import com.cometchat.sampleapp.compose.push.appflow.screens.GroupDetailsScreen
import com.cometchat.sampleapp.compose.push.appflow.screens.MessagesScreen
import com.cometchat.sampleapp.compose.push.appflow.screens.NewChatScreen
import com.cometchat.sampleapp.compose.push.appflow.screens.SearchScreen
import com.cometchat.uikit.compose.presentation.pinnedmessages.ui.CometChatPinnedMessages
import com.cometchat.uikit.compose.presentation.savedmessages.ui.CometChatSavedMessages
import com.cometchat.sampleapp.compose.push.appflow.screens.ThreadMessageScreen
import com.cometchat.sampleapp.compose.push.appflow.screens.UserDetailsScreen
import com.cometchat.sampleapp.compose.push.screens.AppCredentialsScreen
import com.cometchat.sampleapp.compose.push.screens.LoginScreen
import com.cometchat.sampleapp.compose.push.screens.SplashScreen

private const val TAG = "AppNavigation"

/**
 * Main navigation composable that hosts all app destinations.
 * Uses Navigation Compose with type-safe routes for navigation.
 *
 * @param modifier Modifier for the NavHost container
 * @param navController The NavHostController for managing navigation
 * @param isDarkMode Current dark mode state
 * @param onDarkModeChange Callback to toggle dark mode
 */
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit
) {
    // Notification deep-link navigation is handled by:
    // 1. SplashRoute → navigates to AppFlowHomeRoute when shouldNavigateToNotifications is true (cold start)
    // 2. AppFlowScreen reads the flag during remember{} initializer and sets Notifications tab
    // 3. For warm start, AppFlowScreen's LaunchedEffect observes trigger changes and switches tab
    // We do NOT navigate from here to avoid creating duplicate composable instances.

    // A tapped chat notification on a warm start: the app is already past Splash, so route straight
    // to the conversation. On a cold start the trigger fires while Splash is still up — the guard
    // below leaves the link parked and SplashScreen's onNavigateToHome routes it after SDK init.
    val chatDeepLinkTrigger = MainActivity.chatDeepLinkTrigger
    LaunchedEffect(chatDeepLinkTrigger) {
        if (chatDeepLinkTrigger == 0) return@LaunchedEffect
        // No destination yet means the graph is still being set up — that is the cold-start case.
        val currentRoute = navController.currentDestination?.route ?: return@LaunchedEffect
        val isPreAuthScreen = listOf("Splash", "Login", "AppCredentials").any { currentRoute.contains(it) }
        if (isPreAuthScreen) return@LaunchedEffect
        MainActivity.consumeChatDeepLink()?.let { navController.navigate(it.toNavRoute()) }
    }

    NavHost(
        navController = navController,
        startDestination = SplashRoute,
        modifier = modifier
    ) {
        // ====================================================================
        // Login Flow Routes
        // ====================================================================

        /**
         * Splash screen - initial screen displayed on app launch.
         * Handles SDK initialization and navigation based on credentials and login state.
         *
         * Navigation:
         * - No credentials → AppCredentialsRoute (popUpTo SplashRoute, inclusive)
         * - User logged in → AppFlowHomeRoute (popUpTo SplashRoute, inclusive)
         * - Not logged in → LoginRoute (popUpTo SplashRoute, inclusive)
         */
        composable<SplashRoute> {
            SplashScreen(
                onNavigateToAppCredentials = {
                    navController.navigate(AppCredentialsRoute) {
                        popUpTo<SplashRoute> { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    // Nobody is signed in, so a parked chat deep link can no longer be opened.
                    MainActivity.consumeChatDeepLink()
                    navController.navigate(LoginRoute) {
                        popUpTo<SplashRoute> { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    val chatDeepLink = MainActivity.consumeChatDeepLink()
                    if (chatDeepLink != null) {
                        // Cold start from a chat notification. Land on the app flow first so Back
                        // returns to the conversation list instead of leaving the app, then open
                        // the conversation (or the thread, for a reply) on top of it.
                        navController.navigate(AppFlowHomeRoute) {
                            popUpTo<SplashRoute> { inclusive = true }
                        }
                        navController.navigate(chatDeepLink.toNavRoute())
                    } else if (MainActivity.shouldNavigateToNotifications) {
                        // Don't consume here — AppFlowScreen will consume it and switch to Notifications tab
                        navController.navigate(AppFlowHomeRoute) {
                            popUpTo<SplashRoute> { inclusive = true }
                        }
                    } else if (navController.currentDestination?.route?.contains("Splash") == true) {
                        navController.navigate(AppFlowHomeRoute) {
                            popUpTo<SplashRoute> { inclusive = true }
                        }
                    }
                }
            )
        }

        /**
         * Login screen - allows users to authenticate via sample user selection
         * or manual UID entry.
         *
         * Navigation:
         * - Login success → AppFlowHomeRoute (popUpTo LoginRoute, inclusive)
         * - Change credentials → AppCredentialsRoute
         */
        composable<LoginRoute> {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate(AppFlowHomeRoute) {
                        popUpTo<LoginRoute> { inclusive = true }
                    }
                },
                onNavigateToAppCredentials = {
                    navController.navigate(AppCredentialsRoute)
                }
            )
        }

        /**
         * App credentials screen - allows users to configure CometChat app credentials.
         *
         * Navigation:
         * - Credentials saved → LoginRoute (popUpTo AppCredentialsRoute, inclusive)
         */
        composable<AppCredentialsRoute> {
            AppCredentialsScreen(
                onNavigateToLogin = {
                    navController.navigate(LoginRoute) {
                        popUpTo<AppCredentialsRoute> { inclusive = true }
                    }
                }
            )
        }

        // App flow destinations
        composable<AppFlowHomeRoute> {
            AppFlowScreen(
                onNavigateToMessages = { user, group, lastMessageId ->
                    user?.let { EntityCache.putUser(it.uid, it) }
                    group?.let { EntityCache.putGroup(it.guid, it) }
                    navController.navigate(MessagesRoute(userId = user?.uid, groupId = group?.guid, lastMessageId = lastMessageId))
                },
                onNavigateToCallDetails = { callLog ->
                    // Store CallLog in cache before navigating (SDK doesn't support fetching by session ID)
                    EntityCache.putCallLog(callLog.sessionID, callLog)
                    navController.navigate(CallDetailsRoute(callSessionId = callLog.sessionID))
                },
                onNavigateToNewChat = {
                    navController.navigate(NewChatRoute)
                },
                onNavigateToSearch = {
                    // Global search - no user/group context
                    navController.navigate(SearchRoute())
                },
                onNavigateToSavedMessages = {
                    navController.navigate(SavedMessagesRoute)
                },
                onLogout = {
                    // Navigate to Login route and clear back stack
                    // Validates: Requirements 11.3, 11.4
                    navController.navigate(LoginRoute) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable<MessagesRoute> { backStackEntry ->
            val route: MessagesRoute = backStackEntry.toRoute()
            
            // Resolve entities from cache first (populated before navigation),
            // falling back to an API fetch if the cache is empty (e.g., deep link).
            val cachedUser = route.userId?.let { EntityCache.getUser(it) }
            val cachedGroup = route.groupId?.let { EntityCache.getGroup(it) }

            var user by remember { mutableStateOf(cachedUser) }
            var group by remember { mutableStateOf(cachedGroup) }
            var isLoading by remember { mutableStateOf(cachedUser == null && cachedGroup == null && (route.userId != null || route.groupId != null)) }
            var error by remember { mutableStateOf<String?>(null) }
            
            // Only fetch from API if cache miss
            if (isLoading) {
                LaunchedEffect(route.userId, route.groupId) {
                    route.userId?.let { uid ->
                        CometChat.getUser(uid, object : CometChat.CallbackListener<User>() {
                            override fun onSuccess(fetchedUser: User) {
                                user = fetchedUser
                                isLoading = false
                            }
                            override fun onError(e: CometChatException) {
                                error = e.message
                                isLoading = false
                            }
                        })
                    }
                    
                    route.groupId?.let { guid ->
                        CometChat.getGroup(guid, object : CometChat.CallbackListener<Group>() {
                            override fun onSuccess(fetchedGroup: Group) {
                                group = fetchedGroup
                                isLoading = false
                            }
                            override fun onError(e: CometChatException) {
                                error = e.message
                                isLoading = false
                            }
                        })
                    }
                }
            }
            
            when {
                isLoading -> LoadingIndicator()
                error != null -> {
                    // On error, navigate back
                    LaunchedEffect(error) {
                        navController.popBackStack()
                    }
                }
                else -> {
                    MessagesScreen(
                        user = user,
                        group = group,
                        goToMessageId = route.goToMessageId,
                        lastMessageId = route.lastMessageId,
                        parentMessageId = route.parentMessageId,
                        onBackPress = { navController.popBackStack() },
                        onNavigateToUserDetails = { userId, lastMessageId ->
                            navController.navigate(UserDetailsRoute(
                                userId = userId,
                                lastMessageId = lastMessageId
                            ))
                        },
                        onNavigateToGroupDetails = { groupId, lastMessageId ->
                            navController.navigate(GroupDetailsRoute(
                                groupId = groupId,
                                lastMessageId = lastMessageId
                            ))
                        },
                        onNavigateToThread = { parentMessageId, userId, groupId ->
                            navController.navigate(ThreadMessageRoute(
                                parentMessageId = parentMessageId,
                                userId = userId,
                                groupId = groupId
                            ))
                        },
                        onNavigateToSearch = { userId, groupId ->
                            navController.navigate(SearchRoute(
                                userId = userId,
                                groupId = groupId
                            ))
                        },
                        onNavigateToChatHistory = { userId ->
                            navController.navigate(ChatHistoryRoute(userId = userId))
                        },
                        onNavigateToPinnedMessages = { userId, groupId ->
                            user?.let { EntityCache.putUser(it.uid, it) }
                            group?.let { EntityCache.putGroup(it.guid, it) }
                            navController.navigate(PinnedMessagesRoute(userId = userId, groupId = groupId))
                        },
                        onNewChat = { userId ->
                            // Fresh AI conversation — no parentMessageId
                            navController.navigate(MessagesRoute(userId = userId)) {
                                popUpTo<MessagesRoute> { inclusive = true }
                            }
                        }
                    )
                }
            }
        }

        composable<PinnedMessagesRoute> { backStackEntry ->
            val route: PinnedMessagesRoute = backStackEntry.toRoute()
            var user by remember { mutableStateOf(route.userId?.let { EntityCache.getUser(it) }) }
            var group by remember { mutableStateOf(route.groupId?.let { EntityCache.getGroup(it) }) }
            LaunchedEffect(route.userId, route.groupId) {
                if (user == null && route.userId != null) {
                    CometChat.getUser(route.userId, object : CometChat.CallbackListener<User>() {
                        override fun onSuccess(u: User) { user = u }
                        override fun onError(e: CometChatException) {}
                    })
                }
                if (group == null && route.groupId != null) {
                    CometChat.getGroup(route.groupId, object : CometChat.CallbackListener<Group>() {
                        override fun onSuccess(g: Group) { group = g }
                        override fun onError(e: CometChatException) {}
                    })
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CometChatTheme.colorScheme.backgroundColor1)
            ) {
              CometChatPinnedMessages(
                modifier = Modifier.statusBarsPadding(),
                user = user,
                group = group,
                onBackClick = { navController.popBackStack() },
                onMessageClick = { message ->
                    user?.let { EntityCache.putUser(it.uid, it) }
                    group?.let { EntityCache.putGroup(it.guid, it) }
                    navController.navigate(
                        MessagesRoute(userId = user?.uid, groupId = group?.guid, goToMessageId = message.id)
                    ) {
                        popUpTo<MessagesRoute> { inclusive = true }
                    }
                }
              )
            }
        }

        composable<SavedMessagesRoute> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CometChatTheme.colorScheme.backgroundColor1)
            ) {
              CometChatSavedMessages(
                modifier = Modifier.statusBarsPadding(),
                onBackClick = { navController.popBackStack() },
                onMessageClick = { message ->
                    // Open the conversation the saved message belongs to, scrolled to it.
                    if (message.receiverType == com.cometchat.chat.constants.CometChatConstants.RECEIVER_TYPE_GROUP) {
                        val g = message.receiver as? Group
                        g?.let { EntityCache.putGroup(it.guid, it) }
                        navController.navigate(MessagesRoute(groupId = g?.guid, goToMessageId = message.id))
                    } else {
                        val myUid = CometChat.getLoggedInUser()?.uid
                        val peer = if (message.sender?.uid == myUid) message.receiver as? User else message.sender
                        peer?.let { EntityCache.putUser(it.uid, it) }
                        navController.navigate(MessagesRoute(userId = peer?.uid, goToMessageId = message.id))
                    }
                }
              )
            }
        }

        composable<UserDetailsRoute> { backStackEntry ->
            val route: UserDetailsRoute = backStackEntry.toRoute()
            
            var user by remember { mutableStateOf<User?>(null) }
            var lastMessage by remember { mutableStateOf<BaseMessage?>(null) }
            var isLoading by remember { mutableStateOf(true) }
            var error by remember { mutableStateOf<String?>(null) }
            
            // Resolve user from ID
            LaunchedEffect(route.userId) {
                isLoading = true
                error = null
                
                CometChat.getUser(route.userId, object : CometChat.CallbackListener<User>() {
                    override fun onSuccess(fetchedUser: User) {
                        user = fetchedUser
                        
                        // Resolve last message if provided
                        route.lastMessageId?.let { messageId ->
                            CometChat.getMessageDetails(messageId, object : CometChat.CallbackListener<BaseMessage>() {
                                override fun onSuccess(message: BaseMessage) {
                                    lastMessage = message
                                    isLoading = false
                                }
                                override fun onError(e: CometChatException) {
                                    // Message resolution failure is not critical
                                    isLoading = false
                                }
                            })
                        } ?: run {
                            isLoading = false
                        }
                    }
                    override fun onError(e: CometChatException) {
                        error = e.message
                        isLoading = false
                    }
                })
            }
            
            when {
                isLoading -> LoadingIndicator()
                error != null || user == null -> {
                    LaunchedEffect(error) {
                        navController.popBackStack()
                    }
                }
                else -> {
                    UserDetailsScreen(
                        user = user!!,
                        lastMessage = lastMessage,
                        onBackPress = { navController.popBackStack() },
                        onChatDeleted = {
                            // Navigate back to app flow home by popping to it
                            navController.popBackStack<AppFlowHomeRoute>(inclusive = false)
                        },
                        onNavigateToPinnedMessages = {
                            user?.let { EntityCache.putUser(it.uid, it) }
                            navController.navigate(PinnedMessagesRoute(userId = user!!.uid))
                        }
                    )
                }
            }
        }

        composable<GroupDetailsRoute> { backStackEntry ->
            val route: GroupDetailsRoute = backStackEntry.toRoute()
            
            var group by remember { mutableStateOf<Group?>(null) }
            var lastMessage by remember { mutableStateOf<BaseMessage?>(null) }
            var isLoading by remember { mutableStateOf(true) }
            var error by remember { mutableStateOf<String?>(null) }
            
            // Resolve group from ID
            LaunchedEffect(route.groupId) {
                isLoading = true
                error = null
                
                CometChat.getGroup(route.groupId, object : CometChat.CallbackListener<Group>() {
                    override fun onSuccess(fetchedGroup: Group) {
                        group = fetchedGroup
                        
                        // Resolve last message if provided
                        route.lastMessageId?.let { messageId ->
                            CometChat.getMessageDetails(messageId, object : CometChat.CallbackListener<BaseMessage>() {
                                override fun onSuccess(message: BaseMessage) {
                                    lastMessage = message
                                    isLoading = false
                                }
                                override fun onError(e: CometChatException) {
                                    // Message resolution failure is not critical
                                    isLoading = false
                                }
                            })
                        } ?: run {
                            isLoading = false
                        }
                    }
                    override fun onError(e: CometChatException) {
                        error = e.message
                        isLoading = false
                    }
                })
            }
            
            when {
                isLoading -> LoadingIndicator()
                error != null || group == null -> {
                    LaunchedEffect(error) {
                        navController.popBackStack()
                    }
                }
                else -> {
                    GroupDetailsScreen(
                        group = group!!,
                        lastMessage = lastMessage,
                        onBackPress = { navController.popBackStack() },
                        onGroupLeft = {
                            navController.popBackStack<AppFlowHomeRoute>(inclusive = false)
                        },
                        onGroupDeleted = {
                            navController.popBackStack<AppFlowHomeRoute>(inclusive = false)
                        },
                        onChatDeleted = {
                            navController.popBackStack<AppFlowHomeRoute>(inclusive = false)
                        },
                        onNavigateToAddMembers = { groupId ->
                            navController.navigate(AddMembersRoute(groupId = groupId))
                        },
                        onNavigateToPinnedMessages = {
                            group?.let { EntityCache.putGroup(it.guid, it) }
                            navController.navigate(PinnedMessagesRoute(groupId = group!!.guid))
                        }
                    )
                }
            }
        }
        
        composable<AddMembersRoute> { backStackEntry ->
            val route: AddMembersRoute = backStackEntry.toRoute()
            
            var group by remember { mutableStateOf<Group?>(null) }
            var isLoading by remember { mutableStateOf(true) }
            var error by remember { mutableStateOf<String?>(null) }
            
            // Resolve group from ID
            LaunchedEffect(route.groupId) {
                isLoading = true
                error = null
                
                CometChat.getGroup(route.groupId, object : CometChat.CallbackListener<Group>() {
                    override fun onSuccess(fetchedGroup: Group) {
                        group = fetchedGroup
                        isLoading = false
                    }
                    override fun onError(e: CometChatException) {
                        error = e.message
                        isLoading = false
                    }
                })
            }
            
            when {
                isLoading -> LoadingIndicator()
                error != null || group == null -> {
                    LaunchedEffect(error) {
                        navController.popBackStack()
                    }
                }
                else -> {
                    AddMembersScreen(
                        group = group!!,
                        onBackPress = { navController.popBackStack() },
                        onMembersAdded = { navController.popBackStack() }
                    )
                }
            }
        }
        
        composable<CallDetailsRoute> { backStackEntry ->
            val route: CallDetailsRoute = backStackEntry.toRoute()
            
            // Retrieve CallLog from cache (SDK doesn't support fetching by session ID)
            val callLog = remember(route.callSessionId) {
                EntityCache.getCallLog(route.callSessionId)
            }
            
            if (callLog != null) {
                CallDetailsScreen(
                    callLog = callLog,
                    onBackPress = { navController.popBackStack() }
                )
            } else {
                // CallLog not found in cache, navigate back
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }
        
        composable<NewChatRoute> {
            NewChatScreen(
                onBackPress = { navController.popBackStack() },
                onUserSelected = { user ->
                    EntityCache.putUser(user.uid, user)
                    navController.popBackStack()
                    navController.navigate(MessagesRoute(userId = user.uid))
                },
                onGroupSelected = { group ->
                    EntityCache.putGroup(group.guid, group)
                    navController.popBackStack()
                    navController.navigate(MessagesRoute(groupId = group.guid))
                }
            )
        }

        composable<ThreadMessageRoute> { backStackEntry ->
            val route: ThreadMessageRoute = backStackEntry.toRoute()
            
            var parentMessage by remember { mutableStateOf<BaseMessage?>(null) }
            var user by remember { mutableStateOf<User?>(null) }
            var group by remember { mutableStateOf<Group?>(null) }
            var isLoading by remember { mutableStateOf(true) }
            var error by remember { mutableStateOf<String?>(null) }
            
            // Resolve entities from IDs
            LaunchedEffect(route.parentMessageId, route.userId, route.groupId) {
                isLoading = true
                error = null
                
                // First resolve the parent message
                CometChat.getMessageDetails(route.parentMessageId, object : CometChat.CallbackListener<BaseMessage>() {
                    override fun onSuccess(message: BaseMessage) {
                        parentMessage = message
                        
                        // Then resolve user or group
                        var entityResolved = false
                        
                        route.userId?.let { uid ->
                            CometChat.getUser(uid, object : CometChat.CallbackListener<User>() {
                                override fun onSuccess(fetchedUser: User) {
                                    user = fetchedUser
                                    isLoading = false
                                }
                                override fun onError(e: CometChatException) {
                                    error = e.message
                                    isLoading = false
                                }
                            })
                            entityResolved = true
                        }
                        
                        route.groupId?.let { guid ->
                            CometChat.getGroup(guid, object : CometChat.CallbackListener<Group>() {
                                override fun onSuccess(fetchedGroup: Group) {
                                    group = fetchedGroup
                                    isLoading = false
                                }
                                override fun onError(e: CometChatException) {
                                    error = e.message
                                    isLoading = false
                                }
                            })
                            entityResolved = true
                        }
                        
                        if (!entityResolved) {
                            isLoading = false
                        }
                    }
                    override fun onError(e: CometChatException) {
                        error = e.message
                        isLoading = false
                    }
                })
            }
            
            when {
                isLoading -> LoadingIndicator()
                error != null || parentMessage == null -> {
                    LaunchedEffect(error) {
                        navController.popBackStack()
                    }
                }
                else -> {
                    ThreadMessageScreen(
                        parentMessage = parentMessage!!,
                        user = user,
                        group = group,
                        goToMessageId = route.goToMessageId,
                        onBackPress = { navController.popBackStack() }
                    )
                }
            }
        }
        
        composable<SearchRoute> { backStackEntry ->
            val route: SearchRoute = backStackEntry.toRoute()

            SearchScreen(
                userId = route.userId,
                groupId = route.groupId,
                onBackPress = { navController.popBackStack() },
                onNavigateToMessages = { userId, groupId, messageId ->
                    navController.navigate(MessagesRoute(
                        userId = userId,
                        groupId = groupId,
                        goToMessageId = messageId
                    )) {
                        // Pop search from back stack to match sample-app behavior
                        popUpTo<SearchRoute> { inclusive = true }
                    }
                },
                onNavigateToThread = { parentMessageId, goToMessageId ->
                    navController.navigate(ThreadMessageRoute(
                        parentMessageId = parentMessageId,
                        userId = route.userId,
                        groupId = route.groupId,
                        // The hit itself, so the thread scrolls to the reply and not the parent.
                        goToMessageId = goToMessageId.takeIf { it > 0 }
                    )) {
                        popUpTo<SearchRoute> { inclusive = true }
                    }
                }
            )
        }

        composable<ChatHistoryRoute> { backStackEntry ->
            val route: ChatHistoryRoute = backStackEntry.toRoute()

            val cachedUser = EntityCache.getUser(route.userId)
            var user by remember { mutableStateOf(cachedUser) }
            var isLoading by remember { mutableStateOf(cachedUser == null) }

            if (isLoading) {
                LaunchedEffect(route.userId) {
                    CometChat.getUser(route.userId, object : CometChat.CallbackListener<User>() {
                        override fun onSuccess(fetchedUser: User) {
                            user = fetchedUser
                            isLoading = false
                        }
                        override fun onError(e: CometChatException) {
                            isLoading = false
                        }
                    })
                }
            }

            when {
                isLoading -> LoadingIndicator()
                user != null -> ChatHistoryScreen(
                    user = user!!,
                    onBackPress = { navController.popBackStack() },
                    onNavigateToMessages = { userId, parentMsgId ->
                        navController.navigate(MessagesRoute(userId = userId, parentMessageId = parentMsgId)) {
                            popUpTo<MessagesRoute> { inclusive = true }
                        }
                    },
                    onNewChat = { userId ->
                        // Fresh AI conversation — no parentMessageId
                        navController.navigate(MessagesRoute(userId = userId)) {
                            popUpTo<MessagesRoute> { inclusive = true }
                        }
                    }
                )
                else -> LaunchedEffect(Unit) { navController.popBackStack() }
            }
        }
    }
}

/**
 * Loading indicator shown while resolving entities.
 */
@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = CometChatTheme.colorScheme.primary
        )
    }
}

/**
 * Destination for a tapped chat notification: the thread when the tapped message is a reply — a
 * reply is not in the main list, so opening the conversation would highlight nothing — otherwise
 * the conversation itself. Either way the tapped message is the one scrolled to.
 */
private fun MainActivity.ChatDeepLink.toNavRoute(): Any =
    if (parentMessageId > 0) {
        ThreadMessageRoute(
            parentMessageId = parentMessageId,
            userId = userId,
            groupId = groupId,
            goToMessageId = messageId.takeIf { it > 0 }
        )
    } else {
        MessagesRoute(
            userId = userId,
            groupId = groupId,
            goToMessageId = messageId.takeIf { it > 0 }
        )
    }
