package com.cometchat.uikit.core.domain.usecase

import com.cometchat.chat.core.Call
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.uikit.core.domain.repository.CallButtonsRepository

/**
 * Use case for initiating a 1-to-1 call with a user.
 * Checks for active calls before initiating a new call.
 */
open class InitiateUserCallUseCase(
    private val repository: CallButtonsRepository
) {
    /**
     * Initiates a call with the specified user.
     * @param receiverId The UID of the user to call
     * @param callType The type of call (audio/video)
     * @return Result containing the initiated Call or an error if active call exists or initiation fails
     */
    open suspend operator fun invoke(receiverId: String, callType: String): Result<Call> {
        if (repository.hasActiveCall()) {
            return Result.failure(
                CometChatException(
                    "ACTIVE_CALL",
                    "Cannot initiate call while another call is active",
                    "An active call is already in progress"
                )
            )
        }
        return repository.initiateUserCall(receiverId, callType)
    }
}
