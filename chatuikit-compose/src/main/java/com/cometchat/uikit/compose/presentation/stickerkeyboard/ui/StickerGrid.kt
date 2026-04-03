package com.cometchat.uikit.compose.presentation.stickerkeyboard.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.domain.model.Sticker

/**
 * A composable that displays a grid of stickers.
 *
 * This component renders stickers in a 4-column grid layout with support for:
 * - GIF animations using Coil's GIF decoder
 * - Loading state with progress indicator
 * - Error state with placeholder
 * - Click handling for sticker selection
 *
 * @param stickers List of stickers to display
 * @param modifier Modifier for the grid container
 * @param stickerItemSize Size of each sticker item
 * @param onStickerClick Callback when a sticker is clicked
 */
@Composable
internal fun StickerGrid(
    stickers: List<Sticker>,
    modifier: Modifier = Modifier,
    stickerItemSize: Dp = 80.dp,
    onStickerClick: ((Sticker) -> Unit)? = null
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = stickers,
            key = { sticker -> "${sticker.setName}_${sticker.name}_${sticker.url}" }
        ) { sticker ->
            StickerGridItem(
                sticker = sticker,
                size = stickerItemSize,
                onClick = { onStickerClick?.invoke(sticker) }
            )
        }
    }
}


/**
 * A single sticker item in the grid.
 *
 * Displays the sticker image with loading and error states.
 * Supports GIF animations for animated stickers.
 *
 * @param sticker The sticker to display
 * @param size Size of the sticker item
 * @param onClick Callback when the sticker is clicked
 */
@Composable
private fun StickerGridItem(
    sticker: Sticker,
    size: Dp,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .size(size)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = sticker.name.ifEmpty { "Sticker" }
                role = Role.Button
            },
        contentAlignment = Alignment.Center
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(sticker.url)
                .crossfade(true)
                .build(),
            contentDescription = sticker.name.ifEmpty { "Sticker" },
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = CometChatTheme.colorScheme.iconTintSecondary,
                        strokeWidth = 2.dp
                    )
                }
            },
            error = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "!",
                        style = CometChatTheme.typography.caption1Regular,
                        color = CometChatTheme.colorScheme.textColorTertiary
                    )
                }
            }
        )
    }
}
