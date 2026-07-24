package com.cometchat.uikit.compose.presentation.messagecomposer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.cometchat.uikit.compose.presentation.messagecomposer.style.CometChatAttachmentTileStyle
import com.cometchat.uikit.compose.presentation.messagecomposer.style.CometChatAttachmentTrayStyle
import com.cometchat.uikit.core.models.AttachmentUploadTile

/**
 * The composer staging strip: a horizontally-scrolling row of [CometChatAttachmentTile]s.
 *
 * The tray renders **nothing** when [tiles] is empty — it is only visible while ≥1 attachment is
 * staged, mirroring the design doc. It is fully stateless: the hosting composer owns the tile list
 * (on the shared `ViewModel`) and reacts to the per-tile intents emitted here. Adding more
 * attachments is done from the composer's attachment button, not from within the tray.
 *
 * @param tiles The staged tiles, in insertion order.
 * @param modifier Modifier for the tray container.
 * @param style Tray-level styling (background, spacing).
 * @param tileStyle Per-tile styling, forwarded to each [CometChatAttachmentTile].
 * @param onCancelTile Invoked to cancel an in-flight tile's upload.
 * @param onRemoveTile Invoked to remove an uploaded / failed / rejected tile (remove + cancel).
 * @param onRetryTile Invoked to retry a failed tile.
 * @param onTileClick Invoked when a successfully-uploaded media tile is tapped (open the preview).
 * @param onRejectedTile Invoked when a rejected tile is tapped (surface the SDK rejection reason).
 */
@Composable
fun CometChatAttachmentTray(
    tiles: List<AttachmentUploadTile>,
    modifier: Modifier = Modifier,
    style: CometChatAttachmentTrayStyle = CometChatAttachmentTrayStyle.default(),
    tileStyle: CometChatAttachmentTileStyle = CometChatAttachmentTileStyle.default(),
    onCancelTile: (AttachmentUploadTile) -> Unit = {},
    onRemoveTile: (AttachmentUploadTile) -> Unit = {},
    onRetryTile: (AttachmentUploadTile) -> Unit = {},
    onTileClick: (AttachmentUploadTile) -> Unit = {},
    onRejectedTile: (AttachmentUploadTile) -> Unit = {}
) {
    if (tiles.isEmpty()) return

    // A fixed row height lets the LazyRow fast-return intrinsic-height queries from the composer's
    // layout — lazy lists are SubcomposeLayouts and otherwise throw when asked for intrinsic
    // measurements. Height = tallest tile (media tile + the top corner-badge overhang) plus the
    // row's vertical content padding so nothing is clipped.
    val verticalContentPadding =
        style.contentPadding.calculateTopPadding() + style.contentPadding.calculateBottomPadding()
    val rowHeight = maxOf(tileStyle.mediaTileSize, tileStyle.audioCardHeight) +
        tileStyle.cornerBadgeOverhang + verticalContentPadding

    Column(modifier = modifier.fillMaxWidth().background(style.backgroundColor)) {
        LazyRow(
            modifier = Modifier.fillMaxWidth().height(rowHeight),
            contentPadding = style.contentPadding,
            horizontalArrangement = Arrangement.spacedBy(style.tileSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(
                items = tiles,
                key = { it.fileId },
                // Distinct content types let the lazy layout recycle media / audio / file tiles
                // separately instead of re-measuring across shapes.
                contentType = { it.tileKind() }
            ) { tile ->
                CometChatAttachmentTile(
                    tile = tile,
                    style = tileStyle,
                    onCancel = { onCancelTile(tile) },
                    onRemove = { onRemoveTile(tile) },
                    onRetry = { onRetryTile(tile) },
                    onClick = { onTileClick(tile) },
                    onRejected = { onRejectedTile(tile) }
                )
            }
        }
    }
}
