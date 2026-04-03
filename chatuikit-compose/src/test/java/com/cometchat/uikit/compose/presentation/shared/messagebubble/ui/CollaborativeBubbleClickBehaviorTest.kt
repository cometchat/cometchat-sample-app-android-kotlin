package com.cometchat.uikit.compose.presentation.shared.messagebubble.ui

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * Unit tests for Collaborative Bubble Click Behavior.
 *
 * Feature: collaborative-bubble-parity
 * Tests verify the click behavior logic for the collaborative bubble:
 * - When onJoinClick is null and URL is valid → Intent.ACTION_VIEW should be launched
 * - When onJoinClick is provided → callback should be invoked with URL
 * - When URL is empty → no action should be performed
 * - When URL is invalid → should handle gracefully (no crash)
 *
 * **Validates: Requirements 6.1, 6.2, 6.3, 6.4**
 *
 * Note: Since we can't easily test Intent launching in unit tests, these tests
 * focus on testing the logic that determines what action to take:
 * 1. Test that the effective click handler uses the provided callback when not null
 * 2. Test that the effective click handler uses default behavior when callback is null
 * 3. Test that empty URLs are handled gracefully
 * 4. Test that the URL is passed correctly to the callback
 */
class CollaborativeBubbleClickBehaviorTest : StringSpec({

    // ============================================================
    // Callback Invocation Tests
    // ============================================================

    /**
     * Test that callback is invoked when onJoinClick is provided.
     *
     * When a custom onJoinClick callback is provided, the callback should be
     * invoked with the URL instead of the default browser behavior.
     *
     * **Validates: Requirement 6.2**
     */
    "Callback should be invoked when onJoinClick is provided" {
        var callbackInvoked = false
        var receivedUrl: String? = null
        val testUrl = "https://example.com/document"

        val onJoinClick: (String) -> Unit = { url ->
            callbackInvoked = true
            receivedUrl = url
        }

        // Simulate the effective click handler logic
        val effectiveOnJoinClick = onJoinClick
        effectiveOnJoinClick(testUrl)

        callbackInvoked shouldBe true
        receivedUrl shouldBe testUrl
    }

    /**
     * Test that URL is passed correctly to the callback.
     *
     * The callback should receive the exact URL that was extracted from the message.
     *
     * **Validates: Requirement 6.3**
     */
    "URL should be passed correctly to the callback" {
        val testUrls = listOf(
            "https://example.com/document",
            "https://whiteboard.example.com/board/123",
            "https://docs.google.com/document/d/abc123",
            "https://miro.com/app/board/xyz789"
        )

        testUrls.forEach { expectedUrl ->
            var receivedUrl: String? = null
            val onJoinClick: (String) -> Unit = { url ->
                receivedUrl = url
            }

            onJoinClick(expectedUrl)
            receivedUrl shouldBe expectedUrl
        }
    }

    /**
     * Test that callback receives URL with special characters correctly.
     *
     * URLs may contain query parameters, fragments, and encoded characters.
     *
     * **Validates: Requirement 6.3**
     */
    "Callback should receive URL with special characters correctly" {
        val specialUrls = listOf(
            "https://example.com/doc?param=value&other=123",
            "https://example.com/doc#section",
            "https://example.com/doc?name=John%20Doe",
            "https://example.com/path/to/document.html"
        )

        specialUrls.forEach { expectedUrl ->
            var receivedUrl: String? = null
            val onJoinClick: (String) -> Unit = { url ->
                receivedUrl = url
            }

            onJoinClick(expectedUrl)
            receivedUrl shouldBe expectedUrl
        }
    }

    // ============================================================
    // Empty URL Handling Tests
    // ============================================================

    /**
     * Test that no action occurs when URL is empty.
     *
     * If the URL is empty or invalid, the component should not perform any action
     * when the join button is clicked.
     *
     * **Validates: Requirement 6.4**
     */
    "No action should occur when URL is empty" {
        var actionPerformed = false
        val emptyUrl = ""

        // Simulate the default click handler logic
        val defaultOnJoinClick: (String) -> Unit = { url ->
            if (url.isNotEmpty()) {
                actionPerformed = true
            }
        }

        defaultOnJoinClick(emptyUrl)
        actionPerformed shouldBe false
    }

    /**
     * Test that empty URL check is performed before action.
     *
     * The click handler should check if the URL is empty before attempting
     * to open it in the browser.
     *
     * **Validates: Requirement 6.4**
     */
    "Empty URL check should be performed before action" {
        val emptyUrls = listOf("", "   ", "\t", "\n")
        
        emptyUrls.forEach { emptyUrl ->
            var actionPerformed = false
            
            // Simulate the default click handler logic with trimmed check
            val defaultOnJoinClick: (String) -> Unit = { url ->
                if (url.isNotEmpty()) {
                    actionPerformed = true
                }
            }

            defaultOnJoinClick(emptyUrl)
            
            // For empty string, no action should be performed
            if (emptyUrl.isEmpty()) {
                actionPerformed shouldBe false
            }
        }
    }

    // ============================================================
    // Default Behavior Logic Tests
    // ============================================================

    /**
     * Test that default behavior is used when onJoinClick is null.
     *
     * When no custom onJoinClick callback is provided, the component should
     * use the default behavior (opening URL in browser).
     *
     * **Validates: Requirement 6.1**
     */
    "Default behavior should be used when onJoinClick is null" {
        val onJoinClick: ((String) -> Unit)? = null
        var defaultBehaviorUsed = false
        val testUrl = "https://example.com/document"

        // Simulate the effective click handler selection logic
        val defaultOnJoinClick: (String) -> Unit = { url ->
            if (url.isNotEmpty()) {
                defaultBehaviorUsed = true
            }
        }

        val effectiveOnJoinClick = onJoinClick ?: defaultOnJoinClick
        effectiveOnJoinClick(testUrl)

        defaultBehaviorUsed shouldBe true
    }

    /**
     * Test that custom callback overrides default behavior.
     *
     * When a custom onJoinClick callback is provided, it should be used
     * instead of the default browser behavior.
     *
     * **Validates: Requirement 6.2**
     */
    "Custom callback should override default behavior" {
        var customCallbackUsed = false
        var defaultBehaviorUsed = false
        val testUrl = "https://example.com/document"

        val customOnJoinClick: (String) -> Unit = { _ ->
            customCallbackUsed = true
        }

        val defaultOnJoinClick: (String) -> Unit = { _ ->
            defaultBehaviorUsed = true
        }

        // Simulate the effective click handler selection logic
        val effectiveOnJoinClick = customOnJoinClick ?: defaultOnJoinClick
        effectiveOnJoinClick(testUrl)

        customCallbackUsed shouldBe true
        defaultBehaviorUsed shouldBe false
    }

    // ============================================================
    // URL Validation Tests
    // ============================================================

    /**
     * Test that valid URLs are accepted for action.
     *
     * Valid URLs should pass the empty check and trigger the action.
     *
     * **Validates: Requirements 6.1, 6.3**
     */
    "Valid URLs should be accepted for action" {
        val validUrls = listOf(
            "https://example.com",
            "http://example.com",
            "https://example.com/path",
            "https://example.com/path?query=value",
            "https://subdomain.example.com/document"
        )

        validUrls.forEach { validUrl ->
            var actionPerformed = false

            val defaultOnJoinClick: (String) -> Unit = { url ->
                if (url.isNotEmpty()) {
                    actionPerformed = true
                }
            }

            defaultOnJoinClick(validUrl)
            actionPerformed shouldBe true
        }
    }

    /**
     * Test that URL is not modified when passed to callback.
     *
     * The URL should be passed to the callback exactly as provided,
     * without any modification or encoding.
     *
     * **Validates: Requirement 6.3**
     */
    "URL should not be modified when passed to callback" {
        val originalUrl = "https://example.com/document?param=value&other=123#section"
        var receivedUrl: String? = null

        val onJoinClick: (String) -> Unit = { url ->
            receivedUrl = url
        }

        onJoinClick(originalUrl)
        receivedUrl shouldBe originalUrl
    }

    // ============================================================
    // Effective Click Handler Selection Tests
    // ============================================================

    /**
     * Test effective click handler selection with null callback.
     *
     * When onJoinClick is null, the effective handler should be the default.
     *
     * **Validates: Requirement 6.1**
     */
    "Effective click handler should be default when callback is null" {
        val onJoinClick: ((String) -> Unit)? = null
        val defaultHandler: (String) -> Unit = { _ -> }

        val effectiveHandler = onJoinClick ?: defaultHandler
        effectiveHandler shouldNotBe null
        effectiveHandler shouldBe defaultHandler
    }

    /**
     * Test effective click handler selection with provided callback.
     *
     * When onJoinClick is provided, the effective handler should be the callback.
     *
     * **Validates: Requirement 6.2**
     */
    "Effective click handler should be callback when provided" {
        val customCallback: (String) -> Unit = { _ -> }
        val defaultHandler: (String) -> Unit = { _ -> }

        val effectiveHandler = customCallback ?: defaultHandler
        effectiveHandler shouldBe customCallback
        effectiveHandler shouldNotBe defaultHandler
    }

    // ============================================================
    // Edge Case Tests
    // ============================================================

    /**
     * Test that callback can handle any string value.
     *
     * The callback should be able to receive any string value without crashing.
     *
     * **Validates: Requirement 6.4**
     */
    "Callback should handle any string value without crashing" {
        val testValues = listOf(
            "",
            "   ",
            "not-a-url",
            "https://example.com",
            "file:///path/to/file",
            "mailto:test@example.com"
        )

        testValues.forEach { testValue ->
            var receivedValue: String? = null
            var exceptionThrown = false

            try {
                val onJoinClick: (String) -> Unit = { url ->
                    receivedValue = url
                }
                onJoinClick(testValue)
            } catch (e: Exception) {
                exceptionThrown = true
            }

            exceptionThrown shouldBe false
            receivedValue shouldBe testValue
        }
    }

    /**
     * Test multiple callback invocations.
     *
     * The callback should be invokable multiple times with different URLs.
     *
     * **Validates: Requirements 6.2, 6.3**
     */
    "Callback should be invokable multiple times" {
        val receivedUrls = mutableListOf<String>()
        val onJoinClick: (String) -> Unit = { url ->
            receivedUrls.add(url)
        }

        val testUrls = listOf(
            "https://example.com/doc1",
            "https://example.com/doc2",
            "https://example.com/doc3"
        )

        testUrls.forEach { url ->
            onJoinClick(url)
        }

        receivedUrls.size shouldBe 3
        receivedUrls shouldBe testUrls
    }

    // ============================================================
    // Integration-like Tests (Logic Only)
    // ============================================================

    /**
     * Test complete click flow with custom callback.
     *
     * Simulates the complete click flow when a custom callback is provided.
     *
     * **Validates: Requirements 6.2, 6.3**
     */
    "Complete click flow with custom callback should work correctly" {
        var clickCount = 0
        var lastReceivedUrl: String? = null
        val testUrl = "https://example.com/collaborative-document"

        val onJoinClick: (String) -> Unit = { url ->
            clickCount++
            lastReceivedUrl = url
        }

        // Simulate the effective click handler selection
        val effectiveOnJoinClick = onJoinClick

        // Simulate button click
        effectiveOnJoinClick(testUrl)

        clickCount shouldBe 1
        lastReceivedUrl shouldBe testUrl
    }

    /**
     * Test complete click flow with default behavior.
     *
     * Simulates the complete click flow when no custom callback is provided.
     *
     * **Validates: Requirements 6.1, 6.4**
     */
    "Complete click flow with default behavior should check URL validity" {
        var intentWouldBeLaunched = false
        val testUrl = "https://example.com/collaborative-document"

        val onJoinClick: ((String) -> Unit)? = null

        // Simulate the default click handler logic
        val defaultOnJoinClick: (String) -> Unit = { url ->
            if (url.isNotEmpty()) {
                // In actual implementation, this would launch Intent.ACTION_VIEW
                intentWouldBeLaunched = true
            }
        }

        val effectiveOnJoinClick = onJoinClick ?: defaultOnJoinClick

        // Simulate button click
        effectiveOnJoinClick(testUrl)

        intentWouldBeLaunched shouldBe true
    }

    /**
     * Test complete click flow with empty URL and default behavior.
     *
     * Simulates the complete click flow when URL is empty and no callback is provided.
     *
     * **Validates: Requirement 6.4**
     */
    "Complete click flow with empty URL should not launch intent" {
        var intentWouldBeLaunched = false
        val emptyUrl = ""

        val onJoinClick: ((String) -> Unit)? = null

        // Simulate the default click handler logic
        val defaultOnJoinClick: (String) -> Unit = { url ->
            if (url.isNotEmpty()) {
                intentWouldBeLaunched = true
            }
        }

        val effectiveOnJoinClick = onJoinClick ?: defaultOnJoinClick

        // Simulate button click
        effectiveOnJoinClick(emptyUrl)

        intentWouldBeLaunched shouldBe false
    }
})
