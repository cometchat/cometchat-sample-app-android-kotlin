package com.cometchat.chatuikit.messagelist;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cometchat.chat.constants.CometChatConstants;
import com.cometchat.chat.core.Call;
import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.core.MessagesRequest;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.helpers.CometChatHelper;
import com.cometchat.chat.models.Action;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.CustomMessage;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.GroupMember;
import com.cometchat.chat.models.InteractionReceipt;
import com.cometchat.chat.models.InteractiveMessage;
import com.cometchat.chat.models.MediaMessage;
import com.cometchat.chat.models.MessageReceipt;
import com.cometchat.chat.models.Reaction;
import com.cometchat.chat.models.ReactionEvent;
import com.cometchat.chat.models.TextMessage;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.logger.CometChatLogger;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKitHelper;
import com.cometchat.chatuikit.shared.constants.MessageStatus;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.events.CometChatCallEvents;
import com.cometchat.chatuikit.shared.events.CometChatGroupEvents;
import com.cometchat.chatuikit.shared.events.CometChatMessageEvents;
import com.cometchat.chatuikit.shared.events.CometChatUIEvents;
import com.cometchat.chatuikit.shared.interfaces.Function1;
import com.cometchat.chatuikit.shared.models.CometChatMessageTemplate;
import com.cometchat.chatuikit.shared.models.interactivemessage.CardMessage;
import com.cometchat.chatuikit.shared.models.interactivemessage.CustomInteractiveMessage;
import com.cometchat.chatuikit.shared.models.interactivemessage.FormMessage;
import com.cometchat.chatuikit.shared.models.interactivemessage.SchedulerMessage;
import com.cometchat.chatuikit.shared.resources.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MessageListViewModel extends ViewModel {
    private static final String TAG = MessageListViewModel.class.getSimpleName();
    private final String LISTENERS_TAG;
    private final MutableLiveData<List<BaseMessage>> mutableMessageList;
    private final MutableLiveData<Integer> mutableMessagesRangeChanged;
    private final List<BaseMessage> messageArrayList;
    private final MutableLiveData<Integer> updateMessage;
    private final MutableLiveData<Integer> removeMessage;
    private final MutableLiveData<BaseMessage> addMessage;
    private final MutableLiveData<BaseMessage> readMessage;
    @NonNull
    private final MutableLiveData<CometChatException> cometchatException;
    private final MutableLiveData<UIKitConstants.States> states;
    private final MutableLiveData<UIKitConstants.DeleteState> messageDeleteState;
    private final int limit = 30;
    private final MutableLiveData<Boolean> mutableHasMore;
    private final MutableLiveData<Boolean> mutableIsInProgress;
    private final MutableLiveData<Void> notifyUpdate;
    private final Void unused = null;
    private final MutableLiveData<List<String>> mutableSmartReplies;
    private final MutableLiveData<List<String>> mutableConversationStarterReplies;
    private final MutableLiveData<Boolean> removeConversationStarter;
    private final MutableLiveData<UIKitConstants.States> smartReplayUIState;
    private final MutableLiveData<UIKitConstants.States> conversationStarterUIState;
    public boolean firstFetch = true;
    public Void aVoid;
    public HashMap<String, String> idMap;
    public MutableLiveData<BaseMessage> onMessageDeleted;
    public MutableLiveData<HashMap<String, String>> mutableHashMap;
    public MutableLiveData<Function1<Context, View>> showTopPanel;
    public MutableLiveData<Function1<Context, View>> showBottomPanel;
    public MutableLiveData<Void> closeBottomPanel;
    public MutableLiveData<Void> closeTopPanel;
    public MessagesRequest actionMessagesRequest;
    public List<String> actionMessageTypes;
    public List<String> actionCategories;
    public MutableLiveData<BaseMessage> processMessageData;
    public Handler handler = new Handler(Looper.getMainLooper());
    public HashMap<String, CometChatMessageTemplate> messageTemplateHashMap;
    private int smartRepliesDelayDuration;
    private MessagesRequest.MessagesRequestBuilder messagesRequestBuilder = null;
    private MessagesRequest messagesRequest;
    private boolean hasMore = true;
    private boolean disableReceipt;
    private Group group;
    private User user;
    private String id;
    private String type;
    private boolean hideDeleteMessage;
    private List<String> messagesTypes;
    private List<String> messagesCategories;
    private long parentMessageId = -1;
    private String conversationId;
    private boolean disableReactions;
    private List<String> smartRepliesKeywords;
    private Timer smartReplyDelayTimer;
    private boolean enableConversationStarter = false;
    private boolean enableSmartReplies = false;

    public MessageListViewModel() {
        mutableMessageList = new MutableLiveData<>();
        mutableMessagesRangeChanged = new MutableLiveData<>();
        updateMessage = new MutableLiveData<>();
        onMessageDeleted = new MutableLiveData<>();
        removeMessage = new MutableLiveData<>();
        readMessage = new MutableLiveData<>();
        addMessage = new MutableLiveData<>();
        messageDeleteState = new MutableLiveData<>();
        cometchatException = new MutableLiveData<>();
        mutableIsInProgress = new MutableLiveData<>();
        mutableHasMore = new MutableLiveData<>();
        processMessageData = new MutableLiveData<>();
        states = new MutableLiveData<>();
        messageArrayList = new ArrayList<>();
        messagesTypes = new ArrayList<>();
        messagesCategories = new ArrayList<>();
        notifyUpdate = new MutableLiveData<>();
        mutableHashMap = new MutableLiveData<>();
        closeBottomPanel = new MutableLiveData<>();
        closeTopPanel = new MutableLiveData<>();
        showTopPanel = new MutableLiveData<>();
        showBottomPanel = new MutableLiveData<>();
        idMap = new HashMap<>();
        smartReplyDelayTimer = new Timer();
        actionMessageTypes = new ArrayList<>();
        messageTemplateHashMap = new HashMap<>();
        actionMessageTypes.add(CometChatConstants.CATEGORY_MESSAGE);
        actionCategories = new ArrayList<>();
        smartRepliesKeywords = new ArrayList<>();
        mutableSmartReplies = new MutableLiveData<>();
        mutableConversationStarterReplies = new MutableLiveData<>();
        removeConversationStarter = new MutableLiveData<>();
        smartRepliesDelayDuration = 10000;
        smartReplayUIState = new MutableLiveData<>();
        conversationStarterUIState = new MutableLiveData<>();
        actionCategories.add(CometChatConstants.CATEGORY_ACTION);
        LISTENERS_TAG = System.currentTimeMillis() + "";
    }

    public MutableLiveData<BaseMessage> getProcessMessageData() {
        return processMessageData;
    }

    public MutableLiveData<Boolean> getRemoveConversationStarter() {
        return removeConversationStarter;
    }

    public MutableLiveData<UIKitConstants.States> getSmartRepliesUIState() {
        return smartReplayUIState;
    }

    public MutableLiveData<UIKitConstants.States> getConversationStarterUIState() {
        return conversationStarterUIState;
    }

    public MutableLiveData<List<BaseMessage>> getMutableMessageList() {
        return mutableMessageList;
    }

    public MutableLiveData<List<String>> getMutableSmartReplies() {
        return mutableSmartReplies;
    }

    public MutableLiveData<List<String>> getMutableConversationStarterReplies() {
        return mutableConversationStarterReplies;
    }

    public MutableLiveData<Integer> messagesRangeChanged() {
        return mutableMessagesRangeChanged;
    }

    public MutableLiveData<Integer> updateMessage() {
        return updateMessage;
    }

    public MutableLiveData<BaseMessage> getOnMessageDeleted() {
        return onMessageDeleted;
    }

    public MutableLiveData<Integer> removeMessage() {
        return removeMessage;
    }

    public MutableLiveData<BaseMessage> addMessage() {
        return addMessage;
    }

    public MutableLiveData<UIKitConstants.DeleteState> getMessageDeleteState() {
        return messageDeleteState;
    }

    public MutableLiveData<CometChatException> getCometChatException() {
        return cometchatException;
    }

    public MutableLiveData<BaseMessage> getReadMessage() {
        return readMessage;
    }

    public MutableLiveData<UIKitConstants.States> getStates() {
        return states;
    }

    public MutableLiveData<Boolean> getMutableIsInProgress() {
        return mutableIsInProgress;
    }

    public MutableLiveData<Boolean> getMutableHasMore() {
        return mutableHasMore;
    }

    public MutableLiveData<Void> notifyUpdate() {
        return notifyUpdate;
    }

    public MutableLiveData<HashMap<String, String>> getMutableHashMap() {
        return mutableHashMap;
    }

    public MutableLiveData<Void> closeBottomPanel() {
        return closeBottomPanel;
    }

    public MutableLiveData<Void> closeTopPanel() {
        return closeTopPanel;
    }

    public MutableLiveData<Function1<Context, View>> showTopPanel() {
        return showTopPanel;
    }

    public MutableLiveData<Function1<Context, View>> showBottomPanel() {
        return showBottomPanel;
    }

    public void setMessageTemplateHashMap(HashMap<String, CometChatMessageTemplate> messageTemplateHashMap) {
        this.messageTemplateHashMap = messageTemplateHashMap;
    }

    public void setGroup(Group group, List<String> messagesTypes, List<String> messagesCategories, long parentMessageId) {
        if (group != null) {
            this.group = group;
            this.type = UIKitConstants.ReceiverType.GROUP;
            this.id = group.getGuid();
            this.messagesTypes = messagesTypes;
            this.messagesCategories = messagesCategories;
            this.parentMessageId = parentMessageId;
            setIdMap();
        }
        initializeGroupRequestBuilder();
    }

    public void setIdMap() {
        if (parentMessageId > 0) idMap.put(UIKitConstants.MapId.PARENT_MESSAGE_ID, String.valueOf(parentMessageId));
        if (user != null) {
            idMap.put(UIKitConstants.MapId.RECEIVER_ID, user.getUid());
            idMap.put(UIKitConstants.MapId.RECEIVER_TYPE, UIKitConstants.ReceiverType.USER);
        } else if (group != null) {
            idMap.put(UIKitConstants.MapId.RECEIVER_ID, group.getGuid());
            idMap.put(UIKitConstants.MapId.RECEIVER_TYPE, UIKitConstants.ReceiverType.GROUP);
        }
        mutableHashMap.setValue(idMap);
    }

    public void initializeGroupRequestBuilder() {
        if (messagesRequestBuilder == null) {
            messagesRequestBuilder = new MessagesRequest.MessagesRequestBuilder()
                .setTypes(this.messagesTypes)
                .setLimit(limit)
                .setCategories(this.messagesCategories)
                .hideReplies(true);
            if (parentMessageId > -1) messagesRequestBuilder.setParentMessageId(parentMessageId);
        }
        messagesRequest = messagesRequestBuilder.setGUID(id).build();
    }

    public void setUser(User user, List<String> messagesTypes, List<String> messagesCategories, long parentMessageId) {
        if (user != null) {
            this.user = user;
            this.id = user.getUid();
            this.type = UIKitConstants.ReceiverType.USER;
            this.messagesTypes = messagesTypes;
            this.messagesCategories = messagesCategories;
            this.parentMessageId = parentMessageId;
            setIdMap();
        }
        initializeUserRequestBuilder();
    }

    public void initializeUserRequestBuilder() {
        if (messagesRequestBuilder == null) {
            messagesRequestBuilder = new MessagesRequest.MessagesRequestBuilder()
                .setTypes(this.messagesTypes)
                .setLimit(limit)
                .setCategories(this.messagesCategories)
                .hideReplies(true);
            if (parentMessageId > -1) messagesRequestBuilder.setParentMessageId(parentMessageId);
        }
        messagesRequest = messagesRequestBuilder.setUID(id).build();
    }

    public void setSmartReplyKeywords(List<String> keywords) {
        if (keywords != null) this.smartRepliesKeywords = keywords;
    }

    public void setSmartRepliesDelay(int delay) {
        this.smartRepliesDelayDuration = delay;
    }

    public void setEnableConversationStarter(boolean enable) {
        this.enableConversationStarter = enable;
    }

    public void setEnableSmartReplies(boolean enable) {
        this.enableSmartReplies = enable;
    }

    public void setMessagesTypesAndCategories(List<String> messagesTypes, List<String> messagesCategories) {
        this.messagesTypes = messagesTypes;
        this.messagesCategories = messagesCategories;
        messageArrayList.clear();
        if (user != null) initializeUserRequestBuilder();
        else if (group != null) initializeGroupRequestBuilder();
    }

    public void setDisableReactions(boolean reactions) {
        this.disableReactions = reactions;
    }

    public void addListener() {

        CometChat.addGroupListener(LISTENERS_TAG, new CometChat.GroupListener() {
            @Override
            public void onGroupMemberJoined(Action action, User joinedUser, Group joinedGroup) {
                onMessageReceived(action);
            }

            @Override
            public void onGroupMemberLeft(Action action, User leftUser, Group leftGroup) {
                onMessageReceived(action);
            }

            @Override
            public void onGroupMemberKicked(Action action, User kickedUser, User kickedBy, Group kickedFrom) {
                onMessageReceived(action);
            }

            @Override
            public void onGroupMemberBanned(Action action, User bannedUser, User bannedBy, Group bannedFrom) {
                onMessageReceived(action);
            }

            @Override
            public void onGroupMemberUnbanned(Action action, User unbannedUser, User unbannedBy, Group unbannedFrom) {
                onMessageReceived(action);
            }

            @Override
            public void onGroupMemberScopeChanged(Action action,
                                                  User updatedBy,
                                                  User updatedUser,
                                                  String scopeChangedTo,
                                                  String scopeChangedFrom,
                                                  Group group) {
                updateGroupScope(group, updatedUser, scopeChangedTo);
                onMessageReceived(action);
            }

            @Override
            public void onMemberAddedToGroup(Action action, User addedBy, User userAdded, Group addedTo) {
                onMessageReceived(action);
            }
        });

        CometChatMessageEvents.addListener(LISTENERS_TAG, new CometChatMessageEvents() {
            @Override
            public void ccMessageSent(BaseMessage message, int status) {
                if (status == MessageStatus.IN_PROGRESS) {
                    if (isThreadedMessageForTheCurrentChat(message)) addMessage(message);
                } else if (status == MessageStatus.SUCCESS || status == MessageStatus.ERROR) updateOptimisticMessage(message);
            }

            @Override
            public void ccMessageEdited(BaseMessage baseMessage, @MessageStatus int status) {
                if (status == MessageStatus.SUCCESS) updateMessage(baseMessage);
            }

            @Override
            public void ccMessageDeleted(BaseMessage baseMessage) {
                onMessageDeleted.setValue(baseMessage);
                if (hideDeleteMessage) removeMessage(baseMessage);
                else updateMessage(baseMessage);
            }

            @Override
            public void onTextMessageReceived(TextMessage message) {
                onMessageReceived(message);
                fetchSmartRepliesWithDelay(message);
            }

            @Override
            public void onMediaMessageReceived(MediaMessage message) {
                onMessageReceived(message);
            }

            @Override
            public void onCustomMessageReceived(CustomMessage message) {
                onMessageReceived(message);
            }

            @Override
            public void onMessagesDelivered(MessageReceipt messageReceipt) {
                setMessageReceipt(messageReceipt);
            }

            @Override
            public void onMessagesRead(MessageReceipt messageReceipt) {
                setMessageReceipt(messageReceipt);
            }

            @Override
            public void onMessageEdited(BaseMessage message) {
                updateMessage(message);
            }

            @Override
            public void onMessageDeleted(BaseMessage message) {
                onMessageDeleted.setValue(message);
                if (hideDeleteMessage) removeMessage(message);
                else updateMessage(message);
            }

            @Override
            public void onFormMessageReceived(FormMessage formMessage) {
                onMessageReceived(formMessage);
            }

            @Override
            public void onSchedulerMessageReceived(SchedulerMessage schedulerMessage) {
                onMessageReceived(schedulerMessage);
            }

            @Override
            public void onCardMessageReceived(CardMessage cardMessage) {
                onMessageReceived(cardMessage);
            }

            @Override
            public void onInteractionGoalCompleted(InteractionReceipt interactionReceipt) {
                setInteractions(interactionReceipt);
            }

            @Override
            public void onCustomInteractiveMessageReceived(CustomInteractiveMessage customInteractiveMessage) {
                onMessageReceived(customInteractiveMessage);
            }

            @Override
            public void onMessageReactionAdded(ReactionEvent reactionEvent) {
                if (!disableReactions) onReactionAdded(reactionEvent);
            }

            @Override
            public void onMessageReactionRemoved(ReactionEvent reactionEvent) {
                if (!disableReactions) onReactionRemoved(reactionEvent);
            }

            @Override
            public void onMessagesDeliveredToAll(MessageReceipt messageReceipt) {
                setMessageReceipt(messageReceipt);
            }

            @Override
            public void onMessagesReadByAll(MessageReceipt messageReceipt) {
                setMessageReceipt(messageReceipt);
            }
        });

        CometChatGroupEvents.addGroupListener(LISTENERS_TAG, new CometChatGroupEvents() {
            @Override
            public void ccGroupMemberAdded(List<Action> actionMessages, List<User> usersAdded, Group userAddedIn, User addedBy) {
                for (Action action : actionMessages) {
                    onMessageReceived(action);
                }
            }

            @Override
            public void ccGroupMemberKicked(Action actionMessage, User kickedUser, User kickedBy, Group kickedFrom) {
                onMessageReceived(actionMessage);
            }

            @Override
            public void ccGroupMemberBanned(Action actionMessage, User bannedUser, User bannedBy, Group bannedFrom) {
                onMessageReceived(actionMessage);
            }

            @Override
            public void ccGroupMemberUnBanned(Action actionMessage, User unbannedUser, User unBannedBy, Group unBannedFrom) {
                onMessageReceived(actionMessage);
            }

            @Override
            public void ccGroupMemberScopeChanged(Action actionMessage,
                                                  User updatedUser,
                                                  String scopeChangedTo,
                                                  String scopeChangedFrom,
                                                  Group group) {
                onMessageReceived(actionMessage);
            }

            @Override
            public void ccOwnershipChanged(Group group, GroupMember newOwner) {
            }
        });

        CometChatUIEvents.addListener(LISTENERS_TAG, new CometChatUIEvents() {
            @Override
            public void showPanel(HashMap<String, String> id, UIKitConstants.CustomUIPosition alignment, Function1<Context, View> view) {
                if (UIKitConstants.CustomUIPosition.MESSAGE_LIST_BOTTOM.equals(alignment) && idMap.equals(id)) showBottomPanel.setValue(view);
                else if (UIKitConstants.CustomUIPosition.MESSAGE_LIST_TOP.equals(alignment) && idMap.equals(id)) showTopPanel.setValue(view);
            }

            @Override
            public void hidePanel(HashMap<String, String> id, UIKitConstants.CustomUIPosition alignment) {
                if (UIKitConstants.CustomUIPosition.MESSAGE_LIST_BOTTOM.equals(alignment) && idMap.equals(id)) closeBottomPanel.setValue(aVoid);
                else if (UIKitConstants.CustomUIPosition.MESSAGE_LIST_TOP.equals(alignment) && idMap.equals(id)) closeTopPanel.setValue(aVoid);
            }
        });
        if (isCallingAdded()) {
            CometChatCallEvents.addListener(LISTENERS_TAG, new CometChatCallEvents() {
                @Override
                public void ccOutgoingCall(Call call) {
                    onMessageReceived(call);
                }

                @Override
                public void ccCallAccepted(Call call) {
                    onMessageReceived(call);
                }

                @Override
                public void ccCallRejected(Call call) {
                    onMessageReceived(call);
                }

                @Override
                public void ccCallEnded(Call call) {
                    onMessageReceived(call);
                }

                @Override
                public void ccMessageSent(BaseMessage message, int status) {
                    if (status == MessageStatus.IN_PROGRESS) {
                        if (isThreadedMessageForTheCurrentChat(message)) addMessage(message);
                    } else if (status == MessageStatus.SUCCESS || status == MessageStatus.ERROR) updateOptimisticMessage(message);
                }
            });

            CometChat.addCallListener(LISTENERS_TAG, new CometChat.CallListener() {
                @Override
                public void onIncomingCallReceived(Call call) {
                    onMessageReceived(call);
                }

                @Override
                public void onOutgoingCallAccepted(Call call) {
                    onMessageReceived(call);
                }

                @Override
                public void onOutgoingCallRejected(Call call) {
                    onMessageReceived(call);
                }

                @Override
                public void onIncomingCallCancelled(Call call) {
                    onMessageReceived(call);
                }

                @Override
                public void onCallEndedMessageReceived(Call call) {
                    onMessageReceived(call);
                }
            });
        }
    }

    private void updateGroupScope(Group group, User user, String scopeChangedTo) {
        if (this.group != null) {
            if (group.getGuid().equalsIgnoreCase(this.group.getGuid())) {
                if (user.getUid().equalsIgnoreCase(CometChatUIKit.getLoggedInUser().getUid())) {
                    this.group.setScope(scopeChangedTo);
                }
            }
        }
    }

    public void setInteractions(InteractionReceipt interactionReceipt) {
        if (interactionReceipt != null && interactionReceipt.getSender().getUid().equalsIgnoreCase(CometChatUIKit.getLoggedInUser().getUid())) {
            for (int i = messageArrayList.size() - 1; i >= 0; i--) {
                BaseMessage baseMessage = messageArrayList.get(i);
                if (baseMessage.getId() == interactionReceipt.getMessageId()) {
                    InteractiveMessage interactiveMessage = (InteractiveMessage) baseMessage;
                    interactiveMessage.setInteractions(interactionReceipt.getInteractions());
                    updateMessage(baseMessage);
                    break;
                }
            }
        }
    }

    private void onReactionAdded(ReactionEvent reactionEvent) {
        if (reactionEvent.getConversationId().equals(conversationId)) {
            for (int i = messageArrayList.size() - 1; i >= 0; i--) {
                BaseMessage baseMessage = messageArrayList.get(i);
                if (baseMessage.getId() == reactionEvent.getReaction().getMessageId()) {
                    BaseMessage modifiedBaseMessage = CometChatHelper.updateMessageWithReactionInfo(baseMessage,
                                                                                                    reactionEvent.getReaction(),
                                                                                                    CometChatConstants.REACTION_ADDED);
                    updateMessage(modifiedBaseMessage);
                    break;
                }
            }
        }
    }

    private void onReactionRemoved(ReactionEvent reactionEvent) {
        if (reactionEvent.getConversationId().equals(conversationId)) {
            for (int i = messageArrayList.size() - 1; i >= 0; i--) {
                BaseMessage baseMessage = messageArrayList.get(i);
                if (baseMessage.getId() == reactionEvent.getReaction().getMessageId()) {
                    BaseMessage modifiedBaseMessage = CometChatHelper.updateMessageWithReactionInfo(baseMessage,
                                                                                                    reactionEvent.getReaction(),
                                                                                                    CometChatConstants.REACTION_REMOVED);
                    updateMessage(modifiedBaseMessage);
                    break;
                }
            }
        }
    }

    public void setMessage(BaseMessage message) {
        removeConversationStarter.setValue(Boolean.TRUE);
        if (isThreadedMessageForTheCurrentChat(message)) addMessage(message);
        else updateReplyCount(message.getParentMessageId());
    }

    public boolean isThreadedMessageForTheCurrentChat(BaseMessage baseMessage) {
        if (baseMessage.getParentMessageId() == 0 && parentMessageId == -1) {
            return true;
        } else return parentMessageId > -1 && parentMessageId == baseMessage.getParentMessageId();
    }

    public void hideDeleteMessages(boolean hide) {
        this.hideDeleteMessage = hide;
    }

    public void updateOptimisticMessage(BaseMessage baseMessage) {
        if (baseMessage != null) {
            if (isThreadedMessageForTheCurrentChat(baseMessage)) {
                updateMessageFromMUID(baseMessage);
            } else {
                updateReplyCount(baseMessage.getParentMessageId());
            }
            checkIsEmpty(messageArrayList);
        }
    }

    public void onMessageEdit(BaseMessage baseMessage) {
        CometChatUIKitHelper.onMessageEdited(baseMessage, MessageStatus.IN_PROGRESS);
    }

    public void updateMessageFromMUID(BaseMessage baseMessage) {
        for (int i = messageArrayList.size() - 1; i >= 0; i--) {
            String mUid = messageArrayList.get(i).getMuid();
            if (mUid != null && mUid.equals(baseMessage.getMuid())) {
                messageArrayList.remove(i);
                messageArrayList.add(i, baseMessage);
                updateMessage.setValue(i);
            }
        }
    }

    public void updateReplyCount(long parentMessageId) {
        for (int i = 0; i < messageArrayList.size(); i++) {
            BaseMessage baseMessage = messageArrayList.get(i);
            int replyCount = baseMessage.getReplyCount();
            if (baseMessage.getId() == parentMessageId) {
                baseMessage.setReplyCount(++replyCount);
                updateMessage(baseMessage);
                break;
            }
        }
    }

    public void setMessageReceipt(MessageReceipt messageReceipt) {
        if (messageReceipt.getReceivertype().equals(CometChatConstants.RECEIVER_TYPE_USER)) {
            if (messageReceipt.getSender().getUid().equals(id)) {
                if (messageReceipt.getReceiptType().equals(MessageReceipt.RECEIPT_TYPE_DELIVERED)) setDeliveryReceipts(messageReceipt);
                else if (messageReceipt.getReceiptType().equals(MessageReceipt.RECEIPT_TYPE_READ)) setReadReceipts(messageReceipt);
            }
        } else if (messageReceipt.getReceivertype().equals(CometChatConstants.RECEIVER_TYPE_GROUP)) {
            if (messageReceipt.getReceiverId().equals(id)) {
                if (messageReceipt.getReceiptType().equals(MessageReceipt.RECEIPT_TYPE_DELIVERED_TO_ALL)) setDeliveryReceipts(messageReceipt);
                else if (messageReceipt.getReceiptType().equals(MessageReceipt.RECEIPT_TYPE_READ_BY_ALL)) setReadReceipts(messageReceipt);
            }
        }
    }

    public void setDeliveryReceipts(MessageReceipt messageReceipt) {
        boolean isDelivered = false;
        for (int i = messageArrayList.size() - 1; i >= 0; i--) {
            BaseMessage baseMessage = messageArrayList.get(i);
            if (baseMessage.getDeliveredAt() == 0 || baseMessage.getId() == messageReceipt.getMessageId()) {
                isDelivered = true;
                baseMessage.setDeliveredAt(messageReceipt.getDeliveredAt());
            } else if (isDelivered) {
                break;
            }
        }
        notifyUpdate.setValue(unused);
    }

    public void setReadReceipts(MessageReceipt messageReceipt) {
        boolean isRead = false;
        for (int i = messageArrayList.size() - 1; i >= 0; i--) {
            BaseMessage baseMessage = messageArrayList.get(i);
            if (baseMessage.getReadAt() == 0 || baseMessage.getId() == messageReceipt.getMessageId()) {
                isRead = true;
                baseMessage.setReadAt(messageReceipt.getReadAt());
            } else if (isRead) {
                break;
            }
        }
        notifyUpdate.setValue(unused);
    }

    public void fetchMessages() {
        fetchMessages(0);
    }

    public void fetchMessages(int unreadCount) {
        if (messagesRequestBuilder != null && messagesRequest != null) {
            if (hasMore) {
                if (messageArrayList.isEmpty()) states.setValue(UIKitConstants.States.LOADING);
                messagesRequest.fetchPrevious(new CometChat.CallbackListener<List<BaseMessage>>() {
                    @Override
                    public void onSuccess(List<BaseMessage> messageList) {
                        new Thread(() -> {
                            hasMore = !messageList.isEmpty();
                            if (hasMore) {
                                processMessageList(messageList);
                            }
                            if (messageArrayList.isEmpty()) {
                                messageArrayList.addAll(0, messageList);
                                mutableMessageList.postValue(messageArrayList);
                            } else {
                                messageArrayList.addAll(0, messageList);
                                mutableMessagesRangeChanged.postValue(messageList.size());
                            }
                            handler.post(() -> {
                                if (firstFetch) {
                                    CometChatUIKitHelper.onActiveChatChanged(getIdMap(),
                                                                             messageList.isEmpty() ? null : messageList.get(messageList.size() - 1),
                                                                             user,
                                                                             group,
                                                                             unreadCount);
                                    addConnectionListener();
                                    firstFetch = false;
                                    if (messageList.isEmpty()) {
                                        fetchConversationStarter();
                                    } else {
                                        if (messageList.get(messageList.size() - 1) instanceof TextMessage)
                                            fetchSmartRepliesWithDelay((TextMessage) messageList.get(messageList.size() - 1));
                                    }

                                }
                                mutableHasMore.setValue(hasMore);
                                mutableIsInProgress.setValue(false);
                                states.setValue(UIKitConstants.States.LOADED);
                                states.setValue(checkIsEmpty(messageArrayList));
                                conversationId = !messageArrayList.isEmpty() ? messageArrayList.get(0).getConversationId() : null;
                            });
                        }).start();
                    }

                    @Override
                    public void onError(CometChatException exception) {
                        cometchatException.setValue(exception);
                        states.setValue(UIKitConstants.States.LOADED);
                        states.setValue(UIKitConstants.States.ERROR);
                    }
                });
            }
        }
    }

    public void processMessageList(List<BaseMessage> messageList) {
        messageList.replaceAll(Utils::convertToUIKitMessage);
    }

    public HashMap<String, String> getIdMap() {
        HashMap<String, String> idMap = new HashMap<>();
        if (parentMessageId > 0) idMap.put(UIKitConstants.MapId.PARENT_MESSAGE_ID, String.valueOf(parentMessageId));
        if (user != null) {
            idMap.put(UIKitConstants.MapId.RECEIVER_ID, user.getUid());
            idMap.put(UIKitConstants.MapId.RECEIVER_TYPE, UIKitConstants.ReceiverType.USER);
        } else if (group != null) {
            idMap.put(UIKitConstants.MapId.RECEIVER_ID, group.getGuid());
            idMap.put(UIKitConstants.MapId.RECEIVER_TYPE, UIKitConstants.ReceiverType.GROUP);
        }
        return idMap;
    }

    public void addConnectionListener() {
        CometChat.addConnectionListener(LISTENERS_TAG, new CometChat.ConnectionListener() {
            @Override
            public void onConnected() {
                fetchMissedMessages();
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

    private void fetchConversationStarter() {
        if (enableConversationStarter && parentMessageId == -1)
            conversationStarterUIState.postValue(UIKitConstants.States.LOADING);
        CometChat.getConversationStarter(
            user != null ? user.getUid() : group != null ? group.getGuid() : "",
            user != null ? UIKitConstants.ReceiverType.USER : group != null ? UIKitConstants.ReceiverType.GROUP : "",
            null,
            new CometChat.CallbackListener<List<String>>() {
                @Override
                public void onSuccess(List<String> list) {
                    mutableConversationStarterReplies.setValue(list);
                }

                @Override
                public void onError(CometChatException e) {
                    CometChatLogger.e(TAG, e.toString());
                    conversationStarterUIState.setValue(UIKitConstants.States.ERROR);
                    cometchatException.setValue(e);
                }
            });
    }

    public void fetchSmartRepliesWithDelay(TextMessage textMessage) {
        if (isMessageForCurrentChat(textMessage) && !textMessage
            .getSender()
            .getUid()
            .equals(CometChatUIKit.getLoggedInUser().getUid()) && parentMessageId == -1) {
            if (enableSmartReplies) {
                smartReplyDelayTimer.cancel();
                smartReplyDelayTimer = new Timer();
                smartReplyDelayTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        fetchSmartRepliesWithKeywordsCheck(textMessage);
                    }
                }, smartRepliesDelayDuration);
            }
        }
    }

    public UIKitConstants.States checkIsEmpty(List<BaseMessage> baseMessageList) {
        if (baseMessageList.isEmpty()) return UIKitConstants.States.EMPTY;
        return UIKitConstants.States.NON_EMPTY;
    }

    public void fetchMissedMessages() {
        Thread fetchMissedThread = new Thread(() -> {
            updateListByActionMessages();
            fetchNextMessages();
        });
        fetchMissedThread.start();
    }

    private boolean isMessageForCurrentChat(BaseMessage baseMessage) {
        return ((id != null && !id.isEmpty()) && ((baseMessage
            .getReceiverType()
            .equals(CometChatConstants.RECEIVER_TYPE_USER) && (id.equalsIgnoreCase(
            baseMessage.getSender().getUid()) || id.equalsIgnoreCase(baseMessage.getReceiverUid()) && baseMessage
            .getSender()
            .getUid()
            .equalsIgnoreCase(CometChatUIKit.getLoggedInUser().getUid()))) || (baseMessage
            .getReceiverType()
            .equals(CometChatConstants.RECEIVER_TYPE_GROUP) && (id.equalsIgnoreCase(baseMessage.getReceiverUid())))));
    }

    public void fetchSmartRepliesWithKeywordsCheck(TextMessage textMessage) {
        if (textMessage != null) {
            if (!smartRepliesKeywords.isEmpty()) {
                boolean isKeyPresent = false;
                String text = textMessage.getText();
                if (text != null && !text.isEmpty()) {
                    for (String keyword : smartRepliesKeywords) {
                        if (text.toLowerCase().contains(keyword.toLowerCase())) {
                            isKeyPresent = true;
                            break;
                        }
                    }
                    if (!isKeyPresent) {
                        return;
                    }
                }
            }
            fetchSmartReplies();
        }
    }

    public void updateListByActionMessages() {
        if (!messageArrayList.isEmpty()) {
            MessagesRequest.MessagesRequestBuilder actionRequestBuilder = new MessagesRequest.MessagesRequestBuilder()
                .setMessageId(messageArrayList.get(messageArrayList.size() - 1).getId())
                .setTypes(actionMessageTypes)
                .setCategories(actionCategories);
            if (user != null) actionMessagesRequest = actionRequestBuilder.setUID(user.getUid()).build();
            else if (group != null) actionMessagesRequest = actionRequestBuilder.setGUID(group.getGuid()).build();
            if (actionMessagesRequest != null) {
                actionMessagesRequest.fetchNext(new CometChat.CallbackListener<List<BaseMessage>>() {
                    @Override
                    public void onSuccess(List<BaseMessage> baseMessages) {
                        for (BaseMessage baseMessage : baseMessages) {
                            if (baseMessage.getCategory().equals(CometChatConstants.CATEGORY_ACTION)) {
                                if (baseMessage instanceof Action && ((Action) baseMessage).getActionOn() != null && ((Action) baseMessage).getActionOn() instanceof BaseMessage) {
                                    BaseMessage actionOn = (BaseMessage) ((Action) baseMessage).getActionOn();
                                    updateMessage(Utils.convertToUIKitMessage(actionOn));
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(CometChatException e) {
                        CometChatLogger.e(TAG, e.toString());
                    }
                });
            }
        }
    }

    public void fetchNextMessages() {
        if (!messageArrayList.isEmpty()) {
            MessagesRequest fetchNextMessagesRequest = messagesRequestBuilder
                .setMessageId(messageArrayList.get(messageArrayList.size() - 1).getId())
                .build();
            fetchNextMessagesRequest.fetchNext(new CometChat.CallbackListener<List<BaseMessage>>() {
                @Override
                public void onSuccess(List<BaseMessage> baseMessages) {
                    if (!baseMessages.isEmpty()) {
                        for (BaseMessage baseMessage : baseMessages) {
                            addMessage(Utils.convertToUIKitMessage(baseMessage));
                        }
                        fetchNextMessages();
                    } else {
                        CometChatUIKitHelper.onActiveChatChanged(getIdMap(), messageArrayList.get(messageArrayList.size() - 1), user, group);
                    }
                }

                @Override
                public void onError(CometChatException e) {
                }
            });
        }
    }

    public void fetchSmartReplies() {
        smartReplayUIState.postValue(UIKitConstants.States.LOADING);
        CometChat.getSmartReplies(
            user != null ? user.getUid() : group != null ? group.getGuid() : "",
            user != null ? UIKitConstants.ReceiverType.USER : group != null ? UIKitConstants.ReceiverType.GROUP : "",
            null,
            new CometChat.CallbackListener<HashMap<String, String>>() {
                @Override
                public void onSuccess(HashMap<String, String> list) {
                    mutableSmartReplies.setValue(new ArrayList<>(list.values()));
                }

                @Override
                public void onError(CometChatException e) {
                    CometChatLogger.e(TAG, e.getMessage());
                    smartReplayUIState.setValue(UIKitConstants.States.ERROR);
                    cometchatException.setValue(e);
                }
            });
    }

    public void addMessage(BaseMessage message) {
        if (message != null) {
            removeConversationStarter.setValue(Boolean.TRUE);
            if (messageArrayList.isEmpty()) addList(messageArrayList);
            messageArrayList.add(message);
            addMessage.setValue(message);
            states.setValue(checkIsEmpty(messageArrayList));
        }
    }

    public void addList(List<BaseMessage> messageList) {
        if (messageArrayList.isEmpty()) {
            this.messageArrayList.addAll(0, messageList);
            mutableMessageList.setValue(messageArrayList);
        } else {
            this.messageArrayList.addAll(0, messageList);
            mutableMessagesRangeChanged.setValue(messageList.size());
        }
    }

    public void fetchMessagesWithUnreadCount() {
        states.setValue(UIKitConstants.States.LOADING);
        if (user != null) {
            CometChat.getUnreadMessageCountForUser(user.getUid(), new CometChat.CallbackListener<HashMap<String, Integer>>() {
                @Override
                public void onSuccess(HashMap<String, Integer> stringIntegerHashMap) {
                    int unreadCount = 0;
                    if (stringIntegerHashMap != null && stringIntegerHashMap.containsKey(user.getUid())) {
                        Integer count = stringIntegerHashMap.get(user.getUid());
                        unreadCount = (count != null) ? count : 0;
                    }
                    fetchMessages(unreadCount);
                }

                @Override
                public void onError(CometChatException e) {
                    CometChatLogger.e(TAG, e.toString());
                    fetchMessages(0);
                }
            });
        } else if (group != null) {
            CometChat.getUnreadMessageCountForGroup(group.getGuid(), new CometChat.CallbackListener<HashMap<String, Integer>>() {
                @Override
                public void onSuccess(HashMap<String, Integer> stringIntegerHashMap) {
                    int unreadCount = 0;
                    if (stringIntegerHashMap != null && stringIntegerHashMap.containsKey(group.getGuid())) {
                        Integer count = stringIntegerHashMap.get(group.getGuid());
                        unreadCount = (count != null) ? count : 0;
                    }
                    fetchMessages(unreadCount);
                }

                @Override
                public void onError(CometChatException e) {
                    CometChatLogger.e(TAG, e.toString());
                    fetchMessages(0);
                }
            });
        }
    }

    public void markLastMessageAsRead(BaseMessage lastMessage) {
        boolean markAsRead = false;
        if (lastMessage != null) {
            if (lastMessage.getReadAt() == 0 && lastMessage.getParentMessageId() == 0) markAsRead = true;
            else if (parentMessageId > -1 && parentMessageId == lastMessage.getParentMessageId() && lastMessage.getReadAt() == 0) markAsRead = true;

            if (markAsRead && !disableReceipt) {
                if (lastMessage.getSender().getUid().equals(CometChatUIKit.getLoggedInUser().getUid())) return;
                markMessageAsRead(lastMessage);
            }
        }
    }

    public void markMessageAsRead(BaseMessage lastMessage) {
        CometChat.markAsRead(lastMessage, new CometChat.CallbackListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                lastMessage.setReadAt(System.currentTimeMillis() / 1000);
                CometChatUIKitHelper.onMessageRead(lastMessage);
                updateMessage(lastMessage);
            }

            @Override
            public void onError(CometChatException e) {
            }
        });
    }

    public void updateMessage(BaseMessage message) {
        if (message != null) {
            if (messageArrayList.contains(message)) {
                int index = messageArrayList.indexOf(message);
                BaseMessage oldMessage = messageArrayList.get(index);
                messageArrayList.remove(oldMessage);
                messageArrayList.add(index, message);
                updateMessage.setValue(index);
            }
        }
    }

    public void disableReceipt(boolean disableReceipt) {
        this.disableReceipt = disableReceipt;
    }

    public void markAsDeliverInternally(BaseMessage message) {
        if (message != null && message.getSender() != null && CometChatUIKit.getLoggedInUser() != null && !message
            .getSender()
            .getUid()
            .equalsIgnoreCase(CometChatUIKit.getLoggedInUser().getUid()) && !disableReceipt) CometChat.markAsDelivered(message);
    }

    public void removeMessage(@Nullable BaseMessage message) {
        if (message != null) {
            int index = messageArrayList.indexOf(message);
            removeMessage.setValue(index);
            messageArrayList.remove(message);
            states.setValue(checkIsEmpty(messageArrayList));
        }
    }

    public void onMessageReceived(BaseMessage message) {
        if (message != null) {
            if (messageTemplateHashMap.containsKey(message.getCategory() + "_" + message.getType())) {
                markAsDeliverInternally(message);
                if (message.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                    if (id != null && id.equalsIgnoreCase(message.getSender().getUid())) {
                        setMessage(message);
                    } else if (id != null && id.equalsIgnoreCase(message.getReceiverUid()) && message
                        .getSender()
                        .getUid()
                        .equalsIgnoreCase(CometChatUIKit.getLoggedInUser().getUid())) {
                        setMessage(message);
                    }
                } else {
                    if (id != null && id.equalsIgnoreCase(message.getReceiverUid())) {
                        setMessage(message);
                    }
                }
            }
        }
    }

    public void setMessagesRequestBuilder(MessagesRequest.MessagesRequestBuilder builder) {
        if (builder != null) {
            this.messagesRequestBuilder = builder;
            if (user != null) initializeUserRequestBuilder();
            else if (group != null) initializeGroupRequestBuilder();
        }
    }

    public void deleteMessage(BaseMessage baseMessage) {
        messageDeleteState.setValue(UIKitConstants.DeleteState.INITIATED_DELETE);
        CometChat.deleteMessage(baseMessage.getId(), new CometChat.CallbackListener<BaseMessage>() {
            @Override
            public void onSuccess(BaseMessage baseMessage) {
                messageDeleteState.setValue(UIKitConstants.DeleteState.SUCCESS_DELETE);
                CometChatUIKitHelper.onMessageDeleted(Utils.convertToUIKitMessage(baseMessage));
            }

            @Override
            public void onError(CometChatException e) {
                cometchatException.setValue(e);
                messageDeleteState.setValue(UIKitConstants.DeleteState.FAILURE_DELETE);
            }
        });
    }

    public void removeListener() {
        CometChat.removeGroupListener(LISTENERS_TAG);
        CometChatMessageEvents.removeListener(LISTENERS_TAG);
        CometChatGroupEvents.removeListener(LISTENERS_TAG);
        CometChatUIEvents.removeListener(LISTENERS_TAG);
        CometChat.removeConnectionListener(LISTENERS_TAG);
        if (isCallingAdded()) {
            CometChatCallEvents.removeListener(LISTENERS_TAG);
            CometChat.removeCallListener(LISTENERS_TAG);
        }
    }

    public boolean isCallingAdded() {
        return messagesCategories.contains(CometChatConstants.CATEGORY_CALL) && (messagesTypes.contains(CometChatConstants.CALL_TYPE_VIDEO) || messagesTypes.contains(
            CometChatConstants.CALL_TYPE_AUDIO));
    }

    public List<BaseMessage> getMessageList() {
        return messageArrayList;
    }

    public BaseMessage getLastMessage() {
        int length = messageArrayList.size() - 1;
        if (length >= 0) return messageArrayList.get(length);
        return null;
    }

    public void fetchMessageSender(BaseMessage baseMessage) {
        if (baseMessage != null && baseMessage.getSender() != null) {
            CometChat.getUser(baseMessage.getSender().getUid(), new CometChat.CallbackListener<User>() {
                @Override
                public void onSuccess(User user) {
                    CometChatUIKitHelper.onOpenChat(user, null);
                }

                @Override
                public void onError(CometChatException e) {
                    cometchatException.setValue(e);
                }
            });
        }
    }

    public void addReaction(BaseMessage baseMessage, String emoji) {
        Reaction reaction = new Reaction();
        reaction.setMessageId(baseMessage.getId());
        reaction.setReaction(emoji);
        reaction.setUid(CometChatUIKit.getLoggedInUser().getUid());
        reaction.setReactedBy(CometChatUIKit.getLoggedInUser());
        BaseMessage newBaseMessage = CometChatHelper.updateMessageWithReactionInfo(baseMessage, reaction, CometChatConstants.REACTION_ADDED);
        updateMessage(newBaseMessage);
        CometChat.addReaction(baseMessage.getId(), emoji, new CometChat.CallbackListener<BaseMessage>() {
            @Override
            public void onSuccess(BaseMessage baseMessage) {
            }

            @Override
            public void onError(CometChatException e) {
                BaseMessage newBaseMessage = CometChatHelper.updateMessageWithReactionInfo(baseMessage,
                                                                                           reaction,
                                                                                           CometChatConstants.REACTION_REMOVED);
                CometChatUIKitHelper.onMessageEdited(newBaseMessage, MessageStatus.SUCCESS);
            }
        });
    }

    public void removeReaction(BaseMessage baseMessage, String emoji) {
        Reaction reaction = new Reaction();
        reaction.setMessageId(baseMessage.getId());
        reaction.setReaction(emoji);
        reaction.setUid(CometChatUIKit.getLoggedInUser().getUid());
        reaction.setReactedBy(CometChatUIKit.getLoggedInUser());
        BaseMessage newBaseMessage = CometChatHelper.updateMessageWithReactionInfo(baseMessage, reaction, CometChatConstants.REACTION_REMOVED);
        updateMessage(newBaseMessage);
        CometChat.removeReaction(baseMessage.getId(), emoji, new CometChat.CallbackListener<BaseMessage>() {
            @Override
            public void onSuccess(BaseMessage baseMessage) {
            }

            @Override
            public void onError(CometChatException e) {
                BaseMessage newBaseMessage = CometChatHelper.updateMessageWithReactionInfo(baseMessage, reaction, CometChatConstants.REACTION_ADDED);
                CometChatUIKitHelper.onMessageEdited(newBaseMessage, MessageStatus.SUCCESS);
            }
        });
    }
}
