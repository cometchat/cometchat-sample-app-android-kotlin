package com.cometchat.uikit.kotlin.presentation.messageinformation.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.MessageReceipt
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.factory.CometChatMessageInformationViewModelFactory
import com.cometchat.uikit.core.state.MessageInformationUIState
import com.cometchat.uikit.core.viewmodel.CometChatMessageInformationViewModel
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.databinding.CometchatMessageInformationBinding
import com.cometchat.uikit.kotlin.presentation.messageinformation.adapter.MessageInformationAdapter
import com.cometchat.uikit.kotlin.presentation.messageinformation.style.CometChatMessageInformationStyle
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar.CometChatAvatarStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.BubbleFactory
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.CometChatMessageBubble
import com.cometchat.uikit.kotlin.presentation.shared.receipts.CometChatReceiptStyle
import com.cometchat.uikit.kotlin.presentation.shared.receipts.ReceiptStatus
import com.cometchat.uikit.kotlin.presentation.shared.shimmer.CometChatShimmerAdapter
import com.cometchat.uikit.kotlin.presentation.shared.shimmer.CometChatShimmerUtils
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.google.android.material.card.MaterialCardView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * CometChatMessageInformation displays detailed receipt information for a message.
 *
 * This component shows when a message was delivered and read, with different UI layouts
 * for user (one-to-one) and group conversations. It is designed to be used within a
 * BottomSheetDialogFragment.
 *
 * Per design doc: Overview section.
 *
 * Features:
 * - For USER conversations: Shows static read/delivered timestamps from the message
 * - For GROUP conversations: Fetches and displays a list of receipts from all group members
 * - Supports real-time receipt updates via CometChatMessageEvents
 * - Displays the message bubble at the top for context (rendered internally using CometChatMessageBubble)
 *
 * Usage in XML:
 * ```xml
 * <com.cometchat.uikit.kotlin.presentation.messageinformation.ui.CometChatMessageInformation
 *     android:id="@+id/messageInformation"
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent"
 *     app:cometchatMessageInformationStyle="@style/CometChatMessageInformation" />
 * ```
 *
 * Usage in Kotlin:
 * ```kotlin
 * val messageInfo = CometChatMessageInformation(context)
 * messageInfo.setBubbleFactories(bubbleFactories) // Optional: for custom bubble rendering
 * messageInfo.setMessage(message)
 * ```
 */
class CometChatMessageInformation @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.cometchatMessageInformationStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = CometChatMessageInformation::class.java.simpleName
    }

    // View Binding
    private val binding: CometchatMessageInformationBinding

    // ViewModel (shared from chatuikit-core)
    private var viewModel: CometChatMessageInformationViewModel? = null
    private var isExternalViewModel: Boolean = false

    // Coroutine scope for collecting flows
    private var viewScope: CoroutineScope? = null

    // Data
    private var message: BaseMessage? = null
    private var conversationType: String? = null

    // Adapter for group receipts
    private lateinit var adapter: MessageInformationAdapter
    private lateinit var shimmerAdapter: CometChatShimmerAdapter

    // Callbacks
    private var onError: ((CometChatException) -> Unit)? = null

    // Bubble factories for internal bubble rendering
    private var bubbleFactories: Map<String, BubbleFactory> = emptyMap()

    // Text formatters for mentions and markdown rendering in the message bubble
    private var textFormatters: List<com.cometchat.uikit.kotlin.shared.formatters.CometChatTextFormatter> = emptyList()

    // Internal message bubble component
    private var messageBubble: CometChatMessageBubble? = null

    // Visibility controls
    private var toolBarVisibility = View.VISIBLE

    // Single style object
    private var style: CometChatMessageInformationStyle = CometChatMessageInformationStyle()

    init {
        binding = CometchatMessageInformationBinding.inflate(
            LayoutInflater.from(context), this, true
        )
        Utils.initMaterialCard(this)
        setupRecyclerViews()
        applyStyleAttributes(attrs, defStyleAttr)
        setupOverlay()
        initViewModel()
    }

    /**
     * Sets up RecyclerViews for receipts and shimmer.
     */
    private fun setupRecyclerViews() {
        // Group receipts adapter
        adapter = MessageInformationAdapter(context)
        binding.messageInfoRecyclerViewGroup.layoutManager = LinearLayoutManager(context)
        binding.messageInfoRecyclerViewGroup.adapter = adapter

        // Shimmer adapter
        shimmerAdapter = CometChatShimmerAdapter(30, R.layout.shimmer_cometchat_message_information)
        binding.shimmerRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.shimmerRecyclerView.adapter = shimmerAdapter
    }

    /**
     * Sets up the overlay to block touches on the message bubble.
     * Per design doc: Message Bubble Overlay section.
     */
    private fun setupOverlay() {
        binding.messageBubbleParentLayout.doOnLayout {
            val width = it.width
            val height = it.height
            binding.messageBubbleViewOverlay.layoutParams = binding.messageBubbleViewOverlay.layoutParams.apply {
                this.width = width
                this.height = height
            }
        }
        binding.messageBubbleViewOverlay.setOnClickListener { /* Block touches */ }
    }

    /**
     * Applies style attributes from XML using the style class factory method.
     */
    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        var typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatMessageInformation, defStyleAttr, 0
        )
        val styleResId = typedArray.getResourceId(
            R.styleable.CometChatMessageInformation_cometchatMessageInformationStyle, 0
        )
        typedArray.recycle()

        typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatMessageInformation, defStyleAttr, styleResId
        )
        style = CometChatMessageInformationStyle.fromTypedArray(context, typedArray)
        applyStyle()
    }

    /**
     * Applies all style properties from the style object to views.
     */
    private fun applyStyle() {
        // Container styling
        if (style.backgroundColor != 0) setCardBackgroundColor(style.backgroundColor)
        // Always set stroke width to 0 for bottom sheet (no border)
        strokeWidth = 0
        if (style.cornerRadius != 0) {
            // Apply corner radius only to top corners
            shapeAppearanceModel = ShapeAppearanceModel.builder()
                .setTopLeftCorner(CornerFamily.ROUNDED, style.cornerRadius.toFloat())
                .setTopRightCorner(CornerFamily.ROUNDED, style.cornerRadius.toFloat())
                .setBottomLeftCorner(CornerFamily.ROUNDED, 0f)
                .setBottomRightCorner(CornerFamily.ROUNDED, 0f)
                .build()
        }

        // Drag handle styling - use border color or a default gray
        val dragHandleColor = if (style.strokeColor != 0) {
            style.strokeColor
        } else {
            CometChatTheme.getBorderColorLight(context)
        }
        binding.dragHandle.setBackgroundColor(dragHandleColor)

        // Background highlight color for message bubble section
        if (style.backgroundHighlightColor != 0) {
            binding.messageBubbleParentLayout.setBackgroundColor(style.backgroundHighlightColor)
        }

        // Title styling
        if (style.titleTextColor != 0) binding.tvToolBarTitle.setTextColor(style.titleTextColor)
        if (style.titleTextAppearance != 0) binding.tvToolBarTitle.setTextAppearance(style.titleTextAppearance)

        // User receipt view styling
        applyUserReceiptStyle()

        // Apply style to adapter for group receipts
        adapter.setStyle(style)

        // Receipt icons styling
        style.messageReceiptStyle?.let { receiptStyle ->
            binding.messageReceiptRead.setStyle(receiptStyle)
            binding.messageReceiptDelivered.setStyle(receiptStyle)
        }
    }

    /**
     * Applies styling to user receipt view elements.
     */
    private fun applyUserReceiptStyle() {
        // Get default text color for timestamps
        val defaultTextColor = CometChatTheme.getTextColorSecondary(context)
        
        // Read label styling (uses itemReadTextAppearance/Color)
        if (style.itemReadTextAppearance != 0) {
            binding.tvReadReceiptUser.setTextAppearance(style.itemReadTextAppearance)
        }
        val readTextColor = if (style.itemReadTextColor != 0) style.itemReadTextColor else defaultTextColor
        binding.tvReadReceiptUser.setTextColor(readTextColor)

        // Read timestamp styling - always apply color
        if (style.itemReadDateTextAppearance != 0) {
            binding.tvReadTimestampUser.setTextAppearance(style.itemReadDateTextAppearance)
        }
        val readDateTextColor = if (style.itemReadDateTextColor != 0) style.itemReadDateTextColor else defaultTextColor
        binding.tvReadTimestampUser.setTextColor(readDateTextColor)

        // Delivered label styling
        if (style.itemDeliveredTextAppearance != 0) {
            binding.tvDeliveredReceiptUser.setTextAppearance(style.itemDeliveredTextAppearance)
        }
        val deliveredTextColor = if (style.itemDeliveredTextColor != 0) style.itemDeliveredTextColor else defaultTextColor
        binding.tvDeliveredReceiptUser.setTextColor(deliveredTextColor)

        // Delivered timestamp styling - always apply color
        if (style.itemDeliveredDateTextAppearance != 0) {
            binding.tvDeliveredTimestampUser.setTextAppearance(style.itemDeliveredDateTextAppearance)
        }
        val deliveredDateTextColor = if (style.itemDeliveredDateTextColor != 0) style.itemDeliveredDateTextColor else defaultTextColor
        binding.tvDeliveredTimestampUser.setTextColor(deliveredDateTextColor)
    }

    /**
     * Initializes the ViewModel.
     */
    private fun initViewModel() {
        if (!isExternalViewModel) {
            viewModel = CometChatMessageInformationViewModelFactory()
                .create(CometChatMessageInformationViewModel::class.java)
        }
        startCollectingFlows()
    }

    /**
     * Starts collecting flows from the ViewModel.
     */
    private fun startCollectingFlows() {
        viewScope?.cancel()
        viewScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

        viewScope?.launch {
            viewModel?.state?.collectLatest { state ->
                handleStateChange(state)
            }
        }

        viewScope?.launch {
            viewModel?.listData?.collectLatest { receipts ->
                setList(receipts)
            }
        }

        viewScope?.launch {
            viewModel?.updateReceipt?.collectLatest { index ->
                index?.let { notifyUpdateReceipt(it) }
            }
        }

        viewScope?.launch {
            viewModel?.addReceipt?.collectLatest { index ->
                index?.let { notifyAddReceipt(it) }
            }
        }

        viewScope?.launch {
            viewModel?.exception?.collectLatest { exception ->
                exception?.let { onError?.invoke(it) }
            }
        }
    }

    /**
     * Handles state changes from the ViewModel.
     * Per design doc: State Handling in UI section.
     */
    private fun handleStateChange(state: MessageInformationUIState?) {
        when (state) {
            is MessageInformationUIState.Loading -> handleLoadingState()
            is MessageInformationUIState.Loaded -> handleLoadedState()
            is MessageInformationUIState.Empty -> handleEmptyState()
            is MessageInformationUIState.Error -> handleErrorState()
            null -> handleLoadingState()
        }
    }

    /**
     * Handles LOADING state.
     */
    private fun handleLoadingState() {
        binding.shimmerEffectFrame.visibility = View.VISIBLE
        binding.shimmerEffectFrame.setShimmer(CometChatShimmerUtils.getCometChatShimmerConfig(context))
        binding.shimmerEffectFrame.startShimmer()
        binding.messageReceiptsUser.visibility = View.GONE
        binding.messageInfoRecyclerViewGroup.visibility = View.GONE
    }

    /**
     * Handles LOADED state.
     */
    private fun handleLoadedState() {
        binding.shimmerEffectFrame.stopShimmer()
        binding.shimmerEffectFrame.visibility = View.GONE

        if (conversationType == CometChatConstants.RECEIVER_TYPE_USER) {
            binding.messageReceiptsUser.visibility = View.VISIBLE
            binding.messageInfoRecyclerViewGroup.visibility = View.GONE
        } else {
            binding.messageReceiptsUser.visibility = View.GONE
            binding.messageInfoRecyclerViewGroup.visibility = View.VISIBLE
        }
    }

    /**
     * Handles EMPTY state.
     */
    private fun handleEmptyState() {
        binding.shimmerEffectFrame.stopShimmer()
        binding.shimmerEffectFrame.visibility = View.GONE
        binding.messageInfoRecyclerViewGroup.visibility = View.GONE

        // Show user receipt view only for USER conversations
        binding.messageReceiptsUser.visibility = if (conversationType == CometChatConstants.RECEIVER_TYPE_USER) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    /**
     * Handles ERROR state - treat as empty.
     */
    private fun handleErrorState() {
        handleEmptyState()
    }

    /**
     * Sets the receipt list.
     */
    private fun setList(receipts: List<MessageReceipt>) {
        adapter.submitList(receipts.toList())
    }

    /**
     * Notifies adapter of updated receipt.
     */
    private fun notifyUpdateReceipt(index: Int) {
        adapter.notifyItemChanged(index)
    }

    /**
     * Notifies adapter of added receipt.
     */
    private fun notifyAddReceipt(index: Int) {
        adapter.notifyItemInserted(index)
    }

    /**
     * Updates the user receipt view with message data.
     * Per design doc: User Conversation Receipt Display section.
     */
    private fun updateUserReceiptView(message: BaseMessage) {
        // Set receipt icons
        binding.messageReceiptRead.setReceipt(ReceiptStatus.READ)
        binding.messageReceiptDelivered.setReceipt(ReceiptStatus.DELIVERED)

        // Get default text color for timestamps
        val defaultTextColor = CometChatTheme.getTextColorSecondary(context)

        // Read timestamp - show if readAt > 0
        if (message.readAt > 0) {
            binding.tvReadTimestampUser.visibility = View.VISIBLE
            binding.tvReadTimestampUser.text = formatDateTime(message.readAt * 1000)
            // Ensure text color is applied
            val readDateColor = if (style.itemReadDateTextColor != 0) style.itemReadDateTextColor else defaultTextColor
            binding.tvReadTimestampUser.setTextColor(readDateColor)
        } else {
            binding.tvReadTimestampUser.visibility = View.GONE
        }

        // Delivered timestamp - show if deliveredAt > 0
        if (message.deliveredAt > 0) {
            binding.tvDeliveredTimestampUser.visibility = View.VISIBLE
            binding.tvDeliveredTimestampUser.text = formatDateTime(message.deliveredAt * 1000)
            // Ensure text color is applied
            val deliveredDateColor = if (style.itemDeliveredDateTextColor != 0) style.itemDeliveredDateTextColor else defaultTextColor
            binding.tvDeliveredTimestampUser.setTextColor(deliveredDateColor)
        } else {
            binding.tvDeliveredTimestampUser.visibility = View.GONE
        }
    }

    /**
     * Formats timestamp to "dd/M/yyyy, h:mm a" format.
     * Per design doc: Date/Time Formatting section.
     */
    private fun formatDateTime(milliseconds: Long): String {
        val sdf = SimpleDateFormat("dd/M/yyyy, h:mm a", Locale.getDefault())
        return sdf.format(Date(milliseconds))
    }

    // ==================== Public API ====================

    /**
     * Sets the message to display information for.
     * This initializes the component and triggers data fetching.
     *
     * @param message The message to display information for
     */
    fun setMessage(message: BaseMessage) {
        this.message = message

        // Determine conversation type
        conversationType = when (message.receiverType.lowercase()) {
            CometChatConstants.RECEIVER_TYPE_USER -> CometChatConstants.RECEIVER_TYPE_USER
            CometChatConstants.RECEIVER_TYPE_GROUP -> CometChatConstants.RECEIVER_TYPE_GROUP
            else -> null
        }

        // For USER conversations, show the receipt view immediately and update it
        // (no need to wait for ViewModel state since data comes from message object)
        if (conversationType == CometChatConstants.RECEIVER_TYPE_USER) {
            binding.shimmerEffectFrame.visibility = View.GONE
            binding.messageInfoRecyclerViewGroup.visibility = View.GONE
            binding.messageReceiptsUser.visibility = View.VISIBLE
            updateUserReceiptView(message)
        }

        // Render message bubble internally using CometChatMessageBubble
        renderMessageBubble(message)

        // Initialize ViewModel with message
        viewModel?.setMessage(message)
    }

    /**
     * Renders the message bubble internally using CometChatMessageBubble.
     * The bubble is always displayed with RIGHT alignment (outgoing style).
     */
    private fun renderMessageBubble(message: BaseMessage) {
        // Only create the bubble once, don't recreate on every call
        if (messageBubble == null) {
            messageBubble = CometChatMessageBubble(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                // Set text formatters if any are provided
                if (textFormatters.isNotEmpty()) {
                    setTextFormatters(textFormatters)
                }
                // Set alignment to STANDARD (right-aligned for outgoing)
                setAlignment(UIKitConstants.MessageListAlignment.STANDARD)
                // Hide avatar in message info view
                setAvatarVisibility(View.GONE)
            }
            
            // Add the bubble to the container
            binding.messageBubbleView.removeAllViews()
            binding.messageBubbleView.addView(messageBubble)
        }
        
        // Look up the appropriate factory from the map based on message type
        if (bubbleFactories.isNotEmpty()) {
            val factoryKey = BubbleFactory.getFactoryKey(message)
            val factory = bubbleFactories[factoryKey]
            messageBubble?.setBubbleFactory(factory)
        }
        
        // Set the message to render with RIGHT alignment (outgoing style)
        messageBubble?.setMessage(message, UIKitConstants.MessageBubbleAlignment.RIGHT)
    }

    /**
     * Gets the current message.
     */
    fun getMessage(): BaseMessage? = message

    /**
     * Sets the bubble factories for rendering the message bubble internally.
     * 
     * The message bubble is rendered using CometChatMessageBubble with these factories.
     * If no factories are provided, the default InternalContentRenderer handles rendering.
     * 
     * When a message is rendered, the appropriate factory is looked up from this map
     * using the message's factory key (category_type).
     *
     * @param factories Map of factory key to BubbleFactory
     */
    fun setBubbleFactories(factories: Map<String, BubbleFactory>) {
        bubbleFactories = factories
        // Factory will be applied when renderMessageBubble is called with a message
    }

    /**
     * Gets the current bubble factories.
     */
    fun getBubbleFactories(): Map<String, BubbleFactory> = bubbleFactories

    /**
     * Sets the text formatters for rendering mentions and markdown in the message bubble.
     *
     * Text formatters are used to customize how message text is rendered,
     * including mentions, links, markdown, and other text transformations.
     *
     * @param formatters The list of text formatters to use for text rendering
     */
    fun setTextFormatters(formatters: List<com.cometchat.uikit.kotlin.shared.formatters.CometChatTextFormatter>) {
        textFormatters = formatters
        // Update the message bubble if already created
        messageBubble?.setTextFormatters(formatters)
    }

    /**
     * Gets the current text formatters.
     *
     * @return The list of text formatters, or an empty list if none are set
     */
    fun getTextFormatters(): List<com.cometchat.uikit.kotlin.shared.formatters.CometChatTextFormatter> = textFormatters

    /**
     * Sets the toolbar title text.
     *
     * @param title The title text to display
     */
    fun setToolBarTitleText(title: String) {
        binding.tvToolBarTitle.text = title
    }

    /**
     * Hides or shows the toolbar.
     *
     * @param hide True to hide the toolbar, false to show it
     */
    fun hideToolBar(hide: Boolean) {
        toolBarVisibility = if (hide) View.GONE else View.VISIBLE
        binding.toolBarView.visibility = toolBarVisibility
    }

    /**
     * Sets the error callback.
     *
     * @param callback Function to call when an error occurs
     */
    fun setOnError(callback: (CometChatException) -> Unit) {
        onError = callback
    }

    /**
     * Adds the real-time event listener.
     * Call this when the component becomes visible.
     */
    fun addListener() {
        viewModel?.addListener()
    }

    /**
     * Removes the real-time event listener.
     * Call this when the component is dismissed.
     */
    fun removeListener() {
        viewModel?.removeListener()
    }

    // ==================== Style Methods ====================

    /**
     * Sets the style from a style object.
     */
    fun setStyle(style: CometChatMessageInformationStyle) {
        this.style = style
        applyStyle()
    }

    /**
     * Sets the style from a style resource.
     */
    fun setStyle(@StyleRes styleRes: Int) {
        if (styleRes != 0) {
            val typedArray = context.theme.obtainStyledAttributes(
                styleRes, R.styleable.CometChatMessageInformation
            )
            setStyle(CometChatMessageInformationStyle.fromTypedArray(context, typedArray))
        }
    }

    // ==================== Individual Style Getters ====================

    fun getInfoBackgroundColor(): Int = style.backgroundColor
    fun getBackgroundHighlightColor(): Int = style.backgroundHighlightColor
    fun getInfoCornerRadius(): Int = style.cornerRadius
    fun getInfoStrokeWidth(): Int = style.strokeWidth
    fun getInfoStrokeColor(): Int = style.strokeColor
    fun getTitleTextAppearance(): Int = style.titleTextAppearance
    fun getTitleTextColor(): Int = style.titleTextColor
    fun getItemNameTextAppearance(): Int = style.itemNameTextAppearance
    fun getItemNameTextColor(): Int = style.itemNameTextColor
    fun getItemReadTextAppearance(): Int = style.itemReadTextAppearance
    fun getItemReadTextColor(): Int = style.itemReadTextColor
    fun getItemReadDateTextAppearance(): Int = style.itemReadDateTextAppearance
    fun getItemReadDateTextColor(): Int = style.itemReadDateTextColor
    fun getItemDeliveredTextAppearance(): Int = style.itemDeliveredTextAppearance
    fun getItemDeliveredTextColor(): Int = style.itemDeliveredTextColor
    fun getItemDeliveredDateTextAppearance(): Int = style.itemDeliveredDateTextAppearance
    fun getItemDeliveredDateTextColor(): Int = style.itemDeliveredDateTextColor
    fun getItemAvatarStyle(): CometChatAvatarStyle? = style.itemAvatarStyle
    fun getMessageReceiptStyle(): CometChatReceiptStyle? = style.messageReceiptStyle

    // ==================== Individual Style Setters ====================

    fun setInfoBackgroundColor(@ColorInt color: Int) {
        style = style.copy(backgroundColor = color)
        if (color != 0) setCardBackgroundColor(color)
    }

    fun setBackgroundHighlightColor(@ColorInt color: Int) {
        style = style.copy(backgroundHighlightColor = color)
        if (color != 0) binding.messageBubbleParentLayout.setBackgroundColor(color)
    }

    fun setInfoCornerRadius(@Dimension radius: Int) {
        style = style.copy(cornerRadius = radius)
        if (radius != 0) {
            shapeAppearanceModel = ShapeAppearanceModel.builder()
                .setTopLeftCorner(CornerFamily.ROUNDED, radius.toFloat())
                .setTopRightCorner(CornerFamily.ROUNDED, radius.toFloat())
                .setBottomLeftCorner(CornerFamily.ROUNDED, 0f)
                .setBottomRightCorner(CornerFamily.ROUNDED, 0f)
                .build()
        }
    }

    fun setInfoStrokeWidth(@Dimension width: Int) {
        style = style.copy(strokeWidth = width)
        if (width != 0) strokeWidth = width
    }

    fun setInfoStrokeColor(@ColorInt color: Int) {
        style = style.copy(strokeColor = color)
        if (color != 0) setStrokeColor(color)
    }

    fun setTitleTextAppearance(@StyleRes appearance: Int) {
        style = style.copy(titleTextAppearance = appearance)
        if (appearance != 0) binding.tvToolBarTitle.setTextAppearance(appearance)
    }

    fun setTitleTextColor(@ColorInt color: Int) {
        style = style.copy(titleTextColor = color)
        if (color != 0) binding.tvToolBarTitle.setTextColor(color)
    }

    fun setItemNameTextAppearance(@StyleRes appearance: Int) {
        style = style.copy(itemNameTextAppearance = appearance)
        adapter.setItemNameTextAppearance(appearance)
    }

    fun setItemNameTextColor(@ColorInt color: Int) {
        style = style.copy(itemNameTextColor = color)
        adapter.setItemNameTextColor(color)
    }

    fun setItemReadTextAppearance(@StyleRes appearance: Int) {
        style = style.copy(itemReadTextAppearance = appearance)
        if (appearance != 0) binding.tvReadReceiptUser.setTextAppearance(appearance)
        adapter.setItemReadTextAppearance(appearance)
    }

    fun setItemReadTextColor(@ColorInt color: Int) {
        style = style.copy(itemReadTextColor = color)
        if (color != 0) binding.tvReadReceiptUser.setTextColor(color)
        adapter.setItemReadTextColor(color)
    }

    fun setItemReadDateTextAppearance(@StyleRes appearance: Int) {
        style = style.copy(itemReadDateTextAppearance = appearance)
        if (appearance != 0) binding.tvReadTimestampUser.setTextAppearance(appearance)
        adapter.setItemReadDateTextAppearance(appearance)
    }

    fun setItemReadDateTextColor(@ColorInt color: Int) {
        style = style.copy(itemReadDateTextColor = color)
        if (color != 0) binding.tvReadTimestampUser.setTextColor(color)
        adapter.setItemReadDateTextColor(color)
    }

    fun setItemDeliveredTextAppearance(@StyleRes appearance: Int) {
        style = style.copy(itemDeliveredTextAppearance = appearance)
        if (appearance != 0) binding.tvDeliveredReceiptUser.setTextAppearance(appearance)
        adapter.setItemDeliveredTextAppearance(appearance)
    }

    fun setItemDeliveredTextColor(@ColorInt color: Int) {
        style = style.copy(itemDeliveredTextColor = color)
        if (color != 0) binding.tvDeliveredReceiptUser.setTextColor(color)
        adapter.setItemDeliveredTextColor(color)
    }

    fun setItemDeliveredDateTextAppearance(@StyleRes appearance: Int) {
        style = style.copy(itemDeliveredDateTextAppearance = appearance)
        if (appearance != 0) binding.tvDeliveredTimestampUser.setTextAppearance(appearance)
        adapter.setItemDeliveredDateTextAppearance(appearance)
    }

    fun setItemDeliveredDateTextColor(@ColorInt color: Int) {
        style = style.copy(itemDeliveredDateTextColor = color)
        if (color != 0) binding.tvDeliveredTimestampUser.setTextColor(color)
        adapter.setItemDeliveredDateTextColor(color)
    }

    fun setItemAvatarStyle(avatarStyle: CometChatAvatarStyle?) {
        style = style.copy(itemAvatarStyle = avatarStyle)
        adapter.setAvatarStyle(avatarStyle)
    }

    fun setMessageReceiptStyle(receiptStyle: CometChatReceiptStyle?) {
        style = style.copy(messageReceiptStyle = receiptStyle)
        receiptStyle?.let {
            binding.messageReceiptRead.setStyle(it)
            binding.messageReceiptDelivered.setStyle(it)
        }
    }

    // ==================== ViewModel Setter ====================

    /**
     * Sets an external ViewModel.
     * Use this for testing or when sharing ViewModel between components.
     */
    fun setViewModel(viewModel: CometChatMessageInformationViewModel) {
        this.viewModel = viewModel
        isExternalViewModel = true
        startCollectingFlows()
    }

    // ==================== Lifecycle ====================

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewModel?.addListener()
    }

    override fun onDetachedFromWindow() {
        viewScope?.cancel()
        viewModel?.removeListener()
        super.onDetachedFromWindow()
    }
}
