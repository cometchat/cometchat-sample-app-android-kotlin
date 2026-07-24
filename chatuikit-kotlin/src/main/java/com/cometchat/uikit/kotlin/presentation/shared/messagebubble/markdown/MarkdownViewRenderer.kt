package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.markdown

import android.content.Context
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import android.util.TypedValue
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import com.cometchat.uikit.core.formatter.MarkdownRenderer
import com.cometchat.uikit.core.formatter.RichTextFormat
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.shared.spans.MentionMovementMethod
import com.cometchat.uikit.kotlin.shared.spans.TagSpan
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Renders markdown as a stack of block-level child views — the single implementation shared by the
 * text bubble ([com.cometchat.uikit.kotlin.presentation.shared.messagebubble.textbubble.CometChatTextBubble])
 * and by media-message captions
 * ([com.cometchat.uikit.kotlin.presentation.shared.messagebubble.multiattachment.MultiAttachmentUtils.renderCaptionInto]).
 *
 * Each [MarkdownRenderer.RenderedSegment] becomes its own view: a paragraph is a [TextView], a
 * fenced code block a bordered container with an optional language label, a list item an indented
 * prefix + content row, and consecutive blockquote lines a single group behind one continuous
 * vertical stripe.
 *
 * Text formatters must run BEFORE markdown parsing (a `<@uid:...>` mention token becomes a styled
 * display name first), and the markdown is then parsed from that output. The mention spans the
 * formatters produced are re-overlaid per segment, remapped through a position map because
 * stripping the markdown markers shifts every offset left.
 */
object MarkdownViewRenderer {

    /**
     * Visual configuration for a render pass.
     *
     * @param textColor Body text color; `0` falls back to the theme's primary text color and leaves
     *   the [TextView]'s own color untouched (the text appearance decides).
     * @param linkColor Color for links and the blockquote bar; `0` falls back to the theme's info color.
     * @param textAppearance Text appearance applied to every text view; `0` to skip.
     * @param textSizeSp Explicit text size applied after [textAppearance]; `0` to skip.
     * @param isOutgoing True for a bubble aligned RIGHT (sent by the logged-in user) — code blocks,
     *   blockquotes and inline code use the sender color set instead of the receiver one.
     */
    data class Style(
        @ColorInt val textColor: Int = 0,
        @ColorInt val linkColor: Int = 0,
        @StyleRes val textAppearance: Int = 0,
        val textSizeSp: Float = 0f,
        val isOutgoing: Boolean = false
    )

    /**
     * Clears [container] and fills it with one child view per markdown block of [markdown].
     *
     * @param container Vertical [LinearLayout] that receives the segment views.
     * @param markdown Markdown source — the formatter output when formatters ran, so that
     *   [formatterSpans] offsets line up with it.
     * @param formatterSpans Formatter output carrying the mention spans, or `null` when no
     *   formatters ran.
     */
    @JvmStatic
    @JvmOverloads
    fun render(
        container: LinearLayout,
        markdown: String,
        formatterSpans: Spanned? = null,
        style: Style = Style()
    ) {
        container.removeAllViews()
        for (view in buildViews(container.context, markdown, formatterSpans, style)) {
            container.addView(view)
        }
    }

    /**
     * Builds the block-level views for [markdown] without attaching them to a parent. Callers that
     * own their container (or need to interleave other views) can add these themselves; most should
     * use [render].
     */
    @JvmStatic
    @JvmOverloads
    fun buildViews(
        context: Context,
        markdown: String,
        formatterSpans: Spanned? = null,
        style: Style = Style()
    ): List<View> {
        val mentionSpans = formatterSpans?.let { extractMentionSpans(it) } ?: emptyList()
        val segments = MarkdownRenderer.parse(markdown)

        val textColor = if (style.textColor != 0) style.textColor else CometChatTheme.getTextColorPrimary(context)
        val linkColor = if (style.linkColor != 0) style.linkColor else CometChatTheme.getInfoColor(context)

        val views = mutableListOf<View>()
        // Position in the markdown source, used to map mention spans onto the segment they cover.
        var originalOffset = 0

        var i = 0
        while (i < segments.size) {
            when (val segment = segments[i]) {
                is MarkdownRenderer.RenderedSegment.Text -> {
                    val segmentMentions = findMentionSpansForSegment(
                        segment.text, markdown, originalOffset, mentionSpans
                    )
                    views.add(createTextView(context, style).apply {
                        text = buildStyledText(
                            context, segment.text, textColor, linkColor, style, segmentMentions
                        )
                    })
                    originalOffset += segment.text.length + 1
                }

                is MarkdownRenderer.RenderedSegment.CodeBlock ->
                    views.add(createCodeBlockView(context, segment.code, segment.language, style.isOutgoing))

                is MarkdownRenderer.RenderedSegment.BulletItem ->
                    views.add(createPrefixedItemView(context, BULLET_PREFIX, segment.text, textColor, linkColor, style))

                is MarkdownRenderer.RenderedSegment.OrderedItem ->
                    views.add(
                        createPrefixedItemView(
                            context, "${segment.number}. ", segment.text, textColor, linkColor, style
                        )
                    )

                is MarkdownRenderer.RenderedSegment.Blockquote -> {
                    // Consecutive blockquote lines render behind one continuous stripe.
                    val group = mutableListOf(segment)
                    while (i + 1 < segments.size && segments[i + 1] is MarkdownRenderer.RenderedSegment.Blockquote) {
                        i++
                        group.add(segments[i] as MarkdownRenderer.RenderedSegment.Blockquote)
                    }
                    views.add(createGroupedBlockquoteView(context, group, textColor, style))
                }
            }
            i++
        }
        return views
    }

    // ── Segment views ───────────────────────────────────────────────────

    /** Plain paragraph view. [MentionMovementMethod] makes the mention (TagSpan) clicks work. */
    private fun createTextView(context: Context, style: Style): TextView = TextView(context).apply {
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        if (style.textAppearance != 0) setTextAppearance(style.textAppearance)
        if (style.textColor != 0) setTextColor(style.textColor)
        if (style.linkColor != 0) setLinkTextColor(style.linkColor)
        if (style.textSizeSp > 0f) setTextSize(TypedValue.COMPLEX_UNIT_SP, style.textSizeSp)
        movementMethod = MentionMovementMethod.getInstance()
    }

    /**
     * Fenced code block: rounded, bordered container with an optional language label above the
     * monospace code. Sender uses the extended primary palette, receiver background2 + default stroke.
     */
    private fun createCodeBlockView(
        context: Context,
        code: String,
        language: String,
        isOutgoing: Boolean
    ): View {
        val bgColor = if (isOutgoing) {
            CometChatTheme.getExtendedPrimaryColor700(context)
        } else {
            CometChatTheme.getBackgroundColor2(context).takeIf { it != 0 } ?: FALLBACK_CODE_BG
        }
        val borderColor = if (isOutgoing) {
            CometChatTheme.getExtendedPrimaryColor600(context)
        } else {
            CometChatTheme.getStrokeColorDefault(context).takeIf { it != 0 } ?: FALLBACK_CODE_BORDER
        }
        val codeTextColor = if (isOutgoing) {
            CometChatTheme.getColorWhite(context)
        } else {
            CometChatTheme.getTextColorPrimary(context)
        }

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(context, 4)
                bottomMargin = dpToPx(context, 4)
            }
            background = GradientDrawable().apply {
                setColor(bgColor)
                cornerRadius = dpToPx(context, 8).toFloat()
                setStroke(1, borderColor)
            }
        }

        if (language.isNotEmpty()) {
            container.addView(TextView(context).apply {
                text = language
                setTextColor(adjustAlpha(codeTextColor, 0.5f))
                typeface = Typeface.MONOSPACE
                setTextSize(TypedValue.COMPLEX_UNIT_SP, CODE_LANGUAGE_TEXT_SIZE_SP)
                setPadding(dpToPx(context, 12), dpToPx(context, 8), dpToPx(context, 12), 0)
            })
        }

        // Code wraps rather than scrolling horizontally.
        container.addView(TextView(context).apply {
            text = code
            setTextColor(codeTextColor)
            typeface = Typeface.MONOSPACE
            setTextSize(TypedValue.COMPLEX_UNIT_SP, CODE_TEXT_SIZE_SP)
            setPadding(dpToPx(context, 12), dpToPx(context, 8), dpToPx(context, 12), dpToPx(context, 8))
        })

        return container
    }

    /** Bullet ("•  ") or ordered ("1. ") list row: bold prefix + styled content. */
    private fun createPrefixedItemView(
        context: Context,
        prefix: String,
        itemText: String,
        @ColorInt textColor: Int,
        @ColorInt linkColor: Int,
        style: Style
    ): View {
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        container.addView(TextView(context).apply {
            text = prefix
            setTextColor(textColor)
            setTypeface(typeface, Typeface.BOLD)
            if (style.textAppearance != 0) setTextAppearance(style.textAppearance)
            if (style.textSizeSp > 0f) setTextSize(TypedValue.COMPLEX_UNIT_SP, style.textSizeSp)
        })

        container.addView(createTextView(context, style).apply {
            text = buildStyledText(context, itemText, textColor, linkColor, style)
        })

        return container
    }

    /**
     * Consecutive blockquote lines behind one continuous vertical stripe on a rounded, tinted
     * background. Sender: white stripe on a 20% white wash; receiver: highlight stripe on background3.
     */
    private fun createGroupedBlockquoteView(
        context: Context,
        segments: List<MarkdownRenderer.RenderedSegment.Blockquote>,
        @ColorInt textColor: Int,
        style: Style
    ): View {
        val isOutgoing = style.isOutgoing
        val stripeColor = if (isOutgoing) {
            CometChatTheme.getColorWhite(context)
        } else {
            CometChatTheme.getStrokeColorHighlight(context).takeIf { it != 0 }
                ?: CometChatTheme.getStrokeColorDark(context)
        }
        val bgColor = if (isOutgoing) WHITE_20_PERCENT else CometChatTheme.getBackgroundColor3(context)
        val cornerRadius = context.resources.getDimension(R.dimen.cometchat_radius_2)

        val outerContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(context, 4)
                bottomMargin = dpToPx(context, 4)
            }
            background = GradientDrawable().apply {
                setColor(if (bgColor != 0) bgColor else Color.TRANSPARENT)
                setCornerRadius(cornerRadius)
            }
            // Clip children to the rounded background so the stripe's left corners are cut with it.
            clipToOutline = true
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setRoundRect(0, 0, view.width, view.height, cornerRadius)
                }
            }
            setPadding(0, 0, dpToPx(context, 12), 0)
        }

        val stripeView = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                dpToPx(context, 4),
                LinearLayout.LayoutParams.MATCH_PARENT
            ).apply {
                marginStart = 0
                marginEnd = dpToPx(context, 10)
            }
            background = GradientDrawable().apply { setColor(stripeColor) }
        }

        val contentBuilder = SpannableStringBuilder()
        for ((index, segment) in segments.withIndex()) {
            if (index > 0) contentBuilder.append("\n")
            val (plainText, inlineSpans) = MarkdownRenderer.parseInline(segment.text)
            val segStart = contentBuilder.length
            contentBuilder.append(plainText)
            for (span in inlineSpans) {
                val spanStart = segStart + span.start
                val spanEnd = segStart + span.end
                if (spanStart < 0 || spanEnd > contentBuilder.length || spanStart >= spanEnd) continue
                val androidSpan = when (span.format) {
                    RichTextFormat.BOLD -> StyleSpan(Typeface.BOLD)
                    RichTextFormat.ITALIC -> StyleSpan(Typeface.ITALIC)
                    RichTextFormat.STRIKETHROUGH -> StrikethroughSpan()
                    RichTextFormat.UNDERLINE -> UnderlineSpan()
                    RichTextFormat.INLINE_CODE -> TypefaceSpan("monospace")
                    else -> null
                }
                if (androidSpan != null) {
                    contentBuilder.setSpan(androidSpan, spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }

        val contentTextView = createTextView(context, style).apply {
            text = contentBuilder
            setTextColor(textColor)
            setPadding(0, dpToPx(context, 8), 0, dpToPx(context, 8))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        outerContainer.addView(stripeView)
        outerContainer.addView(contentTextView)
        return outerContainer
    }

    // ── Inline styling ──────────────────────────────────────────────────

    /**
     * Strips the inline markdown markers of [text] and returns it with the corresponding Android
     * spans applied, with [mentionSpans] re-overlaid on top at their remapped offsets.
     */
    private fun buildStyledText(
        context: Context,
        text: String,
        @ColorInt defaultColor: Int,
        @ColorInt linkColor: Int,
        style: Style,
        mentionSpans: List<MentionSpanInfo> = emptyList()
    ): SpannableString {
        val (plainText, inlineSpans) = MarkdownRenderer.parseInline(text)
        val spannable = SpannableString(plainText)

        spannable.setSpan(
            ForegroundColorSpan(defaultColor), 0, plainText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Mentions are applied before the inline formats so a bold/italic run inside one still wins.
        if (mentionSpans.isNotEmpty()) {
            val positionMap = buildPositionMap(text, plainText)
            for (mention in mentionSpans) {
                val mappedStart = mapPosition(mention.start, positionMap, plainText.length)
                val mappedEnd = mapPosition(mention.end, positionMap, plainText.length)
                if (mappedStart >= 0 && mappedEnd > mappedStart && mappedEnd <= plainText.length) {
                    spannable.setSpan(mention.span, mappedStart, mappedEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }

        for (span in inlineSpans) {
            val start = span.start.coerceAtMost(plainText.length)
            val end = span.end.coerceAtMost(plainText.length)
            if (start >= end) continue

            when (span.format) {
                RichTextFormat.BOLD ->
                    spannable.setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                RichTextFormat.ITALIC ->
                    spannable.setSpan(StyleSpan(Typeface.ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                RichTextFormat.UNDERLINE ->
                    spannable.setSpan(UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                RichTextFormat.STRIKETHROUGH ->
                    spannable.setSpan(StrikethroughSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                RichTextFormat.INLINE_CODE -> {
                    spannable.setSpan(TypefaceSpan("monospace"), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    val inlineCodeBg = if (style.isOutgoing) {
                        WHITE_20_PERCENT
                    } else {
                        CometChatTheme.getBackgroundColor3(context)
                    }
                    spannable.setSpan(
                        BackgroundColorSpan(inlineCodeBg), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                RichTextFormat.LINK -> {
                    span.url?.let { url ->
                        spannable.setSpan(URLSpan(url), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                    spannable.setSpan(
                        ForegroundColorSpan(linkColor), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    spannable.setSpan(UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

                else -> { /* Block-level formats are handled per segment. */ }
            }
        }

        return spannable
    }

    // ── Mention span overlay ────────────────────────────────────────────

    /** A formatter-produced mention span with its offsets in the formatter output. */
    private data class MentionSpanInfo(val start: Int, val end: Int, val span: Any)

    /** [TagSpan] is the clickable mention span the message-bubble formatters emit. */
    private fun extractMentionSpans(spanned: Spanned): List<MentionSpanInfo> {
        return spanned.getSpans(0, spanned.length, TagSpan::class.java).mapNotNull { span ->
            val start = spanned.getSpanStart(span)
            val end = spanned.getSpanEnd(span)
            if (start >= 0 && end > start) MentionSpanInfo(start, end, span) else null
        }
    }

    /**
     * Narrows [mentionSpans] to the ones covering this segment and re-bases their offsets on the
     * segment's own (still marker-carrying) text.
     */
    private fun findMentionSpansForSegment(
        segmentText: String,
        originalText: String,
        originalOffset: Int,
        mentionSpans: List<MentionSpanInfo>
    ): List<MentionSpanInfo> {
        if (mentionSpans.isEmpty() || segmentText.isEmpty()) return emptyList()

        // Locate the segment in the source; a multi-line segment is found by its first line.
        val firstLine = segmentText.split("\n").firstOrNull().orEmpty()
        var segmentStart = if (firstLine.isNotEmpty()) {
            originalText.indexOf(firstLine, originalOffset)
        } else {
            -1
        }
        if (segmentStart < 0) segmentStart = originalOffset
        val segmentEnd = segmentStart + segmentText.length

        return mentionSpans.mapNotNull { mention ->
            if (mention.end <= segmentStart || mention.start >= segmentEnd) return@mapNotNull null
            val relativeStart = (mention.start - segmentStart).coerceAtLeast(0)
            val relativeEnd = (mention.end - segmentStart).coerceAtMost(segmentText.length)
            if (relativeEnd > relativeStart) MentionSpanInfo(relativeStart, relativeEnd, mention.span) else null
        }
    }

    /**
     * Maps each position in [original] to its position in [plain] (same text minus the markdown
     * markers), by walking both in step and only advancing the plain cursor on a match.
     */
    private fun buildPositionMap(original: String, plain: String): IntArray {
        val map = IntArray(original.length + 1) { -1 }
        var plainIdx = 0
        var origIdx = 0
        while (origIdx < original.length && plainIdx < plain.length) {
            if (original[origIdx] == plain[plainIdx]) {
                map[origIdx] = plainIdx
                plainIdx++
            }
            origIdx++
        }
        map[original.length] = plain.length
        return map
    }

    /** Maps one position through [buildPositionMap], falling back to the nearest mapped one. */
    private fun mapPosition(pos: Int, map: IntArray, plainLength: Int): Int {
        if (pos < 0) return -1
        if (pos >= map.size) return plainLength
        if (map[pos] >= 0) return map[pos]
        for (i in pos downTo 0) {
            if (map[i] >= 0) return map[i]
        }
        return 0
    }

    private fun adjustAlpha(@ColorInt color: Int, factor: Float): Int {
        val alpha = (Color.alpha(color) * factor).toInt()
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
    }

    private fun dpToPx(context: Context, dp: Int): Int =
        (dp * context.resources.displayMetrics.density).toInt()

    private const val BULLET_PREFIX = "•  "
    private const val CODE_TEXT_SIZE_SP = 13f
    private const val CODE_LANGUAGE_TEXT_SIZE_SP = 11f
    private const val WHITE_20_PERCENT = 0x33FFFFFF
    private const val FALLBACK_CODE_BG = 0xFFF5F5F5.toInt()
    private const val FALLBACK_CODE_BORDER = 0xFFDDDDDD.toInt()
}
