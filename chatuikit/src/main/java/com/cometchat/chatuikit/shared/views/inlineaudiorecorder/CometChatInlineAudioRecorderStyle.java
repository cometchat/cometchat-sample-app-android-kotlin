package com.cometchat.chatuikit.shared.views.inlineaudiorecorder;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;

import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.shared.models.BaseStyle;

/**
 * Style configuration class for CometChatInlineAudioRecorder.
 * <p>
 * This class provides a fluent Builder pattern for configuring the visual appearance
 * of the inline audio recorder component, including colors, dimensions, and icons.
 * </p>
 * <p>
 * Use {@link #getDefault(Context)} to get a style with theme-appropriate defaults,
 * or use the {@link Builder} to create a custom configuration.
 * </p>
 *
 * @see CometChatInlineAudioRecorder
 */
public class CometChatInlineAudioRecorderStyle extends BaseStyle {
    private static final String TAG = CometChatInlineAudioRecorderStyle.class.getSimpleName();

    // Colors
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

    // Dimensions
    private @Dimension float waveformBarWidth;
    private @Dimension float waveformBarSpacing;
    private @Dimension float waveformMinBarHeight;
    private @Dimension float waveformMaxBarHeight;
    private int waveformMaxBarCount;

    // Icons
    private Drawable deleteIcon;
    private Drawable recordIcon;
    private Drawable pauseIcon;
    private Drawable playIcon;
    private Drawable sendIcon;
    private Drawable micIcon;

    /**
     * Private constructor. Use {@link Builder} to create instances.
     */
    private CometChatInlineAudioRecorderStyle() {
    }


    /**
     * Creates a default style configuration using CometChatTheme defaults.
     *
     * @param context The context used to access theme resources.
     * @return A new CometChatInlineAudioRecorderStyle with theme-appropriate defaults.
     */
    public static CometChatInlineAudioRecorderStyle getDefault(@NonNull Context context) {
        return new Builder()
            .setBackgroundColor(CometChatTheme.getBackgroundColor1(context))
            .setWaveformRecordingColor(CometChatTheme.getPrimaryColor(context))
            .setWaveformPlayingColor(CometChatTheme.getPrimaryColor(context))
            .setWaveformUnplayedColor(CometChatTheme.getNeutralColor300(context))
            .setDurationTextColor(CometChatTheme.getTextColorSecondary(context))
            .setDeleteButtonIconColor(CometChatTheme.getIconTintSecondary(context))
            .setSendButtonIconColor(CometChatTheme.getColorWhite(context))
            .setSendButtonBackgroundColor(CometChatTheme.getPrimaryColor(context))
            .setPlayButtonIconColor(CometChatTheme.getIconTintSecondary(context))
            .setPauseButtonIconColor(CometChatTheme.getIconTintSecondary(context))
            .setRecordButtonIconColor(CometChatTheme.getIconTintSecondary(context))
            .setRecordingIndicatorColor(CometChatTheme.getErrorColor(context))
            .setWaveformBarWidth(3f)
            .setWaveformBarSpacing(2f)
            .setWaveformMinBarHeight(4f)
            .setWaveformMaxBarHeight(32f)
            .setWaveformMaxBarCount(35)
            .build();
    }

    // Getters

    /**
     * Returns the background color of the inline audio recorder.
     *
     * @return The background color.
     */
    public @ColorInt int getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Returns the waveform color used during recording.
     *
     * @return The recording waveform color.
     */
    public @ColorInt int getWaveformRecordingColor() {
        return waveformRecordingColor;
    }

    /**
     * Returns the waveform color for played portions during playback.
     *
     * @return The playing waveform color.
     */
    public @ColorInt int getWaveformPlayingColor() {
        return waveformPlayingColor;
    }

    /**
     * Returns the waveform color for unplayed portions during playback.
     *
     * @return The unplayed waveform color.
     */
    public @ColorInt int getWaveformUnplayedColor() {
        return waveformUnplayedColor;
    }

    /**
     * Returns the duration text color.
     *
     * @return The duration text color.
     */
    public @ColorInt int getDurationTextColor() {
        return durationTextColor;
    }

    /**
     * Returns the duration text appearance style resource.
     *
     * @return The text appearance style resource ID.
     */
    public @StyleRes int getDurationTextAppearance() {
        return durationTextAppearance;
    }

    /**
     * Returns the delete button icon color.
     *
     * @return The delete button icon color.
     */
    public @ColorInt int getDeleteButtonIconColor() {
        return deleteButtonIconColor;
    }

    /**
     * Returns the send button icon color.
     *
     * @return The send button icon color.
     */
    public @ColorInt int getSendButtonIconColor() {
        return sendButtonIconColor;
    }

    /**
     * Returns the send button background color.
     *
     * @return The send button background color.
     */
    public @ColorInt int getSendButtonBackgroundColor() {
        return sendButtonBackgroundColor;
    }

    /**
     * Returns the play button icon color.
     *
     * @return The play button icon color.
     */
    public @ColorInt int getPlayButtonIconColor() {
        return playButtonIconColor;
    }

    /**
     * Returns the pause button icon color.
     *
     * @return The pause button icon color.
     */
    public @ColorInt int getPauseButtonIconColor() {
        return pauseButtonIconColor;
    }

    /**
     * Returns the record button icon color.
     *
     * @return The record button icon color.
     */
    public @ColorInt int getRecordButtonIconColor() {
        return recordButtonIconColor;
    }

    /**
     * Returns the recording indicator color.
     *
     * @return The recording indicator color.
     */
    public @ColorInt int getRecordingIndicatorColor() {
        return recordingIndicatorColor;
    }

    /**
     * Returns the waveform bar width in dp.
     *
     * @return The waveform bar width.
     */
    public @Dimension float getWaveformBarWidth() {
        return waveformBarWidth;
    }

    /**
     * Returns the waveform bar spacing in dp.
     *
     * @return The waveform bar spacing.
     */
    public @Dimension float getWaveformBarSpacing() {
        return waveformBarSpacing;
    }

    /**
     * Returns the minimum waveform bar height in dp.
     *
     * @return The minimum waveform bar height.
     */
    public @Dimension float getWaveformMinBarHeight() {
        return waveformMinBarHeight;
    }

    /**
     * Returns the maximum waveform bar height in dp.
     *
     * @return The maximum waveform bar height.
     */
    public @Dimension float getWaveformMaxBarHeight() {
        return waveformMaxBarHeight;
    }

    /**
     * Returns the maximum number of waveform bars to display.
     *
     * @return The maximum bar count.
     */
    public int getWaveformMaxBarCount() {
        return waveformMaxBarCount;
    }

    /**
     * Returns the delete icon drawable.
     *
     * @return The delete icon drawable, or null if not set.
     */
    public Drawable getDeleteIcon() {
        return deleteIcon;
    }

    /**
     * Returns the record icon drawable.
     *
     * @return The record icon drawable, or null if not set.
     */
    public Drawable getRecordIcon() {
        return recordIcon;
    }

    /**
     * Returns the pause icon drawable.
     *
     * @return The pause icon drawable, or null if not set.
     */
    public Drawable getPauseIcon() {
        return pauseIcon;
    }

    /**
     * Returns the play icon drawable.
     *
     * @return The play icon drawable, or null if not set.
     */
    public Drawable getPlayIcon() {
        return playIcon;
    }

    /**
     * Returns the send icon drawable.
     *
     * @return The send icon drawable, or null if not set.
     */
    public Drawable getSendIcon() {
        return sendIcon;
    }

    /**
     * Returns the mic icon drawable.
     *
     * @return The mic icon drawable, or null if not set.
     */
    public Drawable getMicIcon() {
        return micIcon;
    }


    /**
     * Builder class for creating CometChatInlineAudioRecorderStyle instances.
     * <p>
     * Provides a fluent API for configuring all style properties.
     * </p>
     */
    public static class Builder {
        private final CometChatInlineAudioRecorderStyle style;

        /**
         * Creates a new Builder instance.
         */
        public Builder() {
            style = new CometChatInlineAudioRecorderStyle();
        }

        /**
         * Sets the background color of the inline audio recorder.
         *
         * @param backgroundColor The background color.
         * @return This Builder for chaining.
         */
        public Builder setBackgroundColor(@ColorInt int backgroundColor) {
            style.backgroundColor = backgroundColor;
            return this;
        }

        /**
         * Sets the waveform color used during recording.
         *
         * @param waveformRecordingColor The recording waveform color.
         * @return This Builder for chaining.
         */
        public Builder setWaveformRecordingColor(@ColorInt int waveformRecordingColor) {
            style.waveformRecordingColor = waveformRecordingColor;
            return this;
        }

        /**
         * Sets the waveform color for played portions during playback.
         *
         * @param waveformPlayingColor The playing waveform color.
         * @return This Builder for chaining.
         */
        public Builder setWaveformPlayingColor(@ColorInt int waveformPlayingColor) {
            style.waveformPlayingColor = waveformPlayingColor;
            return this;
        }

        /**
         * Sets the waveform color for unplayed portions during playback.
         *
         * @param waveformUnplayedColor The unplayed waveform color.
         * @return This Builder for chaining.
         */
        public Builder setWaveformUnplayedColor(@ColorInt int waveformUnplayedColor) {
            style.waveformUnplayedColor = waveformUnplayedColor;
            return this;
        }

        /**
         * Sets the duration text color.
         *
         * @param durationTextColor The duration text color.
         * @return This Builder for chaining.
         */
        public Builder setDurationTextColor(@ColorInt int durationTextColor) {
            style.durationTextColor = durationTextColor;
            return this;
        }

        /**
         * Sets the duration text appearance style resource.
         *
         * @param durationTextAppearance The text appearance style resource ID.
         * @return This Builder for chaining.
         */
        public Builder setDurationTextAppearance(@StyleRes int durationTextAppearance) {
            style.durationTextAppearance = durationTextAppearance;
            return this;
        }

        /**
         * Sets the delete button icon color.
         *
         * @param deleteButtonIconColor The delete button icon color.
         * @return This Builder for chaining.
         */
        public Builder setDeleteButtonIconColor(@ColorInt int deleteButtonIconColor) {
            style.deleteButtonIconColor = deleteButtonIconColor;
            return this;
        }

        /**
         * Sets the send button icon color.
         *
         * @param sendButtonIconColor The send button icon color.
         * @return This Builder for chaining.
         */
        public Builder setSendButtonIconColor(@ColorInt int sendButtonIconColor) {
            style.sendButtonIconColor = sendButtonIconColor;
            return this;
        }

        /**
         * Sets the send button background color.
         *
         * @param sendButtonBackgroundColor The send button background color.
         * @return This Builder for chaining.
         */
        public Builder setSendButtonBackgroundColor(@ColorInt int sendButtonBackgroundColor) {
            style.sendButtonBackgroundColor = sendButtonBackgroundColor;
            return this;
        }

        /**
         * Sets the play button icon color.
         *
         * @param playButtonIconColor The play button icon color.
         * @return This Builder for chaining.
         */
        public Builder setPlayButtonIconColor(@ColorInt int playButtonIconColor) {
            style.playButtonIconColor = playButtonIconColor;
            return this;
        }

        /**
         * Sets the pause button icon color.
         *
         * @param pauseButtonIconColor The pause button icon color.
         * @return This Builder for chaining.
         */
        public Builder setPauseButtonIconColor(@ColorInt int pauseButtonIconColor) {
            style.pauseButtonIconColor = pauseButtonIconColor;
            return this;
        }

        /**
         * Sets the record button icon color.
         *
         * @param recordButtonIconColor The record button icon color.
         * @return This Builder for chaining.
         */
        public Builder setRecordButtonIconColor(@ColorInt int recordButtonIconColor) {
            style.recordButtonIconColor = recordButtonIconColor;
            return this;
        }

        /**
         * Sets the recording indicator color.
         *
         * @param recordingIndicatorColor The recording indicator color.
         * @return This Builder for chaining.
         */
        public Builder setRecordingIndicatorColor(@ColorInt int recordingIndicatorColor) {
            style.recordingIndicatorColor = recordingIndicatorColor;
            return this;
        }

        /**
         * Sets the waveform bar width in dp.
         *
         * @param waveformBarWidth The waveform bar width.
         * @return This Builder for chaining.
         */
        public Builder setWaveformBarWidth(@Dimension float waveformBarWidth) {
            style.waveformBarWidth = waveformBarWidth;
            return this;
        }

        /**
         * Sets the waveform bar spacing in dp.
         *
         * @param waveformBarSpacing The waveform bar spacing.
         * @return This Builder for chaining.
         */
        public Builder setWaveformBarSpacing(@Dimension float waveformBarSpacing) {
            style.waveformBarSpacing = waveformBarSpacing;
            return this;
        }

        /**
         * Sets the minimum waveform bar height in dp.
         *
         * @param waveformMinBarHeight The minimum waveform bar height.
         * @return This Builder for chaining.
         */
        public Builder setWaveformMinBarHeight(@Dimension float waveformMinBarHeight) {
            style.waveformMinBarHeight = waveformMinBarHeight;
            return this;
        }

        /**
         * Sets the maximum waveform bar height in dp.
         *
         * @param waveformMaxBarHeight The maximum waveform bar height.
         * @return This Builder for chaining.
         */
        public Builder setWaveformMaxBarHeight(@Dimension float waveformMaxBarHeight) {
            style.waveformMaxBarHeight = waveformMaxBarHeight;
            return this;
        }

        /**
         * Sets the maximum number of waveform bars to display.
         *
         * @param waveformMaxBarCount The maximum bar count.
         * @return This Builder for chaining.
         */
        public Builder setWaveformMaxBarCount(int waveformMaxBarCount) {
            style.waveformMaxBarCount = waveformMaxBarCount;
            return this;
        }

        /**
         * Sets the delete icon drawable.
         *
         * @param deleteIcon The delete icon drawable.
         * @return This Builder for chaining.
         */
        public Builder setDeleteIcon(Drawable deleteIcon) {
            style.deleteIcon = deleteIcon;
            return this;
        }

        /**
         * Sets the record icon drawable.
         *
         * @param recordIcon The record icon drawable.
         * @return This Builder for chaining.
         */
        public Builder setRecordIcon(Drawable recordIcon) {
            style.recordIcon = recordIcon;
            return this;
        }

        /**
         * Sets the pause icon drawable.
         *
         * @param pauseIcon The pause icon drawable.
         * @return This Builder for chaining.
         */
        public Builder setPauseIcon(Drawable pauseIcon) {
            style.pauseIcon = pauseIcon;
            return this;
        }

        /**
         * Sets the play icon drawable.
         *
         * @param playIcon The play icon drawable.
         * @return This Builder for chaining.
         */
        public Builder setPlayIcon(Drawable playIcon) {
            style.playIcon = playIcon;
            return this;
        }

        /**
         * Sets the send icon drawable.
         *
         * @param sendIcon The send icon drawable.
         * @return This Builder for chaining.
         */
        public Builder setSendIcon(Drawable sendIcon) {
            style.sendIcon = sendIcon;
            return this;
        }

        /**
         * Sets the mic icon drawable.
         *
         * @param micIcon The mic icon drawable.
         * @return This Builder for chaining.
         */
        public Builder setMicIcon(Drawable micIcon) {
            style.micIcon = micIcon;
            return this;
        }

        /**
         * Builds and returns the configured CometChatInlineAudioRecorderStyle.
         *
         * @return The configured style instance.
         */
        public CometChatInlineAudioRecorderStyle build() {
            return style;
        }
    }
}
