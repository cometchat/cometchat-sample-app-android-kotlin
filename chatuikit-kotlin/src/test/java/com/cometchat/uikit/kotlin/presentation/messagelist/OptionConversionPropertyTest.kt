package com.cometchat.uikit.kotlin.presentation.messagelist

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Represents a CometChatMessageOption's fields for pure-logic testing.
 * Mirrors the fields from [com.cometchat.uikit.core.domain.model.CometChatMessageOption].
 */
private data class TestMessageOption(
    val id: String,
    val title: String,
    val titleColor: Int = 0,
    val icon: Int = 0,
    val iconTintColor: Int = 0,
    val titleAppearance: Int = 0
)

/**
 * Represents a converted MenuItem's fields for pure-logic testing.
 * Mirrors the fields from [com.cometchat.uikit.kotlin.presentation.shared.popupmenu.CometChatPopupMenu.MenuItem].
 */
private data class TestMenuItem(
    val id: String,
    val name: String,
    val hasStartIcon: Boolean,
    val startIconTint: Int,
    val textColor: Int,
    val textAppearance: Int
)

/**
 * Pure-logic function that mirrors the CometChatMessageOption → MenuItem conversion
 * performed in CometChatMessageList.openMessageOptionBottomSheet():
 *
 * ```kotlin
 * val menuItems = originalOptions.map { option ->
 *     MenuItem(
 *         id = option.id,
 *         name = option.title,
 *         startIcon = if (option.icon != 0) drawable else null,
 *         startIconTint = option.iconTintColor,
 *         textColor = option.titleColor,
 *         textAppearance = option.titleAppearance
 *     )
 * }
 * ```
 */
private fun convertOptionToMenuItem(option: TestMessageOption): TestMenuItem {
    return TestMenuItem(
        id = option.id,
        name = option.title,
        hasStartIcon = option.icon != 0,
        startIconTint = option.iconTintColor,
        textColor = option.titleColor,
        textAppearance = option.titleAppearance
    )
}

/**
 * Property-based tests for CometChatMessageOption to MenuItem conversion.
 *
 * Feature: message-popup-menu, Property 9: CometChatMessageOption to MenuItem conversion preserves all fields
 *
 * *For any* CometChatMessageOption (with arbitrary id, title, icon, iconTintColor, titleColor,
 * titleAppearance), converting it to a MenuItem should preserve all mapped fields.
 *
 * **Validates: Requirements 6.3**
 */
class OptionConversionPropertyTest : FunSpec({

    // ==================== Generators ====================

    val idArb = Arb.string(1..20)
    val titleArb = Arb.string(1..50)
    val colorArb = Arb.int(0..0xFFFFFF)
    val iconResArb = Arb.int(0..1000)
    val styleResArb = Arb.int(0..500)

    // ==================== Property Tests ====================

    context("Property 9: CometChatMessageOption to MenuItem conversion preserves all fields") {

        test("id maps to id") {
            checkAll(100, idArb, titleArb, iconResArb, colorArb, colorArb, styleResArb) { id, title, icon, iconTint, titleColor, titleAppearance ->
                val option = TestMessageOption(id, title, titleColor, icon, iconTint, titleAppearance)
                val menuItem = convertOptionToMenuItem(option)
                menuItem.id shouldBe option.id
            }
        }

        test("title maps to name") {
            checkAll(100, idArb, titleArb, iconResArb, colorArb, colorArb, styleResArb) { id, title, icon, iconTint, titleColor, titleAppearance ->
                val option = TestMessageOption(id, title, titleColor, icon, iconTint, titleAppearance)
                val menuItem = convertOptionToMenuItem(option)
                menuItem.name shouldBe option.title
            }
        }

        test("icon maps to startIcon presence (non-zero = present, zero = null)") {
            checkAll(100, idArb, titleArb, iconResArb) { id, title, icon ->
                val option = TestMessageOption(id = id, title = title, icon = icon)
                val menuItem = convertOptionToMenuItem(option)
                menuItem.hasStartIcon shouldBe (icon != 0)
            }
        }

        test("iconTintColor maps to startIconTint") {
            checkAll(100, idArb, titleArb, colorArb) { id, title, iconTint ->
                val option = TestMessageOption(id = id, title = title, iconTintColor = iconTint)
                val menuItem = convertOptionToMenuItem(option)
                menuItem.startIconTint shouldBe option.iconTintColor
            }
        }

        test("titleColor maps to textColor") {
            checkAll(100, idArb, titleArb, colorArb) { id, title, titleColor ->
                val option = TestMessageOption(id = id, title = title, titleColor = titleColor)
                val menuItem = convertOptionToMenuItem(option)
                menuItem.textColor shouldBe option.titleColor
            }
        }

        test("titleAppearance maps to textAppearance") {
            checkAll(100, idArb, titleArb, styleResArb) { id, title, titleAppearance ->
                val option = TestMessageOption(id = id, title = title, titleAppearance = titleAppearance)
                val menuItem = convertOptionToMenuItem(option)
                menuItem.textAppearance shouldBe option.titleAppearance
            }
        }

        test("all fields are preserved in a single conversion") {
            checkAll(100, idArb, titleArb, colorArb, iconResArb, colorArb, styleResArb) { id, title, titleColor, icon, iconTint, titleAppearance ->
                val option = TestMessageOption(id, title, titleColor, icon, iconTint, titleAppearance)
                val menuItem = convertOptionToMenuItem(option)

                menuItem.id shouldBe option.id
                menuItem.name shouldBe option.title
                menuItem.hasStartIcon shouldBe (option.icon != 0)
                menuItem.startIconTint shouldBe option.iconTintColor
                menuItem.textColor shouldBe option.titleColor
                menuItem.textAppearance shouldBe option.titleAppearance
            }
        }
    }
})
