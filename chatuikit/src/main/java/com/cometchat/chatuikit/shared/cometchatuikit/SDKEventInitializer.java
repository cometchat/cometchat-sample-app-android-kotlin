package com.cometchat.chatuikit.shared.cometchatuikit;

import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.models.AIAssistantMessage;
import com.cometchat.chat.models.AIToolArgumentMessage;
import com.cometchat.chat.models.AIToolResultMessage;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.CustomMessage;
import com.cometchat.chat.models.InteractionReceipt;
import com.cometchat.chat.models.InteractiveMessage;
import com.cometchat.chat.models.MediaMessage;
import com.cometchat.chat.models.MessageReceipt;
import com.cometchat.chat.models.ReactionEvent;
import com.cometchat.chat.models.TextMessage;
import com.cometchat.chat.models.TransientMessage;
import com.cometchat.chat.models.TypingIndicator;
import com.cometchat.chatuikit.shared.models.interactivemessage.CardMessage;
import com.cometchat.chatuikit.shared.models.interactivemessage.CustomInteractiveMessage;
import com.cometchat.chatuikit.shared.models.interactivemessage.FormMessage;
import com.cometchat.chatuikit.shared.models.interactivemessage.SchedulerMessage;
import com.cometchat.chatuikit.shared.resources.utils.Utils;

final class SDKEventInitializer {
    private static final String TAG = SDKEventInitializer.class.getSimpleName();

    public static void addMessageListener() {
        CometChat.addMessageListener("internalListening_@#$%" + SDKEventInitializer.class.getSimpleName(), new CometChat.MessageListener() {
            @Override
            public void onTextMessageReceived(TextMessage textMessage) {
                CometChatUIKitHelper.onTextMessageReceived(textMessage);
            }

            @Override
            public void onMediaMessageReceived(MediaMessage mediaMessage) {
                CometChatUIKitHelper.onMediaMessageReceived(mediaMessage);
            }

            @Override
            public void onCustomMessageReceived(CustomMessage customMessage) {
                CometChatUIKitHelper.onCustomMessageReceived(customMessage);
            }

            @Override
            public void onInteractiveMessageReceived(InteractiveMessage interactiveMessage) {
                BaseMessage baseMessage = Utils.convertToUIKitMessage(interactiveMessage);
                if (baseMessage instanceof FormMessage)
                    CometChatUIKitHelper.onFormMessageReceived((FormMessage) baseMessage);
                else if (baseMessage instanceof CardMessage)
                    CometChatUIKitHelper.onCardMessageReceived((CardMessage) baseMessage);
                else if (baseMessage instanceof SchedulerMessage)
                    CometChatUIKitHelper.onSchedulerMessageReceived((SchedulerMessage) baseMessage);
                else if (baseMessage instanceof CustomInteractiveMessage)
                    CometChatUIKitHelper.onCustomInteractiveMessageReceived((CustomInteractiveMessage) baseMessage);
            }

            @Override
            public void onTypingStarted(TypingIndicator typingIndicator) {
                CometChatUIKitHelper.onTypingStarted(typingIndicator);
            }

            @Override
            public void onTypingEnded(TypingIndicator typingIndicator) {
                CometChatUIKitHelper.onTypingEnded(typingIndicator);
            }

            @Override
            public void onMessagesDelivered(MessageReceipt messageReceipt) {
                CometChatUIKitHelper.onMessagesDelivered(messageReceipt);
            }

            @Override
            public void onMessagesRead(MessageReceipt messageReceipt) {
                CometChatUIKitHelper.onMessagesRead(messageReceipt);
            }

            @Override
            public void onInteractionGoalCompleted(InteractionReceipt interactionReceipt) {
                CometChatUIKitHelper.onInteractionGoalCompleted(interactionReceipt);
            }

            @Override
            public void onMessageEdited(BaseMessage baseMessage) {
                CometChatUIKitHelper.onMessageEdited(Utils.convertToUIKitMessage(baseMessage));
            }

            @Override
            public void onMessageDeleted(BaseMessage baseMessage) {
                CometChatUIKitHelper.onMessageDeleted(Utils.convertToUIKitMessage(baseMessage));
            }

            @Override
            public void onTransientMessageReceived(TransientMessage transientMessage) {
                CometChatUIKitHelper.onTransientMessageReceived(transientMessage);
            }

            @Override
            public void onMessageReactionAdded(ReactionEvent reactionEvent) {
                CometChatUIKitHelper.onMessageReactionAdded(reactionEvent);
            }

            @Override
            public void onMessageReactionRemoved(ReactionEvent reactionEvent) {
                CometChatUIKitHelper.onMessageReactionRemoved(reactionEvent);
            }

            @Override
            public void onMessagesDeliveredToAll(MessageReceipt messageReceipt) {
                CometChatUIKitHelper.onMessagesDeliveredToAll(messageReceipt);
            }

            @Override
            public void onMessagesReadByAll(MessageReceipt messageReceipt) {
                CometChatUIKitHelper.onMessagesReadByAll(messageReceipt);
            }

            @Override
            public void onMessageModerated(BaseMessage baseMessage) {
                CometChatUIKitHelper.onMessageModerated(baseMessage);
            }

            @Override
            public void onAIAssistantMessageReceived(AIAssistantMessage aiAssistantMessage) {
                CometChatUIKitHelper.onAIAssistantMessageReceived(aiAssistantMessage);
            }

            @Override
            public void onAIToolResultReceived(AIToolResultMessage aiToolResultMessage) {
                CometChatUIKitHelper.onAIToolResultReceived(aiToolResultMessage);
            }

            @Override
            public void onAIToolArgumentsReceived(AIToolArgumentMessage aiToolArgumentMessage) {
                CometChatUIKitHelper.onAIToolArgumentsReceived(aiToolArgumentMessage);
            }
        });
    }
}
