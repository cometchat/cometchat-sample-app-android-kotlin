package com.cometchat.sampleapp.kotlin.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.sampleapp.kotlin.utils.AppPreferences
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Represents the different states of the logout process.
 */
sealed interface LogoutState {
    /** Initial state when no logout action has been taken. */
    data object Idle : LogoutState
    
    /** State indicating that a logout operation is in progress. */
    data object Loading : LogoutState
    
    /** State indicating successful logout. */
    data object Success : LogoutState
    
    /** State indicating a logout error occurred. */
    data class Error(val message: String) : LogoutState
}

/**
 * ViewModel for the Home screen.
 *
 * This ViewModel manages:
 * - Logout functionality
 * - Navigation state between tabs
 * - User session management
 *
 * ## Usage:
 * ```kotlin
 * class HomeActivity : AppCompatActivity() {
 *     private val viewModel: HomeViewModel by viewModels()
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         lifecycleScope.launch {
 *             viewModel.logoutState.collect { state ->
 *                 when (state) {
 *                     is LogoutState.Success -> navigateToLogin()
 *                     is LogoutState.Error -> showError(state.message)
 *                     // ...
 *                 }
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * @param application The application context for accessing preferences
 *
 * @see LogoutState
 *
 * Validates: Requirements 3.6, 3.7
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "HomeViewModel"
    }

    private val appPreferences = AppPreferences(application)

    private val _logoutState = MutableStateFlow<LogoutState>(LogoutState.Idle)
    /**
     * The current state of the logout process.
     * Observe this StateFlow to react to logout state changes in the UI.
     */
    val logoutState: StateFlow<LogoutState> = _logoutState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    /**
     * Navigation events emitted by the ViewModel.
     * Collect this SharedFlow to handle navigation in the UI.
     */
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    /**
     * Initiates the logout process.
     *
     * This method:
     * 1. Logs out from CometChat SDK
     * 2. Clears the user session from preferences
     * 3. Emits navigation event to return to login screen
     *
     * Validates: Requirements 3.7
     */
    fun logout() {
        if (_logoutState.value == LogoutState.Loading) {
            Log.d(TAG, "Logout already in progress")
            return
        }

        _logoutState.value = LogoutState.Loading
        Log.d(TAG, "Starting logout process...")

        CometChat.logout(object : CometChat.CallbackListener<String>() {
            override fun onSuccess(result: String?) {
                Log.d(TAG, "CometChat logout successful")
                // Clear session from preferences
                appPreferences.clearSession()
                _logoutState.value = LogoutState.Success
                
                // Emit navigation event
                viewModelScope.launch {
                    _navigationEvent.emit(NavigationEvent.ToLogin)
                }
            }

            override fun onError(exception: CometChatException?) {
                Log.e(TAG, "CometChat logout failed: ${exception?.message}")
                // Even if logout fails, clear local session and navigate to login
                // This ensures the user can always log out from the app
                appPreferences.clearSession()
                _logoutState.value = LogoutState.Success
                
                viewModelScope.launch {
                    _navigationEvent.emit(NavigationEvent.ToLogin)
                }
            }
        })
    }

    /**
     * Resets the logout state to Idle.
     */
    fun resetLogoutState() {
        _logoutState.value = LogoutState.Idle
    }

    /**
     * Gets the currently logged-in user's UID.
     *
     * @return The user's UID or null if not logged in
     */
    fun getLoggedInUserUid(): String? {
        return appPreferences.getLoggedInUserUid()
    }
}

/**
 * Navigation events for the Home screen.
 */
sealed interface NavigationEvent {
    /** Navigate to the login screen. */
    data object ToLogin : NavigationEvent
}
