package com.cometchat.uikit.core

import android.content.Context
import android.util.Log
import com.cometchat.calls.core.CallAppSettings
import com.cometchat.calls.core.CometChatCalls
import com.cometchat.chat.core.AppSettings
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.ConversationUpdateSettings
import com.cometchat.chat.models.CustomMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.events.CometChatMessageEvent
import com.cometchat.uikit.core.events.MessageStatus
import org.json.JSONObject

/**
 * The CometChatUIKit object provides utility methods for initializing and
 * interacting with the CometChat SDK in Jetpack Compose applications.
 * 
 * This is a simplified version without extension features.
 * 
 * Usage:
 * ```kotlin
 * val settings = UIKitSettings.UIKitSettingsBuilder()
 *     .setAppId("YOUR_APP_ID")
 *     .setRegion("YOUR_REGION")
 *     .setAuthKey("YOUR_AUTH_KEY")
 *     .build()
 * 
 * CometChatUIKit.init(context, settings, object : CometChat.CallbackListener<String>() {
 *     override fun onSuccess(result: String) { }
 *     override fun onError(e: CometChatException) { }
 * })
 * ```
 */
object CometChatUIKit {
    private const val TAG = "CometChatUIKit"

    private var authenticationSettings: UIKitSettings? = null
    private var isCallsSDKInitialized: Boolean = false
    private var storedSessionSettingsBuilder: CometChatCalls.SessionSettingsBuilder? = null

    /**
     * Initializes the CometChat SDK with the provided authentication settings.
     *
     * @param context The context of the calling activity or application
     * @param authSettings The UIKitSettings object containing the authentication settings
     * @param callbackListener The callback listener to handle initialization success or failure
     */
    fun init(
        context: Context,
        authSettings: UIKitSettings,
        callbackListener: CometChat.CallbackListener<String>?
    ) {
        authenticationSettings = authSettings

        if (!checkAuthSettings(callbackListener)) return

        val appSettingsBuilder = AppSettings.AppSettingsBuilder()

        authenticationSettings?.let { settings ->
            val roles = settings.roles
            when {
                !roles.isNullOrEmpty() -> appSettingsBuilder.subscribePresenceForRoles(roles)
                settings.subscriptionType == "ALL_USERS" -> appSettingsBuilder.subscribePresenceForAllUsers()
                settings.subscriptionType == "FRIENDS" -> appSettingsBuilder.subscribePresenceForFriends()
            }

            appSettingsBuilder.autoEstablishSocketConnection(settings.autoEstablishSocketConnection)
            settings.region?.let { appSettingsBuilder.setRegion(it) }
            settings.overrideAdminHost?.let { appSettingsBuilder.overrideAdminHost(it) }
            settings.overrideClientHost?.let { appSettingsBuilder.overrideClientHost(it) }
        }

        val appSettings = appSettingsBuilder.build()

        val appId = authenticationSettings?.appId ?: return

        CometChat.init(
            context,
            appId,
            appSettings,
            object : CometChat.CallbackListener<String>() {
                override fun onSuccess(result: String) {
                    CometChat.setSource("uikit-v6", "android", "kotlin")

                    // Auto-initialize CometChatCalls if enableCalling is true
                    if (authenticationSettings?.enableCalling == true) {
                        initCometChatCalls(context, callbackListener, result)
                    } else {
                        callbackListener?.onSuccess(result)
                    }
                }

                override fun onError(e: CometChatException?) {
                    e?.let { callbackListener?.onError(it) }
                }
            }
        )
    }

    /**
     * Initializes the CometChat UIKit by reading configuration from the
     * `cometchat-settings.json` file located in the app's assets directory.
     *
     * This method reads the `uiKit` and `credentials` sections from the settings file,
     * builds [UIKitSettings], and delegates to the Chat SDK's `initFromSettings` method
     * which persists the `integrationSource` flag for telemetry attribution.
     *
     * The settings file must contain at minimum `appId` and `region` at the root level.
     * The `uiKit` section and `credentials` section are optional with sensible defaults.
     *
     * @param context The context of the calling activity or application
     * @param callbackListener The callback listener to handle initialization success or failure
     *
     */
    fun initFromSettings(
        context: Context,
        callbackListener: CometChat.CallbackListener<String>?
    ) {
        // 1. Read cometchat-settings.json from assets
        val settingsJson: JSONObject
        try {
            val jsonString = context.assets.open("cometchat-settings.json").bufferedReader().use { it.readText() }
            settingsJson = JSONObject(jsonString)
        } catch (e: Exception) {
            callbackListener?.onError(
                CometChatException(
                    "ERR_SETTINGS_FILE_NOT_FOUND",
                    "cometchat-settings.json not found. Ensure the file exists at app/src/main/assets/.",
                    e.message ?: ""
                )
            )
            return
        }

        // 2. Parse uiKit + credentials sections and build UIKitSettings
        val appId = settingsJson.optString("appId", "")
        if (appId.isEmpty()) {
            callbackListener?.onError(
                CometChatException("ERR_SETTINGS_INVALID", "appId is required in cometchat-settings.json.", "")
            )
            return
        }

        val region = settingsJson.optString("region", "")
        if (region.isEmpty()) {
            callbackListener?.onError(
                CometChatException("ERR_SETTINGS_INVALID", "region is required in cometchat-settings.json.", "")
            )
            return
        }

        val credentials = settingsJson.optJSONObject("credentials")
        val authKey = credentials?.optString("authKey", "") ?: ""

        val uiKitSection = settingsJson.optJSONObject("uiKit")
        val subscribePresenceForAllUsers = uiKitSection?.optBoolean("subscribePresenceForAllUsers", true) ?: true
        // Calling toggle for the file-based path (ENG-37369) — without this the
        // settings door could never initialize the Calls SDK at all (UIKitSettings
        // defaults enableCalling to false and the file was the only input here).
        val enableCalling = uiKitSection?.optBoolean("enableCalling", false) ?: false

        val settingsBuilder = UIKitSettings.UIKitSettingsBuilder()
            .setAppId(appId)
            .setRegion(region)
            .setEnableCalling(enableCalling)

        if (authKey.isNotEmpty()) {
            settingsBuilder.setAuthKey(authKey)
        }

        if (subscribePresenceForAllUsers) {
            settingsBuilder.subscribePresenceForAllUsers()
        }

        authenticationSettings = settingsBuilder.build()

        // 3. Delegate to Chat SDK's initFromSettings which reads the chatSDK section
        //    and persists integrationSource = "ai-agent"
        CometChat.initFromSettings(
            context,
            object : CometChat.CallbackListener<String>() {
                override fun onSuccess(result: String) {
                    CometChat.setSource("uikit-v6", "android", "kotlin")

                    // 4. Auto-initialize CometChatCalls if enableCalling is true.
                    //    fromSettings = true routes the Calls SDK through its own
                    //    telemetry-aware initFromSettings so integrationSource =
                    //    "ai-agent" propagates past the Chat SDK (ENG-37369).
                    if (authenticationSettings?.enableCalling == true) {
                        initCometChatCalls(context, callbackListener, result, fromSettings = true)
                    } else {
                        callbackListener?.onSuccess(result)
                    }
                }

                override fun onError(e: CometChatException?) {
                    e?.let { callbackListener?.onError(it) }
                }
            }
        )
    }

    /**
     * Initializes the CometChatCalls SDK internally.
     * Called automatically when enableCalling is set to true in UIKitSettings.
     *
     * @param context The application context
     * @param callbackListener The callback listener to notify after Calls SDK initialization
     * @param chatInitResult The result from CometChat SDK initialization
     * @param fromSettings true when the app entered through [initFromSettings] — routes the
     * Calls SDK through its telemetry-aware CometChatCalls.initFromSettings (reads the same
     * assets/cometchat-settings.json and persists integrationSource = "ai-agent", ENG-37369);
     * false keeps the plain CometChatCalls.init (integrationSource = "manual"), preserving
     * existing behavior for the [init] path.
     */
    private fun initCometChatCalls(
        context: Context,
        callbackListener: CometChat.CallbackListener<String>?,
        chatInitResult: String,
        fromSettings: Boolean = false
    ) {
        // Store the custom sessionSettingsBuilder if provided
        storedSessionSettingsBuilder = authenticationSettings?.callSettingsBuilder as? CometChatCalls.SessionSettingsBuilder

        val callsInitCallback = object : CometChatCalls.CallbackListener<String>() {
            override fun onSuccess(result: String?) {
                Log.d(TAG, "CometChatCalls initialized successfully: $result")
                isCallsSDKInitialized = true
                callbackListener?.onSuccess(chatInitResult)
            }

            override fun onError(e: com.cometchat.calls.exceptions.CometChatException?) {
                Log.e(TAG, "CometChatCalls initialization failed: ${e?.message}")
                isCallsSDKInitialized = false
                // Still report success for Chat SDK, but log the Calls SDK error
                callbackListener?.onSuccess(chatInitResult)
            }
        }

        if (fromSettings) {
            // The settings file is guaranteed present on this path — initFromSettings()
            // already read it. The Calls SDK reads appId/region (+ optional callsSDK
            // host overrides) from the file itself.
            CometChatCalls.initFromSettings(context, callsInitCallback)
            return
        }

        val appId = authenticationSettings?.appId
        val region = authenticationSettings?.region
        val clientHost = authenticationSettings?.overrideClientHost

        if (appId.isNullOrEmpty() || region.isNullOrEmpty()) {
            Log.e(TAG, "Cannot initialize CometChatCalls: missing appId or region")
            callbackListener?.onSuccess(chatInitResult)
            return
        }

        val callAppSettings = CallAppSettings.CallAppSettingBuilder()
            .setAppId(appId)
            .setRegion(region)
            .setHost(clientHost)
            .build()

        CometChatCalls.init(context, callAppSettings, callsInitCallback)
    }

    /**
     * Checks if the authentication settings are valid.
     *
     * @param onError The callback listener to handle the error case
     * @return True if the authentication settings are valid, false otherwise
     */
    private fun checkAuthSettings(onError: CometChat.CallbackListener<*>?): Boolean {
        if (authenticationSettings == null) {
            onError?.onError(
                CometChatException(
                    "ERR",
                    "Authentication null",
                    "Populate authSettings before initializing"
                )
            )
            return false
        }

        if (authenticationSettings?.appId == null) {
            onError?.onError(
                CometChatException(
                    "appIdErr",
                    "APP ID null",
                    "Populate authSettings before initializing"
                )
            )
            return false
        }
        return true
    }

    /**
     * Retrieves the currently logged-in user.
     *
     * @return The User object representing the logged-in user, or null if no user is logged in
     */
    fun getLoggedInUser(): User? = CometChat.getLoggedInUser()

    /**
     * Checks if the SDK has been initialized.
     *
     * @return True if the SDK is initialized, false otherwise
     */
    fun isSDKInitialized(): Boolean = CometChat.isInitialized()

    /**
     * Checks if the CometChatCalls SDK has been initialized.
     * This will return true only if enableCalling was set to true in UIKitSettings
     * and the Calls SDK was successfully initialized.
     *
     * @return True if the Calls SDK is initialized, false otherwise
     */
    fun isCallsSDKInitialized(): Boolean = isCallsSDKInitialized

    /**
     * Gets the custom SessionSettingsBuilder if one was provided during initialization,
     * or null if using defaults.
     *
     * This can be used by call components to get the configured SessionSettingsBuilder
     * for joining sessions. If null, components should create a default SessionSettingsBuilder.
     *
     * @return The custom CometChatCalls.SessionSettingsBuilder if provided, null otherwise
     */
    fun getSessionSettingsBuilder(): CometChatCalls.SessionSettingsBuilder? = storedSessionSettingsBuilder

    /**
     * Logs in a user with the specified UID.
     *
     * @param uid The UID of the user to be logged in
     * @param callbackListener The callback listener to handle login success or failure
     */
    fun login(uid: String, callbackListener: CometChat.CallbackListener<User>?) {
        if (!checkAuthSettings(callbackListener)) return

        val authKey = authenticationSettings?.authKey ?: ""
        val loggedInUser = getLoggedInUser()
        if (loggedInUser == null || loggedInUser.uid != uid) {
            CometChat.login(
                uid,
                authKey,
                object : CometChat.CallbackListener<User>() {
                    override fun onSuccess(user: User) {
                        if (authenticationSettings?.enableCalling == true && isCallsSDKInitialized) {
                            loginCometChatCalls(user, callbackListener)
                        } else {
                            callbackListener?.onSuccess(user)
                        }
                    }

                    override fun onError(e: CometChatException?) {
                        e?.let { callbackListener?.onError(it) }
                    }
                }
            )
        } else {
            callbackListener?.onSuccess(loggedInUser)
        }
    }

    /**
     * Logs in a user with the provided authentication token.
     *
     * @param authToken The authentication token for the user
     * @param callbackListener The callback listener to handle the login result
     */
    fun loginWithAuthToken(authToken: String, callbackListener: CometChat.CallbackListener<User>?) {
        if (!checkAuthSettings(callbackListener)) return

        if (getLoggedInUser() == null) {
            CometChat.login(
                authToken,
                object : CometChat.CallbackListener<User>() {
                    override fun onSuccess(user: User) {
                        if (authenticationSettings?.enableCalling == true && isCallsSDKInitialized) {
                            loginCometChatCalls(user, callbackListener)
                        } else {
                            callbackListener?.onSuccess(user)
                        }
                    }
                    override fun onError(e: CometChatException?) {
                        e?.let { callbackListener?.onError(it) }
                    }
                }
            )
        } else {
            callbackListener?.onSuccess(getLoggedInUser())
        }
    }

    /**
     * Logs in to the CometChatCalls SDK using the current user's auth token.
     * Called automatically after successful CometChat login when calling is enabled.
     * v5 requires CometChatCalls.login() to cache the auth token internally.
     */
    private fun loginCometChatCalls(user: User, callbackListener: CometChat.CallbackListener<User>?) {
        val authToken = CometChat.getUserAuthToken()
        CometChatCalls.login(authToken, object : CometChatCalls.CallbackListener<com.cometchat.calls.model.CallUser>() {
            override fun onSuccess(callUser: com.cometchat.calls.model.CallUser?) {
                callbackListener?.onSuccess(user)
                Log.d(TAG, "CometChatCalls login successful")
            }

            override fun onError(e: com.cometchat.calls.exceptions.CometChatException?) {
                Log.e(TAG, "CometChatCalls login failed: ${e?.message}")
                callbackListener?.onError(CometChatException(e?.code ?: "ERR", e?.message ?: "Unknown error"))
            }
        })
    }

    /**
     * Logs out the currently logged-in user.
     *
     * @param callbackListener The callback listener to handle the logout result
     */
    fun logout(callbackListener: CometChat.CallbackListener<String>?) {
        CometChat.logout(object : CometChat.CallbackListener<String>() {
            override fun onSuccess(successMessage: String) {
                // Cleanup the events bridge on logou
                if (authenticationSettings?.enableCalling == true && isCallsSDKInitialized) {
                    CometChatCalls.logout(object : CometChatCalls.CallbackListener<String>() {
                        override fun onSuccess(result: String?) {
                            callbackListener?.onSuccess(successMessage)
                        }

                        override fun onError(e: com.cometchat.calls.exceptions.CometChatException?) {
                            e?.let { callbackListener?.onError(CometChatException(e.code, e.message)) }
                        }
                    })
                } else {
                    callbackListener?.onSuccess(successMessage)
                }
            }

            override fun onError(e: CometChatException?) {
                e?.let { callbackListener?.onError(it) }
            }
        })
    }

    /**
     * Creates a new user in the CometChat platform.
     *
     * @param user The user object containing the details of the user to be created
     * @param callbackListener The callback listener to handle the create user result
     */
    fun createUser(user: User, callbackListener: CometChat.CallbackListener<User>?) {
        if (!checkAuthSettings(callbackListener)) return

        val authKey = authenticationSettings?.authKey ?: ""
        CometChat.createUser(
            user,
            authKey,
            object : CometChat.CallbackListener<User>() {
                override fun onSuccess(createdUser: User) {
                    callbackListener?.onSuccess(createdUser)
                }

                override fun onError(e: CometChatException?) {
                    e?.let { callbackListener?.onError(it) }
                }
            }
        )
    }

    /**
     * Sends a text message.
     *
     * @param textMessage The text message to be sent
     * @param callbackListener The callback listener to handle the send result
     */
    fun sendTextMessage(
        textMessage: TextMessage,
        callbackListener: CometChat.CallbackListener<TextMessage>?
    ) {
        if (textMessage.sender == null) {
            textMessage.sender = CometChat.getLoggedInUser()
        }
        if (textMessage.muid.isNullOrEmpty()) {
            textMessage.muid = System.currentTimeMillis().toString()
        }
        if (textMessage.sentAt == 0L) {
            textMessage.sentAt = System.currentTimeMillis() / 1000
        }

        android.util.Log.d(TAG, "sendTextMessage: text='${textMessage.text}', mentionedUsers=${textMessage.mentionedUsers?.map { it.uid }}")

        // Emit IN_PROGRESS event
        CometChatEvents.emitMessageEvent(
            CometChatMessageEvent.MessageSent(textMessage, MessageStatus.IN_PROGRESS)
        )

        CometChat.sendMessage(
            textMessage,
            object : CometChat.CallbackListener<TextMessage>() {
                override fun onSuccess(message: TextMessage) {
                    // Emit SUCCESS event
                    CometChatEvents.emitMessageEvent(
                        CometChatMessageEvent.MessageSent(message, MessageStatus.SUCCESS)
                    )
                    callbackListener?.onSuccess(message)
                }

                override fun onError(e: CometChatException?) {
                    textMessage.metadata = placeErrorObjectInMetaData(e)
                    // Emit ERROR event
                    CometChatEvents.emitMessageEvent(
                        CometChatMessageEvent.MessageSent(textMessage, MessageStatus.ERROR)
                    )
                    e?.let { callbackListener?.onError(it) }
                }
            }
        )
    }

    /**
     * Sends a media message.
     *
     * @param mediaMessage The media message to be sent
     * @param callbackListener The callback listener to handle the send result
     */
    fun sendMediaMessage(
        mediaMessage: MediaMessage,
        callbackListener: CometChat.CallbackListener<MediaMessage>?
    ) {
        android.util.Log.d(
            "CometChatUIKit",
            "sendMediaMessage: file=${mediaMessage.file?.absolutePath}, fileSize=${mediaMessage.file?.length()}, type=${mediaMessage.type}"
        )

        if (mediaMessage.sender == null) {
            mediaMessage.sender = CometChat.getLoggedInUser()
        }
        if (mediaMessage.muid.isNullOrEmpty()) {
            mediaMessage.muid = System.currentTimeMillis().toString()
        }
        if (mediaMessage.sentAt == 0L) {
            mediaMessage.sentAt = System.currentTimeMillis() / 1000
        }

        // Emit IN_PROGRESS event
        CometChatEvents.emitMessageEvent(
            CometChatMessageEvent.MessageSent(mediaMessage, MessageStatus.IN_PROGRESS)
        )

        CometChat.sendMediaMessage(
            mediaMessage,
            object : CometChat.CallbackListener<MediaMessage>() {
                override fun onSuccess(message: MediaMessage) {
                    android.util.Log.d(
                        "CometChatUIKit",
                        "sendMediaMessage SUCCESS: id=${message.id}, attachment=${message.attachment}, attachmentFileSize=${message.attachment?.fileSize}"
                    )
                    // Emit SUCCESS event
                    CometChatEvents.emitMessageEvent(
                        CometChatMessageEvent.MessageSent(message, MessageStatus.SUCCESS)
                    )
                    callbackListener?.onSuccess(message)
                }

                override fun onError(e: CometChatException?) {
                    android.util.Log.e("CometChatUIKit", "sendMediaMessage ERROR: code=${e?.code}, message=${e?.message}")
                    mediaMessage.metadata = placeErrorObjectInMetaData(e)
                    // Emit ERROR event
                    CometChatEvents.emitMessageEvent(
                        CometChatMessageEvent.MessageSent(mediaMessage, MessageStatus.ERROR)
                    )
                    e?.let { callbackListener?.onError(it) }
                }
            }
        )
    }

    /**
     * Sends a custom message.
     *
     * @param customMessage The custom message to be sent
     * @param callbackListener The callback listener to handle the send result
     */
    fun sendCustomMessage(
        customMessage: CustomMessage,
        callbackListener: CometChat.CallbackListener<CustomMessage>?
    ) {
        if (customMessage.sender == null) {
            customMessage.sender = CometChat.getLoggedInUser()
        }
        if (customMessage.muid.isNullOrEmpty()) {
            customMessage.muid = System.currentTimeMillis().toString()
        }
        if (customMessage.sentAt == 0L) {
            customMessage.sentAt = System.currentTimeMillis() / 1000
        }
        customMessage.shouldSendNotification(true)

        // Emit IN_PROGRESS event
        CometChatEvents.emitMessageEvent(
            CometChatMessageEvent.MessageSent(customMessage, MessageStatus.IN_PROGRESS)
        )

        CometChat.sendCustomMessage(
            customMessage,
            object : CometChat.CallbackListener<CustomMessage>() {
                override fun onSuccess(message: CustomMessage) {
                    // Emit SUCCESS event
                    CometChatEvents.emitMessageEvent(
                        CometChatMessageEvent.MessageSent(message, MessageStatus.SUCCESS)
                    )
                    callbackListener?.onSuccess(message)
                }

                override fun onError(e: CometChatException?) {
                    customMessage.metadata = placeErrorObjectInMetaData(e)
                    // Emit ERROR event
                    CometChatEvents.emitMessageEvent(
                        CometChatMessageEvent.MessageSent(customMessage, MessageStatus.ERROR)
                    )
                    e?.let { callbackListener?.onError(it) }
                }
            }
        )
    }

    /**
     * Gets the conversation update settings.
     *
     * @return The ConversationUpdateSettings object
     */
    fun getConversationUpdateSettings(): ConversationUpdateSettings {
        return CometChat.getConversationUpdateSettings()
    }

    /**
     * Gets the current authentication settings.
     *
     * @return The UIKitSettings object, or null if not initialized
     */
    fun getAuthSettings(): UIKitSettings? = authenticationSettings

    /**
     * Places error information in the message metadata.
     *
     * @param e The CometChatException to extract error info from
     * @return A JSONObject containing the error information
     */
    private fun placeErrorObjectInMetaData(e: CometChatException?): JSONObject {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("error", e?.message ?: "Unknown error")
            jsonObject.put("errorCode", e?.code ?: "UNKNOWN")
            jsonObject.put("errorDetails", e?.details ?: "")
        } catch (ex: Exception) {
            // Ignore JSON exceptions
        }
        return jsonObject
    }
}
