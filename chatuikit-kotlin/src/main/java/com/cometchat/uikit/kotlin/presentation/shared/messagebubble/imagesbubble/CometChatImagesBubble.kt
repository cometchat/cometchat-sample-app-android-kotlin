package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.imagesbubble

import android.content.Context
import android.util.AttributeSet
import android.util.LruCache
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.gridlayout.widget.GridLayout
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.cometchat.chat.models.Attachment
import com.cometchat.chat.models.MediaMessage
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.imagebubble.CometChatImageBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.multiattachment.MultiAttachmentUtils
import com.cometchat.uikit.kotlin.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.google.android.material.card.MaterialCardView

/**
 * ENG-36737 — per-type multi-attachment bubble that renders every image attachment of a message
 * as a grid. Replaces the single [com.cometchat.uikit.kotlin.presentation.shared.messagebubble.imagebubble.CometChatImageBubble]
 * on the receive side when `enableMultipleAttachments` is on (the single bubble stays as the
 * deprecated fallback). Grid rules (match Compose): 1 = full-width; 2 = two squares side-by-side;
 * 3 = two squares + one full-width; 4 = 2x2; 5+ = 2x2 with a "+N" overlay on the 4th tile.
 */
open class CometChatImagesBubble @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    protected val isVideo: Boolean get() = renderAsVideo

    private var renderAsVideo: Boolean = false
    private var style: CometChatImageBubbleStyle = CometChatImageBubbleStyle()
    private val gridLayout: GridLayout = GridLayout(context)
    // Block-level caption (code blocks / blockquotes / lists get their own child views, as in the
    // text bubble), so the caption is a container rather than a single TextView.
    private val captionContainer: LinearLayout = LinearLayout(context)
    private val editedTextView: TextView = MultiAttachmentUtils.createEditedLabel(context)

    private var onMediaClick: ((Int, Attachment) -> Unit)? = null
    private var onMoreClick: ((List<Attachment>) -> Unit)? = null

    private val bubbleWidthPx = resources.getDimensionPixelSize(R.dimen.cometchat_240dp)
    private val insetPx = dp(5)
    private val spacingPx = dp(2)

    private var statusInfoVisible = true

    /** Thumbnail Generation extension url_medium for the current message (first attachment). */
    private var messageThumbnailUrl: String? = null

    private var textFormatters: List<CometChatTextFormatter> = emptyList()
    private var messageAlignment = UIKitConstants.MessageBubbleAlignment.LEFT

    init {
        orientation = VERTICAL
        layoutParams = LayoutParams(bubbleWidthPx, LayoutParams.WRAP_CONTENT)
        applyRootPadding()
        addView(gridLayout, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        captionContainer.apply {
            orientation = VERTICAL
            visibility = View.GONE
            // 0 bottom — the timestamp row below the bubble provides the gap (same rule as the
            // root padding); a bottom inset here doubles up and bloats captioned bubbles.
            setPadding(dp(8), dp(6), dp(8), 0)
        }
        addView(captionContainer, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        addView(editedTextView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
    }

    /** Marks this instance as the video variant (play badge + duration on each tile). */
    protected fun setRenderAsVideo(video: Boolean) {
        renderAsVideo = video
    }

    fun setStyle(style: CometChatImageBubbleStyle) {
        this.style = style
    }

    fun setOnMediaClickListener(listener: (Int, Attachment) -> Unit) {
        onMediaClick = listener
    }

    fun setOnMoreClickListener(listener: (List<Attachment>) -> Unit) {
        onMoreClick = listener
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
    // hidden so the grid isn't flush with the bubble edge.
    private fun applyRootPadding() {
        setPadding(insetPx, insetPx, insetPx, if (statusInfoVisible) 0 else insetPx)
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
        messageThumbnailUrl = MultiAttachmentUtils.thumbnailUrl(message)
        // Every attachment stays in the grid — a kind-mismatched one (server-sent mixed payload)
        // renders as a broken tile in createTile rather than being dropped.
        val items = MultiAttachmentUtils.resolveAttachments(message)
        populateGrid(items)

        val caption = message.caption
        if (caption.isNullOrEmpty()) {
            captionContainer.visibility = View.GONE
            captionContainer.removeAllViews()
        } else {
            captionContainer.visibility = View.VISIBLE
            // Captions travel as markdown — render the same block-level views as the text bubble.
            MultiAttachmentUtils.renderCaptionInto(
                captionContainer,
                caption,
                message,
                textFormatters,
                messageAlignment,
                captionTextColor = style.captionTextColor,
                captionTextAppearance = style.captionTextAppearance,
                // The style's text appearance owns the size when one is set.
                textSizeSp = if (style.captionTextAppearance != 0) 0f else MultiAttachmentUtils.CAPTION_TEXT_SIZE_SP
            )
        }
        MultiAttachmentUtils.bindEditedLabel(editedTextView, message, style.captionTextColor)
    }

    private fun populateGrid(attachments: List<Attachment>) {
        gridLayout.removeAllViews()
        lastAttachments = attachments
        if (attachments.isEmpty()) return

        val count = attachments.size
        val visibleCount = minOf(count, MAX_VISIBLE)
        val contentWidth = bubbleWidthPx - insetPx * 2
        val square = (contentWidth - spacingPx) / 2

        when (visibleCount) {
            1 -> {
                gridLayout.columnCount = 1; gridLayout.rowCount = 1
                addGridTile(0, contentWidth, dp(224), 0, 1, 0, 1, 0)
            }
            2 -> {
                gridLayout.columnCount = 2; gridLayout.rowCount = 1
                addGridTile(0, square, square, 0, 1, 0, 1, 0)
                addGridTile(1, square, square, 0, 1, 1, 1, 0)
            }
            3 -> layoutThree(attachments)
            else -> {
                gridLayout.columnCount = 2; gridLayout.rowCount = 2
                for (i in 0 until visibleCount) {
                    val more = if (i == MAX_VISIBLE - 1 && count > MAX_VISIBLE) count - MAX_VISIBLE else 0
                    addGridTile(i, square, square, i / 2, 1, i % 2, 1, more)
                }
            }
        }
    }

    /** Adds a tile spanning [rowSpan]×[colSpan] cells starting at ([row],[col]). */
    private fun addGridTile(
        index: Int, w: Int, h: Int, row: Int, rowSpan: Int, col: Int, colSpan: Int, overlayMore: Int
    ) {
        val tile = createTile(lastAttachments[index], index, overlayMore)
        tile.layoutParams = GridLayout.LayoutParams(
            GridLayout.spec(row, rowSpan), GridLayout.spec(col, colSpan)
        ).apply {
            width = w
            height = h
            setMargins(spacingPx / 2, spacingPx / 2, spacingPx / 2, spacingPx / 2)
        }
        gridLayout.addView(tile)
    }

    /**
     * Three-item grid whose first tile follows its orientation: a PORTRAIT first image becomes a
     * tall tile on the left with the other two stacked on the right; a LANDSCAPE (or still-loading)
     * first image becomes a wide tile on top with the other two as squares below. The first image's
     * orientation is detected async (Glide) and the grid is rebuilt only if it turns out portrait.
     */
    private fun layoutThree(attachments: List<Attachment>) {
        // A broken (kind-mismatched) first tile has no decodable media — skip detection and keep
        // the wide-top layout.
        val expectedKind = if (renderAsVideo) MultiAttachmentUtils.KIND_VIDEO else MultiAttachmentUtils.KIND_IMAGE
        val firstUrl = (messageThumbnailUrl ?: attachments[0].fileUrl)
            ?.takeIf { MultiAttachmentUtils.kindOf(attachments[0]) == expectedKind }
        val cached = firstUrl?.let { portraitCache.get(it) }
        renderThree(cached ?: false)
        if (cached == null && !firstUrl.isNullOrEmpty()) {
            detectPortrait(firstUrl) { portrait ->
                portraitCache.put(firstUrl, portrait)
                // Rebuild only if this bubble still shows the same message and it's portrait.
                if (lastAttachments === attachments && portrait) renderThree(true)
            }
        }
    }

    private fun renderThree(portrait: Boolean) {
        gridLayout.removeAllViews()
        gridLayout.columnCount = 2; gridLayout.rowCount = 2
        val contentWidth = bubbleWidthPx - insetPx * 2
        val square = (contentWidth - spacingPx) / 2
        if (portrait) {
            val fullH = square * 2 + spacingPx
            addGridTile(0, square, fullH, 0, 2, 0, 1, 0)  // tall first, left column, spans both rows
            addGridTile(1, square, square, 0, 1, 1, 1, 0) // top-right
            addGridTile(2, square, square, 1, 1, 1, 1, 0) // bottom-right
        } else {
            addGridTile(0, contentWidth, square, 0, 1, 0, 2, 0) // wide first on top, spans both cols
            addGridTile(1, square, square, 1, 1, 0, 1, 0)       // bottom-left
            addGridTile(2, square, square, 1, 1, 1, 1, 0)       // bottom-right
        }
    }

    private fun detectPortrait(url: String, onResult: (Boolean) -> Unit) {
        Glide.with(context).asBitmap().load(url).into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                onResult(resource.height > resource.width)
            }

            override fun onLoadCleared(placeholder: Drawable?) {}
        })
    }

    private fun createTile(attachment: Attachment, index: Int, overlayMore: Int): View {
        val card = MaterialCardView(context)
        Utils.initMaterialCard(card)
        card.radius = style.imageCornerRadius
        card.cardElevation = 0f
        card.strokeWidth = 0
        card.setCardBackgroundColor(0xFFE8E8E8.toInt())

        // Kind-mismatched attachment (server-sent mixed payload) — broken tile: placeholder + the
        // unknown-file "?" glyph, no play badge. Tapping it fires onMediaClick like any tile; the
        // renderer routes it to the viewer's "No preview available" page.
        val expectedKind = if (renderAsVideo) MultiAttachmentUtils.KIND_VIDEO else MultiAttachmentUtils.KIND_IMAGE
        val isBroken = MultiAttachmentUtils.kindOf(attachment) != expectedKind

        if (isBroken) {
            card.addView(ImageView(context).apply {
                layoutParams = FrameLayout.LayoutParams(dp(36), dp(44), Gravity.CENTER)
                scaleType = ImageView.ScaleType.FIT_CENTER
                setImageResource(R.drawable.cometchat_unsupported_file_icon)
            })
        } else {
            val imageView = ImageView(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
            card.addView(imageView)

            // Video tiles decorate with a play badge + duration in the subclass hook.
            decorateTile(card, imageView, attachment)

            // The Thumbnail Generation extension's url_medium is generated from the message's first
            // attachment — use it for tile 0 (expected thumbnail source), full fileUrl elsewhere.
            val model = if (index == 0) messageThumbnailUrl ?: attachment.fileUrl else attachment.fileUrl
            Glide.with(context).load(model)
                .placeholder(R.drawable.cometchat_image_placeholder)
                .into(imageView)
        }

        if (overlayMore > 0) {
            val overlay = TextView(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
                )
                gravity = Gravity.CENTER
                setBackgroundColor(0x99000000.toInt())
                setTextColor(0xFFFFFFFF.toInt())
                textSize = 22f
                text = "+$overlayMore"
            }
            card.addView(overlay)
        }

        card.setOnClickListener {
            if (overlayMore > 0) onMoreClick?.invoke(lastAttachments)
            else onMediaClick?.invoke(index, attachment)
        }
        card.setOnLongClickListener { v -> Utils.performAdapterClick(v); true }
        return card
    }

    private var lastAttachments: List<Attachment> = emptyList()

    /** Overridden by the video subclass to add a play badge + duration; no-op for images. */
    protected open fun decorateTile(card: MaterialCardView, imageView: ImageView, attachment: Attachment) {}

    protected fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    companion object {
        private const val MAX_VISIBLE = 4

        /** Max first-image orientation entries kept (cheap booleans, but bounded to avoid unbounded growth). */
        private const val PORTRAIT_CACHE_MAX = 256

        /**
         * First-image portrait/landscape by url, shared across recycled bubbles to avoid re-decoding.
         * LRU-bounded and internally synchronized so the async (Glide) detection callback and the
         * bind thread can share it safely.
         */
        private val portraitCache = LruCache<String, Boolean>(PORTRAIT_CACHE_MAX)
    }
}
