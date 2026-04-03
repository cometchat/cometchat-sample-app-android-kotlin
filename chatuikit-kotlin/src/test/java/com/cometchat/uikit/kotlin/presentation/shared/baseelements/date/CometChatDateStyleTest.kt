package com.cometchat.uikit.kotlin.presentation.shared.baseelements.date

import android.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Unit tests for CometChatDateStyle data class.
 */
class CometChatDateStyleTest {

    @Test
    fun `default constructor should set all values to zero`() {
        val style = CometChatDateStyle()
        
        assertEquals(0, style.backgroundColor)
        assertEquals(0, style.textAppearance)
        assertEquals(0, style.textColor)
        assertEquals(0, style.cornerRadius)
        assertEquals(0, style.strokeWidth)
        assertEquals(0, style.strokeColor)
    }

    @Test
    fun `custom values should be correctly assigned`() {
        val backgroundColor = Color.RED
        val textAppearance = 123
        val textColor = Color.WHITE
        val cornerRadius = 8
        val strokeWidth = 2
        val strokeColor = Color.BLUE
        
        val style = CometChatDateStyle(
            backgroundColor = backgroundColor,
            textAppearance = textAppearance,
            textColor = textColor,
            cornerRadius = cornerRadius,
            strokeWidth = strokeWidth,
            strokeColor = strokeColor
        )
        
        assertEquals(backgroundColor, style.backgroundColor)
        assertEquals(textAppearance, style.textAppearance)
        assertEquals(textColor, style.textColor)
        assertEquals(cornerRadius, style.cornerRadius)
        assertEquals(strokeWidth, style.strokeWidth)
        assertEquals(strokeColor, style.strokeColor)
    }

    @Test
    fun `copy should create new instance with modified values`() {
        val original = CometChatDateStyle(
            backgroundColor = Color.RED,
            textColor = Color.WHITE
        )
        
        val copied = original.copy(backgroundColor = Color.GREEN)
        
        assertEquals(Color.GREEN, copied.backgroundColor)
        assertEquals(Color.WHITE, copied.textColor) // Unchanged
    }

    @Test
    fun `equals should return true for identical styles`() {
        val style1 = CometChatDateStyle(
            backgroundColor = Color.RED,
            textColor = Color.WHITE,
            cornerRadius = 8
        )
        
        val style2 = CometChatDateStyle(
            backgroundColor = Color.RED,
            textColor = Color.WHITE,
            cornerRadius = 8
        )
        
        assertEquals(style1, style2)
        assertEquals(style1.hashCode(), style2.hashCode())
    }

    @Test
    fun `equals should return false for different styles`() {
        val style1 = CometChatDateStyle(backgroundColor = Color.RED)
        val style2 = CometChatDateStyle(backgroundColor = Color.BLUE)
        
        assertNotEquals(style1, style2)
    }
}
