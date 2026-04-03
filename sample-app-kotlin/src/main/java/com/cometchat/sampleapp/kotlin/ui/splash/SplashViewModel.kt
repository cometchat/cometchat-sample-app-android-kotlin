package com.cometchat.sampleapp.kotlin.ui.splash

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.sampleapp.kotlin.app.SampleApplication
import com.cometchat.sampleapp.kotlin.utils.AppConstants
import com.cometchat.sampleapp.kotlin.utils.AppPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Represents the navigation destination from SplashActivity.
 *
 * Validates: Requirements 2.2, 2.3, 3.2, 3.3
 */
sealed class SplashNavigation {
    /** Navigate to AppCredentialsActivity when no App ID is stored */
    data object ToAppCredentials : SplashNavigation()
    
    /** Navigate to LoginActivity when user is not logged in */
    data object ToLogin : SplashNavigation()
    
    /** Navigate to HomeActivity when user is already logged in */
    data object ToHome : SplashNavigation()
}

/**
 * State class representing the splash screen state.
 */
data class SplashState(
    val isLoading: Boolean = true,
    val error: CometChatException? = null,
    val navigation: SplashNavigation? = null
)

/**
 * ViewModel for the Splash screen.
 *
 * This ViewModel manages the splash flow including:
 * - Checking for stored app credentials
 * - Initializing the CometChat SDK
 * - Checking user login status
 * - Determining navigation destination
 *
 * ## Navigation Flow:
 * 1. Check if App ID exists in SharedPreferences
 *    - If no App ID → Navigate to AppCredentialsActivity
 * 2. Initialize CometChat SDK with stored credentials
 * 3. Check if user is logged in
 *    - If logged in → Navigate to HomeActivity
 *    - If not logged in → Navigate to LoginActivity
 *
 * Validates: Requirements 1.1, 1.2, 1.3, 1.4, 1.5, 2.1, 2.2, 2.3, 2.4, 3.1, 3.2, 3.3, 3.4
 */
class SplashViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "SplashViewModel"
    }

    private val appPreferences = AppPreferences(application)
    private val sampleApplication = application as SampleApplication

    private val _state = MutableStateFlow(SplashState())
    val state: StateFlow<SplashState> = _state.asStateFlow()

    init {
        checkAppCredentials()
    }

    /**
     * Checks if app credentials exist in SharedPreferences or AppConstants.
     * 
     * Uses default credentials from AppConstants if SharedPreferences is empty.
     * This allows the app to work out-of-the-box with pre-configured credentials.
     *
     * Validates: Requirements 2.1, 2.2, 2.3
     */
    private fun checkAppCredentials() {
        Log.d(TAG, "Checking app credentials...")
        
        // Check SharedPreferences first, fall back to AppConstants defaults
        val appId = appPreferences.getAppId() ?: AppConstants.APP_ID
        
        if (appId.isBlank()) {
            Log.d(TAG, "No App ID found, navigating to AppCredentials")
            _state.update { 
                it.copy(
                    isLoading = false,
                    navigation = SplashNavigation.ToAppCredentials
                )
            }
        } else {
            Log.d(TAG, "App ID found, initializing SDK")
            initializeSDK()
        }
    }

    /**
     * Initializes the CometChat SDK with stored credentials.
     *
     * If SDK is already initialized, proceeds to check login status.
     * Otherwise, initializes SDK first then checks login status.
     *
     * Validates: Requirements 1.2, 1.3, 2.4
     */
    private fun initializeSDK() {
        // Check if SDK is already initialized
        if (sampleApplication.isSDKInitialized()) {
            Log.d(TAG, "SDK already initialized, checking login status")
            checkLoginStatus()
            return
        }

        // Get credentials from preferences or use defaults
        val appId = appPreferences.getAppId() ?: AppConstants.APP_ID
        val region = appPreferences.getRegion() ?: AppConstants.REGION
        val authKey = appPreferences.getAuthKey() ?: AppConstants.AUTH_KEY

        Log.d(TAG, "Initializing CometChat SDK...")

        sampleApplication.initializeCometChat(
            appId = appId,
            region = region,
            authKey = authKey,
            onSuccess = {
                Log.d(TAG, "SDK initialized successfully")
                checkLoginStatus()
            },
            onError = { exception ->
                Log.e(TAG, "SDK initialization failed: ${exception.message}")
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = exception
                    )
                }
            }
        )
    }

    /**
     * Checks if a user is already logged in via CometChatUIKit.
     *
     * If user is logged in, navigates to HomeActivity.
     * If no user is logged in, navigates to LoginActivity.
     *
     * Validates: Requirements 3.1, 3.2, 3.3
     */
    private fun checkLoginStatus() {
        Log.d(TAG, "Checking login status...")

        try {
            val loggedInUser = CometChatUIKit.getLoggedInUser()
            
            if (loggedInUser != null) {
                Log.d(TAG, "User is logged in: ${loggedInUser.uid}")
                // Establish socket connection for real-time updates
                establishSocketConnection()
            } else {
                Log.d(TAG, "No user logged in, navigating to Login")
                _state.update {
                    it.copy(
                        isLoading = false,
                        navigation = SplashNavigation.ToLogin
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking login status: ${e.message}")
            // If there's an error, navigate to login
            _state.update {
                it.copy(
                    isLoading = false,
                    navigation = SplashNavigation.ToLogin
                )
            }
        }
    }

    /**
     * Establishes the CometChat socket connection for real-time updates.
     * After connection (success or failure), navigates to HomeActivity.
     */
    private fun establishSocketConnection() {
        CometChat.connect(object : CometChat.CallbackListener<String?>() {
            override fun onSuccess(result: String?) {
                Log.d(TAG, "Socket connection established")
                _state.update {
                    it.copy(
                        isLoading = false,
                        navigation = SplashNavigation.ToHome
                    )
                }
            }

            override fun onError(exception: CometChatException?) {
                // Even on socket connection failure, proceed to home
                // The socket will reconnect automatically
                Log.w(TAG, "Socket connection failed (non-blocking): ${exception?.message}")
                _state.update {
                    it.copy(
                        isLoading = false,
                        navigation = SplashNavigation.ToHome
                    )
                }
            }
        })
    }

    /**
     * Clears any error in the state.
     */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    /**
     * Retries SDK initialization after an error.
     *
     * Validates: Requirement 1.4
     */
    fun retry() {
        _state.update { it.copy(isLoading = true, error = null) }
        checkAppCredentials()
    }
}
