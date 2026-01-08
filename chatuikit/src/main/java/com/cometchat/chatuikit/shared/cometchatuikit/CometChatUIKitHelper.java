package com.cometchat.chatuikit.shared.cometchatuikit;

import android.content.Context;
import android.view.View;

import androidx.annotation.DrawableRes;

import com.cometchat.chat.core.Call;
import com.cometchat.chat.models.AIAssistantMessage;
import com.cometchat.chat.models.AIToolArgumentMessage;
import com.cometchat.chat.models.AIToolResultMessage;
import com.cometchat.chat.models.Action;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.Conversation;
import com.cometchat.chat.models.CustomMessage;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.GroupMember;
import com.cometchat.chat.models.InteractionReceipt;
import com.cometchat.chat.models.MediaMessage;
import com.cometchat.chat.models.MessageReceipt;
import com.cometchat.chat.models.ReactionEvent;
import com.cometchat.chat.models.TextMessage;
import com.cometchat.chat.models.TransientMessage;
import com.cometchat.chat.models.TypingIndicator;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.shared.constants.MessageStatus;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.events.CometChatCallEvents;
import com.cometchat.chatuikit.shared.events.CometChatConversationEvents;
import com.cometchat.chatuikit.shared.events.CometChatGroupEvents;
import com.cometchat.chatuikit.shared.events.CometChatMessageEvents;
import com.cometchat.chatuikit.shared.events.CometChatUIEvents;
import com.cometchat.chatuikit.shared.events.CometChatUserEvents;
import com.cometchat.chatuikit.shared.interfaces.Function1;
import com.cometchat.chatuikit.shared.models.interactivemessage.CardMessage;
import com.cometchat.chatuikit.shared.models.interactivemessage.CustomInteractiveMessage;
import com.cometchat.chatuikit.shared.models.interactivemessage.FormMessage;
import com.cometchat.chatuikit.shared.models.interactivemessage.SchedulerMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CometChatUIKitHelper {
    private static final String TAG = CometChatUIKitHelper.class.getSimpleName();

    public static void onMessageSent(BaseMessage message, @MessageStatus int status) {
        List<CometChatMessageEvents> events = new ArrayList<>(CometChatMessageEvents.messageEvents.values());
        for (CometChatMessageEvents event : events) {
            event.ccMessageSent(message, status);
        }
    }

    public static void onMessageEdited(BaseMessage message, @MessageStatus int status) {
        List<CometChatMessageEvents> events = new ArrayList<>(CometChatMessageEvents.messageEvents.values());
        for (CometChatMessageEvents event : events) {
            event.ccMessageEdited(message, status);
        }
    }

    public static void onMessageDeleted(BaseMessage message) {
        List<CometChatMessageEvents> events = new ArrayList<>(CometChatMessageEvents.messageEvents.values());
        for (CometChatMessageEvents event : events) {
            event.ccMessageDeleted(message);
        }
    }

    public static void onMessageRead(BaseMessage message) {
        List<CometChatMessageEvents> events = new ArrayList<>(CometChatMessageEvents.messageEvents.values());
        for (CometChatMessageEvents event : events) {
            event.ccMessageRead(message);
        }
    }

    public static void onTextMessageReceived(TextMessage textMessage) {
        List<CometChatMessageEvents> events = new ArrayList<>(CometChatMessageEvents.messageEvents.values());
        for (CometChatMessageEvents event : events) {
            event.onTextMessageReceived(textMessage);
        }
    }

    public static void onMediaMessageReceived(MediaMessage mediaMessage) {
        List<CometChatMessageEvents> events = new ArrayList<>(CometChatMessageEvents.messageEvents.values());
        for (CometChatMessageEvents event : events) {
            event.onMediaMessageReceived(mediaMessage);
        }
    }

    public static void onCustomMessageReceived(CustomMessage customMessage) {
        List<CometChatMessageEvents> events = new ArrayList<>(CometChatMessageEvents.messageEvents.values());
        for (CometChatMessageEvents event : events) {
            event.onCustomMessageReceived(customMessage);
        }
    }

    public static void onTypingStarted(TypingIndicator typingIndicator) {
        List<CometChatMessageEvents> events = new ArrayList<>(CometChatMessageEvents.messageEvents.values());
        for (CometChatMessageEvents event : events) {
            event.onTypingStarted(typingIndicator);
        }
    }

    public static void onTypingEnded(TypingIndicator typingIndicator) {
        List<CometChatMessageEvents> events = new ArrayList<>(CometChatMessageEvents.messageEvents.values());
        for (CometChatMessageEvents event : events) {
            event.onTypingEnded(typingIndicator);
        }
    }

    public static void onMessagesDelivered(MessageReceipt messageReceipt) {
        List<CometChatMessageEvents> events = new ArrayList<>(CometChatMessageEvents.messageEvents.values());
        for (CometChatMessageEvents event : events) {
            event.onMessagesDelivered(messageReceipt);
        }
    }

    public static void onMessagesRead(MessageReceipt messageReceipt) {
        List<CometChatMessageEvents> events = new ArrayList<>(CometChatMessageEvents.messageEvents.values());
        for (CometChatMessageEvents event : events) {
            event.onMessagesRead(messageReceipt);
        }
    }

    public static void onInteractionGoalCompleted(InteractionReceipt interactionReceipt) {
        List<CometChatMessageEvents> events = new ArrayList<>(CometChatMessageEvents.messageEvents.values());
        for (CometChatMessageEvents event : events) {
            event.onInteractionGoalCompleted(interactionReceipt);
        }
    }

    public static void onMessageEdited(BaseMessage message) {
        List<CometChatMessageEvents> events = new ArrayList<>(CometChatMessageEvents.messageEvents.values());
        for (CometChatMessageEvents event : events) {
            event.onMessageEdited(message);
        }
    }

    public static void onTransientMessageReceived(TransientMessage message) {
        List<CometChatMessageEvents> events = new ArrayList<>(CometChatMessageEvents.messageEvents.values());
        for (CometChatMessageEvents event : events) {
            event.onTransientMessageReceived(message);
        }
    }

    public static void onFormMessageReceived(FormMessage formMessage) {
        List<CometChatMessageEvents> events = new ArrayList<>(CometChatMessageEvents.messageEvents.values());
        for (CometChatMessageEvents event : events) {
            event.onFormMessageReceived(formMessage);
        }
    }

    public static void onSchedulerMessageReceived(SchedulerMessage schedulerMessage) {
        List<CometChatMessageEvents> events = new ArrayList<>(CometChatMessageEvents.messageEvents.values());
        for (CometChatMessageEvents event : events) {
            event.onSchedulerMessageReceived(schedulerMessage);
        }
    }

    public static void onCardMessageReceived(CardMessage cardMessage) {
        List<CometChatMessageEvents> events = new ArrayList<>(CometChatMessageEvents.messageEvents.values());
        for (CometChatMessageEvents event : events) {
            event.onCardMessageReceived(cardMessage);
        }
    }

    public static void onCustomInteractiveMessageReceived(CustomInteractiveMessage customInteractiveMessage) {
        List<CometChatMessageEvents> events = new ArrayList<>(CometChatMessageEvents.messageEvents.values());
        for (CometChatMessageEvents event : events) {
            event.onCustomInteractiveMessageReceived(customInteractiveMessage);
        }
    }

    public static void onMessageReactionAdded(ReactionEvent reactionEvent) {
        List<CometChatMessageEvents> events = new ArrayList<>(CometChatMessageEvents.messageEvents.values());
        for (CometChatMessageEvents event : events) {
            event.onMessageReactionAdded(reactionEvent);
        }
    }

    public static void onMessageReactionRemoved(ReactionEvent reactionEvent) {
        List<CometChatMessageEvents> events = new ArrayList<>(CometChatMessageEvents.messageEvents.values());
        for (CometChatMessageEvents event : events) {
            event.onMessageReactionRemoved(reactionEvent);
        }
    }

    public static void onMessagesDeliveredToAll(MessageReceipt messageReceipt) {
        List<CometChatMessageEvents> events = new ArrayList<>(CometChatMessageEvents.messageEvents.values());
        for (CometChatMessageEvents event : events) {
            event.onMessagesDeliveredToAll(messageReceipt);
        }
    }

    public static void onMessagesReadByAll(MessageReceipt messageReceipt) {
        List<CometChatMessageEvents> events = new ArrayList<>(CometChatMessageEvents.messageEvents.values());
        for (CometChatMessageEvents event : events) {
            event.onMessagesReadByAll(messageReceipt);
        }
    }

    public static void onMessageModerated(BaseMessage baseMessage) {
        List<CometChatMessageEvents> events = new ArrayList<>(CometChatMessageEvents.messageEvents.values());
        for (CometChatMessageEvents event : events) {
            event.onMessageModerated(baseMessage);
        }
    }

    public static void onLiveReaction(@DrawableRes int icon) {
        List<CometChatMessageEvents> events = new ArrayList<>(CometChatMessageEvents.messageEvents.values());
        for (CometChatMessageEvents event : events) {
            event.ccLiveReaction(icon);
        }
    }

    public static void onMessageReply(BaseMessage message, @MessageStatus int status) {
        List<CometChatMessageEvents> events = new ArrayList<>(CometChatMessageEvents.messageEvents.values());
        for (CometChatMessageEvents event : events) {
            event.ccReplyToMessage(message, status);
        }
    }

    public static void onUserBlocked(User user) {
        List<CometChatUserEvents> events = new ArrayList<>(CometChatUserEvents.userEvents.values());
        for (CometChatUserEvents event : events) {
            event.ccUserBlocked(user);
        }
    }

    public static void onUserUnblocked(User user) {
        List<CometChatUserEvents> events = new ArrayList<>(CometChatUserEvents.userEvents.values());
        for (CometChatUserEvents event : events) {
            event.ccUserUnblocked(user);
        }
    }

    public static void onGroupCreated(Group group) {
        List<CometChatGroupEvents> events = new ArrayList<>(CometChatGroupEvents.groupEvents.values());
        for (CometChatGroupEvents event : events) {
            event.ccGroupCreated(group);
        }
    }

    public static void onGroupDeleted(Group group) {
        List<CometChatGroupEvents> events = new ArrayList<>(CometChatGroupEvents.groupEvents.values());
        for (CometChatGroupEvents event : events) {
            event.ccGroupDeleted(group);
        }
    }

    public static void onGroupLeft(Action message, User leftUser, Group leftGroup) {
        List<CometChatGroupEvents> events = new ArrayList<>(CometChatGroupEvents.groupEvents.values());
        for (CometChatGroupEvents event : events) {
            event.ccGroupLeft(message, leftUser, leftGroup);
        }
    }

    public static void onGroupMemberScopeChanged(Action message, User updatedUser, String scopeChangedTo, String scopeChangedFrom, Group group) {
        List<CometChatGroupEvents> events = new ArrayList<>(CometChatGroupEvents.groupEvents.values());
        for (CometChatGroupEvents event : events) {
            event.ccGroupMemberScopeChanged(message, updatedUser, scopeChangedTo, scopeChangedFrom, group);
        }
    }

    public static void onGroupMemberBanned(Action message, User bannedUser, User bannedBy, Group bannedFrom) {
        List<CometChatGroupEvents> events = new ArrayList<>(CometChatGroupEvents.groupEvents.values());
        for (CometChatGroupEvents event : events) {
            event.ccGroupMemberBanned(message, bannedUser, bannedBy, bannedFrom);
        }
    }

    public static void onGroupMemberKicked(Action message, User kickedUser, User kickedBy, Group kickedFrom) {
        List<CometChatGroupEvents> events = new ArrayList<>(CometChatGroupEvents.groupEvents.values());
        for (CometChatGroupEvents event : events) {
            event.ccGroupMemberKicked(message, kickedUser, kickedBy, kickedFrom);
        }
    }

    public static void onGroupMemberUnbanned(Action message, User unbannedUser, User unbannedBy, Group unbannedFrom) {
        List<CometChatGroupEvents> events = new ArrayList<>(CometChatGroupEvents.groupEvents.values());
        for (CometChatGroupEvents event : events) {
            event.ccGroupMemberUnBanned(message, unbannedUser, unbannedBy, unbannedFrom);
        }
    }

    public static void onGroupMemberJoined(User joinedUser, Group joinedGroup) {
        List<CometChatGroupEvents> events = new ArrayList<>(CometChatGroupEvents.groupEvents.values());
        for (CometChatGroupEvents event : events) {
            event.ccGroupMemberJoined(joinedUser, joinedGroup);
        }
    }

    public static void onGroupMemberAdded(List<Action> messages, List<User> usersAdded, Group groupAddedIn, User addedBy) {
        List<CometChatGroupEvents> events = new ArrayList<>(CometChatGroupEvents.groupEvents.values());
        for (CometChatGroupEvents event : events) {
            event.ccGroupMemberAdded(messages, usersAdded, groupAddedIn, addedBy);
        }
    }

    public static void onOwnershipChanged(Group group, GroupMember newOwner) {
        List<CometChatGroupEvents> events = new ArrayList<>(CometChatGroupEvents.groupEvents.values());
        for (CometChatGroupEvents event : events) {
            event.ccOwnershipChanged(group, newOwner);
        }
    }

    public static void showPanel(HashMap<String, String> id, UIKitConstants.CustomUIPosition alignment, Function1<Context, View> view) {
        List<CometChatUIEvents> events = new ArrayList<>(CometChatUIEvents.uiEvents.values());
        for (CometChatUIEvents event : events) {
            event.showPanel(id, alignment, view);
        }
    }

    public static void hidePanel(HashMap<String, String> id, UIKitConstants.CustomUIPosition alignment) {
        List<CometChatUIEvents> events = new ArrayList<>(CometChatUIEvents.uiEvents.values());
        for (CometChatUIEvents event : events) {
            event.hidePanel(id, alignment);
        }
    }

    public static void onActiveChatChanged(HashMap<String, String> id, BaseMessage message, User user, Group group) {
        List<CometChatUIEvents> events = new ArrayList<>(CometChatUIEvents.uiEvents.values());
        for (CometChatUIEvents event : events) {
            event.ccActiveChatChanged(id, message, user, group);
            event.ccActiveChatChanged(id, message, user, group, 0);
        }
    }

    public static void onActiveChatChanged(HashMap<String, String> id, BaseMessage message, User user, Group group, int unreadCount) {
        List<CometChatUIEvents> events = new ArrayList<>(CometChatUIEvents.uiEvents.values());
        for (CometChatUIEvents event : events) {
            event.ccActiveChatChanged(id, message, user, group, unreadCount);
            event.ccActiveChatChanged(id, message, user, group);
        }
    }

    public static void onComposeMessage(String id, String text) {
        List<CometChatUIEvents> events = new ArrayList<>(CometChatUIEvents.uiEvents.values());
        for (CometChatUIEvents event : events) {
            event.ccComposeMessage(id, text);
        }
    }

    public static void onOpenChat(User user, Group group) {
        List<CometChatUIEvents> events = new ArrayList<>(CometChatUIEvents.uiEvents.values());
        for (CometChatUIEvents event : events) {
            event.ccOpenChat(user, group);
        }
    }

    public static void onOutgoingCall(Call call) {
        List<CometChatCallEvents> events = new ArrayList<>(CometChatCallEvents.callingEvents.values());
        for (CometChatCallEvents event : events) {
            event.ccOutgoingCall(call);
        }
    }

    public static void onCallAccepted(Call call) {
        List<CometChatCallEvents> events = new ArrayList<>(CometChatCallEvents.callingEvents.values());
        for (CometChatCallEvents event : events) {
            event.ccCallAccepted(call);
        }
    }

    public static void onCallRejected(Call call) {
        List<CometChatCallEvents> events = new ArrayList<>(CometChatCallEvents.callingEvents.values());
        for (CometChatCallEvents event : events) {
            event.ccCallRejected(call);
        }
    }

    public static void onCallEnded(Call call) {
        List<CometChatCallEvents> events = new ArrayList<>(CometChatCallEvents.callingEvents.values());
        for (CometChatCallEvents event : events) {
            event.ccCallEnded(call);
        }
    }

    public static void onConversationDeleted(Conversation conversation) {
        List<CometChatConversationEvents> events = new ArrayList<>(CometChatConversationEvents.conversationEvents.values());
        for (CometChatConversationEvents event : events) {
            event.ccConversationDeleted(conversation);
        }
    }

    public static void onConversationUpdate(Conversation conversation) {
        List<CometChatConversationEvents> events = new ArrayList<>(CometChatConversationEvents.conversationEvents.values());
        for (CometChatConversationEvents event : events) {
            event.ccUpdateConversation(conversation);
        }
    }

    public static void onAIToolArgumentsReceived(AIToolArgumentMessage aiToolArgumentMessage) {
        List<CometChatMessageEvents> events = new ArrayList<>(CometChatMessageEvents.messageEvents.values());
        for (CometChatMessageEvents event : events) {
            event.onAIToolArgumentsReceived(aiToolArgumentMessage);
        }
    }

    public static void onAIToolResultReceived(AIToolResultMessage aiToolResultMessage) {
        List<CometChatMessageEvents> events = new ArrayList<>(CometChatMessageEvents.messageEvents.values());
        for (CometChatMessageEvents event : events) {
            event.onAIToolResultReceived(aiToolResultMessage);
        }
    }

    public static void onAIAssistantMessageReceived(AIAssistantMessage aiAssistantMessage) {
        List<CometChatMessageEvents> events = new ArrayList<>(CometChatMessageEvents.messageEvents.values());
        for (CometChatMessageEvents event : events) {
            event.onAIAssistantMessageReceived(aiAssistantMessage);
        }
    }
}
