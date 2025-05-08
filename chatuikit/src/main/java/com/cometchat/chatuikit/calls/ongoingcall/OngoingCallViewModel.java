package com.cometchat.chatuikit.calls.ongoingcall;

import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cometchat.calls.core.CallSettings;
import com.cometchat.calls.core.CometChatCalls;
import com.cometchat.calls.listeners.CometChatCallsEventsListener;
import com.cometchat.calls.model.AudioMode;
import com.cometchat.calls.model.CallSwitchRequestInfo;
import com.cometchat.calls.model.GenerateToken;
import com.cometchat.calls.model.RTCMutedUser;
import com.cometchat.calls.model.RTCRecordingInfo;
import com.cometchat.calls.model.RTCUser;
import com.cometchat.chat.constants.CometChatConstants;
import com.cometchat.chat.core.Call;
import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chatuikit.calls.CallingExtension;
import com.cometchat.chatuikit.logger.CometChatLogger;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKitHelper;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;

import java.util.ArrayList;

public class OngoingCallViewModel extends ViewModel {
    private static final String TAG = OngoingCallViewModel.class.getSimpleName();
    private final MutableLiveData<Boolean> hideProgressBar;
    private final MutableLiveData<Boolean> endCall;
    private final MutableLiveData<CometChatException> exception;
    private final MutableLiveData<Boolean> isJoined;
    private String LISTENER_ID;
    private String sessionId;
    private String callType;
    private CometChatCalls.CallSettingsBuilder callSettingsBuilder;
    private UIKitConstants.CallWorkFlow callWorkFlow = UIKitConstants.CallWorkFlow.DEFAULT;
    private CallSettings callSettings;

    public OngoingCallViewModel() {
        endCall = new MutableLiveData<>();
        exception = new MutableLiveData<>();
        hideProgressBar = new MutableLiveData<>();
        isJoined = new MutableLiveData<>();

    }

    public MutableLiveData<Boolean> getEndCall() {
        return endCall;
    }

    public MutableLiveData<CometChatException> getException() {
        return exception;
    }

    public MutableLiveData<Boolean> hideProgressBar() {
        return hideProgressBar;
    }

    public MutableLiveData<Boolean> isJoined() {
        return isJoined;
    }

    public void addListener() {
        LISTENER_ID = System.currentTimeMillis() + "";
        CometChatCalls.addCallsEventListeners(LISTENER_ID, new CometChatCallsEventsListener() {
            @Override
            public void onCallEnded() {
                if (!callWorkFlow.equals(UIKitConstants.CallWorkFlow.MEETING)) {
                    CometChatCalls.endSession();
                    CometChat.clearActiveCall();
                    exitScreen();
                }
            }

            @Override
            public void onCallEndButtonPressed() {
                if (!callWorkFlow.equals(UIKitConstants.CallWorkFlow.MEETING))
                    endCall();
                else {
                    CallingExtension.setIsActiveMeeting(false);
                    CometChatCalls.endSession();
                    exitScreen();
                }
            }

            @Override
            public void onSessionTimeout() {
                CallingExtension.setIsActiveMeeting(false);
                CometChatCalls.endSession();
                exitScreen();
            }

            @Override
            public void onUserJoined(RTCUser rtcUser) {
                if (rtcUser.getUid().equals(CometChat.getLoggedInUser().getUid())) {
                    isJoined.postValue(true);
                }
            }

            @Override
            public void onUserLeft(RTCUser rtcUser) {
            }

            @Override
            public void onUserListChanged(ArrayList<RTCUser> arrayList) {
            }

            @Override
            public void onAudioModeChanged(ArrayList<AudioMode> arrayList) {
            }

            @Override
            public void onCallSwitchedToVideo(CallSwitchRequestInfo callSwitchRequestInfo) {
            }

            @Override
            public void onUserMuted(RTCMutedUser rtcMutedUser) {
            }

            @Override
            public void onRecordingToggled(RTCRecordingInfo rtcRecordingInfo) {
            }

            @Override
            public void onError(com.cometchat.calls.exceptions.CometChatException e) {
                CometChatLogger.e(TAG, e.toString());
            }
        });
    }

    public void exitScreen() {
        endCall.postValue(Boolean.TRUE);
    }

    public void endCall() {
        CometChat.endCall(sessionId, new CometChat.CallbackListener<Call>() {
            @Override
            public void onSuccess(@Nullable Call call) {
                if (call != null) {
                    CometChatUIKitHelper.onCallEnded(call);
                }
                CometChatCalls.endSession();
                CometChat.clearActiveCall();
                exitScreen();
            }

            @Override
            public void onError(CometChatException e) {
                exitScreen();
                exception.setValue(e);
                CometChatLogger.e(TAG, e.toString());
            }
        });
    }

    public void removeListener() {
        CometChatCalls.removeCallsEventListeners(LISTENER_ID);
    }

    public void setCallWorkFlow(UIKitConstants.CallWorkFlow callWorkFlow) {
        this.callWorkFlow = callWorkFlow;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setCallSettingsBuilder(CometChatCalls.CallSettingsBuilder builder) {
        if (builder != null) {
            this.callSettingsBuilder = builder;
        }
    }

    public void startCall(RelativeLayout callingViewContainer) {
        String userAuthToken = CometChat.getUserAuthToken();
        hideProgressBar.setValue(false);
        callSettings = callSettingsBuilder.setIsAudioOnly(callType.equalsIgnoreCase(CometChatConstants.CALL_TYPE_AUDIO)).build();

        CometChatCalls.generateToken(sessionId, userAuthToken, new CometChatCalls.CallbackListener<GenerateToken>() {
            @Override
            public void onSuccess(GenerateToken generateToken) {
                startCall(generateToken.getToken(), callingViewContainer);
            }

            @Override
            public void onError(com.cometchat.calls.exceptions.CometChatException e) {
                CometChatLogger.e(TAG, e.toString());
            }
        });
    }

    private void startCall(String token, RelativeLayout callingViewContainer) {
        CometChatCalls.startSession(token, callSettings, callingViewContainer, new CometChatCalls.CallbackListener<String>() {
            @Override
            public void onSuccess(String s) {
                hideProgressBar.setValue(true);
                if (callWorkFlow.equals(UIKitConstants.CallWorkFlow.MEETING)) {
                    CallingExtension.setIsActiveMeeting(true);
                }
            }

            @Override
            public void onError(com.cometchat.calls.exceptions.CometChatException e) {
                CometChatLogger.e(TAG, e.toString());
            }
        });
    }

}
