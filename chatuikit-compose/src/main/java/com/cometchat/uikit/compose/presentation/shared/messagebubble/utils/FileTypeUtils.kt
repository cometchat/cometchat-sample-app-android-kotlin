package com.cometchat.uikit.compose.presentation.shared.messagebubble.utils

import com.cometchat.uikit.compose.R

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
    
    return when {
        // PDF
        lowerMimeType?.contains("pdf") == true ||
        lowerUrl?.endsWith(".pdf") == true -> FileType.PDF
        
        // Word documents
        lowerMimeType?.contains("msword") == true ||
        lowerMimeType?.contains("wordprocessingml") == true ||
        lowerUrl?.endsWith(".doc") == true ||
        lowerUrl?.endsWith(".docx") == true -> FileType.DOC
        
        // Excel spreadsheets
        lowerMimeType?.contains("spreadsheet") == true ||
        lowerMimeType?.contains("excel") == true ||
        lowerUrl?.endsWith(".xls") == true ||
        lowerUrl?.endsWith(".xlsx") == true ||
        lowerUrl?.endsWith(".csv") == true -> FileType.XLS
        
        // PowerPoint presentations
        lowerMimeType?.contains("presentation") == true ||
        lowerMimeType?.contains("powerpoint") == true ||
        lowerUrl?.endsWith(".ppt") == true ||
        lowerUrl?.endsWith(".pptx") == true -> FileType.PPT
        
        // Archives
        lowerMimeType?.contains("zip") == true ||
        lowerMimeType?.contains("compressed") == true ||
        lowerMimeType?.contains("archive") == true ||
        lowerMimeType?.contains("rar") == true ||
        lowerMimeType?.contains("7z") == true ||
        lowerMimeType?.contains("tar") == true ||
        lowerMimeType?.contains("gzip") == true ||
        lowerUrl?.endsWith(".zip") == true ||
        lowerUrl?.endsWith(".rar") == true ||
        lowerUrl?.endsWith(".7z") == true ||
        lowerUrl?.endsWith(".tar") == true ||
        lowerUrl?.endsWith(".gz") == true -> FileType.ZIP
        
        // Audio files
        lowerMimeType?.startsWith("audio/") == true ||
        lowerUrl?.endsWith(".mp3") == true ||
        lowerUrl?.endsWith(".wav") == true ||
        lowerUrl?.endsWith(".aac") == true ||
        lowerUrl?.endsWith(".m4a") == true ||
        lowerUrl?.endsWith(".ogg") == true ||
        lowerUrl?.endsWith(".flac") == true -> FileType.AUDIO
        
        // Video files
        lowerMimeType?.startsWith("video/") == true ||
        lowerUrl?.endsWith(".mp4") == true ||
        lowerUrl?.endsWith(".mov") == true ||
        lowerUrl?.endsWith(".avi") == true ||
        lowerUrl?.endsWith(".mkv") == true ||
        lowerUrl?.endsWith(".webm") == true -> FileType.VIDEO
        
        // Image files
        lowerMimeType?.startsWith("image/") == true ||
        lowerUrl?.endsWith(".jpg") == true ||
        lowerUrl?.endsWith(".jpeg") == true ||
        lowerUrl?.endsWith(".png") == true ||
        lowerUrl?.endsWith(".gif") == true ||
        lowerUrl?.endsWith(".webp") == true ||
        lowerUrl?.endsWith(".bmp") == true ||
        lowerUrl?.endsWith(".svg") == true -> FileType.IMAGE
        
        // Text files
        lowerMimeType?.startsWith("text/") == true ||
        lowerUrl?.endsWith(".txt") == true ||
        lowerUrl?.endsWith(".rtf") == true ||
        lowerUrl?.endsWith(".md") == true ||
        lowerUrl?.endsWith(".json") == true ||
        lowerUrl?.endsWith(".xml") == true ||
        lowerUrl?.endsWith(".html") == true ||
        lowerUrl?.endsWith(".css") == true ||
        lowerUrl?.endsWith(".js") == true -> FileType.TEXT
        
        // Links
        lowerUrl?.startsWith("http://") == true ||
        lowerUrl?.startsWith("https://") == true -> FileType.LINK
        
        // Unknown
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
