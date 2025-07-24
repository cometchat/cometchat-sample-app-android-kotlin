package com.cometchat.chatuikit.shared.views.audiobubble;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.StyleRes;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.value.LottieValueCallback;
import com.cometchat.chat.models.Attachment;
import com.cometchat.chat.models.MediaMessage;
import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.shared.interfaces.OnClick;
import com.cometchat.chatuikit.shared.resources.utils.AudioPlayer;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.google.android.material.card.MaterialCardView;

import java.util.Locale;

/**
 * This class represents a custom view for displaying an audio message bubble in
 * a chat application. It extends MaterialCardView for styling purposes and
 * implements MediaPlayer for playing the audio.
 *
 * <p>
 * The view contains a play icon, a pause icon, a title, and a subtitle. It can
 * be customized by setting the text, text color, text font, text appearance,
 * icon color, and icon image.
 *
 * <p>
 * The view also has a stopPlaying() method for stopping the audio when
 * necessary.
 *
 * @see AudioPlayer
 * @see MaterialCardView
 */
public class CometChatAudioBubble extends MaterialCardView {
    private static final String TAG = CometChatAudioBubble.class.getSimpleName();

    private LinearLayout layout;
    private ProgressBar progressBar;
    private ImageView playIconImageView, pauseIconImageView;
    private MaterialCardView buttonCardView;
    private TextView subtitle;
    private LottieAnimationView audioWaveAnimation;

    // Audio player and related properties
    private String audioUrl;
    private AudioPlayer audioPlayer;
    private OnClick onClick;

    // Customizable colors for play, pause, and button tints
    private @ColorInt int playIconTint, pauseIconTint, buttonTint;

    // Customizable icons for play and pause
    private @DrawableRes int playIcon, pauseIcon;
    private Handler handler;
    private Runnable updateRunnable;
    private @StyleRes int style;

    /**
     * Default constructor for creating a CometChatAudioBubble programmatically.
     *
     * @param context The context of the application.
     */
    public CometChatAudioBubble(Context context) {
        this(context, null);
    }

    /**
     * Constructor with attribute set for XML layout usage.
     *
     * @param context The context of the application.
     * @param attrs   The attribute set from the XML layout.
     */
    public CometChatAudioBubble(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.cometchatAudioBubbleStyle);
    }

    /**
     * Constructor with attribute set and default style.
     *
     * @param context      The context of the application.
     * @param attrs        The attribute set from the XML layout.
     * @param defStyleAttr The default style attribute to apply.
     */
    public CometChatAudioBubble(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflateAndInitializeView(attrs, defStyleAttr);
    }

    /**
     * Inflates the layout and initializes the view components.
     *
     * @param attributeSet The attributes to apply from XML.
     * @param defStyleAttr The default style attribute.
     */
    private void inflateAndInitializeView(AttributeSet attributeSet, int defStyleAttr) {
        Utils.initMaterialCard(this); // Initialize MaterialCardView settings
        // UI components
        View view1 = View.inflate(getContext(), R.layout.cometchat_audio_bubble, null);
        audioPlayer = AudioPlayer.getInstance(); // Initialize audio player instance

        // View components initialization
        audioWaveAnimation = view1.findViewById(R.id.animationView);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                                                               getContext()
                                                                                   .getResources()
                                                                                   .getDimensionPixelSize(R.dimen.cometchat_50dp));
        setMargin(0, -45, 0, 0, layoutParams);
        layout = view1.findViewById(R.id.parent);
        subtitle = view1.findViewById(R.id.tv_subtitle);
        playIconImageView = view1.findViewById(R.id.iv_play);
        pauseIconImageView = view1.findViewById(R.id.iv_pause);
        buttonCardView = view1.findViewById(R.id.iv_button);
        progressBar = view1.findViewById(R.id.progress_bar);

        // Initially hide controls
        playIconImageView.setVisibility(GONE);
        progressBar.setVisibility(GONE);
        pauseIconImageView.setVisibility(GONE);
        subtitle.setVisibility(GONE);

        addView(view1);

        // initialize handler
        handler = new Handler();

        // Set play button click listener
        playIconImageView.setOnClickListener(view -> {
            if (onClick != null) {
                onClick.onClick();
            } else {
                startPlaying();
            }
        });

        // Set pause button click listener
        pauseIconImageView.setOnClickListener(view -> stopPlaying());

        // Apply custom style attributes
        applyStyleAttributes(attributeSet, defStyleAttr);
    }

    public void setMargin(int left, int top, int right, int bottom, LinearLayout.LayoutParams layoutParams) {
        layoutParams.topMargin = top;
        layoutParams.bottomMargin = bottom;
        layoutParams.leftMargin = left;
        layoutParams.rightMargin = right;
        audioWaveAnimation.setLayoutParams(layoutParams);
    }

    /**
     * Starts the audio playback and updates the UI states.
     */
    public void startPlaying() {
        if (audioUrl == null || audioUrl.isEmpty()) return;
        playIconImageView.setVisibility(GONE);
        progressBar.setVisibility(VISIBLE);

        // Reset and prepare the audio player
        audioPlayer.reset();

        audioPlayer.setAudioUrl(
            audioUrl,
            mediaPlayer -> {
                progressBar.setVisibility(GONE);
                playIconImageView.setVisibility(GONE);
                playIconImageView.setVisibility(GONE);
                pauseIconImageView.setVisibility(VISIBLE);
                subtitle.setVisibility(VISIBLE);
            },
            mediaPlayer -> stopPlaying()
        );

        // Get the duration of the audio
        final int totalDuration = audioPlayer.getMediaPlayer().getDuration(); // Assuming audioPlayer provides this

        // Start playing audio
        audioPlayer.start();

        // Start updating the subtitle text with the current position
        if (updateRunnable != null) {
            handler.removeCallbacks(updateRunnable);
        }
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                if (audioPlayer.isPlaying()) {
                    if (!audioWaveAnimation.isAnimating()) audioWaveAnimation.playAnimation();

                    int currentPosition = audioPlayer.getMediaPlayer().getCurrentPosition(); // Assuming audioPlayer
                    // provides this method

                    // Update subtitle text immediately
                    subtitle.setText(formatTime(currentPosition) + "/" + formatTime(totalDuration));

                    // Continue updating until the audio ends
                    if (currentPosition < totalDuration) {
                        // Schedule the next update after a short interval (200 ms)
                        handler.postDelayed(this, 200);
                    } else {
                        // Ensure the final subtitle shows the total duration
                        subtitle.setText(formatTime(totalDuration) + "/" + formatTime(totalDuration));
                    }
                }
            }
        };
        handler.post(updateRunnable); // Post immediately to update at the start
    }

    /**
     * Stops the audio playback, resets UI states.
     */
    public void stopPlaying() {
        audioPlayer.stop();
        playIconImageView.setVisibility(VISIBLE);
        pauseIconImageView.setVisibility(GONE);
        audioWaveAnimation.pauseAnimation();
        handler.removeCallbacks(updateRunnable);
    }

    /**
     * Apply custom style attributes to the view.
     *
     * @param attrs        The attribute set to apply.
     * @param defStyleAttr The default style attribute.
     */
    private void applyStyleAttributes(AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatAudioBubble, defStyleAttr, 0);
        int styleResId = typedArray.getResourceId(R.styleable.CometChatAudioBubble_cometchatAudioBubbleStyle, 0);
        // Apply default style if defined
        TypedArray finalTypedArray = getContext()
            .getTheme()
            .obtainStyledAttributes(attrs, R.styleable.CometChatAudioBubble, defStyleAttr, styleResId);
        extractAttributesAndApplyDefaults(finalTypedArray);
    }

    private String formatTime(int millis) {
        int seconds = (millis / 1000) % 60;
        int minutes = (millis / (1000 * 60)) % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    /**
     * Extract attributes from the typed array and apply them to the view.
     *
     * @param typedArray The array of attributes to extract and apply.
     */
    private void extractAttributesAndApplyDefaults(TypedArray typedArray) {
        try {
            setPlayIconTint(typedArray.getColor(R.styleable.CometChatAudioBubble_cometchatAudioBubblePlayIconTint,
                                                CometChatTheme.getPrimaryColor(getContext())));
            setPauseIconTint(typedArray.getColor(R.styleable.CometChatAudioBubble_cometchatAudioBubblePauseIconTint,
                                                 CometChatTheme.getPrimaryColor(getContext())));
            setButtonTint(typedArray.getColor(R.styleable.CometChatAudioBubble_cometchatAudioBubbleButtonBackgroundColor,
                                              CometChatTheme.getColorWhite(getContext())));
            setAudioWaveColor(typedArray.getColor(R.styleable.CometChatAudioBubble_cometchatAudioBubbleAudioWaveColor,
                                                  CometChatTheme.getPrimaryColor(getContext())));
            setSubtitleTextAppearance(typedArray.getResourceId(R.styleable.CometChatAudioBubble_cometchatAudioBubbleSubtitleTextAppearance, 0));
            setSubtitleTextColor(typedArray.getColor(R.styleable.CometChatAudioBubble_cometchatAudioBubbleSubtitleTextColor, 0));
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Sets the color for the audio wave animation.
     *
     * @param color The color to apply to the audio wave.
     */
    private void setAudioWaveColor(@ColorInt int color) {
        audioWaveAnimation.addValueCallback(new KeyPath("**"),
                                            // Target the layer for color change
                                            LottieProperty.COLOR_FILTER,
                                            new LottieValueCallback<>(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)));
    }

    /**
     * Sets the appearance of the subtitle text.
     *
     * @param appearance The resource ID of the text appearance to apply.
     */
    public void setSubtitleTextAppearance(@StyleRes int appearance) {
        if (appearance != 0) {
            subtitle.setTextAppearance(appearance);
        }
    }

    /**
     * Sets the color of the subtitle text.
     *
     * @param color The color to apply to the subtitle text.
     */
    public void setSubtitleTextColor(@ColorInt int color) {
        if (color != 0) {
            subtitle.setTextColor(color);
        }
    }

    public OnClick getOnClick() {
        return onClick;
    }

    /**
     * Sets an OnClick listener for the AudioBubble view. It will be triggered when
     * user clicks on a play button
     *
     * @param onClick the OnClick listener to be set
     */
    public void setOnClick(OnClick onClick) {
        this.onClick = onClick;
    }

    public LinearLayout getView() {
        return layout;
    }

    public TextView getSubtitle() {
        return subtitle;
    }

    public @DrawableRes int getPauseIcon() {
        return pauseIcon;
    }

    /**
     * Sets the pause icon image resource for the media player.
     *
     * @param icon the resource ID of the pause icon image to set
     */
    public void setPauseIcon(@DrawableRes int icon) {
        this.pauseIcon = icon;
        pauseIconImageView.setImageResource(icon);
    }

    public @DrawableRes int getPlayIcon() {
        return playIcon;
    }

    /**
     * Sets the play icon of the media player.
     *
     * @param icon the drawable resource ID of the play icon.
     */
    public void setPlayIcon(@DrawableRes int icon) {
        this.playIcon = icon;
        playIconImageView.setImageResource(icon);
    }

    // Getters for testing or direct access if needed

    public int getPlayIconTint() {
        return playIconTint;
    }

    /**
     * Sets the tint color for the play icon.
     *
     * @param color The color to apply to the play icon.
     */
    public void setPlayIconTint(@ColorInt int color) {
        this.playIconTint = color;
        playIconImageView.setImageTintList(ColorStateList.valueOf(color));
        progressBar.getIndeterminateDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    public int getPauseIconTint() {
        return pauseIconTint;
    }

    /**
     * Sets the tint color for the pause icon.
     *
     * @param color The color to apply to the pause icon.
     */
    public void setPauseIconTint(@ColorInt int color) {
        this.pauseIconTint = color;
        pauseIconImageView.setImageTintList(ColorStateList.valueOf(color));
    }

    public int getButtonTint() {
        return buttonTint;
    }

    /**
     * Sets the background color of the button.
     *
     * @param color The color to apply to the button background.
     */
    public void setButtonTint(@ColorInt int color) {
        this.buttonTint = color;
        buttonCardView.setCardBackgroundColor(ColorStateList.valueOf(color));
    }

    public @StyleRes int getStyle() {
        return style;
    }

    /**
     * Sets the style for the audio bubble.
     *
     * @param style The resource ID of the style to apply.
     */
    public void setStyle(@StyleRes int style) {
        this.style = style;
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(style, R.styleable.CometChatAudioBubble);
        extractAttributesAndApplyDefaults(typedArray);
    }

    public void setMessage(MediaMessage mediaMessage) {
        Attachment attachment = mediaMessage.getAttachment();
        if (attachment != null) {
            int size = attachment.getFileSize();
            setAudioUrl(attachment.getFileUrl(), Utils.getFileSize(size));
        } else {
            setAudioUrl(null, Utils.getFileSize((int) mediaMessage.getFile().length()));
        }
    }

    /**
     * Sets the audio URL and corresponding title and subtitle texts.
     *
     * @param audioUrl     The URL of the audio file to be played.
     * @param subtitleText The subtitle text to be displayed.
     */
    public void setAudioUrl(String audioUrl, String subtitleText) {
        if (audioUrl != null && !audioUrl.isEmpty()) {
            this.audioUrl = audioUrl;
            playIconImageView.setEnabled(true);
        } else {
            playIconImageView.setEnabled(false);
        }
        playIconImageView.setVisibility(VISIBLE);
        setSubtitleText(subtitleText);
    }

    /**
     * Sets the subtitle text to be displayed.
     *
     * @param text The text to display as the subtitle.
     */
    public void setSubtitleText(String text) {
        if (text != null && !text.isEmpty()) {
            subtitle.setVisibility(VISIBLE);
            subtitle.setText(text);
        }
    }

    public boolean isPlaying() {
        return audioPlayer.isPlaying();
    }

    public View getPlayIconImageView() {
        return playIconImageView;
    }

    public View getPauseIconImageView() {
        return pauseIconImageView;
    }

}
