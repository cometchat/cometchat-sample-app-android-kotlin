package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.actionbubble

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Preservation Property Tests for Action Bubble Style Fix.
 *
 * These tests verify that existing functionality is preserved and will NOT be
 * affected by the fix. They establish a baseline of correct behavior that must
 * continue to work after the fix is applied.
 *
 * **Property 2: Preservation** - Style Property Access and Default Values
 *
 * **Key Observations on UNFIXED code:**
 * - `CometChatActionBubbleStyle.default(context)` returns a style with theme-based default values
 * - `CometChatActionBubbleStyle.fromTypedArray(context, typedArray)` extracts action-bubble-specific properties
 * - Default values use `CometChatTheme.getTextColorSecondary()` for textColor
 * - Default values use `CometChatTheme.getBackgroundColor2()` for backgroundColor
 *
 * **EXPECTED OUTCOME:** Tests PASS on both UNFIXED and FIXED code (confirms baseline behavior to preserve)
 *
 * **Validates: Requirements 3.1, 3.2, 3.3, 3.4**
 *
 * Feature: action-bubble-style-fix
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
class ActionBubbleStylePreservationTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    // ==================== Preservation: Default Style Values ====================

    /**
     * Property 2: Preservation - Default textColor Uses Theme Secondary Text Color
     *
     * Verifies that `default(context)` returns a style with textColor equal to
     * `CometChatTheme.getTextColorSecondary(context)`.
     *
     * This behavior must be preserved after the fix.
     *
     * **Validates: Requirement 3.3**
     */
    @Test
    fun `default style should have textColor equal to CometChatTheme getTextColorSecondary`() {
        val expectedTextColor = CometChatTheme.getTextColorSecondary(context)

        val defaultStyle = CometChatActionBubbleStyle.default(context)

        assertEquals(
            "default() textColor should equal CometChatTheme.getTextColorSecondary()",
            expectedTextColor,
            defaultStyle.textColor
        )
    }

    /**
     * Property 2: Preservation - Default backgroundColor Uses Theme Background Color 2
     *
     * Verifies that `default(context)` returns a style with backgroundColor equal to
     * `CometChatTheme.getBackgroundColor2(context)`.
     *
     * This behavior must be preserved after the fix.
     *
     * **Validates: Requirement 3.3**
     */
    @Test
    fun `default style should have backgroundColor equal to CometChatTheme getBackgroundColor2`() {
        val expectedBackgroundColor = CometChatTheme.getBackgroundColor2(context)

        val defaultStyle = CometChatActionBubbleStyle.default(context)

        assertEquals(
            "default() backgroundColor should equal CometChatTheme.getBackgroundColor2()",
            expectedBackgroundColor,
            defaultStyle.backgroundColor
        )
    }

    /**
     * Property 2: Preservation - Default textAppearance Uses Theme Caption1Regular
     *
     * Verifies that `default(context)` returns a style with textAppearance equal to
     * `CometChatTheme.getTextAppearanceCaption1Regular(context)`.
     *
     * This behavior must be preserved after the fix.
     *
     * **Validates: Requirement 3.3**
     */
    @Test
    fun `default style should have textAppearance equal to CometChatTheme getTextAppearanceCaption1Regular`() {
        val expectedTextAppearance = CometChatTheme.getTextAppearanceCaption1Regular(context)

        val defaultStyle = CometChatActionBubbleStyle.default(context)

        assertEquals(
            "default() textAppearance should equal CometChatTheme.getTextAppearanceCaption1Regular()",
            expectedTextAppearance,
            defaultStyle.textAppearance
        )
    }

    /**
     * Property 2: Preservation - Default backgroundDrawable Is Null
     *
     * Verifies that `default(context)` returns a style with backgroundDrawable as null
     * (no custom drawable by default).
     *
     * This behavior must be preserved after the fix.
     *
     * **Validates: Requirement 3.3**
     */
    @Test
    fun `default style should have null backgroundDrawable`() {
        val defaultStyle = CometChatActionBubbleStyle.default(context)

        assertNull(
            "default() backgroundDrawable should be null",
            defaultStyle.backgroundDrawable
        )
    }

    // ==================== Preservation: Action-Bubble-Specific Properties ====================

    /**
     * Property 2: Preservation - Style Has textColor Property
     *
     * Verifies that `CometChatActionBubbleStyle` has a textColor property that is accessible.
     *
     * **Validates: Requirement 3.1**
     */
    @Test
    fun `style should have accessible textColor property`() {
        val style = CometChatActionBubbleStyle.default(context)

        // textColor should be accessible and return a valid color value
        assertNotNull("textColor property should be accessible", style.textColor)
    }

    /**
     * Property 2: Preservation - Style Has textAppearance Property
     *
     * Verifies that `CometChatActionBubbleStyle` has a textAppearance property that is accessible.
     *
     * **Validates: Requirement 3.1**
     */
    @Test
    fun `style should have accessible textAppearance property`() {
        val style = CometChatActionBubbleStyle.default(context)

        // textAppearance should be accessible and return a valid resource ID
        assertNotNull("textAppearance property should be accessible", style.textAppearance)
    }

    /**
     * Property 2: Preservation - Style Has backgroundColor Property
     *
     * Verifies that `CometChatActionBubbleStyle` has a backgroundColor property that is accessible.
     *
     * **Validates: Requirement 3.1**
     */
    @Test
    fun `style should have accessible backgroundColor property`() {
        val style = CometChatActionBubbleStyle.default(context)

        // backgroundColor should be accessible and return a valid color value
        assertNotNull("backgroundColor property should be accessible", style.backgroundColor)
    }

    /**
     * Property 2: Preservation - Style Has cornerRadius Property
     *
     * Verifies that `CometChatActionBubbleStyle` has a cornerRadius property that is accessible.
     *
     * **Validates: Requirement 3.1**
     */
    @Test
    fun `style should have accessible cornerRadius property`() {
        val style = CometChatActionBubbleStyle.default(context)

        // cornerRadius should be accessible
        assertNotNull("cornerRadius property should be accessible", style.cornerRadius)
    }

    /**
     * Property 2: Preservation - Style Has strokeWidth Property
     *
     * Verifies that `CometChatActionBubbleStyle` has a strokeWidth property that is accessible.
     *
     * **Validates: Requirement 3.1**
     */
    @Test
    fun `style should have accessible strokeWidth property`() {
        val style = CometChatActionBubbleStyle.default(context)

        // strokeWidth should be accessible
        assertNotNull("strokeWidth property should be accessible", style.strokeWidth)
    }

    /**
     * Property 2: Preservation - Style Has strokeColor Property
     *
     * Verifies that `CometChatActionBubbleStyle` has a strokeColor property that is accessible.
     *
     * **Validates: Requirement 3.1**
     */
    @Test
    fun `style should have accessible strokeColor property`() {
        val style = CometChatActionBubbleStyle.default(context)

        // strokeColor should be accessible
        assertNotNull("strokeColor property should be accessible", style.strokeColor)
    }

    // ==================== Preservation: Explicit Property Values ====================

    /**
     * Property 2: Preservation - Explicit Property Values Are Stored
     *
     * Verifies that when `CometChatActionBubbleStyle` is created with explicit property values,
     * those values are stored and accessible.
     *
     * **Validates: Requirement 3.1**
     */
    @Test
    fun `style created with explicit values should store those values`() {
        val explicitTextColor = 0xFF112233.toInt()
        val explicitBackgroundColor = 0xFF445566.toInt()
        val explicitCornerRadius = 16f
        val explicitStrokeWidth = 2f
        val explicitStrokeColor = 0xFF778899.toInt()

        val style = CometChatActionBubbleStyle(
            textColor = explicitTextColor,
            backgroundColor = explicitBackgroundColor,
            cornerRadius = explicitCornerRadius,
            strokeWidth = explicitStrokeWidth,
            strokeColor = explicitStrokeColor
        )

        assertEquals("textColor should match explicit value", explicitTextColor, style.textColor)
        assertEquals("backgroundColor should match explicit value", explicitBackgroundColor, style.backgroundColor)
        assertEquals("cornerRadius should match explicit value", explicitCornerRadius, style.cornerRadius, 0.001f)
        assertEquals("strokeWidth should match explicit value", explicitStrokeWidth, style.strokeWidth, 0.001f)
        assertEquals("strokeColor should match explicit value", explicitStrokeColor, style.strokeColor)
    }

    // ==================== Preservation: Style Consistency ====================

    /**
     * Property 2: Preservation - Multiple Calls to default() Return Consistent Values
     *
     * Verifies that calling `default(context)` multiple times returns styles with
     * consistent property values.
     *
     * **Validates: Requirement 3.3**
     */
    @Test
    fun `multiple calls to default should return consistent values`() {
        val style1 = CometChatActionBubbleStyle.default(context)
        val style2 = CometChatActionBubbleStyle.default(context)

        assertEquals("textColor should be consistent", style1.textColor, style2.textColor)
        assertEquals("backgroundColor should be consistent", style1.backgroundColor, style2.backgroundColor)
        assertEquals("textAppearance should be consistent", style1.textAppearance, style2.textAppearance)
        assertEquals("cornerRadius should be consistent", style1.cornerRadius, style2.cornerRadius, 0.001f)
        assertEquals("strokeWidth should be consistent", style1.strokeWidth, style2.strokeWidth, 0.001f)
        assertEquals("strokeColor should be consistent", style1.strokeColor, style2.strokeColor)
    }

    /**
     * Property 2: Preservation - Default Values Match Theme Values
     *
     * Verifies that all default values are derived from CometChatTheme.
     * This ensures the style integrates properly with the theming system.
     *
     * **Validates: Requirement 3.3, 3.4**
     */
    @Test
    fun `default style values should match CometChatTheme values`() {
        val defaultStyle = CometChatActionBubbleStyle.default(context)

        // Content-specific properties should use theme defaults
        assertEquals(
            "textColor should match theme secondary text color",
            CometChatTheme.getTextColorSecondary(context),
            defaultStyle.textColor
        )
        assertEquals(
            "textAppearance should match theme caption1 regular",
            CometChatTheme.getTextAppearanceCaption1Regular(context),
            defaultStyle.textAppearance
        )
        assertEquals(
            "backgroundColor should match theme background color 2",
            CometChatTheme.getBackgroundColor2(context),
            defaultStyle.backgroundColor
        )
    }

    // ==================== Preservation: fromTypedArray Method ====================

    /**
     * Property 2: Preservation - fromTypedArray Method Exists
     *
     * Verifies that the `fromTypedArray(context, typedArray)` method exists and is callable.
     * This method is used to extract style properties from XML attributes.
     *
     * **Validates: Requirement 3.2**
     */
    @Test
    fun `fromTypedArray method should exist on companion object`() {
        val companionClass = CometChatActionBubbleStyle.Companion::class.java
        val methods = companionClass.declaredMethods.map { it.name }

        assert(methods.contains("fromTypedArray")) {
            "fromTypedArray method should exist on companion object. Found methods: $methods"
        }
    }

    /**
     * Property 2: Preservation - Style Provides All Necessary Properties for Rendering
     *
     * Verifies that the style provides all properties needed by CometChatActionBubble
     * component for rendering: textColor, textAppearance, backgroundColor,
     * backgroundDrawable, cornerRadius, strokeWidth, strokeColor.
     *
     * **Validates: Requirement 3.4**
     */
    @Test
    fun `style should provide all necessary properties for rendering action bubble`() {
        val style = CometChatActionBubbleStyle.default(context)

        // All action-bubble-specific properties should be present
        // These are the properties needed by CometChatActionBubble for rendering
        assertNotNull("textColor should be present", style.textColor)
        assertNotNull("textAppearance should be present", style.textAppearance)
        assertNotNull("backgroundColor should be present", style.backgroundColor)
        // backgroundDrawable can be null (optional)
        assertNotNull("cornerRadius should be present", style.cornerRadius)
        assertNotNull("strokeWidth should be present", style.strokeWidth)
        assertNotNull("strokeColor should be present", style.strokeColor)
    }

    // ==================== Preservation: Data Class Behavior ====================

    /**
     * Property 2: Preservation - Style Is a Data Class with Copy Support
     *
     * Verifies that `CometChatActionBubbleStyle` is a data class that supports
     * the `copy()` method for creating modified copies.
     *
     * **Validates: Requirement 3.1**
     */
    @Test
    fun `style should support copy method for creating modified copies`() {
        val originalStyle = CometChatActionBubbleStyle.default(context)
        val newTextColor = 0xFFAABBCC.toInt()

        val copiedStyle = originalStyle.copy(textColor = newTextColor)

        // The copied style should have the new textColor
        assertEquals("copied style should have new textColor", newTextColor, copiedStyle.textColor)
        // Other properties should remain unchanged
        assertEquals("backgroundColor should remain unchanged", originalStyle.backgroundColor, copiedStyle.backgroundColor)
        assertEquals("textAppearance should remain unchanged", originalStyle.textAppearance, copiedStyle.textAppearance)
    }

    /**
     * Property 2: Preservation - Style Equality Works Correctly
     *
     * Verifies that two styles with the same property values are considered equal.
     *
     * **Validates: Requirement 3.1**
     */
    @Test
    fun `styles with same values should be equal`() {
        val style1 = CometChatActionBubbleStyle(
            textColor = 0xFF112233.toInt(),
            textAppearance = 0,
            backgroundColor = 0xFF445566.toInt(),
            backgroundDrawable = null,
            cornerRadius = 8f,
            strokeWidth = 1f,
            strokeColor = 0xFF778899.toInt()
        )
        val style2 = CometChatActionBubbleStyle(
            textColor = 0xFF112233.toInt(),
            textAppearance = 0,
            backgroundColor = 0xFF445566.toInt(),
            backgroundDrawable = null,
            cornerRadius = 8f,
            strokeWidth = 1f,
            strokeColor = 0xFF778899.toInt()
        )

        assertEquals("styles with same values should be equal", style1, style2)
    }
}
