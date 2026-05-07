package com.cometchat.ai.sampleapp.compose.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.cometchat.ai.sampleapp.compose.R
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Splash screen — handles SDK init + credentials + login-status routing.
 *
 * Shows the CometChat logo while work is in progress and presents an AlertDialog
 * on init failures (retry / cancel).
 */
@Composable
fun SplashScreen(
    viewModel: SplashViewModel = viewModel(),
    onNavigateToAppCredentials: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToAIUsers: () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = CometChatTheme.colorScheme
    val typography = CometChatTheme.typography

    val state by viewModel.state.collectAsStateWithLifecycle()

    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(state.navigation) {
        when (state.navigation) {
            SplashNavigation.ToAppCredentials -> onNavigateToAppCredentials()
            SplashNavigation.ToLogin -> onNavigateToLogin()
            SplashNavigation.ToAIAssistantUsers -> onNavigateToAIUsers()
            null -> {}
        }
    }

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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_cometchat_logo),
                contentDescription = stringResource(R.string.app_name)
            )
        }

        if (showErrorDialog) {
            AlertDialog(
                onDismissRequest = { /* non-dismissible */ },
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
                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primaryButtonBackgroundColor)
                    ) {
                        Text(
                            text = stringResource(R.string.retry),
                            style = typography.buttonMedium,
                            color = colorScheme.colorWhite
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showErrorDialog = false
                        onNavigateToAppCredentials()
                    }) {
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
