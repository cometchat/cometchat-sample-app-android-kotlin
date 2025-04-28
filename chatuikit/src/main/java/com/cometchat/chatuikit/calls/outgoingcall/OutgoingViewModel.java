package com.cometchat.chatuikit.calls.outgoingcall;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cometchat.chat.constants.CometChatConstants;
import com.cometchat.chat.core.Call;
import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKitHelper;

public class OutgoingViewModel extends ViewModel {
    private static final String TAG = OutgoingViewModel.class.getSimpleName();
    private final MutableLiveData<Call> rejectCall;
    private final MutableLiveData<Call> acceptedCall;
    private final MutableLiveData<CometChatException> exception;
    private final MutableLiveData<Boolean> disableEndCallButton;
    private String LISTENER_ID;

    public OutgoingViewModel() {
        rejectCall = new MutableLiveData<>();
        acceptedCall = new MutableLiveData<>();
        exception = new MutableLiveData<>();
        disableEndCallButton = new MutableLiveData<>();
    }

    public MutableLiveData<Call> getRejectCall() {
        return rejectCall;
    }

    public MutableLiveData<Call> getAcceptedCall() {
        return acceptedCall;
    }

    public MutableLiveData<CometChatException> getException() {
        return exception;
    }

    public MutableLiveData<Boolean> getDisableEndCallButton() {
        return disableEndCallButton;
    }

    public void rejectCall(Call call) {
        CometChat.rejectCall(call.getSessionId(), CometChatConstants.CALL_STATUS_CANCELLED, new CometChat.CallbackListener<Call>() {
            @Override
            public void onSuccess(Call call) {
                CometChatUIKitHelper.onCallRejected(call);
                rejectCall.setValue(call);
            }

            @Override
            public void onError(CometChatException e) {
                CometChatUIKitHelper.onCallRejected(call);

                if (CometChat.getActiveCall() != null) {
                    CometChat.clearActiveCall();
                }

                exception.setValue(e);
            }
        });
    }

    public void addListeners() {
        LISTENER_ID = System.currentTimeMillis() + "";
        CometChat.addCallListener(LISTENER_ID, new CometChat.CallListener() {
            @Override
            public void onIncomingCallReceived(Call call) {
            }

            @Override
            public void onOutgoingCallAccepted(Call call) {
                acceptedCall.setValue(call);
                disableEndCallButton.setValue(true);
            }

            @Override
            public void onOutgoingCallRejected(Call call) {
                rejectCall.setValue(call);
            }

            @Override
            public void onIncomingCallCancelled(Call call) {
            }
        });
    }

    public void removeListeners() {
        CometChat.removeCallListener(LISTENER_ID);
    }
}
