package com.cometchat.sampleapp.java.fcm.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;

import com.cometchat.chat.core.Call;
import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.calls.CallingExtension;
import com.cometchat.chatuikit.calls.CometChatCallActivity;
import com.cometchat.chatuikit.calls.incomingcall.CometChatIncomingCall;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKitHelper;
import com.cometchat.chatuikit.shared.events.CometChatCallEvents;
import com.cometchat.chatuikit.shared.resources.soundmanager.CometChatSoundManager;
import com.cometchat.chatuikit.shared.resources.soundmanager.Sound;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.sampleapp.java.fcm.data.repository.Repository;
import com.cometchat.sampleapp.java.fcm.viewmodels.SplashViewModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;

/**
 * This is a custom Application class for managing call notifications and
 * handling CometChat events throughout the app lifecycle. It also registers
 * lifecycle callbacks to keep track of the currently active activity.
 */
public class MyApplication extends Application {
    public static final List<PopupWindow> popupWindows = new ArrayList<>();
    public static String currentOpenChatId;
    private static String LISTENER_ID;
    private static boolean isAppInForeground;
    private static Activity currentActivity;
    private static Call tempCall;
    private static CometChatSoundManager soundManager;
    private final AtomicBoolean isConnectedToWebSockets = new AtomicBoolean(false);
    private Snackbar snackbar;

    public static boolean isAppInForeground() {
        return isAppInForeground;
    }

    public static Activity getCurrentActivity() {
        return currentActivity;
    }

    public static Call getTempCall() {
        return tempCall;
    }

    public static void setTempCall(Call call) {
        tempCall = call;
        if (tempCall == null && soundManager != null)
            soundManager.pauseSilently();
    }

    /**
     * Initializes the application by setting up Firebase, adding the CometChat call
     * listener, and registering activity lifecycle callbacks.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        if (!CometChatUIKit.isSDKInitialized()) {
            SplashViewModel viewModel = new SplashViewModel();
            viewModel.initUIKit(this);
        }

        LISTENER_ID = String.valueOf(System.currentTimeMillis());

        FirebaseApp.initializeApp(this);

        addCallListener();

        // Initialize the sensor manager and shake detector
        soundManager = new CometChatSoundManager(this);

        // Register activity lifecycle callbacks to keep track of the current activity
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            private boolean isActivityChangingConfigurations = false;
            private int activityReferences = 0;

            @Override
            public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {
                currentActivity = activity;
                if (currentActivity instanceof CometChatCallActivity) {
                    Utils.setStatusBarColor(currentActivity, CometChatTheme.getBackgroundColor3(activity));
                } else {
                    Utils.setStatusBarColor(currentActivity, CometChatTheme.getBackgroundColor1(activity));
                }
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                if (CometChatUIKit.isSDKInitialized() && isConnectedToWebSockets.compareAndSet(false, true)) {
                    CometChat.connect(new CometChat.CallbackListener<String>() {
                        @Override
                        public void onSuccess(String s) {
                            isConnectedToWebSockets.set(true);
                        }

                        @Override
                        public void onError(CometChatException e) {
                            isConnectedToWebSockets.set(false);
                        }
                    });
                }
                currentActivity = activity;
                if (++activityReferences == 1 && !isActivityChangingConfigurations) {
                    isAppInForeground = true;
                }
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                currentActivity = activity;
                if (snackbar != null && tempCall != null) {
                    showTopSnackBar(tempCall);
                } else
                    dismissTopSnackBar();
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
                isActivityChangingConfigurations = activity.isChangingConfigurations();
                if (--activityReferences == 0 && !isActivityChangingConfigurations) {
                    isAppInForeground = false;
                }
                if (CometChatUIKit.isSDKInitialized() && !isAppInForeground) {
                    CometChat.disconnect(new CometChat.CallbackListener<String>() {
                        @Override
                        public void onSuccess(String s) {
                            isConnectedToWebSockets.set(false);
                        }

                        @Override
                        public void onError(CometChatException e) {

                        }
                    });
                }
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                if (currentActivity == activity) {
                    currentActivity = null;
                }
            }
        });
    }

    /**
     * Adds a call listener using CometChat to handle incoming and outgoing call
     * events.
     */
    public void addCallListener() {
        CometChat.addCallListener(LISTENER_ID, new CometChat.CallListener() {
            @Override
            public void onIncomingCallReceived(Call call) {
                playSound();
                launchIncomingCallPopup(call);
            }

            @Override
            public void onOutgoingCallAccepted(Call call) {
                dismissTopSnackBar();
            }

            @Override
            public void onOutgoingCallRejected(Call call) {
                dismissTopSnackBar();
            }

            @Override
            public void onIncomingCallCancelled(Call call) {
                dismissTopSnackBar();
            }

            @Override
            public void onCallEndedMessageReceived(Call call) {
                dismissTopSnackBar();
            }
        });

        CometChatCallEvents.addListener(LISTENER_ID, new CometChatCallEvents() {
            @Override
            public void ccCallAccepted(Call call) {
                dismissTopSnackBar();
            }

            @Override
            public void ccCallRejected(Call call) {
                dismissTopSnackBar();
            }
        });
    }

    /**
     * Displays a custom SnackBar notification at the top of the screen for incoming
     * calls.
     *
     * @param call The call object representing the incoming call.
     */
    @SuppressLint("RestrictedApi")
    public void showTopSnackBar(Call call) {
        if (!isAppInForeground()) return;
        if (currentActivity != null && call != null) {
            tempCall = call;
            View rootView = currentActivity.findViewById(android.R.id.content);

            CometChatIncomingCall cometChatIncomingCall = new CometChatIncomingCall(currentActivity);
            cometChatIncomingCall.disableSoundForCalls(true);
            cometChatIncomingCall.setCall(call);
            cometChatIncomingCall.setOnError((cometchatException) -> dismissTopSnackBar());

            snackbar = Snackbar.make(rootView, " ", Snackbar.LENGTH_INDEFINITE);
            Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) layout.getLayoutParams();
            params.gravity = Gravity.TOP;
            layout.setLayoutParams(params);
            layout.setBackgroundColor(currentActivity.getResources().getColor(android.R.color.transparent, null));

            layout.addView(cometChatIncomingCall, 0);

            for (PopupWindow popupWindow : popupWindows) {
                if (popupWindow.isShowing())
                    popupWindow.dismiss();
            }
            popupWindows.clear();

            snackbar.show();
        }
    }

    /**
     * Launches an incoming call popup when an incoming call is received.
     *
     * @param call The call object representing the incoming call.
     */
    public void launchIncomingCallPopup(@Nonnull Call call) {
        if (call.getCallInitiator() instanceof User) {
            User callInitiator = (User) call.getCallInitiator();
            if (CometChatUIKit.getLoggedInUser().getUid().equalsIgnoreCase(callInitiator.getUid()))
                return;
        }

        if (CometChat.getActiveCall() == null && CallingExtension.getActiveCall() == null && !CallingExtension.isActiveMeeting()) {
            CallingExtension.setActiveCall(call);
            showTopSnackBar(call);
        } else {
            rejectCallWithBusyStatus(call);
        }
    }

    /**
     * Rejects an incoming call with a "busy" status if another call is active.
     *
     * @param call The call object representing the call to be rejected.
     */
    private void rejectCallWithBusyStatus(Call call) {
        new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Repository.rejectCallWithBusyStatus(call, new CometChat.CallbackListener<Call>() {
                @Override
                public void onSuccess(Call call) {
                    CometChatUIKitHelper.onCallRejected(call);
                }

                @Override
                public void onError(CometChatException e) {
                }
            });
        }).start();
    }

    /**
     * Dismisses the current top SnackBar if it's being shown and resets any related
     * call state.
     */
    public void dismissTopSnackBar() {
        if (snackbar != null && snackbar.isShown()) {
            snackbar.dismiss();
            snackbar = null;
            tempCall = null;
            CallingExtension.setActiveCall(null);
        }
        pauseSound();
    }

    /**
     * Silently pauses any currently playing sound.
     */
    public void pauseSound() {
        soundManager.pauseSilently();
    }

    /**
     * Plays the sound for an incoming call notification.
     */
    public void playSound() {
        soundManager.play(Sound.incomingCall);
    }
}
