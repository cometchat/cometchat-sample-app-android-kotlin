package com.cometchat.sampleapp.compose.ui.login

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User
import com.cometchat.sampleapp.compose.app.SampleApplication
import com.cometchat.sampleapp.compose.utils.AppConstants
import com.cometchat.sampleapp.compose.utils.AppPreferences
import com.cometchat.uikit.core.CometChatUIKit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/**
 * Represents the different states of the login process.
 */
sealed interface LoginState {
    data object Idle : LoginState
    data object Loading : LoginState
    data object Success : LoginState
    data class Error(val message: String) : LoginState
}

/**
 * ViewModel for the Login screen.
 *
 * This ViewModel manages the login flow including:
 * - Fetching sample users from API
 * - Handling user selection and manual UID entry
 * - Initializing the CometChat SDK
 * - Authenticating users with CometChat
 * - Managing login state for UI updates
 *
 * UI parity with master-app-java LoginViewModel.
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "LoginViewModel"
        private const val SAMPLE_APP_USERS_URL = "https://assets.cometchat.io/sampleapp/sampledata.json"
        private const val KEY_USER = "users"
        private const val KEY_UID = "uid"
        private const val KEY_NAME = "name"
        private const val KEY_AVATAR = "avatar"
    }

    private val appPreferences = AppPreferences(application)
    private val sampleApplication = application as SampleApplication

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _selectedUser = MutableStateFlow<User?>(null)
    val selectedUser: StateFlow<User?> = _selectedUser.asStateFlow()

    private val _manualUid = MutableStateFlow("")
    val manualUid: StateFlow<String> = _manualUid.asStateFlow()

    init {
        getSampleUsers()
    }

    /**
     * Fetches sample users from the CometChat sample data API.
     * Matches master-app-java Repository.fetchSampleUsers() logic.
     */
    fun getSampleUsers() {
        viewModelScope.launch {
            try {
                val userList = withContext(Dispatchers.IO) {
                    fetchSampleUsersFromApi()
                }
                _users.value = userList
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch sample users: ${e.message}")
                _users.value = emptyList()
            }
        }
    }

    private fun fetchSampleUsersFromApi(): List<User> {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(SAMPLE_APP_USERS_URL)
            .get()
            .build()

        val response = client.newCall(request).execute()
        if (response.isSuccessful && response.body != null) {
            val jsonString = response.body!!.string()
            return processSampleUserList(jsonString)
        }
        return emptyList()
    }

    private fun processSampleUserList(jsonString: String): List<User> {
        val users = mutableListOf<User>()
        try {
            val jsonObject = JSONObject(jsonString)
            val jsonArray = jsonObject.getJSONArray(KEY_USER)
            for (i in 0 until jsonArray.length()) {
                val userJson = jsonArray.getJSONObject(i)
                val user = User().apply {
                    uid = userJson.getString(KEY_UID)
                    name = userJson.getString(KEY_NAME)
                    avatar = userJson.getString(KEY_AVATAR)
                }
                users.add(user)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing sample users: ${e.message}")
        }
        return users
    }

    /**
     * Handles selection of a sample user.
     */
    fun selectUser(user: User?) {
        _selectedUser.value = if (_selectedUser.value?.uid == user?.uid) null else user
        _manualUid.value = ""
    }

    /**
     * Called when the manual UID input field receives focus.
     * Clears sample user selection.
     */
    fun onManualUidFocused() {
        _selectedUser.value = null
    }

    /**
     * Updates the manual UID value.
     */
    fun setManualUid(uid: String) {
        _manualUid.value = uid
        if (uid.isNotEmpty()) {
            _selectedUser.value = null
        }
    }

    /**
     * Performs login with the given UID.
     * Matches master-app-java LoginViewModel.login() logic.
     */
    fun login(uid: String? = null) {
        val loginUid = uid ?: _selectedUser.value?.uid ?: _manualUid.value.trim()

        if (loginUid.isBlank()) {
            _loginState.value = LoginState.Error("Please select a user or enter a UID")
            return
        }

        _loginState.value = LoginState.Loading

        val appId = appPreferences.getAppId() ?: AppConstants.APP_ID
        val region = appPreferences.getRegion() ?: AppConstants.REGION
        val authKey = appPreferences.getAuthKey() ?: AppConstants.AUTH_KEY

        if (sampleApplication.isSDKInitialized()) {
            performLogin(loginUid)
        } else {
            initializeAndLogin(appId, region, authKey, loginUid)
        }
    }

    private fun initializeAndLogin(
        appId: String,
        region: String,
        authKey: String,
        uid: String
    ) {
        Log.d(TAG, "Initializing CometChat SDK...")

        sampleApplication.initializeCometChat(
            appId = appId,
            region = region,
            authKey = authKey,
            onSuccess = {
                Log.d(TAG, "SDK initialized successfully")
                appPreferences.saveCredentials(appId, region, authKey)
                performLogin(uid)
            },
            onError = { exception ->
                Log.e(TAG, "SDK initialization failed: ${exception.message}")
                _loginState.value = LoginState.Error(
                    exception.message ?: "Failed to initialize CometChat SDK"
                )
            }
        )
    }

    private fun performLogin(uid: String) {
        Log.d(TAG, "Logging in with UID: $uid")

        CometChatUIKit.login(uid, object : CometChat.CallbackListener<User>() {
            override fun onSuccess(user: User?) {
                Log.d(TAG, "Login successful for user: ${user?.uid}")
                appPreferences.saveLoggedInUserUid(uid)
                CometChat.connect(object : CometChat.CallbackListener<String?>() {
                    override fun onSuccess(result: String?) {
                        Log.d(TAG, "Socket connection established")
                        _loginState.value = LoginState.Success
                    }

                    override fun onError(exception: CometChatException?) {
                        Log.w(TAG, "Socket connection failed (non-blocking): ${exception?.message}")
                        _loginState.value = LoginState.Success
                    }
                })
            }

            override fun onError(exception: CometChatException?) {
                Log.e(TAG, "Login failed: ${exception?.message}")
                _loginState.value = LoginState.Error(
                    exception?.message ?: "Login failed. Please check your UID and try again."
                )
            }
        })
    }

    /**
     * Checks if user is already logged in.
     */
    fun checkUserIsNotLoggedIn() {
        if (CometChatUIKit.getLoggedInUser() != null) {
            _loginState.value = LoginState.Success
        }
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}
