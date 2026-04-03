package com.cometchat.uikit.kotlin.presentation.shared.receipts

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils

/**
 * CometChatReceipt is a custom view that displays message receipt status icons.
 * It shows different icons for sent, delivered, and read states.
 */
class CometChatReceipt @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = CometChatReceipt::class.java.simpleName
    }

    private val receiptIcon: ImageView
    private var currentStatus: ReceiptStatus = ReceiptStatus.SENT

    // Single style object - NO individual style properties
    private var style: CometChatReceiptStyle = CometChatReceiptStyle()

    // Default icon resources (used when style doesn't provide custom icons)
    private val defaultWaitIcon: Drawable? by lazy { ContextCompat.getDrawable(context, R.drawable.cometchat_ic_message_waiting) }
    private val defaultSentIcon: Drawable? by lazy { ContextCompat.getDrawable(context, R.drawable.cometchat_ic_message_sent) }
    private val defaultDeliveredIcon: Drawable? by lazy { ContextCompat.getDrawable(context, R.drawable.cometchat_ic_message_delivered) }
    private val defaultReadIcon: Drawable? by lazy { ContextCompat.getDrawable(context, R.drawable.cometchat_ic_message_read) }
    private val defaultErrorIcon: Drawable? by lazy { ContextCompat.getDrawable(context, R.drawable.cometchat_ic_message_error) }

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL

        receiptIcon = ImageView(context).apply {
            val size = Utils.convertDpToPx(context, 16)
            layoutParams = LayoutParams(size, size)
        }
        addView(receiptIcon)

        applyStyleAttributes(attrs, defStyleAttr)
    }

    /**
     * Applies style attributes from XML using the style class factory method.
     */
    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        var typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatMessageReceipt, defStyleAttr, 0
        )
        val styleResId = typedArray.getResourceId(
            R.styleable.CometChatMessageReceipt_cometchatMessageReceiptStyle, 0
        )
        typedArray.recycle()
        
        typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatMessageReceipt, defStyleAttr, styleResId
        )
        // fromTypedArray handles recycling internally
        style = CometChatReceiptStyle.fromTypedArray(context, typedArray)
        applyStyle()
    }

    /**
     * Applies all style properties from the style object to views.
     */
    private fun applyStyle() {
        updateReceiptIcon()
    }

    // ========================================
    // Private Apply Methods
    // ========================================

    private fun updateReceiptIcon() {
        val (icon, tint) = when (currentStatus) {
            ReceiptStatus.IN_PROGRESS -> Pair(style.waitIcon ?: defaultWaitIcon, style.waitIconTint)
            ReceiptStatus.SENT -> Pair(style.sentIcon ?: defaultSentIcon, style.sentIconTint)
            ReceiptStatus.DELIVERED -> Pair(style.deliveredIcon ?: defaultDeliveredIcon, style.deliveredIconTint)
            ReceiptStatus.READ -> Pair(style.readIcon ?: defaultReadIcon, style.readIconTint)
            ReceiptStatus.ERROR -> Pair(style.errorIcon ?: defaultErrorIcon, style.errorIconTint)
        }

        icon?.let { receiptIcon.setImageDrawable(it) }
        if (tint != 0) receiptIcon.setColorFilter(tint)
    }

    private fun getReceiptStatus(message: BaseMessage): ReceiptStatus {
        return when {
            message.readAt > 0 -> ReceiptStatus.READ
            message.deliveredAt > 0 -> ReceiptStatus.DELIVERED
            message.id > 0 -> ReceiptStatus.SENT  // Message has server-assigned ID = sent
            else -> ReceiptStatus.IN_PROGRESS     // No ID yet = in progress
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
            val typedArray = context.theme.obtainStyledAttributes(styleRes, R.styleable.CometChatMessageReceipt)
            // fromTypedArray handles recycling internally
            setStyle(CometChatReceiptStyle.fromTypedArray(context, typedArray))
        }
    }

    /**
     * Applies a style to the receipt view.
     */
    fun setStyle(style: CometChatReceiptStyle) {
        this.style = style
        applyStyle()
    }

    /**
     * Sets the receipt status from a BaseMessage.
     */
    fun setReceipt(message: BaseMessage) {
        currentStatus = getReceiptStatus(message)
        updateReceiptIcon()
    }

    /**
     * Sets the receipt status directly.
     */
    fun setReceipt(status: ReceiptStatus) {
        currentStatus = status
        updateReceiptIcon()
    }

    // ========================================
    // Getters (read from style object)
    // ========================================

    fun getWaitIconTint(): Int = style.waitIconTint

    fun getSentIconTint(): Int = style.sentIconTint

    fun getDeliveredIconTint(): Int = style.deliveredIconTint

    fun getReadIconTint(): Int = style.readIconTint

    fun getErrorIconTint(): Int = style.errorIconTint

    fun getWaitIcon(): Drawable? = style.waitIcon

    fun getSentIcon(): Drawable? = style.sentIcon

    fun getDeliveredIcon(): Drawable? = style.deliveredIcon

    fun getReadIcon(): Drawable? = style.readIcon

    fun getErrorIcon(): Drawable? = style.errorIcon

    // ========================================
    // Setters (update style object + apply)
    // ========================================

    fun setWaitIconTint(@ColorInt tint: Int) {
        style = style.copy(waitIconTint = tint)
        if (currentStatus == ReceiptStatus.IN_PROGRESS) updateReceiptIcon()
    }

    fun setSentIconTint(@ColorInt tint: Int) {
        style = style.copy(sentIconTint = tint)
        if (currentStatus == ReceiptStatus.SENT) updateReceiptIcon()
    }

    fun setDeliveredIconTint(@ColorInt tint: Int) {
        style = style.copy(deliveredIconTint = tint)
        if (currentStatus == ReceiptStatus.DELIVERED) updateReceiptIcon()
    }

    fun setReadIconTint(@ColorInt tint: Int) {
        style = style.copy(readIconTint = tint)
        if (currentStatus == ReceiptStatus.READ) updateReceiptIcon()
    }

    fun setErrorIconTint(@ColorInt tint: Int) {
        style = style.copy(errorIconTint = tint)
        if (currentStatus == ReceiptStatus.ERROR) updateReceiptIcon()
    }

    fun setWaitIcon(icon: Drawable?) {
        style = style.copy(waitIcon = icon)
        if (currentStatus == ReceiptStatus.IN_PROGRESS) updateReceiptIcon()
    }

    fun setSentIcon(icon: Drawable?) {
        style = style.copy(sentIcon = icon)
        if (currentStatus == ReceiptStatus.SENT) updateReceiptIcon()
    }

    fun setDeliveredIcon(icon: Drawable?) {
        style = style.copy(deliveredIcon = icon)
        if (currentStatus == ReceiptStatus.DELIVERED) updateReceiptIcon()
    }

    fun setReadIcon(icon: Drawable?) {
        style = style.copy(readIcon = icon)
        if (currentStatus == ReceiptStatus.READ) updateReceiptIcon()
    }

    fun setErrorIcon(icon: Drawable?) {
        style = style.copy(errorIcon = icon)
        if (currentStatus == ReceiptStatus.ERROR) updateReceiptIcon()
    }
}
