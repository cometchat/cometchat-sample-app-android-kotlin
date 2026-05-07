package com.cometchat.ai.sampleapp.compose.ui.credentials

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
import com.cometchat.ai.sampleapp.compose.R
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * App Credentials screen — region selection, App ID + Auth Key inputs, Continue.
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

    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(state.isCredentialsSaved) {
        if (state.isCredentialsSaved) onCredentialsSaved()
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_cometchat_logo),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 20.dp)
            )

            Text(
                text = stringResource(R.string.app_credentials_title),
                style = typography.heading2Bold,
                color = colorScheme.textColorPrimary,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 20.dp)
            )

            Text(
                text = stringResource(R.string.app_select_region),
                style = typography.caption1Medium,
                color = colorScheme.textColorPrimary,
                modifier = Modifier.padding(top = 20.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RegionCard(Region.US, R.drawable.ic_flag_us, state.selectedRegion == Region.US, { viewModel.selectRegion(Region.US) }, Modifier.weight(1f))
                RegionCard(Region.EU, R.drawable.ic_flag_eu, state.selectedRegion == Region.EU, { viewModel.selectRegion(Region.EU) }, Modifier.weight(1f))
                RegionCard(Region.IN, R.drawable.ic_flag_india, state.selectedRegion == Region.IN, { viewModel.selectRegion(Region.IN) }, Modifier.weight(1f))
            }

            Text(
                text = stringResource(R.string.app_app_id),
                style = typography.caption1Medium,
                color = colorScheme.textColorPrimary,
                modifier = Modifier.padding(top = 20.dp)
            )

            TextField(
                value = state.appId,
                onValueChange = { viewModel.setAppId(it) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                placeholder = { Text(stringResource(R.string.app_enter_app_id), color = colorScheme.textColorTertiary) },
                textStyle = typography.bodyRegular,
                colors = textFieldColors(colorScheme),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                enabled = !state.isLoading,
                isError = state.appIdError != null,
                supportingText = state.appIdError?.let { err ->
                    { Text(err, color = colorScheme.errorColor, style = typography.caption1Regular) }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.app_auth_key),
                style = typography.caption1Medium,
                color = colorScheme.textColorPrimary
            )

            TextField(
                value = state.authKey,
                onValueChange = { viewModel.setAuthKey(it) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                placeholder = { Text(stringResource(R.string.app_enter_auth_key), color = colorScheme.textColorTertiary) },
                textStyle = typography.bodyRegular,
                colors = textFieldColors(colorScheme),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                enabled = !state.isLoading,
                isError = state.authKeyError != null,
                supportingText = state.authKeyError?.let { err ->
                    { Text(err, color = colorScheme.errorColor, style = typography.caption1Regular) }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done)
            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(100.dp))

            Button(
                onClick = {
                    val err = viewModel.onContinueClick()
                    if (err != null) Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !state.isLoading,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primaryButtonBackgroundColor)
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

        if (showErrorDialog) {
            AlertDialog(
                onDismissRequest = { showErrorDialog = false },
                title = { Text(stringResource(R.string.error_title), style = typography.heading4Bold, color = colorScheme.textColorPrimary) },
                text = { Text(errorMessage, style = typography.bodyRegular, color = colorScheme.textColorSecondary) },
                confirmButton = {
                    Button(
                        onClick = { showErrorDialog = false },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primaryButtonBackgroundColor)
                    ) {
                        Text(stringResource(R.string.app_ok), style = typography.buttonMedium, color = colorScheme.colorWhite)
                    }
                },
                containerColor = colorScheme.backgroundColor1,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
private fun textFieldColors(colorScheme: com.cometchat.uikit.compose.theme.CometChatColorScheme) =
    TextFieldDefaults.colors(
        focusedContainerColor = colorScheme.backgroundColor2,
        unfocusedContainerColor = colorScheme.backgroundColor2,
        focusedTextColor = colorScheme.textColorPrimary,
        unfocusedTextColor = colorScheme.textColorPrimary,
        cursorColor = colorScheme.primary,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        errorContainerColor = colorScheme.backgroundColor2,
        errorIndicatorColor = Color.Transparent
    )

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

    val backgroundColor = if (isSelected) colorScheme.extendedPrimaryColor50 else colorScheme.backgroundColor1
    val borderColor = if (isSelected) colorScheme.strokeColorHighlight else colorScheme.strokeColorDefault
    val textColor = if (isSelected) colorScheme.primary else colorScheme.textColorSecondary

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Image(
                painter = painterResource(id = flagResId),
                contentDescription = region.displayName,
                modifier = Modifier.size(20.dp, 15.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = region.code.uppercase(), style = typography.buttonMedium, color = textColor)
        }
    }
}
