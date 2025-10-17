package com.cometchat.sampleapp.java.fcm.viewmodels;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.models.Action;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.Conversation;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.constants.MessageStatus;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.events.CometChatConversationEvents;
import com.cometchat.chatuikit.shared.events.CometChatGroupEvents;
import com.cometchat.chatuikit.shared.events.CometChatMessageEvents;
import com.cometchat.chatuikit.shared.events.CometChatUIEvents;
import com.cometchat.chatuikit.shared.events.CometChatUserEvents;
import com.cometchat.sampleapp.java.fcm.data.repository.Repository;
import com.cometchat.sampleapp.java.fcm.utils.AppConstants;

import java.util.HashMap;

/**
 * ViewModel for managing the state and data of messages in a group chat.
 */
public class MessagesViewModel extends ViewModel {
    @NonNull
    private final String LISTENER_ID;
    @NonNull
    private final MutableLiveData<BaseMessage> baseMessage;
    private final MutableLiveData<BaseMessage> sentMessage;
    private final MutableLiveData<Group> updatedGroup;
    private final MutableLiveData<User> updateUser;
    private final MutableLiveData<User> openUserChat;
    private final MutableLiveData<Boolean> isExitActivity;
    private final MutableLiveData<UIKitConstants.DialogState> unblockButtonState;
    private User mUser;
    private Group mGroup;

    /**
     * Initializes the MessagesViewModel with LiveData objects.
     */
    public MessagesViewModel() {
        updatedGroup = new MutableLiveData<>();
        updateUser = new MutableLiveData<>();
        openUserChat = new MutableLiveData<>();
        isExitActivity = new MutableLiveData<>();
        unblockButtonState = new MutableLiveData<>();
        baseMessage = new MutableLiveData<>();
        LISTENER_ID = System.currentTimeMillis() + this.getClass().getSimpleName();
        sentMessage = new MutableLiveData<>();
    }

    /**
     * Gets the LiveData for sent messages.
     *
     * @return MutableLiveData object containing sent BaseMessage data.
     */
    public MutableLiveData<BaseMessage> getSentMessage() {
        return sentMessage;
    }

    /**
     * Gets the LiveData for updated group information.
     *
     * @return MutableLiveData object containing updated Group data.
     */
    public MutableLiveData<Group> getUpdatedGroup() {
        return updatedGroup;
    }

    /**
     * Gets the LiveData for updated user information.
     *
     * @return MutableLiveData object containing updated User data.
     */
    public MutableLiveData<User> getUpdateUser() {
        return updateUser;
    }

    /**
     * Gets the LiveData for opening a user chat.
     *
     * @return MutableLiveData object containing the User data for the chat to be opened.
     */
    public MutableLiveData<User> openUserChat() {
        return openUserChat;
    }

    @NonNull
    public MutableLiveData<BaseMessage> getBaseMessage() {
        return baseMessage;
    }

    /**
     * Gets the LiveData that indicates whether to exit the activity.
     *
     * @return MutableLiveData object containing a boolean indicating exit state.
     */
    public MutableLiveData<Boolean> getIsExitActivity() {
        return isExitActivity;
    }

    /**
     * Gets the LiveData for the state of the unblock button.
     *
     * @return MutableLiveData object containing the DialogState for unblock
     * operation.
     */
    public MutableLiveData<UIKitConstants.DialogState> getUnblockButtonState() {
        return unblockButtonState;
    }

    /**
     * Sets the group associated with this ViewModel.
     *
     * @param group The Group to be set.
     */
    public void setGroup(Group group) {
        mGroup = group;
    }

    /**
     * Sets the user associated with this ViewModel.
     *
     * @param user The User to be set.
     */
    public void setUser(User user) {
        mUser = user;
    }

    /**
     * Adds listeners for group and user events.
     */
    public void addListener() {
        CometChatMessageEvents.addListener(LISTENER_ID, new CometChatMessageEvents() {
            @Override
            public void ccMessageSent(BaseMessage baseMessage, int status) {
                if(baseMessage != null && status == MessageStatus.IN_PROGRESS) {
                    sentMessage.setValue(baseMessage);
                }
            }
        });
        CometChat.addGroupListener(LISTENER_ID, new CometChat.GroupListener() {
            @Override
            public void onGroupMemberJoined(Action action, User user, Group group) {
                updateGroupJoinedStatus(group, user, true);
            }

            @Override
            public void onGroupMemberLeft(Action action, User user, Group group) {
                updateGroupJoinedStatus(group, user, false);
            }

            @Override
            public void onGroupMemberKicked(Action action, User user, User user1, Group group) {
                updateGroupJoinedStatus(group, user, false);
            }

            @Override
            public void onGroupMemberBanned(Action action, User user, User user1, Group group) {
                updateGroupJoinedStatus(group, user, false);
            }

            @Override
            public void onMemberAddedToGroup(Action action, User addedBy, User userAdded, Group addedTo) {
                updateGroupJoinedStatus(addedTo, userAdded, true);
            }
        });

        CometChatGroupEvents.addGroupListener(LISTENER_ID, new CometChatGroupEvents() {
            @Override
            public void ccGroupDeleted(Group group) {
                isExitActivity.setValue(true);
            }

            @Override
            public void ccGroupLeft(Action actionMessage, User leftUser, Group leftGroup) {
                isExitActivity.setValue(true);
            }
        });

        CometChatUserEvents.addUserListener(LISTENER_ID, new CometChatUserEvents() {
            @Override
            public void ccUserBlocked(User user) {
                updateUser.setValue(user);
            }

            @Override
            public void ccUserUnblocked(User user) {
                updateUser.setValue(user);
            }
        });

        CometChatUIEvents.addListener(LISTENER_ID, new CometChatUIEvents() {
            @Override
            public void ccActiveChatChanged(HashMap<String, String> id, BaseMessage message, User user, Group group) {
                baseMessage.setValue(message);
            }

            @Override
            public void ccOpenChat(User user, Group group) {
                openUserChat.setValue(user);
            }
        });

        CometChatConversationEvents.addListener(LISTENER_ID, new CometChatConversationEvents() {
            @Override
            public void ccConversationDeleted(@NonNull Conversation conversation) {
                isExitActivity.setValue(true);
            }
        });
    }

    /**
     * Updates the joined status of the group based on user actions.
     *
     * @param group The group to update.
     * @param user  The user whose action triggered the update.
     */
    private void updateGroupJoinedStatus(@Nullable Group group, User user, boolean isJoined) {
        if (group != null && mGroup != null && group.getGuid().equals(mGroup.getGuid())) { // Add null check for group
            if (user.getUid().equals(CometChatUIKit.getLoggedInUser().getUid())) {
                group.setHasJoined(isJoined);
                updatedGroup.setValue(group);
            }
        }
    }

    /**
     * Removes listeners for group and user events.
     */
    public void removeListener() {
        CometChat.removeGroupListener(LISTENER_ID);
        CometChatGroupEvents.removeListener(LISTENER_ID);
        CometChatUserEvents.removeListener(LISTENER_ID);
        CometChatUIEvents.removeListener(LISTENER_ID);
    }

    /**
     * Unblocks a user and updates the unblock button state.
     */
    public void unblockUser() {
        if (mUser == null) return;
        unblockButtonState.setValue(UIKitConstants.DialogState.INITIATED);
        Repository.unblockUser(mUser, new CometChat.CallbackListener<HashMap<String, String>>() {
            @Override
            public void onSuccess(HashMap<String, String> resultMap) {
                if (resultMap != null && AppConstants.SuccessConstants.SUCCESS.equalsIgnoreCase(resultMap.get(mUser.getUid()))) {
                    unblockButtonState.setValue(UIKitConstants.DialogState.SUCCESS);
                } else {
                    unblockButtonState.setValue(UIKitConstants.DialogState.FAILURE);
                }
            }

            @Override
            public void onError(CometChatException e) {
                unblockButtonState.setValue(UIKitConstants.DialogState.FAILURE);
            }
        });
    }
}
