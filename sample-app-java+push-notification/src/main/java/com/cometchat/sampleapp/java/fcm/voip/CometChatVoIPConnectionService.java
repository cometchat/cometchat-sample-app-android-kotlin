package com.cometchat.sampleapp.java.fcm.voip;

import android.os.Bundle;
import android.os.Handler;
import android.telecom.Connection;
import android.telecom.ConnectionRequest;
import android.telecom.ConnectionService;
import android.telecom.DisconnectCause;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;

public class CometChatVoIPConnectionService extends ConnectionService {
    private static final int CALL_TIMEOUT = 40000; // 40 seconds
    private Handler timeoutHandler;
    private Runnable timeoutRunnable;

    @Override
    public Connection onCreateIncomingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        CometChatVoIPUtils.isCallOngoing = true;
        Bundle extras = request.getExtras();
        String callerName = extras.getString("callerName", "Unknown Caller");
        String callerAvatar = extras.getString("callerAvatar", "");
        CometChatVoIPConnection connection = new CometChatVoIPConnection(this);
        connection.setExtras(extras);
        connection.setCallerDisplayName(callerName, TelecomManager.PRESENTATION_ALLOWED);
        connection.setAddress(request.getAddress(), TelecomManager.PRESENTATION_ALLOWED);
        connection.setRinging();

        // Initialize handler and timeout runnable
        timeoutHandler = new Handler();
        timeoutRunnable = () -> {
            if (connection.getState() == Connection.STATE_RINGING) {
                connection.setDisconnected(new DisconnectCause(DisconnectCause.REJECTED));
                connection.destroy();
            }
        };
        timeoutHandler.postDelayed(timeoutRunnable, CALL_TIMEOUT);

        return connection;
    }

    @Override
    public Connection onCreateOutgoingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        return new CometChatVoIPConnection(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timeoutHandler != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
        }
        CometChatVoIPUtils.isCallOngoing = false;
    }


}