package com.cometchat.uikit.compose.presentation.search.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.presentation.search.style.CometChatSearchStyle

private const val DEFAULT_PLACEHOLDER = "Search"

/**
 * A composable search bar component with back, clear, and search icons.
 */
@Composable
fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    style: CometChatSearchStyle = CometChatSearchStyle.default(),
    placeholder: String = DEFAULT_PLACEHOLDER,
    onBackPress: (() -> Unit)? = null,
    onSearch: ((String) -> Unit)? = null,
    onClear: (() -> Unit)? = null
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        style.backIcon?.let { icon ->
            IconButton(
                onClick = { onBackPress?.invoke() },
                modifier = Modifier
                    .size(24.dp)
                    .semantics { contentDescription = "Back" }
            ) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = style.backIconTint
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        SearchInputField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            style = style,
            onSearch = {
                keyboardController?.hide()
                onSearch?.invoke(value)
            },
            onClear = {
                onValueChange("")
                onClear?.invoke()
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SearchInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    style: CometChatSearchStyle,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(style.searchBarCornerRadius)

    Row(
        modifier = modifier
            .clip(shape)
            .background(style.searchBarBackgroundColor, shape)
            .then(
                if (style.searchBarStrokeWidth > 0.dp) {
                    Modifier.border(
                        width = style.searchBarStrokeWidth,
                        color = style.searchBarStrokeColor,
                        shape = shape
                    )
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        style.searchIcon?.let { icon ->
            Icon(
                painter = icon,
                contentDescription = null,
                tint = style.searchIconTint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = style.searchBarHintTextStyle,
                    color = style.searchBarHintTextColor
                )
            }

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = style.searchBarTextStyle.copy(color = style.searchBarTextColor),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { onSearch() }
                ),
                cursorBrush = SolidColor(style.searchBarTextColor)
            )
        }

        if (value.isNotEmpty()) {
            Spacer(modifier = Modifier.width(8.dp))
            style.clearIcon?.let { icon ->
                IconButton(
                    onClick = onClear,
                    modifier = Modifier
                        .size(20.dp)
                        .semantics { contentDescription = "Clear search" }
                ) {
                    Icon(
                        painter = icon,
                        contentDescription = null,
                        tint = style.clearIconTint
                    )
                }
            }
        }
    }
}
