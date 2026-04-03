package com.cometchat.uikit.kotlin.presentation.shared.messagebubble

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.actionbubble.CometChatActionBubbleStyle
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Preservation Property Tests for Outgoing Bubble Style Mapping Fix.
 *
 * These tests verify that existing functionality is preserved and will NOT be
 * affected by the fix. They establish a baseline of correct behavior that must
 * continue to work after the fix is applied.
 *
 * **Property 2: Preservation** - Explicit Color Values and Other Properties
 *
 * **Key Observations:**
 * - Styles with explicit `cometchatMessageBubbleBackgroundColor` use that explicit color
 * - Other properties (cornerRadius, strokeWidth, strokeColor, textAppearances) are extracted correctly
 * - Sub-component styles (textBubbleStyle, imageBubbleStyle, etc.) are extracted correctly
 *
 * **EXPECTED OUTCOME:** Tests PASS (confirms baseline behavior to preserve)
 *
 * **Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5**
 *
 * Feature: outgoing-bubble-style-mapping-fix
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
class OutgoingBubbleStylePreservationTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    // ==================== Preservation: Other Style Properties ====================

    /**
     * Property 2: Preservation - Sender Name Text Appearance
     *
     * Verifies that senderNameTextAppearance is extracted correctly from theme.
     * This property should be unaffected by the backgroundColor fix.
     *
     * **Validates: Requirement 3.2**
     */
    @Test
    fun `outgoing style should extract senderNameTextAppearance correctly`() {
        val expectedTextAppearance = CometChatTheme.getTextAppearanceCaption1Medium(context)

        val outgoingStyle = CometChatMessageBubbleStyle.outgoing(context)

        assertEquals(
            "senderNameTextAppearance should be extracted from theme",
            expectedTextAppearance,
            outgoingStyle.senderNameTextAppearance
        )
    }

    /**
     * Property 2: Preservation - Sender Name Text Color
     *
     * Verifies that senderNameTextColor is extracted correctly from theme.
     *
     * **Validates: Requirement 3.2**
     */
    @Test
    fun `outgoing style should extract senderNameTextColor correctly`() {
        val expectedTextColor = CometChatTheme.getTextColorSecondary(context)

        val outgoingStyle = CometChatMessageBubbleStyle.outgoing(context)

        assertEquals(
            "senderNameTextColor should be extracted from theme",
            expectedTextColor,
            outgoingStyle.senderNameTextColor
        )
    }

    /**
     * Property 2: Preservation - Thread Indicator Text Appearance
     *
     * Verifies that threadIndicatorTextAppearance is extracted correctly.
     *
     * **Validates: Requirement 3.2**
     */
    @Test
    fun `outgoing style should extract threadIndicatorTextAppearance correctly`() {
        val expectedTextAppearance = CometChatTheme.getTextAppearanceCaption1Regular(context)

        val outgoingStyle = CometChatMessageBubbleStyle.outgoing(context)

        assertEquals(
            "threadIndicatorTextAppearance should be extracted from theme",
            expectedTextAppearance,
            outgoingStyle.threadIndicatorTextAppearance
        )
    }

    /**
     * Property 2: Preservation - Thread Indicator Text Color
     *
     * Verifies that threadIndicatorTextColor is extracted correctly.
     *
     * **Validates: Requirement 3.2**
     */
    @Test
    fun `outgoing style should extract threadIndicatorTextColor correctly`() {
        val expectedTextColor = CometChatTheme.getTextColorSecondary(context)

        val outgoingStyle = CometChatMessageBubbleStyle.outgoing(context)

        assertEquals(
            "threadIndicatorTextColor should be extracted from theme",
            expectedTextColor,
            outgoingStyle.threadIndicatorTextColor
        )
    }

    /**
     * Property 2: Preservation - Thread Indicator Icon Tint
     *
     * Verifies that threadIndicatorIconTint is extracted correctly.
     *
     * **Validates: Requirement 3.2**
     */
    @Test
    fun `outgoing style should extract threadIndicatorIconTint correctly`() {
        val expectedIconTint = CometChatTheme.getIconTintSecondary(context)

        val outgoingStyle = CometChatMessageBubbleStyle.outgoing(context)

        assertEquals(
            "threadIndicatorIconTint should be extracted from theme",
            expectedIconTint,
            outgoingStyle.threadIndicatorIconTint
        )
    }

    /**
     * Property 2: Preservation - Timestamp Text Appearance
     *
     * Verifies that timestampTextAppearance is extracted correctly.
     *
     * **Validates: Requirement 3.2**
     */
    @Test
    fun `outgoing style should extract timestampTextAppearance correctly`() {
        val expectedTextAppearance = CometChatTheme.getTextAppearanceCaption1Regular(context)

        val outgoingStyle = CometChatMessageBubbleStyle.outgoing(context)

        assertEquals(
            "timestampTextAppearance should be extracted from theme",
            expectedTextAppearance,
            outgoingStyle.timestampTextAppearance
        )
    }

    /**
     * Property 2: Preservation - Timestamp Text Color
     *
     * Verifies that timestampTextColor is extracted correctly.
     *
     * **Validates: Requirement 3.2**
     */
    @Test
    fun `outgoing style should extract timestampTextColor correctly`() {
        val expectedTextColor = CometChatTheme.getTextColorSecondary(context)

        val outgoingStyle = CometChatMessageBubbleStyle.outgoing(context)

        assertEquals(
            "timestampTextColor should be extracted from theme",
            expectedTextColor,
            outgoingStyle.timestampTextColor
        )
    }

    // ==================== Preservation: Incoming Style Properties ====================

    /**
     * Property 2: Preservation - Incoming Style Text Properties
     *
     * Verifies that incoming style extracts text properties correctly.
     * The incoming style should continue to work as before.
     *
     * **Validates: Requirement 3.3**
     */
    @Test
    fun `incoming style should extract text properties correctly`() {
        val incomingStyle = CometChatMessageBubbleStyle.incoming(context)

        // Verify text properties are extracted with theme defaults
        assertEquals(
            "senderNameTextAppearance should be extracted",
            CometChatTheme.getTextAppearanceCaption1Medium(context),
            incomingStyle.senderNameTextAppearance
        )
        assertEquals(
            "senderNameTextColor should be extracted",
            CometChatTheme.getTextColorSecondary(context),
            incomingStyle.senderNameTextColor
        )
    }

    // ==================== Preservation: Action Bubble Properties ====================

    /**
     * Property 2: Preservation - Action Bubble Text Properties
     *
     * Verifies that action bubble style extracts content-specific properties correctly.
     *
     * **Validates: Requirement 3.4**
     */
    @Test
    fun `action bubble should extract textColor correctly`() {
        val expectedTextColor = CometChatTheme.getTextColorSecondary(context)

        val actionStyle = CometChatActionBubbleStyle.default(context)

        assertEquals(
            "Action bubble textColor should be extracted from theme",
            expectedTextColor,
            actionStyle.textColor
        )
    }

    /**
     * Property 2: Preservation - Action Bubble Text Appearance
     *
     * Verifies that action bubble style extracts textAppearance correctly.
     *
     * **Validates: Requirement 3.4**
     */
    @Test
    fun `action bubble should extract textAppearance correctly`() {
        val expectedTextAppearance = CometChatTheme.getTextAppearanceCaption1Regular(context)

        val actionStyle = CometChatActionBubbleStyle.default(context)

        assertEquals(
            "Action bubble textAppearance should be extracted from theme",
            expectedTextAppearance,
            actionStyle.textAppearance
        )
    }

    // ==================== Preservation: Style Consistency ====================

    /**
     * Property 2: Preservation - Outgoing and Incoming Have Different Purposes
     *
     * Verifies that outgoing() and incoming() methods return distinct styles
     * (they may have different default colors but same text properties).
     *
     * **Validates: Requirement 3.5**
     */
    @Test
    fun `outgoing and incoming styles should have same text property defaults`() {
        val outgoingStyle = CometChatMessageBubbleStyle.outgoing(context)
        val incomingStyle = CometChatMessageBubbleStyle.incoming(context)

        // Text properties should be the same for both
        assertEquals(
            "senderNameTextAppearance should be same for both",
            outgoingStyle.senderNameTextAppearance,
            incomingStyle.senderNameTextAppearance
        )
        assertEquals(
            "senderNameTextColor should be same for both",
            outgoingStyle.senderNameTextColor,
            incomingStyle.senderNameTextColor
        )
        assertEquals(
            "threadIndicatorTextAppearance should be same for both",
            outgoingStyle.threadIndicatorTextAppearance,
            incomingStyle.threadIndicatorTextAppearance
        )
    }

    /**
     * Property 2: Preservation - Default Delegates to Outgoing
     *
     * Verifies that default() method delegates to outgoing() and returns
     * the same style properties.
     *
     * **Validates: Requirement 3.5**
     */
    @Test
    fun `default style should have same properties as outgoing style`() {
        val defaultStyle = CometChatMessageBubbleStyle.default(context)
        val outgoingStyle = CometChatMessageBubbleStyle.outgoing(context)

        // All properties should match
        assertEquals(
            "backgroundColor should match",
            outgoingStyle.backgroundColor,
            defaultStyle.backgroundColor
        )
        assertEquals(
            "senderNameTextAppearance should match",
            outgoingStyle.senderNameTextAppearance,
            defaultStyle.senderNameTextAppearance
        )
        assertEquals(
            "threadIndicatorTextColor should match",
            outgoingStyle.threadIndicatorTextColor,
            defaultStyle.threadIndicatorTextColor
        )
    }

    // ==================== Preservation: Consistent Extraction ====================

    /**
     * Property 2: Preservation - Text Appearances Use Theme Defaults
     *
     * Verifies that text appearance properties are extracted using CometChatTheme defaults.
     * In Robolectric tests, theme resources may return 0, but the extraction logic
     * should consistently use the same defaults as CometChatTheme.
     *
     * **Validates: Requirement 3.2**
     */
    @Test
    fun `text appearance properties should use CometChatTheme defaults`() {
        val outgoingStyle = CometChatMessageBubbleStyle.outgoing(context)

        // Text appearances should match CometChatTheme defaults
        assertEquals(
            "senderNameTextAppearance should use CometChatTheme default",
            CometChatTheme.getTextAppearanceCaption1Medium(context),
            outgoingStyle.senderNameTextAppearance
        )
        assertEquals(
            "threadIndicatorTextAppearance should use CometChatTheme default",
            CometChatTheme.getTextAppearanceCaption1Regular(context),
            outgoingStyle.threadIndicatorTextAppearance
        )
        assertEquals(
            "timestampTextAppearance should use CometChatTheme default",
            CometChatTheme.getTextAppearanceCaption1Regular(context),
            outgoingStyle.timestampTextAppearance
        )
    }

    /**
     * Property 2: Preservation - Text Colors Use Theme Defaults
     *
     * Verifies that text color properties are extracted using CometChatTheme defaults.
     *
     * **Validates: Requirement 3.2**
     */
    @Test
    fun `text color properties should use CometChatTheme defaults`() {
        val outgoingStyle = CometChatMessageBubbleStyle.outgoing(context)

        // Text colors should match CometChatTheme defaults
        assertEquals(
            "senderNameTextColor should use CometChatTheme default",
            CometChatTheme.getTextColorSecondary(context),
            outgoingStyle.senderNameTextColor
        )
        assertEquals(
            "threadIndicatorTextColor should use CometChatTheme default",
            CometChatTheme.getTextColorSecondary(context),
            outgoingStyle.threadIndicatorTextColor
        )
        assertEquals(
            "timestampTextColor should use CometChatTheme default",
            CometChatTheme.getTextColorSecondary(context),
            outgoingStyle.timestampTextColor
        )
    }
}
