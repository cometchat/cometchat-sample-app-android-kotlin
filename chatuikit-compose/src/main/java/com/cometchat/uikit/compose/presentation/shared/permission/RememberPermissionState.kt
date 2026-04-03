package com.cometchat.uikit.compose.presentation.shared.permission

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Creates and remembers a permission state for a single permission.
 * This composable manages the permission state and provides a launcher to request the permission.
 *
 * @param permission The permission to manage (e.g., android.Manifest.permission.CAMERA)
 * @param onPermissionResult Callback invoked when permission result is available
 * @return A [ManagedPermissionState] object that can be used to check and request permission
 *
 * @sample
 * ```
 * val cameraPermissionState = rememberPermissionState(
 *     permission = Manifest.permission.CAMERA,
 *     onPermissionResult = { granted ->
 *         if (granted) {
 *             // Permission granted, proceed with camera
 *         } else {
 *             // Permission denied, show message
 *         }
 *     }
 * )
 *
 * Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
 *     Text("Request Camera Permission")
 * }
 * ```
 */
@Composable
fun rememberPermissionState(
    permission: String,
    onPermissionResult: ((Boolean) -> Unit)? = null
): ManagedPermissionState {
    val context = LocalContext.current
    
    var permissionState by remember(permission) {
        mutableStateOf(
            PermissionState(
                permission = permission,
                hasPermission = checkPermission(context, permission),
                shouldShowRationale = shouldShowRationale(context, permission)
            )
        )
    }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionState = PermissionState(
            permission = permission,
            hasPermission = isGranted,
            shouldShowRationale = shouldShowRationale(context, permission)
        )
        onPermissionResult?.invoke(isGranted)
    }
    
    return remember(permissionState, launcher) {
        ManagedPermissionState(
            state = permissionState,
            launchRequest = { launcher.launch(permission) }
        )
    }
}

/**
 * Wrapper class that provides permission state and a method to launch permission request.
 *
 * @property state The current permission state
 * @property launchRequest Function to launch the permission request
 */
@Stable
class ManagedPermissionState(
    val state: PermissionState,
    private val launchRequest: () -> Unit
) {
    /**
     * The permission identifier
     */
    val permission: String get() = state.permission
    
    /**
     * Whether the permission is currently granted
     */
    val hasPermission: Boolean get() = state.hasPermission
    
    /**
     * Whether we should show rationale for requesting this permission
     */
    val shouldShowRationale: Boolean get() = state.shouldShowRationale
    
    /**
     * Launches the permission request dialog
     */
    fun launchPermissionRequest() {
        launchRequest()
    }
}

/**
 * Checks if a permission is granted.
 *
 * @param context The context to use for checking permission
 * @param permission The permission to check
 * @return true if permission is granted, false otherwise
 */
private fun checkPermission(context: Context, permission: String): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        permission
    ) == PackageManager.PERMISSION_GRANTED
}

/**
 * Checks if we should show rationale for requesting a permission.
 *
 * @param context The context to use for checking
 * @param permission The permission to check
 * @return true if rationale should be shown, false otherwise
 */
private fun shouldShowRationale(context: Context, permission: String): Boolean {
    return if (context is ComponentActivity) {
        ActivityCompat.shouldShowRequestPermissionRationale(context, permission)
    } else {
        false
    }
}
