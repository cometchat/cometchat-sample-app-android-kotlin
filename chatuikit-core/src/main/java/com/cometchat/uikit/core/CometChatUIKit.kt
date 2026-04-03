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
    private var storedCallSettingsBuilder: CometChatCalls.CallSettingsBuilder? = null

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
                    CometChat.setSource("uikit-v5", "android", "kotlin")
                    
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
     * Initializes the CometChatCalls SDK internally.
     * Called automatically when enableCalling is set to true in UIKitSettings.
     *
     * @param context The application context
     * @param callbackListener The callback listener to notify after Calls SDK initialization
     * @param chatInitResult The result from CometChat SDK initialization
     */
    private fun initCometChatCalls(
        context: Context,
        callbackListener: CometChat.CallbackListener<String>?,
        chatInitResult: String
    ) {
        val appId = authenticationSettings?.appId
        val region = authenticationSettings?.region

        if (appId.isNullOrEmpty() || region.isNullOrEmpty()) {
            Log.e(TAG, "Cannot initialize CometChatCalls: missing appId or region")
            callbackListener?.onSuccess(chatInitResult)
            return
        }

        // Store the custom callSettingsBuilder if provided
        storedCallSettingsBuilder = authenticationSettings?.callSettingsBuilder as? CometChatCalls.CallSettingsBuilder

        val callAppSettings = CallAppSettings.CallAppSettingBuilder()
            .setAppId(appId)
            .setRegion(region)
            .build()

        CometChatCalls.init(context, callAppSettings, object : CometChatCalls.CallbackListener<String>() {
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
        })
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
     * Gets the custom CallSettingsBuilder if one was provided during initialization,
     * or null if using defaults.
     *
     * This can be used by call components to get the configured CallSettingsBuilder
     * for starting calls. If null, components should create a default CallSettingsBuilder.
     *
     * @return The custom CometChatCalls.CallSettingsBuilder if provided, null otherwise
     */
    fun getCallSettingsBuilder(): CometChatCalls.CallSettingsBuilder? = storedCallSettingsBuilder

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
                        callbackListener?.onSuccess(user)
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
                        callbackListener?.onSuccess(user)
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
     * Logs out the currently logged-in user.
     *
     * @param callbackListener The callback listener to handle the logout result
     */
    fun logout(callbackListener: CometChat.CallbackListener<String>?) {
        CometChat.logout(object : CometChat.CallbackListener<String>() {
            override fun onSuccess(successMessage: String) {
                // Cleanup the events bridge on logou
                callbackListener?.onSuccess(successMessage)
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
        android.util.Log.d("CometChatUIKit", "sendMediaMessage: file=${mediaMessage.file?.absolutePath}, fileSize=${mediaMessage.file?.length()}, type=${mediaMessage.type}")
        
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
                    android.util.Log.d("CometChatUIKit", "sendMediaMessage SUCCESS: id=${message.id}, attachment=${message.attachment}, attachmentFileSize=${message.attachment?.fileSize}")
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
