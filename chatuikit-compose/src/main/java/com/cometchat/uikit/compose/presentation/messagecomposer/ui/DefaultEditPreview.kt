package com.cometchat.uikit.compose.presentation.messagecomposer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.messagecomposer.style.CometChatMessageComposerStyle
import com.cometchat.uikit.compose.presentation.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.compose.presentation.shared.messagebubble.ui.buildReplyPreviewAnnotatedString
import com.cometchat.uikit.compose.presentation.shared.messagepreview.mediaPreviewSubtitle
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.formatter.MarkdownRenderer

/**
 * Default edit preview composable that shows the message being edited.
 * Displays a title "Edit" and the original message text with a close button.
 *
 * @param modifier Modifier for the preview
 * @param message The message being edited
 * @param textFormatters List of text formatters to resolve mention tokens
 * @param style Style configuration for the preview
 * @param onClose Callback when the close button is clicked
 */
@Composable
fun DefaultEditPreview(
    modifier: Modifier = Modifier,
    message: BaseMessage,
    textFormatters: List<CometChatTextFormatter> = emptyList(),
    style: CometChatMessageComposerStyle = CometChatMessageComposerStyle.default(),
    onClose: () -> Unit = {}
) {
    val context = LocalContext.current
    
    // Run formatter pipeline to resolve mention tokens (e.g., <@uid:userId> -> @userName),
    // then apply partial formatting (bold/italic/underline/strikethrough only) for edit preview
    val messageText = remember(message, textFormatters) {
        // Text messages preview their text; media messages show the summarized attachment
        // preview: "N Images · caption" / "N Images" / caption / file name — same rules
        // as the quoted message preview.
        if (message is MediaMessage) {
            return@remember mediaPreviewSubtitle(context, message)
        }
        val rawText = (message as? TextMessage)?.text ?: ""
        if (rawText.isEmpty()) {
            AnnotatedString(rawText)
        } else {
            // Step 1: Run formatter pipeline to resolve mention tokens
            val formattedText = if (textFormatters.isEmpty()) {
                rawText
            } else {
                var result: AnnotatedString = AnnotatedString(rawText)
                for (formatter in textFormatters) {
                    result = formatter.prepareMessageString(
                        context,
                        message,
                        result,
                        UIKitConstants.MessageBubbleAlignment.LEFT,
                        UIKitConstants.FormattingType.MESSAGE_COMPOSER
                    )
                }
                result.text
            }
            // Step 2: Parse markdown, keep bold/italic/underline/strikethrough, plain text for code/blockquote
            val segments = MarkdownRenderer.parse(formattedText)
            buildReplyPreviewAnnotatedString(
                segments = segments,
                textColor = style.editPreviewMessageTextColor
            )
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
            .background(
                color = style.editPreviewBackgroundColor,
                shape = RoundedCornerShape(style.editPreviewCornerRadius)
            )
            .then(
                if (style.editPreviewStrokeWidth > 0.dp) {
                    Modifier.border(
                        width = style.editPreviewStrokeWidth,
                        color = style.editPreviewStrokeColor,
                        shape = RoundedCornerShape(style.editPreviewCornerRadius)
                    )
                } else Modifier
            )
            .padding(8.dp)
            .semantics { contentDescription = "Edit Preview" },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Title
            Text(
                text = context.getString(R.string.cometchat_edit),
                color = style.editPreviewTitleTextColor,
                style = style.editPreviewTitleTextStyle
            )

            // Message text as plain text (markdown stripped)
            if (messageText.text.isNotEmpty()) {
                Text(
                    text = messageText,
                    color = style.editPreviewMessageTextColor,
                    style = style.editPreviewMessageTextStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Close button
        IconButton(
            onClick = onClose,
            modifier = Modifier.size(32.dp)
        ) {
            style.editPreviewCloseIcon?.let { icon ->
                Icon(
                    painter = icon,
                    contentDescription = "Close edit preview",
                    tint = style.editPreviewCloseIconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
