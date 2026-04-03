package com.cometchat.uikit.compose.presentation.shared.messagebubble.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.cometchat.chat.models.CustomMessage
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatStickerBubbleStyle
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * A composable that displays a sticker message bubble.
 *
 * This component renders sticker images with support for:
 * - Aspect ratio preservation
 * - Loading state with progress indicator
 * - Error state with placeholder
 * - Click handling
 *
 * Example usage:
 * ```kotlin
 * CometChatStickerBubble(
 *     message = customMessage,
 *     alignment = MessageBubbleAlignment.LEFT,
 *     style = CometChatStickerBubbleStyle.incoming()
 * )
 * ```
 *
 * @param message The [CustomMessage] containing sticker data in customData
 * @param alignment The bubble alignment (LEFT, RIGHT, or CENTER)
 * @param modifier Modifier for the bubble container
 * @param style Style configuration for the bubble appearance. Since [CometChatStickerBubbleStyle]
 *   extends [CometChatMessageBubbleStyle], all wrapper properties (backgroundColor, cornerRadius,
 *   strokeWidth, strokeColor) are directly accessible on the style object.
 * @param onClick Callback when the sticker is clicked
 * @param onLongClick Callback when the bubble is long-pressed (propagates to parent message bubble)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CometChatStickerBubble(
    message: CustomMessage,
    alignment: UIKitConstants.MessageBubbleAlignment,
    modifier: Modifier = Modifier,
    style: CometChatStickerBubbleStyle = when (alignment) {
        UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatStickerBubbleStyle.outgoing()
        else -> CometChatStickerBubbleStyle.incoming()
    },
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    // Extract sticker URL from customData
    val stickerUrl = remember(message.id) {
        extractStickerUrl(message)
    }
    val stickerName = remember(message.id) {
        extractStickerName(message)
    }

    CometChatStickerBubble(
        stickerUrl = stickerUrl,
        stickerName = stickerName,
        modifier = modifier,
        style = style,
        onClick = onClick,
        onLongClick = onLongClick
    )
}

/**
 * Overload for displaying sticker bubble with a URL directly.
 * Useful for previews and testing.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CometChatStickerBubble(
    stickerUrl: String,
    stickerName: String? = null,
    modifier: Modifier = Modifier,
    style: CometChatStickerBubbleStyle = CometChatStickerBubbleStyle.default(),
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    StickerBubbleContent(
        stickerUrl = stickerUrl,
        stickerName = stickerName,
        modifier = modifier,
        style = style,
        onClick = onClick,
        onLongClick = onLongClick
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun StickerBubbleContent(
    stickerUrl: String,
    stickerName: String?,
    modifier: Modifier,
    style: CometChatStickerBubbleStyle,
    onClick: (() -> Unit)?,
    onLongClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .width(240.dp)
            .height(196.dp)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onClick?.invoke() },
                onLongClick = onLongClick
            )
            .semantics {
                contentDescription = stickerName ?: "Sticker"
            }
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(stickerUrl)
                .crossfade(true)
                .build(),
            contentDescription = stickerName ?: "Sticker",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = CometChatTheme.colorScheme.iconTintSecondary,
                        strokeWidth = 2.dp
                    )
                }
            },
            error = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CometChatTheme.colorScheme.backgroundColor3),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Failed to load sticker",
                        style = CometChatTheme.typography.caption1Regular,
                        color = CometChatTheme.colorScheme.textColorTertiary
                    )
                }
            }
        )
    }
}

/**
 * Extracts the sticker URL from a CustomMessage's customData.
 */
private fun extractStickerUrl(message: CustomMessage): String {
    return try {
        val customData = message.customData
        customData?.optString("sticker_url", "") 
            ?: customData?.optString("url", "")
            ?: ""
    } catch (e: Exception) {
        ""
    }
}

/**
 * Extracts the sticker name from a CustomMessage's customData.
 */
private fun extractStickerName(message: CustomMessage): String? {
    return try {
        val customData = message.customData
        customData?.optString("sticker_name", null)
            ?: customData?.optString("name", null)
    } catch (e: Exception) {
        null
    }
}
