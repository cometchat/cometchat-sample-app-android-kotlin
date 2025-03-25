package com.cometchat.sampleapp.java.fcm.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cometchat.chat.constants.CometChatConstants;
import com.cometchat.chat.core.Call;
import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.User;
import com.cometchat.sampleapp.java.fcm.data.repository.Repository;
import com.cometchat.sampleapp.java.fcm.utils.AppConstants;

import java.util.HashMap;

public class UserDetailsViewModel extends ViewModel {
    private final String USER_LISTENER_ID = UserDetailsViewModel.class.getSimpleName();

    private final MutableLiveData<User> user = new MutableLiveData<>();
    private final MutableLiveData<BaseMessage> baseMessage = new MutableLiveData<>();
    private final MutableLiveData<Call> onCallStart = new MutableLiveData<>();
    private final MutableLiveData<String> onStarCallError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isUserBlockedByMe = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isUserBlocked = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isUserUnblocked = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isChatDeleted = new MutableLiveData<>();

    public MutableLiveData<User> getUser() {
        return user;
    }

    public void setUser(User mUser) {
        user.setValue(mUser);
        isUserBlockedByMe.setValue(mUser.isBlockedByMe());
        Repository.getUser(mUser.getUid(), new CometChat.CallbackListener<User>() {
            @Override
            public void onSuccess(@NonNull User userObj) {
                user.setValue(userObj);
                isUserBlockedByMe.setValue(userObj.isBlockedByMe());
            }

            @Override
            public void onError(CometChatException e) {
            }
        });
    }

    public MutableLiveData<BaseMessage> getBaseMessage() {
        return baseMessage;
    }

    public void setBaseMessage(BaseMessage message) {
        this.baseMessage.setValue(message);
    }

    public MutableLiveData<Boolean> isUserBlockedByMe() {
        return isUserBlockedByMe;
    }

    public MutableLiveData<Boolean> isUserBlocked() {
        return isUserBlocked;
    }

    public MutableLiveData<Boolean> isUserUnblocked() {
        return isUserUnblocked;
    }

    public MutableLiveData<Boolean> isChatDeleted() {
        return isChatDeleted;
    }

    public MutableLiveData<Call> onCallStart() {
        return onCallStart;
    }

    public MutableLiveData<String> onCallStartError() {
        return onStarCallError;
    }

    public void addListeners() {
        CometChat.addUserListener(USER_LISTENER_ID, new CometChat.UserListener() {
            @Override
            public void onUserOnline(User mUser) {
                if (user.getValue() != null && user.getValue().getUid().equals(mUser.getUid())) {
                    user.setValue(mUser);
                }
            }

            @Override
            public void onUserOffline(User mUser) {
                if (user.getValue() != null && user.getValue().getUid().equals(mUser.getUid())) {
                    user.setValue(mUser);
                }
            }
        });
    }

    public void removeListeners() {
        CometChat.removeUserListener(USER_LISTENER_ID);
    }

    public void blockUser() {
        if (user != null && user.getValue() != null) {
            Repository.blockUser(user.getValue(), new CometChat.CallbackListener<HashMap<String, String>>() {
                @Override
                public void onSuccess(HashMap<String, String> resultMap) {
                    if (resultMap != null && AppConstants.SuccessConstants.SUCCESS.equalsIgnoreCase(resultMap.get(user.getValue().getUid()))) {
                        isUserBlocked.setValue(true);
                        isUserBlockedByMe.setValue(true);
                        user.getValue().setBlockedByMe(true);
                        setUser(user.getValue());
                    } else {
                        isUserBlocked.setValue(false);
                    }
                }

                @Override
                public void onError(CometChatException e) {
                    isUserBlocked.setValue(false);
                }
            });
        } else {
            isUserBlocked.setValue(false);
        }
    }

    public void unblockUser() {
        if (user != null && user.getValue() != null) {
            Repository.unblockUser(user.getValue(), new CometChat.CallbackListener<HashMap<String, String>>() {
                @Override
                public void onSuccess(HashMap<String, String> resultMap) {
                    if (resultMap != null && AppConstants.SuccessConstants.SUCCESS.equalsIgnoreCase(resultMap.get(user.getValue().getUid()))) {
                        isUserUnblocked.setValue(true);
                        isUserBlockedByMe.setValue(false);
                        user.getValue().setBlockedByMe(false);
                        setUser(user.getValue());
                    } else {
                        isUserUnblocked.setValue(false);
                    }
                }

                @Override
                public void onError(CometChatException e) {
                    isUserUnblocked.setValue(false);
                }
            });
        } else {
            isUserUnblocked.setValue(false);
        }
    }

    public void deleteChat() {
        if (user.getValue() != null) {
            Repository.deleteChat(
                user.getValue().getUid(),
                baseMessage.getValue(),
                CometChatConstants.RECEIVER_TYPE_USER,
                new CometChat.CallbackListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        isChatDeleted.setValue(true);
                    }

                    @Override
                    public void onError(CometChatException e) {
                        e.getMessage();
                        isChatDeleted.setValue(false);
                    }
                }
            );
        } else {
            isChatDeleted.setValue(false);
        }
    }

    public void startCall(String callType) {
        if (user != null && user.getValue() != null) {
            Call call = new Call(user.getValue().getUid(), CometChatConstants.RECEIVER_TYPE_USER, callType);
            Repository.initiateCall(call, new CometChat.CallbackListener<Call>() {
                @Override
                public void onSuccess(Call call) {
                    onCallStart.setValue(call);
                }

                @Override
                public void onError(CometChatException e) {
                    onStarCallError.setValue("");
                }
            });
        } else {
            onStarCallError.setValue("User is null");
        }
    }
}
