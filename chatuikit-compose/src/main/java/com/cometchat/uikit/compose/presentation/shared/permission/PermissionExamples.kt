package com.cometchat.uikit.compose.presentation.shared.permission

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Example composables demonstrating how to use the CometChat permission utilities.
 * These examples show the Jetpack Compose equivalent of the Java CometChatPermissionHandler.
 * Example: Single permission request (Camera)
 *
 * Java equivalent:
 * ```
 * CometChatPermissionHandler.withContext(context)
 *     .withPermissions(new String[]{Manifest.permission.CAMERA})
 *     .withListener((granted, denied) -> {
 *         // Handle result
 *     })
 *     .check();
 * ```
 **/
@Composable
fun ExampleSinglePermission() {
    val cameraPermissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA,
        onPermissionResult = { granted ->
            if (granted) {
                // Permission granted - open camera
            } else {
                // Permission denied - show message
            }
        }
    )

    Column(modifier = Modifier.padding(16.dp)) {
        if (cameraPermissionState.hasPermission) {
            Text("Camera permission granted!")
            // Show camera UI
        } else {
            if (cameraPermissionState.shouldShowRationale) {
                Text("Camera permission is required to take photos")
            }

            Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                Text("Request Camera Permission")
            }
        }
    }
}

/**
 * Example: Multiple permissions request (Camera + Audio)
 *
 * Java equivalent:
 * ```
 * CometChatPermissionHandler.withContext(context)
 *     .withPermissions(new String[]{
 *         Manifest.permission.CAMERA,
 *         Manifest.permission.RECORD_AUDIO
 *     })
 *     .withListener((granted, denied) -> {
 *         if (denied.isEmpty()) {
 *             // All permissions granted
 *         }
 *     })
 *     .check();
 * ```
 */
@Composable
fun ExampleMultiplePermissions() {
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ),
        onPermissionsResult = { granted, denied ->
            if (denied.isEmpty()) {
                // All permissions granted - start video recording
            } else {
                // Some permissions denied
            }
        }
    )

    Column(modifier = Modifier.padding(16.dp)) {
        if (permissionsState.allPermissionsGranted) {
            Text("All permissions granted!")
            // Show video recording UI
        } else {
            if (permissionsState.shouldShowRationale) {
                Text("Camera and microphone permissions are required for video calls")
            }

            Button(onClick = { permissionsState.launchPermissionsRequest() }) {
                Text("Request Permissions")
            }

            // Show individual permission states
            permissionsState.permissions.forEach { permission ->
                Text(
                    text = "${permission.permission}: ${if (permission.hasPermission) "✓" else "✗"}",
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}