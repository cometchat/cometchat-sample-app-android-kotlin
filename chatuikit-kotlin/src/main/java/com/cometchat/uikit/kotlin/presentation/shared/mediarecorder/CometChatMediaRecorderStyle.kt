package com.cometchat.uikit.kotlin.presentation.shared.mediarecorder

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for CometChatMediaRecorder.
 */
data class CometChatMediaRecorderStyle(
    // Container styling
    @ColorInt val backgroundColor: Int = 0,
    @Dimension val strokeWidth: Int = 0,
    @ColorInt val strokeColor: Int = 0,
    @Dimension val cornerRadius: Float = 0f,

    // Recording icon styling
    val recordingIcon: Drawable? = null,
    @ColorInt val recordingIconBackgroundColor: Int = 0,
    @ColorInt val recordingIconTint: Int = 0,

    // Text styling
    @StyleRes val textAppearance: Int = 0,
    @ColorInt val textColor: Int = 0,

    // Delete icon styling
    val deleteIcon: Drawable? = null,
    @ColorInt val deleteIconTint: Int = 0,
    @ColorInt val deleteIconBackgroundColor: Int = 0,
    @Dimension val deleteIconRadius: Float = 0f,
    @Dimension val deleteIconStrokeWidth: Float = 0f,
    @ColorInt val deleteIconStrokeColor: Int = 0,
    @Dimension val deleteIconElevation: Float = 0f,

    // Start icon styling
    val startIcon: Drawable? = null,
    @ColorInt val startIconTint: Int = 0,
    @ColorInt val startIconBackgroundColor: Int = 0,
    @Dimension val startIconRadius: Float = 0f,
    @Dimension val startIconStrokeWidth: Float = 0f,
    @ColorInt val startIconStrokeColor: Int = 0,
    @Dimension val startIconElevation: Float = 0f,

    // Pause icon styling
    val pauseIcon: Drawable? = null,
    @ColorInt val pauseIconTint: Int = 0,
    @ColorInt val pauseIconBackgroundColor: Int = 0,
    @Dimension val pauseIconRadius: Float = 0f,
    @Dimension val pauseIconStrokeWidth: Float = 0f,
    @ColorInt val pauseIconStrokeColor: Int = 0,
    @Dimension val pauseIconElevation: Float = 0f,

    // Stop icon styling
    val stopIcon: Drawable? = null,
    @ColorInt val stopIconTint: Int = 0,
    @ColorInt val stopIconBackgroundColor: Int = 0,
    @Dimension val stopIconRadius: Float = 0f,
    @Dimension val stopIconStrokeWidth: Float = 0f,
    @ColorInt val stopIconStrokeColor: Int = 0,
    @Dimension val stopIconElevation: Float = 0f,

    // Play icon styling
    @ColorInt val playIconTint: Int = 0,

    // Visualizer styling
    @ColorInt val visualizerColor: Int = 0,

    // Send icon styling
    val sendIcon: Drawable? = null,
    @ColorInt val sendIconTint: Int = 0,
    @ColorInt val sendIconBackgroundColor: Int = 0,
    @Dimension val sendIconRadius: Float = 0f,
    @Dimension val sendIconStrokeWidth: Float = 0f,
    @ColorInt val sendIconStrokeColor: Int = 0,
    @Dimension val sendIconElevation: Float = 0f,

    // Restart icon styling
    val restartIcon: Drawable? = null,
    @ColorInt val restartIconTint: Int = 0,
    @ColorInt val restartIconBackgroundColor: Int = 0,
    @Dimension val restartIconRadius: Float = 0f,
    @Dimension val restartIconStrokeWidth: Float = 0f,
    @ColorInt val restartIconStrokeColor: Int = 0,
    @Dimension val restartIconElevation: Float = 0f,

    // Legacy visualizer properties
    @ColorInt val recordingChunkColor: Int = 0,
    @ColorInt val barColor: Int = 0,
    @ColorInt val activeBarColor: Int = 0,
    @Dimension val chunkWidth: Int = 0,
    @Dimension val chunkSpacing: Int = 0,
    @Dimension val chunkMinHeight: Int = 0,
    @Dimension val chunkMaxHeight: Int = 0,
    @Dimension val chunkCornerRadius: Int = 0,
    val chunkCount: Int = 45,

    // Legacy properties
    @ColorInt val deleteIconColor: Int = 0,
    @ColorInt val submitIconTint: Int = 0,
    @ColorInt val submitButtonBackgroundColor: Int = 0,
    @ColorInt val timerTextColor: Int = 0
) {
    companion object {
        fun default(context: Context): CometChatMediaRecorderStyle {
            return extractFromThemeStyle(context)
        }

        private fun extractFromThemeStyle(context: Context): CometChatMediaRecorderStyle {
            val themeTypedArray = context.obtainStyledAttributes(intArrayOf(R.attr.cometchatMediaRecorderStyle))
            val styleResId = themeTypedArray.getResourceId(0, 0)
            themeTypedArray.recycle()
            return extractFromStyleResource(context, styleResId)
        }

        private fun extractFromStyleResource(context: Context, styleResId: Int): CometChatMediaRecorderStyle {
            if (styleResId == 0) return extractFromTypedArray(context, null)
            val typedArray = context.obtainStyledAttributes(styleResId, R.styleable.CometChatMediaRecorder)
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        private fun adjustAlpha(@ColorInt color: Int, factor: Float): Int {
            val alpha = (android.graphics.Color.alpha(color) * factor).toInt()
            return android.graphics.Color.argb(alpha, android.graphics.Color.red(color),
                android.graphics.Color.green(color), android.graphics.Color.blue(color))
        }

        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatMediaRecorderStyle {
            return try { extractFromTypedArray(context, typedArray) } finally { typedArray.recycle() }
        }

        private fun extractFromTypedArray(context: Context, typedArray: TypedArray?): CometChatMediaRecorderStyle {
            val primaryColor = CometChatTheme.getPrimaryColor(context)
            val bg1 = CometChatTheme.getBackgroundColor1(context)

            return CometChatMediaRecorderStyle(
                backgroundColor = typedArray?.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderBackgroundColor, bg1) ?: bg1,
                strokeWidth = typedArray?.getDimensionPixelSize(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderStrokeWidth, 0) ?: 0,
                strokeColor = typedArray?.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderStrokeColor, CometChatTheme.getStrokeColorLight(context)) ?: CometChatTheme.getStrokeColorLight(context),
                cornerRadius = typedArray?.getDimension(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderCornerRadius, context.resources.getDimension(R.dimen.cometchat_corner_radius_3)) ?: context.resources.getDimension(R.dimen.cometchat_corner_radius_3),
                recordingIcon = typedArray?.getDrawable(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderRecordingIcon),
                recordingIconBackgroundColor = typedArray?.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderRecordingIconBackgroundColor, bg1) ?: bg1,
                recordingIconTint = typedArray?.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderRecordingIconTint, CometChatTheme.getErrorColor(context)) ?: CometChatTheme.getErrorColor(context),
                textAppearance = typedArray?.getResourceId(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderTextAppearance, CometChatTheme.getTextAppearanceBodyRegular(context)) ?: CometChatTheme.getTextAppearanceBodyRegular(context),
                textColor = typedArray?.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderTextColor, CometChatTheme.getTextColorSecondary(context)) ?: CometChatTheme.getTextColorSecondary(context),
                deleteIcon = typedArray?.getDrawable(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderDeleteIcon),
                deleteIconTint = typedArray?.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderDeleteIconTint, CometChatTheme.getIconTintSecondary(context)) ?: CometChatTheme.getIconTintSecondary(context),
                deleteIconBackgroundColor = typedArray?.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderDeleteIconBackgroundColor, bg1) ?: bg1,
                deleteIconRadius = typedArray?.getDimension(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderDeleteIconRadius, 0f) ?: 0f,
                deleteIconStrokeWidth = typedArray?.getDimension(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderDeleteIconStrokeWidth, 0f) ?: 0f,
                deleteIconStrokeColor = typedArray?.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderDeleteIconStrokeColor, 0) ?: 0,
                deleteIconElevation = typedArray?.getDimension(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderDeleteIconElevation, 0f) ?: 0f,
                startIcon = typedArray?.getDrawable(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderStartIcon),
                startIconTint = typedArray?.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderStartIconTint, CometChatTheme.getErrorColor(context)) ?: CometChatTheme.getErrorColor(context),
                startIconBackgroundColor = typedArray?.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderStartIconBackgroundColor, bg1) ?: bg1,
                startIconRadius = typedArray?.getDimension(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderStartIconRadius, 0f) ?: 0f,
                startIconStrokeWidth = typedArray?.getDimension(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderStartIconStrokeWidth, 0f) ?: 0f,
                startIconStrokeColor = typedArray?.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderStartIconStrokeColor, 0) ?: 0,
                startIconElevation = typedArray?.getDimension(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderStartIconElevation, 0f) ?: 0f,
                pauseIcon = typedArray?.getDrawable(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderPauseIcon),
                pauseIconTint = typedArray?.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderPauseIconTint, CometChatTheme.getIconTintHighlight(context)) ?: CometChatTheme.getIconTintHighlight(context),
                pauseIconBackgroundColor = typedArray?.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderPauseIconBackgroundColor, bg1) ?: bg1,
                pauseIconRadius = typedArray?.getDimension(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderPauseIconRadius, 0f) ?: 0f,
                pauseIconStrokeWidth = typedArray?.getDimension(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderPauseIconStrokeWidth, 0f) ?: 0f,
                pauseIconStrokeColor = typedArray?.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderPauseIconStrokeColor, 0) ?: 0,
                pauseIconElevation = typedArray?.getDimension(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderPauseIconElevation, 0f) ?: 0f,
                stopIcon = typedArray?.getDrawable(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderStopIcon),
                stopIconTint = typedArray?.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderStopIconTint, CometChatTheme.getIconTintSecondary(context)) ?: CometChatTheme.getIconTintSecondary(context),
                stopIconBackgroundColor = typedArray?.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderStopIconBackgroundColor, bg1) ?: bg1,
                stopIconRadius = typedArray?.getDimension(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderStopIconRadius, 0f) ?: 0f,
                stopIconStrokeWidth = typedArray?.getDimension(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderStopIconStrokeWidth, 0f) ?: 0f,
                stopIconStrokeColor = typedArray?.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderStopIconStrokeColor, 0) ?: 0,
                stopIconElevation = typedArray?.getDimension(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderStopIconElevation, 0f) ?: 0f,
                playIconTint = typedArray?.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderPlayIconTint, CometChatTheme.getIconTintHighlight(context)) ?: CometChatTheme.getIconTintHighlight(context),
                visualizerColor = typedArray?.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderVisualizerColor, primaryColor) ?: primaryColor,
                sendIcon = typedArray?.getDrawable(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderSendIcon),
                sendIconTint = typedArray?.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderSendIconTint, CometChatTheme.getIconTintHighlight(context)) ?: CometChatTheme.getIconTintHighlight(context),
                sendIconBackgroundColor = typedArray?.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderSendIconBackgroundColor, bg1) ?: bg1,
                sendIconRadius = typedArray?.getDimension(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderSendIconRadius, 0f) ?: 0f,
                sendIconStrokeWidth = typedArray?.getDimension(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderSendIconStrokeWidth, 0f) ?: 0f,
                sendIconStrokeColor = typedArray?.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderSendIconStrokeColor, 0) ?: 0,
                sendIconElevation = typedArray?.getDimension(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderSendIconElevation, 0f) ?: 0f,
                restartIcon = typedArray?.getDrawable(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderRestartIcon),
                restartIconTint = typedArray?.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderRestartIconTint, CometChatTheme.getIconTintSecondary(context)) ?: CometChatTheme.getIconTintSecondary(context),
                restartIconBackgroundColor = typedArray?.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderRestartIconBackgroundColor, bg1) ?: bg1,
                restartIconRadius = typedArray?.getDimension(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderRestartIconRadius, 0f) ?: 0f,
                restartIconStrokeWidth = typedArray?.getDimension(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderRestartIconStrokeWidth, 0f) ?: 0f,
                restartIconStrokeColor = typedArray?.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderRestartIconStrokeColor, 0) ?: 0,
                restartIconElevation = typedArray?.getDimension(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderRestartIconElevation, 0f) ?: 0f,
                // Legacy visualizer properties
                recordingChunkColor = typedArray?.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderVisualizerColor, primaryColor) ?: primaryColor,
                barColor = adjustAlpha(typedArray?.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderVisualizerColor, primaryColor) ?: primaryColor, 0.2f),
                activeBarColor = typedArray?.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderVisualizerColor, primaryColor) ?: primaryColor,
                chunkWidth = context.resources.getDimensionPixelSize(R.dimen.cometchat_2dp),
                chunkSpacing = (2.7f * context.resources.displayMetrics.density).toInt(),
                chunkMinHeight = context.resources.getDimensionPixelSize(R.dimen.cometchat_2dp),
                chunkMaxHeight = context.resources.getDimensionPixelSize(R.dimen.cometchat_16dp),
                chunkCornerRadius = (1000f * context.resources.displayMetrics.density).toInt(),
                chunkCount = 45,
                // Legacy properties
                deleteIconColor = typedArray?.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderDeleteIconTint, CometChatTheme.getIconTintSecondary(context)) ?: CometChatTheme.getIconTintSecondary(context),
                submitIconTint = typedArray?.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderSendIconTint, CometChatTheme.getIconTintHighlight(context)) ?: CometChatTheme.getIconTintHighlight(context),
                submitButtonBackgroundColor = typedArray?.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderSendIconBackgroundColor, bg1) ?: bg1,
                timerTextColor = typedArray?.getColor(R.styleable.CometChatMediaRecorder_cometchatMediaRecorderTextColor, CometChatTheme.getTextColorSecondary(context)) ?: CometChatTheme.getTextColorSecondary(context)
            )
        }
    }
}
