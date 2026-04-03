package com.cometchat.uikit.kotlin.presentation.calllogs.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StyleRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.cometchat.calls.model.CallLog
import com.cometchat.uikit.core.utils.CallLogsUtils
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.databinding.CometchatCallLogsListItemBinding
import com.cometchat.uikit.kotlin.presentation.calllogs.style.CometChatCallLogsListItemStyle
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.date.DatePattern
import com.cometchat.uikit.kotlin.shared.interfaces.DateTimeFormatterCallback
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * CometChatCallLogsListItem is a custom Android View that displays a single call log item
 * with avatar, title (caller/callee name), subtitle (direction icon + date), and trailing (call type icon).
 *
 * This component can be used standalone or within a RecyclerView for displaying call logs lists.
 * It supports full customization through styles and custom view slots.
 */
class CometChatCallLogsListItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = CometChatCallLogsListItem::class.java.simpleName
    }

    // View Binding
    internal val binding: CometchatCallLogsListItemBinding = CometchatCallLogsListItemBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    // Current call log
    private var callLog: CallLog? = null

    // Callbacks
    private var onItemClick: ((CallLog) -> Unit)? = null
    private var onItemLongClick: ((CallLog) -> Unit)? = null
    private var onCallTypeIconClick: ((CallLog) -> Unit)? = null

    // Custom views
    private var customLeadingView: View? = null
    private var customTitleView: View? = null
    private var customSubtitleView: View? = null
    private var customTrailingView: View? = null

    // Visibility controls
    private var hideSeparator: Boolean = false

    // Style - initialized with default values
    private var style: CometChatCallLogsListItemStyle = CometChatCallLogsListItemStyle.default(context)

    // Date/time formatter callback
    private var dateTimeFormatter: DateTimeFormatterCallback? = null

    init {
        // Apply XML attributes (will override defaults if style is provided)
        applyStyleAttributes(attrs, defStyleAttr)

        // Setup click listeners
        setupClickListeners()
    }

    /**
     * Applies style attributes from XML.
     */
    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        var typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatCallLogs, defStyleAttr, 0
        )
        val styleResId = typedArray.getResourceId(
            R.styleable.CometChatCallLogs_cometchatCallLogsStyle, 0
        )
        typedArray.recycle()

        // Only extract from TypedArray if a style resource is provided
        if (styleResId != 0) {
            typedArray = context.theme.obtainStyledAttributes(
                attrs, R.styleable.CometChatCallLogs, defStyleAttr, styleResId
            )
            style = CometChatCallLogsListItemStyle.fromTypedArray(context, typedArray)
        }
        // Otherwise keep the default style initialized in the field
        
        applyStyle()
    }

    /**
     * Applies all style properties from the style object to views.
     */
    private fun applyStyle() {
        // Background
        if (style.backgroundColor != 0) {
            binding.parentLayout.setBackgroundColor(style.backgroundColor)
        }

        // Title
        if (style.titleTextColor != 0) {
            binding.tvTitle.setTextColor(style.titleTextColor)
        }
        if (style.titleTextAppearance != 0) {
            binding.tvTitle.setTextAppearance(style.titleTextAppearance)
        }

        // Avatar style
        binding.avatar.setStyle(style.avatarStyle)

        // Date style
        binding.dateView.setStyle(style.dateStyle)

        // Separator
        if (style.separatorColor != 0) {
            binding.separator.setBackgroundColor(style.separatorColor)
        }
        if (style.separatorHeight != 0) {
            binding.separator.layoutParams = binding.separator.layoutParams.apply {
                height = style.separatorHeight
            }
        }
        binding.separator.visibility = if (hideSeparator) View.GONE else View.VISIBLE
    }

    /**
     * Sets up click listeners for the item.
     */
    private fun setupClickListeners() {
        binding.parentLayout.setOnClickListener {
            callLog?.let { log ->
                onItemClick?.invoke(log)
            }
        }

        binding.parentLayout.setOnLongClickListener {
            callLog?.let { log ->
                onItemLongClick?.invoke(log)
            }
            onItemLongClick != null
        }

        binding.ivCallTypeIcon.setOnClickListener {
            callLog?.let { log ->
                onCallTypeIconClick?.invoke(log)
            }
        }
    }

    // ==================== Public API Methods ====================

    /**
     * Sets the call log to display.
     */
    fun setCallLog(callLog: CallLog) {
        this.callLog = callLog
        bindCallLog()
    }

    /**
     * Sets the item click callback.
     */
    fun setOnItemClick(callback: (CallLog) -> Unit) {
        onItemClick = callback
    }

    /**
     * Sets the item long click callback.
     */
    fun setOnItemLongClick(callback: (CallLog) -> Unit) {
        onItemLongClick = callback
    }

    /**
     * Sets the call type icon click callback (for initiating calls).
     */
    fun setOnCallTypeIconClick(callback: (CallLog) -> Unit) {
        onCallTypeIconClick = callback
    }

    /**
     * Sets custom leading view (replaces avatar section).
     * Pass null to restore the default leading view.
     */
    fun setLeadingView(view: View?) {
        customLeadingView = view
        if (view != null) {
            binding.leadingView.removeAllViews()
            binding.leadingView.addView(view)
        } else {
            resetLeadingView()
        }
    }

    /**
     * Sets custom title view.
     * Pass null to restore the default title view.
     */
    fun setTitleView(view: View?) {
        customTitleView = view
        if (view != null) {
            binding.titleView.removeAllViews()
            binding.titleView.addView(view)
        } else {
            resetTitleView()
        }
    }

    /**
     * Sets custom subtitle view.
     * Pass null to restore the default subtitle view.
     */
    fun setSubtitleView(view: View?) {
        customSubtitleView = view
        if (view != null) {
            binding.subtitleView.removeAllViews()
            binding.subtitleView.addView(view)
        } else {
            resetSubtitleView()
        }
    }

    /**
     * Sets custom trailing view (replaces call type icon).
     * Pass null to restore the default trailing view.
     */
    fun setTrailingView(view: View?) {
        customTrailingView = view
        if (view != null) {
            binding.trailingView.removeAllViews()
            binding.trailingView.addView(view)
        } else {
            resetTrailingView()
        }
    }

    /**
     * Sets whether to hide item separator.
     */
    fun setHideSeparator(hide: Boolean) {
        hideSeparator = hide
        binding.separator.visibility = if (hide) View.GONE else View.VISIBLE
    }

    /**
     * Sets the style from a style object.
     */
    fun setStyle(style: CometChatCallLogsListItemStyle) {
        android.util.Log.d(TAG, "setStyle called with: incomingCallIcon=${style.incomingCallIcon}, outgoingCallIcon=${style.outgoingCallIcon}, missedCallIcon=${style.missedCallIcon}, audioCallIcon=${style.audioCallIcon}, videoCallIcon=${style.videoCallIcon}")
        this.style = style
        applyStyle()
        callLog?.let { bindCallLog() }
    }

    /**
     * Sets the style from a style resource.
     */
    fun setStyle(@StyleRes styleRes: Int) {
        if (styleRes != 0) {
            val typedArray = context.theme.obtainStyledAttributes(
                styleRes, R.styleable.CometChatCallLogs
            )
            setStyle(CometChatCallLogsListItemStyle.fromTypedArray(context, typedArray))
        }
    }

    /**
     * Sets a custom date/time formatter callback for formatting dates.
     * This allows customization of how dates are displayed (today, yesterday, last week, etc.).
     *
     * @param formatter The DateTimeFormatterCallback for custom formatting, or null to use default.
     */
    fun setDateTimeFormatter(formatter: DateTimeFormatterCallback?) {
        dateTimeFormatter = formatter
        binding.dateView.setDateTimeFormatterCallback(formatter)
        callLog?.let { bindSubtitle() }
    }

    /**
     * Gets the current date/time formatter callback.
     *
     * @return The current DateTimeFormatterCallback, or null if using default.
     */
    fun getDateTimeFormatter(): DateTimeFormatterCallback? = dateTimeFormatter

    /**
     * Gets the current call log.
     */
    fun getCallLog(): CallLog? = callLog

    /**
     * Gets the leading view container for custom view placement.
     */
    fun getLeadingViewContainer(): ViewGroup = binding.leadingView

    /**
     * Gets the title view container for custom view placement.
     */
    fun getTitleViewContainer(): ViewGroup = binding.titleView

    /**
     * Gets the subtitle view container for custom view placement.
     */
    fun getSubtitleViewContainer(): ViewGroup = binding.subtitleView

    /**
     * Gets the trailing view container for custom view placement.
     */
    fun getTrailingViewContainer(): ViewGroup = binding.trailingView

    /**
     * Gets the parent layout for full item replacement.
     */
    fun getParentLayout(): ViewGroup = binding.parentLayout

    /**
     * Resets the leading view to default.
     */
    fun resetLeadingView() {
        customLeadingView = null
        binding.leadingView.removeAllViews()
        binding.leadingView.addView(binding.avatar)
        bindLeading()
    }

    /**
     * Resets the title view to default.
     */
    fun resetTitleView() {
        customTitleView = null
        binding.titleView.removeAllViews()
        binding.titleView.addView(binding.tvTitle)
        bindTitle()
    }

    /**
     * Resets the subtitle view to default.
     */
    fun resetSubtitleView() {
        customSubtitleView = null
        binding.subtitleView.removeAllViews()
        binding.subtitleView.addView(binding.defaultSubtitleLayout)
        bindSubtitle()
    }

    /**
     * Resets the trailing view to default.
     */
    fun resetTrailingView() {
        customTrailingView = null
        binding.trailingView.removeAllViews()
        binding.trailingView.addView(binding.ivCallTypeIcon)
        bindTrailing()
    }

    // ==================== Private Binding Methods ====================

    private fun bindCallLog() {
        callLog?.let {
            bindLeading()
            bindTitle()
            bindSubtitle()
            bindTrailing()
        }
    }

    private fun bindLeading() {
        if (customLeadingView != null) return

        callLog?.let { log ->
            val displayName = CallLogsUtils.getDisplayName(log)
            val avatarUrl = CallLogsUtils.getAvatarUrl(log)
            binding.avatar.setAvatar(displayName, avatarUrl)
        }
    }

    private fun bindTitle() {
        if (customTitleView != null) return

        callLog?.let { log ->
            val displayName = CallLogsUtils.getDisplayName(log)
            val isMissed = CallLogsUtils.isMissedCall(log)
            
            binding.tvTitle.text = displayName
            
            // Apply missed call styling
            if (isMissed && style.missedCallTitleColor != 0) {
                binding.tvTitle.setTextColor(style.missedCallTitleColor)
            } else if (style.titleTextColor != 0) {
                binding.tvTitle.setTextColor(style.titleTextColor)
            }
        }
    }

    private fun bindSubtitle() {
        if (customSubtitleView != null) return

        callLog?.let { log ->
            val isMissed = CallLogsUtils.isMissedCall(log)
            val isOutgoing = CallLogsUtils.isOutgoingCall(log)
            val isIncoming = CallLogsUtils.isIncomingCall(log)

            android.util.Log.d(TAG, "bindSubtitle: isMissed=$isMissed, isOutgoing=$isOutgoing, isIncoming=$isIncoming")
            android.util.Log.d(TAG, "bindSubtitle: missedCallIcon=${style.missedCallIcon}, outgoingCallIcon=${style.outgoingCallIcon}, incomingCallIcon=${style.incomingCallIcon}")

            // Set direction icon and tint
            binding.ivDirectionIcon.visibility = View.VISIBLE
            when {
                isMissed -> {
                    android.util.Log.d(TAG, "bindSubtitle: Setting MISSED call icon")
                    if (style.missedCallIcon != 0) {
                        binding.ivDirectionIcon.setImageDrawable(
                            androidx.core.content.ContextCompat.getDrawable(context, style.missedCallIcon)
                        )
                    } else {
                        android.util.Log.w(TAG, "bindSubtitle: missedCallIcon is 0!")
                    }
                    if (style.missedCallIconTint != 0) {
                        binding.ivDirectionIcon.setColorFilter(style.missedCallIconTint)
                    }
                }
                isOutgoing -> {
                    android.util.Log.d(TAG, "bindSubtitle: Setting OUTGOING call icon")
                    if (style.outgoingCallIcon != 0) {
                        binding.ivDirectionIcon.setImageDrawable(
                            androidx.core.content.ContextCompat.getDrawable(context, style.outgoingCallIcon)
                        )
                    } else {
                        android.util.Log.w(TAG, "bindSubtitle: outgoingCallIcon is 0!")
                    }
                    if (style.outgoingCallIconTint != 0) {
                        binding.ivDirectionIcon.setColorFilter(style.outgoingCallIconTint)
                    }
                }
                isIncoming -> {
                    android.util.Log.d(TAG, "bindSubtitle: Setting INCOMING call icon")
                    if (style.incomingCallIcon != 0) {
                        binding.ivDirectionIcon.setImageDrawable(
                            androidx.core.content.ContextCompat.getDrawable(context, style.incomingCallIcon)
                        )
                    } else {
                        android.util.Log.w(TAG, "bindSubtitle: incomingCallIcon is 0!")
                    }
                    if (style.incomingCallIconTint != 0) {
                        binding.ivDirectionIcon.setColorFilter(style.incomingCallIconTint)
                    }
                }
                else -> {
                    android.util.Log.d(TAG, "bindSubtitle: No call direction detected, hiding icon")
                    binding.ivDirectionIcon.visibility = View.GONE
                }
            }

            // Set date using call logs timestamp format (e.g., "17 February, 11:30 AM")
            val timestamp = log.initiatedAt
            if (timestamp > 0) {
                val formattedDate = Utils.callLogsTimeStamp(timestamp.toLong())
                binding.dateView.setDateText(formattedDate)
                binding.dateView.visibility = View.VISIBLE
            } else {
                binding.dateView.visibility = View.GONE
            }
        }
    }

    private fun bindTrailing() {
        if (customTrailingView != null) return

        callLog?.let { log ->
            val isVideoCall = CallLogsUtils.isVideoCall(log)

            android.util.Log.d(TAG, "bindTrailing: isVideoCall=$isVideoCall")
            android.util.Log.d(TAG, "bindTrailing: videoCallIcon=${style.videoCallIcon}, audioCallIcon=${style.audioCallIcon}")

            // Set call type icon
            binding.ivCallTypeIcon.visibility = View.VISIBLE
            if (isVideoCall) {
                android.util.Log.d(TAG, "bindTrailing: Setting VIDEO call icon")
                if (style.videoCallIcon != 0) {
                    binding.ivCallTypeIcon.setImageDrawable(
                        androidx.core.content.ContextCompat.getDrawable(context, style.videoCallIcon)
                    )
                } else {
                    android.util.Log.w(TAG, "bindTrailing: videoCallIcon is 0!")
                }
                if (style.videoCallIconTint != 0) {
                    binding.ivCallTypeIcon.setColorFilter(style.videoCallIconTint)
                }
                binding.ivCallTypeIcon.contentDescription = context.getString(R.string.cometchat_video_call)
            } else {
                android.util.Log.d(TAG, "bindTrailing: Setting AUDIO call icon")
                if (style.audioCallIcon != 0) {
                    binding.ivCallTypeIcon.setImageDrawable(
                        androidx.core.content.ContextCompat.getDrawable(context, style.audioCallIcon)
                    )
                } else {
                    android.util.Log.w(TAG, "bindTrailing: audioCallIcon is 0!")
                }
                if (style.audioCallIconTint != 0) {
                    binding.ivCallTypeIcon.setColorFilter(style.audioCallIconTint)
                }
                binding.ivCallTypeIcon.contentDescription = context.getString(R.string.cometchat_audio_call)
            }
        }
    }
}
