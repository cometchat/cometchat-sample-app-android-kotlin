package com.cometchat.sampleapp.compose.push.appflow.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.AvatarStyle
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.CometChatAvatar
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.events.CometChatGroupEvent

/**
 * Dialog state for join password group operation.
 */
private enum class JoinDialogState {
    IDLE,
    JOINING,
    ERROR
}

/**
 * Dialog for joining a password-protected group.
 *
 * @param group The group to join
 * @param onDismiss Callback when dialog is dismissed
 * @param onJoinSuccess Callback when join is successful
 * @param onJoinError Callback when join fails
 */
@Composable
fun JoinPasswordGroupDialog(
    group: Group,
    onDismiss: () -> Unit,
    onJoinSuccess: (Group) -> Unit,
    onJoinError: (CometChatException) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var dialogState by remember { mutableStateOf(JoinDialogState.IDLE) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val backgroundColor = CometChatTheme.colorScheme.backgroundColor1
    val textColorPrimary = CometChatTheme.colorScheme.textColorPrimary
    val textColorSecondary = CometChatTheme.colorScheme.textColorSecondary
    val primaryColor = CometChatTheme.colorScheme.primary
    val errorColor = CometChatTheme.colorScheme.errorColor
    
    Dialog(
        onDismissRequest = {
            if (dialogState != JoinDialogState.JOINING) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = dialogState != JoinDialogState.JOINING,
            dismissOnClickOutside = dialogState != JoinDialogState.JOINING
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Group avatar
                CometChatAvatar(
                    modifier = Modifier.size(80.dp),
                    name = group.name,
                    avatarUrl = group.icon,
                    style = AvatarStyle.default(cornerRadius = 40.dp)
                )
                
                // Group name
                Text(
                    text = group.name,
                    style = CometChatTheme.typography.heading2Bold,
                    color = textColorPrimary,
                    textAlign = TextAlign.Center
                )
                
                // Member count
                Text(
                    text = "${group.membersCount} members",
                    style = CometChatTheme.typography.caption1Regular,
                    color = textColorSecondary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Password field
                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        if (dialogState == JoinDialogState.ERROR) {
                            dialogState = JoinDialogState.IDLE
                            errorMessage = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Password") },
                    placeholder = { Text("Enter group password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    isError = dialogState == JoinDialogState.ERROR,
                    enabled = dialogState != JoinDialogState.JOINING,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = CometChatTheme.colorScheme.strokeColorDefault,
                        errorBorderColor = errorColor,
                        focusedLabelColor = primaryColor,
                        unfocusedLabelColor = textColorSecondary,
                        cursorColor = primaryColor
                    )
                )
                
                // Error message
                if (dialogState == JoinDialogState.ERROR && errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        style = CometChatTheme.typography.caption1Regular,
                        color = errorColor,
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Join button
                Button(
                    onClick = {
                        if (password.isNotEmpty()) {
                            dialogState = JoinDialogState.JOINING
                            joinGroup(
                                group = group,
                                password = password,
                                onSuccess = { joinedGroup ->
                                    dialogState = JoinDialogState.IDLE
                                    onJoinSuccess(joinedGroup)
                                },
                                onError = { exception ->
                                    dialogState = JoinDialogState.ERROR
                                    errorMessage = exception.message ?: "Failed to join group"
                                    onJoinError(exception)
                                }
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = password.isNotEmpty() && dialogState != JoinDialogState.JOINING,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        disabledContainerColor = primaryColor.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (dialogState == JoinDialogState.JOINING) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = CometChatTheme.colorScheme.primaryButtonIconTint,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Join",
                            style = CometChatTheme.typography.buttonBold,
                            color = CometChatTheme.colorScheme.primaryButtonIconTint
                        )
                    }
                }
            }
        }
    }
}

/**
 * Joins a password-protected group.
 */
private fun joinGroup(
    group: Group,
    password: String,
    onSuccess: (Group) -> Unit,
    onError: (CometChatException) -> Unit
) {
    CometChat.joinGroup(
        group.guid,
        group.groupType,
        password,
        object : CometChat.CallbackListener<Group>() {
            override fun onSuccess(joinedGroup: Group?) {
                joinedGroup?.let { 
                    // Emit member joined event so ConversationList can update
                    CometChatUIKit.getLoggedInUser()?.let { currentUser ->
                        CometChatEvents.emitGroupEvent(
                            CometChatGroupEvent.MemberJoined(currentUser, it)
                        )
                    }
                    onSuccess(it) 
                }
            }
            
            override fun onError(exception: CometChatException?) {
                exception?.let { onError(it) }
            }
        }
    )
}
