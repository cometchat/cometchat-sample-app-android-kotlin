package com.cometchat.sampleapp.compose.ui.login

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.sampleapp.compose.R

/**
 * Login screen composable for the CometChat Sample App.
 * UI parity with master-app-jetpack LoginScreen.
 *
 * Features:
 * - Displays sample users in a 3-column grid (LazyVerticalGrid)
 * - Supports single-select with toggle behavior
 * - Manual UID input clears sample user selection
 * - Shows/hides grid and divider based on API response
 * - Navigates to Home on successful login
 * - Auto-login check: redirects to Home if already logged in
 * - Uses CometChatTheme for all styling
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: () -> Unit,
    onChangeAppCredentials: () -> Unit = {}
) {
    val context = LocalContext.current
    val colorScheme = CometChatTheme.colorScheme
    val typography = CometChatTheme.typography

    val loginState by viewModel.loginState.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()
    val selectedUser by viewModel.selectedUser.collectAsStateWithLifecycle()
    val manualUid by viewModel.manualUid.collectAsStateWithLifecycle()

    // Track manual UID input locally for TextField
    var manualUidText by remember { mutableStateOf("") }

    // Auto-login check: if user is already logged in, navigate to Home
    // Note: Only check if SDK is initialized to avoid crash
    LaunchedEffect(Unit) {
        try {
            if (CometChatUIKit.getLoggedInUser() != null) {
                onLoginSuccess()
            }
        } catch (e: Exception) {
            // SDK not initialized yet, continue to show login screen
        }
    }

    // Handle navigation on successful login
    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginState.Success -> onLoginSuccess()
            is LoginState.Error -> {
                Toast.makeText(context, (loginState as LoginState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    // Sync manual UID text with state (when selection clears it)
    LaunchedEffect(manualUid) {
        if (manualUid != manualUidText) {
            manualUidText = manualUid
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.backgroundColor1)
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Logo - CometChat logo, centered, marginTop: 20dp
        Image(
            painter = painterResource(id = R.drawable.ic_cometchat_logo),
            contentDescription = "CometChat Logo",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 20.dp)
        )

        // Title - "Login", Heading2.Bold, centered
        Text(
            text = stringResource(R.string.app_login),
            style = typography.heading2Bold,
            color = colorScheme.textColorPrimary,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 20.dp)
        )

        // Show sample users section only if users are available
        if (users.isNotEmpty()) {
            // Subtitle - "Select a sample user or enter UID below", Body.Medium
            Text(
                text = stringResource(R.string.app_choose_sample_user),
                style = typography.bodyMedium,
                color = colorScheme.textColorPrimary,
                modifier = Modifier.padding(top = 5.dp)
            )

            // User Grid - LazyVerticalGrid, 3 columns
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(calculateGridHeight(users.size))
                    .padding(top = 4.dp),
                userScrollEnabled = false
            ) {
                items(users, key = { it.uid }) { user ->
                    SampleUserItem(
                        user = user,
                        isSelected = selectedUser?.uid == user.uid,
                        onClick = { viewModel.selectUser(user) }
                    )
                }
            }

            // OR Divider - Horizontal line + "OR" text + horizontal line
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = 1.dp,
                    color = colorScheme.strokeColorDefault
                )
                Text(
                    text = stringResource(R.string.app_or),
                    style = typography.bodyMedium,
                    color = colorScheme.textColorTertiary,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = 1.dp,
                    color = colorScheme.strokeColorDefault
                )
            }
        }

        // UID Label - "Enter your UID", Caption1Medium
        Text(
            text = stringResource(R.string.app_enter_your_uid),
            style = typography.caption1Medium,
            color = colorScheme.textColorPrimary,
            modifier = Modifier.padding(top = 20.dp)
        )

        // UID Input - Rounded background, hint: "Enter UID"
        // Matches XML: wrap_content height with padding: 8dp (cometchat_padding_2)
        BasicTextField(
            value = manualUidText,
            onValueChange = { newValue ->
                manualUidText = newValue
                viewModel.setManualUid(newValue)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .background(
                    color = colorScheme.backgroundColor2,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(8.dp) // cometchat_padding_2 = 8dp
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        // On manual UID focus → call viewModel.onManualUidFocused()
                        viewModel.onManualUidFocused()
                    }
                },
            textStyle = typography.bodyRegular.copy(color = colorScheme.textColorPrimary),
            singleLine = true,
            enabled = loginState !is LoginState.Loading,
            cursorBrush = SolidColor(colorScheme.primary),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            decorationBox = { innerTextField ->
                Box {
                    if (manualUidText.isEmpty()) {
                        Text(
                            text = stringResource(R.string.app_enter_uid),
                            color = colorScheme.textColorTertiary,
                            style = typography.bodyRegular
                        )
                    }
                    innerTextField()
                }
            }
        )

        // Spacer to push continue button to bottom
        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(100.dp))

        // Continue Button - Full width, 50dp height, primary color
        // Matches XML: cometchat_50dp height, cometchat_radius_2 = 8dp corner radius
        Button(
            onClick = { viewModel.login() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = loginState !is LoginState.Loading &&
                    (selectedUser != null || manualUid.isNotBlank()),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.primaryButtonBackgroundColor,
                disabledContainerColor = colorScheme.primaryButtonBackgroundColor.copy(alpha = 0.5f),
                disabledContentColor = colorScheme.colorWhite.copy(alpha = 0.5f)
            )
        ) {
            Text(
                text = stringResource(R.string.app_continue),
                style = typography.buttonMedium,
                color = colorScheme.colorWhite
            )
        }

        // Change Credentials - "Change" + "App Credentials" (highlighted)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
                .clickable { onChangeAppCredentials() },
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.app_change),
                style = typography.bodyRegular,
                color = colorScheme.textColorSecondary
            )
            Text(
                text = " ",
                style = typography.bodyRegular
            )
            Text(
                text = stringResource(R.string.app_app_credentials),
                style = typography.bodyRegular,
                color = colorScheme.textColorHighlight
            )
        }
    }
}

/**
 * Calculates the height for the LazyVerticalGrid based on the number of items.
 * Each row is approximately 130dp (item height + padding).
 */
private fun calculateGridHeight(itemCount: Int): androidx.compose.ui.unit.Dp {
    val rows = (itemCount + 2) / 3 // Ceiling division for 3 columns
    return (rows * 130).dp
}
