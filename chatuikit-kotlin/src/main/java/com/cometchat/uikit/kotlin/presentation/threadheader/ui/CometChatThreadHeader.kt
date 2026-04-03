package com.cometchat.uikit.kotlin.presentation.threadheader.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.core.constants.UIKitConstants.MessageListAlignment
import com.cometchat.uikit.core.viewmodel.CometChatThreadHeaderViewModel
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.databinding.CometchatThreadHeaderBinding
import com.cometchat.uikit.kotlin.presentation.messagelist.adapter.MessageAdapter
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.BubbleFactory
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.CometChatMessageBubbleStyle
import com.cometchat.uikit.kotlin.presentation.threadheader.style.CometChatThreadHeaderStyle
import com.cometchat.uikit.kotlin.shared.formatters.CometChatMentionsFormatter
import com.cometchat.uikit.kotlin.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

/**
 * CometChatThreadHeader displays the parent message in a threaded conversation view.
 *
 * This component provides context for users viewing and composing thread replies by showing
 * the original message that started the thread, along with a reply count indicator.
 *
 * ## Features
 * - Displays parent message using CometChatMessageBubble
 * - Shows reply count with proper formatting ("1 Reply" / "N Replies")
 * - Real-time updates for message edits, reactions, and new replies
 * - Extensive style customization
 * - Support for text formatters (mentions, markdown)
 *
 * ## Usage in XML
 * ```xml
 * <com.cometchat.uikit.kotlin.presentation.threadheader.ui.CometChatThreadHeader
 *     android:id="@+id/threadHeader"
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:cometchatThreadHeaderStyle="@style/CometChatThreadHeader" />
 * ```
 *
 * ## Usage in Kotlin
 * ```kotlin
 * val threadHeader = CometChatThreadHeader(context)
 * threadHeader.setParentMessage(parentMessage)
 * ```
 *
 * @see ThreadHeaderViewModel
 * @see CometChatThreadHeaderStyle
 */
class CometChatThreadHeader @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.cometchatThreadHeaderStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = CometChatThreadHeader::class.java.simpleName
    }

    // ==================== View Binding ====================

    private val binding: CometchatThreadHeaderBinding

    // ==================== ViewModel ====================

    private var viewModel: CometChatThreadHeaderViewModel? = null
    private var isExternalViewModel: Boolean = false

    // ==================== Lifecycle ====================

    private var lifecycleOwner: LifecycleOwner? = null
    private var viewScope: CoroutineScope? = null
    private var isDetachedFromWindow: Boolean = false

    // ==================== State ====================

    private var parentMessage: BaseMessage? = null
    private var style: CometChatThreadHeaderStyle = CometChatThreadHeaderStyle()

    // ==================== Visibility Flags ====================

    private var reactionVisibility: Int = View.VISIBLE
    private var avatarVisibility: Int = View.VISIBLE
    private var receiptsVisibility: Int = View.VISIBLE
    private var replyCountVisibility: Int = View.VISIBLE
    private var replyCountBarVisibility: Int = View.VISIBLE

    // ==================== Alignment ====================

    private var alignment: MessageListAlignment = MessageListAlignment.STANDARD

    // ==================== Max Height ====================

    private var maxHeightLimit: Int = 0

    // ==================== Text Formatters ====================

    private var textFormatters: List<CometChatTextFormatter>? = null
    private var mentionsFormatter: CometChatMentionsFormatter? = null
    private var timeFormat: SimpleDateFormat? = null

    // ==================== Bubble Margins ====================

    private var leftBubbleMargin: IntArray? = null  // [top, bottom, left, right]
    private var rightBubbleMargin: IntArray? = null // [top, bottom, left, right]

    // ==================== Message Adapter ====================

    private var messageAdapter: MessageAdapter? = null

    // ==================== Initialization ====================

    init {
        // Inflate the layout binding
        binding = CometchatThreadHeaderBinding.inflate(LayoutInflater.from(context), this, true)

        // Initialize MaterialCard properties (no elevation, no stroke)
        Utils.initMaterialCard(this)

        // Process MentionsFormatter from DataSource
        processMentionsFormatter()

        // Create and configure MessageAdapter
        initializeMessageAdapter()

        // Initialize ViewModel
        initViewModel()

        // Apply style attributes from XML
        applyStyleAttributes(attrs, defStyleAttr)

        // Setup RecyclerView with adapter
        setupRecyclerView()
    }

    /**
     * Initializes the ViewModel and lifecycle owner.
     */
    private fun initViewModel() {
        if (!isExternalViewModel) {
            viewModel = CometChatThreadHeaderViewModel()
        }
        lifecycleOwner = Utils.getLifecycleOwner(context)
        
        // Attach observers immediately if lifecycle owner is available
        // This ensures LiveData updates are observed even before onAttachedToWindow
        if (lifecycleOwner != null) {
            attachObservers()
        }
    }

    /**
     * Processes the MentionsFormatter from DataSource.
     * Creates a CometChatMentionsFormatter and adds it to the text formatters list.
     */
    private fun processMentionsFormatter() {
        // Create mentions formatter
        mentionsFormatter = CometChatMentionsFormatter(context)
        
        // Add to formatters list
        val formatters = mutableListOf<CometChatTextFormatter>()
        mentionsFormatter?.let { formatters.add(it) }
        textFormatters = formatters
    }

    /**
     * Initializes the MessageAdapter with configuration for thread header.
     * - Creates MessageAdapter
     * - Sets hideThreadView = true to hide thread indicators on parent bubble
     * - Configures text formatters
     */
    private fun initializeMessageAdapter() {
        messageAdapter = MessageAdapter(context).apply {
            // Hide thread view on parent message bubble (thread header shows the parent, not thread replies)
            hideThreadView = true
            
            // Apply text formatters
            textFormatters?.let { this.textFormatters = it }
        }
    }

    /**
     * Sets up the RecyclerView with LinearLayoutManager and adapter.
     */
    private fun setupRecyclerView() {
        binding.rvParentBubbleView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = messageAdapter
            // Disable item animator to prevent blinking on updates
            itemAnimator = null
        }
    }

    /**
     * Applies style attributes from XML using the style class factory method.
     */
    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        var typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatThreadHeader, defStyleAttr, 0
        )
        val styleResId = typedArray.getResourceId(
            R.styleable.CometChatThreadHeader_cometchatThreadHeaderStyle, 0
        )
        typedArray.recycle()

        typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatThreadHeader, defStyleAttr, styleResId
        )
        // fromTypedArray handles recycling internally
        style = CometChatThreadHeaderStyle.fromTypedArray(context, typedArray)
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
        style.backgroundDrawable?.let { background = it }

        // Reply count styling
        if (style.replyCountBackgroundColor != 0) {
            binding.repliesLayout.setBackgroundColor(style.replyCountBackgroundColor)
        }
        if (style.replyCountTextColor != 0) {
            binding.tvReplies.setTextColor(style.replyCountTextColor)
        }
        if (style.replyCountTextAppearance != 0) {
            binding.tvReplies.setTextAppearance(style.replyCountTextAppearance)
        }
    }

    // ==================== Max Height Constraint ====================

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightSpec = heightMeasureSpec
        if (maxHeightLimit > 0) {
            val heightSize = MeasureSpec.getSize(heightMeasureSpec)
            val heightMode = MeasureSpec.getMode(heightMeasureSpec)
            
            // If the measured height exceeds max limit, constrain it
            if (heightSize > maxHeightLimit || heightMode == MeasureSpec.UNSPECIFIED) {
                // Use AT_MOST mode to allow content to be smaller but not exceed max
                heightSpec = MeasureSpec.makeMeasureSpec(maxHeightLimit, MeasureSpec.AT_MOST)
            }
        }
        super.onMeasure(widthMeasureSpec, heightSpec)
    }

    // ==================== Lifecycle Methods ====================

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // Always attach observers when attached to window
        attachObservers()
        viewModel?.addListener()
        viewModel?.addLocalEventListeners()
        isDetachedFromWindow = false
    }

    override fun onDetachedFromWindow() {
        viewModel?.removeListener()
        disposeObservers()
        isDetachedFromWindow = true
        super.onDetachedFromWindow()
    }

    /**
     * Attaches observers to ViewModel LiveData.
     */
    private fun attachObservers() {
        viewScope?.cancel()
        viewScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

        lifecycleOwner?.let { owner ->
            viewModel?.parentMessageListLiveData?.observe(owner) { messages ->
                updateMessage(messages)
            }
            viewModel?.replyCount?.observe(owner) { count ->
                updateReplyCount(count)
            }
        }
    }

    /**
     * Removes observers from ViewModel LiveData.
     */
    private fun disposeObservers() {
        viewScope?.cancel()
        viewScope = null
        lifecycleOwner?.let { owner ->
            viewModel?.parentMessageListLiveData?.removeObservers(owner)
            viewModel?.replyCount?.removeObservers(owner)
        }
    }

    // ==================== Update Methods ====================

    /**
     * Updates the message display by submitting the message list to the adapter.
     * 
     * @param messages The list of messages to display (typically contains only the parent message)
     */
    private fun updateMessage(messages: List<BaseMessage>) {
        messageAdapter?.setMessages(messages)
    }

    /**
     * Updates the reply count display.
     * Stub - will be implemented in Task 4.5
     */
    private fun updateReplyCount(count: Int) {
        // TODO: Implement in Task 4.5 - Format and display reply count
        val text = when {
            count == 0 -> "0 ${context.getString(R.string.cometchat_replies)}"
            count == 1 -> "1 ${context.getString(R.string.cometchat_reply)}"
            else -> "$count ${context.getString(R.string.cometchat_replies)}"
        }
        binding.tvReplies.text = text
    }

    // ==================== Public API - Parent Message ====================

    /**
     * Sets the parent message for the thread header.
     *
     * This method:
     * 1. Stores the parent message reference
     * 2. Updates the ViewModel with the parent message
     * 3. Processes and applies text formatters to the adapter
     * 4. Determines message alignment based on sender (incoming vs outgoing)
     * 5. Applies the alignment to the adapter
     *
     * @param message The parent message to display
     */
    fun setParentMessage(message: BaseMessage) {
        this.parentMessage = message
        viewModel?.setParentMessage(message)
        
        // Process and apply text formatters to the adapter
        processFormatters()
        
        // Determine alignment based on sender and apply to adapter
        determineAndApplyAlignment(message)
    }
    
    /**
     * Processes text formatters and applies them to the message adapter.
     * This ensures mentions, markdown, and other text transformations are rendered correctly.
     */
    private fun processFormatters() {
        textFormatters?.let { formatters ->
            messageAdapter?.textFormatters = formatters
        }
    }
    
    /**
     * Determines the message alignment based on the sender and applies it to the adapter.
     *
     * Alignment logic:
     * - STANDARD alignment: incoming messages (from other users) = LEFT, outgoing messages (from logged-in user) = RIGHT
     * - LEFT_ALIGNED alignment: all messages are aligned to the LEFT
     *
     * @param message The message to determine alignment for
     */
    private fun determineAndApplyAlignment(message: BaseMessage) {
        // If alignment is explicitly set to LEFT_ALIGNED, use that
        if (alignment == MessageListAlignment.LEFT_ALIGNED) {
            messageAdapter?.listAlignment = MessageListAlignment.LEFT_ALIGNED
            return
        }
        
        // For STANDARD alignment, determine based on sender
        // Compare message sender's uid with logged-in user's uid
        val loggedInUser = CometChat.getLoggedInUser()
        val isOutgoing = message.sender?.uid == loggedInUser?.uid
        
        // Apply alignment to adapter
        // Note: The adapter handles the actual LEFT/RIGHT positioning based on listAlignment
        // and the sender comparison in its getMessageAlignment() method
        messageAdapter?.listAlignment = alignment
    }

    /**
     * Gets the current parent message.
     *
     * @return The parent message, or null if not set
     */
    fun getParentMessage(): BaseMessage? = parentMessage

    // ==================== Public API - Visibility Controls ====================

    /**
     * Sets the visibility of reactions on the parent message bubble.
     *
     * When set to [View.GONE], reactions will be hidden on the parent message.
     * When set to [View.VISIBLE], reactions will be shown (if the message has reactions).
     *
     * @param visibility One of [View.VISIBLE], [View.INVISIBLE], or [View.GONE]
     */
    fun setReactionVisibility(visibility: Int) {
        reactionVisibility = visibility
        viewModel?.hideReaction = (visibility == View.GONE)
        // Apply to adapter - disableReactions = true hides reactions
        messageAdapter?.disableReactions = (visibility == View.GONE)
    }

    /**
     * Sets the visibility of the avatar on the parent message bubble.
     *
     * When set to [View.GONE], the avatar will be hidden on the parent message.
     * When set to [View.VISIBLE], the avatar will be shown for incoming messages.
     *
     * @param visibility One of [View.VISIBLE], [View.INVISIBLE], or [View.GONE]
     */
    fun setAvatarVisibility(visibility: Int) {
        avatarVisibility = visibility
        // Apply to adapter - when VISIBLE, show avatar; when GONE, hide avatar
        // showAvatar = true shows avatar for all left-aligned messages
        // To hide avatar completely, set showAvatar = false and also set the group/user avatar flags to false
        if (visibility == View.GONE) {
            messageAdapter?.showAvatar = false
            messageAdapter?.showLeftBubbleUserAvatar = false
            messageAdapter?.showLeftBubbleGroupAvatar = false
        } else {
            // When VISIBLE, enable avatar display
            messageAdapter?.showAvatar = true
        }
    }

    /**
     * Sets the visibility of read receipts on the parent message bubble.
     *
     * When set to [View.GONE], read receipts (delivered/read indicators) will be hidden.
     * When set to [View.VISIBLE], read receipts will be shown for outgoing messages.
     *
     * @param visibility One of [View.VISIBLE], [View.INVISIBLE], or [View.GONE]
     */
    fun setReceiptsVisibility(visibility: Int) {
        receiptsVisibility = visibility
        // Apply to adapter - disableReadReceipt = true hides receipts
        messageAdapter?.disableReadReceipt = (visibility == View.GONE)
    }

    /**
     * Sets the visibility of the reply count text.
     *
     * When set to [View.GONE], the reply count text (e.g., "5 Replies") will be hidden.
     * The reply count bar/container may still be visible if [setReplyCountBarVisibility]
     * is set to [View.VISIBLE].
     *
     * @param visibility One of [View.VISIBLE], [View.INVISIBLE], or [View.GONE]
     */
    fun setReplyCountVisibility(visibility: Int) {
        replyCountVisibility = visibility
        binding.tvReplies.visibility = visibility
    }

    /**
     * Sets the visibility of the reply count bar/container.
     *
     * When set to [View.GONE], the entire reply count section (including the text
     * and separator bar) will be hidden. This takes precedence over [setReplyCountVisibility].
     *
     * @param visibility One of [View.VISIBLE], [View.INVISIBLE], or [View.GONE]
     */
    fun setReplyCountBarVisibility(visibility: Int) {
        replyCountBarVisibility = visibility
        binding.repliesLayout.visibility = visibility
    }

    // ==================== Public API - Alignment ====================

    /**
     * Sets the message alignment mode.
     *
     * This controls how messages are positioned horizontally:
     * - [MessageListAlignment.STANDARD]: Outgoing messages align to the right,
     *   incoming messages align to the left. This is the default chat layout.
     * - [MessageListAlignment.LEFT_ALIGNED]: All messages align to the left,
     *   regardless of sender. Useful for feed-style layouts.
     *
     * @param alignment The alignment mode (STANDARD or LEFT_ALIGNED)
     */
    fun setAlignment(alignment: MessageListAlignment) {
        this.alignment = alignment
        
        // Apply alignment to the message adapter
        messageAdapter?.listAlignment = alignment
        
        // If a parent message is already set, re-apply the alignment logic
        // This ensures the message bubble is re-rendered with the correct alignment
        parentMessage?.let { message ->
            determineAndApplyAlignment(message)
        }
    }

    /**
     * Gets the current alignment mode.
     */
    fun getAlignment(): MessageListAlignment = alignment

    // ==================== Public API - Style Customization ====================

    /**
     * Sets the maximum height limit for the thread header.
     *
     * @param maxHeight Maximum height in pixels (0 = no limit)
     */
    fun setMaxHeight(@Dimension maxHeight: Int) {
        maxHeightLimit = maxHeight
        requestLayout()
    }

    /**
     * Sets the style for incoming message bubbles.
     *
     * @param style The style to apply to incoming (left-aligned) message bubbles
     */
    fun setIncomingMessageBubbleStyle(style: CometChatMessageBubbleStyle) {
        this.style = this.style.copy(incomingMessageBubbleStyle = style)
        messageAdapter?.incomingMessageBubbleStyle = style
    }

    /**
     * Sets the style for outgoing message bubbles.
     *
     * @param style The style to apply to outgoing (right-aligned) message bubbles
     */
    fun setOutgoingMessageBubbleStyle(style: CometChatMessageBubbleStyle) {
        this.style = this.style.copy(outgoingMessageBubbleStyle = style)
        messageAdapter?.outgoingMessageBubbleStyle = style
    }

    /**
     * Sets the margins for left-aligned (incoming) message bubbles.
     *
     * @param top Top margin in pixels
     * @param bottom Bottom margin in pixels
     * @param left Left margin in pixels
     * @param right Right margin in pixels
     */
    fun setLeftBubbleMargin(top: Int, bottom: Int, left: Int, right: Int) {
        leftBubbleMargin = intArrayOf(top, bottom, left, right)
        messageAdapter?.setLeftBubbleMargin(top, bottom, left, right)
    }

    /**
     * Sets the margins for right-aligned (outgoing) message bubbles.
     *
     * @param top Top margin in pixels
     * @param bottom Bottom margin in pixels
     * @param left Left margin in pixels
     * @param right Right margin in pixels
     */
    fun setRightBubbleMargin(top: Int, bottom: Int, left: Int, right: Int) {
        rightBubbleMargin = intArrayOf(top, bottom, left, right)
        messageAdapter?.setRightBubbleMargin(top, bottom, left, right)
    }

    /**
     * Sets the reply count text color.
     */
    fun setReplyCountTextColor(@ColorInt color: Int) {
        style = style.copy(replyCountTextColor = color)
        if (color != 0) binding.tvReplies.setTextColor(color)
    }

    /**
     * Sets the reply count text appearance.
     */
    fun setReplyCountTextAppearance(@StyleRes appearance: Int) {
        style = style.copy(replyCountTextAppearance = appearance)
        if (appearance != 0) binding.tvReplies.setTextAppearance(appearance)
    }

    /**
     * Sets the reply count background color.
     */
    fun setReplyCountBackgroundColor(@ColorInt color: Int) {
        style = style.copy(replyCountBackgroundColor = color)
        if (color != 0) binding.repliesLayout.setBackgroundColor(color)
    }

    // ==================== Public API - Text Formatters ====================

    /**
     * Sets the text formatters for message content.
     *
     * Text formatters transform message text for display, such as:
     * - [CometChatMentionsFormatter] for @mention rendering
     * - Markdown formatters for bold, italic, etc.
     * - Link formatters for URL detection
     *
     * @param formatters List of text formatters to apply
     */
    fun setTextFormatters(formatters: List<CometChatTextFormatter>) {
        textFormatters = formatters
        // Extract mentions formatter if present
        mentionsFormatter = formatters.filterIsInstance<CometChatMentionsFormatter>().firstOrNull()
        // Apply to adapter
        messageAdapter?.textFormatters = formatters
    }

    /**
     * Sets a custom time format for message timestamps.
     *
     * @param format The SimpleDateFormat to use for timestamps (e.g., "h:mm a" for "2:30 PM")
     */
    fun setTimeFormat(format: SimpleDateFormat) {
        timeFormat = format
        // Apply to adapter
        messageAdapter?.timeFormat = format
    }

    /**
     * Gets the mentions formatter for click handling.
     *
     * @return The CometChatMentionsFormatter, or null if not set
     */
    fun getCometchatMentionsFormatter(): CometChatMentionsFormatter? = mentionsFormatter

    // ==================== Public API - Style Object ====================

    /**
     * Sets the style from a style object.
     */
    fun setStyle(style: CometChatThreadHeaderStyle) {
        this.style = style
        applyStyle()
    }

    /**
     * Sets the style from a style resource.
     */
    fun setStyle(@StyleRes styleRes: Int) {
        if (styleRes != 0) {
            val typedArray = context.theme.obtainStyledAttributes(
                styleRes, R.styleable.CometChatThreadHeader
            )
            // fromTypedArray handles recycling internally
            setStyle(CometChatThreadHeaderStyle.fromTypedArray(context, typedArray))
        }
    }

    /**
     * Gets the current style.
     */
    fun getStyle(): CometChatThreadHeaderStyle = style

    // ==================== Public API - ViewModel ====================

    /**
     * Sets an external ViewModel.
     *
     * @param viewModel The ViewModel to use
     */
    fun setViewModel(viewModel: CometChatThreadHeaderViewModel) {
        this.viewModel = viewModel
        isExternalViewModel = true
        attachObservers()
    }

    /**
     * Gets the current ViewModel.
     */
    fun getViewModel(): CometChatThreadHeaderViewModel? = viewModel

    // ==================== Public API - MessageBubbleFactory ====================

    /**
     * Sets multiple BubbleFactories at once.
     *
     * @param factories Map of factory key (category_type) to BubbleFactory
     */
    fun setBubbleFactories(factories: Map<String, BubbleFactory>) {
        messageAdapter?.setBubbleFactories(factories)
    }

    /**
     * Gets the current bubble factories map.
     *
     * @return The bubble factories map (read-only copy)
     */
    fun getBubbleFactories(): Map<String, BubbleFactory>? = messageAdapter?.getBubbleFactories()
}
