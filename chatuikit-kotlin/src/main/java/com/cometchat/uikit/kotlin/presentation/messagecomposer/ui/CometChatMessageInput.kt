package com.cometchat.uikit.kotlin.presentation.messagecomposer.ui

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.shared.mentions.CometChatTextWatcher
import com.cometchat.uikit.kotlin.shared.views.cometchatedittext.CometChatEditText

/**
 * CometChatMessageInput is a custom view component for text input in the message composer.
 * 
 * This component provides:
 * - A CometChatEditText for text input with span-aware cursor handling
 * - Methods to set CometChatTextWatcher for mention detection
 * - Selection methods for cursor manipulation
 * - Support for auxiliary buttons (attachment, send, etc.)
 * 
 * Based on the design document specifications for mention handling in the message composer.
 * 
 * Usage:
 * ```kotlin
 * val messageInput = CometChatMessageInput(context)
 * messageInput.setTextWatcher(object : CometChatTextWatcher() {
 *     override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
 *         // Handle text changes for mention detection
 *     }
 *     override fun onSpanDeleted(span: NonEditableSpan) {
 *         // Handle mention span deletion
 *     }
 * })
 * ```
 */
class CometChatMessageInput @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    /**
     * The CometChatEditText for text input with span-aware cursor handling.
     */
    private val editText: CometChatEditText

    /**
     * Container for secondary button (e.g., attachment button).
     */
    private val secondaryButtonContainer: FrameLayout

    /**
     * Container for auxiliary buttons (e.g., sticker, AI, voice recording).
     */
    private val auxiliaryButtonContainer: LinearLayout

    /**
     * Container for send button.
     */
    private val sendButtonContainer: FrameLayout

    /**
     * Separator view between secondary button and input.
     */
    private val separatorView: View

    /**
     * Selection change listener for cursor position changes.
     */
    private var onSelectionChangedListener: OnSelectionChangedListener? = null

    /**
     * Text change listener for external observers.
     */
    private var externalTextWatcher: TextWatcher? = null

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL

        // Create secondary button container (attachment)
        secondaryButtonContainer = FrameLayout(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            )
        }
        addView(secondaryButtonContainer)

        // Create separator view
        separatorView = View(context).apply {
            layoutParams = LayoutParams(
                resources.getDimensionPixelSize(R.dimen.cometchat_1dp),
                resources.getDimensionPixelSize(R.dimen.cometchat_20dp)
            ).apply {
                marginStart = resources.getDimensionPixelSize(R.dimen.cometchat_padding_3)
            }
        }
        addView(separatorView)

        // Create CometChatEditText
        editText = CometChatEditText(context).apply {
            layoutParams = LayoutParams(
                0,
                LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                marginStart = resources.getDimensionPixelSize(R.dimen.cometchat_padding_2)
                marginEnd = resources.getDimensionPixelSize(R.dimen.cometchat_padding_2)
            }
            background = null
            gravity = Gravity.CENTER_VERTICAL or Gravity.START
            inputType = InputType.TYPE_CLASS_TEXT or 
                       InputType.TYPE_TEXT_FLAG_MULTI_LINE or 
                       InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            maxLines = 3
            minLines = 1
            isSingleLine = false
            setHint(R.string.cometchat_composer_place_holder_text)
        }
        addView(editText)

        // Create auxiliary button container
        auxiliaryButtonContainer = LinearLayout(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            )
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        addView(auxiliaryButtonContainer)

        // Create send button container
        sendButtonContainer = FrameLayout(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = resources.getDimensionPixelSize(R.dimen.cometchat_padding_3)
            }
        }
        addView(sendButtonContainer)
    }

    // ==================== Text Watcher Methods ====================

    /**
     * Sets the CometChatTextWatcher for receiving text change and span deletion callbacks.
     * 
     * This is the primary method for integrating mention detection. The text watcher
     * will receive callbacks for:
     * - Text changes (for detecting @ trigger character)
     * - Span deletions (when a mention is deleted via backspace)
     * 
     * @param watcher The CometChatTextWatcher to set, or null to remove
     */
    fun setTextWatcher(watcher: CometChatTextWatcher?) {
        editText.setTextWatcher(watcher)
    }

    /**
     * Gets the current CometChatTextWatcher.
     * 
     * @return The current CometChatTextWatcher, or null if not set
     */
    fun getTextWatcher(): CometChatTextWatcher? = editText.getTextWatcher()

    /**
     * Adds a standard Android TextWatcher for external text change observation.
     * 
     * @param watcher The TextWatcher to add
     */
    fun addTextChangedListener(watcher: TextWatcher) {
        editText.addTextChangedListener(watcher)
        externalTextWatcher = watcher
    }

    /**
     * Removes a previously added TextWatcher.
     * 
     * @param watcher The TextWatcher to remove
     */
    fun removeTextChangedListener(watcher: TextWatcher) {
        editText.removeTextChangedListener(watcher)
        if (externalTextWatcher == watcher) {
            externalTextWatcher = null
        }
    }

    // ==================== Selection Methods ====================

    /**
     * Gets the starting position of the current selection.
     * 
     * @return The selection start position, or -1 if no selection
     */
    fun getSelectionStart(): Int = editText.selectionStart

    /**
     * Gets the ending position of the current selection.
     * 
     * @return The selection end position, or -1 if no selection
     */
    fun getSelectionEnd(): Int = editText.selectionEnd

    /**
     * Sets the cursor position to the specified index.
     * 
     * @param index The position to set the cursor to
     */
    fun setSelection(index: Int) {
        if (index >= 0 && index <= (editText.text?.length ?: 0)) {
            editText.setSelection(index)
        }
    }

    /**
     * Sets the selection range.
     * 
     * @param start The start position of the selection
     * @param stop The end position of the selection
     */
    fun setSelection(start: Int, stop: Int) {
        val textLength = editText.text?.length ?: 0
        if (start >= 0 && stop >= start && stop <= textLength) {
            editText.setSelection(start, stop)
        }
    }

    /**
     * Selects all text in the input.
     */
    fun selectAll() {
        editText.selectAll()
    }

    /**
     * Sets a listener for selection changes.
     * 
     * @param listener The listener to set, or null to remove
     */
    fun setOnSelectionChangedListener(listener: OnSelectionChangedListener?) {
        this.onSelectionChangedListener = listener
    }

    // ==================== Text Methods ====================

    /**
     * Gets the current text as Editable.
     * 
     * @return The editable text, or null if not available
     */
    fun getText(): Editable? = editText.text

    /**
     * Gets the current text as a String.
     * 
     * @return The text string
     */
    fun getTextString(): String = editText.text?.toString() ?: ""

    /**
     * Sets the text content.
     * 
     * @param text The text to set
     */
    fun setText(text: CharSequence?) {
        editText.setText(text)
    }

    /**
     * Sets the hint text.
     * 
     * @param hint The hint text to display when empty
     */
    fun setHint(hint: CharSequence?) {
        editText.hint = hint
    }

    /**
     * Gets the hint text.
     * 
     * @return The current hint text
     */
    fun getHint(): CharSequence? = editText.hint

    /**
     * Clears all text from the input.
     */
    fun clearText() {
        editText.text?.clear()
    }

    /**
     * Gets the editable text for direct manipulation.
     * 
     * @return The Editable text object
     */
    fun getEditableText(): Editable? = editText.editableText

    // ==================== EditText Access ====================

    /**
     * Gets the underlying CometChatEditText for advanced operations.
     * 
     * Use this method when you need direct access to the EditText for:
     * - Setting input filters
     * - Configuring IME options
     * - Adding custom spans
     * 
     * @return The CometChatEditText instance
     */
    fun getEditText(): CometChatEditText = editText

    /**
     * Requests focus on the input field.
     * 
     * @return true if focus was successfully requested
     */
    override fun requestFocus(direction: Int, previouslyFocusedRect: android.graphics.Rect?): Boolean {
        return editText.requestFocus(direction, previouslyFocusedRect)
    }

    /**
     * Clears focus from the input field.
     */
    override fun clearFocus() {
        editText.clearFocus()
        super.clearFocus()
    }

    // ==================== Auxiliary Button Methods ====================

    /**
     * Sets the secondary button view (typically attachment button).
     * 
     * @param view The view to set as secondary button, or null to clear
     */
    fun setSecondaryButtonView(view: View?) {
        secondaryButtonContainer.removeAllViews()
        if (view != null) {
            secondaryButtonContainer.addView(view)
            secondaryButtonContainer.visibility = View.VISIBLE
            separatorView.visibility = View.VISIBLE
        } else {
            secondaryButtonContainer.visibility = View.GONE
            separatorView.visibility = View.GONE
        }
    }

    /**
     * Gets the secondary button container.
     * 
     * @return The FrameLayout containing the secondary button
     */
    fun getSecondaryButtonContainer(): FrameLayout = secondaryButtonContainer

    /**
     * Adds an auxiliary button to the auxiliary button container.
     * 
     * @param view The view to add as an auxiliary button
     */
    fun addAuxiliaryButton(view: View) {
        auxiliaryButtonContainer.addView(view)
    }

    /**
     * Removes an auxiliary button from the container.
     * 
     * @param view The view to remove
     */
    fun removeAuxiliaryButton(view: View) {
        auxiliaryButtonContainer.removeView(view)
    }

    /**
     * Clears all auxiliary buttons.
     */
    fun clearAuxiliaryButtons() {
        auxiliaryButtonContainer.removeAllViews()
    }

    /**
     * Gets the auxiliary button container.
     * 
     * @return The LinearLayout containing auxiliary buttons
     */
    fun getAuxiliaryButtonContainer(): LinearLayout = auxiliaryButtonContainer

    /**
     * Sets the send button view.
     * 
     * @param view The view to set as send button, or null to clear
     */
    fun setSendButtonView(view: View?) {
        sendButtonContainer.removeAllViews()
        if (view != null) {
            sendButtonContainer.addView(view)
            sendButtonContainer.visibility = View.VISIBLE
        } else {
            sendButtonContainer.visibility = View.GONE
        }
    }

    /**
     * Gets the send button container.
     * 
     * @return The FrameLayout containing the send button
     */
    fun getSendButtonContainer(): FrameLayout = sendButtonContainer

    // ==================== Separator Methods ====================

    /**
     * Sets the separator color.
     * 
     * @param color The color to set
     */
    fun setSeparatorColor(@ColorInt color: Int) {
        separatorView.setBackgroundColor(color)
    }

    /**
     * Sets the separator visibility.
     * 
     * @param visible true to show, false to hide
     */
    fun setSeparatorVisible(visible: Boolean) {
        separatorView.visibility = if (visible) View.VISIBLE else View.GONE
    }

    // ==================== Styling Methods ====================

    /**
     * Sets the text color.
     * 
     * @param color The color to set
     */
    fun setTextColor(@ColorInt color: Int) {
        editText.setTextColor(color)
    }

    /**
     * Sets the hint text color.
     * 
     * @param color The color to set
     */
    fun setHintTextColor(@ColorInt color: Int) {
        editText.setHintTextColor(color)
    }

    /**
     * Sets the text appearance.
     * 
     * @param resId The style resource ID
     */
    fun setTextAppearance(resId: Int) {
        editText.setTextAppearance(resId)
    }

    /**
     * Sets the maximum number of lines.
     * 
     * @param maxLines The maximum number of lines
     */
    fun setMaxLines(maxLines: Int) {
        editText.maxLines = maxLines
    }

    /**
     * Sets the minimum number of lines.
     * 
     * @param minLines The minimum number of lines
     */
    fun setMinLines(minLines: Int) {
        editText.minLines = minLines
    }

    // ==================== Media Listener ====================

    /**
     * Sets the listener for media content pasted into the EditText.
     * 
     * @param listener The listener to set
     */
    fun setOnEditTextMediaListener(listener: CometChatEditText.OnEditTextMediaListener?) {
        editText.onEditTextMediaListener = listener
    }

    // ==================== Listener Interface ====================

    /**
     * Interface for receiving selection change callbacks.
     */
    interface OnSelectionChangedListener {
        /**
         * Called when the selection changes.
         * 
         * @param selStart The new selection start position
         * @param selEnd The new selection end position
         */
        fun onSelectionChanged(selStart: Int, selEnd: Int)
    }

    companion object {
        private const val TAG = "CometChatMessageInput"
    }
}
