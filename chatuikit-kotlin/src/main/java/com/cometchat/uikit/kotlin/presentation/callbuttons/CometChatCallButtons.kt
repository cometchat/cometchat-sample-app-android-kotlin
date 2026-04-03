package com.cometchat.uikit.kotlin.presentation.callbuttons

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.factory.CometChatCallButtonsViewModelFactory
import com.cometchat.uikit.core.state.CallButtonsEvent
import com.cometchat.uikit.core.viewmodel.CometChatCallButtonsViewModel
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.databinding.CometchatCallButtonsBinding
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * CometChatCallButtons is a custom view that displays voice and video call buttons
 * for initiating calls with users or groups.
 *
 * This component supports:
 * - 1-to-1 (User) calls via CometChat.initiateCall
 * - Conference (Group) calls via CometChatUIKit.sendCustomMessage
 * - Active call detection to prevent concurrent calls
 * - Full customization of button appearance and behavior
 * - Custom click handlers for advanced use cases
 *
 * Usage in XML:
 * ```xml
 * <com.cometchat.uikit.kotlin.presentation.callbuttons.CometChatCallButtons
 *     android:id="@+id/call_buttons"
 *     android:layout_width="wrap_content"
 *     android:layout_height="wrap_content" />
 * ```
 *
 * Usage in Kotlin:
 * ```kotlin
 * val callButtons = CometChatCallButtons(context)
 * callButtons.setUser(user)
 * ```
 */
@Suppress("unused")
class CometChatCallButtons @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.cometchatCallButtonsStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = CometChatCallButtons::class.java.simpleName
    }

    private lateinit var binding: CometchatCallButtonsBinding
    private var viewModel: CometChatCallButtonsViewModel? = null
    private var coroutineScope: CoroutineScope? = null

    // Entity state
    private var user: User? = null
    private var group: Group? = null

    // Callbacks
    private var onVoiceCallClick: ((User?, Group?) -> Unit)? = null
    private var onVideoCallClick: ((User?, Group?) -> Unit)? = null
    private var onCallInitiated: ((Call) -> Unit)? = null
    private var onStartDirectCall: ((BaseMessage) -> Unit)? = null
    private var onError: ((CometChatException) -> Unit)? = null

    // Icon properties
    private var voiceCallIcon: Drawable? = null
    private var videoCallIcon: Drawable? = null
    private var voiceCallIconTint: Int = 0
    private var videoCallIconTint: Int = 0
    private var voiceCallIconSize: Int = 0
    private var videoCallIconSize: Int = 0

    // Text properties
    private var voiceCallText: String = ""
    private var videoCallText: String = ""
    private var voiceCallTextColor: Int = 0
    private var videoCallTextColor: Int = 0
    private var voiceCallTextAppearance: Int = 0
    private var videoCallTextAppearance: Int = 0

    // Background properties
    private var voiceCallBackgroundColor: Int = 0
    private var videoCallBackgroundColor: Int = 0
    private var voiceCallCornerRadius: Int = 0
    private var videoCallCornerRadius: Int = 0

    // Stroke properties
    private var voiceCallStrokeWidth: Int = 0
    private var videoCallStrokeWidth: Int = 0
    private var voiceCallStrokeColor: Int = 0
    private var videoCallStrokeColor: Int = 0

    // Padding properties
    private var voiceCallButtonPadding: Int = 0
    private var videoCallButtonPadding: Int = 0

    // Layout
    private var marginBetweenButtons: Int = 0

    init {
        if (!isInEditMode) {
            inflateAndInitializeView(attrs, defStyleAttr)
        }
    }

    private fun inflateAndInitializeView(attrs: AttributeSet?, defStyleAttr: Int) {
        binding = CometchatCallButtonsBinding.inflate(LayoutInflater.from(context), this, true)
        Utils.initMaterialCard(this)
        initializeContainers()
        setupClickListeners()
        applyStyleAttributes(attrs, defStyleAttr)
    }

    private fun initializeContainers() {
        // Initialize voice call container with transparent background and no elevation
        Utils.initMaterialCard(binding.voiceCallContainer)
        binding.voiceCallContainer.cardElevation = 0f
        binding.voiceCallContainer.setCardBackgroundColor(android.graphics.Color.TRANSPARENT)

        // Initialize video call container with transparent background and no elevation
        Utils.initMaterialCard(binding.videoCallContainer)
        binding.videoCallContainer.cardElevation = 0f
        binding.videoCallContainer.setCardBackgroundColor(android.graphics.Color.TRANSPARENT)
    }

    private fun setupClickListeners() {
        // Click listeners on container views (not icons) for better touch target
        binding.voiceCallContainer.setOnClickListener {
            if (onVoiceCallClick != null) {
                onVoiceCallClick?.invoke(user, group)
            } else {
                viewModel?.initiateCall(CometChatConstants.CALL_TYPE_AUDIO)
            }
        }

        binding.videoCallContainer.setOnClickListener {
            if (onVideoCallClick != null) {
                onVideoCallClick?.invoke(user, group)
            } else {
                viewModel?.initiateCall(CometChatConstants.CALL_TYPE_VIDEO)
            }
        }
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setupViewModel()
        startCollectingEvents()
    }

    override fun onDetachedFromWindow() {
        coroutineScope?.cancel()
        coroutineScope = null
        super.onDetachedFromWindow()
    }

    private fun setupViewModel() {
        if (viewModel == null) {
            val factory = CometChatCallButtonsViewModelFactory()
            viewModel = factory.create(CometChatCallButtonsViewModel::class.java)
        }
        user?.let { viewModel?.setUser(it) }
        group?.let { viewModel?.setGroup(it) }
    }

    private fun startCollectingEvents() {
        coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

        coroutineScope?.launch {
            viewModel?.events?.collect { event ->
                when (event) {
                    is CallButtonsEvent.CallInitiated -> {
                        if (onCallInitiated != null) {
                            onCallInitiated?.invoke(event.call)
                        } else {
                            com.cometchat.uikit.kotlin.calls.CometChatCallActivity.launchOutgoingCallScreen(
                                context,
                                event.call,
                                null
                            )
                        }
                    }
                    is CallButtonsEvent.StartDirectCall -> {
                        if (onStartDirectCall != null) {
                            onStartDirectCall?.invoke(event.message)
                        } else {
                            com.cometchat.uikit.kotlin.calls.CometChatCallActivity.launchConferenceCallScreen(
                                context,
                                event.message,
                                null
                            )
                        }
                    }
                    is CallButtonsEvent.CallRejected -> {
                        // Call rejection handled internally
                    }
                }
            }
        }

        coroutineScope?.launch {
            viewModel?.errorEvent?.collect { exception ->
                onError?.invoke(exception)
            }
        }
    }

    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        var typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatCallButtons, defStyleAttr, 0
        )
        val styleResId = typedArray.getResourceId(
            R.styleable.CometChatCallButtons_cometchatCallButtonsStyle, 0
        )
        typedArray.recycle()

        typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatCallButtons, defStyleAttr, styleResId
        )

        try {
            val defaultIconTint = CometChatTheme.getIconTintPrimary(context)
            val defaultTextColor = CometChatTheme.getTextColorPrimary(context)

            // Voice call icon properties
            voiceCallIcon = typedArray.getDrawable(R.styleable.CometChatCallButtons_cometchatCallButtonsVoiceCallIcon)
                ?: ContextCompat.getDrawable(context, R.drawable.cometchat_ic_call_voice)
            voiceCallIconTint = typedArray.getColor(
                R.styleable.CometChatCallButtons_cometchatCallButtonsVoiceCallIconTint, defaultIconTint
            )
            voiceCallIconSize = typedArray.getDimensionPixelSize(
                R.styleable.CometChatCallButtons_cometchatCallButtonsVoiceCallIconSize,
                resources.getDimensionPixelSize(R.dimen.cometchat_24dp)
            )

            // Video call icon properties
            videoCallIcon = typedArray.getDrawable(R.styleable.CometChatCallButtons_cometchatCallButtonsVideoCallIcon)
                ?: ContextCompat.getDrawable(context, R.drawable.cometchat_ic_call_video)
            videoCallIconTint = typedArray.getColor(
                R.styleable.CometChatCallButtons_cometchatCallButtonsVideoCallIconTint, defaultIconTint
            )
            videoCallIconSize = typedArray.getDimensionPixelSize(
                R.styleable.CometChatCallButtons_cometchatCallButtonsVideoCallIconSize,
                resources.getDimensionPixelSize(R.dimen.cometchat_24dp)
            )

            // Text color properties
            voiceCallTextColor = typedArray.getColor(
                R.styleable.CometChatCallButtons_cometchatCallButtonsVoiceCallTextColor, defaultTextColor
            )
            videoCallTextColor = typedArray.getColor(
                R.styleable.CometChatCallButtons_cometchatCallButtonsVideoCallTextColor, defaultTextColor
            )

            // Text appearance properties
            voiceCallTextAppearance = typedArray.getResourceId(
                R.styleable.CometChatCallButtons_cometchatCallButtonsVoiceCallTextAppearance, 0
            )
            videoCallTextAppearance = typedArray.getResourceId(
                R.styleable.CometChatCallButtons_cometchatCallButtonsVideoCallTextAppearance, 0
            )

            // Background color properties
            voiceCallBackgroundColor = typedArray.getColor(
                R.styleable.CometChatCallButtons_cometchatCallButtonsVoiceCallBackgroundColor, 0
            )
            videoCallBackgroundColor = typedArray.getColor(
                R.styleable.CometChatCallButtons_cometchatCallButtonsVideoCallBackgroundColor, 0
            )

            // Corner radius properties
            voiceCallCornerRadius = typedArray.getDimensionPixelSize(
                R.styleable.CometChatCallButtons_cometchatCallButtonsVoiceCallCornerRadius, 0
            )
            videoCallCornerRadius = typedArray.getDimensionPixelSize(
                R.styleable.CometChatCallButtons_cometchatCallButtonsVideoCallCornerRadius, 0
            )

            // Stroke properties
            voiceCallStrokeWidth = typedArray.getDimensionPixelSize(
                R.styleable.CometChatCallButtons_cometchatCallButtonsVoiceCallStrokeWidth, 0
            )
            videoCallStrokeWidth = typedArray.getDimensionPixelSize(
                R.styleable.CometChatCallButtons_cometchatCallButtonsVideoCallStrokeWidth, 0
            )
            voiceCallStrokeColor = typedArray.getColor(
                R.styleable.CometChatCallButtons_cometchatCallButtonsVoiceCallStrokeColor, 0
            )
            videoCallStrokeColor = typedArray.getColor(
                R.styleable.CometChatCallButtons_cometchatCallButtonsVideoCallStrokeColor, 0
            )

            // Padding properties
            voiceCallButtonPadding = typedArray.getDimensionPixelSize(
                R.styleable.CometChatCallButtons_cometchatCallButtonsVoiceCallButtonPadding, 0
            )
            videoCallButtonPadding = typedArray.getDimensionPixelSize(
                R.styleable.CometChatCallButtons_cometchatCallButtonsVideoCallButtonPadding, 0
            )

            // Layout
            marginBetweenButtons = typedArray.getDimensionPixelSize(
                R.styleable.CometChatCallButtons_cometchatCallButtonsMarginBetween,
                resources.getDimensionPixelSize(R.dimen.cometchat_16dp)
            )
        } finally {
            typedArray.recycle()
        }

        applyStyle()
    }


    private fun applyStyle() {
        // Apply voice call icon style
        voiceCallIcon?.let { binding.voiceCallIcon.setImageDrawable(it) }
        if (voiceCallIconTint != 0) {
            binding.voiceCallIcon.imageTintList = ColorStateList.valueOf(voiceCallIconTint)
        }
        if (voiceCallIconSize > 0) {
            binding.voiceCallIcon.layoutParams.width = voiceCallIconSize
            binding.voiceCallIcon.layoutParams.height = voiceCallIconSize
            binding.voiceCallIcon.requestLayout()
        }

        // Apply voice call text style
        if (voiceCallTextColor != 0) {
            binding.voiceCallText.setTextColor(voiceCallTextColor)
        }
        if (voiceCallTextAppearance != 0) {
            binding.voiceCallText.setTextAppearance(voiceCallTextAppearance)
        }

        // Apply voice call container style
        if (voiceCallBackgroundColor != 0) {
            binding.voiceCallContainer.setCardBackgroundColor(voiceCallBackgroundColor)
        }
        binding.voiceCallContainer.radius = voiceCallCornerRadius.toFloat()
        binding.voiceCallContainer.strokeWidth = voiceCallStrokeWidth
        if (voiceCallStrokeColor != 0) {
            binding.voiceCallContainer.strokeColor = voiceCallStrokeColor
        }
        if (voiceCallButtonPadding > 0) {
            // Apply padding as margins on inner container (matching Java behavior)
            val voiceParams = binding.voiceCallButtonContainer.layoutParams as? MarginLayoutParams
            voiceParams?.let {
                it.setMargins(voiceCallButtonPadding, voiceCallButtonPadding, voiceCallButtonPadding, voiceCallButtonPadding)
                binding.voiceCallButtonContainer.layoutParams = it
            }
        }

        // Apply video call icon style
        videoCallIcon?.let { binding.videoCallIcon.setImageDrawable(it) }
        if (videoCallIconTint != 0) {
            binding.videoCallIcon.imageTintList = ColorStateList.valueOf(videoCallIconTint)
        }
        if (videoCallIconSize > 0) {
            binding.videoCallIcon.layoutParams.width = videoCallIconSize
            binding.videoCallIcon.layoutParams.height = videoCallIconSize
            binding.videoCallIcon.requestLayout()
        }

        // Apply video call text style
        if (videoCallTextColor != 0) {
            binding.videoCallText.setTextColor(videoCallTextColor)
        }
        if (videoCallTextAppearance != 0) {
            binding.videoCallText.setTextAppearance(videoCallTextAppearance)
        }

        // Apply video call container style
        if (videoCallBackgroundColor != 0) {
            binding.videoCallContainer.setCardBackgroundColor(videoCallBackgroundColor)
        }
        binding.videoCallContainer.radius = videoCallCornerRadius.toFloat()
        binding.videoCallContainer.strokeWidth = videoCallStrokeWidth
        if (videoCallStrokeColor != 0) {
            binding.videoCallContainer.strokeColor = videoCallStrokeColor
        }
        if (videoCallButtonPadding > 0) {
            // Apply padding as margins on inner container (matching Java behavior)
            val videoParams = binding.videoCallButtonContainer.layoutParams as? MarginLayoutParams
            videoParams?.let {
                it.setMargins(videoCallButtonPadding, videoCallButtonPadding, videoCallButtonPadding, videoCallButtonPadding)
                binding.videoCallButtonContainer.layoutParams = it
            }
        }

        // Apply margin between buttons
        if (marginBetweenButtons > 0) {
            val params = binding.space.layoutParams as LinearLayout.LayoutParams
            params.width = marginBetweenButtons
            binding.space.layoutParams = params
        }
    }

    // ========================================
    // Public API Methods - Entity
    // ========================================

    /**
     * Sets the user for 1-to-1 calls.
     * @param user The User to call
     */
    fun setUser(user: User) {
        this.user = user
        this.group = null
        viewModel?.setUser(user)
    }

    /**
     * Sets the group for conference calls.
     * @param group The Group to start a call with
     */
    fun setGroup(group: Group) {
        this.group = group
        this.user = null
        viewModel?.setGroup(group)
    }

    /**
     * Gets the currently set user.
     * @return The User or null if not set
     */
    fun getUser(): User? = user

    /**
     * Gets the currently set group.
     * @return The Group or null if not set
     */
    fun getGroup(): Group? = group

    // ========================================
    // Public API Methods - Callbacks
    // ========================================

    /**
     * Sets a custom click handler for the voice call button.
     * If set, overrides the default call initiation behavior.
     * @param callback The callback to invoke when voice call button is clicked
     */
    fun setOnVoiceCallClick(callback: ((User?, Group?) -> Unit)?) {
        this.onVoiceCallClick = callback
    }

    /**
     * Sets a custom click handler for the video call button.
     * If set, overrides the default call initiation behavior.
     * @param callback The callback to invoke when video call button is clicked
     */
    fun setOnVideoCallClick(callback: ((User?, Group?) -> Unit)?) {
        this.onVideoCallClick = callback
    }

    /**
     * Sets a callback for when a user call is successfully initiated.
     * @param callback The callback to invoke with the initiated Call
     */
    fun setOnCallInitiated(callback: ((Call) -> Unit)?) {
        this.onCallInitiated = callback
    }

    /**
     * Sets a callback for when a group call message is successfully sent.
     * @param callback The callback to invoke with the sent BaseMessage
     */
    fun setOnStartDirectCall(callback: ((BaseMessage) -> Unit)?) {
        this.onStartDirectCall = callback
    }

    /**
     * Sets a callback for when an error occurs.
     * @param callback The callback to invoke with the CometChatException
     */
    fun setOnError(callback: ((CometChatException) -> Unit)?) {
        this.onError = callback
    }


    // ========================================
    // Public API Methods - Button Text
    // ========================================

    /**
     * Sets the text for the voice call button.
     * @param text The text to display. If non-empty, makes text visible. If empty/null, hides the text.
     */
    fun setVoiceButtonText(text: String?) {
        if (!text.isNullOrEmpty()) {
            voiceCallText = text
            binding.voiceCallText.text = text
            binding.voiceCallText.visibility = View.VISIBLE
        } else {
            voiceCallText = ""
            binding.voiceCallText.text = ""
            binding.voiceCallText.visibility = View.GONE
        }
    }

    /**
     * Sets the text for the video call button.
     * @param text The text to display. If non-empty, makes text visible. If empty/null, hides the text.
     */
    fun setVideoButtonText(text: String?) {
        if (!text.isNullOrEmpty()) {
            videoCallText = text
            binding.videoCallText.text = text
            binding.videoCallText.visibility = View.VISIBLE
        } else {
            videoCallText = ""
            binding.videoCallText.text = ""
            binding.videoCallText.visibility = View.GONE
        }
    }

    /**
     * Sets the visibility of text labels on both buttons.
     * @param visibility View.VISIBLE, View.INVISIBLE, or View.GONE
     */
    fun setButtonTextVisibility(visibility: Int) {
        binding.voiceCallText.visibility = if (visibility == View.VISIBLE) View.VISIBLE else View.GONE
        binding.videoCallText.visibility = if (visibility == View.VISIBLE) View.VISIBLE else View.GONE
    }

    /**
     * Sets the visibility of icons on both buttons.
     * @param visibility View.VISIBLE, View.INVISIBLE, or View.GONE
     */
    fun setButtonIconVisibility(visibility: Int) {
        binding.voiceCallIcon.visibility = if (visibility == View.VISIBLE) View.VISIBLE else View.GONE
        binding.videoCallIcon.visibility = if (visibility == View.VISIBLE) View.VISIBLE else View.GONE
    }

    /**
     * Hides or shows the circular background behind both call button icons.
     * When visible, displays a FloatingActionButton as a circular background.
     * @param hide True to hide the background, false to show it
     */
    fun hideButtonBackground(hide: Boolean) {
        binding.voiceCallBackground.visibility = if (hide) View.GONE else View.VISIBLE
        binding.videoCallBackground.visibility = if (hide) View.GONE else View.VISIBLE
    }

    /**
     * Hides or shows the circular background behind the voice call button icon.
     * @param hide True to hide the background, false to show it
     */
    fun hideVoiceCallButtonBackground(hide: Boolean) {
        binding.voiceCallBackground.visibility = if (hide) View.GONE else View.VISIBLE
    }

    /**
     * Hides or shows the circular background behind the video call button icon.
     * @param hide True to hide the background, false to show it
     */
    fun hideVideoCallButtonBackground(hide: Boolean) {
        binding.videoCallBackground.visibility = if (hide) View.GONE else View.VISIBLE
    }

    /**
     * Sets the background color for the voice call button's circular FAB background.
     * @param color The color value (not resource ID)
     */
    fun setVoiceCallButtonBackgroundTint(@ColorInt color: Int) {
        if (color != 0) {
            binding.voiceCallBackground.backgroundTintList = ColorStateList.valueOf(color)
        }
    }

    /**
     * Sets the background color for the video call button's circular FAB background.
     * @param color The color value (not resource ID)
     */
    fun setVideoCallButtonBackgroundTint(@ColorInt color: Int) {
        if (color != 0) {
            binding.videoCallBackground.backgroundTintList = ColorStateList.valueOf(color)
        }
    }

    // ========================================
    // Public API Methods - Text Styling
    // ========================================

    /**
     * Sets the text color for the voice call button label.
     * @param color The color value (not resource ID)
     */
    fun setVoiceCallTextColor(@ColorInt color: Int) {
        voiceCallTextColor = color
        binding.voiceCallText.setTextColor(color)
    }

    /**
     * Sets the text color for the video call button label.
     * @param color The color value (not resource ID)
     */
    fun setVideoCallTextColor(@ColorInt color: Int) {
        videoCallTextColor = color
        binding.videoCallText.setTextColor(color)
    }

    /**
     * Sets the text appearance for the voice call button label.
     * @param appearance The style resource ID
     */
    fun setVoiceCallTextAppearance(@StyleRes appearance: Int) {
        voiceCallTextAppearance = appearance
        if (appearance != 0) {
            binding.voiceCallText.setTextAppearance(appearance)
        }
    }

    /**
     * Sets the text appearance for the video call button label.
     * @param appearance The style resource ID
     */
    fun setVideoCallTextAppearance(@StyleRes appearance: Int) {
        videoCallTextAppearance = appearance
        if (appearance != 0) {
            binding.videoCallText.setTextAppearance(appearance)
        }
    }

    // ========================================
    // Public API Methods - Background Styling
    // ========================================

    /**
     * Sets the background color for the voice call button container.
     * @param color The color value (not resource ID)
     */
    fun setVoiceCallBackgroundColor(@ColorInt color: Int) {
        voiceCallBackgroundColor = color
        binding.voiceCallContainer.setCardBackgroundColor(color)
    }

    /**
     * Sets the background color for the video call button container.
     * @param color The color value (not resource ID)
     */
    fun setVideoCallBackgroundColor(@ColorInt color: Int) {
        videoCallBackgroundColor = color
        binding.videoCallContainer.setCardBackgroundColor(color)
    }

    /**
     * Sets the corner radius for the voice call button container.
     * @param radius The radius in pixels
     */
    fun setVoiceCallCornerRadius(@Dimension radius: Int) {
        voiceCallCornerRadius = radius
        binding.voiceCallContainer.radius = radius.toFloat()
    }

    /**
     * Sets the corner radius for the video call button container.
     * @param radius The radius in pixels
     */
    fun setVideoCallCornerRadius(@Dimension radius: Int) {
        videoCallCornerRadius = radius
        binding.videoCallContainer.radius = radius.toFloat()
    }

    // ========================================
    // Public API Methods - Stroke Styling
    // ========================================

    /**
     * Sets the stroke width for the voice call button container.
     * @param width The stroke width in pixels
     */
    fun setVoiceCallStrokeWidth(@Dimension width: Int) {
        voiceCallStrokeWidth = width
        binding.voiceCallContainer.strokeWidth = width
    }

    /**
     * Sets the stroke width for the video call button container.
     * @param width The stroke width in pixels
     */
    fun setVideoCallStrokeWidth(@Dimension width: Int) {
        videoCallStrokeWidth = width
        binding.videoCallContainer.strokeWidth = width
    }

    /**
     * Sets the stroke color for the voice call button container.
     * @param color The color value (not resource ID)
     */
    fun setVoiceCallStrokeColor(@ColorInt color: Int) {
        voiceCallStrokeColor = color
        binding.voiceCallContainer.strokeColor = color
    }

    /**
     * Sets the stroke color for the video call button container.
     * @param color The color value (not resource ID)
     */
    fun setVideoCallStrokeColor(@ColorInt color: Int) {
        videoCallStrokeColor = color
        binding.videoCallContainer.strokeColor = color
    }

    // ========================================
    // Public API Methods - Padding
    // ========================================

    /**
     * Sets the padding (as margins) for the voice call button container.
     * This matches Java behavior where padding is applied as margins on the inner container.
     * @param padding The padding in pixels (applied to all sides as margins)
     */
    fun setVoiceCallButtonPadding(@Dimension padding: Int) {
        voiceCallButtonPadding = padding
        val params = binding.voiceCallButtonContainer.layoutParams as? MarginLayoutParams
        params?.let {
            it.setMargins(padding, padding, padding, padding)
            binding.voiceCallButtonContainer.layoutParams = it
        }
    }

    /**
     * Sets the padding (as margins) for the video call button container.
     * This matches Java behavior where padding is applied as margins on the inner container.
     * @param padding The padding in pixels (applied to all sides as margins)
     */
    fun setVideoCallButtonPadding(@Dimension padding: Int) {
        videoCallButtonPadding = padding
        val params = binding.videoCallButtonContainer.layoutParams as? MarginLayoutParams
        params?.let {
            it.setMargins(padding, padding, padding, padding)
            binding.videoCallButtonContainer.layoutParams = it
        }
    }


    // ========================================
    // Public API Methods - Visibility
    // ========================================

    /**
     * Sets the visibility of the voice call button.
     * @param visibility View.VISIBLE, View.INVISIBLE, or View.GONE
     */
    fun setVoiceCallButtonVisibility(visibility: Int) {
        binding.voiceCallContainer.visibility = visibility
    }

    /**
     * Gets the visibility of the voice call button.
     * @return The visibility value
     */
    fun getVoiceCallButtonVisibility(): Int = binding.voiceCallContainer.visibility

    /**
     * Sets the visibility of the video call button.
     * @param visibility View.VISIBLE, View.INVISIBLE, or View.GONE
     */
    fun setVideoCallButtonVisibility(visibility: Int) {
        binding.videoCallContainer.visibility = visibility
    }

    /**
     * Gets the visibility of the video call button.
     * @return The visibility value
     */
    fun getVideoCallButtonVisibility(): Int = binding.videoCallContainer.visibility

    // ========================================
    // Public API Methods - Icon Styling
    // ========================================

    /**
     * Sets the margin between the voice and video call buttons.
     * @param margin The margin in pixels
     */
    fun setMarginBetweenButtons(@Dimension margin: Int) {
        marginBetweenButtons = margin
        val params = binding.space.layoutParams as LinearLayout.LayoutParams
        params.width = margin
        binding.space.layoutParams = params
    }

    /**
     * Gets the margin between buttons.
     * @return The margin in pixels
     */
    fun getMarginBetweenButtons(): Int = marginBetweenButtons

    /**
     * Sets the voice call button icon.
     * @param icon The drawable to use as the icon
     */
    fun setVoiceCallIcon(icon: Drawable?) {
        voiceCallIcon = icon
        icon?.let { binding.voiceCallIcon.setImageDrawable(it) }
    }

    /**
     * Gets the voice call button icon.
     * @return The icon drawable
     */
    fun getVoiceCallIcon(): Drawable? = voiceCallIcon

    /**
     * Sets the video call button icon.
     * @param icon The drawable to use as the icon
     */
    fun setVideoCallIcon(icon: Drawable?) {
        videoCallIcon = icon
        icon?.let { binding.videoCallIcon.setImageDrawable(it) }
    }

    /**
     * Gets the video call button icon.
     * @return The icon drawable
     */
    fun getVideoCallIcon(): Drawable? = videoCallIcon

    /**
     * Sets the voice call button icon tint.
     * @param color The tint color
     */
    fun setVoiceCallIconTint(@ColorInt color: Int) {
        voiceCallIconTint = color
        binding.voiceCallIcon.imageTintList = ColorStateList.valueOf(color)
    }

    /**
     * Gets the voice call button icon tint.
     * @return The tint color
     */
    fun getVoiceCallIconTint(): Int = voiceCallIconTint

    /**
     * Sets the video call button icon tint.
     * @param color The tint color
     */
    fun setVideoCallIconTint(@ColorInt color: Int) {
        videoCallIconTint = color
        binding.videoCallIcon.imageTintList = ColorStateList.valueOf(color)
    }

    /**
     * Gets the video call button icon tint.
     * @return The tint color
     */
    fun getVideoCallIconTint(): Int = videoCallIconTint

    /**
     * Sets the voice call button icon size.
     * @param size The size in pixels
     */
    fun setVoiceCallIconSize(@Dimension size: Int) {
        voiceCallIconSize = size
        binding.voiceCallIcon.layoutParams.width = size
        binding.voiceCallIcon.layoutParams.height = size
        binding.voiceCallIcon.requestLayout()
    }

    /**
     * Gets the voice call button icon size.
     * @return The size in pixels
     */
    fun getVoiceCallIconSize(): Int = voiceCallIconSize

    /**
     * Sets the video call button icon size.
     * @param size The size in pixels
     */
    fun setVideoCallIconSize(@Dimension size: Int) {
        videoCallIconSize = size
        binding.videoCallIcon.layoutParams.width = size
        binding.videoCallIcon.layoutParams.height = size
        binding.videoCallIcon.requestLayout()
    }

    /**
     * Gets the video call button icon size.
     * @return The size in pixels
     */
    fun getVideoCallIconSize(): Int = videoCallIconSize

    // ========================================
    // Public API Methods - Style Resource
    // ========================================

    /**
     * Applies a style resource to the call buttons.
     * @param styleRes The style resource ID
     */
    fun setStyle(@StyleRes styleRes: Int) {
        if (styleRes != 0) {
            val typedArray = context.theme.obtainStyledAttributes(styleRes, R.styleable.CometChatCallButtons)
            try {
                // Icon properties
                voiceCallIcon = typedArray.getDrawable(R.styleable.CometChatCallButtons_cometchatCallButtonsVoiceCallIcon)
                    ?: voiceCallIcon
                voiceCallIconTint = typedArray.getColor(
                    R.styleable.CometChatCallButtons_cometchatCallButtonsVoiceCallIconTint, voiceCallIconTint
                )
                voiceCallIconSize = typedArray.getDimensionPixelSize(
                    R.styleable.CometChatCallButtons_cometchatCallButtonsVoiceCallIconSize, voiceCallIconSize
                )
                videoCallIcon = typedArray.getDrawable(R.styleable.CometChatCallButtons_cometchatCallButtonsVideoCallIcon)
                    ?: videoCallIcon
                videoCallIconTint = typedArray.getColor(
                    R.styleable.CometChatCallButtons_cometchatCallButtonsVideoCallIconTint, videoCallIconTint
                )
                videoCallIconSize = typedArray.getDimensionPixelSize(
                    R.styleable.CometChatCallButtons_cometchatCallButtonsVideoCallIconSize, videoCallIconSize
                )

                // Text color properties
                voiceCallTextColor = typedArray.getColor(
                    R.styleable.CometChatCallButtons_cometchatCallButtonsVoiceCallTextColor, voiceCallTextColor
                )
                videoCallTextColor = typedArray.getColor(
                    R.styleable.CometChatCallButtons_cometchatCallButtonsVideoCallTextColor, videoCallTextColor
                )

                // Text appearance properties
                voiceCallTextAppearance = typedArray.getResourceId(
                    R.styleable.CometChatCallButtons_cometchatCallButtonsVoiceCallTextAppearance, voiceCallTextAppearance
                )
                videoCallTextAppearance = typedArray.getResourceId(
                    R.styleable.CometChatCallButtons_cometchatCallButtonsVideoCallTextAppearance, videoCallTextAppearance
                )

                // Background color properties
                voiceCallBackgroundColor = typedArray.getColor(
                    R.styleable.CometChatCallButtons_cometchatCallButtonsVoiceCallBackgroundColor, voiceCallBackgroundColor
                )
                videoCallBackgroundColor = typedArray.getColor(
                    R.styleable.CometChatCallButtons_cometchatCallButtonsVideoCallBackgroundColor, videoCallBackgroundColor
                )

                // Corner radius properties
                voiceCallCornerRadius = typedArray.getDimensionPixelSize(
                    R.styleable.CometChatCallButtons_cometchatCallButtonsVoiceCallCornerRadius, voiceCallCornerRadius
                )
                videoCallCornerRadius = typedArray.getDimensionPixelSize(
                    R.styleable.CometChatCallButtons_cometchatCallButtonsVideoCallCornerRadius, videoCallCornerRadius
                )

                // Stroke properties
                voiceCallStrokeWidth = typedArray.getDimensionPixelSize(
                    R.styleable.CometChatCallButtons_cometchatCallButtonsVoiceCallStrokeWidth, voiceCallStrokeWidth
                )
                videoCallStrokeWidth = typedArray.getDimensionPixelSize(
                    R.styleable.CometChatCallButtons_cometchatCallButtonsVideoCallStrokeWidth, videoCallStrokeWidth
                )
                voiceCallStrokeColor = typedArray.getColor(
                    R.styleable.CometChatCallButtons_cometchatCallButtonsVoiceCallStrokeColor, voiceCallStrokeColor
                )
                videoCallStrokeColor = typedArray.getColor(
                    R.styleable.CometChatCallButtons_cometchatCallButtonsVideoCallStrokeColor, videoCallStrokeColor
                )

                // Padding properties
                voiceCallButtonPadding = typedArray.getDimensionPixelSize(
                    R.styleable.CometChatCallButtons_cometchatCallButtonsVoiceCallButtonPadding, voiceCallButtonPadding
                )
                videoCallButtonPadding = typedArray.getDimensionPixelSize(
                    R.styleable.CometChatCallButtons_cometchatCallButtonsVideoCallButtonPadding, videoCallButtonPadding
                )

                // Layout
                marginBetweenButtons = typedArray.getDimensionPixelSize(
                    R.styleable.CometChatCallButtons_cometchatCallButtonsMarginBetween, marginBetweenButtons
                )
            } finally {
                typedArray.recycle()
            }
            applyStyle()
        }
    }
}
