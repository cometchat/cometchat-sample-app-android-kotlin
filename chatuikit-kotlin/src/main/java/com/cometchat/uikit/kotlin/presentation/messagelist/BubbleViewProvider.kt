package com.cometchat.uikit.kotlin.presentation.messagelist

import android.content.Context
import android.view.View
import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.core.constants.UIKitConstants

/**
 * Callback interface for providing custom views for message bubble slots.
 *
 * Used by [CometChatMessageList] to allow developers to customize views based on message data.
 * This interface separates view creation from data binding for efficient RecyclerView recycling:
 *
 * - [createView] is called once when ViewHolder is created
 * - [bindView] is called each time a message is bound to update the view
 *
 * The returned View is passed directly to `CometChatMessageBubble.setXxxView(view)`.
 *
 * Example usage:
 * ```kotlin
 * messageList.setHeaderViewProvider(object : BubbleViewProvider {
 *     override fun createView(
 *         context: Context,
 *         message: BaseMessage,
 *         alignment: MessageBubbleAlignment
 *     ): View? {
 *         // Create view structure once
 *         return if (alignment == MessageBubbleAlignment.LEFT) {
 *             TextView(context).apply {
 *                 textSize = 12f
 *             }
 *         } else null
 *     }
 *
 *     override fun bindView(
 *         view: View,
 *         message: BaseMessage,
 *         alignment: MessageBubbleAlignment
 *     ) {
 *         // Update view with message data
 *         (view as? TextView)?.text = message.sender?.name
 *     }
 * })
 * ```
 */
interface BubbleViewProvider {

    /**
     * Creates a view for the slot. Called once when ViewHolder is created.
     * The view will be recycled and reused for different messages.
     *
     * @param context The Android context
     * @param message The message (used to determine view type if needed)
     * @param alignment The bubble alignment (LEFT, RIGHT, CENTER)
     * @return The view to display, or null to use default/hide the slot
     */
    fun createView(
        context: Context,
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment
    ): View?

    /**
     * Binds message data to an existing view. Called every time a message is displayed.
     * Update the view's content based on the message data.
     *
     * @param view The recycled view from [createView]
     * @param message The message to display
     * @param alignment The bubble alignment (LEFT, RIGHT, CENTER)
     */
    fun bindView(
        view: View,
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment
    )
}
