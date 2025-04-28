package com.cometchat.chatuikit.shared.views.mediarecorder;

import android.Manifest;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.databinding.CometchatMediarecorderBinding;
import com.cometchat.chatuikit.shared.interfaces.OnClick;
import com.cometchat.chatuikit.shared.permission.CometChatPermissionHandler;
import com.cometchat.chatuikit.shared.permission.builder.PermissionHandlerBuilder;
import com.cometchat.chatuikit.shared.resources.utils.AudioPlayer;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

/**
 * CometChatMediaRecorder is a custom view for recording and playing audio. It
 * provides functionality for recording audio, playing recorded audio, and
 * visualizing the audio recording.
 */
public class CometChatMediaRecorder extends MaterialCardView {
    private static final String TAG = CometChatMediaRecorder.class.getSimpleName();
    private final Handler timerHandler = new Handler();
    private CometchatMediarecorderBinding binding;
    private @ColorInt int backgroundColor;
    private @Dimension int strokeWidth;
    private @ColorInt int strokeColor;
    private @Dimension int cornerRadius;
    private Drawable recordingIcon;
    private @ColorInt int recordingIconTint;
    private @ColorInt int recordingIconBackgroundColor;
    private @StyleRes int textAppearance;
    private @ColorInt int textColor;
    private Drawable deleteIcon;
    private @ColorInt int deleteIconTint;
    private @ColorInt int deleteIconBackgroundColor;
    private @Dimension int deleteIconRadius;
    private @Dimension int deleteIconStrokeWidth;
    private @ColorInt int deleteIconStrokeColor;
    private int deleteIconElevation;
    private Drawable startIcon;
    private @ColorInt int startIconTint;
    private @ColorInt int startIconBackgroundColor;
    private @Dimension int startIconRadius;
    private @Dimension int startIconStrokeWidth;
    private @ColorInt int startIconStrokeColor;
    private int startIconElevation;
    private Drawable pauseIcon;
    private @ColorInt int pauseIconTint;
    private @ColorInt int pauseIconBackgroundColor;
    private @Dimension int pauseIconRadius;
    private @Dimension int pauseIconStrokeWidth;
    private @ColorInt int pauseIconStrokeColor;
    private int pauseIconElevation;
    private Drawable stopIcon;
    private @ColorInt int stopIconTint;
    private @ColorInt int stopIconBackgroundColor;
    private @Dimension int stopIconRadius;
    private @Dimension int stopIconStrokeWidth;
    private @ColorInt int stopIconStrokeColor;
    private int stopIconElevation;
    private Drawable sendIcon;
    private @ColorInt int sendIconTint;
    private @ColorInt int sendIconBackgroundColor;
    private @Dimension int sendIconRadius;
    private @Dimension int sendIconStrokeWidth;
    private @ColorInt int sendIconStrokeColor;
    private int sendIconElevation;
    private Drawable restartIcon;
    private @ColorInt int restartIconTint;
    private @ColorInt int restartIconBackgroundColor;
    private @Dimension int restartIconRadius;
    private @Dimension int restartIconStrokeWidth;
    private @ColorInt int restartIconStrokeColor;
    private int restartIconElevation;
    private @StyleRes int outgoingMessageBubbleStyle;
    private @ColorInt int messageBubbleBackgroundColor;
    private @Dimension int messageBubbleCornerRadius;
    private @Dimension int messageBubbleStrokeWidth;
    private @ColorInt int messageBubbleStrokeColor;
    private Drawable messageBubbleBackgroundDrawable;
    private MediaRecorder recorder;
    private String recordedFilePath;
    private boolean isRecording = false;
    private boolean isAutoPermissionCheck = true;
    private long startTime = 0;
    private long pauseTime = 0;
    private Runnable timerRunnable;

    private String[] permissions;
    private PermissionHandlerBuilder permissionHandlerBuilder;

    private OnClick onClose;
    private OnSubmitClick onSubmit;

    /**
     * Constructs a new CometChatMediaRecorder instance.
     *
     * @param context The context of the application.
     */
    public CometChatMediaRecorder(Context context) {
        this(context, null);
    }

    /**
     * Constructs a new CometChatMediaRecorder instance.
     *
     * @param context The context of the application.
     * @param attrs   The set of attributes associated with the view.
     */
    public CometChatMediaRecorder(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.cometchatMediaRecorderStyle);
    }

    /**
     * Constructs a new CometChatMediaRecorder instance.
     *
     * @param context      The context of the application.
     * @param attrs        The set of attributes associated with the view.
     * @param defStyleAttr The default style for the view.
     */
    public CometChatMediaRecorder(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            inflateAndInitializeView(attrs, defStyleAttr);
        }
    }

    /**
     * Inflates the layout for this view and initializes necessary components.
     *
     * @param attrs        the attribute set containing the view's XML attributes
     * @param defStyleAttr the default style attribute to apply
     */
    private void inflateAndInitializeView(AttributeSet attrs, int defStyleAttr) {
        // Reset the card view to default values
        Utils.initMaterialCard(this);

        // Inflate the layout for this view
        binding = CometchatMediarecorderBinding.inflate(LayoutInflater.from(getContext()), this, true);
        Utils.initMaterialCard(binding.audioRippleEffect);
        binding.audioRippleEffect.setRadius(1000);

        // Initialization
        checkMicrophonePermission();
        setUpAudioManager();

        // Apply style attributes
        applyStyleAttributes(attrs, defStyleAttr);

        // Other initializations
        initClickListeners();
        recordingStateHandler(RecordingState.START);
    }

    /**
     * Checks for microphone permissions required for audio recording. Sets up a
     * permission handler to manage permission requests and initiates recording if
     * granted.
     */
    private void checkMicrophonePermission() {
        permissions = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        permissionHandlerBuilder = CometChatPermissionHandler.withContext(getContext()).withListener((grantedPermission, deniedPermission) -> {
            if (deniedPermission.isEmpty() && !isAutoPermissionCheck) {
                timerRunnable = new Runnable() {
                    @Override
                    public void run() {
                        long elapsedTime = System.currentTimeMillis() - startTime;
                        int seconds = (int) (elapsedTime / 1000);
                        int minutes = seconds / 60;
                        seconds = seconds % 60;
                        binding.tvRecordingTime.setText(String.format(Locale.US, "%02d:%02d", minutes, seconds));
                        timerHandler.postDelayed(this, 500);
                    }
                };
                startRecordingInternally();
            } else {
                Log.e(TAG, "Permission request denied");
            }
        });
    }

    /**
     * Sets up the audio manager to manage audio focus for recording. Requests audio
     * focus if music is currently playing. If audio focus is lost, the recording is
     * deleted.
     */
    private void setUpAudioManager() {
        AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        if (audioManager.isMusicActive()) {
            audioManager.requestAudioFocus(focusChange -> {
            }, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
    }

    /**
     * Initializes click listeners for the various controls in the media recorder.
     * Sets up actions for starting, pausing, resuming, restarting, sending,
     * stopping, and deleting recordings.
     */
    private void initClickListeners() {
        binding.ivCenterStart.setOnClickListener(v -> {
            startRecording();
        });
        binding.ivCenterPause.setOnClickListener(v -> {
            pauseRecording();
        });
        binding.ivCenterResume.setOnClickListener(v -> {
            resumeRecording();
        });
        binding.ivRightRestart.setOnClickListener(v -> {
            startRecording();
        });
        binding.ivCenterSend.setOnClickListener(v -> {
            binding.audioBubble.stopPlaying();
            if (recordedFilePath != null) {
                invokeSendClick(getContext(), new File(recordedFilePath));
            } else {
                invokeSendClick(getContext(), null);
            }
        });
        binding.ivRightStop.setOnClickListener(v -> {
            if (recordedFilePath != null) {
                binding.audioBubble.setAudioUrl(recordedFilePath, Utils.getFileSize(recordedFilePath));
                stopRecording();
            }
        });
        binding.ivLeftDelete.setOnClickListener(v -> {
            deleteRecording();
        });
    }

    /**
     * Applies custom style attributes from XML to the media recorder.
     *
     * @param attrs        the attribute set containing the style attributes
     * @param defStyleAttr the default style attribute to apply
     */
    private void applyStyleAttributes(AttributeSet attrs, int defStyleAttr) {
        TypedArray directAttributes = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatMediaRecorder, defStyleAttr, 0);
        @StyleRes int styleResId = directAttributes.getResourceId(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderStyle, 0);
        directAttributes = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatMediaRecorder, defStyleAttr, styleResId);
        extractAttributesAndApplyDefaults(directAttributes);
    }

    /**
     * Sets the style of the media recorder from a specified style resource.
     *
     * @param style the resource ID of the style to apply
     */
    public void setStyle(@StyleRes int style) {
        if (style != 0) {
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(style, R.styleable.CometChatMediaRecorder);
            extractAttributesAndApplyDefaults(typedArray);
        }
    }

    /**
     * Extracts the attributes and applies the default values if they are not set in
     * the XML.
     *
     * @param typedArray The TypedArray containing the attributes to be extracted.
     */
    private void extractAttributesAndApplyDefaults(TypedArray typedArray) {
        if (typedArray == null) return;
        try {
            // Extract attributes or apply default values
            backgroundColor = typedArray.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderBackgroundColor, Color.TRANSPARENT);
            strokeWidth = typedArray.getDimensionPixelSize(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderStrokeWidth, 0);
            strokeColor = typedArray.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderStrokeColor, Color.TRANSPARENT);
            cornerRadius = typedArray.getDimensionPixelSize(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderCornerRadius, 0);
            recordingIcon = typedArray.getDrawable(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderRecordingIcon);
            recordingIconTint = typedArray.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderRecordingIconTint, 0);
            recordingIconBackgroundColor = typedArray.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderRecordingIconBackgroundColor,
                                                               0);
            textAppearance = typedArray.getResourceId(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderTextAppearance, 0);
            textColor = typedArray.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderTextColor, 0);
            deleteIcon = typedArray.getDrawable(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderDeleteIcon);
            deleteIconTint = typedArray.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderDeleteIconTint, 0);
            deleteIconBackgroundColor = typedArray.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderDeleteIconBackgroundColor, 0);
            deleteIconRadius = typedArray.getDimensionPixelSize(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderDeleteIconRadius, 0);
            deleteIconStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderDeleteIconStrokeWidth,
                                                                     0);
            deleteIconStrokeColor = typedArray.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderDeleteIconStrokeColor, 0);
            deleteIconElevation = typedArray.getDimensionPixelSize(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderDeleteIconElevation, 0);
            startIcon = typedArray.getDrawable(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderStartIcon);
            startIconTint = typedArray.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderStartIconTint, 0);
            startIconBackgroundColor = typedArray.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderStartIconBackgroundColor, 0);
            startIconRadius = typedArray.getDimensionPixelSize(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderStartIconRadius, 0);
            startIconStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderStartIconStrokeWidth, 0);
            startIconStrokeColor = typedArray.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderStartIconStrokeColor, 0);
            startIconElevation = typedArray.getDimensionPixelSize(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderStartIconElevation, 0);
            pauseIcon = typedArray.getDrawable(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderPauseIcon);
            pauseIconTint = typedArray.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderPauseIconTint, 0);
            pauseIconBackgroundColor = typedArray.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderPauseIconBackgroundColor, 0);
            pauseIconRadius = typedArray.getDimensionPixelSize(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderPauseIconRadius, 0);
            pauseIconStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderPauseIconStrokeWidth, 0);
            pauseIconStrokeColor = typedArray.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderPauseIconStrokeColor, 0);
            pauseIconElevation = typedArray.getDimensionPixelSize(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderPauseIconElevation, 0);
            stopIcon = typedArray.getDrawable(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderStopIcon);
            stopIconTint = typedArray.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderStopIconTint, 0);
            stopIconBackgroundColor = typedArray.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderStopIconBackgroundColor, 0);
            stopIconRadius = typedArray.getDimensionPixelSize(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderStopIconRadius, 0);
            stopIconStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderStopIconStrokeWidth, 0);
            stopIconStrokeColor = typedArray.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderStopIconStrokeColor, 0);
            stopIconElevation = typedArray.getDimensionPixelSize(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderStopIconElevation, 0);
            sendIcon = typedArray.getDrawable(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderSendIcon);
            sendIconTint = typedArray.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderSendIconTint, 0);
            sendIconBackgroundColor = typedArray.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderSendIconBackgroundColor, 0);
            sendIconRadius = typedArray.getDimensionPixelSize(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderSendIconRadius, 0);
            sendIconStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderSendIconStrokeWidth, 0);
            sendIconStrokeColor = typedArray.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderSendIconStrokeColor, 0);
            sendIconElevation = typedArray.getDimensionPixelSize(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderSendIconElevation, 0);
            restartIcon = typedArray.getDrawable(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderRestartIcon);
            restartIconTint = typedArray.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderRestartIconTint, 0);
            restartIconBackgroundColor = typedArray.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderRestartIconBackgroundColor, 0);
            restartIconRadius = typedArray.getDimensionPixelSize(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderRestartIconRadius, 0);
            restartIconStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderRestartIconStrokeWidth,
                                                                      0);
            restartIconStrokeColor = typedArray.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderRestartIconStrokeColor, 0);
            restartIconElevation = typedArray.getDimensionPixelSize(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderRestartIconElevation, 0);
            outgoingMessageBubbleStyle = typedArray.getResourceId(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderOutgoingMessageBubbleStyle,
                                                                  0);

            // Update UI
            updateUI();
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Updates the UI elements of the media recorder with the current attribute
     * values. Sets background color, stroke width, stroke color, corner radius, and
     * various icon attributes.
     */
    private void updateUI() {
        setBackgroundColor(backgroundColor);
        setStrokeWidth(strokeWidth);
        setStrokeColor(strokeColor);
        setCornerRadius(cornerRadius);
        setRecordingIcon(recordingIcon);
        setRecordingIconTint(recordingIconTint);
        setRecordingIconBackgroundColor(recordingIconBackgroundColor);
        setTextAppearance(textAppearance);
        setTextColor(textColor);
        setDeleteIconAttributes();
        setStartIconAttributes();
        setPauseIconAttributes();
        setStopIconAttributes();
        setSendIconAttributes();
        setRestartIconAttributes();
        setOutgoingMessageBubbleStyle(outgoingMessageBubbleStyle);
    }

    /**
     * Sets the stroke color of the media recorder.
     *
     * @param strokeColor the stroke color to set
     */
    @Override
    public void setStrokeColor(@ColorInt int strokeColor) {
        this.strokeColor = strokeColor;
        super.setStrokeColor(strokeColor);
    }

    /**
     * Returns the current stroke color of the media recorder.
     *
     * @return the stroke color
     */
    @Override
    public ColorStateList getStrokeColorStateList() {
        return ColorStateList.valueOf(strokeColor);
    }

    /**
     * Sets the attributes for the delete icon based on the defined properties.
     */
    private void setDeleteIconAttributes() {
        setDeleteIcon(deleteIcon);
        setDeleteIconTint(deleteIconTint);
        setDeleteIconBackgroundColor(deleteIconBackgroundColor);
        setDeleteIconRadius(deleteIconRadius);
        setDeleteIconStrokeColor(deleteIconStrokeColor);
        setDeleteIconStrokeWidth(deleteIconStrokeWidth);
        setDeleteIconElevation(deleteIconElevation);
    }

    /**
     * Sets the attributes for the start icon based on the defined properties.
     */
    private void setStartIconAttributes() {
        setStartIcon(startIcon);
        setStartIconTint(startIconTint);
        setStartIconBackgroundColor(startIconBackgroundColor);
        setStartIconRadius(startIconRadius);
        setStartIconStrokeColor(startIconStrokeColor);
        setStartIconStrokeWidth(startIconStrokeWidth);
        setStartIconElevation(startIconElevation);
    }

    /**
     * Sets the attributes for the pause icon based on the defined properties.
     */
    private void setPauseIconAttributes() {
        setPauseIcon(pauseIcon);
        setPauseIconTint(pauseIconTint);
        setPauseIconBackgroundColor(pauseIconBackgroundColor);
        setPauseIconRadius(pauseIconRadius);
        setPauseIconStrokeColor(pauseIconStrokeColor);
        setPauseIconStrokeWidth(pauseIconStrokeWidth);
        setPauseIconElevation(pauseIconElevation);
    }

    /**
     * Sets the attributes for the stop icon based on the defined properties.
     */
    private void setStopIconAttributes() {
        setStopIcon(stopIcon);
        setStopIconTint(stopIconTint);
        setStopIconBackgroundColor(stopIconBackgroundColor);
        setStopIconRadius(stopIconRadius);
        setStopIconStrokeColor(stopIconStrokeColor);
        setStopIconStrokeWidth(stopIconStrokeWidth);
        setStopIconElevation(stopIconElevation);
    }

    /**
     * Sets the attributes for the send icon based on the defined properties.
     */
    private void setSendIconAttributes() {
        setSendIcon(sendIcon);
        setSendIconTint(sendIconTint);
        setSendIconBackgroundColor(sendIconBackgroundColor);
        setSendIconRadius(sendIconRadius);
        setSendIconStrokeColor(sendIconStrokeColor);
        setSendIconStrokeWidth(sendIconStrokeWidth);
        setSendIconElevation(sendIconElevation);
    }

    /**
     * Sets the attributes for the restart icon based on the defined properties.
     */
    private void setRestartIconAttributes() {
        setRestartIcon(restartIcon);
        setRestartIconTint(restartIconTint);
        setRestartIconBackgroundColor(restartIconBackgroundColor);
        setRestartIconRadius(restartIconRadius);
        setRestartIconStrokeColor(restartIconStrokeColor);
        setRestartIconStrokeWidth(restartIconStrokeWidth);
        setRestartIconElevation(restartIconElevation);
    }

    /**
     * Applies the outgoing audio bubble style by obtaining styled attributes from
     * the specified style resource and updating the UI accordingly.
     *
     * @param style the style resource ID for the audio bubble
     */
    public void setOutgoingAudioBubbleStyle(@StyleRes int style) {
        try (TypedArray typedArray = getContext().obtainStyledAttributes(style, R.styleable.CometChatAudioBubble)) {
            messageBubbleBackgroundColor = typedArray.getColor(R.styleable.CometChatAudioBubble_cometchatAudioBubbleBackgroundColor,
                                                               messageBubbleBackgroundColor);
            messageBubbleCornerRadius = typedArray.getDimensionPixelSize(R.styleable.CometChatAudioBubble_cometchatAudioBubbleCornerRadius,
                                                                         messageBubbleCornerRadius);
            messageBubbleStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.CometChatAudioBubble_cometchatAudioBubbleStrokeWidth,
                                                                        messageBubbleStrokeWidth);
            messageBubbleStrokeColor = typedArray.getColor(R.styleable.CometChatAudioBubble_cometchatAudioBubbleStrokeColor,
                                                           messageBubbleStrokeColor);

            if (typedArray.getDrawable(R.styleable.CometChatAudioBubble_cometchatAudioBubbleBackgroundDrawable) != null) {
                messageBubbleBackgroundDrawable = typedArray.getDrawable(R.styleable.CometChatAudioBubble_cometchatAudioBubbleBackgroundDrawable);
            }

            binding.mediaRecorderPostView.setCardBackgroundColor(messageBubbleBackgroundColor);
            binding.mediaRecorderPostView.setStrokeWidth(messageBubbleStrokeWidth);
            binding.mediaRecorderPostView.setStrokeColor(messageBubbleStrokeColor);
            binding.mediaRecorderPostView.setRadius(messageBubbleCornerRadius);

            if (messageBubbleBackgroundDrawable != null) {
                binding.mediaRecorderPostView.setBackground(messageBubbleBackgroundDrawable);
            }

            binding.audioBubble.setStyle(style);
        }
    }

    /**
     * Returns the current background color of the media recorder.
     *
     * @return the background color
     */
    public @ColorInt int getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the background color of the media recorder.
     *
     * @param backgroundColor the background color to set
     */
    @Override
    public void setBackgroundColor(@ColorInt int backgroundColor) {
        this.backgroundColor = backgroundColor;
        super.setCardBackgroundColor(backgroundColor);
    }

    /**
     * Returns the current corner radius of the media recorder.
     *
     * @return the corner radius
     */
    public @Dimension int getCornerRadius() {
        return cornerRadius;
    }

    /**
     * Sets the corner radius of the media recorder.
     *
     * @param cornerRadius the corner radius to set
     */
    public void setCornerRadius(@Dimension int cornerRadius) {
        this.cornerRadius = cornerRadius;
        ShapeAppearanceModel shapeAppearanceModel = new ShapeAppearanceModel()
            .toBuilder()
            .setTopLeftCorner(CornerFamily.ROUNDED, cornerRadius)
            .setTopRightCorner(CornerFamily.ROUNDED, cornerRadius)
            .setBottomLeftCorner(CornerFamily.ROUNDED, 0)
            .setBottomRightCorner(CornerFamily.ROUNDED, 0)
            .build();
        super.setShapeAppearanceModel(shapeAppearanceModel);
    }

    /**
     * Returns the current recording icon drawable.
     *
     * @return the recording icon drawable
     */
    public Drawable getRecordingIcon() {
        return recordingIcon;
    }

    /**
     * Sets the recording icon drawable.
     *
     * @param recordingIcon the recording icon to set
     */
    public void setRecordingIcon(Drawable recordingIcon) {
        this.recordingIcon = recordingIcon;
        binding.ivRecordIcon.setImageDrawable(recordingIcon);
    }

    /**
     * Returns the current tint color for the recording icon.
     *
     * @return the recording icon tint color
     */
    public @ColorInt int getRecordingIconTint() {
        return recordingIconTint;
    }

    /**
     * Sets the tint color for the recording icon.
     *
     * @param recordingIconTint the recording icon tint color to set
     */
    public void setRecordingIconTint(@ColorInt int recordingIconTint) {
        this.recordingIconTint = recordingIconTint;
        binding.ivRecordIcon.setColorFilter(recordingIconTint);
    }

    /**
     * Returns the current background color of the recording icon.
     *
     * @return the recording icon background color
     */
    public @ColorInt int getRecordingIconBackgroundColor() {
        return recordingIconBackgroundColor;
    }

    /**
     * Sets the background color of the recording icon.
     *
     * @param recordingIconBackgroundColor the recording icon background color to set
     */
    public void setRecordingIconBackgroundColor(@ColorInt int recordingIconBackgroundColor) {
        this.recordingIconBackgroundColor = recordingIconBackgroundColor;
        binding.cardRecordIcon.setCardBackgroundColor(recordingIconBackgroundColor);
        binding.audioRippleEffect.setColor(recordingIconBackgroundColor);
    }

    /**
     * Returns the current text appearance style resource ID.
     *
     * @return the text appearance style resource ID
     */
    public @StyleRes int getTextAppearance() {
        return textAppearance;
    }

    /**
     * Sets the text appearance style for the recording time display.
     *
     * @param textAppearance the text appearance style resource ID to set
     */
    public void setTextAppearance(@StyleRes int textAppearance) {
        this.textAppearance = textAppearance;
        binding.tvRecordingTime.setTextAppearance(textAppearance);
    }

    /**
     * Returns the current text color of the recording time display.
     *
     * @return the text color
     */
    public @ColorInt int getTextColor() {
        return textColor;
    }

    /**
     * Sets the text color of the recording time display.
     *
     * @param textColor the text color to set
     */
    public void setTextColor(@ColorInt int textColor) {
        this.textColor = textColor;
        binding.tvRecordingTime.setTextColor(textColor);
    }

    /**
     * Returns the current delete icon drawable.
     *
     * @return the delete icon drawable
     */
    public Drawable getDeleteIcon() {
        return deleteIcon;
    }

    /**
     * Sets the delete icon drawable.
     *
     * @param deleteIcon the delete icon to set
     */
    public void setDeleteIcon(Drawable deleteIcon) {
        this.deleteIcon = deleteIcon;
        binding.ivLeftDelete.setImageDrawable(deleteIcon);
    }

    /**
     * Returns the current tint color for the delete icon.
     *
     * @return the delete icon tint color
     */
    public @ColorInt int getDeleteIconTint() {
        return deleteIconTint;
    }    /**
     * Returns the current stroke width of the media recorder.
     *
     * @return the stroke width
     */
    @Override
    public @Dimension int getStrokeWidth() {
        return strokeWidth;
    }

    /**
     * Sets the tint color for the delete icon.
     *
     * @param deleteIconTint the delete icon tint color to set
     */
    public void setDeleteIconTint(@ColorInt int deleteIconTint) {
        this.deleteIconTint = deleteIconTint;
        binding.ivLeftDelete.setColorFilter(deleteIconTint);
    }

    /**
     * Returns the current background color of the delete icon.
     *
     * @return the delete icon background color
     */
    public @ColorInt int getDeleteIconBackgroundColor() {
        return deleteIconBackgroundColor;
    }

    /**
     * Sets the background color of the delete icon.
     *
     * @param deleteIconBackgroundColor the delete icon background color to set
     */
    public void setDeleteIconBackgroundColor(@ColorInt int deleteIconBackgroundColor) {
        this.deleteIconBackgroundColor = deleteIconBackgroundColor;
        binding.btnLeft.setCardBackgroundColor(deleteIconBackgroundColor);
    }

    /**
     * Returns the current radius of the delete icon.
     *
     * @return the delete icon radius
     */
    public @Dimension int getDeleteIconRadius() {
        return deleteIconRadius;
    }

    /**
     * Sets the radius of the delete icon.
     *
     * @param deleteIconRadius the delete icon radius to set
     */
    public void setDeleteIconRadius(@Dimension int deleteIconRadius) {
        this.deleteIconRadius = deleteIconRadius;
        binding.btnLeft.setRadius(deleteIconRadius);
    }

    /**
     * Returns the current stroke width of the delete icon.
     *
     * @return the delete icon stroke width
     */
    public @Dimension int getDeleteIconStrokeWidth() {
        return deleteIconStrokeWidth;
    }

    /**
     * Sets the stroke width of the delete icon.
     *
     * @param deleteIconStrokeWidth the delete icon stroke width to set
     */
    public void setDeleteIconStrokeWidth(@Dimension int deleteIconStrokeWidth) {
        this.deleteIconStrokeWidth = deleteIconStrokeWidth;
        binding.btnLeft.setStrokeWidth(deleteIconStrokeWidth);
    }

    /**
     * Returns the current stroke color of the delete icon.
     *
     * @return the delete icon stroke color
     */
    public @ColorInt int getDeleteIconStrokeColor() {
        return deleteIconStrokeColor;
    }

    /**
     * Sets the stroke color of the delete icon.
     *
     * @param deleteIconStrokeColor the delete icon stroke color to set
     */
    public void setDeleteIconStrokeColor(@ColorInt int deleteIconStrokeColor) {
        this.deleteIconStrokeColor = deleteIconStrokeColor;
        binding.btnLeft.setStrokeColor(deleteIconStrokeColor);
    }

    /**
     * Returns the current elevation of the delete icon.
     *
     * @return the delete icon elevation
     */
    public int getDeleteIconElevation() {
        return deleteIconElevation;
    }

    /**
     * Sets the elevation of the delete icon.
     *
     * @param deleteIconElevation the delete icon elevation to set
     */
    public void setDeleteIconElevation(int deleteIconElevation) {
        this.deleteIconElevation = deleteIconElevation;
        binding.btnLeft.setCardElevation(deleteIconElevation);
    }

    /**
     * Returns the current start icon drawable.
     *
     * @return the start icon drawable
     */
    public Drawable getStartIcon() {
        return startIcon;
    }

    /**
     * Sets the start icon drawable.
     *
     * @param startIcon the start icon to set
     */
    public void setStartIcon(Drawable startIcon) {
        this.startIcon = startIcon;
        binding.ivCenterStart.setImageDrawable(startIcon);
        binding.ivCenterResume.setImageDrawable(startIcon);
    }

    /**
     * Returns the current tint color for the start icon.
     *
     * @return the start icon tint color
     */
    public @ColorInt int getStartIconTint() {
        return startIconTint;
    }

    /**
     * Sets the tint color for the start icon.
     *
     * @param startIconTint the start icon tint color to set
     */
    public void setStartIconTint(@ColorInt int startIconTint) {
        this.startIconTint = startIconTint;
        binding.ivCenterStart.setColorFilter(startIconTint);
        binding.ivCenterResume.setColorFilter(startIconTint);
    }

    /**
     * Returns the current background color of the start icon.
     *
     * @return the start icon background color
     */
    public @ColorInt int getStartIconBackgroundColor() {
        return startIconBackgroundColor;
    }

    /**
     * Sets the background color of the start icon.
     *
     * @param startIconBackgroundColor the start icon background color to set
     */
    public void setStartIconBackgroundColor(@ColorInt int startIconBackgroundColor) {
        this.startIconBackgroundColor = startIconBackgroundColor;
        binding.btnCenter.setCardBackgroundColor(startIconBackgroundColor);
    }

    /**
     * Returns the current radius of the start icon.
     *
     * @return the start icon radius
     */
    public @Dimension int getStartIconRadius() {
        return startIconRadius;
    }    /**
     * Sets the stroke width of the media recorder.
     *
     * @param strokeWidth the stroke width to set
     */
    @Override
    public void setStrokeWidth(@Dimension int strokeWidth) {
        this.strokeWidth = strokeWidth;
        super.setStrokeWidth(strokeWidth);
    }

    /**
     * Sets the radius of the start icon.
     *
     * @param startIconRadius the start icon radius to set
     */
    public void setStartIconRadius(@Dimension int startIconRadius) {
        this.startIconRadius = startIconRadius;
        binding.btnCenter.setRadius(startIconRadius);
    }

    /**
     * Returns the current stroke width of the start icon.
     *
     * @return the start icon stroke width
     */
    public @Dimension int getStartIconStrokeWidth() {
        return startIconStrokeWidth;
    }

    /**
     * Sets the stroke width of the start icon.
     *
     * @param startIconStrokeWidth the start icon stroke width to set
     */
    public void setStartIconStrokeWidth(@Dimension int startIconStrokeWidth) {
        this.startIconStrokeWidth = startIconStrokeWidth;
        binding.btnCenter.setStrokeWidth(startIconStrokeWidth);
    }

    /**
     * Returns the current stroke color of the start icon.
     *
     * @return the start icon stroke color
     */
    public @ColorInt int getStartIconStrokeColor() {
        return startIconStrokeColor;
    }

    /**
     * Sets the stroke color of the start icon.
     *
     * @param startIconStrokeColor the start icon stroke color to set
     */
    public void setStartIconStrokeColor(@ColorInt int startIconStrokeColor) {
        this.startIconStrokeColor = startIconStrokeColor;
        binding.btnCenter.setStrokeColor(startIconStrokeColor);
    }

    /**
     * Returns the current elevation of the start icon.
     *
     * @return the start icon elevation
     */
    public int getStartIconElevation() {
        return startIconElevation;
    }

    /**
     * Sets the elevation of the start icon.
     *
     * @param startIconElevation the start icon elevation to set
     */
    public void setStartIconElevation(int startIconElevation) {
        this.startIconElevation = startIconElevation;
        binding.btnCenter.setCardElevation(startIconElevation);
    }

    /**
     * Returns the current pause icon drawable.
     *
     * @return the pause icon drawable
     */
    public Drawable getPauseIcon() {
        return pauseIcon;
    }

    /**
     * Sets the pause icon drawable.
     *
     * @param pauseIcon the pause icon to set
     */
    public void setPauseIcon(Drawable pauseIcon) {
        this.pauseIcon = pauseIcon;
        binding.ivCenterPause.setImageDrawable(pauseIcon);
    }

    /**
     * Returns the current tint color for the pause icon.
     *
     * @return the pause icon tint color
     */
    public @ColorInt int getPauseIconTint() {
        return pauseIconTint;
    }

    /**
     * Sets the tint color for the pause icon.
     *
     * @param pauseIconTint the pause icon tint color to set
     */
    public void setPauseIconTint(@ColorInt int pauseIconTint) {
        this.pauseIconTint = pauseIconTint;
        binding.ivCenterPause.setColorFilter(pauseIconTint);
    }

    /**
     * Returns the current background color of the pause icon.
     *
     * @return the pause icon background color
     */
    public @ColorInt int getPauseIconBackgroundColor() {
        return pauseIconBackgroundColor;
    }

    /**
     * Sets the background color of the pause icon.
     *
     * @param pauseIconBackgroundColor the pause icon background color to set
     */
    public void setPauseIconBackgroundColor(@ColorInt int pauseIconBackgroundColor) {
        this.pauseIconBackgroundColor = pauseIconBackgroundColor;
        binding.btnCenter.setCardBackgroundColor(pauseIconBackgroundColor);
    }

    /**
     * Returns the current radius of the pause icon.
     *
     * @return the pause icon radius
     */
    public @Dimension int getPauseIconRadius() {
        return pauseIconRadius;
    }

    /**
     * Sets the radius of the pause icon.
     *
     * @param pauseIconRadius the pause icon radius to set
     */
    public void setPauseIconRadius(@Dimension int pauseIconRadius) {
        this.pauseIconRadius = pauseIconRadius;
        binding.btnCenter.setRadius(pauseIconRadius);
    }

    /**
     * Returns the current stroke width of the pause icon.
     *
     * @return the pause icon stroke width
     */
    public @Dimension int getPauseIconStrokeWidth() {
        return pauseIconStrokeWidth;
    }

    /**
     * Sets the stroke width of the pause icon.
     *
     * @param pauseIconStrokeWidth the pause icon stroke width to set
     */
    public void setPauseIconStrokeWidth(@Dimension int pauseIconStrokeWidth) {
        this.pauseIconStrokeWidth = pauseIconStrokeWidth;
        binding.btnCenter.setStrokeWidth(pauseIconStrokeWidth);
    }

    /**
     * Returns the current stroke color of the pause icon.
     *
     * @return the pause icon stroke color
     */
    public @ColorInt int getPauseIconStrokeColor() {
        return pauseIconStrokeColor;
    }

    /**
     * Sets the stroke color of the pause icon.
     *
     * @param pauseIconStrokeColor the pause icon stroke color to set
     */
    public void setPauseIconStrokeColor(@ColorInt int pauseIconStrokeColor) {
        this.pauseIconStrokeColor = pauseIconStrokeColor;
        binding.btnCenter.setStrokeColor(pauseIconStrokeColor);
    }

    /**
     * Returns the current elevation of the pause icon.
     *
     * @return the pause icon elevation
     */
    public int getPauseIconElevation() {
        return pauseIconElevation;
    }

    /**
     * Sets the elevation of the pause icon.
     *
     * @param pauseIconElevation the pause icon elevation to set
     */
    public void setPauseIconElevation(int pauseIconElevation) {
        this.pauseIconElevation = pauseIconElevation;
        binding.btnCenter.setCardElevation(pauseIconElevation);
    }

    /**
     * Returns the current stop icon drawable.
     *
     * @return the stop icon drawable
     */
    public Drawable getStopIcon() {
        return stopIcon;
    }

    /**
     * Sets the stop icon drawable.
     *
     * @param stopIcon the stop icon to set
     */
    public void setStopIcon(Drawable stopIcon) {
        this.stopIcon = stopIcon;
        binding.ivRightStop.setImageDrawable(stopIcon);
    }

    /**
     * Returns the current tint color for the stop icon.
     *
     * @return the stop icon tint color
     */
    public @ColorInt int getStopIconTint() {
        return stopIconTint;
    }

    /**
     * Sets the tint color for the stop icon.
     *
     * @param stopIconTint the stop icon tint color to set
     */
    public void setStopIconTint(@ColorInt int stopIconTint) {
        this.stopIconTint = stopIconTint;
        binding.ivRightStop.setColorFilter(stopIconTint);
    }

    /**
     * Returns the current background color of the stop icon.
     *
     * @return the stop icon background color
     */
    public @ColorInt int getStopIconBackgroundColor() {
        return stopIconBackgroundColor;
    }

    /**
     * Sets the background color of the stop icon.
     *
     * @param stopIconBackgroundColor the stop icon background color to set
     */
    public void setStopIconBackgroundColor(@ColorInt int stopIconBackgroundColor) {
        this.stopIconBackgroundColor = stopIconBackgroundColor;
        binding.btnRight.setCardBackgroundColor(stopIconBackgroundColor);
    }

    /**
     * Returns the current radius of the stop icon.
     *
     * @return the stop icon radius
     */
    public @Dimension int getStopIconRadius() {
        return stopIconRadius;
    }

    /**
     * Sets the radius of the stop icon.
     *
     * @param stopIconRadius the stop icon radius to set
     */
    public void setStopIconRadius(@Dimension int stopIconRadius) {
        this.stopIconRadius = stopIconRadius;
        binding.btnRight.setRadius(stopIconRadius);
    }

    /**
     * Returns the current stroke width of the stop icon.
     *
     * @return the stop icon stroke width
     */
    public @Dimension int getStopIconStrokeWidth() {
        return stopIconStrokeWidth;
    }

    /**
     * Sets the stroke width of the stop icon.
     *
     * @param stopIconStrokeWidth the stop icon stroke width to set
     */
    public void setStopIconStrokeWidth(@Dimension int stopIconStrokeWidth) {
        this.stopIconStrokeWidth = stopIconStrokeWidth;
        binding.btnRight.setStrokeWidth(stopIconStrokeWidth);
    }

    /**
     * Returns the current stroke color of the stop icon.
     *
     * @return the stop icon stroke color
     */
    public @ColorInt int getStopIconStrokeColor() {
        return stopIconStrokeColor;
    }

    /**
     * Sets the stroke color of the stop icon.
     *
     * @param stopIconStrokeColor the stop icon stroke color to set
     */
    public void setStopIconStrokeColor(@ColorInt int stopIconStrokeColor) {
        this.stopIconStrokeColor = stopIconStrokeColor;
        binding.btnRight.setStrokeColor(stopIconStrokeColor);
    }

    /**
     * Returns the current elevation of the stop icon.
     *
     * @return the stop icon elevation
     */
    public int getStopIconElevation() {
        return stopIconElevation;
    }

    /**
     * Sets the elevation of the stop icon.
     *
     * @param stopIconElevation the stop icon elevation to set
     */
    public void setStopIconElevation(int stopIconElevation) {
        this.stopIconElevation = stopIconElevation;
        binding.btnRight.setCardElevation(stopIconElevation);
    }

    /**
     * Returns the current send icon drawable.
     *
     * @return the send icon drawable
     */
    public Drawable getSendIcon() {
        return sendIcon;
    }

    /**
     * Sets the send icon drawable.
     *
     * @param sendIcon the send icon to set
     */
    public void setSendIcon(Drawable sendIcon) {
        this.sendIcon = sendIcon;
        binding.ivCenterSend.setImageDrawable(sendIcon);
    }

    /**
     * Returns the current tint color for the send icon.
     *
     * @return the send icon tint color
     */
    public @ColorInt int getSendIconTint() {
        return sendIconTint;
    }

    /**
     * Sets the tint color for the send icon.
     *
     * @param sendIconTint the send icon tint color to set
     */
    public void setSendIconTint(@ColorInt int sendIconTint) {
        this.sendIconTint = sendIconTint;
        binding.ivCenterSend.setColorFilter(sendIconTint);
    }

    /**
     * Returns the current background color of the send icon.
     *
     * @return the send icon background color
     */
    public @ColorInt int getSendIconBackgroundColor() {
        return sendIconBackgroundColor;
    }

    /**
     * Sets the background color of the send icon.
     *
     * @param sendIconBackgroundColor the send icon background color to set
     */
    public void setSendIconBackgroundColor(@ColorInt int sendIconBackgroundColor) {
        this.sendIconBackgroundColor = sendIconBackgroundColor;
        binding.btnCenter.setCardBackgroundColor(sendIconBackgroundColor);
    }

    /**
     * Returns the current radius of the send icon.
     *
     * @return the send icon radius
     */
    public @Dimension int getSendIconRadius() {
        return sendIconRadius;
    }

    /**
     * Sets the radius of the send icon.
     *
     * @param sendIconRadius the send icon radius to set
     */
    public void setSendIconRadius(@Dimension int sendIconRadius) {
        this.sendIconRadius = sendIconRadius;
        binding.btnCenter.setRadius(sendIconRadius);
    }

    /**
     * Returns the current stroke width of the send icon.
     *
     * @return the send icon stroke width
     */
    public @Dimension int getSendIconStrokeWidth() {
        return sendIconStrokeWidth;
    }

    /**
     * Sets the stroke width of the send icon.
     *
     * @param sendIconStrokeWidth the send icon stroke width to set
     */
    public void setSendIconStrokeWidth(@Dimension int sendIconStrokeWidth) {
        this.sendIconStrokeWidth = sendIconStrokeWidth;
        binding.btnCenter.setStrokeWidth(sendIconStrokeWidth);
    }

    /**
     * Returns the current stroke color of the send icon.
     *
     * @return the send icon stroke color
     */
    public @ColorInt int getSendIconStrokeColor() {
        return sendIconStrokeColor;
    }

    /**
     * Sets the stroke color of the send icon.
     *
     * @param sendIconStrokeColor the send icon stroke color to set
     */
    public void setSendIconStrokeColor(@ColorInt int sendIconStrokeColor) {
        this.sendIconStrokeColor = sendIconStrokeColor;
        binding.btnCenter.setStrokeColor(sendIconStrokeColor);
    }

    /**
     * Returns the current elevation of the send icon.
     *
     * @return the send icon elevation
     */
    public int getSendIconElevation() {
        return sendIconElevation;
    }

    /**
     * Sets the elevation of the send icon.
     *
     * @param sendIconElevation the send icon elevation to set
     */
    public void setSendIconElevation(int sendIconElevation) {
        this.sendIconElevation = sendIconElevation;
        binding.btnCenter.setCardElevation(sendIconElevation);
    }

    /**
     * Returns the current restart icon drawable.
     *
     * @return the restart icon drawable
     */
    public Drawable getRestartIcon() {
        return restartIcon;
    }

    /**
     * Sets the restart icon drawable.
     *
     * @param restartIcon the restart icon to set
     */
    public void setRestartIcon(Drawable restartIcon) {
        this.restartIcon = restartIcon;
        binding.ivRightRestart.setImageDrawable(restartIcon);
    }

    /**
     * Returns the current tint color for the restart icon.
     *
     * @return the restart icon tint color
     */
    public @ColorInt int getRestartIconTint() {
        return restartIconTint;
    }

    /**
     * Sets the tint color for the restart icon.
     *
     * @param restartIconTint the restart icon tint color to set
     */
    public void setRestartIconTint(@ColorInt int restartIconTint) {
        this.restartIconTint = restartIconTint;
        binding.ivRightRestart.setColorFilter(restartIconTint);
    }

    /**
     * Returns the current background color of the restart icon.
     *
     * @return the restart icon background color
     */
    public @ColorInt int getRestartIconBackgroundColor() {
        return restartIconBackgroundColor;
    }

    /**
     * Sets the background color of the restart icon.
     *
     * @param restartIconBackgroundColor the restart icon background color to set
     */
    public void setRestartIconBackgroundColor(@ColorInt int restartIconBackgroundColor) {
        this.restartIconBackgroundColor = restartIconBackgroundColor;
        binding.btnRight.setCardBackgroundColor(restartIconBackgroundColor);
    }

    /**
     * Returns the current radius of the restart icon.
     *
     * @return the restart icon radius
     */
    public @Dimension int getRestartIconRadius() {
        return restartIconRadius;
    }

    /**
     * Sets the radius of the restart icon.
     *
     * @param restartIconRadius the restart icon radius to set
     */
    public void setRestartIconRadius(@Dimension int restartIconRadius) {
        this.restartIconRadius = restartIconRadius;
        binding.btnRight.setRadius(restartIconRadius);
    }

    /**
     * Returns the current stroke width of the restart icon.
     *
     * @return the restart icon stroke width
     */
    public @Dimension int getRestartIconStrokeWidth() {
        return restartIconStrokeWidth;
    }

    /**
     * Sets the stroke width of the restart icon.
     *
     * @param restartIconStrokeWidth the restart icon stroke width to set
     */
    public void setRestartIconStrokeWidth(@Dimension int restartIconStrokeWidth) {
        this.restartIconStrokeWidth = restartIconStrokeWidth;
        binding.btnRight.setStrokeWidth(restartIconStrokeWidth);
    }

    /**
     * Returns the current stroke color of the restart icon.
     *
     * @return the restart icon stroke color
     */
    public @ColorInt int getRestartIconStrokeColor() {
        return restartIconStrokeColor;
    }

    /**
     * Sets the stroke color of the restart icon.
     *
     * @param restartIconStrokeColor the restart icon stroke color to set
     */
    public void setRestartIconStrokeColor(@ColorInt int restartIconStrokeColor) {
        this.restartIconStrokeColor = restartIconStrokeColor;
        binding.btnRight.setStrokeColor(restartIconStrokeColor);
    }

    /**
     * Returns the current elevation of the restart icon.
     *
     * @return the restart icon elevation
     */
    public int getRestartIconElevation() {
        return restartIconElevation;
    }

    /**
     * Sets the elevation of the restart icon.
     *
     * @param restartIconElevation the restart icon elevation to set
     */
    public void setRestartIconElevation(int restartIconElevation) {
        this.restartIconElevation = restartIconElevation;
        binding.btnRight.setCardElevation(restartIconElevation);
    }

    /**
     * Returns the current style resource for the outgoing message bubble.
     *
     * @return the outgoing message bubble style resource ID
     */
    public @StyleRes int getOutgoingMessageBubbleStyle() {
        return outgoingMessageBubbleStyle;
    }

    /**
     * Applies the outgoing message bubble style by obtaining styled attributes from
     * the specified style resource.
     *
     * @param messageBubbleStyle the style resource ID for the message bubble
     */
    public void setOutgoingMessageBubbleStyle(@StyleRes int messageBubbleStyle) {
        if (messageBubbleStyle != 0) {
            this.outgoingMessageBubbleStyle = messageBubbleStyle;
            try (TypedArray typedArray = getContext().obtainStyledAttributes(messageBubbleStyle, R.styleable.CometChatMessageBubble)) {
                messageBubbleBackgroundColor = typedArray.getColor(R.styleable.CometChatMessageBubble_cometchatMessageBubbleBackgroundColor, 0);
                messageBubbleCornerRadius = typedArray.getDimensionPixelSize(R.styleable.CometChatMessageBubble_cometchatMessageBubbleCornerRadius,
                                                                             0);
                messageBubbleStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.CometChatMessageBubble_cometchatMessageBubbleStrokeWidth, 0);
                messageBubbleStrokeColor = typedArray.getColor(R.styleable.CometChatMessageBubble_cometchatMessageBubbleStrokeColor, 0);
                messageBubbleBackgroundDrawable = typedArray.getDrawable(R.styleable.CometChatMessageBubble_cometchatMessageBubbleBackgroundDrawable);
                setOutgoingAudioBubbleStyle(typedArray.getResourceId(R.styleable.CometChatMessageBubble_cometchatAudioBubbleStyle, 0));
            }
        }
    }

    /**
     * Invokes the send click action with the specified file and context.
     *
     * @param context the context to use for the action
     * @param file    the file to send
     */
    private void invokeSendClick(Context context, File file) {
        if (onSubmit != null) {
            onSubmit.onClick(file, context);
            recordedFilePath = null;
            deleteRecording();
        }
    }

    /**
     * Starts recording audio internally. Initializes the MediaRecorder, sets the
     * audio source, output format, output file, and audio encoder, and starts
     * recording.
     */
    private void startRecordingInternally() {
        recordedFilePath = Objects.requireNonNull(getContext().getExternalCacheDir()).getAbsolutePath() + "/audio_record.m4a";
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setOutputFile(recordedFilePath);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        try {
            recorder.prepare();
            recorder.start();
            binding.audioRippleEffect.startAnimation();
            isRecording = true;
            startTime = System.currentTimeMillis();
            timerHandler.postDelayed(timerRunnable, 0);
        } catch (Exception e) {
            Log.e("AUDIO_RECORD", "Recording preparation failed.");
        }
    }

    /**
     * Pauses the audio recording if it is currently in progress. Stops the ripple
     * effect animation and removes the timer updates.
     */
    private void pauseRecording() {
        recordingStateHandler(RecordingState.PAUSED);
        if (isRecording) {
            pauseTime = System.currentTimeMillis() - startTime;
            recorder.pause();
            binding.audioRippleEffect.stopAnimation();
            timerHandler.removeCallbacks(timerRunnable);
            isRecording = false;
        }
    }

    /**
     * Resumes the audio recording from the paused state. Restarts the ripple effect
     * animation and resumes the timer updates.
     */
    private void resumeRecording() {
        if (recorder != null) {
            recordingStateHandler(RecordingState.RECORDING);
            recorder.resume();
            binding.audioRippleEffect.startAnimation();
            isRecording = true;
            startTime = System.currentTimeMillis() - pauseTime;
            timerHandler.postDelayed(timerRunnable, 0);
        }
    }

    /**
     * Restarts the audio recording process by stopping the current recording and
     * initiating a new one. Resets the recording time display.
     */
    private void restartRecording() {
        stopRecording();
        startRecording();
        binding.tvRecordingTime.setText("00:00");
        timerHandler.removeCallbacks(timerRunnable);
    }

    /**
     * Plays the recorded audio file. Stops any ongoing recording and initializes
     * the MediaPlayer to play the audio.
     */
    private void playRecording() {
        stopRecording();
        MediaPlayer player = new MediaPlayer();
        try {
            player.setDataSource(recordedFilePath);
            player.prepare();
            player.start();
        } catch (IOException e) {
            Log.e("AUDIO_PLAY", "Playing audio failed.");
        }
    }

    /**
     * Stops the audio recording if it is currently in progress. Releases the
     * MediaRecorder resources and resets the UI elements.
     */
    public void stopRecording() {
        AudioPlayer.getInstance().reset();
        recordingStateHandler(RecordingState.STOPPED);
        if (recorder != null) {
            try {
                recorder.stop();
                binding.audioRippleEffect.stopAnimation();
                recorder.release();
                recorder = null;
                isRecording = false;
                binding.tvRecordingTime.setText("00:00");
                timerHandler.removeCallbacks(timerRunnable);
            } catch (Exception ignored) {
            }

        }
    }

    /**
     * Handles the visibility and attributes of UI elements based on the current
     * recording state.
     *
     * @param state the current recording state
     */
    private void recordingStateHandler(RecordingState state) {
        switch (state) {
            case START:
                setVisibilityForStart();
                setStartIconAttributes();
                break;

            case RECORDING:
                setVisibilityForRecording();
                setDeleteIconAttributes();
                setPauseIconAttributes();
                setStopIconAttributes();
                break;

            case PAUSED:
                setVisibilityForPaused();
                setDeleteIconAttributes();
                setStartIconAttributes();
                setStopIconAttributes();
                break;

            case STOPPED:
                setVisibilityForStopped();
                setDeleteIconAttributes();
                setSendIconAttributes();
                setRestartIconAttributes();
                break;
        }
    }

    /**
     * Sets the visibility of UI elements for the start recording state. Makes the
     * pre-view visible and hides the post-view.
     */
    private void setVisibilityForStart() {
        binding.mediaRecorderPreView.setVisibility(View.VISIBLE);
        binding.mediaRecorderPostView.setVisibility(View.GONE);

        binding.btnLeft.setVisibility(View.GONE);
        binding.btnCenter.setVisibility(View.VISIBLE);
        binding.btnRight.setVisibility(View.GONE);

        binding.ivCenterStart.setVisibility(View.VISIBLE);
        binding.ivCenterPause.setVisibility(View.GONE);
        binding.ivCenterSend.setVisibility(View.GONE);
    }

    /**
     * Sets the visibility of UI elements for the recording state. Displays the
     * necessary buttons and icons for recording operations.
     */
    private void setVisibilityForRecording() {
        binding.mediaRecorderPreView.setVisibility(View.VISIBLE);
        binding.mediaRecorderPostView.setVisibility(View.GONE);

        binding.btnLeft.setVisibility(View.VISIBLE);
        binding.btnCenter.setVisibility(View.VISIBLE);
        binding.btnRight.setVisibility(View.VISIBLE);

        binding.ivLeftDelete.setVisibility(View.VISIBLE);
        binding.ivCenterStart.setVisibility(View.GONE);
        binding.ivCenterResume.setVisibility(View.GONE);
        binding.ivCenterPause.setVisibility(View.VISIBLE);
        binding.ivCenterSend.setVisibility(View.GONE);
        binding.ivRightStop.setVisibility(View.VISIBLE);
        binding.ivRightRestart.setVisibility(View.GONE);
    }

    /**
     * Sets the visibility of UI elements for the paused recording state. Displays
     * options to resume or stop the recording.
     */
    private void setVisibilityForPaused() {
        binding.ivLeftDelete.setVisibility(View.VISIBLE);
        binding.ivCenterResume.setVisibility(View.VISIBLE);
        binding.ivCenterPause.setVisibility(View.GONE);
        binding.ivCenterSend.setVisibility(View.GONE);
        binding.ivRightStop.setVisibility(View.VISIBLE);
        binding.ivRightRestart.setVisibility(View.GONE);
    }

    /**
     * Sets the visibility of UI elements for the stopped recording state. Displays
     * the options to delete or send the recorded audio.
     */
    private void setVisibilityForStopped() {
        binding.mediaRecorderPreView.setVisibility(View.GONE);
        binding.mediaRecorderPostView.setVisibility(View.VISIBLE);

        binding.ivLeftDelete.setVisibility(View.VISIBLE);
        binding.ivCenterStart.setVisibility(View.GONE);
        binding.ivCenterResume.setVisibility(View.GONE);
        binding.ivCenterPause.setVisibility(View.GONE);
        binding.ivCenterSend.setVisibility(View.VISIBLE);
        binding.ivRightStop.setVisibility(View.GONE);
        binding.ivRightRestart.setVisibility(View.VISIBLE);
    }

    /**
     * Sets the click listener for the close (delete) button.
     *
     * @param onClose The click listener to be set for the close button.
     */
    public void setOnCloseClickListener(@Nullable OnClick onClose) {
        if (onClose != null) {
            this.onClose = onClose;
        }
    }

    /**
     * Sets the click listener for the submit button.
     *
     * @param onSubmit The click listener to be set for the submit button.
     */
    public void setOnSubmitClickListener(OnSubmitClick onSubmit) {
        if (onSubmit != null) {
            this.onSubmit = onSubmit;
        }
    }

    /**
     * Starts the audio recording process. Checks for permissions and initiates the
     * recording if allowed.
     */
    public void startRecording() {
        AudioPlayer.getInstance().reset();
        recordingStateHandler(RecordingState.RECORDING);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
            permissions = new String[]{Manifest.permission.RECORD_AUDIO};
        }
        isAutoPermissionCheck = false;
        permissionHandlerBuilder.withPermissions(permissions).check();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopRecording();
    }

    /**
     * Deletes the recorded audio file and invokes the onClose callback if defined.
     */
    private void deleteRecording() {
        stopRecording();
        if (onClose != null) {
            onClose.onClick();
        }
    }

    /**
     * Enum representing the various states of audio recording.
     */
    private enum RecordingState {
        /**
         * The state when recording is ready to start.
         */
        START,
        /**
         * The state when audio is currently being recorded.
         */
        RECORDING,
        /**
         * The state when recording is temporarily paused.
         */
        PAUSED,
        /**
         * The state when recording has been stopped.
         */
        STOPPED,
    }






}
