package com.cometchat.ai.sampleapp.kotlin.ui.credentials

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.cometchat.ai.sampleapp.kotlin.app.AIAssistantApplication
import com.cometchat.ai.sampleapp.kotlin.utils.AppPreferences
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.UIKitSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AppCredentialsState(
    val selectedRegion: String? = null,
    val appId: String = "",
    val authKey: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isInitialized: Boolean = false
)

/**
 * ViewModel for the App Credentials screen.
 * Handles region selection, credential input, validation, and SDK init.
 */
class AppCredentialsViewModel : ViewModel() {

    private val _state = MutableStateFlow(AppCredentialsState())
    val state: StateFlow<AppCredentialsState> = _state.asStateFlow()

    companion object {
        private const val TAG = "AppCredentialsViewModel"
    }

    fun selectRegion(region: String) {
        _state.update { it.copy(selectedRegion = region.lowercase(), error = null) }
    }

    fun setAppId(appId: String) {
        _state.update { it.copy(appId = appId, error = null) }
    }

    fun setAuthKey(authKey: String) {
        _state.update { it.copy(authKey = authKey, error = null) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    /**
     * Validates input and saves credentials if valid.
     * Returns an error message if validation fails, null otherwise.
     */
    fun onContinueClick(context: Context): String? {
        val currentState = _state.value

        when {
            currentState.selectedRegion == null -> {
                val error = "Please select app region"
                _state.update { it.copy(error = error) }
                return error
            }
            currentState.appId.isEmpty() -> {
                val error = "Invalid App ID"
                _state.update { it.copy(error = error) }
                return error
            }
            currentState.authKey.isEmpty() -> {
                val error = "Invalid Auth Key"
                _state.update { it.copy(error = error) }
                return error
            }
        }

        val appPreferences = AppPreferences(context)
        appPreferences.clearAll()
        appPreferences.saveCredentials(
            currentState.appId,
            currentState.selectedRegion!!,
            currentState.authKey
        )

        initUIKit(context)
        return null
    }

    private fun initUIKit(context: Context) {
        val currentState = _state.value
        _state.update { it.copy(isLoading = true, error = null) }

        val settings = UIKitSettings.UIKitSettingsBuilder()
            .setAutoEstablishSocketConnection(true)
            .setAppId(currentState.appId)
            .setRegion(currentState.selectedRegion!!)
            .setAuthKey(currentState.authKey)
            .subscribePresenceForAllUsers()
            .build()

        CometChatUIKit.init(
            context,
            settings,
            object : CometChat.CallbackListener<String>() {
                override fun onSuccess(result: String?) {
                    Log.d(TAG, "UIKit init success")
                    _state.update {
                        it.copy(isLoading = false, isInitialized = true)
                    }
                }

                override fun onError(exception: CometChatException?) {
                    val errorMessage = exception?.message ?: "SDK initialization failed"
                    Log.e(TAG, "UIKit init failed: $errorMessage")
                    _state.update {
                        it.copy(isLoading = false, error = errorMessage)
                    }
                }
            }
        )
    }
}
