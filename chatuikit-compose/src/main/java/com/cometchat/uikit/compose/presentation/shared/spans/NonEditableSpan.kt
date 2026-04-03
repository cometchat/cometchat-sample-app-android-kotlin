package com.cometchat.uikit.compose.presentation.shared.spans

import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import com.cometchat.uikit.compose.presentation.shared.formatters.SuggestionItem
import com.cometchat.uikit.compose.presentation.shared.formatters.style.PromptTextStyle

class NonEditableSpan private constructor(
    private var id: Char,
    private var text: String,
    private var suggestionItem: SuggestionItem?,
    private var textAppearance: PromptTextStyle?
) : ClickableSpan() {

    constructor(id: Char, text: String, suggestionItem: SuggestionItem) : 
        this(id, text, suggestionItem, suggestionItem.promptTextStyle)

    constructor(id: Char, text: String, textAppearance: PromptTextStyle?) : 
        this(id, text, null, textAppearance)

    override fun onClick(widget: View) { widget.isEnabled = false }

    override fun updateDrawState(ds: TextPaint) {
        textAppearance?.let { style ->
            style.getTextAppearance()?.let { ds.typeface = it }
            if (style.getColor() != 0) ds.color = style.getColor()
            if (style.getTextSize() > 0) ds.textSize = style.getTextSize().toFloat()
            if (style.getBackgroundColor() != 0) ds.bgColor = applyColorWithAlpha(style.getBackgroundColor(), 51)
        }
    }

    private fun applyColorWithAlpha(color: Int, alpha: Int): Int = (alpha shl 24) or (color and 0x00FFFFFF)

    fun getId(): Char = id
    fun setId(id: Char) { this.id = id }
    fun getText(): String = text
    fun setText(text: String) { this.text = text }
    fun getSuggestionItem(): SuggestionItem? = suggestionItem
    fun setSuggestionItem(item: SuggestionItem?) { 
        this.suggestionItem = item 
        if (item != null) this.textAppearance = item.promptTextStyle
    }
    fun getTextAppearance(): PromptTextStyle? = textAppearance
    fun setTextAppearance(style: PromptTextStyle?) { this.textAppearance = style }
}
