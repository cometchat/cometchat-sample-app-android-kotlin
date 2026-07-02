package com.cometchat.uikit.kotlin.presentation.outgoingcall.style

import android.content.Context
import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import com.cometchat.uikit.kotlin.presentation.outgoingcall.style.CometChatOutgoingCallStyle
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests for CometChatOutgoingCallStyle data class.
 *
 * Verifies:
 * - Default style creation via Builder
 * - Builder fluent API (all setters)
 * - Data class equality and copy
 * - All style properties are accessible
 *
 * Note: Uses Robolectric because the style class requires a Context
 * to access theme attributes for default values.
 *
 * Validates: Requirements 12a.1-12a.15
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*CometChatOutgoingCallStyleTest"
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class CometChatOutgoingCallStyleTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    // ==================== Default Style ====================

    @Test
    fun `default style should create valid instance`() {
        println("  🧪 default style should create valid instance")

        val style = CometChatOutgoingCallStyle.default(context)

        style shouldNotBe null

        println("    ✅ Default style created successfully")
    }

    @Test
    fun `default style should have zero corner radius`() {
        println("  🧪 default style should have zero corner radius")

        val style = CometChatOutgoingCallStyle.default(context)

        style.cornerRadius shouldBe 0f

        println("    ✅ Default cornerRadius=0f")
    }

    @Test
    fun `default style should have zero stroke width`() {
        println("  🧪 default style should have zero stroke width")

        val style = CometChatOutgoingCallStyle.default(context)

        style.strokeWidth shouldBe 0

        println("    ✅ Default strokeWidth=0")
    }

    @Test
    fun `default style should have transparent stroke color`() {
        println("  🧪 default style should have transparent stroke color")

        val style = CometChatOutgoingCallStyle.default(context)

        style.strokeColor shouldBe Color.TRANSPARENT

        println("    ✅ Default strokeColor=TRANSPARENT")
    }

    // ==================== Builder API ====================

    @Test
    fun `builder should set background color`() {
        println("  🧪 builder should set background color")

        val style = CometChatOutgoingCallStyle.builder(context)
            .setBackgroundColor(Color.RED)
            .build()

        style.backgroundColor shouldBe Color.RED

        println("    ✅ Builder set backgroundColor=RED")
    }

    @Test
    fun `builder should set corner radius`() {
        println("  🧪 builder should set corner radius")

        val style = CometChatOutgoingCallStyle.builder(context)
            .setCornerRadius(16f)
            .build()

        style.cornerRadius shouldBe 16f

        println("    ✅ Builder set cornerRadius=16f")
    }

    @Test
    fun `builder should set stroke width and color`() {
        println("  🧪 builder should set stroke width and color")

        val style = CometChatOutgoingCallStyle.builder(context)
            .setStrokeWidth(2)
            .setStrokeColor(Color.BLUE)
            .build()

        style.strokeWidth shouldBe 2
        style.strokeColor shouldBe Color.BLUE

        println("    ✅ Builder set strokeWidth=2, strokeColor=BLUE")
    }

    @Test
    fun `builder should set title text color`() {
        println("  🧪 builder should set title text color")

        val style = CometChatOutgoingCallStyle.builder(context)
            .setTitleTextColor(Color.WHITE)
            .build()

        style.titleTextColor shouldBe Color.WHITE

        println("    ✅ Builder set titleTextColor=WHITE")
    }

    @Test
    fun `builder should set subtitle text color`() {
        println("  🧪 builder should set subtitle text color")

        val style = CometChatOutgoingCallStyle.builder(context)
            .setSubtitleTextColor(Color.GRAY)
            .build()

        style.subtitleTextColor shouldBe Color.GRAY

        println("    ✅ Builder set subtitleTextColor=GRAY")
    }

    @Test
    fun `builder should set end call icon tint`() {
        println("  🧪 builder should set end call icon tint")

        val style = CometChatOutgoingCallStyle.builder(context)
            .setEndCallIconTint(Color.CYAN)
            .build()

        style.endCallIconTint shouldBe Color.CYAN

        println("    ✅ Builder set endCallIconTint=CYAN")
    }

    @Test
    fun `builder should set end call button background color`() {
        println("  🧪 builder should set end call button background color")

        val style = CometChatOutgoingCallStyle.builder(context)
            .setEndCallButtonBackgroundColor(Color.MAGENTA)
            .build()

        style.endCallButtonBackgroundColor shouldBe Color.MAGENTA

        println("    ✅ Builder set endCallButtonBackgroundColor=MAGENTA")
    }

    // ==================== Data Class Equality ====================

    @Test
    fun `two styles with same properties should be equal`() {
        println("  🧪 two styles with same properties should be equal")

        val style1 = CometChatOutgoingCallStyle.builder(context)
            .setBackgroundColor(Color.RED)
            .setCornerRadius(8f)
            .build()

        val style2 = CometChatOutgoingCallStyle.builder(context)
            .setBackgroundColor(Color.RED)
            .setCornerRadius(8f)
            .build()

        style1 shouldBe style2

        println("    ✅ Styles with same properties are equal")
    }

    @Test
    fun `two styles with different properties should not be equal`() {
        println("  🧪 two styles with different properties should not be equal")

        val style1 = CometChatOutgoingCallStyle.builder(context)
            .setBackgroundColor(Color.RED)
            .build()

        val style2 = CometChatOutgoingCallStyle.builder(context)
            .setBackgroundColor(Color.BLUE)
            .build()

        (style1 == style2) shouldBe false

        println("    ✅ Styles with different properties are not equal")
    }

    // ==================== Copy Behavior ====================

    @Test
    fun `copy should create independent instance with modified property`() {
        println("  🧪 copy should create independent instance with modified property")

        val original = CometChatOutgoingCallStyle.builder(context)
            .setBackgroundColor(Color.RED)
            .setCornerRadius(8f)
            .setTitleTextColor(Color.WHITE)
            .build()

        val copied = original.copy(backgroundColor = Color.BLUE)

        copied.backgroundColor shouldBe Color.BLUE
        copied.cornerRadius shouldBe 8f
        copied.titleTextColor shouldBe Color.WHITE

        // Original unchanged
        original.backgroundColor shouldBe Color.RED

        println("    ✅ Copy creates independent instance, original unchanged")
    }

    // ==================== All Properties Accessible ====================

    @Test
    fun `all style properties should be accessible`() {
        println("  🧪 all style properties should be accessible")

        val style = CometChatOutgoingCallStyle.builder(context)
            .setBackgroundColor(Color.parseColor("#1A1A2E"))
            .setCornerRadius(12f)
            .setStrokeWidth(1)
            .setStrokeColor(Color.parseColor("#333333"))
            .setTitleTextColor(Color.WHITE)
            .setSubtitleTextColor(Color.parseColor("#AAAAAA"))
            .setEndCallIconTint(Color.parseColor("#FFFFFF"))
            .setEndCallButtonBackgroundColor(Color.parseColor("#F44336"))
            .build()

        // Verify all properties are accessible
        style.backgroundColor shouldBe Color.parseColor("#1A1A2E")
        style.cornerRadius shouldBe 12f
        style.strokeWidth shouldBe 1
        style.strokeColor shouldBe Color.parseColor("#333333")
        style.titleTextColor shouldBe Color.WHITE
        style.subtitleTextColor shouldBe Color.parseColor("#AAAAAA")
        style.endCallIconTint shouldBe Color.parseColor("#FFFFFF")
        style.endCallButtonBackgroundColor shouldBe Color.parseColor("#F44336")

        println("    ✅ All style properties accessible and correct")
    }
}
