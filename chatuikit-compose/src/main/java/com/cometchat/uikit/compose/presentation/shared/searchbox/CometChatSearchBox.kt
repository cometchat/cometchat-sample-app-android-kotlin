package com.cometchat.uikit.compose.presentation.shared.searchbox

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

/**
 * CometChatSearchBox displays a search input field with customizable icons and styling.
 *
 * This is a standalone reusable component that can be used across the UIKit.
 * Based on the existing View-based CometChatSearchBox.java implementation.
 *
 * @param modifier Modifier for the search box container
 * @param style Style configuration for the search box
 * @param text Current text value
 * @param onTextChange Callback when text changes
 * @param placeholderText Placeholder text when empty
 * @param enabled Whether the search box is enabled for input
 * @param onClick Callback when search box is clicked (for navigation mode)
 * @param onSearch Callback when search action is triggered (IME_ACTION_SEARCH)
 * @param onClear Callback when clear button is clicked
 */
@Composable
fun CometChatSearchBox(
    modifier: Modifier = Modifier,
    style: CometChatSearchBoxStyle = CometChatSearchBoxStyle.default(),
    text: String = "",
    onTextChange: ((String) -> Unit)? = null,
    placeholderText: String = "Search",
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    onSearch: ((String) -> Unit)? = null,
    onClear: (() -> Unit)? = null
) {
    var internalText by remember { mutableStateOf(text) }
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }
    
    val shape = RoundedCornerShape(style.cornerRadius)
    
    val containerModifier = modifier
        .fillMaxWidth()
        .clip(shape)
        .background(style.backgroundColor, shape)
        .then(
            if (style.strokeWidth > 0.dp) {
                Modifier.border(style.strokeWidth, style.strokeColor, shape)
            } else {
                Modifier
            }
        )
        .then(
            if (!enabled && onClick != null) {
                Modifier
                    .focusable()
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                    .semantics { 
                        contentDescription = "Search conversations, tap to open search"
                        role = Role.Button
                    }
            } else {
                Modifier.semantics { 
                    contentDescription = "Search conversations" 
                }
            }
        )
        .padding(style.contentPadding)
    
    Row(
        modifier = containerModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Start icon (search icon)
        style.startIcon?.let { icon ->
            Icon(
                painter = icon,
                contentDescription = "Search",
                tint = style.startIconTint,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .size(24.dp)
            )
        }
        
        // Text field
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            if (enabled) {
                BasicTextField(
                    value = internalText,
                    onValueChange = { newText ->
                        internalText = newText
                        onTextChange?.invoke(newText)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    textStyle = style.textStyle.copy(color = style.textColor),
                    singleLine = true,
                    cursorBrush = SolidColor(style.textColor),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            onSearch?.invoke(internalText)
                        }
                    ),
                    decorationBox = { innerTextField ->
                        Box {
                            if (internalText.isEmpty()) {
                                Text(
                                    text = placeholderText,
                                    style = style.placeholderTextStyle,
                                    color = style.placeholderColor
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            } else {
                // Non-editable mode - just show placeholder
                Text(
                    text = if (internalText.isEmpty()) placeholderText else internalText,
                    style = if (internalText.isEmpty()) style.placeholderTextStyle else style.textStyle,
                    color = if (internalText.isEmpty()) style.placeholderColor else style.textColor
                )
            }
        }
        
        // End icon (clear icon) - only show when there's text
        if (internalText.isNotEmpty() && style.endIcon != null) {
            Icon(
                painter = style.endIcon,
                contentDescription = "Clear search",
                tint = style.endIconTint,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(24.dp)
                    .focusable()
                    .clickable {
                        internalText = ""
                        onTextChange?.invoke("")
                        onClear?.invoke()
                    }
                    .semantics { role = Role.Button }
            )
        }
    }
}
