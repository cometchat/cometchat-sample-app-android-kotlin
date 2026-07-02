package com.cometchat.uikit.kotlin.presentation.polls.style

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * Tests for CometChatCreatePollStyle data class (Kotlin XML View).
 *
 * Verifies:
 * - Default values (all zeros/nulls for data class defaults)
 * - Copy with changes
 * - All style properties accessible
 * - Equality and hashCode
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*.CometChatCreatePollStyleTest"
 */
class CometChatCreatePollStyleTest : FunSpec({

    beforeTest {
        println("  🧪 ${it.name.testName}")
    }

    // ==================== Default Values ====================

    test("default constructor creates style with zero/null defaults") {
        println("    → Creating style with default constructor")
        val style = CometChatCreatePollStyle()

        style.backgroundColor shouldBe 0
        style.backgroundDrawable shouldBe null
        style.cornerRadius shouldBe 0
        style.strokeWidth shouldBe 0
        style.strokeColor shouldBe 0
        style.titleTextColor shouldBe 0
        style.titleTextAppearance shouldBe 0
        style.questionTitleTextColor shouldBe 0
        style.questionTextColor shouldBe 0
        style.questionHintColor shouldBe 0
        style.questionCornerRadius shouldBe 0
        style.questionStrokeWidth shouldBe 0
        style.questionStrokeColor shouldBe 0
        style.optionTitleTextColor shouldBe 0
        style.optionTextColor shouldBe 0
        style.optionHintColor shouldBe 0
        style.optionBackgroundColor shouldBe 0
        style.optionCornerRadius shouldBe 0
        style.optionStrokeWidth shouldBe 0
        style.optionStrokeColor shouldBe 0
        style.dragIconTint shouldBe 0
        style.dragIcon shouldBe null
        style.backIconTint shouldBe 0
        style.backIcon shouldBe null
        style.separatorColor shouldBe 0
        style.submitButtonBackgroundColor shouldBe 0
        style.disabledSubmitButtonBackgroundColor shouldBe 0
        style.submitButtonCornerRadius shouldBe 0
        style.submitButtonStrokeWidth shouldBe 0
        style.submitButtonStrokeColor shouldBe 0
        style.submitButtonTextColor shouldBe 0
        style.submitButtonTextAppearance shouldBe 0
        style.progressIndicatorColor shouldBe 0
        style.errorTextColor shouldBe 0
        style.errorTextAppearance shouldBe 0
        println("    ✓ All default values are zero/null")
    }

    // ==================== Copy with Changes ====================

    test("copy with backgroundColor change preserves other values") {
        println("    → Copying style with backgroundColor change")
        val original = CometChatCreatePollStyle(
            backgroundColor = 0xFF000000.toInt(),
            titleTextColor = 0xFFFFFFFF.toInt(),
            submitButtonBackgroundColor = 0xFF6200EE.toInt()
        )

        val copied = original.copy(backgroundColor = 0xFFFF0000.toInt())

        copied.backgroundColor shouldBe 0xFFFF0000.toInt()
        copied.titleTextColor shouldBe 0xFFFFFFFF.toInt()
        copied.submitButtonBackgroundColor shouldBe 0xFF6200EE.toInt()
        println("    ✓ Copy preserves unchanged values")
    }

    test("copy with multiple changes applies all") {
        println("    → Copying style with multiple changes")
        val original = CometChatCreatePollStyle()

        val modified = original.copy(
            backgroundColor = 0xFF1A1A2E.toInt(),
            titleTextColor = 0xFFFFFFFF.toInt(),
            separatorColor = 0xFF333333.toInt(),
            submitButtonBackgroundColor = 0xFF6851D6.toInt(),
            errorTextColor = 0xFFFF0000.toInt()
        )

        modified.backgroundColor shouldBe 0xFF1A1A2E.toInt()
        modified.titleTextColor shouldBe 0xFFFFFFFF.toInt()
        modified.separatorColor shouldBe 0xFF333333.toInt()
        modified.submitButtonBackgroundColor shouldBe 0xFF6851D6.toInt()
        modified.errorTextColor shouldBe 0xFFFF0000.toInt()
        println("    ✓ All changes applied correctly")
    }

    // ==================== All Properties Accessible ====================

    test("all container properties are accessible") {
        println("    → Verifying container property access")
        val style = CometChatCreatePollStyle(
            backgroundColor = 1,
            cornerRadius = 16,
            strokeWidth = 2,
            strokeColor = 3
        )

        style.backgroundColor shouldBe 1
        style.cornerRadius shouldBe 16
        style.strokeWidth shouldBe 2
        style.strokeColor shouldBe 3
        println("    ✓ Container properties accessible")
    }

    test("all question section properties are accessible") {
        println("    → Verifying question section property access")
        val style = CometChatCreatePollStyle(
            questionTitleTextColor = 1,
            questionTitleTextAppearance = 2,
            questionTextColor = 3,
            questionTextAppearance = 4,
            questionHintColor = 5,
            questionCornerRadius = 8,
            questionStrokeWidth = 1,
            questionStrokeColor = 6
        )

        style.questionTitleTextColor shouldBe 1
        style.questionTitleTextAppearance shouldBe 2
        style.questionTextColor shouldBe 3
        style.questionTextAppearance shouldBe 4
        style.questionHintColor shouldBe 5
        style.questionCornerRadius shouldBe 8
        style.questionStrokeWidth shouldBe 1
        style.questionStrokeColor shouldBe 6
        println("    ✓ Question section properties accessible")
    }

    test("all option section properties are accessible") {
        println("    → Verifying option section property access")
        val style = CometChatCreatePollStyle(
            optionTitleTextColor = 1,
            optionTitleTextAppearance = 2,
            optionTextColor = 3,
            optionTextAppearance = 4,
            optionHintColor = 5,
            optionBackgroundColor = 6,
            optionCornerRadius = 8,
            optionStrokeWidth = 1,
            optionStrokeColor = 7
        )

        style.optionTitleTextColor shouldBe 1
        style.optionTitleTextAppearance shouldBe 2
        style.optionTextColor shouldBe 3
        style.optionTextAppearance shouldBe 4
        style.optionHintColor shouldBe 5
        style.optionBackgroundColor shouldBe 6
        style.optionCornerRadius shouldBe 8
        style.optionStrokeWidth shouldBe 1
        style.optionStrokeColor shouldBe 7
        println("    ✓ Option section properties accessible")
    }

    test("all submit button properties are accessible") {
        println("    → Verifying submit button property access")
        val style = CometChatCreatePollStyle(
            submitButtonBackgroundColor = 1,
            disabledSubmitButtonBackgroundColor = 2,
            submitButtonCornerRadius = 12,
            submitButtonStrokeWidth = 1,
            submitButtonStrokeColor = 3,
            submitButtonTextColor = 4,
            submitButtonTextAppearance = 5
        )

        style.submitButtonBackgroundColor shouldBe 1
        style.disabledSubmitButtonBackgroundColor shouldBe 2
        style.submitButtonCornerRadius shouldBe 12
        style.submitButtonStrokeWidth shouldBe 1
        style.submitButtonStrokeColor shouldBe 3
        style.submitButtonTextColor shouldBe 4
        style.submitButtonTextAppearance shouldBe 5
        println("    ✓ Submit button properties accessible")
    }

    test("icon and separator properties are accessible") {
        println("    → Verifying icon and separator property access")
        val style = CometChatCreatePollStyle(
            dragIconTint = 1,
            backIconTint = 2,
            separatorColor = 3,
            progressIndicatorColor = 4,
            errorTextColor = 5,
            errorTextAppearance = 6
        )

        style.dragIconTint shouldBe 1
        style.backIconTint shouldBe 2
        style.separatorColor shouldBe 3
        style.progressIndicatorColor shouldBe 4
        style.errorTextColor shouldBe 5
        style.errorTextAppearance shouldBe 6
        println("    ✓ Icon and separator properties accessible")
    }

    // ==================== Equality ====================

    test("two styles with same values are equal") {
        println("    → Verifying equality")
        val style1 = CometChatCreatePollStyle(
            backgroundColor = 0xFF000000.toInt(),
            titleTextColor = 0xFFFFFFFF.toInt()
        )
        val style2 = CometChatCreatePollStyle(
            backgroundColor = 0xFF000000.toInt(),
            titleTextColor = 0xFFFFFFFF.toInt()
        )

        style1 shouldBe style2
        style1.hashCode() shouldBe style2.hashCode()
        println("    ✓ Equal styles have same hashCode")
    }

    test("two styles with different values are not equal") {
        println("    → Verifying inequality")
        val style1 = CometChatCreatePollStyle(backgroundColor = 0xFF000000.toInt())
        val style2 = CometChatCreatePollStyle(backgroundColor = 0xFFFFFFFF.toInt())

        style1 shouldNotBe style2
        println("    ✓ Different styles are not equal")
    }
})
