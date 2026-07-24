package com.cometchat.uikit.compose.preview.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.presentation.messagecomposer.ui.CometChatAttachmentTray
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.models.AttachmentSource
import com.cometchat.uikit.core.models.AttachmentUploadStatus
import com.cometchat.uikit.core.models.AttachmentUploadTile

/* ------------------------------------------------------------------------------------------------
 * Tile factories
 * ---------------------------------------------------------------------------------------------- */

private fun mediaTile(
    id: String,
    name: String,
    mimeType: String,
    status: AttachmentUploadStatus,
    percent: Int = 100,
    durationMillis: Long? = null
): AttachmentUploadTile = AttachmentUploadTile(
    fileId = id,
    name = name,
    size = 2_400_000L,
    mimeType = mimeType,
    percent = percent,
    loaded = (2_400_000L * percent) / 100,
    total = 2_400_000L,
    status = status,
    source = AttachmentSource.PICKER,
    durationMillis = durationMillis
)

private fun fileTile(
    id: String,
    name: String,
    mimeType: String,
    status: AttachmentUploadStatus
): AttachmentUploadTile = AttachmentUploadTile(
    fileId = id,
    name = name,
    size = 1_200_000L,
    mimeType = mimeType,
    status = status,
    source = AttachmentSource.PICKER
)

/** A representative mix of staged states (media + documents; voice notes are never staged). */
private fun mockTiles(): List<AttachmentUploadTile> = listOf(
    mediaTile("1", "watch-1.jpg", "image/jpeg", AttachmentUploadStatus.DONE),
    mediaTile("2", "watch-2.mp4", "video/mp4", AttachmentUploadStatus.DONE, durationMillis = 32_000L),
    fileTile("3", "Document.pdf", "application/pdf", AttachmentUploadStatus.REJECTED),
    fileTile("4", "Ringtone45821.mp3", "audio/mpeg", AttachmentUploadStatus.UPLOADING),
    fileTile("5", "Archive.zip", "application/zip", AttachmentUploadStatus.FAILED)
)

/* ------------------------------------------------------------------------------------------------
 * Previews
 * ---------------------------------------------------------------------------------------------- */

@Composable
private fun TrayHost(tiles: List<AttachmentUploadTile>) {
    CometChatTheme {
        // Mimic the composer surface above the input row.
        CometChatAttachmentTray(
            tiles = tiles,
            modifier = Modifier
                .fillMaxWidth()
                .background(CometChatTheme.colorScheme.backgroundColor1)
                .padding(bottom = 8.dp)
        )
    }
}

@Preview(name = "Tray — mixed states", showBackground = true, widthDp = 460)
@Composable
private fun PreviewTrayMixed() {
    TrayHost(tiles = mockTiles())
}

@Preview(name = "Tray — media states", showBackground = true, widthDp = 460)
@Composable
private fun PreviewTrayMedia() {
    TrayHost(
        tiles = listOf(
            mediaTile("1", "a.jpg", "image/jpeg", AttachmentUploadStatus.DONE),
            mediaTile("2", "b.jpg", "image/jpeg", AttachmentUploadStatus.UPLOADING, percent = 30),
            mediaTile("3", "c.mp4", "video/mp4", AttachmentUploadStatus.FAILED),
            mediaTile("4", "d.png", "image/png", AttachmentUploadStatus.REJECTED)
        )
    )
}

@Preview(name = "Tray — documents", showBackground = true, widthDp = 460)
@Composable
private fun PreviewTrayDocuments() {
    TrayHost(
        tiles = listOf(
            fileTile("1", "Report.docx", "application/msword", AttachmentUploadStatus.DONE),
            fileTile("2", "Slides.pptx", "application/vnd.ms-powerpoint", AttachmentUploadStatus.UPLOADING),
            fileTile("3", "Sheet.xlsx", "application/vnd.ms-excel", AttachmentUploadStatus.FAILED)
        )
    )
}
