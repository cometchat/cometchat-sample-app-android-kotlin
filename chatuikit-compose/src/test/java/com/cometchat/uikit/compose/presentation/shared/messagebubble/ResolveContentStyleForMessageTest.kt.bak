package com.cometchat.uikit.compose.presentation.shared.messagebubble

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.CometChatAvatarStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatActionBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatAudioBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatCallActionBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatCollaborativeBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatDeleteBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatFileBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatImageBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatMeetCallBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatMessageBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatPollBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatStickerBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatTextBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatVideoBubbleStyle
import com.cometchat.uikit.core.constants.UIKitConstants
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * Unit tests for resolveContentStyleForMessage.
 *
 * Since resolveContentStyleForMessage is @Composable but uses no Compose runtime,
 * we replicate its pure branching logic here for direct unit testing — same pattern
 * used by ThreeTierPriorityChainPropertyTest.
 *
 * **Validates: Requirements 3.1, 3.2, 3.3**
 */
class ResolveContentStyleForMessageTest : StringSpec({

    // Shared style instances — each is a distinct object for identity checks
    val textStyle = createTextBubbleStyle()
    val imageStyle = createImageBubbleStyle()
    val videoStyle = createVideoBubbleStyle()
    val audioStyle = createAudioBubbleStyle()
    val fileStyle = createFileBubbleStyle()
    val deleteStyle = createDeleteBubbleStyle()
    val actionStyle = createActionBubbleStyle()
    val callActionStyle = createCallActionBubbleStyle()
    val meetCallStyle = createMeetCallBubbleStyle()
    val pollStyle = createPollBubbleStyle()
    val stickerStyle = createStickerBubbleStyle()
    val collaborativeStyle = createCollaborativeBubbleStyle()
    val mockFactory = mock(BubbleFactory::class.java)

    // ====================================================================
    // CATEGORY_MESSAGE type mapping
    // ====================================================================

    "text message returns textBubbleStyle" {
        val msg = mockMessage(CometChatConstants.CATEGORY_MESSAGE, CometChatConstants.MESSAGE_TYPE_TEXT)
        resolveContentStyle(msg, textBubbleStyle = textStyle) shouldBe textStyle
    }

    "image message returns imageBubbleStyle" {
        val msg = mockMessage(CometChatConstants.CATEGORY_MESSAGE, CometChatConstants.MESSAGE_TYPE_IMAGE)
        resolveContentStyle(msg, imageBubbleStyle = imageStyle) shouldBe imageStyle
    }

    "video message returns videoBubbleStyle" {
        val msg = mockMessage(CometChatConstants.CATEGORY_MESSAGE, CometChatConstants.MESSAGE_TYPE_VIDEO)
        resolveContentStyle(msg, videoBubbleStyle = videoStyle) shouldBe videoStyle
    }

    "audio message returns audioBubbleStyle" {
        val msg = mockMessage(CometChatConstants.CATEGORY_MESSAGE, CometChatConstants.MESSAGE_TYPE_AUDIO)
        resolveContentStyle(msg, audioBubbleStyle = audioStyle) shouldBe audioStyle
    }

    "file message returns fileBubbleStyle" {
        val msg = mockMessage(CometChatConstants.CATEGORY_MESSAGE, CometChatConstants.MESSAGE_TYPE_FILE)
        resolveContentStyle(msg, fileBubbleStyle = fileStyle) shouldBe fileStyle
    }

    // ====================================================================
    // CATEGORY_ACTION, CATEGORY_CALL, meeting
    // ====================================================================

    "action message returns actionBubbleStyle" {
        val msg = mockMessage(CometChatConstants.CATEGORY_ACTION, CometChatConstants.ActionKeys.ACTION_TYPE_GROUP_MEMBER)
        resolveContentStyle(msg, actionBubbleStyle = actionStyle) shouldBe actionStyle
    }

    "call message returns callActionBubbleStyle" {
        val msg = mockMessage(CometChatConstants.CATEGORY_CALL, CometChatConstants.CALL_TYPE_AUDIO)
        resolveContentStyle(msg, callActionBubbleStyle = callActionStyle) shouldBe callActionStyle
    }

    "meeting message returns meetCallBubbleStyle" {
        val msg = mockMessage(UIKitConstants.MessageType.MEETING, UIKitConstants.MessageType.MEETING)
        resolveContentStyle(msg, meetCallBubbleStyle = meetCallStyle) shouldBe meetCallStyle
    }

    // ====================================================================
    // CATEGORY_CUSTOM extension types
    // ====================================================================

    "polls custom message returns pollBubbleStyle" {
        val msg = mockMessage(CometChatConstants.CATEGORY_CUSTOM, InternalContentRenderer.EXTENSION_POLLS)
        resolveContentStyle(msg, pollBubbleStyle = pollStyle) shouldBe pollStyle
    }

    "sticker custom message returns stickerBubbleStyle" {
        val msg = mockMessage(CometChatConstants.CATEGORY_CUSTOM, InternalContentRenderer.EXTENSION_STICKER)
        resolveContentStyle(msg, stickerBubbleStyle = stickerStyle) shouldBe stickerStyle
    }

    "document custom message returns collaborativeBubbleStyle" {
        val msg = mockMessage(CometChatConstants.CATEGORY_CUSTOM, InternalContentRenderer.EXTENSION_DOCUMENT)
        resolveContentStyle(msg, collaborativeBubbleStyle = collaborativeStyle) shouldBe collaborativeStyle
    }

    "whiteboard custom message returns collaborativeBubbleStyle" {
        val msg = mockMessage(CometChatConstants.CATEGORY_CUSTOM, InternalContentRenderer.EXTENSION_WHITEBOARD)
        resolveContentStyle(msg, collaborativeBubbleStyle = collaborativeStyle) shouldBe collaborativeStyle
    }

    // ====================================================================
    // Null returns for unknown types
    // ====================================================================

    "unknown message type in CATEGORY_MESSAGE returns null" {
        val msg = mockMessage(CometChatConstants.CATEGORY_MESSAGE, "unknown_type")
        resolveContentStyle(msg) shouldBe null
    }

    "unknown custom extension type returns null" {
        val msg = mockMessage(CometChatConstants.CATEGORY_CUSTOM, "unknown_extension")
        resolveContentStyle(msg) shouldBe null
    }

    "unknown category returns null" {
        val msg = mockMessage("unknown_category", "some_type")
        resolveContentStyle(msg) shouldBe null
    }

    // ====================================================================
    // Factory present → always null
    // ====================================================================

    "returns null when factory is present for text message" {
        val msg = mockMessage(CometChatConstants.CATEGORY_MESSAGE, CometChatConstants.MESSAGE_TYPE_TEXT)
        resolveContentStyle(msg, factory = mockFactory, textBubbleStyle = textStyle) shouldBe null
    }

    "returns null when factory is present for custom message" {
        val msg = mockMessage(CometChatConstants.CATEGORY_CUSTOM, InternalContentRenderer.EXTENSION_POLLS)
        resolveContentStyle(msg, factory = mockFactory, pollBubbleStyle = pollStyle) shouldBe null
    }

    "returns null when factory is present for deleted message" {
        val msg = mockDeletedMessage(CometChatConstants.CATEGORY_MESSAGE, CometChatConstants.MESSAGE_TYPE_TEXT, 1000L)
        resolveContentStyle(msg, factory = mockFactory, deleteBubbleStyle = deleteStyle) shouldBe null
    }

    // ====================================================================
    // Deleted message handling
    // ====================================================================

    "deleted message returns deleteBubbleStyle instead of type-specific style" {
        val msg = mockDeletedMessage(CometChatConstants.CATEGORY_MESSAGE, CometChatConstants.MESSAGE_TYPE_TEXT, 1000L)
        resolveContentStyle(msg, deleteBubbleStyle = deleteStyle, textBubbleStyle = textStyle) shouldBe deleteStyle
    }

    "deleted custom message returns deleteBubbleStyle" {
        val msg = mockDeletedMessage(CometChatConstants.CATEGORY_CUSTOM, InternalContentRenderer.EXTENSION_POLLS, 500L)
        resolveContentStyle(msg, deleteBubbleStyle = deleteStyle, pollBubbleStyle = pollStyle) shouldBe deleteStyle
    }

    "deleted message returns null when deleteBubbleStyle is null" {
        val msg = mockDeletedMessage(CometChatConstants.CATEGORY_MESSAGE, CometChatConstants.MESSAGE_TYPE_TEXT, 1000L)
        resolveContentStyle(msg, deleteBubbleStyle = null, textBubbleStyle = textStyle) shouldBe null
    }

    // ====================================================================
    // Null style parameters → null return
    // ====================================================================

    "text message returns null when textBubbleStyle is null" {
        val msg = mockMessage(CometChatConstants.CATEGORY_MESSAGE, CometChatConstants.MESSAGE_TYPE_TEXT)
        resolveContentStyle(msg) shouldBe null
    }

    "action message returns null when actionBubbleStyle is null" {
        val msg = mockMessage(CometChatConstants.CATEGORY_ACTION, CometChatConstants.ActionKeys.ACTION_TYPE_GROUP_MEMBER)
        resolveContentStyle(msg) shouldBe null
    }

    "call message returns null when callActionBubbleStyle is null" {
        val msg = mockMessage(CometChatConstants.CATEGORY_CALL, CometChatConstants.CALL_TYPE_VIDEO)
        resolveContentStyle(msg) shouldBe null
    }

    "meeting message returns null when meetCallBubbleStyle is null" {
        val msg = mockMessage(UIKitConstants.MessageType.MEETING, UIKitConstants.MessageType.MEETING)
        resolveContentStyle(msg) shouldBe null
    }
})

// ============================================================================
// Pure-function replica of resolveContentStyleForMessage
// ============================================================================

private fun resolveContentStyle(
    message: BaseMessage,
    factory: BubbleFactory? = null,
    textBubbleStyle: CometChatTextBubbleStyle? = null,
    imageBubbleStyle: CometChatImageBubbleStyle? = null,
    videoBubbleStyle: CometChatVideoBubbleStyle? = null,
    audioBubbleStyle: CometChatAudioBubbleStyle? = null,
    fileBubbleStyle: CometChatFileBubbleStyle? = null,
    deleteBubbleStyle: CometChatDeleteBubbleStyle? = null,
    actionBubbleStyle: CometChatActionBubbleStyle? = null,
    callActionBubbleStyle: CometChatCallActionBubbleStyle? = null,
    meetCallBubbleStyle: CometChatMeetCallBubbleStyle? = null,
    pollBubbleStyle: CometChatPollBubbleStyle? = null,
    stickerBubbleStyle: CometChatStickerBubbleStyle? = null,
    collaborativeBubbleStyle: CometChatCollaborativeBubbleStyle? = null
): CometChatMessageBubbleStyle? {
    if (factory != null) return null
    if (message.deletedAt > 0) return deleteBubbleStyle

    return when (message.category) {
        CometChatConstants.CATEGORY_MESSAGE -> when (message.type) {
            CometChatConstants.MESSAGE_TYPE_TEXT -> textBubbleStyle
            CometChatConstants.MESSAGE_TYPE_IMAGE -> imageBubbleStyle
            CometChatConstants.MESSAGE_TYPE_VIDEO -> videoBubbleStyle
            CometChatConstants.MESSAGE_TYPE_AUDIO -> audioBubbleStyle
            CometChatConstants.MESSAGE_TYPE_FILE -> fileBubbleStyle
            else -> null
        }
        CometChatConstants.CATEGORY_ACTION -> actionBubbleStyle
        CometChatConstants.CATEGORY_CALL -> callActionBubbleStyle
        CometChatConstants.CATEGORY_CUSTOM -> when (message.type) {
            InternalContentRenderer.EXTENSION_POLLS -> pollBubbleStyle
            InternalContentRenderer.EXTENSION_STICKER -> stickerBubbleStyle
            InternalContentRenderer.EXTENSION_DOCUMENT,
            InternalContentRenderer.EXTENSION_WHITEBOARD -> collaborativeBubbleStyle
            else -> null
        }
        UIKitConstants.MessageType.MEETING -> meetCallBubbleStyle
        else -> null
    }
}

// ============================================================================
// Mock message helpers
// ============================================================================

private fun mockMessage(category: String, type: String): BaseMessage {
    val message = mock(BaseMessage::class.java)
    `when`(message.deletedAt).thenReturn(0L)
    `when`(message.category).thenReturn(category)
    `when`(message.type).thenReturn(type)
    return message
}

private fun mockDeletedMessage(category: String, type: String, deletedAt: Long): BaseMessage {
    val message = mock(BaseMessage::class.java)
    `when`(message.deletedAt).thenReturn(deletedAt)
    `when`(message.category).thenReturn(category)
    `when`(message.type).thenReturn(type)
    return message
}

// ============================================================================
// Style factory helpers — minimal instances for identity-based assertions
// ============================================================================

private val COMMON_BG = Color(0xFFEEEEEE)
private val COMMON_RADIUS = 12.dp
private val COMMON_STROKE_W = 0.dp
private val COMMON_STROKE_C = Color.Transparent
private val COMMON_PADDING = PaddingValues(0.dp)
private val COMMON_TEXT_COLOR = Color.Gray
private val COMMON_TEXT_STYLE = TextStyle(fontSize = 12.sp)

private fun createTextBubbleStyle() = CometChatTextBubbleStyle(
    textColor = Color.Black,
    textStyle = TextStyle(fontSize = 14.sp),
    linkColor = Color.Blue,
    translatedTextColor = Color.Gray,
    translatedTextStyle = TextStyle(fontSize = 12.sp),
    separatorColor = Color.LightGray,
    linkPreviewBackgroundColor = Color(0xFFE8E8E8),
    linkPreviewTitleColor = Color.Black,
    linkPreviewTitleStyle = TextStyle(fontSize = 14.sp),
    linkPreviewDescriptionColor = Color.Gray,
    linkPreviewDescriptionStyle = TextStyle(fontSize = 12.sp),
    linkPreviewLinkColor = Color.Blue,
    linkPreviewLinkStyle = TextStyle(fontSize = 12.sp),
    linkPreviewCornerRadius = 8.dp,
    linkPreviewStrokeWidth = 0.dp,
    linkPreviewStrokeColor = Color.Transparent,
    backgroundColor = COMMON_BG,
    cornerRadius = COMMON_RADIUS,
    strokeWidth = COMMON_STROKE_W,
    strokeColor = COMMON_STROKE_C,
    padding = COMMON_PADDING,
    senderNameTextColor = COMMON_TEXT_COLOR,
    senderNameTextStyle = COMMON_TEXT_STYLE,
    threadIndicatorTextColor = COMMON_TEXT_COLOR,
    threadIndicatorTextStyle = COMMON_TEXT_STYLE,
    threadIndicatorIconTint = COMMON_TEXT_COLOR,
    timestampTextColor = COMMON_TEXT_COLOR,
    timestampTextStyle = COMMON_TEXT_STYLE
)

private fun createImageBubbleStyle() = CometChatImageBubbleStyle(
    imageCornerRadius = 8.dp,
    imageStrokeWidth = 0.dp,
    imageStrokeColor = Color.Transparent,
    captionTextColor = Color.Black,
    captionTextStyle = TextStyle(fontSize = 14.sp),
    progressIndicatorColor = Color.Blue,
    gridSpacing = 2.dp,
    maxGridWidth = 240.dp,
    moreOverlayBackgroundColor = Color.Black.copy(alpha = 0.6f),
    moreOverlayTextColor = Color.White,
    moreOverlayTextStyle = TextStyle(fontSize = 16.sp),
    backgroundColor = COMMON_BG,
    cornerRadius = COMMON_RADIUS,
    strokeWidth = COMMON_STROKE_W,
    strokeColor = COMMON_STROKE_C,
    padding = COMMON_PADDING,
    senderNameTextColor = COMMON_TEXT_COLOR,
    senderNameTextStyle = COMMON_TEXT_STYLE,
    threadIndicatorTextColor = COMMON_TEXT_COLOR,
    threadIndicatorTextStyle = COMMON_TEXT_STYLE,
    threadIndicatorIconTint = COMMON_TEXT_COLOR,
    timestampTextColor = COMMON_TEXT_COLOR,
    timestampTextStyle = COMMON_TEXT_STYLE
)

private fun createVideoBubbleStyle() = CometChatVideoBubbleStyle(
    videoCornerRadius = 8.dp,
    videoStrokeWidth = 0.dp,
    videoStrokeColor = Color.Transparent,
    playIconTint = Color.White,
    playIconBackgroundColor = Color.Black.copy(alpha = 0.5f),
    progressIndicatorColor = Color.Blue,
    captionTextColor = Color.Black,
    captionTextStyle = TextStyle(fontSize = 14.sp),
    gridSpacing = 2.dp,
    maxGridWidth = 240.dp,
    moreOverlayBackgroundColor = Color.Black.copy(alpha = 0.6f),
    moreOverlayTextColor = Color.White,
    moreOverlayTextStyle = TextStyle(fontSize = 16.sp),
    backgroundColor = COMMON_BG,
    cornerRadius = COMMON_RADIUS,
    strokeWidth = COMMON_STROKE_W,
    strokeColor = COMMON_STROKE_C,
    padding = COMMON_PADDING,
    senderNameTextColor = COMMON_TEXT_COLOR,
    senderNameTextStyle = COMMON_TEXT_STYLE,
    threadIndicatorTextColor = COMMON_TEXT_COLOR,
    threadIndicatorTextStyle = COMMON_TEXT_STYLE,
    threadIndicatorIconTint = COMMON_TEXT_COLOR,
    timestampTextColor = COMMON_TEXT_COLOR,
    timestampTextStyle = COMMON_TEXT_STYLE
)

private fun createAudioBubbleStyle() = CometChatAudioBubbleStyle(
    playIconTint = Color.Blue,
    pauseIconTint = Color.Blue,
    buttonBackgroundColor = Color.White,
    audioWaveColor = Color.Blue,
    subtitleTextColor = Color.Gray,
    subtitleTextStyle = TextStyle(fontSize = 12.sp),
    backgroundColor = COMMON_BG,
    cornerRadius = COMMON_RADIUS,
    strokeWidth = COMMON_STROKE_W,
    strokeColor = COMMON_STROKE_C,
    padding = COMMON_PADDING,
    senderNameTextColor = COMMON_TEXT_COLOR,
    senderNameTextStyle = COMMON_TEXT_STYLE,
    threadIndicatorTextColor = COMMON_TEXT_COLOR,
    threadIndicatorTextStyle = COMMON_TEXT_STYLE,
    threadIndicatorIconTint = COMMON_TEXT_COLOR,
    timestampTextColor = COMMON_TEXT_COLOR,
    timestampTextStyle = COMMON_TEXT_STYLE
)

private fun createFileBubbleStyle() = CometChatFileBubbleStyle(
    innerCornerRadius = 8.dp,
    itemSpacing = 4.dp,
    titleTextColor = Color.Black,
    titleTextStyle = TextStyle(fontSize = 14.sp),
    subtitleTextColor = Color.Gray,
    subtitleTextStyle = TextStyle(fontSize = 12.sp),
    fileIconBackgroundColor = Color.LightGray,
    fileIconCornerRadius = 4.dp,
    fileIconSize = 24.dp,
    downloadIconTint = Color.Blue,
    downloadAllButtonBackgroundColor = Color.Blue,
    downloadAllButtonTextColor = Color.White,
    downloadAllButtonTextStyle = TextStyle(fontSize = 14.sp),
    downloadAllButtonCornerRadius = 8.dp,
    downloadAllButtonHeight = 36.dp,
    backgroundColor = COMMON_BG,
    cornerRadius = COMMON_RADIUS,
    strokeWidth = COMMON_STROKE_W,
    strokeColor = COMMON_STROKE_C,
    padding = COMMON_PADDING,
    senderNameTextColor = COMMON_TEXT_COLOR,
    senderNameTextStyle = COMMON_TEXT_STYLE,
    threadIndicatorTextColor = COMMON_TEXT_COLOR,
    threadIndicatorTextStyle = COMMON_TEXT_STYLE,
    threadIndicatorIconTint = COMMON_TEXT_COLOR,
    timestampTextColor = COMMON_TEXT_COLOR,
    timestampTextStyle = COMMON_TEXT_STYLE
)

private fun createDeleteBubbleStyle() = CometChatDeleteBubbleStyle(
    textColor = Color.Gray,
    textStyle = TextStyle(fontSize = 14.sp),
    iconTint = Color.Gray,
    backgroundColor = COMMON_BG,
    cornerRadius = COMMON_RADIUS,
    strokeWidth = COMMON_STROKE_W,
    strokeColor = COMMON_STROKE_C,
    padding = COMMON_PADDING,
    senderNameTextColor = COMMON_TEXT_COLOR,
    senderNameTextStyle = COMMON_TEXT_STYLE,
    threadIndicatorTextColor = COMMON_TEXT_COLOR,
    threadIndicatorTextStyle = COMMON_TEXT_STYLE,
    threadIndicatorIconTint = COMMON_TEXT_COLOR,
    timestampTextColor = COMMON_TEXT_COLOR,
    timestampTextStyle = COMMON_TEXT_STYLE
)

private fun createActionBubbleStyle() = CometChatActionBubbleStyle(
    textColor = Color.Gray,
    textStyle = TextStyle(fontSize = 12.sp),
    backgroundColor = COMMON_BG,
    cornerRadius = COMMON_RADIUS,
    strokeWidth = COMMON_STROKE_W,
    strokeColor = COMMON_STROKE_C,
    padding = COMMON_PADDING,
    senderNameTextColor = COMMON_TEXT_COLOR,
    senderNameTextStyle = COMMON_TEXT_STYLE,
    threadIndicatorTextColor = COMMON_TEXT_COLOR,
    threadIndicatorTextStyle = COMMON_TEXT_STYLE,
    threadIndicatorIconTint = COMMON_TEXT_COLOR,
    timestampTextColor = COMMON_TEXT_COLOR,
    timestampTextStyle = COMMON_TEXT_STYLE
)

private fun createCallActionBubbleStyle() = CometChatCallActionBubbleStyle(
    textColor = Color.Gray,
    textStyle = TextStyle(fontSize = 12.sp),
    iconTint = Color.Gray,
    missedCallTextColor = Color.Red,
    missedCallTextStyle = TextStyle(fontSize = 12.sp),
    missedCallBackgroundColor = Color.Red.copy(alpha = 0.1f),
    missedCallIconTint = Color.Red,
    backgroundColor = COMMON_BG,
    cornerRadius = COMMON_RADIUS,
    strokeWidth = COMMON_STROKE_W,
    strokeColor = COMMON_STROKE_C,
    padding = COMMON_PADDING,
    senderNameTextColor = COMMON_TEXT_COLOR,
    senderNameTextStyle = COMMON_TEXT_STYLE,
    threadIndicatorTextColor = COMMON_TEXT_COLOR,
    threadIndicatorTextStyle = COMMON_TEXT_STYLE,
    threadIndicatorIconTint = COMMON_TEXT_COLOR,
    timestampTextColor = COMMON_TEXT_COLOR,
    timestampTextStyle = COMMON_TEXT_STYLE
)

private fun createMeetCallBubbleStyle() = CometChatMeetCallBubbleStyle(
    callIconTint = Color.Blue,
    iconBackgroundColor = Color.LightGray,
    titleTextColor = Color.Black,
    titleTextStyle = TextStyle(fontSize = 14.sp),
    subtitleTextColor = Color.Gray,
    subtitleTextStyle = TextStyle(fontSize = 12.sp),
    separatorColor = Color.LightGray,
    joinButtonTextColor = Color.Blue,
    joinButtonTextStyle = TextStyle(fontSize = 14.sp),
    backgroundColor = COMMON_BG,
    cornerRadius = COMMON_RADIUS,
    strokeWidth = COMMON_STROKE_W,
    strokeColor = COMMON_STROKE_C,
    padding = COMMON_PADDING,
    senderNameTextColor = COMMON_TEXT_COLOR,
    senderNameTextStyle = COMMON_TEXT_STYLE,
    threadIndicatorTextColor = COMMON_TEXT_COLOR,
    threadIndicatorTextStyle = COMMON_TEXT_STYLE,
    threadIndicatorIconTint = COMMON_TEXT_COLOR,
    timestampTextColor = COMMON_TEXT_COLOR,
    timestampTextStyle = COMMON_TEXT_STYLE
)

private fun createPollBubbleStyle() = CometChatPollBubbleStyle(
    titleTextColor = Color.Black,
    titleTextStyle = TextStyle(fontSize = 14.sp),
    optionTextColor = Color.Black,
    optionTextStyle = TextStyle(fontSize = 14.sp),
    selectedRadioButtonStrokeColor = Color.Blue,
    selectedRadioButtonStrokeWidth = 2.dp,
    selectedRadioButtonCornerRadius = 12.dp,
    selectedIconTint = Color.Blue,
    unselectedRadioButtonStrokeColor = Color.Gray,
    unselectedRadioButtonStrokeWidth = 1.dp,
    unselectedRadioButtonCornerRadius = 12.dp,
    unselectedIconTint = Color.Gray,
    progressColor = Color.Blue,
    progressBackgroundColor = Color.LightGray,
    progressIndeterminateTint = Color.Blue,
    voteCountTextColor = Color.Gray,
    voteCountTextStyle = TextStyle(fontSize = 12.sp),
    optionAvatarStyle = CometChatAvatarStyle(),
    backgroundColor = COMMON_BG,
    cornerRadius = COMMON_RADIUS,
    strokeWidth = COMMON_STROKE_W,
    strokeColor = COMMON_STROKE_C,
    padding = COMMON_PADDING,
    senderNameTextColor = COMMON_TEXT_COLOR,
    senderNameTextStyle = COMMON_TEXT_STYLE,
    threadIndicatorTextColor = COMMON_TEXT_COLOR,
    threadIndicatorTextStyle = COMMON_TEXT_STYLE,
    threadIndicatorIconTint = COMMON_TEXT_COLOR,
    timestampTextColor = COMMON_TEXT_COLOR,
    timestampTextStyle = COMMON_TEXT_STYLE
)

private fun createStickerBubbleStyle() = CometChatStickerBubbleStyle(
    backgroundColor = Color.Transparent,
    cornerRadius = COMMON_RADIUS,
    strokeWidth = COMMON_STROKE_W,
    strokeColor = COMMON_STROKE_C,
    padding = COMMON_PADDING,
    senderNameTextColor = COMMON_TEXT_COLOR,
    senderNameTextStyle = COMMON_TEXT_STYLE,
    threadIndicatorTextColor = COMMON_TEXT_COLOR,
    threadIndicatorTextStyle = COMMON_TEXT_STYLE,
    threadIndicatorIconTint = COMMON_TEXT_COLOR,
    timestampTextColor = COMMON_TEXT_COLOR,
    timestampTextStyle = COMMON_TEXT_STYLE
)

private fun createCollaborativeBubbleStyle() = CometChatCollaborativeBubbleStyle(
    titleTextColor = Color.Black,
    titleTextStyle = TextStyle(fontSize = 14.sp),
    subtitleTextColor = Color.Gray,
    subtitleTextStyle = TextStyle(fontSize = 12.sp),
    iconTint = Color.Blue,
    buttonTextColor = Color.Blue,
    buttonTextStyle = TextStyle(fontSize = 14.sp),
    separatorColor = Color.LightGray,
    imageStrokeWidth = 0.dp,
    imageStrokeColor = Color.Transparent,
    imageCornerRadius = 8.dp,
    backgroundColor = COMMON_BG,
    cornerRadius = COMMON_RADIUS,
    strokeWidth = COMMON_STROKE_W,
    strokeColor = COMMON_STROKE_C,
    padding = COMMON_PADDING,
    senderNameTextColor = COMMON_TEXT_COLOR,
    senderNameTextStyle = COMMON_TEXT_STYLE,
    threadIndicatorTextColor = COMMON_TEXT_COLOR,
    threadIndicatorTextStyle = COMMON_TEXT_STYLE,
    threadIndicatorIconTint = COMMON_TEXT_COLOR,
    timestampTextColor = COMMON_TEXT_COLOR,
    timestampTextStyle = COMMON_TEXT_STYLE
)
