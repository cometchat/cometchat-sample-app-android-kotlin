package com.cometchat.chatuikit.aiassistantchathistory;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.core.MessagesRequest;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKitHelper;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.events.CometChatMessageEvents;
import com.cometchat.chatuikit.shared.resources.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class CometChatAIAssistantChatHistoryViewModel extends ViewModel {
    private static final String TAG = "CometChatAIAssistChatHistVM";
    private String LISTENERS_TAG;
    private User user;
    private Group group;
    private MessagesRequest messagesRequest;
    private MessagesRequest.MessagesRequestBuilder messageRequestBuilder;

    private final MutableLiveData<UIKitConstants.DeleteState> deleteStateMutableLiveData;

    // LiveData for observing message updates
    private final MutableLiveData<List<BaseMessage>> messagesLiveData = new MutableLiveData<>();
    private final MutableLiveData<UIKitConstants.States> states = new MutableLiveData<>();
    private final MutableLiveData<Integer> removeMessage;
    private final MutableLiveData<Integer> mutableMessagesRangeChanged;
    private final List<BaseMessage> messageArrayList;
    private boolean hasMore = true;
    private final MutableLiveData<Boolean> mutableHasMore;
    private final MutableLiveData<Boolean> mutableIsInProgress;

    public CometChatAIAssistantChatHistoryViewModel() {
        messageArrayList = new ArrayList<>();
        deleteStateMutableLiveData = new MutableLiveData<>();
        mutableMessagesRangeChanged = new MutableLiveData<>();
        removeMessage = new MutableLiveData<>();
        mutableHasMore = new MutableLiveData<>();
        mutableIsInProgress = new MutableLiveData<>();
    }

    public MutableLiveData<Integer> getMutableMessagesRangeChanged() {
        return mutableMessagesRangeChanged;
    }

    public LiveData<UIKitConstants.States> getStateLiveData() {
        return states;
    }

    public List<BaseMessage> getMessageList() {
        return messageArrayList;
    }

    public MutableLiveData<Boolean> getMutableHasMore() {
        return mutableHasMore;
    }

    public MutableLiveData<Integer> getRemoveMessage() {
        return removeMessage;
    }

    public MutableLiveData<UIKitConstants.DeleteState> getDeleteStateMutableLiveData() {
        return deleteStateMutableLiveData;
    }

    public LiveData<Boolean> getMutableIsInProgress() {
        return mutableIsInProgress;
    }

    public MutableLiveData<List<BaseMessage>> getMessagesLiveData() {
        return messagesLiveData;
    }

    public void addListener() {
        LISTENERS_TAG = System.currentTimeMillis() + "";
        CometChatMessageEvents.addListener(LISTENERS_TAG, new CometChatMessageEvents() {
            @Override
            public void ccMessageDeleted(BaseMessage baseMessage) {
                remove(baseMessage);
            }
        });
    }

    public void remove(BaseMessage baseMessage) {
        int oldIndex = messageArrayList.indexOf(baseMessage);
        messageArrayList.remove(baseMessage);
        removeMessage.setValue(oldIndex);
        if (messageArrayList.isEmpty()) {
            states.setValue(UIKitConstants.States.EMPTY);
        } else states.setValue(UIKitConstants.States.NON_EMPTY);
    }

    // Set user and initialize message request
    public void setUser(User user) {
        this.user = user;
        initializeMessagesRequest();
        fetchMessages();
    }

    public void setGroup(Group group) {
        this.group = group;
        initializeMessagesRequest();
    }

    private void initializeMessagesRequest() {
        if (messageRequestBuilder == null) {
            messageRequestBuilder = new MessagesRequest.MessagesRequestBuilder()
                    .setLimit(20)
                    .setCategories(List.of(UIKitConstants.MessageCategory.MESSAGE))
                    .setTypes(List.of(UIKitConstants.MessageType.TEXT))
                    .hideDeletedMessages(true)
                    .hideReplies(true);

            if (user != null) {
                messagesRequest = messageRequestBuilder.setUID(user.getUid()).build();
            } else if (group != null) {
                messagesRequest = messageRequestBuilder.setGUID(group.getGuid()).build();
            }
        }
    }

    public void fetchMessages() {
        if (messagesRequest == null) {
            return;
        }
        if (hasMore) {
            if (messageArrayList.isEmpty()) states.setValue(UIKitConstants.States.LOADING);
            messagesRequest.fetchPrevious(new CometChat.CallbackListener<List<BaseMessage>>() {
                @Override
                public void onSuccess(List<BaseMessage> messages) {
                    hasMore = !messages.isEmpty();
                    for (int i = messages.size() - 1; i >= 0; i--) {
                        messageArrayList.add(messages.get(i));
                    }
                    messagesLiveData.setValue(messageArrayList);
                    mutableHasMore.setValue(hasMore);
                    mutableIsInProgress.setValue(false);
                    if (messageArrayList.isEmpty()) states.setValue(UIKitConstants.States.EMPTY);
                    else states.setValue(UIKitConstants.States.NON_EMPTY);
                }

                @Override
                public void onError(CometChatException e) {
                    states.setValue(UIKitConstants.States.ERROR);
                }
            });
        }
    }

    public void deleteChatHistoryItem(BaseMessage baseMessage) {
        if (baseMessage != null) {
            deleteStateMutableLiveData.setValue(UIKitConstants.DeleteState.INITIATED_DELETE);
            CometChat.deleteMessage(baseMessage.getId(), new CometChat.CallbackListener<BaseMessage>() {
                @Override
                public void onSuccess(BaseMessage message) {
                    deleteStateMutableLiveData.setValue(UIKitConstants.DeleteState.SUCCESS_DELETE);
                    CometChatUIKitHelper.onMessageDeleted(Utils.convertToUIKitMessage(message));
                }

                @Override
                public void onError(CometChatException e) {
                    deleteStateMutableLiveData.setValue(UIKitConstants.DeleteState.FAILURE_DELETE);
                }
            });
        }
    }

    public void removeListener() {
        CometChatMessageEvents.removeListener(LISTENERS_TAG);
    }
}
