package com.cometchat.sampleapp.compose.ui.messages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.presentation.messagecomposer.ui.CometChatMessageComposer
import com.cometchat.uikit.compose.presentation.messagelist.ui.CometChatMessageList
import com.cometchat.uikit.compose.presentation.threadheader.ui.CometChatThreadHeader

/**
 * Thread messages screen composable for displaying thread replies.
 *
 * This screen displays thread replies using CometChat UI Kit components:
 * - CometChatThreadHeader: Shows the parent message
 * - CometChatMessageList: Displays thread replies
 * - CometChatMessageComposer: Allows composing thread replies
 *
 * ## Features:
 * - Display parent message context
 * - Real-time thread reply updates
 * - Support for all message types in replies
 *
 * ## Usage:
 * ```kotlin
 * ThreadMessagesScreen(
 *     parentMessageId = 12345,
 *     onBackPress = { navController.popBackStack() }
 * )
 * ```
 *
 * @param parentMessageId The ID of the parent message
 * @param onBackPress Callback when back button is pressed
 *
 * Validates: Requirements 6.9
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreadMessagesScreen(
    parentMessageId: Long,
    onBackPress: () -> Unit
) {
    // State for parent message and receiver
    var parentMessage by remember { mutableStateOf<BaseMessage?>(null) }
    var user by remember { mutableStateOf<User?>(null) }
    var group by remember { mutableStateOf<Group?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Load parent message
    LaunchedEffect(parentMessageId) {
        isLoading = true
        CometChat.getMessageDetails(parentMessageId, object : CometChat.CallbackListener<BaseMessage>() {
            override fun onSuccess(message: BaseMessage) {
                parentMessage = message
                // Load receiver based on message type
                when (message.receiverType) {
                    CometChatConstants.RECEIVER_TYPE_USER -> {
                        CometChat.getUser(message.receiverUid, object : CometChat.CallbackListener<User>() {
                            override fun onSuccess(fetchedUser: User) {
                                user = fetchedUser
                                isLoading = false
                            }

                            override fun onError(e: CometChatException) {
                                isLoading = false
                            }
                        })
                    }
                    CometChatConstants.RECEIVER_TYPE_GROUP -> {
                        CometChat.getGroup(message.receiverUid, object : CometChat.CallbackListener<Group>() {
                            override fun onSuccess(fetchedGroup: Group) {
                                group = fetchedGroup
                                isLoading = false
                            }

                            override fun onError(e: CometChatException) {
                                isLoading = false
                            }
                        })
                    }
                    else -> {
                        isLoading = false
                    }
                }
            }

            override fun onError(e: CometChatException) {
                isLoading = false
            }
        })
    }

    // Show content when data is loaded
    if (!isLoading && parentMessage != null && (user != null || group != null)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            // Top App Bar with back navigation
            TopAppBar(
                title = { Text("Thread") },
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )

            // Thread Header - shows parent message
            CometChatThreadHeader(
                modifier = Modifier.fillMaxWidth(),
                parentMessage = parentMessage!!
            )

            // Message List - displays thread replies
            CometChatMessageList(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                user = user,
                group = group,
                parentMessageId = parentMessageId.toLong(),
                // Enable real-time updates
                scrollToBottomOnNewMessage = true,
                // Hide thread option in thread view (no nested threads)
                hideReplyInThreadOption = true
            )

            // Message Composer - for composing thread replies
            CometChatMessageComposer(
                modifier = Modifier.fillMaxWidth(),
                user = user,
                group = group,
                parentMessageId = parentMessageId.toLong(),
                // Enable all attachment types
                hideAttachmentButton = false,
                hideVoiceRecordingButton = false,
                hideSendButton = false
            )
        }
    }
}
