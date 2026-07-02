package com.cometchat.sampleapp.compose.push.appflow.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.events.CometChatGroupEvent
import com.cometchat.sampleapp.compose.push.R
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Dialog state for create group operation.
 */
private enum class CreateDialogState {
    IDLE,
    CREATING,
    ERROR
}

/**
 * Group type options for the segmented toggle.
 */
private enum class GroupTypeOption(val value: String, val label: String) {
    PUBLIC(CometChatConstants.GROUP_TYPE_PUBLIC, "Public"),
    PRIVATE(CometChatConstants.GROUP_TYPE_PRIVATE, "Private"),
    PASSWORD(CometChatConstants.GROUP_TYPE_PASSWORD, "Protected")
}

/**
 * Bottom sheet dialog for creating a new group.
 * Matches the reference implementation in master-app-kotlin.
 *
 * @param onDismiss Callback when dialog is dismissed
 * @param onGroupCreated Callback when group is created successfully
 * @param onError Callback when group creation fails
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupDialog(
    onDismiss: () -> Unit,
    onGroupCreated: (Group) -> Unit,
    onError: (CometChatException) -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedGroupType by remember { mutableStateOf(GroupTypeOption.PUBLIC) }
    var dialogState by remember { mutableStateOf(CreateDialogState.IDLE) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    
    val backgroundColor1 = CometChatTheme.colorScheme.backgroundColor1
    val backgroundColor2 = CometChatTheme.colorScheme.backgroundColor2
    val backgroundColor3 = CometChatTheme.colorScheme.backgroundColor3
    val textColorPrimary = CometChatTheme.colorScheme.textColorPrimary
    val textColorSecondary = CometChatTheme.colorScheme.textColorSecondary
    val textColorTertiary = CometChatTheme.colorScheme.textColorTertiary
    val primaryColor = CometChatTheme.colorScheme.primary
    val errorColor = CometChatTheme.colorScheme.errorColor
    val strokeColorLight = CometChatTheme.colorScheme.strokeColorLight
    val neutralColor500 = CometChatTheme.colorScheme.neutralColor500
    val iconTintHighlight = CometChatTheme.colorScheme.iconTintHighlight
    
    ModalBottomSheet(
        onDismissRequest = {
            if (dialogState != CreateDialogState.CREATING) {
                onDismiss()
            }
        },
        sheetState = sheetState,
        containerColor = backgroundColor1,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 0.dp)
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .width(32.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(neutralColor500)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Circular group icon
            Card(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = backgroundColor2),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_group),
                        contentDescription = "Group Icon",
                        modifier = Modifier.size(48.dp),
                        tint = iconTintHighlight
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Title
            Text(
                text = "New Group",
                style = CometChatTheme.typography.heading2Medium,
                color = textColorPrimary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Group Type label
            Text(
                text = "Group Type",
                style = CometChatTheme.typography.caption1Medium,
                color = textColorPrimary,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(4.dp))

            // Segmented toggle for group type
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp),
                shape = RoundedCornerShape(5.dp),
                colors = CardDefaults.cardColors(containerColor = backgroundColor3),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    GroupTypeOption.entries.forEach { option ->
                        val isSelected = selectedGroupType == option
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(26.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(if (isSelected) backgroundColor1 else Color.Transparent)
                                .clickable(enabled = dialogState != CreateDialogState.CREATING) {
                                    selectedGroupType = option
                                    if (option != GroupTypeOption.PASSWORD) {
                                        password = ""
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = option.label,
                                style = CometChatTheme.typography.bodyRegular,
                                color = if (isSelected) primaryColor else textColorSecondary
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Group Name label
            Text(
                text = "Name",
                style = CometChatTheme.typography.caption1Medium,
                color = textColorPrimary,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Group Name input
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = backgroundColor2),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, strokeColorLight)
            ) {
                BasicTextField(
                    value = groupName,
                    onValueChange = { 
                        groupName = it
                        if (dialogState == CreateDialogState.ERROR) {
                            dialogState = CreateDialogState.IDLE
                            errorMessage = null
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    textStyle = CometChatTheme.typography.bodyRegular.copy(color = textColorPrimary),
                    singleLine = true,
                    enabled = dialogState != CreateDialogState.CREATING,
                    decorationBox = { innerTextField ->
                        Box {
                            if (groupName.isEmpty()) {
                                Text(
                                    text = "Enter group name",
                                    style = CometChatTheme.typography.bodyRegular,
                                    color = textColorTertiary
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
            
            // Password field (only for password type)
            if (selectedGroupType == GroupTypeOption.PASSWORD) {
                Spacer(modifier = Modifier.height(20.dp))
                
                // Password label
                Text(
                    text = "Password",
                    style = CometChatTheme.typography.caption1Medium,
                    color = textColorPrimary,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Password input
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = backgroundColor2),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, strokeColorLight)
                ) {
                    BasicTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        textStyle = CometChatTheme.typography.bodyRegular.copy(color = textColorPrimary),
                        singleLine = true,
                        enabled = dialogState != CreateDialogState.CREATING,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        decorationBox = { innerTextField ->
                            Box {
                                if (password.isEmpty()) {
                                    Text(
                                        text = "Enter the password",
                                        style = CometChatTheme.typography.bodyRegular,
                                        color = textColorTertiary
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
            }
            
            // Error message
            if (dialogState == CreateDialogState.ERROR && errorMessage != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = errorMessage!!,
                    style = CometChatTheme.typography.caption1Regular,
                    color = errorColor,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Create button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = isFormValid(groupName, selectedGroupType, password) && 
                              dialogState != CreateDialogState.CREATING) {
                        if (isFormValid(groupName, selectedGroupType, password)) {
                            dialogState = CreateDialogState.CREATING
                            createGroup(
                                name = groupName,
                                groupType = selectedGroupType.value,
                                password = if (selectedGroupType == GroupTypeOption.PASSWORD) password else "",
                                onSuccess = { createdGroup ->
                                    dialogState = CreateDialogState.IDLE
                                    scope.launch {
                                        sheetState.hide()
                                        onGroupCreated(createdGroup)
                                    }
                                },
                                onError = { exception ->
                                    dialogState = CreateDialogState.ERROR
                                    errorMessage = exception.message ?: "Failed to create group"
                                    onError(exception)
                                }
                            )
                        }
                    },
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isFormValid(groupName, selectedGroupType, password) && 
                                        dialogState != CreateDialogState.CREATING) 
                        primaryColor else primaryColor.copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (dialogState == CreateDialogState.CREATING) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(15.dp),
                            color = CometChatTheme.colorScheme.iconTintSecondary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Create Group",
                            style = CometChatTheme.typography.caption1Medium,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

/**
 * Validates the form inputs.
 */
private fun isFormValid(
    groupName: String,
    groupType: GroupTypeOption,
    password: String
): Boolean {
    if (groupName.isBlank()) return false
    if (groupType == GroupTypeOption.PASSWORD && password.isBlank()) return false
    return true
}

/**
 * Creates a new group.
 */
private fun createGroup(
    name: String,
    groupType: String,
    password: String,
    onSuccess: (Group) -> Unit,
    onError: (CometChatException) -> Unit
) {
    val guid = UUID.randomUUID().toString()
    val group = Group(guid, name, groupType, password)
    
    CometChat.createGroup(group, object : CometChat.CallbackListener<Group>() {
        override fun onSuccess(createdGroup: Group?) {
            createdGroup?.let { 
                // Emit group created event so ConversationList can update
                CometChatEvents.emitGroupEvent(CometChatGroupEvent.GroupCreated(it))
                onSuccess(it) 
            }
        }
        
        override fun onError(exception: CometChatException?) {
            exception?.let { onError(it) }
        }
    })
}
