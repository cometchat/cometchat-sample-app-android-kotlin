package com.cometchat.uikit.kotlin.presentation.shared.statusindicator

import android.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for CometChatStatusIndicatorStyle data class.
 */
class CometChatStatusIndicatorStyleTest {

    @Test
    fun `default constructor should set all values to zero or null`() {
        val style = CometChatStatusIndicatorStyle()
        
        assertEquals(0, style.strokeColor)
        assertEquals(0f, style.strokeWidth, 0.001f)
        assertEquals(0f, style.cornerRadius, 0.001f)
        assertNull(style.onlineIcon)
        assertNull(style.privateGroupIcon)
        assertNull(style.protectedGroupIcon)
    }

    @Test
    fun `custom values should be correctly assigned`() {
        val strokeColor = Color.RED
        val strokeWidth = 2f
        val cornerRadius = 16f
        
        val style = CometChatStatusIndicatorStyle(
            strokeColor = strokeColor,
            strokeWidth = strokeWidth,
            cornerRadius = cornerRadius
        )
        
        assertEquals(strokeColor, style.strokeColor)
        assertEquals(strokeWidth, style.strokeWidth, 0.001f)
        assertEquals(cornerRadius, style.cornerRadius, 0.001f)
    }

    @Test
    fun `copy should create new instance with modified values`() {
        val original = CometChatStatusIndicatorStyle(
            strokeColor = Color.RED,
            strokeWidth = 2f
        )
        
        val copied = original.copy(strokeColor = Color.GREEN)
        
        assertEquals(Color.GREEN, copied.strokeColor)
        assertEquals(2f, copied.strokeWidth, 0.001f) // Unchanged
    }

    @Test
    fun `equals should return true for identical styles`() {
        val style1 = CometChatStatusIndicatorStyle(
            strokeColor = Color.RED,
            strokeWidth = 2f,
            cornerRadius = 16f
        )
        
        val style2 = CometChatStatusIndicatorStyle(
            strokeColor = Color.RED,
            strokeWidth = 2f,
            cornerRadius = 16f
        )
        
        assertEquals(style1, style2)
        assertEquals(style1.hashCode(), style2.hashCode())
    }

    @Test
    fun `equals should return false for different styles`() {
        val style1 = CometChatStatusIndicatorStyle(strokeColor = Color.RED)
        val style2 = CometChatStatusIndicatorStyle(strokeColor = Color.BLUE)
        
        assertNotEquals(style1, style2)
    }
}
