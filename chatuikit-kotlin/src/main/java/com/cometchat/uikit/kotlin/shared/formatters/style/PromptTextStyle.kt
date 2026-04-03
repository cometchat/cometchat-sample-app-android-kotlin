package com.cometchat.uikit.kotlin.shared.formatters.style

import android.graphics.Typeface
import androidx.annotation.ColorInt
import androidx.annotation.Dimension

/**
 * A style class for formatting prompt text with customizable appearance properties.
 * Provides fluent API for setting text color, appearance, background color, and size.
 */
class PromptTextStyle {
    private var color: Int = 0
    private var textAppearance: Typeface? = null
    private var backgroundColor: Int = 0
    private var textSize: Int = 0

    /**
     * Sets the text color.
     *
     * @param color The color to set
     * @return This PromptTextStyle instance for method chaining
     */
    fun setColor(@ColorInt color: Int): PromptTextStyle {
        this.color = color
        return this
    }

    /**
     * Sets the text appearance (typeface).
     *
     * @param textAppearance The typeface to set
     * @return This PromptTextStyle instance for method chaining
     */
    fun setTextAppearance(textAppearance: Typeface?): PromptTextStyle {
        this.textAppearance = textAppearance
        return this
    }

    /**
     * Sets the background color.
     *
     * @param backgroundColor The background color to set
     * @return This PromptTextStyle instance for method chaining
     */
    fun setBackgroundColor(@ColorInt backgroundColor: Int): PromptTextStyle {
        this.backgroundColor = backgroundColor
        return this
    }

    /**
     * Sets the text size in pixels.
     *
     * @param size The text size to set
     * @return This PromptTextStyle instance for method chaining
     */
    fun setTextSize(@Dimension size: Int): PromptTextStyle {
        this.textSize = size
        return this
    }

    /**
     * Gets the text color.
     *
     * @return The text color
     */
    fun getColor(): Int = color

    /**
     * Gets the text appearance (typeface).
     *
     * @return The typeface
     */
    fun getTextAppearance(): Typeface? = textAppearance

    /**
     * Gets the background color.
     *
     * @return The background color
     */
    fun getBackgroundColor(): Int = backgroundColor

    /**
     * Gets the text size.
     *
     * @return The text size in pixels
     */
    fun getTextSize(): Int = textSize
}
