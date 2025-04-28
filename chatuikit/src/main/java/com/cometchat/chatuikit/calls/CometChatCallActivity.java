package com.cometchat.chatuikit.calls;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.app.PictureInPictureParams;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Rational;
import android.widget.LinearLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.cometchat.calls.core.CometChatCalls;
import com.cometchat.chat.core.Call;
import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.CustomMessage;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.calls.outgoingcall.CometChatOutgoingCall;
import com.cometchat.chatuikit.calls.outgoingcall.OutgoingCallConfiguration;
import com.cometchat.chatuikit.logger.CometChatLogger;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CometChatCallActivity extends AppCompatActivity {
    private static final String TAG = CometChatCallActivity.class.getSimpleName();
    private static final String OUTGOING_CALL = "outgoing_call";
    private static final String DIRECT_CALL = "direct_call";
    private static BaseMessage baseMessage;
    private static Call call;
    private static User user;
    private static OutgoingCallConfiguration outgoingCallConfiguration;
    private static CometChatCalls.CallSettingsBuilder onGoingCallSettingsBuilder;
    private static String callingType;

    public static void launchOutgoingCallScreen(
        @Nonnull Context mContext, @Nonnull Call mCall, @Nullable OutgoingCallConfiguration mOutgoingCallConfiguration
    ) {
        callingType = OUTGOING_CALL;
        outgoingCallConfiguration = mOutgoingCallConfiguration;
        call = mCall;
        baseMessage = null;
        if (mCall.getReceiverType() != null && mCall.getReceiverType().equals(UIKitConstants.ReceiverType.USER) && mCall.getReceiver() != null) {
            user = (User) mCall.getReceiver();
        }
        startActivity(mContext);
    }

    private static void startActivity(Context context) {
        Intent intent = new Intent(context, CometChatCallActivity.class);
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void launchConferenceCallScreen(
        @Nonnull Context context, @Nonnull BaseMessage baseMessage_, @Nullable CometChatCalls.CallSettingsBuilder callSettingsBuilder_
    ) {
        callingType = DIRECT_CALL;
        baseMessage = baseMessage_;
        onGoingCallSettingsBuilder = callSettingsBuilder_;
        startActivity(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cometchat_activity_comet_chat_call);

        LinearLayout callScreen = findViewById(R.id.call_screen_parent_layout);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                                                               LinearLayout.LayoutParams.MATCH_PARENT
        );

        CallActivityViewModel callActivityViewModel = new ViewModelProvider(this).get(CallActivityViewModel.class);

        if (savedInstanceState != null && savedInstanceState.containsKey(UIKitConstants.IntentStrings.STORE_INSTANCE)) {
            baseMessage = callActivityViewModel.getBaseMessage();
            call = callActivityViewModel.getCall();
            user = callActivityViewModel.getUser();
            outgoingCallConfiguration = callActivityViewModel.getOutgoingCallConfiguration();
            callingType = callActivityViewModel.getCallingType();
            onGoingCallSettingsBuilder = callActivityViewModel.getOnGoingCallSettingsBuilder();
        } else {
            callActivityViewModel.setCallingType(callingType);
            callActivityViewModel.setUser(user);
            callActivityViewModel.setCall(call);
            callActivityViewModel.setBaseMessage(baseMessage);
            callActivityViewModel.setOutgoingCallConfiguration(outgoingCallConfiguration);
            callActivityViewModel.setOnGoingCallSettingsBuilder(onGoingCallSettingsBuilder);
        }
        if (OUTGOING_CALL.equals(callingType) || DIRECT_CALL.equals(callingType)) {
            CometChatOutgoingCall cometchatOutgoingCall = new CometChatOutgoingCall(this);
            cometchatOutgoingCall.setLayoutParams(layoutParams);
            if (OUTGOING_CALL.equals(callingType)) {
                if (outgoingCallConfiguration != null) {
                    cometchatOutgoingCall.setEndCallIcon(outgoingCallConfiguration.getDeclineButtonIcon());
                    cometchatOutgoingCall.setOnEndCallClick(outgoingCallConfiguration.getOnDeclineButtonClick());
                    cometchatOutgoingCall.setStyle(outgoingCallConfiguration.getOutgoingCallStyle());
                    cometchatOutgoingCall.disableSoundForCall(outgoingCallConfiguration.isDisableSoundForCalls());
                    cometchatOutgoingCall.setCustomSoundForCalls(outgoingCallConfiguration.getCustomSoundForCalls());
                    cometchatOutgoingCall.setOnError(outgoingCallConfiguration.getOnError());
                    cometchatOutgoingCall.setCallSettingsBuilder(outgoingCallConfiguration.getCallSettingBuilder());
                }
                if (user != null && call != null) {
                    cometchatOutgoingCall.setUser(user);
                    cometchatOutgoingCall.setCall(call);
                }
            } else if (DIRECT_CALL.equals(callingType)) {
                if (baseMessage instanceof CustomMessage && baseMessage.getType().equalsIgnoreCase(UIKitConstants.MessageType.MEETING)) {
                    CustomMessage customMessage = (CustomMessage) baseMessage;
                    try {
                        String id = customMessage.getCustomData().getString(UIKitConstants.CallingJSONConstants.CALL_SESSION_ID);
                        String callType = customMessage.getCustomData().getString(UIKitConstants.CallingJSONConstants.CALL_TYPE);
                        cometchatOutgoingCall.setCallSettingsBuilder(onGoingCallSettingsBuilder);
                        Call call = new Call(customMessage.getReceiverUid(), UIKitConstants.ReceiverType.GROUP, callType);
                        call.setSessionId(id);
                        cometchatOutgoingCall.launchOnGoingScreen(call);
                    } catch (java.lang.Exception e) {
                        CometChatLogger.e(TAG, e.toString());
                    }
                }
            }
            callScreen.addView(cometchatOutgoingCall);
        }
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
            this.enterPictureInPictureMode(new PictureInPictureParams.Builder()
                                               .setAspectRatio(new Rational(metrics.widthPixels, metrics.heightPixels))
                                               .build());
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(UIKitConstants.IntentStrings.STORE_INSTANCE, true);
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
        CometChatCalls.endSession();
        CallingExtension.setActiveCall(null);
        CometChat.clearActiveCall();
        baseMessage = null;
        call = null;
        callingType = null;
        user = null;
        onGoingCallSettingsBuilder = null;
        outgoingCallConfiguration = null;
    }
}
