package com.cometchat.sampleapp.compose.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cometchat.sampleapp.compose.R
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Splash screen composable for the CometChat Sample App.
 *
 * This screen serves as the entry point and handles:
 * - SDK initialization
 * - Credential checking
 * - Auto-login for returning users
 * - Navigation routing based on app state
 *
 * ## Navigation Flow:
 * 1. App launches → SplashScreen displays logo and loading indicator
 * 2. Check SharedPreferences for stored App ID
 *    - No App ID → Navigate to AppCredentialsScreen
 * 3. Initialize CometChat SDK with stored credentials
 *    - SDK init fails → Show error dialog with retry option
 * 4. Check if user is logged in
 *    - User logged in → Navigate to HomeScreen
 *    - No user → Navigate to LoginScreen
 *
 * ## Architecture:
 * - Uses [SplashViewModel] with StateFlow for state management
 * - Follows MVVM pattern with unidirectional data flow
 * - Uses CometChatTheme for all styling
 *
 * Validates: Requirements 1.1, 1.2, 1.3, 1.4, 1.5, 2.1, 2.2, 2.3, 2.4, 3.1, 3.2, 3.3, 3.4
 *
 * @param viewModel The SplashViewModel instance
 * @param onNavigateToAppCredentials Callback when navigating to AppCredentialsScreen
 * @param onNavigateToLogin Callback when navigating to LoginScreen
 * @param onNavigateToHome Callback when navigating to HomeScreen
 *
 * @see SplashViewModel
 */
@Composable
fun SplashScreen(
    viewModel: SplashViewModel = viewModel(),
    onNavigateToAppCredentials: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = CometChatTheme.colorScheme
    val typography = CometChatTheme.typography

    val state by viewModel.state.collectAsStateWithLifecycle()

    // Track if error dialog should be shown
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Handle navigation based on state
    LaunchedEffect(state.navigation) {
        state.navigation?.let { navigation ->
            when (navigation) {
                is SplashNavigation.ToAppCredentials -> onNavigateToAppCredentials()
                is SplashNavigation.ToLogin -> onNavigateToLogin()
                is SplashNavigation.ToHome -> onNavigateToHome()
            }
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
        // Centered content with logo and loading indicator
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo
            // Validates: Requirement 1.1
            Image(
                painter = painterResource(id = R.drawable.ic_cometchat_logo),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier.size(width = 200.dp, height = 40.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))
        }

        // Error Dialog
        // Validates: Requirement 1.4
        if (showErrorDialog) {
            AlertDialog(
                onDismissRequest = { /* Non-dismissible */ },
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
                        onClick = {
                            showErrorDialog = false
                            viewModel.retry()
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primaryButtonBackgroundColor
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.retry),
                            style = typography.buttonMedium,
                            color = colorScheme.colorWhite
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showErrorDialog = false
                            onNavigateToAppCredentials()
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.cancel),
                            style = typography.buttonMedium,
                            color = colorScheme.textColorSecondary
                        )
                    }
                },
                containerColor = colorScheme.backgroundColor1,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}
