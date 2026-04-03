package com.cometchat.uikit.compose.presentation.shared.spans

import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.presentation.shared.formatters.SuggestionItem
import com.cometchat.uikit.compose.presentation.shared.formatters.style.PromptTextStyle


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
            } catch (e: Exception) { }
        }
    }

    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        textAppearance?.let { style ->
            style.getTextAppearance()?.let { ds.typeface = it }
            if (style.getTextSize() > 0) ds.textSize = style.getTextSize().toFloat()
            if (style.getColor() != 0) ds.color = style.getColor()
            if (style.getBackgroundColor() != 0) ds.bgColor = applyColorWithAlpha(style.getBackgroundColor(), 51)
        }
        ds.isUnderlineText = false
    }

    private fun applyColorWithAlpha(color: Int, alpha: Int): Int = (alpha shl 24) or (color and 0x00FFFFFF)

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
}
