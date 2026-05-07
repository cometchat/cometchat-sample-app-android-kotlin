package com.cometchat.ai.sampleapp.kotlin.ui.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cometchat.ai.sampleapp.kotlin.repository.SampleUsersRepository
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.CometChatUIKit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginState(
    val users: List<User> = emptyList(),
    val selectedUser: User? = null,
    val manualUid: String = "",
    val isLoading: Boolean = false,
    val error: CometChatException? = null,
    val isLoggedIn: Boolean = false
)

class LoginViewModel : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    companion object {
        private const val TAG = "LoginViewModel"
    }

    fun fetchSampleUsers() {
        SampleUsersRepository.fetchSampleUsers { users ->
            viewModelScope.launch(Dispatchers.Main) {
                _state.update { it.copy(users = users) }
            }
        }
    }

    fun selectUser(user: User?) {
        _state.update { currentState ->
            val newSelectedUser =
                if (currentState.selectedUser?.uid == user?.uid) null else user
            currentState.copy(
                selectedUser = newSelectedUser,
                manualUid = ""
            )
        }
    }

    fun onManualUidFocused() {
        _state.update { it.copy(selectedUser = null) }
    }

    fun setManualUid(uid: String) {
        _state.update { it.copy(manualUid = uid) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

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
            else -> "Please select user or enter the correct UID"
        }
    }

    fun login(uid: String) {
        _state.update { it.copy(isLoading = true, error = null) }

        CometChatUIKit.login(uid, object : CometChat.CallbackListener<User>() {
            override fun onSuccess(user: User?) {
                Log.d(TAG, "Login successful: ${user?.uid}")
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

    private fun establishSocketConnection() {
        CometChat.connect(object : CometChat.CallbackListener<String?>() {
            override fun onSuccess(result: String?) {
                _state.update { it.copy(isLoading = false, isLoggedIn = true) }
            }

            override fun onError(exception: CometChatException?) {
                _state.update { it.copy(isLoading = false, isLoggedIn = true) }
            }
        })
    }
}
