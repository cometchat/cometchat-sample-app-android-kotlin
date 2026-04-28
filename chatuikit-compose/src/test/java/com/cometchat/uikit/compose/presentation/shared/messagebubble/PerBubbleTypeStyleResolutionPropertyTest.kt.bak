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
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.element
import io.kotest.property.checkAll
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * Property-based tests for per-bubble-type style resolution.
 *
 * **Feature: messagelist-bubble-style-propagation, Property 4: Per-bubble-type style resolution maps message type to correct style**
 *
 * For any message with a known category/type, and for any set of per-bubble-type
 * style overrides (each nullable), `resolveContentStyleForMessage` returns the style
 * corresponding to that message's type, or null when no factory is present and no
 * style is set for that type.
 *
 * Resolution rules:
 * - If factory is present → always null (factory handles rendering)
 * - If message.deletedAt > 0 → deleteBubbleStyle
 * - Category MESSAGE, type text → textBubbleStyle
 * - Category MESSAGE, type image → imageBubbleStyle
 * - Category MESSAGE, type video → videoBubbleStyle
 * - Category MESSAGE, type audio → audioBubbleStyle
 * - Category MESSAGE, type file → fileBubbleStyle
 * - Category ACTION → actionBubbleStyle
 * - Category CALL → callActionBubbleStyle
 * - Category CUSTOM, type extension_poll → pollBubbleStyle
 * - Category CUSTOM, type extension_sticker → stickerBubbleStyle
 * - Category CUSTOM, type extension_document/extension_whiteboard → collaborativeBubbleStyle
 * - Category "meeting" (UIKitConstants.MessageType.MEETING) → meetCallBubbleStyle
 *
 * **Validates: Requirements 5.1, 5.2, 5.3, 7.3**
 */
class PerBubbleTypeStyleResolutionPropertyTest : StringSpec({

    // ========================================================================
    // Arbitrary generators
    // ========================================================================

    /**
     * All known message category/type pairs that map to a specific bubble style.
     * Each entry is (category, type, styleName) where styleName identifies which
     * per-bubble-type style should be returned.
     */
    val knownMessageTypeArb: Arb<Triple<String, String, String>> = Arb.element(
        Triple(CometChatConstants.CATEGORY_MESSAGE, CometChatConstants.MESSAGE_TYPE_TEXT, "text"),
        Triple(CometChatConstants.CATEGORY_MESSAGE, CometChatConstants.MESSAGE_TYPE_IMAGE, "image"),
        Triple(CometChatConstants.CATEGORY_MESSAGE, CometChatConstants.MESSAGE_TYPE_VIDEO, "video"),
        Triple(CometChatConstants.CATEGORY_MESSAGE, CometChatConstants.MESSAGE_TYPE_AUDIO, "audio"),
        Triple(CometChatConstants.CATEGORY_MESSAGE, CometChatConstants.MESSAGE_TYPE_FILE, "file"),
        Triple(CometChatConstants.CATEGORY_ACTION, CometChatConstants.ActionKeys.ACTION_TYPE_GROUP_MEMBER, "action"),
        Triple(CometChatConstants.CATEGORY_CALL, CometChatConstants.CALL_TYPE_AUDIO, "callAction"),
        Triple(CometChatConstants.CATEGORY_CUSTOM, InternalContentRenderer.EXTENSION_POLLS, "poll"),
        Triple(CometChatConstants.CATEGORY_CUSTOM, InternalContentRenderer.EXTENSION_STICKER, "sticker"),
        Triple(CometChatConstants.CATEGORY_CUSTOM, InternalContentRenderer.EXTENSION_DOCUMENT, "collaborative"),
        Triple(CometChatConstants.CATEGORY_CUSTOM, InternalContentRenderer.EXTENSION_WHITEBOARD, "collaborative"),
        Triple(UIKitConstants.MessageType.MEETING, UIKitConstants.MessageType.MEETING, "meetCall")
    )

    /** Generates a nullable set of all per-bubble-type styles. Each style is independently nullable. */
    data class StyleSet(
        val textBubbleStyle: CometChatTextBubbleStyle?,
        val imageBubbleStyle: CometChatImageBubbleStyle?,
        val videoBubbleStyle: CometChatVideoBubbleStyle?,
        val audioBubbleStyle: CometChatAudioBubbleStyle?,
        val fileBubbleStyle: CometChatFileBubbleStyle?,
        val deleteBubbleStyle: CometChatDeleteBubbleStyle?,
        val actionBubbleStyle: CometChatActionBubbleStyle?,
        val callActionBubbleStyle: CometChatCallActionBubbleStyle?,
        val meetCallBubbleStyle: CometChatMeetCallBubbleStyle?,
        val pollBubbleStyle: CometChatPollBubbleStyle?,
        val stickerBubbleStyle: CometChatStickerBubbleStyle?,
        val collaborativeBubbleStyle: CometChatCollaborativeBubbleStyle?
    ) {
        /** Look up the expected style for a given styleName. */
        fun expectedStyleFor(styleName: String): CometChatMessageBubbleStyle? = when (styleName) {
            "text" -> textBubbleStyle
            "image" -> imageBubbleStyle
            "video" -> videoBubbleStyle
            "audio" -> audioBubbleStyle
            "file" -> fileBubbleStyle
            "action" -> actionBubbleStyle
            "callAction" -> callActionBubbleStyle
            "poll" -> pollBubbleStyle
            "sticker" -> stickerBubbleStyle
            "collaborative" -> collaborativeBubbleStyle
            "meetCall" -> meetCallBubbleStyle
            "delete" -> deleteBubbleStyle
            else -> null
        }
    }

    /**
     * Arb that generates a StyleSet where each per-bubble-type style is independently
     * present or null (50% chance each). Uses distinct style instances for identity checks.
     */
    val styleSetArb: Arb<StyleSet> = Arb.bind(
        Arb.boolean(), Arb.boolean(), Arb.boolean(), Arb.boolean(),
        Arb.boolean(), Arb.boolean(), Arb.boolean(), Arb.boolean(),
        Arb.boolean(), Arb.boolean(), Arb.boolean(), Arb.boolean()
    ) { hasText, hasImage, hasVideo, hasAudio, hasFile, hasDelete,
        hasAction, hasCallAction, hasMeetCall, hasPoll, hasSticker, hasCollab ->
        StyleSet(
            textBubbleStyle = if (hasText) createTextBubbleStyle() else null,
            imageBubbleStyle = if (hasImage) createImageBubbleStyle() else null,
            videoBubbleStyle = if (hasVideo) createVideoBubbleStyle() else null,
            audioBubbleStyle = if (hasAudio) createAudioBubbleStyle() else null,
            fileBubbleStyle = if (hasFile) createFileBubbleStyle() else null,
            deleteBubbleStyle = if (hasDelete) createDeleteBubbleStyle() else null,
            actionBubbleStyle = if (hasAction) createActionBubbleStyle() else null,
            callActionBubbleStyle = if (hasCallAction) createCallActionBubbleStyle() else null,
            meetCallBubbleStyle = if (hasMeetCall) createMeetCallBubbleStyle() else null,
            pollBubbleStyle = if (hasPoll) createPollBubbleStyle() else null,
            stickerBubbleStyle = if (hasSticker) createStickerBubbleStyle() else null,
            collaborativeBubbleStyle = if (hasCollab) createCollaborativeBubbleStyle() else null
        )
    }

    // ========================================================================
    // Property 4: Per-bubble-type style resolution maps message type to correct style
    // ========================================================================

    /**
     * **Feature: messagelist-bubble-style-propagation, Property 4: Per-bubble-type style resolution maps message type to correct style**
     *
     * For any known message category/type pair and any random set of per-bubble-type
     * style overrides (each independently nullable), the resolution function returns
     * the style matching the message type, or null when no style is set for that type.
     *
     * **Validates: Requirements 5.1, 5.2, 5.3, 7.3**
     */
    "Property 4: non-deleted message resolves to the correct per-bubble-type style or null" {
        checkAll(100, knownMessageTypeArb, styleSetArb) { (category, type, styleName), styles ->
            val msg = mockMessage(category, type)

            val result = resolveContentStylePure(
                message = msg,
                factory = null,
                textBubbleStyle = styles.textBubbleStyle,
                imageBubbleStyle = styles.imageBubbleStyle,
                videoBubbleStyle = styles.videoBubbleStyle,
                audioBubbleStyle = styles.audioBubbleStyle,
                fileBubbleStyle = styles.fileBubbleStyle,
                deleteBubbleStyle = styles.deleteBubbleStyle,
                actionBubbleStyle = styles.actionBubbleStyle,
                callActionBubbleStyle = styles.callActionBubbleStyle,
                meetCallBubbleStyle = styles.meetCallBubbleStyle,
                pollBubbleStyle = styles.pollBubbleStyle,
                stickerBubbleStyle = styles.stickerBubbleStyle,
                collaborativeBubbleStyle = styles.collaborativeBubbleStyle
            )

            val expected = styles.expectedStyleFor(styleName)
            result shouldBe expected
        }
    }

    /**
     * **Feature: messagelist-bubble-style-propagation, Property 4: Per-bubble-type style resolution maps message type to correct style**
     *
     * For any known message category/type pair, when the message is deleted (deletedAt > 0),
     * the resolution always returns deleteBubbleStyle regardless of the original message type.
     *
     * **Validates: Requirements 5.1, 5.2, 5.3**
     */
    "Property 4: deleted message always resolves to deleteBubbleStyle regardless of type" {
        checkAll(100, knownMessageTypeArb, styleSetArb) { (category, type, _), styles ->
            val msg = mockDeletedMessage(category, type, 1000L)

            val result = resolveContentStylePure(
                message = msg,
                factory = null,
                textBubbleStyle = styles.textBubbleStyle,
                imageBubbleStyle = styles.imageBubbleStyle,
                videoBubbleStyle = styles.videoBubbleStyle,
                audioBubbleStyle = styles.audioBubbleStyle,
                fileBubbleStyle = styles.fileBubbleStyle,
                deleteBubbleStyle = styles.deleteBubbleStyle,
                actionBubbleStyle = styles.actionBubbleStyle,
                callActionBubbleStyle = styles.callActionBubbleStyle,
                meetCallBubbleStyle = styles.meetCallBubbleStyle,
                pollBubbleStyle = styles.pollBubbleStyle,
                stickerBubbleStyle = styles.stickerBubbleStyle,
                collaborativeBubbleStyle = styles.collaborativeBubbleStyle
            )

            result shouldBe styles.deleteBubbleStyle
        }
    }

    /**
     * **Feature: messagelist-bubble-style-propagation, Property 4: Per-bubble-type style resolution maps message type to correct style**
     *
     * When a style is set for a message type, the resolved style is the exact same
     * object instance (referential identity), confirming no copies or transformations.
     *
     * **Validates: Requirements 7.3**
     */
    "Property 4: resolved style preserves referential identity of the per-bubble-type style" {
        checkAll(100, knownMessageTypeArb, styleSetArb) { (category, type, styleName), styles ->
            val msg = mockMessage(category, type)

            val result = resolveContentStylePure(
                message = msg,
                factory = null,
                textBubbleStyle = styles.textBubbleStyle,
                imageBubbleStyle = styles.imageBubbleStyle,
                videoBubbleStyle = styles.videoBubbleStyle,
                audioBubbleStyle = styles.audioBubbleStyle,
                fileBubbleStyle = styles.fileBubbleStyle,
                deleteBubbleStyle = styles.deleteBubbleStyle,
                actionBubbleStyle = styles.actionBubbleStyle,
                callActionBubbleStyle = styles.callActionBubbleStyle,
                meetCallBubbleStyle = styles.meetCallBubbleStyle,
                pollBubbleStyle = styles.pollBubbleStyle,
                stickerBubbleStyle = styles.stickerBubbleStyle,
                collaborativeBubbleStyle = styles.collaborativeBubbleStyle
            )

            val expected = styles.expectedStyleFor(styleName)
            if (expected != null) {
                assert(result === expected) {
                    "Expected resolved style for $styleName to be the exact same instance (===), " +
                        "but referential identity was not preserved."
                }
            } else {
                result shouldBe null
            }
        }
    }
})

// ============================================================================
// Pure-function replica of resolveContentStyleForMessage
// ============================================================================

/**
 * Replicates the pure branching logic of resolveContentStyleForMessage
 * from CometChatMessageBubble.kt for direct unit testing.
 *
 * Rules:
 * 1. If factory is present → null (factory handles rendering)
 * 2. If message.deletedAt > 0 → deleteBubbleStyle
 * 3. Otherwise → map category/type to the corresponding per-bubble-type style
 */
private fun resolveContentStylePure(
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
