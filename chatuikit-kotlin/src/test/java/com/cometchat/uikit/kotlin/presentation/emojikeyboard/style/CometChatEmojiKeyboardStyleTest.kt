package com.cometchat.uikit.kotlin.presentation.emojikeyboard.style

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Tests for CometChatEmojiKeyboardStyle data class (chatuikit-kotlin).
 *
 * Verifies:
 * - Default values are sourced from CometChatTheme
 * - Copy creates a new instance with modified values
 * - All style properties are accessible
 * - Data class equality and hashCode
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class CometChatEmojiKeyboardStyleTest {

    private val context: Context get() = RuntimeEnvironment.getApplication()

    // ==================== Default Values Tests ====================

    @Test
    fun `default style has backgroundColor from theme`() {
        println("  🧪 default style has backgroundColor from theme")

        val style = CometChatEmojiKeyboardStyle.default(context)

        style.backgroundColor shouldBe CometChatTheme.getBackgroundColor1(context)
        println("    ✅ backgroundColor defaults to CometChatTheme.getBackgroundColor1()")
    }

    @Test
    fun `default style has cornerRadius of 0`() {
        println("  🧪 default style has cornerRadius of 0")

        val style = CometChatEmojiKeyboardStyle.default(context)

        style.cornerRadius shouldBe 0
        println("    ✅ cornerRadius defaults to 0")
    }

    @Test
    fun `default style has strokeWidth of 0`() {
        println("  🧪 default style has strokeWidth of 0")

        val style = CometChatEmojiKeyboardStyle.default(context)

        style.strokeWidth shouldBe 0
        println("    ✅ strokeWidth defaults to 0")
    }

    @Test
    fun `default style has strokeColor from theme`() {
        println("  🧪 default style has strokeColor from theme")

        val style = CometChatEmojiKeyboardStyle.default(context)

        style.strokeColor shouldBe CometChatTheme.getStrokeColorLight(context)
        println("    ✅ strokeColor defaults to CometChatTheme.getStrokeColorLight()")
    }

    @Test
    fun `default style has separatorColor from theme`() {
        println("  🧪 default style has separatorColor from theme")

        val style = CometChatEmojiKeyboardStyle.default(context)

        style.separatorColor shouldBe CometChatTheme.getStrokeColorDefault(context)
        println("    ✅ separatorColor defaults to CometChatTheme.getStrokeColorDefault()")
    }

    @Test
    fun `default style has categoryTextColor from theme`() {
        println("  🧪 default style has categoryTextColor from theme")

        val style = CometChatEmojiKeyboardStyle.default(context)

        style.categoryTextColor shouldBe CometChatTheme.getTextColorTertiary(context)
        println("    ✅ categoryTextColor defaults to CometChatTheme.getTextColorTertiary()")
    }

    @Test
    fun `default style has categoryTextAppearance of 0`() {
        println("  🧪 default style has categoryTextAppearance of 0")

        val style = CometChatEmojiKeyboardStyle.default(context)

        style.categoryTextAppearance shouldBe 0
        println("    ✅ categoryTextAppearance defaults to 0 (no custom text appearance)")
    }

    @Test
    fun `default style has categoryIconTint from theme`() {
        println("  🧪 default style has categoryIconTint from theme")

        val style = CometChatEmojiKeyboardStyle.default(context)

        style.categoryIconTint shouldBe CometChatTheme.getIconTintSecondary(context)
        println("    ✅ categoryIconTint defaults to CometChatTheme.getIconTintSecondary()")
    }

    @Test
    fun `default style has selectedCategoryIconTint from theme`() {
        println("  🧪 default style has selectedCategoryIconTint from theme")

        val style = CometChatEmojiKeyboardStyle.default(context)

        style.selectedCategoryIconTint shouldBe CometChatTheme.getIconTintHighlight(context)
        println("    ✅ selectedCategoryIconTint defaults to CometChatTheme.getIconTintHighlight()")
    }

    @Test
    fun `default style has selectedCategoryBackgroundColor from theme`() {
        println("  🧪 default style has selectedCategoryBackgroundColor from theme")

        val style = CometChatEmojiKeyboardStyle.default(context)

        style.selectedCategoryBackgroundColor shouldBe CometChatTheme.getExtendedPrimaryColor100(context)
        println("    ✅ selectedCategoryBackgroundColor defaults to CometChatTheme.getExtendedPrimaryColor100()")
    }

    // ==================== Copy Tests ====================

    @Test
    fun `copy creates new instance with modified backgroundColor`() {
        println("  🧪 copy creates new instance with modified backgroundColor")

        val original = CometChatEmojiKeyboardStyle.default(context)
        val modified = original.copy(backgroundColor = 0xFFFF0000.toInt())

        modified.backgroundColor shouldBe 0xFFFF0000.toInt()
        modified.separatorColor shouldBe original.separatorColor
        modified.categoryIconTint shouldBe original.categoryIconTint
        println("    ✅ copy() modifies only specified field, preserves others")
    }

    @Test
    fun `copy creates new instance with modified cornerRadius`() {
        println("  🧪 copy creates new instance with modified cornerRadius")

        val original = CometChatEmojiKeyboardStyle.default(context)
        val modified = original.copy(cornerRadius = 24)

        modified.cornerRadius shouldBe 24
        modified.backgroundColor shouldBe original.backgroundColor
        println("    ✅ copy() with cornerRadius=24 preserves other fields")
    }

    @Test
    fun `copy creates new instance with modified separatorColor`() {
        println("  🧪 copy creates new instance with modified separatorColor")

        val original = CometChatEmojiKeyboardStyle.default(context)
        val modified = original.copy(separatorColor = 0xFF00FF00.toInt())

        modified.separatorColor shouldBe 0xFF00FF00.toInt()
        modified.backgroundColor shouldBe original.backgroundColor
        println("    ✅ copy() with separatorColor preserves other fields")
    }

    // ==================== Equality Tests ====================

    @Test
    fun `two default styles from same context are equal`() {
        println("  🧪 two default styles from same context are equal")

        val style1 = CometChatEmojiKeyboardStyle.default(context)
        val style2 = CometChatEmojiKeyboardStyle.default(context)

        style1 shouldBe style2
        style1.hashCode() shouldBe style2.hashCode()
        println("    ✅ Two default styles are equal (data class equality)")
    }

    @Test
    fun `styles with different backgroundColor are not equal`() {
        println("  🧪 styles with different backgroundColor are not equal")

        val style1 = CometChatEmojiKeyboardStyle.default(context)
        val style2 = style1.copy(backgroundColor = 0xFFFF0000.toInt())

        style1 shouldNotBe style2
        println("    ✅ Styles with different backgroundColor are not equal")
    }

    @Test
    fun `styles with different categoryIconTint are not equal`() {
        println("  🧪 styles with different categoryIconTint are not equal")

        val style1 = CometChatEmojiKeyboardStyle.default(context)
        val style2 = style1.copy(categoryIconTint = 0xFF123456.toInt())

        style1 shouldNotBe style2
        println("    ✅ Styles with different categoryIconTint are not equal")
    }

    // ==================== All Properties Accessible ====================

    @Test
    fun `all style properties are accessible via getters`() {
        println("  🧪 all style properties are accessible via getters")

        val style = CometChatEmojiKeyboardStyle(
            backgroundColor = 0xFF111111.toInt(),
            cornerRadius = 12,
            strokeWidth = 2,
            strokeColor = 0xFF222222.toInt(),
            separatorColor = 0xFF333333.toInt(),
            categoryTextColor = 0xFF444444.toInt(),
            categoryTextAppearance = 0,
            categoryIconTint = 0xFF555555.toInt(),
            selectedCategoryIconTint = 0xFF666666.toInt(),
            selectedCategoryBackgroundColor = 0xFF777777.toInt()
        )

        style.backgroundColor shouldBe 0xFF111111.toInt()
        style.cornerRadius shouldBe 12
        style.strokeWidth shouldBe 2
        style.strokeColor shouldBe 0xFF222222.toInt()
        style.separatorColor shouldBe 0xFF333333.toInt()
        style.categoryTextColor shouldBe 0xFF444444.toInt()
        style.categoryTextAppearance shouldBe 0
        style.categoryIconTint shouldBe 0xFF555555.toInt()
        style.selectedCategoryIconTint shouldBe 0xFF666666.toInt()
        style.selectedCategoryBackgroundColor shouldBe 0xFF777777.toInt()
        println("    ✅ All 10 style properties are accessible and return correct values")
    }

    // ==================== Constructor Tests ====================

    @Test
    fun `default constructor creates style with all zeros`() {
        println("  🧪 default constructor creates style with all zeros")

        val style = CometChatEmojiKeyboardStyle()

        style.backgroundColor shouldBe 0
        style.cornerRadius shouldBe 0
        style.strokeWidth shouldBe 0
        style.strokeColor shouldBe 0
        style.separatorColor shouldBe 0
        style.categoryTextColor shouldBe 0
        style.categoryTextAppearance shouldBe 0
        style.categoryIconTint shouldBe 0
        style.selectedCategoryIconTint shouldBe 0
        style.selectedCategoryBackgroundColor shouldBe 0
        println("    ✅ Default constructor initializes all properties to 0")
    }
}
