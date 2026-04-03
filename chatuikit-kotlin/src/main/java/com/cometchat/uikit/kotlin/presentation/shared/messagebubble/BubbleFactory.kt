package com.cometchat.uikit.kotlin.presentation.shared.messagebubble

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.core.constants.UIKitConstants

/**
 * Abstract factory class for creating and binding message bubble views.
 *
 * This class provides a pattern for efficient RecyclerView usage by separating
 * view creation from data binding. It supports all bubble slots:
 * - **Bubble View** (optional): Complete bubble replacement - when provided, replaces the entire
 *   CometChatMessageBubble including all slots
 * - **Content View** (required): The main message content
 * - **Leading View**: Typically avatar (optional)
 * - **Header View**: Typically sender name (optional)
 * - **Reply View**: Reply-to-message preview (optional)
 * - **Bottom View**: Typically reactions (optional)
 * - **Status Info View**: Typically timestamp/receipt (optional)
 * - **Thread View**: Threaded replies indicator (optional)
 * - **Footer View**: Additional footer content (optional)
 *
 * ## Bubble View vs Content View
 *
 * - **BubbleView**: Replaces the ENTIRE message bubble including all slots (leading, header,
 *   content, footer, etc.). Use this when you need complete control over the bubble layout.
 * - **ContentView**: Only replaces the content area inside the bubble. Other slots (header,
 *   footer, etc.) are still rendered by the bubble.
 *
 * When [createBubbleView] returns a non-null view, [createContentView] and all other slot
 * methods are ignored for that message.
 *
 * ## View Lifecycle
 *
 * For each slot, there are create and bind methods:
 * - `create{Slot}View(context)` - Called once when ViewHolder is created (message NOT available)
 * - `bind{Slot}View(view, message, alignment, ...)` - Called every time message is displayed
 *
 * IMPORTANT: At the time of `create*View()`, only the factory key is known - the actual
 * message object is NOT available. The message is only supplied during `bind*View()`.
 *
 * Example usage:
 * ```kotlin
 * class TextBubbleFactory : BubbleFactory() {
 *     override fun getCategory(): String = CometChatConstants.CATEGORY_MESSAGE
 *     override fun getType(): String = CometChatConstants.MESSAGE_TYPE_TEXT
 *
 *     override fun createContentView(context: Context): View {
 *         return CometChatTextBubble(context)
 *     }
 *
 *     override fun bindContentView(
 *         view: View,
 *         message: BaseMessage,
 *         alignment: MessageBubbleAlignment,
 *         holder: RecyclerView.ViewHolder?,
 *         position: Int
 *     ) {
 *         (view as CometChatTextBubble).setMessage(message as TextMessage, alignment)
 *     }
 * }
 * ```
 */
abstract class BubbleFactory {

    // ========================================
    // Self-describing identity (mirrors ComposeBubbleFactory)
    // ========================================

    /**
     * Returns the message category this factory handles (e.g., "message", "custom", "call").
     * Returns empty string by default (not self-describing).
     */
    open fun getCategory(): String = ""

    /**
     * Returns the message type this factory handles (e.g., "text", "image", "polls").
     * Returns empty string by default (not self-describing).
     */
    open fun getType(): String = ""

    // ========================================
    // Style resolution (mirrors ComposeBubbleFactory)
    // ========================================

    /**
     * Returns a custom bubble style for this message type, or null to use defaults.
     * When non-null, this is the highest priority in the style resolution chain.
     * @param message The message to style
     * @param alignment The bubble alignment
     * @return Custom style or null for alignment-based defaults
     */
    open fun getBubbleStyle(
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment
    ): CometChatMessageBubbleStyle? = null

    // ========================================
    // Bubble View (Optional - complete replacement)
    // ========================================

    /**
     * Creates a view that replaces the entire message bubble.
     *
     * When this method returns a non-null view, it completely replaces the
     * CometChatMessageBubble component including all slots (leading, header, content,
     * footer, thread, etc.). The returned view is responsible for rendering
     * the complete bubble UI.
     *
     * Use this when you need complete control over the bubble layout and don't want
     * to use the standard slot-based structure.
     *
     * When this returns null (default), the standard CometChatMessageBubble is used
     * with [createContentView] and other slot methods.
     *
     * IMPORTANT: At this point, the message object is NOT available - only the factory key
     * is known. Do not attempt to access message data here. All message-specific
     * configuration should be done in [bindBubbleView].
     *
     * @param context The Android context
     * @return View to replace the entire bubble, or null to use standard bubble
     */
    open fun createBubbleView(context: Context): View? = null

    /**
     * Binds message data to the custom bubble view.
     *
     * Called every time a message is displayed when [createBubbleView] returned non-null.
     *
     * @param view The view from [createBubbleView]
     * @param message The message to display
     * @param alignment LEFT, RIGHT, or CENTER alignment
     * @param holder The ViewHolder for additional context (may be null)
     * @param position Position in the list (-1 if not applicable)
     */
    open fun bindBubbleView(
        view: View,
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment,
        holder: RecyclerView.ViewHolder?,
        position: Int
    ) {
        // Default: no-op
    }

    // ========================================
    // Content View (Required unless using createBubbleView)
    // ========================================

    /**
     * Creates the content view. Called once when ViewHolder is created.
     * The view will be recycled and reused for different messages.
     *
     * When [createBubbleView] returns non-null, this method is not called.
     * Has a default implementation returning an empty View so that factories
     * using [createBubbleView] don't need to override this.
     *
     * IMPORTANT: At this point, the message object is NOT available - only the factory key
     * is known. Do not attempt to access message data here. All message-specific
     * configuration should be done in [bindContentView].
     *
     * @param context The Android context
     * @return View to be used as content view (will be recycled)
     */
    open fun createContentView(context: Context): View = View(context)

    /**
     * Binds message data to an existing content view. Called every time a message is displayed.
     *
     * When [createBubbleView] returns non-null, this method is not called.
     *
     * @param view The recycled view from [createContentView]
     * @param message The message to display
     * @param alignment LEFT, RIGHT, or CENTER alignment
     * @param holder The ViewHolder for additional context (may be null)
     * @param position Position in the list (-1 if not applicable)
     */
    open fun bindContentView(
        view: View,
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment,
        holder: RecyclerView.ViewHolder?,
        position: Int
    ) {
        // Default: no-op
    }

    // ========================================
    // Leading View (Optional - typically avatar)
    // ========================================

    /**
     * Creates the leading view (typically avatar). Called once when ViewHolder is created.
     *
     * Override this to provide a custom leading view. Return null to use default or hide.
     *
     * @param context The Android context
     * @return View to be used as leading view, or null for default/none
     */
    open fun createLeadingView(context: Context): View? = null

    /**
     * Binds message data to the leading view.
     *
     * @param view The view from [createLeadingView]
     * @param message The message to display
     * @param alignment The bubble alignment
     */
    open fun bindLeadingView(
        view: View,
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment
    ) {
        // Default: no-op
    }

    // ========================================
    // Header View (Optional - typically sender name)
    // ========================================

    /**
     * Creates the header view (typically sender name). Called once when ViewHolder is created.
     *
     * Override this to provide a custom header view. Return null to use default or hide.
     *
     * @param context The Android context
     * @return View to be used as header view, or null for default/none
     */
    open fun createHeaderView(context: Context): View? = null

    /**
     * Binds message data to the header view.
     *
     * @param view The view from [createHeaderView]
     * @param message The message to display
     * @param alignment The bubble alignment
     */
    open fun bindHeaderView(
        view: View,
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment
    ) {
        // Default: no-op
    }

    // ========================================
    // Reply View (Optional - reply-to-message preview)
    // ========================================

    /**
     * Creates the reply view (reply-to-message preview). Called once when ViewHolder is created.
     *
     * Override this to provide a custom reply view. Return null to use default or hide.
     *
     * @param context The Android context
     * @return View to be used as reply view, or null for default/none
     */
    open fun createReplyView(context: Context): View? = null

    /**
     * Binds message data to the reply view.
     *
     * @param view The view from [createReplyView]
     * @param message The message to display
     * @param alignment The bubble alignment
     */
    open fun bindReplyView(
        view: View,
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment
    ) {
        // Default: no-op
    }

    // ========================================
    // Bottom View (Optional - typically reactions)
    // ========================================

    /**
     * Creates the bottom view (typically reactions). Called once when ViewHolder is created.
     *
     * Override this to provide a custom bottom view. Return null to use default or hide.
     *
     * @param context The Android context
     * @return View to be used as bottom view, or null for default/none
     */
    open fun createBottomView(context: Context): View? = null

    /**
     * Binds message data to the bottom view.
     *
     * @param view The view from [createBottomView]
     * @param message The message to display
     * @param alignment The bubble alignment
     */
    open fun bindBottomView(
        view: View,
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment
    ) {
        // Default: no-op
    }

    // ========================================
    // Status Info View (Optional - typically timestamp/receipt)
    // ========================================

    /**
     * Creates the status info view (typically timestamp/receipt). Called once when ViewHolder is created.
     *
     * Override this to provide a custom status info view. Return null to use default or hide.
     *
     * @param context The Android context
     * @return View to be used as status info view, or null for default/none
     */
    open fun createStatusInfoView(context: Context): View? = null

    /**
     * Binds message data to the status info view.
     *
     * @param view The view from [createStatusInfoView]
     * @param message The message to display
     * @param alignment The bubble alignment
     */
    open fun bindStatusInfoView(
        view: View,
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment
    ) {
        // Default: no-op
    }

    // ========================================
    // Thread View (Optional - threaded replies indicator)
    // ========================================

    /**
     * Creates the thread view (threaded replies indicator). Called once when ViewHolder is created.
     *
     * Override this to provide a custom thread view. Return null to use default or hide.
     *
     * @param context The Android context
     * @return View to be used as thread view, or null for default/none
     */
    open fun createThreadView(context: Context): View? = null

    /**
     * Binds message data to the thread view.
     *
     * @param view The view from [createThreadView]
     * @param message The message to display
     * @param alignment The bubble alignment
     */
    open fun bindThreadView(
        view: View,
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment
    ) {
        // Default: no-op
    }

    // ========================================
    // Footer View (Optional - additional footer content)
    // ========================================

    /**
     * Creates the footer view. Called once when ViewHolder is created.
     *
     * Override this to provide a custom footer view. Return null to use default or hide.
     *
     * @param context The Android context
     * @return View to be used as footer view, or null for default/none
     */
    open fun createFooterView(context: Context): View? = null

    /**
     * Binds message data to the footer view.
     *
     * @param view The view from [createFooterView]
     * @param message The message to display
     * @param alignment The bubble alignment
     */
    open fun bindFooterView(
        view: View,
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment
    ) {
        // Default: no-op
    }

    // ========================================
    // Lifecycle Methods
    // ========================================

    /**
     * Called when view is recycled. Override to release resources like image loads,
     * animations, or media playback.
     *
     * Default implementation does nothing.
     *
     * @param contentView The content view being recycled
     */
    open fun onViewRecycled(contentView: View) {
        // Default: no-op
    }

    // ========================================
    // Deprecated Methods (for backward compatibility)
    // ========================================


    companion object {
        /**
         * Returns the factory key for a given message.
         *
         * The key format is `category_type` (e.g., "message_text", "custom_extension_poll").
         * Deleted messages return the special key "deleted" regardless of original type.
         *
         * @param message The message to get the factory key for
         * @return The factory key string
         */
        @JvmStatic
        fun getFactoryKey(message: BaseMessage): String {
            return if (message.deletedAt > 0) {
                DELETED_KEY
            } else {
                "${message.category}_${message.type}"
            }
        }

        /**
         * Creates a factory key from category and type.
         *
         * @param category The message category
         * @param type The message type
         * @return The factory key string in format "category_type"
         */
        @JvmStatic
        fun getKey(category: String, type: String): String {
            return "${category}_${type}"
        }

        /**
         * Special key used for deleted messages.
         */
        const val DELETED_KEY = "deleted"
    }
}
