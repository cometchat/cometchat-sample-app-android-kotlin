package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.filesbubble

import android.content.Context
import android.content.res.ColorStateList
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import com.cometchat.chat.models.Attachment
import com.cometchat.chat.models.MediaMessage
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.filebubble.CometChatFileBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.multiattachment.MultiAttachmentUtils
import com.cometchat.uikit.kotlin.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.kotlin.shared.resources.utils.MediaUtils
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.google.android.material.card.MaterialCardView

/**
 * ENG-36737 — per-type multi-attachment bubble that renders every non-media attachment as a stack of
 * file cards (colored type icon + name + size/ext + download). More than 3 files collapse behind a
 * "+N more" / "Show less" toggle. Cards are a translucent overlay (white on the tinted outgoing
 * bubble, subtle dark on incoming). Replaces the single CometChatFileBubble when
 * `enableMultipleAttachments` is on.
 */
class CometChatFilesBubble @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var style: CometChatFileBubbleStyle = CometChatFileBubbleStyle()
    private var isOutgoing: Boolean = false
    private var expanded: Boolean = false
    private var files: List<Attachment> = emptyList()
    private var caption: String? = null
    private var isEdited: Boolean = false

    // The message currently bound, so a rebind of the *same* message (e.g. the search-highlight
    // fade ending in notifyItemChanged) preserves the expanded toggle instead of collapsing it.
    // Reset only when the recycled view binds a different message — the imperative equivalent of
    // the Compose bubble's `remember(message.id) { mutableStateOf(false) }`. (ENG-37143)
    private var boundMessageId: Long = -1L

    /** Kept for the caption's formatter pass (mentions are resolved against the message). */
    private var captionMessage: MediaMessage? = null
    private var textFormatters: List<CometChatTextFormatter> = emptyList()
    private var messageAlignment = UIKitConstants.MessageBubbleAlignment.LEFT

    private val bubbleWidthPx = resources.getDimensionPixelSize(R.dimen.cometchat_240dp)

    private var statusInfoVisible = true

    init {
        orientation = VERTICAL
        layoutParams = LayoutParams(bubbleWidthPx, LayoutParams.WRAP_CONTENT)
        applyRootPadding()
    }

    /**
     * Tells the bubble whether the message's timestamp/receipt row is visible below it, so it can
     * compensate with bottom padding when the row is hidden (non-last message of a batch). Called
     * by [CometChatMessageBubble][com.cometchat.uikit.kotlin.presentation.shared.messagebubble.CometChatMessageBubble].
     */
    fun setStatusInfoVisible(visible: Boolean) {
        if (statusInfoVisible == visible) return
        statusInfoVisible = visible
        applyRootPadding()
    }

    // 0 bottom while the timestamp row below provides the visual gap; pad it when the row is
    // hidden so the cards aren't flush with the bubble edge.
    private fun applyRootPadding() {
        setPadding(dp(5), dp(5), dp(5), if (statusInfoVisible) 0 else dp(5))
    }

    fun setStyle(style: CometChatFileBubbleStyle) {
        this.style = style
    }

    fun setOutgoing(outgoing: Boolean) {
        isOutgoing = outgoing
    }

    /**
     * Formatters applied to the caption, exactly as the text bubble applies them to its text (so a
     * mention resolves to a display name instead of a raw `<@uid:...>` token). Call before
     * [setMessage] — the caption is rendered there.
     */
    fun setTextFormatters(
        formatters: List<CometChatTextFormatter>?,
        alignment: UIKitConstants.MessageBubbleAlignment
    ) {
        textFormatters = formatters ?: emptyList()
        messageAlignment = alignment
    }

    fun setMessage(message: MediaMessage) {
        // A recycled bubble may have been padded for a hidden timestamp row — restore the default;
        // the adapter re-hides after bind.
        setStatusInfoVisible(true)
        files = MultiAttachmentUtils.resolveAttachments(message)
        captionMessage = message
        caption = message.caption
        isEdited = message.editedAt > 0
        // Keep the toggle open across a rebind of the same message; collapse only when this
        // recycled view is reused for a different message. (ENG-37143)
        if (message.id != boundMessageId) {
            expanded = false
            boundMessageId = message.id
        }
        render()
    }

    private fun render() {
        removeAllViews()
        if (files.isEmpty()) return

        val canCollapse = files.size > COLLAPSED_COUNT
        val visibleCount = if (!canCollapse || expanded) files.size else COLLAPSED_COUNT

        for (i in 0 until visibleCount) {
            addView(buildFileCard(files[i], i))
        }
        if (canCollapse) {
            addView(buildToggle(files.size - visibleCount))
        }
        // The caption rides the last message of a multi-attachment batch, which is the files
        // message whenever files are present (image → video → audio → file send order).
        caption?.takeIf { it.isNotEmpty() }?.let {
            addView(buildCaption(it))
            if (isEdited) addView(buildEditedLabel())
        }
    }

    private fun buildEditedLabel(): View = MultiAttachmentUtils.createEditedLabel(context).apply {
        visibility = View.VISIBLE
        setTextColor(captionColor())
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    private fun buildCaption(caption: String): View = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        // 0 bottom — the timestamp row below the bubble provides the gap (same rule as the
        // root padding); a bottom inset here doubles up and bloats captioned bubbles.
        setPadding(dp(8), dp(6), dp(8), 0)
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        // Captions travel as markdown — render the same block-level views as the text bubble.
        MultiAttachmentUtils.renderCaptionInto(
            this, caption, captionMessage, textFormatters, messageAlignment,
            captionTextColor = captionColor()
        )
    }

    // A single file needs no card separation, so it sits directly on the bubble background.
    private fun cardColor(): Int = when {
        files.size == 1 -> CometChatTheme.getColorTransparent(context)
        isOutgoing -> ColorUtils.setAlphaComponent(CometChatTheme.getColorWhite(context), 41) // ~0.16
        else -> ColorUtils.setAlphaComponent(CometChatTheme.getColorBlack(context), 10) // ~0.04
    }

    private fun titleColor(): Int =
        if (style.titleColor != 0) style.titleColor
        else if (isOutgoing) CometChatTheme.getColorWhite(context)
        else CometChatTheme.getTextColorPrimary(context)

    private fun subtitleColor(): Int =
        if (style.subtitleColor != 0) style.subtitleColor
        else if (isOutgoing) ColorUtils.setAlphaComponent(CometChatTheme.getColorWhite(context), 204) // ~0.8
        else CometChatTheme.getTextColorSecondary(context)

    // The caption is the user's message text, so it matches the image/video bubble caption
    // (full white on the tinted outgoing bubble, primary on incoming) rather than the dimmer
    // subtitle used for file size/ext.
    private fun captionColor(): Int =
        if (isOutgoing) CometChatTheme.getColorWhite(context)
        else CometChatTheme.getTextColorPrimary(context)

    // Download icon + "Show N more" toggle use the primary accent on incoming; white on the tinted
    // outgoing bubble so they stay visible.
    private fun accentColor(): Int =
        if (isOutgoing) CometChatTheme.getColorWhite(context) else CometChatTheme.getPrimaryColor(context)

    private fun buildFileCard(attachment: Attachment, index: Int): View {
        val card = MaterialCardView(context)
        Utils.initMaterialCard(card)
        card.radius = dp(12).toFloat()
        card.cardElevation = 0f
        card.strokeWidth = 0
        card.setCardBackgroundColor(cardColor())
        card.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            topMargin = if (index == 0) 0 else dp(4)
        }
        card.setOnClickListener { openAttachment(attachment) }
        card.setOnLongClickListener { v -> Utils.performAdapterClick(v); true }

        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(10), dp(10), dp(10), dp(10))
        }

        // Colored type glyph on a white rounded plate, so the icon reads on both the tinted
        // outgoing card and the light incoming one.
        val icon = MaterialCardView(context).apply {
            Utils.initMaterialCard(this)
            radius = dp(12).toFloat()
            cardElevation = 0f
            strokeWidth = 0
            setCardBackgroundColor(CometChatTheme.getColorWhite(context))
            layoutParams = LayoutParams(dp(40), dp(40))
            addView(ImageView(context).apply {
                setImageResource(MultiAttachmentUtils.fileIconRes(attachment.fileMimeType, attachment.fileUrl))
                layoutParams = android.widget.FrameLayout.LayoutParams(dp(26), dp(26), Gravity.CENTER)
            })
        }

        val textColumn = LinearLayout(context).apply {
            orientation = VERTICAL
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f).apply { marginStart = dp(12) }
        }
        val name = TextView(context).apply {
            text = attachment.fileName ?: "Unknown file"
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            setTextColor(titleColor())
            textSize = 15f
        }
        // Prefer the SDK extension / file name — the URL is signed ("...mp3?Expires=...") and its
        // last segment isn't a clean extension.
        val ext = (attachment.fileExtension?.trim('.')?.takeIf { it.isNotEmpty() }
            ?: attachment.fileName?.substringAfterLast('.', "")?.takeIf { it.isNotEmpty() }
            ?: attachment.fileUrl?.substringBefore('?')?.substringAfterLast('.', "").orEmpty())
            .uppercase()
        val subtitle = TextView(context).apply {
            text = Utils.getFileSize(attachment.fileSize) + (if (ext.isNotEmpty()) " • $ext" else "")
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            setTextColor(subtitleColor())
            textSize = 12f
        }
        textColumn.addView(name)
        textColumn.addView(subtitle)

        val download = ImageView(context).apply {
            setImageResource(R.drawable.cometchat_download_icon)
            layoutParams = LayoutParams(dp(24), dp(24))
            imageTintList = ColorStateList.valueOf(accentColor())
            setOnClickListener {
                if (!attachment.fileUrl.isNullOrEmpty()) {
                    Utils.downloadFile(context, attachment.fileUrl, attachment.fileName ?: System.currentTimeMillis().toString())
                }
            }
        }

        row.addView(icon)
        row.addView(textColumn)
        row.addView(download)
        card.addView(row)
        return card
    }

    private fun buildToggle(hidden: Int): View {
        val card = MaterialCardView(context)
        Utils.initMaterialCard(card)
        card.radius = dp(12).toFloat()
        card.cardElevation = 0f
        card.strokeWidth = 0
        card.setCardBackgroundColor(cardColor())
        card.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(4)
        }
        card.setOnClickListener {
            expanded = !expanded
            render()
        }
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(10), dp(10), dp(10), dp(10))
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }
        val chevron = ImageView(context).apply {
            setImageResource(R.drawable.cometchat_ic_arrow_down)
            imageTintList = ColorStateList.valueOf(accentColor())
            rotation = if (expanded) 180f else 0f
            layoutParams = LinearLayout.LayoutParams(dp(18), dp(18)).apply { marginEnd = dp(4) }
        }
        val label = TextView(context).apply {
            text = if (expanded) context.getString(R.string.cometchat_show_less)
            else context.getString(R.string.cometchat_show_n_more, hidden)
            gravity = Gravity.CENTER
            setTextColor(accentColor())
            textSize = 15f
        }
        row.addView(chevron)
        row.addView(label)
        card.addView(row)
        return card
    }

    private fun openAttachment(attachment: Attachment) {
        if (!attachment.fileUrl.isNullOrEmpty()) {
            MediaUtils.openMediaInPlayer(context, attachment.fileUrl, attachment.fileMimeType)
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    companion object {
        private const val COLLAPSED_COUNT = 3
    }
}
