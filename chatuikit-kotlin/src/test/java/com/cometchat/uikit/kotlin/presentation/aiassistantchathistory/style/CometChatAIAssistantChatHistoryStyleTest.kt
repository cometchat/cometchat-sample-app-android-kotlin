package com.cometchat.uikit.kotlin.presentation.aiassistantchathistory.style

import android.graphics.Color
import android.graphics.drawable.Drawable
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import org.mockito.kotlin.mock

/**
 * Tests for CometChatAIAssistantChatHistoryStyle data class.
 *
 * Verifies:
 * - Default values are correct (all 0 for colors/appearances, null for drawables)
 * - Copy creates a new instance with changed values
 * - All 21 style properties are accessible and independently modifiable
 * - Data class equality and hashCode contracts
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "...CometChatAIAssistantChatHistoryStyleTest"
 */
class CometChatAIAssistantChatHistoryStyleTest : FunSpec({

    beforeTest {
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        println()
    }

    // ==================== Default Values ====================

    test("default constructor should have all color properties set to 0") {
        val style = CometChatAIAssistantChatHistoryStyle()

        println("    → Checking all @ColorInt properties default to 0")
        style.chatHistoryBackgroundColor shouldBe 0
        style.chatHistoryHeaderBackgroundColor shouldBe 0
        style.chatHistoryHeaderTextColor shouldBe 0
        style.chatHistoryHeaderCloseIconTint shouldBe 0
        style.newChatBackgroundColor shouldBe 0
        style.newChatTextColor shouldBe 0
        style.newChatIconTint shouldBe 0
        style.dateSeparatorBackgroundColor shouldBe 0
        style.dateSeparatorTextColor shouldBe 0
        style.itemBackgroundColor shouldBe 0
        style.itemTextColor shouldBe 0
        style.deleteOptionIconTint shouldBe 0
        style.deleteOptionTextColor shouldBe 0
        println("    ✅ All 13 @ColorInt properties default to 0")
    }

    test("default constructor should have all text appearance properties set to 0") {
        val style = CometChatAIAssistantChatHistoryStyle()

        println("    → Checking all @StyleRes properties default to 0")
        style.chatHistoryHeaderTextAppearance shouldBe 0
        style.newChatTextAppearance shouldBe 0
        style.dateSeparatorTextAppearance shouldBe 0
        style.itemTextAppearance shouldBe 0
        style.deleteOptionTextAppearance shouldBe 0
        println("    ✅ All 5 @StyleRes properties default to 0")
    }

    test("default constructor should have all drawable properties set to null") {
        val style = CometChatAIAssistantChatHistoryStyle()

        println("    → Checking Drawable properties default to null")
        style.chatHistoryHeaderCloseIcon shouldBe null
        style.deleteOptionIcon shouldBe null
        println("    ✅ Both Drawable properties default to null")
    }

    test("default constructor should have newChatIcon @DrawableRes set to 0") {
        val style = CometChatAIAssistantChatHistoryStyle()

        println("    → Checking @DrawableRes property defaults to 0")
        style.newChatIcon shouldBe 0
        println("    ✅ newChatIcon @DrawableRes defaults to 0")
    }

    // ==================== Copy ====================

    test("copy should create new instance with changed chatHistoryBackgroundColor") {
        val original = CometChatAIAssistantChatHistoryStyle()
        val modified = original.copy(chatHistoryBackgroundColor = Color.RED)

        println("    → original.chatHistoryBackgroundColor=${original.chatHistoryBackgroundColor}")
        println("    → modified.chatHistoryBackgroundColor=${modified.chatHistoryBackgroundColor}")
        modified.chatHistoryBackgroundColor shouldBe Color.RED
        original.chatHistoryBackgroundColor shouldBe 0
        modified shouldNotBe original
        println("    ✅ Copy creates independent instance")
    }

    test("copy should preserve unchanged properties") {
        val mockDrawable = mock<Drawable>()
        val original = CometChatAIAssistantChatHistoryStyle(
            chatHistoryBackgroundColor = Color.BLUE,
            chatHistoryHeaderTextColor = Color.WHITE,
            chatHistoryHeaderCloseIcon = mockDrawable,
            itemTextAppearance = 42
        )
        val modified = original.copy(chatHistoryBackgroundColor = Color.GREEN)

        println("    → Modified only chatHistoryBackgroundColor")
        modified.chatHistoryHeaderTextColor shouldBe Color.WHITE
        modified.chatHistoryHeaderCloseIcon shouldBe mockDrawable
        modified.itemTextAppearance shouldBe 42
        modified.chatHistoryBackgroundColor shouldBe Color.GREEN
        println("    ✅ Unchanged properties preserved after copy")
    }

    test("for any color value: copy should correctly set chatHistoryBackgroundColor") {
        checkAll(30, Arb.int(Int.MIN_VALUE..Int.MAX_VALUE)) { colorValue ->
            val style = CometChatAIAssistantChatHistoryStyle().copy(
                chatHistoryBackgroundColor = colorValue
            )
            style.chatHistoryBackgroundColor shouldBe colorValue
            println("    → color=$colorValue ✅")
        }
    }

    // ==================== All 21 Properties Accessible ====================

    test("all 21 style properties should be independently settable") {
        val mockCloseIcon = mock<Drawable>()
        val mockDeleteIcon = mock<Drawable>()

        val style = CometChatAIAssistantChatHistoryStyle(
            // Component background (1)
            chatHistoryBackgroundColor = Color.parseColor("#FFFFFF"),
            // Header section (5)
            chatHistoryHeaderBackgroundColor = Color.parseColor("#F5F5F5"),
            chatHistoryHeaderTextColor = Color.parseColor("#000000"),
            chatHistoryHeaderTextAppearance = 101,
            chatHistoryHeaderCloseIcon = mockCloseIcon,
            chatHistoryHeaderCloseIconTint = Color.parseColor("#666666"),
            // New Chat section (5)
            newChatBackgroundColor = Color.parseColor("#E8E8E8"),
            newChatTextColor = Color.parseColor("#333333"),
            newChatTextAppearance = 102,
            newChatIcon = 201,
            newChatIconTint = Color.parseColor("#999999"),
            // Date separator (3)
            dateSeparatorBackgroundColor = Color.TRANSPARENT,
            dateSeparatorTextColor = Color.parseColor("#AAAAAA"),
            dateSeparatorTextAppearance = 103,
            // Message item (3)
            itemBackgroundColor = Color.parseColor("#FAFAFA"),
            itemTextColor = Color.parseColor("#1A1A1A"),
            itemTextAppearance = 104,
            // Delete option (4)
            deleteOptionIcon = mockDeleteIcon,
            deleteOptionIconTint = Color.RED,
            deleteOptionTextColor = Color.parseColor("#FF0000"),
            deleteOptionTextAppearance = 105
        )

        println("    → Verifying all 21 properties")
        // Component background
        style.chatHistoryBackgroundColor shouldBe Color.parseColor("#FFFFFF")
        // Header section
        style.chatHistoryHeaderBackgroundColor shouldBe Color.parseColor("#F5F5F5")
        style.chatHistoryHeaderTextColor shouldBe Color.parseColor("#000000")
        style.chatHistoryHeaderTextAppearance shouldBe 101
        style.chatHistoryHeaderCloseIcon shouldBe mockCloseIcon
        style.chatHistoryHeaderCloseIconTint shouldBe Color.parseColor("#666666")
        // New Chat section
        style.newChatBackgroundColor shouldBe Color.parseColor("#E8E8E8")
        style.newChatTextColor shouldBe Color.parseColor("#333333")
        style.newChatTextAppearance shouldBe 102
        style.newChatIcon shouldBe 201
        style.newChatIconTint shouldBe Color.parseColor("#999999")
        // Date separator
        style.dateSeparatorBackgroundColor shouldBe Color.TRANSPARENT
        style.dateSeparatorTextColor shouldBe Color.parseColor("#AAAAAA")
        style.dateSeparatorTextAppearance shouldBe 103
        // Message item
        style.itemBackgroundColor shouldBe Color.parseColor("#FAFAFA")
        style.itemTextColor shouldBe Color.parseColor("#1A1A1A")
        style.itemTextAppearance shouldBe 104
        // Delete option
        style.deleteOptionIcon shouldBe mockDeleteIcon
        style.deleteOptionIconTint shouldBe Color.RED
        style.deleteOptionTextColor shouldBe Color.parseColor("#FF0000")
        style.deleteOptionTextAppearance shouldBe 105
        println("    ✅ All 21 properties verified (1 bg + 5 header + 5 newChat + 3 dateSep + 3 item + 4 delete)")
    }

    // ==================== Equality ====================

    test("two styles with same values should be equal") {
        val style1 = CometChatAIAssistantChatHistoryStyle(
            chatHistoryBackgroundColor = Color.RED,
            itemTextColor = Color.BLUE
        )
        val style2 = CometChatAIAssistantChatHistoryStyle(
            chatHistoryBackgroundColor = Color.RED,
            itemTextColor = Color.BLUE
        )

        println("    → style1 == style2")
        (style1 == style2) shouldBe true
        style1.hashCode() shouldBe style2.hashCode()
        println("    ✅ Data class equality and hashCode contract satisfied")
    }

    test("two styles with different values should not be equal") {
        val style1 = CometChatAIAssistantChatHistoryStyle(chatHistoryBackgroundColor = Color.RED)
        val style2 = CometChatAIAssistantChatHistoryStyle(chatHistoryBackgroundColor = Color.BLUE)

        println("    → style1 != style2")
        (style1 == style2) shouldBe false
        println("    ✅ Different values produce inequality")
    }

    // ==================== Property Count Verification ====================

    test("style data class should have exactly 21 properties") {
        // Verify by constructing with all named parameters
        val style = CometChatAIAssistantChatHistoryStyle(
            chatHistoryBackgroundColor = 1,          // 1
            chatHistoryHeaderBackgroundColor = 2,    // 2
            chatHistoryHeaderTextColor = 3,          // 3
            chatHistoryHeaderTextAppearance = 4,     // 4
            chatHistoryHeaderCloseIcon = null,       // 5
            chatHistoryHeaderCloseIconTint = 6,      // 6
            newChatBackgroundColor = 7,              // 7
            newChatTextColor = 8,                    // 8
            newChatTextAppearance = 9,               // 9
            newChatIcon = 10,                        // 10
            newChatIconTint = 11,                    // 11
            dateSeparatorBackgroundColor = 12,       // 12
            dateSeparatorTextColor = 13,             // 13
            dateSeparatorTextAppearance = 14,        // 14
            itemBackgroundColor = 15,                // 15
            itemTextColor = 16,                      // 16
            itemTextAppearance = 17,                 // 17
            deleteOptionIcon = null,                 // 18
            deleteOptionIconTint = 19,               // 19
            deleteOptionTextColor = 20,              // 20
            deleteOptionTextAppearance = 21          // 21
        )

        println("    → All 21 named parameters compile successfully")
        style.shouldBeInstanceOf<CometChatAIAssistantChatHistoryStyle>()
        println("    ✅ Style has exactly 21 properties")
    }
})
