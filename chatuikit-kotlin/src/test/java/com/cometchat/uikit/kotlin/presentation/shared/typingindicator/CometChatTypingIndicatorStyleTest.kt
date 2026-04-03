package com.cometchat.uikit.kotlin.presentation.shared.typingindicator

import android.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Unit tests for CometChatTypingIndicatorStyle data class.
 */
class CometChatTypingIndicatorStyleTest {

    @Test
    fun `default constructor should set default animation duration`() {
        val style = CometChatTypingIndicatorStyle()
        
        assertEquals(0, style.textColor)
        assertEquals(0, style.textAppearance)
        assertEquals(0, style.dotColor)
        assertEquals(300L, style.animationDuration)
    }

    @Test
    fun `custom values should be correctly assigned`() {
        val textColor = Color.BLUE
        val textAppearance = 123
        val dotColor = Color.GREEN
        val animationDuration = 500L
        
        val style = CometChatTypingIndicatorStyle(
            textColor = textColor,
            textAppearance = textAppearance,
            dotColor = dotColor,
            animationDuration = animationDuration
        )
        
        assertEquals(textColor, style.textColor)
        assertEquals(textAppearance, style.textAppearance)
        assertEquals(dotColor, style.dotColor)
        assertEquals(animationDuration, style.animationDuration)
    }

    @Test
    fun `copy should create new instance with modified values`() {
        val original = CometChatTypingIndicatorStyle(
            textColor = Color.BLUE,
            dotColor = Color.GREEN
        )
        
        val copied = original.copy(textColor = Color.RED)
        
        assertEquals(Color.RED, copied.textColor)
        assertEquals(Color.GREEN, copied.dotColor) // Unchanged
    }

    @Test
    fun `equals should return true for identical styles`() {
        val style1 = CometChatTypingIndicatorStyle(
            textColor = Color.BLUE,
            dotColor = Color.GREEN,
            animationDuration = 500L
        )
        
        val style2 = CometChatTypingIndicatorStyle(
            textColor = Color.BLUE,
            dotColor = Color.GREEN,
            animationDuration = 500L
        )
        
        assertEquals(style1, style2)
        assertEquals(style1.hashCode(), style2.hashCode())
    }

    @Test
    fun `equals should return false for different styles`() {
        val style1 = CometChatTypingIndicatorStyle(textColor = Color.BLUE)
        val style2 = CometChatTypingIndicatorStyle(textColor = Color.RED)
        
        assertNotEquals(style1, style2)
    }

    @Test
    fun `animation duration should accept custom values`() {
        val fastAnimation = CometChatTypingIndicatorStyle(animationDuration = 100L)
        val slowAnimation = CometChatTypingIndicatorStyle(animationDuration = 1000L)
        
        assertEquals(100L, fastAnimation.animationDuration)
        assertEquals(1000L, slowAnimation.animationDuration)
    }

    @Test
    fun `default animation duration should be 300ms`() {
        val style = CometChatTypingIndicatorStyle()
        
        assertEquals(300L, style.animationDuration)
    }
}
