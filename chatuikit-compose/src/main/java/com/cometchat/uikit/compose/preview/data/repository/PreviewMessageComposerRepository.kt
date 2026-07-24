package com.cometchat.uikit.compose.preview.data.repository

import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.CustomMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.uikit.core.domain.repository.MessageComposerRepository

/**
 * A mock repository implementation for Compose Previews of CometChatMessageComposer.
 * Returns success results without making any SDK calls.
 *
 * @param simulateError If true, all operations will return failure results.
 */
class PreviewMessageComposerRepository(
    private val simulateError: Boolean = false
) : MessageComposerRepository {

    override suspend fun sendTextMessage(message: TextMessage): Result<TextMessage> {
        if (simulateError) {
            return Result.failure(
                com.cometchat.chat.exceptions.CometChatException(
                    "PREVIEW_ERROR",
                    "Simulated error for preview"
                )
            )
        }
        return Result.success(message)
    }

    override suspend fun sendMediaMessage(message: MediaMessage): Result<MediaMessage> {
        if (simulateError) {
            return Result.failure(
                com.cometchat.chat.exceptions.CometChatException(
                    "PREVIEW_ERROR",
                    "Simulated error for preview"
                )
            )
        }
        return Result.success(message)
    }

    override suspend fun sendCustomMessage(message: CustomMessage): Result<CustomMessage> {
        if (simulateError) {
            return Result.failure(
                com.cometchat.chat.exceptions.CometChatException(
                    "PREVIEW_ERROR",
                    "Simulated error for preview"
                )
            )
        }
        return Result.success(message)
    }

    override suspend fun editMessage(message: BaseMessage): Result<BaseMessage> {
        if (simulateError) {
            return Result.failure(
                com.cometchat.chat.exceptions.CometChatException(
                    "PREVIEW_ERROR",
                    "Simulated error for preview"
                )
            )
        }
        return Result.success(message)
    }
}
