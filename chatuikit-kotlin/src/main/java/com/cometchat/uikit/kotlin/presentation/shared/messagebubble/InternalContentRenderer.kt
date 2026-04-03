package com.cometchat.uikit.kotlin.presentation.shared.messagebubble

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StyleRes
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.Action
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.core.Call
import com.cometchat.chat.models.CustomMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar.CometChatAvatar
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.date.CometChatDate
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.date.CometChatDateStyle
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.date.DatePattern
import com.cometchat.uikit.kotlin.presentation.shared.receipts.CometChatReceipt
import com.cometchat.uikit.kotlin.presentation.shared.receipts.CometChatReceiptStyle
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.google.android.material.card.MaterialCardView
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.actionbubble.CometChatActionBubble
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.actionbubble.CometChatActionBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.audiobubble.CometChatAudioBubble
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.audiobubble.CometChatAudioBubbleStyle
import com.cometchat.uikit.kotlin.calls.CometChatCallActivity
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.callactionbubble.CometChatCallActionBubble
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.callactionbubble.CometChatCallActionBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.collaborativebubble.CometChatCollaborativeBubble
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.collaborativebubble.CometChatCollaborativeBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.deletebubble.CometChatDeleteBubble
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.deletebubble.CometChatDeleteBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.filebubble.CometChatFileBubble
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.filebubble.CometChatFileBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.imagebubble.CometChatImageBubble
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.imagebubble.CometChatImageBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.meetcallbubble.CometChatMeetCallBubble
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.meetcallbubble.CometChatMeetCallBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.pollbubble.CometChatPollBubble
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.pollbubble.CometChatPollBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.stickerbubble.CometChatStickerBubble
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.stickerbubble.CometChatStickerBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.textbubble.CometChatTextBubble
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.textbubble.CometChatTextBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.videobubble.CometChatVideoBubble
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.videobubble.CometChatVideoBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagepreview.CometChatMessagePreview
import com.cometchat.uikit.kotlin.presentation.shared.messagepreview.CometChatMessagePreviewStyle
import com.cometchat.uikit.kotlin.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.kotlin.shared.interfaces.DateTimeFormatterCallback
import java.text.SimpleDateFormat

/**
 * Consolidated default rendering for all standard message types and default slot views.
 *
 * This object replaces the individual factory classes (TextBubbleFactory, ImageBubbleFactory, etc.)
 * and MessageBubbleUtils by providing a single entry point for content view creation, content view
 * binding, and default slot view creation/binding.
 *
 * When no external [BubbleFactory] is registered for a message type, [CometChatMessageBubble]
 * delegates to this renderer for all rendering logic.
 */
internal object InternalContentRenderer {

    private const val TAG = "InternalContentRenderer"

    // Extension type constants
    internal const val EXTENSION_POLLS = "extension_poll"
    internal const val EXTENSION_STICKER = "extension_sticker"
    internal const val EXTENSION_DOCUMENT = "extension_document"
    internal const val EXTENSION_WHITEBOARD = "extension_whiteboard"

    // ================================================================
    // Content View Creation
    // ================================================================

    /**
     * Creates the content view for the given factory key.
     * Returns null if the factory key is not recognized (triggers fallback).
     *
     * @param context The Android context
     * @param factoryKey The factory key (e.g., "message_text", "deleted")
     * @return The content view, or null for unknown types
     */
    fun createContentView(context: Context, factoryKey: String): View? {
        return when (factoryKey) {
            // Standard messages
            BubbleFactory.getKey(CometChatConstants.CATEGORY_MESSAGE, CometChatConstants.MESSAGE_TYPE_TEXT) ->
                CometChatTextBubble(context)
            BubbleFactory.getKey(CometChatConstants.CATEGORY_MESSAGE, CometChatConstants.MESSAGE_TYPE_IMAGE) ->
                CometChatImageBubble(context)
            BubbleFactory.getKey(CometChatConstants.CATEGORY_MESSAGE, CometChatConstants.MESSAGE_TYPE_VIDEO) ->
                CometChatVideoBubble(context)
            BubbleFactory.getKey(CometChatConstants.CATEGORY_MESSAGE, CometChatConstants.MESSAGE_TYPE_AUDIO) ->
                CometChatAudioBubble(context)
            BubbleFactory.getKey(CometChatConstants.CATEGORY_MESSAGE, CometChatConstants.MESSAGE_TYPE_FILE) ->
                CometChatFileBubble(context)
            // Deleted
            BubbleFactory.DELETED_KEY ->
                CometChatDeleteBubble(context)
            // Action
            BubbleFactory.getKey(CometChatConstants.CATEGORY_ACTION, CometChatConstants.ActionKeys.ACTION_TYPE_GROUP_MEMBER) ->
                CometChatActionBubble(context)
            // Call
            BubbleFactory.getKey(CometChatConstants.CATEGORY_CALL, CometChatConstants.CALL_TYPE_AUDIO) ->
                CometChatCallActionBubble(context)
            BubbleFactory.getKey(CometChatConstants.CATEGORY_CALL, CometChatConstants.CALL_TYPE_VIDEO) ->
                CometChatCallActionBubble(context)
            // Meeting
            BubbleFactory.getKey(CometChatConstants.CATEGORY_CUSTOM, UIKitConstants.MessageType.MEETING) ->
                CometChatMeetCallBubble(context)
            // Extensions
            BubbleFactory.getKey(CometChatConstants.CATEGORY_CUSTOM, EXTENSION_POLLS) ->
                CometChatPollBubble(context)
            BubbleFactory.getKey(CometChatConstants.CATEGORY_CUSTOM, EXTENSION_STICKER) ->
                CometChatStickerBubble(context)
            BubbleFactory.getKey(CometChatConstants.CATEGORY_CUSTOM, EXTENSION_DOCUMENT) ->
                CometChatCollaborativeBubble(context)
            BubbleFactory.getKey(CometChatConstants.CATEGORY_CUSTOM, EXTENSION_WHITEBOARD) ->
                CometChatCollaborativeBubble(context)
            // Unknown
            else -> null
        }
    }

    // ================================================================
    // Minimal Slots
    // ================================================================

    /**
     * Determines if the message type should use minimal slot views.
     * Action and call messages are system messages that don't need full bubble chrome.
     * 
     * Note: Meeting messages (group calls) are NOT minimal - they show avatar in leading view
     * for LEFT-aligned messages (incoming). They are aligned based on sender like regular messages.
     *
     * @param message The message to check
     * @return true if the message should use minimal slots (hide header, reply, statusInfo, thread, footer)
     */
    fun shouldUseMinimalSlots(message: BaseMessage): Boolean {
        return when (message.category) {
            CometChatConstants.CATEGORY_ACTION -> true
            CometChatConstants.CATEGORY_CALL -> true
            // Meeting messages are NOT minimal - they show avatar and other slots
            else -> false
        }
    }

    /**
     * Determines if the message is a meeting message.
     *
     * Meeting messages are custom messages with type "meeting" that represent
     * group audio/video call invitations. They have their own self-contained
     * bubble (CometChatMeetCallBubble) that includes timestamp internally,
     * so the outer status info view should be hidden.
     *
     * @param message The message to check
     * @return true if the message is a meeting message, false otherwise
     */
    fun isMeetingMessage(message: BaseMessage): Boolean {
        return message.category == CometChatConstants.CATEGORY_CUSTOM &&
               message.type == UIKitConstants.MessageType.MEETING
    }

    /**
     * Determines if the status info view should be hidden for this message.
     *
     * Status info view (timestamp + receipt) should be hidden for:
     * - Messages that use minimal slots (action, call)
     * - Meeting messages (they have timestamp in the bubble itself)
     *
     * @param message The message to check
     * @param useMinimalSlots Whether the message uses minimal slots
     * @return true if status info should be hidden, false otherwise
     */
    fun shouldHideStatusInfo(message: BaseMessage, useMinimalSlots: Boolean): Boolean {
        return useMinimalSlots || isMeetingMessage(message)
    }

    // ================================================================
    // Content View Binding
    // ================================================================

    /**
     * Binds message data to a content view created by [createContentView].
     *
     * Deleted messages are handled first (regardless of category). Otherwise,
     * dispatches to the appropriate bind helper based on message category.
     *
     * @param view The content view to bind data into
     * @param message The message to bind
     * @param alignment The bubble alignment
     * @param style The resolved [CometChatMessageBubbleStyle] for the outer container
     * @param bubbleStyles Per-bubble-type style overrides; null entries fall back to defaults
     * @param holder The ViewHolder, may be null
     * @param position Position in the list, defaults to -1
     * @return true if binding succeeded, false if the message type is unrecognized
     */
    fun bindContentView(
        view: View,
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment,
        style: CometChatMessageBubbleStyle,
        bubbleStyles: BubbleStyles = BubbleStyles(),
        textFormatters: List<CometChatTextFormatter> = emptyList(),
        holder: RecyclerView.ViewHolder? = null,
        position: Int = -1
    ): Boolean {
        // Deleted messages take precedence regardless of category
        if (message.deletedAt > 0) {
            val deleteBubble = view as? CometChatDeleteBubble
                ?: run {
                    Log.w(TAG, "bindContentView: expected CometChatDeleteBubble but got ${view.javaClass.simpleName}")
                    return false
                }
            val deleteStyle = bubbleStyles.deleteBubbleStyle
                ?: when (alignment) {
                    UIKitConstants.MessageBubbleAlignment.LEFT -> CometChatDeleteBubbleStyle.incoming(view.context)
                    UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatDeleteBubbleStyle.outgoing(view.context)
                    else -> CometChatDeleteBubbleStyle.default(view.context)
                }
            deleteBubble.setStyle(deleteStyle)
            deleteBubble.setMessage(message)
            return true
        }

        return when (message.category) {
            CometChatConstants.CATEGORY_MESSAGE -> bindStandardMessage(view, message, alignment, style, bubbleStyles, textFormatters)
            CometChatConstants.CATEGORY_ACTION -> bindActionMessage(view, message, bubbleStyles)
            CometChatConstants.CATEGORY_CALL -> bindCallMessage(view, message, style, bubbleStyles)
            CometChatConstants.CATEGORY_CUSTOM -> {
                // Meeting messages are custom messages with type "meeting"
                if (message.type == UIKitConstants.MessageType.MEETING) {
                    bindMeetingMessage(view, message, alignment, style, bubbleStyles)
                } else {
                    bindCustomMessage(view, message, alignment, style, bubbleStyles)
                }
            }
            else -> {
                Log.w(TAG, "bindContentView: unrecognized category '${message.category}'")
                false
            }
        }
    }

    /**
     * Binds standard message types (text, image, video, audio, file).
     */
    private fun bindStandardMessage(
        view: View,
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment,
        baseStyle: CometChatMessageBubbleStyle,
        bubbleStyles: BubbleStyles,
        textFormatters: List<CometChatTextFormatter> = emptyList()
    ): Boolean {
        when (message.type) {
            CometChatConstants.MESSAGE_TYPE_TEXT -> {
                val textBubble = view as? CometChatTextBubble
                    ?: run {
                        Log.w(TAG, "bindStandardMessage: expected CometChatTextBubble but got ${view.javaClass.simpleName}")
                        return false
                    }
                val textStyle = bubbleStyles.textBubbleStyle
                    ?: when (alignment) {
                        UIKitConstants.MessageBubbleAlignment.LEFT -> CometChatTextBubbleStyle.incoming(view.context)
                        UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatTextBubbleStyle.outgoing(view.context)
                        else -> CometChatTextBubbleStyle.default(view.context)
                    }
                textBubble.setStyle(textStyle)
                textBubble.setMessage(message as? TextMessage, textFormatters, alignment)
            }
            CometChatConstants.MESSAGE_TYPE_IMAGE -> {
                val imageBubble = view as? CometChatImageBubble
                    ?: run {
                        Log.w(TAG, "bindStandardMessage: expected CometChatImageBubble but got ${view.javaClass.simpleName}")
                        return false
                    }
                val imageStyle = bubbleStyles.imageBubbleStyle
                    ?: when (alignment) {
                        UIKitConstants.MessageBubbleAlignment.LEFT -> CometChatImageBubbleStyle.incoming(view.context)
                        UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatImageBubbleStyle.outgoing(view.context)
                        else -> CometChatImageBubbleStyle.default(view.context)
                    }
                imageBubble.setStyle(imageStyle)
                imageBubble.setMessage(message as? MediaMessage ?: return false)
            }
            CometChatConstants.MESSAGE_TYPE_VIDEO -> {
                val videoBubble = view as? CometChatVideoBubble
                    ?: run {
                        Log.w(TAG, "bindStandardMessage: expected CometChatVideoBubble but got ${view.javaClass.simpleName}")
                        return false
                    }
                val videoStyle = bubbleStyles.videoBubbleStyle
                    ?: when (alignment) {
                        UIKitConstants.MessageBubbleAlignment.LEFT -> CometChatVideoBubbleStyle.incoming(view.context)
                        UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatVideoBubbleStyle.outgoing(view.context)
                        else -> CometChatVideoBubbleStyle.default(view.context)
                    }
                videoBubble.setStyle(videoStyle)
                videoBubble.setMessage(message as? MediaMessage ?: return false)
            }
            CometChatConstants.MESSAGE_TYPE_AUDIO -> {
                val audioBubble = view as? CometChatAudioBubble
                    ?: run {
                        Log.w(TAG, "bindStandardMessage: expected CometChatAudioBubble but got ${view.javaClass.simpleName}")
                        return false
                    }
                val audioStyle = bubbleStyles.audioBubbleStyle
                    ?: when (alignment) {
                        UIKitConstants.MessageBubbleAlignment.LEFT -> CometChatAudioBubbleStyle.incoming(view.context)
                        UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatAudioBubbleStyle.outgoing(view.context)
                        else -> CometChatAudioBubbleStyle.default(view.context)
                    }
                audioBubble.setStyle(audioStyle)
                audioBubble.setMessage(message as? MediaMessage ?: return false)
            }
            CometChatConstants.MESSAGE_TYPE_FILE -> {
                val fileBubble = view as? CometChatFileBubble
                    ?: run {
                        Log.w(TAG, "bindStandardMessage: expected CometChatFileBubble but got ${view.javaClass.simpleName}")
                        return false
                    }
                val fileStyle = bubbleStyles.fileBubbleStyle
                    ?: when (alignment) {
                        UIKitConstants.MessageBubbleAlignment.LEFT -> CometChatFileBubbleStyle.incoming(view.context)
                        UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatFileBubbleStyle.outgoing(view.context)
                        else -> CometChatFileBubbleStyle.default(view.context)
                    }
                fileBubble.setStyle(fileStyle)
                fileBubble.setMessage(message as? MediaMessage)
            }
            else -> {
                Log.w(TAG, "bindStandardMessage: unrecognized type '${message.type}'")
                return false
            }
        }
        return true
    }

    /**
     * Binds action messages (group member actions).
     */
    private fun bindActionMessage(
        view: View,
        message: BaseMessage,
        bubbleStyles: BubbleStyles
    ): Boolean {
        val actionBubble = view as? CometChatActionBubble
            ?: run {
                Log.w(TAG, "bindActionMessage: expected CometChatActionBubble but got ${view.javaClass.simpleName}")
                return false
            }
        val actionStyle = bubbleStyles.actionBubbleStyle
            ?: CometChatActionBubbleStyle.default(view.context)
        actionBubble.setStyle(actionStyle)
        actionBubble.setMessage(message as? Action)
        return true
    }

    /**
     * Binds call messages (audio/video call actions).
     */
    private fun bindCallMessage(
        view: View,
        message: BaseMessage,
        baseStyle: CometChatMessageBubbleStyle,
        bubbleStyles: BubbleStyles
    ): Boolean {
        val callBubble = view as? CometChatCallActionBubble
            ?: run {
                Log.w(TAG, "bindCallMessage: expected CometChatCallActionBubble but got ${view.javaClass.simpleName}")
                return false
            }
        val callStyle = bubbleStyles.callActionBubbleStyle
            ?: CometChatCallActionBubbleStyle.default(view.context)
        callBubble.setStyle(callStyle)
        callBubble.setMessage(message as? Call)
        return true
    }

    /**
     * Binds custom message types (polls, stickers, document, whiteboard).
     */
    private fun bindCustomMessage(
        view: View,
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment,
        baseStyle: CometChatMessageBubbleStyle,
        bubbleStyles: BubbleStyles
    ): Boolean {
        when (message.type) {
            EXTENSION_POLLS -> {
                val pollBubble = view as? CometChatPollBubble
                    ?: run {
                        Log.w(TAG, "bindCustomMessage: expected CometChatPollBubble but got ${view.javaClass.simpleName}")
                        return false
                    }
                val pollStyle = bubbleStyles.pollBubbleStyle
                    ?: when (alignment) {
                        UIKitConstants.MessageBubbleAlignment.LEFT -> CometChatPollBubbleStyle.incoming(view.context)
                        UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatPollBubbleStyle.outgoing(view.context)
                        else -> CometChatPollBubbleStyle.default(view.context)
                    }
                pollBubble.setStyle(pollStyle)
                pollBubble.setMessage(message as? CustomMessage)
            }
            EXTENSION_STICKER -> {
                val stickerBubble = view as? CometChatStickerBubble ?: return false
                val stickerStyle = bubbleStyles.stickerBubbleStyle
                    ?: when (alignment) {
                        UIKitConstants.MessageBubbleAlignment.LEFT -> CometChatStickerBubbleStyle.incoming(view.context)
                        UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatStickerBubbleStyle.outgoing(view.context)
                        else -> CometChatStickerBubbleStyle.default(view.context)
                    }
                stickerBubble.setStyle(stickerStyle)
                stickerBubble.setMessage(message as? CustomMessage)
            }
            EXTENSION_DOCUMENT, EXTENSION_WHITEBOARD -> {
                val collaborativeBubble = view as? CometChatCollaborativeBubble
                    ?: run {
                        Log.w(TAG, "bindCustomMessage: expected CometChatCollaborativeBubble but got ${view.javaClass.simpleName}")
                        return false
                    }
                val collaborativeStyle = bubbleStyles.collaborativeBubbleStyle
                    ?: when (alignment) {
                        UIKitConstants.MessageBubbleAlignment.LEFT -> CometChatCollaborativeBubbleStyle.incoming(view.context)
                        UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatCollaborativeBubbleStyle.outgoing(view.context)
                        else -> CometChatCollaborativeBubbleStyle.default(view.context)
                    }
                collaborativeBubble.setStyle(collaborativeStyle)
                collaborativeBubble.setMessage(message as? CustomMessage ?: return false)
            }
            else -> {
                Log.w(TAG, "bindCustomMessage: unrecognized custom type '${message.type}'")
                return false
            }
        }
        return true
    }

    /**
     * Binds meeting messages (group calls via custom message).
     */
    private fun bindMeetingMessage(
        view: View,
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment,
        baseStyle: CometChatMessageBubbleStyle,
        bubbleStyles: BubbleStyles
    ): Boolean {
        val meetBubble = view as? CometChatMeetCallBubble
            ?: run {
                Log.w(TAG, "bindMeetingMessage: expected CometChatMeetCallBubble but got ${view.javaClass.simpleName}")
                return false
            }
        val meetStyle = (bubbleStyles.meetCallBubbleStyle
            ?: when (alignment) {
                UIKitConstants.MessageBubbleAlignment.LEFT -> CometChatMeetCallBubbleStyle.incoming(view.context)
                UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatMeetCallBubbleStyle.outgoing(view.context)
                else -> CometChatMeetCallBubbleStyle.default(view.context)
            }).resolve(baseStyle)
        meetBubble.setStyle(meetStyle)
        meetBubble.setMessage(message as? CustomMessage)
        meetBubble.setOnJoinClick {
            CometChatCallActivity.launchConferenceCallScreen(
                view.context,
                message,
                null
            )
        }
        return true
    }

    // ================================================================
    // Default Slot View Creation (moved from MessageBubbleUtils)
    // ================================================================

    /**
     * Creates the leading view containing a [CometChatAvatar] at 32dp × 32dp.
     *
     * Inflates `cometchat_avatar_leading_view.xml` and sets margin based on alignment:
     * - LEFT alignment: 8dp end margin
     * - RIGHT alignment: 8dp start margin
     *
     * @param context The Android context
     * @param alignment The bubble alignment
     * @return The inflated leading view
     */
    fun createLeadingView(
        context: Context,
        alignment: UIKitConstants.MessageBubbleAlignment
    ): View {
        val view = View.inflate(context, R.layout.cometchat_avatar_leading_view, null)
        val avatar = view.findViewById<CometChatAvatar>(R.id.avatar)
        val layoutParams = avatar.layoutParams as LinearLayout.LayoutParams
        val margin8dp = context.resources.getDimensionPixelSize(R.dimen.cometchat_padding_2)
        when (alignment) {
            UIKitConstants.MessageBubbleAlignment.RIGHT -> {
                layoutParams.setMargins(margin8dp, 0, 0, 0)
            }
            UIKitConstants.MessageBubbleAlignment.LEFT -> {
                layoutParams.setMargins(0, 0, margin8dp, 0)
            }
            else -> { /* CENTER — no margin adjustment */ }
        }
        avatar.layoutParams = layoutParams
        return view
    }

    /**
     * Creates the header view containing a sender name [TextView] and a [CometChatDate].
     *
     * Inflates `cometchat_top_view.xml`.
     *
     * @param context The Android context
     * @return The inflated header view
     */
    fun createHeaderView(context: Context): View {
        return View.inflate(context, R.layout.cometchat_top_view, null)
    }

    /**
     * Creates the status info view containing a [CometChatDate] and [CometChatReceipt].
     *
     * Inflates `cometchat_status_info_view.xml`.
     *
     * @param context The Android context
     * @return The inflated status info view
     */
    fun createStatusInfoView(context: Context): View {
        return View.inflate(context, R.layout.cometchat_status_info_view, null)
    }

    /**
     * Creates the reply view containing a message preview component.
     *
     * Inflates `cometchat_message_preview_container.xml`.
     *
     * @param context The Android context
     * @return The inflated reply view
     */
    fun createReplyView(context: Context): View {
        return LayoutInflater.from(context).inflate(R.layout.cometchat_message_preview_container, null)
    }

    /**
     * Creates the bottom view containing a moderation indicator.
     *
     * Inflates `cometchat_moderation_message.xml`.
     *
     * @param context The Android context
     * @return The inflated bottom/moderation view
     */
    fun createBottomView(context: Context): View {
        return View.inflate(context, R.layout.cometchat_moderation_message, null)
    }

    /**
     * Creates the thread view containing a thread icon and reply count [TextView].
     *
     * Inflates `cometchat_thread_view.xml`.
     *
     * @param context The Android context
     * @return The inflated thread view
     */
    fun createThreadView(context: Context): View {
        return View.inflate(context, R.layout.cometchat_thread_view, null)
    }

    /**
     * Creates the footer view containing a reaction container.
     *
     * Inflates `cometchat_reaction_layout_container.xml` and sets gravity based on alignment:
     * - RIGHT alignment: END gravity
     * - Other alignments: START gravity
     *
     * @param context The Android context
     * @param alignment The bubble alignment
     * @return The inflated footer/reactions view
     */
    fun createFooterView(
        context: Context,
        alignment: UIKitConstants.MessageBubbleAlignment
    ): View {
        val view = View.inflate(context, R.layout.cometchat_reaction_layout_container, null)
        val reactionLayout = view.findViewById<LinearLayout>(R.id.cometchat_reaction_layout_parent_container)
        val gravity = if (alignment == UIKitConstants.MessageBubbleAlignment.RIGHT) {
            Gravity.END
        } else {
            Gravity.START
        }
        val layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        layoutParams.gravity = gravity
        reactionLayout?.layoutParams = layoutParams
        return view
    }

    // ================================================================
    // Default Slot View Binding (moved from MessageBubbleUtils)
    // ================================================================

    /**
     * Binds the sender's avatar image and name to the leading view.
     *
     * @param view The leading view created by [createLeadingView]
     * @param message The message whose sender avatar to display
     * @param alignment The bubble alignment
     */
    fun bindLeadingView(
        view: View,
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment
    ) {
        val avatar = view.findViewById<CometChatAvatar>(R.id.avatar)
        val sender = message.sender
        // Use sender name if available, otherwise use a placeholder
        // This handles cases where sender might be null (e.g., some custom messages)
        val name = sender?.name?.takeIf { it.isNotEmpty() } ?: "?"
        val url = sender?.avatar
        avatar.setAvatar(name, url)
    }

    /**
     * Binds the sender name and optionally the sent time to the header view.
     *
     * For RIGHT-aligned (outgoing) messages:
     * - Sender name is hidden (outgoing messages don't show sender name)
     * - Time is hidden (time is shown in status info view instead)
     *
     * For LEFT-aligned (incoming) messages:
     * - Sender name is shown with styling from [style]
     * - Time visibility depends on [timeStampAlignment]:
     *   - TOP: Time is shown in header view
     *   - BOTTOM: Time is hidden (shown in status info view instead)
     *
     * @param view The header view created by [createHeaderView]
     * @param message The message to display header for
     * @param alignment The bubble alignment
     * @param style The bubble style containing sender name text color and appearance
     * @param timeStampAlignment Controls where timestamp is displayed (TOP = header, BOTTOM = status info)
     */
    fun bindHeaderView(
        view: View,
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment,
        style: CometChatMessageBubbleStyle? = null,
        timeStampAlignment: UIKitConstants.TimeStampAlignment = UIKitConstants.TimeStampAlignment.BOTTOM
    ) {
        val nameText = view.findViewById<TextView>(R.id.cometchat_bubble_header_name_tv)
        val cometchatDate = view.findViewById<CometChatDate>(R.id.cometchat_bubble_header_time)

        // For RIGHT-aligned (outgoing) messages, hide both sender name and time
        // Time is shown in status info view for outgoing messages
        if (alignment == UIKitConstants.MessageBubbleAlignment.RIGHT) {
            nameText.visibility = View.GONE
            cometchatDate.visibility = View.GONE
            return
        }

        // Set sender name (only for LEFT-aligned messages)
        val sender = message.sender
        nameText.text = sender?.name ?: ""
        nameText.maxLines = 1
        nameText.ellipsize = android.text.TextUtils.TruncateAt.END
        nameText.visibility = View.VISIBLE

        // Apply sender name styling from bubble style
        // Note: Apply text appearance FIRST, then text color, because setTextAppearance can override color
        if (style != null) {
            if (style.senderNameTextAppearance != 0) {
                nameText.setTextAppearance(style.senderNameTextAppearance)
            }
            if (style.senderNameTextColor != 0) {
                nameText.setTextColor(style.senderNameTextColor)
            }
        } else {
            // Apply default styling when no style is provided
            nameText.setTextAppearance(CometChatTheme.getTextAppearanceCaption1Medium(view.context))
            nameText.setTextColor(CometChatTheme.getTextColorHighlight(view.context))
        }

        // Show time in header only when timeStampAlignment is TOP
        if (timeStampAlignment == UIKitConstants.TimeStampAlignment.TOP && message.sentAt > 0) {
            cometchatDate.setDate(message.sentAt, DatePattern.TIME)
            cometchatDate.visibility = View.VISIBLE
        } else {
            // Hide time in header - time is shown in status info view (bottom) by default
            cometchatDate.visibility = View.GONE
        }
    }

    /**
     * Binds the time, receipt status, and applies 60% opacity background to the status info view.
     *
     * When [timeStampAlignment] is TOP, the time is hidden in the status info view
     * (it's shown in the header view instead).
     *
     * @param view The status info view created by [createStatusInfoView]
     * @param message The message to display status for
     * @param alignment The bubble alignment
     * @param style The bubble style containing dateStyle and messageReceiptStyle
     * @param timeFormat Optional custom time format for message timestamps
     * @param dateTimeFormatter Optional custom date/time formatter callback
     * @param timeStampAlignment Controls where timestamp is displayed (TOP = header, BOTTOM = status info)
     * @param receiptsVisibility Controls visibility of the receipt indicator (View.VISIBLE, View.GONE, View.INVISIBLE)
     */
    fun bindStatusInfoView(
        view: View,
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment,
        style: CometChatMessageBubbleStyle,
        timeFormat: SimpleDateFormat? = null,
        dateTimeFormatter: DateTimeFormatterCallback? = null,
        timeStampAlignment: UIKitConstants.TimeStampAlignment = UIKitConstants.TimeStampAlignment.BOTTOM,
        receiptsVisibility: Int = View.VISIBLE
    ) {
        val receipt = view.findViewById<CometChatReceipt>(R.id.receipt)
        val dateTimeContainer = view.findViewById<MaterialCardView>(R.id.date_time_container)
        val cometchatDate = view.findViewById<CometChatDate>(R.id.date_time)

        Utils.initMaterialCard(dateTimeContainer)

        // Apply dateStyle from bubble style
        val dateStyle = style.dateStyle ?: CometChatDateStyle.default(view.context)
        cometchatDate.setStyle(dateStyle)

        // Apply receiptStyle from bubble style
        val receiptStyle = style.messageReceiptStyle ?: CometChatReceiptStyle.default(view.context)
        receipt.setStyle(receiptStyle)

        // Set receipt status
        receipt.setReceipt(message)

        // Apply custom time format if provided
        timeFormat?.let { cometchatDate.setTimeFormat(it) }
        
        // Apply custom date/time formatter callback if provided
        dateTimeFormatter?.let { cometchatDate.setDateTimeFormatterCallback(it) }

        // Set time - only show in status info when timeStampAlignment is BOTTOM
        // When TOP, time is shown in header view instead
        val shouldShowTimeInStatusInfo = timeStampAlignment == UIKitConstants.TimeStampAlignment.BOTTOM
        if (shouldShowTimeInStatusInfo && message.sentAt > 0) {
            cometchatDate.setDate(message.sentAt, DatePattern.TIME)
            cometchatDate.visibility = View.VISIBLE
        } else {
            cometchatDate.visibility = View.GONE
        }

        // Determine if receipt should be hidden
        // First check the receiptsVisibility setting, then the message-based logic
        val shouldHideReceipt = receiptsVisibility == View.GONE || 
            (receiptsVisibility == View.VISIBLE && shouldHideReceipt(message))
        receipt.visibility = when {
            receiptsVisibility == View.INVISIBLE -> View.INVISIBLE
            shouldHideReceipt -> View.GONE
            else -> View.VISIBLE
        }

        // Apply 60% opacity background (alpha 153) — mirrors Java reference
        val strokeWidth = cometchatDate.getDateStrokeWidth()
        val strokeColor = cometchatDate.getStrokeColor()
        val cornerRadius = cometchatDate.getDateCornerRadius()
        val backgroundColor = cometchatDate.getDateBackgroundColor()

        // Make the date itself transparent (styling moves to the container)
        cometchatDate.setDateBackgroundColor(Color.TRANSPARENT)
        cometchatDate.setStrokeColor(Color.TRANSPARENT)
        cometchatDate.setDateStrokeWidth(0)

        dateTimeContainer.strokeColor = 0
        if (backgroundColor != 0) {
            dateTimeContainer.setCardBackgroundColor(applyColorWithAlpha(backgroundColor, 153))
        }
        dateTimeContainer.radius = cornerRadius.toFloat()
        dateTimeContainer.strokeWidth = strokeWidth
        dateTimeContainer.strokeColor = strokeColor
    }

    /**
     * Binds the quoted message preview to the reply view.
     *
     * Shows the preview when `message.quotedMessage != null` AND `message.deletedAt == 0`.
     * Hides the close icon (not applicable in bubble context).
     * Applies incoming/outgoing styles based on whether the sender is the logged-in user.
     *
     * @param view The reply view created by [createReplyView]
     * @param message The message containing the quoted message
     * @param alignment The bubble alignment
     * @param textFormatters Optional list of text formatters for text messages
     * @param incomingStyle Optional style for incoming message previews
     * @param outgoingStyle Optional style for outgoing message previews
     * @param onMessagePreviewClick Optional click listener for the message preview
     */
    fun bindReplyView(
        view: View,
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment,
        textFormatters: List<CometChatTextFormatter> = emptyList(),
        incomingStyle: CometChatMessagePreviewStyle? = null,
        outgoingStyle: CometChatMessagePreviewStyle? = null,
        onMessagePreviewClick: ((BaseMessage) -> Unit)? = null
    ) {
        val messagePreview = view.findViewById<CometChatMessagePreview>(R.id.reply_message_preview)

        if (messagePreview != null && message.deletedAt == 0L) {
            val quotedMessage = message.quotedMessage
            if (quotedMessage != null) {
                // Determine if this is an outgoing message (sent by logged-in user)
                val isOutgoing = try {
                    val loggedInUser = CometChatUIKit.getLoggedInUser()
                    loggedInUser?.uid == message.sender?.uid
                } catch (e: Exception) {
                    false
                }

                // Apply appropriate style based on message direction
                val style = if (isOutgoing) {
                    outgoingStyle ?: CometChatMessagePreviewStyle.outgoing(view.context)
                } else {
                    incomingStyle ?: CometChatMessagePreviewStyle.incoming(view.context)
                }
                messagePreview.setStyle(style)

                // Hide close icon (not applicable in bubble context)
                messagePreview.setCloseIconVisibility(View.GONE)
                messagePreview.visibility = View.VISIBLE

                // Set click listener for the message preview
                if (onMessagePreviewClick != null) {
                    messagePreview.setOnMessagePreviewClickListener {
                        onMessagePreviewClick(quotedMessage)
                    }
                } else {
                    messagePreview.setOnMessagePreviewClickListener(null)
                }

                // Bind message content using the setMessage method
                messagePreview.setMessage(
                    context = view.context,
                    message = quotedMessage,
                    textFormatters = textFormatters,
                    formattingType = UIKitConstants.FormattingType.MESSAGE_BUBBLE,
                    alignment = alignment
                )
            } else {
                messagePreview.visibility = View.GONE
            }
        } else {
            messagePreview?.visibility = View.GONE
        }
    }

    /**
     * Binds the moderation indicator to the bottom view.
     *
     * Shows the moderation indicator only for disapproved [TextMessage] or [MediaMessage].
     *
     * @param view The bottom view created by [createBottomView]
     * @param message The message to check moderation status for
     * @param alignment The bubble alignment
     */
    fun bindBottomView(
        view: View,
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment
    ) {
        val parent = view.parent as? View
        val params = parent?.layoutParams as? LinearLayout.LayoutParams

        when (message) {
            is TextMessage -> {
                if (UIKitConstants.ModerationConstants.DISAPPROVED == message.moderationStatus?.name?.lowercase()) {
                    val length = message.text?.length ?: 0
                    if (params != null) {
                        if (length < 15) {
                            params.width = Utils.convertDpToPx(view.context, 200)
                        } else {
                            params.width = MATCH_PARENT
                        }
                    }
                    view.visibility = View.VISIBLE
                } else {
                    if (params != null) {
                        params.width = WRAP_CONTENT
                    }
                    view.visibility = View.GONE
                }
            }
            is MediaMessage -> {
                if (UIKitConstants.ModerationConstants.DISAPPROVED == message.moderationStatus?.name?.lowercase()) {
                    if (params != null) {
                        params.width = MATCH_PARENT
                    }
                    view.visibility = View.VISIBLE
                } else {
                    if (params != null) {
                        params.width = WRAP_CONTENT
                    }
                    view.visibility = View.GONE
                }
            }
            else -> {
                if (params != null) {
                    params.width = WRAP_CONTENT
                }
                view.visibility = View.GONE
            }
        }
        parent?.layoutParams = params
    }

    /**
     * Binds the thread reply count to the thread view.
     *
     * Shows "1 Reply" or "{count} Replies" when `message.replyCount > 0` AND `message.deletedAt == 0`.
     *
     * @param view The thread view created by [createThreadView]
     * @param message The message to display thread info for
     * @param alignment The bubble alignment
     */
    fun bindThreadView(
        view: View,
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment
    ) {
        val textView = view.findViewById<TextView>(R.id.cometchat_thread_reply_count)
        val imageView = view.findViewById<ImageView>(R.id.cometchat_thread_left_image)

        if (message.deletedAt == 0L && message.replyCount > 0) {
            imageView?.visibility = View.VISIBLE
            textView?.visibility = View.VISIBLE

            val replyText = if (message.replyCount == 1) {
                "${message.replyCount} ${view.context.getString(R.string.cometchat_reply)}"
            } else {
                "${message.replyCount} ${view.context.getString(R.string.cometchat_replies)}"
            }
            textView?.text = replyText
        } else {
            imageView?.visibility = View.GONE
            textView?.visibility = View.GONE
        }
    }

    /**
     * Binds reaction data from the message to the footer view.
     *
     * Finds the [CometChatMessageReaction] widget and populates it with reaction chips.
     * Shows the view when reactions exist, hides when they don't.
     *
     * @param view The footer view created by [createFooterView]
     * @param message The message containing reactions
     * @param alignment The bubble alignment
     */
    fun bindFooterView(
        view: View,
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment
    ) {
        val reactions = message.reactions
        if (reactions != null && reactions.isNotEmpty()) {
            view.visibility = View.VISIBLE
            val messageReaction = view.findViewById<com.cometchat.uikit.kotlin.presentation.shared.reaction.CometChatMessageReaction>(R.id.cometchat_reaction_view)
            messageReaction?.bindReactionsToMessage(message, REACTION_LIMIT)
        } else {
            view.visibility = View.GONE
        }
    }

    /**
     * Binds reaction callbacks to the footer view's [CometChatMessageReaction] widget.
     *
     * Should be called by the adapter after [bindFooterView] to wire up click handlers.
     */
    fun bindReactionCallbacks(
        view: View,
        @StyleRes reactionStyle: Int,
        onReactionClick: com.cometchat.uikit.kotlin.presentation.shared.reaction.OnReactionClick?,
        onReactionLongClick: com.cometchat.uikit.kotlin.presentation.shared.reaction.OnReactionLongClick?,
        onAddMoreReactionsClick: com.cometchat.uikit.kotlin.presentation.shared.reaction.OnAddMoreReactionsClick?
    ) {
        val messageReaction = view.findViewById<com.cometchat.uikit.kotlin.presentation.shared.reaction.CometChatMessageReaction>(R.id.cometchat_reaction_view)
        if (messageReaction != null) {
            if (reactionStyle != 0) messageReaction.setStyle(reactionStyle)
            messageReaction.setOnReactionClick(onReactionClick)
            messageReaction.setOnReactionLongClick(onReactionLongClick)
            messageReaction.setOnAddMoreReactionsClick(onAddMoreReactionsClick)
        }
    }

    private const val REACTION_LIMIT = 4

    // ================================================================
    // Private Helpers (moved from MessageBubbleUtils)
    // ================================================================

    /**
     * Determines whether the receipt indicator should be hidden for a message.
     *
     * Receipts are hidden when:
     * - The message is deleted
     * - No logged-in user exists
     * - The message was not sent by the logged-in user
     * - The message category is not "message" or "interactive"
     */
    private fun shouldHideReceipt(message: BaseMessage): Boolean {
        if (message.deletedAt != 0L) return true

        val loggedInUser = try {
            CometChatUIKit.getLoggedInUser()
        } catch (e: Exception) {
            null
        } ?: return true

        val sender = message.sender ?: return true
        val category = message.category

        val isMessageOrInteractive = category == "message" || category == "interactive"
        if (isMessageOrInteractive) {
            return sender.uid != loggedInUser.uid
        }

        // Check for incrementUnreadCount in metadata
        val metadata = message.metadata
        if (metadata != null && metadata.has("incrementUnreadCount")) {
            return sender.uid != loggedInUser.uid
        }

        return true
    }

    /**
     * Applies an alpha value to a color.
     */
    private fun applyColorWithAlpha(color: Int, alpha: Int): Int {
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }
}
