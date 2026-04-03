package com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar

import android.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Unit tests for CometChatAvatarStyle data class.
 * 
 * Tests verify:
 * - Default values are correctly set
 * - Custom values override defaults
 * - Data class equality and copy functionality
 */
class CometChatAvatarStyleTest {

    @Test
    fun `default constructor should set all values to zero or default`() {
        val style = CometChatAvatarStyle()
        
        assertEquals(0, style.backgroundColor)
        assertEquals(0, style.strokeColor)
        assertEquals(0f, style.strokeWidth, 0.001f)
        assertEquals(0f, style.cornerRadius, 0.001f)
        assertEquals(0, style.placeHolderTextAppearance)
        assertEquals(0, style.placeHolderTextColor)
    }

    @Test
    fun `custom values should be correctly assigned`() {
        val backgroundColor = Color.RED
        val strokeColor = Color.BLUE
        val strokeWidth = 2f
        val cornerRadius = 16f
        val textAppearance = 123
        val textColor = Color.WHITE
        
        val style = CometChatAvatarStyle(
            backgroundColor = backgroundColor,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth,
            cornerRadius = cornerRadius,
            placeHolderTextAppearance = textAppearance,
            placeHolderTextColor = textColor
        )
        
        assertEquals(backgroundColor, style.backgroundColor)
        assertEquals(strokeColor, style.strokeColor)
        assertEquals(strokeWidth, style.strokeWidth, 0.001f)
        assertEquals(cornerRadius, style.cornerRadius, 0.001f)
        assertEquals(textAppearance, style.placeHolderTextAppearance)
        assertEquals(textColor, style.placeHolderTextColor)
    }

    @Test
    fun `copy should create new instance with modified values`() {
        val original = CometChatAvatarStyle(
            backgroundColor = Color.RED,
            strokeColor = Color.BLUE
        )
        
        val copied = original.copy(backgroundColor = Color.GREEN)
        
        assertEquals(Color.GREEN, copied.backgroundColor)
        assertEquals(Color.BLUE, copied.strokeColor) // Unchanged
        assertNotEquals(original.backgroundColor, copied.backgroundColor)
    }

    @Test
    fun `equals should return true for identical styles`() {
        val style1 = CometChatAvatarStyle(
            backgroundColor = Color.RED,
            strokeColor = Color.BLUE,
            strokeWidth = 2f,
            cornerRadius = 16f
        )
        
        val style2 = CometChatAvatarStyle(
            backgroundColor = Color.RED,
            strokeColor = Color.BLUE,
            strokeWidth = 2f,
            cornerRadius = 16f
        )
        
        assertEquals(style1, style2)
        assertEquals(style1.hashCode(), style2.hashCode())
    }

    @Test
    fun `equals should return false for different styles`() {
        val style1 = CometChatAvatarStyle(backgroundColor = Color.RED)
        val style2 = CometChatAvatarStyle(backgroundColor = Color.BLUE)
        
        assertNotEquals(style1, style2)
    }

    @Test
    fun `partial style should only set specified values`() {
        val style = CometChatAvatarStyle(
            backgroundColor = Color.RED
            // Other values remain default (0)
        )
        
        assertEquals(Color.RED, style.backgroundColor)
        assertEquals(0, style.strokeColor)
        assertEquals(0f, style.strokeWidth, 0.001f)
    }
}
