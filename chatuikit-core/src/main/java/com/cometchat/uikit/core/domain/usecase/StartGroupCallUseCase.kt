package com.cometchat.uikit.core.domain.usecase

import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.CustomMessage
import com.cometchat.uikit.core.domain.repository.CallButtonsRepository

/**
 * Use case for starting a group conference call.
 * Checks for active calls before starting a new group call.
 */
open class StartGroupCallUseCase(
    private val repository: CallButtonsRepository
) {
    /**
     * Starts a group conference call by sending a custom message.
     * @param groupId The GUID of the group
     * @param callType The type of call (audio/video)
     * @return Result containing the sent CustomMessage or an error if active call exists or sending fails
     */
    open suspend operator fun invoke(groupId: String, callType: String): Result<CustomMessage> {
        if (repository.hasActiveCall()) {
            return Result.failure(
                CometChatException(
                    "ACTIVE_CALL",
                    "Cannot start call while another call is active",
                    "An active call is already in progress"
                )
            )
        }
        return repository.startGroupCall(groupId, callType)
    }
}
