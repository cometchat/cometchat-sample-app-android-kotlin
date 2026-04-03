package com.cometchat.uikit.kotlin.presentation.messagelist.ui

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.chat.core.ReactionsRequest
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.factory.CometChatMessageListViewModelFactory
import com.cometchat.uikit.core.state.MessageListUIState
import com.cometchat.uikit.core.viewmodel.CometChatMessageListViewModel
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.messageinformation.ui.CometChatMessageInformationBottomSheet
import com.cometchat.uikit.kotlin.presentation.messagelist.BubbleViewProvider
import com.cometchat.uikit.kotlin.presentation.messagelist.adapter.MessageAdapter
import com.cometchat.uikit.kotlin.presentation.messagelist.adapter.ThreadReplyClick
import com.cometchat.uikit.kotlin.presentation.messagelist.style.CometChatMessageListStyle
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar.CometChatAvatar
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.badgecount.CometChatBadgeCount
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.BubbleFactory
import com.cometchat.uikit.kotlin.presentation.shared.aiconversationstarter.CometChatAIConversationStarterStyle
import com.cometchat.uikit.kotlin.presentation.shared.aiconversationstarter.CometChatAIConversationStarterView
import com.cometchat.uikit.kotlin.presentation.shared.shimmer.CometChatShimmerFrameLayout
import com.cometchat.uikit.kotlin.presentation.shared.shimmer.CometChatShimmerAdapter
import com.cometchat.uikit.kotlin.presentation.shared.shimmer.CometChatShimmerUtils
import com.cometchat.uikit.core.utils.MessageOptionsUtils
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.events.CometChatUIEvent
import com.cometchat.uikit.core.state.ConversationStarterUIState
import com.cometchat.uikit.core.state.ConversationSummaryUIState
import com.cometchat.uikit.core.state.SmartRepliesUIState
import com.cometchat.uikit.kotlin.presentation.shared.aiconversationsummary.CometChatAIConversationSummaryStyle
import com.cometchat.uikit.kotlin.presentation.shared.aiconversationsummary.CometChatAIConversationSummaryView
import com.cometchat.uikit.kotlin.presentation.shared.aismartreplies.CometChatAISmartRepliesStyle
import com.cometchat.uikit.kotlin.presentation.shared.aismartreplies.CometChatAISmartRepliesView
import com.cometchat.uikit.kotlin.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.kotlin.shared.formatters.CometChatMentionsFormatter
import com.cometchat.uikit.kotlin.shared.formatters.FormatterUtils
import com.cometchat.uikit.kotlin.shared.interfaces.DateTimeFormatterCallback
import com.cometchat.uikit.kotlin.shared.interfaces.EmojiPickerClickListener
import com.cometchat.uikit.kotlin.shared.interfaces.MessageOptionClickListener
import com.cometchat.uikit.kotlin.shared.interfaces.ReactionClickListener
import com.cometchat.uikit.kotlin.shared.interfaces.ToolCallListener
import com.cometchat.uikit.kotlin.presentation.messagelist.CometChatMessagePopupMenu
import com.cometchat.uikit.kotlin.presentation.shared.popupmenu.CometChatPopupMenu
import com.cometchat.uikit.kotlin.presentation.emojikeyboard.ui.CometChatEmojiKeyboard
import com.cometchat.uikit.kotlin.presentation.emojikeyboard.ui.EmojiKeyBoardView
import com.cometchat.uikit.kotlin.presentation.reactionlist.ui.CometChatReactionList
import com.cometchat.uikit.core.domain.model.CometChatMessageOption
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.SimpleDateFormat
import com.cometchat.uikit.kotlin.shared.resources.utils.sticky_header.StickyHeaderAdapter
import com.cometchat.uikit.kotlin.shared.resources.utils.sticky_header.StickyHeaderDecoration
import com.cometchat.uikit.kotlin.shared.resources.utils.unread_message_decoration.NewMessageIndicatorDecoration
import com.cometchat.uikit.kotlin.shared.resources.utils.MediaUtils
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.date.CometChatDateStyle
import com.cometchat.uikit.kotlin.presentation.shared.dialog.CometChatConfirmDialog
import com.cometchat.uikit.kotlin.presentation.report.CometChatFlagMessageDialog
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * A View component for displaying messages in a chat interface.
 *
 * CometChatMessageList is an Android View (XML-based) component that displays messages
 * in a scrollable list with support for:
 * - Automatic message alignment (LEFT for incoming, RIGHT for outgoing, CENTER for actions)
 * - Pagination for loading older and newer messages
 * - Real-time message updates via [CometChatMessageListViewModel]
 * - Custom view slots for bubble customization via [BubbleViewProvider]
 * - Factory-based content rendering via [BubbleFactory]
 * - Loading, empty, and error states
 * - New message indicator when scrolled up
 *
 * ## Basic Usage
 *
 * ```kotlin
 * // Create programmatically
 * val messageList = CometChatMessageList(context)
 * messageList.setViewModel(viewModel)
 * messageList.setUser(user)
 *
 * // Or use in XML
 * <com.cometchat.uikit.kotlin.presentation.messagelist.ui.CometChatMessageList
 *     android:id="@+id/message_list"
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent"
 *     app:cometchatMessageListStyle="@style/CustomMessageListStyle" />
 * ```
 *
 * ## BubbleViewProvider Usage
 *
 * [BubbleViewProvider] allows customization of individual bubble slots. Each provider
 * receives the message and its alignment, enabling direction-aware customization:
 *
 * ```kotlin
 * messageList.setLeadingViewProvider(object : BubbleViewProvider {
 *     override fun createView(context: Context, message: BaseMessage, alignment: MessageAlignment): View? {
 *         return if (alignment == MessageAlignment.LEFT) {
 *             CometChatAvatar(context).apply {
 *                 setUser(message.sender)
 *             }
 *         } else null
 *     }
 *
 *     override fun bindView(view: View, message: BaseMessage, alignment: MessageAlignment) {
 *         (view as? CometChatAvatar)?.setUser(message.sender)
 *     }
 * })
 * ```
 *
 * ## BubbleFactory Integration
 *
 * Content rendering is handled by [BubbleFactory] instances. When no factory is registered
 * for a message type, [InternalContentRenderer] handles default rendering. Custom factories
 * can be added for custom message types:
 *
 * ```kotlin
 * messageList.setBubbleFactories(listOf(
 *     LocationBubbleFactory(),
 *     PaymentBubbleFactory()
 * ))
 * ```
 *
 * ## Styling
 *
 * Apply custom styles via XML attributes or programmatically:
 *
 * ```kotlin
 * messageList.setStyle(CometChatMessageListStyle.default(context).copy(
 *     backgroundColor = Color.WHITE
 * ))
 * ```
 *
 * @param context The Android context.
 * @param attrs Optional XML attributes.
 * @param defStyleAttr Default style attribute reference.
 *
 * @see CometChatMessageListViewModel
 * @see MessageAdapter
 * @see BubbleFactory
 * @see BubbleViewProvider
 * @see CometChatMessageListStyle
 */
class CometChatMessageList @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.cometchatMessageListStyle
) : FrameLayout(context, attrs, defStyleAttr) {

    // ========================================
    // View References
    // ========================================

    private var parentLayout: LinearLayout? = null
    private var headerViewContainer: FrameLayout? = null
    private var footerViewContainer: FrameLayout? = null
    private var loadingStateView: LinearLayout? = null
    private var shimmerEffectFrame: CometChatShimmerFrameLayout? = null
    private var shimmerRecyclerView: RecyclerView? = null
    private var emptyStateView: LinearLayout? = null
    private var emptyAvatar: CometChatAvatar? = null
    private var emptyTitle: TextView? = null
    private var emptySubtitle: TextView? = null
    private var aiSuggestedMessagesContainer: FlexboxLayout? = null
    private var errorStateView: LinearLayout? = null
    private var errorImage: ImageView? = null
    private var errorTitle: TextView? = null
    private var errorSubtitle: TextView? = null
    private var customViewContainer: FrameLayout? = null
    private var messageListLayout: RelativeLayout? = null
    private var topPaginationIndicator: ProgressBar? = null
    private var recyclerViewMessageList: RecyclerView? = null
    private var bottomPaginationIndicator: ProgressBar? = null
    private var newMessageIndicator: MaterialCardView? = null
    private var newMessageBadge: CometChatBadgeCount? = null
    private var newMessageArrow: ImageView? = null

    // ========================================
    // Adapter and Layout Manager
    // ========================================

    private val messageAdapter: MessageAdapter = MessageAdapter()
    private lateinit var linearLayoutManager: LinearLayoutManager

    // ========================================
    // ViewModel
    // ========================================

    private var viewModel: CometChatMessageListViewModel? = null
    private var isExternalViewModel: Boolean = false
    private var pendingObservation: Boolean = false

    // ========================================
    // Style
    // ========================================

    private var style: CometChatMessageListStyle = CometChatMessageListStyle.default(context)

    // ========================================
    // Configuration
    // ========================================

    /**
     * Text formatters for message text rendering.
     * These formatters are applied to text messages to handle mentions, links, markdown, etc.
     */
    private var _textFormatters: List<CometChatTextFormatter> = emptyList()

    /**
     * The mentions formatter extracted from the text formatters list.
     * Used to configure @mentions functionality including @all mentions.
     */
    private var cometchatMentionsFormatter: CometChatMentionsFormatter? = null

    /**
     * The ID used for @all mentions (e.g., group GUID).
     */
    private var mentionAllLabelId: String? = null

    /**
     * The label text displayed for @all mentions.
     */
    private var mentionAllLabel: String? = null

    private var user: User? = null
    private var group: Group? = null
    private var parentMessageId: Long = -1
    private var messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder? = null
    private var messagesTypes: List<String>? = null
    private var messagesCategories: List<String>? = null

    // Behavior
    private var scrollToBottomOnNewMessage: Boolean = true
    private var swipeToReplyEnabled: Boolean = true
    private var itemTouchHelper: ItemTouchHelper? = null
    private var disableSoundForMessages: Boolean = false
    private var customSoundForMessages: Int = 0
    private var autoFetch: Boolean = true
    private var startFromUnreadMessages: Boolean = false
    private var unreadMessageThreshold: Int = 30
    private var disableReceipt: Boolean = false
    private var enableConversationStarter: Boolean = false

    // AI Conversation Starter View
    private var aiConversationStarterView: CometChatAIConversationStarterView? = null
    private var conversationStarterStyle: Int = 0

    // AI Conversation Summary View
    private var aiConversationSummaryView: CometChatAIConversationSummaryView? = null
    private var conversationSummaryStyle: Int = 0
    private var enableConversationSummary: Boolean = true

    // AI Smart Replies View
    private var aiSmartRepliesView: CometChatAISmartRepliesView? = null
    private var smartRepliesStyle: Int = 0
    private var enableSmartReplies: Boolean = false
    private var smartRepliesKeywords: List<String> = emptyList()
    private var smartRepliesDelayDuration: Int = 10000

    // Visibility - Int-based fields for Java API parity
    private var avatarVisibility: Int = View.VISIBLE
    private var receiptsVisibility: Int = View.VISIBLE
    private var reactionVisibility: Int = View.VISIBLE
    private var groupActionMessageVisibility: Int = View.VISIBLE
    private var errorStateVisibility: Int = View.VISIBLE
    private var stickyDateVisibility: Int = View.VISIBLE

    // Message Option Visibility - Boolean fields (true = visible)
    private var replyInThreadOptionVisible: Boolean = true
    private var replyOptionVisible: Boolean = true
    private var copyOptionVisible: Boolean = true
    private var editOptionVisible: Boolean = true
    private var deleteOptionVisible: Boolean = true
    private var reactOptionVisible: Boolean = true
    private var messageInfoOptionVisible: Boolean = true
    private var translateOptionVisible: Boolean = true
    private var shareOptionVisible: Boolean = true
    private var messagePrivatelyOptionVisible: Boolean = true
    private var markAsUnreadOptionVisible: Boolean = false
    private var reportOptionVisible: Boolean = true
    private var flagRemarkInputFieldVisible: Boolean = true

    // Sticky header decoration for date separators
    private var stickyHeaderDecoration: StickyHeaderDecoration? = null
    
    // New message indicator decoration for unread messages separator
    private var newMessageIndicatorDecoration: NewMessageIndicatorDecoration<MessageAdapter.NewMessageIndicatorViewHolder>? = null

    // Message alignment configuration
    private var messageAlignment: UIKitConstants.MessageListAlignment = UIKitConstants.MessageListAlignment.STANDARD

    // Visibility - Boolean fields (derived from int-based fields)
    private var hideAvatar: Boolean = false
    private var hideReceipts: Boolean = false
    private var hideGroupActionMessages: Boolean = false
    private var hideLoadingState: Boolean = false
    private var hideEmptyState: Boolean = false
    private var hideErrorState: Boolean = false

    // BubbleFactories
    private var bubbleFactories: MutableMap<String, BubbleFactory> = mutableMapOf()

    // BubbleViewProviders
    private var leadingViewProvider: BubbleViewProvider? = null
    private var headerViewProvider: BubbleViewProvider? = null
    private var replyViewProvider: BubbleViewProvider? = null
    private var contentViewProvider: BubbleViewProvider? = null
    private var bottomViewProvider: BubbleViewProvider? = null
    private var statusInfoViewProvider: BubbleViewProvider? = null
    private var threadViewProvider: BubbleViewProvider? = null
    private var footerViewProvider: BubbleViewProvider? = null

    // Custom Views
    private var customHeaderView: View? = null
    private var customFooterView: View? = null
    private var customEmptyStateView: View? = null
    private var customErrorStateView: View? = null
    private var customLoadingStateView: View? = null
    private var customNewMessageIndicatorView: View? = null

    // AI Assistant Empty Chat Greeting View
    private var aiAssistantEmptyChatGreetingView: View? = null
    private var aiAssistantEmptyChatGreetingViewResId: Int = 0

    // AI Assistant Suggested Messages
    private var aiAssistantSuggestedMessages: List<String> = emptyList()
    private var aiAssistantSuggestedMessagesVisibility: Int = View.VISIBLE

    // AI Assistant Tools for function calling
    private var aiAssistantTools: HashMap<String, ToolCallListener> = hashMapOf()

    // AI Streaming Configuration
    private var streamingSpeed: Int? = null

    // Date/Time Formatting Configuration
    private var timeFormat: SimpleDateFormat? = null
    private var dateFormat: SimpleDateFormat? = null
    private var dateTimeFormatter: DateTimeFormatterCallback? = null

    // Callbacks
    private var onError: ((Throwable) -> Unit)? = null
    private var onLoad: (() -> Unit)? = null
    private var onEmpty: (() -> Unit)? = null
    private var onItemClick: ((BaseMessage, Int) -> Unit)? = null
    private var onItemLongClick: ((BaseMessage, Int) -> Boolean)? = null
    private var onThreadRepliesClick: ((BaseMessage) -> Unit)? = null
    private var onMessagePrivately: ((User) -> Unit)? = null
    private var onReactionClick: ((BaseMessage, String) -> Unit)? = null
    private var onReactionLongClick: ((BaseMessage, String) -> Unit)? = null
    private var onAddMoreReactionsClick: ((BaseMessage) -> Unit)? = null

    // Message Context Menu Callbacks
    private var messageOptionClickListener: MessageOptionClickListener? = null
    private var quickReactionClickListener: ReactionClickListener? = null
    private var emojiPickerClickListener: EmojiPickerClickListener? = null

    // Current message being long-pressed (for callback invocation)
    private var currentLongPressedMessage: BaseMessage? = null

    // Quick Reactions Configuration
    private var quickReactions: List<String> = listOf("👍", "❤️", "😂", "😮", "😢", "🙏")
    private var addReactionIcon: Int = 0

    // Popup Menu
    private var cometchatPopUpMenuMessage: CometChatMessagePopupMenu? = null

    // Emoji Keyboard
    private var emojiKeyboard: CometChatEmojiKeyboard? = null

    // Reactions Request Builder
    private var reactionsRequestBuilder: ReactionsRequest.ReactionsRequestBuilder? = null

    // Reaction List Bottom Sheet
    private var bottomSheetDialog: BottomSheetDialog? = null

    // Scroll tracking
    private var newMessageCount: Int = 0
    private var isUserAtBottom: Boolean = true
    private var isScrolling: Boolean = false
    
    // Highlight animation - stored as property to prevent garbage collection
    private var highlightAnimator: android.animation.ValueAnimator? = null
    private var isPaginatingPrevious: Boolean = false
    private var isPaginatingNext: Boolean = false

    init {
        inflateLayout()
        applyStyleAttributes(attrs, defStyleAttr, 0)
        setupRecyclerView()
        setupClickListeners()
        initializeConversationStarterView()
        initializeConversationSummaryView()
        initializeSmartRepliesView()
        processMentionsFormatter()
        initViewModel()
        // Initialize swipe to reply if enabled by default
        if (swipeToReplyEnabled) {
            initializeItemTouchHelper()
        }
    }

    // ========================================
    // Initialization
    // ========================================

    private fun inflateLayout() {
        val view = View.inflate(context, R.layout.cometchat_message_list, this)
        
        parentLayout = view.findViewById(R.id.parent_layout)
        headerViewContainer = view.findViewById(R.id.header_view_container)
        footerViewContainer = view.findViewById(R.id.footer_view_container)
        loadingStateView = view.findViewById(R.id.loading_state_view)
        shimmerEffectFrame = view.findViewById(R.id.shimmer_effect_frame)
        shimmerRecyclerView = view.findViewById(R.id.shimmer_recyclerview)
        emptyStateView = view.findViewById(R.id.empty_state_view)
        emptyAvatar = view.findViewById(R.id.iv_empty_avatar)
        emptyTitle = view.findViewById(R.id.tv_empty_title)
        emptySubtitle = view.findViewById(R.id.tv_empty_subtitle)
        aiSuggestedMessagesContainer = view.findViewById(R.id.ai_suggested_messages_container)
        errorStateView = view.findViewById(R.id.error_state_view)
        errorImage = view.findViewById(R.id.iv_error)
        errorTitle = view.findViewById(R.id.tv_error_title)
        errorSubtitle = view.findViewById(R.id.tv_error_subtitle)
        customViewContainer = view.findViewById(R.id.custom_view_container)
        messageListLayout = view.findViewById(R.id.message_list_layout)
        topPaginationIndicator = view.findViewById(R.id.top_pagination_indicator)
        recyclerViewMessageList = view.findViewById(R.id.recyclerview_message_list)
        bottomPaginationIndicator = view.findViewById(R.id.bottom_pagination_indicator)
        newMessageIndicator = view.findViewById(R.id.new_message_indicator)
        newMessageBadge = view.findViewById(R.id.new_message_badge)
        newMessageArrow = view.findViewById(R.id.new_message_arrow)
        
        // Start shimmer immediately since loading state is visible by default
        setupShimmer()

        // Initialize popup menu
        cometchatPopUpMenuMessage = CometChatMessagePopupMenu(context, 0)
    }

    /**
     * Sets up the shimmer effect for the loading state.
     * Creates a shimmer adapter and configures the shimmer animation.
     */
    private fun setupShimmer() {
        val shimmerAdapter = CometChatShimmerAdapter(2, R.layout.cometchat_shimmer_message_list)
        shimmerRecyclerView?.layoutManager = LinearLayoutManager(context).apply {
            // Disable scrolling for shimmer view
            object : LinearLayoutManager(context) {
                override fun canScrollVertically(): Boolean = false
            }
        }
        shimmerRecyclerView?.adapter = shimmerAdapter
        shimmerEffectFrame?.setShimmer(CometChatShimmerUtils.getCometChatShimmerConfig(context))
        shimmerEffectFrame?.startShimmer()
    }

    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        val typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatMessageList, defStyleAttr, defStyleRes
        )
        val styleResId = typedArray.getResourceId(
            R.styleable.CometChatMessageList_cometchatMessageListStyle, 0
        )
        val styledArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatMessageList, defStyleRes, styleResId
        )
        style = CometChatMessageListStyle.fromTypedArray(context, styledArray)
        applyStyle()
    }

    private fun setupRecyclerView() {
        linearLayoutManager = LinearLayoutManager(context).apply {
            reverseLayout = false  // Standard layout, index 0 at top
            stackFromEnd = false   // Don't stack from end
        }
        
        recyclerViewMessageList?.apply {
            layoutManager = linearLayoutManager
            adapter = messageAdapter
            
            // Completely disable item animator to prevent any blinking/animation when messages are updated
            itemAnimator = null
            
            // Add scroll listener for pagination and new message indicator
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    handleScrollStateChange(newState)
                }
                
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    handleScroll()
                }
            })
        }
        
        // Set adapter click listeners
        messageAdapter.setOnItemClickListener { message, position ->
            onItemClick?.invoke(message, position)
        }
        messageAdapter.setOnItemLongClickListener { message, position ->
            onItemLongClick?.invoke(message, position) ?: false
        }
        
        // Set adapter long click callback for message options
        messageAdapter.onMessageLongClick = { options, message, factory, bubble ->
            // Skip showing options for in-progress or deleted messages
            if (message.id != 0L && message.sentAt != 0L && message.deletedAt == 0L) {
                // Store the current message for callback invocation
                currentLongPressedMessage = message

                // Build options using core utils: defaults → filter by visibility → append custom
                val isThreadView = parentMessageId > 0
                val defaultOptions = MessageOptionsUtils.getDefaultMessageOptions(context, message, user, group, isThreadView)
                val filtered = MessageOptionsUtils.getFilteredMessageOptions(defaultOptions, buildOptionVisibilityMap())
                // Apply error color to delete option (core module can't resolve theme colors)
                val errorColor = CometChatTheme.getErrorColor(context)
                val themed = filtered.map { option ->
                    if (option.id == UIKitConstants.MessageOption.DELETE && option.titleColor == 0) {
                        option.copy(titleColor = errorColor, iconTintColor = errorColor)
                    } else {
                        option
                    }
                }
                // Resolve final options via ViewModel callbacks (setOptions / addOptions)
                val finalOptions = viewModel?.resolveMessageOptions(message, themed) ?: themed

                // Show popup menu
                openMessageOptionBottomSheet(finalOptions, message, bubble)

                // Notify the consumer with the final options list
                onItemLongClick?.invoke(message, messageAdapter.findMessagePosition(message.id))
            }
        }
        
        // Pass bubble factories to adapter
        messageAdapter.setBubbleFactories(bubbleFactories)

        // Wire up reaction callbacks on the adapter
        messageAdapter.onReactionClick = { reaction, message ->
            onReactionClick?.invoke(message, reaction.reaction)
        }
        messageAdapter.onReactionLongClick = { reaction, message ->
            if (onReactionLongClick != null) {
                onReactionLongClick?.invoke(message, reaction.reaction)
            } else {
                openReactionListBottomSheet(reaction.reaction, message)
            }
        }
        messageAdapter.onAddMoreReactionsClick = { message ->
            if (onAddMoreReactionsClick != null) {
                onAddMoreReactionsClick?.invoke(message)
            } else {
                openEmojiKeyboardForReaction(message)
            }
        }
        
        // Wire up message preview click callback for jump to parent message
        messageAdapter.onMessagePreviewClick = { quotedMessage ->
            handleMessagePreviewClick(quotedMessage)
        }

        // Wire up thread reply click — maps to the same callback as the "Reply in Thread" option
        messageAdapter.threadReplyClick = ThreadReplyClick { context, message, factory ->
            onThreadRepliesClick?.invoke(message)
        }
        
        // Pass view providers to adapter
        updateAdapterProviders()
        // Initialize sticky date header (visible by default)
        setStickyDateVisibility(stickyDateVisibility)
    }

    private fun setupClickListeners() {
        newMessageIndicator?.setOnClickListener {
            scrollToBottom()
        }
    }

    /**
     * Initializes the AI conversation starter view.
     * 
     * This view displays AI-generated conversation starter suggestions
     * when the message list is empty and the feature is enabled.
     */
    private fun initializeConversationStarterView() {
        aiConversationStarterView = CometChatAIConversationStarterView(context).apply {
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            val margin = Utils.convertDpToPx(context, 10)
            layoutParams.setMargins(margin, margin, margin, margin)
            this.layoutParams = layoutParams
            
            setOnItemClickListener { id, reply, _ ->
                CometChatEvents.emitUIEvent(CometChatUIEvent.ComposeMessage(id = id, text = reply))
                detachConversationStarterView()
            }
        }
    }

    /**
     * Initializes the AI conversation summary view.
     * 
     * This view displays an AI-generated conversation summary at the top
     * of the message list when there are many unread messages (above threshold).
     */
    private fun initializeConversationSummaryView() {
        aiConversationSummaryView = CometChatAIConversationSummaryView(context).apply {
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            val margin = Utils.convertDpToPx(context, 10)
            layoutParams.setMargins(margin, margin, margin, margin)
            this.layoutParams = layoutParams
            
            setOnCloseClickListener {
                viewModel?.dismissConversationSummary()
            }
        }
    }

    /**
     * Initializes the AI smart replies view.
     * 
     * This view displays AI-generated smart reply suggestions when a text
     * message is received from another user. The suggestions appear in the
     * footer area and allow users to quickly respond with one tap.
     */
    private fun initializeSmartRepliesView() {
        aiSmartRepliesView = CometChatAISmartRepliesView(context).apply {
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            val margin = Utils.convertDpToPx(context, 10)
            layoutParams.setMargins(margin, margin, margin, margin)
            this.layoutParams = layoutParams
            
            // Handle close icon click - clear smart replies
            setOnCloseIconClick {
                viewModel?.clearSmartReplies()
                detachSmartRepliesView()
            }
            
            // Handle smart reply item click - compose message and detach view
            setOnClick { _, id, reply, _ ->
                CometChatEvents.emitUIEvent(CometChatUIEvent.ComposeMessage(id = id, text = reply))
                detachSmartRepliesView()
            }
        }
    }

    /**
     * Creates a default ViewModel if none was externally provided.
     * Follows the same pattern as CometChatMessageHeader.
     */
    private fun initViewModel() {
        if (!isExternalViewModel) {
            viewModel = CometChatMessageListViewModelFactory()
                .create(CometChatMessageListViewModel::class.java)
            viewModel?.initSoundManager(context)
            viewModel?.setDisableReceipt(disableReceipt)
            viewModel?.setHideDeleteMessage(false)
            viewModel?.setStartFromUnreadMessages(startFromUnreadMessages)
            viewModel?.setUnreadThreshold(unreadMessageThreshold)
            viewModel?.setDisableSoundForMessages(disableSoundForMessages)
            if (customSoundForMessages != 0) {
                viewModel?.setCustomSoundForMessages(customSoundForMessages)
            }
        }
        observeViewModel()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (pendingObservation) {
            observeViewModel()
        }
    }

    /**
     * Handles scroll state changes to track when user is actively scrolling.
     * Pagination is only triggered during active touch scroll to prevent
     * unnecessary fetches during programmatic scrolls or flings.
     *
     * @param newState The new scroll state of the RecyclerView.
     */
    private fun handleScrollStateChange(newState: Int) {
        when (newState) {
            android.widget.AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL -> isScrolling = true
            android.widget.AbsListView.OnScrollListener.SCROLL_STATE_IDLE -> isScrolling = false
        }
    }

    private fun handleScroll() {
        val firstVisiblePosition = linearLayoutManager.findFirstVisibleItemPosition()
        val lastVisiblePosition = linearLayoutManager.findLastVisibleItemPosition()
        val totalItemCount = linearLayoutManager.itemCount
        
        // With reverseLayout = false:
        // - Position 0 = TOP (oldest messages)
        // - Last position = BOTTOM (newest messages)
        
        // Check if user is at bottom (last visible item is near the end)
        isUserAtBottom = lastVisiblePosition >= totalItemCount - 2
        
        // Update new message indicator visibility
        if (isUserAtBottom) {
            newMessageCount = 0
            newMessageIndicator?.visibility = View.GONE
        }
        
        val vm = viewModel ?: return
        val hasMorePrevious = vm.hasMorePreviousMessages.value
        val hasMoreNext = vm.hasMoreNewMessages.value
        val inProgress = vm.isInProgress.value
        
        // Only trigger pagination when:
        // 1. User is actively scrolling (touch scroll)
        // 2. No fetch operation is in progress
        // 3. There are more messages to load
        // 4. User has reached the extreme end of the list
        
        // Pagination - at TOP → load older messages
        if (hasMorePrevious && isScrolling && !inProgress && 
            (firstVisiblePosition == 0 || recyclerViewMessageList?.canScrollVertically(-1) == false)) {
            isPaginatingPrevious = true
            isScrolling = false
            topPaginationIndicator?.visibility = View.VISIBLE
            vm.fetchMessages()
        }
        
        // Pagination - at BOTTOM → load newer messages
        if (hasMoreNext && isScrolling && !inProgress && 
            (lastVisiblePosition == totalItemCount - 1 || recyclerViewMessageList?.canScrollVertically(1) == false)) {
            isPaginatingNext = true
            isScrolling = false
            bottomPaginationIndicator?.visibility = View.VISIBLE
            vm.fetchNextMessages()
        }
    }

    // ========================================
    // Style Application
    // ========================================

    private fun applyStyle() {
        // Container
        parentLayout?.setBackgroundColor(style.backgroundColor)
        
        // Error state
        errorTitle?.apply {
            setTextColor(style.errorStateTitleTextColor)
            if (style.errorStateTitleTextAppearance != 0) {
                setTextAppearance(style.errorStateTitleTextAppearance)
            }
        }
        errorSubtitle?.apply {
            setTextColor(style.errorStateSubtitleTextColor)
            if (style.errorStateSubtitleTextAppearance != 0) {
                setTextAppearance(style.errorStateSubtitleTextAppearance)
            }
        }
        
        // Empty state
        emptyTitle?.apply {
            setTextColor(style.emptyChatGreetingTitleTextColor)
            if (style.emptyChatGreetingTitleTextAppearance != 0) {
                setTextAppearance(style.emptyChatGreetingTitleTextAppearance)
            }
        }
        emptySubtitle?.apply {
            setTextColor(style.emptyChatGreetingSubtitleTextColor)
            if (style.emptyChatGreetingSubtitleTextAppearance != 0) {
                setTextAppearance(style.emptyChatGreetingSubtitleTextAppearance)
            }
        }
        
        // Apply AI component styles - use style objects or defaults
        val smartRepliesStyle = style.aiSmartRepliesStyle ?: CometChatAISmartRepliesStyle.default(context)
        aiSmartRepliesView?.setStyle(smartRepliesStyle)
        
        val conversationStarterStyle = style.aiConversationStarterStyle ?: CometChatAIConversationStarterStyle.default(context)
        aiConversationStarterView?.setStyle(conversationStarterStyle)
        
        val conversationSummaryStyle = style.aiConversationSummaryStyle ?: CometChatAIConversationSummaryStyle.default(context)
        aiConversationSummaryView?.setStyle(conversationSummaryStyle)
        
        // Pass bubble style objects to adapter
        messageAdapter.incomingMessageBubbleStyle = style.incomingMessageBubbleStyle
        messageAdapter.outgoingMessageBubbleStyle = style.outgoingMessageBubbleStyle
        messageAdapter.actionBubbleStyle = style.actionBubbleStyle
        messageAdapter.callActionBubbleStyle = style.callActionBubbleStyle
        messageAdapter.dateSeparatorStyleObject = style.dateSeparatorStyle ?: CometChatDateStyle.default(context)
    }

    // ========================================
    // ViewModel Observation
    // ========================================

    private fun observeViewModel() {
        val lifecycleOwner = findViewTreeLifecycleOwner()
        if (lifecycleOwner == null) {
            pendingObservation = true
            return
        }
        pendingObservation = false
        val vm = viewModel ?: return
        
        lifecycleOwner.lifecycleScope.launch {
            vm.uiState.collectLatest { state ->
                handleUIState(state)
            }
        }
        
        lifecycleOwner.lifecycleScope.launch {
            vm.messages.collect { messages ->
                handleMessagesUpdate(messages)
            }
        }

        // Observe in-place message updates (e.g., reply count changes) that
        // StateFlow conflation would suppress because the object reference is the same.
        lifecycleOwner.lifecycleScope.launch {
            vm.messageUpdated.collect { updatedMessage ->
                val position = messageAdapter.getMessages().indexOfFirst { it.id == updatedMessage.id }
                if (position >= 0) {
                    android.util.Log.d("ThreadReplyDebug", "messageUpdated: notifyItemChanged at position=$position for id=${updatedMessage.id}, replyCount=${updatedMessage.replyCount}")
                    messageAdapter.notifyItemChanged(position)
                }
            }
        }
        
        lifecycleOwner.lifecycleScope.launch {
            vm.isInProgress.collectLatest { inProgress ->
                handleProgressState(inProgress)
            }
        }
        
        lifecycleOwner.lifecycleScope.launch {
            vm.scrollToMessageId.collectLatest { messageId ->
                messageId?.let { scrollToMessage(it) }
            }
        }
        
        lifecycleOwner.lifecycleScope.launch {
            vm.scrollToBottomEvent.collectLatest {
                // Scroll to bottom to show newest messages (for reverseLayout = false)
                scrollToLastItem()
            }
        }
        
        // Observe conversation starter replies
        lifecycleOwner.lifecycleScope.launch {
            vm.conversationStarterReplies.collectLatest { replies ->
                handleConversationStarterReplies(replies)
            }
        }
        
        // Observe conversation starter UI state
        lifecycleOwner.lifecycleScope.launch {
            vm.conversationStarterUIState.collectLatest { state ->
                handleConversationStarterUIState(state)
            }
        }
        
        // Observe remove conversation starter event
        lifecycleOwner.lifecycleScope.launch {
            vm.removeConversationStarter.collect {
                handleRemoveConversationStarter()
            }
        }
        
        // Observe conversation summary UI state
        lifecycleOwner.lifecycleScope.launch {
            vm.conversationSummaryUIState.collectLatest { state ->
                handleConversationSummaryUIState(state)
            }
        }
        
        // Observe remove conversation summary event
        lifecycleOwner.lifecycleScope.launch {
            vm.removeConversationSummary.collect {
                handleRemoveConversationSummary()
            }
        }
        
        // Observe smart replies UI state
        lifecycleOwner.lifecycleScope.launch {
            vm.smartRepliesUIState.collectLatest { state ->
                handleSmartRepliesUIState(state)
            }
        }

        // Observe delete confirmation requests from ViewModel
        lifecycleOwner.lifecycleScope.launch {
            vm.deleteConfirmationRequest.collectLatest { message ->
                handleDeleteConfirmationRequest(message)
            }
        }

        // Observe message translation completions from ViewModel
        lifecycleOwner.lifecycleScope.launch {
            vm.messageTranslated.collectLatest { message ->
                handleMessageTranslated(message)
            }
        }
        
        // Observe flag state for report dialog
        lifecycleOwner.lifecycleScope.launch {
            vm.flagState.collectLatest { state ->
                handleFlagState(state)
            }
        }
        
        // Observe message sender fetched for message privately
        lifecycleOwner.lifecycleScope.launch {
            vm.messageSenderFetched.collect { user ->
                onMessagePrivately?.invoke(user)
            }
        }
        
        // Observe unread message anchor for "New Messages" separator
        lifecycleOwner.lifecycleScope.launch {
            vm.unreadMessageAnchor.collectLatest { message ->
                handleUnreadMessageAnchor(message)
            }
        }
    }
    
    /**
     * Handles the unread message anchor update from the ViewModel.
     * Sets up the NewMessageIndicatorDecoration to show the "New" separator
     * above the first unread message.
     */
    @Suppress("UNCHECKED_CAST")
    private fun handleUnreadMessageAnchor(message: BaseMessage?) {
        if (message != null) {
            // Initialize decoration if needed
            if (newMessageIndicatorDecoration == null) {
                newMessageIndicatorDecoration = NewMessageIndicatorDecoration(messageAdapter)
                recyclerViewMessageList?.addItemDecoration(newMessageIndicatorDecoration!!)
            }
            // Set the unread message ID to show the separator
            newMessageIndicatorDecoration?.setUnreadMessageId(message.id)
            recyclerViewMessageList?.invalidateItemDecorations()
        } else {
            // Clear the unread indicator
            newMessageIndicatorDecoration?.setUnreadMessageId(-1)
            recyclerViewMessageList?.invalidateItemDecorations()
        }
    }
    
    // Reference to the current flag dialog (if shown)
    private var currentFlagDialog: CometChatFlagMessageDialog? = null
    
    /**
     * Handles flag state changes from the ViewModel.
     */
    private fun handleFlagState(state: com.cometchat.uikit.core.state.MessageFlagState) {
        when (state) {
            is com.cometchat.uikit.core.state.MessageFlagState.Success -> {
                currentFlagDialog?.dismiss()
                currentFlagDialog = null
                viewModel?.resetFlagState()
            }
            is com.cometchat.uikit.core.state.MessageFlagState.Error -> {
                currentFlagDialog?.hidePositiveButtonProgressBar(true)
                currentFlagDialog?.onFlagMessageError()
                viewModel?.resetFlagState()
            }
            is com.cometchat.uikit.core.state.MessageFlagState.InProgress -> {
                // Progress is shown by the dialog
            }
            is com.cometchat.uikit.core.state.MessageFlagState.Idle -> {
                // No action needed
            }
        }
    }

    private fun handleUIState(state: MessageListUIState) {
        when (state) {
            is MessageListUIState.Loading -> showLoadingState()
            is MessageListUIState.Loaded -> showLoadedState()
            is MessageListUIState.Empty -> showEmptyState()
            is MessageListUIState.Error -> showErrorState(state.exception)
        }
    }

    private fun handleMessagesUpdate(messages: List<BaseMessage>) {
        android.util.Log.d("ThreadReplyDebug", "handleMessagesUpdate: ${messages.size} messages")
        val previousCount = messageAdapter.itemCount
        val wasAtBottom = isUserAtBottom || previousCount == 0
        
        messageAdapter.setMessages(messages)
        
        // Clear sticky header cache to ensure headers are redrawn with correct styles
        stickyHeaderDecoration?.clearHeaderCache()

        // Dismiss popup if the currently displayed message was deleted
        val popupMessage = cometchatPopUpMenuMessage?.getCurrentMessage()
        if (popupMessage != null) {
            val updatedMessage = messages.find { it.id == popupMessage.id }
            if (updatedMessage != null && updatedMessage.deletedAt > 0) {
                cometchatPopUpMenuMessage?.dismiss()
                clearCurrentLongPressedMessage()
            }
        }
        
        val newCount = messageAdapter.itemCount
        
        // Auto-scroll to bottom when new messages are added and user was at bottom
        if (newCount > previousCount && wasAtBottom) {
            if (scrollToBottomOnNewMessage) {
                // Always scroll to bottom when scrollToBottomOnNewMessage is enabled
                scrollToLastItem()
            } else {
                // Only scroll if user is within 5 items of the bottom
                val lastVisiblePosition = linearLayoutManager.findLastVisibleItemPosition()
                if (previousCount == 0 || (previousCount - 1) - lastVisiblePosition < 5) {
                    scrollToLastItem()
                }
            }
        }
    }

    private fun handleProgressState(inProgress: Boolean) {
        // Only hide pagination indicators when progress completes
        // The indicators are shown in handleScroll when pagination is triggered
        if (!inProgress) {
            if (isPaginatingPrevious) {
                topPaginationIndicator?.visibility = View.GONE
                isPaginatingPrevious = false
            }
            if (isPaginatingNext) {
                bottomPaginationIndicator?.visibility = View.GONE
                isPaginatingNext = false
            }
        }
    }

    /**
     * Handles conversation starter replies from the ViewModel.
     * Updates the conversation starter view with the list of suggestions.
     *
     * @param replies The list of conversation starter suggestions.
     */
    private fun handleConversationStarterReplies(replies: List<String>) {
        aiConversationStarterView?.setReplyList(replies)
    }

    /**
     * Handles conversation starter UI state changes.
     * Shows loading, error, or loaded state in the conversation starter view.
     *
     * @param state The current conversation starter UI state.
     */
    private fun handleConversationStarterUIState(state: ConversationStarterUIState) {
        when (state) {
            is ConversationStarterUIState.Loading -> {
                aiConversationStarterView?.showLoadingView()
                attachConversationStarterView()
            }
            is ConversationStarterUIState.Error -> {
                aiConversationStarterView?.showErrorView()
            }
            is ConversationStarterUIState.Loaded -> {
                // Replies are handled by handleConversationStarterReplies
            }
            is ConversationStarterUIState.Idle -> {
                // No action needed for idle state
            }
        }
    }

    /**
     * Handles the remove conversation starter event.
     * Detaches the conversation starter view from the footer.
     */
    private fun handleRemoveConversationStarter() {
        detachConversationStarterView()
    }

    /**
     * Handles conversation summary UI state changes.
     * Shows loading, error, or loaded state in the conversation summary view.
     *
     * @param state The current conversation summary UI state.
     */
    private fun handleConversationSummaryUIState(state: ConversationSummaryUIState) {
        when (state) {
            is ConversationSummaryUIState.Loading -> {
                aiConversationSummaryView?.showLoadingView()
                attachConversationSummaryView()
            }
            is ConversationSummaryUIState.Loaded -> {
                aiConversationSummaryView?.setSummary(state.summary)
                attachConversationSummaryView()
            }
            is ConversationSummaryUIState.Error -> {
                aiConversationSummaryView?.showErrorView()
            }
            is ConversationSummaryUIState.Idle -> {
                detachConversationSummaryView()
            }
        }
    }

    /**
     * Handles the remove conversation summary event.
     * Detaches the conversation summary view from the header.
     */
    private fun handleRemoveConversationSummary() {
        detachConversationSummaryView()
    }

    /**
     * Handles smart replies UI state changes.
     * Shows loading, error, loaded, or idle state in the smart replies view.
     *
     * @param state The current smart replies UI state.
     */
    private fun handleSmartRepliesUIState(state: SmartRepliesUIState) {
        when (state) {
            is SmartRepliesUIState.Loading -> {
                aiSmartRepliesView?.showLoadingView()
                attachSmartRepliesView()
            }
            is SmartRepliesUIState.Loaded -> {
                aiSmartRepliesView?.setSmartReplies(state.replies)
                attachSmartRepliesView()
            }
            is SmartRepliesUIState.Error -> {
                aiSmartRepliesView?.showErrorView()
            }
            is SmartRepliesUIState.Idle -> {
                detachSmartRepliesView()
            }
        }
    }

    /**
     * Handles a delete confirmation request emitted by the ViewModel.
     *
     * When the user selects the "Delete" option, the ViewModel emits a request
     * via [CometChatMessageListViewModel.deleteConfirmationRequest]. The presentation
     * layer should show a confirmation dialog and call [CometChatMessageListViewModel.deleteMessage]
     * if the user confirms.
     *
     * @param message The [BaseMessage] that the user wants to delete.
     */
    private fun handleDeleteConfirmationRequest(message: BaseMessage) {
        // Show delete confirmation dialog using CometChatConfirmDialog
        val dialog = CometChatConfirmDialog(context)
        
        // Set icon
        dialog.setConfirmDialogIcon(
            androidx.core.content.res.ResourcesCompat.getDrawable(
                resources, R.drawable.cometchat_ic_delete, null
            )
        )
        
        // Set text content
        dialog.setTitleText(context.getString(R.string.cometchat_delete_message_title))
        dialog.setSubtitleText(context.getString(R.string.cometchat_delete_message_subtitle))
        dialog.setPositiveButtonText(context.getString(R.string.cometchat_delete))
        dialog.setNegativeButtonText(context.getString(R.string.cometchat_cancel))
        
        // Set click listeners
        dialog.setOnPositiveButtonClick {
            viewModel?.deleteMessage(message)
            dialog.dismiss()
        }
        dialog.setOnNegativeButtonClick {
            dialog.dismiss()
        }
        
        // Apply style if set
        style.deleteDialogStyle?.let { dialogStyle ->
            dialog.setStyle(dialogStyle)
        }
        
        // Configure dialog behavior
        dialog.setConfirmDialogElevation(0)
        dialog.setCancelable(false)
        
        dialog.show()
    }

    /**
     * Handles a message translation completion emitted by the ViewModel.
     *
     * When the ViewModel successfully translates a message, it emits the updated
     * message via [CometChatMessageListViewModel.messageTranslated]. The presentation
     * layer updates the message in the adapter to reflect the translated text.
     *
     * @param message The [BaseMessage] with updated metadata containing the translated text.
     */
    private fun handleMessageTranslated(message: BaseMessage) {
        messageAdapter.updateMessage(message)
    }

    /**
     * Attaches the conversation starter view to the footer container.
     */
    private fun attachConversationStarterView() {
        aiConversationStarterView?.let { view ->
            if (view.parent == null) {
                footerViewContainer?.removeAllViews()
                footerViewContainer?.addView(view)
                footerViewContainer?.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Detaches the conversation starter view from the footer container.
     */
    private fun detachConversationStarterView() {
        aiConversationStarterView?.let { view ->
            footerViewContainer?.removeView(view)
            if (footerViewContainer?.childCount == 0) {
                footerViewContainer?.visibility = View.GONE
            }
        }
    }

    /**
     * Attaches the conversation summary view to the footer container.
     * The conversation summary appears at the bottom of the message list.
     */
    private fun attachConversationSummaryView() {
        aiConversationSummaryView?.let { view ->
            if (view.parent == null) {
                footerViewContainer?.removeAllViews()
                footerViewContainer?.addView(view)
                footerViewContainer?.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Detaches the conversation summary view from the footer container.
     */
    private fun detachConversationSummaryView() {
        aiConversationSummaryView?.let { view ->
            footerViewContainer?.removeView(view)
            if (footerViewContainer?.childCount == 0) {
                footerViewContainer?.visibility = View.GONE
            }
        }
    }

    /**
     * Attaches the smart replies view to the footer container.
     * The smart replies view appears above the message composer area.
     */
    private fun attachSmartRepliesView() {
        aiSmartRepliesView?.let { view ->
            if (view.parent == null) {
                // Set the UID for the smart replies view
                val uid = user?.uid ?: group?.guid ?: ""
                view.setUid(uid)
                
                footerViewContainer?.removeAllViews()
                footerViewContainer?.addView(view)
                footerViewContainer?.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Detaches the smart replies view from the footer container.
     */
    private fun detachSmartRepliesView() {
        aiSmartRepliesView?.let { view ->
            footerViewContainer?.removeView(view)
            if (footerViewContainer?.childCount == 0) {
                footerViewContainer?.visibility = View.GONE
            }
        }
    }

    private fun showLoadingState() {
        if (hideLoadingState) return
        
        if (customLoadingStateView != null) {
            customViewContainer?.removeAllViews()
            customViewContainer?.addView(customLoadingStateView)
            customViewContainer?.visibility = View.VISIBLE
        } else {
            loadingStateView?.visibility = View.VISIBLE
            shimmerEffectFrame?.startShimmer()
        }
        
        emptyStateView?.visibility = View.GONE
        errorStateView?.visibility = View.GONE
        messageListLayout?.visibility = View.GONE
    }

    private fun showLoadedState() {
        loadingStateView?.visibility = View.GONE
        shimmerEffectFrame?.stopShimmer()
        emptyStateView?.visibility = View.GONE
        errorStateView?.visibility = View.GONE
        customViewContainer?.visibility = View.GONE
        messageListLayout?.visibility = View.VISIBLE
        
        onLoad?.invoke()
    }

    private fun showEmptyState() {
        if (hideEmptyState) {
            messageListLayout?.visibility = View.VISIBLE
            return
        }
        
        loadingStateView?.visibility = View.GONE
        shimmerEffectFrame?.stopShimmer()
        errorStateView?.visibility = View.GONE
        messageListLayout?.visibility = View.GONE
        
        // Priority: AI Assistant Empty Chat Greeting View > Custom Empty State View > Default Empty State
        when {
            aiAssistantEmptyChatGreetingView != null -> {
                customViewContainer?.removeAllViews()
                customViewContainer?.addView(aiAssistantEmptyChatGreetingView)
                customViewContainer?.visibility = View.VISIBLE
                emptyStateView?.visibility = View.GONE
            }
            customEmptyStateView != null -> {
                customViewContainer?.removeAllViews()
                customViewContainer?.addView(customEmptyStateView)
                customViewContainer?.visibility = View.VISIBLE
                emptyStateView?.visibility = View.GONE
            }
            else -> {
                customViewContainer?.visibility = View.GONE
                emptyStateView?.visibility = View.VISIBLE
                updateEmptyStateContent()
            }
        }
        
        // Update AI suggested messages in empty state
        updateAISuggestedMessagesView()
        
        onEmpty?.invoke()
    }

    private fun showErrorState(error: Throwable) {
        if (hideErrorState) return
        
        loadingStateView?.visibility = View.GONE
        shimmerEffectFrame?.stopShimmer()
        emptyStateView?.visibility = View.GONE
        messageListLayout?.visibility = View.GONE
        
        if (customErrorStateView != null) {
            customViewContainer?.removeAllViews()
            customViewContainer?.addView(customErrorStateView)
            customViewContainer?.visibility = View.VISIBLE
            errorStateView?.visibility = View.GONE
        } else {
            customViewContainer?.visibility = View.GONE
            errorStateView?.visibility = View.VISIBLE
        }
        
        onError?.invoke(error)
    }

    private fun updateEmptyStateContent() {
        val entity = user ?: group
        when (entity) {
            is User -> {
                emptyTitle?.text = context.getString(R.string.cometchat_say_hello, entity.name)
                emptySubtitle?.text = context.getString(R.string.cometchat_start_conversation)
            }
            is Group -> {
                emptyTitle?.text = context.getString(R.string.cometchat_no_messages_yet)
                emptySubtitle?.text = context.getString(R.string.cometchat_be_first_to_send_message)
            }
            else -> {
                emptyTitle?.text = context.getString(R.string.cometchat_no_messages_yet)
                emptySubtitle?.text = ""
            }
        }
    }

    private fun updateNewMessageIndicator() {
        if (newMessageCount > 0 && !isUserAtBottom) {
            newMessageIndicator?.visibility = View.VISIBLE
            newMessageBadge?.setCount(newMessageCount)
        } else {
            newMessageIndicator?.visibility = View.GONE
        }
    }

    // ========================================
    // Public Configuration Methods
    // ========================================

    /**
     * Sets the user for this message list.
     * Configures the ViewModel and fetches messages if autoFetch is enabled.
     *
     * @param user The user to display messages for
     */
    fun setUser(user: User) {
        this.user = user
        this.group = null
        
        viewModel?.setUser(
            user = user,
            parentMessageId = parentMessageId,
            messagesRequestBuilder = messagesRequestBuilder
        )
        
        // Set UID for conversation starter view
        aiConversationStarterView?.setUid(user.uid)
        
        // Set UID for smart replies view
        aiSmartRepliesView?.setUid(user.uid)
        
        if (autoFetch) {
            if (startFromUnreadMessages) {
                viewModel?.fetchMessagesWithUnreadCount()
            } else {
                viewModel?.fetchMessages()
            }
        }
    }

    /**
     * Sets the group for this message list.
     * Configures the ViewModel and fetches messages if autoFetch is enabled.
     *
     * @param group The group to display messages for
     */
    fun setGroup(group: Group) {
        this.group = group
        this.user = null
        
        viewModel?.setGroup(
            group = group,
            parentMessageId = parentMessageId,
            messagesRequestBuilder = messagesRequestBuilder
        )
        
        // Set UID for conversation starter view
        aiConversationStarterView?.setUid(group.guid)
        
        // Set UID for smart replies view
        aiSmartRepliesView?.setUid(group.guid)
        
        if (autoFetch) {
            if (startFromUnreadMessages) {
                viewModel?.fetchMessagesWithUnreadCount()
            } else {
                viewModel?.fetchMessages()
            }
        }
    }

    /**
     * Sets the parent message ID for threaded conversations.
     *
     * @param parentMessageId The parent message ID (-1 for main conversation)
     */
    fun setParentMessageId(parentMessageId: Long) {
        this.parentMessageId = parentMessageId
    }

    /**
     * Sets the ViewModel for this message list.
     *
     * @param viewModel The ViewModel to use
     */
    fun setViewModel(viewModel: CometChatMessageListViewModel) {
        this.viewModel = viewModel
        isExternalViewModel = true
        viewModel.initSoundManager(context)
        viewModel.setDisableReceipt(disableReceipt)
        viewModel.setHideDeleteMessage(false)
        viewModel.setStartFromUnreadMessages(startFromUnreadMessages)
        viewModel.setUnreadThreshold(unreadMessageThreshold)
        viewModel.setDisableSoundForMessages(disableSoundForMessages)
        if (customSoundForMessages != 0) {
            viewModel.setCustomSoundForMessages(customSoundForMessages)
        }
        observeViewModel()
    }

    /**
     * Sets the style for this message list.
     *
     * @param style The style to apply
     */
    fun setStyle(style: CometChatMessageListStyle) {
        this.style = style
        applyStyle()
    }

    /**
     * Gets the current style.
     *
     * @return The current style
     */
    fun getStyle(): CometChatMessageListStyle = style

    /**
     * Sets the messages request builder for custom message fetching.
     *
     * @param builder The builder to use
     */
    fun setMessagesRequestBuilder(builder: MessagesRequest.MessagesRequestBuilder?) {
        this.messagesRequestBuilder = builder
    }

    /**
     * Sets the message types to fetch.
     *
     * @param types List of message types
     */
    fun setMessagesTypes(types: List<String>) {
        this.messagesTypes = types
    }

    /**
     * Sets the message categories to fetch.
     *
     * @param categories List of message categories
     */
    fun setMessagesCategories(categories: List<String>) {
        this.messagesCategories = categories
    }

    // ========================================
    // Behavior Configuration
    // ========================================

    fun setScrollToBottomOnNewMessage(enabled: Boolean) {
        this.scrollToBottomOnNewMessage = enabled
    }

    /**
     * Enables or disables swipe to reply functionality.
     *
     * When enabled, users can swipe left or right on a message to trigger
     * the reply action. The swipe gesture will call [CometChatMessageListViewModel.onMessageReply]
     * which emits a [CometChatMessageEvent.ReplyToMessage] event.
     *
     * @param enabled `true` to enable swipe to reply, `false` to disable.
     *
     * @see isSwipeToReplyEnabled
     */
    fun setSwipeToReplyEnabled(enabled: Boolean) {
        this.swipeToReplyEnabled = enabled
        if (enabled) {
            initializeItemTouchHelper()
        } else {
            itemTouchHelper?.attachToRecyclerView(null)
        }
    }

    /**
     * Returns whether swipe to reply is enabled.
     *
     * @return `true` if swipe to reply is enabled, `false` otherwise.
     *
     * @see setSwipeToReplyEnabled
     */
    fun isSwipeToReplyEnabled(): Boolean = swipeToReplyEnabled

    /**
     * Initializes the ItemTouchHelper for swipe to reply functionality.
     *
     * Uses a custom ItemTouchHelper.Callback that limits swipe distance (WhatsApp-style),
     * draws a reply icon behind the swiped item, and triggers reply when threshold is reached.
     */
    private fun initializeItemTouchHelper() {
        val replyIcon = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.cometchat_ic_reply_to_message)
        val iconTint = CometChatTheme.getIconTintSecondary(context)
        replyIcon?.setTint(iconTint)

        val circlePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = CometChatTheme.getNeutralColor300(context)
            style = android.graphics.Paint.Style.FILL
        }

        val swipeThreshold = resources.getDimensionPixelSize(R.dimen.cometchat_72dp).toFloat()
        val iconSize = resources.getDimensionPixelSize(R.dimen.cometchat_24dp)
        val maxCircleRadius = iconSize * 0.85f
        val isRtl = resources.configuration.layoutDirection == android.view.View.LAYOUT_DIRECTION_RTL

        itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {

            private var swipeTriggered = false

            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                // Disable swipe for ACTION and CALL category messages
                val position = viewHolder.adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val message = messageAdapter.getMessages().getOrNull(position)
                    if (message != null) {
                        val category = message.category
                        if (category.equals(CometChatConstants.CATEGORY_ACTION, ignoreCase = true) ||
                            category.equals(CometChatConstants.CATEGORY_CALL, ignoreCase = true)) {
                            return makeMovementFlags(0, 0)
                        }
                        // Also disable for deleted messages and messages not yet sent
                        if (message.deletedAt > 0 || message.sentAt == 0L || message.id == 0L) {
                            return makeMovementFlags(0, 0)
                        }
                    }
                }

                // LTR: swipe left-to-right (START to END) = RIGHT
                // RTL: swipe right-to-left (START to END) = LEFT
                val swipeFlag = if (isRtl) ItemTouchHelper.LEFT else ItemTouchHelper.RIGHT
                return makeMovementFlags(0, swipeFlag)
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Not used — reply is triggered in onChildDraw when threshold is crossed
            }

            override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
                return 2.0f
            }

            override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
                return Float.MAX_VALUE
            }

            override fun getSwipeVelocityThreshold(defaultValue: Float): Float {
                return Float.MAX_VALUE
            }

            override fun onChildDraw(
                c: android.graphics.Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                if (actionState != ItemTouchHelper.ACTION_STATE_SWIPE) {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    return
                }

                val clampedDx = if (dX > 0) dX.coerceAtMost(swipeThreshold) else dX.coerceAtLeast(-swipeThreshold)

                super.onChildDraw(c, recyclerView, viewHolder, clampedDx, dY, actionState, isCurrentlyActive)

                val itemView = viewHolder.itemView
                val progress = (kotlin.math.abs(clampedDx) / swipeThreshold).coerceIn(0f, 1f)

                if (progress > 0f) {
                    val iconMargin = resources.getDimensionPixelSize(R.dimen.cometchat_padding_4)
                    val centerY = itemView.top + itemView.height / 2f
                    // LTR: icon on the left (start) side; RTL: icon on the right (start) side
                    val centerX = if (isRtl) {
                        itemView.right - iconMargin - iconSize / 2f
                    } else {
                        itemView.left + iconMargin + iconSize / 2f
                    }

                    // Draw growing circular background
                    val circleRadius = maxCircleRadius * progress
                    circlePaint.alpha = (progress * 255).toInt()
                    c.drawCircle(centerX, centerY, circleRadius, circlePaint)

                    // Draw reply icon
                    replyIcon?.let { icon ->
                        icon.alpha = (progress * 255).toInt()
                        val halfIcon = iconSize / 2
                        icon.setBounds(
                            (centerX - halfIcon).toInt(),
                            (centerY - halfIcon).toInt(),
                            (centerX + halfIcon).toInt(),
                            (centerY + halfIcon).toInt()
                        )
                        icon.draw(c)
                    }
                }

                // Trigger reply when threshold is reached
                if (kotlin.math.abs(clampedDx) >= swipeThreshold && isCurrentlyActive && !swipeTriggered) {
                    swipeTriggered = true
                    val position = viewHolder.adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val message = messageAdapter.getMessages().getOrNull(position)
                        message?.let { viewModel?.onMessageReply(it) }
                    }
                }

                if (!isCurrentlyActive) {
                    swipeTriggered = false
                }
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                swipeTriggered = false
            }
        })
        itemTouchHelper?.attachToRecyclerView(recyclerViewMessageList)
    }

    fun setDisableSoundForMessages(disabled: Boolean) {
        this.disableSoundForMessages = disabled
        viewModel?.setDisableSoundForMessages(disabled)
    }

    fun setCustomSoundForMessages(rawRes: Int) {
        this.customSoundForMessages = rawRes
        viewModel?.setCustomSoundForMessages(rawRes)
    }

    fun setAutoFetch(enabled: Boolean) {
        this.autoFetch = enabled
    }

    /**
     * Whether to start the message list from the first unread message.
     * 
     * When set to `true`, the message list will scroll to position the first unread
     * message in view when initially loaded. This requires calling this property
     * BEFORE setting the user or group.
     * 
     * Usage:
     * ```kotlin
     * messageList.isStartFromUnreadMessages = true
     * messageList.user = user // Set user AFTER enabling this feature
     * ```
     */
    var isStartFromUnreadMessages: Boolean
        get() = startFromUnreadMessages
        set(value) {
            startFromUnreadMessages = value
            viewModel?.setStartFromUnreadMessages(value)
        }

    fun setUnreadMessageThreshold(threshold: Int) {
        this.unreadMessageThreshold = threshold
        viewModel?.setUnreadThreshold(threshold)
    }

    fun setDisableReceipt(disabled: Boolean) {
        this.disableReceipt = disabled
        viewModel?.setDisableReceipt(disabled)
    }

    /**
     * Sets the text formatters for message text rendering.
     *
     * Text formatters are used to customize how message text is rendered,
     * including mentions, links, markdown, and other text transformations.
     * The formatters are propagated through the component chain:
     * MessageList → MessageAdapter → MessageBubble → TextMessageBubble.
     *
     * @param formatters The list of [CometChatTextFormatter] instances to use for text rendering.
     *                   Pass an empty list or null to clear formatters.
     *
     * @see CometChatTextFormatter
     */
    fun setTextFormatters(formatters: List<CometChatTextFormatter>?) {
        val formatterList = formatters ?: emptyList()
        _textFormatters = formatterList
        // Propagate to adapter for message rendering
        messageAdapter.textFormatters = formatterList
    }

    /**
     * Gets the current text formatters.
     *
     * @return The list of text formatters, or an empty list if none are set.
     */
    fun getTextFormatters(): List<CometChatTextFormatter> {
        return _textFormatters
    }

    /**
     * Processes and adds the [CometChatMentionsFormatter] to the list of text
     * formatters. This method creates a mentions formatter, configures it with
     * the mentionAllLabel settings if set, and stores it for later use.
     *
     * This is called during initialization to set up @mentions functionality.
     */
    private fun processMentionsFormatter() {
        // Create mentions formatter directly
        cometchatMentionsFormatter = CometChatMentionsFormatter(context)
        
        // Configure mentionAllLabel if already set
        if (!mentionAllLabelId.isNullOrEmpty() && !mentionAllLabel.isNullOrEmpty()) {
            cometchatMentionsFormatter?.setMentionAllLabel(mentionAllLabelId!!, mentionAllLabel!!)
        }
        
        // Add to formatters list and propagate
        val formatters = mutableListOf<CometChatTextFormatter>()
        cometchatMentionsFormatter?.let { formatters.add(it) }
        setTextFormatters(formatters)
    }

    /**
     * Returns the instance of the mentions formatter.
     *
     * @return The [CometChatMentionsFormatter] instance, or null if not initialized.
     */
    fun getMentionsFormatter(): CometChatMentionsFormatter? {
        return cometchatMentionsFormatter
    }

    /**
     * Disables or enables the @all mention feature.
     *
     * When disabled, users will not be able to mention all members of a group
     * using the @all syntax.
     *
     * @param disable `true` to disable @all mentions, `false` to enable.
     */
    fun setDisableMentionAll(disable: Boolean) {
        cometchatMentionsFormatter?.setDisableMentionAll(disable)
    }

    /**
     * Sets the custom label for @all mentions.
     *
     * This allows customizing the ID and display text used when mentioning
     * all members of a group.
     *
     * @param id The unique identifier (such as a group GUID) for which the mention all label should be set.
     * @param mentionAllLabel The custom label to display when mentioning all members.
     *
     * If either parameter is null or empty, this method does nothing.
     */
    fun setMentionAllLabelId(id: String?, mentionAllLabel: String?) {
        if (!id.isNullOrEmpty() && !mentionAllLabel.isNullOrEmpty()) {
            cometchatMentionsFormatter?.setMentionAllLabel(id, mentionAllLabel)
            this.mentionAllLabelId = id
            this.mentionAllLabel = mentionAllLabel
        }
    }

    /**
     * Enables or disables AI conversation starters in the message list.
     *
     * When enabled and the message list is empty, AI-generated conversation
     * starter suggestions will be fetched and displayed to help users
     * begin conversations.
     *
     * @param enable `true` to enable AI conversation starters, `false` to disable.
     */
    fun setEnableConversationStarter(enable: Boolean) {
        this.enableConversationStarter = enable
        viewModel?.setEnableConversationStarter(enable)
    }

    /**
     * Returns whether AI conversation starters are enabled.
     *
     * @return `true` if AI conversation starters are enabled, `false` otherwise.
     */
    fun isEnableConversationStarter(): Boolean = enableConversationStarter

    /**
     * Sets the style for the AI conversation starter view.
     *
     * @param styleResId The style resource ID for the AI conversation starter view.
     */
    fun setAIConversationStarterStyle(@androidx.annotation.StyleRes styleResId: Int) {
        this.conversationStarterStyle = styleResId
        aiConversationStarterView?.setStyle(styleResId)
    }

    /**
     * Gets the style resource ID for the AI conversation starter view.
     *
     * @return The style resource ID for the AI conversation starter view.
     */
    fun getAIConversationStarterStyle(): Int = conversationStarterStyle

    /**
     * Enables or disables AI conversation summary in the message list.
     *
     * When enabled and there are many unread messages (above threshold),
     * an AI-generated conversation summary will be fetched and displayed
     * at the top of the message list to help users catch up on the conversation.
     *
     * @param enable `true` to enable AI conversation summary, `false` to disable.
     */
    fun setEnableConversationSummary(enable: Boolean) {
        this.enableConversationSummary = enable
        viewModel?.setEnableConversationSummary(enable)
    }

    /**
     * Returns whether AI conversation summary is enabled.
     *
     * @return `true` if AI conversation summary is enabled, `false` otherwise.
     */
    fun isEnableConversationSummary(): Boolean = enableConversationSummary

    /**
     * Sets the style for the AI conversation summary view.
     *
     * @param styleResId The style resource ID for the AI conversation summary view.
     */
    fun setAIConversationSummaryStyle(@androidx.annotation.StyleRes styleResId: Int) {
        this.conversationSummaryStyle = styleResId
        aiConversationSummaryView?.setStyle(styleResId)
    }

    /**
     * Gets the style resource ID for the AI conversation summary view.
     *
     * @return The style resource ID for the AI conversation summary view.
     */
    fun getAIConversationSummaryStyle(): Int = conversationSummaryStyle

    /**
     * Enables or disables AI smart replies in the message list.
     *
     * When enabled, AI-generated smart reply suggestions will be fetched
     * automatically when text messages are received from other users.
     * The suggestions appear in the footer area and allow users to quickly
     * respond with one tap.
     *
     * @param enable `true` to enable AI smart replies, `false` to disable.
     */
    fun setEnableSmartReplies(enable: Boolean) {
        this.enableSmartReplies = enable
        viewModel?.setEnableSmartReplies(enable)
    }

    /**
     * Returns whether AI smart replies are enabled.
     *
     * @return `true` if AI smart replies are enabled, `false` otherwise.
     */
    fun isEnableSmartReplies(): Boolean = enableSmartReplies

    /**
     * Sets the keywords for AI smart replies filtering.
     *
     * When keywords are set, smart replies will only be fetched if the
     * received message contains at least one of the keywords (case-insensitive).
     * An empty list means all messages will trigger smart replies.
     *
     * @param keywords The list of keywords for AI smart replies filtering.
     */
    fun setSmartRepliesKeywords(keywords: List<String>) {
        this.smartRepliesKeywords = keywords
        viewModel?.setSmartReplyKeywords(keywords)
    }

    /**
     * Gets the keywords for AI smart replies filtering.
     *
     * @return The list of keywords for AI smart replies filtering.
     */
    fun getSmartRepliesKeywords(): List<String> = smartRepliesKeywords

    /**
     * Sets the delay duration before fetching smart replies after receiving a message.
     *
     * This delay allows for multiple messages to arrive before triggering
     * the smart replies fetch. If another message arrives before the delay
     * expires, the timer is reset.
     *
     * @param delayMs Delay in milliseconds. Default is 10000 (10 seconds).
     */
    fun setSmartRepliesDelayDuration(delayMs: Int) {
        this.smartRepliesDelayDuration = delayMs
        viewModel?.setSmartRepliesDelay(delayMs)
    }

    /**
     * Gets the delay duration before fetching smart replies.
     *
     * @return The delay duration in milliseconds.
     */
    fun getSmartRepliesDelayDuration(): Int = smartRepliesDelayDuration

    /**
     * Sets the style for the AI smart replies view.
     *
     * @param styleResId The style resource ID for the AI smart replies view.
     */
    fun setAISmartRepliesStyle(@androidx.annotation.StyleRes styleResId: Int) {
        this.smartRepliesStyle = styleResId
        aiSmartRepliesView?.setStyle(styleResId)
    }

    /**
     * Gets the style resource ID for the AI smart replies view.
     *
     * @return The style resource ID for the AI smart replies view.
     */
    fun getAISmartRepliesStyle(): Int = smartRepliesStyle

    /**
     * Sets the style for the AI smart replies view using a typed style object.
     *
     * This method allows programmatic styling of the AI smart replies view
     * without requiring an XML style resource.
     *
     * @param style The [CometChatAISmartRepliesStyle] to apply.
     */
    fun setAISmartRepliesStyle(style: CometChatAISmartRepliesStyle) {
        aiSmartRepliesView?.setStyle(style)
    }

    /**
     * Sets the style for the AI conversation starter view using a typed style object.
     *
     * This method allows programmatic styling of the AI conversation starter view
     * without requiring an XML style resource.
     *
     * @param style The [CometChatAIConversationStarterStyle] to apply.
     */
    fun setAIConversationStarterStyle(style: CometChatAIConversationStarterStyle) {
        aiConversationStarterView?.setStyle(style)
    }

    /**
     * Sets the style for the AI conversation summary view using a typed style object.
     *
     * This method allows programmatic styling of the AI conversation summary view
     * without requiring an XML style resource.
     *
     * @param style The [CometChatAIConversationSummaryStyle] to apply.
     */
    fun setAIConversationSummaryStyle(style: CometChatAIConversationSummaryStyle) {
        aiConversationSummaryView?.setStyle(style)
    }

    // ========================================
    // AI Assistant Empty Chat Greeting View
    // ========================================

    /**
     * Sets the layout resource for the AI assistant empty chat greeting view.
     *
     * When set, this custom layout is used instead of the default empty state view
     * when the chat is empty in AI assistant/agent chat mode. This allows customization
     * of the greeting message and appearance for AI-powered conversations.
     *
     * @param layoutResId The layout resource ID for the AI assistant empty chat greeting view,
     *                    or 0 to use the default empty state view.
     *
     * @see getAIAssistantEmptyChatGreetingView
     */
    fun setAIAssistantEmptyChatGreetingView(@androidx.annotation.LayoutRes layoutResId: Int) {
        this.aiAssistantEmptyChatGreetingViewResId = layoutResId
        if (layoutResId != 0) {
            try {
                aiAssistantEmptyChatGreetingView = View.inflate(context, layoutResId, null)
            } catch (e: Exception) {
                aiAssistantEmptyChatGreetingView = null
            }
        } else {
            aiAssistantEmptyChatGreetingView = null
        }
    }

    /**
     * Returns the layout resource ID for the AI assistant empty chat greeting view.
     *
     * @return The layout resource ID, or 0 if using the default empty state view.
     *
     * @see setAIAssistantEmptyChatGreetingView
     */
    fun getAIAssistantEmptyChatGreetingView(): Int = aiAssistantEmptyChatGreetingViewResId

    // ========================================
    // AI Assistant Suggested Messages
    // ========================================

    /**
     * Sets the list of suggested messages for the AI assistant empty chat state.
     *
     * When set, these suggested messages are displayed in the empty state view
     * when the chat is empty in AI assistant/agent chat mode. Users can tap on
     * a suggested message to quickly start a conversation.
     *
     * @param suggestedMessages The list of suggested message strings to display,
     *                          or an empty list to clear suggestions.
     *
     * @see getAIAssistantSuggestedMessages
     * @see setAiAssistantSuggestedMessagesVisibility
     */
    fun setAIAssistantSuggestedMessages(suggestedMessages: List<String>?) {
        this.aiAssistantSuggestedMessages = suggestedMessages ?: emptyList()
        updateAISuggestedMessagesView()
    }

    /**
     * Returns the current list of AI assistant suggested messages.
     *
     * @return The list of suggested message strings, or an empty list if none are set.
     *
     * @see setAIAssistantSuggestedMessages
     */
    fun getAIAssistantSuggestedMessages(): List<String> = aiAssistantSuggestedMessages

    /**
     * Sets the visibility of AI assistant suggested messages using Android View visibility constants.
     *
     * When set to [View.VISIBLE], the suggested messages are displayed in the empty state.
     * When set to [View.GONE] or [View.INVISIBLE], the suggested messages are hidden.
     *
     * @param visibility The visibility value: [View.VISIBLE], [View.INVISIBLE], or [View.GONE].
     *
     * @see getAiAssistantSuggestedMessagesVisibility
     * @see setAIAssistantSuggestedMessages
     */
    fun setAiAssistantSuggestedMessagesVisibility(visibility: Int) {
        this.aiAssistantSuggestedMessagesVisibility = visibility
        aiSuggestedMessagesContainer?.visibility = visibility
    }

    /**
     * Returns the current visibility of AI assistant suggested messages.
     *
     * @return The visibility value: [View.VISIBLE], [View.INVISIBLE], or [View.GONE].
     *
     * @see setAiAssistantSuggestedMessagesVisibility
     */
    fun getAiAssistantSuggestedMessagesVisibility(): Int = aiAssistantSuggestedMessagesVisibility

    // ========================================
    // AI Assistant Tools Configuration
    // ========================================

    /**
     * Sets the tools available for the AI assistant function calling.
     *
     * AI assistant tools allow the AI to perform custom actions during conversations.
     * Each tool is identified by a unique name (key) and has an associated
     * [ToolCallListener] that handles the tool invocation.
     *
     * When the AI assistant decides to use a tool, it will invoke the corresponding
     * listener's [ToolCallListener.call] method with the tool arguments as a JSON string.
     *
     * ## Example
     *
     * ```kotlin
     * val tools = hashMapOf<String, ToolCallListener>(
     *     "get_weather" to ToolCallListener { args ->
     *         // Parse args JSON and fetch weather data
     *         val location = JSONObject(args).getString("location")
     *         fetchWeatherForLocation(location)
     *     },
     *     "search_products" to ToolCallListener { args ->
     *         // Handle product search
     *         val query = JSONObject(args).getString("query")
     *         searchProducts(query)
     *     }
     * )
     * messageList.setAiAssistantTools(tools)
     * ```
     *
     * @param tools A [HashMap] containing tool names as keys and their corresponding
     *              [ToolCallListener] implementations as values. Pass an empty map
     *              to clear all registered tools.
     *
     * @see getAiAssistantTools
     * @see ToolCallListener
     */
    fun setAiAssistantTools(tools: HashMap<String, ToolCallListener>) {
        this.aiAssistantTools = tools
    }

    /**
     * Returns the currently registered AI assistant tools.
     *
     * @return A [HashMap] containing the registered tool names and their listeners.
     *
     * @see setAiAssistantTools
     */
    fun getAiAssistantTools(): HashMap<String, ToolCallListener> = aiAssistantTools

    /**
     * Sets the streaming speed for AI responses.
     *
     * This controls the delay (in milliseconds) between characters when streaming
     * AI assistant responses, creating a typing effect. A lower value results in
     * faster streaming, while a higher value creates a slower, more deliberate
     * typing effect.
     *
     * ## Usage
     *
     * ```kotlin
     * // Set a 50ms delay between characters for a moderate typing speed
     * messageList.setStreamingSpeed(50)
     *
     * // Set a faster streaming speed
     * messageList.setStreamingSpeed(20)
     *
     * // Reset to default streaming speed
     * messageList.setStreamingSpeed(null)
     * ```
     *
     * @param streamingSpeed The delay in milliseconds between characters when
     *                       streaming AI responses. Pass null to use the default
     *                       streaming speed.
     *
     * @see getStreamingSpeed
     */
    fun setStreamingSpeed(streamingSpeed: Int?) {
        this.streamingSpeed = streamingSpeed
    }

    /**
     * Returns the currently configured streaming speed for AI responses.
     *
     * @return The delay in milliseconds between characters, or null if using
     *         the default streaming speed.
     *
     * @see setStreamingSpeed
     */
    fun getStreamingSpeed(): Int? = streamingSpeed

    // ========================================
    // Date/Time Formatting Configuration
    // ========================================

    /**
     * Sets the time format for message timestamps.
     *
     * When set, this format is used to display the time portion of message
     * timestamps in the message list. The format is applied to the status
     * info view of each message bubble.
     *
     * ## Example
     *
     * ```kotlin
     * // Use 24-hour format
     * messageList.setTimeFormat(SimpleDateFormat("HH:mm", Locale.getDefault()))
     *
     * // Use 12-hour format with AM/PM
     * messageList.setTimeFormat(SimpleDateFormat("h:mm a", Locale.getDefault()))
     * ```
     *
     * @param timeFormat The [SimpleDateFormat] to use for message timestamps,
     *                   or `null` to use the default format ("h:mm a").
     *
     * @see getTimeFormat
     * @see setDateFormat
     * @see setDateTimeFormatter
     */
    fun setTimeFormat(timeFormat: SimpleDateFormat?) {
        this.timeFormat = timeFormat
        timeFormat?.let { messageAdapter.timeFormat = it }
    }

    /**
     * Returns the currently configured time format for message timestamps.
     *
     * @return The current [SimpleDateFormat] for timestamps, or `null` if using
     *         the default format.
     *
     * @see setTimeFormat
     */
    fun getTimeFormat(): SimpleDateFormat? = timeFormat

    /**
     * Sets the date format for date separators in the message list.
     *
     * When set, this format is used to display dates in the sticky date headers
     * and date separator views between messages from different days.
     *
     * ## Example
     *
     * ```kotlin
     * // Use short date format
     * messageList.setDateFormat(SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()))
     *
     * // Use long date format
     * messageList.setDateFormat(SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()))
     * ```
     *
     * @param dateFormat The [SimpleDateFormat] to use for date separators,
     *                   or `null` to use the default format.
     *
     * @see getDateFormat
     * @see setTimeFormat
     * @see setDateTimeFormatter
     */
    fun setDateFormat(dateFormat: SimpleDateFormat?) {
        this.dateFormat = dateFormat
        messageAdapter.dateSeparatorFormat = dateFormat
    }

    /**
     * Returns the currently configured date format for date separators.
     *
     * @return The current [SimpleDateFormat] for date separators, or `null` if using
     *         the default format.
     *
     * @see setDateFormat
     */
    fun getDateFormat(): SimpleDateFormat? = dateFormat

    /**
     * Sets a custom date/time formatter callback for advanced date/time formatting.
     *
     * The [DateTimeFormatterCallback] provides fine-grained control over how dates
     * and times are formatted in different contexts (today, yesterday, last week,
     * older dates). This is useful for localization or custom date display logic.
     *
     * When set, this callback takes precedence over the formats set via
     * [setTimeFormat] and [setDateFormat] for the specific date categories
     * it handles.
     *
     * ## Example
     *
     * ```kotlin
     * messageList.setDateTimeFormatter(object : DateTimeFormatterCallback {
     *     override fun time(timestamp: Long): String? = "Custom Time"
     *     override fun today(timestamp: Long): String? = "Today"
     *     override fun yesterday(timestamp: Long): String? = "Yesterday"
     *     override fun lastWeek(timestamp: Long): String? = "Last Week"
     *     override fun otherDays(timestamp: Long): String? {
     *         return SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
     *             .format(Date(timestamp))
     *     }
     * })
     * ```
     *
     * @param dateTimeFormatter The [DateTimeFormatterCallback] for custom formatting,
     *                          or `null` to use the default formatting logic.
     *
     * @see getDateTimeFormatter
     * @see setTimeFormat
     * @see setDateFormat
     */
    fun setDateTimeFormatter(dateTimeFormatter: DateTimeFormatterCallback?) {
        this.dateTimeFormatter = dateTimeFormatter
        messageAdapter.dateTimeFormatter = dateTimeFormatter
    }

    /**
     * Returns the currently configured date/time formatter callback.
     *
     * @return The current [DateTimeFormatterCallback], or `null` if using
     *         the default formatting logic.
     *
     * @see setDateTimeFormatter
     */
    fun getDateTimeFormatter(): DateTimeFormatterCallback? = dateTimeFormatter

    /**
     * Updates the AI suggested messages view in the empty state.
     *
     * This method populates the suggested messages container with clickable
     * message chips that users can tap to start a conversation.
     */
    private fun updateAISuggestedMessagesView() {
        aiSuggestedMessagesContainer?.removeAllViews()
        
        if (aiAssistantSuggestedMessages.isEmpty() || 
            aiAssistantSuggestedMessagesVisibility != View.VISIBLE) {
            aiSuggestedMessagesContainer?.visibility = View.GONE
            return
        }
        
        aiSuggestedMessagesContainer?.visibility = View.VISIBLE
        
        for (suggestion in aiAssistantSuggestedMessages) {
            val chip = createSuggestedMessageChip(suggestion)
            aiSuggestedMessagesContainer?.addView(chip)
        }
    }

    /**
     * Creates a clickable chip view for a suggested message.
     *
     * @param message The suggested message text.
     * @return A configured MaterialButton styled as a chip.
     */
    private fun createSuggestedMessageChip(message: String): View {
        val chip = com.google.android.material.button.MaterialButton(
            context,
            null,
            com.google.android.material.R.attr.materialButtonOutlinedStyle
        ).apply {
            text = message
            isAllCaps = false
            
            // Apply styling from style object
            if (style.aiAssistantSuggestedMessageTextColor != 0) {
                setTextColor(style.aiAssistantSuggestedMessageTextColor)
            }
            if (style.aiAssistantSuggestedMessageTextAppearance != 0) {
                setTextAppearance(style.aiAssistantSuggestedMessageTextAppearance)
            }
            if (style.aiAssistantSuggestedMessageBackgroundColor != 0) {
                setBackgroundColor(style.aiAssistantSuggestedMessageBackgroundColor)
            }
            if (style.aiAssistantSuggestedMessageCornerRadius > 0) {
                cornerRadius = style.aiAssistantSuggestedMessageCornerRadius
            }
            if (style.aiAssistantSuggestedMessageStrokeWidth > 0) {
                strokeWidth = style.aiAssistantSuggestedMessageStrokeWidth
            }
            if (style.aiAssistantSuggestedMessageStrokeColor != 0) {
                strokeColor = android.content.res.ColorStateList.valueOf(
                    style.aiAssistantSuggestedMessageStrokeColor
                )
            }
            if (style.aiAssistantSuggestedMessageEndIcon != null) {
                icon = style.aiAssistantSuggestedMessageEndIcon
                iconGravity = com.google.android.material.button.MaterialButton.ICON_GRAVITY_END
            }
            if (style.aiAssistantSuggestedMessageEndIconTint != 0) {
                iconTint = android.content.res.ColorStateList.valueOf(
                    style.aiAssistantSuggestedMessageEndIconTint
                )
            }
            
            // Set layout params with margins
            val layoutParams = com.google.android.flexbox.FlexboxLayout.LayoutParams(
                com.google.android.flexbox.FlexboxLayout.LayoutParams.WRAP_CONTENT,
                com.google.android.flexbox.FlexboxLayout.LayoutParams.WRAP_CONTENT
            )
            val margin = Utils.convertDpToPx(context, 4)
            layoutParams.setMargins(margin, margin, margin, margin)
            this.layoutParams = layoutParams
            
            // Handle click - emit compose message event
            setOnClickListener {
                val uid = user?.uid ?: group?.guid ?: ""
                CometChatEvents.emitUIEvent(CometChatUIEvent.ComposeMessage(id = uid, text = message))
            }
        }
        return chip
    }

    // ========================================
    // Visibility Configuration
    // ========================================

    /**
     * Shows or hides avatars in the message list.
     *
     * When enabled, avatars are shown for incoming messages (left-aligned).
     * This is the backward-compatible boolean method that internally calls
     * the int-based [setAvatarVisibility] method.
     *
     * @param show `true` to show avatars, `false` to hide them.
     *
     * @see setAvatarVisibility
     * @see getAvatarVisibility
     */
    fun showAvatar(show: Boolean) {
        setAvatarVisibility(if (show) View.VISIBLE else View.GONE)
    }

    /**
     * Sets the visibility of avatars in the message list using Android View visibility constants.
     *
     * This method provides Java API parity with the reference implementation.
     * When set to [View.VISIBLE], avatars are shown for incoming messages.
     * When set to [View.GONE] or [View.INVISIBLE], avatars are hidden.
     *
     * @param visibility The visibility value: [View.VISIBLE], [View.INVISIBLE], or [View.GONE].
     *
     * @see showAvatar
     * @see getAvatarVisibility
     */
    fun setAvatarVisibility(visibility: Int) {
        this.avatarVisibility = visibility
        this.hideAvatar = visibility != View.VISIBLE
        messageAdapter.showAvatar = (visibility == View.VISIBLE)
    }

    /**
     * Returns the current visibility of avatars in the message list.
     *
     * @return The visibility value: [View.VISIBLE], [View.INVISIBLE], or [View.GONE].
     *
     * @see setAvatarVisibility
     * @see showAvatar
     */
    fun getAvatarVisibility(): Int = avatarVisibility

    /**
     * Shows or hides read receipts in the message list.
     *
     * When enabled, read/delivery receipts are shown on outgoing messages.
     * This is the backward-compatible boolean method that internally calls
     * the int-based [setReceiptsVisibility] method.
     *
     * @param show `true` to show receipts, `false` to hide them.
     *
     * @see setReceiptsVisibility
     * @see getReceiptsVisibility
     */
    fun showReceipts(show: Boolean) {
        setReceiptsVisibility(if (show) View.VISIBLE else View.GONE)
    }

    /**
     * Sets the visibility of read receipts in the message list using Android View visibility constants.
     *
     * This method provides Java API parity with the reference implementation.
     * When set to [View.VISIBLE], read/delivery receipts are shown on outgoing messages.
     * When set to [View.GONE] or [View.INVISIBLE], receipts are hidden.
     *
     * @param visibility The visibility value: [View.VISIBLE], [View.INVISIBLE], or [View.GONE].
     *
     * @see showReceipts
     * @see getReceiptsVisibility
     */
    fun setReceiptsVisibility(visibility: Int) {
        this.receiptsVisibility = visibility
        this.hideReceipts = visibility != View.VISIBLE
        messageAdapter.disableReadReceipt = (visibility != View.VISIBLE)
    }

    /**
     * Returns the current visibility of read receipts in the message list.
     *
     * @return The visibility value: [View.VISIBLE], [View.INVISIBLE], or [View.GONE].
     *
     * @see setReceiptsVisibility
     * @see showReceipts
     */
    fun getReceiptsVisibility(): Int = receiptsVisibility

    /**
     * Shows or hides reactions on message bubbles.
     *
     * When enabled, reactions are shown on messages that have them.
     * This is the backward-compatible boolean method that internally calls
     * the int-based [setReactionVisibility] method.
     *
     * @param show `true` to show reactions, `false` to hide them.
     *
     * @see setReactionVisibility
     * @see getReactionVisibility
     */
    fun showReactions(show: Boolean) {
        setReactionVisibility(if (show) View.VISIBLE else View.GONE)
    }

    /**
     * Sets the visibility of reactions on message bubbles using Android View visibility constants.
     *
     * This method provides Java API parity with the reference implementation.
     * When set to [View.VISIBLE], reactions are shown on messages that have them.
     * When set to [View.GONE] or [View.INVISIBLE], reactions are hidden.
     *
     * @param visibility The visibility value: [View.VISIBLE], [View.INVISIBLE], or [View.GONE].
     *
     * @see showReactions
     * @see getReactionVisibility
     */
    fun setReactionVisibility(visibility: Int) {
        this.reactionVisibility = visibility
        messageAdapter.disableReactions = (visibility != View.VISIBLE)
    }

    /**
     * Returns the current visibility of reactions in the message list.
     *
     * @return The visibility value: [View.VISIBLE], [View.INVISIBLE], or [View.GONE].
     *
     * @see setReactionVisibility
     * @see showReactions
     */
    fun getReactionVisibility(): Int = reactionVisibility

    /**
     * Shows or hides the error state view.
     *
     * When enabled, the error state view is shown when an error occurs.
     * This is the backward-compatible boolean method that internally calls
     * the int-based [setErrorStateVisibility] method.
     *
     * @param show `true` to show error state, `false` to hide it.
     *
     * @see setErrorStateVisibility
     * @see getErrorStateVisibility
     */
    fun showErrorState(show: Boolean) {
        setErrorStateVisibility(if (show) View.VISIBLE else View.GONE)
    }

    /**
     * Sets the visibility of the error state view using Android View visibility constants.
     *
     * This method provides Java API parity with the reference implementation.
     * When set to [View.VISIBLE], the error state view is shown when an error occurs.
     * When set to [View.GONE] or [View.INVISIBLE], the error state is hidden.
     *
     * @param visibility The visibility value: [View.VISIBLE], [View.INVISIBLE], or [View.GONE].
     *
     * @see showErrorState
     * @see getErrorStateVisibility
     */
    fun setErrorStateVisibility(visibility: Int) {
        this.errorStateVisibility = visibility
        this.hideErrorState = visibility != View.VISIBLE
    }

    /**
     * Returns the current visibility of the error state view.
     *
     * @return The visibility value: [View.VISIBLE], [View.INVISIBLE], or [View.GONE].
     *
     * @see setErrorStateVisibility
     * @see showErrorState
     */
    fun getErrorStateVisibility(): Int = errorStateVisibility

    /**
     * Shows or hides group action messages (member joined, left, etc.).
     *
     * When enabled, group action messages are displayed in the message list.
     * This is the backward-compatible boolean method that internally calls
     * the int-based [setGroupActionMessageVisibility] method.
     *
     * @param show `true` to show group action messages, `false` to hide them.
     *
     * @see setGroupActionMessageVisibility
     * @see getGroupActionMessageVisibility
     */
    fun showGroupActionMessages(show: Boolean) {
        setGroupActionMessageVisibility(if (show) View.VISIBLE else View.GONE)
    }

    /**
     * Sets the visibility of group action messages using Android View visibility constants.
     *
     * This method provides Java API parity with the reference implementation.
     * When set to [View.VISIBLE], group action messages (member joined, left, etc.) are shown.
     * When set to [View.GONE] or [View.INVISIBLE], group action messages are hidden.
     *
     * @param visibility The visibility value: [View.VISIBLE], [View.INVISIBLE], or [View.GONE].
     *
     * @see showGroupActionMessages
     * @see getGroupActionMessageVisibility
     */
    fun setGroupActionMessageVisibility(visibility: Int) {
        this.groupActionMessageVisibility = visibility
        this.hideGroupActionMessages = visibility != View.VISIBLE
        messageAdapter.hideGroupActionMessage = (visibility != View.VISIBLE)
    }

    /**
     * Returns the current visibility of group action messages.
     *
     * @return The visibility value: [View.VISIBLE], [View.INVISIBLE], or [View.GONE].
     *
     * @see setGroupActionMessageVisibility
     * @see showGroupActionMessages
     */
    fun getGroupActionMessageVisibility(): Int = groupActionMessageVisibility

    /**
     * Sets the visibility of sticky date headers using Android View visibility constants.
     *
     * Sticky date headers appear at the top of the message list while scrolling,
     * showing the date of the currently visible messages. This provides context
     * for when messages were sent.
     *
     * When set to [View.VISIBLE], a [StickyHeaderDecoration] is added to the RecyclerView
     * to draw date headers on top of the list. When set to [View.GONE] or [View.INVISIBLE],
     * the decoration is removed.
     *
     * @param visibility The visibility value: [View.VISIBLE], [View.INVISIBLE], or [View.GONE].
     *
     * @see getStickyDateVisibility
     */
    @Suppress("UNCHECKED_CAST")
    fun setStickyDateVisibility(visibility: Int) {
        stickyDateVisibility = visibility
        if (visibility == View.VISIBLE) {
            if (stickyHeaderDecoration == null) {
                stickyHeaderDecoration = StickyHeaderDecoration(
                    messageAdapter as StickyHeaderAdapter<RecyclerView.ViewHolder>
                )
            }
            // Only add if not already added
            stickyHeaderDecoration?.let { decoration ->
                recyclerViewMessageList?.removeItemDecoration(decoration)
                recyclerViewMessageList?.addItemDecoration(decoration, 0)
            }
        } else {
            stickyHeaderDecoration?.let { decoration ->
                recyclerViewMessageList?.removeItemDecoration(decoration)
            }
        }
    }

    /**
     * Returns the current visibility of sticky date headers.
     *
     * @return The visibility value: [View.VISIBLE], [View.INVISIBLE], or [View.GONE].
     *
     * @see setStickyDateVisibility
     */
    fun getStickyDateVisibility(): Int = stickyDateVisibility

    // ========================================
    // Message Option Visibility Configuration
    // ========================================

    /**
     * Builds a map of message option visibility settings for filtering.
     *
     * This map is used by [com.cometchat.uikit.core.utils.MessageOptionsUtils.getFilteredMessageOptions] to filter
     * the available message options based on the current visibility settings.
     *
     * @return Map of option ID to visibility (true = visible, false = hidden)
     */
    private fun buildOptionVisibilityMap(): Map<String, Boolean> {
        return mapOf(
            UIKitConstants.MessageOption.REPLY_IN_THREAD to replyInThreadOptionVisible,
            UIKitConstants.MessageOption.REPLY to replyOptionVisible,
            UIKitConstants.MessageOption.REPLY_TO_MESSAGE to replyOptionVisible,
            UIKitConstants.MessageOption.COPY to copyOptionVisible,
            UIKitConstants.MessageOption.EDIT to editOptionVisible,
            UIKitConstants.MessageOption.DELETE to deleteOptionVisible,
            UIKitConstants.MessageOption.TRANSLATE to translateOptionVisible,
            UIKitConstants.MessageOption.SHARE to shareOptionVisible,
            UIKitConstants.MessageOption.MESSAGE_PRIVATELY to messagePrivatelyOptionVisible,
            UIKitConstants.MessageOption.MESSAGE_INFORMATION to messageInfoOptionVisible,
            UIKitConstants.MessageOption.REPORT to reportOptionVisible,
            UIKitConstants.MessageOption.MARK_AS_UNREAD to markAsUnreadOptionVisible,
            UIKitConstants.MessageOption.REACT to reactOptionVisible
        )
    }

    /**
     * Sets the visibility of the "Reply in Thread" option in the message context menu.
     *
     * When set to [View.VISIBLE], users can start or continue a threaded conversation
     * from a message. When set to [View.GONE] or [View.INVISIBLE], the option is hidden.
     *
     * @param visibility The visibility value: [View.VISIBLE], [View.INVISIBLE], or [View.GONE].
     *
     * @see getReplyInThreadOptionVisibility
     */
    fun setReplyInThreadOptionVisibility(visibility: Int) {
        this.replyInThreadOptionVisible = (visibility == View.VISIBLE)
    }

    /**
     * Returns the current visibility of the "Reply in Thread" option.
     *
     * @return The visibility value: [View.VISIBLE] or [View.GONE].
     *
     * @see setReplyInThreadOptionVisibility
     */
    fun getReplyInThreadOptionVisibility(): Int = if (replyInThreadOptionVisible) View.VISIBLE else View.GONE

    /**
     * Sets the visibility of the "Reply" option in the message context menu.
     *
     * When set to [View.VISIBLE], users can reply to a message with a quoted preview.
     * When set to [View.GONE] or [View.INVISIBLE], the option is hidden.
     *
     * @param visibility The visibility value: [View.VISIBLE], [View.INVISIBLE], or [View.GONE].
     *
     * @see getReplyOptionVisibility
     */
    fun setReplyOptionVisibility(visibility: Int) {
        this.replyOptionVisible = (visibility == View.VISIBLE)
    }

    /**
     * Returns the current visibility of the "Reply" option.
     *
     * @return The visibility value: [View.VISIBLE] or [View.GONE].
     *
     * @see setReplyOptionVisibility
     */
    fun getReplyOptionVisibility(): Int = if (replyOptionVisible) View.VISIBLE else View.GONE

    /**
     * Sets the visibility of the "Copy" option in the message context menu.
     *
     * When set to [View.VISIBLE], users can copy message text to the clipboard.
     * When set to [View.GONE] or [View.INVISIBLE], the option is hidden.
     *
     * @param visibility The visibility value: [View.VISIBLE], [View.INVISIBLE], or [View.GONE].
     *
     * @see getCopyMessageOptionVisibility
     */
    fun setCopyMessageOptionVisibility(visibility: Int) {
        this.copyOptionVisible = (visibility == View.VISIBLE)
    }

    /**
     * Returns the current visibility of the "Copy" option.
     *
     * @return The visibility value: [View.VISIBLE] or [View.GONE].
     *
     * @see setCopyMessageOptionVisibility
     */
    fun getCopyMessageOptionVisibility(): Int = if (copyOptionVisible) View.VISIBLE else View.GONE

    /**
     * Sets the visibility of the "Edit" option in the message context menu.
     *
     * When set to [View.VISIBLE], users can edit their own sent messages.
     * When set to [View.GONE] or [View.INVISIBLE], the option is hidden.
     * Note: This option is typically only shown for the sender's own messages.
     *
     * @param visibility The visibility value: [View.VISIBLE], [View.INVISIBLE], or [View.GONE].
     *
     * @see getEditMessageOptionVisibility
     */
    fun setEditMessageOptionVisibility(visibility: Int) {
        this.editOptionVisible = (visibility == View.VISIBLE)
    }

    /**
     * Returns the current visibility of the "Edit" option.
     *
     * @return The visibility value: [View.VISIBLE] or [View.GONE].
     *
     * @see setEditMessageOptionVisibility
     */
    fun getEditMessageOptionVisibility(): Int = if (editOptionVisible) View.VISIBLE else View.GONE

    /**
     * Sets the visibility of the "Delete" option in the message context menu.
     *
     * When set to [View.VISIBLE], users can delete their own messages or (for admins)
     * delete other users' messages in groups. When set to [View.GONE] or [View.INVISIBLE],
     * the option is hidden.
     *
     * @param visibility The visibility value: [View.VISIBLE], [View.INVISIBLE], or [View.GONE].
     *
     * @see getDeleteMessageOptionVisibility
     */
    fun setDeleteMessageOptionVisibility(visibility: Int) {
        this.deleteOptionVisible = (visibility == View.VISIBLE)
    }

    /**
     * Returns the current visibility of the "Delete" option.
     *
     * @return The visibility value: [View.VISIBLE] or [View.GONE].
     *
     * @see setDeleteMessageOptionVisibility
     */
    fun getDeleteMessageOptionVisibility(): Int = if (deleteOptionVisible) View.VISIBLE else View.GONE

    /**
     * Sets the visibility of the "React" option in the message context menu.
     *
     * When set to [View.VISIBLE], users can add emoji reactions to messages.
     * When set to [View.GONE] or [View.INVISIBLE], the option is hidden.
     *
     * @param visibility The visibility value: [View.VISIBLE], [View.INVISIBLE], or [View.GONE].
     *
     * @see getMessageReactionOptionVisibility
     */
    fun setMessageReactionOptionVisibility(visibility: Int) {
        this.reactOptionVisible = (visibility == View.VISIBLE)
    }

    /**
     * Returns the current visibility of the "React" option.
     *
     * @return The visibility value: [View.VISIBLE] or [View.GONE].
     *
     * @see setMessageReactionOptionVisibility
     */
    fun getMessageReactionOptionVisibility(): Int = if (reactOptionVisible) View.VISIBLE else View.GONE

    /**
     * Sets the visibility of the "Message Info" option in the message context menu.
     *
     * When set to [View.VISIBLE], users can view detailed information about a message
     * including read receipts and delivery status. When set to [View.GONE] or [View.INVISIBLE],
     * the option is hidden.
     * Note: This option is typically only shown for the sender's own messages.
     *
     * @param visibility The visibility value: [View.VISIBLE], [View.INVISIBLE], or [View.GONE].
     *
     * @see getMessageInfoOptionVisibility
     */
    fun setMessageInfoOptionVisibility(visibility: Int) {
        this.messageInfoOptionVisible = (visibility == View.VISIBLE)
    }

    /**
     * Returns the current visibility of the "Message Info" option.
     *
     * @return The visibility value: [View.VISIBLE] or [View.GONE].
     *
     * @see setMessageInfoOptionVisibility
     */
    fun getMessageInfoOptionVisibility(): Int = if (messageInfoOptionVisible) View.VISIBLE else View.GONE

    /**
     * Sets the visibility of the "Translate" option in the message context menu.
     *
     * When set to [View.VISIBLE], users can translate message text to their preferred language.
     * When set to [View.GONE] or [View.INVISIBLE], the option is hidden.
     *
     * @param visibility The visibility value: [View.VISIBLE], [View.INVISIBLE], or [View.GONE].
     *
     * @see getTranslateMessageOptionVisibility
     */
    fun setTranslateMessageOptionVisibility(visibility: Int) {
        this.translateOptionVisible = (visibility == View.VISIBLE)
    }

    /**
     * Returns the current visibility of the "Translate" option.
     *
     * @return The visibility value: [View.VISIBLE] or [View.GONE].
     *
     * @see setTranslateMessageOptionVisibility
     */
    fun getTranslateMessageOptionVisibility(): Int = if (translateOptionVisible) View.VISIBLE else View.GONE

    /**
     * Sets the visibility of the "Share" option in the message context menu.
     *
     * When set to [View.VISIBLE], users can share message content via other apps.
     * When set to [View.GONE] or [View.INVISIBLE], the option is hidden.
     *
     * @param visibility The visibility value: [View.VISIBLE], [View.INVISIBLE], or [View.GONE].
     *
     * @see getShareMessageOptionVisibility
     */
    fun setShareMessageOptionVisibility(visibility: Int) {
        this.shareOptionVisible = (visibility == View.VISIBLE)
    }

    /**
     * Returns the current visibility of the "Share" option.
     *
     * @return The visibility value: [View.VISIBLE] or [View.GONE].
     *
     * @see setShareMessageOptionVisibility
     */
    fun getShareMessageOptionVisibility(): Int = if (shareOptionVisible) View.VISIBLE else View.GONE

    /**
     * Sets the visibility of the "Mark as Unread" option in the message context menu.
     *
     * When set to [View.VISIBLE], users can mark a conversation as unread from a specific message.
     * When set to [View.GONE] or [View.INVISIBLE], the option is hidden.
     * Note: This option is hidden by default.
     *
     * @param visibility The visibility value: [View.VISIBLE], [View.INVISIBLE], or [View.GONE].
     *
     * @see getMarkAsUnreadOptionVisibility
     */
    fun setMarkAsUnreadOptionVisibility(visibility: Int) {
        this.markAsUnreadOptionVisible = (visibility == View.VISIBLE)
    }

    /**
     * Returns the current visibility of the "Mark as Unread" option.
     *
     * @return The visibility value: [View.VISIBLE] or [View.GONE].
     *
     * @see setMarkAsUnreadOptionVisibility
     */
    fun getMarkAsUnreadOptionVisibility(): Int = if (markAsUnreadOptionVisible) View.VISIBLE else View.GONE

    // ========================================
    // Quick Reactions Configuration
    // ========================================

    /**
     * Sets the list of quick reaction emojis displayed in the message context menu.
     *
     * Quick reactions appear in a horizontal bar at the top of the message context menu,
     * allowing users to quickly add common reactions without opening the full emoji picker.
     *
     * If `null` is passed, the default reactions are used: ["👍", "❤️", "😂", "😮", "😢", "🙏"]
     *
     * @param reactions The list of emoji strings to display as quick reactions,
     *                  or `null` to use default reactions.
     *
     * @see getQuickReactions
     */
    fun setQuickReactions(reactions: List<String>?) {
        if (reactions != null) {
            this.quickReactions = reactions
        }
    }

    /**
     * Returns the current list of quick reaction emojis.
     *
     * @return The list of emoji strings displayed in the quick reaction bar.
     *
     * @see setQuickReactions
     */
    fun getQuickReactions(): List<String> = quickReactions

    /**
     * Sets the drawable resource ID for the add reaction icon in the quick reaction bar.
     *
     * The add reaction icon is displayed at the end of the quick reactions list,
     * allowing users to open the full emoji picker to add additional reactions.
     *
     * @param addReactionIcon The drawable resource ID for the add reaction icon,
     *                        or 0 to use the default icon.
     *
     * @see getAddReactionIcon
     */
    fun setAddReactionIcon(addReactionIcon: Int) {
        this.addReactionIcon = addReactionIcon
    }

    /**
     * Returns the current drawable resource ID for the add reaction icon.
     *
     * @return The drawable resource ID, or 0 if using the default icon.
     *
     * @see setAddReactionIcon
     */
    fun getAddReactionIcon(): Int = addReactionIcon

    // ========================================
    // Reactions Request Builder Configuration
    // ========================================

    /**
     * Sets the custom request builder for fetching reactions.
     *
     * When set, this builder is used to customize how reactions are fetched
     * from the server. It is passed to the reaction list bottom sheet when
     * displaying reactions for a message.
     *
     * @param builder The [ReactionsRequest.ReactionsRequestBuilder] to use for fetching reactions,
     *                or `null` to use the default request builder.
     *
     * @see getReactionsRequestBuilder
     */
    fun setReactionsRequestBuilder(builder: ReactionsRequest.ReactionsRequestBuilder?) {
        this.reactionsRequestBuilder = builder
    }

    /**
     * Returns the current reactions request builder.
     *
     * @return The current [ReactionsRequest.ReactionsRequestBuilder], or `null` if using the default.
     *
     * @see setReactionsRequestBuilder
     */
    fun getReactionsRequestBuilder(): ReactionsRequest.ReactionsRequestBuilder? = reactionsRequestBuilder

    // ========================================
    // Message Alignment Configuration
    // ========================================

    /**
     * Sets the alignment mode for messages in the list.
     *
     * This controls how messages are positioned horizontally:
     * - [UIKitConstants.MessageListAlignment.STANDARD]: Outgoing messages align to the right,
     *   incoming messages align to the left. This is the default chat layout.
     * - [UIKitConstants.MessageListAlignment.LEFT_ALIGNED]: All messages align to the left,
     *   regardless of sender. Useful for feed-style layouts.
     *
     * Note: Action messages (member joined, left, etc.) and call messages are always
     * centered regardless of this setting.
     *
     * @param alignment The [UIKitConstants.MessageListAlignment] to use.
     *
     * @see getMessageAlignment
     */
    fun setMessageAlignment(alignment: UIKitConstants.MessageListAlignment) {
        this.messageAlignment = alignment
        messageAdapter.listAlignment = alignment
    }

    /**
     * Returns the current message alignment mode.
     *
     * @return The current [UIKitConstants.MessageListAlignment].
     *
     * @see setMessageAlignment
     */
    fun getMessageAlignment(): UIKitConstants.MessageListAlignment = messageAlignment

    fun setHideAvatar(hide: Boolean) {
        setAvatarVisibility(if (hide) View.GONE else View.VISIBLE)
    }

    fun setHideReceipts(hide: Boolean) {
        setReceiptsVisibility(if (hide) View.GONE else View.VISIBLE)
    }

    fun setHideGroupActionMessages(hide: Boolean) {
        setGroupActionMessageVisibility(if (hide) View.GONE else View.VISIBLE)
    }

    fun setHideLoadingState(hide: Boolean) {
        this.hideLoadingState = hide
    }

    fun setHideEmptyState(hide: Boolean) {
        this.hideEmptyState = hide
    }

    fun setHideErrorState(hide: Boolean) {
        setErrorStateVisibility(if (hide) View.GONE else View.VISIBLE)
    }

    // ========================================
    // Bubble Margin Configuration
    // ========================================

    /**
     * Sets margins for both left and right message bubbles.
     * Delegates to the [MessageAdapter]'s [MessageAdapter.setBubbleMargin] method.
     *
     * @param top Top margin in dp
     * @param bottom Bottom margin in dp
     * @param left Left/start margin in dp
     * @param right Right/end margin in dp
     */
    fun setBubbleMargin(top: Int, bottom: Int, left: Int, right: Int) {
        messageAdapter.setBubbleMargin(top, bottom, left, right)
    }

    /**
     * Sets margins for left-aligned (incoming) message bubbles only.
     * Delegates to the [MessageAdapter]'s [MessageAdapter.setLeftBubbleMargin] method.
     *
     * @param top Top margin in dp
     * @param bottom Bottom margin in dp
     * @param left Left/start margin in dp
     * @param right Right/end margin in dp
     */
    fun setLeftBubbleMargin(top: Int, bottom: Int, left: Int, right: Int) {
        messageAdapter.setLeftBubbleMargin(top, bottom, left, right)
    }

    /**
     * Sets margins for right-aligned (outgoing) message bubbles only.
     * Delegates to the [MessageAdapter]'s [MessageAdapter.setRightBubbleMargin] method.
     *
     * @param top Top margin in dp
     * @param bottom Bottom margin in dp
     * @param left Left/start margin in dp
     * @param right Right/end margin in dp
     */
    fun setRightBubbleMargin(top: Int, bottom: Int, left: Int, right: Int) {
        messageAdapter.setRightBubbleMargin(top, bottom, left, right)
    }

    // ========================================
    // BubbleFactory Methods
    // ========================================

    /**
     * Sets the bubble factories for content rendering.
     *
     * Bubble factories determine how message content is rendered based on
     * message category and type. The provided factories replace any
     * previously registered factories. When no factory is registered for
     * a message type, [InternalContentRenderer] handles default rendering.
     *
     * Each factory must be self-describing (getCategory/getType return non-empty).
     * Factories with empty category or type are silently skipped.
     * Last factory wins for duplicates.
     *
     * Example:
     * ```kotlin
     * messageList.setBubbleFactories(listOf(
     *     MyTextBubbleFactory(),
     *     MyImageBubbleFactory()
     * ))
     * ```
     *
     * @param factories List of [BubbleFactory] instances.
     *
     * @see getBubbleFactories
     */
    fun setBubbleFactories(factories: List<BubbleFactory>) {
        // Convert List to Map
        val factoryMap = mutableMapOf<String, BubbleFactory>()
        factories.forEach { factory ->
            val cat = factory.getCategory()
            val type = factory.getType()
            if (cat.isNotEmpty() && type.isNotEmpty()) {
                factoryMap["${cat}_${type}"] = factory
            }
        }
        bubbleFactories = factoryMap
        messageAdapter.setBubbleFactories(factoryMap)
    }

    /**
     * Gets the current bubble factories map.
     *
     * @return An immutable copy of the current bubble factories map.
     */
    fun getBubbleFactories(): Map<String, BubbleFactory> = bubbleFactories.toMap()

    // ========================================
    // BubbleViewProvider Methods
    // ========================================

    /**
     * Updates all view providers on the adapter.
     */
    private fun updateAdapterProviders() {
        messageAdapter.setLeadingViewProvider(leadingViewProvider)
        messageAdapter.setHeaderViewProvider(headerViewProvider)
        messageAdapter.setReplyViewProvider(replyViewProvider)
        messageAdapter.setContentViewProvider(contentViewProvider)
        messageAdapter.setBottomViewProvider(bottomViewProvider)
        messageAdapter.setStatusInfoViewProvider(statusInfoViewProvider)
        messageAdapter.setThreadViewProvider(threadViewProvider)
        messageAdapter.setFooterViewProvider(footerViewProvider)
    }

    /**
     * Sets the provider for the leading view slot (typically avatar area).
     *
     * The leading view appears at the start of the message bubble, typically
     * used for displaying the sender's avatar on incoming messages.
     *
     * The provider receives the message and its alignment, allowing different
     * views for incoming vs outgoing messages.
     *
     * @param provider The [BubbleViewProvider] for the leading slot, or `null` to remove.
     *
     * @see BubbleViewProvider
     */
    fun setLeadingViewProvider(provider: BubbleViewProvider?) {
        this.leadingViewProvider = provider
        messageAdapter.setLeadingViewProvider(provider)
    }

    /**
     * Sets the provider for the header view slot (typically sender name).
     *
     * The header view appears above the message content, typically used for
     * displaying the sender's name in group conversations.
     *
     * @param provider The [BubbleViewProvider] for the header slot, or `null` to remove.
     */
    fun setHeaderViewProvider(provider: BubbleViewProvider?) {
        this.headerViewProvider = provider
        messageAdapter.setHeaderViewProvider(provider)
    }

    /**
     * Sets the provider for the reply view slot (quoted message preview).
     *
     * The reply view appears above the message content when the message is
     * a reply to another message, showing a preview of the quoted message.
     *
     * @param provider The [BubbleViewProvider] for the reply slot, or `null` to remove.
     */
    fun setReplyViewProvider(provider: BubbleViewProvider?) {
        this.replyViewProvider = provider
        messageAdapter.setReplyViewProvider(provider)
    }

    /**
     * Sets the provider for the content view slot.
     *
     * **Important:** Setting a content view provider overrides the factory-based
     * content rendering. Use this only when you need complete control over
     * content rendering for all message types.
     *
     * For custom rendering of specific message types, use [setBubbleFactories] instead.
     *
     * @param provider The [BubbleViewProvider] for the content slot, or `null` to use factories.
     *
     * @see setBubbleFactories
     */
    fun setContentViewProvider(provider: BubbleViewProvider?) {
        this.contentViewProvider = provider
        messageAdapter.setContentViewProvider(provider)
    }

    /**
     * Sets the provider for the bottom view slot (typically reactions).
     *
     * The bottom view appears below the message content, typically used for
     * displaying message reactions.
     *
     * @param provider The [BubbleViewProvider] for the bottom slot, or `null` to remove.
     */
    fun setBottomViewProvider(provider: BubbleViewProvider?) {
        this.bottomViewProvider = provider
        messageAdapter.setBottomViewProvider(provider)
    }

    /**
     * Sets the provider for the status info view slot (time and receipts).
     *
     * The status info view appears at the bottom of the bubble, typically
     * showing the message timestamp and read/delivery receipts.
     *
     * @param provider The [BubbleViewProvider] for the status info slot, or `null` to remove.
     */
    fun setStatusInfoViewProvider(provider: BubbleViewProvider?) {
        this.statusInfoViewProvider = provider
        messageAdapter.setStatusInfoViewProvider(provider)
    }

    /**
     * Sets the provider for the thread view slot (reply count indicator).
     *
     * The thread view appears when a message has replies, showing the reply
     * count and allowing navigation to the threaded conversation.
     *
     * @param provider The [BubbleViewProvider] for the thread slot, or `null` to remove.
     */
    fun setThreadViewProvider(provider: BubbleViewProvider?) {
        this.threadViewProvider = provider
        messageAdapter.setThreadViewProvider(provider)
    }

    /**
     * Sets the provider for the footer view slot.
     *
     * The footer view appears at the very bottom of the message item,
     * outside the bubble. Use for additional metadata or actions.
     *
     * @param provider The [BubbleViewProvider] for the footer slot, or `null` to remove.
     */
    fun setFooterViewProvider(provider: BubbleViewProvider?) {
        this.footerViewProvider = provider
        messageAdapter.setFooterViewProvider(provider)
    }

    // ========================================
    // Custom View Slots
    // ========================================

    /**
     * Sets a custom header view.
     *
     * @param view The view to display as header
     */
    fun setHeaderView(view: View?) {
        customHeaderView = view
        headerViewContainer?.removeAllViews()
        if (view != null) {
            headerViewContainer?.addView(view)
            headerViewContainer?.visibility = View.VISIBLE
        } else {
            headerViewContainer?.visibility = View.GONE
        }
    }

    /**
     * Sets a custom footer view.
     *
     * @param view The view to display as footer
     */
    fun setFooterView(view: View?) {
        customFooterView = view
        footerViewContainer?.removeAllViews()
        if (view != null) {
            footerViewContainer?.addView(view)
            footerViewContainer?.visibility = View.VISIBLE
        } else {
            footerViewContainer?.visibility = View.GONE
        }
    }

    /**
     * Sets a custom empty state view.
     *
     * @param view The view to display when empty
     */
    fun setEmptyStateView(view: View?) {
        customEmptyStateView = view
    }

    /**
     * Sets a custom error state view.
     *
     * @param view The view to display on error
     */
    fun setErrorStateView(view: View?) {
        customErrorStateView = view
    }

    /**
     * Sets a custom loading state view.
     *
     * @param view The view to display while loading
     */
    fun setLoadingStateView(view: View?) {
        customLoadingStateView = view
    }

    /**
     * Sets a custom new message indicator view.
     *
     * @param view The view to display as new message indicator
     */
    fun setNewMessageIndicatorView(view: View?) {
        customNewMessageIndicatorView = view
        // TODO: Replace default indicator with custom view
    }

    // ========================================
    // Callback Setters
    // ========================================

    fun setOnError(callback: ((Throwable) -> Unit)?) {
        this.onError = callback
    }

    fun setOnLoad(callback: (() -> Unit)?) {
        this.onLoad = callback
    }

    fun setOnEmpty(callback: (() -> Unit)?) {
        this.onEmpty = callback
    }

    fun setOnItemClick(callback: ((BaseMessage, Int) -> Unit)?) {
        this.onItemClick = callback
        messageAdapter.setOnItemClickListener(callback)
    }

    fun setOnItemLongClick(callback: ((BaseMessage, Int) -> Boolean)?) {
        this.onItemLongClick = callback
        messageAdapter.setOnItemLongClickListener(callback)
    }

    fun setOnThreadRepliesClick(callback: ((BaseMessage) -> Unit)?) {
        this.onThreadRepliesClick = callback
    }
    
    /**
     * Sets the callback for when the "Message Privately" option is selected.
     *
     * When the user selects the "Message Privately" option, the ViewModel fetches
     * the message sender's user details and emits them via [CometChatMessageListViewModel.messageSenderFetched].
     * This callback is invoked with the fetched [User] object.
     *
     * @param callback The callback to invoke with the fetched user, or null to remove.
     */
    fun setOnMessagePrivately(callback: ((User) -> Unit)?) {
        this.onMessagePrivately = callback
    }

    fun setOnReactionClick(callback: ((BaseMessage, String) -> Unit)?) {
        this.onReactionClick = callback
    }

    fun setOnReactionLongClick(callback: ((BaseMessage, String) -> Unit)?) {
        this.onReactionLongClick = callback
    }

    fun setOnAddMoreReactionsClick(callback: ((BaseMessage) -> Unit)?) {
        this.onAddMoreReactionsClick = callback
    }

    // ========================================
    // Message Context Menu Callback Setters
    // ========================================

    /**
     * Sets the listener for message option click events.
     *
     * This listener is invoked when a user clicks on any message option
     * in the message context menu (e.g., reply, copy, edit, delete, etc.).
     *
     * @param listener The [MessageOptionClickListener] to set, or `null` to remove.
     *
     * @see getMessageOptionClickListener
     */
    fun setMessageOptionClickListener(listener: MessageOptionClickListener?) {
        this.messageOptionClickListener = listener
    }

    /**
     * Returns the current message option click listener.
     *
     * @return The current [MessageOptionClickListener], or `null` if not set.
     *
     * @see setMessageOptionClickListener
     */
    fun getMessageOptionClickListener(): MessageOptionClickListener? = messageOptionClickListener

    /**
     * Sets a callback that **replaces** the default message options for a given message.
     *
     * When the callback returns a non-null list, that list is used as the entire set of
     * options (default options are discarded). When it returns `null`, the default options
     * are shown as usual.
     *
     * Takes precedence over [addOptions] — when this callback returns a non-null list,
     * [addOptions] is not invoked.
     *
     * @param callback A function receiving the [BaseMessage] and returning either a
     *   replacement list or `null` to keep defaults.
     */
    fun setOptions(callback: (BaseMessage) -> List<CometChatMessageOption>?) {
        viewModel?.setOptions(callback)
    }

    /**
     * Sets a callback that **appends** additional options after the default message options.
     *
     * Invoked only when [setOptions] is not set or returns `null`.
     *
     * @param callback A function receiving the [BaseMessage] and returning a list of
     *   additional [CometChatMessageOption] to append.
     */
    fun addOptions(callback: (BaseMessage) -> List<CometChatMessageOption>) {
        viewModel?.addOptions(callback)
    }

    /**
     * Sets the listener for quick reaction click events.
     *
     * This listener is invoked when a user clicks on a quick reaction emoji
     * in the message context menu's quick reaction bar.
     *
     * @param listener The [ReactionClickListener] to set, or `null` to remove.
     *
     * @see getQuickReactionClickListener
     */
    fun setQuickReactionClickListener(listener: ReactionClickListener?) {
        this.quickReactionClickListener = listener
    }

    /**
     * Returns the current quick reaction click listener.
     *
     * @return The current [ReactionClickListener], or `null` if not set.
     *
     * @see setQuickReactionClickListener
     */
    fun getQuickReactionClickListener(): ReactionClickListener? = quickReactionClickListener

    /**
     * Sets the listener for emoji picker click events.
     *
     * This listener is invoked when a user clicks on the emoji picker button
     * (typically the "+" or "add more reactions" button) in the message context menu.
     *
     * @param listener The [EmojiPickerClickListener] to set, or `null` to remove.
     *
     * @see getEmojiPickerClick
     */
    fun setEmojiPickerClick(listener: EmojiPickerClickListener?) {
        this.emojiPickerClickListener = listener
    }

    /**
     * Returns the current emoji picker click listener.
     *
     * @return The current [EmojiPickerClickListener], or `null` if not set.
     *
     * @see setEmojiPickerClick
     */
    fun getEmojiPickerClick(): EmojiPickerClickListener? = emojiPickerClickListener

    // ========================================
    // Message Context Menu Callback Invocation Methods
    // ========================================

    /**
     * Handles a message option click by delegating to the ViewModel for business-logic
     * actions (copy, edit, reply, delete, mark unread, translate) and falling through
     * to the presentation layer for UI-context actions (thread navigation, share,
     * message info, report, message privately).
     *
     * The integrator's [MessageOptionClickListener] is always invoked regardless of
     * whether the ViewModel handled the option, allowing integrators to hook into
     * all option clicks.
     *
     * @param message The message for which the option was clicked.
     * @param optionId The ID of the clicked option (e.g., "reply", "copy", "edit", "delete").
     * @param optionName The display name of the clicked option.
     */
    fun invokeMessageOptionClick(message: BaseMessage, optionId: String, optionName: String) {
        // Create text formatter callback if formatters are available
        val textFormatterCallback = if (_textFormatters.isNotEmpty()) {
            CometChatMessageListViewModel.TextFormatterCallback { ctx, msg, formattingType, alignment, text ->
                FormatterUtils.getFormattedText(ctx, msg, formattingType, alignment, text, _textFormatters)
            }
        } else {
            null
        }

        // 1. Delegate to ViewModel for business-logic actions
        val handled = viewModel?.handleMessageOptionClick(context, optionId, message, textFormatterCallback) ?: false

        // 2. If ViewModel didn't handle it, handle locally (UI-context actions)
        if (!handled) {
            handleLocalMessageOption(optionId, message)
        }

        // 3. Always invoke integrator callback
        messageOptionClickListener?.onMessageOptionClick(message, optionId, optionName)

        // 4. Clear state after processing (Property 12: State Cleanup After Selection)
        currentLongPressedMessage = null
    }

    /**
     * Handles message options that require UI context (Activity, navigation, dialogs).
     *
     * These options are not handled by the ViewModel because they need Android UI
     * components like Intents, dialogs, or navigation transitions.
     *
     * @param optionId The option ID from [UIKitConstants.MessageOption].
     * @param message The [BaseMessage] the option was clicked on.
     */
    private fun handleLocalMessageOption(optionId: String, message: BaseMessage) {
        when (optionId) {
            UIKitConstants.MessageOption.REPLY_IN_THREAD -> {
                onThreadRepliesClick?.invoke(message)
            }
            UIKitConstants.MessageOption.SHARE -> {
                shareMessage(message)
            }
            UIKitConstants.MessageOption.MESSAGE_INFORMATION -> {
                // Show message information bottom sheet
                showMessageInformationBottomSheet(message)
            }
            UIKitConstants.MessageOption.REPORT -> {
                // Show flag message dialog
                showFlagMessageDialog(message)
            }
            UIKitConstants.MessageOption.MESSAGE_PRIVATELY -> {
                // Fetch the message sender and emit via callback
                viewModel?.fetchMessageSender(message)
            }
        }
    }
    
    /**
     * Shows the flag message dialog for reporting a message.
     *
     * @param message The [BaseMessage] to report.
     */
    private fun showFlagMessageDialog(message: BaseMessage) {
        val flagDialog = CometChatFlagMessageDialog(context, message)
        currentFlagDialog = flagDialog
        
        // Set flag reasons - fetch from SDK
        com.cometchat.chat.core.CometChat.getFlagReasons(object : com.cometchat.chat.core.CometChat.CallbackListener<List<com.cometchat.chat.models.FlagReason>>() {
            override fun onSuccess(reasons: List<com.cometchat.chat.models.FlagReason>?) {
                flagDialog.setFlagReasons(reasons)
            }
            
            override fun onError(e: com.cometchat.chat.exceptions.CometChatException?) {
                // Use empty list if fetch fails
                flagDialog.setFlagReasons(emptyList())
            }
        })
        
        // Set listeners
        flagDialog.setOnPositiveButtonClickListener { flagDetail ->
            flagDialog.hidePositiveButtonProgressBar(false)
            viewModel?.flagMessage(message, flagDetail.reasonId ?: "", flagDetail.remark ?: "")
        }
        
        flagDialog.setOnCancelButtonClickListener {
            flagDialog.dismiss()
            currentFlagDialog = null
        }
        
        flagDialog.setOnCloseButtonClickListener {
            flagDialog.dismiss()
            currentFlagDialog = null
        }
        
        flagDialog.show()
    }

    /**
     * Shares a message's content via Android's share intent.
     *
     * For text messages, shares the formatted text content (applying text formatters if available).
     * For image messages, downloads the image via Glide and shares it as a file.
     * For other media messages, downloads the file and shares it.
     *
     * @param message The [BaseMessage] to share.
     */
    private fun shareMessage(message: BaseMessage) {
        when {
            message is com.cometchat.chat.models.TextMessage -> {
                // Share text message with formatted text (similar to copy)
                try {
                    val rawText = message.text
                    val shareText = if (_textFormatters.isNotEmpty()) {
                        FormatterUtils.getFormattedText(
                            context,
                            message,
                            UIKitConstants.FormattingType.MESSAGE_BUBBLE,
                            UIKitConstants.MessageBubbleAlignment.RIGHT,
                            rawText,
                            _textFormatters
                        ).toString()
                    } else {
                        rawText
                    }
                    if (shareText.isNotEmpty()) {
                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_SUBJECT, context.getString(R.string.cometchat_share))
                            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(
                            android.content.Intent.createChooser(shareIntent, context.getString(R.string.cometchat_share))
                        )
                    }
                } catch (e: Exception) {
                    android.util.Log.e("CometChatMessageList", "Share failed: ${e.message}")
                }
            }
            message is com.cometchat.chat.models.MediaMessage && 
                message.type == com.cometchat.chat.constants.CometChatConstants.MESSAGE_TYPE_IMAGE -> {
                // Share image message via Glide bitmap download
                val attachment = message.attachment ?: return
                val fileUrl = attachment.fileUrl ?: return
                val mediaName = attachment.fileName ?: "shared_image"
                val mimeType = attachment.fileMimeType ?: "image/*"
                
                com.bumptech.glide.Glide.with(context)
                    .asBitmap()
                    .load(fileUrl)
                    .into(object : com.bumptech.glide.request.target.CustomTarget<android.graphics.Bitmap>() {
                        override fun onResourceReady(
                            resource: android.graphics.Bitmap,
                            transition: com.bumptech.glide.request.transition.Transition<in android.graphics.Bitmap>?
                        ) {
                            try {
                                @Suppress("DEPRECATION")
                                val path = android.provider.MediaStore.Images.Media.insertImage(
                                    context.contentResolver,
                                    resource,
                                    mediaName,
                                    null
                                )
                                if (path != null) {
                                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                        action = android.content.Intent.ACTION_SEND
                                        putExtra(android.content.Intent.EXTRA_STREAM, android.net.Uri.parse(path))
                                        type = mimeType
                                    }
                                    context.startActivity(
                                        android.content.Intent.createChooser(shareIntent, context.getString(R.string.cometchat_share))
                                    )
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("CometChatMessageList", "Failed to share image: ${e.message}")
                            }
                        }
                        
                        override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {
                            // No-op
                        }
                        
                        override fun onLoadFailed(errorDrawable: android.graphics.drawable.Drawable?) {
                            android.util.Log.e("CometChatMessageList", "Failed to load image for sharing")
                        }
                    })
            }
            message is com.cometchat.chat.models.MediaMessage -> {
                // Share other media types (video, audio, files) via file download
                val attachment = message.attachment ?: return
                val fileUrl = attachment.fileUrl ?: return
                val fileName = "${message.id}${attachment.fileName ?: "media_file"}"
                val mimeType = attachment.fileMimeType ?: "*/*"
                
                MediaUtils.downloadFileAndShare(
                    context = context,
                    fileUrl = fileUrl,
                    fileName = fileName,
                    mimeType = mimeType,
                    onError = { e ->
                        android.util.Log.e("CometChatMessageList", "Failed to download and share media: ${e.message}")
                    }
                )
            }
        }
    }

    /**
     * Shows the message information bottom sheet for the given message.
     *
     * This displays detailed receipt information including delivered/read timestamps
     * for user conversations, or a list of receipts for group conversations.
     *
     * @param message The [BaseMessage] to show information for.
     */
    private fun showMessageInformationBottomSheet(message: BaseMessage) {
        val activity = context as? androidx.fragment.app.FragmentActivity ?: return
        
        val bottomSheet = CometChatMessageInformationBottomSheet.newInstance(message)
        bottomSheet.setBubbleFactories(bubbleFactories)
        bottomSheet.setOnErrorListener { exception ->
            onError?.invoke(exception)
        }
        bottomSheet.show(activity.supportFragmentManager, "message_information")
    }

    /**
     * Invokes the quick reaction click listener for the given message and reaction.
     *
     * This method should be called by the consumer when a quick reaction emoji
     * is selected from the context menu's quick reaction bar.
     *
     * @param message The message for which the reaction was clicked.
     * @param reaction The emoji reaction that was clicked (e.g., "👍", "❤️", "😂").
     */
    fun invokeQuickReactionClick(message: BaseMessage, reaction: String) {
        quickReactionClickListener?.onReactionClick(message, reaction)
    }

    /**
     * Invokes the emoji picker click listener.
     *
     * This method should be called by the consumer when the emoji picker button
     * (typically the "+" or "add more reactions" button) is clicked in the context menu.
     */
    fun invokeEmojiPickerClick() {
        emojiPickerClickListener?.onEmojiPickerClick()
    }

    /**
     * Shows the emoji keyboard bottom sheet for adding a reaction to the given message.
     *
     * When an emoji is selected, it adds the reaction via the ViewModel and dismisses
     * both the emoji keyboard and the popup menu.
     *
     * @param message The message to add a reaction to.
     */
    private fun showEmojiKeyBoard(message: BaseMessage) {
        if (emojiKeyboard == null) {
            emojiKeyboard = CometChatEmojiKeyboard()
        }

        emojiKeyboard?.show(context)
        emojiKeyboard?.setOnClick(object : EmojiKeyBoardView.OnClick {
            override fun onClick(emoji: String) {
                viewModel?.addReaction(message, emoji)
                emojiKeyboard?.dismiss()
                cometchatPopUpMenuMessage?.dismiss()
            }

            override fun onLongClick(emoji: String) {
                // No-op, matching Java reference
            }
        })
    }

    /**
     * Opens the reaction list bottom sheet dialog for a given message and emoji.
     * Mirrors the Java UIKit's openReactionListBottomSheet behavior:
     * - Creates a CometChatReactionList view
     * - Sets the selected reaction, base message, and style
     * - Shows it in a BottomSheetDialog at 50% screen height
     *
     * @param emoji The selected emoji for the reaction, or "All" for all reactions.
     * @param baseMessage The message for which the reactions are being displayed.
     */
    private fun openReactionListBottomSheet(emoji: String, baseMessage: BaseMessage) {
        val dialog = BottomSheetDialog(context)
        bottomSheetDialog = dialog

        val reactionList = CometChatReactionList(context)
        reactionList.setSelectedReaction(emoji)
        reactionList.setBaseMessage(baseMessage)
        reactionList.setOnEmpty {
            dialog.dismiss()
        }

        dialog.setContentView(reactionList)
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            if (bottomSheet != null) {
                bottomSheet.setBackgroundResource(android.R.color.transparent)
                val behavior = BottomSheetBehavior.from(bottomSheet)
                val halfScreenHeight = (context.resources.displayMetrics.heightPixels * 0.5).toInt()
                behavior.peekHeight = halfScreenHeight
                behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                bottomSheet.layoutParams.height = halfScreenHeight
                bottomSheet.requestLayout()
            }
        }
        dialog.setCancelable(true)
        dialog.show()
    }

    /**
     * Opens the emoji keyboard as a bottom sheet for adding a reaction to a message.
     * When an emoji is selected, it is added as a reaction to the given message.
     *
     * @param message The message to add a reaction to.
     */
    private fun openEmojiKeyboardForReaction(message: BaseMessage) {
        if (emojiKeyboard == null) {
            emojiKeyboard = CometChatEmojiKeyboard()
        }
        emojiKeyboard?.show(context)
        emojiKeyboard?.setOnClick(object : EmojiKeyBoardView.OnClick {
            override fun onClick(emoji: String) {
                viewModel?.addReaction(message, emoji)
                emojiKeyboard?.dismiss()
            }

            override fun onLongClick(emoji: String) {
                // No-op for long click
            }
        })
    }

    /**
     * Opens the message popup menu with the given options for the specified message.
     *
     * Configures the popup menu with style, reactions visibility, click listeners,
     * and converts [CometChatMessageOption] items to [MenuItem] items before showing.
     *
     * @param originalOptions The filtered list of message options to display.
     * @param message The message for which the popup is being shown.
     * @param bubble The message bubble view used as the anchor for positioning.
     */
    private fun openMessageOptionBottomSheet(
        originalOptions: List<CometChatMessageOption>,
        message: BaseMessage,
        bubble: com.cometchat.uikit.kotlin.presentation.shared.messagebubble.CometChatMessageBubble
    ) {
        val popup = cometchatPopUpMenuMessage ?: return

        popup.setAddReactionIcon(addReactionIcon)
        popup.setReceiptsVisibility(receiptsVisibility)
        popup.setMessageAlignment(messageAlignment)

        // Quick reactions visibility conditions
        val moderationStatus = getModerationStatusString(message)
        if (message.category == UIKitConstants.MessageCategory.INTERACTIVE
            || getMessageReactionOptionVisibility() != View.VISIBLE
            || moderationStatus == UIKitConstants.ModerationConstants.DISAPPROVED
        ) {
            popup.setQuickReactionsVisibility(View.GONE)
        } else {
            popup.setQuickReactionsVisibility(View.VISIBLE)
        }

        // Emoji picker click
        popup.setEmojiPickerClickListener {
            if (emojiPickerClickListener != null) {
                emojiPickerClickListener?.onEmojiPickerClick()
            } else {
                showEmojiKeyBoard(message)
            }
            popup.dismiss()
        }

        // Reaction click
        popup.setReactionClickListener(
            com.cometchat.uikit.kotlin.presentation.messagelist.ReactionClickListener { baseMessage, reaction ->
                invokeQuickReactionClick(baseMessage, reaction)
                // Default: add reaction via ViewModel when no custom listener
                if (quickReactionClickListener == null) {
                    viewModel?.addReaction(baseMessage, reaction)
                }
                popup.dismiss()
            }
        )

        popup.setTextFormatters(_textFormatters)
        popup.setQuickReactions(quickReactions)

        // Convert CometChatMessageOption → MenuItem
        val menuItems = originalOptions.map { option ->
            CometChatPopupMenu.MenuItem(
                id = option.id,
                name = option.title,
                startIcon = if (option.icon != 0)
                    androidx.core.content.res.ResourcesCompat.getDrawable(resources, option.icon, context.theme) else null,
                startIconTint = option.iconTintColor,
                textColor = option.titleColor,
                textAppearance = option.titleAppearance
            )
        }
        popup.setMenuItems(menuItems)

        // Menu item click handler
        // Note: The popup menu adapter already invokes option.onClick before this listener.
        // This listener handles the built-in handler and integrator callback.
        // Order: 1. Custom onClick (in popup adapter) → 2. Built-in handler → 3. Integrator callback
        popup.setOnMenuItemClickListener(
            com.cometchat.uikit.kotlin.presentation.messagelist.OnMenuItemClickListener { id, name ->
                val msg = currentLongPressedMessage ?: message
                // invokeMessageOptionClick handles:
                // - Delegating to ViewModel for business-logic actions
                // - Handling UI-context actions locally
                // - Always invoking the integrator callback at the end
                invokeMessageOptionClick(msg, id, name)
                popup.dismiss()
            }
        )

        popup.show(bubble.getContentView() ?: bubble, this, message)
    }

    /**
     * Extracts the moderation status string from a [BaseMessage].
     *
     * Only [TextMessage] and [MediaMessage] have moderation status.
     * Returns the lowercase name of the moderation status, or `null` if not available.
     */
    private fun getModerationStatusString(message: BaseMessage): String? {
        return when (message) {
            is com.cometchat.chat.models.TextMessage -> message.moderationStatus?.name?.lowercase()
            is com.cometchat.chat.models.MediaMessage -> message.moderationStatus?.name?.lowercase()
            else -> null
        }
    }

    /**
     * Gets the current long-pressed message.
     *
     * This is useful for consumers who need to know which message was long-pressed
     * when handling message option clicks.
     *
     * @return The current long-pressed message, or `null` if no message is being long-pressed.
     */
    fun getCurrentLongPressedMessage(): BaseMessage? = currentLongPressedMessage

    /**
     * Clears the current long-pressed message.
     *
     * This should be called by the consumer after handling the message option click
     * or when the context menu is dismissed.
     */
    fun clearCurrentLongPressedMessage() {
        currentLongPressedMessage = null
    }

    // ========================================
    // Navigation Methods
    // ========================================

    /**
     * Scrolls to a specific message by ID.
     *
     * @param messageId The ID of the message to scroll to
     */
    fun gotoMessage(messageId: Long) {
        viewModel?.goToMessage(messageId)
    }

    /**
     * Scrolls to the bottom of the message list by reloading messages from the server.
     * 
     * This matches the Java implementation behavior:
     * 1. Resets the new message count
     * 2. Stops any ongoing scroll
     * 3. Clears the current message list and resets the request
     * 4. Clears any pending scroll-to-message navigation
     * 5. Fetches fresh messages from the server (with or without unread count based on position)
     * 6. Hides the new message indicator
     */
    fun scrollToBottom() {
        newMessageCount = 0
        recyclerViewMessageList?.stopScroll()
        viewModel?.clear()
        viewModel?.clearScrollToMessage()
        if (isUserAtBottom) {
            viewModel?.fetchMessages()
        } else {
            viewModel?.fetchMessagesWithUnreadCount()
        }
        newMessageIndicator?.visibility = View.GONE
    }

    private fun scrollToMessage(messageId: Long) {
        android.util.Log.d("MessagePreviewClick", "scrollToMessage called with messageId: $messageId")
        val position = messageAdapter.findMessagePosition(messageId)
        android.util.Log.d("MessagePreviewClick", "scrollToMessage - position found: $position")
        if (position >= 0) {
            // Scroll to position with center offset
            val centerOffset = recyclerViewMessageList?.height?.div(2) ?: 0
            android.util.Log.d("MessagePreviewClick", "scrollToMessage - scrolling to position $position with centerOffset $centerOffset")
            (recyclerViewMessageList?.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(position, centerOffset)
            viewModel?.clearScrollToMessage()
            
            // Check if highlight is needed
            val shouldHighlight = viewModel?.highlightScroll?.value == true
            android.util.Log.d("MessagePreviewClick", "scrollToMessage - shouldHighlight: $shouldHighlight")
            if (shouldHighlight) {
                // Use post() to ensure highlight happens after scroll and layout are complete
                // This prevents race conditions where notifyItemChanged is called before the view is visible
                recyclerViewMessageList?.post {
                    android.util.Log.d("MessagePreviewClick", "scrollToMessage - post() executing highlight")
                    highlightMessageAtPosition(messageId, position)
                }
                viewModel?.clearHighlightScroll()
            }
        } else {
            android.util.Log.d("MessagePreviewClick", "scrollToMessage - position not found, message may not be in list")
        }
    }
    
    /**
     * Handles click on message preview (quoted message) to jump to parent message.
     * 
     * If the quoted message is in the current list, scrolls to it and highlights.
     * If not in the list, calls ViewModel to fetch and navigate to it.
     */
    private fun handleMessagePreviewClick(quotedMessage: BaseMessage) {
        val messages = messageAdapter.getMessages()
        val existingMessage = messages.find { it.id == quotedMessage.id }
        
        if (existingMessage != null) {
            // Message is in current list - scroll to it directly
            val position = messageAdapter.findMessagePosition(quotedMessage.id)
            if (position >= 0) {
                recyclerViewMessageList?.stopScroll()
                val centerOffset = recyclerViewMessageList?.height?.div(2) ?: 0
                (recyclerViewMessageList?.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(position, centerOffset)
                // Use postDelayed() to ensure highlight happens after scroll and layout are complete
                recyclerViewMessageList?.postDelayed({
                    highlightMessageAtPosition(quotedMessage.id, position)
                }, 150)
            }
        } else {
            // Message not in list - fetch it via goToMessage
            viewModel?.goToMessage(quotedMessage.id, highlight = true)
        }
    }
    
    /**
     * Highlights a message at the given position with a fade-out animation.
     * 
     * Sets the highlight on the message and starts a 2-second fade-out animation
     * that gradually reduces the highlight alpha from 1.0 to 0.0.
     */
    private fun highlightMessageAtPosition(messageId: Long, position: Int) {
        messageAdapter.setHighlightedMessage(messageId, position, recyclerViewMessageList)
        fadeOutMessageHighlight(position)
    }
    
    /**
     * Initiates a fade-out animation for the highlighted message.
     * The animation gradually reduces the highlight effect over 2 seconds.
     * 
     * Uses Handler.postDelayed for reliable animation since ValueAnimator
     * was being cancelled prematurely by RecyclerView's item animator.
     */
    private fun fadeOutMessageHighlight(position: Int) {
        // Cancel any existing animation
        highlightAnimator?.cancel()
        highlightAnimator = null
        
        // Get the ViewHolder to animate directly
        val viewHolder = recyclerViewMessageList?.findViewHolderForAdapterPosition(position)
        if (viewHolder == null) {
            // ViewHolder not visible, just clear the highlight
            messageAdapter.clearHighlight(position)
            return
        }
        
        val viewToAnimate = viewHolder.itemView
        val baseColor = CometChatTheme.getExtendedPrimaryColor800(context)
        
        // Use Handler-based animation for reliability
        // Total duration: 2000ms, update every 50ms = 40 steps
        val totalDuration = 2000L
        val stepDuration = 50L
        val totalSteps = (totalDuration / stepDuration).toInt()
        var currentStep = 0
        
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        val animationRunnable = object : Runnable {
            override fun run() {
                currentStep++
                val progress = currentStep.toFloat() / totalSteps
                val alpha = 1f - progress
                
                // Calculate color with current alpha
                val color = android.graphics.Color.argb(
                    (android.graphics.Color.alpha(baseColor) * alpha).toInt(),
                    android.graphics.Color.red(baseColor),
                    android.graphics.Color.green(baseColor),
                    android.graphics.Color.blue(baseColor)
                )
                
                // Update background directly
                viewToAnimate.setBackgroundColor(color)
                messageAdapter.highlightAlpha = alpha
                
                if (currentStep < totalSteps) {
                    // Schedule next step
                    handler.postDelayed(this, stepDuration)
                } else {
                    // Animation complete
                    messageAdapter.clearHighlight(position)
                }
            }
        }
        
        // Start the animation
        handler.post(animationRunnable)
    }

    /**
     * Scrolls to the last item in the message list (newest message).
     * 
     * With reverseLayout = false, the newest messages are at the bottom (highest index).
     * This function scrolls to show the newest messages without reloading data.
     */
    private fun scrollToLastItem() {
        val itemCount = messageAdapter.itemCount
        if (itemCount > 0) {
            recyclerViewMessageList?.scrollToPosition(itemCount - 1)
        }
    }

    // ========================================
    // Message Operations
    // ========================================

    /**
     * Adds a message to the list.
     *
     * @param message The message to add
     */
    fun addMessage(message: BaseMessage) {
        viewModel?.addMessage(message)
    }

    /**
     * Updates a message in the list.
     *
     * @param message The updated message
     */
    fun updateMessage(message: BaseMessage) {
        viewModel?.updateMessage(message)
    }

    /**
     * Removes a message from the list.
     *
     * @param message The message to remove
     */
    fun removeMessage(message: BaseMessage) {
        viewModel?.removeMessage(message)
    }

    /**
     * Clears all messages from the list.
     */
    fun clearMessages() {
        viewModel?.clear()
    }

    /**
     * Gets the current list of messages.
     *
     * @return The current messages list
     */
    fun getMessages(): List<BaseMessage> = messageAdapter.getMessages()

    /**
     * Fetches messages manually.
     */
    fun fetchMessages() {
        viewModel?.fetchMessages()
    }

    // ========================================
    // Getters
    // ========================================

    fun getUser(): User? = user
    fun getGroup(): Group? = group
    fun getParentMessageId(): Long = parentMessageId
    fun getRecyclerView(): RecyclerView? = recyclerViewMessageList
    fun getAdapter(): MessageAdapter = messageAdapter
    fun getViewModel(): CometChatMessageListViewModel? = viewModel
}
