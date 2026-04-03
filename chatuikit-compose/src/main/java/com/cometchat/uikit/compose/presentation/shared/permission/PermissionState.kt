package com.cometchat.uikit.compose.presentation.shared.permission

import android.content.Intent
import androidx.compose.runtime.Stable

/**
 * Represents the state of a single permission.
 *
 * @property permission The permission identifier (e.g., android.Manifest.permission.CAMERA)
 * @property hasPermission Whether the permission is currently granted
 * @property shouldShowRationale Whether we should show UI with rationale for requesting permission
 */
@Stable
data class PermissionState(
    val permission: String,
    val hasPermission: Boolean,
    val shouldShowRationale: Boolean
)

/**
 * Represents the state of multiple permissions.
 *
 * @property permissions List of individual permission states
 * @property allPermissionsGranted Whether all permissions in the list are granted
 * @property shouldShowRationale Whether at least one permission requires showing rationale
 */
@Stable
data class MultiplePermissionsState(
    val permissions: List<PermissionState>,
    val allPermissionsGranted: Boolean,
    val shouldShowRationale: Boolean
) {
    /**
     * List of permissions that are granted
     */
    val grantedPermissions: List<String>
        get() = permissions.filter { it.hasPermission }.map { it.permission }
    
    /**
     * List of permissions that are denied
     */
    val deniedPermissions: List<String>
        get() = permissions.filter { !it.hasPermission }.map { it.permission }
}

/**
 * Result of an activity launch.
 *
 * @property resultCode The result code from the activity
 * @property data The intent data returned from the activity
 */
@Stable
data class ActivityResult(
    val resultCode: Int,
    val data: Intent?
)
