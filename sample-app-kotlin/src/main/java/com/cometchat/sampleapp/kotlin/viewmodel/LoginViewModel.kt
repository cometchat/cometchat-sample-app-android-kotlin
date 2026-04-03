package com.cometchat.sampleapp.kotlin.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User
import com.cometchat.sampleapp.kotlin.repository.SampleUsersRepository
import com.cometchat.uikit.core.CometChatUIKit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * State class representing the login screen state.
 */
data class LoginState(
    val users: List<User> = emptyList(),
    val selectedUser: User? = null,
    val manualUid: String = "",
    val isLoading: Boolean = false,
    val error: CometChatException? = null,
    val isLoggedIn: Boolean = false
)

/**
 * ViewModel for the Login screen.
 * Handles sample user fetching, user selection, manual UID entry, and authentication.
 * 
 * Adapted from master-app-kotlin2/shared/LoginViewModel.kt
 * Package: com.cometchat.sampleapp.kotlin.shared
 * 
 * Note: FCM token registration is excluded from sample apps.
 */
class LoginViewModel : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    companion object {
        private const val TAG = "LoginViewModel"
    }

    /**
     * Fetches sample users from the CometChat sample users API.
     */
    fun fetchSampleUsers() {
        SampleUsersRepository.fetchSampleUsers { users ->
            viewModelScope.launch(Dispatchers.Main) {
                _state.update { it.copy(users = users) }
            }
        }
    }

    /**
     * Handles user selection with toggle behavior.
     */
    fun selectUser(user: User?) {
        _state.update { currentState ->
            val newSelectedUser = if (currentState.selectedUser?.uid == user?.uid) {
                null
            } else {
                user
            }
            currentState.copy(
                selectedUser = newSelectedUser,
                manualUid = ""
            )
        }
    }

    /**
     * Called when the manual UID input field receives focus.
     */
    fun onManualUidFocused() {
        _state.update { it.copy(selectedUser = null) }
    }

    /**
     * Updates the manual UID value in state.
     */
    fun setManualUid(uid: String) {
        _state.update { it.copy(manualUid = uid) }
    }

    /**
     * Clears any error in the state.
     */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    /**
     * Handles the Continue button click.
     */
    fun onContinueClick(): String? {
        val currentState = _state.value
        
        return when {
            currentState.selectedUser != null -> {
                login(currentState.selectedUser.uid)
                null
            }
            currentState.manualUid.isNotEmpty() -> {
                login(currentState.manualUid)
                null
            }
            else -> {
                "Please select user or enter the correct UID"
            }
        }
    }

    /**
     * Initiates login with the given UID.
     * On success, establishes socket connection.
     * 
     * Note: FCM token registration is excluded from sample apps.
     */
    fun login(uid: String) {
        _state.update { it.copy(isLoading = true, error = null) }

        CometChatUIKit.login(uid, object : CometChat.CallbackListener<User>() {
            override fun onSuccess(user: User?) {
                Log.d(TAG, "Login successful for user: ${user?.uid}")
                // Note: FCM token registration is excluded from sample apps
                establishSocketConnection()
            }

            override fun onError(exception: CometChatException?) {
                Log.e(TAG, "Login failed: ${exception?.message}")
                _state.update { 
                    it.copy(
                        isLoading = false, 
                        error = exception ?: CometChatException("ERROR", "Login failed")
                    ) 
                }
            }
        })
    }

    /**
     * Establishes the CometChat socket connection.
     */
    private fun establishSocketConnection() {
        CometChat.connect(object : CometChat.CallbackListener<String?>() {
            override fun onSuccess(result: String?) {
                Log.d(TAG, "Socket connection established")
                _state.update { 
                    it.copy(isLoading = false, isLoggedIn = true) 
                }
            }

            override fun onError(exception: CometChatException?) {
                Log.w(TAG, "Socket connection failed (non-blocking): ${exception?.message}")
                _state.update { 
                    it.copy(isLoading = false, isLoggedIn = true) 
                }
            }
        })
    }
}
