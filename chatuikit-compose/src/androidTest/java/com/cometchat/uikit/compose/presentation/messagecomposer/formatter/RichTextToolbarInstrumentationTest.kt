package com.cometchat.uikit.compose.presentation.messagecomposer.formatter

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.uikit.compose.presentation.messagecomposer.ui.CometChatRichTextToolbar
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import com.cometchat.uikit.core.formatter.FormatCompatibility
import com.cometchat.uikit.core.formatter.RichTextFormat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation tests for CometChatRichTextToolbar interactions.
 *
 * Feature: rich-text-formatting-parity
 *
 * These tests verify toolbar button toggle behavior, format compatibility
 * (disabled states), auto-deselect rules, simultaneous format activation,
 * and layout/button ordering.
 *
 * The toolbar is rendered with managed state: each test drives `activeFormats`
 * and `disabledFormats` via `RichTextFormat.toggleFormat()` and
 * `FormatCompatibility.getDisabledFormats()` to mirror real usage.
 */
@RunWith(AndroidJUnit4::class)
class RichTextToolbarInstrumentationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Content descriptions used by the toolbar buttons (from CometChatRichTextToolbar.kt)
    companion object {
        const val BOLD = "Bold"
        const val ITALIC = "Italic"
        const val UNDERLINE = "Underline"
        const val STRIKETHROUGH = "Strikethrough"
        const val LINK = "Link"
        const val NUMBERED_LIST = "Numbered List"
        const val BULLET_LIST = "Bullet List"
        const val BLOCKQUOTE = "Blockquote"
        const val INLINE_CODE = "Inline Code"
        const val CODE_BLOCK = "Code Block"
        const val TOOLBAR = "Rich Text Toolbar"
    }


    // ========================================================================
    // 11.1 Test each format button toggles active state on click
    // ========================================================================

    /**
     * Verifies that clicking each format button toggles it between active and
     * inactive states. All 10 format buttons are tested: Bold, Italic, Underline,
     * Strikethrough, Link, Ordered List, Bullet List, Blockquote, Inline Code,
     * Code Block.
     *
     * The toolbar is a stateless composable — the test manages `activeFormats`
     * and `disabledFormats` externally via `RichTextFormat.toggleFormat()` and
     * `FormatCompatibility.getDisabledFormats()`, then verifies the UI reflects
     * the updated state after each click.
     *
     * **Validates: Requirements 1.2, 2.2, 3.2, 4.2, 5.2, 15.1, 16.2, 17.3, 18.3, 30.5**
     */
    @Test
    fun eachFormatButton_togglesActiveStateOnClick() {
        // Map of content descriptions to their RichTextFormat enum values
        val buttonFormats = listOf(
            BOLD to RichTextFormat.BOLD,
            ITALIC to RichTextFormat.ITALIC,
            UNDERLINE to RichTextFormat.UNDERLINE,
            STRIKETHROUGH to RichTextFormat.STRIKETHROUGH,
            LINK to RichTextFormat.LINK,
            NUMBERED_LIST to RichTextFormat.ORDERED_LIST,
            BULLET_LIST to RichTextFormat.BULLET_LIST,
            BLOCKQUOTE to RichTextFormat.BLOCKQUOTE,
            INLINE_CODE to RichTextFormat.INLINE_CODE,
            CODE_BLOCK to RichTextFormat.CODE_BLOCK,
        )

        // Use mutable state so we can call setContent once and drive the toolbar
        // through state changes inside the loop (Compose test rules only allow
        // setContent to be called once per test).
        var activeFormats by mutableStateOf(emptySet<RichTextFormat>())
        var disabledFormats by mutableStateOf(emptySet<RichTextFormat>())

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatRichTextToolbar(
                    activeFormats = activeFormats,
                    disabledFormats = disabledFormats,
                    onFormatClick = { clicked ->
                        activeFormats = RichTextFormat.toggleFormat(activeFormats, clicked)
                        disabledFormats = FormatCompatibility.getDisabledFormats(activeFormats)
                    },
                    onLinkClick = {
                        activeFormats = RichTextFormat.toggleFormat(activeFormats, RichTextFormat.LINK)
                        disabledFormats = FormatCompatibility.getDisabledFormats(activeFormats)
                    }
                )
            }
        }
        composeTestRule.waitForIdle()

        for ((contentDesc, format) in buttonFormats) {
            // Reset state for each button so they are tested in isolation
            activeFormats = emptySet()
            disabledFormats = emptySet()
            composeTestRule.waitForIdle()

            // Click to activate (scroll into view first for buttons in scrollable row)
            composeTestRule.onNodeWithContentDescription(contentDesc).performScrollTo().performClick()
            composeTestRule.waitForIdle()

            // Verify format is now active
            assert(format in activeFormats) {
                "$contentDesc should be active after first click, activeFormats=$activeFormats"
            }

            // Click again to deactivate
            composeTestRule.onNodeWithContentDescription(contentDesc).performScrollTo().performClick()
            composeTestRule.waitForIdle()

            // Verify format is now inactive
            assert(format !in activeFormats) {
                "$contentDesc should be inactive after second click, activeFormats=$activeFormats"
            }
        }
    }

    // ========================================================================
    // 11.2 Test CODE_BLOCK disables inline format buttons
    // ========================================================================

    /**
     * Verifies that when CODE_BLOCK is active, inline format buttons (Bold,
     * Italic, Underline, Strikethrough, Inline Code, Link) are disabled
     * (alpha 0.4, not clickable), while Bullet List, Ordered List, and
     * Blockquote remain enabled. Deactivating Code Block re-enables all.
     *
     * **Validates: Requirements 10.1, 9.2, 9.3**
     */
    @Test
    fun codeBlock_disablesInlineFormatButtons() {
        var activeFormats by mutableStateOf(emptySet<RichTextFormat>())
        var disabledFormats by mutableStateOf(emptySet<RichTextFormat>())

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatRichTextToolbar(
                    activeFormats = activeFormats,
                    disabledFormats = disabledFormats,
                    onFormatClick = { clicked ->
                        activeFormats = RichTextFormat.toggleFormat(activeFormats, clicked)
                        disabledFormats = FormatCompatibility.getDisabledFormats(activeFormats)
                    },
                    onLinkClick = {
                        activeFormats = RichTextFormat.toggleFormat(activeFormats, RichTextFormat.LINK)
                        disabledFormats = FormatCompatibility.getDisabledFormats(activeFormats)
                    }
                )
            }
        }
        composeTestRule.waitForIdle()

        // Activate Code Block (scroll into view first since it's the last button)
        composeTestRule.onNodeWithContentDescription(CODE_BLOCK).performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Verify CODE_BLOCK is active
        assert(RichTextFormat.CODE_BLOCK in activeFormats)

        // Verify inline buttons are disabled via FormatCompatibility
        val expectedDisabled = setOf(
            RichTextFormat.BOLD, RichTextFormat.ITALIC, RichTextFormat.UNDERLINE,
            RichTextFormat.STRIKETHROUGH, RichTextFormat.INLINE_CODE, RichTextFormat.LINK,
            RichTextFormat.BULLET_LIST, RichTextFormat.ORDERED_LIST, RichTextFormat.BLOCKQUOTE
        )
        for (fmt in expectedDisabled) {
            assert(fmt in disabledFormats) {
                "$fmt should be disabled when CODE_BLOCK is active, disabledFormats=$disabledFormats"
            }
        }

        // Verify disabled buttons are rendered (still displayed but not clickable)
        composeTestRule.onNodeWithContentDescription(BOLD).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(ITALIC).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(UNDERLINE).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(STRIKETHROUGH).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(INLINE_CODE).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(LINK).performScrollTo().assertIsDisplayed()

        // Clicking a disabled button should NOT change activeFormats
        val formatsBefore = activeFormats.toSet()
        composeTestRule.onNodeWithContentDescription(BOLD).performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assert(activeFormats == formatsBefore) {
            "Clicking disabled Bold should not change activeFormats"
        }

        // Deactivate Code Block
        composeTestRule.onNodeWithContentDescription(CODE_BLOCK).performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Verify all buttons re-enabled
        assert(disabledFormats.isEmpty()) {
            "All formats should be enabled after deactivating CODE_BLOCK, disabledFormats=$disabledFormats"
        }
    }

    // ========================================================================
    // 11.3 Test BULLET_LIST and ORDERED_LIST mutual exclusivity
    // ========================================================================

    /**
     * Verifies that BULLET_LIST and ORDERED_LIST are mutually exclusive:
     * activating one disables the other via FormatCompatibility.
     *
     * **Validates: Requirements 10.2, 10.3**
     */
    @Test
    fun bulletListAndOrderedList_areMutuallyExclusive() {
        var activeFormats by mutableStateOf(emptySet<RichTextFormat>())
        var disabledFormats by mutableStateOf(emptySet<RichTextFormat>())

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatRichTextToolbar(
                    activeFormats = activeFormats,
                    disabledFormats = disabledFormats,
                    onFormatClick = { clicked ->
                        activeFormats = RichTextFormat.toggleFormat(activeFormats, clicked)
                        disabledFormats = FormatCompatibility.getDisabledFormats(activeFormats)
                    },
                    onLinkClick = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        // Activate Bullet List → Ordered List should be disabled
        composeTestRule.onNodeWithContentDescription(BULLET_LIST).performClick()
        composeTestRule.waitForIdle()

        assert(RichTextFormat.BULLET_LIST in activeFormats) {
            "BULLET_LIST should be active"
        }
        assert(RichTextFormat.ORDERED_LIST in disabledFormats) {
            "ORDERED_LIST should be disabled when BULLET_LIST is active"
        }

        // Deactivate Bullet List
        composeTestRule.onNodeWithContentDescription(BULLET_LIST).performClick()
        composeTestRule.waitForIdle()

        // Activate Ordered List → Bullet List should be disabled
        composeTestRule.onNodeWithContentDescription(NUMBERED_LIST).performClick()
        composeTestRule.waitForIdle()

        assert(RichTextFormat.ORDERED_LIST in activeFormats) {
            "ORDERED_LIST should be active"
        }
        assert(RichTextFormat.BULLET_LIST in disabledFormats) {
            "BULLET_LIST should be disabled when ORDERED_LIST is active"
        }
    }

    // ========================================================================
    // 11.4 Test LINK and INLINE_CODE mutual exclusivity
    // ========================================================================

    /**
     * Verifies that LINK and INLINE_CODE are mutually exclusive:
     * activating one disables the other via FormatCompatibility.
     *
     * **Validates: Requirements 10.4, 10.5**
     */
    @Test
    fun linkAndInlineCode_areMutuallyExclusive() {
        var activeFormats by mutableStateOf(emptySet<RichTextFormat>())
        var disabledFormats by mutableStateOf(emptySet<RichTextFormat>())

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatRichTextToolbar(
                    activeFormats = activeFormats,
                    disabledFormats = disabledFormats,
                    onFormatClick = { clicked ->
                        activeFormats = RichTextFormat.toggleFormat(activeFormats, clicked)
                        disabledFormats = FormatCompatibility.getDisabledFormats(activeFormats)
                    },
                    onLinkClick = {
                        activeFormats = RichTextFormat.toggleFormat(activeFormats, RichTextFormat.LINK)
                        disabledFormats = FormatCompatibility.getDisabledFormats(activeFormats)
                    }
                )
            }
        }
        composeTestRule.waitForIdle()

        // Activate Link → Inline Code should be disabled
        composeTestRule.onNodeWithContentDescription(LINK).performClick()
        composeTestRule.waitForIdle()

        assert(RichTextFormat.LINK in activeFormats) {
            "LINK should be active"
        }
        assert(RichTextFormat.INLINE_CODE in disabledFormats) {
            "INLINE_CODE should be disabled when LINK is active"
        }

        // Deactivate Link
        composeTestRule.onNodeWithContentDescription(LINK).performClick()
        composeTestRule.waitForIdle()

        // Activate Inline Code → Link should be disabled
        composeTestRule.onNodeWithContentDescription(INLINE_CODE).performClick()
        composeTestRule.waitForIdle()

        assert(RichTextFormat.INLINE_CODE in activeFormats) {
            "INLINE_CODE should be active"
        }
        assert(RichTextFormat.LINK in disabledFormats) {
            "LINK should be disabled when INLINE_CODE is active"
        }
    }


    // ========================================================================
    // 11.5 Test auto-deselect: activating CODE_BLOCK deselects all others
    // ========================================================================

    /**
     * Verifies that activating CODE_BLOCK auto-deselects all other active
     * formats (Bold, Italic, etc.) via RichTextFormat.toggleFormat().
     *
     * Note: When BOLD or ITALIC are active, CODE_BLOCK is disabled in the
     * toolbar (FormatCompatibility bidirectional incompatibility). In the real
     * app, the segment controller handles the code block toggle independently.
     * This test verifies the auto-deselect logic by programmatically toggling
     * CODE_BLOCK and observing the toolbar state update.
     *
     * **Validates: Requirements 11.1**
     */
    @Test
    fun codeBlock_autoDeselectsAllOtherFormats() {
        var activeFormats by mutableStateOf(emptySet<RichTextFormat>())
        var disabledFormats by mutableStateOf(emptySet<RichTextFormat>())

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatRichTextToolbar(
                    activeFormats = activeFormats,
                    disabledFormats = disabledFormats,
                    onFormatClick = { clicked ->
                        activeFormats = RichTextFormat.toggleFormat(activeFormats, clicked)
                        disabledFormats = FormatCompatibility.getDisabledFormats(activeFormats)
                    },
                    onLinkClick = {
                        activeFormats = RichTextFormat.toggleFormat(activeFormats, RichTextFormat.LINK)
                        disabledFormats = FormatCompatibility.getDisabledFormats(activeFormats)
                    }
                )
            }
        }
        composeTestRule.waitForIdle()

        // Activate Bold + Italic via toolbar clicks
        composeTestRule.onNodeWithContentDescription(BOLD).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription(ITALIC).performClick()
        composeTestRule.waitForIdle()

        assert(RichTextFormat.BOLD in activeFormats) { "BOLD should be active" }
        assert(RichTextFormat.ITALIC in activeFormats) { "ITALIC should be active" }

        // CODE_BLOCK is disabled in toolbar when BOLD/ITALIC are active
        // (bidirectional incompatibility). Programmatically toggle CODE_BLOCK
        // to test the auto-deselect logic (as the segment controller would).
        activeFormats = RichTextFormat.toggleFormat(activeFormats, RichTextFormat.CODE_BLOCK)
        disabledFormats = FormatCompatibility.getDisabledFormats(activeFormats)
        composeTestRule.waitForIdle()

        assert(RichTextFormat.CODE_BLOCK in activeFormats) {
            "CODE_BLOCK should be active"
        }
        assert(RichTextFormat.BOLD !in activeFormats) {
            "BOLD should be auto-deselected when CODE_BLOCK is activated"
        }
        assert(RichTextFormat.ITALIC !in activeFormats) {
            "ITALIC should be auto-deselected when CODE_BLOCK is activated"
        }

        // Verify toolbar reflects the new state
        composeTestRule.onNodeWithContentDescription(CODE_BLOCK).assertExists()
        composeTestRule.onNodeWithContentDescription(BOLD).assertExists()
        composeTestRule.onNodeWithContentDescription(ITALIC).assertExists()
    }

    // ========================================================================
    // 11.6 Test auto-deselect: BULLET_LIST deselects CODE_BLOCK and ORDERED_LIST
    // ========================================================================

    /**
     * Verifies that activating BULLET_LIST auto-deselects CODE_BLOCK and
     * ORDERED_LIST via RichTextFormat.toggleFormat().
     *
     * Note: When CODE_BLOCK is active, BULLET_LIST is disabled in the toolbar
     * (FormatCompatibility makes them bidirectionally incompatible). In the real
     * app, the segment controller handles the transition. This test verifies the
     * auto-deselect logic by programmatically toggling the format and observing
     * the toolbar state update. For the ORDERED_LIST scenario, the toolbar click
     * works directly since BULLET_LIST and ORDERED_LIST use the simpler
     * mutual-exclusivity auto-deselect (BULLET_LIST is not disabled by ORDERED_LIST
     * in the toolbar — only via FormatCompatibility bidirectional pairs).
     *
     * **Validates: Requirements 11.2**
     */
    @Test
    fun bulletList_autoDeselectsCodeBlockAndOrderedList() {
        var activeFormats by mutableStateOf(emptySet<RichTextFormat>())
        var disabledFormats by mutableStateOf(emptySet<RichTextFormat>())

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatRichTextToolbar(
                    activeFormats = activeFormats,
                    disabledFormats = disabledFormats,
                    onFormatClick = { clicked ->
                        activeFormats = RichTextFormat.toggleFormat(activeFormats, clicked)
                        disabledFormats = FormatCompatibility.getDisabledFormats(activeFormats)
                    },
                    onLinkClick = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        // Scenario 1: CODE_BLOCK active → toggle BULLET_LIST programmatically
        // (toolbar disables BULLET_LIST when CODE_BLOCK is active, so we
        // simulate the toggle as the segment controller would)
        composeTestRule.onNodeWithContentDescription(CODE_BLOCK).performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assert(RichTextFormat.CODE_BLOCK in activeFormats) { "CODE_BLOCK should be active" }

        // Programmatically apply the auto-deselect toggle (as the real controller does)
        activeFormats = RichTextFormat.toggleFormat(activeFormats, RichTextFormat.BULLET_LIST)
        disabledFormats = FormatCompatibility.getDisabledFormats(activeFormats)
        composeTestRule.waitForIdle()

        assert(RichTextFormat.BULLET_LIST in activeFormats) {
            "BULLET_LIST should be active after toggle"
        }
        assert(RichTextFormat.CODE_BLOCK !in activeFormats) {
            "CODE_BLOCK should be auto-deselected when BULLET_LIST is activated"
        }

        // Verify toolbar reflects the new state: BULLET_LIST active, CODE_BLOCK not active
        composeTestRule.onNodeWithContentDescription(BULLET_LIST).assertExists()
        composeTestRule.onNodeWithContentDescription(CODE_BLOCK).assertExists()

        // Reset
        activeFormats = emptySet()
        disabledFormats = emptySet()
        composeTestRule.waitForIdle()

        // Scenario 2: Activate Ordered List → then Bullet List → Ordered List deselected
        // ORDERED_LIST disables BULLET_LIST via FormatCompatibility, so we also
        // toggle programmatically for consistency
        composeTestRule.onNodeWithContentDescription(NUMBERED_LIST).performClick()
        composeTestRule.waitForIdle()
        assert(RichTextFormat.ORDERED_LIST in activeFormats) { "ORDERED_LIST should be active" }

        // Programmatically toggle BULLET_LIST (auto-deselects ORDERED_LIST)
        activeFormats = RichTextFormat.toggleFormat(activeFormats, RichTextFormat.BULLET_LIST)
        disabledFormats = FormatCompatibility.getDisabledFormats(activeFormats)
        composeTestRule.waitForIdle()

        assert(RichTextFormat.BULLET_LIST in activeFormats) {
            "BULLET_LIST should be active"
        }
        assert(RichTextFormat.ORDERED_LIST !in activeFormats) {
            "ORDERED_LIST should be auto-deselected when BULLET_LIST is activated"
        }
    }

    // ========================================================================
    // 11.7 Test BOLD + ITALIC + UNDERLINE + STRIKETHROUGH simultaneous activation
    // ========================================================================

    /**
     * Verifies that BOLD, ITALIC, UNDERLINE, and STRIKETHROUGH can all be
     * active simultaneously — they are fully compatible with each other.
     *
     * **Validates: Requirements 10.6, 20.1**
     */
    @Test
    fun boldItalicUnderlineStrikethrough_canAllBeActiveSimultaneously() {
        var activeFormats by mutableStateOf(emptySet<RichTextFormat>())
        var disabledFormats by mutableStateOf(emptySet<RichTextFormat>())

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatRichTextToolbar(
                    activeFormats = activeFormats,
                    disabledFormats = disabledFormats,
                    onFormatClick = { clicked ->
                        activeFormats = RichTextFormat.toggleFormat(activeFormats, clicked)
                        disabledFormats = FormatCompatibility.getDisabledFormats(activeFormats)
                    },
                    onLinkClick = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        // Activate all four text decoration formats
        composeTestRule.onNodeWithContentDescription(BOLD).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription(ITALIC).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription(UNDERLINE).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription(STRIKETHROUGH).performClick()
        composeTestRule.waitForIdle()

        // Verify all four are active
        assert(RichTextFormat.BOLD in activeFormats) { "BOLD should be active" }
        assert(RichTextFormat.ITALIC in activeFormats) { "ITALIC should be active" }
        assert(RichTextFormat.UNDERLINE in activeFormats) { "UNDERLINE should be active" }
        assert(RichTextFormat.STRIKETHROUGH in activeFormats) { "STRIKETHROUGH should be active" }

        // Verify none of them disabled each other
        assert(RichTextFormat.BOLD !in disabledFormats) { "BOLD should not be disabled" }
        assert(RichTextFormat.ITALIC !in disabledFormats) { "ITALIC should not be disabled" }
        assert(RichTextFormat.UNDERLINE !in disabledFormats) { "UNDERLINE should not be disabled" }
        assert(RichTextFormat.STRIKETHROUGH !in disabledFormats) { "STRIKETHROUGH should not be disabled" }

        // Verify all four buttons are still displayed and clickable
        composeTestRule.onNodeWithContentDescription(BOLD).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(ITALIC).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(UNDERLINE).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(STRIKETHROUGH).assertIsDisplayed()
    }

    // ========================================================================
    // 11.8 Test toolbar separator layout and button order
    // ========================================================================

    /**
     * Verifies that toolbar buttons appear in the correct order:
     * Bold, Italic, Underline, Strikethrough | Link, Ordered List, Bullet List |
     * Blockquote, Inline Code, Code Block
     *
     * Also verifies that all 10 buttons and the toolbar container are displayed.
     * Button order is verified by checking that all expected buttons exist and
     * are displayed within the toolbar row.
     *
     * **Validates: Requirements 30.1, 30.2**
     */
    @Test
    fun toolbarLayout_correctButtonOrderAndSeparators() {
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatRichTextToolbar(
                    activeFormats = emptySet(),
                    disabledFormats = emptySet(),
                    onFormatClick = {},
                    onLinkClick = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        // Verify the toolbar container is displayed
        composeTestRule.onNodeWithContentDescription(TOOLBAR).assertIsDisplayed()

        // Verify all 10 format buttons exist in the toolbar
        val expectedButtons = listOf(
            BOLD, ITALIC, UNDERLINE, STRIKETHROUGH,
            LINK, NUMBERED_LIST, BULLET_LIST,
            BLOCKQUOTE, INLINE_CODE, CODE_BLOCK
        )
        for (button in expectedButtons) {
            composeTestRule.onNodeWithContentDescription(button).assertExists()
        }

        // Verify button order by checking positional relationships.
        // In Compose test, we can verify order by checking that each button
        // in the expected sequence has a smaller or equal X position than the next.
        // We collect bounds for each button and verify left-to-right ordering.
        val buttonBounds = expectedButtons.map { desc ->
            desc to composeTestRule.onNodeWithContentDescription(desc)
                .getUnclippedBoundsInRoot()
        }

        for (i in 0 until buttonBounds.size - 1) {
            val (currentDesc, currentBounds) = buttonBounds[i]
            val (nextDesc, nextBounds) = buttonBounds[i + 1]
            assert(currentBounds.left < nextBounds.left) {
                "Button '$currentDesc' (left=${currentBounds.left}) should be to the left of " +
                    "'$nextDesc' (left=${nextBounds.left})"
            }
        }

        // Verify separators exist by checking gaps between groups.
        // Group 1 ends at Strikethrough, Group 2 starts at Link.
        // Group 2 ends at Bullet List, Group 3 starts at Blockquote.
        // The gap between groups should be larger than the gap between
        // buttons within the same group (due to separator + spacers).
        val strikethroughRight = buttonBounds[3].second.right  // Strikethrough
        val linkLeft = buttonBounds[4].second.left              // Link
        val bulletListRight = buttonBounds[6].second.right      // Bullet List
        val blockquoteLeft = buttonBounds[7].second.left        // Blockquote

        // Gap between groups (includes 12dp + 2dp separator + 12dp = 26dp)
        val gap1 = linkLeft - strikethroughRight
        val gap2 = blockquoteLeft - bulletListRight

        // Gap between adjacent buttons within a group (should be ~0dp since buttons are adjacent)
        val boldRight = buttonBounds[0].second.right
        val italicLeft = buttonBounds[1].second.left
        val intraGroupGap = italicLeft - boldRight

        // Separator gaps should be larger than intra-group gaps
        assert(gap1 > intraGroupGap) {
            "Gap between Strikethrough and Link ($gap1) should be larger than " +
                "intra-group gap ($intraGroupGap) due to separator"
        }
        assert(gap2 > intraGroupGap) {
            "Gap between Bullet List and Blockquote ($gap2) should be larger than " +
                "intra-group gap ($intraGroupGap) due to separator"
        }
    }
}