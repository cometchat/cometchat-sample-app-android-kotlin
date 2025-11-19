package com.cometchat.chatuikit.shared.events;

import androidx.annotation.DrawableRes;

import com.cometchat.chat.models.AIAssistantMessage;
import com.cometchat.chat.models.AIToolArgumentMessage;
import com.cometchat.chat.models.AIToolResultMessage;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.CustomMessage;
import com.cometchat.chat.models.InteractionReceipt;
import com.cometchat.chat.models.MediaMessage;
import com.cometchat.chat.models.MessageReceipt;
import com.cometchat.chat.models.ReactionEvent;
import com.cometchat.chat.models.TextMessage;
import com.cometchat.chat.models.TransientMessage;
import com.cometchat.chat.models.TypingIndicator;
import com.cometchat.chatuikit.shared.constants.MessageStatus;
import com.cometchat.chatuikit.shared.models.interactivemessage.CardMessage;
import com.cometchat.chatuikit.shared.models.interactivemessage.CustomInteractiveMessage;
import com.cometchat.chatuikit.shared.models.interactivemessage.FormMessage;
import com.cometchat.chatuikit.shared.models.interactivemessage.SchedulerMessage;

import java.util.HashMap;

/**
 * Abstract class for handling CometChat message events.
 */
public abstract class CometChatMessageEvents {
    private static final String TAG = CometChatMessageEvents.class.getSimpleName();
    /**
     * Map to store the registered message event listeners.
     */
    public static final HashMap<String, CometChatMessageEvents> messageEvents = new HashMap<>();

    /**
     * Called when a message is sent.
     *
     * @param baseMessage The sent message object.
     * @param status      The status of the sent message.
     */
    public void ccMessageSent(BaseMessage baseMessage, @MessageStatus int status) {
    }

    /**
     * Called when a message is edited.
     *
     * @param baseMessage The edited message object.
     * @param status      The status of the edited message.
     */
    public void ccMessageEdited(BaseMessage baseMessage, @MessageStatus int status) {
    }

    /**
     * Called when a message is deleted.
     *
     * @param baseMessage The deleted message object.
     */
    public void ccMessageDeleted(BaseMessage baseMessage) {
    }

    /**
     * Called when a message is read.
     *
     * @param baseMessage The read message object.
     */
    public void ccMessageRead(BaseMessage baseMessage) {
    }

    /**
     * Called when a live reaction is received.
     *
     * @param icon The drawable resource ID of the live reaction.
     */
    public void ccLiveReaction(@DrawableRes int icon) {
    }

    /**
     * Called when a reply to a message is sent/in progress.
     *
     * @param baseMessage The replied message object.
     * @param status      The status of the reply message.
     */
    public void ccReplyToMessage(BaseMessage baseMessage, @MessageStatus int status) {
    }

    public void onTextMessageReceived(TextMessage textMessage) {
    }

    public void onMediaMessageReceived(MediaMessage mediaMessage) {
    }

    public void onCustomMessageReceived(CustomMessage customMessage) {
    }

    public void onTypingStarted(TypingIndicator typingIndicator) {
    }

    public void onTypingEnded(TypingIndicator typingIndicator) {
    }

    public void onMessagesDelivered(MessageReceipt messageReceipt) {
    }

    public void onMessagesRead(MessageReceipt messageReceipt) {
    }

    public void onMessageEdited(BaseMessage message) {
    }

    public void onMessageDeleted(BaseMessage message) {
    }

    public void onTransientMessageReceived(TransientMessage message) {
    }

    public void onFormMessageReceived(FormMessage formMessage) {
    }

    public void onSchedulerMessageReceived(SchedulerMessage schedulerMessage) {
    }

    public void onCardMessageReceived(CardMessage cardMessage) {
    }

    public void onInteractionGoalCompleted(InteractionReceipt interactionReceipt) {
    }

    public void onCustomInteractiveMessageReceived(CustomInteractiveMessage customInteractiveMessage) {
    }

    public void onMessageReactionAdded(ReactionEvent reactionEvent) {
    }

    public void onMessageReactionRemoved(ReactionEvent reactionEvent) {
    }

    public void onMessagesDeliveredToAll(MessageReceipt messageReceipt) {
    }

    public void onMessagesReadByAll(MessageReceipt messageReceipt) {
    }

    public void onMessageModerated(BaseMessage baseMessage) {
    }

    public void onAIToolArgumentsReceived(AIToolArgumentMessage aiToolArgumentMessage) {
    }

    public void onAIToolResultReceived(AIToolResultMessage aiToolResultMessage) {
    }

    public void onAIAssistantMessageReceived(AIAssistantMessage aiAssistantMessage) {
    }

    /**
     * Adds a message event listener with the specified tag.
     *
     * @param TAG    The tag to identify the listener.
     * @param events The message event listener to be added.
     */
    public static void addListener(String TAG, CometChatMessageEvents events) {
        messageEvents.put(TAG, events);
    }

    /**
     * Removes the message event listener associated with the specified tag.
     *
     * @param TAG The tag of the listener to be removed.
     */
    public static void removeListener(String TAG) {
        messageEvents.remove(TAG);
    }
}
