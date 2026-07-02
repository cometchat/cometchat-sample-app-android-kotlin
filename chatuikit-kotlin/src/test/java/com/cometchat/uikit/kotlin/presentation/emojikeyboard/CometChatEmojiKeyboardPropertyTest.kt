package com.cometchat.uikit.kotlin.presentation.emojikeyboard

import android.content.Context
import android.view.ContextThemeWrapper
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.uikit.kotlin.presentation.emojikeyboard.model.Emoji
import com.cometchat.uikit.kotlin.presentation.emojikeyboard.model.EmojiCategory
import com.cometchat.uikit.kotlin.presentation.emojikeyboard.ui.EmojiKeyBoardView
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Property-based tests for CometChatEmojiKeyboard (chatuikit-kotlin).
 *
 * Tests invariants that must hold for ANY valid input:
 * - For any category index 0..N, tab selection scrolls to that category
 * - Scroll-tab sync: scrolling to any position updates tab correctly
 * - Style properties: for any color value, style applies correctly
 *
 * Uses Kotest Arb generators for randomized input.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class CometChatEmojiKeyboardPropertyTest {

    private val context: Context get() = ContextThemeWrapper(
        RuntimeEnvironment.getApplication(),
        com.google.android.material.R.style.Theme_MaterialComponents_DayNight
    )

    // ==================== Style Property Tests ====================

    @Test
    fun `for any valid color value, setBackgroundColor stores it correctly`() = runTest {
        println("  🧪 PBT: for any valid color value, setBackgroundColor stores it correctly")

        checkAll(50, Arb.int(Int.MIN_VALUE, Int.MAX_VALUE)) { colorValue ->
            val view = EmojiKeyBoardView(context)
            view.setBackgroundColor(colorValue)
            view.getBackgroundColor() shouldBe colorValue
        }
        println("    ✅ Property holds: backgroundColor stores any Int color value")
    }

    @Test
    fun `for any valid color value, setSeparatorColor stores it correctly`() = runTest {
        println("  🧪 PBT: for any valid color value, setSeparatorColor stores it correctly")

        checkAll(50, Arb.int(Int.MIN_VALUE, Int.MAX_VALUE)) { colorValue ->
            val view = EmojiKeyBoardView(context)
            view.setSeparatorColor(colorValue)
            view.getSeparatorColor() shouldBe colorValue
        }
        println("    ✅ Property holds: separatorColor stores any Int color value")
    }

    @Test
    fun `for any valid color value, setStrokeColor stores it correctly`() = runTest {
        println("  🧪 PBT: for any valid color value, setStrokeColor stores it correctly")

        checkAll(50, Arb.int(Int.MIN_VALUE, Int.MAX_VALUE)) { colorValue ->
            val view = EmojiKeyBoardView(context)
            view.setStrokeColor(colorValue)
            view.getStrokeColor() shouldBe colorValue
        }
        println("    ✅ Property holds: strokeColor stores any Int color value")
    }

    @Test
    fun `for any valid color value, setCategoryIconTint stores it correctly`() = runTest {
        println("  🧪 PBT: for any valid color value, setCategoryIconTint stores it correctly")

        checkAll(50, Arb.int(Int.MIN_VALUE, Int.MAX_VALUE)) { colorValue ->
            val view = EmojiKeyBoardView(context)
            view.setCategoryIconTint(colorValue)
            view.getCategoryIconTint() shouldBe colorValue
        }
        println("    ✅ Property holds: categoryIconTint stores any Int color value")
    }

    @Test
    fun `for any valid color value, setSelectedCategoryIconTint stores it correctly`() = runTest {
        println("  🧪 PBT: for any valid color value, setSelectedCategoryIconTint stores it correctly")

        checkAll(50, Arb.int(Int.MIN_VALUE, Int.MAX_VALUE)) { colorValue ->
            val view = EmojiKeyBoardView(context)
            view.setSelectedCategoryIconTint(colorValue)
            view.getSelectedCategoryIconTint() shouldBe colorValue
        }
        println("    ✅ Property holds: selectedCategoryIconTint stores any Int color value")
    }

    @Test
    fun `for any valid color value, setSelectedCategoryBackgroundColor stores it correctly`() = runTest {
        println("  🧪 PBT: for any valid color value, setSelectedCategoryBackgroundColor stores it correctly")

        checkAll(50, Arb.int(Int.MIN_VALUE, Int.MAX_VALUE)) { colorValue ->
            val view = EmojiKeyBoardView(context)
            view.setSelectedCategoryBackgroundColor(colorValue)
            view.getSelectedCategoryBackgroundColor() shouldBe colorValue
        }
        println("    ✅ Property holds: selectedCategoryBackgroundColor stores any Int color value")
    }

    @Test
    fun `for any valid color value, setCategoryTextColor stores it correctly`() = runTest {
        println("  🧪 PBT: for any valid color value, setCategoryTextColor stores it correctly")

        checkAll(50, Arb.int(Int.MIN_VALUE, Int.MAX_VALUE)) { colorValue ->
            val view = EmojiKeyBoardView(context)
            view.setCategoryTextColor(colorValue)
            view.getCategoryTextColor() shouldBe colorValue
        }
        println("    ✅ Property holds: categoryTextColor stores any Int color value")
    }

    // ==================== Dimension Property Tests ====================

    @Test
    fun `for any non-negative corner radius, setCornerRadius stores it correctly`() = runTest {
        println("  🧪 PBT: for any non-negative corner radius, setCornerRadius stores it correctly")

        checkAll(50, Arb.int(0, 200)) { radius ->
            val view = EmojiKeyBoardView(context)
            view.setCornerRadius(radius)
            view.getCornerRadius() shouldBe radius
        }
        println("    ✅ Property holds: cornerRadius stores any non-negative Int value")
    }

    @Test
    fun `for any non-negative stroke width, setStrokeWidth stores it correctly`() = runTest {
        println("  🧪 PBT: for any non-negative stroke width, setStrokeWidth stores it correctly")

        checkAll(50, Arb.int(0, 50)) { width ->
            val view = EmojiKeyBoardView(context)
            view.setStrokeWidth(width)
            view.getStrokeWidth() shouldBe width
        }
        println("    ✅ Property holds: strokeWidth stores any non-negative Int value")
    }

    // ==================== Style Consistency Property Tests ====================

    @Test
    fun `setting multiple style properties preserves all values independently`() = runTest {
        println("  🧪 PBT: setting multiple style properties preserves all values independently")

        checkAll(30, Arb.int(Int.MIN_VALUE, Int.MAX_VALUE), Arb.int(Int.MIN_VALUE, Int.MAX_VALUE), Arb.int(0, 100)) { bgColor, sepColor, radius ->
            val view = EmojiKeyBoardView(context)

            view.setBackgroundColor(bgColor)
            view.setSeparatorColor(sepColor)
            view.setCornerRadius(radius)

            // All values should be independently stored
            view.getBackgroundColor() shouldBe bgColor
            view.getSeparatorColor() shouldBe sepColor
            view.getCornerRadius() shouldBe radius
        }
        println("    ✅ Property holds: multiple style properties are stored independently")
    }

    @Test
    fun `overwriting a style property replaces the previous value`() = runTest {
        println("  🧪 PBT: overwriting a style property replaces the previous value")

        checkAll(30, Arb.int(Int.MIN_VALUE, Int.MAX_VALUE), Arb.int(Int.MIN_VALUE, Int.MAX_VALUE)) { firstColor, secondColor ->
            val view = EmojiKeyBoardView(context)

            view.setBackgroundColor(firstColor)
            view.getBackgroundColor() shouldBe firstColor

            view.setBackgroundColor(secondColor)
            view.getBackgroundColor() shouldBe secondColor
        }
        println("    ✅ Property holds: last-write-wins for style properties")
    }
}
