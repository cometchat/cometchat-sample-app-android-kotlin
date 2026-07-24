package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.multiattachment

import android.content.Context
import android.text.Spanned
import android.text.TextUtils
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import com.cometchat.chat.models.Attachment
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.conversations.utils.ConversationSubtitleRenderer
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.markdown.MarkdownViewRenderer
import com.cometchat.uikit.kotlin.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.kotlin.shared.formatters.FormatterUtils
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * ENG-36737 shared helpers for the Views per-type multi-attachment bubbles
 * (CometChatImagesBubble / VideosBubble / FilesBubble). Attachment kind is derived from the
 * attachment MIME (not the message type), and the attachment list is read from the SDK-parsed
 * `getAttachments()` first (that's where a split-send puts them), then the legacy
 * `metadata.attachments[]`, then the single `getAttachment()`.
 */
object MultiAttachmentUtils {

    const val KIND_IMAGE = "image"
    const val KIND_VIDEO = "video"
    const val KIND_AUDIO = "audio"
    const val KIND_FILE = "file"

    fun resolveAttachments(message: MediaMessage): List<Attachment> {
        message.attachments?.takeIf { it.isNotEmpty() }?.let { return it }
        resolveFromMetadata(message)?.let { return it }
        return message.attachment?.let { listOf(it) } ?: emptyList()
    }

    /**
     * `url_medium` from the Thumbnail Generation extension
     * (metadata.@injected.extensions.thumbnail-generation) — generated from the message's
     * primary (first) attachment, so it only stands in for tile/thumbnail index 0. The SDK
     * appends a `fat=` token that invalidates the CloudFront signature (HTTP 403), so it is
     * stripped. Null when the extension is disabled or the metadata is absent — callers fall
     * back to the attachment's fileUrl.
     */
    fun thumbnailUrl(message: MediaMessage): String? {
        return try {
            val thumbnailGeneration = message.metadata
                ?.optJSONObject("@injected")
                ?.optJSONObject("extensions")
                ?.optJSONObject("thumbnail-generation")
                ?: return null
            val urlMedium = thumbnailGeneration.optString("url_medium", null)
            if (urlMedium.isNullOrEmpty()) null else sanitizeThumbnailUrl(urlMedium)
        } catch (e: Exception) {
            null
        }
    }

    // The `fat` (File Access Token) param the SDK appends breaks the CloudFront signature —
    // signed thumbnail URLs already carry their own auth (Signature + Key-Pair-Id).
    private fun sanitizeThumbnailUrl(url: String): String {
        val ampIndex = url.indexOf("&fat=")
        if (ampIndex > 0) return url.substring(0, ampIndex)
        val queryIndex = url.indexOf("?fat=")
        if (queryIndex > 0) return url.substring(0, queryIndex)
        return url
    }

    private fun resolveFromMetadata(message: MediaMessage): List<Attachment>? {
        return try {
            val metadata = message.metadata ?: return null
            if (!metadata.has("attachments")) return null
            val array = metadata.getJSONArray("attachments")
            val result = ArrayList<Attachment>(array.length())
            for (i in 0 until array.length()) {
                val json = array.getJSONObject(i)
                result.add(Attachment().apply {
                    fileUrl = json.optString("url", "")
                    fileName = json.optString("fileName", "")
                    fileExtension = json.optString("extension", "")
                    fileMimeType = json.optString("mimeType", "")
                    fileSize = json.optLong("size", 0).toInt()
                })
            }
            result.takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            null
        }
    }

    fun kindOf(attachment: Attachment): String {
        val mime = attachment.fileMimeType?.lowercase().orEmpty()
        return when {
            mime.startsWith("image/") -> KIND_IMAGE
            mime.startsWith("video/") -> KIND_VIDEO
            mime.startsWith("audio/") -> KIND_AUDIO
            else -> KIND_FILE
        }
    }

    /**
     * True when the message is a mic-recorded voice note. Reads the DD / iOS contract
     * `metaData["audioType"] == "voice_note"`, falling back to the legacy `metaData["voiceNote"]`
     * Bool for messages sent before the key change.
     */
    fun isVoiceNote(message: MediaMessage): Boolean {
        val metadata = message.metadata ?: return false
        return metadata.optString(UIKitConstants.JSONKeys.AUDIO_TYPE, null) ==
            UIKitConstants.JSONKeys.AUDIO_TYPE_VOICE_NOTE ||
            metadata.optBoolean(UIKitConstants.JSONKeys.VOICE_NOTE, false)
    }

    /**
     * Count summary for a multi-attachment media message, keyed by the message type,
     * e.g. "6 Images" / "6 Videos" / "2 Audio" / "3 Files". Used by summarized previews
     * (reply preview) when the message carries more than one attachment.
     */
    fun countLabel(context: Context, messageType: String?, count: Int): String =
        when (messageType?.lowercase()) {
            UIKitConstants.MessageType.IMAGE -> context.getString(R.string.cometchat_n_images, count)
            UIKitConstants.MessageType.VIDEO -> context.getString(R.string.cometchat_n_videos, count)
            UIKitConstants.MessageType.AUDIO -> context.getString(R.string.cometchat_n_audio, count)
            else -> context.getString(R.string.cometchat_n_files, count)
        }

    /**
     * Renders a caption as a single flat [CharSequence] for the one-line preview surfaces (reply /
     * quoted preview, composer banners, search & conversation-list previews). Captions travel as
     * markdown (the composer serializes the styled compose text with `MarkdownConverter.toMarkdown()`
     * on send, same as a plain text message), so parse it back into standard Android spans via
     * [ConversationSubtitleRenderer]. Blockquotes and fenced code blocks get inline-level styling
     * only — a preview is one TextView. Bubbles must use [renderCaptionInto] instead, which gives
     * the caption the same block-level views as the text bubble.
     *
     * Pass [message] and [textFormatters] to run the same two-stage pipeline the text bubble uses:
     * formatters first (a `<@uid:...>` mention token becomes a styled display name), markdown
     * parsed from that output so the two sets of spans line up. Without them the caption still
     * renders markdown, just no mentions.
     */
    @JvmOverloads
    fun renderCaption(
        context: Context,
        caption: String,
        message: BaseMessage? = null,
        textFormatters: List<CometChatTextFormatter> = emptyList(),
        alignment: UIKitConstants.MessageBubbleAlignment = UIKitConstants.MessageBubbleAlignment.LEFT
    ): CharSequence {
        if (message == null || textFormatters.isEmpty()) {
            return ConversationSubtitleRenderer.render(context, caption)
        }
        val formatted = FormatterUtils.getFormattedText(
            context,
            message,
            UIKitConstants.FormattingType.MESSAGE_BUBBLE,
            alignment,
            caption,
            textFormatters
        )
        return ConversationSubtitleRenderer.render(
            context,
            formatted.toString(),
            formatted as? Spanned
        )
    }

    /** Default caption body size, matching the caption TextViews the bubbles used before. */
    const val CAPTION_TEXT_SIZE_SP = 15f

    /**
     * Renders a caption into a bubble's caption container as block-level views — exactly what the
     * text bubble does with a message's text, via the shared
     * [com.cometchat.uikit.kotlin.presentation.shared.messagebubble.markdown.MarkdownViewRenderer]:
     * a fenced code block becomes a bordered code container, consecutive blockquote lines a group
     * behind one vertical stripe, and list items indented prefix rows. [renderCaption] stays for the
     * one-line, single-TextView preview surfaces, which must remain flat.
     *
     * Runs the same two-stage pipeline the text bubble uses: formatters first (a `<@uid:...>` mention
     * token becomes a styled display name), then markdown parsed from that output so the two sets of
     * spans line up. Without [message]/[textFormatters] the caption still renders markdown, just no
     * mentions.
     *
     * @param container Vertical [LinearLayout] owned by the bubble; cleared on every call, so a
     *   recycled bubble never shows a previous message's caption.
     * @param captionTextColor Body color from the bubble's style; `0` falls back to the theme.
     * @param captionTextAppearance Text appearance from the bubble's style; `0` to skip.
     * @param textSizeSp Body size applied after the appearance; `0` to leave the appearance's size.
     */
    @JvmOverloads
    fun renderCaptionInto(
        container: LinearLayout,
        caption: String,
        message: BaseMessage? = null,
        textFormatters: List<CometChatTextFormatter> = emptyList(),
        alignment: UIKitConstants.MessageBubbleAlignment = UIKitConstants.MessageBubbleAlignment.LEFT,
        @ColorInt captionTextColor: Int = 0,
        @StyleRes captionTextAppearance: Int = 0,
        textSizeSp: Float = CAPTION_TEXT_SIZE_SP
    ) {
        val formatted: CharSequence = if (message == null || textFormatters.isEmpty()) {
            caption
        } else {
            FormatterUtils.getFormattedText(
                container.context,
                message,
                UIKitConstants.FormattingType.MESSAGE_BUBBLE,
                alignment,
                caption,
                textFormatters
            )
        }
        MarkdownViewRenderer.render(
            container,
            formatted.toString(),
            formatted as? Spanned,
            MarkdownViewRenderer.Style(
                textColor = captionTextColor,
                textAppearance = captionTextAppearance,
                textSizeSp = textSizeSp,
                isOutgoing = alignment == UIKitConstants.MessageBubbleAlignment.RIGHT
            )
        )
    }

    /**
     * Summarized one-line subtitle for a media message, shared by every preview surface
     * (quoted/reply preview, composer reply & edit banners): more than one attachment →
     * "N Images · caption" / "N Images"; single attachment → caption if present, else file name.
     */
    fun mediaPreviewSubtitle(context: Context, message: MediaMessage): CharSequence {
        val attachmentCount = resolveAttachments(message).size
        val caption = message.caption?.takeIf { it.isNotBlank() }
        return when {
            attachmentCount > 1 -> {
                val countLabel = countLabel(context, message.type, attachmentCount)
                if (caption != null) {
                    TextUtils.concat("$countLabel · ", renderCaption(context, caption))
                } else {
                    countLabel
                }
            }

            caption != null -> renderCaption(context, caption)

            else -> message.attachment?.fileName ?: message.type ?: ""
        }
    }

    /**
     * "Edited" label placed under a bubble's caption. Hidden until [bindEditedLabel] shows it —
     * it only appears when the message was edited AND a caption is displayed (only the caption
     * of a media message is editable, so without one there is nothing the label could refer to).
     */
    fun createEditedLabel(context: Context): TextView = TextView(context).apply {
        visibility = View.GONE
        text = context.getString(R.string.cometchat_edited)
        setTextAppearance(CometChatTheme.getTextAppearanceCaption2Regular(context))
        val horizontal = (8 * resources.displayMetrics.density).toInt()
        val top = (2 * resources.displayMetrics.density).toInt()
        // 0 bottom — the timestamp row below the bubble provides the gap (same rule as captions).
        setPadding(horizontal, top, horizontal, 0)
    }

    /** Shows the "Edited" label when the edited media message displays a caption. */
    fun bindEditedLabel(label: TextView, message: MediaMessage, @ColorInt captionColor: Int) {
        val show = message.editedAt > 0 && !message.caption.isNullOrEmpty()
        label.visibility = if (show) View.VISIBLE else View.GONE
        if (show && captionColor != 0) label.setTextColor(captionColor)
    }

    /**
     * Resolves the colored file-type icon drawable for an attachment (by MIME / extension).
     * Matches against [UIKitConstants.FileTypeMatchers] — the same source of truth the Compose
     * UIKit's `FileTypeUtils.getFileType` uses — so both UIKits show the same icon for the same
     * file (tray tiles and bubbles alike).
     */
    @DrawableRes
    fun fileIconRes(mimeType: String?, fileUrl: String?): Int {
        val mime = mimeType?.lowercase()
        val url = fileUrl?.lowercase()
        val matchers = UIKitConstants.FileTypeMatchers

        fun mimeContainsAny(keywords: List<String>) = keywords.any { mime?.contains(it) == true }
        fun urlEndsWithAny(extensions: List<String>) = extensions.any { url?.endsWith(it) == true }

        return when {
            mime?.contains(matchers.PDF_KEYWORD) == true || urlEndsWithAny(matchers.PDF_EXTENSIONS) ->
                R.drawable.cometchat_pdf_file_icon

            mimeContainsAny(matchers.DOC_KEYWORDS) || urlEndsWithAny(matchers.DOC_EXTENSIONS) ->
                R.drawable.cometchat_word_file_icon

            mimeContainsAny(matchers.XLS_KEYWORDS) || urlEndsWithAny(matchers.XLS_EXTENSIONS) ->
                R.drawable.cometchat_xlsx_file_icon

            mimeContainsAny(matchers.PPT_KEYWORDS) || urlEndsWithAny(matchers.PPT_EXTENSIONS) ->
                R.drawable.cometchat_ppt_file_icon

            mimeContainsAny(matchers.ARCHIVE_KEYWORDS) || urlEndsWithAny(matchers.ARCHIVE_EXTENSIONS) ->
                R.drawable.cometchat_zip_file_icon

            mime?.startsWith(matchers.AUDIO_MIME_PREFIX) == true ||
                urlEndsWithAny(matchers.AUDIO_EXTENSIONS) ->
                R.drawable.cometchat_audio_file_icon

            mime?.startsWith(matchers.VIDEO_MIME_PREFIX) == true ||
                urlEndsWithAny(matchers.VIDEO_EXTENSIONS) ->
                R.drawable.cometchat_video_file_icon

            mime?.startsWith(matchers.IMAGE_MIME_PREFIX) == true ||
                urlEndsWithAny(matchers.IMAGE_EXTENSIONS) ->
                R.drawable.cometchat_image_file_icon

            mime?.startsWith(matchers.TEXT_MIME_PREFIX) == true ||
                urlEndsWithAny(matchers.TEXT_EXTENSIONS) ->
                R.drawable.cometchat_text_file_icon

            matchers.LINK_PREFIXES.any { url?.startsWith(it) == true } ->
                R.drawable.cometchat_link_file_icon

            else -> R.drawable.cometchat_unknown_file_icon
        }
    }
}
