package com.cometchat.chatuikit.shared.models;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import com.cometchat.chat.models.AIAssistantMessage;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;

public class StreamMessage extends AIAssistantMessage {

    private boolean isStreamingInterrupted;

    @SuppressLint("WrongConstant")
    public StreamMessage(@NonNull String receiverUid,
                         @NonNull String receiverType,
                         @NonNull String text) {
        super(receiverUid, receiverType, text);
        setType(UIKitConstants.MessageType.STREAM);
        setCategory(UIKitConstants.MessageCategory.STREAM);
    }

    /**
     * Returns true if the stream message delivery failed
     */
    public boolean isStreamingInterrupted() {
        return isStreamingInterrupted;
    }

    /**
     * Marks this stream message as failed/succeeded
     */
    public void setStreamingInterrupted(boolean deliveryFailed) {
        this.isStreamingInterrupted = deliveryFailed;
    }
}
