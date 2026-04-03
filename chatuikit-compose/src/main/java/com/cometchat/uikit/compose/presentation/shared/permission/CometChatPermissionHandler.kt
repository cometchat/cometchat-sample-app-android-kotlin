package com.cometchat.uikit.compose.presentation.shared.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Permission types supported by the CometChat permission handler.
 */
enum class PermissionType {
    CAMERA,
    MICROPHONE,
    STORAGE,
    CAMERA_AND_STORAGE,
    MICROPHONE_AND_STORAGE
}

/**
 * Gets the required permissions for a given permission type.
 *
 * @param type The permission type
 * @return List of Android permission strings
 */
fun getPermissionsForType(type: PermissionType): List<String> {
    return when (type) {
        PermissionType.CAMERA -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                listOf(Manifest.permission.CAMERA)
            } else {
                listOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        }
        PermissionType.MICROPHONE -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                listOf(Manifest.permission.RECORD_AUDIO)
            } else {
                listOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        }
        PermissionType.STORAGE -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                listOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            } else {
                listOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        }
        PermissionType.CAMERA_AND_STORAGE -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                listOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO
                )
            } else {
                listOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        }
        PermissionType.MICROPHONE_AND_STORAGE -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                listOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_MEDIA_AUDIO
                )
            } else {
                listOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        }
    }
}

/**
 * Gets the warning message for a given permission type.
 *
 * @param type The permission type
 * @return String resource ID for the warning message
 */
@Composable
fun getPermissionWarningMessage(type: PermissionType): String {
    return when (type) {
        PermissionType.CAMERA -> stringResource(R.string.cometchat_camera_permission_warning)
        PermissionType.MICROPHONE -> stringResource(R.string.cometchat_microphone_permission_warning)
        PermissionType.STORAGE -> stringResource(R.string.cometchat_storage_permission_warning)
        PermissionType.CAMERA_AND_STORAGE -> stringResource(R.string.cometchat_camera_permission_warning)
        PermissionType.MICROPHONE_AND_STORAGE -> stringResource(R.string.cometchat_microphone_permission_warning)
    }
}

/**
 * A composable that handles permission requests with a rationale dialog.
 *
 * Features:
 * - Requests permissions based on permission type
 * - Shows rationale dialog when permissions are denied
 * - Provides option to open app settings when permissions are permanently denied
 * - Integrates with CometChat theming system
 *
 * @param permissionType The type of permission to request
 * @param onPermissionGranted Callback when all permissions are granted
 * @param onPermissionDenied Callback when permissions are denied
 * @param content The content to display, receives a function to trigger permission request
 *
 * @sample
 * ```
 * CometChatPermissionHandler(
 *     permissionType = PermissionType.CAMERA,
 *     onPermissionGranted = { /* Open camera */ },
 *     onPermissionDenied = { /* Show error */ }
 * ) { requestPermission ->
 *     Button(onClick = { requestPermission() }) {
 *         Text("Take Photo")
 *     }
 * }
 * ```
 */
@Composable
fun CometChatPermissionHandler(
    permissionType: PermissionType,
    onPermissionGranted: () -> Unit,
    onPermissionDenied: (() -> Unit)? = null,
    content: @Composable (requestPermission: () -> Unit) -> Unit
) {
    val context = LocalContext.current
    val permissions = remember(permissionType) { getPermissionsForType(permissionType) }
    
    var showRationaleDialog by remember { mutableStateOf(false) }
    
    val permissionState = rememberMultiplePermissionsState(
        permissions = permissions,
        onPermissionsResult = { granted, denied ->
            if (denied.isEmpty()) {
                onPermissionGranted()
            } else {
                onPermissionDenied?.invoke()
            }
        }
    )
    
    // Content with permission request trigger
    content {
        if (permissionState.allPermissionsGranted) {
            onPermissionGranted()
        } else if (permissionState.shouldShowRationale) {
            showRationaleDialog = true
        } else {
            permissionState.launchPermissionsRequest()
        }
    }
    
    // Rationale dialog
    if (showRationaleDialog) {
        PermissionRationaleDialog(
            permissionType = permissionType,
            onDismiss = { 
                showRationaleDialog = false
                onPermissionDenied?.invoke()
            },
            onConfirm = {
                showRationaleDialog = false
                permissionState.launchPermissionsRequest()
            },
            onOpenSettings = {
                showRationaleDialog = false
                openAppSettings(context)
            }
        )
    }
}

/**
 * Dialog that explains why a permission is needed and provides options to grant or deny.
 *
 * @param permissionType The type of permission being requested
 * @param onDismiss Callback when the dialog is dismissed
 * @param onConfirm Callback when the user confirms to grant permission
 * @param onOpenSettings Callback when the user wants to open app settings
 */
@Composable
fun PermissionRationaleDialog(
    permissionType: PermissionType,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val warningMessage = getPermissionWarningMessage(permissionType)
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = CometChatTheme.colorScheme.backgroundColor1
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = stringResource(R.string.cometchat_permission_required),
                    style = CometChatTheme.typography.heading3Bold,
                    color = CometChatTheme.colorScheme.textColorPrimary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Message
                Text(
                    text = warningMessage,
                    style = CometChatTheme.typography.bodyRegular,
                    color = CometChatTheme.colorScheme.textColorSecondary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = stringResource(R.string.cometchat_cancel_button),
                            style = CometChatTheme.typography.bodyMedium,
                            color = CometChatTheme.colorScheme.textColorSecondary
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    TextButton(onClick = onOpenSettings) {
                        Text(
                            text = stringResource(R.string.cometchat_settings_button),
                            style = CometChatTheme.typography.bodyMedium,
                            color = CometChatTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Opens the app settings page where the user can manually grant permissions.
 *
 * @param context The context to use for launching the intent
 */
fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

/**
 * Checks if a specific permission is granted.
 *
 * @param context The context to use for checking permission
 * @param permission The permission to check
 * @return true if permission is granted, false otherwise
 */
fun isPermissionGranted(context: Context, permission: String): Boolean {
    return androidx.core.content.ContextCompat.checkSelfPermission(
        context,
        permission
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
}

/**
 * Checks if all permissions for a given type are granted.
 *
 * @param context The context to use for checking permissions
 * @param type The permission type to check
 * @return true if all permissions are granted, false otherwise
 */
fun arePermissionsGranted(context: Context, type: PermissionType): Boolean {
    return getPermissionsForType(type).all { isPermissionGranted(context, it) }
}
