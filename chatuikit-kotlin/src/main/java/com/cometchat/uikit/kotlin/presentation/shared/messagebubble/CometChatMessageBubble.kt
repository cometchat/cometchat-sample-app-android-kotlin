package com.cometchat.uikit.kotlin.presentation.shared.messagebubble

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.kotlin.shared.interfaces.DateTimeFormatterCallback
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import java.text.SimpleDateFormat
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.google.android.material.card.MaterialCardView

/**
 * A smart message bubble component that automatically creates and binds content views
 * based on message type using the [BubbleFactory] registry pattern.
 *
 * This component provides:
 * - Automatic content view creation based on message type via factories
 * - Support for custom factory overrides
 * - Slot-based customization (leading, header, reply, content, bottom, statusInfo, thread, footer)
 * - Efficient RecyclerView integration with view recycling
 * - Alignment support (LEFT, RIGHT, CENTER)
 *
 * ## Factory-Based Content Rendering
 *
 * When [setMessage] is called, the bubble:
 * 1. Determines the factory key using [BubbleFactory.getFactoryKey]
 * 2. Looks up the appropriate factory from [bubbleFactories]
 * 3. Creates the content view using [BubbleFactory.createView] (if not already created)
 * 4. Binds the message using [BubbleFactory.bindView]
 * 5. Falls back to a placeholder view if no factory is found
 *
 * ## Custom Factories
 *
 * Override default factories using [setBubbleFactories]:
 * ```kotlin
 * messageBubble.setBubbleFactories(listOf(MyCustomTextFactory()))
 * ```
 *
 * ## Slot Customization
 *
 * Set custom views for each slot using the setter methods:
 * ```kotlin
 * messageBubble.setHeaderView(myHeaderView)
 * messageBubble.setContentView(myContentView) // Overrides factory-based content
 * ```
 *
 * @see BubbleFactory
 * @see InternalContentRenderer
 */
class CometChatMessageBubble @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.cometchatMessageBubbleStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = CometChatMessageBubble::class.java.simpleName
    }

    // Layout containers
    private var rootView: View? = null
    private var parent: LinearLayout? = null
    private var messageBubble: MaterialCardView? = null
    private var leadingViewContainer: LinearLayout? = null
    private var headerViewContainer: LinearLayout? = null
    private var replyViewContainer: LinearLayout? = null
    private var contentViewContainer: LinearLayout? = null
    private var bottomViewContainer: LinearLayout? = null
    private var statusInfoViewContainer: LinearLayout? = null
    private var threadViewContainer: LinearLayout? = null
    private var footerViewContainer: LinearLayout? = null
    private var messageContainer: LinearLayout? = null

    // Custom views set by user
    private var customLeadingView: View? = null
    private var customHeaderView: View? = null
    private var customReplyView: View? = null
    private var customContentView: View? = null
    private var customBottomView: View? = null
    private var customStatusInfoView: View? = null
    private var customThreadView: View? = null
    private var customFooterView: View? = null

    // Style properties
    @ColorInt private var bubbleBackgroundColor: Int = 0
    @Dimension private var bubbleStrokeWidth: Int = 0
    @ColorInt private var bubbleStrokeColor: Int = 0
    @Dimension private var bubbleCornerRadius: Int = 0
    private var backgroundDrawable: Drawable? = null
    @StyleRes private var styleResId: Int = 0

    // Style resource IDs for incoming/outgoing messages (set by adapter, loaded internally)
    @StyleRes private var incomingMessageBubbleStyleResId: Int = 0
    @StyleRes private var outgoingMessageBubbleStyleResId: Int = 0

    // Cached loaded styles (lazily loaded from resource IDs)
    private var loadedIncomingStyle: CometChatMessageBubbleStyle? = null
    private var loadedOutgoingStyle: CometChatMessageBubbleStyle? = null

    // Factory registry — starts empty; InternalContentRenderer handles all standard types
    // Single factory for this bubble instance (set by adapter after filtering from map)
    private var bubbleFactory: BubbleFactory? = null

    // Per-bubble-type style overrides passed to InternalContentRenderer
    private var bubbleStyles: BubbleStyles = BubbleStyles()

    // Complete bubble replacement view from factory
    private var factoryBubbleView: View? = null

    // Current state
    private var currentMessage: BaseMessage? = null
    private var currentAlignment: UIKitConstants.MessageBubbleAlignment = UIKitConstants.MessageBubbleAlignment.LEFT
    private var currentFactoryKey: String? = null
    
    // Factory-created views for each slot
    private var factoryLeadingView: View? = null
    private var factoryHeaderView: View? = null
    private var factoryReplyView: View? = null
    private var factoryContentView: View? = null
    private var factoryBottomView: View? = null
    private var factoryStatusInfoView: View? = null
    private var factoryThreadView: View? = null
    private var factoryFooterView: View? = null

    // Text formatters for message text rendering
    private var textFormatters: List<CometChatTextFormatter> = emptyList()

    // Date/Time formatting configuration
    private var timeFormat: SimpleDateFormat? = null
    private var dateTimeFormatter: DateTimeFormatterCallback? = null

    // Timestamp alignment configuration (TOP = header view, BOTTOM = status info view)
    private var timeStampAlignment: UIKitConstants.TimeStampAlignment = UIKitConstants.TimeStampAlignment.BOTTOM

    // Message list alignment configuration
    private var messageListAlignment: UIKitConstants.MessageListAlignment = UIKitConstants.MessageListAlignment.STANDARD

    // Visibility configuration for sub-components
    private var reactionVisibility: Int = View.VISIBLE
    private var avatarVisibility: Int = View.VISIBLE
    private var receiptsVisibility: Int = View.VISIBLE

    // Click listener for message preview (quoted message)
    private var onMessagePreviewClick: ((BaseMessage) -> Unit)? = null

    init {
        Utils.initMaterialCard(this)
        applyStyleAttributes(attrs, defStyleAttr, 0)
    }

    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        val typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatMessageBubble, defStyleAttr, defStyleRes
        )
        val styleResId = typedArray.getResourceId(
            R.styleable.CometChatMessageBubble_cometchatMessageBubbleStyle, 0
        )
        val styledArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatMessageBubble, defStyleRes, styleResId
        )
        extractAttributesAndApplyDefaults(styledArray)
    }

    private fun extractAttributesAndApplyDefaults(typedArray: TypedArray) {
        try {
            backgroundDrawable = typedArray.getDrawable(
                R.styleable.CometChatMessageBubble_cometchatMessageBubbleBackgroundDrawable
            )
            bubbleBackgroundColor = typedArray.getColor(
                R.styleable.CometChatMessageBubble_cometchatMessageBubbleBackgroundColor,
                CometChatTheme.getPrimaryColor(context)
            )
            bubbleStrokeWidth = typedArray.getDimension(
                R.styleable.CometChatMessageBubble_cometchatMessageBubbleStrokeWidth, 0f
            ).toInt()
            bubbleStrokeColor = typedArray.getColor(
                R.styleable.CometChatMessageBubble_cometchatMessageBubbleStrokeColor, 0
            )
            bubbleCornerRadius = typedArray.getDimension(
                R.styleable.CometChatMessageBubble_cometchatMessageBubbleCornerRadius, 0f
            ).toInt()
        } finally {
            typedArray.recycle()
        }
    }

    // ========================================
    // Factory Methods
    // ========================================

    /**
     * Sets the single bubble factory for this bubble instance.
     * 
     * This method is called by the adapter after filtering the appropriate factory
     * from its internal map based on the message's category and type.
     * 
     * When a factory is set, it will be used for creating and binding views.
     * When null, the bubble falls back to [InternalContentRenderer] for default rendering.
     *
     * @param factory The [BubbleFactory] to use for this bubble, or null to use default rendering
     */
    fun setBubbleFactory(factory: BubbleFactory?) {
        this.bubbleFactory = factory
    }

    /**
     * Gets the current bubble factory.
     *
     * @return The current [BubbleFactory], or null if using default rendering
     */
    fun getBubbleFactory(): BubbleFactory? = bubbleFactory

    // ========================================
    // BubbleStyles
    // ========================================

    /**
     * Sets the per-bubble-type style overrides.
     *
     * These styles are passed to [InternalContentRenderer] during content binding,
     * allowing callers to customize individual bubble type appearances.
     *
     * @param styles The [BubbleStyles] containing optional per-bubble-type overrides
     */
    fun setBubbleStyles(styles: BubbleStyles) {
        this.bubbleStyles = styles
    }

    /**
     * Gets the current per-bubble-type style overrides.
     */
    fun getBubbleStyles(): BubbleStyles = bubbleStyles

    // ========================================
    // Text Formatters
    // ========================================

    /**
     * Sets the text formatters for message text rendering.
     *
     * Text formatters are used to customize how message text is rendered,
     * including mentions, links, markdown, and other text transformations.
     * These formatters are passed to [InternalContentRenderer] during content binding.
     *
     * @param formatters The list of [CometChatTextFormatter] instances to use for text rendering.
     */
    fun setTextFormatters(formatters: List<CometChatTextFormatter>) {
        this.textFormatters = formatters
    }

    /**
     * Gets the current text formatters.
     *
     * @return The list of text formatters, or an empty list if none are set.
     */
    fun getTextFormatters(): List<CometChatTextFormatter> = textFormatters

    // ========================================
    // Date/Time Formatting
    // ========================================

    /**
     * Sets the time format for message timestamps.
     *
     * When set, this format is used to display the time portion of message
     * timestamps in the status info view of the bubble.
     *
     * @param format The [SimpleDateFormat] to use for message timestamps,
     *               or `null` to use the default format.
     */
    fun setTimeFormat(format: SimpleDateFormat?) {
        this.timeFormat = format
    }

    /**
     * Gets the current time format.
     *
     * @return The current [SimpleDateFormat] for timestamps, or `null` if using default.
     */
    fun getTimeFormat(): SimpleDateFormat? = timeFormat

    /**
     * Sets a custom date/time formatter callback for advanced date/time formatting.
     *
     * The [DateTimeFormatterCallback] provides fine-grained control over how dates
     * and times are formatted in different contexts (today, yesterday, last week,
     * older dates).
     *
     * @param formatter The [DateTimeFormatterCallback] for custom formatting,
     *                  or `null` to use the default formatting logic.
     */
    fun setDateTimeFormatter(formatter: DateTimeFormatterCallback?) {
        this.dateTimeFormatter = formatter
    }

    /**
     * Gets the current date/time formatter callback.
     *
     * @return The current [DateTimeFormatterCallback], or `null` if using default.
     */
    fun getDateTimeFormatter(): DateTimeFormatterCallback? = dateTimeFormatter

    /**
     * Sets the timestamp alignment for message bubbles.
     *
     * Controls where the timestamp is displayed:
     * - [UIKitConstants.TimeStampAlignment.TOP]: Timestamp displayed in the header view alongside sender name
     * - [UIKitConstants.TimeStampAlignment.BOTTOM]: Timestamp displayed in the status info view (default)
     *
     * @param alignment The [UIKitConstants.TimeStampAlignment] to use for timestamp positioning.
     */
    fun setTimeStampAlignment(alignment: UIKitConstants.TimeStampAlignment) {
        this.timeStampAlignment = alignment
    }

    /**
     * Gets the current timestamp alignment.
     *
     * @return The current [UIKitConstants.TimeStampAlignment].
     */
    fun getTimeStampAlignment(): UIKitConstants.TimeStampAlignment = timeStampAlignment

    // ========================================
    // Message Setting (Smart Content Creation)
    // ========================================

    /**
     * Creates all views for the given factory key.
     * 
     * This method should be called once when the ViewHolder is created.
     * At this point, the message object is NOT available - only the factory key is known.
     * 
     * When a factory is set via [setBubbleFactory]:
     * 1. Check [BubbleFactory.createBubbleView] — if non-null, use as complete bubble replacement
     * 2. Otherwise use factory's slot methods for each slot
     *
     * When no factory is set (null), delegates to [InternalContentRenderer] for all slots.
     * 
     * @param factoryKey The factory key (e.g., "message_text", "custom_extension_poll")
     * @return The created content view, or a fallback view if no factory is found
     */
    fun createViews(factoryKey: String): View {
        val factory = bubbleFactory  // Use single factory property
        
        if (factory != null) {
            // --- External factory path ---
            currentFactoryKey = factoryKey

            // Check for complete bubble replacement first
            val bubbleView = factory.createBubbleView(context)
            if (bubbleView != null) {
                factoryBubbleView = bubbleView
                handleView(contentViewContainer, bubbleView)
                // Hide all other slots — bubbleView replaces everything
                handleView(leadingViewContainer, null)
                handleView(headerViewContainer, null)
                handleView(replyViewContainer, null)
                handleView(bottomViewContainer, null)
                handleView(statusInfoViewContainer, null)
                handleView(threadViewContainer, null)
                handleView(footerViewContainer, null)
                return bubbleView
            }

            factoryBubbleView = null

            // Content: custom > factory
            if (customContentView == null) {
                factoryContentView = factory.createContentView(context)
                handleView(contentViewContainer, factoryContentView)
            }
            
            // Each slot: custom > factory (factory may return null)
            if (customLeadingView == null) {
                factoryLeadingView = factory.createLeadingView(context)
                handleView(leadingViewContainer, factoryLeadingView)
            }
            
            if (customHeaderView == null) {
                factoryHeaderView = factory.createHeaderView(context)
                handleView(headerViewContainer, factoryHeaderView)
            }
            
            if (customReplyView == null) {
                factoryReplyView = factory.createReplyView(context)
                handleView(replyViewContainer, factoryReplyView)
            }
            
            if (customBottomView == null) {
                factoryBottomView = factory.createBottomView(context)
                handleView(bottomViewContainer, factoryBottomView)
            }
            
            if (customStatusInfoView == null) {
                factoryStatusInfoView = factory.createStatusInfoView(context)
                handleView(statusInfoViewContainer, factoryStatusInfoView)
            }
            
            if (customThreadView == null) {
                factoryThreadView = factory.createThreadView(context)
                handleView(threadViewContainer, factoryThreadView)
            }
            
            if (customFooterView == null) {
                factoryFooterView = factory.createFooterView(context)
                handleView(footerViewContainer, factoryFooterView)
            }
            
            return factoryContentView ?: customContentView!!
        } else {
            // --- InternalContentRenderer path ---
            currentFactoryKey = factoryKey
            factoryBubbleView = null

            // Content: custom > InternalContentRenderer > fallback
            if (customContentView == null) {
                factoryContentView = InternalContentRenderer.createContentView(context, factoryKey)
                    ?: createFallbackView()
                handleView(contentViewContainer, factoryContentView)
            }

            // Slot views: custom > InternalContentRenderer defaults
            if (customLeadingView == null) {
                factoryLeadingView = InternalContentRenderer.createLeadingView(context, currentAlignment)
                handleView(leadingViewContainer, factoryLeadingView)
            }

            // Header view: skip creation for RIGHT alignment (outgoing messages don't show header)
            if (currentAlignment == UIKitConstants.MessageBubbleAlignment.RIGHT) {
                headerViewContainer?.visibility = GONE
            } else if (customHeaderView == null) {
                factoryHeaderView = InternalContentRenderer.createHeaderView(context)
                handleView(headerViewContainer, factoryHeaderView)
            }

            if (customReplyView == null) {
                factoryReplyView = InternalContentRenderer.createReplyView(context)
                handleView(replyViewContainer, factoryReplyView)
            }

            if (customBottomView == null) {
                factoryBottomView = InternalContentRenderer.createBottomView(context)
                handleView(bottomViewContainer, factoryBottomView)
            }

            if (customStatusInfoView == null) {
                factoryStatusInfoView = InternalContentRenderer.createStatusInfoView(context)
                handleView(statusInfoViewContainer, factoryStatusInfoView)
            }

            if (customThreadView == null) {
                factoryThreadView = InternalContentRenderer.createThreadView(context)
                handleView(threadViewContainer, factoryThreadView)
            }

            if (customFooterView == null) {
                factoryFooterView = InternalContentRenderer.createFooterView(context, currentAlignment)
                handleView(footerViewContainer, factoryFooterView)
            }

            return factoryContentView ?: customContentView!!
        }
    }

    /**
     * Creates the content view for the given factory key.
     * 
     * This method should be called once when the ViewHolder is created.
     * At this point, the message object is NOT available - only the factory key is known.
     * 
     * Note: This only creates the content view. Use [createViews] to create all slot views.
     * 
     * @param factoryKey The factory key (e.g., "message_text", "custom_extension_poll")
     * @return The created content view, or a fallback view if no factory is found
     */
    fun createContentView(factoryKey: String): View {
        // If user set a custom content view, return that
        if (customContentView != null) {
            return customContentView!!
        }
        
        // Use single factory property
        val factory = bubbleFactory
        
        if (factory != null) {
            factoryContentView = factory.createContentView(context)
            currentFactoryKey = factoryKey
            handleView(contentViewContainer, factoryContentView)
            return factoryContentView!!
        } else {
            // No factory set - use InternalContentRenderer or create fallback
            factoryContentView = InternalContentRenderer.createContentView(context, factoryKey)
                ?: createFallbackView()
            currentFactoryKey = factoryKey
            handleView(contentViewContainer, factoryContentView)
            return factoryContentView!!
        }
    }

    /**
     * Binds the message data to all factory-created views.
     * 
     * When a factory is set via [setBubbleFactory], delegates binding to the factory.
     * When no factory is set, delegates to [InternalContentRenderer] with
     * [InternalContentRenderer.shouldUseMinimalSlots] visibility logic.
     * 
     * @param message The message to bind
     * @param alignment The bubble alignment (LEFT, RIGHT, CENTER)
     * @param holder The ViewHolder (for additional context, may be null)
     * @param position Position in the list (-1 if not applicable)
     */
    fun bindViews(
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment,
        holder: RecyclerView.ViewHolder? = null,
        position: Int = -1
    ) {
        currentMessage = message
        
        // Inflate layout if alignment changed or not yet inflated
        if (currentAlignment != alignment || rootView == null) {
            setMessageAlignment(alignment)
        }
        
        // Get factory key from message
        val factoryKey = BubbleFactory.getFactoryKey(message)
        
        // Create views if factory key changed
        if (currentFactoryKey != factoryKey) {
            createViews(factoryKey)
        }
        
        val factory = bubbleFactory  // Use single factory property

        // Resolve effective style using three-tier priority chain
        val effectiveStyle = resolveEffectiveStyle(message, alignment, factory)

        if (factory != null) {
            // --- External factory binding ---

            // Check for bubble view replacement
            if (factoryBubbleView != null) {
                @Suppress("UNCHECKED_CAST")
                factory.bindBubbleView(
                    factoryBubbleView!!, message, alignment, holder, position
                )
                return
            }

            @Suppress("UNCHECKED_CAST")
            val typedFactory = factory
            
            // Bind content view (required)
            if (factoryContentView != null && customContentView == null) {
                typedFactory.bindContentView(factoryContentView!!, message, alignment, holder, position)
            }
            
            // Bind optional slot views
            if (factoryLeadingView != null && customLeadingView == null) {
                typedFactory.bindLeadingView(factoryLeadingView!!, message, alignment)
            }
            
            if (factoryHeaderView != null && customHeaderView == null) {
                typedFactory.bindHeaderView(factoryHeaderView!!, message, alignment)
            }
            
            if (factoryReplyView != null && customReplyView == null) {
                typedFactory.bindReplyView(factoryReplyView!!, message, alignment)
            }
            
            if (factoryBottomView != null && customBottomView == null) {
                typedFactory.bindBottomView(factoryBottomView!!, message, alignment)
            }
            
            if (factoryStatusInfoView != null && customStatusInfoView == null) {
                typedFactory.bindStatusInfoView(factoryStatusInfoView!!, message, alignment)
            }
            
            if (factoryThreadView != null && customThreadView == null) {
                typedFactory.bindThreadView(factoryThreadView!!, message, alignment)
            }
            
            if (factoryFooterView != null && customFooterView == null) {
                typedFactory.bindFooterView(factoryFooterView!!, message, alignment)
            }
        } else {
            // --- InternalContentRenderer binding ---
            val useMinimalSlots = InternalContentRenderer.shouldUseMinimalSlots(message)

            // Content binding
            if (factoryContentView != null && customContentView == null) {
                InternalContentRenderer.bindContentView(
                    factoryContentView!!, message, alignment, effectiveStyle,
                    bubbleStyles = bubbleStyles, textFormatters = textFormatters,
                    holder = holder, position = position
                )
            }

            // Leading view: hide for minimal slots (center bubbles don't show avatar)
            // Also respect avatarVisibility setting
            if (useMinimalSlots || avatarVisibility == GONE) {
                leadingViewContainer?.visibility = GONE
            } else if (avatarVisibility == INVISIBLE) {
                leadingViewContainer?.visibility = INVISIBLE
            } else if (factoryLeadingView != null && customLeadingView == null) {
                InternalContentRenderer.bindLeadingView(factoryLeadingView!!, message, alignment)
                leadingViewContainer?.visibility = VISIBLE
            }

            // Header: hide for minimal slots and for RIGHT-aligned (outgoing) messages
            if (useMinimalSlots) {
                headerViewContainer?.visibility = GONE
            } else if (alignment == UIKitConstants.MessageBubbleAlignment.RIGHT) {
                // Hide header view for outgoing messages (sender name not shown, time is in status info)
                headerViewContainer?.visibility = GONE
            } else if (factoryHeaderView != null && customHeaderView == null) {
                InternalContentRenderer.bindHeaderView(factoryHeaderView!!, message, alignment, effectiveStyle, timeStampAlignment)
                headerViewContainer?.visibility = VISIBLE
            }

            // Reply: hide for minimal slots
            if (useMinimalSlots) {
                replyViewContainer?.visibility = GONE
            } else if (factoryReplyView != null && customReplyView == null) {
                InternalContentRenderer.bindReplyView(
                    factoryReplyView!!,
                    message,
                    alignment,
                    textFormatters,
                    bubbleStyles.incomingMessagePreviewStyle,
                    bubbleStyles.outgoingMessagePreviewStyle,
                    onMessagePreviewClick
                )
            }

            // Bottom view (not hidden for minimal slots — moderation can apply to any type)
            if (factoryBottomView != null && customBottomView == null) {
                InternalContentRenderer.bindBottomView(factoryBottomView!!, message, alignment)
            }

            // StatusInfo: hide for minimal slots or meeting messages
            // Also respect receiptsVisibility setting for the receipt indicator
            val shouldHideStatusInfo = InternalContentRenderer.shouldHideStatusInfo(message, useMinimalSlots)
            if (shouldHideStatusInfo) {
                statusInfoViewContainer?.visibility = GONE
            } else if (factoryStatusInfoView != null && customStatusInfoView == null) {
                InternalContentRenderer.bindStatusInfoView(
                    factoryStatusInfoView!!, 
                    message, 
                    alignment,
                    effectiveStyle,
                    timeFormat,
                    dateTimeFormatter,
                    timeStampAlignment,
                    receiptsVisibility
                )
            }

            // Thread: hide for minimal slots
            if (useMinimalSlots) {
                threadViewContainer?.visibility = GONE
            } else if (factoryThreadView != null && customThreadView == null) {
                InternalContentRenderer.bindThreadView(factoryThreadView!!, message, alignment)
            }

            // Footer: hide for minimal slots
            // Also respect reactionVisibility setting
            if (useMinimalSlots || reactionVisibility == GONE) {
                footerViewContainer?.visibility = GONE
            } else if (reactionVisibility == INVISIBLE) {
                footerViewContainer?.visibility = INVISIBLE
            } else if (factoryFooterView != null && customFooterView == null) {
                InternalContentRenderer.bindFooterView(factoryFooterView!!, message, alignment)
            }
        }

        // Apply outer container style
        applyBubbleStyle(effectiveStyle)
    }

    // ========================================
    // Style Resolution
    // ========================================

    /**
     * Resolves the effective style for a message bubble using a three-tier priority chain:
     * 1. Factory style (highest) — from [BubbleFactory.getBubbleStyle] when a factory is registered
     * 2. Loaded style object or from resource ID — from [loadedIncomingStyle]/[loadedOutgoingStyle] or [incomingMessageBubbleStyleResId]/[outgoingMessageBubbleStyleResId]
     * 3. Alignment-based default (lowest) — incoming/outgoing/default based on alignment
     *
     * Special handling for stickers: they always use transparent background
     * for the outer wrapper, matching the Java implementation behavior.
     *
     * Per-bubble-type content styles are resolved separately via [BubbleStyles] in
     * [InternalContentRenderer].
     *
     * @param message The message to resolve style for
     * @param alignment The bubble alignment
     * @param factory The factory for this message type, or null if using InternalContentRenderer
     * @return The resolved effective style
     */
    private fun resolveEffectiveStyle(
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment,
        factory: BubbleFactory?
    ): CometChatMessageBubbleStyle {
        // Priority 1 (highest): Factory style
        factory?.getBubbleStyle(message, alignment)?.let { return it }

        // Special handling for stickers: they should always have transparent background
        // for the outer wrapper, matching the Java implementation behavior
        if (message.category == CometChatConstants.CATEGORY_CUSTOM && 
            message.type == InternalContentRenderer.EXTENSION_STICKER) {
            return CometChatMessageBubbleStyle(backgroundColor = android.graphics.Color.TRANSPARENT)
        }

        // Priority 2: Loaded style object or from resource ID (if set)
        when (alignment) {
            UIKitConstants.MessageBubbleAlignment.LEFT -> {
                // Check for directly set style object first
                loadedIncomingStyle?.let { return it }
                // Then check for resource ID
                if (incomingMessageBubbleStyleResId != 0) {
                    return getOrLoadIncomingStyle()
                }
            }
            UIKitConstants.MessageBubbleAlignment.RIGHT -> {
                // Check for directly set style object first
                loadedOutgoingStyle?.let { return it }
                // Then check for resource ID
                if (outgoingMessageBubbleStyleResId != 0) {
                    return getOrLoadOutgoingStyle()
                }
            }
            UIKitConstants.MessageBubbleAlignment.CENTER -> {
                // CENTER uses transparent fallback - no resource ID loading
            }
        }

        // Priority 3 (lowest): Alignment-based default
        return when (alignment) {
            UIKitConstants.MessageBubbleAlignment.LEFT ->
                CometChatMessageBubbleStyle.incoming(context)
            UIKitConstants.MessageBubbleAlignment.RIGHT ->
                CometChatMessageBubbleStyle.outgoing(context)
            UIKitConstants.MessageBubbleAlignment.CENTER ->
                // CENTER-aligned messages (action/system messages) use transparent outer bubble
                // because the actual styling comes from CometChatActionBubbleStyle applied to the content view
                CometChatMessageBubbleStyle(backgroundColor = android.graphics.Color.TRANSPARENT)
        }
    }

    /**
     * Lazily loads and caches the incoming message bubble style from the resource ID.
     */
    private fun getOrLoadIncomingStyle(): CometChatMessageBubbleStyle {
        if (loadedIncomingStyle == null && incomingMessageBubbleStyleResId != 0) {
            val typedArray = context.obtainStyledAttributes(
                incomingMessageBubbleStyleResId,
                R.styleable.CometChatMessageBubble
            )
            loadedIncomingStyle = CometChatMessageBubbleStyle.fromTypedArray(context, typedArray)
        }
        return loadedIncomingStyle ?: CometChatMessageBubbleStyle.incoming(context)
    }

    /**
     * Lazily loads and caches the outgoing message bubble style from the resource ID.
     */
    private fun getOrLoadOutgoingStyle(): CometChatMessageBubbleStyle {
        if (loadedOutgoingStyle == null && outgoingMessageBubbleStyleResId != 0) {
            val typedArray = context.obtainStyledAttributes(
                outgoingMessageBubbleStyleResId,
                R.styleable.CometChatMessageBubble
            )
            loadedOutgoingStyle = CometChatMessageBubbleStyle.fromTypedArray(context, typedArray)
        }
        return loadedOutgoingStyle ?: CometChatMessageBubbleStyle.outgoing(context)
    }

    /**
     * Applies the resolved bubble style to the outer message bubble container.
     *
     * @param style The resolved effective style to apply
     */
    private fun applyBubbleStyle(style: CometChatMessageBubbleStyle) {
        messageBubble?.setCardBackgroundColor(style.backgroundColor)
        style.backgroundDrawable?.let { messageBubble?.background = it }
        messageBubble?.strokeWidth = style.strokeWidth.toInt()
        messageBubble?.strokeColor = style.strokeColor
        messageBubble?.radius = style.cornerRadius
    }

    /**
     * Applies a [CometChatMessageBubbleStyle] to the outer MaterialCardView container.
     *
     * If [CometChatMessageBubbleStyle.backgroundDrawable] is non-null, it takes precedence
     * over [CometChatMessageBubbleStyle.backgroundColor].
     *
     * This is the public API for the adapter to apply Tier 2 styles during binding.
     *
     * @param style The style to apply to the outer bubble container
     */
    fun applyStyle(style: CometChatMessageBubbleStyle) {
        applyBubbleStyle(style)
    }

    // ========================================
    // Incoming/Outgoing Style Resource ID Setters
    // ========================================

    /**
     * Sets the style resource ID for incoming (LEFT-aligned) message bubbles.
     *
     * When set, the bubble will internally load the style from this resource ID
     * during binding for LEFT-aligned messages. If not set (0), the bubble falls
     * back to [CometChatMessageBubbleStyle.incoming].
     *
     * The style is lazily loaded and cached on first use.
     *
     * @param styleResId The style resource ID (e.g., R.style.MyIncomingBubbleStyle), or 0 to use default
     */
    fun setIncomingMessageBubbleStyle(@StyleRes styleResId: Int) {
        if (incomingMessageBubbleStyleResId != styleResId) {
            incomingMessageBubbleStyleResId = styleResId
            loadedIncomingStyle = null // Clear cache to reload on next use
        }
    }

    /**
     * Gets the style resource ID for incoming message bubbles.
     *
     * @return The style resource ID, or 0 if using default
     */
    fun getIncomingMessageBubbleStyleResId(): Int = incomingMessageBubbleStyleResId

    /**
     * Sets the style resource ID for outgoing (RIGHT-aligned) message bubbles.
     *
     * When set, the bubble will internally load the style from this resource ID
     * during binding for RIGHT-aligned messages. If not set (0), the bubble falls
     * back to [CometChatMessageBubbleStyle.outgoing].
     *
     * The style is lazily loaded and cached on first use.
     *
     * @param styleResId The style resource ID (e.g., R.style.MyOutgoingBubbleStyle), or 0 to use default
     */
    fun setOutgoingMessageBubbleStyle(@StyleRes styleResId: Int) {
        if (outgoingMessageBubbleStyleResId != styleResId) {
            outgoingMessageBubbleStyleResId = styleResId
            loadedOutgoingStyle = null // Clear cache to reload on next use
        }
    }

    /**
     * Gets the style resource ID for outgoing message bubbles.
     *
     * @return The style resource ID, or 0 if using default
     */
    fun getOutgoingMessageBubbleStyleResId(): Int = outgoingMessageBubbleStyleResId

    /**
     * Sets the style object for incoming (LEFT-aligned) message bubbles.
     *
     * When set, the bubble will use this style object directly for LEFT-aligned messages,
     * bypassing the resource ID loading mechanism. If null, the bubble falls back to
     * the resource ID style or [CometChatMessageBubbleStyle.incoming].
     *
     * @param style The style object to use, or null to clear and use resource ID or default
     */
    fun setIncomingMessageBubbleStyleObject(style: CometChatMessageBubbleStyle?) {
        loadedIncomingStyle = style
        // Clear resource ID if style object is set to avoid confusion
        if (style != null) {
            incomingMessageBubbleStyleResId = 0
        }
    }

    /**
     * Sets the style object for outgoing (RIGHT-aligned) message bubbles.
     *
     * When set, the bubble will use this style object directly for RIGHT-aligned messages,
     * bypassing the resource ID loading mechanism. If null, the bubble falls back to
     * the resource ID style or [CometChatMessageBubbleStyle.outgoing].
     *
     * @param style The style object to use, or null to clear and use resource ID or default
     */
    fun setOutgoingMessageBubbleStyleObject(style: CometChatMessageBubbleStyle?) {
        loadedOutgoingStyle = style
        // Clear resource ID if style object is set to avoid confusion
        if (style != null) {
            outgoingMessageBubbleStyleResId = 0
        }
    }

    /**
     * Binds the message data to the content view only.
     * 
     * This method should be called every time a message needs to be displayed.
     * The content view must have been created first via [createContentView].
     * 
     * Note: This only binds the content view. Use [bindViews] to bind all slot views.
     * 
     * @param message The message to bind
     * @param alignment The bubble alignment (LEFT, RIGHT, CENTER)
     * @param holder The ViewHolder (for additional context, may be null)
     * @param position Position in the list (-1 if not applicable)
     */
    fun bindContentView(
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment,
        holder: RecyclerView.ViewHolder? = null,
        position: Int = -1
    ) {
        currentMessage = message
        
        // Set alignment (inflates layout if changed)
        if (currentAlignment != alignment || rootView == null) {
            setMessageAlignment(alignment)
        }
        
        // If user set a custom content view, skip factory binding
        if (customContentView != null) {
            return
        }
        
        // Get factory key from message
        val factoryKey = BubbleFactory.getFactoryKey(message)
        
        // If factory key changed, we need to recreate the view
        if (currentFactoryKey != factoryKey) {
            createContentView(factoryKey)
        }
        
        // Use single factory property and bind
        val factory = bubbleFactory
        
        if (factory != null && factoryContentView != null) {
            @Suppress("UNCHECKED_CAST")
            factory.bindContentView(
                factoryContentView!!,
                message,
                alignment,
                holder,
                position
            )
        }
    }

    /**
     * Sets the message and alignment, automatically creating and binding all views.
     *
     * This is a convenience method that combines [createViews] and [bindViews].
     * For RecyclerView usage, prefer calling [createViews] in onCreateViewHolder
     * and [bindViews] in onBindViewHolder for better performance.
     *
     * @param message The message to display
     * @param alignment The bubble alignment (LEFT, RIGHT, CENTER)
     */
    fun setMessage(message: BaseMessage, alignment: UIKitConstants.MessageBubbleAlignment) {
        currentMessage = message
        
        // Set alignment (inflates layout if changed)
        if (currentAlignment != alignment || rootView == null) {
            setMessageAlignment(alignment)
        }
        
        // Get factory key
        val factoryKey = BubbleFactory.getFactoryKey(message)
        
        // Create views if needed (different factory or first time)
        if (currentFactoryKey != factoryKey || factoryContentView == null) {
            createViews(factoryKey)
        }
        
        // Bind message to all views
        bindViews(message, alignment, null, -1)
    }

    /**
     * Rebinds a message to existing views. Used for RecyclerView recycling.
     *
     * @param message The message to bind
     * @param alignment The bubble alignment
     * @param holder The ViewHolder (for additional context)
     * @param position Position in the list
     */
    fun rebindMessage(
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment,
        holder: RecyclerView.ViewHolder?,
        position: Int
    ) {
        bindViews(message, alignment, holder, position)
    }

    /**
     * Called when the view is recycled. Notifies the factory to release resources.
     */
    fun onRecycled() {
        if (factoryContentView != null) {
            bubbleFactory?.onViewRecycled(factoryContentView!!)
        }
    }

    /**
     * Creates a fallback view for unsupported message types.
     */
    private fun createFallbackView(): View {
        return TextView(context).apply {
            text = context.getString(R.string.cometchat_this_message_type_is_not_supported)
            setTextColor(CometChatTheme.getTextColorSecondary(context))
            gravity = Gravity.CENTER
            setPadding(
                resources.getDimensionPixelSize(R.dimen.cometchat_padding_3),
                resources.getDimensionPixelSize(R.dimen.cometchat_padding_2),
                resources.getDimensionPixelSize(R.dimen.cometchat_padding_3),
                resources.getDimensionPixelSize(R.dimen.cometchat_padding_2)
            )
        }
    }

    // ========================================
    // Alignment and Layout
    // ========================================

    /**
     * Sets the alignment of the message bubble.
     *
     * @param alignment The alignment (LEFT, RIGHT, CENTER)
     */
    fun setMessageAlignment(alignment: UIKitConstants.MessageBubbleAlignment) {
        currentAlignment = alignment
        
        val layoutRes = when (alignment) {
            UIKitConstants.MessageBubbleAlignment.LEFT -> R.layout.cometchat_message_bubble_left
            UIKitConstants.MessageBubbleAlignment.RIGHT -> R.layout.cometchat_message_bubble_right
            UIKitConstants.MessageBubbleAlignment.CENTER -> R.layout.cometchat_message_bubble_center
        }
        
        removeAllViewsInLayout()
        rootView = LayoutInflater.from(context).inflate(layoutRes, this, true)
        initView()
    }

    private fun initView() {
        parent = findViewById(R.id.parent)
        messageBubble = findViewById<MaterialCardView>(R.id.message_bubble)?.also {
            Utils.initMaterialCard(it)
        }
        leadingViewContainer = findViewById(R.id.leading_view)
        headerViewContainer = findViewById(R.id.header_view_layout)
        replyViewContainer = findViewById(R.id.reply_bubble)
        contentViewContainer = findViewById(R.id.content_view)
        bottomViewContainer = findViewById(R.id.bottom_view)
        statusInfoViewContainer = findViewById(R.id.status_info_view)
        threadViewContainer = findViewById(R.id.view_replies)
        footerViewContainer = findViewById(R.id.footer_view_layout)
        messageContainer = findViewById(R.id.message_container)

        // Restore custom views (custom views take priority over factory views)
        handleView(leadingViewContainer, customLeadingView ?: factoryLeadingView)
        handleView(headerViewContainer, customHeaderView ?: factoryHeaderView)
        handleView(replyViewContainer, customReplyView ?: factoryReplyView)
        handleView(contentViewContainer, customContentView ?: factoryContentView)
        handleView(bottomViewContainer, customBottomView ?: factoryBottomView)
        handleView(statusInfoViewContainer, customStatusInfoView ?: factoryStatusInfoView)
        handleView(threadViewContainer, customThreadView ?: factoryThreadView)
        handleView(footerViewContainer, customFooterView ?: factoryFooterView)

        // Remove addView(view) - no longer needed since inflate attaches directly
        applyStyle()
    }

    private fun handleView(container: LinearLayout?, view: View?) {
        if (view != null && container != null) {
            container.removeAllViews()
            removeParentFromView(view)
            container.visibility = VISIBLE
            
            // Always create LinearLayout.LayoutParams for views added to LinearLayout containers
            // This prevents ClassCastException when the view has incompatible LayoutParams (e.g., FrameLayout.LayoutParams)
            val lp = view.layoutParams
            val linearLp = LinearLayout.LayoutParams(
                lp?.width ?: LinearLayout.LayoutParams.MATCH_PARENT,
                lp?.height ?: LinearLayout.LayoutParams.WRAP_CONTENT
            )
            
            // Set layoutParams on the view before adding to ensure correct type
            view.layoutParams = linearLp
            container.addView(view)
        } else {
            container?.visibility = GONE
        }
    }

    private fun removeParentFromView(view: View) {
        (view.parent as? ViewGroup)?.removeView(view)
    }

    private fun applyStyle() {
        setBackgroundColor(bubbleBackgroundColor)
        setBackgroundDrawable(backgroundDrawable)
        setStrokeWidth(bubbleStrokeWidth)
        setStrokeColor(bubbleStrokeColor)
        setCornerRadius(bubbleCornerRadius)
    }


    // ========================================
    // Slot View Setters
    // ========================================

    /**
     * Sets a custom view as the leading view (typically avatar).
     */
    fun setLeadingView(view: View?) {
        customLeadingView = view
        handleView(leadingViewContainer, view)
    }

    /**
     * Sets a custom view as the header view (typically sender name).
     */
    fun setHeaderView(view: View?) {
        customHeaderView = view
        handleView(headerViewContainer, view)
    }

    /**
     * Sets a custom view as the reply view (for reply-to-message).
     */
    fun setReplyView(view: View?) {
        customReplyView = view
        handleView(replyViewContainer, view)
    }

    /**
     * Sets a custom view as the content view.
     *
     * Note: Setting this overrides factory-based content rendering.
     * The factory will not be used when a custom content view is set.
     */
    fun setContentView(view: View?) {
        customContentView = view
        if (view != null) {
            factoryContentView = null
            currentFactoryKey = null
        }
        handleView(contentViewContainer, view)
    }

    /**
     * Sets a custom view as the bottom view (typically reactions).
     */
    fun setBottomView(view: View?) {
        customBottomView = view
        handleView(bottomViewContainer, view)
    }

    /**
     * Sets a custom view as the status info view (typically timestamp/receipt).
     */
    fun setStatusInfoView(view: View?) {
        customStatusInfoView = view
        handleView(statusInfoViewContainer, view)
    }

    /**
     * Sets a custom view as the thread view (for threaded replies).
     */
    fun setThreadView(view: View?) {
        customThreadView = view
        handleView(threadViewContainer, view)
    }

    /**
     * Sets a custom view as the footer view.
     */
    fun setFooterView(view: View?) {
        customFooterView = view
        handleView(footerViewContainer, view)
    }

    // ========================================
    // Visibility Setters
    // ========================================

    fun setLeadingViewVisibility(visibility: Int) {
        leadingViewContainer?.visibility = visibility
    }

    fun setHeaderViewVisibility(visibility: Int) {
        headerViewContainer?.visibility = visibility
    }

    fun setReplyViewVisibility(visibility: Int) {
        replyViewContainer?.visibility = visibility
    }

    fun setContentViewVisibility(visibility: Int) {
        contentViewContainer?.visibility = visibility
    }

    fun setBottomViewVisibility(visibility: Int) {
        bottomViewContainer?.visibility = visibility
    }

    fun setStatusInfoViewVisibility(visibility: Int) {
        statusInfoViewContainer?.visibility = visibility
    }

    fun setThreadViewVisibility(visibility: Int) {
        threadViewContainer?.visibility = visibility
    }

    /**
     * Sets a click listener on the thread view container.
     * This is called when the user clicks on the thread reply indicator.
     *
     * @param listener The click listener to set, or null to remove
     */
    fun setOnThreadViewClickListener(listener: OnClickListener?) {
        threadViewContainer?.setOnClickListener(listener)
    }

    /**
     * Sets a click listener for the message preview (quoted message) view.
     * This is called when the user clicks on the quoted message preview in a reply.
     *
     * @param listener The click listener to set, or null to remove.
     *                 The listener receives the message that contains the quoted message.
     */
    fun setOnMessagePreviewClickListener(listener: ((BaseMessage) -> Unit)?) {
        this.onMessagePreviewClick = listener
    }

    /**
     * Gets the current message preview click listener.
     *
     * @return The current click listener, or null if not set
     */
    fun getOnMessagePreviewClickListener(): ((BaseMessage) -> Unit)? = onMessagePreviewClick

    fun setFooterViewVisibility(visibility: Int) {
        footerViewContainer?.visibility = visibility
    }

    // ========================================
    // Alignment Configuration
    // ========================================

    /**
     * Sets the message list alignment for the bubble.
     *
     * This controls how messages are aligned in the message list:
     * - [UIKitConstants.MessageListAlignment.STANDARD]: Incoming messages on left, outgoing on right
     * - [UIKitConstants.MessageListAlignment.LEFT_ALIGNED]: All messages aligned to the left
     *
     * @param alignment The [UIKitConstants.MessageListAlignment] to use
     */
    fun setAlignment(alignment: UIKitConstants.MessageListAlignment) {
        this.messageListAlignment = alignment
    }

    /**
     * Gets the current message list alignment.
     *
     * @return The current [UIKitConstants.MessageListAlignment]
     */
    fun getAlignment(): UIKitConstants.MessageListAlignment = messageListAlignment

    // ========================================
    // Sub-Component Visibility Configuration
    // ========================================

    /**
     * Sets the visibility of reactions in the message bubble.
     *
     * Controls whether the reaction view (bottom view) is shown or hidden.
     * This setting is applied during binding via [InternalContentRenderer].
     *
     * @param visibility View visibility constant (View.VISIBLE, View.GONE, or View.INVISIBLE)
     */
    fun setReactionVisibility(visibility: Int) {
        this.reactionVisibility = visibility
    }

    /**
     * Gets the current reaction visibility setting.
     *
     * @return The visibility constant for reactions
     */
    fun getReactionVisibility(): Int = reactionVisibility

    /**
     * Sets the visibility of the avatar in the message bubble.
     *
     * Controls whether the avatar (leading view) is shown or hidden.
     * This setting is applied during binding via [InternalContentRenderer].
     *
     * @param visibility View visibility constant (View.VISIBLE, View.GONE, or View.INVISIBLE)
     */
    fun setAvatarVisibility(visibility: Int) {
        this.avatarVisibility = visibility
        // Also apply immediately to the leading view container
        leadingViewContainer?.visibility = visibility
    }

    /**
     * Gets the current avatar visibility setting.
     *
     * @return The visibility constant for avatar
     */
    fun getAvatarVisibility(): Int = avatarVisibility

    /**
     * Sets the visibility of read receipts in the message bubble.
     *
     * Controls whether the receipt indicators (in status info view) are shown or hidden.
     * This setting is applied during binding via [InternalContentRenderer].
     *
     * @param visibility View visibility constant (View.VISIBLE, View.GONE, or View.INVISIBLE)
     */
    fun setReceiptsVisibility(visibility: Int) {
        this.receiptsVisibility = visibility
    }

    /**
     * Gets the current receipts visibility setting.
     *
     * @return The visibility constant for receipts
     */
    fun getReceiptsVisibility(): Int = receiptsVisibility

    // ========================================
    // Style Setters
    // ========================================

    override fun setBackgroundColor(@ColorInt color: Int) {
        bubbleBackgroundColor = color
        messageBubble?.setCardBackgroundColor(color)
    }

    fun getBackgroundColor(): Int = bubbleBackgroundColor

    override fun setBackgroundDrawable(drawable: Drawable?) {
        if (drawable != null) {
            backgroundDrawable = drawable
            messageBubble?.background = drawable
        }
    }

    fun getBackgroundDrawable(): Drawable? = backgroundDrawable

    override fun setStrokeWidth(@Dimension strokeWidth: Int) {
        if (strokeWidth >= 0) {
            bubbleStrokeWidth = strokeWidth
            messageBubble?.strokeWidth = strokeWidth
        }
    }

    override fun getStrokeWidth(): Int = bubbleStrokeWidth

    override fun setStrokeColor(@ColorInt strokeColor: Int) {
        bubbleStrokeColor = strokeColor
        messageBubble?.strokeColor = strokeColor
    }

    override fun getStrokeColor(): Int = bubbleStrokeColor

    fun setCornerRadius(@Dimension cornerRadius: Int) {
        if (cornerRadius >= 0) {
            bubbleCornerRadius = cornerRadius
            messageBubble?.radius = cornerRadius.toFloat()
        }
    }

    fun getCornerRadius(): Int = bubbleCornerRadius

    fun setStyle(@StyleRes style: Int) {
        if (style != 0) {
            styleResId = style
            val typedArray = context.theme.obtainStyledAttributes(
                style, R.styleable.CometChatMessageBubble
            )
            extractAttributesAndApplyDefaults(typedArray)
            applyStyle()
        }
    }

    fun getStyle(): Int = styleResId

    /**
     * Sets the padding of the message container.
     */
    fun setMessagePadding(left: Int, top: Int, right: Int, bottom: Int) {
        val layoutParams = messageContainer?.layoutParams
        if (layoutParams is MarginLayoutParams) {
            layoutParams.setMargins(
                if (left > -1) left else 0,
                if (top > -1) top else 0,
                if (right > -1) right else 0,
                if (bottom > -1) bottom else 0
            )
            messageContainer?.layoutParams = layoutParams
        }
    }

    // ========================================
    // Getters
    // ========================================

    fun getView(): LinearLayout? = parent
    fun getLeadingView(): LinearLayout? = leadingViewContainer
    fun getHeaderView(): LinearLayout? = headerViewContainer
    fun getReplyView(): LinearLayout? = replyViewContainer
    fun getContentView(): LinearLayout? = contentViewContainer
    fun getBottomView(): LinearLayout? = bottomViewContainer
    fun getStatusInfoView(): LinearLayout? = statusInfoViewContainer
    fun getThreadView(): LinearLayout? = threadViewContainer
    fun getFooterView(): LinearLayout? = footerViewContainer
    fun getCurrentMessage(): BaseMessage? = currentMessage
    fun getCurrentAlignment(): UIKitConstants.MessageBubbleAlignment = currentAlignment
    fun getCurrentContentView(): View? = factoryContentView
}
