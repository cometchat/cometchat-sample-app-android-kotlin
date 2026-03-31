package com.cometchat.chatuikit.shared.views.waveform;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cometchat.chatuikit.CometChatTheme;

import java.util.ArrayList;
import java.util.List;

/**
 * A custom View that displays audio amplitude as animated vertical bars.
 * <p>
 * During recording mode (isAnimating=true), new bars appear on the right and scroll left.
 * During playback mode, bars show progress from unplayed color to played color based on playbackProgress.
 * </p>
 * <p>
 * The visualizer supports tap and drag gestures for seeking to a specific position.
 * </p>
 * <p>
 * This component is used by both the inline audio recorder and audio bubble views.
 * </p>
 */
public class AudioWaveformVisualizer extends View {
    private static final String TAG = AudioWaveformVisualizer.class.getSimpleName();

    // Default configuration values (in dp)
    private static final float DEFAULT_BAR_WIDTH_DP = 3f;
    private static final float DEFAULT_BAR_SPACING_DP = 2f;
    private static final float DEFAULT_MIN_BAR_HEIGHT_DP = 4f;
    private static final float DEFAULT_MAX_BAR_HEIGHT_DP = 32f;
    private static final float DEFAULT_BAR_CORNER_RADIUS_DP = 2f;
    private static final int DEFAULT_MAX_BAR_COUNT = 35;

    // Seek throttling
    private static final long SEEK_THROTTLE_MS = 100;

    // Configuration properties
    private @ColorInt int barColor;
    private @ColorInt int playedBarColor;
    private @ColorInt int unplayedBarColor;
    private float barWidth;
    private float barSpacing;
    private float minBarHeight;
    private float maxBarHeight;
    private int maxBarCount;
    private float barCornerRadius;

    // State properties
    private boolean isAnimating;
    private boolean isPlaying;
    private float playbackProgress;
    private boolean allowSeeking;

    // Data lists
    private List<Float> amplitudes;
    private List<Float> localBars;

    // Drawing
    private Paint barPaint;
    private RectF barRect;

    // Gesture detection
    private GestureDetector gestureDetector;
    private OnSeekListener onSeekListener;
    private long lastSeekTime;

    /**
     * Interface for receiving seek events from the waveform visualizer.
     */
    public interface OnSeekListener {
        /**
         * Called when the user seeks to a position by tapping or dragging.
         *
         * @param progress The seek progress in range [0.0, 1.0].
         */
        void onSeek(float progress);
    }

    /**
     * Constructs an AudioWaveformVisualizer with the specified context.
     *
     * @param context The context to use.
     */
    public AudioWaveformVisualizer(Context context) {
        super(context);
        init(context);
    }

    /**
     * Constructs an AudioWaveformVisualizer with the specified context and attributes.
     *
     * @param context The context to use.
     * @param attrs   The attribute set containing custom attributes.
     */
    public AudioWaveformVisualizer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * Constructs an AudioWaveformVisualizer with the specified context, attributes, and default style.
     *
     * @param context      The context to use.
     * @param attrs        The attribute set containing custom attributes.
     * @param defStyleAttr The default style to apply to this view.
     */
    public AudioWaveformVisualizer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * Initializes the view with default values and sets up drawing objects.
     *
     * @param context The context to use.
     */
    private void init(Context context) {
        // Initialize data lists
        amplitudes = new ArrayList<>();
        localBars = new ArrayList<>();

        // Initialize drawing objects
        barPaint = new Paint();
        barPaint.setStyle(Paint.Style.FILL);
        barPaint.setAntiAlias(true);
        barRect = new RectF();

        // Set default colors from theme
        barColor = CometChatTheme.getPrimaryColor(context);
        playedBarColor = CometChatTheme.getPrimaryColor(context);
        unplayedBarColor = CometChatTheme.getExtendedPrimaryColor600(context);

        // Convert dp to pixels for default values
        float density = context.getResources().getDisplayMetrics().density;
        barWidth = DEFAULT_BAR_WIDTH_DP * density;
        barSpacing = DEFAULT_BAR_SPACING_DP * density;
        minBarHeight = DEFAULT_MIN_BAR_HEIGHT_DP * density;
        maxBarHeight = DEFAULT_MAX_BAR_HEIGHT_DP * density;
        barCornerRadius = DEFAULT_BAR_CORNER_RADIUS_DP * density;
        maxBarCount = DEFAULT_MAX_BAR_COUNT;

        // Initialize state
        isAnimating = false;
        isPlaying = false;
        playbackProgress = 0f;
        allowSeeking = true;
        lastSeekTime = 0;

        // Setup gesture detector
        setupGestureDetector(context);
    }

    /**
     * Sets up the gesture detector for tap detection.
     *
     * @param context The context to use.
     */
    private void setupGestureDetector(Context context) {
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (allowSeeking && onSeekListener != null && !isAnimating) {
                    float progress = calculateProgressFromX(e.getX());
                    onSeekListener.onSeek(progress);
                    return true;
                }
                return false;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }
        });
    }

    // ==================== Configuration Getters and Setters ====================

    /**
     * Gets the bar color used during recording mode.
     *
     * @return The bar color.
     */
    public @ColorInt int getBarColor() {
        return barColor;
    }

    /**
     * Sets the bar color used during recording mode.
     *
     * @param barColor The bar color to set.
     */
    public void setBarColor(@ColorInt int barColor) {
        this.barColor = barColor;
        invalidate();
    }

    /**
     * Gets the color for played (past) bars during playback.
     *
     * @return The played bar color.
     */
    public @ColorInt int getPlayedBarColor() {
        return playedBarColor;
    }

    /**
     * Sets the color for played (past) bars during playback.
     *
     * @param playedBarColor The played bar color to set.
     */
    public void setPlayedBarColor(@ColorInt int playedBarColor) {
        this.playedBarColor = playedBarColor;
        invalidate();
    }

    /**
     * Gets the color for unplayed (future) bars during playback.
     *
     * @return The unplayed bar color.
     */
    public @ColorInt int getUnplayedBarColor() {
        return unplayedBarColor;
    }

    /**
     * Sets the color for unplayed (future) bars during playback.
     *
     * @param unplayedBarColor The unplayed bar color to set.
     */
    public void setUnplayedBarColor(@ColorInt int unplayedBarColor) {
        this.unplayedBarColor = unplayedBarColor;
        invalidate();
    }

    /**
     * Gets the width of each bar in pixels.
     *
     * @return The bar width in pixels.
     */
    public float getBarWidth() {
        return barWidth;
    }

    /**
     * Sets the width of each bar in pixels.
     *
     * @param barWidth The bar width in pixels.
     */
    public void setBarWidth(float barWidth) {
        this.barWidth = barWidth;
        invalidate();
    }

    /**
     * Gets the spacing between bars in pixels.
     *
     * @return The bar spacing in pixels.
     */
    public float getBarSpacing() {
        return barSpacing;
    }

    /**
     * Sets the spacing between bars in pixels.
     *
     * @param barSpacing The bar spacing in pixels.
     */
    public void setBarSpacing(float barSpacing) {
        this.barSpacing = barSpacing;
        invalidate();
    }

    /**
     * Gets the minimum bar height in pixels.
     *
     * @return The minimum bar height in pixels.
     */
    public float getMinBarHeight() {
        return minBarHeight;
    }

    /**
     * Sets the minimum bar height in pixels.
     *
     * @param minBarHeight The minimum bar height in pixels.
     */
    public void setMinBarHeight(float minBarHeight) {
        this.minBarHeight = minBarHeight;
        invalidate();
    }

    /**
     * Gets the maximum bar height in pixels.
     *
     * @return The maximum bar height in pixels.
     */
    public float getMaxBarHeight() {
        return maxBarHeight;
    }

    /**
     * Sets the maximum bar height in pixels.
     *
     * @param maxBarHeight The maximum bar height in pixels.
     */
    public void setMaxBarHeight(float maxBarHeight) {
        this.maxBarHeight = maxBarHeight;
        invalidate();
    }

    /**
     * Gets the maximum number of bars to display.
     *
     * @return The maximum bar count.
     */
    public int getMaxBarCount() {
        return maxBarCount;
    }

    /**
     * Sets the maximum number of bars to display.
     *
     * @param maxBarCount The maximum bar count.
     */
    public void setMaxBarCount(int maxBarCount) {
        this.maxBarCount = maxBarCount;
        invalidate();
    }

    /**
     * Gets the corner radius of bars in pixels.
     *
     * @return The bar corner radius in pixels.
     */
    public float getBarCornerRadius() {
        return barCornerRadius;
    }

    /**
     * Sets the corner radius of bars in pixels.
     *
     * @param barCornerRadius The bar corner radius in pixels.
     */
    public void setBarCornerRadius(float barCornerRadius) {
        this.barCornerRadius = barCornerRadius;
        invalidate();
    }

    // ==================== State Getters and Setters ====================

    /**
     * Checks if the visualizer is in animating (recording) mode.
     *
     * @return true if animating, false otherwise.
     */
    public boolean isAnimating() {
        return isAnimating;
    }

    /**
     * Sets whether the visualizer is in animating (recording) mode.
     * When animating, new bars appear on the right and scroll left.
     *
     * @param animating true to enable animation mode, false otherwise.
     */
    public void setIsAnimating(boolean animating) {
        this.isAnimating = animating;
        invalidate();
    }

    /**
     * Checks if the visualizer is in playing mode.
     *
     * @return true if playing, false otherwise.
     */
    public boolean isPlaying() {
        return isPlaying;
    }

    /**
     * Sets whether the visualizer is in playing mode.
     *
     * @param playing true if playing, false otherwise.
     */
    public void setIsPlaying(boolean playing) {
        this.isPlaying = playing;
        invalidate();
    }

    /**
     * Gets the current playback progress.
     *
     * @return The playback progress in range [0.0, 1.0].
     */
    public float getPlaybackProgress() {
        return playbackProgress;
    }

    /**
     * Sets the playback progress for coloring bars during playback.
     *
     * @param progress The playback progress in range [0.0, 1.0].
     */
    public void setPlaybackProgress(float progress) {
        this.playbackProgress = Math.max(0f, Math.min(1f, progress));
        invalidate();
    }

    /**
     * Checks if seeking is allowed.
     *
     * @return true if seeking is allowed, false otherwise.
     */
    public boolean isAllowSeeking() {
        return allowSeeking;
    }

    /**
     * Sets whether seeking via tap/drag is allowed.
     *
     * @param allowSeeking true to allow seeking, false otherwise.
     */
    public void setAllowSeeking(boolean allowSeeking) {
        this.allowSeeking = allowSeeking;
    }

    // ==================== Data Methods ====================

    /**
     * Gets the list of amplitudes for playback mode.
     *
     * @return The list of amplitude values.
     */
    public List<Float> getAmplitudes() {
        return new ArrayList<>(amplitudes);
    }

    /**
     * Sets the amplitudes for playback mode.
     *
     * @param amplitudes The list of amplitude values.
     */
    public void setAmplitudes(List<Float> amplitudes) {
        if (amplitudes == null || amplitudes.isEmpty()) {
            this.amplitudes = new ArrayList<>();
        } else if (amplitudes.size() < maxBarCount) {
            // Resample to fill maxBarCount bars so waveform fills the full width
            this.amplitudes = resampleAmplitudes(amplitudes, maxBarCount);
        } else {
            this.amplitudes = new ArrayList<>(amplitudes);
        }
        invalidate();
    }
    
    /**
     * Resamples amplitude data to a target number of bars using linear interpolation.
     * Used to ensure the waveform always fills the full width of the view.
     */
    private static List<Float> resampleAmplitudes(List<Float> source, int targetCount) {
        List<Float> result = new ArrayList<>(targetCount);
        int sourceSize = source.size();
        for (int i = 0; i < targetCount; i++) {
            float position = (float) i / (targetCount - 1) * (sourceSize - 1);
            int lower = (int) position;
            int upper = Math.min(lower + 1, sourceSize - 1);
            float fraction = position - lower;
            float value = source.get(lower) + fraction * (source.get(upper) - source.get(lower));
            result.add(value);
        }
        return result;
    }

    /**
     * Adds a single amplitude value for recording mode.
     * The amplitude is added to localBars and the view is redrawn.
     *
     * @param amplitude The amplitude value in range [0.0, 1.0].
     */
    public void addAmplitude(float amplitude) {
        float clampedAmplitude = Math.max(0f, Math.min(1f, amplitude));
        localBars.add(clampedAmplitude);
        
        // Limit to maxBarCount
        while (localBars.size() > maxBarCount) {
            localBars.remove(0);
        }
        
        invalidate();
    }

    /**
     * Clears all bars from the visualizer.
     */
    public void clearBars() {
        amplitudes.clear();
        localBars.clear();
        playbackProgress = 0f;
        invalidate();
    }
    
    /**
     * Generates flat/uniform waveform amplitudes.
     * Creates straight lines of equal height for initial/loading state.
     *
     * @return List of uniform amplitude values (35 bars)
     */
    public static List<Float> generateFlatAmplitudes() {
        return generateFlatAmplitudes(35);
    }
    
    /**
     * Generates flat/uniform waveform amplitudes with specified bar count.
     *
     * @param barCount Number of bars to generate
     * @return List of uniform amplitude values
     */
    public static List<Float> generateFlatAmplitudes(int barCount) {
        List<Float> amplitudes = new ArrayList<>();
        // Use a uniform height for all bars (0.4 gives a nice medium height)
        for (int i = 0; i < barCount; i++) {
            amplitudes.add(0.1f);
        }
        return amplitudes;
    }

    // ==================== Listener ====================

    /**
     * Sets the listener for seek events.
     *
     * @param listener The listener to set.
     */
    public void setOnSeekListener(OnSeekListener listener) {
        this.onSeekListener = listener;
    }

    // ==================== Drawing ====================

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        List<Float> barsToRender = isAnimating ? localBars : amplitudes;
        if (barsToRender.isEmpty()) {
            return;
        }

        int viewWidth = getWidth();
        int viewHeight = getHeight();
        float centerY = viewHeight / 2f;

        // Calculate how many bars we can display
        int barCount = Math.min(barsToRender.size(), maxBarCount);
        
        // Get the bars to display (most recent ones)
        int startIndex = Math.max(0, barsToRender.size() - barCount);
        List<Float> visibleBars = barsToRender.subList(startIndex, barsToRender.size());

        if (isAnimating) {
            // During recording, bars scroll from right to left with fixed bar width
            float totalBarsWidth = barCount * barWidth + (barCount - 1) * barSpacing;
            float startX = viewWidth - totalBarsWidth;

            for (int i = 0; i < visibleBars.size(); i++) {
                float amplitude = visibleBars.get(i);
                float barHeight = minBarHeight + amplitude * (maxBarHeight - minBarHeight);
                
                float left = startX + i * (barWidth + barSpacing);
                float top = centerY - barHeight / 2f;
                float right = left + barWidth;
                float bottom = centerY + barHeight / 2f;

                barPaint.setColor(barColor);
                barRect.set(left, top, right, bottom);
                canvas.drawRoundRect(barRect, barCornerRadius, barCornerRadius, barPaint);
            }
        } else {
            // Playback mode: use fixed bar width, left-aligned
            float startX = 0;

            for (int i = 0; i < visibleBars.size(); i++) {
                float amplitude = visibleBars.get(i);
                float barHeight = minBarHeight + amplitude * (maxBarHeight - minBarHeight);
                
                float left = startX + i * (barWidth + barSpacing);
                float top = centerY - barHeight / 2f;
                float right = left + barWidth;
                float bottom = centerY + barHeight / 2f;

                // Color based on progress
                float barProgress = (float) (i + 1) / visibleBars.size();
                if (barProgress <= playbackProgress) {
                    barPaint.setColor(playedBarColor);
                } else {
                    barPaint.setColor(unplayedBarColor);
                }

                barRect.set(left, top, right, bottom);
                canvas.drawRoundRect(barRect, barCornerRadius, barCornerRadius, barPaint);
            }
        }
    }

    // ==================== Touch Handling ====================

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!allowSeeking || isAnimating) {
            return super.onTouchEvent(event);
        }

        // Let gesture detector handle taps
        boolean handled = gestureDetector.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Request parent to not intercept touch events (prevents swipe-to-reply conflict)
                getParent().requestDisallowInterceptTouchEvent(true);
                handled = true;
                break;
                
            case MotionEvent.ACTION_MOVE:
                // Handle drag for seeking with throttling
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastSeekTime >= SEEK_THROTTLE_MS) {
                    if (onSeekListener != null) {
                        float progress = calculateProgressFromX(event.getX());
                        onSeekListener.onSeek(progress);
                        lastSeekTime = currentTime;
                    }
                    handled = true;
                }
                break;
                
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // Allow parent to intercept touch events again
                getParent().requestDisallowInterceptTouchEvent(false);
                handled = true;
                break;
        }

        return handled || super.onTouchEvent(event);
    }

    /**
     * Calculates the progress value from an X coordinate.
     *
     * @param x The X coordinate.
     * @return The progress value in range [0.0, 1.0].
     */
    float calculateProgressFromX(float x) {
        int width = getWidth();
        if (width <= 0) {
            return 0f;
        }
        return Math.max(0f, Math.min(1f, x / width));
    }

    /**
     * Gets the number of visible bars that would be displayed.
     * This is useful for testing.
     *
     * @return The number of visible bars.
     */
    int getVisibleBarCount() {
        List<Float> barsToRender = isAnimating ? localBars : amplitudes;
        return Math.min(barsToRender.size(), maxBarCount);
    }
}
