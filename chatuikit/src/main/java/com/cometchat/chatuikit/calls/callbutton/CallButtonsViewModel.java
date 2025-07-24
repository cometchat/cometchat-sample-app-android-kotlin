package com.cometchat.chatuikit.calls.callbutton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cometchat.chat.constants.CometChatConstants;
import com.cometchat.chat.core.Call;
import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.CustomMessage;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.calls.CallingExtension;
import com.cometchat.chatuikit.logger.CometChatLogger;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKitHelper;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.events.CometChatCallEvents;
import com.cometchat.chatuikit.shared.resources.utils.Utils;

import org.json.JSONObject;

public class CallButtonsViewModel extends ViewModel {
    private static final String TAG = CallButtonsViewModel.class.getSimpleName();
    private final MutableLiveData<BaseMessage> startDirectCall;
    private final MutableLiveData<Call> callInitiated;
    private final MutableLiveData<Call> callRejected;
    private final MutableLiveData<CometChatException> error;
    private String LISTENER_ID;
    private String receiverType;
    private String receiverId;

    public CallButtonsViewModel() {
        startDirectCall = new MutableLiveData<>();
        callInitiated = new MutableLiveData<>();
        callRejected = new MutableLiveData<>();
        error = new MutableLiveData<>();
    }

    public MutableLiveData<Call> getCallInitiated() {
        return callInitiated;
    }

    public MutableLiveData<CometChatException> getError() {
        return error;
    }

    public MutableLiveData<BaseMessage> getStartDirectCall() {
        return startDirectCall;
    }

    public MutableLiveData<Call> getCallRejected() {
        return callRejected;
    }

    public void setUser(User user) {
        if (user != null) {
            receiverType = UIKitConstants.ReceiverType.USER;
            receiverId = user.getUid();
        }
    }

    public void setGroup(Group group) {
        if (group != null) {
            receiverType = UIKitConstants.ReceiverType.GROUP;
            receiverId = group.getGuid();
        }
    }

    public void addListener() {
        LISTENER_ID = System.currentTimeMillis() + "";
        CometChatCallEvents.addListener(LISTENER_ID, new CometChatCallEvents() {
            @Override
            public void ccCallRejected(Call call) {
                callRejected.setValue(call);
            }

            @Override
            public void ccCallEnded(Call call) {
                try {
                    callRejected.postValue(call);
                } catch (Exception e) {
                    CometChatLogger.e(TAG, e.getMessage());
                }
            }
        });

        CometChat.addCallListener(LISTENER_ID, new CometChat.CallListener() {
            @Override
            public void onIncomingCallReceived(Call call) {
            }

            @Override
            public void onOutgoingCallAccepted(Call call) {
            }

            @Override
            public void onOutgoingCallRejected(Call call) {
                callRejected.setValue(call);
            }

            @Override
            public void onIncomingCallCancelled(Call call) {
                callRejected.setValue(call);
            }

            @Override
            public void onCallEndedMessageReceived(Call call) {
                callRejected.setValue(call);
            }
        });
    }

    public void removeListener() {
        CometChatCallEvents.removeListener(LISTENER_ID);
        CometChat.removeCallListener(LISTENER_ID);
    }

    public void initiateCall(String callType) {
        if (CometChat.getActiveCall() == null && CallingExtension.getActiveCall() == null && !CallingExtension.isActiveMeeting()) {
            if (receiverType.equalsIgnoreCase(CometChatConstants.RECEIVER_TYPE_GROUP)) {
                CustomMessage customMessage = getCustomMessage(callType);
                JSONObject jsonObject = getJsonObject(customMessage);
                customMessage.setMetadata(jsonObject);
                customMessage.shouldUpdateConversation(true);
                CometChatUIKit.sendCustomMessage(customMessage, new CometChat.CallbackListener<CustomMessage>() {
                    @Override
                    public void onSuccess(CustomMessage customMessage) {
                        startDirectCall.setValue(customMessage);
                    }

                    @Override
                    public void onError(CometChatException e) {
                        customMessage.setMetadata(Utils.placeErrorObjectInMetaData(e));
                    }
                });
            } else {
                Call call = new Call(receiverId, CometChatConstants.RECEIVER_TYPE_USER, callType);
                CometChat.initiateCall(call, new CometChat.CallbackListener<Call>() {
                    @Override
                    public void onSuccess(Call call) {
                        callInitiated.setValue(call);
                        CometChatUIKitHelper.onOutgoingCall(call);
                    }

                    @Override
                    public void onError(CometChatException e) {
                        error.setValue(e);
                    }
                });
            }
        }
    }

    @NonNull
    private CustomMessage getCustomMessage(String callType) {
        JSONObject customData = new JSONObject();
        try {
            customData.put(UIKitConstants.CallingJSONConstants.CALL_TYPE, callType);
            customData.put(UIKitConstants.CallingJSONConstants.CALL_SESSION_ID, receiverId);
        } catch (Exception exception) {
            CometChatLogger.e(TAG, exception.getMessage());
        }
        return new CustomMessage(receiverId,
                                 CometChatConstants.RECEIVER_TYPE_GROUP,
                                 UIKitConstants.MessageType.MEETING,
                                 customData
        );
    }

    @Nullable
    private static JSONObject getJsonObject(CustomMessage customMessage) {
        JSONObject jsonObject = null;
        try {
            jsonObject = customMessage.getMetadata();
            if (jsonObject == null) {
                jsonObject = new JSONObject();
                jsonObject.put("incrementUnreadCount", true);
                jsonObject.put("pushNotification", UIKitConstants.MessageType.MEETING);
            } else {
                jsonObject.accumulate("incrementUnreadCount", true);
            }
        } catch (Exception ignored) {

        }
        return jsonObject;
    }
}
