package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.meetcallbubble

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.CustomMessage
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Enum representing the type of meeting call.
 */
enum class MeetCallType {
    VOICE_INCOMING,
    VOICE_OUTGOING,
    VIDEO_INCOMING,
    VIDEO_OUTGOING
}

/**
 * A custom view that displays a meeting/call invitation message bubble.
 *
 * This class extends [MaterialCardView] to provide rich material design support.
 *
 * Features:
 * - Display meeting call invitations with call type icon
 * - Call type icon (voice/video) with direction indicator
 * - Call title and subtitle
 * - "Join" button with separator
 * - Circular icon background
 *
 * Example usage:
 * ```kotlin
 * val meetCallBubble = CometChatMeetCallBubble(context)
 * meetCallBubble.setMessage(customMessage)
 * ```
 */
class CometChatMeetCallBubble @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    // Views bound from XML layout
    private lateinit var callIconCard: MaterialCardView
    private lateinit var callIconImageView: ImageView
    private lateinit var titleTextView: TextView
    private lateinit var subtitleTextView: TextView
    private lateinit var separatorView: View
    private lateinit var joinCallContainer: LinearLayout
    private lateinit var joinCallText: TextView

    // Single style object - nullable during initialization to handle parent constructor calls
    private var style: CometChatMeetCallBubbleStyle? = null

    // State
    private var sessionId: String = ""
    private var currentCallType: MeetCallType = MeetCallType.VOICE_OUTGOING
    private var onJoinClickListener: OnClickListener? = null

    init {
        inflateAndInitializeView(attrs, defStyleAttr)
    }

    private fun inflateAndInitializeView(attrs: AttributeSet?, defStyleAttr: Int) {
        Utils.initMaterialCard(this)
        
        // Set fixed width to match Java reference: 240dp (cometchat_meet_call_bubble_container.xml)
        layoutParams = LayoutParams(
            resources.getDimensionPixelSize(R.dimen.cometchat_240dp),
            LayoutParams.WRAP_CONTENT
        )
        
        LayoutInflater.from(context).inflate(R.layout.cometchat_meet_call_bubble, this, true)
        
        // Bind views from XML layout
        callIconCard = findViewById(R.id.call_icon_card)
        callIconImageView = findViewById(R.id.call_icon)
        titleTextView = findViewById(R.id.title_text)
        subtitleTextView = findViewById(R.id.subtitle_text)
        separatorView = findViewById(R.id.separator)
        joinCallContainer = findViewById(R.id.join_call)
        joinCallText = findViewById(R.id.tv_join_call)
        
        // Initialize icon card (but preserve circular shape)
        Utils.initMaterialCard(callIconCard)
        callIconCard.radius = resources.getDimension(R.dimen.cometchat_radius_max)
        
        // Set up join button click listener
        joinCallContainer.setOnClickListener { onJoinClickListener?.onClick(it) }

        // Set long click listener to propagate to parent for message actions
        setOnLongClickListener { v ->
            Utils.performAdapterClick(v)
            true
        }
        
        applyStyleAttributes(attrs, defStyleAttr)
    }

    /**
     * Extracts style attributes from XML and applies them.
     */
    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        var typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatMeetCallBubble, defStyleAttr, 0
        )
        val styleResId = typedArray.getResourceId(
            R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleStyle, 0
        )
        typedArray.recycle()

        typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatMeetCallBubble, defStyleAttr, styleResId
        )
        // fromTypedArray handles recycling internally
        style = CometChatMeetCallBubbleStyle.fromTypedArray(context, typedArray)
        applyStyle()
    }

    /**
     * Applies all style properties to views.
     */
    private fun applyStyle() {
        val currentStyle = style ?: return

        // Bubble container styling
        if (currentStyle.cornerRadius != 0f) applyCornerRadius(currentStyle.cornerRadius)
        if (currentStyle.strokeWidth != 0f) applyStrokeWidth(currentStyle.strokeWidth)
        if (currentStyle.strokeColor != 0) applyStrokeColor(currentStyle.strokeColor)
        currentStyle.backgroundDrawable?.let { applyBackgroundDrawable(it) }

        // Title styling
        if (currentStyle.titleTextColor != 0) applyTitleTextColor(currentStyle.titleTextColor)
        if (currentStyle.titleTextAppearance != 0) applyTitleTextAppearance(currentStyle.titleTextAppearance)

        // Subtitle styling
        if (currentStyle.subtitleTextColor != 0) applySubtitleTextColor(currentStyle.subtitleTextColor)
        if (currentStyle.subtitleTextAppearance != 0) applySubtitleTextAppearance(currentStyle.subtitleTextAppearance)

        // Icon styling
        applyCallIconTint(currentStyle.callIconTint)
        applyIconBackgroundColor(currentStyle.iconBackgroundColor)

        // Button styling
        if (currentStyle.joinButtonTextColor != 0) applyJoinButtonTextColor(currentStyle.joinButtonTextColor)
        if (currentStyle.joinButtonTextAppearance != 0) applyJoinButtonTextAppearance(currentStyle.joinButtonTextAppearance)

        // Separator styling
        if (currentStyle.separatorColor != 0) applySeparatorColor(currentStyle.separatorColor)

        // Update icon based on current call type
        updateCallIcon()
    }

    private fun updateCallIcon() {
        val currentStyle = style
        
        // Check if we have a drawable from style
        val iconDrawable = when (currentCallType) {
            MeetCallType.VOICE_INCOMING -> currentStyle?.incomingVoiceCallIcon
            MeetCallType.VOICE_OUTGOING -> currentStyle?.outgoingVoiceCallIcon
            MeetCallType.VIDEO_INCOMING -> currentStyle?.incomingVideoCallIcon
            MeetCallType.VIDEO_OUTGOING -> currentStyle?.outgoingVideoCallIcon
        }
        
        if (iconDrawable != null) {
            callIconImageView.setImageDrawable(iconDrawable)
        } else {
            val defaultIconRes = when (currentCallType) {
                MeetCallType.VOICE_INCOMING -> R.drawable.cometchat_ic_incoming_voice_call
                MeetCallType.VOICE_OUTGOING -> R.drawable.cometchat_ic_outgoing_voice_call
                MeetCallType.VIDEO_INCOMING -> R.drawable.cometchat_ic_incoming_video_call
                MeetCallType.VIDEO_OUTGOING -> R.drawable.cometchat_ic_outgoing_video_call
            }
            callIconImageView.setImageResource(defaultIconRes)
        }
    }

    /**
     * Sets the message to display in the meet call bubble.
     * Extracts call type, session ID, and title from message.customData.
     *
     * @param message The CustomMessage containing meeting call data
     */
    fun setMessage(message: CustomMessage?) {
        if (message == null) return

        try {
            val customData = message.customData

            // Extract call type from customData
            val callTypeString = customData?.optString("callType", CometChatConstants.CALL_TYPE_AUDIO)
                ?: CometChatConstants.CALL_TYPE_AUDIO
            val isVideo = callTypeString == CometChatConstants.CALL_TYPE_VIDEO

            // Determine direction based on sender vs logged-in user
            val loggedInUserId = CometChatUIKit.getLoggedInUser()?.uid
            val isIncoming = message.sender?.uid != loggedInUserId

            // Determine call type enum
            currentCallType = when {
                isVideo && isIncoming -> MeetCallType.VIDEO_INCOMING
                isVideo -> MeetCallType.VIDEO_OUTGOING
                isIncoming -> MeetCallType.VOICE_INCOMING
                else -> MeetCallType.VOICE_OUTGOING
            }

            // Extract session ID
            sessionId = customData?.optString("sessionId", "") ?: ""

            // Extract or generate title
            val titleFromData = customData?.optString("title", "")
            val title = if (titleFromData.isNullOrEmpty()) {
                if (isVideo) context.getString(R.string.cometchat_video_call) else context.getString(R.string.cometchat_audio_call)
            } else {
                titleFromData
            }
            setTitle(title)

            // Format subtitle from sentAt timestamp
            val subtitle = formatTimestamp(message.sentAt)
            setSubtitle(subtitle)

            // Update icon
            updateCallIcon()
        } catch (e: Exception) {
            setTitle(context.getString(R.string.cometchat_audio_call))
            setSubtitle("")
            currentCallType = MeetCallType.VOICE_OUTGOING
            updateCallIcon()
        }
    }

    /**
     * Sets the call type manually.
     *
     * @param isVideo Whether this is a video call
     * @param isIncoming Whether this is an incoming call
     */
    fun setCallType(isVideo: Boolean, isIncoming: Boolean) {
        currentCallType = when {
            isVideo && isIncoming -> MeetCallType.VIDEO_INCOMING
            isVideo -> MeetCallType.VIDEO_OUTGOING
            isIncoming -> MeetCallType.VOICE_INCOMING
            else -> MeetCallType.VOICE_OUTGOING
        }
        updateCallIcon()
    }

    /**
     * Sets the call type using the enum directly.
     *
     * @param callType The MeetCallType enum value
     */
    fun setCallType(callType: MeetCallType) {
        currentCallType = callType
        updateCallIcon()
    }

    /**
     * Formats a timestamp (in seconds) to a human-readable date string.
     *
     * @param seconds The timestamp in seconds since epoch
     * @return Formatted date string (e.g., "15 Jan, 10:30 AM")
     */
    private fun formatTimestamp(seconds: Long): String {
        return try {
            val milliseconds = seconds * 1000
            val date = Date(milliseconds)
            val formatter = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
            formatter.format(date)
        } catch (e: Exception) {
            ""
        }
    }

    // ========================================
    // Public API Methods (maintained for backward compatibility)
    // ========================================

    fun setTitle(title: String) {
        titleTextView.text = title
    }

    fun getTitle(): String = titleTextView.text.toString()

    /**
     * Sets the title text of the call bubble.
     * Alias for setTitle() to match Java API.
     *
     * @param title The text to be displayed as the title.
     */
    fun setTitleText(title: String) {
        titleTextView.text = title
    }

    fun setSubtitle(subtitle: String) {
        subtitleTextView.text = subtitle
        subtitleTextView.visibility = if (subtitle.isEmpty()) View.GONE else View.VISIBLE
    }

    fun getSubtitle(): String = subtitleTextView.text.toString()

    /**
     * Sets the subtitle text of the call bubble.
     * Alias for setSubtitle() to match Java API.
     *
     * @param title The text to be displayed as the subtitle.
     */
    fun setSubtitleText(title: String) {
        setSubtitle(title)
    }

    fun setSessionId(sessionId: String) {
        this.sessionId = sessionId
    }

    fun getSessionId(): String = sessionId

    fun setOnJoinClick(listener: OnClickListener?) {
        onJoinClickListener = listener
    }

    /**
     * Sets the click listener for the join call button.
     * Matches Java API signature using OnClick interface.
     *
     * @param onClick The callback to be invoked when the button is clicked.
     */
    fun setOnClick(onClick: (() -> Unit)?) {
        if (onClick != null) {
            onJoinClickListener = OnClickListener { onClick() }
        } else {
            onJoinClickListener = null
        }
    }

    fun getCallType(): MeetCallType = currentCallType

    /**
     * Sets the text of the join call button.
     *
     * @param text The text to be displayed on the button.
     */
    fun setButtonText(text: String) {
        joinCallText.text = text
    }

    // ========================================
    // Public Style Methods
    // ========================================

    /**
     * Sets the style from a style object.
     */
    fun setStyle(style: CometChatMeetCallBubbleStyle) {
        this.style = style
        applyStyle()
    }

    /**
     * Sets the style from a style resource.
     */
    fun setStyle(@StyleRes styleRes: Int) {
        if (styleRes != 0) {
            val typedArray = context.theme.obtainStyledAttributes(
                styleRes, R.styleable.CometChatMeetCallBubble
            )
            // fromTypedArray handles recycling internally
            setStyle(CometChatMeetCallBubbleStyle.fromTypedArray(context, typedArray))
        }
    }

    // ========================================
    // Getters (read from style object)
    // ========================================

    fun getBubbleBackgroundColor(): Int = style?.backgroundColor ?: 0
    fun getBubbleCornerRadius(): Float = style?.cornerRadius ?: 0f
    fun getBubbleStrokeWidth(): Float = style?.strokeWidth ?: 0f
    fun getBubbleStrokeColor(): Int = style?.strokeColor ?: 0
    fun getCallIconTint(): Int = style?.callIconTint ?: 0
    fun getIconBackgroundColor(): Int = style?.iconBackgroundColor ?: 0
    fun getTitleTextColor(): Int = style?.titleTextColor ?: 0
    fun getTitleTextAppearance(): Int = style?.titleTextAppearance ?: 0
    fun getSubtitleTextColor(): Int = style?.subtitleTextColor ?: 0
    fun getSubtitleTextAppearance(): Int = style?.subtitleTextAppearance ?: 0
    fun getSeparatorColor(): Int = style?.separatorColor ?: 0
    fun getJoinButtonTextColor(): Int = style?.joinButtonTextColor ?: 0
    fun getJoinButtonTextAppearance(): Int = style?.joinButtonTextAppearance ?: 0
    fun getSenderNameTextColor(): Int = style?.senderNameTextColor ?: 0
    fun getSenderNameTextAppearance(): Int = style?.senderNameTextAppearance ?: 0
    fun getThreadIndicatorTextColor(): Int = style?.threadIndicatorTextColor ?: 0
    fun getThreadIndicatorTextAppearance(): Int = style?.threadIndicatorTextAppearance ?: 0
    fun getThreadIndicatorIconTint(): Int = style?.threadIndicatorIconTint ?: 0

    // ========================================
    // Setters (update style object + apply)
    // ========================================

    override fun setBackgroundColor(@ColorInt color: Int) {
        style = style?.copy(backgroundColor = color) ?: CometChatMeetCallBubbleStyle(backgroundColor = color)
        applyBackgroundColor(color)
    }

    fun setCornerRadius(@Dimension radius: Float) {
        style = style?.copy(cornerRadius = radius) ?: CometChatMeetCallBubbleStyle(cornerRadius = radius)
        applyCornerRadius(radius)
    }

    fun setBubbleStrokeWidth(@Dimension width: Float) {
        style = style?.copy(strokeWidth = width) ?: CometChatMeetCallBubbleStyle(strokeWidth = width)
        applyStrokeWidth(width)
    }

    fun setBubbleStrokeColor(@ColorInt color: Int) {
        style = style?.copy(strokeColor = color) ?: CometChatMeetCallBubbleStyle(strokeColor = color)
        applyStrokeColor(color)
    }

    override fun setBackgroundDrawable(drawable: Drawable?) {
        // Guard against calls during parent constructor initialization when style is null
        if (style == null) {
            super.setBackgroundDrawable(drawable)
            return
        }
        style = style?.copy(backgroundDrawable = drawable)
        drawable?.let { applyBackgroundDrawable(it) }
    }

    fun setCallIconTint(@ColorInt color: Int) {
        style = style?.copy(callIconTint = color) ?: CometChatMeetCallBubbleStyle(callIconTint = color)
        applyCallIconTint(color)
    }

    fun setIconBackgroundColor(@ColorInt color: Int) {
        style = style?.copy(iconBackgroundColor = color) ?: CometChatMeetCallBubbleStyle(iconBackgroundColor = color)
        applyIconBackgroundColor(color)
    }

    fun setTitleTextColor(@ColorInt color: Int) {
        style = style?.copy(titleTextColor = color) ?: CometChatMeetCallBubbleStyle(titleTextColor = color)
        applyTitleTextColor(color)
    }

    fun setTitleTextAppearance(@StyleRes appearance: Int) {
        style = style?.copy(titleTextAppearance = appearance) ?: CometChatMeetCallBubbleStyle(titleTextAppearance = appearance)
        applyTitleTextAppearance(appearance)
    }

    fun setSubtitleTextColor(@ColorInt color: Int) {
        style = style?.copy(subtitleTextColor = color) ?: CometChatMeetCallBubbleStyle(subtitleTextColor = color)
        applySubtitleTextColor(color)
    }

    fun setSubtitleTextAppearance(@StyleRes appearance: Int) {
        style = style?.copy(subtitleTextAppearance = appearance) ?: CometChatMeetCallBubbleStyle(subtitleTextAppearance = appearance)
        applySubtitleTextAppearance(appearance)
    }

    fun setSeparatorColor(@ColorInt color: Int) {
        style = style?.copy(separatorColor = color) ?: CometChatMeetCallBubbleStyle(separatorColor = color)
        applySeparatorColor(color)
    }

    fun setJoinButtonTextColor(@ColorInt color: Int) {
        style = style?.copy(joinButtonTextColor = color) ?: CometChatMeetCallBubbleStyle(joinButtonTextColor = color)
        applyJoinButtonTextColor(color)
    }

    fun setJoinButtonTextAppearance(@StyleRes appearance: Int) {
        style = style?.copy(joinButtonTextAppearance = appearance) ?: CometChatMeetCallBubbleStyle(joinButtonTextAppearance = appearance)
        applyJoinButtonTextAppearance(appearance)
    }

    /**
     * Sets the text color of the join call button.
     * Alias for setJoinButtonTextColor() to match Java API.
     *
     * @param color The color to be applied to the button text.
     */
    fun setButtonTextColor(@ColorInt color: Int) {
        setJoinButtonTextColor(color)
    }

    /**
     * Sets the appearance of the join call button text using a text style resource.
     * Alias for setJoinButtonTextAppearance() to match Java API.
     *
     * @param appearance The text style resource defining the appearance of the button text.
     */
    fun setButtonTextAppearance(@StyleRes appearance: Int) {
        setJoinButtonTextAppearance(appearance)
    }

    fun setSenderNameTextColor(@ColorInt color: Int) {
        style = style?.copy(senderNameTextColor = color) ?: CometChatMeetCallBubbleStyle(senderNameTextColor = color)
    }

    fun setSenderNameTextAppearance(@StyleRes appearance: Int) {
        style = style?.copy(senderNameTextAppearance = appearance) ?: CometChatMeetCallBubbleStyle(senderNameTextAppearance = appearance)
    }

    fun setThreadIndicatorTextColor(@ColorInt color: Int) {
        style = style?.copy(threadIndicatorTextColor = color) ?: CometChatMeetCallBubbleStyle(threadIndicatorTextColor = color)
    }

    fun setThreadIndicatorTextAppearance(@StyleRes appearance: Int) {
        style = style?.copy(threadIndicatorTextAppearance = appearance) ?: CometChatMeetCallBubbleStyle(threadIndicatorTextAppearance = appearance)
    }

    fun setThreadIndicatorIconTint(@ColorInt color: Int) {
        style = style?.copy(threadIndicatorIconTint = color) ?: CometChatMeetCallBubbleStyle(threadIndicatorIconTint = color)
    }

    fun setIncomingVoiceCallIcon(icon: Drawable?) {
        style = style?.copy(incomingVoiceCallIcon = icon) ?: CometChatMeetCallBubbleStyle(incomingVoiceCallIcon = icon)
        if (currentCallType == MeetCallType.VOICE_INCOMING) {
            icon?.let { callIconImageView.setImageDrawable(it) }
        }
    }

    fun setIncomingVideoCallIcon(icon: Drawable?) {
        style = style?.copy(incomingVideoCallIcon = icon) ?: CometChatMeetCallBubbleStyle(incomingVideoCallIcon = icon)
        if (currentCallType == MeetCallType.VIDEO_INCOMING) {
            icon?.let { callIconImageView.setImageDrawable(it) }
        }
    }

    fun setOutgoingVoiceCallIcon(icon: Drawable?) {
        style = style?.copy(outgoingVoiceCallIcon = icon) ?: CometChatMeetCallBubbleStyle(outgoingVoiceCallIcon = icon)
        if (currentCallType == MeetCallType.VOICE_OUTGOING) {
            icon?.let { callIconImageView.setImageDrawable(it) }
        }
    }

    fun setOutgoingVideoCallIcon(icon: Drawable?) {
        style = style?.copy(outgoingVideoCallIcon = icon) ?: CometChatMeetCallBubbleStyle(outgoingVideoCallIcon = icon)
        if (currentCallType == MeetCallType.VIDEO_OUTGOING) {
            icon?.let { callIconImageView.setImageDrawable(it) }
        }
    }

    // ========================================
    // Private Apply Methods
    // ========================================

    private fun applyBackgroundColor(@ColorInt color: Int) {
        setCardBackgroundColor(color)
    }

    private fun applyCornerRadius(@Dimension radius: Float) {
        setRadius(radius)
    }

    private fun applyStrokeWidth(@Dimension width: Float) {
        strokeWidth = width.toInt()
    }

    private fun applyStrokeColor(@ColorInt color: Int) {
        strokeColor = color
    }

    private fun applyBackgroundDrawable(drawable: Drawable) {
        super.setBackgroundDrawable(drawable)
    }

    private fun applyCallIconTint(@ColorInt color: Int) {
        callIconImageView.setColorFilter(color)
    }

    private fun applyIconBackgroundColor(@ColorInt color: Int) {
        callIconCard.setCardBackgroundColor(color)
    }

    private fun applyTitleTextColor(@ColorInt color: Int) {
        titleTextView.setTextColor(color)
    }

    private fun applyTitleTextAppearance(@StyleRes appearance: Int) {
        if (appearance != 0) {
            titleTextView.setTextAppearance(appearance)
        }
    }

    private fun applySubtitleTextColor(@ColorInt color: Int) {
        subtitleTextView.setTextColor(color)
    }

    private fun applySubtitleTextAppearance(@StyleRes appearance: Int) {
        if (appearance != 0) {
            subtitleTextView.setTextAppearance(appearance)
        }
    }

    private fun applySeparatorColor(@ColorInt color: Int) {
        separatorView.setBackgroundColor(color)
    }

    private fun applyJoinButtonTextColor(@ColorInt color: Int) {
        joinCallText.setTextColor(color)
    }

    private fun applyJoinButtonTextAppearance(@StyleRes appearance: Int) {
        if (appearance != 0) {
            joinCallText.setTextAppearance(appearance)
        }
    }

    companion object {
        private val TAG = CometChatMeetCallBubble::class.java.simpleName
    }
}
