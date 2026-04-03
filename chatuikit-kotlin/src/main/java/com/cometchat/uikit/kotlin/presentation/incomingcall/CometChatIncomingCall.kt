package com.cometchat.uikit.kotlin.presentation.incomingcall

import android.app.Activity
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.RawRes
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.cometchat.calls.core.CometChatCalls
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants.CallWorkFlow
import com.cometchat.uikit.core.resources.soundmanager.CometChatSoundManager
import com.cometchat.uikit.core.resources.soundmanager.Sound
import com.cometchat.uikit.core.viewmodel.CometChatIncomingCallViewModel
import com.cometchat.uikit.kotlin.presentation.ongoingcall.ui.CometChatOngoingCallActivity
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.incomingcall.style.CometChatIncomingCallStyle
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar.CometChatAvatar
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar.CometChatAvatarStyle
import com.cometchat.uikit.kotlin.shared.interfaces.OnClick
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * CometChatIncomingCall is a custom view that displays incoming call UI.
 * 
 * This component displays:
 * - Caller's name and avatar
 * - Call type (audio/video) with appropriate icon
 * - Accept and Decline buttons
 * - Sound playback for incoming calls
 * 
 * The UI is identical to the Java implementation in chatuikit.
 * 
 * **Validates: Requirements 7a.1-7a.20**
 * 
 * Usage in XML:
 * ```xml
 * <com.cometchat.uikit.kotlin.presentation.incomingcall.CometChatIncomingCall
 *     android:id="@+id/incoming_call"
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content" />
 * ```
 * 
 * Usage in Kotlin:
 * ```kotlin
 * val incomingCall = CometChatIncomingCall(context)
 * incomingCall.setCall(call)
 * incomingCall.setOnAcceptClickListener { /* handle accept */ }
 * incomingCall.setOnRejectClickListener { /* handle reject */ }
 * ```
 */
class CometChatIncomingCall @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.cometchatIncomingCallStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "CometChatIncomingCall"
    }

    // ==================== Views ====================
    private lateinit var innerCard: MaterialCardView
    private lateinit var mainContainer: LinearLayout
    private lateinit var itemViewContainer: LinearLayout
    private lateinit var leadingViewContainer: LinearLayout
    private lateinit var contentContainer: LinearLayout
    private lateinit var titleContainer: LinearLayout
    private lateinit var subtitleContainer: LinearLayout
    private lateinit var trailingViewContainer: LinearLayout
    private lateinit var callerNameTextView: TextView
    private lateinit var callTypeTextView: TextView
    private lateinit var callTypeIcon: ImageView
    private lateinit var callerAvatar: CometChatAvatar
    private lateinit var buttonContainer: LinearLayout
    private lateinit var declineButton: MaterialButton
    private lateinit var acceptButton: MaterialButton

    // ==================== State ====================
    private var call: Call? = null
    private var viewModel: CometChatIncomingCallViewModel? = null
    private var lifecycleOwner: LifecycleOwner? = null
    private var viewScope: CoroutineScope? = null
    private var isDetachedFromWindow = false
    private var soundManager: CometChatSoundManager
    
    // ==================== Callbacks ====================
    private var onAcceptClick: OnClick? = null
    private var onRejectClick: OnClick? = null
    private var onError: ((CometChatException) -> Unit)? = null

    // ==================== Configuration ====================
    private var disableSoundForCalls = false
    @RawRes private var customSoundForCalls = 0
    private var callSettingsBuilder: Any? = null

    // ==================== Custom Views ====================
    private var customItemView: View? = null
    private var customLeadingView: View? = null
    private var customTitleView: View? = null
    private var customSubtitleView: View? = null
    private var customTrailingView: View? = null

    // ==================== Style ====================
    private var style: CometChatIncomingCallStyle

    // ==================== Observer Jobs ====================
    private var acceptedCallJob: Job? = null
    private var rejectedCallJob: Job? = null
    private var errorJob: Job? = null

    init {
        soundManager = CometChatSoundManager(context)
        style = CometChatIncomingCallStyle.default(context)
        
        if (!isInEditMode) {
            initializeView()
            applyStyleAttributes(attrs, defStyleAttr)
            initViewModel()
            setupClickListeners()
            keepScreenOn()
        }
    }

    // ==================== Initialization ====================

    /**
     * Initializes the view hierarchy programmatically.
     * Creates a layout identical to the Java implementation's XML layout.
     */
    private fun initializeView() {
        Utils.initMaterialCard(this)
        
        // Create inner MaterialCardView
        innerCard = MaterialCardView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = Utils.convertDpToPx(context, 16)
            }
            radius = Utils.convertDpToPx(context, 12f).toFloat()
            strokeWidth = 0
            setCardBackgroundColor(CometChatTheme.getBackgroundColor3(context))
        }
        addView(innerCard)

        // Create main container
        // Java layout uses cometchat_padding_5 = 20dp for padding
        mainContainer = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            val padding = Utils.convertDpToPx(context, 20) // cometchat_padding_5 = 20dp
            setPadding(padding, padding, padding, padding)
        }
        innerCard.addView(mainContainer)

        // Create item view container (horizontal layout for caller info)
        itemViewContainer = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        mainContainer.addView(itemViewContainer)

        // Create leading view container
        leadingViewContainer = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
        }
        itemViewContainer.addView(leadingViewContainer)

        // Create content container (title + subtitle)
        contentContainer = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            orientation = LinearLayout.VERTICAL
        }
        itemViewContainer.addView(contentContainer)

        // Create title container
        titleContainer = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
        }
        contentContainer.addView(titleContainer)

        // Create caller name TextView
        callerNameTextView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.MARQUEE
            setTextColor(CometChatTheme.getTextColorPrimary(context))
            setTextAppearance(CometChatTheme.getTextAppearanceHeading2Bold(context))
            contentDescription = context.getString(R.string.cometchat_caller_name)
        }
        titleContainer.addView(callerNameTextView)

        // Create subtitle container (call type icon + text)
        subtitleContainer = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        contentContainer.addView(subtitleContainer)

        // Create call type icon
        callTypeIcon = ImageView(context).apply {
            val iconSize = Utils.convertDpToPx(context, 16)
            layoutParams = LinearLayout.LayoutParams(iconSize, iconSize).apply {
                gravity = Gravity.CENTER_VERTICAL
            }
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            setColorFilter(CometChatTheme.getIconTintSecondary(context))
        }
        subtitleContainer.addView(callTypeIcon)

        // Create call type TextView
        callTypeTextView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = Utils.convertDpToPx(context, 8) // cometchat_margin_2 = 8dp
            }
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.MARQUEE
            setTextColor(CometChatTheme.getTextColorSecondary(context))
            setTextAppearance(CometChatTheme.getTextAppearanceBodyRegular(context))
            contentDescription = context.getString(R.string.cometchat_call_type)
        }
        subtitleContainer.addView(callTypeTextView)

        // Create trailing view container (avatar)
        trailingViewContainer = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
        }
        itemViewContainer.addView(trailingViewContainer)

        // Create caller avatar
        val avatarSize = Utils.convertDpToPx(context, 48)
        callerAvatar = CometChatAvatar(context).apply {
            layoutParams = LinearLayout.LayoutParams(avatarSize, avatarSize).apply {
                marginStart = Utils.convertDpToPx(context, 32)
            }
            setAvatarStrokeRadius(Utils.convertDpToPx(context, 24f).toFloat())
            contentDescription = context.getString(R.string.cometchat_caller_avatar)
        }
        trailingViewContainer.addView(callerAvatar)

        // Create button container
        buttonContainer = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = Utils.convertDpToPx(context, 12)
            }
            orientation = LinearLayout.HORIZONTAL
        }
        mainContainer.addView(buttonContainer)

        // Create decline button
        // Java layout uses cometchat_margin_2 = 8dp for marginEnd, cometchat_padding_5 = 20dp horizontal, cometchat_padding_2 = 8dp vertical
        declineButton = MaterialButton(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                marginEnd = Utils.convertDpToPx(context, 8) // cometchat_margin_2 = 8dp
            }
            text = context.getString(R.string.cometchat_incoming_call_decline)
            setBackgroundColor(CometChatTheme.getErrorColor(context))
            setTextColor(CometChatTheme.getColorWhite(context))
            cornerRadius = Utils.convertDpToPx(context, 8) // cometchat_radius_2 = 8dp
            val horizontalPadding = Utils.convertDpToPx(context, 20) // cometchat_padding_5 = 20dp
            val verticalPadding = Utils.convertDpToPx(context, 8) // cometchat_padding_2 = 8dp
            setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
            contentDescription = context.getString(R.string.cometchat_decline_call_button)
        }
        buttonContainer.addView(declineButton)

        // Create accept button
        // Java layout uses cometchat_margin_2 = 8dp for marginStart, cometchat_padding_5 = 20dp horizontal, cometchat_padding_2 = 8dp vertical
        acceptButton = MaterialButton(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                marginStart = Utils.convertDpToPx(context, 8) // cometchat_margin_2 = 8dp
            }
            text = context.getString(R.string.cometchat_incoming_call_accept)
            setBackgroundColor(CometChatTheme.getSuccessColor(context))
            setTextColor(CometChatTheme.getColorWhite(context))
            cornerRadius = Utils.convertDpToPx(context, 8) // cometchat_radius_2 = 8dp
            val horizontalPadding = Utils.convertDpToPx(context, 20) // cometchat_padding_5 = 20dp
            val verticalPadding = Utils.convertDpToPx(context, 8) // cometchat_padding_2 = 8dp
            setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
            contentDescription = context.getString(R.string.cometchat_accept_call_button)
        }
        buttonContainer.addView(acceptButton)
    }


    /**
     * Keeps the screen on while the incoming call is displayed.
     */
    private fun keepScreenOn() {
        val activity = Utils.getActivity(context)
        if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    /**
     * Applies style attributes from XML.
     */
    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        var typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatIncomingCall, defStyleAttr, 0
        )
        val styleResId = typedArray.getResourceId(
            R.styleable.CometChatIncomingCall_cometchatIncomingCallStyle, 0
        )
        typedArray.recycle()
        
        typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatIncomingCall, defStyleAttr, styleResId
        )
        style = CometChatIncomingCallStyle.fromTypedArray(context, typedArray)
        applyStyle()
    }

    /**
     * Initializes the ViewModel and sets up lifecycle observation.
     * 
     * Note: Uses NewInstanceFactory to create a new ViewModel instance each time,
     * matching the Java implementation behavior. This ensures that each incoming call
     * UI gets a fresh ViewModel with clean state, preventing issues where old
     * StateFlow values (like acceptedCall) would trigger actions on new calls.
     */
    private fun initViewModel() {
        lifecycleOwner = Utils.getLifecycleOwner(context)
        if (lifecycleOwner == null) return
        
        viewScope = lifecycleOwner?.lifecycleScope
        
        // Create a new ViewModel instance each time (matching Java implementation)
        // This prevents stale StateFlow values from triggering actions on new calls
        viewModel = ViewModelProvider.NewInstanceFactory().create(CometChatIncomingCallViewModel::class.java)
        
        attachObservers()
    }

    /**
     * Sets up click listeners for accept and decline buttons.
     */
    private fun setupClickListeners() {
        acceptButton.setOnClickListener {
            if (onAcceptClick != null) {
                onAcceptClick?.onClick()
            } else {
                viewModel?.acceptCall()
            }
        }

        declineButton.setOnClickListener {
            if (onRejectClick != null) {
                onRejectClick?.onClick()
            } else {
                viewModel?.rejectCall()
            }
        }
    }

    // ==================== Public Methods ====================

    /**
     * Sets the incoming call to display.
     * 
     * **Validates: Requirements 7a.4, 7a.5, 7a.6, 7a.7**
     * 
     * @param call The incoming Call object
     */
    fun setCall(call: Call) {
        this.call = call
        viewModel?.setCall(call)
        updateCallerInfo()
    }

    /**
     * Gets the current call.
     * 
     * @return The current Call or null if not set
     */
    fun getCall(): Call? = call

    /**
     * Sets the style for this component.
     * 
     * **Validates: Requirements 7a.17**
     * 
     * @param style The CometChatIncomingCallStyle to apply
     */
    fun setStyle(style: CometChatIncomingCallStyle) {
        this.style = style
        applyStyle()
    }

    /**
     * Sets the style from a style resource.
     * 
     * @param styleRes The style resource ID
     */
    fun setStyle(@StyleRes styleRes: Int) {
        if (styleRes != 0) {
            val typedArray = context.theme.obtainStyledAttributes(
                styleRes, R.styleable.CometChatIncomingCall
            )
            style = CometChatIncomingCallStyle.fromTypedArray(context, typedArray)
            applyStyle()
        }
    }

    /**
     * Gets the current style.
     * 
     * @return The current CometChatIncomingCallStyle
     */
    fun getStyle(): CometChatIncomingCallStyle = style

    /**
     * Sets the accept button click listener.
     * 
     * **Validates: Requirements 7a.10**
     * 
     * @param listener The click listener or null to use default behavior
     */
    fun setOnAcceptClickListener(listener: OnClick?) {
        this.onAcceptClick = listener
    }

    /**
     * Gets the accept button click listener.
     * 
     * @return The current OnClick listener
     */
    fun getOnAcceptClickListener(): OnClick? = onAcceptClick

    /**
     * Sets the reject button click listener.
     * 
     * **Validates: Requirements 7a.11**
     * 
     * @param listener The click listener or null to use default behavior
     */
    fun setOnRejectClickListener(listener: OnClick?) {
        this.onRejectClick = listener
    }

    /**
     * Gets the reject button click listener.
     * 
     * @return The current OnClick listener
     */
    fun getOnRejectClickListener(): OnClick? = onRejectClick

    /**
     * Sets the error callback.
     * 
     * **Validates: Requirements 7a.12**
     * 
     * @param onError The error callback
     */
    fun setOnError(onError: ((CometChatException) -> Unit)?) {
        this.onError = onError
    }

    /**
     * Gets the error callback.
     * 
     * @return The current error callback
     */
    fun getOnError(): ((CometChatException) -> Unit)? = onError

    /**
     * Disables or enables sound for incoming calls.
     * 
     * **Validates: Requirements 7a.13**
     * 
     * @param disable True to disable sound, false to enable
     */
    fun setDisableSoundForCalls(disable: Boolean) {
        this.disableSoundForCalls = disable
    }

    /**
     * Gets whether sound is disabled for calls.
     * 
     * @return True if sound is disabled
     */
    fun isDisableSoundForCalls(): Boolean = disableSoundForCalls

    /**
     * Sets a custom sound resource for incoming calls.
     * 
     * **Validates: Requirements 7a.14**
     * 
     * @param soundRes The raw resource ID of the custom sound
     */
    fun setCustomSoundForCalls(@RawRes soundRes: Int) {
        this.customSoundForCalls = soundRes
    }

    /**
     * Gets the custom sound resource.
     * 
     * @return The raw resource ID of the custom sound
     */
    fun getCustomSoundForCalls(): Int = customSoundForCalls

    /**
     * Sets the call settings builder for ongoing call configuration.
     * 
     * @param builder The CallSettingsBuilder instance
     */
    fun setCallSettingsBuilder(builder: Any?) {
        this.callSettingsBuilder = builder
    }

    /**
     * Gets the call settings builder.
     * 
     * @return The CallSettingsBuilder instance
     */
    fun getCallSettingsBuilder(): Any? = callSettingsBuilder

    // ==================== Custom View Methods ====================

    /**
     * Sets a custom item view that replaces the entire content.
     * 
     * **Validates: Requirements 7a.18**
     * 
     * @param view The custom view or null to use default
     */
    fun setItemView(view: View?) {
        this.customItemView = view
        Utils.handleView(itemViewContainer, view, false)
    }

    /**
     * Gets the custom item view.
     * 
     * @return The custom item view
     */
    fun getItemView(): View? = customItemView

    /**
     * Sets a custom leading view.
     * 
     * **Validates: Requirements 7a.18**
     * 
     * @param view The custom view or null to use default
     */
    fun setLeadingView(view: View?) {
        this.customLeadingView = view
        Utils.handleView(leadingViewContainer, view, true)
    }

    /**
     * Gets the custom leading view.
     * 
     * @return The custom leading view
     */
    fun getLeadingView(): View? = customLeadingView

    /**
     * Sets a custom title view.
     * 
     * **Validates: Requirements 7a.18**
     * 
     * @param view The custom view or null to use default
     */
    fun setTitleView(view: View?) {
        this.customTitleView = view
        Utils.handleView(titleContainer, view, true)
    }

    /**
     * Gets the custom title view.
     * 
     * @return The custom title view
     */
    fun getTitleView(): View? = customTitleView

    /**
     * Sets a custom subtitle view.
     * 
     * **Validates: Requirements 7a.18**
     * 
     * @param view The custom view or null to use default
     */
    fun setSubtitleView(view: View?) {
        this.customSubtitleView = view
        Utils.handleView(subtitleContainer, view, true)
    }

    /**
     * Gets the custom subtitle view.
     * 
     * @return The custom subtitle view
     */
    fun getSubtitleView(): View? = customSubtitleView

    /**
     * Sets a custom trailing view.
     * 
     * **Validates: Requirements 7a.18**
     * 
     * @param view The custom view or null to use default
     */
    fun setTrailingView(view: View?) {
        this.customTrailingView = view
        Utils.handleView(trailingViewContainer, view, true)
    }

    /**
     * Gets the custom trailing view.
     * 
     * @return The custom trailing view
     */
    fun getTrailingView(): View? = customTrailingView

    // ==================== ViewModel Access ====================

    /**
     * Gets the ViewModel associated with this component.
     * 
     * @return The CometChatIncomingCallViewModel instance
     */
    fun getViewModel(): CometChatIncomingCallViewModel? = viewModel

    // ==================== Observer Management ====================

    /**
     * Attaches observers to the ViewModel StateFlows.
     * 
     * **Validates: Requirements 7a.20**
     */
    fun attachObservers() {
        val scope = viewScope ?: return
        val vm = viewModel ?: return

        // Observe accepted call
        acceptedCallJob = scope.launch {
            vm.acceptedCall.collect { acceptedCall ->
                acceptedCall?.let { handleAcceptedCall(it) }
            }
        }

        // Observe rejected call
        rejectedCallJob = scope.launch {
            vm.rejectedCall.collect { rejectedCall ->
                rejectedCall?.let { handleRejectedCall(it) }
            }
        }

        // Observe errors
        errorJob = scope.launch {
            vm.errorEvent.collect { exception ->
                handleError(exception)
            }
        }
    }

    /**
     * Disposes observers from the ViewModel.
     */
    fun disposeObservers() {
        acceptedCallJob?.cancel()
        rejectedCallJob?.cancel()
        errorJob?.cancel()
        acceptedCallJob = null
        rejectedCallJob = null
        errorJob = null
    }

    // ==================== Private Methods ====================

    /**
     * Updates the UI with caller information.
     */
    private fun updateCallerInfo() {
        val currentCall = call ?: return
        val callUser = currentCall.callInitiator as? User ?: return

        // Update caller name
        callerNameTextView.text = callUser.name

        // Update avatar
        callerAvatar.setAvatar(callUser.name ?: "", callUser.avatar)

        // Update call type icon and text
        val isAudioCall = currentCall.type == CometChatConstants.CALL_TYPE_AUDIO
        val iconRes = if (isAudioCall) {
            style.voiceCallIcon.takeIf { it != 0 } ?: R.drawable.cometchat_ic_call_voice
        } else {
            style.videoCallIcon.takeIf { it != 0 } ?: R.drawable.cometchat_ic_video_call
        }
        callTypeIcon.setImageResource(iconRes)
        callTypeIcon.setColorFilter(style.iconTint)

        val callTypeString = if (isAudioCall) {
            context.getString(R.string.cometchat_incoming_call_audio)
        } else {
            context.getString(R.string.cometchat_incoming_call_video)
        }
        callTypeTextView.text = context.getString(R.string.cometchat_incoming_call_type, callTypeString)
    }

    /**
     * Applies the current style to all views.
     */
    private fun applyStyle() {
        // Container styling
        innerCard.setCardBackgroundColor(style.backgroundColor)
        innerCard.radius = style.cornerRadius
        innerCard.strokeWidth = style.strokeWidth
        innerCard.strokeColor = style.strokeColor

        // Title styling
        callerNameTextView.setTextColor(style.titleTextColor)
        if (style.titleTextAppearance != 0) {
            callerNameTextView.setTextAppearance(style.titleTextAppearance)
        }

        // Subtitle styling
        callTypeTextView.setTextColor(style.subtitleTextColor)
        if (style.subtitleTextAppearance != 0) {
            callTypeTextView.setTextAppearance(style.subtitleTextAppearance)
        }

        // Icon styling
        callTypeIcon.setColorFilter(style.iconTint)

        // Accept button styling
        acceptButton.setBackgroundColor(style.acceptButtonBackgroundColor)
        acceptButton.setTextColor(style.acceptButtonTextColor)
        if (style.acceptButtonTextAppearance != 0) {
            acceptButton.setTextAppearance(style.acceptButtonTextAppearance)
        }

        // Reject button styling
        declineButton.setBackgroundColor(style.rejectButtonBackgroundColor)
        declineButton.setTextColor(style.rejectButtonTextColor)
        if (style.rejectButtonTextAppearance != 0) {
            declineButton.setTextAppearance(style.rejectButtonTextAppearance)
        }

        // Avatar styling
        style.avatarStyle?.let { avatarStyle ->
            callerAvatar.setStyle(avatarStyle)
        }
    }

    /**
     * Handles accepted call state.
     */
    private fun handleAcceptedCall(call: Call) {
        pauseSound()
        // Launch ongoing call activity
        launchOngoingCall(call)
    }

    /**
     * Handles rejected/cancelled call state.
     */
    private fun handleRejectedCall(call: Call) {
        pauseSound()
    }

    /**
     * Handles error events.
     */
    private fun handleError(exception: CometChatException) {
        onError?.invoke(exception)
    }

    /**
     * Launches the ongoing call activity.
     */
    private fun launchOngoingCall(call: Call) {
        CometChatOngoingCallActivity.launchOngoingCallActivity(
            context,
            call.sessionId,
            call.type,
            CallWorkFlow.DEFAULT,
            callSettingsBuilder as? CometChatCalls.CallSettingsBuilder,
            null
        )
    }

    /**
     * Plays the incoming call sound.
     * 
     * **Validates: Requirements 7a.15**
     */
    fun playSound() {
        if (!disableSoundForCalls) {
            soundManager.play(Sound.INCOMING_CALL, customSoundForCalls)
        }
    }

    /**
     * Pauses the incoming call sound.
     */
    fun pauseSound() {
        soundManager.pauseSilently()
    }

    // ==================== Lifecycle ====================

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isDetachedFromWindow) {
            attachObservers()
            isDetachedFromWindow = false
        }
        viewModel?.addListeners()
        if (call != null) {
            playSound()
        }
    }

    override fun onDetachedFromWindow() {
        isDetachedFromWindow = true
        viewModel?.removeListeners()
        pauseSound()
        disposeObservers()
        super.onDetachedFromWindow()
    }

    // ==================== Individual Style Setters ====================

    /**
     * Sets the background color for the component.
     */
    override fun setBackgroundColor(@ColorInt color: Int) {
        style = style.copy(backgroundColor = color)
        innerCard.setCardBackgroundColor(color)
    }

    /**
     * Gets the background color.
     */
    fun getBackgroundColorValue(): Int = style.backgroundColor

    /**
     * Sets the corner radius.
     */
    fun setCornerRadius(@Dimension radius: Float) {
        style = style.copy(cornerRadius = radius)
        innerCard.radius = radius
    }

    /**
     * Gets the corner radius.
     */
    fun getCornerRadiusValue(): Float = style.cornerRadius

    /**
     * Sets the stroke width.
     */
    override fun setStrokeWidth(@Dimension width: Int) {
        style = style.copy(strokeWidth = width)
        if (::innerCard.isInitialized) {
            innerCard.strokeWidth = width
        }
    }

    /**
     * Gets the stroke width.
     */
    override fun getStrokeWidth(): Int = style.strokeWidth

    /**
     * Sets the stroke color.
     */
    override fun setStrokeColor(@ColorInt color: Int) {
        style = style.copy(strokeColor = color)
        innerCard.strokeColor = color
    }

    /**
     * Gets the stroke color.
     */
    fun getStrokeColorValue(): Int = style.strokeColor

    /**
     * Sets the title text color.
     */
    fun setTitleTextColor(@ColorInt color: Int) {
        style = style.copy(titleTextColor = color)
        callerNameTextView.setTextColor(color)
    }

    /**
     * Gets the title text color.
     */
    fun getTitleTextColor(): Int = style.titleTextColor

    /**
     * Sets the title text appearance.
     */
    fun setTitleTextAppearance(@StyleRes appearance: Int) {
        style = style.copy(titleTextAppearance = appearance)
        if (appearance != 0) {
            callerNameTextView.setTextAppearance(appearance)
        }
    }

    /**
     * Gets the title text appearance.
     */
    fun getTitleTextAppearance(): Int = style.titleTextAppearance

    /**
     * Sets the subtitle text color.
     */
    fun setSubtitleTextColor(@ColorInt color: Int) {
        style = style.copy(subtitleTextColor = color)
        callTypeTextView.setTextColor(color)
    }

    /**
     * Gets the subtitle text color.
     */
    fun getSubtitleTextColor(): Int = style.subtitleTextColor

    /**
     * Sets the subtitle text appearance.
     */
    fun setSubtitleTextAppearance(@StyleRes appearance: Int) {
        style = style.copy(subtitleTextAppearance = appearance)
        if (appearance != 0) {
            callTypeTextView.setTextAppearance(appearance)
        }
    }

    /**
     * Gets the subtitle text appearance.
     */
    fun getSubtitleTextAppearance(): Int = style.subtitleTextAppearance

    /**
     * Sets the icon tint color.
     */
    fun setIconTint(@ColorInt color: Int) {
        style = style.copy(iconTint = color)
        callTypeIcon.setColorFilter(color)
    }

    /**
     * Gets the icon tint color.
     */
    fun getIconTint(): Int = style.iconTint

    /**
     * Sets the accept button background color.
     */
    fun setAcceptButtonBackgroundColor(@ColorInt color: Int) {
        style = style.copy(acceptButtonBackgroundColor = color)
        acceptButton.setBackgroundColor(color)
    }

    /**
     * Gets the accept button background color.
     */
    fun getAcceptButtonBackgroundColor(): Int = style.acceptButtonBackgroundColor

    /**
     * Sets the reject button background color.
     */
    fun setRejectButtonBackgroundColor(@ColorInt color: Int) {
        style = style.copy(rejectButtonBackgroundColor = color)
        declineButton.setBackgroundColor(color)
    }

    /**
     * Gets the reject button background color.
     */
    fun getRejectButtonBackgroundColor(): Int = style.rejectButtonBackgroundColor

    /**
     * Sets the accept button text color.
     */
    fun setAcceptButtonTextColor(@ColorInt color: Int) {
        style = style.copy(acceptButtonTextColor = color)
        acceptButton.setTextColor(color)
    }

    /**
     * Gets the accept button text color.
     */
    fun getAcceptButtonTextColor(): Int = style.acceptButtonTextColor

    /**
     * Sets the reject button text color.
     */
    fun setRejectButtonTextColor(@ColorInt color: Int) {
        style = style.copy(rejectButtonTextColor = color)
        declineButton.setTextColor(color)
    }

    /**
     * Gets the reject button text color.
     */
    fun getRejectButtonTextColor(): Int = style.rejectButtonTextColor

    /**
     * Sets the avatar style.
     */
    fun setAvatarStyle(avatarStyle: CometChatAvatarStyle) {
        style = style.copy(avatarStyle = avatarStyle)
        callerAvatar.setStyle(avatarStyle)
    }

    /**
     * Gets the avatar style.
     */
    fun getAvatarStyle(): CometChatAvatarStyle? = style.avatarStyle
}
