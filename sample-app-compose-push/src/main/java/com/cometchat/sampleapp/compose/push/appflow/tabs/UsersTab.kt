package com.cometchat.sampleapp.compose.push.appflow.tabs

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.presentation.users.ui.CometChatUsers

/**
 * Users tab displaying the users list.
 * Clicking on a user navigates to the messages screen.
 *
 * @param onUserClick Callback when a user is clicked
 * @param contentPadding Padding from the scaffold
 */
@Composable
fun UsersTab(
    onUserClick: (User) -> Unit,
    contentPadding: PaddingValues
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        CometChatUsers(
            modifier = Modifier.fillMaxSize(),
            title = "Users",
            hideBackIcon = true,
            onItemClick = { user ->
                onUserClick(user)
            },
            onError = { exception ->
                Log.e("UsersTab", "Error: ${exception.message}")
            }
        )
    }
}
