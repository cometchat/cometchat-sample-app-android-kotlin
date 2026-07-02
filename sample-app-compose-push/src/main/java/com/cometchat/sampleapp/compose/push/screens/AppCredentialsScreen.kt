package com.cometchat.sampleapp.compose.push.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.cometchat.sampleapp.compose.push.shared.AppCredentialsViewModel

@Composable
fun AppCredentialsScreen(
    onNavigateToLogin: () -> Unit,
    appCredentialsViewModel: AppCredentialsViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by appCredentialsViewModel.state.collectAsState()
    val colorScheme = CometChatTheme.colorScheme
    val typography = CometChatTheme.typography
    
    var appIdText by remember { mutableStateOf("") }
    var authKeyText by remember { mutableStateOf("") }
    
    LaunchedEffect(state.isInitialized) {
        if (state.isInitialized) { onNavigateToLogin() }
    }
    
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            appCredentialsViewModel.clearError()
        }
    }
    
    LaunchedEffect(state.appId) {
        if (state.appId != appIdText) { appIdText = state.appId }
    }
    
    LaunchedEffect(state.authKey) {
        if (state.authKey != authKeyText) { authKeyText = state.authKey }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.backgroundColor1)
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_cometchat_logo),
            contentDescription = "CometChat Logo",
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 20.dp)
        )
        
        Text(
            text = stringResource(R.string.app_app_credentials),
            style = typography.heading2Bold,
            color = colorScheme.textColorPrimary,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 20.dp)
        )
        
        Text(
            text = stringResource(R.string.app_region),
            style = typography.caption1Medium,
            color = colorScheme.textColorPrimary,
            modifier = Modifier.padding(top = 5.dp)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .padding(vertical = 8.dp)
        ) {
            RegionCard(
                region = "us",
                label = stringResource(R.string.app_region_us),
                isSelected = state.selectedRegion == "us",
                onClick = { appCredentialsViewModel.selectRegion("us") },
                modifier = Modifier.weight(1f)
            )
            RegionCard(
                region = "eu",
                label = stringResource(R.string.app_region_eu),
                isSelected = state.selectedRegion == "eu",
                onClick = { appCredentialsViewModel.selectRegion("eu") },
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
            )
            RegionCard(
                region = "in",
                label = stringResource(R.string.app_region_in),
                isSelected = state.selectedRegion == "in",
                onClick = { appCredentialsViewModel.selectRegion("in") },
                modifier = Modifier.weight(1f)
            )
        }
        
        Text(
            text = stringResource(R.string.app_app_id),
            style = typography.caption1Medium,
            color = colorScheme.textColorPrimary,
            modifier = Modifier.padding(top = 5.dp)
        )
        
        TextField(
            value = appIdText,
            onValueChange = { appIdText = it; appCredentialsViewModel.setAppId(it) },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            placeholder = {
                Text(
                    stringResource(R.string.app_enter_app_id),
                    style = typography.bodyRegular,
                    color = colorScheme.textColorTertiary
                )
            },
            textStyle = typography.bodyRegular.copy(color = colorScheme.textColorPrimary),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = colorScheme.backgroundColor2,
                unfocusedContainerColor = colorScheme.backgroundColor2,
                cursorColor = colorScheme.primary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )
        
        Text(
            text = stringResource(R.string.app_auth_key),
            style = typography.caption1Medium,
            color = colorScheme.textColorPrimary,
            modifier = Modifier.padding(top = 5.dp)
        )
        
        TextField(
            value = authKeyText,
            onValueChange = { authKeyText = it; appCredentialsViewModel.setAuthKey(it) },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            placeholder = {
                Text(
                    stringResource(R.string.app_enter_auth_key),
                    style = typography.bodyRegular,
                    color = colorScheme.textColorTertiary
                )
            },
            textStyle = typography.bodyRegular.copy(color = colorScheme.textColorPrimary),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = colorScheme.backgroundColor2,
                unfocusedContainerColor = colorScheme.backgroundColor2,
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
        
        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(100.dp))
        
        Button(
            onClick = {
                val err = appCredentialsViewModel.onContinueClick(context)
                if (err != null) Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.primaryButtonBackgroundColor
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Text(
                stringResource(R.string.app_continue),
                style = typography.buttonMedium,
                color = colorScheme.colorWhite
            )
        }
    }
}
