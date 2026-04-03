package com.cometchat.uikit.kotlin.presentation.shared.baseelements.badgecount

import android.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Unit tests for CometChatBadgeCountStyle data class.
 */
class CometChatBadgeCountStyleTest {

    @Test
    fun `default constructor should set all values to zero`() {
        val style = CometChatBadgeCountStyle()
        
        assertEquals(0, style.backgroundColor)
        assertEquals(0, style.textColor)
        assertEquals(0, style.textAppearance)
        assertEquals(0f, style.cornerRadius, 0.001f)
        assertEquals(0, style.borderColor)
        assertEquals(0f, style.borderWidth, 0.001f)
    }

    @Test
    fun `custom values should be correctly assigned`() {
        val backgroundColor = Color.RED
        val textColor = Color.WHITE
        val textAppearance = 123
        val cornerRadius = 16f
        val borderColor = Color.BLUE
        val borderWidth = 2f
        
        val style = CometChatBadgeCountStyle(
            backgroundColor = backgroundColor,
            textColor = textColor,
            textAppearance = textAppearance,
            cornerRadius = cornerRadius,
            borderColor = borderColor,
            borderWidth = borderWidth
        )
        
        assertEquals(backgroundColor, style.backgroundColor)
        assertEquals(textColor, style.textColor)
        assertEquals(textAppearance, style.textAppearance)
        assertEquals(cornerRadius, style.cornerRadius, 0.001f)
        assertEquals(borderColor, style.borderColor)
        assertEquals(borderWidth, style.borderWidth, 0.001f)
    }

    @Test
    fun `copy should create new instance with modified values`() {
        val original = CometChatBadgeCountStyle(
            backgroundColor = Color.RED,
            textColor = Color.WHITE
        )
        
        val copied = original.copy(backgroundColor = Color.GREEN)
        
        assertEquals(Color.GREEN, copied.backgroundColor)
        assertEquals(Color.WHITE, copied.textColor) // Unchanged
    }

    @Test
    fun `equals should return true for identical styles`() {
        val style1 = CometChatBadgeCountStyle(
            backgroundColor = Color.RED,
            textColor = Color.WHITE,
            cornerRadius = 16f
        )
        
        val style2 = CometChatBadgeCountStyle(
            backgroundColor = Color.RED,
            textColor = Color.WHITE,
            cornerRadius = 16f
        )
        
        assertEquals(style1, style2)
        assertEquals(style1.hashCode(), style2.hashCode())
    }

    @Test
    fun `equals should return false for different styles`() {
        val style1 = CometChatBadgeCountStyle(backgroundColor = Color.RED)
        val style2 = CometChatBadgeCountStyle(backgroundColor = Color.BLUE)
        
        assertNotEquals(style1, style2)
    }

    @Test
    fun `max corner radius should create circular badge`() {
        val style = CometChatBadgeCountStyle(
            cornerRadius = Float.MAX_VALUE
        )
        
        assertEquals(Float.MAX_VALUE, style.cornerRadius, 0.001f)
    }
}
