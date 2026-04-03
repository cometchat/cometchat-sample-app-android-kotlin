package com.cometchat.uikit.compose.presentation.shared.messagebubble.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cometchat.chat.models.CustomMessage
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatCollaborativeBubbleStyle

/**
 * Enum representing collaborative document types.
 */
enum class CollaborativeType {
    DOCUMENT,
    WHITEBOARD
}

/**
 * A composable that displays a collaborative document/whiteboard message bubble.
 *
 * This component renders collaborative messages with:
 * - Document/whiteboard icon
 * - Title and subtitle
 * - "Join" or "Open" button
 *
 * Example usage:
 * ```kotlin
 * CometChatCollaborativeBubble(
 *     message = customMessage,
 *     alignment = MessageBubbleAlignment.LEFT,
 *     style = CometChatCollaborativeBubbleStyle.incoming()
 * )
 * ```
 *
 * @param message The [CustomMessage] containing collaborative data
 * @param alignment The bubble alignment (LEFT, RIGHT, or CENTER)
 * @param modifier Modifier for the bubble container
 * @param style Style configuration for the bubble appearance (extends CometChatMessageBubbleStyle)
 * @param onJoinClick Callback when the "Join" button is clicked with the document URL
 */
@Composable
fun CometChatCollaborativeBubble(
    message: CustomMessage,
    alignment: UIKitConstants.MessageBubbleAlignment,
    modifier: Modifier = Modifier,
    style: CometChatCollaborativeBubbleStyle = when (alignment) {
        UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatCollaborativeBubbleStyle.outgoing()
        else -> CometChatCollaborativeBubbleStyle.incoming()
    },
    onJoinClick: ((String) -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    val collaborativeData = extractCollaborativeDataWithLocalizedDefaults(message)

    CometChatCollaborativeBubble(
        title = collaborativeData.title,
        subtitle = collaborativeData.subtitle,
        type = collaborativeData.type,
        url = collaborativeData.url,
        buttonText = collaborativeData.buttonText,
        modifier = modifier,
        style = style,
        onJoinClick = onJoinClick,
        onLongClick = onLongClick
    )
}

/**
 * Overload for displaying collaborative bubble with direct parameters.
 *
 * @param title The document title
 * @param subtitle The document subtitle/description
 * @param type The collaborative type (document or whiteboard)
 * @param url The document URL
 * @param buttonText The text for the action button (default: "Join")
 * @param modifier Modifier for the bubble container
 * @param style Style configuration for the bubble appearance (extends CometChatMessageBubbleStyle)
 * @param onJoinClick Callback when the "Join" button is clicked with the document URL
 */
@Composable
fun CometChatCollaborativeBubble(
    title: String,
    subtitle: String,
    type: CollaborativeType,
    url: String,
    buttonText: String = "Join",
    modifier: Modifier = Modifier,
    style: CometChatCollaborativeBubbleStyle = CometChatCollaborativeBubbleStyle.default(),
    onJoinClick: ((String) -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    CollaborativeBubbleContent(
        title = title,
        subtitle = subtitle,
        type = type,
        url = url,
        buttonText = buttonText,
        modifier = modifier,
        style = style,
        onJoinClick = onJoinClick,
        onLongClick = onLongClick
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CollaborativeBubbleContent(
    title: String,
    subtitle: String,
    type: CollaborativeType,
    url: String,
    buttonText: String,
    modifier: Modifier,
    style: CometChatCollaborativeBubbleStyle,
    onJoinClick: ((String) -> Unit)?,
    onLongClick: (() -> Unit)? = null
) {
    val context = LocalContext.current

    // Default click handler that opens URL in browser
    val effectiveOnJoinClick: (String) -> Unit = onJoinClick ?: { clickedUrl ->
        if (clickedUrl.isNotEmpty()) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(clickedUrl))
                context.startActivity(intent)
            } catch (e: Exception) {
                // Log error but don't crash - URL might be invalid
            }
        }
    }

    val shape = RoundedCornerShape(style.cornerRadius)
    val imageShape = RoundedCornerShape(style.imageCornerRadius)

    Column(
        modifier = modifier
            .width(240.dp)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
                onLongClick = onLongClick
            )
            .semantics {
                contentDescription = "Collaborative ${type.name.lowercase()}: $title"
            }
    ) {
        // Banner image at top (matching Kotlin layout - 136dp height)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, top = 4.dp, end = 4.dp)  // cometchat_padding_1 = 4dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(136.dp)
                    .clip(imageShape)
                    .then(
                        if (style.imageStrokeWidth > 0.dp) {
                            Modifier.border(
                                width = style.imageStrokeWidth,
                                color = style.imageStrokeColor,
                                shape = imageShape
                            )
                        } else {
                            Modifier
                        }
                    )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.cometchat_collaborative_document_img),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Content row with icon and text
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Collaborative icon
            Icon(
                painter = painterResource(
                    id = when (type) {
                        CollaborativeType.DOCUMENT -> R.drawable.cometchat_ic_collaborative_document
                        CollaborativeType.WHITEBOARD -> R.drawable.cometchat_ic_collaborative
                    }
                ),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = style.iconTint
            )

            Spacer(modifier = Modifier.width(4.dp))  // cometchat_margin_1 = 4dp

            // Title and subtitle
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = style.titleTextStyle,
                    color = style.titleTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))  // cometchat_margin = 2dp
                    Text(
                        text = subtitle,
                        style = style.subtitleTextStyle,
                        color = style.subtitleTextColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Separator
        HorizontalDivider(
            color = style.separatorColor,
            thickness = 1.dp
        )

        // Join button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { effectiveOnJoinClick(url) }
                .padding(horizontal = 20.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = buttonText,
                style = style.buttonTextStyle,
                color = style.buttonTextColor
            )
        }
    }
}

/**
 * Data class for collaborative document data.
 */
private data class CollaborativeData(
    val title: String,
    val subtitle: String,
    val buttonText: String,
    val type: CollaborativeType,
    val url: String
)

/**
 * Extracts collaborative data from a CustomMessage and applies localized defaults.
 *
 * This composable function extracts data from the message and replaces any
 * hardcoded English defaults with localized string resources.
 *
 * @param message The CustomMessage to extract data from
 * @return CollaborativeData with localized default values
 */
@Composable
private fun extractCollaborativeDataWithLocalizedDefaults(message: CustomMessage): CollaborativeData {
    // First extract raw data from the message
    val rawData = remember(message.id) {
        extractCollaborativeData(message)
    }

    // Get localized defaults based on type
    val localizedTitle: String
    val localizedSubtitle: String
    val localizedButtonText: String

    when (rawData.type) {
        CollaborativeType.DOCUMENT -> {
            localizedTitle = stringResource(R.string.cometchat_collaborative_doc)
            localizedSubtitle = stringResource(R.string.cometchat_open_document_to_edit_content_together)
            localizedButtonText = stringResource(R.string.cometchat_open_document)
        }
        CollaborativeType.WHITEBOARD -> {
            localizedTitle = stringResource(R.string.cometchat_collaborative_whiteboard)
            localizedSubtitle = stringResource(R.string.cometchat_open_whiteboard_to_edit_content_together)
            localizedButtonText = stringResource(R.string.cometchat_open_whiteboard)
        }
    }

    // Use extracted values if they differ from hardcoded defaults, otherwise use localized
    // The extraction function returns hardcoded English defaults, so we check against those
    val finalTitle = if (rawData.title == "Collaborative Document" || rawData.title == "Collaborative Whiteboard") {
        localizedTitle
    } else {
        rawData.title
    }

    val finalSubtitle = if (rawData.subtitle == "Open document to edit content together" || rawData.subtitle == "Open whiteboard to edit content together") {
        localizedSubtitle
    } else {
        rawData.subtitle
    }

    val finalButtonText = if (rawData.buttonText == "Open Document" || rawData.buttonText == "Open Whiteboard") {
        localizedButtonText
    } else {
        rawData.buttonText
    }

    return CollaborativeData(
        title = finalTitle,
        subtitle = finalSubtitle,
        buttonText = finalButtonText,
        type = rawData.type,
        url = rawData.url
    )
}

/**
 * Extracts collaborative data from a CustomMessage.
 *
 * Checks both the message metadata (for extension data) and customData
 * for collaborative information.
 */
private fun extractCollaborativeData(message: CustomMessage): CollaborativeData {
    var url = ""
    var type = CollaborativeType.DOCUMENT
    var title = "Collaborative Document"
    var subtitle = "Open document to edit content together"
    var buttonText = "Open Document"

    try {
        // First, try to extract URL from metadata extensions
        val metadata = message.metadata
        if (metadata != null) {
            val extensionData = extractExtensionData(metadata)
            if (extensionData != null) {
                url = extensionData.first
                type = extensionData.second
            }
        }

        // Extract additional data from customData
        val customData = message.customData
        if (customData != null) {
            // Title
            if (customData.has("title")) {
                title = customData.optString("title", title)
            }
            // Subtitle
            if (customData.has("subtitle")) {
                subtitle = customData.optString("subtitle", subtitle)
            }
            // Button text
            if (customData.has("button_text")) {
                buttonText = customData.optString("button_text", buttonText)
            }
            // URL fallback from customData
            if (url.isEmpty()) {
                url = customData.optString("url",
                    customData.optString("board_url",
                        customData.optString("document_url", "")))
            }
            // Type from customData if not determined from metadata
            if (customData.has("type")) {
                val typeStr = customData.optString("type", "")
                if (typeStr.contains("whiteboard", ignoreCase = true)) {
                    type = CollaborativeType.WHITEBOARD
                }
            }
        }

        // Determine type from message type if still document
        val messageType = message.type
        if (messageType != null && messageType.contains("whiteboard", ignoreCase = true)) {
            type = CollaborativeType.WHITEBOARD
        }

        // Update title/subtitle/button based on type if using defaults
        if (type == CollaborativeType.WHITEBOARD) {
            if (title == "Collaborative Document") {
                title = "Collaborative Whiteboard"
            }
            if (subtitle == "Open document to edit content together") {
                subtitle = "Open whiteboard to edit content together"
            }
            if (buttonText == "Open Document") {
                buttonText = "Open Whiteboard"
            }
        }

    } catch (e: Exception) {
        // Return defaults on error
    }

    return CollaborativeData(
        title = title,
        subtitle = subtitle,
        buttonText = buttonText,
        type = type,
        url = url
    )
}

/**
 * Extracts extension data from message metadata.
 *
 * Looks for whiteboard or document extension data in the structure:
 * `metadata.@injected.extensions.{whiteboard|document}`
 *
 * @return Pair of (url, type) or null if not found
 */
private fun extractExtensionData(metadata: org.json.JSONObject): Pair<String, CollaborativeType>? {
    try {
        if (!metadata.has("@injected")) return null

        val injectedObject = metadata.getJSONObject("@injected")
        if (!injectedObject.has("extensions")) return null

        val extensionsObject = injectedObject.getJSONObject("extensions")

        // Check for whiteboard extension
        if (extensionsObject.has("whiteboard")) {
            val whiteboardData = extensionsObject.getJSONObject("whiteboard")
            if (whiteboardData.has("board_url")) {
                val boardUrl = whiteboardData.getString("board_url")
                return Pair(boardUrl, CollaborativeType.WHITEBOARD)
            }
        }

        // Check for document extension
        if (extensionsObject.has("document")) {
            val documentData = extensionsObject.getJSONObject("document")
            if (documentData.has("document_url")) {
                val documentUrl = documentData.getString("document_url")
                return Pair(documentUrl, CollaborativeType.DOCUMENT)
            }
        }
    } catch (e: Exception) {
        // Return null on error
    }
    return null
}
