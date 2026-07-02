package com.cometchat.sampleapp.compose.push.appflow.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cometchat.chat.core.GroupsRequest
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.presentation.groups.ui.CometChatGroups
import com.cometchat.uikit.compose.presentation.users.ui.CometChatUsers
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.sampleapp.compose.push.appflow.components.AppFlowToolbar
import kotlinx.coroutines.launch

/**
 * New chat screen for starting a new conversation.
 * Contains tabs for Users and Groups with pill-shaped tab styling.
 * Styled to match master-app-kotlin2 activity_new_chat.xml.
 *
 * @param onBackPress Callback when back is pressed
 * @param onUserSelected Callback when a user is selected
 * @param onGroupSelected Callback when a group is selected
 */
@Composable
fun NewChatScreen(
    onBackPress: () -> Unit,
    onUserSelected: (User) -> Unit,
    onGroupSelected: (Group) -> Unit
) {
    val backgroundColor = CometChatTheme.colorScheme.backgroundColor1
    val backgroundColor3 = CometChatTheme.colorScheme.backgroundColor3
    val strokeColorLight = CometChatTheme.colorScheme.strokeColorLight
    val primaryColor = CometChatTheme.colorScheme.primary
    val textColorSecondary = CometChatTheme.colorScheme.textColorSecondary
    
    val tabs = listOf("Users", "Groups")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()
    
    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            AppFlowToolbar(
                title = "New Chat",
                onBackPress = onBackPress,
                titleStyle = CometChatTheme.typography.heading2Bold
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Pill-shaped tab container
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(50), // cometchat_radius_max
                colors = CardDefaults.cardColors(
                    containerColor = backgroundColor3
                ),
                border = androidx.compose.foundation.BorderStroke(2.dp, strokeColorLight)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    tabs.forEachIndexed { index, title ->
                        val isSelected = pagerState.currentPage == index
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (isSelected) backgroundColor else androidx.compose.ui.graphics.Color.Transparent
                                )
                                .clickable {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                style = CometChatTheme.typography.heading4Medium,
                                color = if (isSelected) primaryColor else textColorSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            
            // Tab content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> UsersTabContent(onUserSelected = onUserSelected)
                    1 -> GroupsTabContent(onGroupSelected = onGroupSelected)
                }
            }
        }
    }
}

/**
 * Users tab content with filtered users list.
 */
@Composable
private fun UsersTabContent(
    onUserSelected: (User) -> Unit
) {
    CometChatUsers(
        modifier = Modifier.fillMaxSize(),
        hideToolbar = true,
        hideSearchBox = false,
        onItemClick = { user ->
            onUserSelected(user)
        }
    )
}

/**
 * Groups tab content with joined groups only.
 */
@Composable
private fun GroupsTabContent(
    onGroupSelected: (Group) -> Unit
) {
    // Create request builder to show only joined groups
    val groupsRequestBuilder = GroupsRequest.GroupsRequestBuilder()
        .setLimit(30)
        .joinedOnly(true)
    
    CometChatGroups(
        modifier = Modifier.fillMaxSize(),
        hideToolbar = true,
        hideSearchBox = false,
        groupsRequestBuilder = groupsRequestBuilder,
        onItemClick = { group ->
            onGroupSelected(group)
        }
    )
}
