package com.cometchat.uikit.core.utils

import com.cometchat.uikit.core.constants.UIKitConstants
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Property-based tests for MessageOptionsUtils card message options.
 *
 * Card messages (category = "card") have the same options as text messages
 * MINUS edit, copy, translate, and share (not meaningful for rich cards). The card
 * type is arbitrary (developer-chosen) so options must route on category alone.
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*CardMessageOptionsPropertyTest"
 */
class CardMessageOptionsPropertyTest : FunSpec({

    beforeTest {
        println("  🧪 ${it.name.testName}")
    }

    afterTest { println() }

    // ==================== Card Options Invariants ====================

    test("card category options should never contain EDIT") {
        val options = MessageOptionsUtils.getDefaultOptionIds(
            UIKitConstants.MessageCategory.CARD, "any_type"
        )
        println("    → card options: $options")
        options shouldNotContain UIKitConstants.MessageOption.EDIT
    }

    test("card category options should never contain COPY") {
        val options = MessageOptionsUtils.getDefaultOptionIds(
            UIKitConstants.MessageCategory.CARD, "any_type"
        )
        println("    → card options: $options")
        options shouldNotContain UIKitConstants.MessageOption.COPY
    }

    test("card category options should contain REPLY_IN_THREAD") {
        val options = MessageOptionsUtils.getDefaultOptionIds(
            UIKitConstants.MessageCategory.CARD, "developer_card"
        )
        println("    → card options: $options")
        options shouldContain UIKitConstants.MessageOption.REPLY_IN_THREAD
    }

    test("card category options should contain REPLY") {
        val options = MessageOptionsUtils.getDefaultOptionIds(
            UIKitConstants.MessageCategory.CARD, "any"
        )
        options shouldContain UIKitConstants.MessageOption.REPLY
    }

    test("card category options should contain DELETE") {
        val options = MessageOptionsUtils.getDefaultOptionIds(
            UIKitConstants.MessageCategory.CARD, "any"
        )
        options shouldContain UIKitConstants.MessageOption.DELETE
    }

    test("card category options should contain MESSAGE_INFORMATION") {
        val options = MessageOptionsUtils.getDefaultOptionIds(
            UIKitConstants.MessageCategory.CARD, "any"
        )
        options shouldContain UIKitConstants.MessageOption.MESSAGE_INFORMATION
    }

    test("card category options should never contain SHARE") {
        val options = MessageOptionsUtils.getDefaultOptionIds(
            UIKitConstants.MessageCategory.CARD, "any"
        )
        println("    → card options: $options")
        options shouldNotContain UIKitConstants.MessageOption.SHARE
    }

    test("card category options should never contain TRANSLATE") {
        val options = MessageOptionsUtils.getDefaultOptionIds(
            UIKitConstants.MessageCategory.CARD, "any"
        )
        println("    → card options: $options")
        options shouldNotContain UIKitConstants.MessageOption.TRANSLATE
    }

    test("card category options should contain MARK_AS_UNREAD") {
        val options = MessageOptionsUtils.getDefaultOptionIds(
            UIKitConstants.MessageCategory.CARD, "any"
        )
        options shouldContain UIKitConstants.MessageOption.MARK_AS_UNREAD
    }

    test("card category options should contain REPORT") {
        val options = MessageOptionsUtils.getDefaultOptionIds(
            UIKitConstants.MessageCategory.CARD, "any"
        )
        options shouldContain UIKitConstants.MessageOption.REPORT
    }

    test("card category options should contain MESSAGE_PRIVATELY") {
        val options = MessageOptionsUtils.getDefaultOptionIds(
            UIKitConstants.MessageCategory.CARD, "any"
        )
        options shouldContain UIKitConstants.MessageOption.MESSAGE_PRIVATELY
    }

    // ==================== PBT: Arbitrary developer type ====================

    test("for any arbitrary type string, card category always returns same options without EDIT/COPY") {
        checkAll(100, Arb.string(1..50)) { arbitraryType ->
            val options = MessageOptionsUtils.getDefaultOptionIds(
                UIKitConstants.MessageCategory.CARD, arbitraryType
            )
            println("    → type='$arbitraryType', optionCount=${options.size}")

            // Card options must never contain edit or copy regardless of type
            options shouldNotContain UIKitConstants.MessageOption.EDIT
            options shouldNotContain UIKitConstants.MessageOption.COPY

            // Must always contain the standard set
            options shouldContain UIKitConstants.MessageOption.DELETE
            options shouldContain UIKitConstants.MessageOption.REPLY
            options shouldContain UIKitConstants.MessageOption.REPLY_IN_THREAD
        }
    }

    test("card options equal text options minus EDIT, COPY, TRANSLATE, and SHARE") {
        val textOptions = MessageOptionsUtils.getDefaultOptionIds("message", "text")
        val cardOptions = MessageOptionsUtils.getDefaultOptionIds(
            UIKitConstants.MessageCategory.CARD, "any"
        )
        println("    → text options: $textOptions")
        println("    → card options: $cardOptions")

        // Card = Text - {EDIT, COPY, TRANSLATE, SHARE} (not meaningful for rich cards),
        // preserving the text option ordering.
        val excluded = setOf(
            UIKitConstants.MessageOption.EDIT,
            UIKitConstants.MessageOption.COPY,
            UIKitConstants.MessageOption.TRANSLATE,
            UIKitConstants.MessageOption.SHARE
        )
        val expectedCardOptions = textOptions.filterNot { it in excluded }
        cardOptions shouldBe expectedCardOptions
    }

    // ==================== Category routing ====================

    test("card routing is case-insensitive on category") {
        val lowerOptions = MessageOptionsUtils.getDefaultOptionIds("card", "buy_button")
        val mixedOptions = MessageOptionsUtils.getDefaultOptionIds("Card", "buy_button")
        println("    → lower: $lowerOptions")
        println("    → mixed: $mixedOptions")
        // Both should route to card options (the impl lowercases)
        lowerOptions shouldBe mixedOptions
    }
})
