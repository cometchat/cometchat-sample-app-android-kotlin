package com.cometchat.sampleapp.java.fcm.voip;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telecom.Connection;
import android.telecom.DisconnectCause;
import android.util.Log;

import com.cometchat.calls.core.CometChatCalls;
import com.cometchat.calls.listeners.CometChatCallsEventsListener;
import com.cometchat.calls.model.AudioMode;
import com.cometchat.calls.model.CallSwitchRequestInfo;
import com.cometchat.calls.model.RTCMutedUser;
import com.cometchat.calls.model.RTCRecordingInfo;
import com.cometchat.calls.model.RTCUser;
import com.cometchat.chat.core.Call;
import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chatuikit.calls.CometChatOngoingCallActivity;
import com.cometchat.chatuikit.logger.CometChatLogger;
import com.cometchat.sampleapp.java.fcm.R;
import com.cometchat.sampleapp.java.fcm.data.repository.Repository;
import com.cometchat.sampleapp.java.fcm.utils.MyApplication;

import java.util.ArrayList;

public class CometChatVoIPConnection extends Connection implements CometChatCallsEventsListener {
    private static final String TAG = CometChatVoIPConnection.class.getSimpleName();

    private final Context context;

    // Constructor to pass context
    public CometChatVoIPConnection(Context context) {
        this.context = context;
        CometChatCalls.addCallsEventListeners(TAG, this);
    }

    @Override
    public void onAbort() {
        super.onAbort();
        CometChatLogger.e(TAG, "onAbort: ");
    }

    @Override
    public void onAnswer() {
        super.onAnswer();
        setDisconnected(new DisconnectCause(DisconnectCause.CANCELED, "Accepted"));

        Bundle extras = getExtras();
        String receiverUid = extras.getString("receiverUid");
        String receiverType = extras.getString("receiverType");
        String sessionId = extras.getString("sessionId");
        String callType = extras.getString("callType");

        if (receiverUid == null || receiverType == null || sessionId == null || callType == null) {
            Log.e(TAG, "onAnswer: receiverUid, receiverType, sessionId, or callType is null");
            return;
        }

        // Mark the connection as active
        //setActive();
        Call call = new Call(receiverUid, receiverType, callType);
        call.setSessionId(sessionId);

        Repository.acceptCall(call, new CometChat.CallbackListener<Call>() {
            @Override
            public void onSuccess(Call call) {
                Intent intent = new Intent(context, CometChatOngoingCallActivity.class);
                CometChatVoIPUtils.isCallOngoing = true;
                intent.putExtra(context.getString(R.string.app_session_id), call.getSessionId());
                intent.putExtra(context.getString(R.string.app_call_type), call.getType());
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }

            @Override
            public void onError(CometChatException e) {
                CometChatVoIPUtils.isCallOngoing = false;
            }
        });
    }

    @Override
    public void onReject() {
        super.onReject();
        Bundle extras = getExtras();
        String receiverUid = extras.getString("receiverUid");
        String receiverType = extras.getString("receiverType");
        String sessionId = extras.getString("sessionId");
        String callType = extras.getString("callType");

        if (receiverUid == null || receiverType == null || sessionId == null || callType == null) {
            CometChatLogger.e(TAG, "onReject: receiverUid, receiverType, sessionId, or callType is null");
            return;
        }
        Call call = new Call(receiverUid, receiverType, callType);
        call.setSessionId(sessionId);
        Repository.rejectCall(call, new CometChat.CallbackListener<Call>() {
            @Override
            public void onSuccess(Call call) {
            }

            @Override
            public void onError(CometChatException e) {
            }
        });
        setDisconnected(new DisconnectCause(DisconnectCause.REJECTED, "Rejected"));
        CometChatVoIPUtils.isCallOngoing = false;
    }

    private void killAppAndClearTask() {
        try {
            Activity activity = MyApplication.getCurrentActivity();
            if (activity != null) {
                activity.finishAffinity();
            }
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        } catch (Exception e) {
            CometChatLogger.e("KillApp", "Error while clearing task or killing app: " + e.getMessage());
        }
    }

    @Override
    public void onCallEnded() {
        setDisconnected(new DisconnectCause(DisconnectCause.CANCELED, "Canceled"));
        CometChatVoIPUtils.isCallOngoing = false;
    }

    @Override
    public void onCallEndButtonPressed() {
        setDisconnected(new DisconnectCause(DisconnectCause.CANCELED, "Canceled"));
        CometChatVoIPUtils.isCallOngoing = false;
    }

    @Override
    public void onSessionTimeout() {
        setDisconnected(new DisconnectCause(DisconnectCause.CANCELED, "Session Timeout"));
        CometChatVoIPUtils.isCallOngoing = false;
    }

    @Override
    public void onUserJoined(RTCUser rtcUser) {

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

    }
}
