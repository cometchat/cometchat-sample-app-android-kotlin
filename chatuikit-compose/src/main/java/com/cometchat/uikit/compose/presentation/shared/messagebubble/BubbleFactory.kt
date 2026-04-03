package com.cometchat.uikit.compose.presentation.shared.messagebubble

import androidx.compose.runtime.Composable
import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.compose.presentation.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatMessageBubbleStyle
import com.cometchat.uikit.core.constants.UIKitConstants

/**
 * Abstract factory interface for creating message bubble content and slot views in Compose.
 *
 * This interface provides a pattern for rendering different message types with customizable
 * slot views. Each factory implementation handles a specific message type (text, image, etc.)
 * and provides composable content for each slot:
 *
 * - **BubbleView** (optional): Complete bubble replacement - when provided, replaces the entire
 *   [CometChatMessageBubble] including all slots
 * - **ContentView** (required): The main message content
 * - **Leading View**: Typically avatar (optional)
 * - **Header View**: Typically sender name (optional)
 * - **Reply View**: Reply-to-message preview (optional)
 * - **Bottom View**: Typically moderation indicator (optional)
 * - **Status Info View**: Typically timestamp/receipt (optional)
 * - **Thread View**: Threaded replies indicator (optional)
 * - **Footer View**: Typically reactions (optional)
 *
 * ## Bubble View vs Content View
 *
 * - **BubbleView**: Replaces the ENTIRE message bubble including all slots (leading, header,
 *   content, footer, etc.). Use this when you need complete control over the bubble layout.
 * - **ContentView**: Only replaces the content area inside the bubble. Other slots (header,
 *   footer, etc.) are still rendered by the bubble.
 *
 * When [getBubbleView] returns a non-null composable, [ContentView] and all other slot methods
 * are ignored for that message.
 *
 * ## Usage
 *
 * Factories are registered in a map keyed by message category and type:
 * ```kotlin
 * val factories = mapOf(
 *     "message_text" to TextBubbleFactory(),
 *     "message_image" to ImageBubbleFactory(),
 *     // ...
 * )
 * ```
 *
 * The [CometChatMessageBubble] composable looks up the appropriate factory and calls
 * its slot methods to render the bubble.
 *
 * ## Style Resolution
 *
 * Each factory can optionally provide a custom [CometChatMessageBubbleStyle] via
 * [getBubbleStyle]. This allows per-message-type styling. If null is returned,
 * the bubble uses alignment-based defaults.
 *
 * @see CometChatMessageBubbleStyle
 */
interface BubbleFactory {

    // ========================================
    // Self-Describing Identity
    // ========================================

    /**
     * Returns the message category this factory handles (e.g., "message", "custom", "call").
     */
    fun getCategory(): String

    /**
     * Returns the message type this factory handles (e.g., "text", "image", "polls").
     */
    fun getType(): String

    // ========================================
    // Bubble View (Optional - complete replacement)
    // ========================================

    /**
     * Returns a composable that replaces the entire message bubble.
     *
     * When this method returns a non-null composable, it completely replaces the
     * [CometChatMessageBubble] component including all slots (leading, header, content,
     * footer, thread, etc.). The returned composable is responsible for rendering
     * the complete bubble UI.
     *
     * Use this when you need complete control over the bubble layout and don't want
     * to use the standard slot-based structure.
     *
     * When this returns null (default), the standard [CometChatMessageBubble] is used
     * with [ContentView] and other slot methods.
     *
     * Example usage:
     * ```kotlin
     * override fun getBubbleView(
     *     message: BaseMessage,
     *     alignment: MessageBubbleAlignment
     * ): (@Composable () -> Unit)? {
     *     return {
     *         // Custom bubble implementation
     *         MyCustomBubble(
     *             message = message,
     *             alignment = alignment
     *         )
     *     }
     * }
     * ```
     *
     * @param message The message to display
     * @param alignment The bubble alignment (LEFT, RIGHT, CENTER)
     * @return Composable lambda that renders the complete bubble, or null to use standard bubble
     */
    fun getBubbleView(
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment
    ): (@Composable () -> Unit)? = null

    // ========================================
    // Content View (Required)
    // ========================================

    /**
     * Returns a composable for the main content view of the message.
     *
     * This is the primary content area of the bubble (e.g., text, image, video player).
     * When [getBubbleView] returns non-null, this method is not called.
     *
     * Has a default empty implementation so that factories using [getBubbleView]
     * don't need to provide a content view.
     *
     * Example usage:
     * ```kotlin
     * override fun getContentView(
     *     message: BaseMessage,
     *     alignment: MessageBubbleAlignment,
     *     style: CometChatMessageBubbleStyle,
     *     textFormatters: List<CometChatTextFormatter>
     * ): @Composable () -> Unit = {
     *     Text(text = (message as? TextMessage)?.text ?: "")
     * }
     * ```
     *
     * @param message The message to display
     * @param alignment The bubble alignment (LEFT, RIGHT, CENTER)
     * @param style The resolved bubble style
     * @param textFormatters Text formatters for mentions, markdown, and custom text transformations
     * @return Composable lambda that renders the content view
     */
    fun getContentView(
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment,
        style: CometChatMessageBubbleStyle,
        textFormatters: List<CometChatTextFormatter> = emptyList()
    ): @Composable () -> Unit = { }

    // ========================================
    // Style Resolution
    // ========================================

    /**
     * Returns a custom bubble style for this message type, or null to use defaults.
     *
     * Override this to provide per-message-type styling. The returned style will be
     * used for the bubble container (background, corner radius, stroke, padding).
     *
     * @param message The message to style
     * @param alignment The bubble alignment
     * @return Custom style or null for alignment-based defaults
     */
    fun getBubbleStyle(
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment
    ): CometChatMessageBubbleStyle? = null

    // ========================================
    // Leading View (Optional - typically avatar)
    // ========================================

    /**
     * Returns a composable for the leading view slot (typically avatar).
     *
     * @param message The message to display
     * @param alignment The bubble alignment
     * @param style The resolved bubble style
     * @return Composable lambda or null if no leading view
     */
    fun getLeadingView(
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment,
        style: CometChatMessageBubbleStyle
    ): (@Composable () -> Unit)? = null

    // ========================================
    // Header View (Optional - typically sender name)
    // ========================================

    /**
     * Returns a composable for the header view slot (typically sender name).
     *
     * @param message The message to display
     * @param alignment The bubble alignment
     * @param style The resolved bubble style
     * @param showTime Whether to show timestamp in header
     * @return Composable lambda or null if no header view
     */
    fun getHeaderView(
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment,
        style: CometChatMessageBubbleStyle,
        showTime: Boolean = false
    ): (@Composable () -> Unit)? = null

    // ========================================
    // Reply View (Optional - reply-to-message preview)
    // ========================================

    /**
     * Returns a composable for the reply view slot (quoted message preview).
     *
     * @param message The message to display
     * @param alignment The bubble alignment
     * @param style The resolved bubble style
     * @return Composable lambda or null if no reply view
     */
    fun getReplyView(
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment,
        style: CometChatMessageBubbleStyle
    ): (@Composable () -> Unit)? = null

    // ========================================
    // Bottom View (Optional - typically moderation)
    // ========================================

    /**
     * Returns a composable for the bottom view slot (typically moderation indicator).
     *
     * @param message The message to display
     * @param alignment The bubble alignment
     * @param style The resolved bubble style
     * @param hideModerationView Whether to hide the moderation indicator
     * @return Composable lambda or null if no bottom view
     */
    fun getBottomView(
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment,
        style: CometChatMessageBubbleStyle,
        hideModerationView: Boolean = false
    ): (@Composable () -> Unit)? = null

    // ========================================
    // Status Info View (Optional - typically timestamp/receipt)
    // ========================================

    /**
     * Returns a composable for the status info view slot (timestamp, receipts).
     *
     * @param message The message to display
     * @param alignment The bubble alignment
     * @param style The resolved bubble style
     * @param showTime Whether to show timestamp
     * @return Composable lambda or null if no status info view
     */
    fun getStatusInfoView(
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment,
        style: CometChatMessageBubbleStyle,
        showTime: Boolean = true
    ): (@Composable () -> Unit)? = null

    // ========================================
    // Thread View (Optional - threaded replies indicator)
    // ========================================

    /**
     * Returns a composable for the thread view slot (reply count indicator).
     *
     * @param message The message to display
     * @param alignment The bubble alignment
     * @param style The resolved bubble style
     * @param onThreadRepliesClick Callback when thread indicator is clicked
     * @return Composable lambda or null if no thread view
     */
    fun getThreadView(
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment,
        style: CometChatMessageBubbleStyle,
        onThreadRepliesClick: ((BaseMessage) -> Unit)? = null
    ): (@Composable () -> Unit)? = null

    // ========================================
    // Footer View (Optional - typically reactions)
    // ========================================

    /**
     * Returns a composable for the footer view slot (typically reactions).
     *
     * @param message The message to display
     * @param alignment The bubble alignment
     * @param style The resolved bubble style
     * @param onReactionClick Callback when a reaction is clicked
     * @param onReactionLongClick Callback when a reaction is long-clicked
     * @param onAddMoreReactionsClick Callback when add reaction button is clicked
     * @return Composable lambda or null if no footer view
     */
    fun getFooterView(
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment,
        style: CometChatMessageBubbleStyle,
        onReactionClick: ((BaseMessage, String) -> Unit)? = null,
        onReactionLongClick: ((BaseMessage, String) -> Unit)? = null,
        onAddMoreReactionsClick: ((BaseMessage) -> Unit)? = null
    ): (@Composable () -> Unit)? = null

    // ========================================
    // Lifecycle
    // ========================================

    /**
     * Called when the bubble composable leaves the composition.
     * Override to release resources like media playback or image loads.
     *
     * @param message The message whose bubble is being disposed
     */
    fun onDispose(message: BaseMessage) {
        // Default: no-op
    }
}

/**
 * Converts a list of factories to a map keyed by "category_type".
 * Last factory wins for duplicate keys.
 */
internal fun List<BubbleFactory>.toFactoryMap(): Map<String, BubbleFactory> {
    return associateBy { "${it.getCategory()}_${it.getType()}" }
}
