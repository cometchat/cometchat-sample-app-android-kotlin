package com.cometchat.sampleapp.kotlin.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.sampleapp.kotlin.app.SampleApplication
import com.cometchat.sampleapp.kotlin.utils.AppPreferences
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.UIKitSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * State class representing the app credentials screen state.
 *
 * Property 12: Region single-select - exactly one region selected at a time.
 * Property 13: Credentials validation errors - appropriate error messages displayed.
 * Property 14: Valid credentials save and clear - clears existing and saves new credentials.
 *
 * Validates: Requirements 2.4
 */
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
 * Handles region selection, credential input, validation, and SDK initialization.
 *
 * Property 12: Region Single-Select
 * Property 13: Credentials Validation Errors
 * Property 14: Valid Credentials Save and Clear
 *
 * Validates: Requirements 2.4
 */
class AppCredentialsViewModel : ViewModel() {

    private val _state = MutableStateFlow(AppCredentialsState())
    val state: StateFlow<AppCredentialsState> = _state.asStateFlow()

    companion object {
        private const val TAG = "AppCredentialsViewModel"
    }

    /**
     * Handles region selection with single-select behavior.
     * Tapping a region selects it and deselects any previously selected region.
     *
     * Property 12: Region single-select - exactly one region can be selected at a time.
     *
     * @param region The region to select (US, EU, IN) - will be stored as lowercase
     */
    fun selectRegion(region: String) {
        _state.update { it.copy(selectedRegion = region.lowercase(), error = null) }
    }

    /**
     * Updates the App ID value in state.
     *
     * @param appId The App ID entered by the user
     */
    fun setAppId(appId: String) {
        _state.update { it.copy(appId = appId, error = null) }
    }

    /**
     * Updates the Auth Key value in state.
     *
     * @param authKey The Auth Key entered by the user
     */
    fun setAuthKey(authKey: String) {
        _state.update { it.copy(authKey = authKey, error = null) }
    }

    /**
     * Clears any error in the state.
     */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    /**
     * Handles the Continue button click.
     * Validates inputs and saves credentials if valid.
     *
     * Validation order:
     * 1. If selectedRegion == null → return "Please select app region"
     * 2. If appId.isEmpty() → return "Invalid App ID"
     * 3. If authKey.isEmpty() → return "Invalid Auth Key"
     *
     * If valid:
     * 1. Clear existing SharedPreferences via AppPreferences.clearAll()
     * 2. Save new credentials via AppPreferences.saveCredentials(appId, region, authKey)
     * 3. Call initUIKit()
     *
     * Property 13: Credentials validation errors - appropriate error messages displayed.
     * Property 14: Valid credentials save and clear - clears existing and saves new credentials.
     *
     * @param context The application context for SharedPreferences access
     * @return Error message if validation fails, null if validation passes
     */
    fun onContinueClick(context: Context): String? {
        val currentState = _state.value

        // Validation - Property 13
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

        // Property 14: Clear existing and save new credentials
        Log.d(TAG, "Validation passed, saving credentials")

        val appPreferences = AppPreferences(context)

        // Clear existing preferences
        appPreferences.clearAll()

        // Save new credentials
        appPreferences.saveCredentials(
            currentState.appId,
            currentState.selectedRegion!!,
            currentState.authKey
        )

        // Initialize UIKit with new credentials
        initUIKit(context)

        return null
    }

    /**
     * Initializes CometChatUIKit with the saved credentials.
     *
     * Configuration:
     * - setAutoEstablishSocketConnection(true)
     * - setAppId(appId)
     * - setRegion(region)
     * - setAuthKey(authKey)
     * - subscribePresenceForAllUsers()
     *
     * On success → set isInitialized = true (navigate to Login)
     * On error → set error message
     *
     * @param context The application context for SDK initialization
     */
    fun initUIKit(context: Context) {
        val currentState = _state.value

        _state.update { it.copy(isLoading = true, error = null) }

        Log.d(TAG, "Initializing UIKit with appId=${currentState.appId}, region=${currentState.selectedRegion}")

        val uiKitSettings = UIKitSettings.UIKitSettingsBuilder()
            .setAutoEstablishSocketConnection(true)
            .setAppId(currentState.appId)
            .setRegion(currentState.selectedRegion!!)
            .setAuthKey(currentState.authKey)
            .subscribePresenceForAllUsers()
            .setEnableCalling(true)
            .build()

        CometChatUIKit.init(context, uiKitSettings, object : CometChat.CallbackListener<String>() {
            override fun onSuccess(result: String?) {
                Log.d(TAG, "UIKit initialization successful")
                // Notify Application class that SDK is initialized
                notifyApplicationSDKInitialized(context)
                _state.update {
                    it.copy(isLoading = false, isInitialized = true)
                }
            }

            override fun onError(exception: CometChatException?) {
                val errorMessage = exception?.message ?: "SDK initialization failed"
                Log.e(TAG, "UIKit initialization failed: $errorMessage")
                _state.update {
                    it.copy(isLoading = false, error = errorMessage)
                }
            }
        })
    }

    /**
     * Resets the state to initial values.
     * Useful when navigating back to this screen.
     */
    fun resetState() {
        _state.value = AppCredentialsState()
    }

    /**
     * Notifies the Application class that SDK has been initialized.
     * This triggers call listener registration in the Application class.
     *
     * @param context The application context to get the Application instance
     */
    private fun notifyApplicationSDKInitialized(context: Context) {
        val application = context.applicationContext
        if (application is SampleApplication) {
            application.onSDKInitialized()
        }
    }
}
