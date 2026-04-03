package com.cometchat.uikit.kotlin.presentation.ongoingcall.style

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for CometChatOngoingCall component.
 * Contains all visual styling properties for the ongoing call screen.
 *
 * **Validates: Requirement 16.6**
 *
 * @param backgroundColor Background color for the ongoing call container
 * @param progressIndicatorColor Color for the loading progress indicator
 * @param cornerRadius Corner radius of the ongoing call container
 * @param strokeWidth Width of the container border
 * @param strokeColor Color of the container border
 */
data class CometChatOngoingCallStyle private constructor(
    @ColorInt val backgroundColor: Int,
    @ColorInt val progressIndicatorColor: Int,
    @Dimension val cornerRadius: Float,
    @Dimension val strokeWidth: Int,
    @ColorInt val strokeColor: Int
) {
    /**
     * Builder class for creating CometChatOngoingCallStyle instances.
     * Provides a fluent API for setting style properties.
     */
    class Builder(private val context: Context) {
        @ColorInt private var backgroundColor: Int = context.resources.getColor(R.color.cometchat_calling_background, context.theme)
        @ColorInt private var progressIndicatorColor: Int = CometChatTheme.getColorWhite(context)
        @Dimension private var cornerRadius: Float = 0f
        @Dimension private var strokeWidth: Int = 0
        @ColorInt private var strokeColor: Int = Color.TRANSPARENT

        /**
         * Sets the background color for the ongoing call container.
         */
        fun setBackgroundColor(@ColorInt color: Int) = apply { backgroundColor = color }

        /**
         * Sets the progress indicator color.
         */
        fun setProgressIndicatorColor(@ColorInt color: Int) = apply { progressIndicatorColor = color }

        /**
         * Sets the corner radius of the ongoing call container.
         */
        fun setCornerRadius(@Dimension radius: Float) = apply { cornerRadius = radius }

        /**
         * Sets the stroke width of the container border.
         */
        fun setStrokeWidth(@Dimension width: Int) = apply { strokeWidth = width }

        /**
         * Sets the stroke color of the container border.
         */
        fun setStrokeColor(@ColorInt color: Int) = apply { strokeColor = color }

        /**
         * Builds the CometChatOngoingCallStyle instance.
         */
        fun build() = CometChatOngoingCallStyle(
            backgroundColor = backgroundColor,
            progressIndicatorColor = progressIndicatorColor,
            cornerRadius = cornerRadius,
            strokeWidth = strokeWidth,
            strokeColor = strokeColor
        )
    }

    companion object {
        /**
         * Creates a default style configuration sourcing values from CometChatTheme.
         *
         * @param context The Android context for accessing theme attributes
         * @return A fully configured CometChatOngoingCallStyle with theme defaults
         */
        fun default(context: Context): CometChatOngoingCallStyle {
            return Builder(context).build()
        }

        /**
         * Creates a new Builder instance for fluent style construction.
         *
         * @param context The Android context for accessing theme attributes
         * @return A new Builder instance with theme defaults
         */
        fun builder(context: Context) = Builder(context)
    }
}
