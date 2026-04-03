package com.cometchat.uikit.kotlin.shared.spans

import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import com.cometchat.chat.models.User
import com.cometchat.uikit.kotlin.shared.formatters.SuggestionItem
import com.cometchat.uikit.kotlin.shared.formatters.style.PromptTextStyle

/**
 * A clickable span for mention tags that applies styling and handles click events.
 * Used for displaying clickable mentions in message bubbles and composer.
 * 
 * The background color is automatically applied with 20% opacity (alpha 51)
 * to match the chatuikit Java implementation.
 */
class TagSpan(
    private var id: Char,
    private var text: String,
    private var suggestionItem: SuggestionItem,
    private val onTagClick: OnTagClick<User>?
) : ClickableSpan() {

    private var textAppearance: PromptTextStyle? = suggestionItem.promptTextStyle

    override fun onClick(widget: View) {
        val data = suggestionItem.data
        if (data != null && onTagClick != null) {
            try {
                val user = User.fromJson(data.toString())
                onTagClick.onClick(widget.context, user)
            } catch (e: Exception) { 
                // Ignore parsing errors
            }
        }
    }

    override fun updateDrawState(ds: TextPaint) {
        // Apply styling exactly like NonEditableSpan (don't call super to avoid default underline)
        textAppearance?.let { style ->
            style.getTextAppearance()?.let { ds.typeface = it }
            if (style.getColor() != 0) ds.color = style.getColor()
            if (style.getTextSize() > 0) ds.textSize = style.getTextSize().toFloat()
            if (style.getBackgroundColor() != 0) {
                ds.bgColor = applyColorWithAlpha(style.getBackgroundColor(), BACKGROUND_ALPHA)
            }
        }
    }

    /**
     * Applies alpha value to a color.
     * @param color The original color
     * @param alpha The alpha value (0-255)
     * @return The color with applied alpha
     */
    private fun applyColorWithAlpha(color: Int, alpha: Int): Int {
        return (alpha shl 24) or (color and 0x00FFFFFF)
    }

    fun getId(): Char = id
    fun setId(id: Char) { this.id = id }
    fun getText(): String = text
    fun setText(text: String) { this.text = text }
    fun getSuggestionItem(): SuggestionItem = suggestionItem
    fun setSuggestionItem(item: SuggestionItem) { 
        this.suggestionItem = item 
        this.textAppearance = item.promptTextStyle
    }
    fun getTextAppearance(): PromptTextStyle? = textAppearance
    fun setTextAppearance(style: PromptTextStyle?) { this.textAppearance = style }

    companion object {
        /**
         * Alpha value for background color (51/255 ≈ 20% opacity)
         * Matches the chatuikit Java implementation.
         */
        private const val BACKGROUND_ALPHA = 51
    }
}
