package com.cometchat.uikit.compose.presentation.shared.messagebubble.utils

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotBeEmpty
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.arbitrary.stringPattern
import io.kotest.property.checkAll

/**
 * Property-based tests for File Subtitle Formatting.
 *
 * Feature: remaining-message-bubbles
 * Properties tested:
 * - Property 2: File Subtitle Formatting
 *
 * **Validates: Requirements 1.3**
 *
 * Tests the [formatFileSubtitle] function which formats file size and extension
 * into a human-readable subtitle string (e.g., "1.5 MB • PDF").
 *
 * The function follows these formatting rules:
 * - File size is formatted with appropriate units (B, KB, MB, GB, TB)
 * - Extension is extracted from file name and displayed in uppercase
 * - Format: "{size} • {extension}" when extension exists
 * - Format: "{size}" when no extension
 */
class FileSubtitleFormattingPropertyTest : StringSpec({

    /**
     * Property 2: File Subtitle Formatting
     *
     * *For any* combination of file size (Long) and file name (String),
     * the formatted subtitle string SHALL contain the file size component.
     *
     * **Validates: Requirements 1.3**
     */
    "Property 2: formatFileSubtitle should always contain formatted file size" {
        val fileSizeArb = Arb.long(0L..Long.MAX_VALUE / 2)
        val fileNameArb = Arb.string(0..100)

        checkAll(200, fileSizeArb, fileNameArb) { fileSize, fileName ->
            val result = formatFileSubtitle(fileSize, fileName)
            
            // Result should never be empty
            result.shouldNotBeEmpty()
            
            // Result should contain a size unit
            val containsSizeUnit = result.contains("B") || 
                                   result.contains("KB") || 
                                   result.contains("MB") || 
                                   result.contains("GB") || 
                                   result.contains("TB")
            containsSizeUnit shouldBe true
        }
    }

    "Property 2: formatFileSubtitle should contain extension when file has valid extension" {
        val fileSizeArb = Arb.long(1L..1_000_000_000L)
        val extensions = listOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", 
                                "zip", "mp3", "mp4", "jpg", "png", "txt")
        val extensionArb = Arb.element(extensions)

        checkAll(200, fileSizeArb, extensionArb) { fileSize, extension ->
            val fileName = "document.$extension"
            val result = formatFileSubtitle(fileSize, fileName)
            
            // Result should contain the extension in uppercase
            result shouldContain extension.uppercase()
            
            // Result should contain the bullet separator
            result shouldContain "•"
        }
    }

    "Property 2: formatFileSubtitle should not contain bullet when no extension" {
        val fileSizeArb = Arb.long(1L..1_000_000_000L)
        val fileNamesWithoutExtension = listOf(
            "document",
            "file_without_ext",
            "noextension",
            "",
            null
        )
        val fileNameArb = Arb.element(fileNamesWithoutExtension)

        checkAll(100, fileSizeArb, fileNameArb) { fileSize, fileName ->
            val result = formatFileSubtitle(fileSize, fileName)
            
            // Result should NOT contain the bullet separator when no extension
            result.contains("•") shouldBe false
        }
    }

    "Property 2: formatFileSize should format bytes correctly" {
        // Test sizes in bytes range (0-1023)
        val byteSizeArb = Arb.long(0L..1023L)

        checkAll(100, byteSizeArb) { size ->
            val result = formatFileSize(size)
            
            if (size <= 0) {
                result shouldBe "0 B"
            } else {
                result shouldContain "B"
                result.contains("KB") shouldBe false
            }
        }
    }

    "Property 2: formatFileSize should format kilobytes correctly" {
        // Test sizes in KB range (1024 - 1048575)
        val kbSizeArb = Arb.long(1024L..1048575L)

        checkAll(100, kbSizeArb) { size ->
            val result = formatFileSize(size)
            result shouldContain "KB"
        }
    }

    "Property 2: formatFileSize should format megabytes correctly" {
        // Test sizes in MB range (1MB - 1GB-1)
        val mbSizeArb = Arb.long(1048576L..1073741823L)

        checkAll(100, mbSizeArb) { size ->
            val result = formatFileSize(size)
            result shouldContain "MB"
        }
    }

    "Property 2: formatFileSize should format gigabytes correctly" {
        // Test sizes in GB range (1GB - 1TB-1)
        val gbSizeArb = Arb.long(1073741824L..1099511627775L)

        checkAll(100, gbSizeArb) { size ->
            val result = formatFileSize(size)
            result shouldContain "GB"
        }
    }

    "Property 2: formatFileSize should format terabytes correctly" {
        // Test sizes in TB range (1TB and above)
        val tbSizeArb = Arb.long(1099511627776L..10995116277760L)

        checkAll(100, tbSizeArb) { size ->
            val result = formatFileSize(size)
            result shouldContain "TB"
        }
    }

    "Property 2: formatFileSize should handle zero and negative values" {
        val zeroOrNegativeArb = Arb.long(-1000L..0L)

        checkAll(100, zeroOrNegativeArb) { size ->
            val result = formatFileSize(size)
            result shouldBe "0 B"
        }
    }

    "Property 2: getFileExtension should extract extension correctly" {
        val extensions = listOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
                                "zip", "rar", "mp3", "wav", "mp4", "mov", "jpg", 
                                "jpeg", "png", "gif", "txt", "md", "json")
        val extensionArb = Arb.element(extensions)
        val fileNamePrefixArb = Arb.element(listOf("document", "file", "report", "data", "image"))

        checkAll(200, fileNamePrefixArb, extensionArb) { prefix, extension ->
            val fileName = "$prefix.$extension"
            val result = getFileExtension(fileName)
            
            result shouldBe extension.uppercase()
        }
    }

    "Property 2: getFileExtension should handle URLs with query parameters" {
        val urlsWithParams = listOf(
            "https://example.com/file.pdf?token=abc123",
            "https://cdn.example.com/document.docx?v=1.0&auth=xyz",
            "/storage/file.jpg?timestamp=12345",
            "file.png?size=large"
        )
        val urlArb = Arb.element(urlsWithParams)

        checkAll(100, urlArb) { url ->
            val result = getFileExtension(url)
            
            // Should extract extension without query parameters
            result.contains("?") shouldBe false
            result.shouldNotBeEmpty()
        }
    }

    "Property 2: getFileExtension should return empty for files without extension" {
        val filesWithoutExtension = listOf(
            "document",
            "file_without_ext",
            "noextension",
            "/path/to/file",
            "https://example.com/resource"
        )
        val fileArb = Arb.element(filesWithoutExtension)

        checkAll(100, fileArb) { fileName ->
            val result = getFileExtension(fileName)
            result shouldBe ""
        }
    }

    "Property 2: getFileExtension should return empty for null or blank input" {
        val nullOrBlankInputs = listOf(null, "", "   ", "\t", "\n")
        val inputArb = Arb.element(nullOrBlankInputs)

        checkAll(100, inputArb) { input ->
            val result = getFileExtension(input)
            result shouldBe ""
        }
    }

    "Property 2: getFileExtension should handle files ending with dot" {
        val filesEndingWithDot = listOf(
            "document.",
            "file.",
            "/path/to/file.",
            "https://example.com/resource."
        )
        val fileArb = Arb.element(filesEndingWithDot)

        checkAll(100, fileArb) { fileName ->
            val result = getFileExtension(fileName)
            result shouldBe ""
        }
    }

    "Property 2: getFileExtension should return uppercase extension" {
        val mixedCaseExtensions = listOf(
            "document.PDF",
            "file.Pdf",
            "report.pDf",
            "data.DOC",
            "image.JpG"
        )
        val fileArb = Arb.element(mixedCaseExtensions)

        checkAll(100, fileArb) { fileName ->
            val result = getFileExtension(fileName)
            
            // Result should be all uppercase
            result shouldBe result.uppercase()
        }
    }

    "Property 2: formatFileSubtitle should be deterministic" {
        val fileSizeArb = Arb.long(0L..10_000_000_000L)
        val fileNameArb = Arb.string(0..50)

        checkAll(100, fileSizeArb, fileNameArb) { fileSize, fileName ->
            val result1 = formatFileSubtitle(fileSize, fileName)
            val result2 = formatFileSubtitle(fileSize, fileName)
            
            result1 shouldBe result2
        }
    }

    "Property 2: formatFileSize should be deterministic" {
        val fileSizeArb = Arb.long(0L..Long.MAX_VALUE / 2)

        checkAll(100, fileSizeArb) { fileSize ->
            val result1 = formatFileSize(fileSize)
            val result2 = formatFileSize(fileSize)
            
            result1 shouldBe result2
        }
    }

    "Property 2: formatFileSubtitle format should be 'size • extension' when extension exists" {
        val fileSizeArb = Arb.long(1L..1_000_000_000L)
        val extensions = listOf("pdf", "doc", "xls", "ppt", "zip", "mp3", "jpg", "txt")
        val extensionArb = Arb.element(extensions)

        checkAll(100, fileSizeArb, extensionArb) { fileSize, extension ->
            val fileName = "file.$extension"
            val result = formatFileSubtitle(fileSize, fileName)
            
            // Verify format: "{size} • {extension}"
            val parts = result.split(" • ")
            parts.size shouldBe 2
            
            // First part should be the formatted size
            val sizePart = parts[0]
            val containsSizeUnit = sizePart.contains("B") || 
                                   sizePart.contains("KB") || 
                                   sizePart.contains("MB") || 
                                   sizePart.contains("GB") || 
                                   sizePart.contains("TB")
            containsSizeUnit shouldBe true
            
            // Second part should be the uppercase extension
            parts[1] shouldBe extension.uppercase()
        }
    }

    "Property 2: formatFileSize should produce valid numeric prefix" {
        val fileSizeArb = Arb.long(1L..10_000_000_000L)

        checkAll(100, fileSizeArb) { fileSize ->
            val result = formatFileSize(fileSize)
            
            // Extract the numeric part (before the space)
            val numericPart = result.substringBefore(" ")
            
            // Should be a valid number (integer or decimal)
            val isValidNumber = numericPart.toDoubleOrNull() != null
            isValidNumber shouldBe true
        }
    }

    "Property 2: formatFileSize should not produce negative numbers" {
        val fileSizeArb = Arb.long(Long.MIN_VALUE..Long.MAX_VALUE / 2)

        checkAll(100, fileSizeArb) { fileSize ->
            val result = formatFileSize(fileSize)
            
            // Result should not contain negative sign (except for "0 B" case)
            if (result != "0 B") {
                val numericPart = result.substringBefore(" ")
                val number = numericPart.toDoubleOrNull() ?: 0.0
                (number >= 0) shouldBe true
            }
        }
    }

    "Property 2: Specific size boundary tests" {
        // Test exact boundaries
        formatFileSize(0L) shouldBe "0 B"
        formatFileSize(1L) shouldBe "1 B"
        formatFileSize(1023L) shouldBe "1023 B"
        formatFileSize(1024L) shouldBe "1 KB"
        formatFileSize(1048576L) shouldBe "1 MB"
        formatFileSize(1073741824L) shouldBe "1 GB"
        formatFileSize(1099511627776L) shouldBe "1 TB"
    }

    "Property 2: formatFileSubtitle with common file types" {
        // Test with realistic file scenarios
        val testCases = listOf(
            Triple(1024L, "document.pdf", "1 KB • PDF"),
            Triple(2097152L, "report.docx", "2 MB • DOCX"),
            Triple(512L, "readme.txt", "512 B • TXT"),
            Triple(5368709120L, "video.mp4", "5 GB • MP4")
        )

        testCases.forEach { (size, fileName, expected) ->
            val result = formatFileSubtitle(size, fileName)
            result shouldBe expected
        }
    }

    "Property 2: getFileExtension should handle multiple dots in filename" {
        val filesWithMultipleDots = listOf(
            "document.backup.pdf",
            "file.v2.0.doc",
            "report.2024.01.xlsx",
            "archive.tar.gz"
        )
        val fileArb = Arb.element(filesWithMultipleDots)

        checkAll(100, fileArb) { fileName ->
            val result = getFileExtension(fileName)
            
            // Should extract only the last extension
            result.shouldNotBeEmpty()
            result.contains(".") shouldBe false
        }
    }

    "Property 2: formatFileSubtitle should handle very long file names" {
        val longFileNameArb = Arb.stringPattern("[a-z]{100,200}\\.[a-z]{3}")

        checkAll(50, longFileNameArb) { fileName ->
            val result = formatFileSubtitle(1024L, fileName)
            
            // Should still produce valid output
            result.shouldNotBeEmpty()
            result shouldContain "•"
        }
    }

    "Property 2: formatFileSubtitle should handle special characters in file names" {
        val specialFileNames = listOf(
            "file with spaces.pdf",
            "file-with-dashes.doc",
            "file_with_underscores.xls",
            "file(1).ppt",
            "file[copy].zip"
        )
        val fileArb = Arb.element(specialFileNames)

        checkAll(100, fileArb) { fileName ->
            val result = formatFileSubtitle(1024L, fileName)
            
            // Should still extract extension correctly
            result shouldContain "•"
            result.shouldNotBeEmpty()
        }
    }
})
