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
 * Bug Condition Exploration Test for Outgoing Bubble Style Mapping Fix.
 *
 * This test verifies that the default background colors for message bubble styles
 * are correctly mapped according to the Java reference implementation:
 * - Outgoing message bubble: should use `CometChatTheme.getPrimaryColor(context)`
 * - Action bubble: should use `CometChatTheme.getBackgroundColor2(context)`
 *
 * **Bug Context:**
 * The current Kotlin implementation has incorrect default color mappings:
 * - Outgoing message bubble uses `getBackgroundColor3()` instead of `getPrimaryColor()`
 * - Action bubble uses `STYLE_NOT_SET` sentinel value instead of `getBackgroundColor2()`
 *
 * This causes outgoing bubbles to appear with a neutral gray background instead of
 * the primary color, and action bubbles to have incorrect fallback behavior.
 *
 * **EXPECTED OUTCOME ON UNFIXED CODE:** Test FAILS
 * - Outgoing bubble: returns backgroundColor3 instead of primaryColor
 * - Action bubble: returns STYLE_NOT_SET instead of backgroundColor2
 *
 * **Validates: Requirements 1.1, 1.2, 1.3**
 *
 * Feature: outgoing-bubble-style-mapping-fix
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
class OutgoingBubbleStyleExplorationTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    // ==================== Outgoing Message Bubble Tests ====================

    /**
     * Property 1: Fault Condition - Outgoing Bubble Default Color Mapping
     *
     * When extracting outgoing message bubble style from theme without an explicit
     * `cometchatMessageBubbleBackgroundColor` value, the system SHALL use
     * `CometChatTheme.getPrimaryColor(context)` as the default backgroundColor.
     *
     * **Validates: Requirements 1.1, 1.3, 2.1**
     */
    @Test
    fun `outgoing() should return style with backgroundColor equal to primaryColor`() {
        // Expected: Outgoing bubble backgroundColor = CometChatTheme.getPrimaryColor(context)
        // This is the correct behavior per Java reference implementation
        val expectedColor = CometChatTheme.getPrimaryColor(context)

        // Actual: Extract outgoing style without explicit background color
        val outgoingStyle = CometChatMessageBubbleStyle.outgoing(context)
        val actualColor = outgoingStyle.backgroundColor

        // This assertion will FAIL on unfixed code because:
        // - Current implementation uses: CometChatTheme.getBackgroundColor3(context)
        // - Expected value: CometChatTheme.getPrimaryColor(context)
        // - Result: Outgoing bubbles appear gray instead of primary color (e.g., purple/blue)
        //
        // Counterexample on failure:
        // - Expected: primaryColor (e.g., #6851D6 - purple)
        // - Actual: backgroundColor3 (e.g., #E8E8E8 - gray)
        assertEquals(
            "Outgoing bubble backgroundColor should be primaryColor",
            expectedColor,
            actualColor
        )
    }

    @Test
    fun `default() should return style with backgroundColor equal to primaryColor`() {
        // default() delegates to outgoing(), so it should also use primaryColor
        val expectedColor = CometChatTheme.getPrimaryColor(context)

        val defaultStyle = CometChatMessageBubbleStyle.default(context)
        val actualColor = defaultStyle.backgroundColor

        // This assertion will FAIL on unfixed code for the same reason as above
        assertEquals(
            "Default bubble backgroundColor should be primaryColor (delegates to outgoing)",
            expectedColor,
            actualColor
        )
    }

    // ==================== Action Bubble Tests ====================

    /**
     * Property 1: Fault Condition - Action Bubble Default Color Mapping
     *
     * When extracting action bubble style from theme without an explicit
     * `cometchatActionBubbleBackgroundColor` value, the system SHALL use
     * `CometChatTheme.getBackgroundColor2(context)` as the default backgroundColor.
     *
     * **Validates: Requirements 1.2, 2.3**
     */
    @Test
    fun `action default() should return style with backgroundColor equal to backgroundColor2`() {
        // Expected: Action bubble backgroundColor = CometChatTheme.getBackgroundColor2(context)
        // This is the correct behavior per Java reference implementation
        val expectedColor = CometChatTheme.getBackgroundColor2(context)

        // Actual: Extract action bubble style without explicit background color
        val actionStyle = CometChatActionBubbleStyle.default(context)
        val actualColor = actionStyle.backgroundColor

        // This assertion will FAIL on unfixed code because:
        // - Current implementation uses: STYLE_NOT_SET (Int.MIN_VALUE)
        // - Expected value: CometChatTheme.getBackgroundColor2(context)
        // - Result: Action bubbles rely on incorrect fallback resolution
        //
        // Counterexample on failure:
        // - Expected: backgroundColor2 (e.g., #F5F5F5 - light gray)
        // - Actual: STYLE_NOT_SET (Int.MIN_VALUE = -2147483648)
        assertEquals(
            "Action bubble backgroundColor should be backgroundColor2",
            expectedColor,
            actualColor
        )
    }

    // ==================== Counterexample Documentation Tests ====================

    @Test
    fun `outgoing bubble backgroundColor should NOT be backgroundColor3`() {
        // This test documents the bug: outgoing bubble incorrectly uses backgroundColor3
        val correctColor = CometChatTheme.getPrimaryColor(context)

        val outgoingStyle = CometChatMessageBubbleStyle.outgoing(context)

        // On unfixed code: actualColor == incorrectColor (backgroundColor3)
        // On fixed code: actualColor == correctColor (primaryColor)
        assertEquals(
            "Outgoing bubble should use primaryColor, not backgroundColor3",
            correctColor,
            outgoingStyle.backgroundColor
        )
    }

    @Test
    fun `action bubble backgroundColor should NOT be STYLE_NOT_SET sentinel value`() {
        // This test documents the bug: action bubble incorrectly uses STYLE_NOT_SET
        val correctColor = CometChatTheme.getBackgroundColor2(context)

        val actionStyle = CometChatActionBubbleStyle.default(context)

        // On unfixed code: actualColor == STYLE_NOT_SET (Int.MIN_VALUE)
        // On fixed code: actualColor == correctColor (backgroundColor2)
        assertEquals(
            "Action bubble should use backgroundColor2, not STYLE_NOT_SET",
            correctColor,
            actionStyle.backgroundColor
        )
    }
}
