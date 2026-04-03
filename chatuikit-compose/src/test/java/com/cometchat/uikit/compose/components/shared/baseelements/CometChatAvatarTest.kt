package com.cometchat.uikit.compose.components.shared.baseelements

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CometChatAvatarTest {

    private val avatarKtClass = Class.forName(
        "com.cometchat.uikit.compose.shared.baseelements.avatar.CometChatAvatarKt"
    )

    private val getInitialsMethod = avatarKtClass.getDeclaredMethod(
        "getInitials",
        String::class.java
    ).apply { isAccessible = true }

    private val containsOnlyEmojisMethod = avatarKtClass.getDeclaredMethod(
        "containsOnlyEmojis",
        String::class.java
    ).apply { isAccessible = true }

    private val isEmojiMethod = avatarKtClass.getDeclaredMethod(
        "isEmoji",
        Int::class.javaPrimitiveType
    ).apply { isAccessible = true }

    private fun invokeGetInitials(name: String): String {
        return getInitialsMethod.invoke(null, name) as String
    }

    private fun invokeContainsOnlyEmojis(value: String): Boolean {
        return containsOnlyEmojisMethod.invoke(null, value) as Boolean
    }

    private fun invokeIsEmoji(codePoint: Int): Boolean {
        return isEmojiMethod.invoke(null, codePoint) as Boolean
    }

    @Test
    fun getInitials_returnsFirstCharactersForMultipleWords() {
        val initials = invokeGetInitials("John Doe")

        assertEquals("JD", initials)
    }

    @Test
    fun getInitials_handlesSingleWordNames() {
        val initials = invokeGetInitials("alice")

        assertEquals("al", initials)
    }

    @Test
    fun getInitials_handlesVeryShortNames() {
        val initials = invokeGetInitials("A")

        assertEquals("A", initials)
    }

    @Test
    fun getInitials_returnsEmojiForEmojiOnlyNames() {
        val emoji = String(Character.toChars(0x1F600))
        val initials = invokeGetInitials(emoji)

        assertEquals(emoji, initials)
    }

    @Test
    fun containsOnlyEmojis_identifiesEmojiOnlyStrings() {
        val emojiSequence = String(Character.toChars(0x1F600)) + String(Character.toChars(0x1F602))

        assertTrue(invokeContainsOnlyEmojis(emojiSequence))
    }

    @Test
    fun containsOnlyEmojis_returnsFalseForMixedContent() {
        val mixedContent = "John " + String(Character.toChars(0x1F600))

        assertFalse(invokeContainsOnlyEmojis(mixedContent))
    }

    @Test
    fun isEmoji_returnsTrueForEmojiCodePoint() {
        assertTrue(invokeIsEmoji(0x1F600))
    }

    @Test
    fun isEmoji_returnsFalseForNonEmojiCodePoint() {
        assertFalse(invokeIsEmoji('A'.code))
    }
}
