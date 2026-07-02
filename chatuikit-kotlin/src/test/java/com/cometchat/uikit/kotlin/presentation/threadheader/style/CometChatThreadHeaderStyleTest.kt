package com.cometchat.uikit.kotlin.presentation.threadheader.style

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * Unit tests for CometChatThreadHeaderStyle data class (chatuikit-kotlin).
 * Verifies default values, copy semantics, and property accessibility.
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*CometChatThreadHeaderStyleTest"
 */
class CometChatThreadHeaderStyleTest : FunSpec({

    // ==================== Default Values ====================

    test("default constructor creates style with all zero/null defaults") {
        val style = CometChatThreadHeaderStyle()
        println("    → Default style: backgroundColor=${style.backgroundColor}")

        style.backgroundColor shouldBe 0
        style.strokeColor shouldBe 0
        style.strokeWidth shouldBe 0
        style.cornerRadius shouldBe 0
        style.backgroundDrawable shouldBe null
        style.replyCountBackgroundColor shouldBe 0
        style.replyCountTextColor shouldBe 0
        style.replyCountTextAppearance shouldBe 0
        style.incomingMessageBubbleStyle shouldBe null
        style.outgoingMessageBubbleStyle shouldBe null
    }

    // ==================== Copy with Changes ====================

    test("copy with backgroundColor changes only backgroundColor") {
        val original = CometChatThreadHeaderStyle()
        val copied = original.copy(backgroundColor = 0xFF0000FF.toInt())
        println("    → copy(backgroundColor=blue)")

        copied.backgroundColor shouldBe 0xFF0000FF.toInt()
        copied.strokeColor shouldBe original.strokeColor
        copied.strokeWidth shouldBe original.strokeWidth
        copied.cornerRadius shouldBe original.cornerRadius
        copied.replyCountBackgroundColor shouldBe original.replyCountBackgroundColor
        copied.replyCountTextColor shouldBe original.replyCountTextColor
        copied.replyCountTextAppearance shouldBe original.replyCountTextAppearance
    }

    test("copy with strokeColor changes only strokeColor") {
        val original = CometChatThreadHeaderStyle(backgroundColor = 0xFFFFFFFF.toInt())
        val copied = original.copy(strokeColor = 0xFF00FF00.toInt())
        println("    → copy(strokeColor=green)")

        copied.strokeColor shouldBe 0xFF00FF00.toInt()
        copied.backgroundColor shouldBe 0xFFFFFFFF.toInt()
    }

    test("copy with strokeWidth changes only strokeWidth") {
        val original = CometChatThreadHeaderStyle()
        val copied = original.copy(strokeWidth = 4)
        println("    → copy(strokeWidth=4)")

        copied.strokeWidth shouldBe 4
        copied.strokeColor shouldBe 0
    }

    test("copy with cornerRadius changes only cornerRadius") {
        val original = CometChatThreadHeaderStyle()
        val copied = original.copy(cornerRadius = 16)
        println("    → copy(cornerRadius=16)")

        copied.cornerRadius shouldBe 16
        copied.strokeWidth shouldBe 0
    }

    test("copy with replyCountBackgroundColor changes only replyCountBackgroundColor") {
        val original = CometChatThreadHeaderStyle()
        val copied = original.copy(replyCountBackgroundColor = 0xFFEEEEEE.toInt())
        println("    → copy(replyCountBackgroundColor)")

        copied.replyCountBackgroundColor shouldBe 0xFFEEEEEE.toInt()
        copied.backgroundColor shouldBe 0
    }

    test("copy with replyCountTextColor changes only replyCountTextColor") {
        val original = CometChatThreadHeaderStyle()
        val copied = original.copy(replyCountTextColor = 0xFF333333.toInt())
        println("    → copy(replyCountTextColor)")

        copied.replyCountTextColor shouldBe 0xFF333333.toInt()
        copied.replyCountBackgroundColor shouldBe 0
    }

    test("copy with replyCountTextAppearance changes only replyCountTextAppearance") {
        val original = CometChatThreadHeaderStyle()
        val copied = original.copy(replyCountTextAppearance = 12345)
        println("    → copy(replyCountTextAppearance=12345)")

        copied.replyCountTextAppearance shouldBe 12345
        copied.replyCountTextColor shouldBe 0
    }

    // ==================== All Properties Accessible ====================

    test("all properties are accessible on a fully configured style") {
        val style = CometChatThreadHeaderStyle(
            backgroundColor = 0xFFFFFFFF.toInt(),
            strokeColor = 0xFF000000.toInt(),
            strokeWidth = 2,
            cornerRadius = 8,
            backgroundDrawable = null,
            replyCountBackgroundColor = 0xFFEEEEEE.toInt(),
            replyCountTextColor = 0xFF333333.toInt(),
            replyCountTextAppearance = 99,
            incomingMessageBubbleStyle = null,
            outgoingMessageBubbleStyle = null
        )
        println("    → Fully configured style")

        style.backgroundColor shouldBe 0xFFFFFFFF.toInt()
        style.strokeColor shouldBe 0xFF000000.toInt()
        style.strokeWidth shouldBe 2
        style.cornerRadius shouldBe 8
        style.backgroundDrawable shouldBe null
        style.replyCountBackgroundColor shouldBe 0xFFEEEEEE.toInt()
        style.replyCountTextColor shouldBe 0xFF333333.toInt()
        style.replyCountTextAppearance shouldBe 99
        style.incomingMessageBubbleStyle shouldBe null
        style.outgoingMessageBubbleStyle shouldBe null
    }

    // ==================== Equality ====================

    test("two styles with same values are equal") {
        val style1 = CometChatThreadHeaderStyle(
            backgroundColor = 0xFFFF0000.toInt(),
            strokeWidth = 2
        )
        val style2 = CometChatThreadHeaderStyle(
            backgroundColor = 0xFFFF0000.toInt(),
            strokeWidth = 2
        )
        println("    → style1 == style2")

        style1 shouldBe style2
    }

    test("two styles with different values are not equal") {
        val style1 = CometChatThreadHeaderStyle(backgroundColor = 0xFFFF0000.toInt())
        val style2 = CometChatThreadHeaderStyle(backgroundColor = 0xFF00FF00.toInt())
        println("    → style1 != style2")

        style1 shouldNotBe style2
    }

    // ==================== hashCode ====================

    test("equal styles have same hashCode") {
        val style1 = CometChatThreadHeaderStyle(cornerRadius = 12)
        val style2 = CometChatThreadHeaderStyle(cornerRadius = 12)
        println("    → hashCode equality")

        style1.hashCode() shouldBe style2.hashCode()
    }
})
