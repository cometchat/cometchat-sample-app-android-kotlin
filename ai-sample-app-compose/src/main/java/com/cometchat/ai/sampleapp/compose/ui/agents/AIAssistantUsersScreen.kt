package com.cometchat.ai.sampleapp.compose.ui.agents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cometchat.ai.sampleapp.compose.BuildConfig
import com.cometchat.ai.sampleapp.compose.R
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.UsersRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.CometChatAvatar
import com.cometchat.uikit.compose.presentation.users.ui.CometChatUsers
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.constants.UIKitConstants

/**
 * AI Assistant users screen — full-screen [CometChatUsers] filtered to agents
 * (role = [UIKitConstants.AIConstants.AGENTIC_USER]). Shows the logged-in
 * user's avatar in the toolbar with a dropdown (Profile, Logout, Version).
 */
@Composable
fun AIAssistantUsersScreen(
    onUserClick: (User) -> Unit,
    onLogout: () -> Unit
) {
    val colorScheme = CometChatTheme.colorScheme

    // Filter users by the agent role. The builder is applied via LaunchedEffect
    // inside CometChatUsers and the shared ViewModel now defers its initial
    // fetch to first attach, so only agent users are returned from the start.
    val requestBuilder = remember {
        UsersRequest.UsersRequestBuilder()
            .setRoles(listOf(UIKitConstants.AIConstants.AGENTIC_USER))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.backgroundColor1)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        CometChatUsers(
            modifier = Modifier.fillMaxSize(),
            title = stringResource(R.string.app_ai_agents),
            usersRequestBuilder = requestBuilder,
            searchRequestBuilder = requestBuilder,
            overflowMenu = { UserProfileOverflowMenu(onLogout = onLogout) },
            onItemClick = onUserClick
        )
    }
}

/**
 * Logged-in user's avatar that opens a small dropdown with profile name,
 * logout, and version info.
 */
@Composable
private fun UserProfileOverflowMenu(onLogout: () -> Unit) {
    val colorScheme = CometChatTheme.colorScheme
    val typography = CometChatTheme.typography

    val loggedInUser = remember {
        runCatching { CometChatUIKit.getLoggedInUser() }.getOrNull()
    } ?: return

    var expanded by remember { mutableStateOf(false) }

    Box {
        CometChatAvatar(
            modifier = Modifier
                .size(40.dp)
                .clickable { expanded = true },
            name = loggedInUser.name ?: "",
            avatarUrl = loggedInUser.avatar
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(250.dp)
                .background(colorScheme.backgroundColor1)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.backgroundColor1),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    RowItem(
                        iconResId = R.drawable.ic_user_profile,
                        iconTint = colorScheme.textColorPrimary,
                        text = loggedInUser.name ?: "",
                        textColor = colorScheme.textColorPrimary,
                        textStyle = typography.bodyRegular
                    ) {
                        expanded = false
                    }

                    RowItem(
                        iconResId = R.drawable.ic_logout,
                        iconTint = colorScheme.errorColor,
                        text = stringResource(R.string.app_logout),
                        textColor = colorScheme.errorColor,
                        textStyle = typography.bodyRegular
                    ) {
                        expanded = false
                        CometChat.logout(object : CometChat.CallbackListener<String>() {
                            override fun onSuccess(result: String?) {
                                onLogout()
                            }

                            override fun onError(exception: CometChatException?) {
                                onLogout()
                            }
                        })
                    }

                    Text(
                        text = "V${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})",
                        style = typography.caption1Regular,
                        color = colorScheme.textColorSecondary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RowItem(
    iconResId: Int,
    iconTint: androidx.compose.ui.graphics.Color,
    text: String,
    textColor: androidx.compose.ui.graphics.Color,
    textStyle: androidx.compose.ui.text.TextStyle,
    onClick: () -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = textStyle, color = textColor)
    }
}
