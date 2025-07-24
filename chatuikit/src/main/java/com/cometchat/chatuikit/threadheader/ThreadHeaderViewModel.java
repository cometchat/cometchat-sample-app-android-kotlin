package com.cometchat.chatuikit.threadheader;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cometchat.chat.constants.CometChatConstants;
import com.cometchat.chat.helpers.CometChatHelper;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.CustomMessage;
import com.cometchat.chat.models.MediaMessage;
import com.cometchat.chat.models.MessageReceipt;
import com.cometchat.chat.models.ReactionEvent;
import com.cometchat.chat.models.TextMessage;
import com.cometchat.chatuikit.shared.constants.MessageStatus;
import com.cometchat.chatuikit.shared.events.CometChatMessageEvents;
import com.cometchat.chatuikit.shared.models.interactivemessage.CardMessage;
import com.cometchat.chatuikit.shared.models.interactivemessage.CustomInteractiveMessage;
import com.cometchat.chatuikit.shared.models.interactivemessage.FormMessage;
import com.cometchat.chatuikit.shared.models.interactivemessage.SchedulerMessage;

import java.util.ArrayList;
import java.util.List;

public class ThreadHeaderViewModel extends ViewModel {
    private static final String TAG = ThreadHeaderViewModel.class.getSimpleName();
    public MutableLiveData<BaseMessage> sentMessage;
    public MutableLiveData<BaseMessage> receiveMessage;
    public MutableLiveData<BaseMessage> updateParentMessage;
    public BaseMessage parentMessage;
    public MutableLiveData<Integer> replyCount;
    public MutableLiveData<List<BaseMessage>> parentMessageListLiveData;
    public List<BaseMessage> messageList;
    public boolean hideReaction;
    private String LISTENER_ID;

    public ThreadHeaderViewModel() {
        sentMessage = new MutableLiveData<>();
        messageList = new ArrayList<>();
        receiveMessage = new MutableLiveData<>();
        updateParentMessage = new MutableLiveData<>();
        replyCount = new MutableLiveData<>(0);
        parentMessageListLiveData = new MutableLiveData<>(messageList);
    }

    public MutableLiveData<List<BaseMessage>> getParentMessageListLiveData() {
        return parentMessageListLiveData;
    }

    public MutableLiveData<BaseMessage> getSentMessage() {
        return sentMessage;
    }

    public MutableLiveData<Integer> getReplyCount() {
        return replyCount;
    }

    public MutableLiveData<BaseMessage> getReceiveMessage() {
        return receiveMessage;
    }

    public MutableLiveData<BaseMessage> getUpdateParentMessage() {
        return updateParentMessage;
    }

    public void setParentMessage(BaseMessage parentMessage) {
        if (parentMessage != null) {
            this.parentMessage = parentMessage;
            replyCount.setValue(parentMessage.getReplyCount());
            messageList.clear();
            messageList.add(parentMessage);
            parentMessageListLiveData.setValue(messageList);
        }
    }

    public void addListener() {
        LISTENER_ID = System.currentTimeMillis() + "";
        CometChatMessageEvents.addListener(LISTENER_ID, new CometChatMessageEvents() {
            @Override
            public void ccMessageSent(BaseMessage baseMessage, int status) {
                if (MessageStatus.SUCCESS == status && baseMessage != null && parentMessage != null && parentMessage.getId() == baseMessage.getParentMessageId()) {
                    incrementParentMessageReplyCount();
                }
            }

            @Override
            public void ccMessageEdited(BaseMessage baseMessage, int status) {
                updateParentMessage(baseMessage);
            }

            @Override
            public void ccMessageDeleted(BaseMessage baseMessage) {
                updateParentMessage(baseMessage);
            }

            @Override
            public void onTextMessageReceived(TextMessage textMessage) {
                if (textMessage != null && parentMessage != null && parentMessage.getId() == textMessage.getParentMessageId()) {
                    incrementParentMessageReplyCount();
                }
                updateParentMessage(textMessage);
            }

            @Override
            public void onMediaMessageReceived(MediaMessage mediaMessage) {
                if (mediaMessage != null && parentMessage != null && parentMessage.getId() == mediaMessage.getParentMessageId()) {
                    incrementParentMessageReplyCount();
                }
                updateParentMessage(mediaMessage);
            }

            @Override
            public void onCustomMessageReceived(CustomMessage customMessage) {
                if (customMessage != null && parentMessage != null && parentMessage.getId() == customMessage.getParentMessageId()) {
                    incrementParentMessageReplyCount();
                }
                updateParentMessage(customMessage);
            }

            @Override
            public void onMessagesDelivered(MessageReceipt messageReceipt) {
                updateReceipt(messageReceipt, false);
            }

            @Override
            public void onMessagesRead(@NonNull MessageReceipt messageReceipt) {
                updateReceipt(messageReceipt, true);
            }

            @Override
            public void onMessageEdited(BaseMessage baseMessage) {
                updateParentMessage(baseMessage);
            }

            @Override
            public void onMessageDeleted(BaseMessage baseMessage) {
                updateParentMessage(baseMessage);
            }

            @Override
            public void onFormMessageReceived(FormMessage formMessage) {
                if (formMessage != null && parentMessage != null && parentMessage.getId() == formMessage.getParentMessageId()) {
                    incrementParentMessageReplyCount();
                }
                updateParentMessage(formMessage);
            }

            @Override
            public void onSchedulerMessageReceived(SchedulerMessage schedulerMessage) {
                if (schedulerMessage != null && parentMessage != null && parentMessage.getId() == schedulerMessage.getParentMessageId()) {
                    incrementParentMessageReplyCount();
                }
                updateParentMessage(schedulerMessage);
            }

            @Override
            public void onCardMessageReceived(CardMessage cardMessage) {
                if (cardMessage != null && parentMessage != null && parentMessage.getId() == cardMessage.getParentMessageId()) {
                    incrementParentMessageReplyCount();
                }
                updateParentMessage(cardMessage);
            }

            @Override
            public void onCustomInteractiveMessageReceived(CustomInteractiveMessage customInteractiveMessage) {
                if (customInteractiveMessage != null && parentMessage != null && parentMessage.getId() == customInteractiveMessage.getParentMessageId()) {
                    incrementParentMessageReplyCount();
                }
                updateParentMessage(customInteractiveMessage);
            }

            @Override
            public void onMessageReactionAdded(ReactionEvent reactionEvent) {
                updateReactions(reactionEvent, true);
            }

            @Override
            public void onMessageReactionRemoved(ReactionEvent reactionEvent) {
                updateReactions(reactionEvent, false);
            }

            @Override
            public void onMessagesDeliveredToAll(MessageReceipt messageReceipt) {
                updateReceipt(messageReceipt, false);
            }

            @Override
            public void onMessagesReadByAll(MessageReceipt messageReceipt) {
                updateReceipt(messageReceipt, true);
            }
        });
    }

    private void incrementParentMessageReplyCount() {
        Integer currentCount = replyCount.getValue();
        if (currentCount != null) {
            replyCount.setValue(currentCount + 1);
        } else {
            replyCount.setValue(1);
        }
    }

    public void updateParentMessage(BaseMessage baseMessage) {
        if (baseMessage != null && parentMessage != null) {
            if (baseMessage.getParentMessageId() > 0 && baseMessage.getParentMessageId() == parentMessage.getId()) {
                // Thread message
                if (!messageList.isEmpty()) {
                    messageList.set(0, parentMessage);
                    parentMessageListLiveData.setValue(messageList);
                }
            }
            if (baseMessage.getId() == parentMessage.getId()){
                if (!messageList.isEmpty()) {
                    messageList.set(0, baseMessage);
                    parentMessageListLiveData.setValue(messageList);
                }
            }
            receiveMessage.setValue(baseMessage);
        }
    }

    public void updateReceipt(MessageReceipt messageReceipt, boolean isRead) {
        if (parentMessage != null && messageReceipt != null && messageReceipt.getMessageId() == parentMessage.getId()) {
            if (isRead) {
                parentMessage.setReadAt(messageReceipt.getReadAt());
            } else {
                parentMessage.setDeliveredAt(messageReceipt.getDeliveredAt());
            }
            updateParentMessage(parentMessage);
        }
    }

    private void updateReactions(ReactionEvent reactionEvent, boolean isAdded) {
        if (!hideReaction && reactionEvent.getParentMessageId() == parentMessage.getId()) {
            updateParentMessage(CometChatHelper.updateMessageWithReactionInfo(parentMessage,
                                                                              reactionEvent.getReaction(),
                                                                              isAdded ? CometChatConstants.REACTION_ADDED : CometChatConstants.REACTION_REMOVED));
        }
    }

    public void removeListener() {
        CometChatMessageEvents.removeListener(LISTENER_ID);
    }

    public void setHideReaction(boolean hideReaction) {
        this.hideReaction = hideReaction;
    }

}
