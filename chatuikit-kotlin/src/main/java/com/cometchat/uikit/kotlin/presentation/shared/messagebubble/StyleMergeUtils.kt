package com.cometchat.uikit.kotlin.presentation.shared.messagebubble

import android.graphics.drawable.Drawable
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.actionbubble.CometChatActionBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.audiobubble.CometChatAudioBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.callactionbubble.CometChatCallActionBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.collaborativebubble.CometChatCollaborativeBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.deletebubble.CometChatDeleteBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.filebubble.CometChatFileBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.imagebubble.CometChatImageBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.meetcallbubble.CometChatMeetCallBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.pollbubble.CometChatPollBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.stickerbubble.CometChatStickerBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.textbubble.CometChatTextBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.videobubble.CometChatVideoBubbleStyle

/**
 * Helper holding resolved common properties after merging with a base style.
 */
private data class MergedCommon(
    val backgroundColor: Int,
    val backgroundDrawable: Drawable?,
    val cornerRadius: Float,
    val strokeWidth: Float,
    val strokeColor: Int,
    val senderNameTextAppearance: Int,
    val senderNameTextColor: Int,
    val threadIndicatorTextAppearance: Int,
    val threadIndicatorTextColor: Int,
    val threadIndicatorIconTint: Int,
    val timestampTextAppearance: Int,
    val timestampTextColor: Int
)

private fun computeMerged(
    bg: Int, drawable: Drawable?, radius: Float, strokeW: Float, strokeC: Int,
    senderNameApp: Int, senderNameColor: Int,
    threadApp: Int, threadColor: Int, threadIcon: Int,
    timestampApp: Int, timestampColor: Int,
    base: CometChatMessageBubbleStyle
): MergedCommon = MergedCommon(
    backgroundColor = resolveStyleColor(bg, base.backgroundColor),
    backgroundDrawable = drawable ?: base.backgroundDrawable,
    cornerRadius = resolveStyleDimension(radius, base.cornerRadius),
    strokeWidth = resolveStyleDimension(strokeW, base.strokeWidth),
    strokeColor = resolveStyleColor(strokeC, base.strokeColor),
    senderNameTextAppearance = resolveStyleRes(senderNameApp, base.senderNameTextAppearance),
    senderNameTextColor = resolveStyleColor(senderNameColor, base.senderNameTextColor),
    threadIndicatorTextAppearance = resolveStyleRes(threadApp, base.threadIndicatorTextAppearance),
    threadIndicatorTextColor = resolveStyleColor(threadColor, base.threadIndicatorTextColor),
    threadIndicatorIconTint = resolveStyleColor(threadIcon, base.threadIndicatorIconTint),
    timestampTextAppearance = resolveStyleRes(timestampApp, base.timestampTextAppearance),
    timestampTextColor = resolveStyleColor(timestampColor, base.timestampTextColor)
)

/**
 * Merges a bubble-specific style with a base [CometChatMessageBubbleStyle].
 *
 * For each common property (backgroundColor, cornerRadius, strokeWidth, strokeColor,
 * senderName*, threadIndicator*, timestamp*):
 * - If the bubble-specific value is a sentinel ([STYLE_NOT_SET] / [DIMENSION_NOT_SET]),
 *   use the base value.
 * - If the bubble-specific value is explicitly set, keep it.
 * - For backgroundDrawable: use bubble-specific if non-null, else base.
 *
 * Content-specific properties are always preserved from the original style.
 */
@Suppress("UNCHECKED_CAST")
fun <T : Any> mergeWithBase(
    bubbleStyle: T,
    base: CometChatMessageBubbleStyle
): T {
    return when (bubbleStyle) {
        is CometChatTextBubbleStyle -> {
            val m = computeMerged(
                bubbleStyle.backgroundColor, bubbleStyle.backgroundDrawable, bubbleStyle.cornerRadius,
                bubbleStyle.strokeWidth, bubbleStyle.strokeColor,
                bubbleStyle.senderNameTextAppearance, bubbleStyle.senderNameTextColor,
                bubbleStyle.threadIndicatorTextAppearance, bubbleStyle.threadIndicatorTextColor, bubbleStyle.threadIndicatorIconTint,
                bubbleStyle.timestampTextAppearance, bubbleStyle.timestampTextColor, base
            )
            bubbleStyle.copy(
                backgroundColor = m.backgroundColor, backgroundDrawable = m.backgroundDrawable,
                cornerRadius = m.cornerRadius, strokeWidth = m.strokeWidth, strokeColor = m.strokeColor,
                senderNameTextAppearance = m.senderNameTextAppearance, senderNameTextColor = m.senderNameTextColor,
                threadIndicatorTextAppearance = m.threadIndicatorTextAppearance, threadIndicatorTextColor = m.threadIndicatorTextColor,
                threadIndicatorIconTint = m.threadIndicatorIconTint,
                timestampTextAppearance = m.timestampTextAppearance, timestampTextColor = m.timestampTextColor
            ) as T
        }

        is CometChatImageBubbleStyle -> {
            val m = computeMerged(
                bubbleStyle.backgroundColor, bubbleStyle.backgroundDrawable, bubbleStyle.cornerRadius,
                bubbleStyle.strokeWidth, bubbleStyle.strokeColor,
                bubbleStyle.senderNameTextAppearance, bubbleStyle.senderNameTextColor,
                bubbleStyle.threadIndicatorTextAppearance, bubbleStyle.threadIndicatorTextColor, bubbleStyle.threadIndicatorIconTint,
                bubbleStyle.timestampTextAppearance, bubbleStyle.timestampTextColor, base
            )
            bubbleStyle.copy(
                backgroundColor = m.backgroundColor, backgroundDrawable = m.backgroundDrawable,
                cornerRadius = m.cornerRadius, strokeWidth = m.strokeWidth, strokeColor = m.strokeColor,
                senderNameTextAppearance = m.senderNameTextAppearance, senderNameTextColor = m.senderNameTextColor,
                threadIndicatorTextAppearance = m.threadIndicatorTextAppearance, threadIndicatorTextColor = m.threadIndicatorTextColor,
                threadIndicatorIconTint = m.threadIndicatorIconTint,
                timestampTextAppearance = m.timestampTextAppearance, timestampTextColor = m.timestampTextColor
            ) as T
        }

        is CometChatVideoBubbleStyle -> {
            val m = computeMerged(
                bubbleStyle.backgroundColor, bubbleStyle.backgroundDrawable, bubbleStyle.cornerRadius,
                bubbleStyle.strokeWidth, bubbleStyle.strokeColor,
                bubbleStyle.senderNameTextAppearance, bubbleStyle.senderNameTextColor,
                bubbleStyle.threadIndicatorTextAppearance, bubbleStyle.threadIndicatorTextColor, bubbleStyle.threadIndicatorIconTint,
                bubbleStyle.timestampTextAppearance, bubbleStyle.timestampTextColor, base
            )
            bubbleStyle.copy(
                backgroundColor = m.backgroundColor, backgroundDrawable = m.backgroundDrawable,
                cornerRadius = m.cornerRadius, strokeWidth = m.strokeWidth, strokeColor = m.strokeColor,
                senderNameTextAppearance = m.senderNameTextAppearance, senderNameTextColor = m.senderNameTextColor,
                threadIndicatorTextAppearance = m.threadIndicatorTextAppearance, threadIndicatorTextColor = m.threadIndicatorTextColor,
                threadIndicatorIconTint = m.threadIndicatorIconTint,
                timestampTextAppearance = m.timestampTextAppearance, timestampTextColor = m.timestampTextColor
            ) as T
        }

        is CometChatAudioBubbleStyle -> {
            val m = computeMerged(
                bubbleStyle.backgroundColor, bubbleStyle.backgroundDrawable, bubbleStyle.cornerRadius,
                bubbleStyle.strokeWidth, bubbleStyle.strokeColor,
                bubbleStyle.senderNameTextAppearance, bubbleStyle.senderNameTextColor,
                bubbleStyle.threadIndicatorTextAppearance, bubbleStyle.threadIndicatorTextColor, bubbleStyle.threadIndicatorIconTint,
                bubbleStyle.timestampTextAppearance, bubbleStyle.timestampTextColor, base
            )
            bubbleStyle.copy(
                backgroundColor = m.backgroundColor, backgroundDrawable = m.backgroundDrawable,
                cornerRadius = m.cornerRadius, strokeWidth = m.strokeWidth, strokeColor = m.strokeColor,
                senderNameTextAppearance = m.senderNameTextAppearance, senderNameTextColor = m.senderNameTextColor,
                threadIndicatorTextAppearance = m.threadIndicatorTextAppearance, threadIndicatorTextColor = m.threadIndicatorTextColor,
                threadIndicatorIconTint = m.threadIndicatorIconTint,
                timestampTextAppearance = m.timestampTextAppearance, timestampTextColor = m.timestampTextColor
            ) as T
        }

        is CometChatFileBubbleStyle -> {
            val m = computeMerged(
                bubbleStyle.backgroundColor, bubbleStyle.backgroundDrawable, bubbleStyle.cornerRadius,
                bubbleStyle.strokeWidth, bubbleStyle.strokeColor,
                bubbleStyle.senderNameTextAppearance, bubbleStyle.senderNameTextColor,
                bubbleStyle.threadIndicatorTextAppearance, bubbleStyle.threadIndicatorTextColor, bubbleStyle.threadIndicatorIconTint,
                bubbleStyle.timestampTextAppearance, bubbleStyle.timestampTextColor, base
            )
            bubbleStyle.copy(
                backgroundColor = m.backgroundColor, backgroundDrawable = m.backgroundDrawable,
                cornerRadius = m.cornerRadius, strokeWidth = m.strokeWidth, strokeColor = m.strokeColor,
                senderNameTextAppearance = m.senderNameTextAppearance, senderNameTextColor = m.senderNameTextColor,
                threadIndicatorTextAppearance = m.threadIndicatorTextAppearance, threadIndicatorTextColor = m.threadIndicatorTextColor,
                threadIndicatorIconTint = m.threadIndicatorIconTint,
                timestampTextAppearance = m.timestampTextAppearance, timestampTextColor = m.timestampTextColor
            ) as T
        }

        is CometChatDeleteBubbleStyle -> {
            val m = computeMerged(
                bubbleStyle.backgroundColor, bubbleStyle.backgroundDrawable, bubbleStyle.cornerRadius,
                bubbleStyle.strokeWidth, bubbleStyle.strokeColor,
                bubbleStyle.senderNameTextAppearance, bubbleStyle.senderNameTextColor,
                bubbleStyle.threadIndicatorTextAppearance, bubbleStyle.threadIndicatorTextColor, bubbleStyle.threadIndicatorIconTint,
                bubbleStyle.timestampTextAppearance, bubbleStyle.timestampTextColor, base
            )
            bubbleStyle.copy(
                backgroundColor = m.backgroundColor, backgroundDrawable = m.backgroundDrawable,
                cornerRadius = m.cornerRadius, strokeWidth = m.strokeWidth, strokeColor = m.strokeColor,
                senderNameTextAppearance = m.senderNameTextAppearance, senderNameTextColor = m.senderNameTextColor,
                threadIndicatorTextAppearance = m.threadIndicatorTextAppearance, threadIndicatorTextColor = m.threadIndicatorTextColor,
                threadIndicatorIconTint = m.threadIndicatorIconTint,
                timestampTextAppearance = m.timestampTextAppearance, timestampTextColor = m.timestampTextColor
            ) as T
        }

        is CometChatActionBubbleStyle -> {
            // Action bubbles are centered system messages without sender names, thread indicators, or timestamps.
            // They only need container styling merged from base.
            val m = computeMerged(
                bubbleStyle.backgroundColor, bubbleStyle.backgroundDrawable, bubbleStyle.cornerRadius,
                bubbleStyle.strokeWidth, bubbleStyle.strokeColor,
                0, 0, // No sender name styling for action bubbles
                0, 0, 0, // No thread indicator styling for action bubbles
                0, 0, // No timestamp styling for action bubbles
                base
            )
            bubbleStyle.copy(
                backgroundColor = m.backgroundColor, backgroundDrawable = m.backgroundDrawable,
                cornerRadius = m.cornerRadius, strokeWidth = m.strokeWidth, strokeColor = m.strokeColor
            ) as T
        }

        is CometChatCallActionBubbleStyle -> {
            val m = computeMerged(
                bubbleStyle.backgroundColor, bubbleStyle.backgroundDrawable, bubbleStyle.cornerRadius,
                bubbleStyle.strokeWidth, bubbleStyle.strokeColor,
                bubbleStyle.senderNameTextAppearance, bubbleStyle.senderNameTextColor,
                bubbleStyle.threadIndicatorTextAppearance, bubbleStyle.threadIndicatorTextColor, bubbleStyle.threadIndicatorIconTint,
                bubbleStyle.timestampTextAppearance, bubbleStyle.timestampTextColor, base
            )
            bubbleStyle.copy(
                backgroundColor = m.backgroundColor, backgroundDrawable = m.backgroundDrawable,
                cornerRadius = m.cornerRadius, strokeWidth = m.strokeWidth, strokeColor = m.strokeColor,
                senderNameTextAppearance = m.senderNameTextAppearance, senderNameTextColor = m.senderNameTextColor,
                threadIndicatorTextAppearance = m.threadIndicatorTextAppearance, threadIndicatorTextColor = m.threadIndicatorTextColor,
                threadIndicatorIconTint = m.threadIndicatorIconTint,
                timestampTextAppearance = m.timestampTextAppearance, timestampTextColor = m.timestampTextColor
            ) as T
        }

        is CometChatMeetCallBubbleStyle -> {
            val m = computeMerged(
                bubbleStyle.backgroundColor, bubbleStyle.backgroundDrawable, bubbleStyle.cornerRadius,
                bubbleStyle.strokeWidth, bubbleStyle.strokeColor,
                bubbleStyle.senderNameTextAppearance, bubbleStyle.senderNameTextColor,
                bubbleStyle.threadIndicatorTextAppearance, bubbleStyle.threadIndicatorTextColor, bubbleStyle.threadIndicatorIconTint,
                bubbleStyle.timestampTextAppearance, bubbleStyle.timestampTextColor, base
            )
            bubbleStyle.copy(
                backgroundColor = m.backgroundColor, backgroundDrawable = m.backgroundDrawable,
                cornerRadius = m.cornerRadius, strokeWidth = m.strokeWidth, strokeColor = m.strokeColor,
                senderNameTextAppearance = m.senderNameTextAppearance, senderNameTextColor = m.senderNameTextColor,
                threadIndicatorTextAppearance = m.threadIndicatorTextAppearance, threadIndicatorTextColor = m.threadIndicatorTextColor,
                threadIndicatorIconTint = m.threadIndicatorIconTint,
                timestampTextAppearance = m.timestampTextAppearance, timestampTextColor = m.timestampTextColor
            ) as T
        }

        is CometChatPollBubbleStyle -> {
            val m = computeMerged(
                bubbleStyle.backgroundColor, bubbleStyle.backgroundDrawable, bubbleStyle.cornerRadius,
                bubbleStyle.strokeWidth, bubbleStyle.strokeColor,
                bubbleStyle.senderNameTextAppearance, bubbleStyle.senderNameTextColor,
                bubbleStyle.threadIndicatorTextAppearance, bubbleStyle.threadIndicatorTextColor, bubbleStyle.threadIndicatorIconTint,
                bubbleStyle.timestampTextAppearance, bubbleStyle.timestampTextColor, base
            )
            bubbleStyle.copy(
                backgroundColor = m.backgroundColor, backgroundDrawable = m.backgroundDrawable,
                cornerRadius = m.cornerRadius, strokeWidth = m.strokeWidth, strokeColor = m.strokeColor,
                senderNameTextAppearance = m.senderNameTextAppearance, senderNameTextColor = m.senderNameTextColor,
                threadIndicatorTextAppearance = m.threadIndicatorTextAppearance, threadIndicatorTextColor = m.threadIndicatorTextColor,
                threadIndicatorIconTint = m.threadIndicatorIconTint,
                timestampTextAppearance = m.timestampTextAppearance, timestampTextColor = m.timestampTextColor
            ) as T
        }

        is CometChatStickerBubbleStyle -> {
            val m = computeMerged(
                bubbleStyle.backgroundColor, bubbleStyle.backgroundDrawable, bubbleStyle.cornerRadius,
                bubbleStyle.strokeWidth, bubbleStyle.strokeColor,
                bubbleStyle.senderNameTextAppearance, bubbleStyle.senderNameTextColor,
                bubbleStyle.threadIndicatorTextAppearance, bubbleStyle.threadIndicatorTextColor, bubbleStyle.threadIndicatorIconTint,
                bubbleStyle.timestampTextAppearance, bubbleStyle.timestampTextColor, base
            )
            bubbleStyle.copy(
                backgroundColor = m.backgroundColor, backgroundDrawable = m.backgroundDrawable,
                cornerRadius = m.cornerRadius, strokeWidth = m.strokeWidth, strokeColor = m.strokeColor,
                senderNameTextAppearance = m.senderNameTextAppearance, senderNameTextColor = m.senderNameTextColor,
                threadIndicatorTextAppearance = m.threadIndicatorTextAppearance, threadIndicatorTextColor = m.threadIndicatorTextColor,
                threadIndicatorIconTint = m.threadIndicatorIconTint,
                timestampTextAppearance = m.timestampTextAppearance, timestampTextColor = m.timestampTextColor
            ) as T
        }

        is CometChatCollaborativeBubbleStyle -> {
            val m = computeMerged(
                bubbleStyle.backgroundColor, bubbleStyle.backgroundDrawable, bubbleStyle.cornerRadius,
                bubbleStyle.strokeWidth, bubbleStyle.strokeColor,
                bubbleStyle.senderNameTextAppearance, bubbleStyle.senderNameTextColor,
                bubbleStyle.threadIndicatorTextAppearance, bubbleStyle.threadIndicatorTextColor, bubbleStyle.threadIndicatorIconTint,
                bubbleStyle.timestampTextAppearance, bubbleStyle.timestampTextColor, base
            )
            bubbleStyle.copy(
                backgroundColor = m.backgroundColor, backgroundDrawable = m.backgroundDrawable,
                cornerRadius = m.cornerRadius, strokeWidth = m.strokeWidth, strokeColor = m.strokeColor,
                senderNameTextAppearance = m.senderNameTextAppearance, senderNameTextColor = m.senderNameTextColor,
                threadIndicatorTextAppearance = m.threadIndicatorTextAppearance, threadIndicatorTextColor = m.threadIndicatorTextColor,
                threadIndicatorIconTint = m.threadIndicatorIconTint,
                timestampTextAppearance = m.timestampTextAppearance, timestampTextColor = m.timestampTextColor
            ) as T
        }

        else -> bubbleStyle
    }
}
