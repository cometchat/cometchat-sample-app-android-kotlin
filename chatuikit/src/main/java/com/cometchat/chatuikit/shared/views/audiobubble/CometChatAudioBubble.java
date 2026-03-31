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
import com.cometchat.chatuikit.shared.views.waveform.AudioWaveformExtractor;
import com.cometchat.chatuikit.shared.views.waveform.AudioWaveformVisualizer;
import com.cometchat.chatuikit.shared.views.waveform.WaveformCache;
import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.util.List;
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
    private LottieAnimationView lottieAnimationView;
    private AudioWaveformVisualizer waveformVisualizer;

    // Audio player and related properties
    private String audioUrl;
    private AudioPlayer audioPlayer;
    private OnClick onClick;
    
    // Local file for sent messages (to avoid buffering)
    private File localFile;
    
    // Message ID for caching
    private long messageId = 0;
    
    // Flag to track if waveform extraction is in progress
    private boolean isExtractingWaveform = false;
    
    // Track if THIS bubble's audio is currently paused
    private boolean isThisBubblePaused = false;
    // Track the current audio URL being played by the singleton
    private static String currentlyPlayingUrl = null;

    // Customizable colors for play, pause, and button tints
    private @ColorInt int playIconTint, pauseIconTint, buttonTint;
    private @ColorInt int audioWaveColor;
    private @ColorInt int waveformPlayedColor, waveformUnplayedColor;

    // Customizable icons for play and pause
    private @DrawableRes int playIcon, pauseIcon;
    private Handler handler;
    private Runnable updateRunnable;
    private @StyleRes int style;
    
    // Waveform amplitude data
    private List<Float> waveformAmplitudes;

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
        lottieAnimationView = view1.findViewById(R.id.lottie_animation);
        // Set larger height with negative top margin for overflow effect (like v5)
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                                                               getContext()
                                                                                   .getResources()
                                                                                   .getDimensionPixelSize(R.dimen.cometchat_50dp));
        setLottieMargin(0, -45, 0, 0, layoutParams);
        // Initialize waveform visualizer
        waveformVisualizer = view1.findViewById(R.id.waveform_visualizer);
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
        
        // Initialize waveform with flat/uniform amplitudes (straight lines)
        waveformAmplitudes = AudioWaveformVisualizer.generateFlatAmplitudes();
        waveformVisualizer.setAmplitudes(waveformAmplitudes);
        waveformVisualizer.setPlaybackProgress(0f);
        waveformVisualizer.setAllowSeeking(true);
        
        // Set up seek listener for waveform
        waveformVisualizer.setOnSeekListener(progress -> {
            if (audioPlayer != null && audioPlayer.getMediaPlayer() != null) {
                int duration = audioPlayer.getMediaPlayer().getDuration();
                int seekPosition = (int) (progress * duration);
                audioPlayer.getMediaPlayer().seekTo(seekPosition);
                waveformVisualizer.setPlaybackProgress(progress);
            }
        });

        addView(view1);

        // initialize handler
        handler = new Handler();

        // Set play button click listener
        playIconImageView.setOnClickListener(view -> {
            if (onClick != null) {
                onClick.onClick();
            } else {
                // Check if THIS bubble's audio is paused - if so, resume instead of starting fresh
                String currentUrl = localFile != null && localFile.exists() ? localFile.getAbsolutePath() : audioUrl;
                if (isThisBubblePaused && currentUrl != null && currentUrl.equals(currentlyPlayingUrl) && audioPlayer.isPaused()) {
                    resumePlaying();
                } else {
                    startPlaying();
                }
            }
        });

        // Set pause button click listener - pause instead of stop
        pauseIconImageView.setOnClickListener(view -> pausePlaying());

        // Apply custom style attributes
        applyStyleAttributes(attributeSet, defStyleAttr);
    }
    
    /**
     * Sets the margin for the Lottie animation view.
     * Used to create overflow effect for the waveform animation.
     */
    public void setLottieMargin(int left, int top, int right, int bottom, LinearLayout.LayoutParams layoutParams) {
        layoutParams.topMargin = top;
        layoutParams.bottomMargin = bottom;
        layoutParams.leftMargin = left;
        layoutParams.rightMargin = right;
        lottieAnimationView.setLayoutParams(layoutParams);
    }
    
    /**
     * Starts the audio playback and updates the UI states.
     * Prioritizes local file playback to avoid buffering for sent messages.
     */
    public void startPlaying() {
        // Reset paused state when starting fresh
        isThisBubblePaused = false;
        
        // Check if we have a local file (sent message) - play directly without buffering
        if (localFile != null && localFile.exists()) {
            currentlyPlayingUrl = localFile.getAbsolutePath();
            startPlayingFromLocalFile();
            return;
        }
        
        if (audioUrl == null || audioUrl.isEmpty()) return;
        
        // Track the currently playing URL
        currentlyPlayingUrl = audioUrl;
        
        playIconImageView.setVisibility(GONE);
        progressBar.setVisibility(VISIBLE);
        
        // Check if waveform is already cached
        if (messageId > 0 && WaveformCache.containsByMessageId(messageId)) {
            // Waveform already extracted, play immediately
            startPlayingWithWaveform();
            return;
        }
        
        // For received messages, extract waveform first, then play
        isExtractingWaveform = true;
        final long msgId = this.messageId;
        
        AudioWaveformExtractor.extractWaveform(getContext(), audioUrl, 
            new AudioWaveformExtractor.WaveformExtractionCallback() {
                @Override
                public void onSuccess(List<Float> amplitudes) {
                    // Cache the result
                    if (msgId > 0) {
                        WaveformCache.putByMessageId(msgId, amplitudes);
                    }
                    handler.post(() -> {
                        waveformAmplitudes = amplitudes;
                        waveformVisualizer.setAmplitudes(amplitudes);
                        isExtractingWaveform = false;
                        // Now start playing
                        startPlayingWithWaveform();
                    });
                }

                @Override
                public void onError(String error) {
                    android.util.Log.e(TAG, "Waveform extraction error: " + error);
                    handler.post(() -> {
                        isExtractingWaveform = false;
                        // Play anyway with flat waveform
                        startPlayingWithWaveform();
                    });
                }
            });
    }
    
    /**
     * Starts playing audio after waveform is ready.
     * Called after waveform extraction completes.
     */
    private void startPlayingWithWaveform() {
        if (audioUrl == null || audioUrl.isEmpty()) return;
        
        // Reset and prepare the audio player
        audioPlayer.reset();
        
        // Set context for secure media support
        audioPlayer.setContext(getContext());
        
        // Reset waveform progress
        waveformVisualizer.setPlaybackProgress(0f);
        waveformVisualizer.setIsPlaying(true);

        audioPlayer.setAudioUrl(
            audioUrl,
            mediaPlayer -> {
                progressBar.setVisibility(GONE);
                playIconImageView.setVisibility(GONE);
                pauseIconImageView.setVisibility(VISIBLE);
                subtitle.setVisibility(VISIBLE);
                
                // Start Lottie animation
                lottieAnimationView.playAnimation();
                
                // Get the duration after audio is prepared
                final int totalDuration = mediaPlayer.getDuration();
                
                // Start playing audio
                audioPlayer.start();

                // Start updating the subtitle text
                startProgressUpdater(totalDuration);
            },
            mediaPlayer -> stopPlaying()
        );
    }

    
    /**
     * Starts playing from local file - no buffering needed.
     * Used for sent voice recordings.
     * Waveform should already be set from metadata or cache, so just play.
     */
    private void startPlayingFromLocalFile() {
        playIconImageView.setVisibility(GONE);
        progressBar.setVisibility(VISIBLE);
        
        // Reset waveform progress
        waveformVisualizer.setPlaybackProgress(0f);
        waveformVisualizer.setIsPlaying(true);
        
        String filePath = localFile.getAbsolutePath();
        
        // Play directly from local file (waveform should already be set)
        audioPlayer.playFromLocalFile(
            filePath,
            mediaPlayer -> {
                progressBar.setVisibility(GONE);
                playIconImageView.setVisibility(GONE);
                pauseIconImageView.setVisibility(VISIBLE);
                subtitle.setVisibility(VISIBLE);
                
                // Start Lottie animation
                lottieAnimationView.playAnimation();
                
                final int totalDuration = mediaPlayer.getDuration();
                audioPlayer.start();
                startProgressUpdater(totalDuration);
            },
            mediaPlayer -> stopPlaying()
        );
    }
    
    /**
     * Starts the progress updater runnable.
     */
    private void startProgressUpdater(final int totalDuration) {
        if (updateRunnable != null) {
            handler.removeCallbacks(updateRunnable);
        }
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                if (audioPlayer.isPlaying()) {
                    int currentPosition = audioPlayer.getMediaPlayer().getCurrentPosition();
                    int duration = audioPlayer.getMediaPlayer().getDuration();

                    // Update waveform progress - use actual duration from MediaPlayer for accuracy
                    if (duration > 0) {
                        float progress = (float) currentPosition / duration;
                        waveformVisualizer.setPlaybackProgress(progress);
                    }

                    // Update subtitle text
                    subtitle.setText(formatTime(currentPosition) + "/" + formatTime(totalDuration));

                    // Continue updating until the audio ends
                    if (currentPosition < totalDuration) {
                        handler.postDelayed(this, 100);
                    } else {
                        subtitle.setText(formatTime(totalDuration) + "/" + formatTime(totalDuration));
                        waveformVisualizer.setPlaybackProgress(1f);
                    }
                }
            }
        };
        handler.post(updateRunnable);
    }
    
    /**
     * Pauses the audio playback without resetting.
     * Allows resuming from the same position.
     */
    public void pausePlaying() {
        audioPlayer.pause();
        isThisBubblePaused = true;
        playIconImageView.setVisibility(VISIBLE);
        pauseIconImageView.setVisibility(GONE);
        // Pause Lottie animation
        lottieAnimationView.pauseAnimation();
        waveformVisualizer.setIsPlaying(false);
        handler.removeCallbacks(updateRunnable);
    }
    
    /**
     * Resumes audio playback from the paused position.
     */
    public void resumePlaying() {
        audioPlayer.resume();
        isThisBubblePaused = false;
        playIconImageView.setVisibility(GONE);
        pauseIconImageView.setVisibility(VISIBLE);
        // Resume Lottie animation
        lottieAnimationView.resumeAnimation();
        waveformVisualizer.setIsPlaying(true);
        
        final int totalDuration = audioPlayer.getMediaPlayer().getDuration();
        startProgressUpdater(totalDuration);
    }

    /**
     * Stops the audio playback, resets UI states.
     */
    public void stopPlaying() {
        audioPlayer.stop();
        isThisBubblePaused = false;
        currentlyPlayingUrl = null;
        playIconImageView.setVisibility(VISIBLE);
        pauseIconImageView.setVisibility(GONE);
        // Stop Lottie animation
        lottieAnimationView.cancelAnimation();
        lottieAnimationView.setProgress(0f);
        waveformVisualizer.setIsPlaying(false);
        waveformVisualizer.setPlaybackProgress(0f);
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
     * Sets the color for the audio waveform/animation.
     *
     * @param color The color to apply to the audio wave animation.
     */
    private void setAudioWaveColor(@ColorInt int color) {
        this.audioWaveColor = color;
        // Apply color to Lottie animation using PorterDuffColorFilter (like v5)
        lottieAnimationView.addValueCallback(new KeyPath("**"),
                                            LottieProperty.COLOR_FILTER,
                                            new LottieValueCallback<>(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)));
        // Apply color to waveform visualizer
        this.waveformPlayedColor = color;
        waveformVisualizer.setPlayedBarColor(color);
    }
    
    /**
     * Sets the color for the unplayed portion of the waveform.
     *
     * @param color The color to apply to the unplayed portion of the waveform.
     */
    public void setWaveformUnplayedColor(@ColorInt int color) {
        this.waveformUnplayedColor = color;
        waveformVisualizer.setUnplayedBarColor(color);
    }
    
    /**
     * Gets the waveform visualizer for additional customization.
     *
     * @return The AudioWaveformVisualizer instance.
     */
    public AudioWaveformVisualizer getWaveformVisualizer() {
        return waveformVisualizer;
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
        // Store message ID and MUID for caching
        this.messageId = mediaMessage.getId();
        String muid = mediaMessage.getMuid();
        
        // Get local file for sent messages (to avoid buffering)
        this.localFile = Utils.getFileFromLocalPath(mediaMessage);
        
        Attachment attachment = mediaMessage.getAttachment();
        if (attachment != null) {
            int size = attachment.getFileSize();
            setAudioUrl(attachment.getFileUrl(), Utils.getFileSize(size));
        } else {
            setAudioUrl(null, Utils.getFileSize((int) mediaMessage.getFile().length()));
        }
        
        // Check cache by message ID first to prevent flicker on rebind
        if (messageId > 0 && WaveformCache.containsByMessageId(messageId)) {
            List<Float> cachedAmplitudes = WaveformCache.getByMessageId(messageId);
            if (cachedAmplitudes != null && !cachedAmplitudes.isEmpty()) {
                waveformAmplitudes = cachedAmplitudes;
                waveformVisualizer.setAmplitudes(cachedAmplitudes);
                return;
            }
        }
        
        // Check cache by MUID (for sent messages before server assigns ID)
        List<Float> muidAmplitudes = WaveformCache.getByMuid(muid);
        if (muidAmplitudes != null && !muidAmplitudes.isEmpty()) {
            waveformAmplitudes = muidAmplitudes;
            waveformVisualizer.setAmplitudes(muidAmplitudes);
            // Also cache by message ID if available
            if (messageId > 0) {
                WaveformCache.putByMessageId(messageId, muidAmplitudes);
                // Clean up MUID cache since we now have message ID
                WaveformCache.removeByMuid(muid);
            }
            return;
        }
        
        // Try to get waveform from message metadata (fallback, should not happen with new flow)
        List<Float> metadataAmplitudes = getWaveformFromMetadata(mediaMessage);
        if (metadataAmplitudes != null && !metadataAmplitudes.isEmpty()) {
            waveformAmplitudes = metadataAmplitudes;
            waveformVisualizer.setAmplitudes(metadataAmplitudes);
            // Cache it
            if (messageId > 0) {
                WaveformCache.putByMessageId(messageId, metadataAmplitudes);
            }
            return;
        }
        
        // For sent messages with local file, extract immediately
        if (localFile != null && localFile.exists()) {
            extractWaveformFromLocalFile();
        }
        // For received messages, keep flat waveform - will extract when play is tapped
    }
    
    /**
     * Extracts waveform amplitudes from message metadata if available.
     * This is used for sent messages recorded with the inline audio recorder.
     */
    private List<Float> getWaveformFromMetadata(MediaMessage mediaMessage) {
        try {
            if (mediaMessage.getMetadata() != null && mediaMessage.getMetadata().has("waveform_amplitudes")) {
                org.json.JSONArray amplitudesArray = mediaMessage.getMetadata().getJSONArray("waveform_amplitudes");
                List<Float> amplitudes = new java.util.ArrayList<>();
                for (int i = 0; i < amplitudesArray.length(); i++) {
                    amplitudes.add((float) amplitudesArray.getDouble(i));
                }
                return amplitudes;
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error reading waveform from metadata: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Extracts waveform from local file for sent messages.
     * Called immediately when message is set.
     */
    private void extractWaveformFromLocalFile() {
        if (localFile == null || !localFile.exists()) return;
        
        String filePath = localFile.getAbsolutePath();
        final long msgId = this.messageId;
        
        AudioWaveformExtractor.extractWaveformFromFile(filePath, 
            new AudioWaveformExtractor.WaveformExtractionCallback() {
                @Override
                public void onSuccess(List<Float> amplitudes) {
                    // Cache the result
                    if (msgId > 0) {
                        WaveformCache.putByMessageId(msgId, amplitudes);
                    }
                    handler.post(() -> {
                        waveformAmplitudes = amplitudes;
                        waveformVisualizer.setAmplitudes(amplitudes);
                    });
                }

                @Override
                public void onError(String error) {
                    android.util.Log.e(TAG, "Waveform extraction error: " + error);
                    // Keep flat waveform on error
                }
            });
    }

    /**
     * Sets the audio URL and corresponding title and subtitle texts.
     * TODO: When waveform visualizer is ready, shows flat waveform initially - dynamic waveform is extracted when audio is played.
     *
     * @param audioUrl     The URL of the audio file to be played.
     * @param subtitleText The subtitle text to be displayed.
     */
    public void setAudioUrl(String audioUrl, String subtitleText) {
        if (audioUrl != null && !audioUrl.isEmpty()) {
            this.audioUrl = audioUrl;
            playIconImageView.setEnabled(true);
            
            // Show flat waveform initially - dynamic waveform will be extracted when played
            waveformAmplitudes = AudioWaveformVisualizer.generateFlatAmplitudes();
            waveformVisualizer.setAmplitudes(waveformAmplitudes);
            waveformVisualizer.setPlaybackProgress(0f);
        } else {
            playIconImageView.setEnabled(false);
            // Use flat waveform for null/empty URLs
            waveformAmplitudes = AudioWaveformVisualizer.generateFlatAmplitudes();
            waveformVisualizer.setAmplitudes(waveformAmplitudes);
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
