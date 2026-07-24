package com.cometchat.uikit.core.utils

import android.media.MediaMetadataRetriever
import android.util.Log

/**
 * Extracts the playback duration (in milliseconds) of a local video/audio file, used to stamp a
 * duration badge onto video tiles and audio players (ENG-36737). Returns null for non-media MIME
 * types or when the duration can't be read.
 *
 * Reads directly from the file path — no [android.content.Context] needed. Safe to call off the main
 * thread (the composer stages attachments in a background flow before upload).
 */
fun extractMediaDurationMillis(filePath: String?, mimeType: String?): Long? {
    if (filePath.isNullOrEmpty()) return null
    val mime = mimeType?.lowercase().orEmpty()
    if (!mime.startsWith("video/") && !mime.startsWith("audio/")) return null
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(filePath)
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            ?.toLongOrNull()
            ?.takeIf { it > 0 }
    } catch (e: Exception) {
        Log.w("MediaDurationUtils", "Failed to read duration for $filePath: ${e.message}")
        null
    } finally {
        runCatching { retriever.release() }
    }
}
