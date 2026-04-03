package com.cometchat.uikit.kotlin.presentation.shared.typingindicator

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.appcompat.widget.AppCompatTextView
import com.cometchat.chat.models.TypingIndicator
import com.cometchat.uikit.kotlin.R

/**
 * CometChatTypingIndicator is a custom view that displays typing status.
 * 
 * This component displays:
 * - "typing..." text for 1-on-1 conversations
 * - "{name} is typing..." for group conversations
 * 
 * Usage in XML:
 * ```xml
 * <com.cometchat.uikit.kotlin.presentation.shared.typingindicator.CometChatTypingIndicator
 *     android:id="@+id/typingIndicator"
 *     android:layout_width="wrap_content"
 *     android:layout_height="wrap_content" />
 * ```
 */
@Suppress("unused")
class CometChatTypingIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.cometchatTypingIndicatorStyle
) : AppCompatTextView(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = CometChatTypingIndicator::class.java.simpleName
    }

    private var typingIndicator: TypingIndicator? = null
    private var isGroupConversation: Boolean = false

    // Single style object - NO individual style properties
    private var style: CometChatTypingIndicatorStyle = CometChatTypingIndicatorStyle()

    init {
        if (!isInEditMode) {
            applyStyleAttributes(attrs, defStyleAttr)
        }
    }

    /**
     * Applies style attributes from XML using the style class factory method.
     */
    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        var typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatTypingIndicator, defStyleAttr, 0
        )
        val styleResId = typedArray.getResourceId(
            R.styleable.CometChatTypingIndicator_cometchatTypingIndicatorStyle, 0
        )
        typedArray.recycle()
        
        typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatTypingIndicator, defStyleAttr, styleResId
        )
        // fromTypedArray handles recycling internally
        style = CometChatTypingIndicatorStyle.fromTypedArray(context, typedArray)
        applyStyle()
    }

    /**
     * Applies all style properties from the style object to views.
     */
    private fun applyStyle() {
        if (style.textColor != 0) applyTextColor(style.textColor)
        if (style.textAppearance != 0) applyTextAppearance(style.textAppearance)
    }

    // ========================================
    // Private Apply Methods
    // ========================================

    private fun applyTextColor(@ColorInt color: Int) {
        setTextColor(color)
    }

    private fun applyTextAppearance(@StyleRes appearance: Int) {
        if (appearance != 0) {
            setTextAppearance(appearance)
        }
    }

    // ========================================
    // Public Style Methods
    // ========================================

    /**
     * Sets the style from a style resource.
     */
    fun setStyle(@StyleRes styleRes: Int) {
        if (styleRes != 0) {
            val typedArray = context.theme.obtainStyledAttributes(
                styleRes, R.styleable.CometChatTypingIndicator
            )
            // fromTypedArray handles recycling internally
            setStyle(CometChatTypingIndicatorStyle.fromTypedArray(context, typedArray))
        }
    }

    /**
     * Sets the style from a CometChatTypingIndicatorStyle object.
     */
    fun setStyle(style: CometChatTypingIndicatorStyle) {
        this.style = style
        applyStyle()
    }

    /**
     * Sets the typing indicator from a TypingIndicator object.
     */
    fun setTypingIndicator(indicator: TypingIndicator?) {
        typingIndicator = indicator
        if (indicator == null) {
            visibility = View.GONE
            return
        }
        
        visibility = View.VISIBLE
        updateTypingText()
    }

    /**
     * Sets whether this is a group conversation.
     * In group conversations, the user's name is shown.
     */
    fun setIsGroupConversation(isGroup: Boolean) {
        isGroupConversation = isGroup
        updateTypingText()
    }

    private fun updateTypingText() {
        val indicator = typingIndicator ?: return
        
        val typingText = if (isGroupConversation && indicator.sender != null) {
            val senderName = indicator.sender.name ?: context.getString(R.string.cometchat_user)
            "$senderName ${context.getString(R.string.cometchat_is_typing)}"
        } else {
            context.getString(R.string.cometchat_typing)
        }
        
        text = typingText
    }

    /**
     * Clears the typing indicator.
     */
    fun clearTypingIndicator() {
        typingIndicator = null
        visibility = View.GONE
    }

    // ========================================
    // Getters (read from style object)
    // ========================================

    fun getTypingIndicator(): TypingIndicator? = typingIndicator

    fun getIndicatorTextColor(): Int = style.textColor

    fun getIndicatorTextAppearance(): Int = style.textAppearance

    fun getDotColor(): Int = style.dotColor

    fun getAnimationDuration(): Long = style.animationDuration

    // ========================================
    // Setters (update style object + apply)
    // ========================================

    fun setIndicatorTextColor(@ColorInt color: Int) {
        style = style.copy(textColor = color)
        applyTextColor(color)
    }

    fun setIndicatorTextAppearance(@StyleRes appearance: Int) {
        style = style.copy(textAppearance = appearance)
        applyTextAppearance(appearance)
    }

    fun setDotColor(@ColorInt color: Int) {
        style = style.copy(dotColor = color)
    }

    fun setAnimationDuration(duration: Long) {
        style = style.copy(animationDuration = duration)
    }
}
