package com.cometchat.chatuikit.shared.views.inlineaudiorecorder;

import android.Manifest;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.databinding.CometchatInlineAudioRecorderBinding;
import com.cometchat.chatuikit.shared.permission.CometChatPermissionHandler;
import com.cometchat.chatuikit.shared.permission.builder.PermissionHandlerBuilder;
import com.cometchat.chatuikit.shared.resources.utils.AudioPlayer;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * CometChatInlineAudioRecorder is a compact audio recording component that displays
 * directly in the composer area with real-time waveform visualization, playback preview,
 * and seeking capabilities.
 * <p>
 * This component extends MaterialCardView and provides a WhatsApp/iMessage-style
 * recording experience with:
 * <ul>
 *     <li>Real-time waveform visualization during recording</li>
 *     <li>Playback preview with seeking</li>
 *     <li>Pause/resume recording support (API 24+)</li>
 *     <li>Customizable styling via style attributes</li>
 * </ul>
 * </p>
 *
 * @see InlineAudioRecorderViewModel
 * @see com.cometchat.chatuikit.shared.views.waveform.AudioWaveformVisualizer
 */
public class CometChatInlineAudioRecorder extends MaterialCardView {

    // ViewBinding
    private CometchatInlineAudioRecorderBinding binding;

    // ViewModel
    private InlineAudioRecorderViewModel viewModel;

    // Lifecycle owner for observing LiveData
    private LifecycleOwner lifecycleOwner;

    // Lifecycle observer to pause recording/playback when app goes to background
    private LifecycleEventObserver lifecycleEventObserver;

    // Permission handling
    private PermissionHandlerBuilder permissionHandlerBuilder;
    private String[] permissions;

    // Configuration
    private boolean autoStartRecording = true;

    // Style properties
    private @ColorInt int backgroundColor;
    private @ColorInt int waveformRecordingColor;
    private @ColorInt int waveformPlayingColor;
    private @ColorInt int waveformUnplayedColor;
    private @ColorInt int durationTextColor;
    private @StyleRes int durationTextAppearance;
    private @ColorInt int deleteButtonIconColor;
    private @ColorInt int sendButtonIconColor;
    private @ColorInt int sendButtonBackgroundColor;
    private @ColorInt int playButtonIconColor;
    private @ColorInt int pauseButtonIconColor;
    private @ColorInt int recordButtonIconColor;
    private @ColorInt int recordingIndicatorColor;

    // Custom icons
    private Drawable deleteIcon;
    private Drawable recordIcon;
    private Drawable pauseIcon;
    private Drawable playIcon;
    private Drawable sendIcon;
    private Drawable micIcon;

    // Callbacks
    private OnSubmitListener onSubmitListener;
    private OnSubmitWithWaveformListener onSubmitWithWaveformListener;
    private OnCancelListener onCancelListener;
    private OnErrorListener onErrorListener;

    /**
     * Interface for receiving submit events when the user sends the recording.
     */
    public interface OnSubmitListener {
        /**
         * Called when the user submits the recording.
         *
         * @param filePath The path to the recorded audio file.
         */
        void onSubmit(String filePath);
    }
    
    /**
     * Interface for receiving submit events with waveform data.
     * Use this instead of OnSubmitListener to receive amplitude data for waveform visualization.
     */
    public interface OnSubmitWithWaveformListener {
        /**
         * Called when the user submits the recording.
         *
         * @param filePath The path to the recorded audio file.
         * @param amplitudes The list of amplitude values captured during recording for waveform visualization.
         */
        void onSubmit(String filePath, List<Float> amplitudes);
    }

    /**
     * Interface for receiving cancel events when the user cancels the recording.
     */
    public interface OnCancelListener {
        /**
         * Called when the user cancels the recording.
         */
        void onCancel();
    }

    /**
     * Interface for receiving error events during recording or playback.
     */
    public interface OnErrorListener {
        /**
         * Called when an error occurs.
         *
         * @param e The exception that occurred.
         */
        void onError(Exception e);
    }

    /**
     * Constructs a new CometChatInlineAudioRecorder with default styling.
     *
     * @param context The context to use for the view.
     */
    public CometChatInlineAudioRecorder(@NonNull Context context) {
        this(context, null);
    }

    /**
     * Constructs a new CometChatInlineAudioRecorder with attributes from XML.
     *
     * @param context The context to use for the view.
     * @param attrs   The attribute set from XML layout.
     */
    public CometChatInlineAudioRecorder(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.cometchatInlineAudioRecorderStyle);
    }

    /**
     * Constructs a new CometChatInlineAudioRecorder with custom styling.
     *
     * @param context      The context to use for the view.
     * @param attrs        The attribute set from XML layout.
     * @param defStyleAttr The default style attribute to apply.
     */
    public CometChatInlineAudioRecorder(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            init(context, attrs, defStyleAttr);
        }
    }

    /**
     * Initializes the component by inflating the layout, loading style attributes,
     * setting up the ViewModel, and configuring listeners.
     *
     * @param context      The context to use for initialization.
     * @param attrs        The attribute set from XML layout.
     * @param defStyleAttr The default style attribute to apply.
     */
    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        // Reset the card view to default values
        Utils.initMaterialCard(this);

        // Inflate layout
        binding = CometchatInlineAudioRecorderBinding.inflate(LayoutInflater.from(context), this, true);

        // Apply style attributes
        applyStyleAttributes(attrs, defStyleAttr);

        // Setup permission handler
        setupPermissionHandler();

        // Setup click listeners
        setupClickListeners();

        // Setup AudioPlayer listener to pause recording when audio playback starts
        setupAudioPlayerListener();

        // Initialize to idle state
        showIdleState();
    }

    /**
     * Sets up a listener on AudioPlayer to pause recording or playback when audio playback starts.
     * This ensures mutual exclusion between voice recording/playback and audio message playback.
     */
    private void setupAudioPlayerListener() {
        AudioPlayer.getInstance().setOnPlaybackStartListener(() -> {
            // When audio playback starts (e.g., from audio bubble), pause the inline recorder
            if (viewModel != null) {
                InlineAudioRecorderState state = viewModel.getState().getValue();
                if (state != null) {
                    if (state.getStatus() == InlineAudioRecorderStatus.RECORDING) {
                        // Pause recording if actively recording
                        viewModel.pauseRecording();
                    } else if (state.getStatus() == InlineAudioRecorderStatus.PLAYING) {
                        // Pause playback if playing the recorded audio preview
                        viewModel.pausePlayback();
                    }
                }
            }
        });
    }

    /**
     * Sets up the permission handler for requesting RECORD_AUDIO permission.
     */
    private void setupPermissionHandler() {
        permissions = new String[]{Manifest.permission.RECORD_AUDIO};
        permissionHandlerBuilder = CometChatPermissionHandler.withContext(getContext())
            .withListener((grantedPermission, deniedPermission) -> {
                if (deniedPermission.isEmpty()) {
                    // Permission granted, start recording
                    startRecordingInternal();
                } else {
                    // Permission denied
                    if (onErrorListener != null) {
                        onErrorListener.onError(new SecurityException("Microphone permission denied"));
                    }
                }
            });
    }

    /**
     * Initializes the ViewModel using the provided ViewModelStoreOwner.
     *
     * @param owner The ViewModelStoreOwner to use for ViewModel initialization.
     */
    private void initViewModel(ViewModelStoreOwner owner) {
        if (viewModel == null) {
            viewModel = new ViewModelProvider(owner).get(InlineAudioRecorderViewModel.class);
        }
    }

    /**
     * Sets the lifecycle owner for observing ViewModel state changes.
     * This must be called before starting recording.
     *
     * @param owner The LifecycleOwner to use for observing LiveData.
     */
    public void setLifecycleOwner(@NonNull LifecycleOwner owner) {
        // Remove observer from previous lifecycle owner if any
        if (this.lifecycleOwner != null && lifecycleEventObserver != null) {
            this.lifecycleOwner.getLifecycle().removeObserver(lifecycleEventObserver);
        }

        this.lifecycleOwner = owner;
        if (owner instanceof ViewModelStoreOwner) {
            initViewModel((ViewModelStoreOwner) owner);
            observeState();
        }

        // Register lifecycle observer to pause recording/playback when app goes to background
        lifecycleEventObserver = (source, event) -> {
            if (event == Lifecycle.Event.ON_STOP) {
                pauseRecordingAndPlayback();
            }
        };
        owner.getLifecycle().addObserver(lifecycleEventObserver);
    }

    /**
     * Observes the ViewModel state and updates the UI accordingly.
     */
    private void observeState() {
        if (viewModel != null && lifecycleOwner != null) {
            viewModel.getState().observe(lifecycleOwner, this::updateUI);
        }
    }

    /**
     * Pauses any active recording or playback.
     * Called when the app goes to the background (lifecycle ON_STOP) to prevent
     * voice recording and playback from continuing in the background.
     */
    private void pauseRecordingAndPlayback() {
        if (viewModel == null) return;
        InlineAudioRecorderState state = viewModel.getState().getValue();
        if (state == null) return;

        if (state.getStatus() == InlineAudioRecorderStatus.RECORDING) {
            viewModel.pauseRecording();
        } else if (state.getStatus() == InlineAudioRecorderStatus.PLAYING) {
            viewModel.pausePlayback();
        }
    }


    // ==================== State Observation and UI Updates ====================

    /**
     * Updates the UI based on the current recorder state.
     *
     * @param state The current InlineAudioRecorderState.
     */
    private void updateUI(InlineAudioRecorderState state) {
        if (state == null) return;

        switch (state.getStatus()) {
            case IDLE:
                showIdleState();
                break;
            case RECORDING:
                showRecordingState(state);
                break;
            case PAUSED:
                showPausedState(state);
                break;
            case PLAYING:
                showPlayingState(state);
                break;
            case COMPLETED:
                showCompletedState(state);
                break;
            case ERROR:
                showErrorState(state);
                break;
        }
    }

    /**
     * Shows the idle state UI (initial state before recording).
     */
    private void showIdleState() {
        // Hide all state indicators
        binding.recordingIndicator.setVisibility(View.GONE);
        binding.btnPlay.setVisibility(View.GONE);
        binding.btnPausePlayback.setVisibility(View.GONE);
        binding.btnAction.setVisibility(View.GONE);

        // Reset duration
        binding.tvDuration.setText("0:00");

        // Clear waveform
        binding.waveformVisualizer.clearBars();
        binding.waveformVisualizer.setIsAnimating(false);
        binding.waveformVisualizer.setIsPlaying(false);
    }

    /**
     * Shows the recording state UI.
     *
     * @param state The current state containing recording data.
     */
    private void showRecordingState(InlineAudioRecorderState state) {
        // Show recording indicator (red dot)
        binding.recordingIndicator.setVisibility(View.VISIBLE);
        binding.btnPlay.setVisibility(View.GONE);
        binding.btnPausePlayback.setVisibility(View.GONE);

        // Show action button with pause icon
        binding.btnAction.setVisibility(View.VISIBLE);
        if (pauseIcon != null) {
            binding.btnAction.setImageDrawable(pauseIcon);
        } else {
            binding.btnAction.setImageResource(R.drawable.cometchat_pause_recording);
        }
        binding.btnAction.setColorFilter(pauseButtonIconColor, PorterDuff.Mode.SRC_IN);
        binding.btnAction.setContentDescription(getContext().getString(R.string.cometchat_pause));

        // Update duration text
        binding.tvDuration.setText(InlineAudioRecorderViewModel.formatDuration(state.getDuration()));

        // Update waveform
        binding.waveformVisualizer.setIsAnimating(true);
        binding.waveformVisualizer.setIsPlaying(false);
        binding.waveformVisualizer.setBarColor(waveformRecordingColor);
        binding.waveformVisualizer.setAllowSeeking(false);

        // Add new amplitudes to waveform
        List<Float> amplitudes = state.getAmplitudes();
        if (amplitudes != null && !amplitudes.isEmpty()) {
            // Get the last amplitude and add it
            float lastAmplitude = amplitudes.get(amplitudes.size() - 1);
            binding.waveformVisualizer.addAmplitude(lastAmplitude);
        }
    }

    /**
     * Shows the paused state UI.
     * <p>
     * Note: In PAUSED state, the recording can still be resumed. The play button
     * will trigger playback which will first stop the recording to finalize the file.
     * </p>
     *
     * @param state The current state containing recording data.
     */
    private void showPausedState(InlineAudioRecorderState state) {
        // Hide recording indicator, show play button
        binding.recordingIndicator.setVisibility(View.GONE);
        binding.btnPlay.setVisibility(View.VISIBLE);
        binding.btnPausePlayback.setVisibility(View.GONE);

        // Show action button with mic icon (to resume recording)
        binding.btnAction.setVisibility(View.VISIBLE);
        if (micIcon != null) {
            binding.btnAction.setImageDrawable(micIcon);
        } else {
            binding.btnAction.setImageResource(R.drawable.cometchat_ic_mic);
        }
        binding.btnAction.setColorFilter(recordButtonIconColor, PorterDuff.Mode.SRC_IN);
        binding.btnAction.setContentDescription(getContext().getString(R.string.cometchat_record));

        // Update duration text (show total duration)
        binding.tvDuration.setText(InlineAudioRecorderViewModel.formatDuration(state.getDuration()));

        // Update waveform for playback mode
        binding.waveformVisualizer.setIsAnimating(false);
        binding.waveformVisualizer.setIsPlaying(false);
        binding.waveformVisualizer.setAmplitudes(state.getAmplitudes());
        binding.waveformVisualizer.setPlayedBarColor(waveformPlayingColor);
        binding.waveformVisualizer.setUnplayedBarColor(waveformUnplayedColor);
        binding.waveformVisualizer.setAllowSeeking(false); // Can't seek while recording is paused (file not finalized)
    }

    /**
     * Shows the playing state UI.
     * <p>
     * In PLAYING state, the recording has been finalized and is being played back.
     * Recording cannot be resumed from this state.
     * </p>
     *
     * @param state The current state containing playback data.
     */
    private void showPlayingState(InlineAudioRecorderState state) {
        // Hide recording indicator, show pause playback button
        binding.recordingIndicator.setVisibility(View.GONE);
        binding.btnPlay.setVisibility(View.GONE);
        binding.btnPausePlayback.setVisibility(View.VISIBLE);

        // Hide action button - recording cannot be resumed after file is finalized
        binding.btnAction.setVisibility(View.GONE);

        // Update duration text (show current playback position)
        binding.tvDuration.setText(InlineAudioRecorderViewModel.formatDuration(state.getCurrentPosition()));

        // Update waveform for playback mode with progress
        binding.waveformVisualizer.setIsAnimating(false);
        binding.waveformVisualizer.setIsPlaying(true);
        binding.waveformVisualizer.setAmplitudes(state.getAmplitudes());
        binding.waveformVisualizer.setPlayedBarColor(waveformPlayingColor);
        binding.waveformVisualizer.setUnplayedBarColor(waveformUnplayedColor);
        binding.waveformVisualizer.setAllowSeeking(true);

        // Calculate and set playback progress
        if (state.getDuration() > 0) {
            float progress = (float) state.getCurrentPosition() / state.getDuration();
            binding.waveformVisualizer.setPlaybackProgress(progress);
        }
    }

    /**
     * Shows the completed state UI.
     * <p>
     * In COMPLETED state, the recording has been finalized and can be played back
     * or sent. The mic button is shown to allow continuing the recording by
     * appending a new segment.
     * </p>
     *
     * @param state The current state containing recording data.
     */
    private void showCompletedState(InlineAudioRecorderState state) {
        // Hide recording indicator, show play button
        binding.recordingIndicator.setVisibility(View.GONE);
        binding.btnPlay.setVisibility(View.VISIBLE);
        binding.btnPausePlayback.setVisibility(View.GONE);

        // Show action button with mic icon (to continue recording by appending)
        binding.btnAction.setVisibility(View.VISIBLE);
        if (micIcon != null) {
            binding.btnAction.setImageDrawable(micIcon);
        } else {
            binding.btnAction.setImageResource(R.drawable.cometchat_ic_mic);
        }
        binding.btnAction.setColorFilter(recordButtonIconColor, PorterDuff.Mode.SRC_IN);
        binding.btnAction.setContentDescription(getContext().getString(R.string.cometchat_record));

        // Update duration text (show total duration)
        binding.tvDuration.setText(InlineAudioRecorderViewModel.formatDuration(state.getDuration()));

        // Update waveform for playback mode
        binding.waveformVisualizer.setIsAnimating(false);
        binding.waveformVisualizer.setIsPlaying(false);
        binding.waveformVisualizer.setAmplitudes(state.getAmplitudes());
        binding.waveformVisualizer.setPlayedBarColor(waveformPlayingColor);
        binding.waveformVisualizer.setUnplayedBarColor(waveformUnplayedColor);
        binding.waveformVisualizer.setAllowSeeking(true);

        // Preserve playback progress so the waveform doesn't jump back to
        // the start when the user pauses playback. currentPosition > 0 means
        // we're in a paused-playback state rather than a fresh completion.
        if (state.getDuration() > 0 && state.getCurrentPosition() > 0) {
            float progress = (float) state.getCurrentPosition() / state.getDuration();
            binding.waveformVisualizer.setPlaybackProgress(Math.min(progress, 1f));
        } else {
            binding.waveformVisualizer.setPlaybackProgress(0f);
        }
    }

    /**
     * Shows the error state UI.
     *
     * @param state The current state containing error information.
     */
    private void showErrorState(InlineAudioRecorderState state) {
        // Show idle state UI
        showIdleState();

        // Trigger error callback
        if (onErrorListener != null && state.getErrorMessage() != null) {
            onErrorListener.onError(new Exception(state.getErrorMessage()));
        }
    }


    // ==================== Click Listeners ====================

    /**
     * Sets up click listeners for all interactive elements.
     */
    private void setupClickListeners() {
        // Delete button - cancel recording
        binding.btnDelete.setOnClickListener(v -> {
            cancelRecording();
            if (onCancelListener != null) {
                onCancelListener.onCancel();
            }
        });
        // Remove pressed state visual feedback
        binding.btnDelete.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                v.performClick();
            }
            return true;
        });

        // Play button - start playback
        binding.btnPlay.setOnClickListener(v -> {
            if (viewModel != null) {
                viewModel.playRecording();
            }
        });
        // Remove pressed state visual feedback
        binding.btnPlay.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                v.performClick();
            }
            return true;
        });

        // Pause playback button - pause playback
        binding.btnPausePlayback.setOnClickListener(v -> {
            if (viewModel != null) {
                viewModel.pausePlayback();
            }
        });
        // Remove pressed state visual feedback
        binding.btnPausePlayback.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                v.performClick();
            }
            return true;
        });

        // Action button - pause recording or resume recording based on state
        binding.btnAction.setOnClickListener(v -> {
            if (viewModel != null) {
                InlineAudioRecorderState state = viewModel.getState().getValue();
                if (state != null) {
                    if (state.getStatus() == InlineAudioRecorderStatus.RECORDING) {
                        // Pause recording
                        viewModel.pauseRecording();
                    } else if (state.getStatus() == InlineAudioRecorderStatus.PAUSED ||
                               state.getStatus() == InlineAudioRecorderStatus.PLAYING ||
                               state.getStatus() == InlineAudioRecorderStatus.COMPLETED) {
                        // Resume recording
                        viewModel.resumeRecording();
                    }
                }
            }
        });
        // Remove pressed state visual feedback
        binding.btnAction.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                v.performClick();
            }
            return true;
        });

        // Send button - stop recording and submit
        binding.btnSend.setOnClickListener(v -> {
            if (viewModel != null) {
                // Get amplitudes before stopping (they're cleared on stop)
                InlineAudioRecorderState state = viewModel.getState().getValue();
                List<Float> amplitudes = state != null ? new ArrayList<>(state.getAmplitudes()) : new ArrayList<>();
                
                String filePath = viewModel.stopRecording();
                if (filePath != null) {
                    // Call the new listener with amplitudes if set
                    if (onSubmitWithWaveformListener != null) {
                        onSubmitWithWaveformListener.onSubmit(filePath, amplitudes);
                    } else if (onSubmitListener != null) {
                        // Fallback to old listener for backward compatibility
                        onSubmitListener.onSubmit(filePath);
                    }
                } else if (onErrorListener != null) {
                    onErrorListener.onError(new Exception("Recording file not found or empty"));
                }
            }
        });
        // Remove pressed state visual feedback
        binding.btnSend.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                v.performClick();
            }
            return true;
        });

        // Waveform seek listener
        binding.waveformVisualizer.setOnSeekListener(progress -> {
            if (viewModel != null) {
                viewModel.seekTo(progress);
            }
        });
    }

    // ==================== Permission Handling and Auto-Start ====================

    /**
     * Requests microphone permission and starts recording if granted.
     * This method should be called when the user wants to start recording.
     */
    public void requestPermissionAndStartRecording() {
        if (permissionHandlerBuilder != null) {
            permissionHandlerBuilder.withPermissions(permissions).check();
        }
    }

    /**
     * Starts recording internally after permission is granted.
     */
    private void startRecordingInternal() {
        if (viewModel != null) {
            // Use getExternalCacheDir() like CometChatMediaRecorder for better compatibility
            File outputDir = getContext().getExternalCacheDir();
            if (outputDir != null) {
                boolean started = viewModel.startRecording(outputDir);
                if (!started && onErrorListener != null) {
                    String errorMessage = viewModel.getErrorMessage();
                    onErrorListener.onError(new Exception(errorMessage != null ? errorMessage : "Failed to start recording"));
                }
            } else {
                if (onErrorListener != null) {
                    onErrorListener.onError(new Exception("Unable to access storage directory"));
                }
            }
        }
    }

    /**
     * Sets whether recording should start automatically when the component is shown.
     *
     * @param autoStart true to auto-start recording, false otherwise.
     */
    public void setAutoStartRecording(boolean autoStart) {
        this.autoStartRecording = autoStart;
    }

    /**
     * Gets whether auto-start recording is enabled.
     *
     * @return true if auto-start is enabled, false otherwise.
     */
    public boolean isAutoStartRecording() {
        return autoStartRecording;
    }

    /**
     * Starts recording. This will request permission if not already granted.
     */
    public void startRecording() {
        requestPermissionAndStartRecording();
    }

    /**
     * Stops the current recording and returns the file path.
     *
     * @return The path to the recorded file, or null if no recording exists.
     */
    public String stopRecording() {
        if (viewModel != null) {
            return viewModel.stopRecording();
        }
        return null;
    }

    /**
     * Cancels the current recording and deletes the file.
     */
    public void cancelRecording() {
        if (viewModel != null) {
            viewModel.cancelRecording();
        }
    }

    /**
     * Resets the recorder to its initial idle state.
     * This clears the waveform, resets the ViewModel state, and prepares
     * the recorder for a new recording session.
     * <p>
     * Use this method after submitting a recording to ensure the next
     * recording starts with a clean state.
     * </p>
     */
    public void reset() {
        if (viewModel != null) {
            viewModel.reset();
        }
        // Also reset the UI directly
        showIdleState();
    }


    // ==================== Styling ====================

    /**
     * Applies styling attributes from the provided attribute set and default style.
     *
     * @param attrs        The attribute set from XML layout.
     * @param defStyleAttr The default style attribute to apply.
     */
    private void applyStyleAttributes(AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.CometChatInlineAudioRecorder, defStyleAttr, 0);
        @StyleRes int styleResId = typedArray.getResourceId(R.styleable.CometChatInlineAudioRecorder_cometchatInlineAudioRecorderStyle, 0);
        typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatInlineAudioRecorder, defStyleAttr, styleResId);
        try {
            loadAttributesFromTypedArray(typedArray);
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Loads style attributes from the TypedArray and applies defaults.
     *
     * @param typedArray The TypedArray containing style attributes.
     */
    private void loadAttributesFromTypedArray(TypedArray typedArray) {
        // Background color
        backgroundColor = typedArray.getColor(
            R.styleable.CometChatInlineAudioRecorder_cometchatInlineAudioRecorderBackgroundColor,
            CometChatTheme.getBackgroundColor1(getContext())
        );

        // Waveform colors
        waveformRecordingColor = typedArray.getColor(
            R.styleable.CometChatInlineAudioRecorder_cometchatInlineAudioRecorderWaveformRecordingColor,
            CometChatTheme.getPrimaryColor(getContext())
        );
        waveformPlayingColor = typedArray.getColor(
            R.styleable.CometChatInlineAudioRecorder_cometchatInlineAudioRecorderWaveformPlayingColor,
            CometChatTheme.getPrimaryColor(getContext())
        );
        waveformUnplayedColor = typedArray.getColor(
            R.styleable.CometChatInlineAudioRecorder_cometchatInlineAudioRecorderWaveformUnplayedColor,
            CometChatTheme.getNeutralColor300(getContext())
        );

        // Duration text
        durationTextColor = typedArray.getColor(
            R.styleable.CometChatInlineAudioRecorder_cometchatInlineAudioRecorderDurationTextColor,
            CometChatTheme.getTextColorSecondary(getContext())
        );
        durationTextAppearance = typedArray.getResourceId(
            R.styleable.CometChatInlineAudioRecorder_cometchatInlineAudioRecorderDurationTextAppearance,
            0
        );

        // Button icon colors
        deleteButtonIconColor = typedArray.getColor(
            R.styleable.CometChatInlineAudioRecorder_cometchatInlineAudioRecorderDeleteButtonIconColor,
            CometChatTheme.getIconTintSecondary(getContext())
        );
        sendButtonIconColor = typedArray.getColor(
            R.styleable.CometChatInlineAudioRecorder_cometchatInlineAudioRecorderSendButtonIconColor,
            CometChatTheme.getColorWhite(getContext())
        );
        sendButtonBackgroundColor = typedArray.getColor(
            R.styleable.CometChatInlineAudioRecorder_cometchatInlineAudioRecorderSendButtonBackgroundColor,
            CometChatTheme.getPrimaryColor(getContext())
        );
        playButtonIconColor = typedArray.getColor(
            R.styleable.CometChatInlineAudioRecorder_cometchatInlineAudioRecorderPlayButtonIconColor,
            CometChatTheme.getIconTintSecondary(getContext())
        );
        pauseButtonIconColor = typedArray.getColor(
            R.styleable.CometChatInlineAudioRecorder_cometchatInlineAudioRecorderPauseButtonIconColor,
            CometChatTheme.getIconTintSecondary(getContext())
        );
        recordButtonIconColor = typedArray.getColor(
            R.styleable.CometChatInlineAudioRecorder_cometchatInlineAudioRecorderRecordButtonIconColor,
            CometChatTheme.getIconTintSecondary(getContext())
        );

        // Recording indicator color
        recordingIndicatorColor = typedArray.getColor(
            R.styleable.CometChatInlineAudioRecorder_cometchatInlineAudioRecorderRecordingIndicatorColor,
            CometChatTheme.getErrorColor(getContext())
        );

        // Custom icons
        deleteIcon = typedArray.getDrawable(R.styleable.CometChatInlineAudioRecorder_cometchatInlineAudioRecorderDeleteIcon);
        recordIcon = typedArray.getDrawable(R.styleable.CometChatInlineAudioRecorder_cometchatInlineAudioRecorderRecordIcon);
        pauseIcon = typedArray.getDrawable(R.styleable.CometChatInlineAudioRecorder_cometchatInlineAudioRecorderPauseIcon);
        playIcon = typedArray.getDrawable(R.styleable.CometChatInlineAudioRecorder_cometchatInlineAudioRecorderPlayIcon);
        sendIcon = typedArray.getDrawable(R.styleable.CometChatInlineAudioRecorder_cometchatInlineAudioRecorderSendIcon);
        micIcon = typedArray.getDrawable(R.styleable.CometChatInlineAudioRecorder_cometchatInlineAudioRecorderMicIcon);

        // Apply defaults
        applyDefaults();
    }

    /**
     * Applies default styling to all UI elements.
     */
    private void applyDefaults() {
        // Background
        setCardBackgroundColor(backgroundColor);

        // Duration text
        binding.tvDuration.setTextColor(durationTextColor);
        if (durationTextAppearance != 0) {
            binding.tvDuration.setTextAppearance(durationTextAppearance);
        }

        // Delete button
        if (deleteIcon != null) {
            binding.btnDelete.setImageDrawable(deleteIcon);
        }
        binding.btnDelete.setColorFilter(deleteButtonIconColor, PorterDuff.Mode.SRC_IN);

        // Play button
        if (playIcon != null) {
            binding.btnPlay.setImageDrawable(playIcon);
        }
        binding.btnPlay.setColorFilter(playButtonIconColor, PorterDuff.Mode.SRC_IN);

        // Pause playback button
        if (pauseIcon != null) {
            binding.btnPausePlayback.setImageDrawable(pauseIcon);
        }
        binding.btnPausePlayback.setColorFilter(pauseButtonIconColor, PorterDuff.Mode.SRC_IN);

        // Send button - use the same pattern as message composer (no tint, drawable has colors)
        if (sendIcon != null) {
            binding.btnSend.setImageDrawable(sendIcon);
            binding.btnSend.setColorFilter(sendButtonIconColor, PorterDuff.Mode.SRC_IN);
        } else {
            // Use the active send button drawable without tint (it has built-in colors)
            binding.btnSend.setImageResource(R.drawable.cometchat_ic_send_active);
            binding.btnSend.clearColorFilter();
        }
        binding.cardSend.setCardBackgroundColor(sendButtonBackgroundColor);

        // Recording indicator
        GradientDrawable indicatorDrawable = (GradientDrawable) binding.recordingIndicator.getBackground();
        if (indicatorDrawable != null) {
            indicatorDrawable.setColor(recordingIndicatorColor);
        }

        // Waveform visualizer
        binding.waveformVisualizer.setBarColor(waveformRecordingColor);
        binding.waveformVisualizer.setPlayedBarColor(waveformPlayingColor);
        binding.waveformVisualizer.setUnplayedBarColor(waveformUnplayedColor);
    }

    /**
     * Sets the style from a style resource.
     *
     * @param styleResId The style resource ID to apply.
     */
    public void setStyle(@StyleRes int styleResId) {
        if (styleResId == 0) return;
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(styleResId, R.styleable.CometChatInlineAudioRecorder);
        try {
            loadAttributesFromTypedArray(typedArray);
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Applies a CometChatInlineAudioRecorderStyle to this component.
     *
     * @param style The style configuration to apply.
     */
    public void applyStyle(@NonNull CometChatInlineAudioRecorderStyle style) {
        // Colors
        backgroundColor = style.getBackgroundColor();
        waveformRecordingColor = style.getWaveformRecordingColor();
        waveformPlayingColor = style.getWaveformPlayingColor();
        waveformUnplayedColor = style.getWaveformUnplayedColor();
        durationTextColor = style.getDurationTextColor();
        durationTextAppearance = style.getDurationTextAppearance();
        deleteButtonIconColor = style.getDeleteButtonIconColor();
        sendButtonIconColor = style.getSendButtonIconColor();
        sendButtonBackgroundColor = style.getSendButtonBackgroundColor();
        playButtonIconColor = style.getPlayButtonIconColor();
        pauseButtonIconColor = style.getPauseButtonIconColor();
        recordButtonIconColor = style.getRecordButtonIconColor();
        recordingIndicatorColor = style.getRecordingIndicatorColor();

        // Icons
        if (style.getDeleteIcon() != null) deleteIcon = style.getDeleteIcon();
        if (style.getRecordIcon() != null) recordIcon = style.getRecordIcon();
        if (style.getPauseIcon() != null) pauseIcon = style.getPauseIcon();
        if (style.getPlayIcon() != null) playIcon = style.getPlayIcon();
        if (style.getSendIcon() != null) sendIcon = style.getSendIcon();
        if (style.getMicIcon() != null) micIcon = style.getMicIcon();

        // Apply
        applyDefaults();
    }


    // ==================== Callbacks ====================

    /**
     * Sets the listener for submit events.
     *
     * @param listener The OnSubmitListener to set.
     */
    public void setOnSubmitListener(@Nullable OnSubmitListener listener) {
        this.onSubmitListener = listener;
    }
    
    /**
     * Sets the listener for submit events with waveform data.
     * Use this instead of setOnSubmitListener to receive amplitude data for waveform visualization.
     * If both listeners are set, only this one will be called.
     *
     * @param listener The OnSubmitWithWaveformListener to set.
     */
    public void setOnSubmitWithWaveformListener(@Nullable OnSubmitWithWaveformListener listener) {
        this.onSubmitWithWaveformListener = listener;
    }

    /**
     * Sets the listener for cancel events.
     *
     * @param listener The OnCancelListener to set.
     */
    public void setOnCancelListener(@Nullable OnCancelListener listener) {
        this.onCancelListener = listener;
    }

    /**
     * Sets the listener for error events.
     *
     * @param listener The OnErrorListener to set.
     */
    public void setOnErrorListener(@Nullable OnErrorListener listener) {
        this.onErrorListener = listener;
    }

    // ==================== Lifecycle Cleanup ====================

    /**
     * Called when the view is detached from the window.
     * Cancels any active recording to prevent resource leaks.
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // Remove lifecycle observer to prevent memory leaks
        if (lifecycleOwner != null && lifecycleEventObserver != null) {
            lifecycleOwner.getLifecycle().removeObserver(lifecycleEventObserver);
        }
        // Remove AudioPlayer listener to prevent memory leaks
        AudioPlayer.getInstance().setOnPlaybackStartListener(null);
        // Cancel recording to clean up resources
        if (viewModel != null) {
            InlineAudioRecorderState state = viewModel.getState().getValue();
            if (state != null && (state.isRecording() || state.isPaused() || state.isPlaying())) {
                viewModel.cancelRecording();
            }
        }
    }

    // ==================== Getters for Style Properties ====================

    /**
     * Gets the background color.
     *
     * @return The background color.
     */
    public @ColorInt int getBackgroundColorValue() {
        return backgroundColor;
    }

    /**
     * Gets the waveform recording color.
     *
     * @return The waveform recording color.
     */
    public @ColorInt int getWaveformRecordingColor() {
        return waveformRecordingColor;
    }

    /**
     * Gets the waveform playing color.
     *
     * @return The waveform playing color.
     */
    public @ColorInt int getWaveformPlayingColor() {
        return waveformPlayingColor;
    }

    /**
     * Gets the waveform unplayed color.
     *
     * @return The waveform unplayed color.
     */
    public @ColorInt int getWaveformUnplayedColor() {
        return waveformUnplayedColor;
    }

    /**
     * Gets the duration text color.
     *
     * @return The duration text color.
     */
    public @ColorInt int getDurationTextColor() {
        return durationTextColor;
    }

    /**
     * Gets the delete button icon color.
     *
     * @return The delete button icon color.
     */
    public @ColorInt int getDeleteButtonIconColor() {
        return deleteButtonIconColor;
    }

    /**
     * Gets the send button icon color.
     *
     * @return The send button icon color.
     */
    public @ColorInt int getSendButtonIconColor() {
        return sendButtonIconColor;
    }

    /**
     * Gets the send button background color.
     *
     * @return The send button background color.
     */
    public @ColorInt int getSendButtonBackgroundColor() {
        return sendButtonBackgroundColor;
    }

    /**
     * Gets the play button icon color.
     *
     * @return The play button icon color.
     */
    public @ColorInt int getPlayButtonIconColor() {
        return playButtonIconColor;
    }

    /**
     * Gets the pause button icon color.
     *
     * @return The pause button icon color.
     */
    public @ColorInt int getPauseButtonIconColor() {
        return pauseButtonIconColor;
    }

    /**
     * Gets the record button icon color.
     *
     * @return The record button icon color.
     */
    public @ColorInt int getRecordButtonIconColor() {
        return recordButtonIconColor;
    }

    /**
     * Gets the recording indicator color.
     *
     * @return The recording indicator color.
     */
    public @ColorInt int getRecordingIndicatorColor() {
        return recordingIndicatorColor;
    }

    // ==================== Setters for Style Properties ====================

    /**
     * Sets the background color.
     *
     * @param color The background color to set.
     */
    public void setBackgroundColorValue(@ColorInt int color) {
        this.backgroundColor = color;
        setCardBackgroundColor(color);
    }

    /**
     * Sets the waveform recording color.
     *
     * @param color The waveform recording color to set.
     */
    public void setWaveformRecordingColor(@ColorInt int color) {
        this.waveformRecordingColor = color;
        binding.waveformVisualizer.setBarColor(color);
    }

    /**
     * Sets the waveform playing color.
     *
     * @param color The waveform playing color to set.
     */
    public void setWaveformPlayingColor(@ColorInt int color) {
        this.waveformPlayingColor = color;
        binding.waveformVisualizer.setPlayedBarColor(color);
    }

    /**
     * Sets the waveform unplayed color.
     *
     * @param color The waveform unplayed color to set.
     */
    public void setWaveformUnplayedColor(@ColorInt int color) {
        this.waveformUnplayedColor = color;
        binding.waveformVisualizer.setUnplayedBarColor(color);
    }

    /**
     * Sets the duration text color.
     *
     * @param color The duration text color to set.
     */
    public void setDurationTextColor(@ColorInt int color) {
        this.durationTextColor = color;
        binding.tvDuration.setTextColor(color);
    }

    /**
     * Sets the delete button icon color.
     *
     * @param color The delete button icon color to set.
     */
    public void setDeleteButtonIconColor(@ColorInt int color) {
        this.deleteButtonIconColor = color;
        binding.btnDelete.setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    /**
     * Sets the send button icon color.
     *
     * @param color The send button icon color to set.
     */
    public void setSendButtonIconColor(@ColorInt int color) {
        this.sendButtonIconColor = color;
        binding.btnSend.setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    /**
     * Sets the send button background color.
     *
     * @param color The send button background color to set.
     */
    public void setSendButtonBackgroundColor(@ColorInt int color) {
        this.sendButtonBackgroundColor = color;
        binding.cardSend.setCardBackgroundColor(color);
    }

    /**
     * Sets the play button icon color.
     *
     * @param color The play button icon color to set.
     */
    public void setPlayButtonIconColor(@ColorInt int color) {
        this.playButtonIconColor = color;
        binding.btnPlay.setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    /**
     * Sets the pause button icon color.
     *
     * @param color The pause button icon color to set.
     */
    public void setPauseButtonIconColor(@ColorInt int color) {
        this.pauseButtonIconColor = color;
        binding.btnPausePlayback.setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    /**
     * Sets the record button icon color.
     *
     * @param color The record button icon color to set.
     */
    public void setRecordButtonIconColor(@ColorInt int color) {
        this.recordButtonIconColor = color;
    }

    /**
     * Sets the recording indicator color.
     *
     * @param color The recording indicator color to set.
     */
    public void setRecordingIndicatorColor(@ColorInt int color) {
        this.recordingIndicatorColor = color;
        GradientDrawable indicatorDrawable = (GradientDrawable) binding.recordingIndicator.getBackground();
        if (indicatorDrawable != null) {
            indicatorDrawable.setColor(color);
        }
    }

    // ==================== Custom Icon Setters ====================

    /**
     * Sets the delete button icon.
     *
     * @param icon The drawable to use for the delete button.
     */
    public void setDeleteIcon(@Nullable Drawable icon) {
        this.deleteIcon = icon;
        if (icon != null) {
            binding.btnDelete.setImageDrawable(icon);
        }
    }

    /**
     * Sets the play button icon.
     *
     * @param icon The drawable to use for the play button.
     */
    public void setPlayIcon(@Nullable Drawable icon) {
        this.playIcon = icon;
        if (icon != null) {
            binding.btnPlay.setImageDrawable(icon);
        }
    }

    /**
     * Sets the pause button icon.
     *
     * @param icon The drawable to use for the pause button.
     */
    public void setPauseIcon(@Nullable Drawable icon) {
        this.pauseIcon = icon;
        if (icon != null) {
            binding.btnPausePlayback.setImageDrawable(icon);
        }
    }

    /**
     * Sets the send button icon.
     *
     * @param icon The drawable to use for the send button.
     */
    public void setSendIcon(@Nullable Drawable icon) {
        this.sendIcon = icon;
        if (icon != null) {
            binding.btnSend.setImageDrawable(icon);
        }
    }

    /**
     * Sets the mic button icon.
     *
     * @param icon The drawable to use for the mic button.
     */
    public void setMicIcon(@Nullable Drawable icon) {
        this.micIcon = icon;
    }

    /**
     * Sets the record button icon.
     *
     * @param icon The drawable to use for the record button.
     */
    public void setRecordIcon(@Nullable Drawable icon) {
        this.recordIcon = icon;
    }
}
