package com.cometchat.uikit.compose.presentation.messagelist.ui

import android.util.Log
import androidx.test.core.app.ApplicationProvider
import com.cometchat.chat.core.AppSettings
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Helper object to initialize CometChat SDK and log in for compose instrumented tests.
 *
 * The CometChatMessageList composable internally calls `CometChat.getLoggedInUser()`
 * and accesses `SQLiteManager` when rendering messages, which requires the SDK to be
 * initialized and a user to be logged in. This helper performs initialization with
 * real credentials and logs in as `cometchat-uid-2` so that message alignment
 * (incoming vs outgoing) is correctly determined.
 *
 * Call [ensureInitialized] once in `@Before` of any test that renders the
 * CometChatMessageList composable with messages.
 */
object MessageListComposeTestHelper {

    private const val TAG = "MessageListTestHelper"

    private const val APP_ID = "278059f315a564b4"
    private const val AUTH_KEY = "5bb2416b7eb003c1f94234c26178a4b053c66b97"
    private const val REGION = "in"
    private const val LOGIN_UID = "cometchat-uid-2"

    @Volatile
    private var initialized = false

    /**
     * The UID of the logged-in test user. Use this when creating outgoing messages
     * so that the message alignment logic correctly identifies them as outgoing.
     */
    const val LOGGED_IN_USER_UID = LOGIN_UID

    /**
     * Initializes CometChat SDK with real credentials and logs in as [LOGIN_UID].
     * Safe to call multiple times — only initializes once.
     *
     * This method blocks until both init and login complete (or timeout after 30s).
     */
    fun ensureInitialized() {
        if (initialized) return
        synchronized(this) {
            if (initialized) return

            val context = ApplicationProvider.getApplicationContext<android.app.Application>()
            val appSettings = AppSettings.AppSettingsBuilder()
                .setRegion(REGION)
                .autoEstablishSocketConnection(false)
                .build()

            // Step 1: Initialize the SDK
            val initLatch = CountDownLatch(1)
            var initSuccess = false

            CometChat.init(
                context,
                APP_ID,
                appSettings,
                object : CometChat.CallbackListener<String>() {
                    override fun onSuccess(result: String?) {
                        Log.d(TAG, "CometChat.init() succeeded")
                        initSuccess = true
                        initLatch.countDown()
                    }

                    override fun onError(e: CometChatException?) {
                        Log.e(TAG, "CometChat.init() failed: ${e?.message}")
                        // Mark init as done even on failure so tests don't hang
                        initLatch.countDown()
                    }
                }
            )

            if (!initLatch.await(30, TimeUnit.SECONDS)) {
                Log.e(TAG, "CometChat.init() timed out after 30s")
                initialized = true
                return
            }

            if (!initSuccess) {
                Log.e(TAG, "CometChat init failed, tests may not work correctly")
                initialized = true
                return
            }

            // Step 2: Check if already logged in
            val loggedInUser = CometChat.getLoggedInUser()
            if (loggedInUser != null) {
                Log.d(TAG, "Already logged in as ${loggedInUser.uid}")
                initialized = true
                return
            }

            // Step 3: Log in with the test user
            val loginLatch = CountDownLatch(1)
            var loginSuccess = false

            CometChat.login(
                LOGIN_UID,
                AUTH_KEY,
                object : CometChat.CallbackListener<User>() {
                    override fun onSuccess(user: User?) {
                        Log.d(TAG, "CometChat.login() succeeded for uid=${user?.uid}")
                        loginSuccess = true
                        loginLatch.countDown()
                    }

                    override fun onError(e: CometChatException?) {
                        Log.e(TAG, "CometChat.login() failed: code=${e?.code}, message=${e?.message}")
                        loginLatch.countDown()
                    }
                }
            )

            if (!loginLatch.await(30, TimeUnit.SECONDS)) {
                Log.e(TAG, "CometChat.login() timed out after 30s")
            }

            if (loginSuccess) {
                Log.d(TAG, "Init + Login complete. LoggedInUser uid=${CometChat.getLoggedInUser()?.uid}")
            } else {
                Log.e(TAG, "Login failed. Tests requiring outgoing message detection may fail.")
            }

            initialized = true
        }
    }
}
