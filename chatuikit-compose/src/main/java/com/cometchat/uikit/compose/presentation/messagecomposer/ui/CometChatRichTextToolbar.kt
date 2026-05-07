package com.cometchat.uikit.compose.presentation.messagecomposer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
 * @param onCloseClick Optional callback when the close button is clicked. When provided, a close (X) button is rendered at the start of the toolbar.
 */
@Composable
fun CometChatRichTextToolbar(
    modifier: Modifier = Modifier,
    style: CometChatMessageComposerStyle = CometChatMessageComposerStyle.default(),
    activeFormats: Set<RichTextFormat> = emptySet(),
    disabledFormats: Set<RichTextFormat> = emptySet(),
    enabledFormats: Set<RichTextFormat> = RichTextFormat.entries.toSet(),
    onFormatClick: (RichTextFormat) -> Unit = {},
    onLinkClick: () -> Unit = {},
    onCloseClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .background(style.richTextToolbarBackgroundColor)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .semantics { contentDescription = "Rich Text Toolbar" },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Close button (only shown when onCloseClick is provided — used in multiline mode)
        if (onCloseClick != null) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .focusable(false)
                    .clickable(
                        onClick = onCloseClick,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = style.formattingToolbarCloseIcon
                        ?: painterResource(R.drawable.cometchat_ic_close),
                    contentDescription = "Close formatting toolbar",
                    tint = style.formattingToolbarCloseIconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Bold
        if (RichTextFormat.BOLD in enabledFormats) {
            FormatButton(
                icon = painterResource(R.drawable.cometchat_ic_format_bold),
                contentDescription = "Bold",
                isActive = RichTextFormat.BOLD in activeFormats,
                isDisabled = RichTextFormat.BOLD in disabledFormats,
                activeTint = style.richTextToolbarActiveIconTint,
                inactiveTint = style.richTextToolbarIconTint,
                disabledTint = style.richTextToolbarIconTint.copy(alpha = 0.4f),
                activeBackgroundColor = style.richTextToolbarActiveIconBackgroundColor,
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
                disabledTint = style.richTextToolbarIconTint.copy(alpha = 0.4f),
                activeBackgroundColor = style.richTextToolbarActiveIconBackgroundColor,
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
                disabledTint = style.richTextToolbarIconTint.copy(alpha = 0.4f),
                activeBackgroundColor = style.richTextToolbarActiveIconBackgroundColor,
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
                disabledTint = style.richTextToolbarIconTint.copy(alpha = 0.4f),
                activeBackgroundColor = style.richTextToolbarActiveIconBackgroundColor,
                onClick = { onFormatClick(RichTextFormat.STRIKETHROUGH) }
            )
        }

        // Separator 1: between text formatting group and link/list group
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(24.dp)
                .background(style.composeBoxStrokeColor)
        )
        Spacer(modifier = Modifier.width(12.dp))

        // Link
        if (RichTextFormat.LINK in enabledFormats) {
            FormatButton(
                icon = painterResource(R.drawable.cometchat_ic_format_link),
                contentDescription = "Link",
                isActive = RichTextFormat.LINK in activeFormats,
                isDisabled = RichTextFormat.LINK in disabledFormats,
                activeTint = style.richTextToolbarActiveIconTint,
                inactiveTint = style.richTextToolbarIconTint,
                disabledTint = style.richTextToolbarIconTint.copy(alpha = 0.4f),
                activeBackgroundColor = style.richTextToolbarActiveIconBackgroundColor,
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
                disabledTint = style.richTextToolbarIconTint.copy(alpha = 0.4f),
                activeBackgroundColor = style.richTextToolbarActiveIconBackgroundColor,
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
                disabledTint = style.richTextToolbarIconTint.copy(alpha = 0.4f),
                activeBackgroundColor = style.richTextToolbarActiveIconBackgroundColor,
                onClick = { onFormatClick(RichTextFormat.BULLET_LIST) }
            )
        }

        // Separator 2: between link/list group and quote/code group
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(24.dp)
                .background(style.composeBoxStrokeColor)
        )
        Spacer(modifier = Modifier.width(12.dp))

        // Blockquote
        if (RichTextFormat.BLOCKQUOTE in enabledFormats) {
            FormatButton(
                icon = painterResource(R.drawable.cometchat_ic_format_quote),
                contentDescription = "Blockquote",
                isActive = RichTextFormat.BLOCKQUOTE in activeFormats,
                isDisabled = RichTextFormat.BLOCKQUOTE in disabledFormats,
                activeTint = style.richTextToolbarActiveIconTint,
                inactiveTint = style.richTextToolbarIconTint,
                disabledTint = style.richTextToolbarIconTint.copy(alpha = 0.4f),
                activeBackgroundColor = style.richTextToolbarActiveIconBackgroundColor,
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
                disabledTint = style.richTextToolbarIconTint.copy(alpha = 0.4f),
                activeBackgroundColor = style.richTextToolbarActiveIconBackgroundColor,
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
                disabledTint = style.richTextToolbarIconTint.copy(alpha = 0.4f),
                activeBackgroundColor = style.richTextToolbarActiveIconBackgroundColor,
                onClick = { onFormatClick(RichTextFormat.CODE_BLOCK) }
            )
        }
    }
}

/**
 * Format button: 40dp touch target, 24dp icon, no ripple.
 * Active state: dark icon tint with a subtle rounded-rect background.
 * Disabled state: grayed out (0.4f alpha) and non-clickable.
 */
@Composable
private fun FormatButton(
    icon: Painter,
    contentDescription: String,
    isActive: Boolean,
    isDisabled: Boolean = false,
    activeTint: Color,
    inactiveTint: Color,
    disabledTint: Color = inactiveTint.copy(alpha = 0.4f),
    activeBackgroundColor: Color = Color.Transparent,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .focusable(false)
            .clickable(
                enabled = !isDisabled,
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .then(
                    if (isActive) Modifier.background(activeBackgroundColor, RoundedCornerShape(8.dp))
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = icon,
                contentDescription = contentDescription,
                tint = when {
                    isDisabled -> disabledTint
                    isActive -> activeTint
                    else -> inactiveTint
                },
                modifier = Modifier
                    .size(24.dp)
                    .alpha(if (isDisabled) 0.4f else 1f)
            )
        }
    }
}
