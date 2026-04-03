package com.cometchat.sampleapp.compose.ui.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cometchat.chat.models.Group
import com.cometchat.sampleapp.compose.R
import com.cometchat.uikit.compose.presentation.groups.ui.CometChatGroups
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.CometChatAvatar
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.constants.UIKitConstants.DialogState

/**
 * Groups screen composable for the Groups tab.
 *
 * This screen displays the list of groups using the
 * CometChatGroups component from the UIKit.
 *
 * ## Features:
 * - Displays all groups with avatars, names, and member counts
 * - Supports search functionality
 * - Shows group type indicators (public, private, password-protected)
 * - Handles group tap to navigate to messages or join group
 * - Create group functionality via overflow menu
 * - Join password-protected groups
 * - Shows loading and empty states
 *
 * @param onGroupClick Callback when a group is tapped
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    onGroupClick: (Group) -> Unit,
    viewModel: GroupsViewModel = viewModel()
) {
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var showJoinPasswordDialog by remember { mutableStateOf(false) }
    var selectedGroup by remember { mutableStateOf<Group?>(null) }

    // Handle joined group navigation
    LaunchedEffect(Unit) {
        viewModel.joinedGroup.collect { group ->
            onGroupClick(group)
        }
    }

    // Handle created group navigation
    LaunchedEffect(Unit) {
        viewModel.createdGroup.collect { group ->
            onGroupClick(group)
        }
    }

    // Close dialogs on success
    LaunchedEffect(dialogState) {
        if (dialogState == DialogState.SUCCESS) {
            showCreateGroupDialog = false
            showJoinPasswordDialog = false
            viewModel.resetDialogState()
        }
    }

    // Overflow menu for create group
    val overflowMenu: @Composable () -> Unit = {
        IconButton(
            onClick = { showCreateGroupDialog = true }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_create_group),
                contentDescription = "Create Group",
                tint = CometChatTheme.colorScheme.iconTintHighlight,
                modifier = Modifier.size(24.dp)
            )
        }
    }

    CometChatGroups(
        modifier = Modifier.fillMaxSize(),
        // Show toolbar with title (matching sample-app-kotlin)
        hideToolbar = false,
        // Hide back button since this is a tab
        hideBackIcon = true,
        hideSearchBox = false,
        hideGroupType = false,
        // Hide item separators (matching sample-app-kotlin)
        hideSeparator = true,
        overflowMenu = overflowMenu,
        onItemClick = { group ->
            if (group.isJoined) {
                onGroupClick(group)
            } else {
                when {
                    group.groupType.equals(UIKitConstants.GroupType.PUBLIC, ignoreCase = true) -> {
                        viewModel.joinPasswordGroup(group, "")
                    }
                    group.groupType.equals(UIKitConstants.GroupType.PASSWORD, ignoreCase = true) -> {
                        selectedGroup = group
                        showJoinPasswordDialog = true
                    }
                }
            }
        },
        onError = { /* Error handling done internally */ }
    )

    // Create Group Bottom Sheet
    if (showCreateGroupDialog) {
        CreateGroupBottomSheet(
            dialogState = dialogState,
            error = error,
            onDismiss = {
                showCreateGroupDialog = false
                viewModel.resetDialogState()
            },
            onCreateGroup = { group ->
                viewModel.createGroup(group)
            }
        )
    }

    // Join Password Group Bottom Sheet
    if (showJoinPasswordDialog && selectedGroup != null) {
        JoinPasswordGroupBottomSheet(
            group = selectedGroup!!,
            dialogState = dialogState,
            error = error,
            onDismiss = {
                showJoinPasswordDialog = false
                selectedGroup = null
                viewModel.resetDialogState()
            },
            onJoinGroup = { group, password ->
                viewModel.joinPasswordGroup(group, password)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateGroupBottomSheet(
    dialogState: DialogState?,
    error: String?,
    onDismiss: () -> Unit,
    onCreateGroup: (Group) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var groupName by remember { mutableStateOf("") }
    var groupPassword by remember { mutableStateOf("") }
    var selectedGroupType by remember { mutableStateOf(UIKitConstants.GroupType.PUBLIC) }

    val groupTypeOptions = listOf(
        UIKitConstants.GroupType.PUBLIC to stringResource(R.string.app_type_public),
        UIKitConstants.GroupType.PRIVATE to stringResource(R.string.app_type_private),
        UIKitConstants.GroupType.PASSWORD to stringResource(R.string.app_type_protected)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CometChatTheme.colorScheme.backgroundColor1,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .width(32.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(CometChatTheme.colorScheme.neutralColor500)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Group icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(CometChatTheme.colorScheme.backgroundColor2),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_group),
                    contentDescription = null,
                    tint = CometChatTheme.colorScheme.iconTintHighlight,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Text(
                text = stringResource(R.string.app_new_group),
                style = CometChatTheme.typography.heading2Medium,
                color = CometChatTheme.colorScheme.textColorPrimary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Group Type Label
            Text(
                text = stringResource(R.string.app_group_type),
                style = CometChatTheme.typography.caption1Medium,
                color = CometChatTheme.colorScheme.textColorPrimary,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Group Type Radio Buttons
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(5.dp),
                color = CometChatTheme.colorScheme.backgroundColor3,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    CometChatTheme.colorScheme.strokeColorLight
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .selectableGroup(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    groupTypeOptions.forEach { (type, label) ->
                        val isSelected = selectedGroupType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .padding(2.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(
                                    if (isSelected) CometChatTheme.colorScheme.backgroundColor1
                                    else CometChatTheme.colorScheme.backgroundColor3
                                )
                                .selectable(
                                    selected = isSelected,
                                    onClick = { selectedGroupType = type },
                                    role = Role.RadioButton
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = CometChatTheme.typography.bodyRegular,
                                color = if (isSelected) CometChatTheme.colorScheme.primary
                                else CometChatTheme.colorScheme.textColorSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Group Name Label
            Text(
                text = stringResource(R.string.app_group_name),
                style = CometChatTheme.typography.caption1Medium,
                color = CometChatTheme.colorScheme.textColorPrimary,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Group Name Input
            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                placeholder = {
                    Text(
                        text = stringResource(R.string.app_group_hint_name),
                        style = CometChatTheme.typography.bodyRegular,
                        color = CometChatTheme.colorScheme.textColorTertiary
                    )
                },
                textStyle = CometChatTheme.typography.bodyRegular,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CometChatTheme.colorScheme.backgroundColor2,
                    unfocusedContainerColor = CometChatTheme.colorScheme.backgroundColor2,
                    focusedBorderColor = CometChatTheme.colorScheme.strokeColorLight,
                    unfocusedBorderColor = CometChatTheme.colorScheme.strokeColorLight,
                    focusedTextColor = CometChatTheme.colorScheme.textColorPrimary,
                    unfocusedTextColor = CometChatTheme.colorScheme.textColorPrimary
                ),
                shape = RoundedCornerShape(8.dp)
            )

            // Password field (only for password-protected groups)
            if (selectedGroupType == UIKitConstants.GroupType.PASSWORD) {
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = stringResource(R.string.app_password),
                    style = CometChatTheme.typography.caption1Medium,
                    color = CometChatTheme.colorScheme.textColorPrimary,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = groupPassword,
                    onValueChange = { groupPassword = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.app_hint_enter_the_password),
                            style = CometChatTheme.typography.bodyRegular,
                            color = CometChatTheme.colorScheme.textColorTertiary
                        )
                    },
                    textStyle = CometChatTheme.typography.bodyRegular,
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CometChatTheme.colorScheme.backgroundColor2,
                        unfocusedContainerColor = CometChatTheme.colorScheme.backgroundColor2,
                        focusedBorderColor = CometChatTheme.colorScheme.strokeColorLight,
                        unfocusedBorderColor = CometChatTheme.colorScheme.strokeColorLight,
                        focusedTextColor = CometChatTheme.colorScheme.textColorPrimary,
                        unfocusedTextColor = CometChatTheme.colorScheme.textColorPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // Error message
            if (dialogState == DialogState.FAILURE && error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    style = CometChatTheme.typography.caption1Regular,
                    color = CometChatTheme.colorScheme.errorColor,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Create Button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clickable(enabled = dialogState != DialogState.INITIATED) {
                        val group = Group().apply {
                            guid = System.currentTimeMillis().toString()
                            name = groupName.trim()
                            setGroupType(selectedGroupType)
                            password = groupPassword.trim()
                        }
                        onCreateGroup(group)
                    },
                shape = RoundedCornerShape(8.dp),
                color = CometChatTheme.colorScheme.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (dialogState == DialogState.INITIATED) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(15.dp),
                            color = CometChatTheme.colorScheme.iconTintSecondary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.app_btn_create_group),
                            style = CometChatTheme.typography.caption1Medium,
                            color = CometChatTheme.colorScheme.colorWhite
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JoinPasswordGroupBottomSheet(
    group: Group,
    dialogState: DialogState?,
    error: String?,
    onDismiss: () -> Unit,
    onJoinGroup: (Group, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var password by remember { mutableStateOf("") }

    val memberText = if (group.membersCount > 1) {
        "${group.membersCount} ${stringResource(R.string.app_members)}"
    } else {
        "${group.membersCount} ${stringResource(R.string.app_member)}"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CometChatTheme.colorScheme.backgroundColor1,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .width(32.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(CometChatTheme.colorScheme.neutralColor500)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Text(
                text = stringResource(R.string.app_join_group),
                style = CometChatTheme.typography.heading2Medium,
                color = CometChatTheme.colorScheme.textColorPrimary
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Group Avatar
            CometChatAvatar(
                name = group.name,
                avatarUrl = group.icon,
                modifier = Modifier.size(60.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Group Name
            Text(
                text = group.name,
                style = CometChatTheme.typography.heading2Medium,
                color = CometChatTheme.colorScheme.textColorPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Member Count
            Text(
                text = memberText,
                style = CometChatTheme.typography.caption1Regular,
                color = CometChatTheme.colorScheme.textColorSecondary
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Password Label
            Text(
                text = stringResource(R.string.app_password),
                style = CometChatTheme.typography.caption1Medium,
                color = CometChatTheme.colorScheme.textColorPrimary,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Password Input
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                placeholder = {
                    Text(
                        text = stringResource(R.string.app_enter_the_password),
                        style = CometChatTheme.typography.bodyRegular,
                        color = CometChatTheme.colorScheme.textColorTertiary
                    )
                },
                textStyle = CometChatTheme.typography.bodyRegular,
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CometChatTheme.colorScheme.backgroundColor2,
                    unfocusedContainerColor = CometChatTheme.colorScheme.backgroundColor2,
                    focusedBorderColor = CometChatTheme.colorScheme.strokeColorLight,
                    unfocusedBorderColor = CometChatTheme.colorScheme.strokeColorLight,
                    focusedTextColor = CometChatTheme.colorScheme.textColorPrimary,
                    unfocusedTextColor = CometChatTheme.colorScheme.textColorPrimary
                ),
                shape = RoundedCornerShape(8.dp)
            )

            // Error message
            if (dialogState == DialogState.FAILURE && error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    style = CometChatTheme.typography.caption1Regular,
                    color = CometChatTheme.colorScheme.errorColor,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Join Button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clickable(enabled = dialogState != DialogState.INITIATED) {
                        onJoinGroup(group, password.trim())
                    },
                shape = RoundedCornerShape(8.dp),
                color = CometChatTheme.colorScheme.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (dialogState == DialogState.INITIATED) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(15.dp),
                            color = CometChatTheme.colorScheme.iconTintSecondary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.app_join_group),
                            style = CometChatTheme.typography.caption1Medium,
                            color = CometChatTheme.colorScheme.colorWhite
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
