package com.cometchat.sampleapp.compose.push.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.sampleapp.compose.push.R
import com.cometchat.sampleapp.compose.push.shared.SplashNavigationState
import com.cometchat.sampleapp.compose.push.shared.SplashViewModel

/**
 * Splash screen composable that displays the CometChat logo and handles
 * SDK initialization and navigation based on credentials and login state.
 * 
 * Business Logic:
 * - Uses viewModel() to get SplashViewModel instance
 * - Calls splashViewModel.checkCredentialsAndInitialize(context) in LaunchedEffect
 * - Observes splashViewModel.navigationState via collectAsState()
 * - Handles navigation states:
 *   - NavigateToAppCredentials → navigate to AppCredentials route
 *   - NavigateToLogin → navigate to Login route (show "Not logged in" toast)
 *   - NavigateToHome → navigate to Home route
 *   - Error → show error via Toast
 * 
 * Property 1: Splash Navigation Based on State
 * Validates: Requirements 1.1, 1.3, 1.7, 1.8
 * 
 * @param onNavigateToAppCredentials Callback to navigate to AppCredentials screen
 * @param onNavigateToLogin Callback to navigate to Login screen
 * @param onNavigateToHome Callback to navigate to Home screen
 * @param splashViewModel The ViewModel for splash screen logic
 */
@Composable
fun SplashScreen(
    onNavigateToAppCredentials: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    splashViewModel: SplashViewModel = viewModel()
) {
    val context = LocalContext.current
    val navigationState by splashViewModel.navigationState.collectAsState()
    
    // Check credentials and initialize SDK on first composition
    LaunchedEffect(Unit) {
        splashViewModel.checkCredentialsAndInitialize(context)
    }
    
    // Handle navigation based on state
    LaunchedEffect(navigationState) {
        when (val state = navigationState) {
            is SplashNavigationState.NavigateToAppCredentials -> {
                onNavigateToAppCredentials()
            }
            is SplashNavigationState.NavigateToLogin -> {
                Toast.makeText(context, "Not logged in", Toast.LENGTH_SHORT).show()
                onNavigateToLogin()
            }
            is SplashNavigationState.NavigateToHome -> {
                onNavigateToHome()
            }
            is SplashNavigationState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
            is SplashNavigationState.Loading -> {
                // Still loading, do nothing
            }
        }
    }
    
    // UI: CometChat logo centered with system bars padding
    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_cometchat_logo),
            contentDescription = "CometChat Logo"
        )
    }
}
