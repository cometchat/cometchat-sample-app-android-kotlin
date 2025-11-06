package com.cometchat.chatuikit.calls.ongoingcall;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import com.cometchat.calls.core.CometChatCalls;
import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.calls.CallingExtension;
import com.cometchat.chatuikit.databinding.CometchatOngoingCallScreenBinding;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.interfaces.OnError;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.google.android.material.card.MaterialCardView;

/**
 * CometChatOngoingCall is a custom view that represents an ongoing call. It
 * extends MaterialCardView
 *
 * <p>
 * and provides methods to set the session ID, call type, error listener, and
 * style for the ongoing call view.
 */
public class CometChatOngoingCall extends MaterialCardView implements DefaultLifecycleObserver {
    private static final String TAG = CometChatOngoingCall.class.getSimpleName();
    private CometchatOngoingCallScreenBinding binding;
    private OngoingCallViewModel viewModel;
    private LifecycleOwner lifecycleOwner;
    private UIKitConstants.CallWorkFlow callWorkFlow = UIKitConstants.CallWorkFlow.DEFAULT;
    private OnError onError;
    private String sessionId;
    private String callType;

    public CometChatOngoingCall(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        Utils.initMaterialCard(this);
        binding = CometchatOngoingCallScreenBinding.inflate(LayoutInflater.from(getContext()), this, true);

        initViewModel();

        setCallSettingsBuilder(new CometChatCalls.CallSettingsBuilder((Activity) context));
        // Register the component as a LifecycleObserver
        ((AppCompatActivity) context).getLifecycle().addObserver(this);
        // Request the necessary permissions
        Utils.requestPermissions(context, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA}, 101);
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider.NewInstanceFactory().create(OngoingCallViewModel.class);
        lifecycleOwner = Utils.getLifecycleOwner(getContext());
        if (lifecycleOwner == null) return;
        viewModel.getEndCall().observe(lifecycleOwner, this::endCall);
        viewModel.getException().observe(lifecycleOwner, this::showError);
        viewModel.hideProgressBar().observe(lifecycleOwner, this::hideProgressBar);
        viewModel.isJoined().observe(lifecycleOwner, aBoolean -> ((AppCompatActivity) getContext()).runOnUiThread(() -> {
            if (((AppCompatActivity) getContext()).isInPictureInPictureMode()) {
                CometChatCalls.enterPIPMode();
            }
        }));
    }

    public void setCallSettingsBuilder(CometChatCalls.CallSettingsBuilder callSettingsBuilder) {
        if (callSettingsBuilder != null) {
            viewModel.setCallSettingsBuilder(callSettingsBuilder);
        }
    }

    public void endCall(Boolean call) {
        ((Activity) getContext()).finish();
    }

    public void showError(CometChatException exception) {
        if (onError != null) {
            onError.onError(exception);
        }
    }

    public void hideProgressBar(Boolean hideProgressBar) {
        Utils.setStatusBarColor((Activity) getContext(), getResources().getColor(R.color.cometchat_calling_background, null));
        if (hideProgressBar) {
            binding.progressBar.setVisibility(GONE);
            binding.callView.setVisibility(VISIBLE);
        } else {
            binding.progressBar.setVisibility(VISIBLE);
            binding.callView.setVisibility(GONE);
        }
    }

    public CometChatOngoingCall(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CometChatOngoingCall(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setCallWorkFlow(UIKitConstants.CallWorkFlow callWorkFlow) {
        this.callWorkFlow = callWorkFlow;
        viewModel.setCallWorkFlow(callWorkFlow);
    }

    public void setCallType(String callType) {
        this.callType = callType;
        viewModel.setCallType(callType);
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
        viewModel.setSessionId(sessionId);
    }

    /**
     * Initiates the ongoing call. This method triggers the start of the ongoing
     * call. It delegates the call initiation logic to the underlying view model
     * responsible for handling the ongoing call functionality.
     */
    public void startCall() {
        viewModel.startCall(binding.callView);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        viewModel.addListener();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        dispose();
    }

    private void dispose() {
        viewModel.removeListener();
        if (getContext() instanceof LifecycleOwner) {
            ((LifecycleOwner) getContext()).getLifecycle().removeObserver(this);
        }
        if (lifecycleOwner != null) {
            viewModel.getEndCall().removeObservers(lifecycleOwner);
            viewModel.getException().removeObservers(lifecycleOwner);
            viewModel.hideProgressBar().removeObservers(lifecycleOwner);
            viewModel.isJoined().removeObservers(lifecycleOwner);
        }
        viewModel = null;
        binding = null;
        lifecycleOwner = null;
    }

    @Override
    public void onDestroy(LifecycleOwner owner) {
        if (getContext() instanceof LifecycleOwner) {
            ((LifecycleOwner) getContext()).getLifecycle().removeObserver(this);
        }
    }

    @Override
    public void onStop(LifecycleOwner owner) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (((Activity) getContext()).isInPictureInPictureMode()) {
                handlePiPExit();
            }
        }
    }

    private void handlePiPExit() {
        viewModel.removeListener();
        if (!callWorkFlow.equals(UIKitConstants.CallWorkFlow.MEETING)) {
            viewModel.endCall();
            CometChat.clearActiveCall();
        } else {
            CometChatCalls.endSession();
        }
        CallingExtension.setIsActiveMeeting(false);
        ((Activity) getContext()).finish();
    }

    /**
     * Sets the error listener for the ongoing call.
     *
     * @param onError The OnError listener to be invoked when an error occurs in the
     *                ongoing call.
     */
    public void setOnError(OnError onError) {
        this.onError = onError;
    }
}
