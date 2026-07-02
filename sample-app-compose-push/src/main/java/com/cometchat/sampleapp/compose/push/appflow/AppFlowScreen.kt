package com.cometchat.sampleapp.compose.push.appflow

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.cometchat.calls.model.CallLog
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.sampleapp.compose.push.MainActivity
import com.cometchat.sampleapp.compose.push.R
import com.cometchat.sampleapp.compose.push.appflow.navigation.AppFlowTab
import com.cometchat.sampleapp.compose.push.appflow.tabs.CallsTab
import com.cometchat.sampleapp.compose.push.appflow.tabs.ChatsTab
import com.cometchat.sampleapp.compose.push.appflow.tabs.GroupsTab
import com.cometchat.sampleapp.compose.push.appflow.tabs.NotificationsTab
import com.cometchat.sampleapp.compose.push.appflow.tabs.UsersTab

/**
 * Main App Flow screen with bottom navigation.
 * Contains four tabs: Chats, Calls, Users, Groups.
 *
 * @param onNavigateToMessages Callback when navigating to messages screen, accepts User, Group, and lastMessageId
 * @param onNavigateToCallDetails Callback when navigating to call details screen, accepts CallLog for caching
 * @param onNavigateToNewChat Callback when navigating to new chat screen
 * @param onNavigateToSearch Callback when navigating to search screen
 * @param onLogout Callback when logout is successful, navigates to Login screen
 */
@Composable
fun AppFlowScreen(
    onNavigateToMessages: (user: User?, group: Group?, lastMessageId: Long?) -> Unit,
    onNavigateToCallDetails: (CallLog) -> Unit,
    onNavigateToNewChat: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onLogout: () -> Unit
) {
    // Read the notification flag during remember initializer so all composable instances
    // (including those created during NavHost transitions) start on the Notifications tab.
    // The flag is consumed after a delay to ensure transition-period instances all read it.
    var selectedTab by remember { 
        mutableStateOf(
            if (MainActivity.shouldNavigateToNotifications) AppFlowTab.Notifications else AppFlowTab.Chats
        )
    }
    
    val currentTrigger = MainActivity.notificationDeepLinkTrigger
    
    // Consume the flag after a short delay to ensure navigation transition is complete
    // and all composable instances have read the flag during their remember{} initializer.
    // Also handles warm-start (already on AppFlowHomeRoute when notification arrives).
    LaunchedEffect(currentTrigger) {
        if (MainActivity.shouldNavigateToNotifications) {
            kotlinx.coroutines.delay(500)
            MainActivity.consumeNavigateToNotifications()
            if (selectedTab != AppFlowTab.Notifications) {
                selectedTab = AppFlowTab.Notifications
            }
        }
    }
    
    val backgroundColor = CometChatTheme.colorScheme.backgroundColor1
    val separatorColor = CometChatTheme.colorScheme.strokeColorLight
    
    Scaffold(
        containerColor = backgroundColor,
        bottomBar = {
            AppFlowBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                separatorColor = separatorColor
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            when (selectedTab) {
                AppFlowTab.Chats -> ChatsTab(
                    onConversationClick = { user, group, lastMessageId ->
                        onNavigateToMessages(user, group, lastMessageId)
                    },
                    onNewChatClick = onNavigateToNewChat,
                    onSearchClick = onNavigateToSearch,
                    onLogout = onLogout,
                    contentPadding = paddingValues
                )
                AppFlowTab.Calls -> CallsTab(
                    onCallLogClick = { callLog ->
                        onNavigateToCallDetails(callLog)
                    },
                    contentPadding = paddingValues
                )
                AppFlowTab.Users -> UsersTab(
                    onUserClick = { user ->
                        onNavigateToMessages(user, null, null)
                    },
                    contentPadding = paddingValues
                )
                AppFlowTab.Groups -> GroupsTab(
                    onGroupClick = { group ->
                        onNavigateToMessages(null, group, null)
                    },
                    contentPadding = paddingValues
                )
                AppFlowTab.Notifications -> NotificationsTab(
                    contentPadding = paddingValues
                )
            }
        }
    }
}

/**
 * Bottom navigation bar for the app flow.
 * Styled to match master-app-kotlin2 with 32dp icons, no ripple, and invisible indicator.
 */
@Composable
private fun AppFlowBottomNavigation(
    selectedTab: AppFlowTab,
    onTabSelected: (AppFlowTab) -> Unit,
    separatorColor: Color
) {
    val selectedColor = CometChatTheme.colorScheme.iconTintHighlight
    val unselectedColor = CometChatTheme.colorScheme.iconTintSecondary
    val backgroundColor = CometChatTheme.colorScheme.backgroundColor1
    
    Column {
        // Separator line (1dp)
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = separatorColor
        )
        
        NavigationBar(
            containerColor = backgroundColor
        ) {
            AppFlowTab.entries.forEach { tab ->
                // No ripple interaction source
                val interactionSource = remember { MutableInteractionSource() }
                
                NavigationBarItem(
                    selected = selectedTab == tab,
                    onClick = { onTabSelected(tab) },
                    icon = {
                        Icon(
                            painter = painterResource(id = tab.iconRes),
                            contentDescription = tab.label,
                            modifier = Modifier.size(32.dp) // Match XML itemIconSize="32dp"
                        )
                    },
                    label = {
                        Text(
                            text = tab.label,
                            style = if (selectedTab == tab) {
                                CometChatTheme.typography.caption1Medium
                            } else {
                                CometChatTheme.typography.caption1Regular
                            }
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = selectedColor,
                        selectedTextColor = selectedColor,
                        unselectedIconColor = unselectedColor,
                        unselectedTextColor = unselectedColor,
                        indicatorColor = backgroundColor // Invisible indicator
                    ),
                    interactionSource = interactionSource
                )
            }
        }
    }
}

/**
 * Icon resource and label for each tab.
 * Uses custom icons matching master-app-kotlin2 (32dp icons).
 */
private val AppFlowTab.iconRes: Int
    get() = when (this) {
        AppFlowTab.Chats -> R.drawable.ic_chats
        AppFlowTab.Calls -> R.drawable.ic_calls
        AppFlowTab.Users -> R.drawable.ic_users
        AppFlowTab.Groups -> R.drawable.ic_bottom_bar_groups
        AppFlowTab.Notifications -> R.drawable.ic_notifications
    }

private val AppFlowTab.label: String
    get() = when (this) {
        AppFlowTab.Chats -> "Chats"
        AppFlowTab.Calls -> "Calls"
        AppFlowTab.Users -> "Users"
        AppFlowTab.Groups -> "Groups"
        AppFlowTab.Notifications -> "Notifications"
    }
