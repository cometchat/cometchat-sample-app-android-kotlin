package com.cometchat.uikit.compose.shared.permission

import android.Manifest
import android.app.Activity
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.uikit.compose.presentation.shared.permission.ActivityResult
import com.cometchat.uikit.compose.presentation.shared.permission.MultiplePermissionsState
import com.cometchat.uikit.compose.presentation.shared.permission.PermissionState
import com.cometchat.uikit.compose.presentation.shared.permission.rememberActivityResultLauncher
import com.cometchat.uikit.compose.presentation.shared.permission.rememberMultiplePermissionsState
import com.cometchat.uikit.compose.presentation.shared.permission.rememberPermissionState
import com.cometchat.uikit.compose.theme.CometChatTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for permission handling utilities.
 *
 * Note: These tests focus on data classes and composable structure.
 * System permission dialogs cannot be tested directly in instrumented tests.
 * Use manual testing or UI Automator for complete permission flow testing.
 */
@RunWith(AndroidJUnit4::class)
class PermissionUtilsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testPermissionStateComposable_DisplaysButton() {
        composeTestRule.setContent {
            CometChatTheme {
                val permissionState = rememberPermissionState(
                    permission = Manifest.permission.CAMERA
                )

                Button(onClick = { permissionState.launchPermissionRequest() }) {
                    Text("Request Camera Permission")
                }
            }
        }

        composeTestRule.onNodeWithText("Request Camera Permission").assertIsDisplayed()
    }

    @Test
    fun testMultiplePermissionsStateComposable_DisplaysButton() {
        composeTestRule.setContent {
            CometChatTheme {
                val permissionsState = rememberMultiplePermissionsState(
                    permissions = listOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                    )
                )

                Button(onClick = { permissionsState.launchPermissionsRequest() }) {
                    Text("Request Permissions")
                }
            }
        }

        composeTestRule.onNodeWithText("Request Permissions").assertIsDisplayed()
    }

    @Test
    fun testPermissionStateData_Granted() {
        val permissionState = PermissionState(
            permission = Manifest.permission.CAMERA,
            hasPermission = true,
            shouldShowRationale = false
        )

        assert(permissionState.permission == Manifest.permission.CAMERA)
        assert(permissionState.hasPermission)
        assert(!permissionState.shouldShowRationale)
    }

    @Test
    fun testPermissionStateData_Denied() {
        val permissionState = PermissionState(
            permission = Manifest.permission.CAMERA,
            hasPermission = false,
            shouldShowRationale = true
        )

        assert(permissionState.permission == Manifest.permission.CAMERA)
        assert(!permissionState.hasPermission)
        assert(permissionState.shouldShowRationale)
    }

    @Test
    fun testMultiplePermissionsStateData_AllGranted() {
        val permissions = listOf(
            PermissionState(
                permission = Manifest.permission.CAMERA,
                hasPermission = true,
                shouldShowRationale = false
            ),
            PermissionState(
                permission = Manifest.permission.RECORD_AUDIO,
                hasPermission = true,
                shouldShowRationale = false
            )
        )

        val state = MultiplePermissionsState(
            permissions = permissions,
            allPermissionsGranted = true,
            shouldShowRationale = false
        )

        assert(state.grantedPermissions.size == 2)
        assert(state.deniedPermissions.isEmpty())
        assert(state.allPermissionsGranted)
        assert(!state.shouldShowRationale)
    }

    @Test
    fun testMultiplePermissionsStateData_PartiallyGranted() {
        val permissions = listOf(
            PermissionState(
                permission = Manifest.permission.CAMERA,
                hasPermission = true,
                shouldShowRationale = false
            ),
            PermissionState(
                permission = Manifest.permission.RECORD_AUDIO,
                hasPermission = false,
                shouldShowRationale = true
            )
        )

        val state = MultiplePermissionsState(
            permissions = permissions,
            allPermissionsGranted = false,
            shouldShowRationale = true
        )

        assert(state.grantedPermissions.size == 1)
        assert(state.deniedPermissions.size == 1)
        assert(state.grantedPermissions.contains(Manifest.permission.CAMERA))
        assert(state.deniedPermissions.contains(Manifest.permission.RECORD_AUDIO))
        assert(!state.allPermissionsGranted)
        assert(state.shouldShowRationale)
    }

    @Test
    fun testMultiplePermissionsStateData_AllDenied() {
        val permissions = listOf(
            PermissionState(
                permission = Manifest.permission.CAMERA,
                hasPermission = false,
                shouldShowRationale = true
            ),
            PermissionState(
                permission = Manifest.permission.RECORD_AUDIO,
                hasPermission = false,
                shouldShowRationale = true
            )
        )

        val state = MultiplePermissionsState(
            permissions = permissions,
            allPermissionsGranted = false,
            shouldShowRationale = true
        )

        assert(state.grantedPermissions.isEmpty())
        assert(state.deniedPermissions.size == 2)
        assert(!state.allPermissionsGranted)
        assert(state.shouldShowRationale)
    }

    @Test
    fun testActivityResultData_Success() {
        val intent = android.content.Intent().apply {
            putExtra("key", "value")
            putExtra("number", 42)
        }

        val result = ActivityResult(
            resultCode = Activity.RESULT_OK,
            data = intent
        )

        assert(result.resultCode == android.app.Activity.RESULT_OK)
        assert(result.data?.getStringExtra("key") == "value")
        assert(result.data?.getIntExtra("number", 0) == 42)
    }

    @Test
    fun testActivityResultData_Canceled() {
        val result = ActivityResult(
            resultCode = Activity.RESULT_CANCELED,
            data = null
        )

        assert(result.resultCode == android.app.Activity.RESULT_CANCELED)
        assert(result.data == null)
    }

    @Test
    fun testActivityResultData_CustomResultCode() {
        val customCode = 123
        val intent = android.content.Intent()

        val result = ActivityResult(
            resultCode = customCode,
            data = intent
        )

        assert(result.resultCode == customCode)
        assert(result.data != null)
    }

    @Test
    fun testActivityLauncherComposable_DisplaysButton() {
        composeTestRule.setContent {
            CometChatTheme {
                val launcher = rememberActivityResultLauncher { _, _ -> }

                androidx.compose.material3.Button(onClick = {
                    val intent = android.content.Intent(android.content.Intent.ACTION_PICK)
                    launcher.launch(intent)
                }) {
                    androidx.compose.material3.Text("Pick Image")
                }
            }
        }

        composeTestRule.onNodeWithText("Pick Image").assertIsDisplayed()
    }
}
