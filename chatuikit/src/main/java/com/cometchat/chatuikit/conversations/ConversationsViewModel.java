package com.cometchat.chatuikit.conversations;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cometchat.chat.constants.CometChatConstants;
import com.cometchat.chat.core.Call;
import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.core.ConversationsRequest;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.helpers.CometChatHelper;
import com.cometchat.chat.models.Action;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.Conversation;
import com.cometchat.chat.models.CustomMessage;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.GroupMember;
import com.cometchat.chat.models.MediaMessage;
import com.cometchat.chat.models.MessageReceipt;
import com.cometchat.chat.models.TextMessage;
import com.cometchat.chat.models.TypingIndicator;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKitHelper;
import com.cometchat.chatuikit.shared.constants.MessageStatus;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.constants.UIKitUtilityConstants;
import com.cometchat.chatuikit.shared.events.CometChatCallEvents;
import com.cometchat.chatuikit.shared.events.CometChatConversationEvents;
import com.cometchat.chatuikit.shared.events.CometChatGroupEvents;
import com.cometchat.chatuikit.shared.events.CometChatMessageEvents;
import com.cometchat.chatuikit.shared.events.CometChatUserEvents;
import com.cometchat.chatuikit.shared.models.interactivemessage.CardMessage;
import com.cometchat.chatuikit.shared.models.interactivemessage.CustomInteractiveMessage;
import com.cometchat.chatuikit.shared.models.interactivemessage.FormMessage;
import com.cometchat.chatuikit.shared.models.interactivemessage.SchedulerMessage;
import com.cometchat.chatuikit.shared.resources.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("unused")
public class ConversationsViewModel extends ViewModel {
    private static final String TAG = ConversationsViewModel.class.getSimpleName();
    private final List<Conversation> conversationList = new ArrayList<>();
    private final HashMap<Conversation, TypingIndicator> typingIndicatorHashMap = new HashMap<>();
    private final MutableLiveData<Integer> moveToTop;
    private final MutableLiveData<Integer> insertAtTop;
    private final MutableLiveData<List<Conversation>> mutableConversationList;
    private final MutableLiveData<Integer> updateConversation;
    private final MutableLiveData<Integer> removeConversation;
    private final MutableLiveData<UIKitConstants.DeleteState> conversationDeleteState;
    private final MutableLiveData<CometChatException> cometchatException;
    private final MutableLiveData<UIKitConstants.States> states;
    @NonNull
    private final MutableLiveData<HashMap<Conversation, TypingIndicator>> typing;
    private final MutableLiveData<Boolean> playSound;
    private final Runnable updateTypingRunnable = new Runnable() {
        @Override
        public void run() {
            typing.setValue(typingIndicatorHashMap);
        }
    };
    private String LISTENERS_TAG;
    private boolean hasMore = true;
    private boolean disableReceipt;
    private boolean connectionListerAttached;
    private ConversationsRequest conversationsRequest;
    private ConversationsRequest.ConversationsRequestBuilder conversationsRequestBuilder;
    private Handler handler = new Handler();

    public ConversationsViewModel() {
        typing = new MutableLiveData<>();
        states = new MutableLiveData<>();
        moveToTop = new MutableLiveData<>();
        playSound = new MutableLiveData<>();
        insertAtTop = new MutableLiveData<>();
        removeConversation = new MutableLiveData<>();
        updateConversation = new MutableLiveData<>();
        cometchatException = new MutableLiveData<>();
        mutableConversationList = new MutableLiveData<>();
        conversationDeleteState = new MutableLiveData<>();

        mutableConversationList.setValue(conversationList);
        conversationsRequestBuilder = new ConversationsRequest.ConversationsRequestBuilder().setLimit(30);
        conversationsRequest = conversationsRequestBuilder.build();
    }

    public Conversation typing(TypingIndicator typingIndicator) {
        for (Conversation conversation : conversationList) {
            if (typingIndicator.getReceiverType().equalsIgnoreCase(CometChatConstants.RECEIVER_TYPE_USER)) {
                if (conversation
                    .getConversationId()
                    .contains(typingIndicator.getSender().getUid()) && !Utils.isBlocked((User) conversation.getConversationWith()))
                    return conversation;
            } else {
                if (conversation.getConversationId().contains(typingIndicator.getReceiverId())) return conversation;
            }
        }
        return null;
    }

    public MutableLiveData<Integer> remove() {
        return removeConversation;
    }

    public MutableLiveData<List<Conversation>> getMutableConversationList() {
        return mutableConversationList;
    }

    @NonNull
    public MutableLiveData<HashMap<Conversation, TypingIndicator>> getTyping() {
        return typing;
    }

    public MutableLiveData<UIKitConstants.States> getStates() {
        return states;
    }

    public MutableLiveData<Integer> insertAtTop() {
        return insertAtTop;
    }

    public MutableLiveData<Integer> moveToTop() {
        return moveToTop;
    }

    public MutableLiveData<Integer> updateConversation() {
        return updateConversation;
    }

    public MutableLiveData<UIKitConstants.DeleteState> progressState() {
        return conversationDeleteState;
    }

    public MutableLiveData<Boolean> playSound() {
        return playSound;
    }

    public MutableLiveData<CometChatException> getCometChatException() {
        return cometchatException;
    }

    public void disableReceipt(boolean disableReceipt) {
        this.disableReceipt = disableReceipt;
    }

    private Conversation convertMessageToConversation(BaseMessage message) {
        if (message != null) return CometChatHelper.getConversationFromMessage(message);
        return null;
    }

    public void update(Conversation conversation, boolean isActionMessage) {
        if (conversation != null && conversation.getLastMessage() != null) {
            int oldIndex = -1;
            Conversation oldConversation = null;
            for (int i = 0; i < conversationList.size(); i++) {
                Conversation tempConversation = conversationList.get(i);
                if (tempConversation.getConversationId().equals(conversation.getConversationId())) {
                    oldIndex = i;
                    oldConversation = tempConversation;
                    break;
                }
            }
            if (oldIndex > -1) {
                boolean incrementUnreadCount = false;
                if (conversation.getLastMessage() instanceof CustomMessage) {
                    incrementUnreadCount = shouldUpdateConversationForCustomMessage((CustomMessage) conversation.getLastMessage());
                }
                boolean isCategoryMessage = conversation
                    .getLastMessage()
                    .getCategory()
                    .equalsIgnoreCase(CometChatConstants.CATEGORY_MESSAGE) || (conversation
                    .getLastMessage()
                    .getCategory()
                    .equalsIgnoreCase(CometChatConstants.CATEGORY_ACTION) && conversation
                    .getLastMessage()
                    .getType()
                    .equalsIgnoreCase(CometChatConstants.ActionKeys.ACTION_TYPE_GROUP_MEMBER)) || conversation
                    .getLastMessage()
                    .getCategory()
                    .equalsIgnoreCase(CometChatConstants.CATEGORY_INTERACTIVE) || conversation
                    .getLastMessage()
                    .getCategory()
                    .equalsIgnoreCase(CometChatConstants.CATEGORY_CALL);
                if (!conversation.getLastMessage().getSender().getUid().equalsIgnoreCase(CometChatUIKit.getLoggedInUser().getUid())) {
                    if (conversation.getLastMessage().getReadAt() == 0) {
                        if (isActionMessage) {
                            conversation.setConversationWith(oldConversation.getConversationWith());
                            conversation.setUnreadMessageCount(oldConversation.getUnreadMessageCount());
                            updateConversationObject(oldIndex, conversation);
                        } else if (incrementUnreadCount || isCategoryMessage) {
                            handleUnreadCount(oldIndex, oldConversation, conversation, false);
                        }
                    } else {
                        conversation.setUnreadMessageCount(0);
                        conversationList.set(oldIndex, conversation);
                        updateConversation.setValue(oldIndex);
                    }
                } else {
                    handleUnreadCount(oldIndex, oldConversation, conversation, true);
                }
            } else {
                if (isAddToConversationList(conversation)) {
                    itemInsertedAtTop(conversation);
                }
            }
        }
    }

    private void itemInsertedAtTop(Conversation conversation) {
        if (!conversation
            .getLastMessage()
            .getSender()
            .getUid()
            .equalsIgnoreCase(CometChatUIKit
                                  .getLoggedInUser()
                                  .getUid()) && !(conversation.getLastMessage() instanceof Action || conversation.getLastMessage() instanceof Call)) {
            conversation.setUnreadMessageCount(1);
        }
        conversationList.add(0, conversation);
        states.setValue(checkIsEmpty(conversationList));
        insertAtTop.setValue(0);
    }

    private void handleUnreadCount(int oldConversationIndex, Conversation oldConversation, @NonNull Conversation conversation, boolean isSent) {
        conversation.setConversationWith(oldConversation.getConversationWith());
        if (oldConversation.getLastMessage() != null && conversation.getLastMessage() != null && (oldConversation
            .getLastMessage()
            .getId() != conversation.getLastMessage().getId())) {
            if (conversation.getLastMessage().getEditedAt() != 0 || conversation.getLastMessage().getDeletedAt() != 0) {
                return;
            }
            if (isSent) conversation.setUnreadMessageCount(oldConversation.getUnreadMessageCount());
            else conversation.setUnreadMessageCount(oldConversation.getUnreadMessageCount() + 1);
        } else {
            conversation.setUnreadMessageCount(oldConversation.getUnreadMessageCount());
        }
        updateConversationObject(oldConversationIndex, conversation);
    }

    private void updateConversationObject(int oldConversationIndex, Conversation conversation) {
        conversationList.remove(oldConversationIndex);
        conversationList.add(0, conversation);
        moveToTop.setValue(oldConversationIndex);
    }

    public void addListener() {
        LISTENERS_TAG = System.currentTimeMillis() + "";
        CometChat.addGroupListener(LISTENERS_TAG, new CometChat.GroupListener() {
            @Override
            public void onGroupMemberJoined(@NonNull Action action, User joinedUser, Group joinedGroup) {
                updateConversationForGroup(action, false);
            }

            @Override
            public void onGroupMemberLeft(Action action, User leftUser, Group leftGroup) {
                updateConversationForGroup(action, leftUser.getUid().equals(CometChatUIKit.getLoggedInUser().getUid()));
            }

            @Override
            public void onGroupMemberKicked(Action action, @NonNull User kickedUser, User kickedBy, Group kickedFrom) {
                updateConversationForGroup(action, kickedUser.getUid().equals(CometChatUIKit.getLoggedInUser().getUid()));
            }

            @Override
            public void onGroupMemberBanned(Action action, User bannedUser, User bannedBy, Group group) {
                updateConversationForGroup(action, bannedUser.getUid().equals(CometChatUIKit.getLoggedInUser().getUid()));
            }

            @Override
            public void onGroupMemberScopeChanged(Action action,
                                                  User updatedBy,
                                                  User updatedUser,
                                                  String scopeChangedTo,
                                                  String scopeChangedFrom,
                                                  Group group) {
                if (updatedUser.getUid().equals(CometChatUIKit.getLoggedInUser().getUid())) {
                    group.setScope(scopeChangedTo);
                    group.setHasJoined(true);
                    updateGroupConversation(group);
                }
                updateConversationForGroup(action, false);
            }

            @Override
            public void onMemberAddedToGroup(Action action, User addedBy, User userAdded, Group addedTo) {
                if (userAdded.getUid().equals(CometChatUIKit.getLoggedInUser().getUid())) {
                    addedTo.setScope(CometChatConstants.SCOPE_PARTICIPANT);
                    addedTo.setHasJoined(true);
                    action.setActionFor(addedTo);
                } else {
                    Group group = getGroupFromConversation(addedTo.getGuid());
                    if (group == null) {
                        group = addedTo;
                        group.setHasJoined(true);
                        group.setScope(CometChatConstants.SCOPE_PARTICIPANT);
                    }
                    action.setActionFor(group);
                }
                updateConversationForGroup(action, false);
            }
        });

        CometChat.addUserListener(LISTENERS_TAG, new CometChat.UserListener() {
            @Override
            public void onUserOnline(User user) {
                updateUserConversation(user);
            }

            @Override
            public void onUserOffline(User user) {
                updateUserConversation(user);
            }
        });

        CometChatConversationEvents.addListener(LISTENERS_TAG, new CometChatConversationEvents() {
            @Override
            public void ccConversationDeleted(Conversation conversation) {
                remove(conversation);
            }
        });

        CometChatGroupEvents.addGroupListener(LISTENERS_TAG, new CometChatGroupEvents() {
            @Override
            public void ccGroupDeleted(Group group) {
                if (group != null) removeGroup(group);
            }

            @Override
            public void ccGroupLeft(Action actionMessage, User leftUser, Group leftGroup) {
                removeGroup(leftGroup);
            }

            @Override
            public void ccGroupMemberJoined(User joinedUser, Group joinedGroup) {
                if (joinedGroup != null) updateGroupConversation(joinedGroup);
            }

            @Override
            public void ccGroupMemberAdded(List<Action> actionMessages, List<User> usersAdded, Group userAddedIn, User addedBy) {
                if (userAddedIn != null) {
                    updateGroupConversation(userAddedIn);
                    for (Action action : actionMessages) {
                        updateConversationForGroup(action, false);
                    }
                }
            }

            @Override
            public void ccGroupMemberKicked(Action actionMessage, User kickedUser, User kickedBy, Group kickedFrom) {
                if (kickedFrom != null) {
                    updateGroupConversation(kickedFrom);
                    updateConversationForGroup(actionMessage, false);
                }
            }

            @Override
            public void ccGroupMemberBanned(Action actionMessage, User bannedUser, User bannedBy, Group bannedFrom) {
                if (bannedFrom != null) {
                    updateGroupConversation(bannedFrom);
                    updateConversationForGroup(actionMessage, false);
                }
            }

            @Override
            public void ccGroupMemberUnBanned(Action actionMessage, User unbannedUser, User unBannedBy, @Nullable Group unBannedFrom) {
                if (unBannedFrom != null) {
                    updateGroupConversation(unBannedFrom);
                    updateConversationForGroup(actionMessage, false);
                }
            }

            @Override
            public void ccGroupMemberScopeChanged(Action actionMessage,
                                                  User updatedUser,
                                                  String scopeChangedTo,
                                                  String scopeChangedFrom,
                                                  Group group) {
                if (group != null) {
                    updateGroupConversation(group);
                    updateConversationForGroup(actionMessage, false);
                }
            }

            @Override
            public void ccOwnershipChanged(Group group, GroupMember newOwner) {
                if (group != null) {
                    updateGroupConversation(group);
                }
            }
        });

        CometChatUserEvents.addUserListener(LISTENERS_TAG, new CometChatUserEvents() {
            @Override
            public void ccUserBlocked(User user) {
                if (!conversationsRequest.isIncludeBlockedUsers()) removeUser(user);
                else updateUserConversation(user);
            }

            @Override
            public void ccUserUnblocked(User user) {
                if (conversationsRequest.isIncludeBlockedUsers()) updateUserConversation(user);
            }
        });

        CometChatMessageEvents.addListener(LISTENERS_TAG, new CometChatMessageEvents() {
            @Override
            public void ccMessageSent(BaseMessage baseMessage, int status) {
                if (status == MessageStatus.SUCCESS && baseMessage != null) {
                    checkAndUpdateConversation(baseMessage, false);
                }
            }

            @Override
            public void ccMessageEdited(BaseMessage baseMessage, int status) {
                if (status == MessageStatus.SUCCESS && baseMessage != null) checkAndUpdateConversation(baseMessage, false);
            }

            @Override
            public void ccMessageDeleted(BaseMessage baseMessage) {
                if (baseMessage != null) checkAndUpdateConversation(baseMessage, false);
            }

            @Override
            public void ccMessageRead(BaseMessage baseMessage) {
                if (baseMessage != null) {
                    clearConversationUnreadCount(convertMessageToConversation(baseMessage));
                }
            }

            @Override
            public void onTextMessageReceived(TextMessage message) {
                checkAndUpdateConversation(message, true);
            }

            @Override
            public void onMediaMessageReceived(MediaMessage message) {
                checkAndUpdateConversation(message, true);
            }

            @Override
            public void onCustomMessageReceived(CustomMessage message) {
                checkAndUpdateConversation(message, true);
            }

            @Override
            public void onTypingStarted(TypingIndicator typingIndicator) {
                typingIndicatorHashMap.put(typing(typingIndicator), typingIndicator);
                typing.setValue(typingIndicatorHashMap);
            }

            @Override
            public void onTypingEnded(TypingIndicator typingIndicator) {
                Conversation conversation = typing(typingIndicator);
                typingIndicatorHashMap.put(conversation, null);
                handler.removeCallbacks(updateTypingRunnable);
                handler = new Handler();
                handler.postDelayed(updateTypingRunnable, UIKitUtilityConstants.TYPING_INDICATOR_DEBOUNCER);
            }

            @Override
            public void onMessagesDelivered(MessageReceipt messageReceipt) {
                updateDeliveredReceipts(messageReceipt);
            }

            @Override
            public void onMessagesRead(MessageReceipt messageReceipt) {
                updateReadReceipts(messageReceipt);
            }

            @Override
            public void onMessageEdited(BaseMessage message) {
                update(convertMessageToConversation(message), false);
            }

            @Override
            public void onMessageDeleted(BaseMessage message) {
                update(convertMessageToConversation(message), false);
            }

            @Override
            public void onFormMessageReceived(FormMessage formMessage) {
                checkAndUpdateConversation(formMessage, true);
            }

            @Override
            public void onSchedulerMessageReceived(@NonNull SchedulerMessage schedulerMessage) {
                checkAndUpdateConversation(schedulerMessage, true);
            }

            @Override
            public void onCardMessageReceived(CardMessage cardMessage) {
                checkAndUpdateConversation(cardMessage, true);
            }

            @Override
            public void onCustomInteractiveMessageReceived(CustomInteractiveMessage customInteractiveMessage) {
                checkAndUpdateConversation(customInteractiveMessage, true);
            }

            @Override
            public void onMessagesDeliveredToAll(MessageReceipt messageReceipt) {
                updateDeliveredReceipts(messageReceipt);
            }

            @Override
            public void onMessagesReadByAll(MessageReceipt messageReceipt) {
                updateReadReceipts(messageReceipt);
            }
        });

        if (Utils.isCallingAvailable()) {
            CometChatCallEvents.addListener(LISTENERS_TAG, new CometChatCallEvents() {
                @Override
                public void ccOutgoingCall(Call call) {
                    if (CometChatUIKit.getConversationUpdateSettings().shouldUpdateOnCallActivities())
                        update(convertMessageToConversation(call), false);
                }

                @Override
                public void ccCallAccepted(Call call) {
                    if (CometChatUIKit.getConversationUpdateSettings().shouldUpdateOnCallActivities())
                        update(convertMessageToConversation(call), false);
                }

                @Override
                public void ccCallRejected(Call call) {
                    if (CometChatUIKit.getConversationUpdateSettings().shouldUpdateOnCallActivities())
                        update(convertMessageToConversation(call), false);
                }

                @Override
                public void ccCallEnded(Call call) {
                    if (CometChatUIKit.getConversationUpdateSettings().shouldUpdateOnCallActivities())
                        update(convertMessageToConversation(call), false);
                }

                @Override
                public void ccMessageSent(BaseMessage baseMessage, int status) {
                    if (status == MessageStatus.SUCCESS && baseMessage != null) {
                        update(convertMessageToConversation(baseMessage), false);
                    }
                }
            });

            CometChat.addCallListener(LISTENERS_TAG, new CometChat.CallListener() {
                @Override
                public void onIncomingCallReceived(Call call) {
                    if (CometChatUIKit.getConversationUpdateSettings().shouldUpdateOnCallActivities())
                        update(convertMessageToConversation(call), false);
                }

                @Override
                public void onOutgoingCallAccepted(Call call) {
                    if (CometChatUIKit.getConversationUpdateSettings().shouldUpdateOnCallActivities())
                        update(convertMessageToConversation(call), false);
                }

                @Override
                public void onOutgoingCallRejected(Call call) {
                    if (CometChatUIKit.getConversationUpdateSettings().shouldUpdateOnCallActivities())
                        update(convertMessageToConversation(call), false);
                }

                @Override
                public void onIncomingCallCancelled(Call call) {
                    if (CometChatUIKit.getConversationUpdateSettings().shouldUpdateOnCallActivities())
                        update(convertMessageToConversation(call), false);
                }

                @Override
                public void onCallEndedMessageReceived(Call call) {
                    if (CometChatUIKit.getConversationUpdateSettings().shouldUpdateOnCallActivities())
                        update(convertMessageToConversation(call), false);
                }
            });
        }
    }

    public void clearConversationUnreadCount(Conversation conversation) {
        for (int i = 0; i < conversationList.size(); i++) {
            Conversation tempConversation = conversationList.get(i);
            if (tempConversation.getConversationId().equals(conversation.getConversationId())) {
                tempConversation.setUnreadMessageCount(0);
                updateConversation.setValue(i);
                break;
            }
        }
    }

    private void checkAndUpdateConversation(BaseMessage baseMessage, boolean markAsDeliver) {
        if (isThreadedMessage(baseMessage)) {
            if (CometChatUIKit.getConversationUpdateSettings().shouldUpdateOnMessageReplies()) {
                handleMessageUpdate(baseMessage, markAsDeliver);
            }
        } else {
            handleMessageUpdate(baseMessage, markAsDeliver);
        }
    }

    private void handleMessageUpdate(BaseMessage baseMessage, boolean markAsDeliver) {
        if (baseMessage instanceof CustomMessage) {
            if (shouldUpdateConversationForCustomMessage((CustomMessage) baseMessage)) {
                updateMessageDeliveryStatus(baseMessage, markAsDeliver);
            }
        } else {
            updateMessageDeliveryStatus(baseMessage, markAsDeliver);
        }
    }

    private void updateMessageDeliveryStatus(BaseMessage baseMessage, boolean markAsDeliver) {
        if (markAsDeliver) processMessage(baseMessage);
        else update(convertMessageToConversation(baseMessage), false);
    }

    public void addConnectionListener() {
        CometChat.addConnectionListener(LISTENERS_TAG, new CometChat.ConnectionListener() {
            @Override
            public void onConnected() {
                refreshList();
            }

            @Override
            public void onConnecting() {
            }

            @Override
            public void onDisconnected() {
            }

            @Override
            public void onFeatureThrottled() {
            }

            @Override
            public void onConnectionError(CometChatException e) {
            }
        });
    }

    private void processMessage(BaseMessage baseMessage) {
        markAsDeliverInternally(baseMessage);
        update(convertMessageToConversation(baseMessage), false);
        playSound.setValue(true);
    }

    public void removeListener() {
        CometChat.removeUserListener(LISTENERS_TAG);
        CometChat.removeGroupListener(LISTENERS_TAG);
        CometChat.removeCallListener(LISTENERS_TAG);
        CometChatConversationEvents.removeListener(LISTENERS_TAG);
        CometChatGroupEvents.removeListener(LISTENERS_TAG);
        CometChatUserEvents.removeListener(LISTENERS_TAG);
        CometChatCallEvents.removeListener(LISTENERS_TAG);
        CometChat.removeConnectionListener(LISTENERS_TAG);
    }

    private void updateConversationForGroup(BaseMessage baseMessage, boolean isRemove) {
        Conversation conversation = CometChatHelper.getConversationFromMessage(baseMessage);
        if (isRemove) remove(conversation);
        else {
            if (CometChatUIKit.getConversationUpdateSettings().shouldUpdateOnGroupActions()) update(conversation, true);
        }
    }

    private boolean isAddToConversationList(Conversation conversation) {
        if (conversation != null && conversationsRequest != null) {
            if (conversationsRequest.getConversationType() == null) return true;
            else return conversation.getConversationType().equalsIgnoreCase(conversationsRequest.getConversationType());
        }
        return false;
    }

    private void markAsDeliverInternally(BaseMessage message) {
        if (!message.getSender().getUid().equalsIgnoreCase(CometChatUIKit.getLoggedInUser().getUid()) && !disableReceipt)
            CometChat.markAsDelivered(message);
    }

    public void remove(Conversation conversation) {
        int oldIndex = conversationList.indexOf(conversation);
        conversationList.remove(conversation);
        removeConversation.setValue(oldIndex);
        states.setValue(checkIsEmpty(conversationList));
    }

    public void updateDeliveredReceipts(MessageReceipt receipt) {
        for (int i = 0; i < conversationList.size() - 1; i++) {
            Conversation conversation = conversationList.get(i);
            if (receipt.getReceivertype().equals(UIKitConstants.ReceiverType.USER)) {
                if (conversation.getConversationType().equals(CometChatConstants.RECEIVER_TYPE_USER) && receipt
                    .getSender()
                    .getUid()
                    .equals(((User) conversation.getConversationWith()).getUid())) {
                    BaseMessage baseMessage = conversation.getLastMessage();
                    if (baseMessage != null && baseMessage.getDeliveredAt() == 0 && baseMessage.getId() == receipt.getMessageId()) {
                        baseMessage.setDeliveredAt(receipt.getDeliveredAt());
                        conversation.setLastMessage(baseMessage);
                        conversationList.set(i, conversation);
                        updateConversation.setValue(i);
                        break;
                    }
                }
            } else if (receipt.getReceivertype().equals(UIKitConstants.ReceiverType.GROUP)) {
                if (conversation.getConversationType().equals(CometChatConstants.RECEIVER_TYPE_GROUP) && receipt
                    .getReceiptType()
                    .equals(MessageReceipt.RECEIPT_TYPE_DELIVERED_TO_ALL) && receipt
                    .getReceiverId()
                    .equals(((Group) conversation.getConversationWith()).getGuid())) {
                    BaseMessage baseMessage = conversation.getLastMessage();
                    if (baseMessage != null && baseMessage.getDeliveredAt() == 0 && baseMessage.getId() == receipt.getMessageId()) {
                        baseMessage.setDeliveredAt(receipt.getDeliveredAt());
                        conversation.setLastMessage(baseMessage);
                        conversationList.set(i, conversation);
                        updateConversation.setValue(i);
                        break;
                    }
                }
            }
        }
    }

    public void updateReadReceipts(MessageReceipt receipt) {
        for (int i = 0; i < conversationList.size() - 1; i++) {
            Conversation conversation = conversationList.get(i);

            if (receipt.getReceivertype().equals(UIKitConstants.ReceiverType.USER)) {
                if (conversation.getConversationType().equals(CometChatConstants.RECEIVER_TYPE_USER) && receipt
                    .getSender()
                    .getUid()
                    .equals(((User) conversation.getConversationWith()).getUid())) {
                    BaseMessage baseMessage = conversation.getLastMessage();
                    if (baseMessage != null && baseMessage.getReadAt() == 0 && baseMessage.getId() == receipt.getMessageId()) {
                        baseMessage.setReadAt(receipt.getReadAt());
                        conversation.setLastMessage(baseMessage);
                        conversationList.set(i, conversation);
                        updateConversation.setValue(i);
                        break;
                    } else if (receipt.getSender().getUid().equals(CometChatUIKit.getLoggedInUser().getUid())) {
                        conversation.setUnreadMessageCount(0);
                        conversationList.set(i, conversation);
                        updateConversation.setValue(i);
                        break;
                    }
                }
            } else if (receipt.getReceivertype().equals(UIKitConstants.ReceiverType.GROUP)) {
                if (conversation.getConversationType().equals(CometChatConstants.RECEIVER_TYPE_GROUP) && receipt
                    .getReceiptType()
                    .equals(MessageReceipt.RECEIPT_TYPE_READ_BY_ALL) && receipt
                    .getReceiverId()
                    .equals(((Group) conversation.getConversationWith()).getGuid())) {
                    BaseMessage baseMessage = conversation.getLastMessage();
                    if (baseMessage != null && baseMessage.getReadAt() == 0 && baseMessage.getId() == receipt.getMessageId()) {
                        baseMessage.setReadAt(receipt.getReadAt());
                        conversation.setLastMessage(baseMessage);
                        conversationList.set(i, conversation);
                        updateConversation.setValue(i);
                        break;
                    }
                } else if (receipt.getSender().getUid().equals(CometChatUIKit.getLoggedInUser().getUid())) {
                    conversation.setUnreadMessageCount(0);
                    conversationList.set(i, conversation);
                    updateConversation.setValue(i);
                    break;
                }
            }
        }
    }

    public void deleteConversation(Conversation conversation) {
        if (conversation != null) {
            conversationDeleteState.setValue(UIKitConstants.DeleteState.INITIATED_DELETE);
            String conversationUid;
            String type;
            if (conversation.getConversationType().equalsIgnoreCase(CometChatConstants.CONVERSATION_TYPE_GROUP)) {
                conversationUid = ((Group) conversation.getConversationWith()).getGuid();
                type = CometChatConstants.CONVERSATION_TYPE_GROUP;
            } else {
                conversationUid = ((User) conversation.getConversationWith()).getUid();
                type = CometChatConstants.CONVERSATION_TYPE_USER;
            }
            String finalConversationUid = conversationUid;
            String finalType = type;
            CometChat.deleteConversation(finalConversationUid, finalType, new CometChat.CallbackListener<String>() {
                @Override
                public void onSuccess(String s) {
                    conversationDeleteState.setValue(UIKitConstants.DeleteState.SUCCESS_DELETE);
                    CometChatUIKitHelper.onConversationDeleted(conversation);
                }

                @Override
                public void onError(CometChatException e) {
                    conversationDeleteState.setValue(UIKitConstants.DeleteState.FAILURE_DELETE);
                    cometchatException.setValue(e);
                }
            });
        }
    }

    public void fetchConversation() {
        if (conversationList.isEmpty()) states.setValue(UIKitConstants.States.LOADING);
        if (hasMore) {
            fetchConversationsList(false);
        }
    }

    private void fetchConversationsList(boolean cleanAndLoad) {
        conversationsRequest.fetchNext(new CometChat.CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                if (cleanAndLoad) clear();
                hasMore = !conversations.isEmpty();
                if (hasMore) addList(conversations);
                states.setValue(checkIsEmpty(conversationList));
                if (!connectionListerAttached) {
                    addConnectionListener();
                    connectionListerAttached = true;
                }
            }

            @Override
            public void onError(CometChatException e) {
                cometchatException.setValue(e);
                if (conversationList.isEmpty()) states.setValue(UIKitConstants.States.ERROR);
            }
        });
    }

    public void refreshList() {
        if (conversationsRequestBuilder != null) {
            conversationsRequest = conversationsRequestBuilder.build();
            hasMore = true;
            fetchConversationsList(true);
        }
    }

    public void clear() {
        conversationList.clear();
        mutableConversationList.setValue(conversationList);
    }

    public void addList(List<Conversation> conversations) {
        for (int i = 0; i < conversations.size(); i++) {
            if (conversationList.contains(conversations.get(i))) {
                int index = conversationList.indexOf(conversations.get(i));
                conversationList.remove(conversations.get(i));
                conversationList.add(index, conversations.get(i));
            } else {
                conversationList.add(conversations.get(i));
            }
        }
        mutableConversationList.setValue(conversationList);
    }

    public void updateGroupConversation(Group group) {
        for (int i = 0; i < conversationList.size(); i++) {
            Conversation conversation = conversationList.get(i);
            if (conversation.getConversationType().equalsIgnoreCase(UIKitConstants.ConversationType.GROUPS)) {
                Group conversationGroup = ((Group) conversation.getConversationWith());
                if (conversationGroup != null && group != null && conversationGroup.getGuid().equals(group.getGuid())) {
                    conversation.setConversationWith(group);
                    updateConversation.setValue(i);
                    break;
                }
            }
        }
    }

    public Group getGroupFromConversation(String guid) {
        for (int i = 0; i < conversationList.size(); i++) {
            Conversation conversation = conversationList.get(i);
            if (conversation.getConversationType().equalsIgnoreCase(UIKitConstants.ConversationType.GROUPS)) {
                Group conversationGroup = ((Group) conversation.getConversationWith());
                if (conversationGroup != null && conversationGroup.getGuid().equals(guid)) {
                    return conversationGroup;
                }
            }
        }
        return null;
    }

    public void updateUserConversation(User user) {
        for (int i = 0; i < conversationList.size(); i++) {
            Conversation conversation = conversationList.get(i);
            if (conversation.getConversationType().equalsIgnoreCase(UIKitConstants.ConversationType.USERS)) {
                User userConversation = ((User) conversation.getConversationWith());
                if (userConversation != null && user != null && userConversation.getUid().equals(user.getUid())) {
                    userConversation.setStatus(user.getStatus());
                    conversation.setConversationWith(userConversation);
                    updateConversation.setValue(i);
                    break;
                }
            }
        }
    }

    public void removeGroup(Group group) {
        for (int i = 0; i < conversationList.size(); i++) {
            Conversation conversation = conversationList.get(i);
            if (conversation.getConversationType().equalsIgnoreCase(UIKitConstants.ConversationType.GROUPS)) {
                Group conversationGroup = ((Group) conversation.getConversationWith());
                if (conversationGroup != null && group != null && conversationGroup.getGuid().equals(group.getGuid())) {
                    remove(conversation);
                    break;
                }
            }
        }
    }

    public void removeUser(User user) {
        for (int i = 0; i < conversationList.size(); i++) {
            Conversation conversation = conversationList.get(i);
            if (conversation.getConversationType().equalsIgnoreCase(UIKitConstants.ConversationType.USERS)) {
                User conversationUser = ((User) conversation.getConversationWith());
                if (conversationUser != null && user != null && conversationUser.getUid().equals(conversationUser.getUid())) {
                    remove(i);
                    break;
                }
            }
        }
    }

    public void remove(int index) {
        conversationList.remove(index);
        removeConversation.setValue(index);
        states.setValue(checkIsEmpty(conversationList));
    }

    public void setConversationsRequestBuilder(ConversationsRequest.ConversationsRequestBuilder conversationsRequestBuilder) {
        if (conversationsRequestBuilder != null) {
            this.conversationsRequestBuilder = conversationsRequestBuilder;
            this.conversationsRequest = conversationsRequestBuilder.build();
        }
    }

    private UIKitConstants.States checkIsEmpty(List<Conversation> conversations) {
        if (conversations.isEmpty()) return UIKitConstants.States.EMPTY;
        return UIKitConstants.States.NON_EMPTY;
    }

    public boolean isThreadedMessage(BaseMessage baseMessage) {
        return baseMessage.getParentMessageId() > 0;
    }

    public boolean shouldUpdateConversationForCustomMessage(CustomMessage customMessage) {
        return willUpdateIncrementUnreadCount(customMessage) || CometChatUIKit.getConversationUpdateSettings().shouldUpdateOnCustomMessages();
    }

    public boolean willUpdateIncrementUnreadCount(BaseMessage baseMessage) {
        if (baseMessage != null) {
            if (baseMessage instanceof CustomMessage) {
                String incrementUnreadCount = "incrementUnreadCount";
                if (baseMessage.getMetadata() != null && baseMessage.getMetadata().has(incrementUnreadCount)) {
                    try {
                        return baseMessage.getMetadata().getBoolean(incrementUnreadCount);
                    } catch (Exception ignored) {
                        return false;
                    }
                } else {
                    return ((CustomMessage) baseMessage).willUpdateConversation();
                }
            }
        }
        return false;
    }
}
