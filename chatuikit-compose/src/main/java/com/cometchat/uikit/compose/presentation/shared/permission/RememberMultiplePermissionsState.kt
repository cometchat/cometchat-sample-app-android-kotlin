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
 * Creates and remembers a state for managing multiple permissions.
 * This composable manages the state of all permissions and provides a launcher to request them.
 *
 * @param permissions The list of permissions to manage
 * @param onPermissionsResult Callback invoked when permission results are available
 * @return A [ManagedMultiplePermissionsState] object that can be used to check and request permissions
 *
 * @sample
 * ```
 * val permissionsState = rememberMultiplePermissionsState(
 *     permissions = listOf(
 *         Manifest.permission.CAMERA,
 *         Manifest.permission.RECORD_AUDIO
 *     ),
 *     onPermissionsResult = { granted, denied ->
 *         if (granted.size == 2) {
 *             // All permissions granted
 *         } else {
 *             // Some permissions denied
 *         }
 *     }
 * )
 *
 * Button(onClick = { permissionsState.launchPermissionsRequest() }) {
 *     Text("Request Permissions")
 * }
 *
 * if (permissionsState.allPermissionsGranted) {
 *     Text("All permissions granted!")
 * }
 * ```
 */
@Composable
fun rememberMultiplePermissionsState(
    permissions: List<String>,
    onPermissionsResult: ((grantedPermissions: List<String>, deniedPermissions: List<String>) -> Unit)? = null
): ManagedMultiplePermissionsState {
    val context = LocalContext.current
    
    var multiplePermissionsState by remember(permissions) {
        mutableStateOf(createMultiplePermissionsState(context, permissions))
    }
    
    // Use rememberUpdatedState to ensure the callback always has the latest reference
    val currentOnPermissionsResult by rememberUpdatedState(onPermissionsResult)
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val updatedState = createMultiplePermissionsState(context, permissions)
        multiplePermissionsState = updatedState
        
        currentOnPermissionsResult?.invoke(
            updatedState.grantedPermissions,
            updatedState.deniedPermissions
        )
    }
    
    return remember(multiplePermissionsState, launcher, permissions) {
        ManagedMultiplePermissionsState(
            state = multiplePermissionsState,
            launchRequest = { launcher.launch(permissions.toTypedArray()) }
        )
    }
}

/**
 * Wrapper class that provides multiple permissions state and a method to launch permission requests.
 *
 * @property state The current multiple permissions state
 * @property launchRequest Function to launch the permissions request
 */
@Stable
class ManagedMultiplePermissionsState(
    val state: MultiplePermissionsState,
    private val launchRequest: () -> Unit
) {
    /**
     * List of individual permission states
     */
    val permissions: List<PermissionState> get() = state.permissions
    
    /**
     * Whether all permissions are currently granted
     */
    val allPermissionsGranted: Boolean get() = state.allPermissionsGranted
    
    /**
     * Whether at least one permission requires showing rationale
     */
    val shouldShowRationale: Boolean get() = state.shouldShowRationale
    
    /**
     * List of granted permissions
     */
    val grantedPermissions: List<String> get() = state.grantedPermissions
    
    /**
     * List of denied permissions
     */
    val deniedPermissions: List<String> get() = state.deniedPermissions
    
    /**
     * Launches the permissions request dialog
     */
    fun launchPermissionsRequest() {
        launchRequest()
    }
}

/**
 * Creates a MultiplePermissionsState from a list of permissions.
 *
 * @param context The context to use for checking permissions
 * @param permissions The list of permissions to check
 * @return A MultiplePermissionsState representing the current state
 */
private fun createMultiplePermissionsState(
    context: Context,
    permissions: List<String>
): MultiplePermissionsState {
    val permissionStates = permissions.map { permission ->
        PermissionState(
            permission = permission,
            hasPermission = checkPermission(context, permission),
            shouldShowRationale = shouldShowRationale(context, permission)
        )
    }
    
    return MultiplePermissionsState(
        permissions = permissionStates,
        allPermissionsGranted = permissionStates.all { it.hasPermission },
        shouldShowRationale = permissionStates.any { it.shouldShowRationale }
    )
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
