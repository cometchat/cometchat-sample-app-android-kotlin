package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.callactionbubble

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.models.User
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.google.android.material.card.MaterialCardView

/**
 * A custom view that displays a call action message bubble.
 *
 * This class extends [MaterialCardView] to provide rich material design support.
 *
 * Features:
 * - Display call-related system messages (incoming, outgoing, missed)
 * - Appropriate icon based on call type and direction
 * - Special styling for missed calls (red text and background)
 * - Pill-shaped appearance
 *
 * Example usage:
 * ```kotlin
 * val callActionBubble = CometChatCallActionBubble(context)
 * callActionBubble.setMessage(callMessage)
 * ```
 */
class CometChatCallActionBubble @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private lateinit var callIconImageView: ImageView
    private lateinit var statusTextView: TextView

    // Single style object - NO individual style properties
    private var style: CometChatCallActionBubbleStyle = CometChatCallActionBubbleStyle()

    // State
    private var isMissedCall: Boolean = false

    init {
        inflateAndInitializeView(attrs, defStyleAttr)
    }

    /**
     * Initializes the view by inflating the XML layout and binding child views.
     *
     * @param attrs The attribute set for customization.
     * @param defStyleAttr The default style attribute.
     */
    private fun inflateAndInitializeView(attrs: AttributeSet?, defStyleAttr: Int) {
        Utils.initMaterialCard(this)
        LayoutInflater.from(context).inflate(R.layout.cometchat_call_action_bubble, this, true)
        callIconImageView = findViewById(R.id.cometchat_call_action_bubble_icon)
        statusTextView = findViewById(R.id.cometchat_call_action_bubble_text)
        applyStyleAttributes(attrs, defStyleAttr)
    }

    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        if (attrs == null) {
            style = CometChatCallActionBubbleStyle.default(context)
            applyStyle()
            return
        }

        var typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatCallActionBubble, defStyleAttr, 0
        )
        val styleResId = typedArray.getResourceId(
            R.styleable.CometChatCallActionBubble_cometchatCallActionBubbleStyle, 0
        )
        typedArray.recycle()

        typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatCallActionBubble, defStyleAttr, styleResId
        )
        // fromTypedArray handles recycling internally
        style = CometChatCallActionBubbleStyle.fromTypedArray(context, typedArray)
        applyStyle()
    }

    /**
     * Applies all style properties to views.
     */
    private fun applyStyle() {
        // Apply based on missed call state
        val textColor = if (isMissedCall) style.missedCallTextColor else style.textColor
        val textAppearance = if (isMissedCall) style.missedCallTextAppearance else style.textAppearance
        val iconTint = if (isMissedCall) style.missedCallIconTint else style.iconTint
        val strokeWidthValue = if (isMissedCall) 0f else style.strokeWidth

        // Bubble container styling
        // Background handled by wrapper CometChatMessageBubble - content views are transparent by default
        // if (bgColor != 0) setCardBackgroundColor(bgColor)
        if (style.cornerRadius != 0f) radius = style.cornerRadius
        strokeWidth = strokeWidthValue.toInt()
        if (style.strokeColor != 0) strokeColor = style.strokeColor

        // Text styling
        if (textColor != 0) statusTextView.setTextColor(textColor)
        if (textAppearance != 0) statusTextView.setTextAppearance(textAppearance)

        // Icon styling
        if (iconTint != 0) callIconImageView.setColorFilter(iconTint)
    }

    // ========================================
    // Public API - Message Setting
    // ========================================

    /**
     * Sets the message to display in the call action bubble.
     * Extracts call type, direction, and status from the Call message.
     *
     * @param message The Call message
     */
    fun setMessage(message: Call?) {
        if (message == null) return

        val isVideo = message.type == CometChatConstants.CALL_TYPE_VIDEO
        val loggedInUserId = CometChatUIKit.getLoggedInUser()?.uid
        val initiatorUid = (message.callInitiator as? User)?.uid
        val isInitiator = initiatorUid == loggedInUserId
        val callStatus = message.callStatus

        // Missed call: status is UNANSWERED AND logged-in user is NOT the initiator
        // (i.e., they received the call but didn't answer)
        isMissedCall = callStatus == CometChatConstants.CALL_STATUS_UNANSWERED && !isInitiator

        // Set icon based on call type, status, and initiator
        val iconRes = getCallIcon(isVideo, isInitiator, callStatus)
        callIconImageView.setImageResource(iconRes)

        // Set status text
        val statusText = getCallStatusText(isVideo, isInitiator, callStatus)
        statusTextView.text = statusText

        // Apply styles based on missed status
        applyStyle()
    }

    /**
     * Sets the call type manually.
     *
     * @param type The call type ("audio" or "video")
     * @param isMissed Whether the call was missed
     * @param isInitiator Whether the logged-in user initiated the call
     */
    fun setCallType(type: String, isMissed: Boolean, isInitiator: Boolean = false) {
        val isVideo = type.equals("video", ignoreCase = true)
        isMissedCall = isMissed

        val callStatus = if (isMissed) CometChatConstants.CALL_STATUS_UNANSWERED else null
        val iconRes = getCallIcon(isVideo, isInitiator, callStatus)
        callIconImageView.setImageResource(iconRes)

        val statusText = getCallStatusText(isVideo, isInitiator, callStatus)
        statusTextView.text = statusText

        applyStyle()
    }

    private fun getCallIcon(isVideo: Boolean, isInitiator: Boolean, callStatus: String?): Int {
        return when {
            isVideo -> when (callStatus) {
                CometChatConstants.CALL_STATUS_INITIATED -> {
                    if (isInitiator) R.drawable.cometchat_ic_outgoing_video_call
                    else R.drawable.cometchat_ic_incoming_video_call
                }
                CometChatConstants.CALL_STATUS_UNANSWERED -> {
                    if (isInitiator) R.drawable.cometchat_ic_video_call
                    else R.drawable.cometchat_ic_missed_video_call
                }
                else -> R.drawable.cometchat_ic_video_call
            }
            else -> when (callStatus) { // Audio call
                CometChatConstants.CALL_STATUS_INITIATED -> {
                    if (isInitiator) R.drawable.cometchat_ic_outgoing_voice_call
                    else R.drawable.cometchat_ic_incoming_voice_call
                }
                CometChatConstants.CALL_STATUS_UNANSWERED -> {
                    if (isInitiator) R.drawable.cometchat_ic_default_voice_call
                    else R.drawable.cometchat_ic_missed_voice_call
                }
                else -> R.drawable.cometchat_ic_default_voice_call
            }
        }
    }

    private fun getCallStatusText(isVideo: Boolean, isInitiator: Boolean, callStatus: String?): String {
        return when {
            callStatus == CometChatConstants.CALL_STATUS_UNANSWERED && !isInitiator -> {
                if (isVideo) context.getString(R.string.cometchat_missed_video_call)
                else context.getString(R.string.cometchat_missed_voice_call)
            }
            callStatus == CometChatConstants.CALL_STATUS_INITIATED -> {
                if (isInitiator) {
                    if (isVideo) context.getString(R.string.cometchat_outgoing_video_call)
                    else context.getString(R.string.cometchat_outgoing_voice_call)
                } else {
                    if (isVideo) context.getString(R.string.cometchat_incoming_video_call)
                    else context.getString(R.string.cometchat_incoming_voice_call)
                }
            }
            else -> {
                if (isVideo) context.getString(R.string.cometchat_video_call)
                else context.getString(R.string.cometchat_voice_call)
            }
        }
    }

    // ========================================
    // Public Style Methods
    // ========================================

    /**
     * Sets the style from a style object.
     */
    fun setStyle(style: CometChatCallActionBubbleStyle) {
        this.style = style
        applyStyle()
    }

    /**
     * Sets the style from a style resource.
     */
    fun setStyle(@StyleRes styleRes: Int) {
        if (styleRes != 0) {
            val typedArray = context.theme.obtainStyledAttributes(
                styleRes, R.styleable.CometChatCallActionBubble
            )
            // fromTypedArray handles recycling internally
            setStyle(CometChatCallActionBubbleStyle.fromTypedArray(context, typedArray))
        }
    }

    // ========================================
    // Getters (read from style object)
    // ========================================

    fun getBubbleBackgroundColor(): Int = style.backgroundColor
    fun getBubbleCornerRadius(): Float = style.cornerRadius
    fun getBubbleStrokeWidth(): Float = style.strokeWidth
    fun getBubbleStrokeColor(): Int = style.strokeColor
    fun getTextColor(): Int = style.textColor
    fun getIconTint(): Int = style.iconTint
    fun getMissedCallTextColor(): Int = style.missedCallTextColor
    fun getMissedCallBackgroundColor(): Int = style.missedCallBackgroundColor
    fun getMissedCallIconTint(): Int = style.missedCallIconTint

    // ========================================
    // Setters (update style object + apply)
    // ========================================

    override fun setBackgroundColor(@ColorInt color: Int) {
        style = style.copy(backgroundColor = color)
        if (!isMissedCall) {
            setCardBackgroundColor(color)
        }
    }

    fun setCornerRadius(@Dimension radius: Int) {
        style = style.copy(cornerRadius = radius.toFloat())
        setRadius(radius.toFloat())
    }

    fun setBubbleStrokeWidth(@Dimension width: Int) {
        style = style.copy(strokeWidth = width.toFloat())
        if (!isMissedCall) {
            strokeWidth = width
        }
    }

    fun setBubbleStrokeColor(@ColorInt color: Int) {
        style = style.copy(strokeColor = color)
        strokeColor = color
    }

    fun setTextColor(@ColorInt color: Int) {
        style = style.copy(textColor = color)
        if (!isMissedCall) {
            statusTextView.setTextColor(color)
        }
    }

    fun setIconTint(@ColorInt color: Int) {
        style = style.copy(iconTint = color)
        if (!isMissedCall) {
            callIconImageView.setColorFilter(color)
        }
    }

    fun setMissedCallTextColor(@ColorInt color: Int) {
        style = style.copy(missedCallTextColor = color)
        if (isMissedCall) {
            statusTextView.setTextColor(color)
        }
    }

    fun setMissedCallBackgroundColor(@ColorInt color: Int) {
        style = style.copy(missedCallBackgroundColor = color)
        if (isMissedCall) {
            setCardBackgroundColor(color)
        }
    }

    fun setMissedCallIconTint(@ColorInt color: Int) {
        style = style.copy(missedCallIconTint = color)
        if (isMissedCall) {
            callIconImageView.setColorFilter(color)
        }
    }

    companion object {
        private val TAG = CometChatCallActionBubble::class.java.simpleName
    }
}
