package com.cometchat.uikit.kotlin.presentation.messageheader.style

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * Tests for CometChatMessageHeaderStyle data class.
 * Verifies defaults, copy, and equality behavior.
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*CometChatMessageHeaderStyleTest"
 */
class CometChatMessageHeaderStyleTest : FunSpec({

    beforeTest { println("  🧪 ${it.name.testName}") }
    afterTest { println() }

    // ==================== Default Values ====================

    test("default constructor should have zero/null values for all properties") {
        val style = CometChatMessageHeaderStyle()
        println("    → Default style created")

        style.backgroundColor shouldBe 0
        style.strokeColor shouldBe 0
        style.strokeWidth shouldBe 0
        style.cornerRadius shouldBe 0
        style.titleTextColor shouldBe 0
        style.titleTextAppearance shouldBe 0
        style.subtitleTextColor shouldBe 0
        style.subtitleTextAppearance shouldBe 0
        style.backIcon shouldBe null
        style.backIconTint shouldBe 0
        style.menuIcon shouldBe null
        style.menuIconTint shouldBe 0
        style.typingIndicatorTextColor shouldBe 0
        style.typingIndicatorTextAppearance shouldBe 0
        style.newChatIcon shouldBe null
        style.newChatIconTint shouldBe 0
        style.chatHistoryIcon shouldBe null
        style.chatHistoryIconTint shouldBe 0
        style.videoCallIcon shouldBe null
        style.videoCallIconTint shouldBe 0
        style.voiceCallIcon shouldBe null
        style.voiceCallIconTint shouldBe 0
        println("    → All default values verified as 0/null")
    }

    // ==================== Copy ====================

    test("copy should create a new instance with modified properties") {
        val original = CometChatMessageHeaderStyle()
        val modified = original.copy(
            backgroundColor = 0xFF000000.toInt(),
            titleTextColor = 0xFFFFFFFF.toInt(),
            subtitleTextColor = 0xFFAAAAAA.toInt()
        )
        println("    → Original: bg=${original.backgroundColor}, Modified: bg=${modified.backgroundColor}")

        modified.backgroundColor shouldBe 0xFF000000.toInt()
        modified.titleTextColor shouldBe 0xFFFFFFFF.toInt()
        modified.subtitleTextColor shouldBe 0xFFAAAAAA.toInt()
        // Unchanged properties should remain default
        modified.strokeColor shouldBe 0
        modified.strokeWidth shouldBe 0
        modified.cornerRadius shouldBe 0
    }

    test("copy should not modify the original instance") {
        val original = CometChatMessageHeaderStyle(backgroundColor = 0xFFFF0000.toInt())
        val modified = original.copy(backgroundColor = 0xFF00FF00.toInt())
        println("    → Original bg: ${original.backgroundColor}, Modified bg: ${modified.backgroundColor}")

        original.backgroundColor shouldBe 0xFFFF0000.toInt()
        modified.backgroundColor shouldBe 0xFF00FF00.toInt()
    }

    // ==================== Equality ====================

    test("two styles with same values should be equal") {
        val style1 = CometChatMessageHeaderStyle(
            backgroundColor = 0xFF000000.toInt(),
            titleTextColor = 0xFFFFFFFF.toInt()
        )
        val style2 = CometChatMessageHeaderStyle(
            backgroundColor = 0xFF000000.toInt(),
            titleTextColor = 0xFFFFFFFF.toInt()
        )
        println("    → style1==style2: ${style1 == style2}")
        style1 shouldBe style2
    }

    test("two styles with different values should not be equal") {
        val style1 = CometChatMessageHeaderStyle(backgroundColor = 0xFF000000.toInt())
        val style2 = CometChatMessageHeaderStyle(backgroundColor = 0xFFFFFFFF.toInt())
        println("    → style1==style2: ${style1 == style2}")
        style1 shouldNotBe style2
    }

    // ==================== Container Styling ====================

    test("container styling properties should be settable") {
        val style = CometChatMessageHeaderStyle(
            backgroundColor = 0xFF1A1A2E.toInt(),
            strokeColor = 0xFF333333.toInt(),
            strokeWidth = 2,
            cornerRadius = 16
        )
        println("    → Container: bg=${style.backgroundColor}, stroke=${style.strokeColor}, width=${style.strokeWidth}, radius=${style.cornerRadius}")

        style.backgroundColor shouldBe 0xFF1A1A2E.toInt()
        style.strokeColor shouldBe 0xFF333333.toInt()
        style.strokeWidth shouldBe 2
        style.cornerRadius shouldBe 16
    }

    // ==================== Text Styling ====================

    test("text styling properties should be settable") {
        val style = CometChatMessageHeaderStyle(
            titleTextColor = 0xFFFFFFFF.toInt(),
            titleTextAppearance = 123,
            subtitleTextColor = 0xFFAAAAAA.toInt(),
            subtitleTextAppearance = 456,
            typingIndicatorTextColor = 0xFF00FF00.toInt(),
            typingIndicatorTextAppearance = 789
        )
        println("    → Title: color=${style.titleTextColor}, appearance=${style.titleTextAppearance}")
        println("    → Subtitle: color=${style.subtitleTextColor}, appearance=${style.subtitleTextAppearance}")
        println("    → Typing: color=${style.typingIndicatorTextColor}, appearance=${style.typingIndicatorTextAppearance}")

        style.titleTextColor shouldBe 0xFFFFFFFF.toInt()
        style.titleTextAppearance shouldBe 123
        style.subtitleTextColor shouldBe 0xFFAAAAAA.toInt()
        style.subtitleTextAppearance shouldBe 456
        style.typingIndicatorTextColor shouldBe 0xFF00FF00.toInt()
        style.typingIndicatorTextAppearance shouldBe 789
    }

    // ==================== Icon Tint Styling ====================

    test("icon tint properties should be settable") {
        val style = CometChatMessageHeaderStyle(
            backIconTint = 0xFF111111.toInt(),
            menuIconTint = 0xFF222222.toInt(),
            newChatIconTint = 0xFF333333.toInt(),
            chatHistoryIconTint = 0xFF444444.toInt(),
            videoCallIconTint = 0xFF555555.toInt(),
            voiceCallIconTint = 0xFF666666.toInt()
        )
        println("    → Icon tints: back=${style.backIconTint}, menu=${style.menuIconTint}, newChat=${style.newChatIconTint}")

        style.backIconTint shouldBe 0xFF111111.toInt()
        style.menuIconTint shouldBe 0xFF222222.toInt()
        style.newChatIconTint shouldBe 0xFF333333.toInt()
        style.chatHistoryIconTint shouldBe 0xFF444444.toInt()
        style.videoCallIconTint shouldBe 0xFF555555.toInt()
        style.voiceCallIconTint shouldBe 0xFF666666.toInt()
    }

    // ==================== hashCode ====================

    test("equal styles should have same hashCode") {
        val style1 = CometChatMessageHeaderStyle(backgroundColor = 0xFF000000.toInt())
        val style2 = CometChatMessageHeaderStyle(backgroundColor = 0xFF000000.toInt())
        println("    → hashCode1=${style1.hashCode()}, hashCode2=${style2.hashCode()}")
        style1.hashCode() shouldBe style2.hashCode()
    }
})
