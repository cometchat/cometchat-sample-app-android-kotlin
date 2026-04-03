package com.cometchat.uikit.kotlin.presentation.shared.receipts

import android.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for CometChatReceiptStyle data class.
 */
class CometChatReceiptStyleTest {

    @Test
    fun `default constructor should set all values to zero or null`() {
        val style = CometChatReceiptStyle()
        
        assertNull(style.sentIcon)
        assertNull(style.deliveredIcon)
        assertNull(style.readIcon)
        assertNull(style.errorIcon)
        assertNull(style.waitIcon)
        assertEquals(0, style.sentIconTint)
        assertEquals(0, style.deliveredIconTint)
        assertEquals(0, style.readIconTint)
        assertEquals(0, style.errorIconTint)
        assertEquals(0, style.waitIconTint)
        assertEquals(0, style.iconSize)
    }

    @Test
    fun `custom tint values should be correctly assigned`() {
        val sentTint = Color.GRAY
        val deliveredTint = Color.GRAY
        val readTint = Color.BLUE
        val errorTint = Color.RED
        val waitTint = Color.YELLOW
        val iconSize = 24
        
        val style = CometChatReceiptStyle(
            sentIconTint = sentTint,
            deliveredIconTint = deliveredTint,
            readIconTint = readTint,
            errorIconTint = errorTint,
            waitIconTint = waitTint,
            iconSize = iconSize
        )
        
        assertEquals(sentTint, style.sentIconTint)
        assertEquals(deliveredTint, style.deliveredIconTint)
        assertEquals(readTint, style.readIconTint)
        assertEquals(errorTint, style.errorIconTint)
        assertEquals(waitTint, style.waitIconTint)
        assertEquals(iconSize, style.iconSize)
    }

    @Test
    fun `copy should create new instance with modified values`() {
        val original = CometChatReceiptStyle(
            sentIconTint = Color.GRAY,
            readIconTint = Color.BLUE
        )
        
        val copied = original.copy(readIconTint = Color.GREEN)
        
        assertEquals(Color.GREEN, copied.readIconTint)
        assertEquals(Color.GRAY, copied.sentIconTint) // Unchanged
    }

    @Test
    fun `equals should return true for identical styles`() {
        val style1 = CometChatReceiptStyle(
            sentIconTint = Color.GRAY,
            deliveredIconTint = Color.GRAY,
            readIconTint = Color.BLUE,
            iconSize = 24
        )
        
        val style2 = CometChatReceiptStyle(
            sentIconTint = Color.GRAY,
            deliveredIconTint = Color.GRAY,
            readIconTint = Color.BLUE,
            iconSize = 24
        )
        
        assertEquals(style1, style2)
        assertEquals(style1.hashCode(), style2.hashCode())
    }

    @Test
    fun `equals should return false for different styles`() {
        val style1 = CometChatReceiptStyle(readIconTint = Color.BLUE)
        val style2 = CometChatReceiptStyle(readIconTint = Color.GREEN)
        
        assertNotEquals(style1, style2)
    }

    @Test
    fun `partial style should only set specified tints`() {
        val style = CometChatReceiptStyle(
            readIconTint = Color.BLUE,
            errorIconTint = Color.RED
        )
        
        assertEquals(Color.BLUE, style.readIconTint)
        assertEquals(Color.RED, style.errorIconTint)
        assertEquals(0, style.sentIconTint) // Default
        assertEquals(0, style.deliveredIconTint) // Default
    }
}
