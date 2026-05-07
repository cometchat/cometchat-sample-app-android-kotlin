package com.cometchat.ai.sampleapp.compose.ui.splash

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.cometchat.ai.sampleapp.compose.app.AIAssistantApplication
import com.cometchat.ai.sampleapp.compose.utils.AppConstants
import com.cometchat.ai.sampleapp.compose.utils.AppPreferences
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.uikit.core.CometChatUIKit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

sealed class SplashNavigation {
    data object ToAppCredentials : SplashNavigation()
    data object ToLogin : SplashNavigation()
    data object ToAIAssistantUsers : SplashNavigation()
}

data class SplashState(
    val isLoading: Boolean = true,
    val error: CometChatException? = null,
    val navigation: SplashNavigation? = null
)

/**
 * ViewModel for the splash screen.
 *
 * Flow:
 * 1. Read stored App ID (falls back to [AppConstants]).
 * 2. No App ID → navigate to AppCredentials.
 * 3. Initialize SDK with stored credentials.
 * 4. If logged in → AI Assistant Users screen; otherwise → Login screen.
 */
class SplashViewModel(application: Application) : AndroidViewModel(application) {

    companion object { private const val TAG = "SplashViewModel" }

    private val appPreferences = AppPreferences(application)
    private val app = application as AIAssistantApplication

    private val _state = MutableStateFlow(SplashState())
    val state: StateFlow<SplashState> = _state.asStateFlow()

    init {
        checkAppCredentials()
    }

    private fun checkAppCredentials() {
        val appId = appPreferences.getAppId() ?: AppConstants.APP_ID
        if (appId.isBlank()) {
            _state.update { it.copy(isLoading = false, navigation = SplashNavigation.ToAppCredentials) }
        } else {
            initializeSDK()
        }
    }

    private fun initializeSDK() {
        if (app.isSDKInitialized()) {
            checkLoginStatus()
            return
        }

        val appId = appPreferences.getAppId() ?: AppConstants.APP_ID
        val region = appPreferences.getRegion() ?: AppConstants.REGION
        val authKey = appPreferences.getAuthKey() ?: AppConstants.AUTH_KEY

        app.initializeCometChat(
            appId = appId,
            region = region,
            authKey = authKey,
            onSuccess = { checkLoginStatus() },
            onError = { exception ->
                _state.update { it.copy(isLoading = false, error = exception) }
            }
        )
    }

    private fun checkLoginStatus() {
        try {
            val loggedInUser = CometChatUIKit.getLoggedInUser()
            if (loggedInUser != null) {
                establishSocketConnection()
            } else {
                _state.update { it.copy(isLoading = false, navigation = SplashNavigation.ToLogin) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Login-status check failed: ${e.message}")
            _state.update { it.copy(isLoading = false, navigation = SplashNavigation.ToLogin) }
        }
    }

    private fun establishSocketConnection() {
        CometChat.connect(object : CometChat.CallbackListener<String?>() {
            override fun onSuccess(result: String?) {
                _state.update { it.copy(isLoading = false, navigation = SplashNavigation.ToAIAssistantUsers) }
            }

            override fun onError(exception: CometChatException?) {
                // Non-blocking — proceed regardless
                _state.update { it.copy(isLoading = false, navigation = SplashNavigation.ToAIAssistantUsers) }
            }
        })
    }

    fun clearError() { _state.update { it.copy(error = null) } }

    fun retry() {
        _state.update { it.copy(isLoading = true, error = null) }
        checkAppCredentials()
    }
}
