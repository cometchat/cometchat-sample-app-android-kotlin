package com.cometchat.uikit.compose.presentation.shared.messagebubble

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.models.Action
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.CustomMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.presentation.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.compose.presentation.shared.mentions.MentionTextStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatActionBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatAudioBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatCallActionBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatCollaborativeBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatDeleteBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatFileBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatImageBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatMeetCallBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatPollBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatStickerBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatTextBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatVideoBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.ui.CometChatActionBubble
import com.cometchat.uikit.compose.presentation.shared.messagebubble.ui.CometChatAudioBubble
import com.cometchat.uikit.compose.presentation.shared.messagebubble.ui.CometChatCallActionBubble
import com.cometchat.uikit.compose.presentation.shared.messagebubble.ui.CometChatCollaborativeBubble
import com.cometchat.uikit.compose.presentation.shared.messagebubble.ui.CometChatDeleteBubble
import com.cometchat.uikit.compose.presentation.shared.messagebubble.ui.CometChatFileBubble
import com.cometchat.uikit.compose.presentation.shared.messagebubble.ui.CometChatImageBubble
import com.cometchat.uikit.compose.presentation.shared.messagebubble.ui.CometChatMeetCallBubble
import com.cometchat.uikit.compose.presentation.shared.messagebubble.ui.CometChatPollBubble
import com.cometchat.uikit.compose.presentation.shared.messagebubble.ui.CometChatStickerBubble
import com.cometchat.uikit.compose.presentation.shared.messagebubble.ui.CometChatTextBubble
import com.cometchat.uikit.compose.presentation.shared.messagebubble.ui.CometChatVideoBubble
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.CometChatAvatar
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.CometChatAvatarStyle
import com.cometchat.uikit.compose.presentation.imageviewer.ui.CometChatImageViewerActivity
import com.cometchat.uikit.compose.calls.CometChatCallActivity
import com.cometchat.uikit.compose.presentation.shared.baseelements.date.CometChatDate
import com.cometchat.uikit.compose.presentation.shared.baseelements.date.DateStyle
import com.cometchat.uikit.compose.presentation.shared.baseelements.date.Pattern
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatMessageBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.mergeWithBase
import com.cometchat.uikit.compose.presentation.shared.messagepreview.CometChatMessagePreview
import com.cometchat.uikit.compose.presentation.shared.messagepreview.CometChatMessagePreviewStyle
import com.cometchat.uikit.compose.presentation.shared.receipts.CometChatReceipts
import com.cometchat.uikit.compose.presentation.shared.receipts.MessageReceiptUtils
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.constants.UIKitConstants

// ============================================================================
// Leading View Resolution API
// ============================================================================

/**
 * Represents the result of leading view resolution.
 *
 * This sealed class encapsulates the three possible outcomes when resolving
 * which leading view to display in a message bubble:
 *
 * - [CustomLeadingView]: A custom leading view was explicitly provided
 * - [FactoryLeadingView]: The factory's default leading view should be used
 * - [NoLeadingView]: No leading view should be displayed
 *
 * This abstraction allows the resolution logic to be tested independently
 * of Compose runtime, as it doesn't require actual composable functions.
 */
sealed class LeadingViewResolution {
    /**
     * Indicates that a custom leading view was explicitly provided.
     *
     * When this result is returned, the custom leading view should always
     * be rendered, regardless of [shouldShowDefaultAvatar] or factory configuration.
     *
     * **Validates: Requirements 1.2** - Custom LeadingView Providers Override Default Behavior
     */
    object CustomLeadingView : LeadingViewResolution()

    /**
     * Indicates that the factory's default leading view should be used.
     *
     * This result is returned when:
     * - No custom leading view is provided
     * - [shouldShowDefaultAvatar] is true
     *
     * The actual factory leading view may still be null if the factory
     * doesn't provide one for the given message type.
     */
    object FactoryLeadingView : LeadingViewResolution()

    /**
     * Indicates that no leading view should be displayed.
     *
     * This result is returned when:
     * - No custom leading view is provided
     * - [shouldShowDefaultAvatar] is false
     *
     * **Validates: Requirements 1.1** - Outgoing Messages Never Show Default Avatar
     */
    object NoLeadingView : LeadingViewResolution()
}

/**
 * Resolves which leading view should be displayed in a message bubble.
 *
 * This function implements the leading view resolution logic as defined in the
 * design document for the message-list-avatar-parity feature. The resolution
 * follows a strict priority order:
 *
 * 1. **Custom leadingView provider**: If [hasCustomLeadingView] is true,
 *    returns [LeadingViewResolution.CustomLeadingView]. The custom view
 *    is always used regardless of other parameters.
 *
 * 2. **shouldShowDefaultAvatar check**: If [hasCustomLeadingView] is false
 *    and [shouldShowDefaultAvatar] is false, returns [LeadingViewResolution.NoLeadingView].
 *    This hides avatars for outgoing messages or user conversations.
 *
 * 3. **Factory's getLeadingView**: If [hasCustomLeadingView] is false and
 *    [shouldShowDefaultAvatar] is true, returns [LeadingViewResolution.FactoryLeadingView].
 *    The factory's default leading view (typically an avatar) will be used.
 *
 * ## Property 2: Custom LeadingView Providers Override Default Behavior
 *
 * *For any* message with any alignment, when a custom leadingView slot provider
 * is explicitly passed, the custom leading view SHALL be rendered regardless
 * of avatar visibility configuration.
 *
 * **Validates: Requirements 1.2**
 *
 * @param hasCustomLeadingView Whether a custom leading view composable was provided
 * @param shouldShowDefaultAvatar Whether the factory's default avatar should be shown.
 *   This is typically computed based on message alignment, conversation type, and hideAvatar flag.
 * @return The resolution result indicating which leading view to use
 *
 * @see LeadingViewResolution
 * @see resolveLeadingViewWithFactory
 */
fun resolveLeadingView(
    hasCustomLeadingView: Boolean,
    shouldShowDefaultAvatar: Boolean
): LeadingViewResolution {
    return when {
        hasCustomLeadingView -> LeadingViewResolution.CustomLeadingView
        !shouldShowDefaultAvatar -> LeadingViewResolution.NoLeadingView
        else -> LeadingViewResolution.FactoryLeadingView
    }
}

/**
 * Resolves which leading view should be displayed in a message bubble,
 * considering whether the factory provides a non-null leading view.
 *
 * This function extends [resolveLeadingView] by adding support for
 * **Property 3: Custom Factory Leading Views Override Visibility Settings**.
 *
 * The resolution follows this priority order:
 *
 * 1. **Custom leadingView provider**: If [hasCustomLeadingView] is true,
 *    returns [LeadingViewResolution.CustomLeadingView]. The custom view
 *    is always used regardless of other parameters.
 *
 * 2. **Factory provides non-null leading view**: If [factoryProvidesLeadingView] is true,
 *    returns [LeadingViewResolution.FactoryLeadingView]. This allows custom factories
 *    to override the default avatar visibility settings.
 *
 * 3. **shouldShowDefaultAvatar check**: If [hasCustomLeadingView] is false,
 *    [factoryProvidesLeadingView] is false, and [shouldShowDefaultAvatar] is false,
 *    returns [LeadingViewResolution.NoLeadingView].
 *
 * 4. **Default factory leading view**: If none of the above conditions are met,
 *    returns [LeadingViewResolution.FactoryLeadingView].
 *
 * ## Property 3: Custom Factory Leading Views Override Visibility Settings
 *
 * *For any* message where the BubbleFactory returns a non-null leading view,
 * that leading view SHALL be rendered regardless of the shouldShowDefaultAvatar flag.
 *
 * **Validates: Requirements 1.3, 4.2**
 *
 * @param hasCustomLeadingView Whether a custom leading view composable was provided
 *   via the leadingView parameter in CometChatMessageBubble
 * @param factoryProvidesLeadingView Whether the BubbleFactory's getLeadingView()
 *   returns a non-null composable for the current message
 * @param shouldShowDefaultAvatar Whether the default avatar should be shown.
 *   This is typically computed based on message alignment, conversation type, and hideAvatar flag.
 * @return The resolution result indicating which leading view to use
 *
 * @see LeadingViewResolution
 * @see resolveLeadingView
 */
fun resolveLeadingViewWithFactory(
    hasCustomLeadingView: Boolean,
    factoryProvidesLeadingView: Boolean,
    shouldShowDefaultAvatar: Boolean
): LeadingViewResolution {
    return when {
        // Priority 1: Custom leadingView provider always takes precedence
        hasCustomLeadingView -> LeadingViewResolution.CustomLeadingView
        // Priority 2: Factory-provided leading view overrides visibility settings
        factoryProvidesLeadingView -> LeadingViewResolution.FactoryLeadingView
        // Priority 3: Respect shouldShowDefaultAvatar for default behavior
        !shouldShowDefaultAvatar -> LeadingViewResolution.NoLeadingView
        // Priority 4: Use factory's default leading view
        else -> LeadingViewResolution.FactoryLeadingView
    }
}

// ============================================================================
// Internal Content Renderer
// ============================================================================

/**
 * Internal content renderer for CometChatMessageBubble.
 *
 * Provides default content rendering for all standard message types without
 * requiring external factory configuration. This makes CometChatMessageBubble
 * self-contained and usable standalone.
 *
 * The renderer handles the following message categories:
 * - Standard messages (text, image, video, audio, file)
 * - Deleted messages (takes precedence over original type)
 * - Action messages (group member events)
 * - Call messages (audio/video calls)
 * - Meeting messages
 * - Custom extension messages (polls, stickers, document, whiteboard)
 *
 * When a message type is not recognized, the renderer returns false to indicate
 * that a fallback should be used.
 *
 * Example usage:
 * ```kotlin
 * val rendered = InternalContentRenderer.renderContent(
 *     message = message,
 *     alignment = alignment,
 *     styles = BubbleStyles(
 *         textBubbleStyle = CometChatTextBubbleStyle.incoming()
 *     )
 * )
 * if (!rendered) {
 *     FallbackBubble(message)
 * }
 * ```
 *
 * @see BubbleStyles
 * @see CometChatMessageBubble
 */
internal object InternalContentRenderer {

    /**
     * Log tag for warning messages.
     */
    private const val TAG = "InternalContentRenderer"

    /**
     * Message type constants for custom extensions.
     * These match the extension types used in ComposeBubbleFactory implementations.
     */
    internal const val EXTENSION_POLLS = "extension_poll"
    internal const val EXTENSION_STICKER = "extension_sticker"
    internal const val EXTENSION_DOCUMENT = "extension_document"
    internal const val EXTENSION_WHITEBOARD = "extension_whiteboard"

    /**
     * Renders the appropriate content view for the given message.
     *
     * The rendering follows this priority:
     * 1. Deleted messages (deletedAt > 0) always render as DeleteBubble
     * 2. Standard messages (category "message") render based on type
     * 3. Action messages (category "action") render as ActionBubble
     * 4. Call messages (category "call") render as CallActionBubble
     * 5. Meeting messages render as MeetCallBubble
     * 6. Custom messages (category "custom") render based on extension type
     *
     * When a message type is not recognized, a warning is logged for debugging
     * and false is returned to trigger fallback rendering.
     *
     * @param message The message to render
     * @param alignment The bubble alignment (LEFT, RIGHT, CENTER)
     * @param styles Container holding all bubble style overrides
     * @param textFormatters List of text formatters for text messages
     * @param onLongClick Callback when the bubble is long-pressed (propagates to parent message bubble)
     * @param onMentionClick Callback when a user mention is clicked in text messages
     * @param onMentionAllClick Callback when "mention all" (@all) is clicked in text messages
     * @param mentionTextStyle Optional custom style for mentions in text messages
     * @return true if content was rendered, false if type is unknown
     */
    @Composable
    fun renderContent(
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment,
        styles: BubbleStyles,
        textFormatters: List<CometChatTextFormatter> = emptyList(),
        onLongClick: (() -> Unit)? = null,
        onMentionClick: ((User) -> Unit)? = null,
        onMentionAllClick: (() -> Unit)? = null,
        mentionTextStyle: MentionTextStyle? = null
    ): Boolean {
        // Extract messageBubbleStyle from BubbleStyles for merging into alignment-based defaults
        val messageBubbleStyle = styles.messageBubbleStyle

        // Check for deleted message first (takes precedence)
        if (message.deletedAt > 0) {
            renderDeleteBubble(message, alignment, styles, messageBubbleStyle)
            return true
        }

        val rendered = when (message.category) {
            CometChatConstants.CATEGORY_MESSAGE -> renderStandardMessage(message, alignment, styles, messageBubbleStyle, textFormatters, onLongClick, onMentionClick, onMentionAllClick, mentionTextStyle)
            CometChatConstants.CATEGORY_ACTION -> renderActionMessage(message, alignment, styles, messageBubbleStyle)
            CometChatConstants.CATEGORY_CALL -> renderCallMessage(message, alignment, styles, messageBubbleStyle)
            CometChatConstants.CATEGORY_CUSTOM -> {
                // Meeting messages are custom messages with type "meeting"
                if (message.type == UIKitConstants.MessageType.MEETING) {
                    renderMeetingMessage(message, alignment, styles, messageBubbleStyle, onLongClick)
                } else {
                    renderCustomMessage(message, alignment, styles, messageBubbleStyle, onLongClick)
                }
            }
            else -> {
                logUnknownType(message)
                false
            }
        }

        // Log warning if rendering failed (unknown type within a known category)
        if (!rendered) {
            logUnknownType(message)
        }

        return rendered
    }

    /**
     * Determines if the message type should use minimal slot views.
     *
     * Action and call messages render minimally without header, footer, 
     * reactions, etc. This is because these message types are system messages 
     * that don't need the full bubble chrome.
     *
     * Note: Deleted messages are NOT minimal - they should still show the
     * header view (sender name) and status info view (timestamp, receipt).
     * Only the content view is replaced with the delete bubble.
     *
     * Note: Meeting messages are NOT minimal - they show avatar in leading view
     * for LEFT-aligned messages (incoming). They are aligned based on sender like regular messages.
     *
     * @param message The message to check
     * @return true if the message should use minimal slots, false otherwise
     */
    fun shouldUseMinimalSlots(message: BaseMessage): Boolean {
        // Deleted messages should NOT use minimal slots - they show header and status info
        // Only action and call messages use minimal slots
        // Meeting messages are NOT minimal - they show avatar and other slots
        return when (message.category) {
            CometChatConstants.CATEGORY_ACTION -> true
            CometChatConstants.CATEGORY_CALL -> true
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

    // ========================================================================
    // Logging helpers
    // ========================================================================

    /**
     * Logs a warning for unknown message types.
     *
     * This is called when internal rendering encounters a message type that
     * is not recognized, helping developers debug issues with custom message types.
     *
     * @param message The message with unknown type
     */
    private fun logUnknownType(message: BaseMessage) {
        Log.w(TAG, "Unknown message type: category=${message.category}, type=${message.type}")
    }

    /**
     * Logs a warning when a safe cast fails.
     *
     * This is called when a message cannot be cast to the expected type,
     * which may indicate a data inconsistency or SDK issue.
     *
     * @param message The message that failed to cast
     * @param expectedType The expected type name
     */
    private fun logCastFailure(message: BaseMessage, expectedType: String) {
        Log.w(TAG, "Failed to cast message to $expectedType: category=${message.category}, type=${message.type}, actualClass=${message.javaClass.simpleName}")
    }

    // ========================================================================
    // File download & open helpers
    // ========================================================================

    /**
     * Determines whether the download icon should be shown for a file message.
     *
     * Matches the Java reference behavior:
     * - If the message has no attachment (local outgoing file) → hide download icon
     * - If the message metadata contains a local "path" and that file exists → hide download icon
     * - Otherwise (remote file not yet downloaded) → show download icon
     *
     * @param mediaMessage The media message to check
     * @return true if the download icon should be shown
     */
    private fun shouldShowDownloadIcon(mediaMessage: MediaMessage): Boolean {
        // No attachment means it's a local outgoing file — no download needed
        val attachment = mediaMessage.attachment ?: return false
        if (attachment.fileUrl.isNullOrEmpty()) return false

        // Check if file already exists locally via metadata "path"
        try {
            val metadata = mediaMessage.metadata
            if (metadata != null && metadata.has("path")) {
                val path = metadata.getString("path")
                if (!path.isNullOrEmpty()) {
                    val file = java.io.File(path)
                    if (file.exists()) return false
                }
            }
        } catch (_: Exception) { }

        return true
    }

    /**
     * Opens a file attachment when the file bubble is clicked.
     *
     * Matches the Java reference behavior:
     * 1. If the file exists locally (from metadata "path") → open with FileProvider + ACTION_VIEW
     * 2. Otherwise → open the remote URL with ACTION_VIEW using the attachment's MIME type
     *
     * @param context The Android context
     * @param mediaMessage The media message containing the file
     */
    private fun openFileAttachment(context: android.content.Context, mediaMessage: MediaMessage) {
        // Try local file first
        try {
            val metadata = mediaMessage.metadata
            if (metadata != null && metadata.has("path")) {
                val path = metadata.getString("path")
                if (!path.isNullOrEmpty()) {
                    val file = java.io.File(path)
                    if (file.exists()) {
                        val uri = androidx.core.content.FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            file
                        )
                        val mimeType = mediaMessage.attachment?.fileMimeType ?: "*/*"
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, mimeType)
                            flags = android.content.Intent.FLAG_ACTIVITY_NO_HISTORY or
                                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                            addCategory(android.content.Intent.CATEGORY_DEFAULT)
                        }
                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                        }
                        return
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to open local file: ${e.message}")
        }

        // Fall back to remote URL
        val attachment = mediaMessage.attachment ?: return
        val fileUrl = attachment.fileUrl ?: return
        if (fileUrl.isEmpty()) return
        val mimeType = attachment.fileMimeType ?: "*/*"
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                setDataAndType(android.net.Uri.parse(fileUrl), mimeType)
                flags = android.content.Intent.FLAG_ACTIVITY_NO_HISTORY
                addCategory(android.content.Intent.CATEGORY_DEFAULT)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to open remote file: ${e.message}")
        }
    }

    /**
     * Downloads a single file attachment at the given index from a MediaMessage.
     *
     * Uses Android's [DownloadManager] to enqueue the download with a visible
     * notification, matching the behavior of the Java and Kotlin UI Kit implementations.
     *
     * For API 29+ (scoped storage), files are saved to the app-specific downloads directory.
     * For older APIs, files are saved to the public downloads directory.
     *
     * @param context The Android context
     * @param mediaMessage The media message containing file attachment(s)
     * @param index The index of the attachment to download
     */
    private fun downloadFileAttachment(context: android.content.Context, mediaMessage: MediaMessage, index: Int) {
        val attachments = getAttachmentsList(mediaMessage)
        val attachment = attachments.getOrNull(index) ?: return
        val fileUrl = attachment.fileUrl ?: return
        if (fileUrl.isEmpty()) return
        val fileName = attachment.fileName?.takeIf { it.isNotEmpty() }
            ?: System.currentTimeMillis().toString()
        enqueueDownload(context, fileUrl, fileName)
    }

    /**
     * Downloads all file attachments from a MediaMessage.
     *
     * @param context The Android context
     * @param mediaMessage The media message containing file attachment(s)
     */
    private fun downloadAllFileAttachments(context: android.content.Context, mediaMessage: MediaMessage) {
        val attachments = getAttachmentsList(mediaMessage)
        for (attachment in attachments) {
            val fileUrl = attachment.fileUrl ?: continue
            if (fileUrl.isEmpty()) continue
            val fileName = attachment.fileName?.takeIf { it.isNotEmpty() }
                ?: System.currentTimeMillis().toString()
            enqueueDownload(context, fileUrl, fileName)
        }
    }

    /**
     * Enqueues a file download using Android's [DownloadManager].
     *
     * @param context The Android context
     * @param fileUrl The URL of the file to download
     * @param fileName The name to save the file as
     */
    private fun enqueueDownload(context: android.content.Context, fileUrl: String, fileName: String) {
        try {
            val request = android.app.DownloadManager.Request(android.net.Uri.parse(fileUrl))
            request.setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                request.setDestinationInExternalFilesDir(context, android.os.Environment.DIRECTORY_DOWNLOADS, fileName)
            } else {
                request.setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_DOWNLOADS, fileName)
            }
            val manager = context.getSystemService(android.content.Context.DOWNLOAD_SERVICE) as? android.app.DownloadManager
            manager?.enqueue(request)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to enqueue file download: ${e.message}")
        }
    }

    /**
     * Extracts the list of attachments from a MediaMessage.
     * Checks metadata for multiple attachments first, then falls back to the single attachment.
     *
     * @param mediaMessage The media message
     * @return List of attachments (may be empty)
     */
    private fun getAttachmentsList(mediaMessage: MediaMessage): List<com.cometchat.chat.models.Attachment> {
        // Check metadata for multiple attachments
        try {
            val metadata = mediaMessage.metadata
            if (metadata != null && metadata.has("attachments")) {
                val attachmentsArray = metadata.getJSONArray("attachments")
                val result = mutableListOf<com.cometchat.chat.models.Attachment>()
                for (i in 0 until attachmentsArray.length()) {
                    val json = attachmentsArray.getJSONObject(i)
                    val attachment = com.cometchat.chat.models.Attachment().apply {
                        fileUrl = json.optString("url", "")
                        fileName = json.optString("fileName", "")
                        fileExtension = json.optString("extension", "")
                        fileMimeType = json.optString("mimeType", "")
                        fileSize = json.optLong("size", 0).toInt()
                    }
                    result.add(attachment)
                }
                if (result.isNotEmpty()) return result
            }
        } catch (_: Exception) { }

        // Fall back to single attachment
        val attachment = mediaMessage.attachment
        return if (attachment != null) listOf(attachment) else emptyList()
    }

    // ========================================================================
    // Private render methods
    // ========================================================================

    /**
     * Renders standard message types (text, image, video, audio, file).
     *
     * Uses provided style from BubbleStyles or falls back to alignment-based defaults.
     * Safe casting is used to convert BaseMessage to the appropriate type.
     * If a cast fails, a warning is logged and false is returned.
     *
     * @param message The message to render (must be category "message")
     * @param alignment The bubble alignment (LEFT, RIGHT, CENTER)
     * @param styles Container holding all bubble style overrides
     * @param messageBubbleStyle Base style for merging
     * @param textFormatters List of text formatters for text messages
     * @param onLongClick Callback when the bubble is long-pressed
     * @param onMentionClick Callback when a user mention is clicked
     * @param onMentionAllClick Callback when "mention all" is clicked
     * @param mentionTextStyle Optional custom style for mentions
     * @return true if content was rendered, false if type is unknown or cast fails
     */
    @Composable
    private fun renderStandardMessage(
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment,
        styles: BubbleStyles,
        messageBubbleStyle: CometChatMessageBubbleStyle?,
        textFormatters: List<CometChatTextFormatter> = emptyList(),
        onLongClick: (() -> Unit)? = null,
        onMentionClick: ((User) -> Unit)? = null,
        onMentionAllClick: (() -> Unit)? = null,
        mentionTextStyle: MentionTextStyle? = null
    ): Boolean {
        when (message.type) {
            CometChatConstants.MESSAGE_TYPE_TEXT -> {
                // Safe cast to TextMessage
                val textMessage = message as? TextMessage
                if (textMessage == null) {
                    logCastFailure(message, "TextMessage")
                    return false
                }
                // Use provided style or fall back to alignment-based default
                val effectiveStyle = styles.textBubbleStyle ?: getDefaultTextBubbleStyle(alignment, messageBubbleStyle)
                CometChatTextBubble(
                    message = textMessage,
                    alignment = alignment,
                    style = effectiveStyle,
                    textFormatters = textFormatters,
                    onMentionClick = onMentionClick,
                    onMentionAllClick = onMentionAllClick,
                    mentionTextStyle = mentionTextStyle
                )
            }
            CometChatConstants.MESSAGE_TYPE_IMAGE -> {
                // Safe cast to MediaMessage
                val mediaMessage = message as? MediaMessage
                if (mediaMessage == null) {
                    logCastFailure(message, "MediaMessage")
                    return false
                }
                val effectiveStyle = styles.imageBubbleStyle ?: getDefaultImageBubbleStyle(alignment, messageBubbleStyle)
                val context = LocalContext.current
                CometChatImageBubble(
                    message = mediaMessage,
                    alignment = alignment,
                    style = effectiveStyle,
                    onImageClick = { _, attachment ->
                        val imageUrl = attachment.fileUrl ?: ""
                        val fileName = attachment.fileName ?: ""
                        val mimeType = attachment.fileMimeType ?: ""
                        if (imageUrl.isNotEmpty()) {
                            context.startActivity(
                                CometChatImageViewerActivity.createIntent(
                                    context = context,
                                    imageUrl = imageUrl,
                                    fileName = fileName,
                                    mimeType = mimeType
                                )
                            )
                        }
                    },
                    onLongClick = onLongClick
                )
            }
            CometChatConstants.MESSAGE_TYPE_VIDEO -> {
                // Safe cast to MediaMessage
                val mediaMessage = message as? MediaMessage
                if (mediaMessage == null) {
                    logCastFailure(message, "MediaMessage")
                    return false
                }
                val effectiveStyle = styles.videoBubbleStyle ?: getDefaultVideoBubbleStyle(alignment, messageBubbleStyle)
                val context = LocalContext.current
                CometChatVideoBubble(
                    message = mediaMessage,
                    alignment = alignment,
                    style = effectiveStyle,
                    onVideoClick = { _, attachment ->
                        val videoUrl = attachment.fileUrl ?: ""
                        val mimeType = attachment.fileMimeType ?: "video/*"
                        if (videoUrl.isNotEmpty()) {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                            intent.setDataAndType(android.net.Uri.parse(videoUrl), mimeType)
                            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            if (intent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(intent)
                            }
                        }
                    },
                    onPlayClick = { attachment ->
                        val videoUrl = attachment.fileUrl ?: ""
                        val mimeType = attachment.fileMimeType ?: "video/*"
                        if (videoUrl.isNotEmpty()) {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                            intent.setDataAndType(android.net.Uri.parse(videoUrl), mimeType)
                            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            if (intent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(intent)
                            }
                        }
                    },
                    onLongClick = onLongClick
                )
            }
            CometChatConstants.MESSAGE_TYPE_AUDIO -> {
                // Safe cast to MediaMessage
                val mediaMessage = message as? MediaMessage
                if (mediaMessage == null) {
                    logCastFailure(message, "MediaMessage")
                    return false
                }
                val effectiveStyle = styles.audioBubbleStyle ?: getDefaultAudioBubbleStyle(alignment, messageBubbleStyle)
                CometChatAudioBubble(
                    message = mediaMessage,
                    alignment = alignment,
                    style = effectiveStyle,
                    onLongClick = onLongClick
                )
            }
            CometChatConstants.MESSAGE_TYPE_FILE -> {
                // Safe cast to MediaMessage
                val mediaMessage = message as? MediaMessage
                if (mediaMessage == null) {
                    logCastFailure(message, "MediaMessage")
                    return false
                }
                val effectiveStyle = styles.fileBubbleStyle ?: getDefaultFileBubbleStyle(alignment, messageBubbleStyle)
                val context = LocalContext.current
                val shouldShowDownload = remember(mediaMessage.id) {
                    shouldShowDownloadIcon(mediaMessage)
                }
                CometChatFileBubble(
                    message = mediaMessage,
                    alignment = alignment,
                    style = effectiveStyle,
                    showDownloadIcon = shouldShowDownload,
                    onFileClick = { _ ->
                        openFileAttachment(context, mediaMessage)
                    },
                    onDownloadClick = { index ->
                        downloadFileAttachment(context, mediaMessage, index)
                    },
                    onDownloadAllClick = {
                        downloadAllFileAttachments(context, mediaMessage)
                    },
                    onLongClick = onLongClick
                )
            }
            else -> return false
        }
        return true
    }

    // ========================================================================
    // Default style helpers - used when no style is passed
    // ========================================================================

    /**
     * Gets the default text bubble style based on alignment.
     *
     * @param alignment The bubble alignment
     * @return The appropriate style variant (incoming, outgoing, or default)
     */
    @Composable
    private fun getDefaultTextBubbleStyle(
        alignment: UIKitConstants.MessageBubbleAlignment,
        messageBubbleStyle: CometChatMessageBubbleStyle?
    ): CometChatTextBubbleStyle {
        val alignmentDefault = when (alignment) {
            UIKitConstants.MessageBubbleAlignment.LEFT -> CometChatTextBubbleStyle.incoming()
            UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatTextBubbleStyle.outgoing()
            UIKitConstants.MessageBubbleAlignment.CENTER -> CometChatTextBubbleStyle.default()
        }
        return if (messageBubbleStyle != null) {
            mergeWithBase(alignmentDefault, messageBubbleStyle)
        } else {
            alignmentDefault
        }
    }

    /**
     * Gets the default image bubble style based on alignment.
     *
     * @param alignment The bubble alignment
     * @return The appropriate style variant (incoming, outgoing, or default)
     */
    @Composable
    private fun getDefaultImageBubbleStyle(
        alignment: UIKitConstants.MessageBubbleAlignment,
        messageBubbleStyle: CometChatMessageBubbleStyle?
    ): CometChatImageBubbleStyle {
        val alignmentDefault = when (alignment) {
            UIKitConstants.MessageBubbleAlignment.LEFT -> CometChatImageBubbleStyle.incoming()
            UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatImageBubbleStyle.outgoing()
            UIKitConstants.MessageBubbleAlignment.CENTER -> CometChatImageBubbleStyle.default()
        }
        return if (messageBubbleStyle != null) {
            mergeWithBase(alignmentDefault, messageBubbleStyle)
        } else {
            alignmentDefault
        }
    }

    /**
     * Gets the default video bubble style based on alignment.
     *
     * @param alignment The bubble alignment
     * @return The appropriate style variant (incoming, outgoing, or default)
     */
    @Composable
    private fun getDefaultVideoBubbleStyle(
        alignment: UIKitConstants.MessageBubbleAlignment,
        messageBubbleStyle: CometChatMessageBubbleStyle?
    ): CometChatVideoBubbleStyle {
        val alignmentDefault = when (alignment) {
            UIKitConstants.MessageBubbleAlignment.LEFT -> CometChatVideoBubbleStyle.incoming()
            UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatVideoBubbleStyle.outgoing()
            UIKitConstants.MessageBubbleAlignment.CENTER -> CometChatVideoBubbleStyle.default()
        }
        return if (messageBubbleStyle != null) {
            mergeWithBase(alignmentDefault, messageBubbleStyle)
        } else {
            alignmentDefault
        }
    }

    /**
     * Gets the default audio bubble style based on alignment.
     *
     * @param alignment The bubble alignment
     * @return The appropriate style variant (incoming, outgoing, or default)
     */
    @Composable
    private fun getDefaultAudioBubbleStyle(
        alignment: UIKitConstants.MessageBubbleAlignment,
        messageBubbleStyle: CometChatMessageBubbleStyle?
    ): CometChatAudioBubbleStyle {
        val alignmentDefault = when (alignment) {
            UIKitConstants.MessageBubbleAlignment.LEFT -> CometChatAudioBubbleStyle.incoming()
            UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatAudioBubbleStyle.outgoing()
            UIKitConstants.MessageBubbleAlignment.CENTER -> CometChatAudioBubbleStyle.default()
        }
        return if (messageBubbleStyle != null) {
            mergeWithBase(alignmentDefault, messageBubbleStyle)
        } else {
            alignmentDefault
        }
    }

    /**
     * Gets the default file bubble style based on alignment.
     *
     * @param alignment The bubble alignment
     * @return The appropriate style variant (incoming, outgoing, or default)
     */
    @Composable
    private fun getDefaultFileBubbleStyle(
        alignment: UIKitConstants.MessageBubbleAlignment,
        messageBubbleStyle: CometChatMessageBubbleStyle?
    ): CometChatFileBubbleStyle {
        val alignmentDefault = when (alignment) {
            UIKitConstants.MessageBubbleAlignment.LEFT -> CometChatFileBubbleStyle.incoming()
            UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatFileBubbleStyle.outgoing()
            UIKitConstants.MessageBubbleAlignment.CENTER -> CometChatFileBubbleStyle.default()
        }
        return if (messageBubbleStyle != null) {
            mergeWithBase(alignmentDefault, messageBubbleStyle)
        } else {
            alignmentDefault
        }
    }

    /**
     * Gets the default delete bubble style based on alignment.
     *
     * @param alignment The bubble alignment
     * @return The appropriate style variant (incoming, outgoing, or default)
     */
    @Composable
    private fun getDefaultDeleteBubbleStyle(
        alignment: UIKitConstants.MessageBubbleAlignment,
        messageBubbleStyle: CometChatMessageBubbleStyle?
    ): CometChatDeleteBubbleStyle {
        val alignmentDefault = when (alignment) {
            UIKitConstants.MessageBubbleAlignment.LEFT -> CometChatDeleteBubbleStyle.incoming()
            UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatDeleteBubbleStyle.outgoing()
            UIKitConstants.MessageBubbleAlignment.CENTER -> CometChatDeleteBubbleStyle.default()
        }
        return if (messageBubbleStyle != null) {
            mergeWithBase(alignmentDefault, messageBubbleStyle)
        } else {
            alignmentDefault
        }
    }

    /**
     * Gets the default action bubble style based on alignment.
     *
     * @param alignment The bubble alignment
     * @return The appropriate style variant (incoming, outgoing, or default)
     */
    @Composable
    private fun getDefaultActionBubbleStyle(
        alignment: UIKitConstants.MessageBubbleAlignment,
        messageBubbleStyle: CometChatMessageBubbleStyle?
    ): CometChatActionBubbleStyle {
        val alignmentDefault = when (alignment) {
            UIKitConstants.MessageBubbleAlignment.LEFT -> CometChatActionBubbleStyle.incoming()
            UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatActionBubbleStyle.outgoing()
            UIKitConstants.MessageBubbleAlignment.CENTER -> CometChatActionBubbleStyle.default()
        }
        return if (messageBubbleStyle != null) {
            mergeWithBase(alignmentDefault, messageBubbleStyle)
        } else {
            alignmentDefault
        }
    }

    /**
     * Gets the default call action bubble style based on alignment.
     *
     * @param alignment The bubble alignment
     * @return The appropriate style variant (incoming, outgoing, or default)
     */
    @Composable
    private fun getDefaultCallActionBubbleStyle(
        alignment: UIKitConstants.MessageBubbleAlignment,
        messageBubbleStyle: CometChatMessageBubbleStyle?
    ): CometChatCallActionBubbleStyle {
        val alignmentDefault = when (alignment) {
            UIKitConstants.MessageBubbleAlignment.LEFT -> CometChatCallActionBubbleStyle.incoming()
            UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatCallActionBubbleStyle.outgoing()
            UIKitConstants.MessageBubbleAlignment.CENTER -> CometChatCallActionBubbleStyle.default()
        }
        return if (messageBubbleStyle != null) {
            mergeWithBase(alignmentDefault, messageBubbleStyle)
        } else {
            alignmentDefault
        }
    }

    /**
     * Gets the default meet call bubble style based on alignment.
     *
     * @param alignment The bubble alignment
     * @return The appropriate style variant (incoming, outgoing, or default)
     */
    @Composable
    private fun getDefaultMeetCallBubbleStyle(
        alignment: UIKitConstants.MessageBubbleAlignment,
        messageBubbleStyle: CometChatMessageBubbleStyle?
    ): CometChatMeetCallBubbleStyle {
        val alignmentDefault = when (alignment) {
            UIKitConstants.MessageBubbleAlignment.LEFT -> CometChatMeetCallBubbleStyle.incoming()
            UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatMeetCallBubbleStyle.outgoing()
            UIKitConstants.MessageBubbleAlignment.CENTER -> CometChatMeetCallBubbleStyle.default()
        }
        return if (messageBubbleStyle != null) {
            mergeWithBase(alignmentDefault, messageBubbleStyle)
        } else {
            alignmentDefault
        }
    }

    // ========================================================================
    // Placeholder implementations for other message types
    // These will be fully implemented in subsequent tasks (1.4, 1.5, 1.6)
    // ========================================================================

    /**
     * Renders deleted message bubble.
     *
     * This method is called when a message has deletedAt > 0, indicating
     * the message was deleted. It renders a CometChatDeleteBubble with
     * appropriate styling based on alignment.
     *
     * @param message The deleted message to render
     * @param alignment The bubble alignment (LEFT, RIGHT, CENTER)
     * @param styles Container holding all bubble style overrides
     */
    @Composable
    private fun renderDeleteBubble(
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment,
        styles: BubbleStyles,
        messageBubbleStyle: CometChatMessageBubbleStyle?
    ) {
        // Use provided style or fall back to alignment-based default
        val effectiveStyle = styles.deleteBubbleStyle ?: getDefaultDeleteBubbleStyle(alignment, messageBubbleStyle)
        CometChatDeleteBubble(
            message = message,
            alignment = alignment,
            style = effectiveStyle
        )
    }

    /**
     * Renders action message bubble (group member events).
     *
     * This method handles messages with category "action" and type "groupMember".
     * Action messages are system messages that indicate group membership changes
     * like "User joined the group" or "User was removed from the group".
     *
     * @param message The action message to render (must be category "action")
     * @param alignment The bubble alignment (LEFT, RIGHT, CENTER)
     * @param styles Container holding all bubble style overrides
     * @return true if content was rendered, false if type is unknown or cast fails
     */
    @Composable
    private fun renderActionMessage(
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment,
        styles: BubbleStyles,
        messageBubbleStyle: CometChatMessageBubbleStyle?
    ): Boolean {
        // Only handle groupMember type for action category
        if (message.type != CometChatConstants.ActionKeys.ACTION_TYPE_GROUP_MEMBER) {
            return false
        }

        // Safe cast to Action
        val actionMessage = message as? Action
        if (actionMessage == null) {
            logCastFailure(message, "Action")
            return false
        }

        // Use provided style or fall back to alignment-based default
        val effectiveStyle = styles.actionBubbleStyle ?: getDefaultActionBubbleStyle(alignment, messageBubbleStyle)

        CometChatActionBubble(
            message = actionMessage,
            style = effectiveStyle
        )
        return true
    }

    /**
     * Renders call message bubble (audio/video calls).
     *
     * This method handles messages with category "call" and types "audio" or "video".
     * Call messages display call-related information like incoming, outgoing, or missed calls.
     *
     * @param message The call message to render (must be category "call")
     * @param alignment The bubble alignment (LEFT, RIGHT, CENTER)
     * @param styles Container holding all bubble style overrides
     * @return true if content was rendered, false if type is unknown or cast fails
     */
    @Composable
    private fun renderCallMessage(
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment,
        styles: BubbleStyles,
        messageBubbleStyle: CometChatMessageBubbleStyle?
    ): Boolean {
        // Only handle audio and video types for call category
        if (message.type != CometChatConstants.CALL_TYPE_AUDIO &&
            message.type != CometChatConstants.CALL_TYPE_VIDEO
        ) {
            return false
        }

        // Safe cast to Call
        val callMessage = message as? Call
        if (callMessage == null) {
            logCastFailure(message, "Call")
            return false
        }

        // Use provided style or fall back to alignment-based default
        val effectiveStyle = styles.callActionBubbleStyle ?: getDefaultCallActionBubbleStyle(alignment, messageBubbleStyle)

        CometChatCallActionBubble(
            message = callMessage,
            style = effectiveStyle
        )
        return true
    }

    /**
     * Renders meeting message bubble.
     *
     * This method handles messages with category "meeting" and type "meeting".
     * Meeting messages display meeting/call invitation information with a "Join" button.
     *
     * @param message The meeting message to render (must be category "meeting")
     * @param alignment The bubble alignment (LEFT, RIGHT, CENTER)
     * @param styles Container holding all bubble style overrides
     * @param onLongClick Callback when the bubble is long-pressed
     * @return true if content was rendered, false if type is unknown or cast fails
     */
    @Composable
    private fun renderMeetingMessage(
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment,
        styles: BubbleStyles,
        messageBubbleStyle: CometChatMessageBubbleStyle?,
        onLongClick: (() -> Unit)? = null
    ): Boolean {
        // Only handle meeting type
        if (message.type != UIKitConstants.MessageType.MEETING) {
            return false
        }

        // Safe cast to CustomMessage
        val customMessage = message as? CustomMessage
        if (customMessage == null) {
            logCastFailure(message, "CustomMessage")
            return false
        }

        // Use provided style or fall back to alignment-based default
        val effectiveStyle = styles.meetCallBubbleStyle ?: getDefaultMeetCallBubbleStyle(alignment, messageBubbleStyle)

        val context = LocalContext.current

        CometChatMeetCallBubble(
            message = customMessage,
            alignment = alignment,
            style = effectiveStyle,
            onJoinClick = { _ ->
                CometChatCallActivity.launchConferenceCallScreen(
                    context = context,
                    baseMessage = customMessage
                )
            },
            onLongClick = onLongClick
        )
        return true
    }

    /**
     * Renders custom extension message bubbles (polls, stickers, document, whiteboard).
     *
     * This method handles messages with category "custom" and the following types:
     * - polls: Renders CometChatPollBubble for interactive poll messages
     * - extension_sticker: Renders CometChatStickerBubble for sticker messages
     * - document: Renders CometChatCollaborativeBubble for collaborative document messages
     * - whiteboard: Renders CometChatCollaborativeBubble for collaborative whiteboard messages
     *
     * @param message The custom message to render (must be category "custom")
     * @param alignment The bubble alignment (LEFT, RIGHT, CENTER)
     * @param styles Container holding all bubble style overrides
     * @param onLongClick Callback when the bubble is long-pressed
     * @return true if content was rendered, false if type is unknown or cast fails
     */
    @Composable
    private fun renderCustomMessage(
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment,
        styles: BubbleStyles,
        messageBubbleStyle: CometChatMessageBubbleStyle?,
        onLongClick: (() -> Unit)? = null
    ): Boolean {
        when (message.type) {
            EXTENSION_POLLS -> {
                // Safe cast to CustomMessage
                val customMessage = message as? CustomMessage
                if (customMessage == null) {
                    logCastFailure(message, "CustomMessage")
                    return false
                }
                // Use provided style or fall back to alignment-based default
                val effectiveStyle = styles.pollBubbleStyle ?: getDefaultPollBubbleStyle(alignment, messageBubbleStyle)
                CometChatPollBubble(
                    message = customMessage,
                    alignment = alignment,
                    style = effectiveStyle,
                    onLongClick = onLongClick
                )
            }
            EXTENSION_STICKER -> {
                // Safe cast to CustomMessage
                val customMessage = message as? CustomMessage
                if (customMessage == null) {
                    logCastFailure(message, "CustomMessage")
                    return false
                }
                // Use provided style or fall back to alignment-based default
                val effectiveStyle = styles.stickerBubbleStyle ?: getDefaultStickerBubbleStyle(alignment, messageBubbleStyle)
                CometChatStickerBubble(
                    message = customMessage,
                    alignment = alignment,
                    style = effectiveStyle,
                    onLongClick = onLongClick
                )
            }
            EXTENSION_DOCUMENT, EXTENSION_WHITEBOARD -> {
                // Safe cast to CustomMessage
                val customMessage = message as? CustomMessage
                if (customMessage == null) {
                    logCastFailure(message, "CustomMessage")
                    return false
                }
                // Use provided style or fall back to alignment-based default
                val effectiveStyle = styles.collaborativeBubbleStyle ?: getDefaultCollaborativeBubbleStyle(alignment, messageBubbleStyle)
                CometChatCollaborativeBubble(
                    message = customMessage,
                    alignment = alignment,
                    style = effectiveStyle,
                    onLongClick = onLongClick
                )
            }
            else -> return false
        }
        return true
    }

    // ========================================================================
    // Default style helpers for custom extension messages
    // ========================================================================

    /**
     * Gets the default poll bubble style based on alignment.
     *
     * @param alignment The bubble alignment
     * @return The appropriate style variant (incoming, outgoing, or default)
     */
    @Composable
    private fun getDefaultPollBubbleStyle(
        alignment: UIKitConstants.MessageBubbleAlignment,
        messageBubbleStyle: CometChatMessageBubbleStyle?
    ): CometChatPollBubbleStyle {
        val alignmentDefault = when (alignment) {
            UIKitConstants.MessageBubbleAlignment.LEFT -> CometChatPollBubbleStyle.incoming()
            UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatPollBubbleStyle.outgoing()
            UIKitConstants.MessageBubbleAlignment.CENTER -> CometChatPollBubbleStyle.default()
        }
        return if (messageBubbleStyle != null) {
            mergeWithBase(alignmentDefault, messageBubbleStyle)
        } else {
            alignmentDefault
        }
    }

    /**
     * Gets the default sticker bubble style based on alignment.
     *
     * @param alignment The bubble alignment
     * @return The appropriate style variant (incoming, outgoing, or default)
     */
    @Composable
    private fun getDefaultStickerBubbleStyle(
        alignment: UIKitConstants.MessageBubbleAlignment,
        messageBubbleStyle: CometChatMessageBubbleStyle?
    ): CometChatStickerBubbleStyle {
        val alignmentDefault = when (alignment) {
            UIKitConstants.MessageBubbleAlignment.LEFT -> CometChatStickerBubbleStyle.incoming()
            UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatStickerBubbleStyle.outgoing()
            UIKitConstants.MessageBubbleAlignment.CENTER -> CometChatStickerBubbleStyle.default()
        }
        return if (messageBubbleStyle != null) {
            mergeWithBase(alignmentDefault, messageBubbleStyle)
        } else {
            alignmentDefault
        }
    }

    /**
     * Gets the default collaborative bubble style based on alignment.
     *
     * @param alignment The bubble alignment
     * @return The appropriate style variant (incoming, outgoing, or default)
     */
    @Composable
    private fun getDefaultCollaborativeBubbleStyle(
        alignment: UIKitConstants.MessageBubbleAlignment,
        messageBubbleStyle: CometChatMessageBubbleStyle?
    ): CometChatCollaborativeBubbleStyle {
        val alignmentDefault = when (alignment) {
            UIKitConstants.MessageBubbleAlignment.LEFT -> CometChatCollaborativeBubbleStyle.incoming()
            UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatCollaborativeBubbleStyle.outgoing()
            UIKitConstants.MessageBubbleAlignment.CENTER -> CometChatCollaborativeBubbleStyle.default()
        }
        return if (messageBubbleStyle != null) {
            mergeWithBase(alignmentDefault, messageBubbleStyle)
        } else {
            alignmentDefault
        }
    }

    // ========================================================================
    // Default Slot View Composables
    // ========================================================================

    /**
     * Default leading view that renders the sender's avatar.
     *
     * Displays a 32×32dp circular [CometChatAvatar] with the sender's image and name.
     * Applies 8dp end margin for LEFT alignment, 8dp start margin for RIGHT.
     * Uses cornerRadius of 16dp (50% of 32dp) to ensure circular shape.
     *
     * @param message The message whose sender avatar to display
     * @param alignment The bubble alignment
     * @param style The bubble style (unused here but kept for API consistency)
     */
    @Composable
    fun DefaultLeadingView(
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment,
        style: CometChatMessageBubbleStyle
    ) {
        val sender = message.sender
        // Apply padding first (as margin), then size - this ensures the avatar is not cut off
        val avatarModifier = when (alignment) {
            UIKitConstants.MessageBubbleAlignment.LEFT -> Modifier
                .padding(end = 8.dp)
                .size(32.dp)
            UIKitConstants.MessageBubbleAlignment.RIGHT -> Modifier
                .padding(start = 8.dp)
                .size(32.dp)
            else -> Modifier.size(32.dp)
        }

        // Use sender name if available, otherwise use a placeholder
        // This handles cases where sender might be null (e.g., some custom messages)
        val displayName = sender?.name?.takeIf { it.isNotEmpty() } ?: "?"
        
        // Use cornerRadius of 16dp (50% of 32dp size) to ensure circular avatar
        CometChatAvatar(
            modifier = avatarModifier,
            name = displayName,
            avatarUrl = sender?.avatar,
            style = CometChatAvatarStyle.default(cornerRadius = 16.dp)
        )
    }

    /**
     * Default header view that renders the sender name and message time.
     *
     * Shows a single-line sender name (max 240dp, ellipsis) followed by a
     * [CometChatDate] with [Pattern.TIME] and 5dp start margin.
     * The entire row has 4dp bottom padding.
     *
     * Note: Sender name is never shown for outgoing (RIGHT alignment) messages.
     *
     * @param message The message to display header for
     * @param alignment The bubble alignment
     * @param style The bubble style providing sender name color/typography
     * @param showName Whether to show the sender name (ignored for RIGHT alignment)
     * @param showTime Whether to show the time
     * @param timeFormat Optional custom time format pattern (e.g. "HH:mm") for the timestamp
     * @param dateTimeFormatter Optional callback for advanced timestamp formatting; takes sentAt (seconds) and returns formatted string
     */
    @Composable
    fun DefaultHeaderView(
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment,
        style: CometChatMessageBubbleStyle,
        showName: Boolean = true,
        showTime: Boolean = true,
        timeFormat: String? = null,
        dateTimeFormatter: ((Long) -> String)? = null
    ) {
        // Sender name is only shown for group conversations with non-RIGHT alignment
        val shouldShowName = showName
            && alignment != UIKitConstants.MessageBubbleAlignment.RIGHT
            && message.receiverType == CometChatConstants.RECEIVER_TYPE_GROUP
        
        // Don't render anything if both name and time are hidden
        if (!shouldShowName && !showTime) return
        
        Row(
            modifier = Modifier.padding(bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (shouldShowName) {
                Text(
                    text = message.sender?.name ?: "",
                    color = style.senderNameTextColor,
                    style = style.senderNameTextStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 240.dp)
                )
            }
            if (showTime) {
                // When dateTimeFormatter is provided, use it to format the timestamp
                // When timeFormat is provided, use it as the time pattern
                // Otherwise, use the default CometChatDate pattern
                val customDateString = if (dateTimeFormatter != null && message.sentAt > 0) {
                    dateTimeFormatter(message.sentAt)
                } else {
                    null
                }

                CometChatDate(
                    timestamp = message.sentAt,
                    pattern = Pattern.TIME,
                    timePattern = timeFormat ?: "h:mm a",
                    customDateString = customDateString,
                    modifier = if (shouldShowName) Modifier.padding(start = 5.dp) else Modifier
                )
            }
        }
    }

    /**
     * Default status info view that renders the message time and delivery receipt.
     *
     * Shows a [CometChatDate] (time) and [CometChatReceipts] in a horizontal row.
     * The status info is aligned to the end (right side) of the bubble, matching
     * the XML layout's `android:layout_gravity="bottom|end"`.
     * 
     * Background styling:
     * - For sticker messages (category "custom", type "extension_sticker"): Shows a
     *   semi-transparent background (60% opacity) with rounded corners
     * - For all other message types: No background, just the time and receipt
     *
     * @param message The message to display status for
     * @param alignment The bubble alignment
     * @param style The bubble style containing timestamp color and style
     * @param showReceipt Whether to show the receipt indicator
     * @param showTime Whether to show the time
     * @param hideReceipts When true, hides the receipt indicator regardless of message state
     * @param timeFormat Optional custom time format pattern (e.g. "HH:mm") for the timestamp
     * @param dateTimeFormatter Optional callback for advanced timestamp formatting; takes sentAt (seconds) and returns formatted string
     */
    @Composable
    fun DefaultStatusInfoView(
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment,
        style: CometChatMessageBubbleStyle,
        showReceipt: Boolean = true,
        showTime: Boolean = true,
        hideReceipts: Boolean = false,
        timeFormat: String? = null,
        dateTimeFormatter: ((Long) -> String)? = null
    ) {
        val shouldHideReceipt = hideReceipts || MessageReceiptUtils.shouldHideReceipt(message)
        val receipt = MessageReceiptUtils.getMessageReceipt(message)
        
        // Only show background for sticker messages (custom category with sticker type)
        val isSticker = message.category == "custom" && message.type == "extension_sticker"

        // Status info view - renders the timestamp and receipt.
        // Alignment to the end of the bubble is handled by the parent (CometChatMessageBubble)
        // which wraps this in a Box with Alignment.End.
        Row(
            modifier = Modifier
                .padding(start = 4.dp, top = 4.dp, end = 4.dp, bottom = 4.dp)
                .then(
                    if (isSticker) {
                        Modifier
                            .background(
                                color = CometChatTheme.colorScheme.backgroundColor2.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    } else {
                        Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    }
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showTime) {
                // When dateTimeFormatter is provided, use it to format the timestamp
                // When timeFormat is provided, use it as the time pattern
                // Otherwise, use the default CometChatDate pattern
                val customDateString = if (dateTimeFormatter != null && message.sentAt > 0) {
                    dateTimeFormatter(message.sentAt)
                } else {
                    null
                }

                CometChatDate(
                    timestamp = message.sentAt,
                    pattern = Pattern.TIME,
                    timePattern = timeFormat ?: "h:mm a",
                    customDateString = customDateString,
                    style = DateStyle.default(
                        textColor = style.timestampTextColor,
                        textStyle = style.timestampTextStyle
                    )
                )
            }
            if (showReceipt && !shouldHideReceipt) {
                CometChatReceipts(
                    receipt = receipt,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(16.dp)
                )
            }
        }
    }

    /**
     * Default thread view that renders a thread icon and reply count.
     *
     * Shows a 16×16dp thread icon followed by "1 Reply" or "{count} Replies".
     * Renders nothing when `replyCount == 0` or the message is deleted.
     * The row has 4dp horizontal padding and 8dp top padding, and is clickable.
     *
     * @param message The message to display thread info for
     * @param alignment The bubble alignment
     * @param style The bubble style providing thread indicator color/typography
     * @param onThreadClick Callback when the thread indicator is clicked
     */
    @Composable
    fun DefaultThreadView(
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment,
        style: CometChatMessageBubbleStyle,
        onThreadClick: ((BaseMessage) -> Unit)? = null
    ) {
        if (message.replyCount <= 0 || message.deletedAt != 0L) return

        val replyText = if (message.replyCount == 1) {
            "${message.replyCount} Reply"
        } else {
            "${message.replyCount} Replies"
        }

        Row(
            modifier = Modifier
                .clickable { onThreadClick?.invoke(message) }
                .padding(horizontal = 4.dp)
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.cometchat_ic_thread),
                contentDescription = "Thread replies",
                tint = style.threadIndicatorIconTint,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = replyText,
                color = style.threadIndicatorTextColor,
                style = style.threadIndicatorTextStyle,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }

    /**
     * Default reply view that renders a preview of the quoted message using
     * [CometChatMessagePreview].
     *
     * Renders the message preview only when the message has a non-null
     * `quotedMessage` and `deletedAt == 0`. The close icon is always hidden
     * in bubble context. The preview style is selected based on whether the
     * message sender matches the logged-in user (outgoing vs incoming).
     *
     * @param message The message containing the quoted message
     * @param alignment The bubble alignment
     * @param style The bubble style
     * @param textFormatters Text formatters for quoted text message content
     * @param incomingMessagePreviewStyle Optional style override for incoming message previews
     * @param outgoingMessagePreviewStyle Optional style override for outgoing message previews
     * @param onMessagePreviewClick Callback when the quoted message preview is tapped
     */
    @Composable
    fun DefaultReplyView(
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment,
        style: CometChatMessageBubbleStyle,
        textFormatters: List<CometChatTextFormatter> = emptyList(),
        incomingMessagePreviewStyle: CometChatMessagePreviewStyle? = null,
        outgoingMessagePreviewStyle: CometChatMessagePreviewStyle? = null,
        onMessagePreviewClick: ((BaseMessage) -> Unit)? = null
    ) {
        val quotedMessage = message.quotedMessage ?: return
        if (message.deletedAt != 0L) return

        // Determine if outgoing based on logged-in user
        val isOutgoing = try {
            CometChatUIKit.getLoggedInUser()?.uid == message.sender?.uid
        } catch (e: Exception) {
            false
        }

        val previewStyle = if (isOutgoing) {
            outgoingMessagePreviewStyle ?: CometChatMessagePreviewStyle.outgoing()
        } else {
            incomingMessagePreviewStyle ?: CometChatMessagePreviewStyle.incoming()
        }

        CometChatMessagePreview(
            message = quotedMessage,
            style = previewStyle,
            showCloseIcon = false, // Always hidden in bubble context
            textFormatters = textFormatters,
            alignment = alignment,
            onClick = onMessagePreviewClick?.let { callback ->
                { callback(quotedMessage) }
            },
            modifier = Modifier.padding(4.dp) // Match kotlin-uikit's cometchat_margin_1
        )
    }

    /**
     * Default bottom view that renders a moderation indicator for disapproved messages.
     *
     * Shows a moderation notice only when the message is a [TextMessage] or
     * [MediaMessage] with moderation status "disapproved". For short text
     * (< 15 chars), constrains width to 200dp.
     *
     * @param message The message to check moderation status for
     * @param alignment The bubble alignment
     * @param style The bubble style
     * @param hideModerationView When true, the moderation indicator is not rendered
     *   regardless of the message's moderation status. Default is false.
     */
    @Composable
    fun DefaultBottomView(
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment,
        style: CometChatMessageBubbleStyle,
        hideModerationView: Boolean = false
    ) {
        // Skip rendering if hideModerationView is true
        if (hideModerationView) return

        val isDisapproved = when (message) {
            is TextMessage -> message.moderationStatus?.name == "DISAPPROVED"
            is MediaMessage -> message.moderationStatus?.name == "DISAPPROVED"
            else -> false
        }

        if (!isDisapproved) return

        val widthModifier = if (message is TextMessage) {
            val textLength = message.text?.length ?: 0
            if (textLength < 15) Modifier.widthIn(min = 200.dp) else Modifier
        } else {
            Modifier
        }

        Box(
            modifier = widthModifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .background(
                    color = CometChatTheme.colorScheme.errorColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(8.dp)
        ) {
            Text(
                text = "This message has been moderated",
                color = CometChatTheme.colorScheme.errorColor,
                style = CometChatTheme.typography.caption1Regular
            )
        }
    }

    /**
     * Default footer view that renders reaction chips below the bubble.
     *
     * Shows reaction chips when the message has non-null, non-empty reactions.
     * Aligns reactions to END for RIGHT alignment, START for LEFT alignment.
     *
     * @param message The message containing reactions
     * @param alignment The bubble alignment
     * @param style The bubble style
     * @param onReactionClick Callback when a reaction chip is clicked
     * @param onReactionLongClick Callback when a reaction chip is long-clicked
     * @param onAddMoreReactionsClick Callback when the add reaction button is clicked
     */
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun DefaultFooterView(
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment,
        style: CometChatMessageBubbleStyle,
        onReactionClick: ((BaseMessage, String) -> Unit)? = null,
        onReactionLongClick: ((BaseMessage, String) -> Unit)? = null,
        onAddMoreReactionsClick: ((BaseMessage) -> Unit)? = null
    ) {
        val reactions = message.reactions
        if (reactions.isNullOrEmpty()) return

        val horizontalArrangement = when (alignment) {
            UIKitConstants.MessageBubbleAlignment.RIGHT -> Arrangement.End
            else -> Arrangement.Start
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = horizontalArrangement
        ) {
            reactions.forEach { reactionCount ->
                val emoji = reactionCount.reaction ?: return@forEach
                val count = reactionCount.count

                Box(
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .background(
                            color = if (reactionCount.reactedByMe)
                                CometChatTheme.colorScheme.extendedPrimaryColor100
                            else
                                CometChatTheme.colorScheme.backgroundColor3,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .then(
                            if (reactionCount.reactedByMe)
                                Modifier.border(
                                    width = 1.dp,
                                    color = CometChatTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            else Modifier
                        )
                        .combinedClickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onReactionClick?.invoke(message, emoji) },
                            onLongClick = { onReactionLongClick?.invoke(message, emoji) }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$emoji $count",
                        style = CometChatTheme.typography.caption1Medium,
                        color = CometChatTheme.colorScheme.textColorPrimary
                    )
                }
            }

            // Add more reactions button
            Box(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .background(
                        color = CometChatTheme.colorScheme.backgroundColor3,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onAddMoreReactionsClick?.invoke(message) }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.cometchat_add_reaction),
                    contentDescription = "Add reaction",
                    tint = CometChatTheme.colorScheme.iconTintSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
