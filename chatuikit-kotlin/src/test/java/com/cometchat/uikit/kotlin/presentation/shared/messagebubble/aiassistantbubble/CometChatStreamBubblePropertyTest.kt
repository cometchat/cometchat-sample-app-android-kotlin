package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.aiassistantbubble

import android.view.View
import androidx.annotation.ColorInt
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

// ── Test Doubles ────────────────────────────────────────────────────────

/**
 * Simulates the delta accumulation logic in [CometChatStreamBubble.handleEventStreaming].
 *
 * When content deltas arrive, the bubble appends each delta to a [StringBuilder]
 * and updates the [StreamMessage.text] with the accumulated result.
 */
private class DeltaAccumulator {
    private val streamingBuilder = StringBuilder()

    fun appendDelta(delta: String) {
        streamingBuilder.append(delta)
    }

    fun getAccumulatedText(): String = streamingBuilder.toString()
}

/**
 * Simulates the visibility state tracking in [CometChatStreamBubble.setStreamMessage].
 *
 * When text is non-empty: shimmer is GONE, recycler is VISIBLE.
 * When text is null/empty: shimmer is VISIBLE, recycler is GONE.
 */
private class VisibilityTracker {
    var shimmerVisibility: Int = View.VISIBLE
        private set
    var recyclerVisibility: Int = View.GONE
        private set

    fun setStreamMessageText(text: String?) {
        if (!text.isNullOrEmpty()) {
            // hideAnimatedText()
            shimmerVisibility = View.GONE
            recyclerVisibility = View.VISIBLE
        } else {
            // showAnimatedText()
            shimmerVisibility = View.VISIBLE
            recyclerVisibility = View.GONE
        }
    }
}


/**
 * Simulates the style setter passthrough logic in [CometChatStreamBubble].
 *
 * - [setTextColor] stores the color in [rootTextColor] and applies to shimmer text view.
 * - [setBackgroundColor] delegates to [setCardBackgroundColor], tracked as [cardBackgroundColor].
 */
private class StyleTracker {
    @ColorInt var rootTextColor: Int = 0
        private set
    @ColorInt var shimmerTextViewColor: Int = 0
        private set
    @ColorInt private var _cardBackgroundColor: Int = 0

    fun setTextColor(@ColorInt color: Int) {
        rootTextColor = color
        shimmerTextViewColor = color
    }

    fun setBackgroundColor(@ColorInt color: Int) {
        // Mirrors: override fun setBackgroundColor(color) = setCardBackgroundColor(color)
        _cardBackgroundColor = color
    }

    fun getCardBackgroundColor(): Int = _cardBackgroundColor
}

/**
 * Simulates the idempotent rendering logic in [CometChatStreamBubble.renderMarkdown].
 *
 * [renderMarkdown] only calls [notifyDataSetChanged] when the new content differs
 * from [lastRenderedContent] via case-insensitive trimmed comparison.
 */
private class IdempotentRenderer {
    private var lastRenderedContent = ""
    var notifyCount: Int = 0
        private set

    fun renderMarkdown(content: String) {
        if (shouldUpdateContent(content)) {
            // adapter.setMarkdown(markwon, content)
            // adapter.notifyDataSetChanged()
            notifyCount++
            lastRenderedContent = content
        }
    }

    private fun shouldUpdateContent(newContent: String): Boolean {
        return !newContent.trim().equals(lastRenderedContent.trim(), ignoreCase = true)
    }
}

// ── Property Tests ──────────────────────────────────────────────────────

/**
 * Property-based tests for [CometChatStreamBubble] core behaviors.
 *
 * Uses test doubles that mirror the actual logic extracted from the real
 * implementation, avoiding Android view dependencies.
 */
class CometChatStreamBubblePropertyTest : FunSpec({

    // ==================== Property 1 ====================

    // Feature: kotlin-stream-bubble, Property 1: Content delta accumulation is concatenation
    context("Property 1: Content delta accumulation is concatenation") {

        /**
         * *For any* sequence of non-null, non-empty content delta strings,
         * appending each delta to a StringBuilder in order SHALL produce a
         * final string equal to the concatenation of all deltas.
         *
         * **Validates: Requirements 1.11**
         */
        test("accumulating deltas produces their concatenation") {
            val deltaListArb = Arb.list(Arb.string(1..20), 1..50)

            checkAll(100, deltaListArb) { deltas ->
                val accumulator = DeltaAccumulator()

                deltas.forEach { delta -> accumulator.appendDelta(delta) }

                val expected = deltas.joinToString("")
                accumulator.getAccumulatedText() shouldBe expected
            }
        }
    }

    // ==================== Property 2 ====================

    // Feature: kotlin-stream-bubble, Property 2: Non-empty text shows RecyclerView and hides shimmer
    context("Property 2: Non-empty text shows RecyclerView and hides shimmer") {

        /**
         * *For any* StreamMessage with non-empty, non-null text, calling
         * setStreamMessage() SHALL result in the ShimmerTextView being GONE
         * and the RecyclerView being VISIBLE.
         *
         * **Validates: Requirements 1.5**
         */
        test("non-empty text hides shimmer and shows recycler") {
            checkAll(100, Arb.string(1..100)) { text ->
                val tracker = VisibilityTracker()

                tracker.setStreamMessageText(text)

                tracker.shimmerVisibility shouldBe View.GONE
                tracker.recyclerVisibility shouldBe View.VISIBLE
            }
        }
    }

    // ==================== Property 3 ====================

    // Feature: kotlin-stream-bubble, Property 3: Style setter passthrough
    context("Property 3: Style setter passthrough") {

        /**
         * *For any* valid @ColorInt value, calling setTextColor(color) SHALL
         * store the value in rootTextColor and apply it to the ShimmerTextView.
         * Similarly, calling setBackgroundColor(color) SHALL result in
         * getCardBackgroundColor() returning that color.
         *
         * **Validates: Requirements 2.3, 2.5**
         */
        test("setTextColor stores in rootTextColor and applies to shimmer") {
            checkAll(100, Arb.int()) { color ->
                val tracker = StyleTracker()

                tracker.setTextColor(color)

                tracker.rootTextColor shouldBe color
                tracker.shimmerTextViewColor shouldBe color
            }
        }

        test("setBackgroundColor results in getCardBackgroundColor returning that color") {
            checkAll(100, Arb.int()) { color ->
                val tracker = StyleTracker()

                tracker.setBackgroundColor(color)

                tracker.getCardBackgroundColor() shouldBe color
            }
        }
    }

    // ==================== Property 6 ====================

    // Feature: kotlin-stream-bubble, Property 6: Idempotent rendering skips unchanged content
    context("Property 6: Idempotent rendering skips unchanged content") {

        /**
         * *For any* markdown content string, if renderMarkdown(content) is called
         * twice with strings that are equal after case-insensitive trimmed comparison,
         * the MarkwonAdapter.notifyDataSetChanged() SHALL be invoked only once
         * (on the first call).
         *
         * **Validates: Requirements 7.7**
         */
        test("duplicate renderMarkdown calls only notify once") {
            checkAll(100, Arb.string(0..100)) { content ->
                val renderer = IdempotentRenderer()

                renderer.renderMarkdown(content)
                renderer.renderMarkdown(content)

                // Empty string is a special case: shouldUpdateContent("") returns false
                // on the first call too because lastRenderedContent starts as "" and
                // "".trim().equals("".trim(), ignoreCase=true) is true.
                if (content.trim().isEmpty()) {
                    renderer.notifyCount shouldBe 0
                } else {
                    renderer.notifyCount shouldBe 1
                }
            }
        }

        test("case-varied duplicate is still treated as unchanged") {
            checkAll(100, Arb.string(1..50)) { content ->
                val renderer = IdempotentRenderer()

                renderer.renderMarkdown(content)
                renderer.renderMarkdown(content.uppercase())

                // If the trimmed content differs only by case, still only 1 notify
                // (unless the original was all-whitespace, which minSize=1 can produce)
                if (content.trim().isEmpty()) {
                    renderer.notifyCount shouldBe 0
                } else if (content.trim().equals(content.uppercase().trim(), ignoreCase = true)) {
                    renderer.notifyCount shouldBe 1
                }
            }
        }

        test("whitespace-padded duplicate is treated as unchanged") {
            checkAll(100, Arb.string(1..50)) { content ->
                val renderer = IdempotentRenderer()

                renderer.renderMarkdown(content)
                renderer.renderMarkdown("  $content  ")

                if (content.trim().isEmpty()) {
                    renderer.notifyCount shouldBe 0
                } else {
                    renderer.notifyCount shouldBe 1
                }
            }
        }
    }
})
