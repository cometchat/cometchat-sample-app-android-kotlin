package com.cometchat.uikit.core.models

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Attachment

/**
 * Derives an attachment's default category (`image` / `video` / `audio` / `file`) from its MIME
 * type. Used when the picker doesn't dictate one — pickers override this: anything chosen through
 * the **file** picker is `file` regardless of MIME, gallery/camera picks are `image`/`video`, and
 * the audio picker yields `audio`.
 */
fun defaultAttachmentCategory(mimeType: String): String {
    val mime = mimeType.lowercase()
    return when {
        mime.startsWith("image/") -> CometChatConstants.MESSAGE_TYPE_IMAGE
        mime.startsWith("video/") -> CometChatConstants.MESSAGE_TYPE_VIDEO
        mime.startsWith("audio/") -> CometChatConstants.MESSAGE_TYPE_AUDIO
        else -> CometChatConstants.MESSAGE_TYPE_FILE
    }
}

/**
 * Lifecycle status of a single staged attachment in the composer tray.
 *
 * Mapped directly from the SDK's `UploadFileListener` callbacks so the tray never has to
 * inspect error codes to pick a recovery affordance:
 * - [UPLOADING] in flight (`onProgress`) — show a cancel (✕) affordance.
 * - [DONE]      upload finished (`onFileUploaded`) — eligible to send; show Remove.
 * - [FAILED]    transfer failed (`onFileFailure`) — retryable; show Retry.
 * - [REJECTED]  not retryable (`onFileError`, e.g. size/MIME reject) — show Remove only.
 * - [CANCELLED] cancelled by the user.
 */
enum class AttachmentUploadStatus { UPLOADING, DONE, FAILED, REJECTED, CANCELLED }

/**
 * Where a staged attachment originated. This decides how an `audio/…` file is treated at send
 * time: audio chosen through the **picker** is sent as a `file` (renders in the files section),
 * while audio captured by the **recorder** is sent as media type `audio` (inline player).
 */
enum class AttachmentSource { PICKER, RECORDER }

/**
 * One staged item in the composer attachment tray, keyed by the SDK-assigned [fileId].
 *
 * Rendered once per [fileId] by `CometChatAttachmentTile`. Every per-file `UploadFileListener`
 * event updates the tile keyed by its [fileId]; there is no cross-tile leakage. The tray holds an
 * ordered collection of these (insertion order) on the shared composer `ViewModel`, consumed by
 * both the Views and Compose UIKits.
 *
 * @property fileId    App-minted id (`batchId_seq`); the tile key, supplied to the SDK at upload.
 * @property name      Display file name.
 * @property size      Declared file size in bytes.
 * @property mimeType  MIME type of the file.
 * @property category  UI/send category (`image` / `video` / `audio` / `file`), decided by the
 *                     **picker** the file came through, not its MIME: a photo chosen via the file
 *                     picker stays `file` (document tile, sent as a file message), while the same
 *                     photo from the gallery/camera is `image` (media tile, image message).
 * @property percent   Upload progress, 0..100.
 * @property loaded    Bytes uploaded so far (drives the byte-weighted aggregate bar).
 * @property total     Total bytes to upload (defaults to [size]).
 * @property status    Current [AttachmentUploadStatus].
 * @property source    Origin of the file ([AttachmentSource]); decides audio handling.
 * @property localUri  Local preview uri/path shown once [status] is [AttachmentUploadStatus.DONE]
 *                     (swapped for the attachment url when available).
 * @property durationMillis Media duration in milliseconds for video/audio, if known — used for the
 *                     duration label on the tile.
 * @property attachment The uploaded [Attachment], set on `onFileUploaded` — what gets sent.
 * @property error     The exception held on a `FAILED`/`REJECTED` tile, for an actionable reason.
 */
data class AttachmentUploadTile(
    val fileId: String,
    val name: String,
    val size: Long,
    val mimeType: String,
    val category: String = defaultAttachmentCategory(mimeType),
    val percent: Int = 0,
    val loaded: Long = 0L,
    val total: Long = size,
    val status: AttachmentUploadStatus = AttachmentUploadStatus.UPLOADING,
    val source: AttachmentSource = AttachmentSource.PICKER,
    val localUri: String? = null,
    val durationMillis: Long? = null,
    val attachment: Attachment? = null,
    val error: CometChatException? = null
)

/**
 * A file the user picked (or captured) that is about to be staged in the composer tray and
 * uploaded. This is the UI-agnostic hand-off from a picker into
 * `CometChatMessageComposerViewModel.stageAttachments`: the ViewModel mints each file's `fileId`, starts the SDK
 * upload, and creates the per-file [AttachmentUploadTile]s (keyed by those ids), and tracks their lifecycle.
 *
 * @property file     The on-disk file to upload (already copied out of the picker's content uri).
 * @property name     Display file name.
 * @property size     File size in bytes.
 * @property mimeType MIME type of the file.
 * @property category UI/send category (`image` / `video` / `audio` / `file`), decided by the
 *                    picker the file came through — see [AttachmentUploadTile.category].
 * @property source   Origin of the file; [AttachmentSource.PICKER] for picked files (the only
 *                    source staged in the tray — recorder voice notes are sent standalone).
 * @property localUri Local preview uri/path; defaults to [file]'s path when not supplied.
 * @property durationMillis Media duration in ms for video/audio, if known.
 */
data class StagedAttachmentInput(
    val file: java.io.File,
    val name: String,
    val size: Long,
    val mimeType: String,
    val category: String = defaultAttachmentCategory(mimeType),
    val source: AttachmentSource = AttachmentSource.PICKER,
    val localUri: String? = null,
    val durationMillis: Long? = null
)
