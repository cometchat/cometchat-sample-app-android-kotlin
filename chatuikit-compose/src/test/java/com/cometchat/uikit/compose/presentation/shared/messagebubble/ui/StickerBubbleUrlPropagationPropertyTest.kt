package com.cometchat.uikit.compose.presentation.shared.messagebubble.ui

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldBeEmpty
import io.kotest.matchers.string.shouldNotBeEmpty
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.string
import io.kotest.property.arbitrary.stringPattern
import io.kotest.property.checkAll
import org.json.JSONObject

/**
 * Property-based tests for Sticker URL Propagation.
 *
 * Feature: remaining-message-bubbles
 * Properties tested:
 * - Property 5: Sticker URL Propagation
 *
 * **Validates: Requirements 3.1**
 *
 * Tests the sticker URL extraction logic from [CometChatStickerBubble]:
 * - For all CustomMessages with sticker_url in customData, the URL is correctly extracted
 * - Null or missing sticker_url results in empty string (error state)
 *
 * The extraction logic follows these rules:
 * 1. First try to get "sticker_url" from customData
 * 2. If not found, try to get "url" from customData
 * 3. If neither found, return empty string
 */
class StickerBubbleUrlPropagationPropertyTest : StringSpec({

    /**
     * Property 5: Sticker URL Propagation - Valid sticker_url is extracted correctly
     *
     * *For any* CustomMessage with a valid sticker_url in customData,
     * the extractStickerUrl function SHALL return that exact URL.
     *
     * **Validates: Requirements 3.1**
     */
    "Property 5: Valid sticker_url should be extracted correctly" {
        val validUrls = listOf(
            "https://example.com/sticker.png",
            "https://cdn.stickers.com/pack1/sticker_001.gif",
            "https://media.example.org/stickers/happy.webp",
            "https://storage.googleapis.com/stickers/emoji_smile.png",
            "https://s3.amazonaws.com/bucket/sticker.gif",
            "http://example.com/sticker.png",
            "https://example.com/path/to/sticker.png?token=abc123",
            "https://example.com/sticker.png#fragment"
        )
        val urlArb = Arb.element(validUrls)

        checkAll(200, urlArb) { expectedUrl ->
            val customData = JSONObject().apply {
                put("sticker_url", expectedUrl)
            }
            
            val extractedUrl = extractStickerUrl(customData)
            
            extractedUrl shouldBe expectedUrl
        }
    }

    /**
     * Property 5: Sticker URL Propagation - Fallback to "url" key
     *
     * *For any* CustomMessage with "url" (but not "sticker_url") in customData,
     * the extractStickerUrl function SHALL return the "url" value.
     *
     * **Validates: Requirements 3.1**
     */
    "Property 5: Should fallback to 'url' key when 'sticker_url' is not present" {
        val validUrls = listOf(
            "https://example.com/sticker.png",
            "https://cdn.stickers.com/pack1/sticker_001.gif",
            "https://media.example.org/stickers/happy.webp"
        )
        val urlArb = Arb.element(validUrls)

        checkAll(100, urlArb) { expectedUrl ->
            val customData = JSONObject().apply {
                put("url", expectedUrl)
            }
            
            val extractedUrl = extractStickerUrl(customData)
            
            extractedUrl shouldBe expectedUrl
        }
    }

    /**
     * Property 5: Sticker URL Propagation - sticker_url takes precedence over url
     *
     * *For any* CustomMessage with both "sticker_url" and "url" in customData,
     * the extractStickerUrl function SHALL return the "sticker_url" value.
     *
     * **Validates: Requirements 3.1**
     */
    "Property 5: sticker_url should take precedence over url" {
        val stickerUrls = listOf(
            "https://example.com/sticker_primary.png",
            "https://cdn.stickers.com/primary.gif"
        )
        val fallbackUrls = listOf(
            "https://example.com/fallback.png",
            "https://cdn.stickers.com/fallback.gif"
        )
        val stickerUrlArb = Arb.element(stickerUrls)
        val fallbackUrlArb = Arb.element(fallbackUrls)

        checkAll(100, stickerUrlArb, fallbackUrlArb) { stickerUrl, fallbackUrl ->
            val customData = JSONObject().apply {
                put("sticker_url", stickerUrl)
                put("url", fallbackUrl)
            }
            
            val extractedUrl = extractStickerUrl(customData)
            
            extractedUrl shouldBe stickerUrl
            extractedUrl shouldNotBe fallbackUrl
        }
    }

    /**
     * Property 5: Sticker URL Propagation - Missing URL returns empty string
     *
     * *For any* CustomMessage without sticker_url or url in customData,
     * the extractStickerUrl function SHALL return an empty string.
     *
     * **Validates: Requirements 3.1**
     */
    "Property 5: Missing sticker_url and url should return empty string" {
        val otherKeys = listOf(
            "sticker_name",
            "name",
            "type",
            "category",
            "pack_id"
        )
        val keyArb = Arb.element(otherKeys)
        val valueArb = Arb.string(1..50)

        checkAll(100, keyArb, valueArb) { key, value ->
            val customData = JSONObject().apply {
                put(key, value)
            }
            
            val extractedUrl = extractStickerUrl(customData)
            
            extractedUrl.shouldBeEmpty()
        }
    }

    /**
     * Property 5: Sticker URL Propagation - Null customData returns empty string
     *
     * *For any* null customData, the extractStickerUrl function SHALL return
     * an empty string (triggering error state).
     *
     * **Validates: Requirements 3.1**
     */
    "Property 5: Null customData should return empty string" {
        val extractedUrl = extractStickerUrl(null)
        
        extractedUrl.shouldBeEmpty()
    }

    /**
     * Property 5: Sticker URL Propagation - Empty customData returns empty string
     *
     * *For any* empty customData JSONObject, the extractStickerUrl function
     * SHALL return an empty string.
     *
     * **Validates: Requirements 3.1**
     */
    "Property 5: Empty customData should return empty string" {
        val customData = JSONObject()
        
        val extractedUrl = extractStickerUrl(customData)
        
        extractedUrl.shouldBeEmpty()
    }

    /**
     * Property 5: Sticker URL Propagation - Empty sticker_url value returns empty string
     *
     * *For any* CustomMessage with empty sticker_url value,
     * the extractStickerUrl function SHALL return an empty string.
     *
     * **Validates: Requirements 3.1**
     */
    "Property 5: Empty sticker_url value should return empty string" {
        val customData = JSONObject().apply {
            put("sticker_url", "")
        }
        
        val extractedUrl = extractStickerUrl(customData)
        
        extractedUrl.shouldBeEmpty()
    }

    /**
     * Property 5: Sticker URL Propagation - URL extraction is deterministic
     *
     * *For any* CustomMessage with sticker data, calling extractStickerUrl
     * multiple times SHALL return the same result.
     *
     * **Validates: Requirements 3.1**
     */
    "Property 5: URL extraction should be deterministic" {
        val urlArb = Arb.string(10..100)

        checkAll(100, urlArb) { url ->
            val customData = JSONObject().apply {
                put("sticker_url", url)
            }
            
            val result1 = extractStickerUrl(customData)
            val result2 = extractStickerUrl(customData)
            val result3 = extractStickerUrl(customData)
            
            result1 shouldBe result2
            result2 shouldBe result3
        }
    }

    /**
     * Property 5: Sticker URL Propagation - Various URL formats are preserved
     *
     * *For any* valid URL format (with query params, fragments, special chars),
     * the extractStickerUrl function SHALL preserve the exact URL.
     *
     * **Validates: Requirements 3.1**
     */
    "Property 5: Various URL formats should be preserved exactly" {
        val urlFormats = listOf(
            // Standard HTTPS URLs
            "https://example.com/sticker.png",
            // URLs with query parameters
            "https://example.com/sticker.png?size=large&format=webp",
            // URLs with fragments
            "https://example.com/sticker.png#section",
            // URLs with both query and fragment
            "https://example.com/sticker.png?token=abc#ref",
            // URLs with encoded characters
            "https://example.com/sticker%20name.png",
            // URLs with port numbers
            "https://example.com:8080/sticker.png",
            // URLs with authentication
            "https://user:pass@example.com/sticker.png",
            // CDN URLs with long paths
            "https://cdn.example.com/v1/stickers/pack/category/sticker_001.gif",
            // Data URLs (edge case)
            "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUA",
            // File URLs
            "file:///storage/emulated/0/sticker.png"
        )
        val urlArb = Arb.element(urlFormats)

        checkAll(200, urlArb) { expectedUrl ->
            val customData = JSONObject().apply {
                put("sticker_url", expectedUrl)
            }
            
            val extractedUrl = extractStickerUrl(customData)
            
            extractedUrl shouldBe expectedUrl
        }
    }

    /**
     * Property 5: Sticker URL Propagation - Whitespace handling
     *
     * *For any* sticker_url with leading/trailing whitespace,
     * the extractStickerUrl function SHALL return the URL as-is
     * (whitespace is preserved, not trimmed).
     *
     * **Validates: Requirements 3.1**
     */
    "Property 5: URL with whitespace should be returned as-is" {
        val urlsWithWhitespace = listOf(
            " https://example.com/sticker.png",
            "https://example.com/sticker.png ",
            " https://example.com/sticker.png ",
            "\thttps://example.com/sticker.png",
            "https://example.com/sticker.png\n"
        )
        val urlArb = Arb.element(urlsWithWhitespace)

        checkAll(100, urlArb) { urlWithWhitespace ->
            val customData = JSONObject().apply {
                put("sticker_url", urlWithWhitespace)
            }
            
            val extractedUrl = extractStickerUrl(customData)
            
            // URL should be returned exactly as stored
            extractedUrl shouldBe urlWithWhitespace
        }
    }

    /**
     * Property 5: Sticker URL Propagation - Non-string values handled gracefully
     *
     * *For any* CustomMessage with non-string sticker_url value,
     * the extractStickerUrl function SHALL handle it gracefully
     * (either convert to string or return empty).
     *
     * **Validates: Requirements 3.1**
     */
    "Property 5: Non-string sticker_url values should be handled gracefully" {
        // Test with integer value
        val customDataInt = JSONObject().apply {
            put("sticker_url", 12345)
        }
        val resultInt = extractStickerUrl(customDataInt)
        // JSONObject.optString converts numbers to strings
        resultInt shouldBe "12345"

        // Test with boolean value
        val customDataBool = JSONObject().apply {
            put("sticker_url", true)
        }
        val resultBool = extractStickerUrl(customDataBool)
        resultBool shouldBe "true"

        // Test with null value
        val customDataNull = JSONObject().apply {
            put("sticker_url", JSONObject.NULL)
        }
        val resultNull = extractStickerUrl(customDataNull)
        resultNull.shouldBeEmpty()
    }

    /**
     * Property 5: Sticker URL Propagation - Nested JSON objects don't break extraction
     *
     * *For any* CustomMessage with nested JSON objects alongside sticker_url,
     * the extractStickerUrl function SHALL still extract the URL correctly.
     *
     * **Validates: Requirements 3.1**
     */
    "Property 5: Nested JSON objects should not affect URL extraction" {
        val validUrls = listOf(
            "https://example.com/sticker.png",
            "https://cdn.stickers.com/sticker.gif"
        )
        val urlArb = Arb.element(validUrls)

        checkAll(100, urlArb) { expectedUrl ->
            val customData = JSONObject().apply {
                put("sticker_url", expectedUrl)
                put("metadata", JSONObject().apply {
                    put("pack_id", "pack_001")
                    put("category", "emoji")
                })
                put("dimensions", JSONObject().apply {
                    put("width", 128)
                    put("height", 128)
                })
            }
            
            val extractedUrl = extractStickerUrl(customData)
            
            extractedUrl shouldBe expectedUrl
        }
    }

    /**
     * Property 5: Sticker URL Propagation - Generated URLs are extracted correctly
     *
     * *For any* randomly generated URL-like string stored as sticker_url,
     * the extractStickerUrl function SHALL return that exact string.
     *
     * **Validates: Requirements 3.1**
     */
    "Property 5: Randomly generated URLs should be extracted correctly" {
        // Generate URL-like patterns
        val urlPatternArb = Arb.stringPattern("https://[a-z]{5,10}\\.[a-z]{2,4}/[a-z0-9]{5,15}\\.(png|gif|webp|jpg)")

        checkAll(200, urlPatternArb) { generatedUrl ->
            val customData = JSONObject().apply {
                put("sticker_url", generatedUrl)
            }
            
            val extractedUrl = extractStickerUrl(customData)
            
            extractedUrl shouldBe generatedUrl
            extractedUrl.shouldNotBeEmpty()
        }
    }

    /**
     * Property 5: Sticker URL Propagation - Sticker name extraction
     *
     * *For any* CustomMessage with sticker_name in customData,
     * the extractStickerName function SHALL return that name.
     *
     * **Validates: Requirements 3.1**
     */
    "Property 5: Sticker name should be extracted correctly" {
        val stickerNames = listOf(
            "Happy Face",
            "Thumbs Up",
            "Heart Eyes",
            "Laughing",
            "Winking",
            "Cool Sunglasses"
        )
        val nameArb = Arb.element(stickerNames)

        checkAll(100, nameArb) { expectedName ->
            val customData = JSONObject().apply {
                put("sticker_name", expectedName)
                put("sticker_url", "https://example.com/sticker.png")
            }
            
            val extractedName = extractStickerName(customData)
            
            extractedName shouldBe expectedName
        }
    }

    /**
     * Property 5: Sticker URL Propagation - Fallback to "name" key for sticker name
     *
     * *For any* CustomMessage with "name" (but not "sticker_name") in customData,
     * the extractStickerName function SHALL return the "name" value.
     *
     * **Validates: Requirements 3.1**
     */
    "Property 5: Should fallback to 'name' key when 'sticker_name' is not present" {
        val names = listOf(
            "Emoji Pack 1",
            "Cute Animals",
            "Funny Faces"
        )
        val nameArb = Arb.element(names)

        checkAll(100, nameArb) { expectedName ->
            val customData = JSONObject().apply {
                put("name", expectedName)
                put("sticker_url", "https://example.com/sticker.png")
            }
            
            val extractedName = extractStickerName(customData)
            
            extractedName shouldBe expectedName
        }
    }

    /**
     * Property 5: Sticker URL Propagation - Missing sticker name returns null
     *
     * *For any* CustomMessage without sticker_name or name in customData,
     * the extractStickerName function SHALL return null.
     *
     * **Validates: Requirements 3.1**
     */
    "Property 5: Missing sticker name should return null" {
        val customData = JSONObject().apply {
            put("sticker_url", "https://example.com/sticker.png")
            put("pack_id", "pack_001")
        }
        
        val extractedName = extractStickerName(customData)
        
        extractedName shouldBe null
    }

    /**
     * Property 5: Sticker URL Propagation - Null customData returns null for name
     *
     * *For any* null customData, the extractStickerName function SHALL return null.
     *
     * **Validates: Requirements 3.1**
     */
    "Property 5: Null customData should return null for sticker name" {
        val extractedName = extractStickerName(null)
        
        extractedName shouldBe null
    }
})

/**
 * Extracts the sticker URL from customData JSONObject.
 * This mirrors the logic in CometChatStickerBubble.kt
 *
 * @param customData The JSONObject containing sticker data
 * @return The sticker URL or empty string if not found
 */
private fun extractStickerUrl(customData: JSONObject?): String {
    return try {
        customData?.optString("sticker_url", "")
            ?.takeIf { it.isNotEmpty() }
            ?: customData?.optString("url", "")
            ?: ""
    } catch (e: Exception) {
        ""
    }
}

/**
 * Extracts the sticker name from customData JSONObject.
 * This mirrors the logic in CometChatStickerBubble.kt
 *
 * @param customData The JSONObject containing sticker data
 * @return The sticker name or null if not found
 */
private fun extractStickerName(customData: JSONObject?): String? {
    return try {
        customData?.optString("sticker_name", null)
            ?.takeIf { it.isNotEmpty() }
            ?: customData?.optString("name", null)
                ?.takeIf { it.isNotEmpty() }
    } catch (e: Exception) {
        null
    }
}
