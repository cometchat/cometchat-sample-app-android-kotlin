package com.cometchat.uikit.kotlin.presentation.conversationlist.style

import android.graphics.Color
import com.cometchat.uikit.kotlin.presentation.conversations.style.CometChatConversationListItemStyle
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar.CometChatAvatarStyle
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.badgecount.CometChatBadgeCountStyle
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.date.CometChatDateStyle
import com.cometchat.uikit.kotlin.presentation.shared.receipts.CometChatReceiptStyle
import com.cometchat.uikit.kotlin.presentation.shared.statusindicator.CometChatStatusIndicatorStyle
import com.cometchat.uikit.kotlin.presentation.shared.typingindicator.CometChatTypingIndicatorStyle
import com.cometchat.uikit.kotlin.shared.formatters.style.CometChatMentionStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for CometChatConversationListItemStyle data class.
 */
class CometChatConversationListItemStyleTest {

    @Test
    fun `default constructor should set all values to zero or default`() {
        val style = CometChatConversationListItemStyle()
        
        assertEquals(0, style.backgroundColor)
        assertEquals(0, style.selectedBackgroundColor)
        assertEquals(0, style.titleTextColor)
        assertEquals(0, style.titleTextAppearance)
        assertEquals(0, style.subtitleTextColor)
        assertEquals(0, style.subtitleTextAppearance)
        assertEquals(0, style.separatorColor)
        assertEquals(0, style.separatorHeight)
        assertNull(style.mentionStyle)
    }

    @Test
    fun `default constructor should initialize nested styles`() {
        val style = CometChatConversationListItemStyle()
        
        assertNotNull(style.avatarStyle)
        assertNotNull(style.statusIndicatorStyle)
        assertNotNull(style.dateStyle)
        assertNotNull(style.badgeCountStyle)
        assertNotNull(style.receiptStyle)
        assertNotNull(style.typingIndicatorStyle)
    }

    @Test
    fun `custom values should be correctly assigned`() {
        val backgroundColor = Color.WHITE
        val selectedBackgroundColor = Color.LTGRAY
        val titleTextColor = Color.BLACK
        val titleTextAppearance = 123
        val subtitleTextColor = Color.GRAY
        val subtitleTextAppearance = 456
        val separatorColor = Color.DKGRAY
        val separatorHeight = 2
        
        val style = CometChatConversationListItemStyle(
            backgroundColor = backgroundColor,
            selectedBackgroundColor = selectedBackgroundColor,
            titleTextColor = titleTextColor,
            titleTextAppearance = titleTextAppearance,
            subtitleTextColor = subtitleTextColor,
            subtitleTextAppearance = subtitleTextAppearance,
            separatorColor = separatorColor,
            separatorHeight = separatorHeight
        )
        
        assertEquals(backgroundColor, style.backgroundColor)
        assertEquals(selectedBackgroundColor, style.selectedBackgroundColor)
        assertEquals(titleTextColor, style.titleTextColor)
        assertEquals(titleTextAppearance, style.titleTextAppearance)
        assertEquals(subtitleTextColor, style.subtitleTextColor)
        assertEquals(subtitleTextAppearance, style.subtitleTextAppearance)
        assertEquals(separatorColor, style.separatorColor)
        assertEquals(separatorHeight, style.separatorHeight)
    }

    @Test
    fun `custom nested styles should be correctly assigned`() {
        val avatarStyle = CometChatAvatarStyle(backgroundColor = Color.RED)
        val statusIndicatorStyle = CometChatStatusIndicatorStyle(strokeColor = Color.GREEN)
        val dateStyle = CometChatDateStyle(textColor = Color.BLUE)
        val badgeCountStyle = CometChatBadgeCountStyle(backgroundColor = Color.YELLOW)
        val receiptStyle = CometChatReceiptStyle(readIconTint = Color.CYAN)
        val typingIndicatorStyle = CometChatTypingIndicatorStyle(textColor = Color.MAGENTA)
        
        val style = CometChatConversationListItemStyle(
            avatarStyle = avatarStyle,
            statusIndicatorStyle = statusIndicatorStyle,
            dateStyle = dateStyle,
            badgeCountStyle = badgeCountStyle,
            receiptStyle = receiptStyle,
            typingIndicatorStyle = typingIndicatorStyle
        )
        
        assertEquals(Color.RED, style.avatarStyle.backgroundColor)
        assertEquals(Color.GREEN, style.statusIndicatorStyle.strokeColor)
        assertEquals(Color.BLUE, style.dateStyle.textColor)
        assertEquals(Color.YELLOW, style.badgeCountStyle.backgroundColor)
        assertEquals(Color.CYAN, style.receiptStyle.readIconTint)
        assertEquals(Color.MAGENTA, style.typingIndicatorStyle.textColor)
    }

    @Test
    fun `copy should create new instance with modified values`() {
        val original = CometChatConversationListItemStyle(
            backgroundColor = Color.WHITE,
            titleTextColor = Color.BLACK
        )
        
        val copied = original.copy(backgroundColor = Color.GRAY)
        
        assertEquals(Color.GRAY, copied.backgroundColor)
        assertEquals(Color.BLACK, copied.titleTextColor) // Unchanged
    }

    @Test
    fun `copy should allow modifying nested styles`() {
        val original = CometChatConversationListItemStyle(
            avatarStyle = CometChatAvatarStyle(backgroundColor = Color.RED)
        )
        
        val newAvatarStyle = CometChatAvatarStyle(backgroundColor = Color.BLUE)
        val copied = original.copy(avatarStyle = newAvatarStyle)
        
        assertEquals(Color.BLUE, copied.avatarStyle.backgroundColor)
        assertEquals(Color.RED, original.avatarStyle.backgroundColor) // Original unchanged
    }

    @Test
    fun `equals should return true for identical styles`() {
        val style1 = CometChatConversationListItemStyle(
            backgroundColor = Color.WHITE,
            titleTextColor = Color.BLACK,
            separatorHeight = 1
        )
        
        val style2 = CometChatConversationListItemStyle(
            backgroundColor = Color.WHITE,
            titleTextColor = Color.BLACK,
            separatorHeight = 1
        )
        
        assertEquals(style1, style2)
        assertEquals(style1.hashCode(), style2.hashCode())
    }

    @Test
    fun `equals should return false for different styles`() {
        val style1 = CometChatConversationListItemStyle(backgroundColor = Color.WHITE)
        val style2 = CometChatConversationListItemStyle(backgroundColor = Color.BLACK)
        
        assertNotEquals(style1, style2)
    }

    @Test
    fun `equals should consider nested styles`() {
        val style1 = CometChatConversationListItemStyle(
            avatarStyle = CometChatAvatarStyle(backgroundColor = Color.RED)
        )
        val style2 = CometChatConversationListItemStyle(
            avatarStyle = CometChatAvatarStyle(backgroundColor = Color.BLUE)
        )
        
        assertNotEquals(style1, style2)
    }

    @Test
    fun `custom mentionStyle should be correctly assigned`() {
        val mentionStyle = CometChatMentionStyle(
            textColor = Color.BLUE,
            textStyle = null,
            backgroundColor = Color.LTGRAY,
            selfTextColor = Color.RED,
            selfTextStyle = null,
            selfBackgroundColor = Color.YELLOW
        )
        
        val style = CometChatConversationListItemStyle(
            mentionStyle = mentionStyle
        )
        
        assertNotNull(style.mentionStyle)
        assertEquals(Color.BLUE, style.mentionStyle?.textColor)
        assertEquals(Color.RED, style.mentionStyle?.selfTextColor)
    }

    @Test
    fun `copy should allow modifying mentionStyle`() {
        val original = CometChatConversationListItemStyle(
            mentionStyle = CometChatMentionStyle(
                textColor = Color.BLUE,
                textStyle = null,
                backgroundColor = Color.LTGRAY,
                selfTextColor = Color.RED,
                selfTextStyle = null,
                selfBackgroundColor = Color.YELLOW
            )
        )
        
        val newMentionStyle = CometChatMentionStyle(
            textColor = Color.GREEN,
            textStyle = null,
            backgroundColor = Color.WHITE,
            selfTextColor = Color.MAGENTA,
            selfTextStyle = null,
            selfBackgroundColor = Color.CYAN
        )
        val copied = original.copy(mentionStyle = newMentionStyle)
        
        assertEquals(Color.GREEN, copied.mentionStyle?.textColor)
        assertEquals(Color.BLUE, original.mentionStyle?.textColor) // Original unchanged
    }
}
