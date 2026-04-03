package com.cometchat.uikit.compose.presentation.shared.messagebubble.utils

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.string
import io.kotest.property.arbitrary.stringPattern
import io.kotest.property.checkAll

/**
 * Property-based tests for File Type Icon Mapping.
 *
 * Feature: remaining-message-bubbles
 * Properties tested:
 * - Property 1: File Type Icon Mapping
 *
 * **Validates: Requirements 1.1**
 *
 * Tests the [getFileType] function which maps MIME types and file URLs to
 * [FileType] enum values for displaying appropriate file icons.
 *
 * The function follows these mapping rules:
 * - PDF: MIME contains "pdf" OR URL ends with ".pdf"
 * - DOC: MIME contains "msword" or "wordprocessingml" OR URL ends with ".doc"/".docx"
 * - XLS: MIME contains "spreadsheet" or "excel" OR URL ends with ".xls"/".xlsx"/".csv"
 * - PPT: MIME contains "presentation" or "powerpoint" OR URL ends with ".ppt"/".pptx"
 * - ZIP: MIME contains "zip", "compressed", "archive", "rar", "7z", "tar", "gzip" OR URL ends with archive extensions
 * - AUDIO: MIME starts with "audio/" OR URL ends with audio extensions
 * - VIDEO: MIME starts with "video/" OR URL ends with video extensions
 * - IMAGE: MIME starts with "image/" OR URL ends with image extensions
 * - TEXT: MIME starts with "text/" OR URL ends with text extensions
 * - LINK: URL starts with "http://" or "https://"
 * - UNKNOWN: All other cases
 */
class FileTypeIconMappingPropertyTest : StringSpec({

    /**
     * Property 1: File Type Icon Mapping
     *
     * *For any* valid MIME type string, the `getFileType()` function SHALL return
     * a valid `FileType` enum value that maps to the appropriate file icon.
     *
     * **Validates: Requirements 1.1**
     */
    "Property 1: getFileType should always return a valid FileType enum for any input" {
        // Generate arbitrary strings for MIME type and URL
        val mimeTypeArb = Arb.string(0..100)
        val fileUrlArb = Arb.string(0..200)

        checkAll(200, mimeTypeArb, fileUrlArb) { mimeType, fileUrl ->
            val result = getFileType(mimeType, fileUrl)
            
            // Result should always be a valid FileType enum value
            result shouldNotBe null
            FileType.entries.contains(result) shouldBe true
        }
    }

    "Property 1: PDF MIME types should map to FileType.PDF" {
        val pdfMimeTypes = listOf(
            "application/pdf",
            "APPLICATION/PDF",
            "application/x-pdf",
            "text/pdf"
        )
        val pdfMimeArb = Arb.element(pdfMimeTypes)

        checkAll(100, pdfMimeArb) { mimeType ->
            val result = getFileType(mimeType, null)
            result shouldBe FileType.PDF
        }
    }

    "Property 1: PDF file URLs should map to FileType.PDF" {
        val pdfUrls = listOf(
            "https://example.com/document.pdf",
            "file:///storage/document.PDF",
            "/path/to/file.pdf",
            "document.pdf"
        )
        val pdfUrlArb = Arb.element(pdfUrls)

        checkAll(100, pdfUrlArb) { fileUrl ->
            val result = getFileType(null, fileUrl)
            result shouldBe FileType.PDF
        }
    }

    "Property 1: Word document MIME types should map to FileType.DOC" {
        val docMimeTypes = listOf(
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "APPLICATION/MSWORD"
        )
        val docMimeArb = Arb.element(docMimeTypes)

        checkAll(100, docMimeArb) { mimeType ->
            val result = getFileType(mimeType, null)
            result shouldBe FileType.DOC
        }
    }

    "Property 1: Word document URLs should map to FileType.DOC" {
        val docUrls = listOf(
            "https://example.com/document.doc",
            "https://example.com/document.docx",
            "/path/to/file.DOC",
            "document.DOCX"
        )
        val docUrlArb = Arb.element(docUrls)

        checkAll(100, docUrlArb) { fileUrl ->
            val result = getFileType(null, fileUrl)
            result shouldBe FileType.DOC
        }
    }

    "Property 1: Excel MIME types should map to FileType.XLS" {
        val xlsMimeTypes = listOf(
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/excel",
            "text/csv"
        )
        val xlsMimeArb = Arb.element(xlsMimeTypes)

        checkAll(100, xlsMimeArb) { mimeType ->
            val result = getFileType(mimeType, null)
            result shouldBe FileType.XLS
        }
    }

    "Property 1: Excel file URLs should map to FileType.XLS" {
        val xlsUrls = listOf(
            "https://example.com/spreadsheet.xls",
            "https://example.com/spreadsheet.xlsx",
            "https://example.com/data.csv",
            "/path/to/file.XLS"
        )
        val xlsUrlArb = Arb.element(xlsUrls)

        checkAll(100, xlsUrlArb) { fileUrl ->
            val result = getFileType(null, fileUrl)
            result shouldBe FileType.XLS
        }
    }

    "Property 1: PowerPoint MIME types should map to FileType.PPT" {
        val pptMimeTypes = listOf(
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/powerpoint"
        )
        val pptMimeArb = Arb.element(pptMimeTypes)

        checkAll(100, pptMimeArb) { mimeType ->
            val result = getFileType(mimeType, null)
            result shouldBe FileType.PPT
        }
    }

    "Property 1: PowerPoint file URLs should map to FileType.PPT" {
        val pptUrls = listOf(
            "https://example.com/presentation.ppt",
            "https://example.com/presentation.pptx",
            "/path/to/file.PPT",
            "slides.PPTX"
        )
        val pptUrlArb = Arb.element(pptUrls)

        checkAll(100, pptUrlArb) { fileUrl ->
            val result = getFileType(null, fileUrl)
            result shouldBe FileType.PPT
        }
    }

    "Property 1: Archive MIME types should map to FileType.ZIP" {
        val zipMimeTypes = listOf(
            "application/zip",
            "application/x-zip-compressed",
            "application/x-compressed",
            "application/x-rar-compressed",
            "application/x-7z-compressed",
            "application/x-tar",
            "application/gzip",
            "application/x-archive"
        )
        val zipMimeArb = Arb.element(zipMimeTypes)

        checkAll(100, zipMimeArb) { mimeType ->
            val result = getFileType(mimeType, null)
            result shouldBe FileType.ZIP
        }
    }

    "Property 1: Archive file URLs should map to FileType.ZIP" {
        val zipUrls = listOf(
            "https://example.com/archive.zip",
            "https://example.com/archive.rar",
            "https://example.com/archive.7z",
            "https://example.com/archive.tar",
            "https://example.com/archive.gz",
            "/path/to/file.ZIP"
        )
        val zipUrlArb = Arb.element(zipUrls)

        checkAll(100, zipUrlArb) { fileUrl ->
            val result = getFileType(null, fileUrl)
            result shouldBe FileType.ZIP
        }
    }

    "Property 1: Audio MIME types should map to FileType.AUDIO" {
        val audioMimeTypes = listOf(
            "audio/mpeg",
            "audio/mp3",
            "audio/wav",
            "audio/aac",
            "audio/ogg",
            "audio/flac",
            "audio/x-m4a",
            "AUDIO/MP3"
        )
        val audioMimeArb = Arb.element(audioMimeTypes)

        checkAll(100, audioMimeArb) { mimeType ->
            val result = getFileType(mimeType, null)
            result shouldBe FileType.AUDIO
        }
    }

    "Property 1: Audio file URLs should map to FileType.AUDIO" {
        val audioUrls = listOf(
            "https://example.com/song.mp3",
            "https://example.com/audio.wav",
            "https://example.com/music.aac",
            "https://example.com/track.m4a",
            "https://example.com/sound.ogg",
            "https://example.com/audio.flac",
            "/path/to/file.MP3"
        )
        val audioUrlArb = Arb.element(audioUrls)

        checkAll(100, audioUrlArb) { fileUrl ->
            val result = getFileType(null, fileUrl)
            result shouldBe FileType.AUDIO
        }
    }

    "Property 1: Video MIME types should map to FileType.VIDEO" {
        val videoMimeTypes = listOf(
            "video/mp4",
            "video/quicktime",
            "video/x-msvideo",
            "video/x-matroska",
            "video/webm",
            "VIDEO/MP4"
        )
        val videoMimeArb = Arb.element(videoMimeTypes)

        checkAll(100, videoMimeArb) { mimeType ->
            val result = getFileType(mimeType, null)
            result shouldBe FileType.VIDEO
        }
    }

    "Property 1: Video file URLs should map to FileType.VIDEO" {
        val videoUrls = listOf(
            "https://example.com/video.mp4",
            "https://example.com/movie.mov",
            "https://example.com/clip.avi",
            "https://example.com/video.mkv",
            "https://example.com/video.webm",
            "/path/to/file.MP4"
        )
        val videoUrlArb = Arb.element(videoUrls)

        checkAll(100, videoUrlArb) { fileUrl ->
            val result = getFileType(null, fileUrl)
            result shouldBe FileType.VIDEO
        }
    }

    "Property 1: Image MIME types should map to FileType.IMAGE" {
        val imageMimeTypes = listOf(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "image/bmp",
            "image/svg+xml",
            "IMAGE/JPEG"
        )
        val imageMimeArb = Arb.element(imageMimeTypes)

        checkAll(100, imageMimeArb) { mimeType ->
            val result = getFileType(mimeType, null)
            result shouldBe FileType.IMAGE
        }
    }

    "Property 1: Image file URLs should map to FileType.IMAGE" {
        val imageUrls = listOf(
            "https://example.com/photo.jpg",
            "https://example.com/photo.jpeg",
            "https://example.com/image.png",
            "https://example.com/animation.gif",
            "https://example.com/image.webp",
            "https://example.com/image.bmp",
            "https://example.com/icon.svg",
            "/path/to/file.JPG"
        )
        val imageUrlArb = Arb.element(imageUrls)

        checkAll(100, imageUrlArb) { fileUrl ->
            val result = getFileType(null, fileUrl)
            result shouldBe FileType.IMAGE
        }
    }

    "Property 1: Text MIME types should map to FileType.TEXT" {
        val textMimeTypes = listOf(
            "text/plain",
            "text/html",
            "text/css",
            "text/javascript",
            "text/xml",
            "text/markdown",
            "TEXT/PLAIN"
        )
        val textMimeArb = Arb.element(textMimeTypes)

        checkAll(100, textMimeArb) { mimeType ->
            val result = getFileType(mimeType, null)
            result shouldBe FileType.TEXT
        }
    }

    "Property 1: Text file URLs should map to FileType.TEXT" {
        val textUrls = listOf(
            "https://example.com/readme.txt",
            "https://example.com/document.rtf",
            "https://example.com/readme.md",
            "https://example.com/data.json",
            "https://example.com/config.xml",
            "https://example.com/page.html",
            "https://example.com/styles.css",
            "https://example.com/script.js",
            "/path/to/file.TXT"
        )
        val textUrlArb = Arb.element(textUrls)

        checkAll(100, textUrlArb) { fileUrl ->
            val result = getFileType(null, fileUrl)
            result shouldBe FileType.TEXT
        }
    }

    "Property 1: HTTP/HTTPS URLs without file extensions should map to FileType.LINK" {
        val linkUrls = listOf(
            "https://example.com",
            "https://example.com/page",
            "http://example.com/resource",
            "https://subdomain.example.com/path/to/resource"
        )
        val linkUrlArb = Arb.element(linkUrls)

        checkAll(100, linkUrlArb) { fileUrl ->
            val result = getFileType(null, fileUrl)
            result shouldBe FileType.LINK
        }
    }

    "Property 1: Unknown MIME types should map to FileType.UNKNOWN" {
        val unknownMimeTypes = listOf(
            "application/octet-stream",
            "application/unknown",
            "custom/type",
            "x-custom/x-type",
            ""
        )
        val unknownMimeArb = Arb.element(unknownMimeTypes)

        checkAll(100, unknownMimeArb) { mimeType ->
            val result = getFileType(mimeType, null)
            result shouldBe FileType.UNKNOWN
        }
    }

    "Property 1: Unknown file extensions should map to FileType.UNKNOWN" {
        val unknownUrls = listOf(
            "/path/to/file.xyz",
            "/path/to/file.unknown",
            "/path/to/file.abc123",
            "file.customext"
        )
        val unknownUrlArb = Arb.element(unknownUrls)

        checkAll(100, unknownUrlArb) { fileUrl ->
            val result = getFileType(null, fileUrl)
            result shouldBe FileType.UNKNOWN
        }
    }

    "Property 1: Null inputs should map to FileType.UNKNOWN" {
        val result = getFileType(null, null)
        result shouldBe FileType.UNKNOWN
    }

    "Property 1: Empty string inputs should map to FileType.UNKNOWN" {
        val result = getFileType("", "")
        result shouldBe FileType.UNKNOWN
    }

    "Property 1: MIME type should take precedence over URL extension" {
        // When MIME type indicates PDF but URL has .doc extension, MIME type wins
        val result = getFileType("application/pdf", "document.doc")
        result shouldBe FileType.PDF
    }

    "Property 1: URL extension should be used when MIME type is null" {
        val result = getFileType(null, "document.pdf")
        result shouldBe FileType.PDF
    }

    "Property 1: URL extension should be used when MIME type is unknown" {
        val result = getFileType("application/octet-stream", "document.pdf")
        // MIME type is checked first, but "octet-stream" doesn't match any known type
        // So it falls through to URL check
        result shouldBe FileType.PDF
    }

    "Property 1: Case insensitivity - MIME types should be case-insensitive" {
        val mimeTypeCases = listOf(
            "APPLICATION/PDF",
            "Application/Pdf",
            "application/PDF",
            "APPLICATION/pdf"
        )
        val mimeArb = Arb.element(mimeTypeCases)

        checkAll(100, mimeArb) { mimeType ->
            val result = getFileType(mimeType, null)
            result shouldBe FileType.PDF
        }
    }

    "Property 1: Case insensitivity - URL extensions should be case-insensitive" {
        val urlCases = listOf(
            "document.PDF",
            "document.Pdf",
            "document.pDf",
            "DOCUMENT.PDF"
        )
        val urlArb = Arb.element(urlCases)

        checkAll(100, urlArb) { fileUrl ->
            val result = getFileType(null, fileUrl)
            result shouldBe FileType.PDF
        }
    }

    "Property 1: getFileType should be deterministic - same inputs produce same output" {
        val mimeTypeArb = Arb.string(0..50)
        val fileUrlArb = Arb.string(0..100)

        checkAll(100, mimeTypeArb, fileUrlArb) { mimeType, fileUrl ->
            val result1 = getFileType(mimeType, fileUrl)
            val result2 = getFileType(mimeType, fileUrl)
            
            result1 shouldBe result2
        }
    }

    "Property 1: All FileType enum values should be reachable" {
        // Verify that each FileType can be produced by getFileType
        val testCases = mapOf(
            FileType.PDF to Pair("application/pdf", null),
            FileType.DOC to Pair("application/msword", null),
            FileType.XLS to Pair("application/vnd.ms-excel", null),
            FileType.PPT to Pair("application/vnd.ms-powerpoint", null),
            FileType.ZIP to Pair("application/zip", null),
            FileType.AUDIO to Pair("audio/mpeg", null),
            FileType.VIDEO to Pair("video/mp4", null),
            FileType.IMAGE to Pair("image/jpeg", null),
            FileType.TEXT to Pair("text/plain", null),
            FileType.LINK to Pair(null, "https://example.com"),
            FileType.UNKNOWN to Pair(null, null)
        )

        testCases.forEach { (expectedType, inputs) ->
            val result = getFileType(inputs.first, inputs.second)
            result shouldBe expectedType
        }
    }

    "Property 1: MIME type partial matching should work correctly" {
        // Test that partial MIME type matching works (e.g., "pdf" anywhere in MIME type)
        val pdfVariants = listOf(
            "application/pdf",
            "application/x-pdf",
            "text/pdf",
            "custom/pdf-document"
        )
        val pdfArb = Arb.element(pdfVariants)

        checkAll(100, pdfArb) { mimeType ->
            val result = getFileType(mimeType, null)
            result shouldBe FileType.PDF
        }
    }

    "Property 1: Audio MIME type prefix matching should work" {
        // Any MIME type starting with "audio/" should map to AUDIO
        val audioMimeArb = Arb.stringPattern("audio/[a-z0-9\\-]+")

        checkAll(100, audioMimeArb) { mimeType ->
            val result = getFileType(mimeType, null)
            result shouldBe FileType.AUDIO
        }
    }

    "Property 1: Video MIME type prefix matching should work" {
        // Any MIME type starting with "video/" should map to VIDEO
        val videoMimeArb = Arb.stringPattern("video/[a-z0-9\\-]+")

        checkAll(100, videoMimeArb) { mimeType ->
            val result = getFileType(mimeType, null)
            result shouldBe FileType.VIDEO
        }
    }

    "Property 1: Image MIME type prefix matching should work" {
        // Any MIME type starting with "image/" should map to IMAGE
        val imageMimeArb = Arb.stringPattern("image/[a-z0-9\\-\\+]+")

        checkAll(100, imageMimeArb) { mimeType ->
            val result = getFileType(mimeType, null)
            result shouldBe FileType.IMAGE
        }
    }

    "Property 1: Text MIME type prefix matching should work" {
        // Any MIME type starting with "text/" should map to TEXT
        val textMimeArb = Arb.stringPattern("text/[a-z0-9\\-]+")

        checkAll(100, textMimeArb) { mimeType ->
            val result = getFileType(mimeType, null)
            result shouldBe FileType.TEXT
        }
    }
})
