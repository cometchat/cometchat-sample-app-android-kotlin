package com.cometchat.chatuikit.calls.outgoingcall;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.PowerManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.RawRes;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import com.cometchat.calls.core.CometChatCalls;
import com.cometchat.chat.core.Call;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.databinding.CometchatOutgoingCallLayoutBinding;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.interfaces.Function2;
import com.cometchat.chatuikit.shared.interfaces.OnBackPress;
import com.cometchat.chatuikit.shared.interfaces.OnClick;
import com.cometchat.chatuikit.shared.interfaces.OnError;
import com.cometchat.chatuikit.shared.resources.soundmanager.CometChatSoundManager;
import com.cometchat.chatuikit.shared.resources.soundmanager.Sound;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.google.android.material.card.MaterialCardView;

/**
 * CometChatOutgoingCall is a custom view that represents the UI for an outgoing
 * call.
 *
 * <p>
 * It extends the MaterialCardView class and provides methods for setting
 * call-related properties and styles.
 */
public class CometChatOutgoingCall extends MaterialCardView implements DefaultLifecycleObserver {
    private static final String TAG = CometChatOutgoingCall.class.getSimpleName();
    private CometchatOutgoingCallLayoutBinding binding;
    private OutgoingViewModel viewModel;

    private Call call;
    private User user;

    private OnError onError;
    private OnClick onEndCallClick;
    private OnBackPress onBackPress;

    private CometChatSoundManager soundManager;
    private CometChatCalls.CallSettingsBuilder callSettingsBuilder;

    private boolean disableSoundForCall;
    private Drawable endCallIcon;
    private @RawRes int customSoundForCalls;
    private @StyleRes int style;
    private @ColorInt int titleTextColor;
    private @ColorInt int subtitleTextColor;
    private @StyleRes int titleTextAppearance;
    private @StyleRes int subtitleTextAppearance;
    private @ColorInt int endCallIconTint;
    private @StyleRes int avatarStyle;
    private @ColorInt int endCallButtonBackgroundColor;
    private @ColorInt int backgroundColor;
    private @Dimension int cornerRadius;
    private @Dimension int strokeWidth;
    private @ColorInt int strokeColor;

    private PowerManager.WakeLock wakeLock;
    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private SensorEventListener proximitySensorListener;
    private Function2<Context, Call, View> titleView, subtitleView, avatarView, endCallView;

    /**
     * Constructs a new CometChatOutgoingCall with the given context.
     *
     * @param context The context in which the view is created.
     */
    public CometChatOutgoingCall(Context context) {
        this(context, null);
    }

    /**
     * Constructs a new CometChatOutgoingCall with the given context and attribute
     * set.
     *
     * @param context The context in which the view is created.
     * @param attrs   The attribute set for the view.
     */
    public CometChatOutgoingCall(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.cometchatOutgoingCallStyle);
    }

    /**
     * Constructs a new CometChatOutgoingCall with the given context, attribute set,
     * and default style.
     *
     * @param context      The context in which the view is created.
     * @param attrs        The attribute set for the view.
     * @param defStyleAttr The default style resource.
     */
    public CometChatOutgoingCall(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflateAndInitializeView(context, attrs, defStyleAttr);
    }

    /**
     * Initializes the view.
     *
     * @param context The context in which the view is created.
     */
    private void inflateAndInitializeView(Context context, AttributeSet attrs, int defStyleAttr) {
        Utils.initMaterialCard(this);
        ((Activity) context).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        binding = CometchatOutgoingCallLayoutBinding.inflate(LayoutInflater.from(getContext()), this, true);

        initSensors(context);
        soundManager = new CometChatSoundManager(context);
        // Register the component as a LifecycleObserver
        ((AppCompatActivity) context).getLifecycle().addObserver(this);
        viewModel = new ViewModelProvider.NewInstanceFactory().create(OutgoingViewModel.class);
        viewModel.getAcceptedCall().observe((LifecycleOwner) context, this::acceptedCall);
        viewModel.getRejectCall().observe((LifecycleOwner) context, this::rejectedCall);
        viewModel.getException().observe((LifecycleOwner) context, this::triggerError);
        viewModel.getDisableEndCallButton().observe((LifecycleOwner) context, this::setDisableEndCallButton);

        binding.endCall.getButton().setOnClickListener(view -> {
            binding.endCall.getButton().setEnabled(false);
            if (onEndCallClick == null) {
                if (call != null) viewModel.rejectCall(call);
            } else onEndCallClick.onClick();
        });
        applyStyleAttributes(attrs, defStyleAttr);
    }


    private void initSensors(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if (proximitySensor == null) {
            return;
        }
        proximitySensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.values[0] < proximitySensor.getMaximumRange()) {
                    turnOffScreen();
                } else {
                    turnOnScreen();
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        };
    }

    /**
     * Handles the acceptance of a call and launches the ongoing call screen.
     *
     * @param call The Call object representing the accepted call.
     */
    public void acceptedCall(Call call) {
        launchOnGoingScreen(call);
    }

    /**
     * Handles the rejection of a call. If a back press handler is set, it invokes
     * the handler; otherwise, it finishes the current activity.
     *
     * @param call The Call object representing the rejected call.
     */
    public void rejectedCall(Call call) {
        if (onBackPress != null) {
            onBackPress.onBack();
        } else {
            ((Activity) getContext()).finish();
        }
    }

    /**
     * Triggers an error callback or finishes the activity if no error handler is
     * set.
     *
     * @param e The CometChatException that contains details about the error that
     *          occurred.
     */
    private void triggerError(CometChatException e) {
        if (onError == null) {
            ((Activity) getContext()).finish();
        } else {
            onError.onError(e);
        }
    }

    private void setDisableEndCallButton(Boolean disable) {
        binding.endCall.getButton().setEnabled(!disable);
    }

    /**
     * Applies style attributes based on the XML layout or theme.
     *
     * @param attrs        The attribute set containing customization.
     * @param defStyleAttr The default style attribute.
     */
    private void applyStyleAttributes(AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatOutgoingCall, defStyleAttr, 0);
        @StyleRes int style = typedArray.getResourceId(R.styleable.CometChatOutgoingCall_cometchatOutgoingCallStyle, 0);
        typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatOutgoingCall, defStyleAttr, style);
        extractAttributesAndApplyDefaults(typedArray);
    }

    private void turnOffScreen() {
        if (getContext() instanceof Activity) {
            Activity activity = (Activity) getContext();
            PowerManager powerManager = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
            if (powerManager != null) {
                if (wakeLock == null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "MyApp::ProximityWakeLock");
                }
                if (!wakeLock.isHeld()) {
                    wakeLock.acquire();
                }
            }
        }
    }

    private void turnOnScreen() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    public void launchOnGoingScreen(Call call) {
        binding.outgoingCallLayout.setVisibility(GONE);
        binding.ongoingCall.setCallWorkFlow(call
                                                .getReceiverType()
                                                .equalsIgnoreCase(UIKitConstants.ReceiverType.GROUP) ? UIKitConstants.CallWorkFlow.MEETING : UIKitConstants.CallWorkFlow.DEFAULT);
        binding.ongoingCall.setSessionId(call.getSessionId());
        binding.ongoingCall.setCallType(call.getType());
        binding.ongoingCall.setCallSettingsBuilder(callSettingsBuilder);
        binding.ongoingCall.startCall();
        binding.ongoingCall.setVisibility(VISIBLE);
        soundManager.pauseSilently();
    }

    /**
     * Extracts attributes from the given {@link TypedArray} and applies default
     * values.
     *
     * @param typedArray The TypedArray containing the view's attributes.
     */
    private void extractAttributesAndApplyDefaults(TypedArray typedArray) {
        try {
            setTitleTextColor(typedArray.getColor(R.styleable.CometChatOutgoingCall_cometchatOutgoingCallTitleTextColor, 0));
            setTitleTextAppearance(typedArray.getResourceId(R.styleable.CometChatOutgoingCall_cometchatOutgoingCallTitleTextAppearance, 0));
            setSubtitleTextColor(typedArray.getColor(R.styleable.CometChatOutgoingCall_cometchatOutgoingCallSubtitleTextColor, 0));
            setSubtitleTextAppearance(typedArray.getResourceId(R.styleable.CometChatOutgoingCall_cometchatOutgoingCallSubtitleTextAppearance, 0));
            setEndCallIcon(typedArray.getDrawable(R.styleable.CometChatOutgoingCall_cometchatOutgoingCallEndCallIcon));
            setEndCallIconTint(typedArray.getColor(R.styleable.CometChatOutgoingCall_cometchatOutgoingCallEndCallIconTint, 0));
            setEndCallButtonBackgroundColor(typedArray.getColor(
                R.styleable.CometChatOutgoingCall_cometchatOutgoingCallEndCallButtonBackgroundColor,
                0
            ));
            setBackgroundColor(typedArray.getColor(R.styleable.CometChatOutgoingCall_cometchatOutgoingCallBackgroundColor, 0));
            setCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatOutgoingCall_cometchatOutgoingCallCornerRadius, 0));
            setStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatOutgoingCall_cometchatOutgoingCallStrokeWidth, 0));
            setStrokeColor(typedArray.getColor(R.styleable.CometChatOutgoingCall_cometchatOutgoingCallStrokeColor, 0));
            setAvatarStyle(typedArray.getResourceId(R.styleable.CometChatOutgoingCall_cometchatOutgoingCallAvatarStyle, 0));
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Sets the stroke color for the UI element.
     *
     * @param strokeColor The color to set for the stroke.
     */
    @Override
    public void setStrokeColor(int strokeColor) {
        this.strokeColor = strokeColor;
        super.setStrokeColor(strokeColor);
    }

    /**
     * Sets the call associated with the outgoing call view.
     *
     * @param call The Call object representing the outgoing call.
     */
    public void setCall(Call call) {
        if (call != null) {
            this.call = call;
            if (call.getReceiverType().equals(UIKitConstants.ReceiverType.USER)) {
                setUser((User) call.getReceiver());
            }
        }
    }

    /**
     * Sets the user associated with the outgoing call.
     *
     * @param user The User object representing the user making the outgoing call.
     */
    public void setUser(User user) {
        if (user != null) {
            this.user = user;
            binding.titleText.setText(user.getName());
            binding.avatar.setAvatar(user.getName(), user.getAvatar());
            binding.subtitleText.setText(getResources().getString(R.string.cometchat_calling) + " ...");
        }
    }

    /**
     * Sets the CallSettingsBuilder for the outgoing call configuration.
     *
     * @param callSettingsBuilder The CallSettingsBuilder to set.
     */
    public void setCallSettingsBuilder(CometChatCalls.CallSettingsBuilder callSettingsBuilder) {
        if (callSettingsBuilder != null) {
            this.callSettingsBuilder = callSettingsBuilder;
        }
    }

    /**
     * Enables or disables sound for the call based on the provided flag.
     *
     * @param disableSoundForCall true to disable sound for calls; false to enable.
     */
    public void disableSoundForCall(boolean disableSoundForCall) {
        this.disableSoundForCall = disableSoundForCall;
    }

    /**
     * Sets a custom sound resource for incoming calls.
     *
     * @param customSoundForCalls The resource identifier for the custom sound.
     */
    public void setCustomSoundForCalls(@RawRes int customSoundForCalls) {
        if (customSoundForCalls != 0) {
            this.customSoundForCalls = customSoundForCalls;
        }
    }

    /**
     * Gets the ViewModel associated with outgoing calls.
     *
     * @return The OutgoingViewModel instance.
     */
    public OutgoingViewModel getViewModel() {
        return viewModel;
    }

    /**
     * Gets the OnClick callback for canceling calls.
     *
     * @return The OnClick callback for canceling calls.
     */
    public OnClick getOnEndCallClick() {
        return onEndCallClick;
    }

    /**
     * Sets a callback to be invoked when the cancel call button is clicked.
     *
     * @param click The OnClick callback to set.
     */
    public void setOnEndCallClick(OnClick click) {
        if (click != null) {
            this.onEndCallClick = click;
        }
    }

    /**
     * Gets the OnError callback for handling errors.
     *
     * @return The OnError callback.
     */
    public OnError getOnError() {
        return onError;
    }

    /**
     * Sets a callback to handle errors during the call process.
     *
     * @param onError The OnError callback to set.
     */
    public void setOnError(OnError onError) {
        if (onError != null) {
            this.onError = onError;
        }
    }

    /**
     * Gets the style resource identifier.
     *
     * @return The style resource identifier.
     */
    public @StyleRes int getStyle() {
        return style;
    }

    /**
     * Sets the style of the text bubble from a specific style resource.
     *
     * @param style The resource ID of the style to apply.
     */
    public void setStyle(@StyleRes int style) {
        if (style != 0) {
            this.style = style;
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(style, R.styleable.CometChatOutgoingCall);
            extractAttributesAndApplyDefaults(typedArray);
        }
    }

    /**
     * Gets the title text color.
     *
     * @return The title text color.
     */
    public @ColorInt int getTitleTextColor() {
        return titleTextColor;
    }

    /**
     * Sets the text color for the title in the outgoing call UI.
     *
     * @param titleTextColor The color to set for the title text.
     */
    public void setTitleTextColor(@ColorInt int titleTextColor) {
        this.titleTextColor = titleTextColor;
        binding.titleText.setTextColor(titleTextColor);
    }

    /**
     * Gets the subtitle text color.
     *
     * @return The subtitle text color.
     */
    public @ColorInt int getSubtitleTextColor() {
        return subtitleTextColor;
    }

    /**
     * Sets the text color for the subtitle in the outgoing call UI.
     *
     * @param color The color to set for the subtitle text.
     */
    public void setSubtitleTextColor(@ColorInt int color) {
        this.subtitleTextColor = color;
        binding.subtitleText.setTextColor(color);
    }

    /**
     * Gets the title text appearance resource identifier.
     *
     * @return The title text appearance resource identifier.
     */
    public @StyleRes int getTitleTextAppearance() {
        return titleTextAppearance;
    }

    /**
     * Sets the text appearance style for the title in the outgoing call UI.
     *
     * @param titleTextAppearance The resource identifier for the title text appearance style.
     */
    public void setTitleTextAppearance(@StyleRes int titleTextAppearance) {
        this.titleTextAppearance = titleTextAppearance;
        binding.titleText.setTextAppearance(titleTextAppearance);
    }

    /**
     * Gets the subtitle text appearance resource identifier.
     *
     * @return The subtitle text appearance resource identifier.
     */
    public @StyleRes int getSubtitleTextAppearance() {
        return subtitleTextAppearance;
    }

    /**
     * Sets the text appearance style for the subtitle in the outgoing call UI.
     *
     * @param appearance The resource identifier for the subtitle text appearance style.
     */
    public void setSubtitleTextAppearance(@StyleRes int appearance) {
        this.subtitleTextAppearance = appearance;
        binding.subtitleText.setTextAppearance(appearance);
    }

    /**
     * Gets the drawable icon for the end call button.
     *
     * @return The end call icon drawable.
     */
    public Drawable getEndCallIcon() {
        return endCallIcon;
    }

    /**
     * Sets the drawable icon for the end call button.
     *
     * @param endCallIcon The Drawable to set as the end call button icon.
     */
    public void setEndCallIcon(Drawable endCallIcon) {
        this.endCallIcon = endCallIcon;
        binding.endCall.setButtonIcon(endCallIcon);
    }

    /**
     * Gets the tint color applied to the end call button icon.
     *
     * @return The tint color of the end call button icon.
     */
    public @ColorInt int getEndCallIconTint() {
        return endCallIconTint;
    }

    /**
     * Sets the tint color for the end call button icon.
     *
     * @param endCallIconTint The tint color to apply to the end call button icon.
     */
    public void setEndCallIconTint(@ColorInt int endCallIconTint) {
        this.endCallIconTint = endCallIconTint;
        binding.endCall.setButtonIconTint(endCallIconTint);
    }

    /**
     * Gets the avatar style resource identifier.
     *
     * @return The avatar style resource identifier.
     */
    public @StyleRes int getAvatarStyle() {
        return avatarStyle;
    }

    /**
     * Sets the style for the avatar in the outgoing call UI.
     *
     * @param avatarStyle The resource identifier for the avatar style.
     */
    public void setAvatarStyle(@StyleRes int avatarStyle) {
        this.avatarStyle = avatarStyle;
        binding.avatar.setStyle(avatarStyle);
    }

    /**
     * Gets the background color of the end call button.
     *
     * @return The background color of the end call button.
     */
    public @ColorInt int getEndCallButtonBackgroundColor() {
        return endCallButtonBackgroundColor;
    }

    /**
     * Sets the background color for the end call button.
     *
     * @param endCallButtonBackgroundColor The color to set for the end call button background.
     */
    public void setEndCallButtonBackgroundColor(@ColorInt int endCallButtonBackgroundColor) {
        this.endCallButtonBackgroundColor = endCallButtonBackgroundColor;
        binding.endCall.setButtonBackgroundColor(endCallButtonBackgroundColor);
    }

    /**
     * Gets the background color of the outgoing call UI.
     *
     * @return The background color of the outgoing call UI.
     */
    public @ColorInt int getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the background color for the outgoing call UI.
     *
     * @param backgroundColor The color to set as the background color.
     */
    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        super.setCardBackgroundColor(backgroundColor);
    }

    /**
     * Gets the corner radius for the UI elements.
     *
     * @return The corner radius for the UI elements.
     */
    public @Dimension float getCornerRadius() {
        return cornerRadius;
    }

    /**
     * Sets the corner radius for the outgoing call UI elements.
     *
     * @param cornerRadius The radius to set for the corners of the UI elements.
     */
    public void setCornerRadius(@Dimension int cornerRadius) {
        this.cornerRadius = cornerRadius;
        super.setRadius(cornerRadius);
    }

    /**
     * Gets the stroke color as a ColorStateList.
     *
     * @return The stroke color as a ColorStateList.
     */
    public ColorStateList getStokeColor() {
        return ColorStateList.valueOf(strokeColor);
    }    /**
     * Plays the outgoing call sound if sound notifications are not disabled. It
     * uses the custom sound resource if provided; otherwise, it defaults to the
     * standard outgoing call sound.
     */
    private void playSound() {
        if (!disableSoundForCall) {
            soundManager.play(Sound.outgoingCall, customSoundForCalls);
        }
    }

    /**
     * Called when the view is detached from the window. Removes listeners from the
     * ViewModel and pauses sound playback.
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        viewModel.removeListeners();
        soundManager.pauseSilently();
        stopProximitySensor();
        if (getContext() instanceof AppCompatActivity) {
            ((AppCompatActivity) getContext()).getLifecycle().removeObserver(this);
        }
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
        }
    }

    public void stopProximitySensor() {
        if (sensorManager != null && proximitySensorListener != null) {
            sensorManager.unregisterListener(proximitySensorListener);
        }
    }

    public Function2<Context, Call, View> getTitleView() {
        return titleView;
    }

    public void setTitleView(Function2<Context, Call, View> titleView) {
        if (titleView != null) {
            Utils.handleView(binding.titleLayout, titleView.apply(getContext(), call), true);
            this.titleView = titleView;
        }
    }

    public Function2<Context, Call, View> getSubtitleView() {
        return subtitleView;
    }

    public void setSubtitleView(Function2<Context, Call, View> subtitleView) {
        if (subtitleView != null) {
            Utils.handleView(binding.subtitleLayout, subtitleView.apply(getContext(), call), true);
            this.subtitleView = subtitleView;
        }
    }

    public Function2<Context, Call, View> getAvatarView() {
        return avatarView;
    }

    public void setAvatarView(Function2<Context, Call, View> avatarView) {
        if (avatarView != null) {
            Utils.handleView(binding.avatarLayout, avatarView.apply(getContext(), call), true);
            this.avatarView = avatarView;
        }
    }

    public Function2<Context, Call, View> getEndCallView() {
        return endCallView;
    }

    public void setEndCallView(Function2<Context, Call, View> endCallView) {
        if (endCallView != null) {
            Utils.handleView(binding.endCallLayout, endCallView.apply(getContext(), call), true);
            this.endCallView = endCallView;
        }
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        if (getContext() instanceof LifecycleOwner) {
            ((LifecycleOwner) getContext()).getLifecycle().removeObserver(this);
        }
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (((Activity) getContext()).isInPictureInPictureMode()) {
                handlePiPExit();
            }
        }
    }

    private void handlePiPExit() {
        viewModel.removeListeners();
        viewModel.rejectCall(call);
        ((Activity) getContext()).finish();
    }



    /**
     * Sets the stroke width for the UI element.
     *
     * @param strokeWidth The width of the stroke to set.
     */
    public void setStrokeWidth(@Dimension int strokeWidth) {
        this.strokeWidth = strokeWidth;
        super.setStrokeWidth(strokeWidth);
    }

    public void startProximitySensor() {
        if (sensorManager != null && proximitySensor != null) {
            sensorManager.registerListener(proximitySensorListener, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }


    /**
     * Gets the stroke width for the UI element.
     *
     * @return The stroke width for the UI element.
     */
    @Override
    public @Dimension int getStrokeWidth() {
        return strokeWidth;
    }


    /**
     * Called when the view is attached to the window. Adds listeners to the
     * ViewModel and plays sound if a user is present.
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        viewModel.addListeners();
        if (user != null) {
            playSound();
        }
        startProximitySensor();
    }


}
