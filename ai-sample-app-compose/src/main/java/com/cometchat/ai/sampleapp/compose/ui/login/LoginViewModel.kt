package com.cometchat.ai.sampleapp.compose.ui.login

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cometchat.ai.sampleapp.compose.app.AIAssistantApplication
import com.cometchat.ai.sampleapp.compose.repository.SampleUsersRepository
import com.cometchat.ai.sampleapp.compose.utils.AppConstants
import com.cometchat.ai.sampleapp.compose.utils.AppPreferences
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.CometChatUIKit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Login UI state. */
sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data object Success : LoginUiState
    data class Error(val message: String) : LoginUiState
}

/**
 * ViewModel for the Login screen.
 *
 * Responsibilities:
 * - Fetch sample users from remote JSON.
 * - Track selected sample user vs. manually entered UID.
 * - Initialize SDK (if not already) + log the user in + establish socket.
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    companion object { private const val TAG = "LoginViewModel" }

    private val appPreferences = AppPreferences(application)
    private val app = application as AIAssistantApplication

    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _selectedUser = MutableStateFlow<User?>(null)
    val selectedUser: StateFlow<User?> = _selectedUser.asStateFlow()

    private val _manualUid = MutableStateFlow("")
    val manualUid: StateFlow<String> = _manualUid.asStateFlow()

    init {
        loadSampleUsers()
    }

    private fun loadSampleUsers() {
        viewModelScope.launch {
            val users = withContext(Dispatchers.IO) { SampleUsersRepository.fetchSampleUsers() }
            _users.value = users
        }
    }

    fun selectUser(user: User?) {
        _selectedUser.value = if (_selectedUser.value?.uid == user?.uid) null else user
        _manualUid.value = ""
    }

    fun onManualUidFocused() {
        _selectedUser.value = null
    }

    fun setManualUid(uid: String) {
        _manualUid.value = uid
        if (uid.isNotEmpty()) _selectedUser.value = null
    }

    fun resetState() {
        _loginState.value = LoginUiState.Idle
    }

    fun login() {
        val uid = _selectedUser.value?.uid ?: _manualUid.value.trim()
        if (uid.isBlank()) {
            _loginState.value = LoginUiState.Error("Please select a user or enter a UID")
            return
        }

        _loginState.value = LoginUiState.Loading

        if (app.isSDKInitialized()) {
            performLogin(uid)
        } else {
            val appId = appPreferences.getAppId() ?: AppConstants.APP_ID
            val region = appPreferences.getRegion() ?: AppConstants.REGION
            val authKey = appPreferences.getAuthKey() ?: AppConstants.AUTH_KEY
            app.initializeCometChat(
                appId = appId, region = region, authKey = authKey,
                onSuccess = { performLogin(uid) },
                onError = { exception ->
                    _loginState.value = LoginUiState.Error(exception.message ?: "SDK init failed")
                }
            )
        }
    }

    private fun performLogin(uid: String) {
        CometChatUIKit.login(uid, object : CometChat.CallbackListener<User>() {
            override fun onSuccess(user: User?) {
                Log.d(TAG, "Login successful: ${user?.uid}")
                appPreferences.saveLoggedInUserUid(uid)
                CometChat.connect(object : CometChat.CallbackListener<String?>() {
                    override fun onSuccess(result: String?) {
                        _loginState.value = LoginUiState.Success
                    }

                    override fun onError(exception: CometChatException?) {
                        // Non-blocking; proceed regardless
                        _loginState.value = LoginUiState.Success
                    }
                })
            }

            override fun onError(exception: CometChatException?) {
                Log.e(TAG, "Login failed: ${exception?.message}")
                _loginState.value = LoginUiState.Error(exception?.message ?: "Login failed")
            }
        })
    }
}
