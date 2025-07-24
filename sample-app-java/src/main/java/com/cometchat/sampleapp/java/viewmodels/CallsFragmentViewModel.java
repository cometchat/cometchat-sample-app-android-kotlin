package com.cometchat.sampleapp.java.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cometchat.calls.model.CallLog;
import com.cometchat.calls.model.CallUser;
import com.cometchat.chat.constants.CometChatConstants;
import com.cometchat.chat.core.Call;
import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.calls.utils.CallUtils;
import com.cometchat.sampleapp.java.data.repository.Repository;

public class CallsFragmentViewModel extends ViewModel {
    private final MutableLiveData<Call> onCallStart = new MutableLiveData<>();
    private final MutableLiveData<CometChatException> onError = new MutableLiveData<>();

    public MutableLiveData<Call> onCallStart() {
        return onCallStart;
    }

    public MutableLiveData<CometChatException> onError() {
        return onError;
    }

    public void startCall(String callType, CallLog callLog, CometChat.CallbackListener<Void> listener) {
        CallUser initiator = (CallUser) callLog.getInitiator();
        boolean isLoggedInUser = CallUtils.isLoggedInUser(initiator);
        CallUser user;
        if (isLoggedInUser) {
            user = (CallUser) callLog.getReceiver();
        } else {
            user = initiator;
        }
        Repository.getUser(user.getUid(), new CometChat.CallbackListener<User>() {
            @Override
            public void onSuccess(@NonNull User userObj) {
                if (listener != null) {
                    if (userObj.isBlockedByMe()) {
                        listener.onError(new CometChatException("BLOCKED_BY_ME", "Call cannot be initiated as user is blocked"));
                        onError.setValue(new CometChatException("BLOCKED_BY_ME", "Call cannot be initiated as user is blocked"));
                    } else if (userObj.isHasBlockedMe()) {
                        listener.onError(new CometChatException("BLOCKED_ME", "Call cannot be initiated as user has blocked you"));
                        onError.setValue(new CometChatException("BLOCKED_ME", "Call cannot be initiated as user has blocked you"));
                    } else {
                        startCall(callType, userObj, listener);
                    }
                }
            }

            @Override
            public void onError(CometChatException e) {
                if (listener != null) {
                    listener.onError(e);
                }
                onError.setValue(e);
            }
        });
    }

    private void startCall(String callType, User user, CometChat.CallbackListener<Void> listener) {
        Call call = new Call(user.getUid(), CometChatConstants.RECEIVER_TYPE_USER, callType);
        Repository.initiateCall(call, new CometChat.CallbackListener<Call>() {
            @Override
            public void onSuccess(Call call) {
                if (listener != null) {
                    listener.onSuccess(null);
                }
                onCallStart.setValue(call);
            }

            @Override
            public void onError(CometChatException e) {
                if (listener != null) {
                    listener.onError(e);
                }
                onError.setValue(e);
            }
        });
    }
}
