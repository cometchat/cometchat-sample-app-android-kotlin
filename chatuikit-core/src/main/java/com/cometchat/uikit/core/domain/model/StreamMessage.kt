package com.cometchat.uikit.core.domain.model

import com.cometchat.chat.models.AIAssistantMessage
import com.cometchat.uikit.core.constants.UIKitConstants

class StreamMessage(
    receiverUid: String,
    receiverType: String,
    text: String
) : AIAssistantMessage(receiverUid, receiverType, text) {

    var isStreamingInterrupted: Boolean = false

    init {
        type = UIKitConstants.MessageType.STREAM
        category = UIKitConstants.MessageCategory.STREAM
    }

    /**
     * Creates a shallow clone of this [StreamMessage].
     *
     * Delegates to [AIAssistantMessage.clone] for all inherited fields and
     * then copies the [isStreamingInterrupted] flag, which is the only field
     * declared at this level.
     *
     * This enables the ViewModel to replace in-place mutations with new-object
     * emissions on the StateFlow, eliminating the need for the SharedFlow-based
     * `messageUpdateTick` workaround in the Compose layer.
     *
     * @return A new [StreamMessage] instance with the same field values.
     */
    public override fun clone(): StreamMessage {
        val cloned = super.clone() as StreamMessage
        cloned.isStreamingInterrupted = this.isStreamingInterrupted
        return cloned
    }

    override fun equals(p0: Any?): Boolean {
        return false;
    }


}
