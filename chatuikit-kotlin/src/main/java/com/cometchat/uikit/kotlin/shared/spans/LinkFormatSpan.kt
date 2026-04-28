package com.cometchat.uikit.kotlin.shared.spans

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import androidx.annotation.ColorInt
import com.cometchat.uikit.core.formatter.RichTextFormat
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Span for links with URL metadata in WYSIWYG rich text editing.
 *
 * Extends [ClickableSpan] to provide clickable link functionality with underline
 * and link color styling. Implements [RichTextFormatSpan] to enable format detection
 * and markdown conversion.
 *
 * The URL is stored as metadata within the span, enabling proper markdown conversion
 * when the message is sent.
 *
 * @see RichTextFormatSpan
 * @see RichTextFormat.LINK
 */
class LinkFormatSpan : ClickableSpan, RichTextFormatSpan {

    var url: String
        private set

    @ColorInt
    private var linkColor: Int

    private var underlineEnabled: Boolean

    private var context: Context?

    /**
     * Creates a new LinkFormatSpan with the specified URL.
     */
    constructor(url: String) {
        this.url = url
        this.linkColor = 0
        this.underlineEnabled = true
        this.context = null
    }

    /**
     * Creates a new LinkFormatSpan with the specified URL and context for theme colors.
     */
    constructor(url: String, context: Context) {
        this.url = url
        this.context = context
        this.linkColor = CometChatTheme.getPrimaryColor(context)
        this.underlineEnabled = true
    }

    /**
     * Creates a new LinkFormatSpan with custom styling.
     */
    constructor(url: String, @ColorInt linkColor: Int, underlineEnabled: Boolean) {
        this.url = url
        this.linkColor = linkColor
        this.underlineEnabled = underlineEnabled
        this.context = null
    }

    override fun getFormatType(): RichTextFormat = RichTextFormat.LINK

    override fun onClick(widget: View) {
        if (url.isEmpty()) return

        try {
            var urlToOpen = url
            if (!urlToOpen.startsWith("http://") && !urlToOpen.startsWith("https://")) {
                urlToOpen = "https://$urlToOpen"
            }

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlToOpen))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            widget.context.startActivity(intent)
        } catch (_: Exception) {
            // Silently fail if URL cannot be opened
        }
    }

    override fun updateDrawState(ds: TextPaint) {
        val color = getEffectiveLinkColor()
        if (color != 0) {
            ds.color = color
        }
        ds.isUnderlineText = underlineEnabled
    }

    @ColorInt
    private fun getEffectiveLinkColor(): Int {
        if (linkColor != 0) return linkColor
        if (context != null) return CometChatTheme.getPrimaryColor(context!!)
        return DEFAULT_LINK_COLOR
    }

    // region Getters and Setters

    fun setUrl(value: String) { url = value }

    fun getLinkColor(): Int = linkColor
    fun setLinkColor(@ColorInt color: Int) { linkColor = color }

    fun isUnderlineEnabled(): Boolean = underlineEnabled
    fun setUnderlineEnabled(enabled: Boolean) { underlineEnabled = enabled }

    // endregion

    companion object {
        @ColorInt
        private const val DEFAULT_LINK_COLOR = 0xFF3D88F5.toInt()
    }
}
