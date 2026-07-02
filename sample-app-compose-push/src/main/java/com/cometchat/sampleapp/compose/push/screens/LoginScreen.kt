package com.cometchat.sampleapp.compose.push.screens

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.sampleapp.compose.push.R
import com.cometchat.sampleapp.compose.push.shared.LoginViewModel

/**
 * Login screen composable that allows users to authenticate via sample user selection
 * or manual UID entry.
 * 
 * Features:
 * - Displays sample users in a 3-column grid (LazyVerticalGrid)
 * - Supports single-select with toggle behavior
 * - Manual UID input clears sample user selection
 * - Shows/hides grid and divider based on API response
 * - Navigates to Home on successful login
 * - Navigates to AppCredentials on "Change App Credentials" click
 * 
 * Business Logic:
 * - Use viewModel() to get LoginViewModel instance
 * - Call loginViewModel.fetchSampleUsers() in LaunchedEffect
 * - Observe loginViewModel.state via collectAsState()
 * - On user tap → call viewModel.selectUser(user)
 * - On manual UID focus → call viewModel.onManualUidFocused()
 * - On Continue click → call viewModel.onContinueClick(), show error toast if returned
 * - On state.isLoggedIn = true → call onNavigateToHome()
 * - On state.error → show error toast
 * - If state.users is empty → hide grid and OR divider
 * - On "Change App Credentials" click → call onNavigateToAppCredentials()
 * 
 * Validates: Requirements 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 3.1, 3.2, 3.3, 4.1, 5.1, 5.2, 7.1, 7.2, 7.3, 7.4, 7.5
 * 
 * @param onNavigateToHome Callback to navigate to Home screen
 * @param onNavigateToAppCredentials Callback to navigate to AppCredentials screen
 * @param loginViewModel The ViewModel for login screen logic
 */
@Composable
fun LoginScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToAppCredentials: () -> Unit,
    loginViewModel: LoginViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by loginViewModel.state.collectAsState()
    val colorScheme = CometChatTheme.colorScheme
    val typography = CometChatTheme.typography
    
    // Track manual UID input locally for TextField
    var manualUidText by remember { mutableStateOf("") }
    
    // Fetch sample users on first composition
    LaunchedEffect(Unit) {
        loginViewModel.fetchSampleUsers()
    }
    
    // Handle navigation on successful login
    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) {
            onNavigateToHome()
        }
    }
    
    // Handle errors
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            Toast.makeText(context, error.message, Toast.LENGTH_LONG).show()
            loginViewModel.clearError()
        }
    }
    
    // Sync manual UID text with state (when selection clears it)
    LaunchedEffect(state.manualUid) {
        if (state.manualUid != manualUidText) {
            manualUidText = state.manualUid
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
        
        // Subtitle - "Select a sample user or enter UID below", Body.Medium
        Text(
            text = stringResource(R.string.app_choose_sample_user),
            style = typography.bodyMedium,
            color = colorScheme.textColorPrimary,
            modifier = Modifier.padding(top = 5.dp)
        )
        
        // User Grid - LazyVerticalGrid, 3 columns
        // Show/hide grid based on API response (if state.users is empty → hide grid)
        if (state.users.isNotEmpty()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(calculateGridHeight(state.users.size))
                    .padding(top = 4.dp),
                userScrollEnabled = false
            ) {
                items(state.users, key = { it.uid }) { user ->
                    SampleUserItem(
                        user = user,
                        isSelected = state.selectedUser?.uid == user.uid,
                        onClick = { loginViewModel.selectUser(user) }
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
        TextField(
            value = manualUidText,
            onValueChange = { newValue ->
                manualUidText = newValue
                loginViewModel.setManualUid(newValue)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        // On manual UID focus → call viewModel.onManualUidFocused()
                        loginViewModel.onManualUidFocused()
                    }
                },
            placeholder = {
                Text(
                    text = stringResource(R.string.app_enter_uid),
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
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            )
        )
        
        // Spacer to push continue button to bottom
        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(100.dp))
        
        // Continue Button - Full width, 50dp height, primary color
        Button(
            onClick = {
                val errorMessage = loginViewModel.onContinueClick()
                if (errorMessage != null) {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.primaryButtonBackgroundColor
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
                .clickable { onNavigateToAppCredentials() },
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
