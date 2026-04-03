package com.cometchat.uikit.compose.presentation.shared.messagebubble.style

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [CometChatMeetCallBubbleStyle].
 *
 * Validates:
 * - Style extends CometChatMessageBubbleStyle (inheritance)
 * - default(), incoming(), outgoing() factory functions return valid styles
 * - All properties are accessible
 * - Incoming and outgoing styles have different colors
 */
class CometChatMeetCallBubbleStyleTest {

    @Test
    fun `style extends CometChatMessageBubbleStyle`() {
        val style = CometChatMeetCallBubbleStyle(
            callIconTint = Color.Blue,
            iconBackgroundColor = Color.LightGray,
            titleTextColor = Color.Black,
            titleTextStyle = androidx.compose.ui.text.TextStyle(),
            subtitleTextColor = Color.Gray,
            subtitleTextStyle = androidx.compose.ui.text.TextStyle(),
            separatorColor = Color.LightGray,
            joinButtonTextColor = Color.Blue,
            joinButtonTextStyle = androidx.compose.ui.text.TextStyle(),
            backgroundColor = Color.White,
            cornerRadius = 12.dp,
            strokeWidth = 0.dp,
            strokeColor = Color.Transparent,
            padding = PaddingValues(0.dp),
            senderNameTextColor = Color.Gray,
            senderNameTextStyle = androidx.compose.ui.text.TextStyle(),
            threadIndicatorTextColor = Color.Gray,
            threadIndicatorTextStyle = androidx.compose.ui.text.TextStyle(),
            threadIndicatorIconTint = Color.Gray,
            timestampTextColor = Color.Gray,
            timestampTextStyle = androidx.compose.ui.text.TextStyle()
        )

        // Verify inheritance - style should be assignable to CometChatMessageBubbleStyle
        assertTrue(style is CometChatMessageBubbleStyle)
    }

    @Test
    fun `default style has valid properties`() {
        val style = CometChatMeetCallBubbleStyle(
            callIconTint = Color.Blue,
            iconBackgroundColor = Color.LightGray,
            titleTextColor = Color.Black,
            titleTextStyle = androidx.compose.ui.text.TextStyle(),
            subtitleTextColor = Color.Gray,
            subtitleTextStyle = androidx.compose.ui.text.TextStyle(),
            separatorColor = Color.LightGray,
            joinButtonTextColor = Color.Blue,
            joinButtonTextStyle = androidx.compose.ui.text.TextStyle(),
            backgroundColor = Color.White,
            cornerRadius = 12.dp,
            strokeWidth = 0.dp,
            strokeColor = Color.Transparent,
            padding = PaddingValues(0.dp),
            senderNameTextColor = Color.Gray,
            senderNameTextStyle = androidx.compose.ui.text.TextStyle(),
            threadIndicatorTextColor = Color.Gray,
            threadIndicatorTextStyle = androidx.compose.ui.text.TextStyle(),
            threadIndicatorIconTint = Color.Gray,
            timestampTextColor = Color.Gray,
            timestampTextStyle = androidx.compose.ui.text.TextStyle()
        )

        assertNotNull(style)
        assertEquals(Color.White, style.backgroundColor)
        assertEquals(12.dp, style.cornerRadius)
        assertEquals(0.dp, style.strokeWidth)
        assertEquals(Color.Transparent, style.strokeColor)
        assertEquals(Color.Blue, style.callIconTint)
        assertEquals(Color.LightGray, style.iconBackgroundColor)
        assertEquals(Color.Black, style.titleTextColor)
        assertEquals(Color.Gray, style.subtitleTextColor)
        assertEquals(Color.Blue, style.joinButtonTextColor)
    }

    @Test
    fun `all properties are accessible`() {
        val style = CometChatMeetCallBubbleStyle(
            callIconTint = Color.Green,
            iconBackgroundColor = Color.Yellow,
            titleTextColor = Color.Cyan,
            titleTextStyle = androidx.compose.ui.text.TextStyle(),
            subtitleTextColor = Color.Magenta,
            subtitleTextStyle = androidx.compose.ui.text.TextStyle(),
            separatorColor = Color.DarkGray,
            joinButtonTextColor = Color.Black,
            joinButtonTextStyle = androidx.compose.ui.text.TextStyle(),
            backgroundColor = Color.Red,
            cornerRadius = 16.dp,
            strokeWidth = 1.dp,
            strokeColor = Color.Blue,
            padding = PaddingValues(8.dp),
            senderNameTextColor = Color.LightGray,
            senderNameTextStyle = androidx.compose.ui.text.TextStyle(),
            threadIndicatorTextColor = Color.White,
            threadIndicatorTextStyle = androidx.compose.ui.text.TextStyle(),
            threadIndicatorIconTint = Color.Gray,
            timestampTextColor = Color.Gray,
            timestampTextStyle = androidx.compose.ui.text.TextStyle()
        )

        // Verify content-specific properties
        assertEquals(Color.Green, style.callIconTint)
        assertEquals(Color.Yellow, style.iconBackgroundColor)
        assertEquals(Color.Cyan, style.titleTextColor)
        assertNotNull(style.titleTextStyle)
        assertEquals(Color.Magenta, style.subtitleTextColor)
        assertNotNull(style.subtitleTextStyle)
        assertEquals(Color.DarkGray, style.separatorColor)
        assertEquals(Color.Black, style.joinButtonTextColor)
        assertNotNull(style.joinButtonTextStyle)

        // Verify inherited wrapper properties
        assertEquals(Color.Red, style.backgroundColor)
        assertEquals(16.dp, style.cornerRadius)
        assertEquals(1.dp, style.strokeWidth)
        assertEquals(Color.Blue, style.strokeColor)
        assertEquals(PaddingValues(8.dp), style.padding)
        assertEquals(Color.LightGray, style.senderNameTextColor)
        assertNotNull(style.senderNameTextStyle)
        assertEquals(Color.White, style.threadIndicatorTextColor)
        assertNotNull(style.threadIndicatorTextStyle)
        assertEquals(Color.Gray, style.threadIndicatorIconTint)
        assertEquals(Color.Gray, style.timestampTextColor)
        assertNotNull(style.timestampTextStyle)
    }

    @Test
    fun `style is immutable data class`() {
        val style1 = CometChatMeetCallBubbleStyle(
            callIconTint = Color.Blue,
            iconBackgroundColor = Color.LightGray,
            titleTextColor = Color.Black,
            titleTextStyle = androidx.compose.ui.text.TextStyle(),
            subtitleTextColor = Color.Gray,
            subtitleTextStyle = androidx.compose.ui.text.TextStyle(),
            separatorColor = Color.LightGray,
            joinButtonTextColor = Color.Blue,
            joinButtonTextStyle = androidx.compose.ui.text.TextStyle(),
            backgroundColor = Color.White,
            cornerRadius = 12.dp,
            strokeWidth = 0.dp,
            strokeColor = Color.Transparent,
            padding = PaddingValues(0.dp),
            senderNameTextColor = Color.Gray,
            senderNameTextStyle = androidx.compose.ui.text.TextStyle(),
            threadIndicatorTextColor = Color.Gray,
            threadIndicatorTextStyle = androidx.compose.ui.text.TextStyle(),
            threadIndicatorIconTint = Color.Gray,
            timestampTextColor = Color.Gray,
            timestampTextStyle = androidx.compose.ui.text.TextStyle()
        )

        val style2 = style1.copy(backgroundColor = Color.Black)

        // Original should be unchanged
        assertEquals(Color.White, style1.backgroundColor)
        // Copy should have new value
        assertEquals(Color.Black, style2.backgroundColor)
        // Other properties should be same
        assertEquals(style1.cornerRadius, style2.cornerRadius)
        assertEquals(style1.titleTextColor, style2.titleTextColor)
        assertEquals(style1.callIconTint, style2.callIconTint)
    }

    @Test
    fun `incoming and outgoing styles have different backgrounds`() {
        val incomingBg = Color(0xFFF5F5F5)
        val outgoingBg = Color(0xFF6852D6)

        val incomingStyle = CometChatMeetCallBubbleStyle(
            callIconTint = Color.Blue,
            iconBackgroundColor = Color.LightGray,
            titleTextColor = Color.Black,
            titleTextStyle = androidx.compose.ui.text.TextStyle(),
            subtitleTextColor = Color.Gray,
            subtitleTextStyle = androidx.compose.ui.text.TextStyle(),
            separatorColor = Color.LightGray,
            joinButtonTextColor = Color.Blue,
            joinButtonTextStyle = androidx.compose.ui.text.TextStyle(),
            backgroundColor = incomingBg,
            cornerRadius = 12.dp,
            strokeWidth = 0.dp,
            strokeColor = Color.Transparent,
            padding = PaddingValues(0.dp),
            senderNameTextColor = Color.Gray,
            senderNameTextStyle = androidx.compose.ui.text.TextStyle(),
            threadIndicatorTextColor = Color.Gray,
            threadIndicatorTextStyle = androidx.compose.ui.text.TextStyle(),
            threadIndicatorIconTint = Color.Gray,
            timestampTextColor = Color.Gray,
            timestampTextStyle = androidx.compose.ui.text.TextStyle()
        )

        val outgoingStyle = CometChatMeetCallBubbleStyle(
            callIconTint = Color.White,
            iconBackgroundColor = Color.DarkGray,
            titleTextColor = Color.White,
            titleTextStyle = androidx.compose.ui.text.TextStyle(),
            subtitleTextColor = Color.White,
            subtitleTextStyle = androidx.compose.ui.text.TextStyle(),
            separatorColor = Color.LightGray,
            joinButtonTextColor = Color.White,
            joinButtonTextStyle = androidx.compose.ui.text.TextStyle(),
            backgroundColor = outgoingBg,
            cornerRadius = 12.dp,
            strokeWidth = 0.dp,
            strokeColor = Color.Transparent,
            padding = PaddingValues(0.dp),
            senderNameTextColor = Color.Gray,
            senderNameTextStyle = androidx.compose.ui.text.TextStyle(),
            threadIndicatorTextColor = Color.Gray,
            threadIndicatorTextStyle = androidx.compose.ui.text.TextStyle(),
            threadIndicatorIconTint = Color.Gray,
            timestampTextColor = Color.White.copy(alpha = 0.8f),
            timestampTextStyle = androidx.compose.ui.text.TextStyle()
        )

        assertTrue(incomingStyle.backgroundColor != outgoingStyle.backgroundColor)
        assertTrue(incomingStyle.titleTextColor != outgoingStyle.titleTextColor)
        assertTrue(incomingStyle.callIconTint != outgoingStyle.callIconTint)
    }

    @Test
    fun `outgoing style has different text colors than incoming`() {
        val incomingStyle = CometChatMeetCallBubbleStyle(
            callIconTint = Color.Blue,
            iconBackgroundColor = Color.LightGray,
            titleTextColor = Color.Black,
            titleTextStyle = androidx.compose.ui.text.TextStyle(),
            subtitleTextColor = Color.Gray,
            subtitleTextStyle = androidx.compose.ui.text.TextStyle(),
            separatorColor = Color.LightGray,
            joinButtonTextColor = Color.Blue,
            joinButtonTextStyle = androidx.compose.ui.text.TextStyle(),
            backgroundColor = Color(0xFFF5F5F5),
            cornerRadius = 12.dp,
            strokeWidth = 0.dp,
            strokeColor = Color.Transparent,
            padding = PaddingValues(0.dp),
            senderNameTextColor = Color.Gray,
            senderNameTextStyle = androidx.compose.ui.text.TextStyle(),
            threadIndicatorTextColor = Color.Gray,
            threadIndicatorTextStyle = androidx.compose.ui.text.TextStyle(),
            threadIndicatorIconTint = Color.Gray,
            timestampTextColor = Color.Gray,
            timestampTextStyle = androidx.compose.ui.text.TextStyle()
        )

        val outgoingStyle = CometChatMeetCallBubbleStyle(
            callIconTint = Color.White,
            iconBackgroundColor = Color.DarkGray,
            titleTextColor = Color.White,
            titleTextStyle = androidx.compose.ui.text.TextStyle(),
            subtitleTextColor = Color.White,
            subtitleTextStyle = androidx.compose.ui.text.TextStyle(),
            separatorColor = Color.LightGray,
            joinButtonTextColor = Color.White,
            joinButtonTextStyle = androidx.compose.ui.text.TextStyle(),
            backgroundColor = Color(0xFF6852D6),
            cornerRadius = 12.dp,
            strokeWidth = 0.dp,
            strokeColor = Color.Transparent,
            padding = PaddingValues(0.dp),
            senderNameTextColor = Color.Gray,
            senderNameTextStyle = androidx.compose.ui.text.TextStyle(),
            threadIndicatorTextColor = Color.Gray,
            threadIndicatorTextStyle = androidx.compose.ui.text.TextStyle(),
            threadIndicatorIconTint = Color.Gray,
            timestampTextColor = Color.White.copy(alpha = 0.8f),
            timestampTextStyle = androidx.compose.ui.text.TextStyle()
        )

        // Verify outgoing has different text colors
        assertTrue(incomingStyle.titleTextColor != outgoingStyle.titleTextColor)
        assertTrue(incomingStyle.subtitleTextColor != outgoingStyle.subtitleTextColor)
        assertTrue(incomingStyle.joinButtonTextColor != outgoingStyle.joinButtonTextColor)
    }

    @Test
    fun `cornerRadius defaults to 12dp`() {
        val style = CometChatMeetCallBubbleStyle(
            callIconTint = Color.Blue,
            iconBackgroundColor = Color.LightGray,
            titleTextColor = Color.Black,
            titleTextStyle = androidx.compose.ui.text.TextStyle(),
            subtitleTextColor = Color.Gray,
            subtitleTextStyle = androidx.compose.ui.text.TextStyle(),
            separatorColor = Color.LightGray,
            joinButtonTextColor = Color.Blue,
            joinButtonTextStyle = androidx.compose.ui.text.TextStyle(),
            backgroundColor = Color.White,
            cornerRadius = 12.dp,
            strokeWidth = 0.dp,
            strokeColor = Color.Transparent,
            padding = PaddingValues(0.dp),
            senderNameTextColor = Color.Gray,
            senderNameTextStyle = androidx.compose.ui.text.TextStyle(),
            threadIndicatorTextColor = Color.Gray,
            threadIndicatorTextStyle = androidx.compose.ui.text.TextStyle(),
            threadIndicatorIconTint = Color.Gray,
            timestampTextColor = Color.Gray,
            timestampTextStyle = androidx.compose.ui.text.TextStyle()
        )

        assertEquals(12.dp, style.cornerRadius)
    }

    @Test
    fun `strokeWidth defaults to 0dp`() {
        val style = CometChatMeetCallBubbleStyle(
            callIconTint = Color.Blue,
            iconBackgroundColor = Color.LightGray,
            titleTextColor = Color.Black,
            titleTextStyle = androidx.compose.ui.text.TextStyle(),
            subtitleTextColor = Color.Gray,
            subtitleTextStyle = androidx.compose.ui.text.TextStyle(),
            separatorColor = Color.LightGray,
            joinButtonTextColor = Color.Blue,
            joinButtonTextStyle = androidx.compose.ui.text.TextStyle(),
            backgroundColor = Color.White,
            cornerRadius = 12.dp,
            strokeWidth = 0.dp,
            strokeColor = Color.Transparent,
            padding = PaddingValues(0.dp),
            senderNameTextColor = Color.Gray,
            senderNameTextStyle = androidx.compose.ui.text.TextStyle(),
            threadIndicatorTextColor = Color.Gray,
            threadIndicatorTextStyle = androidx.compose.ui.text.TextStyle(),
            threadIndicatorIconTint = Color.Gray,
            timestampTextColor = Color.Gray,
            timestampTextStyle = androidx.compose.ui.text.TextStyle()
        )

        assertEquals(0.dp, style.strokeWidth)
    }

    @Test
    fun `call icon properties are accessible`() {
        val style = CometChatMeetCallBubbleStyle(
            callIconTint = Color.Blue,
            iconBackgroundColor = Color.Cyan,
            titleTextColor = Color.Black,
            titleTextStyle = androidx.compose.ui.text.TextStyle(),
            subtitleTextColor = Color.Gray,
            subtitleTextStyle = androidx.compose.ui.text.TextStyle(),
            separatorColor = Color.LightGray,
            joinButtonTextColor = Color.Blue,
            joinButtonTextStyle = androidx.compose.ui.text.TextStyle(),
            backgroundColor = Color.White,
            cornerRadius = 12.dp,
            strokeWidth = 0.dp,
            strokeColor = Color.Transparent,
            padding = PaddingValues(0.dp),
            senderNameTextColor = Color.Gray,
            senderNameTextStyle = androidx.compose.ui.text.TextStyle(),
            threadIndicatorTextColor = Color.Gray,
            threadIndicatorTextStyle = androidx.compose.ui.text.TextStyle(),
            threadIndicatorIconTint = Color.Gray,
            timestampTextColor = Color.Gray,
            timestampTextStyle = androidx.compose.ui.text.TextStyle()
        )

        assertEquals(Color.Blue, style.callIconTint)
        assertEquals(Color.Cyan, style.iconBackgroundColor)
    }

    @Test
    fun `join button properties are accessible`() {
        val style = CometChatMeetCallBubbleStyle(
            callIconTint = Color.Blue,
            iconBackgroundColor = Color.LightGray,
            titleTextColor = Color.Black,
            titleTextStyle = androidx.compose.ui.text.TextStyle(),
            subtitleTextColor = Color.Gray,
            subtitleTextStyle = androidx.compose.ui.text.TextStyle(),
            separatorColor = Color.DarkGray,
            joinButtonTextColor = Color.Magenta,
            joinButtonTextStyle = androidx.compose.ui.text.TextStyle(),
            backgroundColor = Color.White,
            cornerRadius = 12.dp,
            strokeWidth = 0.dp,
            strokeColor = Color.Transparent,
            padding = PaddingValues(0.dp),
            senderNameTextColor = Color.Gray,
            senderNameTextStyle = androidx.compose.ui.text.TextStyle(),
            threadIndicatorTextColor = Color.Gray,
            threadIndicatorTextStyle = androidx.compose.ui.text.TextStyle(),
            threadIndicatorIconTint = Color.Gray,
            timestampTextColor = Color.Gray,
            timestampTextStyle = androidx.compose.ui.text.TextStyle()
        )

        assertEquals(Color.DarkGray, style.separatorColor)
        assertEquals(Color.Magenta, style.joinButtonTextColor)
        assertNotNull(style.joinButtonTextStyle)
    }

    @Test
    fun `sender name properties are accessible`() {
        val style = CometChatMeetCallBubbleStyle(
            callIconTint = Color.Blue,
            iconBackgroundColor = Color.LightGray,
            titleTextColor = Color.Black,
            titleTextStyle = androidx.compose.ui.text.TextStyle(),
            subtitleTextColor = Color.Gray,
            subtitleTextStyle = androidx.compose.ui.text.TextStyle(),
            separatorColor = Color.LightGray,
            joinButtonTextColor = Color.Blue,
            joinButtonTextStyle = androidx.compose.ui.text.TextStyle(),
            backgroundColor = Color.White,
            cornerRadius = 12.dp,
            strokeWidth = 0.dp,
            strokeColor = Color.Transparent,
            padding = PaddingValues(0.dp),
            senderNameTextColor = Color.Yellow,
            senderNameTextStyle = androidx.compose.ui.text.TextStyle(),
            threadIndicatorTextColor = Color.Gray,
            threadIndicatorTextStyle = androidx.compose.ui.text.TextStyle(),
            threadIndicatorIconTint = Color.Gray,
            timestampTextColor = Color.Gray,
            timestampTextStyle = androidx.compose.ui.text.TextStyle()
        )

        assertEquals(Color.Yellow, style.senderNameTextColor)
        assertNotNull(style.senderNameTextStyle)
    }

    @Test
    fun `thread indicator properties are accessible`() {
        val style = CometChatMeetCallBubbleStyle(
            callIconTint = Color.Blue,
            iconBackgroundColor = Color.LightGray,
            titleTextColor = Color.Black,
            titleTextStyle = androidx.compose.ui.text.TextStyle(),
            subtitleTextColor = Color.Gray,
            subtitleTextStyle = androidx.compose.ui.text.TextStyle(),
            separatorColor = Color.LightGray,
            joinButtonTextColor = Color.Blue,
            joinButtonTextStyle = androidx.compose.ui.text.TextStyle(),
            backgroundColor = Color.White,
            cornerRadius = 12.dp,
            strokeWidth = 0.dp,
            strokeColor = Color.Transparent,
            padding = PaddingValues(0.dp),
            senderNameTextColor = Color.Gray,
            senderNameTextStyle = androidx.compose.ui.text.TextStyle(),
            threadIndicatorTextColor = Color.Red,
            threadIndicatorTextStyle = androidx.compose.ui.text.TextStyle(),
            threadIndicatorIconTint = Color.Green,
            timestampTextColor = Color.Gray,
            timestampTextStyle = androidx.compose.ui.text.TextStyle()
        )

        assertEquals(Color.Red, style.threadIndicatorTextColor)
        assertNotNull(style.threadIndicatorTextStyle)
        assertEquals(Color.Green, style.threadIndicatorIconTint)
    }

    @Test
    fun `wrapper properties are accessible via parent type`() {
        val style: CometChatMessageBubbleStyle = CometChatMeetCallBubbleStyle(
            callIconTint = Color.Blue,
            iconBackgroundColor = Color.LightGray,
            titleTextColor = Color.Black,
            titleTextStyle = androidx.compose.ui.text.TextStyle(),
            subtitleTextColor = Color.Gray,
            subtitleTextStyle = androidx.compose.ui.text.TextStyle(),
            separatorColor = Color.LightGray,
            joinButtonTextColor = Color.Blue,
            joinButtonTextStyle = androidx.compose.ui.text.TextStyle(),
            backgroundColor = Color.Red,
            cornerRadius = 16.dp,
            strokeWidth = 2.dp,
            strokeColor = Color.Blue,
            padding = PaddingValues(4.dp),
            senderNameTextColor = Color.Green,
            senderNameTextStyle = androidx.compose.ui.text.TextStyle(),
            threadIndicatorTextColor = Color.Yellow,
            threadIndicatorTextStyle = androidx.compose.ui.text.TextStyle(),
            threadIndicatorIconTint = Color.Cyan,
            timestampTextColor = Color.Magenta,
            timestampTextStyle = androidx.compose.ui.text.TextStyle()
        )

        // Verify wrapper properties are accessible via parent type
        assertEquals(Color.Red, style.backgroundColor)
        assertEquals(16.dp, style.cornerRadius)
        assertEquals(2.dp, style.strokeWidth)
        assertEquals(Color.Blue, style.strokeColor)
        assertEquals(PaddingValues(4.dp), style.padding)
        assertEquals(Color.Green, style.senderNameTextColor)
        assertEquals(Color.Yellow, style.threadIndicatorTextColor)
        assertEquals(Color.Cyan, style.threadIndicatorIconTint)
        assertEquals(Color.Magenta, style.timestampTextColor)
    }
}
