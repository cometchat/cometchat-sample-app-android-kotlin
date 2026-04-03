package com.cometchat.sampleapp.compose.ui.credentials

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.sampleapp.compose.app.SampleApplication
import com.cometchat.sampleapp.compose.utils.AppPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Represents the available CometChat regions.
 *
 * Validates: Requirement 4.1
 */
enum class Region(val code: String, val displayName: String) {
    US("us", "United States"),
    EU("eu", "Europe"),
    IN("in", "India")
}

/**
 * State class representing the app credentials screen state.
 */
data class AppCredentialsState(
    val selectedRegion: Region? = null,
    val appId: String = "",
    val authKey: String = "",
    val appIdError: String? = null,
    val authKeyError: String? = null,
    val regionError: String? = null,
    val isLoading: Boolean = false,
    val error: CometChatException? = null,
    val isCredentialsSaved: Boolean = false
)

/**
 * ViewModel for the App Credentials screen.
 *
 * This ViewModel manages the credentials entry flow including:
 * - Region selection (US, EU, IN)
 * - App ID and Auth Key input validation
 * - SharedPreferences storage for credentials
 * - CometChat UIKit initialization
 *
 * ## Validation Rules:
 * - Region must be selected before continuing
 * - App ID cannot be empty
 * - Auth Key cannot be empty
 *
 * Validates: Requirements 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7
 */
class AppCredentialsViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "AppCredentialsViewModel"
    }

    private val appPreferences = AppPreferences(application)
    private val sampleApplication = application as SampleApplication

    private val _state = MutableStateFlow(AppCredentialsState())
    val state: StateFlow<AppCredentialsState> = _state.asStateFlow()

    /**
     * Selects a region for the CometChat app.
     *
     * Validates: Requirement 4.2
     *
     * @param region The region to select
     */
    fun selectRegion(region: Region) {
        Log.d(TAG, "Region selected: ${region.code}")
        _state.update {
            it.copy(
                selectedRegion = region,
                regionError = null
            )
        }
    }

    /**
     * Updates the App ID value in state.
     *
     * @param appId The App ID entered by the user
     */
    fun setAppId(appId: String) {
        _state.update {
            it.copy(
                appId = appId,
                appIdError = null
            )
        }
    }

    /**
     * Updates the Auth Key value in state.
     *
     * @param authKey The Auth Key entered by the user
     */
    fun setAuthKey(authKey: String) {
        _state.update {
            it.copy(
                authKey = authKey,
                authKeyError = null
            )
        }
    }

    /**
     * Clears any error in the state.
     */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    /**
     * Validates the credentials and saves them if valid.
     * Returns true if validation passes and credentials are being saved.
     *
     * Validates: Requirements 4.3, 4.4, 4.5
     *
     * @return Error message for toast display, or null if validation passes
     */
    fun onContinueClick(): String? {
        val currentState = _state.value
        var hasError = false

        // Validate region selection
        // Validates: Requirement 4.3
        if (currentState.selectedRegion == null) {
            _state.update { it.copy(regionError = "Please select a region") }
            return "Please select a region"
        }

        // Validate App ID
        // Validates: Requirement 4.4
        if (currentState.appId.isBlank()) {
            _state.update { it.copy(appIdError = "App ID is required") }
            hasError = true
        }

        // Validate Auth Key
        // Validates: Requirement 4.5
        if (currentState.authKey.isBlank()) {
            _state.update { it.copy(authKeyError = "Auth Key is required") }
            hasError = true
        }

        if (hasError) {
            return null // Errors are shown inline, no toast needed
        }

        // All validations passed, save credentials and initialize UIKit
        saveCredentialsAndInitialize(
            appId = currentState.appId.trim(),
            authKey = currentState.authKey.trim(),
            region = currentState.selectedRegion.code
        )

        return null
    }

    /**
     * Saves credentials to SharedPreferences and initializes CometChat UIKit.
     *
     * Validates: Requirements 4.6, 4.7
     *
     * @param appId The CometChat App ID
     * @param authKey The CometChat Auth Key
     * @param region The selected region code
     */
    private fun saveCredentialsAndInitialize(
        appId: String,
        authKey: String,
        region: String
    ) {
        Log.d(TAG, "Saving credentials and initializing UIKit...")
        _state.update { it.copy(isLoading = true) }

        // Save credentials to SharedPreferences
        // Validates: Requirement 4.6
        appPreferences.saveCredentials(appId, region, authKey)
        Log.d(TAG, "Credentials saved to SharedPreferences")

        // Initialize CometChat UIKit
        // Validates: Requirement 4.7
        sampleApplication.initializeCometChat(
            appId = appId,
            region = region,
            authKey = authKey,
            onSuccess = {
                Log.d(TAG, "UIKit initialized successfully")
                _state.update {
                    it.copy(
                        isLoading = false,
                        isCredentialsSaved = true
                    )
                }
            },
            onError = { exception ->
                Log.e(TAG, "UIKit initialization failed: ${exception.message}")
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = exception
                    )
                }
            }
        )
    }
}
