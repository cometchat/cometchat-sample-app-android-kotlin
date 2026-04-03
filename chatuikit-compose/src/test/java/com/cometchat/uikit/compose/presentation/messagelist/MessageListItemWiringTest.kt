package com.cometchat.uikit.compose.presentation.messagelist

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cometchat.uikit.compose.presentation.messagelist.style.CometChatMessageListStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatMessageBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatTextBubbleStyle
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeSameInstanceAs

/**
 * Unit tests for MessageListItem wiring of messageBubbleStyle.
 *
 * MessageListItem is a @Composable that passes `style.messageBubbleStyle` to
 * CometChatMessageBubble. Since we cannot invoke @Composable functions in a
 * plain unit test, we verify the data-flow contract instead:
 *
 * 1. CometChatMessageListStyle always exposes a non-null messageBubbleStyle.
 * 2. A custom messageBubbleStyle set on CometChatMessageListStyle is the exact
 *    instance that would be forwarded to CometChatMessageBubble.
 * 3. The default messageBubbleStyle is non-null, ensuring the old `style = null`
 *    path is eliminated.
 *
 * **Validates: Requirements 1.1, 1.2**
 */
class MessageListItemWiringTest : StringSpec({

    // ====================================================================
    // Helpers — create a minimal CometChatMessageListStyle without Compose
    // ====================================================================

    fun createMessageListStyle(
        messageBubbleStyle: CometChatMessageBubbleStyle = createDefaultBubbleStyle()
    ): CometChatMessageListStyle = CometChatMessageListStyle(
        backgroundColor = Color.White,
        cornerRadius = 0.dp,
        strokeWidth = 0.dp,
        strokeColor = Color.Transparent,
        errorStateTitleTextColor = Color.Black,
        errorStateTitleTextStyle = TextStyle.Default,
        errorStateSubtitleTextColor = Color.Gray,
        errorStateSubtitleTextStyle = TextStyle.Default,
        emptyChatGreetingTitleTextColor = Color.Black,
        emptyChatGreetingTitleTextStyle = TextStyle.Default,
        emptyChatGreetingSubtitleTextColor = Color.Gray,
        emptyChatGreetingSubtitleTextStyle = TextStyle.Default,
        dateSeparatorTextColor = Color.Gray,
        dateSeparatorTextStyle = TextStyle.Default,
        dateSeparatorBackgroundColor = Color.LightGray,
        dateSeparatorCornerRadius = 8.dp,
        dateSeparatorStrokeWidth = 1.dp,
        dateSeparatorStrokeColor = Color.Transparent,
        newMessageIndicatorBackgroundColor = Color.White,
        newMessageIndicatorTextColor = Color.White,
        newMessageIndicatorTextStyle = TextStyle.Default,
        newMessageIndicatorCornerRadius = 28.dp,
        newMessageIndicatorElevation = 8.dp,
        newMessageIndicatorStrokeColor = Color.LightGray,
        newMessageIndicatorStrokeWidth = 1.dp,
        newMessageIndicatorIconTint = Color.Gray,
        newMessageIndicatorIconSize = 24.dp,
        newMessageIndicatorPadding = 12.dp,
        newMessagesSeparatorTextColor = Color.Red,
        newMessagesSeparatorTextStyle = TextStyle.Default,
        newMessagesSeparatorLineColor = Color.Red,
        newMessagesSeparatorLineHeight = 1.dp,
        newMessagesSeparatorVerticalPadding = 8.dp,
        messageBubbleStyle = messageBubbleStyle
    )

    // ====================================================================
    // Requirement 1.1: messageBubbleStyle is passed through (not null)
    // ====================================================================

    "custom messageBubbleStyle is the exact instance exposed by CometChatMessageListStyle" {
        val customBubbleStyle = CometChatMessageBubbleStyle(
            backgroundColor = Color.Red,
            cornerRadius = 16.dp,
            strokeWidth = 2.dp,
            strokeColor = Color.Blue,
            padding = PaddingValues(8.dp),
            senderNameTextColor = Color.Magenta,
            senderNameTextStyle = TextStyle(fontSize = 14.sp),
            threadIndicatorTextColor = Color.Cyan,
            threadIndicatorTextStyle = TextStyle(fontSize = 10.sp),
            threadIndicatorIconTint = Color.Yellow,
            timestampTextColor = Color.Green,
            timestampTextStyle = TextStyle(fontSize = 9.sp)
        )

        val listStyle = createMessageListStyle(messageBubbleStyle = customBubbleStyle)

        // The style that MessageListItem would pass to CometChatMessageBubble
        listStyle.messageBubbleStyle shouldBeSameInstanceAs customBubbleStyle
    }

    "custom messageBubbleStyle properties are preserved in CometChatMessageListStyle" {
        val customBubbleStyle = CometChatMessageBubbleStyle(
            backgroundColor = Color(0xFFABCDEF),
            cornerRadius = 20.dp,
            strokeWidth = 3.dp,
            strokeColor = Color(0xFF123456),
            padding = PaddingValues(12.dp),
            senderNameTextColor = Color(0xFF654321),
            senderNameTextStyle = TextStyle(fontSize = 16.sp),
            threadIndicatorTextColor = Color(0xFFAAAAAA),
            threadIndicatorTextStyle = TextStyle(fontSize = 11.sp),
            threadIndicatorIconTint = Color(0xFFBBBBBB),
            timestampTextColor = Color(0xFFCCCCCC),
            timestampTextStyle = TextStyle(fontSize = 10.sp)
        )

        val listStyle = createMessageListStyle(messageBubbleStyle = customBubbleStyle)

        listStyle.messageBubbleStyle.backgroundColor shouldBe Color(0xFFABCDEF)
        listStyle.messageBubbleStyle.cornerRadius shouldBe 20.dp
        listStyle.messageBubbleStyle.strokeWidth shouldBe 3.dp
        listStyle.messageBubbleStyle.strokeColor shouldBe Color(0xFF123456)
        listStyle.messageBubbleStyle.senderNameTextColor shouldBe Color(0xFF654321)
        listStyle.messageBubbleStyle.threadIndicatorTextColor shouldBe Color(0xFFAAAAAA)
        listStyle.messageBubbleStyle.threadIndicatorIconTint shouldBe Color(0xFFBBBBBB)
        listStyle.messageBubbleStyle.timestampTextColor shouldBe Color(0xFFCCCCCC)
    }

    // ====================================================================
    // Requirement 1.2: default messageBubbleStyle is non-null
    // ====================================================================

    "default messageBubbleStyle is non-null when using default constructor value" {
        val listStyle = createMessageListStyle()

        // messageBubbleStyle is a non-nullable property — this verifies the
        // data class always carries a valid style that MessageListItem can forward
        listStyle.messageBubbleStyle shouldNotBe null
    }

    // ====================================================================
    // Per-bubble-type styles default to null (factory defaults used)
    // ====================================================================

    "per-bubble-type styles default to null when not explicitly set" {
        val listStyle = createMessageListStyle()

        listStyle.textBubbleStyle shouldBe null
        listStyle.imageBubbleStyle shouldBe null
        listStyle.videoBubbleStyle shouldBe null
        listStyle.audioBubbleStyle shouldBe null
        listStyle.fileBubbleStyle shouldBe null
        listStyle.deleteBubbleStyle shouldBe null
        listStyle.pollBubbleStyle shouldBe null
        listStyle.stickerBubbleStyle shouldBe null
        listStyle.collaborativeBubbleStyle shouldBe null
        listStyle.meetCallBubbleStyle shouldBe null
        listStyle.actionBubbleStyle shouldBe null
        listStyle.callActionBubbleStyle shouldBe null
    }
})

// ============================================================================
// Helper — creates a default CometChatMessageBubbleStyle without Compose runtime
// ============================================================================

private fun createDefaultBubbleStyle() = CometChatMessageBubbleStyle(
    backgroundColor = Color(0xFFEEEEEE),
    cornerRadius = 12.dp,
    strokeWidth = 0.dp,
    strokeColor = Color.Transparent,
    padding = PaddingValues(0.dp),
    senderNameTextColor = Color.Gray,
    senderNameTextStyle = TextStyle(fontSize = 12.sp),
    threadIndicatorTextColor = Color.Gray,
    threadIndicatorTextStyle = TextStyle(fontSize = 12.sp),
    threadIndicatorIconTint = Color.Gray,
    timestampTextColor = Color.Gray,
    timestampTextStyle = TextStyle(fontSize = 11.sp)
)
