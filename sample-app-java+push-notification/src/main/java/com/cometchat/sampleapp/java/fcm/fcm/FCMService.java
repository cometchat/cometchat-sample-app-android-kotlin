package com.cometchat.sampleapp.java.fcm.fcm;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telecom.PhoneAccount;
import android.telecom.TelecomManager;
import android.telecom.VideoProfile;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.cometchat.chat.constants.CometChatConstants;
import com.cometchat.chat.core.Call;
import com.cometchat.chat.core.CometChat;
import com.cometchat.chatuikit.logger.CometChatLogger;
import com.cometchat.sampleapp.java.fcm.data.repository.Repository;
import com.cometchat.sampleapp.java.fcm.ui.activity.SplashActivity;
import com.cometchat.sampleapp.java.fcm.utils.AppConstants;
import com.cometchat.sampleapp.java.fcm.utils.MyApplication;
import com.cometchat.sampleapp.java.fcm.voip.CometChatVoIP;
import com.cometchat.sampleapp.java.fcm.voip.CometChatVoIPUtils;
import com.cometchat.sampleapp.java.fcm.voip.interfaces.VoIPPermissionListener;
import com.cometchat.sampleapp.java.fcm.voip.model.CometChatVoIPError;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

public class FCMService extends FirebaseMessagingService {
    private static final String TAG = FCMService.class.getSimpleName();
    private static final String CALL_STATUS_INITIATED = "initiated";
    private static final String CALL_STATUS_CANCELLED = "cancelled";
    private static final String CALL_STATUS_UNANSWERED = "unanswered";
    private static String fcmToken;

    public static String getFCMToken() {
        return fcmToken;
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        CometChatLogger.e(TAG, "onMessageReceived:>>>>" + new Gson().toJson(message.getData()));
        if (message.getData().isEmpty()) {
            CometChatLogger.e(TAG, "onMessageReceived: No data payload in message.");
            return;
        }

        try {
            if (message.getData().containsKey("type")) {
                String type = message.getData().get("type");
                if ("chat".equalsIgnoreCase(type)) {
                    FCMMessageDTO fcmMessageDTO = new Gson().fromJson(new Gson().toJson(message.getData()), FCMMessageDTO.class);

                    CometChat.markAsDelivered(Integer.parseInt(fcmMessageDTO.getTag()),
                                              fcmMessageDTO.getSender(),
                                              fcmMessageDTO
                                                  .getReceiverType()
                                                  .equals(CometChatConstants.RECEIVER_TYPE_GROUP) ? CometChatConstants.RECEIVER_TYPE_GROUP : CometChatConstants.RECEIVER_TYPE_USER,
                                              fcmMessageDTO.getReceiver());

                    boolean isUser = fcmMessageDTO.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_USER);
                    String uid = isUser ? fcmMessageDTO.getSender() : fcmMessageDTO.getReceiver();
                    if (!uid.equals(MyApplication.currentOpenChatId)) {
                        CometChatLogger.e(TAG, "onMessageReceived: " + new Gson().toJson(fcmMessageDTO));
                        Intent onNotificationClickIntent = new Intent(this, SplashActivity.class);
                        FCMMessageNotificationUtils.showNotification(
                            this,
                            fcmMessageDTO,
                            onNotificationClickIntent,
                            AppConstants.FCMConstants.NOTIFICATION_KEY_REPLY_ACTION,
                            NotificationCompat.CATEGORY_MESSAGE
                        );
                    }
                } else if ("call".equalsIgnoreCase(type)) {
                    String sessionId = message.getData().get("sessionId");
                    String callAction = message.getData().get("callAction");
                    if (!MyApplication.isAppInForeground() &&
                        MyApplication.getTempCall() != null &&
                        MyApplication
                            .getTempCall()
                            .getSessionId()
                            .equals(sessionId) && (CometChatConstants.CALL_STATUS_CANCELLED.equals(callAction) || CometChatConstants.CALL_STATUS_UNANSWERED.equals(
                        callAction))) {
                        MyApplication.setTempCall(null);
                    }
                    if (!CometChatVoIP.hasReadPhoneStatePermission(this)) return;

                    if (!CometChatVoIP.hasManageOwnCallsPermission(this)) return;

                    if (!CometChatVoIP.hasAnswerPhoneCallsPermission(this)) return;

                    if (CometChatVoIP.hasReadPhoneStatePermission(this) && CometChatVoIP.hasManageOwnCallsPermission(this) && CometChatVoIP.hasAnswerPhoneCallsPermission(
                        this)) {
                        CometChatVoIP.hasEnabledPhoneAccountForVoIP(this, new VoIPPermissionListener() {
                            @Override
                            public void onPermissionsGranted() {
                                CometChatLogger.e(TAG, "VoIP Permissions granted");
                                handleCallFlow(message);
                            }

                            @Override
                            public void onPermissionsDenied(CometChatVoIPError error) {
                                CometChatLogger.e(TAG, "VoIP Permissions denied: " + error.getMessage());
                            }
                        });
                    } else {
                        CometChatLogger.e(TAG, "All VoIP Permissions denied.");
                    }
                } else {
                    CometChatLogger.e(TAG, "onMessageReceived: Unsupported message type: " + type);
                }
            }
        } catch (Exception e) {
            CometChatLogger.e(TAG, "onMessageReceived: Error processing message: " + e.getMessage());
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        fcmToken = token;
        CometChatLogger.e(TAG, "onNewToken: FCM Token: " + fcmToken);
    }

    private void handleCallFlow(RemoteMessage message) {
        Gson gson = new Gson();
        FCMCallDto callData = gson.fromJson(gson.toJson(message.getData()), FCMCallDto.class);
        configureVoIP(callData);

        // Check if the call action is "initiated"
        if (CALL_STATUS_INITIATED.equals(callData.getCallAction())) {
            showIncomingCallScreen(callData);
        } else if (CALL_STATUS_CANCELLED.equals(callData.getCallAction()) || CALL_STATUS_UNANSWERED.equals(callData.getCallAction())) {
            if (CometChatVoIPUtils.currentSessionId.equals(callData.getSessionId())) {
                if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ANSWER_PHONE_CALLS
                ) != PackageManager.PERMISSION_GRANTED) {
                    CometChatLogger.e(TAG, "Error: Permission ANSWER_PHONE_CALLS denied to end call.");
                    return;
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    CometChatLogger.e(TAG, "Error: " + callData.getCallAction());
                    CometChatVoIP.getTelecomManager().endCall();
                }
            }
        }
    }

    private void configureVoIP(FCMCallDto callData) {
        CometChatVoIP.init(this, getApplicationInfo().loadLabel(getPackageManager()).toString());
    }

    private void showIncomingCallScreen(FCMCallDto callData) {
        voipIncomingCall(callData);
    }

    private void voipIncomingCall(FCMCallDto callData) {
        if (MyApplication.isAppInForeground()) {
            CometChatLogger.e(TAG, "Call ignored as app is in the foreground and call popup is showing.");
            return;
        }
        if (CometChat.getActiveCall() != null || CometChatVoIPUtils.isCallOngoing) {
            @SuppressLint("WrongConstant") Call call = new Call(callData.getReceiver(), callData.getReceiverType(), callData.getCallType());
            call.setSessionId(callData.getSessionId());
            Repository.rejectCallWithBusyStatus(call, null);
        } else {
            Bundle extras = new Bundle();
            Uri uri = Uri.fromParts(PhoneAccount.SCHEME_TEL, "", null);
            extras.putInt(
                TelecomManager.EXTRA_INCOMING_VIDEO_STATE,
                callData.getCallType().equals("audio") ? VideoProfile.STATE_AUDIO_ONLY : VideoProfile.STATE_BIDIRECTIONAL
            );
            extras.putParcelable(TelecomManager.EXTRA_INCOMING_CALL_ADDRESS, uri);
            extras.putBoolean(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, false);
            extras.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, CometChatVoIP.getPhoneAccountHandle());

            extras.putString("callerName", callData.getSenderName());
            extras.putString("callerAvatar", callData.getSenderAvatar());
            extras.putString("receiverUid", callData.getReceiver());
            extras.putString("receiverType", callData.getReceiverType());
            extras.putString("sessionId", callData.getSessionId());
            extras.putString("callType", callData.getCallType());

            CometChatVoIPUtils.currentSessionId = callData.getSessionId();
            CometChatVoIP.addNewIncomingCall(this, extras);
        }
    }
}
