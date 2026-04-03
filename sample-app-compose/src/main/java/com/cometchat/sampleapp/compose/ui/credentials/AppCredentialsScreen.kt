package com.cometchat.sampleapp.compose.ui.credentials

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cometchat.sampleapp.compose.R
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * App Credentials screen composable for the CometChat Sample App.
 *
 * This screen allows users to configure their CometChat credentials for first-time setup.
 * It displays region selection cards and input fields for App ID and Auth Key.
 *
 * ## Features:
 * - Region selection cards (US, EU, IN) with visual highlighting
 * - App ID input field with validation
 * - Auth Key input field with validation
 * - Toast message for missing region selection
 * - Inline validation errors for empty fields
 * - SharedPreferences storage for credentials
 * - UIKit initialization with provided credentials
 * - Navigation to LoginScreen after successful setup
 *
 * ## Navigation Flow:
 * 1. User selects a region (US, EU, or IN)
 * 2. User enters App ID and Auth Key
 * 3. User taps Continue
 * 4. Credentials are validated and saved
 * 5. CometChat UIKit is initialized
 * 6. User is navigated to LoginScreen
 *
 * Validates: Requirements 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7
 *
 * @param viewModel The AppCredentialsViewModel instance
 * @param onCredentialsSaved Callback when credentials are saved and UIKit is initialized
 *
 * @see AppCredentialsViewModel
 */
@Composable
fun AppCredentialsScreen(
    viewModel: AppCredentialsViewModel = viewModel(),
    onCredentialsSaved: () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = CometChatTheme.colorScheme
    val typography = CometChatTheme.typography

    val state by viewModel.state.collectAsStateWithLifecycle()

    // Track if error dialog should be shown
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Handle navigation on successful credential save
    // Validates: Requirement 4.7
    LaunchedEffect(state.isCredentialsSaved) {
        if (state.isCredentialsSaved) {
            onCredentialsSaved()
        }
    }

    // Handle errors
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            errorMessage = error.message ?: context.getString(R.string.error_sdk_init_failed)
            showErrorDialog = true
            viewModel.clearError()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.backgroundColor1)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Logo - CometChat logo, centered
            Image(
                painter = painterResource(id = R.drawable.ic_cometchat_logo),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 20.dp)
            )

            // Title - "App Credentials"
            Text(
                text = stringResource(R.string.app_credentials_title),
                style = typography.heading2Bold,
                color = colorScheme.textColorPrimary,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 20.dp)
            )

            // Region Selection Label - matches Kotlin: marginTop 5dp from title
            Text(
                text = stringResource(R.string.app_select_region),
                style = typography.caption1Medium,
                color = colorScheme.textColorPrimary,
                modifier = Modifier.padding(top = 20.dp)
            )

            // Region Selection Cards - matches Kotlin layout
            // Validates: Requirements 4.1, 4.2
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RegionCard(
                    region = Region.US,
                    flagResId = R.drawable.ic_flag_us,
                    isSelected = state.selectedRegion == Region.US,
                    onClick = { viewModel.selectRegion(Region.US) },
                    modifier = Modifier.weight(1f)
                )
                RegionCard(
                    region = Region.EU,
                    flagResId = R.drawable.ic_flag_eu,
                    isSelected = state.selectedRegion == Region.EU,
                    onClick = { viewModel.selectRegion(Region.EU) },
                    modifier = Modifier.weight(1f)
                )
                RegionCard(
                    region = Region.IN,
                    flagResId = R.drawable.ic_flag_india,
                    isSelected = state.selectedRegion == Region.IN,
                    onClick = { viewModel.selectRegion(Region.IN) },
                    modifier = Modifier.weight(1f)
                )
            }

            // App ID Label
            Text(
                text = stringResource(R.string.app_app_id),
                style = typography.caption1Medium,
                color = colorScheme.textColorPrimary,
                modifier = Modifier.padding(top = 20.dp)
            )

            // App ID Input
            // Validates: Requirement 4.4
            TextField(
                value = state.appId,
                onValueChange = { viewModel.setAppId(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                placeholder = {
                    Text(
                        text = stringResource(R.string.app_enter_app_id),
                        color = colorScheme.textColorTertiary
                    )
                },
                textStyle = typography.bodyRegular,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colorScheme.backgroundColor2,
                    unfocusedContainerColor = colorScheme.backgroundColor2,
                    focusedTextColor = colorScheme.textColorPrimary,
                    unfocusedTextColor = colorScheme.textColorPrimary,
                    cursorColor = colorScheme.primary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    errorContainerColor = colorScheme.backgroundColor2,
                    errorIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                enabled = !state.isLoading,
                isError = state.appIdError != null,
                supportingText = state.appIdError?.let { error ->
                    {
                        Text(
                            text = error,
                            color = colorScheme.errorColor,
                            style = typography.caption1Regular
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Auth Key Label
            Text(
                text = stringResource(R.string.app_auth_key),
                style = typography.caption1Medium,
                color = colorScheme.textColorPrimary
            )

            // Auth Key Input
            // Validates: Requirement 4.5
            TextField(
                value = state.authKey,
                onValueChange = { viewModel.setAuthKey(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                placeholder = {
                    Text(
                        text = stringResource(R.string.app_enter_auth_key),
                        color = colorScheme.textColorTertiary
                    )
                },
                textStyle = typography.bodyRegular,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colorScheme.backgroundColor2,
                    unfocusedContainerColor = colorScheme.backgroundColor2,
                    focusedTextColor = colorScheme.textColorPrimary,
                    unfocusedTextColor = colorScheme.textColorPrimary,
                    cursorColor = colorScheme.primary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    errorContainerColor = colorScheme.backgroundColor2,
                    errorIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                enabled = !state.isLoading,
                isError = state.authKeyError != null,
                supportingText = state.authKeyError?.let { error ->
                    {
                        Text(
                            text = error,
                            color = colorScheme.errorColor,
                            style = typography.caption1Regular
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                )
            )

            // Spacer to push continue button to bottom
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(100.dp))

            // Continue Button
            Button(
                onClick = {
                    val errorMsg = viewModel.onContinueClick()
                    // Show toast for region selection error
                    // Validates: Requirement 4.3
                    if (errorMsg != null) {
                        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !state.isLoading,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primaryButtonBackgroundColor
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = colorScheme.colorWhite,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.app_continue),
                        style = typography.buttonMedium,
                        color = colorScheme.colorWhite
                    )
                }
            }
        }

        // Error Dialog
        if (showErrorDialog) {
            AlertDialog(
                onDismissRequest = { showErrorDialog = false },
                title = {
                    Text(
                        text = stringResource(R.string.error_title),
                        style = typography.heading4Bold,
                        color = colorScheme.textColorPrimary
                    )
                },
                text = {
                    Text(
                        text = errorMessage,
                        style = typography.bodyRegular,
                        color = colorScheme.textColorSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { showErrorDialog = false },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primaryButtonBackgroundColor
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.app_ok),
                            style = typography.buttonMedium,
                            color = colorScheme.colorWhite
                        )
                    }
                },
                containerColor = colorScheme.backgroundColor1,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

/**
 * Region selection card composable.
 *
 * Displays a selectable card for a CometChat region with flag icon and region code,
 * matching the sample-app-kotlin UI layout.
 *
 * Validates: Requirements 4.1, 4.2
 *
 * @param region The region this card represents
 * @param flagResId The drawable resource ID for the flag icon
 * @param isSelected Whether this region is currently selected
 * @param onClick Callback when the card is clicked
 * @param modifier Modifier for the card
 */
@Composable
private fun RegionCard(
    region: Region,
    flagResId: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = CometChatTheme.colorScheme
    val typography = CometChatTheme.typography

    val backgroundColor = if (isSelected) {
        colorScheme.primary.copy(alpha = 0.1f)
    } else {
        colorScheme.backgroundColor1
    }

    val borderColor = if (isSelected) {
        colorScheme.primary
    } else {
        colorScheme.strokeColorDefault
    }

    val textColor = if (isSelected) {
        colorScheme.primary
    } else {
        colorScheme.textColorSecondary
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = flagResId),
                contentDescription = region.displayName,
                modifier = Modifier.size(20.dp, 15.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = region.code.uppercase(),
                style = typography.buttonMedium,
                color = textColor
            )
        }
    }
}
