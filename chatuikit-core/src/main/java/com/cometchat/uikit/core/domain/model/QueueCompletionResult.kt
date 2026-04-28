package com.cometchat.uikit.core.domain.model

import com.cometchat.chat.models.AIAssistantMessage
import com.cometchat.chat.models.AIToolArgumentMessage
import com.cometchat.chat.models.AIToolResultMessage

/**
 * Bundles the final messages delivered upon completion of a streaming run.
 *
 * When a run's event queue has been fully drained and all available final
 * messages have been received, a single [QueueCompletionResult] is delivered
 * via the [QueueCompletionCallback] to eliminate race conditions between
 * independent message types.
 *
 * All fields are nullable because any combination of final messages may be
 * present depending on the AI assistant's response.
 */
data class QueueCompletionResult(
    val aiAssistantMessage: AIAssistantMessage? = null,
    val aiToolResultMessage: AIToolResultMessage? = null,
    val aiToolArgumentMessage: AIToolArgumentMessage? = null
)
