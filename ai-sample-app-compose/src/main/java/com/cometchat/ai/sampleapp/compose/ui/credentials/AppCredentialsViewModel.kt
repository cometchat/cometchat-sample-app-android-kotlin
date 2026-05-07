package com.cometchat.ai.sampleapp.compose.ui.credentials

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.cometchat.ai.sampleapp.compose.app.AIAssistantApplication
import com.cometchat.ai.sampleapp.compose.utils.AppPreferences
import com.cometchat.chat.exceptions.CometChatException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Available CometChat regions. */
enum class Region(val code: String, val displayName: String) {
    US("us", "United States"),
    EU("eu", "Europe"),
    IN("in", "India")
}

data class AppCredentialsState(
    val selectedRegion: Region? = null,
    val appId: String = "",
    val authKey: String = "",
    val appIdError: String? = null,
    val authKeyError: String? = null,
    val isLoading: Boolean = false,
    val error: CometChatException? = null,
    val isCredentialsSaved: Boolean = false
)

/**
 * ViewModel for the App Credentials screen. Handles region selection,
 * validation, persistence to [AppPreferences], and SDK init.
 */
class AppCredentialsViewModel(application: Application) : AndroidViewModel(application) {

    companion object { private const val TAG = "AppCredentialsVM" }

    private val appPreferences = AppPreferences(application)
    private val app = application as AIAssistantApplication

    private val _state = MutableStateFlow(AppCredentialsState())
    val state: StateFlow<AppCredentialsState> = _state.asStateFlow()

    fun selectRegion(region: Region) {
        _state.update { it.copy(selectedRegion = region) }
    }

    fun setAppId(appId: String) {
        _state.update { it.copy(appId = appId, appIdError = null) }
    }

    fun setAuthKey(authKey: String) {
        _state.update { it.copy(authKey = authKey, authKeyError = null) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    /**
     * Returns a toast-ready error message if validation fails (region missing);
     * inline field errors are updated in state for App ID / Auth Key.
     */
    fun onContinueClick(): String? {
        val s = _state.value

        if (s.selectedRegion == null) return "Please select a region"

        var hasError = false
        if (s.appId.isBlank()) {
            _state.update { it.copy(appIdError = "App ID is required") }
            hasError = true
        }
        if (s.authKey.isBlank()) {
            _state.update { it.copy(authKeyError = "Auth Key is required") }
            hasError = true
        }
        if (hasError) return null

        saveAndInit(
            appId = s.appId.trim(),
            region = s.selectedRegion.code,
            authKey = s.authKey.trim()
        )
        return null
    }

    private fun saveAndInit(appId: String, region: String, authKey: String) {
        _state.update { it.copy(isLoading = true) }

        appPreferences.clearAll()
        appPreferences.saveCredentials(appId, region, authKey)

        app.initializeCometChat(
            appId = appId,
            region = region,
            authKey = authKey,
            onSuccess = {
                Log.d(TAG, "UIKit init success")
                _state.update { it.copy(isLoading = false, isCredentialsSaved = true) }
            },
            onError = { exception ->
                Log.e(TAG, "UIKit init failed: ${exception.message}")
                _state.update { it.copy(isLoading = false, error = exception) }
            }
        )
    }
}
