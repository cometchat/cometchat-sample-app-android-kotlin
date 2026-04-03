package com.cometchat.uikit.compose.presentation.shared.permission

import android.content.Intent

/**
 * Callback interface for handling permission request results.
 * Used to receive feedback when permissions are granted or denied.
 */
fun interface PermissionResultCallback {
    /**
     * Called when permission request is completed.
     *
     * @param grantedPermissions List of permissions that were granted
     * @param deniedPermissions List of permissions that were denied
     */
    fun onPermissionResult(
        grantedPermissions: List<String>,
        deniedPermissions: List<String>
    )
}

/**
 * Callback interface for handling activity result.
 * Used to receive feedback from launched activities.
 */
fun interface ActivityResultCallback {
    /**
     * Called when the launched activity returns a result.
     *
     * @param resultCode The result code returned by the activity
     * @param data The intent data returned by the activity, can be null
     */
    fun onActivityResult(
        resultCode: Int,
        data: Intent?
    )
}
