package com.cometchat.uikit.core.data.datasource

import com.cometchat.chat.core.Call
import com.cometchat.chat.models.CustomMessage

/**
 * Data source interface for call buttons operations.
 * Defines methods for initiating calls and checking active call state.
 */
interface CallButtonsDataSource {
    /**
     * Initiates a 1-to-1 call with a user.
     * @param receiverId The UID of the user to call
     * @param callType The type of call (audio/video)
     * @return Result containing the initiated Call or an error
     */
    suspend fun initiateUserCall(receiverId: String, callType: String): Result<Call>

    /**
     * Sends a custom message to start a group conference call.
     * @param groupId The GUID of the group
     * @param callType The type of call (audio/video)
     * @return Result containing the sent CustomMessage or an error
     */
    suspend fun sendGroupCallMessage(groupId: String, callType: String): Result<CustomMessage>

    /**
     * Gets the currently active call from CometChat SDK.
     * @return The active Call or null if no call is active
     */
    fun getActiveCall(): Call?

    /**
     * Gets the currently active call from CallingExtension.
     * @return The active Call or null if no call is active
     */
    fun getActiveCallingExtensionCall(): Call?

    /**
     * Checks if there is an active meeting in progress.
     * @return true if a meeting is active, false otherwise
     */
    fun isActiveMeeting(): Boolean
}
