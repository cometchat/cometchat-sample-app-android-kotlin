package com.cometchat.uikit.kotlin.presentation.messageheader.ui

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.TypingIndicator
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.factory.CometChatMessageHeaderViewModelFactory
import com.cometchat.uikit.core.state.MessageHeaderUIState
import com.cometchat.uikit.core.utils.CallsUtils
import com.cometchat.uikit.core.viewmodel.CometChatMessageHeaderViewModel
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.databinding.CometchatMessageHeaderBinding
import com.cometchat.uikit.kotlin.presentation.callbuttons.CometChatCallButtons
import com.cometchat.uikit.kotlin.presentation.messageheader.style.CometChatMessageHeaderStyle
import com.cometchat.uikit.kotlin.presentation.messageheader.utils.MessageHeaderViewHolderListener
import com.cometchat.uikit.kotlin.presentation.shared.popupmenu.CometChatPopupMenu
import com.cometchat.uikit.kotlin.presentation.shared.statusindicator.StatusIndicator
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * CometChatMessageHeader is a custom Android View that displays the header of a chat conversation.
 * Shows user/group information including avatar, name, status, and typing indicators.
 * 
 * This component uses the shared CometChatMessageHeaderViewModel from chatuikit-core,
 * ensuring consistent business logic with the Jetpack Compose version.
 * 
 * Usage in XML:
 * ```xml
 * <com.cometchat.uikit.kotlin.presentation.messageheader.ui.CometChatMessageHeader
 *     android:id="@+id/messageHeader"
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:cometchatMessageHeaderStyle="@style/CometChatMessageHeader" />
 * ```
 * 
 * Usage in Kotlin:
 * ```kotlin
 * val messageHeader = CometChatMessageHeader(context)
 * messageHeader.setUser(user)
 * messageHeader.setOnBackPress { /* Handle back */ }
 * ```
 */
class CometChatMessageHeader @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.cometchatMessageHeaderStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = CometChatMessageHeader::class.java.simpleName
    }

    // View Binding
    private val binding: CometchatMessageHeaderBinding

    // ViewModel (shared from chatuikit-core)
    private var viewModel: CometChatMessageHeaderViewModel? = null
    private var isExternalViewModel: Boolean = false

    // Lifecycle owner for observing flows
    private var lifecycleOwner: LifecycleOwner? = null

    // Coroutine scope for collecting flows
    private var viewScope: CoroutineScope? = null

    // Data
    private var user: User? = null
    private var group: Group? = null

    // Callbacks
    private var onBackPress: (() -> Unit)? = null
    private var onError: ((CometChatException) -> Unit)? = null
    private var onNewChatClick: (() -> Unit)? = null
    private var onChatHistoryClick: (() -> Unit)? = null
    private var onVideoCallClick: ((User?, Group?) -> Unit)? = null
    private var onVoiceCallClick: ((User?, Group?) -> Unit)? = null

    // Custom view listeners (matching chatuikit pattern)
    private var leadingViewListener: MessageHeaderViewHolderListener? = null
    private var titleViewListener: MessageHeaderViewHolderListener? = null
    private var subtitleViewListener: MessageHeaderViewHolderListener? = null
    private var trailingViewListener: MessageHeaderViewHolderListener? = null
    private var auxiliaryViewListener: MessageHeaderViewHolderListener? = null
    private var itemViewListener: MessageHeaderViewHolderListener? = null

    // Formatters
    private var lastSeenTextFormatter: ((Context, User) -> String)? = null
    private var dateTimeFormatter: com.cometchat.uikit.kotlin.shared.interfaces.DateTimeFormatterCallback? = null

    // Menu
    private var menuOptions: List<CometChatPopupMenu.MenuItem>? = null
    private val popupMenu: CometChatPopupMenu

    // Visibility controls
    private var backButtonVisibility = View.GONE
    private var userStatusVisibility = View.VISIBLE
    private var groupStatusVisibility = View.VISIBLE
    private var menuIconVisibility = View.GONE
    // Call button visibility defaults to GONE if calling is not enabled
    private var videoCallButtonVisibility = if (CallsUtils.isCallingEnabled()) View.VISIBLE else View.GONE
    private var voiceCallButtonVisibility = if (CallsUtils.isCallingEnabled()) View.VISIBLE else View.GONE
    private var newChatButtonVisibility = View.GONE
    private var chatHistoryButtonVisibility = View.GONE

    // Single style object - NO individual style properties
    private var style: CometChatMessageHeaderStyle = CometChatMessageHeaderStyle()

    init {
        binding = CometchatMessageHeaderBinding.inflate(
            LayoutInflater.from(context), this
        )
        Utils.initMaterialCard(this)
        popupMenu = CometChatPopupMenu(context, 0)
        applyStyleAttributes(attrs, defStyleAttr)
        setupClickListeners()
        initViewModel()
    }

    /**
     * Applies style attributes from XML using the style class factory method.
     */
    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        var typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatMessageHeader, defStyleAttr, 0
        )
        val styleResId = typedArray.getResourceId(
            R.styleable.CometChatMessageHeader_cometchatMessageHeaderStyle, 0
        )
        typedArray.recycle()

        typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatMessageHeader, defStyleAttr, styleResId
        )
        // fromTypedArray handles recycling internally
        style = CometChatMessageHeaderStyle.fromTypedArray(context, typedArray)
        applyStyle()
    }

    /**
     * Applies all style properties from the style object to views.
     */
    private fun applyStyle() {
        // Container styling
        if (style.backgroundColor != 0) setCardBackgroundColor(style.backgroundColor)
        if (style.strokeColor != 0) setStrokeColor(style.strokeColor)
        if (style.strokeWidth != 0) strokeWidth = style.strokeWidth
        if (style.cornerRadius != 0) radius = style.cornerRadius.toFloat()

        // Title styling
        if (style.titleTextColor != 0) binding.tvMessageHeaderName.setTextColor(style.titleTextColor)
        if (style.titleTextAppearance != 0) binding.tvMessageHeaderName.setTextAppearance(style.titleTextAppearance)

        // Subtitle styling
        if (style.subtitleTextColor != 0) binding.tvMessageHeaderSubtitle.setTextColor(style.subtitleTextColor)
        if (style.subtitleTextAppearance != 0) binding.tvMessageHeaderSubtitle.setTextAppearance(style.subtitleTextAppearance)

        // Typing indicator styling
        if (style.typingIndicatorTextColor != 0) binding.tvMessageHeaderTypingIndicator.setTextColor(style.typingIndicatorTextColor)
        if (style.typingIndicatorTextAppearance != 0) binding.tvMessageHeaderTypingIndicator.setTextAppearance(style.typingIndicatorTextAppearance)

        // Back icon styling
        style.backIcon?.let { binding.ivMessageHeaderBack.setImageDrawable(it) }
        if (style.backIconTint != 0) binding.ivMessageHeaderBack.setColorFilter(style.backIconTint)

        // Menu icon styling
        style.menuIcon?.let { binding.messageHeaderMenuIcon.setImageDrawable(it) }
        if (style.menuIconTint != 0) binding.messageHeaderMenuIcon.setColorFilter(style.menuIconTint)

        // AI buttons styling
        style.newChatIcon?.let { binding.ivNewChat.setImageDrawable(it) }
        if (style.newChatIconTint != 0) binding.ivNewChat.setColorFilter(style.newChatIconTint)
        style.chatHistoryIcon?.let { binding.ivChatHistory.setImageDrawable(it) }
        if (style.chatHistoryIconTint != 0) binding.ivChatHistory.setColorFilter(style.chatHistoryIconTint)

        // Call buttons styling - apply to CometChatCallButtons
        style.videoCallIcon?.let { binding.callButtons.setVideoCallIcon(it) }
        if (style.videoCallIconTint != 0) binding.callButtons.setVideoCallIconTint(style.videoCallIconTint)
        style.voiceCallIcon?.let { binding.callButtons.setVoiceCallIcon(it) }
        if (style.voiceCallIconTint != 0) binding.callButtons.setVoiceCallIconTint(style.voiceCallIconTint)
    }

    /**
     * Sets up click listeners for interactive elements.
     */
    private fun setupClickListeners() {
        binding.ivMessageHeaderBack.setOnClickListener {
            onBackPress?.invoke()
        }

        binding.messageHeaderMenuIcon.setOnClickListener {
            showPopupMenu()
        }

        binding.ivNewChat.setOnClickListener {
            onNewChatClick?.invoke()
        }

        binding.ivChatHistory.setOnClickListener {
            onChatHistoryClick?.invoke()
        }

        // CometChatCallButtons handles its own click listeners internally
        // Custom callbacks are set via setOnVideoCallClick/setOnVoiceCallClick
        setupCallButtonsCallbacks()
    }

    /**
     * Sets up callbacks for CometChatCallButtons.
     */
    private fun setupCallButtonsCallbacks() {
        binding.callButtons.setOnVideoCallClick(onVideoCallClick)
        binding.callButtons.setOnVoiceCallClick(onVoiceCallClick)
        binding.callButtons.setOnError(onError)
    }

    /**
     * Initializes the ViewModel.
     */
    private fun initViewModel() {
        if (!isExternalViewModel) {
            viewModel = CometChatMessageHeaderViewModelFactory()
                .create(CometChatMessageHeaderViewModel::class.java)
        }
        lifecycleOwner = Utils.getLifecycleOwner(context)
        startCollectingFlows()
    }

    /**
     * Starts collecting flows from the ViewModel.
     */
    private fun startCollectingFlows() {
        viewScope?.cancel()
        viewScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

        viewScope?.launch {
            viewModel?.uiState?.collectLatest { state ->
                when (state) {
                    is MessageHeaderUIState.UserContent -> updateUserUI(state.user)
                    is MessageHeaderUIState.GroupContent -> updateGroupUI(state.group)
                    is MessageHeaderUIState.Error -> onError?.invoke(state.exception)
                    is MessageHeaderUIState.Loading -> { /* Show loading if needed */ }
                }
            }
        }

        viewScope?.launch {
            viewModel?.typingIndicator?.collectLatest { indicator ->
                updateTypingIndicator(indicator)
            }
        }

        viewScope?.launch {
            viewModel?.memberCount?.collectLatest { count ->
                updateMemberCount(count)
            }
        }

        viewScope?.launch {
            viewModel?.errorEvent?.collect { error ->
                onError?.invoke(error)
            }
        }
    }

    /**
     * Sets the user for the message header.
     */
    fun setUser(user: User) {
        this.user = user
        this.group = null
        viewModel?.setUser(user)
        binding.callButtons.setUser(user)
        updateCallButtonsVisibility()
        invokeViewCallbacks()
    }

    /**
     * Sets the group for the message header.
     */
    fun setGroup(group: Group) {
        this.group = group
        this.user = null
        viewModel?.setGroup(group)
        binding.callButtons.setGroup(group)
        updateCallButtonsVisibility()
        invokeViewCallbacks()
    }

    /**
     * Updates the visibility of CometChatCallButtons based on visibility flags and calling availability.
     * 
     * Call buttons are only shown when:
     * 1. The visibility flag is set to VISIBLE
     * 2. CallsUtils.isCallingEnabled() returns true (SDK available AND enableCalling is true)
     * 3. A user or group is set
     * 
     * **Validates: Requirements 4.1-4.4**
     */
    private fun updateCallButtonsVisibility() {
        val isCallingEnabled = CallsUtils.isCallingEnabled()
        val showVideoCall = videoCallButtonVisibility == View.VISIBLE && isCallingEnabled
        val showVoiceCall = voiceCallButtonVisibility == View.VISIBLE && isCallingEnabled
        
        binding.callButtons.setVideoCallButtonVisibility(if (showVideoCall) View.VISIBLE else View.GONE)
        binding.callButtons.setVoiceCallButtonVisibility(if (showVoiceCall) View.VISIBLE else View.GONE)
        
        // Show the call buttons container if at least one button is visible and we have a user/group
        val hasEntity = user != null || group != null
        binding.callButtons.visibility = if ((showVideoCall || showVoiceCall) && hasEntity) View.VISIBLE else View.GONE
    }

    /**
     * Updates the UI for a user.
     */
    private fun updateUserUI(user: User) {
        this.user = user
        binding.messageHeaderAvatarView.setAvatar(user.name, user.avatar)
        binding.tvMessageHeaderName.text = user.name

        if (subtitleViewListener == null) {
            if (!isBlocked(user)) {
                binding.messageHeaderStatusIndicatorView.visibility = userStatusVisibility
                showUserStatusAndLastSeen(user)
            } else {
                binding.messageHeaderStatusIndicatorView.visibility = View.GONE
                binding.messageHeaderSubtitleLayout.visibility = View.GONE
            }
        }
    }

    /**
     * Updates the UI for a group.
     */
    private fun updateGroupUI(group: Group) {
        this.group = group
        binding.messageHeaderAvatarView.setAvatar(group.name, group.icon)
        binding.tvMessageHeaderName.text = group.name

        if (subtitleViewListener == null) {
            updateMemberCount(group.membersCount)
            updateGroupStatusIndicator(group)
        }
    }

    /**
     * Shows user status and last seen time.
     */
    private fun showUserStatusAndLastSeen(user: User) {
        if (subtitleViewListener == null) {
            if (userStatusVisibility == View.GONE) {
                binding.tvMessageHeaderSubtitle.visibility = View.GONE
            } else {
                binding.tvMessageHeaderSubtitle.visibility = View.VISIBLE
                if (user.status == CometChatConstants.USER_STATUS_ONLINE) {
                    binding.tvMessageHeaderSubtitle.text = context.getString(R.string.cometchat_online)
                    binding.messageHeaderStatusIndicatorView.setStatusIndicator(StatusIndicator.ONLINE)
                } else {
                    binding.messageHeaderStatusIndicatorView.setStatusIndicator(StatusIndicator.OFFLINE)
                    val lastSeenText = lastSeenTextFormatter?.invoke(context, user)
                        ?: if (user.lastActiveAt == 0L) {
                            context.getString(R.string.cometchat_offline)
                        } else {
                            getFormattedLastSeenTime(user.lastActiveAt)
                        }
                    binding.tvMessageHeaderSubtitle.text = lastSeenText
                }
            }
        }
    }

    /**
     * Gets formatted last seen time using dateTimeFormatter if available, otherwise uses default.
     */
    private fun getFormattedLastSeenTime(timestamp: Long): String {
        dateTimeFormatter?.let { formatter ->
            // Try each formatter method in order of specificity
            val now = java.util.Calendar.getInstance()
            val lastSeen = java.util.Calendar.getInstance()
            var timestampMs = timestamp
            if (timestamp.toString().length == 10) {
                timestampMs = timestamp * 1000
            }
            lastSeen.timeInMillis = timestampMs

            val diffInMillis = now.timeInMillis - lastSeen.timeInMillis
            val diffInMinutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(diffInMillis)
            val diffInHours = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(diffInMillis)

            // Check minutes
            if (diffInMinutes == 1L) {
                formatter.minute(timestampMs)?.let { return it }
            }
            if (diffInMinutes in 2..59) {
                formatter.minutes(diffInMinutes, timestampMs)?.let { return it }
            }

            // Check hours
            if (diffInHours == 1L) {
                formatter.hour(timestampMs)?.let { return it }
            }
            if (diffInHours in 2..23) {
                formatter.hours(diffInHours, timestampMs)?.let { return it }
            }

            // Check day-based patterns
            val isSameDay = now.get(java.util.Calendar.DAY_OF_YEAR) == lastSeen.get(java.util.Calendar.DAY_OF_YEAR) &&
                    now.get(java.util.Calendar.YEAR) == lastSeen.get(java.util.Calendar.YEAR)
            if (isSameDay) {
                formatter.today(timestampMs)?.let { return it }
            }

            val yesterday = java.util.Calendar.getInstance()
            yesterday.add(java.util.Calendar.DAY_OF_YEAR, -1)
            val isYesterday = yesterday.get(java.util.Calendar.DAY_OF_YEAR) == lastSeen.get(java.util.Calendar.DAY_OF_YEAR) &&
                    yesterday.get(java.util.Calendar.YEAR) == lastSeen.get(java.util.Calendar.YEAR)
            if (isYesterday) {
                formatter.yesterday(timestampMs)?.let { return it }
            }

            val diffInDays = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diffInMillis)
            if (diffInDays in 2..7) {
                formatter.lastWeek(timestampMs)?.let { return it }
            }

            // Other days
            formatter.otherDays(timestampMs)?.let { return it }
        }

        // Fall back to default formatting
        return Utils.getLastSeenTime(context, timestamp)
    }

    /**
     * Updates the group status indicator.
     */
    private fun updateGroupStatusIndicator(group: Group) {
        if (groupStatusVisibility == View.VISIBLE) {
            when (group.groupType) {
                CometChatConstants.GROUP_TYPE_PASSWORD ->
                    binding.messageHeaderStatusIndicatorView.setStatusIndicator(StatusIndicator.PROTECTED_GROUP)
                CometChatConstants.GROUP_TYPE_PRIVATE ->
                    binding.messageHeaderStatusIndicatorView.setStatusIndicator(StatusIndicator.PRIVATE_GROUP)
                else ->
                    binding.messageHeaderStatusIndicatorView.visibility = View.GONE
            }
        } else {
            binding.messageHeaderStatusIndicatorView.visibility = View.GONE
        }
    }

    /**
     * Updates the member count display.
     */
    private fun updateMemberCount(count: Int) {
        if (subtitleViewListener == null && group != null) {
            val memberText = if (count > 1) {
                "$count ${context.getString(R.string.cometchat_members)}"
            } else {
                "$count ${context.getString(R.string.cometchat_member)}"
            }
            binding.tvMessageHeaderSubtitle.text = memberText
        }
    }

    /**
     * Updates the typing indicator display.
     */
    private fun updateTypingIndicator(indicator: TypingIndicator?) {
        if (subtitleViewListener == null) {
            if (indicator != null) {
                binding.tvMessageHeaderSubtitle.visibility = View.GONE
                binding.tvMessageHeaderTypingIndicator.visibility = View.VISIBLE
                val typingText = if (indicator.receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
                    context.getString(R.string.cometchat_typing)
                } else {
                    "${indicator.sender.name} ${context.getString(R.string.cometchat_is_typing)}"
                }
                binding.tvMessageHeaderTypingIndicator.text = typingText
            } else {
                binding.tvMessageHeaderSubtitle.visibility = View.VISIBLE
                binding.tvMessageHeaderTypingIndicator.visibility = View.GONE
            }
        }
    }

    /**
     * Invokes custom view callbacks.
     */
    private fun invokeViewCallbacks() {
        if (itemViewListener != null) {
            Utils.handleView(binding.parentLayout, itemViewListener?.createView(context, user, group), true)
        } else {
            leadingViewListener?.let {
                Utils.handleView(binding.messageHeaderAvatarLayout, it.createView(context, user, group), true)
            }
            titleViewListener?.let {
                Utils.handleView(binding.titleView, it.createView(context, user, group), true)
            }
            subtitleViewListener?.let {
                Utils.handleView(binding.messageHeaderSubtitleLayout, it.createView(context, user, group), true)
            }
            trailingViewListener?.let {
                Utils.handleView(binding.messageHeaderTailView, it.createView(context, user, group), true)
            }
            auxiliaryViewListener?.let {
                Utils.handleView(binding.messageHeaderAuxiliaryView, it.createView(context, user, group), true)
            }
        }
    }

    /**
     * Shows the popup menu.
     */
    private fun showPopupMenu() {
        menuOptions?.let { options ->
            if (options.isNotEmpty()) {
                popupMenu.setMenuItems(options)
                popupMenu.showAsDropDown(binding.messageHeaderMenuIcon)
            }
        }
    }

    /**
     * Checks if a user is blocked.
     */
    private fun isBlocked(user: User?): Boolean {
        return user?.let { it.isBlockedByMe || it.isHasBlockedMe } ?: false
    }

    // ==================== Public Visibility Setters ====================

    fun setBackButtonVisibility(visibility: Int) {
        backButtonVisibility = visibility
        binding.ivMessageHeaderBack.visibility = visibility
    }

    fun setUserStatusVisibility(visibility: Int) {
        userStatusVisibility = visibility
    }

    fun setGroupStatusVisibility(visibility: Int) {
        groupStatusVisibility = visibility
    }

    fun setMenuIconVisibility(visibility: Int) {
        menuIconVisibility = visibility
        binding.messageHeaderMenuIcon.visibility = visibility
    }

    fun setVideoCallButtonVisibility(visibility: Int) {
        videoCallButtonVisibility = visibility
        updateCallButtonsVisibility()
    }

    fun setVoiceCallButtonVisibility(visibility: Int) {
        voiceCallButtonVisibility = visibility
        updateCallButtonsVisibility()
    }

    fun setNewChatButtonVisibility(visibility: Int) {
        newChatButtonVisibility = visibility
        binding.ivNewChat.visibility = visibility
    }

    fun setChatHistoryButtonVisibility(visibility: Int) {
        chatHistoryButtonVisibility = visibility
        binding.ivChatHistory.visibility = visibility
    }

    // ==================== Public Callback Setters ====================

    fun setOnBackPress(callback: () -> Unit) {
        onBackPress = callback
    }

    fun setOnError(callback: (CometChatException) -> Unit) {
        onError = callback
        binding.callButtons.setOnError(callback)
    }

    fun setOnNewChatClick(callback: () -> Unit) {
        onNewChatClick = callback
    }

    fun setOnChatHistoryClick(callback: () -> Unit) {
        onChatHistoryClick = callback
    }

    fun setOnVideoCallClick(callback: ((User?, Group?) -> Unit)?) {
        onVideoCallClick = callback
        binding.callButtons.setOnVideoCallClick(callback)
    }

    fun setOnVoiceCallClick(callback: ((User?, Group?) -> Unit)?) {
        onVoiceCallClick = callback
        binding.callButtons.setOnVoiceCallClick(callback)
    }

    // ==================== Public Custom View Listener Setters ====================

    fun setLeadingViewListener(listener: MessageHeaderViewHolderListener) {
        leadingViewListener = listener
    }

    fun setTitleViewListener(listener: MessageHeaderViewHolderListener) {
        titleViewListener = listener
    }

    fun setSubtitleViewListener(listener: MessageHeaderViewHolderListener) {
        subtitleViewListener = listener
    }

    fun setTrailingViewListener(listener: MessageHeaderViewHolderListener) {
        trailingViewListener = listener
    }

    fun setAuxiliaryViewListener(listener: MessageHeaderViewHolderListener) {
        auxiliaryViewListener = listener
    }

    fun setItemViewListener(listener: MessageHeaderViewHolderListener) {
        itemViewListener = listener
    }

    // ==================== Public Formatter Setters ====================

    fun setLastSeenTextFormatter(formatter: (Context, User) -> String) {
        lastSeenTextFormatter = formatter
    }

    /**
     * Sets the date/time formatter callback for customizing how dates are displayed.
     * This provides granular control over different time periods (today, yesterday, etc.).
     *
     * @param formatter The DateTimeFormatterCallback for custom formatting, or null to use default.
     */
    fun setDateTimeFormatter(formatter: com.cometchat.uikit.kotlin.shared.interfaces.DateTimeFormatterCallback?) {
        dateTimeFormatter = formatter
    }

    /**
     * Gets the current date/time formatter callback.
     *
     * @return The current DateTimeFormatterCallback, or null if using default.
     */
    fun getDateTimeFormatter(): com.cometchat.uikit.kotlin.shared.interfaces.DateTimeFormatterCallback? = dateTimeFormatter

    // ==================== Public Menu Options Setter ====================

    fun setMenuOptions(options: List<CometChatPopupMenu.MenuItem>) {
        menuOptions = options
        binding.messageHeaderMenuIcon.visibility = if (options.isNotEmpty()) View.VISIBLE else View.GONE
    }

    /**
     * Sets the menu options for the message header popup menu.
     * This method provides API parity with the Java chatuikit module.
     * Delegates to [setMenuOptions].
     *
     * @param options A list of CometChatPopupMenu.MenuItem to be displayed in the menu.
     */
    fun setOptions(options: List<CometChatPopupMenu.MenuItem>) {
        setMenuOptions(options)
    }

    /**
     * Gets the current menu options.
     * This method provides API parity with the Java chatuikit module.
     *
     * @return A list of CometChatPopupMenu.MenuItem representing the options in the menu.
     */
    fun getOptions(): List<CometChatPopupMenu.MenuItem> {
        return menuOptions ?: emptyList()
    }

    // ==================== Public Style Methods ====================

    /**
     * Sets the style from a style object.
     */
    fun setStyle(style: CometChatMessageHeaderStyle) {
        this.style = style
        applyStyle()
    }

    /**
     * Sets the style from a style resource.
     */
    fun setStyle(@StyleRes styleRes: Int) {
        if (styleRes != 0) {
            val typedArray = context.theme.obtainStyledAttributes(
                styleRes, R.styleable.CometChatMessageHeader
            )
            // fromTypedArray handles recycling internally
            setStyle(CometChatMessageHeaderStyle.fromTypedArray(context, typedArray))
        }
    }

    // ==================== Getters (read from style object) ====================

    fun getHeaderBackgroundColor(): Int = style.backgroundColor
    fun getHeaderStrokeColor(): Int = style.strokeColor
    fun getHeaderStrokeWidth(): Int = style.strokeWidth
    fun getHeaderCornerRadius(): Int = style.cornerRadius
    fun getTitleTextColor(): Int = style.titleTextColor
    fun getTitleTextAppearance(): Int = style.titleTextAppearance
    fun getSubtitleTextColor(): Int = style.subtitleTextColor
    fun getSubtitleTextAppearance(): Int = style.subtitleTextAppearance
    fun getBackIcon(): Drawable? = style.backIcon
    fun getBackIconTint(): Int = style.backIconTint
    fun getMenuIcon(): Drawable? = style.menuIcon
    fun getMenuIconTint(): Int = style.menuIconTint
    fun getTypingIndicatorTextColor(): Int = style.typingIndicatorTextColor
    fun getTypingIndicatorTextAppearance(): Int = style.typingIndicatorTextAppearance
    fun getNewChatIcon(): Drawable? = style.newChatIcon
    fun getNewChatIconTint(): Int = style.newChatIconTint
    fun getChatHistoryIcon(): Drawable? = style.chatHistoryIcon
    fun getChatHistoryIconTint(): Int = style.chatHistoryIconTint
    fun getVideoCallIcon(): Drawable? = style.videoCallIcon
    fun getVideoCallIconTint(): Int = style.videoCallIconTint
    fun getVoiceCallIcon(): Drawable? = style.voiceCallIcon
    fun getVoiceCallIconTint(): Int = style.voiceCallIconTint

    // ==================== Setters (update style object + apply) ====================

    fun setHeaderBackgroundColor(@ColorInt color: Int) {
        style = style.copy(backgroundColor = color)
        if (color != 0) setCardBackgroundColor(color)
    }

    fun setHeaderStrokeColor(@ColorInt color: Int) {
        style = style.copy(strokeColor = color)
        if (color != 0) setStrokeColor(color)
    }

    fun setHeaderStrokeWidth(@Dimension width: Int) {
        style = style.copy(strokeWidth = width)
        if (width != 0) strokeWidth = width
    }

    fun setHeaderCornerRadius(@Dimension radius: Int) {
        style = style.copy(cornerRadius = radius)
        if (radius != 0) this.radius = radius.toFloat()
    }

    fun setTitleTextColor(@ColorInt color: Int) {
        style = style.copy(titleTextColor = color)
        if (color != 0) binding.tvMessageHeaderName.setTextColor(color)
    }

    fun setTitleTextAppearance(@StyleRes appearance: Int) {
        style = style.copy(titleTextAppearance = appearance)
        if (appearance != 0) binding.tvMessageHeaderName.setTextAppearance(appearance)
    }

    fun setSubtitleTextColor(@ColorInt color: Int) {
        style = style.copy(subtitleTextColor = color)
        if (color != 0) binding.tvMessageHeaderSubtitle.setTextColor(color)
    }

    fun setSubtitleTextAppearance(@StyleRes appearance: Int) {
        style = style.copy(subtitleTextAppearance = appearance)
        if (appearance != 0) binding.tvMessageHeaderSubtitle.setTextAppearance(appearance)
    }

    fun setBackIcon(icon: Drawable?) {
        style = style.copy(backIcon = icon)
        icon?.let { binding.ivMessageHeaderBack.setImageDrawable(it) }
    }

    fun setBackIconTint(@ColorInt color: Int) {
        style = style.copy(backIconTint = color)
        if (color != 0) binding.ivMessageHeaderBack.setColorFilter(color)
    }

    fun setMenuIcon(icon: Drawable?) {
        style = style.copy(menuIcon = icon)
        icon?.let { binding.messageHeaderMenuIcon.setImageDrawable(it) }
    }

    fun setMenuIconTint(@ColorInt color: Int) {
        style = style.copy(menuIconTint = color)
        if (color != 0) binding.messageHeaderMenuIcon.setColorFilter(color)
    }

    fun setTypingIndicatorTextColor(@ColorInt color: Int) {
        style = style.copy(typingIndicatorTextColor = color)
        if (color != 0) binding.tvMessageHeaderTypingIndicator.setTextColor(color)
    }

    fun setTypingIndicatorTextAppearance(@StyleRes appearance: Int) {
        style = style.copy(typingIndicatorTextAppearance = appearance)
        if (appearance != 0) binding.tvMessageHeaderTypingIndicator.setTextAppearance(appearance)
    }

    fun setNewChatIcon(icon: Drawable?) {
        style = style.copy(newChatIcon = icon)
        icon?.let { binding.ivNewChat.setImageDrawable(it) }
    }

    fun setNewChatIconTint(@ColorInt color: Int) {
        style = style.copy(newChatIconTint = color)
        if (color != 0) binding.ivNewChat.setColorFilter(color)
    }

    fun setChatHistoryIcon(icon: Drawable?) {
        style = style.copy(chatHistoryIcon = icon)
        icon?.let { binding.ivChatHistory.setImageDrawable(it) }
    }

    fun setChatHistoryIconTint(@ColorInt color: Int) {
        style = style.copy(chatHistoryIconTint = color)
        if (color != 0) binding.ivChatHistory.setColorFilter(color)
    }

    fun setVideoCallIcon(icon: Drawable?) {
        style = style.copy(videoCallIcon = icon)
        icon?.let { binding.callButtons.setVideoCallIcon(it) }
    }

    fun setVideoCallIconTint(@ColorInt color: Int) {
        style = style.copy(videoCallIconTint = color)
        if (color != 0) binding.callButtons.setVideoCallIconTint(color)
    }

    fun setVoiceCallIcon(icon: Drawable?) {
        style = style.copy(voiceCallIcon = icon)
        icon?.let { binding.callButtons.setVoiceCallIcon(it) }
    }

    fun setVoiceCallIconTint(@ColorInt color: Int) {
        style = style.copy(voiceCallIconTint = color)
        if (color != 0) binding.callButtons.setVoiceCallIconTint(color)
    }

    // ==================== Public ViewModel Setter ====================

    fun setViewModel(viewModel: CometChatMessageHeaderViewModel) {
        this.viewModel = viewModel
        isExternalViewModel = true
        startCollectingFlows()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewModel?.let { vm ->
            user?.let { vm.setUser(it) }
            group?.let { vm.setGroup(it) }
        }
    }

    override fun onDetachedFromWindow() {
        viewScope?.cancel()
        super.onDetachedFromWindow()
    }
}
