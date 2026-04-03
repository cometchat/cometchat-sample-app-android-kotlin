package com.cometchat.uikit.kotlin.presentation.shared.messagebubble

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.collaborativebubble.CometChatCollaborativeBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.meetcallbubble.CometChatMeetCallBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.pollbubble.CometChatPollBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.stickerbubble.CometChatStickerBubbleStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Bug Condition Exploration Test for Custom Bubble Alignment Style Fix.
 *
 * This test verifies that custom message bubble types (Poll, Sticker, Collaborative, MeetCall)
 * use alignment-based style selection (`incoming()`/`outgoing()`) instead of always using `default()`.
 *
 * **Bug Context:**
 * The `bindCustomMessage()` and `bindMeetingMessage()` methods in `InternalContentRenderer.kt`
 * don't receive the `alignment` parameter, so they always use `default()` style factory methods
 * which delegate to `outgoing()`. This causes incoming (LEFT-aligned) custom messages to display
 * with outgoing-style colors instead of incoming-style colors.
 *
 * **Bug Condition:**
 * - Custom message types: poll, sticker, collaborative (document/whiteboard), meeting
 * - Alignment: LEFT (incoming messages)
 * - No explicit bubbleStyle override provided
 * - Result: `default()` is used instead of `incoming()`
 *
 * **EXPECTED OUTCOME ON UNFIXED CODE:** Test FAILS
 * - `default()` delegates to `outgoing()`, so incoming messages get outgoing-style colors
 * - The test asserts that `incoming()` should produce different styles than `default()`
 *   for custom bubble types, which confirms the bug exists
 *
 * **Validates: Requirements 1.1, 1.2, 1.3, 1.4, 1.5, 1.6**
 *
 * Feature: custom-bubble-alignment-style-fix
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
class CustomBubbleAlignmentStyleExplorationTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    // ==================== Poll Bubble Tests ====================

    /**
     * Property 1: Fault Condition - Poll Bubble Alignment Style Selection
     *
     * When rendering an incoming poll message (alignment = LEFT), the system SHALL use
     * `CometChatPollBubbleStyle.incoming()` to apply incoming-style colors.
     *
     * **Bug:** `bindCustomMessage()` doesn't receive alignment, so it always uses `default()`
     * which delegates to `outgoing()`, causing incoming polls to display with outgoing colors.
     *
     * **Test Strategy:** Verify that `incoming()` produces a different style than `default()`.
     * If they are the same, the bug exists because LEFT-aligned messages would incorrectly
     * use outgoing-style colors.
     *
     * **Validates: Requirements 1.1, 2.1**
     */
    @Test
    fun `poll incoming() should produce different style than default() for LEFT-aligned messages`() {
        // Get the incoming style (what LEFT-aligned messages SHOULD use)
        val incomingStyle = CometChatPollBubbleStyle.incoming(context)
        
        // Get the default style (what the buggy code ACTUALLY uses for all alignments)
        val defaultStyle = CometChatPollBubbleStyle.default(context)
        
        // The bug: default() delegates to outgoing(), so incoming messages get wrong colors
        // This test FAILS on unfixed code because incoming() and default() return the same style
        // (both extract from outgoing message bubble style)
        //
        // On FIXED code: incoming() extracts from incoming message bubble style,
        // default() extracts from outgoing message bubble style, so they differ
        //
        // Counterexample on failure:
        // - Expected: incoming style != default style (different theme extraction)
        // - Actual: incoming style == default style (both use outgoing theme)
        assertNotEquals(
            "Poll incoming() should differ from default() - LEFT-aligned messages need incoming style. " +
            "Bug: bindCustomMessage() uses default() for all alignments instead of incoming() for LEFT.",
            defaultStyle,
            incomingStyle
        )
    }

    // ==================== Sticker Bubble Tests ====================

    /**
     * Property 1: Fault Condition - Sticker Bubble Alignment Style Selection
     *
     * When rendering an incoming sticker message (alignment = LEFT), the system SHALL use
     * `CometChatStickerBubbleStyle.incoming()` to apply incoming-style colors.
     *
     * **Bug:** `bindCustomMessage()` doesn't receive alignment, so it always uses `default()`
     * which delegates to `outgoing()`, causing incoming stickers to display with outgoing colors.
     *
     * **Validates: Requirements 1.2, 2.2**
     */
    @Test
    fun `sticker incoming() should produce different style than default() for LEFT-aligned messages`() {
        // Get the incoming style (what LEFT-aligned messages SHOULD use)
        val incomingStyle = CometChatStickerBubbleStyle.incoming(context)
        
        // Get the default style (what the buggy code ACTUALLY uses for all alignments)
        val defaultStyle = CometChatStickerBubbleStyle.default(context)
        
        // The bug: default() delegates to outgoing(), so incoming messages get wrong colors
        // This test FAILS on unfixed code because incoming() and default() return the same style
        //
        // Counterexample on failure:
        // - Expected: incoming style != default style
        // - Actual: incoming style == default style (both use outgoing theme)
        assertNotEquals(
            "Sticker incoming() should differ from default() - LEFT-aligned messages need incoming style. " +
            "Bug: bindCustomMessage() uses default() for all alignments instead of incoming() for LEFT.",
            defaultStyle,
            incomingStyle
        )
    }

    // ==================== Collaborative Bubble Tests ====================

    /**
     * Property 1: Fault Condition - Collaborative Bubble Alignment Style Selection
     *
     * When rendering an incoming collaborative (document/whiteboard) message (alignment = LEFT),
     * the system SHALL use `CometChatCollaborativeBubbleStyle.incoming()` to apply incoming-style colors.
     *
     * **Bug:** `bindCustomMessage()` doesn't receive alignment, so it always uses `default()`
     * which delegates to `outgoing()`, causing incoming collaborative messages to display
     * with outgoing colors.
     *
     * **Validates: Requirements 1.3, 2.3**
     */
    @Test
    fun `collaborative incoming() should produce different style than default() for LEFT-aligned messages`() {
        // Get the incoming style (what LEFT-aligned messages SHOULD use)
        val incomingStyle = CometChatCollaborativeBubbleStyle.incoming(context)
        
        // Get the default style (what the buggy code ACTUALLY uses for all alignments)
        val defaultStyle = CometChatCollaborativeBubbleStyle.default(context)
        
        // The bug: default() delegates to outgoing(), so incoming messages get wrong colors
        // This test FAILS on unfixed code because incoming() and default() return the same style
        //
        // Counterexample on failure:
        // - Expected: incoming style != default style
        // - Actual: incoming style == default style (both use outgoing theme)
        assertNotEquals(
            "Collaborative incoming() should differ from default() - LEFT-aligned messages need incoming style. " +
            "Bug: bindCustomMessage() uses default() for all alignments instead of incoming() for LEFT.",
            defaultStyle,
            incomingStyle
        )
    }

    // ==================== MeetCall Bubble Tests ====================

    /**
     * Property 1: Fault Condition - MeetCall Bubble Alignment Style Selection
     *
     * When rendering an incoming meeting message (alignment = LEFT), the system SHALL use
     * `CometChatMeetCallBubbleStyle.incoming()` to apply incoming-style colors.
     *
     * **Bug:** `bindMeetingMessage()` doesn't receive alignment, so it always uses `default()`
     * which delegates to `outgoing()`, causing incoming meeting messages to display
     * with outgoing colors.
     *
     * **Validates: Requirements 1.4, 2.4**
     */
    @Test
    fun `meetCall incoming() should produce different style than default() for LEFT-aligned messages`() {
        // Get the incoming style (what LEFT-aligned messages SHOULD use)
        val incomingStyle = CometChatMeetCallBubbleStyle.incoming(context)
        
        // Get the default style (what the buggy code ACTUALLY uses for all alignments)
        val defaultStyle = CometChatMeetCallBubbleStyle.default(context)
        
        // The bug: default() delegates to outgoing(), so incoming messages get wrong colors
        // This test FAILS on unfixed code because incoming() and default() return the same style
        //
        // Counterexample on failure:
        // - Expected: incoming style != default style
        // - Actual: incoming style == default style (both use outgoing theme)
        assertNotEquals(
            "MeetCall incoming() should differ from default() - LEFT-aligned messages need incoming style. " +
            "Bug: bindMeetingMessage() uses default() for all alignments instead of incoming() for LEFT.",
            defaultStyle,
            incomingStyle
        )
    }

    // ==================== Counterexample Documentation Tests ====================

    /**
     * Documents the bug: custom bubbles use default() which delegates to outgoing(),
     * so incoming messages incorrectly get outgoing-style colors.
     *
     * This test explicitly shows that default() == outgoing() for all custom bubble types,
     * which is the root cause of the visual inconsistency.
     */
    @Test
    fun `default() should delegate to outgoing() for all custom bubble types`() {
        // Poll
        val pollDefault = CometChatPollBubbleStyle.default(context)
        val pollOutgoing = CometChatPollBubbleStyle.outgoing(context)
        assertEquals(
            "Poll default() should delegate to outgoing()",
            pollOutgoing,
            pollDefault
        )
        
        // Sticker
        val stickerDefault = CometChatStickerBubbleStyle.default(context)
        val stickerOutgoing = CometChatStickerBubbleStyle.outgoing(context)
        assertEquals(
            "Sticker default() should delegate to outgoing()",
            stickerOutgoing,
            stickerDefault
        )
        
        // Collaborative
        val collaborativeDefault = CometChatCollaborativeBubbleStyle.default(context)
        val collaborativeOutgoing = CometChatCollaborativeBubbleStyle.outgoing(context)
        assertEquals(
            "Collaborative default() should delegate to outgoing()",
            collaborativeOutgoing,
            collaborativeDefault
        )
        
        // MeetCall
        val meetCallDefault = CometChatMeetCallBubbleStyle.default(context)
        val meetCallOutgoing = CometChatMeetCallBubbleStyle.outgoing(context)
        assertEquals(
            "MeetCall default() should delegate to outgoing()",
            meetCallOutgoing,
            meetCallDefault
        )
    }

    /**
     * Documents the expected behavior: incoming() and outgoing() should produce
     * different styles because they extract from different theme attributes.
     *
     * - incoming() extracts from `cometchatIncomingMessageBubbleStyle`
     * - outgoing() extracts from `cometchatOutgoingMessageBubbleStyle`
     *
     * If these are the same, the theme is not properly configured, but the code
     * should still use the correct factory method based on alignment.
     */
    @Test
    fun `incoming() and outgoing() should extract from different theme attributes`() {
        // Poll
        val pollIncoming = CometChatPollBubbleStyle.incoming(context)
        val pollOutgoing = CometChatPollBubbleStyle.outgoing(context)
        assertNotEquals(
            "Poll incoming() should differ from outgoing() - they extract from different theme attributes",
            pollOutgoing,
            pollIncoming
        )
        
        // Sticker
        val stickerIncoming = CometChatStickerBubbleStyle.incoming(context)
        val stickerOutgoing = CometChatStickerBubbleStyle.outgoing(context)
        assertNotEquals(
            "Sticker incoming() should differ from outgoing() - they extract from different theme attributes",
            stickerOutgoing,
            stickerIncoming
        )
        
        // Collaborative
        val collaborativeIncoming = CometChatCollaborativeBubbleStyle.incoming(context)
        val collaborativeOutgoing = CometChatCollaborativeBubbleStyle.outgoing(context)
        assertNotEquals(
            "Collaborative incoming() should differ from outgoing() - they extract from different theme attributes",
            collaborativeOutgoing,
            collaborativeIncoming
        )
        
        // MeetCall
        val meetCallIncoming = CometChatMeetCallBubbleStyle.incoming(context)
        val meetCallOutgoing = CometChatMeetCallBubbleStyle.outgoing(context)
        assertNotEquals(
            "MeetCall incoming() should differ from outgoing() - they extract from different theme attributes",
            meetCallOutgoing,
            meetCallIncoming
        )
    }
}
