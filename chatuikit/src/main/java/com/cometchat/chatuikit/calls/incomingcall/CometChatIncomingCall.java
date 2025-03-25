package com.cometchat.chatuikit.calls.incomingcall;

import static androidx.core.content.ContextCompat.getString;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.RawRes;
import androidx.annotation.StyleRes;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import com.cometchat.calls.core.CometChatCalls;
import com.cometchat.chat.constants.CometChatConstants;
import com.cometchat.chat.core.Call;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.calls.CometChatOngoingCallActivity;
import com.cometchat.chatuikit.databinding.CometchatIncomingCallComponentBinding;
import com.cometchat.chatuikit.shared.interfaces.OnClick;
import com.cometchat.chatuikit.shared.interfaces.OnError;
import com.cometchat.chatuikit.shared.resources.soundmanager.CometChatSoundManager;
import com.cometchat.chatuikit.shared.resources.soundmanager.Sound;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.google.android.material.card.MaterialCardView;

public class CometChatIncomingCall extends MaterialCardView {
    private Call call;
    private CometChatCalls.CallSettingsBuilder callSettingsBuilder;
    private CometChatSoundManager soundManager;
    private IncomingCallViewModel viewModel;
    private OnError onError;
    private @StyleRes int style;
    private OnClick onAcceptClick, onRejectClick;
    private boolean disableSoundForCalls;
    private @RawRes int customSoundForCalls;
    private View itemView, leadingView, titleView, subtitleView, trailingView;
    private CometchatIncomingCallComponentBinding binding;

    private @ColorInt int titleTextColor;
    private @ColorInt int subtitleTextColor;
    private @StyleRes int titleTextAppearance;
    private @StyleRes int subtitleTextAppearance;
    private @ColorInt int iconTint;
    private Drawable voiceCallIcon;
    private Drawable videoCallIcon;
    private @StyleRes int avatarStyle;
    private @ColorInt int rejectCallButtonBackgroundColor;
    private @ColorInt int acceptCallButtonBackgroundColor;
    private @ColorInt int backgroundColor;
    private @Dimension int cornerRadius;
    private @Dimension int strokeWidth;
    private @ColorInt int strokeColor;
    private @ColorInt int acceptButtonTextColor;
    private @ColorInt int rejectButtonTextColor;
    private @StyleRes int acceptButtonTextAppearance;
    private @StyleRes int rejectButtonTextAppearance;

    public CometChatIncomingCall(Context context) {
        this(context, null);
    }

    public CometChatIncomingCall(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.cometchatIncomingCallStyle);
    }

    public CometChatIncomingCall(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        binding = CometchatIncomingCallComponentBinding.inflate(LayoutInflater.from(getContext()), this, true);
        ((Activity) getContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Utils.initMaterialCard(this);
        soundManager = new CometChatSoundManager(getContext());
        // Initialize ViewModel and observe call state
        viewModel = new ViewModelProvider.NewInstanceFactory().create(IncomingCallViewModel.class);
        viewModel.getAcceptedCall().observe((LifecycleOwner) getContext(), this::acceptedCall);
        viewModel.getRejectCall().observe((LifecycleOwner) getContext(), this::rejectedCall);
        viewModel.getException().observe((LifecycleOwner) getContext(), this::throwError);

        binding.acceptButton.setOnClickListener(v -> {
            if (onAcceptClick != null) {
                onAcceptClick.onClick();
            } else viewModel.acceptCall(call);
        });

        binding.declineButton.setOnClickListener(v -> {
            if (onRejectClick != null) {
                onRejectClick.onClick();
            } else viewModel.rejectCall(call);
        });
        // Apply style attributes to customize the view
        applyStyleAttributes(attrs, defStyleAttr);

    }

    /**
     * Handles acceptance of the call and initiates the ongoing call screen.
     *
     * @param call the call object representing the accepted call.
     */
    private void acceptedCall(Call call) {
        CometChatOngoingCallActivity.launchOngoingCallActivity(getContext(), call.getSessionId(), call.getType(), callSettingsBuilder);
    }

    /**
     * Handles rejection of the call.
     *
     * @param call
     */
    private void rejectedCall(Call call) {
        pauseSound();
    }

    private void throwError(CometChatException e) {
        if (onError != null) {
            onError.onError(e);
        }
    }

    /**
     * Applies style attributes based on the XML layout or theme.
     *
     * @param attrs        The attribute set containing customization.
     * @param defStyleAttr The default style attribute.
     */
    private void applyStyleAttributes(AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatIncomingCall, defStyleAttr, 0);
        @StyleRes int style = typedArray.getResourceId(R.styleable.CometChatIncomingCall_cometchatIncomingCallStyle, 0);
        typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatIncomingCall, defStyleAttr, style);
        extractAttributesAndApplyDefaults(typedArray);
    }

    /**
     * Pauses the call sound.
     */
    public void pauseSound() {
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
            setTitleTextColor(typedArray.getColor(R.styleable.CometChatIncomingCall_cometchatIncomingCallTitleTextColor, 0));
            setTitleTextAppearance(typedArray.getResourceId(R.styleable.CometChatIncomingCall_cometchatIncomingCallTitleTextAppearance, 0));
            setSubtitleTextColor(typedArray.getColor(R.styleable.CometChatIncomingCall_cometchatIncomingCallSubtitleTextColor, 0));
            setSubtitleTextAppearance(typedArray.getResourceId(R.styleable.CometChatIncomingCall_cometchatIncomingCallSubtitleTextAppearance, 0));
            setIconTint(typedArray.getColor(R.styleable.CometChatIncomingCall_cometchatIncomingCallIconTint, 0));
            setVoiceCallIcon(typedArray.getDrawable(R.styleable.CometChatIncomingCall_cometchatIncomingCallVoiceCallIcon));
            setVideoCallIcon(typedArray.getDrawable(R.styleable.CometChatIncomingCall_cometchatIncomingCallVideoCallIcon));
            setAcceptCallButtonBackgroundColor(typedArray.getColor(R.styleable.CometChatIncomingCall_cometchatIncomingCallAcceptButtonBackgroundColor,
                                                                   0));
            setRejectCallButtonBackgroundColor(typedArray.getColor(R.styleable.CometChatIncomingCall_cometchatIncomingCallRejectButtonBackgroundColor,
                                                                   0));
            setBackgroundColor(typedArray.getColor(R.styleable.CometChatIncomingCall_cometchatIncomingCallBackgroundColor, 0));
            setCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatIncomingCall_cometchatIncomingCallCornerRadius, 0));
            setStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatIncomingCall_cometchatIncomingCallStrokeWidth, 0));
            setStrokeColor(typedArray.getColor(R.styleable.CometChatIncomingCall_cometchatIncomingCallStrokeColor, 0));
            setAvatarStyle(typedArray.getResourceId(R.styleable.CometChatIncomingCall_cometchatIncomingCallAvatarStyle, 0));
            setAcceptButtonTextColor(typedArray.getColor(R.styleable.CometChatIncomingCall_cometchatIncomingCallAcceptButtonTextColor, 0));
            setRejectButtonTextColor(typedArray.getColor(R.styleable.CometChatIncomingCall_cometchatIncomingCallRejectButtonTextColor, 0));
            setAcceptButtonTextAppearance(typedArray.getResourceId(R.styleable.CometChatIncomingCall_cometchatIncomingCallAcceptButtonTextAppearance,
                                                                   0));
            setRejectButtonTextAppearance(typedArray.getResourceId(R.styleable.CometChatIncomingCall_cometchatIncomingCallRejectButtonTextAppearance,
                                                                   0));

        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Sets the stroke color for this component.
     *
     * @param strokeColor the color to set as the stroke color.
     */
    @Override
    public void setStrokeColor(@ColorInt int strokeColor) {
        this.strokeColor = strokeColor;
        binding.incomingCallComponent.setStrokeColor(strokeColor);
    }

    /**
     * Retrieves the stroke color as a {@code ColorStateList} for the call card
     * border.
     *
     * @return the stroke color as a {@code ColorStateList}.
     */
    public ColorStateList getStrokeColorStateList() {
        return ColorStateList.valueOf(strokeColor);
    }

    /**
     * disable sound for incoming calls
     *
     * @param disableSoundForCalls
     */
    public void disableSoundForCalls(boolean disableSoundForCalls) {
        this.disableSoundForCalls = disableSoundForCalls;
    }

    public int getAcceptButtonTextColor() {
        return acceptButtonTextColor;
    }

    /**
     * Retrieves the text color for the accept call button.
     *
     * @return the color of the accept call button text.
     */
    public void setAcceptButtonTextColor(@ColorInt int acceptButtonTextColor) {
        this.acceptButtonTextColor = acceptButtonTextColor;
        binding.acceptButton.setTextColor(acceptButtonTextColor);
    }

    /**
     * Retrieves the text color for the reject call button.
     *
     * @return the color of the reject call button text.
     */
    public int getRejectButtonTextColor() {
        return rejectButtonTextColor;
    }

    /**
     * Retrieves the text color for the reject call button.
     *
     * @return the color of the reject call button text.
     */
    public void setRejectButtonTextColor(@ColorInt int rejectButtonTextColor) {
        this.rejectButtonTextColor = rejectButtonTextColor;
        binding.declineButton.setTextColor(rejectButtonTextColor);
    }

    /**
     * Retrieves the text appearance style resource for the accept call button.
     *
     * @return the resource ID of the accept call button text appearance.
     */
    public int getAcceptButtonTextAppearance() {
        return acceptButtonTextAppearance;
    }

    /**
     * Sets the text appearance style for the accept call button text view.
     *
     * @param acceptButtonTextAppearance the resource ID of the text appearance style.
     */
    public void setAcceptButtonTextAppearance(@StyleRes int acceptButtonTextAppearance) {
        this.acceptButtonTextAppearance = acceptButtonTextAppearance;
        binding.acceptButton.setTextAppearance(acceptButtonTextAppearance);
    }

    /**
     * Retrieves the text appearance style resource for the reject call button.
     *
     * @return the resource ID of the reject call button text appearance.
     */
    public int getRejectButtonTextAppearance() {
        return rejectButtonTextAppearance;
    }

    /**
     * Sets the text appearance style for the reject call button text view.
     *
     * @param rejectButtonTextAppearance the resource ID of the text appearance style.
     */
    public void setRejectButtonTextAppearance(@StyleRes int rejectButtonTextAppearance) {
        this.rejectButtonTextAppearance = rejectButtonTextAppearance;
        binding.declineButton.setTextAppearance(rejectButtonTextAppearance);
    }

    /**
     * Retrieves the {@code IncomingCallViewModel} associated with this component.
     *
     * @return the {@code IncomingCallViewModel} instance.
     */
    public IncomingCallViewModel getViewModel() {
        return viewModel;
    }

    /**
     * Retrieves the {@code OnError} callback for handling errors.
     *
     * @return the {@code OnError} instance.
     */
    public OnError getOnError() {
        return onError;
    }

    /**
     * Sets the {@code OnError} callback for handling errors.
     *
     * @param onError the {@code OnError} instance to set.
     */
    public void setOnError(OnError onError) {
        this.onError = onError;
    }

    public Call getCall() {
        return call;
    }

    /**
     * Sets the call object for this component.
     *
     * @param call the call object to set.
     */
    public void setCall(@NonNull Call call) {
        this.call = call;
        setCallerInfo();
        binding.callTypeIcon.setImageDrawable(call.getType().equals(CometChatConstants.CALL_TYPE_AUDIO) ? voiceCallIcon : videoCallIcon);
        binding.callType.setText(String.format(getString(getContext(), R.string.cometchat_incoming_call_type),
                                               call
                                                   .getType()
                                                   .equals(CometChatConstants.CALL_TYPE_AUDIO) ? getContext().getString(R.string.cometchat_incoming_call_audio) :
                                                   getContext().getString(R.string.cometchat_incoming_call_video)));
    }

    private void setCallerInfo() {
        User callUser = (User) call.getCallInitiator();
        binding.callerName.setText(callUser.getName());
        binding.callType.setText(String.format(getContext().getString(R.string.cometchat_incoming_call_type), call.getType()));
        binding.callerAvatar.setAvatar(callUser.getName(), callUser.getAvatar());
        binding.callTypeIcon.setImageResource(call
                                                  .getType()
                                                  .equals(CometChatConstants.CALL_TYPE_AUDIO) ? com.cometchat.chatuikit.R.drawable.cometchat_ic_call_voice : com.cometchat.chatuikit.R.drawable.cometchat_ic_call_video);
    }

    public CometChatCalls.CallSettingsBuilder getCallSettingsBuilder() {
        return callSettingsBuilder;
    }

    /**
     * Sets the call settings builder for this component.
     *
     * @param callSettingsBuilder the call settings builder to set.
     */
    public void setCallSettingsBuilder(CometChatCalls.CallSettingsBuilder callSettingsBuilder) {
        this.callSettingsBuilder = callSettingsBuilder;
    }

    /**
     * Retrieves the text color for the call title.
     *
     * @return the color of the title text.
     */
    public int getTitleTextColor() {
        return titleTextColor;
    }

    /**
     * Sets the text color for the title text view.
     *
     * @param titleTextColor the color to set as the title text color.
     */
    public void setTitleTextColor(@ColorInt int titleTextColor) {
        this.titleTextColor = titleTextColor;
        binding.callerName.setTextColor(titleTextColor);
    }

    /**
     * Retrieves the text color for the call subtitle.
     *
     * @return the color of the subtitle text.
     */
    public int getSubtitleTextColor() {
        return subtitleTextColor;
    }

    /**
     * Sets the text color for the subtitle text view.
     *
     * @param color the color to set as the subtitle text color.
     */
    public void setSubtitleTextColor(@ColorInt int color) {
        this.subtitleTextColor = color;
        binding.callType.setTextColor(color);
    }

    /**
     * Retrieves the text appearance style resource for the title text.
     *
     * @return the resource ID of the title text appearance.
     */
    public int getTitleTextAppearance() {
        return titleTextAppearance;
    }

    /**
     * Sets the text appearance style for the title text view.
     *
     * @param titleTextAppearance the resource ID of the text appearance style.
     */
    public void setTitleTextAppearance(@StyleRes int titleTextAppearance) {
        this.titleTextAppearance = titleTextAppearance;
        binding.callerName.setTextAppearance(titleTextAppearance);
    }

    /**
     * Retrieves the text appearance style resource for the subtitle text.
     *
     * @return the resource ID of the subtitle text appearance.
     */
    public int getSubtitleTextAppearance() {
        return subtitleTextAppearance;
    }

    /**
     * Sets the text appearance style for the subtitle text view.
     *
     * @param appearance the resource ID of the text appearance style.
     */
    public void setSubtitleTextAppearance(@StyleRes int appearance) {
        this.subtitleTextAppearance = appearance;
        binding.callType.setTextAppearance(appearance);
    }

    /**
     * Retrieves the tint color applied to the call type icon.
     *
     * @return the tint color of the icon.
     */
    public int getIconTint() {
        return iconTint;
    }

    /**
     * Sets the tint color for the call type icon.
     *
     * @param iconTint the color to set as the icon tint.
     */
    public void setIconTint(@ColorInt int iconTint) {
        this.iconTint = iconTint;
        binding.callTypeIcon.setColorFilter(iconTint);
    }

    /**
     * Retrieves the drawable icon for voice calls.
     *
     * @return the {@code Drawable} for the voice call icon.
     */
    public Drawable getVoiceCallIcon() {
        return voiceCallIcon;
    }

    /**
     * Sets the icon for voice call functionality.
     *
     * @param voiceCallIcon the drawable to set as the voice call icon.
     */
    public void setVoiceCallIcon(Drawable voiceCallIcon) {
        this.voiceCallIcon = voiceCallIcon;
    }

    /**
     * Retrieves the drawable icon for video calls.
     *
     * @return the {@code Drawable} for the video call icon.
     */
    public Drawable getVideoCallIcon() {
        return videoCallIcon;
    }

    /**
     * Sets the icon for video call functionality.
     *
     * @param videoCallIcon the drawable to set as the video call icon.
     */
    public void setVideoCallIcon(Drawable videoCallIcon) {
        this.videoCallIcon = videoCallIcon;
    }

    /**
     * Retrieves the style resource ID for the avatar.
     *
     * @return the avatar style resource ID.
     */
    public int getAvatarStyle() {
        return avatarStyle;
    }

    /**
     * Sets the style resource for the avatar view.
     *
     * @param avatarStyle the resource ID of the style to apply to the avatar.
     */
    public void setAvatarStyle(@StyleRes int avatarStyle) {
        this.avatarStyle = avatarStyle;
        binding.callerAvatar.setStyle(avatarStyle);
    }

    /**
     * Retrieves the background color for the reject call button.
     *
     * @return the background color of the reject call button.
     */
    public int getRejectCallButtonBackgroundColor() {
        return rejectCallButtonBackgroundColor;
    }

    /**
     * Sets the background color for the reject call button.
     *
     * @param rejectCallButtonBackgroundColor the color to set as the reject call button background color.
     */
    public void setRejectCallButtonBackgroundColor(@ColorInt int rejectCallButtonBackgroundColor) {
        this.rejectCallButtonBackgroundColor = rejectCallButtonBackgroundColor;
        binding.declineButton.setBackgroundColor(rejectCallButtonBackgroundColor);
    }    /**
     * Sets the stroke width for this component.
     *
     * @param strokeWidth the width in pixels to set for the stroke.
     */
    public void setStrokeWidth(@Dimension int strokeWidth) {
        this.strokeWidth = strokeWidth;
        binding.incomingCallComponent.setStrokeWidth(strokeWidth);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        viewModel.removeListeners();
        pauseSound();
    }

    /**
     * Retrieves the background color for the accept call button.
     *
     * @return the background color of the accept call button.
     */
    public int getAcceptCallButtonBackgroundColor() {
        return acceptCallButtonBackgroundColor;
    }

    /**
     * Sets the background color for the accept call button.
     *
     * @param acceptCallButtonBackgroundColor the color to set as the accept call button background color.
     */
    public void setAcceptCallButtonBackgroundColor(@ColorInt int acceptCallButtonBackgroundColor) {
        this.acceptCallButtonBackgroundColor = acceptCallButtonBackgroundColor;
        binding.acceptButton.setBackgroundColor(acceptCallButtonBackgroundColor);
    }

    /**
     * Retrieves the background color of the call card.
     *
     * @return the background color of the call card.
     */
    public int getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the background color for this component.
     *
     * @param backgroundColor the color to set as the background color.
     */
    @Override
    public void setBackgroundColor(@ColorInt int backgroundColor) {
        this.backgroundColor = backgroundColor;
        binding.incomingCallComponent.setCardBackgroundColor(backgroundColor);
    }

    /**
     * Retrieves the corner radius for the call card.
     *
     * @return the corner radius of the card in dimension units.
     */
    public float getCornerRadius() {
        return cornerRadius;
    }

    /**
     * Sets the corner radius for this component.
     *
     * @param cornerRadius the radius in pixels to set for the corners.
     */
    public void setCornerRadius(@Dimension int cornerRadius) {
        this.cornerRadius = cornerRadius;
        binding.incomingCallComponent.setRadius(cornerRadius);
    }

    /**
     * Retrieves the style resource ID applied to the call card.
     *
     * @return the style resource ID.
     */
    public int getStyle() {
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
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(style, R.styleable.CometChatIncomingCall);
            extractAttributesAndApplyDefaults(typedArray);
        }
    }

    /**
     * Retrieves the onAcceptClick listener.
     *
     * @return the onAcceptClick listener.
     */
    public OnClick getOnAcceptClick() {
        return onAcceptClick;
    }

    /**
     * set onAcceptClick listener to override default functionality when accept button is click
     *
     * @param onAcceptClick
     */
    public void setOnAcceptClick(OnClick onAcceptClick) {
        this.onAcceptClick = onAcceptClick;
    }

    /**
     * Retrieves the onRejectClick listener.
     *
     * @return the onRejectClick listener.
     */
    public OnClick getOnRejectClick() {
        return onRejectClick;
    }

    /**
     * set onRejectClick listener to override default functionality when reject button is click
     *
     * @param onRejectClick
     */
    public void setOnRejectClick(OnClick onRejectClick) {
        this.onRejectClick = onRejectClick;
    }

    /**
     * Retrieves the disable sound for incoming calls flag.
     *
     * @return boolean value indicating whether sound is disabled for incoming calls.
     */
    public boolean isDisableSoundForCalls() {
        return disableSoundForCalls;
    }

    /**
     * Retrieves the stroke width for the call card border.
     *
     * @return the width of the stroke in pixels.
     */
    public int getCustomSoundForCalls() {
        return customSoundForCalls;
    }

    /**
     * set custom sound for incoming calls
     *
     * @param customSoundForCalls
     */
    public void setCustomSoundForCalls(@RawRes int customSoundForCalls) {
        this.customSoundForCalls = customSoundForCalls;
    }

    /**
     * Retrieves the stroke color for the call card border.
     *
     * @return the color of the stroke.
     */
    public View getItemView() {
        return itemView;
    }

    /**
     * Sets the item view for this component.
     *
     * @param itemView the view to set as the item view.
     */
    public void setItemView(View itemView) {
        this.itemView = itemView;
        Utils.handleView(binding.itemView, itemView, true);
    }

    /**
     * Retrieves the leading view for this component.
     *
     * @return the leading view.
     */
    public View getLeadingView() {
        return leadingView;
    }

    /**
     * Sets the leading view for this component.
     *
     * @param leadingView the view to set as the leading view.
     */
    public void setLeadingView(View leadingView) {
        this.leadingView = leadingView;
        Utils.handleView(binding.leadingView, leadingView, true);
    }

    /**
     * Retrieves the title view for this component.
     *
     * @return the title view.
     */
    public View getTitleView() {
        return titleView;
    }

    /**
     * Sets the title view for this component.
     *
     * @param titleView the view to set as the title view.
     */
    public void setTitleView(View titleView) {
        this.titleView = titleView;
        Utils.handleView(binding.titleContainer, titleView, true);
    }

    /**
     * Retrieves the subtitle view for this component.
     *
     * @return the subtitle view.
     */
    public View getSubtitleView() {
        return subtitleView;
    }

    /**
     * Sets the subtitle view for this component.
     *
     * @param subtitleView the view to set as the subtitle view.
     */
    public void setSubtitleView(View subtitleView) {
        this.subtitleView = subtitleView;
        Utils.handleView(binding.subtitleContainer, subtitleView, true);
    }

    /**
     * Retrieves the trailing view for this component.
     *
     * @return the trailing view.
     */
    public View getTrailingView() {
        return trailingView;
    }

    /**
     * Sets the trailing view for this component.
     *
     * @param trailingView the view to set as the trailing view.
     */
    public void setTrailingView(View trailingView) {
        this.trailingView = trailingView;
        Utils.handleView(binding.trailingView, trailingView, true);
    }

    public CometchatIncomingCallComponentBinding getBinding() {
        return binding;
    }

    public void setBinding(CometchatIncomingCallComponentBinding binding) {
        this.binding = binding;
    }




    /**
     * Plays the incoming call sound if sound is not disabled.
     */
    public void playSound() {
        if (!disableSoundForCalls) soundManager.play(Sound.incomingCall, customSoundForCalls);
    }


    /**
     * Retrieves the stroke width for the call card border.
     *
     * @return the width of the stroke in pixels.
     */
    @Override
    public int getStrokeWidth() {
        return strokeWidth;
    }


    /**
     * Adds listeners when the view is attached to the window.
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        viewModel.addListeners();
        if (call != null) playSound();
    }

}
