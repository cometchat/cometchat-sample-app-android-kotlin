package com.cometchat.uikit.compose.presentation.messagecomposer.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.messagecomposer.style.CometChatMessageComposerStyle
import com.cometchat.uikit.core.formatter.RichTextFormat

/**
 * Horizontally scrollable rich text formatting toolbar.
 * Button order matches Flutter: Bold, Italic, Underline, Strikethrough,
 * Link, Ordered List, Bullet List, Blockquote, Inline Code, Code Block.
 *
 * @param modifier Modifier for the toolbar
 * @param style Style configuration
 * @param activeFormats Currently active formats (highlighted)
 * @param disabledFormats Formats that are incompatible with current active formats (grayed out)
 * @param enabledFormats Formats to show buttons for
 * @param onFormatClick Callback when a format button is clicked
 * @param onLinkClick Callback when the link button is clicked
 */
@Composable
fun CometChatRichTextToolbar(
    modifier: Modifier = Modifier,
    style: CometChatMessageComposerStyle = CometChatMessageComposerStyle.default(),
    activeFormats: Set<RichTextFormat> = emptySet(),
    disabledFormats: Set<RichTextFormat> = emptySet(),
    enabledFormats: Set<RichTextFormat> = RichTextFormat.entries.toSet(),
    onFormatClick: (RichTextFormat) -> Unit = {},
    onLinkClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .semantics { contentDescription = "Rich Text Toolbar" },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Bold
        if (RichTextFormat.BOLD in enabledFormats) {
            FormatButton(
                icon = painterResource(R.drawable.cometchat_ic_format_bold),
                contentDescription = "Bold",
                isActive = RichTextFormat.BOLD in activeFormats,
                isDisabled = RichTextFormat.BOLD in disabledFormats,
                activeTint = style.richTextToolbarActiveIconTint,
                inactiveTint = style.richTextToolbarIconTint,
                disabledTint = style.richTextToolbarIconTint.copy(alpha = 0.3f),
                onClick = { onFormatClick(RichTextFormat.BOLD) }
            )
        }

        // Italic
        if (RichTextFormat.ITALIC in enabledFormats) {
            FormatButton(
                icon = painterResource(R.drawable.cometchat_ic_format_italic),
                contentDescription = "Italic",
                isActive = RichTextFormat.ITALIC in activeFormats,
                isDisabled = RichTextFormat.ITALIC in disabledFormats,
                activeTint = style.richTextToolbarActiveIconTint,
                inactiveTint = style.richTextToolbarIconTint,
                disabledTint = style.richTextToolbarIconTint.copy(alpha = 0.3f),
                onClick = { onFormatClick(RichTextFormat.ITALIC) }
            )
        }

        // Underline
        if (RichTextFormat.UNDERLINE in enabledFormats) {
            FormatButton(
                icon = painterResource(R.drawable.cometchat_ic_format_underline),
                contentDescription = "Underline",
                isActive = RichTextFormat.UNDERLINE in activeFormats,
                isDisabled = RichTextFormat.UNDERLINE in disabledFormats,
                activeTint = style.richTextToolbarActiveIconTint,
                inactiveTint = style.richTextToolbarIconTint,
                disabledTint = style.richTextToolbarIconTint.copy(alpha = 0.3f),
                onClick = { onFormatClick(RichTextFormat.UNDERLINE) }
            )
        }

        // Strikethrough
        if (RichTextFormat.STRIKETHROUGH in enabledFormats) {
            FormatButton(
                icon = painterResource(R.drawable.cometchat_ic_format_strikethrough),
                contentDescription = "Strikethrough",
                isActive = RichTextFormat.STRIKETHROUGH in activeFormats,
                isDisabled = RichTextFormat.STRIKETHROUGH in disabledFormats,
                activeTint = style.richTextToolbarActiveIconTint,
                inactiveTint = style.richTextToolbarIconTint,
                disabledTint = style.richTextToolbarIconTint.copy(alpha = 0.3f),
                onClick = { onFormatClick(RichTextFormat.STRIKETHROUGH) }
            )
        }

        // Link
        if (RichTextFormat.LINK in enabledFormats) {
            FormatButton(
                icon = painterResource(R.drawable.cometchat_ic_link_outlined),
                contentDescription = "Link",
                isActive = RichTextFormat.LINK in activeFormats,
                isDisabled = RichTextFormat.LINK in disabledFormats,
                activeTint = style.richTextToolbarActiveIconTint,
                inactiveTint = style.richTextToolbarIconTint,
                disabledTint = style.richTextToolbarIconTint.copy(alpha = 0.3f),
                onClick = onLinkClick
            )
        }

        // Ordered List
        if (RichTextFormat.ORDERED_LIST in enabledFormats) {
            FormatButton(
                icon = painterResource(R.drawable.cometchat_ic_format_list_numbered),
                contentDescription = "Numbered List",
                isActive = RichTextFormat.ORDERED_LIST in activeFormats,
                isDisabled = RichTextFormat.ORDERED_LIST in disabledFormats,
                activeTint = style.richTextToolbarActiveIconTint,
                inactiveTint = style.richTextToolbarIconTint,
                disabledTint = style.richTextToolbarIconTint.copy(alpha = 0.3f),
                onClick = { onFormatClick(RichTextFormat.ORDERED_LIST) }
            )
        }

        // Bullet List
        if (RichTextFormat.BULLET_LIST in enabledFormats) {
            FormatButton(
                icon = painterResource(R.drawable.cometchat_ic_format_list_bullet),
                contentDescription = "Bullet List",
                isActive = RichTextFormat.BULLET_LIST in activeFormats,
                isDisabled = RichTextFormat.BULLET_LIST in disabledFormats,
                activeTint = style.richTextToolbarActiveIconTint,
                inactiveTint = style.richTextToolbarIconTint,
                disabledTint = style.richTextToolbarIconTint.copy(alpha = 0.3f),
                onClick = { onFormatClick(RichTextFormat.BULLET_LIST) }
            )
        }

        // Blockquote
        if (RichTextFormat.BLOCKQUOTE in enabledFormats) {
            FormatButton(
                icon = painterResource(R.drawable.cometchat_ic_format_quote),
                contentDescription = "Blockquote",
                isActive = RichTextFormat.BLOCKQUOTE in activeFormats,
                isDisabled = RichTextFormat.BLOCKQUOTE in disabledFormats,
                activeTint = style.richTextToolbarActiveIconTint,
                inactiveTint = style.richTextToolbarIconTint,
                disabledTint = style.richTextToolbarIconTint.copy(alpha = 0.3f),
                onClick = { onFormatClick(RichTextFormat.BLOCKQUOTE) }
            )
        }

        // Inline Code
        if (RichTextFormat.INLINE_CODE in enabledFormats) {
            FormatButton(
                icon = painterResource(R.drawable.cometchat_ic_format_code),
                contentDescription = "Inline Code",
                isActive = RichTextFormat.INLINE_CODE in activeFormats,
                isDisabled = RichTextFormat.INLINE_CODE in disabledFormats,
                activeTint = style.richTextToolbarActiveIconTint,
                inactiveTint = style.richTextToolbarIconTint,
                disabledTint = style.richTextToolbarIconTint.copy(alpha = 0.3f),
                onClick = { onFormatClick(RichTextFormat.INLINE_CODE) }
            )
        }

        // Code Block
        if (RichTextFormat.CODE_BLOCK in enabledFormats) {
            FormatButton(
                icon = painterResource(R.drawable.cometchat_ic_format_code_block),
                contentDescription = "Code Block",
                isActive = RichTextFormat.CODE_BLOCK in activeFormats,
                isDisabled = RichTextFormat.CODE_BLOCK in disabledFormats,
                activeTint = style.richTextToolbarActiveIconTint,
                inactiveTint = style.richTextToolbarIconTint,
                disabledTint = style.richTextToolbarIconTint.copy(alpha = 0.3f),
                onClick = { onFormatClick(RichTextFormat.CODE_BLOCK) }
            )
        }
    }
}

/**
 * Format button: 36dp touch target, 20dp icon.
 * Disabled buttons are grayed out and non-clickable.
 */
@Composable
private fun FormatButton(
    icon: Painter,
    contentDescription: String,
    isActive: Boolean,
    isDisabled: Boolean = false,
    activeTint: Color,
    inactiveTint: Color,
    disabledTint: Color = inactiveTint.copy(alpha = 0.3f),
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(36.dp),
        enabled = !isDisabled
    ) {
        Icon(
            painter = icon,
            contentDescription = contentDescription,
            tint = when {
                isDisabled -> disabledTint
                isActive -> activeTint
                else -> inactiveTint
            },
            modifier = Modifier.size(20.dp)
        )
    }
}
