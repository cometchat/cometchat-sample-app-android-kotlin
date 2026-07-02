package com.cometchat.uikit.kotlin.presentation.stickerkeyboard.style

import android.graphics.Color
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * Tests for CometChatStickerKeyboardStyle data class.
 *
 * Verifies:
 * - Default values (all zeros for data class defaults)
 * - Copy with changes produces new instance
 * - All properties are accessible
 * - Equality and hashCode behavior
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*.CometChatStickerKeyboardStyleTest"
 */
class CometChatStickerKeyboardStyleTest : FunSpec({

    context("Default values") {

        test("Default constructor creates style with zero/default values") {
            val style = CometChatStickerKeyboardStyle()

            style.backgroundColor shouldBe 0
            style.separatorColor shouldBe 0
            style.tabIconSize shouldBe 0
            style.tabActiveIndicatorColor shouldBe 0
            style.stickerItemSize shouldBe 0
            style.emptyStateTitleTextColor shouldBe 0
            style.emptyStateTitleTextAppearance shouldBe 0
            style.emptyStateSubtitleTextColor shouldBe 0
            style.emptyStateSubtitleTextAppearance shouldBe 0
            style.errorStateTextColor shouldBe 0
            style.errorStateTextAppearance shouldBe 0
            println("    ✅ Default values are all zero")
        }
    }

    context("Copy with changes") {

        test("Copy with backgroundColor change produces new instance") {
            val original = CometChatStickerKeyboardStyle()
            val modified = original.copy(backgroundColor = Color.RED)

            modified.backgroundColor shouldBe Color.RED
            modified.separatorColor shouldBe 0
            original.backgroundColor shouldBe 0
            println("    ✅ Copy with backgroundColor change works")
        }

        test("Copy with separatorColor change") {
            val original = CometChatStickerKeyboardStyle(backgroundColor = Color.WHITE)
            val modified = original.copy(separatorColor = Color.GRAY)

            modified.backgroundColor shouldBe Color.WHITE
            modified.separatorColor shouldBe Color.GRAY
            println("    ✅ Copy with separatorColor preserves other values")
        }

        test("Copy with tabActiveIndicatorColor change") {
            val original = CometChatStickerKeyboardStyle()
            val modified = original.copy(tabActiveIndicatorColor = Color.BLUE)

            modified.tabActiveIndicatorColor shouldBe Color.BLUE
            println("    ✅ Copy with tabActiveIndicatorColor works")
        }

        test("Copy with empty state text colors") {
            val original = CometChatStickerKeyboardStyle()
            val modified = original.copy(
                emptyStateTitleTextColor = Color.BLACK,
                emptyStateSubtitleTextColor = Color.DKGRAY
            )

            modified.emptyStateTitleTextColor shouldBe Color.BLACK
            modified.emptyStateSubtitleTextColor shouldBe Color.DKGRAY
            println("    ✅ Copy with empty state text colors works")
        }

        test("Copy with error state text color") {
            val original = CometChatStickerKeyboardStyle()
            val modified = original.copy(errorStateTextColor = Color.RED)

            modified.errorStateTextColor shouldBe Color.RED
            println("    ✅ Copy with errorStateTextColor works")
        }

        test("Copy with dimension values") {
            val original = CometChatStickerKeyboardStyle()
            val modified = original.copy(
                tabIconSize = 108,
                stickerItemSize = 240
            )

            modified.tabIconSize shouldBe 108
            modified.stickerItemSize shouldBe 240
            println("    ✅ Copy with dimension values works")
        }

        test("Copy with text appearance resources") {
            val original = CometChatStickerKeyboardStyle()
            val modified = original.copy(
                emptyStateTitleTextAppearance = 12345,
                emptyStateSubtitleTextAppearance = 67890,
                errorStateTextAppearance = 11111
            )

            modified.emptyStateTitleTextAppearance shouldBe 12345
            modified.emptyStateSubtitleTextAppearance shouldBe 67890
            modified.errorStateTextAppearance shouldBe 11111
            println("    ✅ Copy with text appearance resources works")
        }
    }

    context("All properties accessible") {

        test("All properties can be set via constructor") {
            val style = CometChatStickerKeyboardStyle(
                backgroundColor = Color.WHITE,
                separatorColor = Color.LTGRAY,
                tabIconSize = 108,
                tabActiveIndicatorColor = Color.BLUE,
                stickerItemSize = 240,
                emptyStateTitleTextColor = Color.BLACK,
                emptyStateTitleTextAppearance = 100,
                emptyStateSubtitleTextColor = Color.GRAY,
                emptyStateSubtitleTextAppearance = 200,
                errorStateTextColor = Color.RED,
                errorStateTextAppearance = 300
            )

            style.backgroundColor shouldBe Color.WHITE
            style.separatorColor shouldBe Color.LTGRAY
            style.tabIconSize shouldBe 108
            style.tabActiveIndicatorColor shouldBe Color.BLUE
            style.stickerItemSize shouldBe 240
            style.emptyStateTitleTextColor shouldBe Color.BLACK
            style.emptyStateTitleTextAppearance shouldBe 100
            style.emptyStateSubtitleTextColor shouldBe Color.GRAY
            style.emptyStateSubtitleTextAppearance shouldBe 200
            style.errorStateTextColor shouldBe Color.RED
            style.errorStateTextAppearance shouldBe 300
            println("    ✅ All 11 properties accessible via constructor")
        }
    }

    context("Equality and hashCode") {

        test("Two styles with same values are equal") {
            val style1 = CometChatStickerKeyboardStyle(
                backgroundColor = Color.WHITE,
                separatorColor = Color.GRAY
            )
            val style2 = CometChatStickerKeyboardStyle(
                backgroundColor = Color.WHITE,
                separatorColor = Color.GRAY
            )

            style1 shouldBe style2
            style1.hashCode() shouldBe style2.hashCode()
            println("    ✅ Equal styles have same hashCode")
        }

        test("Two styles with different values are not equal") {
            val style1 = CometChatStickerKeyboardStyle(backgroundColor = Color.WHITE)
            val style2 = CometChatStickerKeyboardStyle(backgroundColor = Color.BLACK)

            style1 shouldNotBe style2
            println("    ✅ Different styles are not equal")
        }
    }

    context("toString") {

        test("toString contains class name and properties") {
            val style = CometChatStickerKeyboardStyle(backgroundColor = Color.RED)
            val str = style.toString()

            (str.contains("CometChatStickerKeyboardStyle")) shouldBe true
            (str.contains("backgroundColor")) shouldBe true
            println("    ✅ toString contains class name and properties")
        }
    }
})
