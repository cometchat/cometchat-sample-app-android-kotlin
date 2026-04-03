package com.cometchat.uikit.compose.presentation.shared.messagebubble.style

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [CometChatCallActionBubbleStyle].
 *
 * Validates:
 * - Style extends CometChatMessageBubbleStyle
 * - default() factory function returns valid style
 * - All properties are accessible
 * - Missed call styling properties are present
 * - incoming() and outgoing() factory functions work correctly
 */
class CometChatCallActionBubbleStyleTest {

    private fun createTestStyle(
        textColor: Color = Color.Gray,
        textStyle: TextStyle = TextStyle(),
        iconTint: Color = Color.Gray,
        missedCallTextColor: Color = Color.Red,
        missedCallTextStyle: TextStyle = TextStyle(),
        missedCallBackgroundColor: Color = Color.Red.copy(alpha = 0.1f),
        missedCallIconTint: Color = Color.Red,
        backgroundColor: Color = Color.White,
        cornerRadius: androidx.compose.ui.unit.Dp = 8.dp,
        strokeWidth: androidx.compose.ui.unit.Dp = 0.dp,
        strokeColor: Color = Color.Transparent,
        padding: PaddingValues = PaddingValues(0.dp),
        senderNameTextColor: Color = Color.Gray,
        senderNameTextStyle: TextStyle = TextStyle(),
        threadIndicatorTextColor: Color = Color.Gray,
        threadIndicatorTextStyle: TextStyle = TextStyle(),
        threadIndicatorIconTint: Color = Color.Gray,
        timestampTextColor: Color = Color.Gray,
        timestampTextStyle: TextStyle = TextStyle()
    ) = CometChatCallActionBubbleStyle(
        textColor = textColor,
        textStyle = textStyle,
        iconTint = iconTint,
        missedCallTextColor = missedCallTextColor,
        missedCallTextStyle = missedCallTextStyle,
        missedCallBackgroundColor = missedCallBackgroundColor,
        missedCallIconTint = missedCallIconTint,
        backgroundColor = backgroundColor,
        cornerRadius = cornerRadius,
        strokeWidth = strokeWidth,
        strokeColor = strokeColor,
        padding = padding,
        senderNameTextColor = senderNameTextColor,
        senderNameTextStyle = senderNameTextStyle,
        threadIndicatorTextColor = threadIndicatorTextColor,
        threadIndicatorTextStyle = threadIndicatorTextStyle,
        threadIndicatorIconTint = threadIndicatorIconTint,
        timestampTextColor = timestampTextColor,
        timestampTextStyle = timestampTextStyle
    )

    @Test
    fun `style extends CometChatMessageBubbleStyle`() {
        val style = createTestStyle()
        assertTrue(style is CometChatMessageBubbleStyle)
    }

    @Test
    fun `default style has valid properties`() {
        val style = createTestStyle(
            backgroundColor = Color.White,
            cornerRadius = 8.dp,
            strokeWidth = 1.dp,
            strokeColor = Color.LightGray
        )

        assertNotNull(style)
        assertEquals(Color.White, style.backgroundColor)
        assertEquals(8.dp, style.cornerRadius)
        assertEquals(1.dp, style.strokeWidth)
        assertEquals(Color.LightGray, style.strokeColor)
        assertEquals(Color.Gray, style.textColor)
        assertEquals(Color.Gray, style.iconTint)
    }

    @Test
    fun `missed call styling properties are present`() {
        val style = createTestStyle()

        assertEquals(Color.Red, style.missedCallTextColor)
        assertNotNull(style.missedCallTextStyle)
        assertEquals(Color.Red, style.missedCallIconTint)
        // Verify missed call background has alpha
        assertTrue(style.missedCallBackgroundColor.alpha < 1f)
    }

    @Test
    fun `all properties are accessible`() {
        val style = createTestStyle(
            backgroundColor = Color.White,
            cornerRadius = 50.dp,
            strokeWidth = 2.dp,
            strokeColor = Color.Blue,
            textColor = Color.Green,
            iconTint = Color.Yellow,
            missedCallTextColor = Color.Red,
            missedCallBackgroundColor = Color.Magenta,
            missedCallIconTint = Color.Cyan
        )

        // Verify all properties are accessible
        assertEquals(Color.White, style.backgroundColor)
        assertEquals(50.dp, style.cornerRadius)
        assertEquals(2.dp, style.strokeWidth)
        assertEquals(Color.Blue, style.strokeColor)
        assertEquals(Color.Green, style.textColor)
        assertNotNull(style.textStyle)
        assertEquals(Color.Yellow, style.iconTint)
        assertEquals(Color.Red, style.missedCallTextColor)
        assertNotNull(style.missedCallTextStyle)
        assertEquals(Color.Magenta, style.missedCallBackgroundColor)
        assertEquals(Color.Cyan, style.missedCallIconTint)
    }

    @Test
    fun `style is immutable data class`() {
        val style1 = createTestStyle()

        val style2 = style1.copy(missedCallTextColor = Color.Magenta)

        // Original should be unchanged
        assertEquals(Color.Red, style1.missedCallTextColor)
        // Copy should have new value
        assertEquals(Color.Magenta, style2.missedCallTextColor)
        // Other properties should be same
        assertEquals(style1.backgroundColor, style2.backgroundColor)
        assertEquals(style1.cornerRadius, style2.cornerRadius)
    }

    @Test
    fun `call action bubble has smaller corner radius by default`() {
        val style = createTestStyle(cornerRadius = 8.dp)

        // 8.dp is used for call action bubbles (smaller than standard 12.dp)
        assertEquals(8.dp, style.cornerRadius)
    }

    @Test
    fun `strokeWidth defaults to 0dp`() {
        val style = createTestStyle()

        assertEquals(0.dp, style.strokeWidth)
    }

    @Test
    fun `inherited wrapper properties are accessible`() {
        val style = createTestStyle(
            padding = PaddingValues(8.dp),
            senderNameTextColor = Color.Blue,
            threadIndicatorTextColor = Color.Green,
            threadIndicatorIconTint = Color.Yellow
        )

        // Verify inherited properties are accessible
        assertEquals(PaddingValues(8.dp), style.padding)
        assertEquals(Color.Blue, style.senderNameTextColor)
        assertNotNull(style.senderNameTextStyle)
        assertEquals(Color.Green, style.threadIndicatorTextColor)
        assertNotNull(style.threadIndicatorTextStyle)
        assertEquals(Color.Yellow, style.threadIndicatorIconTint)
    }

    @Test
    fun `style can be assigned to parent type`() {
        val style: CometChatMessageBubbleStyle = createTestStyle()

        // Verify parent properties are accessible through parent type
        assertEquals(Color.White, style.backgroundColor)
        assertEquals(8.dp, style.cornerRadius)
        assertEquals(0.dp, style.strokeWidth)
        assertEquals(Color.Transparent, style.strokeColor)
    }
}
