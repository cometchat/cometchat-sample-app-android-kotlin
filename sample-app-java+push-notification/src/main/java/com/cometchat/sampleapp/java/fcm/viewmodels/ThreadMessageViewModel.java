package com.cometchat.sampleapp.java.fcm.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.events.CometChatUserEvents;
import com.cometchat.sampleapp.java.fcm.data.repository.Repository;
import com.cometchat.sampleapp.java.fcm.utils.AppConstants;

import java.util.HashMap;

public class ThreadMessageViewModel extends ViewModel {

    private final MutableLiveData<BaseMessage> parentMessage;
    private final String LISTENER_ID;
    private final MutableLiveData<User> updateUser;
    private final MutableLiveData<UIKitConstants.DialogState> unblockButtonState;
    private int id;
    private User mUser;

    public ThreadMessageViewModel() {
        parentMessage = new MutableLiveData<>();
        updateUser = new MutableLiveData<>();
        unblockButtonState = new MutableLiveData<>();
        LISTENER_ID = System.currentTimeMillis() + this.getClass().getSimpleName();
    }

    public int getId() {
        return id;
    }

    public MutableLiveData<BaseMessage> getParentMessage() {
        return parentMessage;
    }

    public void fetchMessageDetails(int id) {
        this.id = id;
        Repository.fetchMessageInformation(id, new CometChat.CallbackListener<BaseMessage>() {
            @Override
            public void onSuccess(BaseMessage message) {
                parentMessage.setValue(message);
            }

            @Override
            public void onError(CometChatException e) {
            }
        });
    }

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

    public void addUserListener() {
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
    }

    public void setUser(User user) {
        mUser = user;
    }

    public LiveData<User> getUserBlockStatus() {
        return updateUser;
    }

    public MutableLiveData<UIKitConstants.DialogState> getUnblockButtonState() {
        return unblockButtonState;
    }
}
