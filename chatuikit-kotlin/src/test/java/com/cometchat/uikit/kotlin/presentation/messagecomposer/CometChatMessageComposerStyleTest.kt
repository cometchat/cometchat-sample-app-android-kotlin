package com.cometchat.uikit.kotlin.presentation.messagecomposer

import android.app.Application
import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import com.cometchat.uikit.kotlin.presentation.messagecomposer.style.CometChatMessageComposerStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for CometChatMessageComposerStyle.
 *
 * Tests the style data class for the MessageComposer component.
 *
 * Validates: Requirements 12.1-12.15, 24.1-24.5
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class CometChatMessageComposerStyleTest {

    /**
     * Bug Condition Exploration Test - Default Styling Visual Defect
     *
     * **Validates: Requirements 1.1, 1.2, 1.3, 1.4**
     *
     * This test verifies that when extractFromTypedArray() is called with null TypedArray
     * (simulating default styling with no custom values), the composeBoxBackgroundColor
     * should be transparent (Color.TRANSPARENT = 0x00000000).
     *
     * **EXPECTED TO FAIL on unfixed code** - failure confirms the bug exists.
     *
     * Current Behavior (Bug): Returns CometChatTheme.getBackgroundColor2(context)
     * Expected Behavior: Returns Color.TRANSPARENT (0x00000000)
     */
    @Test
    fun `Bug Condition - extractFromTypedArray should return transparent for composeBoxBackgroundColor when no custom value provided`() {
        // Get application context using Robolectric
        val context: Application = ApplicationProvider.getApplicationContext()

        // Call extractFromTypedArray with null TypedArray to simulate default styling
        // This uses reflection to access the private method
        val companionClass = CometChatMessageComposerStyle.Companion::class.java
        val extractMethod = companionClass.getDeclaredMethod(
            "extractFromTypedArray",
            android.content.Context::class.java,
            android.content.res.TypedArray::class.java
        )
        extractMethod.isAccessible = true

        val style = extractMethod.invoke(
            CometChatMessageComposerStyle.Companion,
            context,
            null
        ) as CometChatMessageComposerStyle

        // Assert that composeBoxBackgroundColor is transparent
        // This test is EXPECTED TO FAIL on unfixed code because the current implementation
        // returns CometChatTheme.getBackgroundColor2(context) instead of Color.TRANSPARENT
        assertEquals(Color.TRANSPARENT, style.composeBoxBackgroundColor)
    }

    @Test
    fun `Style should have default values when created with no arguments`() {
        val style = CometChatMessageComposerStyle()

        assertEquals(0, style.backgroundColor)
        assertEquals(0, style.strokeColor)
        assertEquals(0, style.strokeWidth)
        assertEquals(0, style.cornerRadius)
        assertEquals(0, style.composeBoxBackgroundColor)
        assertEquals(0, style.composeBoxStrokeColor)
        assertEquals(0, style.composeBoxStrokeWidth)
        assertEquals(0, style.composeBoxCornerRadius)
        assertEquals(0, style.separatorColor)
        assertEquals(null, style.attachmentIcon)
        assertEquals(0, style.attachmentIconTint)
        assertEquals(null, style.voiceRecordingIcon)
        assertEquals(0, style.voiceRecordingIconTint)
        assertEquals(null, style.aiIcon)
        assertEquals(0, style.aiIconTint)
        assertEquals(null, style.stickerIcon)
        assertEquals(0, style.stickerIconTint)
        assertEquals(null, style.sendButtonActiveIcon)
        assertEquals(null, style.sendButtonInactiveIcon)
        assertEquals(0, style.sendButtonActiveBackgroundColor)
        assertEquals(0, style.sendButtonInactiveBackgroundColor)
        assertEquals(null, style.sendButtonStopIcon)
    }

    @Test
    fun `Style should preserve custom values when created with arguments`() {
        val style = CometChatMessageComposerStyle(
            backgroundColor = 0xFF000000.toInt(),
            strokeColor = 0xFF111111.toInt(),
            strokeWidth = 2,
            cornerRadius = 8,
            composeBoxBackgroundColor = 0xFF222222.toInt(),
            composeBoxStrokeColor = 0xFF333333.toInt(),
            composeBoxStrokeWidth = 1,
            composeBoxCornerRadius = 24,
            separatorColor = 0xFF444444.toInt(),
            attachmentIconTint = 0xFF555555.toInt(),
            voiceRecordingIconTint = 0xFF666666.toInt(),
            aiIconTint = 0xFF777777.toInt(),
            stickerIconTint = 0xFF888888.toInt(),
            sendButtonActiveBackgroundColor = 0xFF999999.toInt(),
            sendButtonInactiveBackgroundColor = 0xFFAAAAAA.toInt(),
            editPreviewTitleTextColor = 0xFFBBBBBB.toInt(),
            editPreviewMessageTextColor = 0xFFCCCCCC.toInt(),
            editPreviewBackgroundColor = 0xFFDDDDDD.toInt(),
            editPreviewCornerRadius = 4,
            editPreviewStrokeColor = 0xFFEEEEEE.toInt(),
            editPreviewStrokeWidth = 1,
            editPreviewCloseIconTint = 0xFFFFFFFF.toInt(),
            messagePreviewSeparatorColor = 0xFF123456.toInt(),
            messagePreviewTitleTextColor = 0xFF234567.toInt(),
            messagePreviewSubtitleTextColor = 0xFF345678.toInt(),
            messagePreviewBackgroundColor = 0xFF456789.toInt(),
            messagePreviewCornerRadius = 8,
            messagePreviewStrokeColor = 0xFF56789A.toInt(),
            messagePreviewStrokeWidth = 1,
            messagePreviewCloseIconTint = 0xFF6789AB.toInt(),
            inputTextColor = 0xFF789ABC.toInt(),
            inputPlaceholderColor = 0xFF89ABCD.toInt(),
            richTextToolbarBackgroundColor = 0xFF9ABCDE.toInt(),
            richTextToolbarIconTint = 0xFFABCDEF.toInt(),
            richTextToolbarActiveIconTint = 0xFFBCDEF0.toInt(),
            richTextToolbarToggleIconTint = 0xFFCDEF01.toInt()
        )

        assertEquals(0xFF000000.toInt(), style.backgroundColor)
        assertEquals(0xFF111111.toInt(), style.strokeColor)
        assertEquals(2, style.strokeWidth)
        assertEquals(8, style.cornerRadius)
        assertEquals(0xFF222222.toInt(), style.composeBoxBackgroundColor)
        assertEquals(0xFF333333.toInt(), style.composeBoxStrokeColor)
        assertEquals(1, style.composeBoxStrokeWidth)
        assertEquals(24, style.composeBoxCornerRadius)
        assertEquals(0xFF444444.toInt(), style.separatorColor)
        assertEquals(0xFF555555.toInt(), style.attachmentIconTint)
        assertEquals(0xFF666666.toInt(), style.voiceRecordingIconTint)
        assertEquals(0xFF777777.toInt(), style.aiIconTint)
        assertEquals(0xFF888888.toInt(), style.stickerIconTint)
        assertEquals(0xFF999999.toInt(), style.sendButtonActiveBackgroundColor)
        assertEquals(0xFFAAAAAA.toInt(), style.sendButtonInactiveBackgroundColor)
        assertEquals(0xFFBBBBBB.toInt(), style.editPreviewTitleTextColor)
        assertEquals(0xFFCCCCCC.toInt(), style.editPreviewMessageTextColor)
        assertEquals(0xFFDDDDDD.toInt(), style.editPreviewBackgroundColor)
        assertEquals(4, style.editPreviewCornerRadius)
        assertEquals(0xFFEEEEEE.toInt(), style.editPreviewStrokeColor)
        assertEquals(1, style.editPreviewStrokeWidth)
        assertEquals(0xFFFFFFFF.toInt(), style.editPreviewCloseIconTint)
        assertEquals(0xFF123456.toInt(), style.messagePreviewSeparatorColor)
        assertEquals(0xFF234567.toInt(), style.messagePreviewTitleTextColor)
        assertEquals(0xFF345678.toInt(), style.messagePreviewSubtitleTextColor)
        assertEquals(0xFF456789.toInt(), style.messagePreviewBackgroundColor)
        assertEquals(8, style.messagePreviewCornerRadius)
        assertEquals(0xFF56789A.toInt(), style.messagePreviewStrokeColor)
        assertEquals(1, style.messagePreviewStrokeWidth)
        assertEquals(0xFF6789AB.toInt(), style.messagePreviewCloseIconTint)
        assertEquals(0xFF789ABC.toInt(), style.inputTextColor)
        assertEquals(0xFF89ABCD.toInt(), style.inputPlaceholderColor)
        assertEquals(0xFF9ABCDE.toInt(), style.richTextToolbarBackgroundColor)
        assertEquals(0xFFABCDEF.toInt(), style.richTextToolbarIconTint)
        assertEquals(0xFFBCDEF0.toInt(), style.richTextToolbarActiveIconTint)
        assertEquals(0xFFCDEF01.toInt(), style.richTextToolbarToggleIconTint)
    }

    @Test
    fun `Style copy should create independent instance`() {
        val original = CometChatMessageComposerStyle(
            backgroundColor = 0xFF000000.toInt(),
            strokeWidth = 2
        )

        val copy = original.copy(backgroundColor = 0xFFFFFFFF.toInt())

        assertEquals(0xFF000000.toInt(), original.backgroundColor)
        assertEquals(0xFFFFFFFF.toInt(), copy.backgroundColor)
        assertEquals(original.strokeWidth, copy.strokeWidth)
    }

    @Test
    fun `Style equality should work correctly`() {
        val style1 = CometChatMessageComposerStyle(
            backgroundColor = 0xFF000000.toInt(),
            strokeWidth = 2
        )

        val style2 = CometChatMessageComposerStyle(
            backgroundColor = 0xFF000000.toInt(),
            strokeWidth = 2
        )

        val style3 = CometChatMessageComposerStyle(
            backgroundColor = 0xFFFFFFFF.toInt(),
            strokeWidth = 2
        )

        assertEquals(style1, style2)
        assertNotEquals(style1, style3)
    }

    @Test
    fun `Style hashCode should be consistent with equality`() {
        val style1 = CometChatMessageComposerStyle(
            backgroundColor = 0xFF000000.toInt(),
            strokeWidth = 2
        )

        val style2 = CometChatMessageComposerStyle(
            backgroundColor = 0xFF000000.toInt(),
            strokeWidth = 2
        )

        assertEquals(style1.hashCode(), style2.hashCode())
    }
}
