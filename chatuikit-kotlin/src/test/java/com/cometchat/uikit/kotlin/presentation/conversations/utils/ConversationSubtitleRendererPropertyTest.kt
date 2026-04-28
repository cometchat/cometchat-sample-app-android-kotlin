package com.cometchat.uikit.kotlin.presentation.conversations.utils

import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.shouldNot
import io.kotest.matchers.string.contain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.Codepoint
import io.kotest.property.arbitrary.alphanumeric
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Property-based tests for [ConversationSubtitleRenderer.render].
 *
 * Feature: v6-rich-text-composer, Property 11: Conversation subtitle contains no raw markdown markers
 *
 * **Validates: Requirements 18B.6–18B.17**
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class ConversationSubtitleRendererPropertyTest {

    // ── Generators (grammar-based markdown) ─────────────────────────────

    /** Plain text words with no markdown syntax characters. */
    private val plainWordArb: Arb<String> = Arb.string(1..20, Codepoint.alphanumeric())

    /** Plain sentence (no markdown). */
    private val plainSentenceArb: Arb<String> = Arb.list(plainWordArb, 1..5)
        .map { it.joinToString(" ") }

    /** Bold markdown: **text** */
    private val boldArb: Arb<String> = plainWordArb.map { "**$it**" }

    /** Italic markdown: _text_ */
    private val italicArb: Arb<String> = plainWordArb.map { "_${it}_" }

    /** Strikethrough markdown: ~~text~~ */
    private val strikethroughArb: Arb<String> = plainWordArb.map { "~~$it~~" }

    /** Underline markdown: <u>text</u> */
    private val underlineArb: Arb<String> = plainWordArb.map { "<u>$it</u>" }

    /** Inline code markdown: `text` */
    private val inlineCodeArb: Arb<String> = plainWordArb.map { "`$it`" }

    /** Link markdown: [display](url) */
    private val linkArb: Arb<String> = arbitrary {
        val display = plainWordArb.bind()
        val url = plainWordArb.bind()
        "[$display](https://$url.com)"
    }

    /** A markdown fragment (one of the inline formats or plain text). */
    private val markdownFragmentArb: Arb<String> = Arb.choice(
        plainWordArb,
        boldArb,
        italicArb,
        strikethroughArb,
        underlineArb,
        inlineCodeArb,
        linkArb
    )

    /** A markdown line by joining fragments with spaces. */
    private val markdownLineArb: Arb<String> = Arb.list(markdownFragmentArb, 1..4)
        .map { it.joinToString(" ") }

    /** Fenced code block. */
    private val codeBlockArb: Arb<String> = arbitrary {
        val lang = Arb.element("", "kotlin", "java", "js").bind()
        val code = plainSentenceArb.bind()
        "```$lang\n$code\n```"
    }

    /** Bullet list item. */
    private val bulletItemArb: Arb<String> = markdownLineArb.map { "- $it" }

    /** Ordered list item. */
    private val orderedItemArb: Arb<String> = arbitrary {
        val num = Arb.int(1..9).bind()
        val text = markdownLineArb.bind()
        "$num. $text"
    }

    /** Blockquote line. */
    private val blockquoteArb: Arb<String> = markdownLineArb.map { "> $it" }

    /** Complete markdown document with mixed block types. */
    private val markdownDocArb: Arb<String> = Arb.list(
        Arb.choice(
            markdownLineArb,
            codeBlockArb,
            bulletItemArb,
            orderedItemArb,
            blockquoteArb
        ),
        1..5
    ).map { it.joinToString("\n") }

    // ── Marker detection helpers ────────────────────────────────────────

    private val boldMarker = Regex("\\*\\*")
    private val strikeMarker = Regex("~~")
    private val underlineOpenMarker = Regex("<u>")
    private val underlineCloseMarker = Regex("</u>")
    private val backtickMarker = Regex("`")
    private val codeBlockFence = Regex("```")
    private val linkSyntax = Regex("\\[([^\\]]+)\\]\\(([^)]+)\\)")

    // ── Property Test ───────────────────────────────────────────────────

    /**
     * Property 11: Conversation subtitle contains no raw markdown markers.
     *
     * For any valid markdown string, `ConversationSubtitleRenderer.render()`
     * SHALL produce a SpannableString whose `toString()` plain text contains
     * none of the markdown marker sequences.
     *
     * Note: bullet prefix "• " and number prefix "N. " ARE expected in the
     * output (they are the visual representation), so we do NOT check for those.
     * Similarly, blockquote prefix "▎ " is the visual representation.
     *
     * **Validates: Requirements 18B.6–18B.17**
     */
    @Test
    fun `Property 11 - rendered subtitle contains no raw markdown markers`() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()

        runBlocking {
            checkAll(100, markdownDocArb) { markdown ->
                val rendered = ConversationSubtitleRenderer.render(context, markdown)
                val plainText = rendered.toString()

                // Inline markers must be absent
                plainText shouldNot contain(boldMarker)
                plainText shouldNot contain(strikeMarker)
                plainText shouldNot contain(underlineOpenMarker)
                plainText shouldNot contain(underlineCloseMarker)
                plainText shouldNot contain(backtickMarker)
                plainText shouldNot contain(codeBlockFence)
                plainText shouldNot contain(linkSyntax)
            }
        }
    }
}
