package com.cometchat.uikit.kotlin.presentation.shared.messagebubble

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.actionbubble.CometChatActionBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.audiobubble.CometChatAudioBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.callactionbubble.CometChatCallActionBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.collaborativebubble.CometChatCollaborativeBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.deletebubble.CometChatDeleteBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.filebubble.CometChatFileBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.imagebubble.CometChatImageBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.meetcallbubble.CometChatMeetCallBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.pollbubble.CometChatPollBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.stickerbubble.CometChatStickerBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.textbubble.CometChatTextBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.videobubble.CometChatVideoBubbleStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Preservation Property Tests for Custom Bubble Alignment Style Fix.
 *
 * These tests verify that existing functionality is preserved and will NOT be
 * affected by the fix. They establish a baseline of correct behavior that must
 * continue to work after the fix is applied.
 *
 * **Property 2: Preservation** - Explicit Style Override and Non-Custom Message Behavior
 *
 * **Key Observations (on UNFIXED code):**
 * - When explicit bubbleStyle is provided via BubbleStyles, it is used instead of alignment-based defaults
 * - Standard message types (text, image, video, audio, file) have incoming/outgoing/default factory methods
 * - Deleted messages have incoming/outgoing/default factory methods
 * - Action messages use `CometChatActionBubbleStyle.default()` (center-aligned system messages)
 * - Call action messages use `CometChatCallActionBubbleStyle.default()` (center-aligned system messages)
 * - CENTER-aligned custom messages use `default()` style which delegates to `outgoing()`
 *
 * **EXPECTED OUTCOME:** Tests PASS on unfixed code (confirms baseline behavior to preserve)
 *
 * **Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6**
 *
 * Feature: custom-bubble-alignment-style-fix
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
class CustomBubbleAlignmentStylePreservationTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    // ==================== Preservation: Explicit Style Override (Requirement 3.1) ====================

    /**
     * Property 2: Preservation - Explicit Poll Style Override
     *
     * When an explicit pollBubbleStyle is provided via BubbleStyles, it SHALL be used
     * instead of alignment-based defaults. This behavior must be preserved after the fix.
     *
     * **Observation:** BubbleStyles allows explicit style overrides via non-null entries.
     * The renderer checks `bubbleStyles.pollBubbleStyle ?: <alignment-based-default>`.
     *
     * **Validates: Requirement 3.1**
     */
    @Test
    fun `explicit pollBubbleStyle in BubbleStyles should be available for override`() {
        // Create an explicit custom poll style
        val explicitPollStyle = CometChatPollBubbleStyle(
            titleTextColor = 0xFF00FF00.toInt(), // Custom green color
            backgroundColor = 0xFFFF0000.toInt() // Custom red background
        )

        // Create BubbleStyles with explicit poll style
        val bubbleStyles = BubbleStyles(pollBubbleStyle = explicitPollStyle)

        // Verify the explicit style is accessible
        assertNotNull(
            "BubbleStyles should hold the explicit pollBubbleStyle",
            bubbleStyles.pollBubbleStyle
        )
        assertEquals(
            "Explicit pollBubbleStyle should have custom titleTextColor",
            0xFF00FF00.toInt(),
            bubbleStyles.pollBubbleStyle?.titleTextColor
        )
        assertEquals(
            "Explicit pollBubbleStyle should have custom backgroundColor",
            0xFFFF0000.toInt(),
            bubbleStyles.pollBubbleStyle?.backgroundColor
        )
    }

    /**
     * Property 2: Preservation - Explicit Sticker Style Override
     *
     * When an explicit stickerBubbleStyle is provided via BubbleStyles, it SHALL be used
     * instead of alignment-based defaults.
     *
     * **Validates: Requirement 3.1**
     */
    @Test
    fun `explicit stickerBubbleStyle in BubbleStyles should be available for override`() {
        val explicitStickerStyle = CometChatStickerBubbleStyle(
            backgroundColor = 0xFF0000FF.toInt() // Custom blue background
        )

        val bubbleStyles = BubbleStyles(stickerBubbleStyle = explicitStickerStyle)

        assertNotNull(
            "BubbleStyles should hold the explicit stickerBubbleStyle",
            bubbleStyles.stickerBubbleStyle
        )
        assertEquals(
            "Explicit stickerBubbleStyle should have custom backgroundColor",
            0xFF0000FF.toInt(),
            bubbleStyles.stickerBubbleStyle?.backgroundColor
        )
    }

    /**
     * Property 2: Preservation - Explicit Collaborative Style Override
     *
     * When an explicit collaborativeBubbleStyle is provided via BubbleStyles, it SHALL be used
     * instead of alignment-based defaults.
     *
     * **Validates: Requirement 3.1**
     */
    @Test
    fun `explicit collaborativeBubbleStyle in BubbleStyles should be available for override`() {
        val explicitCollaborativeStyle = CometChatCollaborativeBubbleStyle(
            backgroundColor = 0xFFFFFF00.toInt() // Custom yellow background
        )

        val bubbleStyles = BubbleStyles(collaborativeBubbleStyle = explicitCollaborativeStyle)

        assertNotNull(
            "BubbleStyles should hold the explicit collaborativeBubbleStyle",
            bubbleStyles.collaborativeBubbleStyle
        )
        assertEquals(
            "Explicit collaborativeBubbleStyle should have custom backgroundColor",
            0xFFFFFF00.toInt(),
            bubbleStyles.collaborativeBubbleStyle?.backgroundColor
        )
    }

    /**
     * Property 2: Preservation - Explicit MeetCall Style Override
     *
     * When an explicit meetCallBubbleStyle is provided via BubbleStyles, it SHALL be used
     * instead of alignment-based defaults.
     *
     * **Validates: Requirement 3.1**
     */
    @Test
    fun `explicit meetCallBubbleStyle in BubbleStyles should be available for override`() {
        val explicitMeetCallStyle = CometChatMeetCallBubbleStyle(
            backgroundColor = 0xFFFF00FF.toInt() // Custom magenta background
        )

        val bubbleStyles = BubbleStyles(meetCallBubbleStyle = explicitMeetCallStyle)

        assertNotNull(
            "BubbleStyles should hold the explicit meetCallBubbleStyle",
            bubbleStyles.meetCallBubbleStyle
        )
        assertEquals(
            "Explicit meetCallBubbleStyle should have custom backgroundColor",
            0xFFFF00FF.toInt(),
            bubbleStyles.meetCallBubbleStyle?.backgroundColor
        )
    }

    // ==================== Preservation: Standard Message Factory Methods Exist (Requirement 3.2) ====================

    /**
     * Property 2: Preservation - Text Bubble Has Alignment-Based Factory Methods
     *
     * Standard text messages SHALL CONTINUE TO have incoming/outgoing/default factory methods.
     * These methods enable alignment-based style selection.
     *
     * **Validates: Requirement 3.2**
     */
    @Test
    fun `text bubble should have incoming, outgoing, and default factory methods`() {
        val incomingStyle = CometChatTextBubbleStyle.incoming(context)
        val outgoingStyle = CometChatTextBubbleStyle.outgoing(context)
        val defaultStyle = CometChatTextBubbleStyle.default(context)

        assertNotNull("Text bubble incoming() should return a valid style", incomingStyle)
        assertNotNull("Text bubble outgoing() should return a valid style", outgoingStyle)
        assertNotNull("Text bubble default() should return a valid style", defaultStyle)
    }

    /**
     * Property 2: Preservation - Image Bubble Has Alignment-Based Factory Methods
     *
     * **Validates: Requirement 3.2**
     */
    @Test
    fun `image bubble should have incoming, outgoing, and default factory methods`() {
        val incomingStyle = CometChatImageBubbleStyle.incoming(context)
        val outgoingStyle = CometChatImageBubbleStyle.outgoing(context)
        val defaultStyle = CometChatImageBubbleStyle.default(context)

        assertNotNull("Image bubble incoming() should return a valid style", incomingStyle)
        assertNotNull("Image bubble outgoing() should return a valid style", outgoingStyle)
        assertNotNull("Image bubble default() should return a valid style", defaultStyle)
    }

    /**
     * Property 2: Preservation - Video Bubble Has Alignment-Based Factory Methods
     *
     * **Validates: Requirement 3.2**
     */
    @Test
    fun `video bubble should have incoming, outgoing, and default factory methods`() {
        val incomingStyle = CometChatVideoBubbleStyle.incoming(context)
        val outgoingStyle = CometChatVideoBubbleStyle.outgoing(context)
        val defaultStyle = CometChatVideoBubbleStyle.default(context)

        assertNotNull("Video bubble incoming() should return a valid style", incomingStyle)
        assertNotNull("Video bubble outgoing() should return a valid style", outgoingStyle)
        assertNotNull("Video bubble default() should return a valid style", defaultStyle)
    }

    /**
     * Property 2: Preservation - Audio Bubble Has Alignment-Based Factory Methods
     *
     * **Validates: Requirement 3.2**
     */
    @Test
    fun `audio bubble should have incoming, outgoing, and default factory methods`() {
        val incomingStyle = CometChatAudioBubbleStyle.incoming(context)
        val outgoingStyle = CometChatAudioBubbleStyle.outgoing(context)
        val defaultStyle = CometChatAudioBubbleStyle.default(context)

        assertNotNull("Audio bubble incoming() should return a valid style", incomingStyle)
        assertNotNull("Audio bubble outgoing() should return a valid style", outgoingStyle)
        assertNotNull("Audio bubble default() should return a valid style", defaultStyle)
    }

    /**
     * Property 2: Preservation - File Bubble Has Alignment-Based Factory Methods
     *
     * **Validates: Requirement 3.2**
     */
    @Test
    fun `file bubble should have incoming, outgoing, and default factory methods`() {
        val incomingStyle = CometChatFileBubbleStyle.incoming(context)
        val outgoingStyle = CometChatFileBubbleStyle.outgoing(context)
        val defaultStyle = CometChatFileBubbleStyle.default(context)

        assertNotNull("File bubble incoming() should return a valid style", incomingStyle)
        assertNotNull("File bubble outgoing() should return a valid style", outgoingStyle)
        assertNotNull("File bubble default() should return a valid style", defaultStyle)
    }

    // ==================== Preservation: Deleted Message Styling (Requirement 3.3) ====================

    /**
     * Property 2: Preservation - Delete Bubble Has Alignment-Based Factory Methods
     *
     * Deleted messages SHALL CONTINUE TO have incoming/outgoing/default factory methods.
     *
     * **Validates: Requirement 3.3**
     */
    @Test
    fun `delete bubble should have incoming, outgoing, and default factory methods`() {
        val incomingStyle = CometChatDeleteBubbleStyle.incoming(context)
        val outgoingStyle = CometChatDeleteBubbleStyle.outgoing(context)
        val defaultStyle = CometChatDeleteBubbleStyle.default(context)

        assertNotNull("Delete bubble incoming() should return a valid style", incomingStyle)
        assertNotNull("Delete bubble outgoing() should return a valid style", outgoingStyle)
        assertNotNull("Delete bubble default() should return a valid style", defaultStyle)
    }

    /**
     * Property 2: Preservation - Delete Bubble Default Delegates to Outgoing
     *
     * The default() method for delete bubble should delegate to outgoing().
     *
     * **Validates: Requirement 3.3**
     */
    @Test
    fun `delete bubble default() should delegate to outgoing()`() {
        val defaultStyle = CometChatDeleteBubbleStyle.default(context)
        val outgoingStyle = CometChatDeleteBubbleStyle.outgoing(context)

        assertEquals(
            "Delete bubble default() should delegate to outgoing()",
            outgoingStyle,
            defaultStyle
        )
    }

    // ==================== Preservation: Action Message Styling (Requirement 3.4) ====================

    /**
     * Property 2: Preservation - Action Bubble Uses Default Style
     *
     * Action messages (group member actions) are center-aligned system messages.
     * They SHALL CONTINUE TO use `CometChatActionBubbleStyle.default()` regardless of alignment.
     *
     * **Observation:** Action bubbles don't have incoming/outgoing variants because they
     * are system messages displayed in the center.
     *
     * **Validates: Requirement 3.4**
     */
    @Test
    fun `action bubble should have default() factory method`() {
        val defaultStyle = CometChatActionBubbleStyle.default(context)

        // Action bubble should have a valid default style
        assertNotNull(
            "Action bubble default() should return a valid style",
            defaultStyle
        )
    }

    /**
     * Property 2: Preservation - Action Bubble Style Is Data Class
     *
     * Verifies that action bubble style is a proper data class with expected properties.
     *
     * **Validates: Requirement 3.4**
     */
    @Test
    fun `action bubble style should be a data class with expected properties`() {
        val actionStyle = CometChatActionBubbleStyle.default(context)

        // Verify the style has the expected structure (data class properties)
        // These properties should exist regardless of their values
        val copy = actionStyle.copy(textColor = 0xFF123456.toInt())
        assertEquals(
            "Action bubble style copy should have updated textColor",
            0xFF123456.toInt(),
            copy.textColor
        )
    }

    // ==================== Preservation: Call Action Message Styling (Requirement 3.5) ====================

    /**
     * Property 2: Preservation - Call Action Bubble Uses Default Style
     *
     * Call action messages (audio/video call actions) are center-aligned system messages.
     * They SHALL CONTINUE TO use `CometChatCallActionBubbleStyle.default()` regardless of alignment.
     *
     * **Validates: Requirement 3.5**
     */
    @Test
    fun `call action bubble should have default() factory method`() {
        val defaultStyle = CometChatCallActionBubbleStyle.default(context)

        assertNotNull(
            "Call action bubble default() should return a valid style",
            defaultStyle
        )
    }

    /**
     * Property 2: Preservation - Call Action Bubble Default Delegates to Outgoing
     *
     * The default() method for call action bubble should delegate to outgoing().
     *
     * **Validates: Requirement 3.5**
     */
    @Test
    fun `call action bubble default() should delegate to outgoing()`() {
        val defaultStyle = CometChatCallActionBubbleStyle.default(context)
        val outgoingStyle = CometChatCallActionBubbleStyle.outgoing(context)

        assertEquals(
            "Call action bubble default() should delegate to outgoing()",
            outgoingStyle,
            defaultStyle
        )
    }

    /**
     * Property 2: Preservation - Call Action Bubble Has Incoming/Outgoing Methods
     *
     * Call action bubble has incoming/outgoing factory methods for API consistency,
     * even though call messages are system messages.
     *
     * **Validates: Requirement 3.5**
     */
    @Test
    fun `call action bubble should have incoming and outgoing factory methods`() {
        val incomingStyle = CometChatCallActionBubbleStyle.incoming(context)
        val outgoingStyle = CometChatCallActionBubbleStyle.outgoing(context)

        assertNotNull("Call action bubble incoming() should return a valid style", incomingStyle)
        assertNotNull("Call action bubble outgoing() should return a valid style", outgoingStyle)
    }

    // ==================== Preservation: CENTER Alignment Uses Default (Requirement 3.6) ====================

    /**
     * Property 2: Preservation - Poll Bubble Default Delegates to Outgoing
     *
     * For CENTER-aligned custom messages, the system SHALL CONTINUE TO use `default()` style.
     * The default() method delegates to outgoing() for consistency.
     *
     * **Validates: Requirement 3.6**
     */
    @Test
    fun `poll bubble default() should delegate to outgoing()`() {
        val defaultStyle = CometChatPollBubbleStyle.default(context)
        val outgoingStyle = CometChatPollBubbleStyle.outgoing(context)

        assertEquals(
            "Poll bubble default() should delegate to outgoing()",
            outgoingStyle,
            defaultStyle
        )
    }

    /**
     * Property 2: Preservation - Sticker Bubble Default Delegates to Outgoing
     *
     * **Validates: Requirement 3.6**
     */
    @Test
    fun `sticker bubble default() should delegate to outgoing()`() {
        val defaultStyle = CometChatStickerBubbleStyle.default(context)
        val outgoingStyle = CometChatStickerBubbleStyle.outgoing(context)

        assertEquals(
            "Sticker bubble default() should delegate to outgoing()",
            outgoingStyle,
            defaultStyle
        )
    }

    /**
     * Property 2: Preservation - Collaborative Bubble Default Delegates to Outgoing
     *
     * **Validates: Requirement 3.6**
     */
    @Test
    fun `collaborative bubble default() should delegate to outgoing()`() {
        val defaultStyle = CometChatCollaborativeBubbleStyle.default(context)
        val outgoingStyle = CometChatCollaborativeBubbleStyle.outgoing(context)

        assertEquals(
            "Collaborative bubble default() should delegate to outgoing()",
            outgoingStyle,
            defaultStyle
        )
    }

    /**
     * Property 2: Preservation - MeetCall Bubble Default Delegates to Outgoing
     *
     * **Validates: Requirement 3.6**
     */
    @Test
    fun `meetCall bubble default() should delegate to outgoing()`() {
        val defaultStyle = CometChatMeetCallBubbleStyle.default(context)
        val outgoingStyle = CometChatMeetCallBubbleStyle.outgoing(context)

        assertEquals(
            "MeetCall bubble default() should delegate to outgoing()",
            outgoingStyle,
            defaultStyle
        )
    }

    // ==================== Preservation: BubbleStyles Container Behavior ====================

    /**
     * Property 2: Preservation - BubbleStyles Default Values Are Null
     *
     * When BubbleStyles is created with default constructor, all style entries should be null.
     * This allows the renderer to fall back to alignment-based defaults.
     *
     * **Validates: Requirement 3.1**
     */
    @Test
    fun `BubbleStyles default constructor should have all null entries`() {
        val bubbleStyles = BubbleStyles()

        // All entries should be null by default
        assertEquals("pollBubbleStyle should be null by default", null, bubbleStyles.pollBubbleStyle)
        assertEquals("stickerBubbleStyle should be null by default", null, bubbleStyles.stickerBubbleStyle)
        assertEquals("collaborativeBubbleStyle should be null by default", null, bubbleStyles.collaborativeBubbleStyle)
        assertEquals("meetCallBubbleStyle should be null by default", null, bubbleStyles.meetCallBubbleStyle)
        assertEquals("textBubbleStyle should be null by default", null, bubbleStyles.textBubbleStyle)
        assertEquals("imageBubbleStyle should be null by default", null, bubbleStyles.imageBubbleStyle)
        assertEquals("videoBubbleStyle should be null by default", null, bubbleStyles.videoBubbleStyle)
        assertEquals("audioBubbleStyle should be null by default", null, bubbleStyles.audioBubbleStyle)
        assertEquals("fileBubbleStyle should be null by default", null, bubbleStyles.fileBubbleStyle)
        assertEquals("deleteBubbleStyle should be null by default", null, bubbleStyles.deleteBubbleStyle)
        assertEquals("actionBubbleStyle should be null by default", null, bubbleStyles.actionBubbleStyle)
        assertEquals("callActionBubbleStyle should be null by default", null, bubbleStyles.callActionBubbleStyle)
    }

    /**
     * Property 2: Preservation - BubbleStyles Allows Selective Override
     *
     * BubbleStyles should allow overriding specific styles while leaving others as null.
     *
     * **Validates: Requirement 3.1**
     */
    @Test
    fun `BubbleStyles should allow selective style override`() {
        val customPollStyle = CometChatPollBubbleStyle(titleTextColor = 0xFF123456.toInt())

        val bubbleStyles = BubbleStyles(pollBubbleStyle = customPollStyle)

        // Only pollBubbleStyle should be set
        assertNotNull("pollBubbleStyle should be set", bubbleStyles.pollBubbleStyle)
        assertEquals("stickerBubbleStyle should remain null", null, bubbleStyles.stickerBubbleStyle)
        assertEquals("collaborativeBubbleStyle should remain null", null, bubbleStyles.collaborativeBubbleStyle)
        assertEquals("meetCallBubbleStyle should remain null", null, bubbleStyles.meetCallBubbleStyle)
    }

    // ==================== Preservation: Standard Message Default Delegates to Outgoing ====================

    /**
     * Property 2: Preservation - Text Bubble Default Delegates to Outgoing
     *
     * Standard message bubbles' default() should delegate to outgoing() for consistency.
     *
     * **Validates: Requirement 3.2**
     */
    @Test
    fun `text bubble default() should delegate to outgoing()`() {
        val defaultStyle = CometChatTextBubbleStyle.default(context)
        val outgoingStyle = CometChatTextBubbleStyle.outgoing(context)

        assertEquals(
            "Text bubble default() should delegate to outgoing()",
            outgoingStyle,
            defaultStyle
        )
    }

    /**
     * Property 2: Preservation - Image Bubble Default Delegates to Outgoing
     *
     * **Validates: Requirement 3.2**
     */
    @Test
    fun `image bubble default() should delegate to outgoing()`() {
        val defaultStyle = CometChatImageBubbleStyle.default(context)
        val outgoingStyle = CometChatImageBubbleStyle.outgoing(context)

        assertEquals(
            "Image bubble default() should delegate to outgoing()",
            outgoingStyle,
            defaultStyle
        )
    }

    /**
     * Property 2: Preservation - Video Bubble Default Delegates to Outgoing
     *
     * **Validates: Requirement 3.2**
     */
    @Test
    fun `video bubble default() should delegate to outgoing()`() {
        val defaultStyle = CometChatVideoBubbleStyle.default(context)
        val outgoingStyle = CometChatVideoBubbleStyle.outgoing(context)

        assertEquals(
            "Video bubble default() should delegate to outgoing()",
            outgoingStyle,
            defaultStyle
        )
    }

    /**
     * Property 2: Preservation - Audio Bubble Default Delegates to Outgoing
     *
     * **Validates: Requirement 3.2**
     */
    @Test
    fun `audio bubble default() should delegate to outgoing()`() {
        val defaultStyle = CometChatAudioBubbleStyle.default(context)
        val outgoingStyle = CometChatAudioBubbleStyle.outgoing(context)

        assertEquals(
            "Audio bubble default() should delegate to outgoing()",
            outgoingStyle,
            defaultStyle
        )
    }

    /**
     * Property 2: Preservation - File Bubble Default Delegates to Outgoing
     *
     * **Validates: Requirement 3.2**
     */
    @Test
    fun `file bubble default() should delegate to outgoing()`() {
        val defaultStyle = CometChatFileBubbleStyle.default(context)
        val outgoingStyle = CometChatFileBubbleStyle.outgoing(context)

        assertEquals(
            "File bubble default() should delegate to outgoing()",
            outgoingStyle,
            defaultStyle
        )
    }

    // ==================== Preservation: Custom Bubble Factory Methods Exist ====================

    /**
     * Property 2: Preservation - Poll Bubble Has Alignment-Based Factory Methods
     *
     * Custom poll messages SHALL CONTINUE TO have incoming/outgoing/default factory methods.
     *
     * **Validates: Requirement 3.6**
     */
    @Test
    fun `poll bubble should have incoming, outgoing, and default factory methods`() {
        val incomingStyle = CometChatPollBubbleStyle.incoming(context)
        val outgoingStyle = CometChatPollBubbleStyle.outgoing(context)
        val defaultStyle = CometChatPollBubbleStyle.default(context)

        assertNotNull("Poll bubble incoming() should return a valid style", incomingStyle)
        assertNotNull("Poll bubble outgoing() should return a valid style", outgoingStyle)
        assertNotNull("Poll bubble default() should return a valid style", defaultStyle)
    }

    /**
     * Property 2: Preservation - Sticker Bubble Has Alignment-Based Factory Methods
     *
     * **Validates: Requirement 3.6**
     */
    @Test
    fun `sticker bubble should have incoming, outgoing, and default factory methods`() {
        val incomingStyle = CometChatStickerBubbleStyle.incoming(context)
        val outgoingStyle = CometChatStickerBubbleStyle.outgoing(context)
        val defaultStyle = CometChatStickerBubbleStyle.default(context)

        assertNotNull("Sticker bubble incoming() should return a valid style", incomingStyle)
        assertNotNull("Sticker bubble outgoing() should return a valid style", outgoingStyle)
        assertNotNull("Sticker bubble default() should return a valid style", defaultStyle)
    }

    /**
     * Property 2: Preservation - Collaborative Bubble Has Alignment-Based Factory Methods
     *
     * **Validates: Requirement 3.6**
     */
    @Test
    fun `collaborative bubble should have incoming, outgoing, and default factory methods`() {
        val incomingStyle = CometChatCollaborativeBubbleStyle.incoming(context)
        val outgoingStyle = CometChatCollaborativeBubbleStyle.outgoing(context)
        val defaultStyle = CometChatCollaborativeBubbleStyle.default(context)

        assertNotNull("Collaborative bubble incoming() should return a valid style", incomingStyle)
        assertNotNull("Collaborative bubble outgoing() should return a valid style", outgoingStyle)
        assertNotNull("Collaborative bubble default() should return a valid style", defaultStyle)
    }

    /**
     * Property 2: Preservation - MeetCall Bubble Has Alignment-Based Factory Methods
     *
     * **Validates: Requirement 3.6**
     */
    @Test
    fun `meetCall bubble should have incoming, outgoing, and default factory methods`() {
        val incomingStyle = CometChatMeetCallBubbleStyle.incoming(context)
        val outgoingStyle = CometChatMeetCallBubbleStyle.outgoing(context)
        val defaultStyle = CometChatMeetCallBubbleStyle.default(context)

        assertNotNull("MeetCall bubble incoming() should return a valid style", incomingStyle)
        assertNotNull("MeetCall bubble outgoing() should return a valid style", outgoingStyle)
        assertNotNull("MeetCall bubble default() should return a valid style", defaultStyle)
    }
}
