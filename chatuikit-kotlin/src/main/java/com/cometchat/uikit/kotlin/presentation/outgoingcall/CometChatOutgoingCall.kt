package com.cometchat.uikit.kotlin.presentation.outgoingcall

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.PowerManager
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User
import com.cometchat.calls.core.CometChatCalls
import com.cometchat.uikit.core.constants.UIKitConstants.CallWorkFlow
import com.cometchat.uikit.core.resources.soundmanager.CometChatSoundManager
import com.cometchat.uikit.core.resources.soundmanager.Sound
import com.cometchat.uikit.core.viewmodel.CometChatOutgoingCallViewModel
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.ongoingcall.ui.CometChatOngoingCall
import com.cometchat.uikit.kotlin.presentation.outgoingcall.style.CometChatOutgoingCallStyle
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar.CometChatAvatar
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar.CometChatAvatarStyle
import com.cometchat.uikit.kotlin.shared.interfaces.Function2
import com.cometchat.uikit.kotlin.shared.interfaces.OnClick
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * CometChatOutgoingCall is a custom view that displays outgoing call UI.
 * 
 * This component displays:
 * - Recipient's name centered at top (80dp margin)
 * - "Calling..." subtitle text
 * - Large avatar (120dp) centered
 * - Circular end call button at bottom (80dp margin)
 * - Transitions to CometChatOngoingCall when call is accepted
 * 
 * The UI is identical to the Java implementation in chatuikit.
 * 
 * **Validates: Requirements 11a.1-11a.22**
 * 
 * Usage in XML:
 * ```xml
 * <com.cometchat.uikit.kotlin.presentation.outgoingcall.CometChatOutgoingCall
 *     android:id="@+id/outgoing_call"
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent" />
 * ```
 * 
 * Usage in Kotlin:
 * ```kotlin
 * val outgoingCall = CometChatOutgoingCall(context)
 * outgoingCall.setCall(call)
 * outgoingCall.setOnEndCallClickListener { /* handle end call */ }
 * ```
 */
class CometChatOutgoingCall @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.cometchatOutgoingCallStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "CometChatOutgoingCall"
    }

    // ==================== Views ====================
    private lateinit var outgoingCallLayout: RelativeLayout
    private lateinit var titleLayout: LinearLayout
    private lateinit var titleTextView: TextView
    private lateinit var subtitleLayout: LinearLayout
    private lateinit var subtitleTextView: TextView
    private lateinit var avatarLayout: LinearLayout
    private lateinit var avatar: CometChatAvatar
    private lateinit var endCallLayout: LinearLayout
    private lateinit var endCallButton: MaterialButton
    private lateinit var ongoingCallView: CometChatOngoingCall

    // ==================== State ====================
    private var call: Call? = null
    private var user: User? = null
    private var viewModel: CometChatOutgoingCallViewModel? = null
    private var lifecycleOwner: LifecycleOwner? = null
    private var viewScope: CoroutineScope? = null
    private var isDetachedFromWindow = false
    private var soundManager: CometChatSoundManager
    private var activity: Activity? = null

    // ==================== Callbacks ====================
    private var onEndCallClick: OnClick? = null
    private var onError: ((CometChatException) -> Unit)? = null
    private var onBackPress: (() -> Unit)? = null

    // ==================== Configuration ====================
    private var disableSoundForCalls = false
    @RawRes private var customSoundForCalls = 0
    private var callSettingsBuilder: Any? = null

    // ==================== Custom Views ====================
    private var customTitleView: Function2<Context, Call, View>? = null
    private var customSubtitleView: Function2<Context, Call, View>? = null
    private var customAvatarView: Function2<Context, Call, View>? = null
    private var customEndCallView: Function2<Context, Call, View>? = null

    // ==================== Style ====================
    private var style: CometChatOutgoingCallStyle

    // ==================== Observer Jobs ====================
    private var acceptedCallJob: Job? = null
    private var rejectedCallJob: Job? = null
    private var errorJob: Job? = null
    private var endCallButtonEnabledJob: Job? = null

    // ==================== Proximity Sensor ====================
    private var wakeLock: PowerManager.WakeLock? = null
    private var sensorManager: SensorManager? = null
    private var proximitySensor: Sensor? = null
    private var proximitySensorListener: SensorEventListener? = null

    init {
        soundManager = CometChatSoundManager(context)
        style = CometChatOutgoingCallStyle.default(context)
        activity = Utils.getActivity(context)
        
        if (!isInEditMode) {
            initializeView()
            initSensors()
            applyStyleAttributes(attrs, defStyleAttr)
            initViewModel()
            setupClickListeners()
            keepScreenOn()
        }
    }

    // ==================== Initialization ====================

    /**
     * Initializes the view hierarchy programmatically.
     * Creates a layout identical to the Java implementation's XML layout:
     * - Title centered at top with 80dp margin
     * - Subtitle below title
     * - Large avatar (120dp) centered
     * - End call button at bottom with 80dp margin
     */
    private fun initializeView() {
        Utils.initMaterialCard(this)
        
        // Create main RelativeLayout container
        // Java layout uses cometchat_margin_5 = 20dp for horizontal padding
        outgoingCallLayout = RelativeLayout(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
            val horizontalPadding = Utils.convertDpToPx(context, 20) // cometchat_margin_5 = 20dp
            setPadding(horizontalPadding, 0, horizontalPadding, 0)
        }
        addView(outgoingCallLayout)

        // Create title layout
        titleLayout = LinearLayout(context).apply {
            id = View.generateViewId()
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
        }
        outgoingCallLayout.addView(titleLayout)

        // Create title TextView
        // Java layout uses cometchat_80dp = 80dp for top margin
        titleTextView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = Utils.convertDpToPx(context, 80) // cometchat_80dp = 80dp
            }
            gravity = Gravity.CENTER
            setTextColor(CometChatTheme.getTextColorPrimary(context))
            setTextAppearance(CometChatTheme.getTextAppearanceHeading2Bold(context))
            contentDescription = context.getString(R.string.cometchat_recipient_name)
        }
        titleLayout.addView(titleTextView)

        // Create subtitle layout
        subtitleLayout = LinearLayout(context).apply {
            id = View.generateViewId()
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.BELOW, titleLayout.id)
            }
            orientation = LinearLayout.VERTICAL
        }
        outgoingCallLayout.addView(subtitleLayout)

        // Create subtitle TextView
        // Java layout uses cometchat_margin_2 = 8dp for all margins
        subtitleTextView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                val margin = Utils.convertDpToPx(context, 8) // cometchat_margin_2 = 8dp
                setMargins(margin, margin, margin, margin)
            }
            gravity = Gravity.CENTER
            text = context.getString(R.string.cometchat_calling) + " ..."
            setTextColor(CometChatTheme.getTextColorSecondary(context))
            setTextAppearance(CometChatTheme.getTextAppearanceBodyRegular(context))
            contentDescription = context.getString(R.string.cometchat_calling)
        }
        subtitleLayout.addView(subtitleTextView)

        // Create avatar layout
        avatarLayout = LinearLayout(context).apply {
            id = View.generateViewId()
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.BELOW, subtitleLayout.id)
                addRule(RelativeLayout.CENTER_HORIZONTAL)
            }
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }
        outgoingCallLayout.addView(avatarLayout)

        // Create avatar (120dp size)
        // Java layout uses cometchat_120dp = 120dp for size, cometchat_margin_10 = 40dp for top margin
        val avatarSize = Utils.convertDpToPx(context, 120) // cometchat_120dp = 120dp
        avatar = CometChatAvatar(context).apply {
            layoutParams = LinearLayout.LayoutParams(avatarSize, avatarSize).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                topMargin = Utils.convertDpToPx(context, 40) // cometchat_margin_10 = 40dp
            }
            setAvatarStrokeRadius(Utils.convertDpToPx(context, 100f).toFloat())
            contentDescription = context.getString(R.string.cometchat_recipient_avatar)
        }
        avatarLayout.addView(avatar)

        // Create end call layout (at bottom)
        endCallLayout = LinearLayout(context).apply {
            id = View.generateViewId()
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                addRule(RelativeLayout.CENTER_HORIZONTAL)
            }
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }
        outgoingCallLayout.addView(endCallLayout)

        // Create circular end call button (50dp size)
        val buttonSize = Utils.convertDpToPx(context, 50)
        endCallButton = MaterialButton(context).apply {
            layoutParams = LinearLayout.LayoutParams(buttonSize, buttonSize).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                bottomMargin = Utils.convertDpToPx(context, 80)
            }
            insetTop = 0
            insetBottom = 0
            setPadding(0, 0, 0, 0)
            cornerRadius = buttonSize / 2
            setBackgroundColor(CometChatTheme.getErrorColor(context))
            icon = ContextCompat.getDrawable(context, style.endCallIcon)
            iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
            iconSize = Utils.convertDpToPx(context, 24)
            iconTint = android.content.res.ColorStateList.valueOf(CometChatTheme.getColorWhite(context))
            iconPadding = 0
            text = ""
            contentDescription = context.getString(R.string.cometchat_end_call_button)
        }
        endCallLayout.addView(endCallButton)
        
        // Create embedded CometChatOngoingCall view (initially hidden, like Java layout)
        ongoingCallView = CometChatOngoingCall(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
            visibility = View.GONE
        }
        addView(ongoingCallView)
    }

    /**
     * Initializes proximity sensors for screen management during calls.
     */
    private fun initSensors() {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        proximitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        
        if (proximitySensor == null) return
        
        proximitySensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.values[0] < (proximitySensor?.maximumRange ?: 0f)) {
                    turnOffScreen()
                } else {
                    turnOnScreen()
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
    }

    /**
     * Keeps the screen on while the outgoing call is displayed.
     */
    private fun keepScreenOn() {
        if (activity != null && !activity!!.isFinishing && !activity!!.isDestroyed) {
            activity!!.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    /**
     * Applies style attributes from XML.
     */
    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        var typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatOutgoingCall, defStyleAttr, 0
        )
        val styleResId = typedArray.getResourceId(
            R.styleable.CometChatOutgoingCall_cometchatOutgoingCallStyle, 0
        )
        typedArray.recycle()
        
        typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatOutgoingCall, defStyleAttr, styleResId
        )
        style = CometChatOutgoingCallStyle.fromTypedArray(context, typedArray)
        applyStyle()
    }

    /**
     * Initializes the ViewModel and sets up lifecycle observation.
     */
    private fun initViewModel() {
        lifecycleOwner = Utils.getLifecycleOwner(context)
        if (lifecycleOwner == null) return
        
        viewScope = lifecycleOwner?.lifecycleScope
        
        // Create ViewModel
        val viewModelStoreOwner = lifecycleOwner as? ViewModelStoreOwner
        viewModel = if (viewModelStoreOwner != null) {
            ViewModelProvider(viewModelStoreOwner)[CometChatOutgoingCallViewModel::class.java]
        } else {
            CometChatOutgoingCallViewModel()
        }
        
        attachObservers()
    }

    /**
     * Sets up click listeners for the end call button.
     */
    private fun setupClickListeners() {
        endCallButton.setOnClickListener {
            endCallButton.isEnabled = false
            if (onEndCallClick != null) {
                onEndCallClick?.onClick()
            } else {
                call?.let { viewModel?.cancelCall() }
            }
        }
    }

    // ==================== Public Methods ====================

    /**
     * Sets the outgoing call to display.
     * 
     * **Validates: Requirements 11a.4, 11a.5, 11a.6, 11a.7**
     * 
     * @param call The outgoing Call object
     */
    fun setCall(call: Call) {
        this.call = call
        viewModel?.setCall(call)
        
        // If receiver is a User, set the user
        if (call.receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
            setUser(call.receiver as? User)
        }
    }

    /**
     * Gets the current call.
     * 
     * @return The current Call or null if not set
     */
    fun getCall(): Call? = call

    /**
     * Sets the user for the outgoing call.
     * 
     * **Validates: Requirements 11a.5, 11a.6**
     * 
     * @param user The User being called
     */
    fun setUser(user: User?) {
        if (user != null) {
            this.user = user
            titleTextView.text = user.name
            avatar.setAvatar(user.name ?: "", user.avatar)
            subtitleTextView.text = context.getString(R.string.cometchat_calling) + " ..."
        }
    }

    /**
     * Gets the current user.
     * 
     * @return The current User or null if not set
     */
    fun getUser(): User? = user

    /**
     * Sets the style for this component.
     * 
     * **Validates: Requirements 11a.18**
     * 
     * @param style The CometChatOutgoingCallStyle to apply
     */
    fun setStyle(style: CometChatOutgoingCallStyle) {
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
                styleRes, R.styleable.CometChatOutgoingCall
            )
            style = CometChatOutgoingCallStyle.fromTypedArray(context, typedArray)
            applyStyle()
        }
    }

    /**
     * Gets the current style.
     * 
     * @return The current CometChatOutgoingCallStyle
     */
    fun getStyle(): CometChatOutgoingCallStyle = style

    /**
     * Sets the end call button click listener.
     * 
     * **Validates: Requirements 11a.9**
     * 
     * @param listener The click listener or null to use default behavior
     */
    fun setOnEndCallClickListener(listener: OnClick?) {
        this.onEndCallClick = listener
    }

    /**
     * Gets the end call button click listener.
     * 
     * @return The current OnClick listener
     */
    fun getOnEndCallClickListener(): OnClick? = onEndCallClick

    /**
     * Sets the error callback.
     * 
     * **Validates: Requirements 11a.10**
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
     * Sets the back press callback.
     * 
     * **Validates: Requirements 11a.11**
     * 
     * @param listener The back press callback
     */
    fun setOnBackPressListener(listener: (() -> Unit)?) {
        this.onBackPress = listener
    }

    /**
     * Gets the back press callback.
     * 
     * @return The current back press callback
     */
    fun getOnBackPressListener(): (() -> Unit)? = onBackPress

    /**
     * Disables or enables sound for outgoing calls.
     * 
     * **Validates: Requirements 11a.12**
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
     * Sets a custom sound resource for outgoing calls.
     * 
     * **Validates: Requirements 11a.13**
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
     * **Validates: Requirements 11a.15**
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
     * Sets a custom title view.
     * 
     * **Validates: Requirements 11a.19**
     * 
     * @param view Function that creates the custom view given context and call
     */
    fun setTitleView(view: Function2<Context, Call, View>?) {
        this.customTitleView = view
        if (view != null && call != null) {
            Utils.handleView(titleLayout, view.apply(context, call!!), true)
        }
    }

    /**
     * Gets the custom title view function.
     * 
     * @return The custom title view function
     */
    fun getTitleView(): Function2<Context, Call, View>? = customTitleView

    /**
     * Sets a custom subtitle view.
     * 
     * **Validates: Requirements 11a.19**
     * 
     * @param view Function that creates the custom view given context and call
     */
    fun setSubtitleView(view: Function2<Context, Call, View>?) {
        this.customSubtitleView = view
        if (view != null && call != null) {
            Utils.handleView(subtitleLayout, view.apply(context, call!!), true)
        }
    }

    /**
     * Gets the custom subtitle view function.
     * 
     * @return The custom subtitle view function
     */
    fun getSubtitleView(): Function2<Context, Call, View>? = customSubtitleView

    /**
     * Sets a custom avatar view.
     * 
     * **Validates: Requirements 11a.19**
     * 
     * @param view Function that creates the custom view given context and call
     */
    fun setAvatarView(view: Function2<Context, Call, View>?) {
        this.customAvatarView = view
        if (view != null && call != null) {
            Utils.handleView(avatarLayout, view.apply(context, call!!), true)
        }
    }

    /**
     * Gets the custom avatar view function.
     * 
     * @return The custom avatar view function
     */
    fun getAvatarView(): Function2<Context, Call, View>? = customAvatarView

    /**
     * Sets a custom end call view.
     * 
     * **Validates: Requirements 11a.19**
     * 
     * @param view Function that creates the custom view given context and call
     */
    fun setEndCallView(view: Function2<Context, Call, View>?) {
        this.customEndCallView = view
        if (view != null && call != null) {
            Utils.handleView(endCallLayout, view.apply(context, call!!), true)
        }
    }

    /**
     * Gets the custom end call view function.
     * 
     * @return The custom end call view function
     */
    fun getEndCallView(): Function2<Context, Call, View>? = customEndCallView

    // ==================== ViewModel Access ====================

    /**
     * Gets the ViewModel associated with this component.
     * 
     * @return The CometChatOutgoingCallViewModel instance
     */
    fun getViewModel(): CometChatOutgoingCallViewModel? = viewModel

    // ==================== Observer Management ====================

    /**
     * Attaches observers to the ViewModel StateFlows.
     * 
     * **Validates: Requirements 11a.22**
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

        // Observe end call button enabled state
        endCallButtonEnabledJob = scope.launch {
            vm.endCallButtonEnabled.collect { enabled ->
                setEndCallButtonEnabled(enabled)
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
        endCallButtonEnabledJob?.cancel()
        acceptedCallJob = null
        rejectedCallJob = null
        errorJob = null
        endCallButtonEnabledJob = null
    }

    // ==================== Private Methods ====================

    /**
     * Applies the current style to all views.
     */
    private fun applyStyle() {
        // Container styling
        setCardBackgroundColor(style.backgroundColor)
        radius = style.cornerRadius
        strokeWidth = style.strokeWidth
        strokeColor = style.strokeColor

        // Title styling
        titleTextView.setTextColor(style.titleTextColor)
        if (style.titleTextAppearance != 0) {
            titleTextView.setTextAppearance(style.titleTextAppearance)
        }

        // Subtitle styling
        subtitleTextView.setTextColor(style.subtitleTextColor)
        if (style.subtitleTextAppearance != 0) {
            subtitleTextView.setTextAppearance(style.subtitleTextAppearance)
        }

        // End call button styling
        endCallButton.setBackgroundColor(style.endCallButtonBackgroundColor)
        if (style.endCallIcon != 0) {
            endCallButton.icon = ContextCompat.getDrawable(context, style.endCallIcon)
        }
        endCallButton.iconTint = android.content.res.ColorStateList.valueOf(style.endCallIconTint)

        // Avatar styling
        style.avatarStyle?.let { avatarStyle ->
            avatar.setStyle(avatarStyle)
        }
    }

    /**
     * Handles accepted call state - transitions to ongoing call.
     * 
     * **Validates: Requirements 11a.16**
     */
    private fun handleAcceptedCall(call: Call) {
        launchOngoingCall(call)
    }

    /**
     * Handles rejected/cancelled call state.
     */
    private fun handleRejectedCall(call: Call) {
        pauseSound()
        if (onBackPress != null) {
            onBackPress?.invoke()
        } else {
            if (activity != null && !activity!!.isFinishing && !activity!!.isDestroyed) {
                activity!!.finish()
            }
        }
    }

    /**
     * Handles error events.
     */
    private fun handleError(exception: CometChatException) {
        if (onError != null) {
            onError?.invoke(exception)
        } else {
            if (activity != null && !activity!!.isFinishing && !activity!!.isDestroyed) {
                activity!!.finish()
            }
        }
    }

    /**
     * Sets the end call button enabled state.
     * 
     * **Validates: Requirements 11a.21**
     */
    private fun setEndCallButtonEnabled(enabled: Boolean) {
        endCallButton.isEnabled = enabled
    }

    /**
     * Launches the ongoing call screen directly, bypassing the ringing UI.
     * Used for group conference calls (direct calls).
     * 
     * This method is called by CometChatCallActivity for DIRECT_CALL type.
     * It matches the Java implementation's public launchOnGoingScreen method.
     * 
     * @param call The Call object with session ID and call type set
     */
    fun launchOnGoingScreen(call: Call) {
        launchOngoingCall(call)
    }

    /**
     * Launches the ongoing call screen using embedded view (matching Java implementation).
     * 
     * **Validates: Requirements 11a.16**
     */
    private fun launchOngoingCall(call: Call) {
        // Hide outgoing call layout
        outgoingCallLayout.visibility = View.GONE
        pauseSound()
        
        // Configure and show embedded ongoing call view (matching Java's launchOnGoingScreen)
        val callWorkFlow = if (call.receiverType.equals(CometChatConstants.RECEIVER_TYPE_GROUP, ignoreCase = true)) {
            CallWorkFlow.MEETING
        } else {
            CallWorkFlow.DEFAULT
        }
        
        ongoingCallView.setCallWorkFlow(callWorkFlow)
        ongoingCallView.setSessionId(call.sessionId)
        ongoingCallView.setCallType(call.type)
        (callSettingsBuilder as? CometChatCalls.CallSettingsBuilder)?.let {
            ongoingCallView.setCallSettingsBuilder(it)
        }
        ongoingCallView.startCall()
        ongoingCallView.visibility = View.VISIBLE
    }

    /**
     * Plays the outgoing call sound.
     * 
     * **Validates: Requirements 11a.14**
     */
    private fun playSound() {
        if (!disableSoundForCalls) {
            soundManager.play(Sound.OUTGOING_CALL, customSoundForCalls)
        }
    }

    /**
     * Pauses the outgoing call sound.
     */
    private fun pauseSound() {
        soundManager.pauseSilently()
    }

    /**
     * Turns off the screen using proximity sensor.
     */
    private fun turnOffScreen() {
        if (activity != null && !activity!!.isFinishing && !activity!!.isDestroyed) {
            val powerManager = activity!!.getSystemService(Context.POWER_SERVICE) as? PowerManager
            if (powerManager != null) {
                if (wakeLock == null) {
                    wakeLock = powerManager.newWakeLock(
                        PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
                        "CometChat::ProximityWakeLock"
                    )
                }
                if (wakeLock?.isHeld == false) {
                    wakeLock?.acquire()
                }
            }
        }
    }

    /**
     * Turns on the screen.
     */
    private fun turnOnScreen() {
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }

    /**
     * Starts the proximity sensor.
     */
    private fun startProximitySensor() {
        if (sensorManager != null && proximitySensor != null && proximitySensorListener != null) {
            sensorManager?.registerListener(
                proximitySensorListener,
                proximitySensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    /**
     * Stops the proximity sensor.
     */
    private fun stopProximitySensor() {
        if (sensorManager != null && proximitySensorListener != null) {
            sensorManager?.unregisterListener(proximitySensorListener)
        }
    }

    // ==================== Lifecycle ====================

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isDetachedFromWindow) {
            attachObservers()
            isDetachedFromWindow = false
        }
        viewModel?.addListeners()
        if (user != null) {
            playSound()
        }
        startProximitySensor()
    }

    override fun onDetachedFromWindow() {
        // Clear screen on flag
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        isDetachedFromWindow = true
        viewModel?.removeListeners()
        pauseSound()
        stopProximitySensor()
        
        // Release wake lock
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
            wakeLock = null
        }
        
        disposeObservers()
        super.onDetachedFromWindow()
    }

    // ==================== Individual Style Setters ====================

    /**
     * Sets the background color for the component.
     */
    override fun setBackgroundColor(@ColorInt color: Int) {
        style = style.copy(backgroundColor = color)
        setCardBackgroundColor(color)
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
        setRadius(radius)
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
        super.setStrokeWidth(width)
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
        super.setStrokeColor(color)
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
        titleTextView.setTextColor(color)
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
            titleTextView.setTextAppearance(appearance)
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
        subtitleTextView.setTextColor(color)
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
            subtitleTextView.setTextAppearance(appearance)
        }
    }

    /**
     * Gets the subtitle text appearance.
     */
    fun getSubtitleTextAppearance(): Int = style.subtitleTextAppearance

    /**
     * Sets the end call icon.
     */
    fun setEndCallIcon(@DrawableRes iconRes: Int) {
        style = style.copy(endCallIcon = iconRes)
        if (iconRes != 0) {
            endCallButton.icon = ContextCompat.getDrawable(context, iconRes)
        }
    }

    /**
     * Gets the end call icon resource.
     */
    fun getEndCallIcon(): Int = style.endCallIcon

    /**
     * Sets the end call icon tint.
     */
    fun setEndCallIconTint(@ColorInt color: Int) {
        style = style.copy(endCallIconTint = color)
        endCallButton.iconTint = android.content.res.ColorStateList.valueOf(color)
    }

    /**
     * Gets the end call icon tint.
     */
    fun getEndCallIconTint(): Int = style.endCallIconTint

    /**
     * Sets the end call button background color.
     */
    fun setEndCallButtonBackgroundColor(@ColorInt color: Int) {
        style = style.copy(endCallButtonBackgroundColor = color)
        endCallButton.setBackgroundColor(color)
    }

    /**
     * Gets the end call button background color.
     */
    fun getEndCallButtonBackgroundColor(): Int = style.endCallButtonBackgroundColor

    /**
     * Sets the avatar style.
     */
    fun setAvatarStyle(avatarStyle: CometChatAvatarStyle) {
        style = style.copy(avatarStyle = avatarStyle)
        avatar.setStyle(avatarStyle)
    }

    /**
     * Gets the avatar style.
     */
    fun getAvatarStyle(): CometChatAvatarStyle? = style.avatarStyle
}
