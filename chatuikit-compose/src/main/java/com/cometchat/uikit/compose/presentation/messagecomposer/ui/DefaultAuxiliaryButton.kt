package com.cometchat.uikit.compose.presentation.messagecomposer.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.presentation.messagecomposer.style.CometChatMessageComposerStyle

/**
 * Default auxiliary button composable for the message composer.
 * Contains buttons in order: Rich Text Toggle, Sticker, AI, Voice Recording
 * (matching chatuikit-kotlin layout order)
 * 
 * Uses 24dp icon size with 16dp gap between icons to match Figma design specifications.
 *
 * @param modifier Modifier for the button row
 * @param hideRichTextToggle Whether to hide the rich text toggle button
 * @param hideStickersButton Whether to hide the stickers/emoji button
 * @param hideAIButton Whether to hide the AI button
 * @param hideVoiceRecordingButton Whether to hide the voice recording button
 * @param isRichTextToolbarExpanded Whether the rich text toolbar is currently expanded
 * @param style Style configuration for the buttons
 * @param onRichTextToggleClick Callback when the rich text toggle button is clicked
 * @param onStickerClick Callback when the sticker button is clicked
 * @param onAIClick Callback when the AI button is clicked
 * @param onVoiceRecordClick Callback when the voice recording button is clicked
 */
@Composable
fun DefaultAuxiliaryButton(
    modifier: Modifier = Modifier,
    hideRichTextToggle: Boolean = true,
    hideStickersButton: Boolean = false,
    hideAIButton: Boolean = true,
    hideVoiceRecordingButton: Boolean = true,
    isRichTextToolbarExpanded: Boolean = false,
    style: CometChatMessageComposerStyle = CometChatMessageComposerStyle.default(),
    onRichTextToggleClick: () -> Unit = {},
    onStickerClick: () -> Unit = {},
    onAIClick: () -> Unit = {},
    onVoiceRecordClick: () -> Unit = {}
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Rich Text Toggle button (first, matching chatuikit-kotlin order)
        if (!hideRichTextToggle) {
            IconButton(
                onClick = onRichTextToggleClick,
                modifier = Modifier
                    .size(24.dp)
                    .semantics { contentDescription = "Format Text" }
            ) {
                style.richTextToggleIcon?.let { icon ->
                    Icon(
                        painter = icon,
                        contentDescription = if (isRichTextToolbarExpanded) "Hide formatting options" else "Show formatting options",
                        tint = if (isRichTextToolbarExpanded) style.richTextToggleIconActiveTint else style.richTextToggleIconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
        }

        // 2. Sticker/Emoji button
        if (!hideStickersButton) {
            IconButton(
                onClick = onStickerClick,
                modifier = Modifier
                    .size(24.dp)
                    .semantics { contentDescription = "Stickers" }
            ) {
                style.stickerIcon?.let { icon ->
                    Icon(
                        painter = icon,
                        contentDescription = "Open stickers",
                        tint = style.stickerIconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
        }

        // 3. AI button
        if (!hideAIButton) {
            IconButton(
                onClick = onAIClick,
                modifier = Modifier
                    .size(24.dp)
                    .semantics { contentDescription = "AI Assistant" }
            ) {
                style.aiIcon?.let { icon ->
                    Icon(
                        painter = icon,
                        contentDescription = "Open AI options",
                        tint = style.aiIconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
        }

        // 4. Voice Recording button (last, matching chatuikit-kotlin order)
        if (!hideVoiceRecordingButton) {
            IconButton(
                onClick = onVoiceRecordClick,
                modifier = Modifier
                    .size(24.dp)
                    .semantics { contentDescription = "Voice Recording" }
            ) {
                style.voiceRecordingIcon?.let { icon ->
                    Icon(
                        painter = icon,
                        contentDescription = "Record voice message",
                        tint = style.voiceRecordingIconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
