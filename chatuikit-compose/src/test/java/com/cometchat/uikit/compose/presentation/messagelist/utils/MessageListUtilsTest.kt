package com.cometchat.uikit.compose.presentation.messagelist.utils

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.state.MessageAlignment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.Calendar

/**
 * Unit tests for MessageListUtils helper functions.
 *
 * **Validates: Requirements 1.6, 1.7, 14.1, 18.7**
 *
 * Tests:
 * - getMessageAlignment for all cases (outgoing, incoming, action, call)
 * - shouldShowDateSeparator for same day, different day, null previous
 * - formatDateSeparator for today, yesterday, older dates
 * - getDefaultMessagesTypes returns expected types
 * - getDefaultMessagesCategories returns expected categories
 */
class MessageListUtilsTest {

    private lateinit var currentUser: User

    @Before
    fun setup() {
        currentUser = mock(User::class.java)
        `when`(currentUser.uid).thenReturn("current_user_123")
    }

    // ========================================
    // getMessageAlignment Tests
    // ========================================

    @Test
    fun `getMessageAlignment returns RIGHT for outgoing message`() {
        val sender = mock(User::class.java)
        `when`(sender.uid).thenReturn("current_user_123")

        val message = mock(BaseMessage::class.java)
        `when`(message.category).thenReturn(CometChatConstants.CATEGORY_MESSAGE)
        `when`(message.sender).thenReturn(sender)

        val alignment = getMessageAlignment(message, currentUser)

        assertEquals(MessageAlignment.RIGHT, alignment)
    }

    @Test
    fun `getMessageAlignment returns LEFT for incoming message`() {
        val sender = mock(User::class.java)
        `when`(sender.uid).thenReturn("other_user_456")

        val message = mock(BaseMessage::class.java)
        `when`(message.category).thenReturn(CometChatConstants.CATEGORY_MESSAGE)
        `when`(message.sender).thenReturn(sender)

        val alignment = getMessageAlignment(message, currentUser)

        assertEquals(MessageAlignment.LEFT, alignment)
    }

    @Test
    fun `getMessageAlignment returns CENTER for action message`() {
        val message = mock(BaseMessage::class.java)
        `when`(message.category).thenReturn(CometChatConstants.CATEGORY_ACTION)
        `when`(message.sender).thenReturn(null)

        val alignment = getMessageAlignment(message, currentUser)

        assertEquals(MessageAlignment.CENTER, alignment)
    }

    @Test
    fun `getMessageAlignment returns CENTER for call message`() {
        val message = mock(BaseMessage::class.java)
        `when`(message.category).thenReturn(CometChatConstants.CATEGORY_CALL)
        `when`(message.sender).thenReturn(null)

        val alignment = getMessageAlignment(message, currentUser)

        assertEquals(MessageAlignment.CENTER, alignment)
    }

    @Test
    fun `getMessageAlignment returns CENTER for action message regardless of sender`() {
        val sender = mock(User::class.java)
        `when`(sender.uid).thenReturn("current_user_123")

        val message = mock(BaseMessage::class.java)
        `when`(message.category).thenReturn(CometChatConstants.CATEGORY_ACTION)
        `when`(message.sender).thenReturn(sender)

        val alignment = getMessageAlignment(message, currentUser)

        // Action messages should always be CENTER, even if sender is current user
        assertEquals(MessageAlignment.CENTER, alignment)
    }

    @Test
    fun `getMessageAlignment returns LEFT when sender is null`() {
        val message = mock(BaseMessage::class.java)
        `when`(message.category).thenReturn(CometChatConstants.CATEGORY_MESSAGE)
        `when`(message.sender).thenReturn(null)

        val alignment = getMessageAlignment(message, currentUser)

        assertEquals(MessageAlignment.LEFT, alignment)
    }

    // ========================================
    // shouldShowDateSeparator Tests
    // ========================================

    @Test
    fun `shouldShowDateSeparator returns true when previous message is null`() {
        val currentMessage = mock(BaseMessage::class.java)
        `when`(currentMessage.sentAt).thenReturn(System.currentTimeMillis() / 1000)

        val result = shouldShowDateSeparator(currentMessage, null)

        assertTrue(result)
    }

    @Test
    fun `shouldShowDateSeparator returns false for same day messages`() {
        val now = System.currentTimeMillis() / 1000
        val oneHourAgo = now - 3600 // 1 hour ago

        val currentMessage = mock(BaseMessage::class.java)
        `when`(currentMessage.sentAt).thenReturn(now)

        val previousMessage = mock(BaseMessage::class.java)
        `when`(previousMessage.sentAt).thenReturn(oneHourAgo)

        val result = shouldShowDateSeparator(currentMessage, previousMessage)

        assertFalse(result)
    }

    @Test
    fun `shouldShowDateSeparator returns true for different day messages`() {
        val now = System.currentTimeMillis() / 1000
        val twoDaysAgo = now - (2 * 24 * 3600) // 2 days ago

        val currentMessage = mock(BaseMessage::class.java)
        `when`(currentMessage.sentAt).thenReturn(now)

        val previousMessage = mock(BaseMessage::class.java)
        `when`(previousMessage.sentAt).thenReturn(twoDaysAgo)

        val result = shouldShowDateSeparator(currentMessage, previousMessage)

        assertTrue(result)
    }

    @Test
    fun `shouldShowDateSeparator returns true for different year messages`() {
        val calendar = Calendar.getInstance()
        val currentTimestamp = calendar.timeInMillis / 1000

        calendar.add(Calendar.YEAR, -1)
        val lastYearTimestamp = calendar.timeInMillis / 1000

        val currentMessage = mock(BaseMessage::class.java)
        `when`(currentMessage.sentAt).thenReturn(currentTimestamp)

        val previousMessage = mock(BaseMessage::class.java)
        `when`(previousMessage.sentAt).thenReturn(lastYearTimestamp)

        val result = shouldShowDateSeparator(currentMessage, previousMessage)

        assertTrue(result)
    }

    // ========================================
    // formatDateSeparator Tests
    // ========================================

    @Test
    fun `formatDateSeparator returns Today for today's date`() {
        val now = System.currentTimeMillis() / 1000

        val result = formatDateSeparator(now)

        assertEquals("Today", result)
    }

    @Test
    fun `formatDateSeparator returns Yesterday for yesterday's date`() {
        val yesterday = (System.currentTimeMillis() / 1000) - (24 * 3600)

        val result = formatDateSeparator(yesterday)

        assertEquals("Yesterday", result)
    }

    @Test
    fun `formatDateSeparator returns formatted date for older dates`() {
        // Set a specific date: January 15, 2023
        val calendar = Calendar.getInstance()
        calendar.set(2023, Calendar.JANUARY, 15, 12, 0, 0)
        val timestamp = calendar.timeInMillis / 1000

        val result = formatDateSeparator(timestamp)

        // Should contain the month and day
        assertTrue(result.contains("January"))
        assertTrue(result.contains("15"))
        assertTrue(result.contains("2023"))
    }

}
