package com.cometchat.uikit.kotlin.presentation.conversations.utils

import android.content.Context
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Covers the formatter-span overlay [ConversationSubtitleRenderer.render] performs for media
 * captions: text formatters (mentions) run BEFORE markdown, so their spans are indexed against
 * text that still carries the markdown markers. Stripping those markers shifts the spanned text
 * left, and the overlay has to follow it.
 *
 * Run: ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*ConversationSubtitleRendererFormatterSpanTest"
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class ConversationSubtitleRendererFormatterSpanTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    /** Spans the substring [text] in [this], as a formatter would over a resolved mention. */
    private fun formatterOutput(full: String, mention: String): Spanned =
        SpannableStringBuilder(full).apply {
            val start = full.indexOf(mention)
            setSpan(
                ForegroundColorSpan(Color.RED),
                start, start + mention.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

    @Test
    fun `formatter span is remapped past stripped markdown markers`() {
        val formatted = formatterOutput("**hi** @Alice", "@Alice")

        val rendered = ConversationSubtitleRenderer.render(context, formatted.toString(), formatted)

        assertEquals("hi @Alice", rendered.toString())
        val span = rendered.getSpans(0, rendered.length, ForegroundColorSpan::class.java).single()
        assertEquals(
            "@Alice",
            rendered.substring(rendered.getSpanStart(span), rendered.getSpanEnd(span))
        )
    }

    @Test
    fun `formatter span lands on the right line of a multi-segment caption`() {
        val formatted = formatterOutput("intro\n- ping @Bob", "@Bob")

        val rendered = ConversationSubtitleRenderer.render(context, formatted.toString(), formatted)

        val span = rendered.getSpans(0, rendered.length, ForegroundColorSpan::class.java).single()
        assertEquals(
            "@Bob",
            rendered.substring(rendered.getSpanStart(span), rendered.getSpanEnd(span))
        )
    }

    @Test
    fun `caption without formatter output still renders markdown`() {
        val rendered = ConversationSubtitleRenderer.render(context, "_italic_")

        assertEquals("italic", rendered.toString())
    }
}
