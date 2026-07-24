package com.cometchat.uikit.compose.presentation.shared.messagebubble.utils

import com.cometchat.uikit.compose.R
import com.cometchat.uikit.core.constants.UIKitConstants

/**
 * Enum representing different file types for display in file bubbles.
 * Each type maps to a specific icon resource.
 */
enum class FileType {
    PDF,
    DOC,
    XLS,
    PPT,
    ZIP,
    AUDIO,
    VIDEO,
    IMAGE,
    TEXT,
    LINK,
    UNKNOWN
}

/**
 * Determines the file type based on MIME type and file URL.
 *
 * @param mimeType The MIME type of the file (e.g., "application/pdf")
 * @param fileUrl The URL or path of the file (used for extension-based detection)
 * @return The corresponding [FileType] enum value
 */
fun getFileType(mimeType: String?, fileUrl: String?): FileType {
    val lowerMimeType = mimeType?.lowercase()
    val lowerUrl = fileUrl?.lowercase()
    val matchers = UIKitConstants.FileTypeMatchers

    fun mimeContainsAny(keywords: List<String>) = keywords.any { lowerMimeType?.contains(it) == true }
    fun urlEndsWithAny(extensions: List<String>) = extensions.any { lowerUrl?.endsWith(it) == true }

    return when {
        lowerMimeType?.contains(matchers.PDF_KEYWORD) == true ||
            urlEndsWithAny(matchers.PDF_EXTENSIONS) -> FileType.PDF

        mimeContainsAny(matchers.DOC_KEYWORDS) ||
            urlEndsWithAny(matchers.DOC_EXTENSIONS) -> FileType.DOC

        mimeContainsAny(matchers.XLS_KEYWORDS) ||
            urlEndsWithAny(matchers.XLS_EXTENSIONS) -> FileType.XLS

        mimeContainsAny(matchers.PPT_KEYWORDS) ||
            urlEndsWithAny(matchers.PPT_EXTENSIONS) -> FileType.PPT

        mimeContainsAny(matchers.ARCHIVE_KEYWORDS) ||
            urlEndsWithAny(matchers.ARCHIVE_EXTENSIONS) -> FileType.ZIP

        lowerMimeType?.startsWith(matchers.AUDIO_MIME_PREFIX) == true ||
            urlEndsWithAny(matchers.AUDIO_EXTENSIONS) -> FileType.AUDIO

        lowerMimeType?.startsWith(matchers.VIDEO_MIME_PREFIX) == true ||
            urlEndsWithAny(matchers.VIDEO_EXTENSIONS) -> FileType.VIDEO

        lowerMimeType?.startsWith(matchers.IMAGE_MIME_PREFIX) == true ||
            urlEndsWithAny(matchers.IMAGE_EXTENSIONS) -> FileType.IMAGE

        lowerMimeType?.startsWith(matchers.TEXT_MIME_PREFIX) == true ||
            urlEndsWithAny(matchers.TEXT_EXTENSIONS) -> FileType.TEXT

        matchers.LINK_PREFIXES.any { lowerUrl?.startsWith(it) == true } -> FileType.LINK

        else -> FileType.UNKNOWN
    }
}

/**
 * Gets the drawable resource ID for a given file type.
 *
 * @param fileType The file type
 * @return The drawable resource ID for the file type icon
 */
fun getFileTypeIcon(fileType: FileType): Int {
    return when (fileType) {
        FileType.PDF -> R.drawable.cometchat_pdf_file_icon
        FileType.DOC -> R.drawable.cometchat_word_file_icon
        FileType.XLS -> R.drawable.cometchat_xlsx_file_icon
        FileType.PPT -> R.drawable.cometchat_ppt_file_icon
        FileType.ZIP -> R.drawable.cometchat_zip_file_icon
        FileType.AUDIO -> R.drawable.cometchat_audio_file_icon
        FileType.VIDEO -> R.drawable.cometchat_video_file_icon
        FileType.IMAGE -> R.drawable.cometchat_image_file_icon
        FileType.TEXT -> R.drawable.cometchat_text_file_icon
        FileType.LINK -> R.drawable.cometchat_link_file_icon
        FileType.UNKNOWN -> R.drawable.cometchat_unknown_file_icon
    }
}

/**
 * Formats a file size in bytes to a human-readable string.
 *
 * @param sizeInBytes The file size in bytes
 * @return A formatted string (e.g., "1.5 MB", "200 KB")
 */
fun formatFileSize(sizeInBytes: Long): String {
    if (sizeInBytes <= 0) return "0 B"
    
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(sizeInBytes.toDouble()) / Math.log10(1024.0)).toInt()
    val index = digitGroups.coerceIn(0, units.size - 1)
    
    val size = sizeInBytes / Math.pow(1024.0, index.toDouble())
    return if (size == size.toLong().toDouble()) {
        "${size.toLong()} ${units[index]}"
    } else {
        String.format("%.1f %s", size, units[index])
    }
}

/**
 * Extracts the file extension from a file name or URL.
 *
 * @param fileName The file name or URL
 * @return The file extension in uppercase (e.g., "PDF", "DOC"), or empty string if none
 */
fun getFileExtension(fileName: String?): String {
    if (fileName.isNullOrBlank()) return ""
    
    val lastDotIndex = fileName.lastIndexOf('.')
    if (lastDotIndex == -1 || lastDotIndex == fileName.length - 1) return ""
    
    // Handle URLs with query parameters
    val extension = fileName.substring(lastDotIndex + 1)
    val queryIndex = extension.indexOf('?')
    val cleanExtension = if (queryIndex != -1) extension.substring(0, queryIndex) else extension
    
    return cleanExtension.uppercase()
}

/**
 * Formats the subtitle for a file bubble.
 *
 * @param fileSize The file size in bytes
 * @param fileName The file name (used to extract extension)
 * @return A formatted subtitle string (e.g., "1.5 MB • PDF")
 */
fun formatFileSubtitle(fileSize: Long, fileName: String?): String {
    val sizeStr = formatFileSize(fileSize)
    val extension = getFileExtension(fileName)
    
    return if (extension.isNotEmpty()) {
        "$sizeStr • $extension"
    } else {
        sizeStr
    }
}
