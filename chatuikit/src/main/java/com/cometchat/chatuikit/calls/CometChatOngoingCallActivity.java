package com.cometchat.chatuikit.calls;

import android.app.PictureInPictureParams;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Rational;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.cometchat.calls.core.CometChatCalls;
import com.cometchat.chat.core.CometChat;
import com.cometchat.chatuikit.databinding.CometchatOngoingCallActivityBinding;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;

/**
 * This activity handles ongoing calls, setting up the UI and managing the call
 * session. It retrieves call details from the intent and initializes the call
 * interface.
 */
public class CometChatOngoingCallActivity extends AppCompatActivity {

    private static final String TAG = CometChatOngoingCallActivity.class.getSimpleName();
    private static final String SESSION_ID = "sessionId";
    private static final String CALL_TYPE = "callType";
    private static CometChatCalls.CallSettingsBuilder onGoingCallSettingsBuilder;

    public static void launchOngoingCallActivity(Context context,
                                                 String sessionId,
                                                 String callType,
                                                 CometChatCalls.CallSettingsBuilder onGoingCallSettingsBuilder) {
        Intent intent = new Intent(context, CometChatOngoingCallActivity.class);
        intent.putExtra(SESSION_ID, sessionId);
        intent.putExtra(CALL_TYPE, callType);
        CometChatOngoingCallActivity.onGoingCallSettingsBuilder = onGoingCallSettingsBuilder;
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * Called when the activity is starting. It initializes the UI and starts the
     * call.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     *                           shut down, this Bundle contains the data it most recently supplied
     *                           in onSaveInstanceState.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate the layout using View Binding
        CometchatOngoingCallActivityBinding binding = CometchatOngoingCallActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CallActivityViewModel callActivityViewModel = new ViewModelProvider(this).get(CallActivityViewModel.class);

        if (savedInstanceState != null && savedInstanceState.containsKey(UIKitConstants.IntentStrings.STORE_INSTANCE)) {
            onGoingCallSettingsBuilder = callActivityViewModel.getOnGoingCallSettingsBuilder();
        } else {
            callActivityViewModel.setOnGoingCallSettingsBuilder(onGoingCallSettingsBuilder);
        }

        // Retrieve the call details from the intent
        String sessionId = getIntent().getStringExtra(SESSION_ID);
        String callType = getIntent().getStringExtra(CALL_TYPE);

        // Configure the ongoing call UI with the received Call data
        binding.cometchatOngoingCall.setCallWorkFlow(UIKitConstants.CallWorkFlow.DEFAULT);
        binding.cometchatOngoingCall.setSessionId(sessionId);
        binding.cometchatOngoingCall.setCallType(callType);
        binding.cometchatOngoingCall.setCallSettingsBuilder(onGoingCallSettingsBuilder);

        // Start the call session
        binding.cometchatOngoingCall.startCall();

        // Disable back button press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                startPictureInPictureMode();
            }
        });
    }

    private void startPictureInPictureMode() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.enterPictureInPictureMode(new PictureInPictureParams.Builder().setAspectRatio(new Rational(
                metrics.widthPixels,
                metrics.heightPixels
            )).build());
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
        if (isInPictureInPictureMode) {
            CometChatCalls.enterPIPMode();
        } else {
            CometChatCalls.exitPIPMode();
        }
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        startPictureInPictureMode();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onGoingCallSettingsBuilder = null;
        CallingExtension.setActiveCall(null);
        CometChat.clearActiveCall();
    }
}
