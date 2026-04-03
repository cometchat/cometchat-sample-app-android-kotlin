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

/**
 * Sealed class representing the navigation state from the Splash screen.
 * 
 * Property 1: Splash navigation based on state - navigation destination is determined by:
 * (1) if credentials are not configured, navigate to AppCredentials
 * (2) if credentials exist and user is logged in, navigate to Home
 * (3) if credentials exist and user is not logged in, navigate to Login
 * 
 * Validates: Requirements 2.4
 */
sealed class SplashNavigationState {
    /** Initial loading state while checking credentials and SDK status */
    object Loading : SplashNavigationState()
    
    /** Navigate to AppCredentials screen when no credentials are configured */
    object NavigateToAppCredentials : SplashNavigationState()
    
    /** Navigate to Login screen when credentials exist but user is not logged in */
    object NavigateToLogin : SplashNavigationState()
    
    /** Navigate to Home screen when user is already logged in */
    object NavigateToHome : SplashNavigationState()
    
    /** Error state with message when SDK initialization fails */
    data class Error(val message: String) : SplashNavigationState()
}

/**
 * ViewModel for the Splash screen.
 * Handles SDK initialization and determines navigation destination based on
 * credentials and login status.
 * 
 * Property 1: Splash Navigation Based on State
 * Property 2: SDK Initialization Uses Stored Credentials
 * 
 * Validates: Requirements 2.4
 */
class SplashViewModel : ViewModel() {

    private val _navigationState = MutableStateFlow<SplashNavigationState>(SplashNavigationState.Loading)
    val navigationState: StateFlow<SplashNavigationState> = _navigationState.asStateFlow()

    companion object {
        private const val TAG = "SplashViewModel"
    }

    /**
     * Checks if app credentials exist and initializes the SDK accordingly.
     * 
     * Business Logic:
     * 1. Check if app credentials exist in SharedPreferences using AppPreferences.hasCredentials()
     * 2. If no credentials → navigate to AppCredentials
     * 3. If credentials exist → call initUIKit()
     * 
     * @param context The application context for accessing SharedPreferences
     */
    fun checkCredentialsAndInitialize(context: Context) {
        _navigationState.value = SplashNavigationState.Loading
        
        val appPreferences = AppPreferences(context)
        
        if (!appPreferences.hasCredentials()) {
            // No credentials configured - navigate to AppCredentials screen
            Log.d(TAG, "No credentials found, navigating to AppCredentials")
            _navigationState.value = SplashNavigationState.NavigateToAppCredentials
            return
        }
        
        // Credentials exist - check if SDK is already initialized
        if (CometChatUIKit.isSDKInitialized()) {
            Log.d(TAG, "SDK already initialized, checking login status")
            checkUserLoginStatus()
        } else {
            // Initialize the SDK with stored credentials
            Log.d(TAG, "Credentials found, initializing SDK")
            initUIKit(context)
        }
    }

    /**
     * Initializes CometChatUIKit with credentials stored in SharedPreferences.
     * 
     * Property 2: SDK initialization uses stored credentials - UIKitSettings is configured
     * with App ID, Region, and Auth Key values from SharedPreferences.
     * 
     * Configuration:
     * - setAutoEstablishSocketConnection(false)
     * - setAppId(appId)
     * - setRegion(region)
     * - setAuthKey(authKey)
     * - subscribePresenceForAllUsers()
     * 
     * On success → checkUserLoginStatus()
     * On error → show error via Toast (emit Error state)
     * 
     * @param context The application context for SDK initialization
     */
    fun initUIKit(context: Context) {
        val appPreferences = AppPreferences(context)
        val appId = appPreferences.getAppId()
        val region = appPreferences.getRegion()
        val authKey = appPreferences.getAuthKey()
        
        if (appId.isNullOrEmpty() || region.isNullOrEmpty() || authKey.isNullOrEmpty()) {
            Log.e(TAG, "Invalid credentials: appId=$appId, region=$region, authKey=${authKey?.take(5)}...")
            _navigationState.value = SplashNavigationState.NavigateToAppCredentials
            return
        }
        
        Log.d(TAG, "Initializing UIKit with appId=$appId, region=$region")
        
        val uiKitSettings = UIKitSettings.UIKitSettingsBuilder()
            .setAutoEstablishSocketConnection(false)
            .setAppId(appId)
            .setRegion(region)
            .setAuthKey(authKey)
            .subscribePresenceForAllUsers()
            .setEnableCalling(true)
            .build()
        
        CometChatUIKit.init(context, uiKitSettings, object : CometChat.CallbackListener<String>() {
            override fun onSuccess(result: String?) {
                Log.d(TAG, "UIKit initialization successful")
                // Notify Application class that SDK is initialized
                notifyApplicationSDKInitialized(context)
                checkUserLoginStatus()
            }
            
            override fun onError(exception: CometChatException?) {
                val errorMessage = exception?.message ?: "SDK initialization failed"
                Log.e(TAG, "UIKit initialization failed: $errorMessage")
                _navigationState.value = SplashNavigationState.Error(errorMessage)
            }
        })
    }

    /**
     * Checks the user login status and determines navigation destination.
     * 
     * Business Logic:
     * - If CometChatUIKit.getLoggedInUser() != null → navigate to Home
     * - Else → show "Not logged in" toast, navigate to Login
     */
    fun checkUserLoginStatus() {
        val loggedInUser = CometChatUIKit.getLoggedInUser()
        
        if (loggedInUser != null) {
            Log.d(TAG, "User already logged in: ${loggedInUser.uid}, navigating to Home")
            _navigationState.value = SplashNavigationState.NavigateToHome
        } else {
            Log.d(TAG, "User not logged in, navigating to Login")
            _navigationState.value = SplashNavigationState.NavigateToLogin
        }
    }

    /**
     * Resets the navigation state to Loading.
     * Useful when re-checking credentials after returning from another screen.
     */
    fun resetState() {
        _navigationState.value = SplashNavigationState.Loading
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
