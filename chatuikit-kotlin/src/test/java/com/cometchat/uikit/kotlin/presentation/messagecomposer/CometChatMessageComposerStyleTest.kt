package com.cometchat.uikit.kotlin.presentation.messagecomposer

import android.app.Application
import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import com.cometchat.uikit.kotlin.presentation.messagecomposer.style.CometChatMessageComposerStyle
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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
@Config(sdk = [28])
class CometChatMessageComposerStyleTest : FunSpec({

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
    test("Bug Condition: extractFromTypedArray should return transparent for composeBoxBackgroundColor when no custom value provided") {
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
        style.composeBoxBackgroundColor shouldBe Color.TRANSPARENT
    }

    test("Style should have default values when created with no arguments") {
        val style = CometChatMessageComposerStyle()
        
        style.backgroundColor shouldBe 0
        style.strokeColor shouldBe 0
        style.strokeWidth shouldBe 0
        style.cornerRadius shouldBe 0
        style.composeBoxBackgroundColor shouldBe 0
        style.composeBoxStrokeColor shouldBe 0
        style.composeBoxStrokeWidth shouldBe 0
        style.composeBoxCornerRadius shouldBe 0
        style.separatorColor shouldBe 0
        style.attachmentIcon shouldBe null
        style.attachmentIconTint shouldBe 0
        style.voiceRecordingIcon shouldBe null
        style.voiceRecordingIconTint shouldBe 0
        style.aiIcon shouldBe null
        style.aiIconTint shouldBe 0
        style.stickerIcon shouldBe null
        style.stickerIconTint shouldBe 0
        style.sendButtonActiveIcon shouldBe null
        style.sendButtonInactiveIcon shouldBe null
        style.sendButtonActiveBackgroundColor shouldBe 0
        style.sendButtonInactiveBackgroundColor shouldBe 0
        style.sendButtonStopIcon shouldBe null
    }

    test("Style should preserve custom values when created with arguments") {
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
        
        style.backgroundColor shouldBe 0xFF000000.toInt()
        style.strokeColor shouldBe 0xFF111111.toInt()
        style.strokeWidth shouldBe 2
        style.cornerRadius shouldBe 8
        style.composeBoxBackgroundColor shouldBe 0xFF222222.toInt()
        style.composeBoxStrokeColor shouldBe 0xFF333333.toInt()
        style.composeBoxStrokeWidth shouldBe 1
        style.composeBoxCornerRadius shouldBe 24
        style.separatorColor shouldBe 0xFF444444.toInt()
        style.attachmentIconTint shouldBe 0xFF555555.toInt()
        style.voiceRecordingIconTint shouldBe 0xFF666666.toInt()
        style.aiIconTint shouldBe 0xFF777777.toInt()
        style.stickerIconTint shouldBe 0xFF888888.toInt()
        style.sendButtonActiveBackgroundColor shouldBe 0xFF999999.toInt()
        style.sendButtonInactiveBackgroundColor shouldBe 0xFFAAAAAA.toInt()
        style.editPreviewTitleTextColor shouldBe 0xFFBBBBBB.toInt()
        style.editPreviewMessageTextColor shouldBe 0xFFCCCCCC.toInt()
        style.editPreviewBackgroundColor shouldBe 0xFFDDDDDD.toInt()
        style.editPreviewCornerRadius shouldBe 4
        style.editPreviewStrokeColor shouldBe 0xFFEEEEEE.toInt()
        style.editPreviewStrokeWidth shouldBe 1
        style.editPreviewCloseIconTint shouldBe 0xFFFFFFFF.toInt()
        style.messagePreviewSeparatorColor shouldBe 0xFF123456.toInt()
        style.messagePreviewTitleTextColor shouldBe 0xFF234567.toInt()
        style.messagePreviewSubtitleTextColor shouldBe 0xFF345678.toInt()
        style.messagePreviewBackgroundColor shouldBe 0xFF456789.toInt()
        style.messagePreviewCornerRadius shouldBe 8
        style.messagePreviewStrokeColor shouldBe 0xFF56789A.toInt()
        style.messagePreviewStrokeWidth shouldBe 1
        style.messagePreviewCloseIconTint shouldBe 0xFF6789AB.toInt()
        style.inputTextColor shouldBe 0xFF789ABC.toInt()
        style.inputPlaceholderColor shouldBe 0xFF89ABCD.toInt()
        style.richTextToolbarBackgroundColor shouldBe 0xFF9ABCDE.toInt()
        style.richTextToolbarIconTint shouldBe 0xFFABCDEF.toInt()
        style.richTextToolbarActiveIconTint shouldBe 0xFFBCDEF0.toInt()
        style.richTextToolbarToggleIconTint shouldBe 0xFFCDEF01.toInt()
    }

    test("Style copy should create independent instance") {
        val original = CometChatMessageComposerStyle(
            backgroundColor = 0xFF000000.toInt(),
            strokeWidth = 2
        )
        
        val copy = original.copy(backgroundColor = 0xFFFFFFFF.toInt())
        
        original.backgroundColor shouldBe 0xFF000000.toInt()
        copy.backgroundColor shouldBe 0xFFFFFFFF.toInt()
        original.strokeWidth shouldBe copy.strokeWidth
    }

    test("Style equality should work correctly") {
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
        
        style1 shouldBe style2
        style1 shouldNotBe style3
    }

    test("Style hashCode should be consistent with equality") {
        val style1 = CometChatMessageComposerStyle(
            backgroundColor = 0xFF000000.toInt(),
            strokeWidth = 2
        )
        
        val style2 = CometChatMessageComposerStyle(
            backgroundColor = 0xFF000000.toInt(),
            strokeWidth = 2
        )
        
        style1.hashCode() shouldBe style2.hashCode()
    }
})
