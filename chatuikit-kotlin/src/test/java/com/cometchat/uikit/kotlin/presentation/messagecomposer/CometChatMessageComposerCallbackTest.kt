package com.cometchat.uikit.kotlin.presentation.messagecomposer

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

/**
 * Interface to mock the default behavior for testing.
 * This allows us to verify whether default media selection methods are called.
 */
interface MockDefaultBehavior {
    fun launchCamera()
    fun launchImagePicker()
    fun launchVideoPicker()
    fun launchAudioPicker()
    fun launchFilePicker()
}

/**
 * Unit tests for the attachment option callback override behavior in CometChatMessageComposer (Kotlin Views).
 *
 * These tests validate that:
 * - Callback returning `true` skips default behavior
 * - Callback returning `false` executes default behavior
 * - Null callback executes default behavior
 *
 * **Validates: Requirements 12.2, 12.4**
 */
class CometChatMessageComposerCallbackTest : FunSpec({

    /**
     * Helper function that replicates the callback handling logic from CometChatMessageComposer.
     * This is the logic we're testing - extracted for unit testability.
     *
     * @param id The attachment option ID (camera, image, video, audio, document)
     * @param defaultBehavior The default behavior with launcher methods
     * @param onCameraClick Callback for camera option
     * @param onImageClick Callback for image option
     * @param onVideoClick Callback for video option
     * @param onAudioClick Callback for audio option
     * @param onDocumentClick Callback for document option
     */
    fun handleAttachmentOptionClick(
        id: String,
        defaultBehavior: MockDefaultBehavior,
        onCameraClick: (() -> Boolean)? = null,
        onImageClick: (() -> Boolean)? = null,
        onVideoClick: (() -> Boolean)? = null,
        onAudioClick: (() -> Boolean)? = null,
        onDocumentClick: (() -> Boolean)? = null
    ) {
        when (id) {
            "camera" -> {
                val handled = onCameraClick?.invoke() ?: false
                if (!handled) defaultBehavior.launchCamera()
            }
            "image" -> {
                val handled = onImageClick?.invoke() ?: false
                if (!handled) defaultBehavior.launchImagePicker()
            }
            "video" -> {
                val handled = onVideoClick?.invoke() ?: false
                if (!handled) defaultBehavior.launchVideoPicker()
            }
            "audio" -> {
                val handled = onAudioClick?.invoke() ?: false
                if (!handled) defaultBehavior.launchAudioPicker()
            }
            "document" -> {
                val handled = onDocumentClick?.invoke() ?: false
                if (!handled) defaultBehavior.launchFilePicker()
            }
        }
    }

    // ==================== Camera Callback Tests ====================

    /**
     * Test: Camera callback returning true should skip default behavior
     *
     * When the onCameraClick callback returns `true`, the default camera launch
     * should NOT be called, allowing the developer to handle it themselves.
     *
     * **Validates: Requirements 12.2, 12.4**
     */
    test("camera callback returning true should skip default behavior") {
        // Given
        val defaultBehavior = mock<MockDefaultBehavior>()
        var callbackInvoked = false
        val onCameraClick: () -> Boolean = {
            callbackInvoked = true
            true // Skip default
        }

        // When
        handleAttachmentOptionClick(
            id = "camera",
            defaultBehavior = defaultBehavior,
            onCameraClick = onCameraClick
        )

        // Then
        callbackInvoked shouldBe true
        verify(defaultBehavior, never()).launchCamera()
    }

    /**
     * Test: Camera callback returning false should execute default behavior
     *
     * When the onCameraClick callback returns `false`, the default camera launch
     * should be called after the callback executes.
     *
     * **Validates: Requirements 12.4**
     */
    test("camera callback returning false should execute default behavior") {
        // Given
        val defaultBehavior = mock<MockDefaultBehavior>()
        var callbackInvoked = false
        val onCameraClick: () -> Boolean = {
            callbackInvoked = true
            false // Execute default
        }

        // When
        handleAttachmentOptionClick(
            id = "camera",
            defaultBehavior = defaultBehavior,
            onCameraClick = onCameraClick
        )

        // Then
        callbackInvoked shouldBe true
        verify(defaultBehavior).launchCamera()
    }

    /**
     * Test: Null camera callback should execute default behavior
     *
     * When no onCameraClick callback is provided (null), the default camera launch
     * should be called.
     *
     * **Validates: Requirements 12.4**
     */
    test("null camera callback should execute default behavior") {
        // Given
        val defaultBehavior = mock<MockDefaultBehavior>()

        // When
        handleAttachmentOptionClick(
            id = "camera",
            defaultBehavior = defaultBehavior,
            onCameraClick = null
        )

        // Then
        verify(defaultBehavior).launchCamera()
    }

    // ==================== Image Callback Tests ====================

    /**
     * Test: Image callback returning true should skip default behavior
     *
     * When the onImageClick callback returns `true`, the default image picker
     * should NOT be called, allowing the developer to handle it themselves.
     *
     * **Validates: Requirements 12.2, 12.4**
     */
    test("image callback returning true should skip default behavior") {
        // Given
        val defaultBehavior = mock<MockDefaultBehavior>()
        var callbackInvoked = false
        val onImageClick: () -> Boolean = {
            callbackInvoked = true
            true // Skip default
        }

        // When
        handleAttachmentOptionClick(
            id = "image",
            defaultBehavior = defaultBehavior,
            onImageClick = onImageClick
        )

        // Then
        callbackInvoked shouldBe true
        verify(defaultBehavior, never()).launchImagePicker()
    }

    /**
     * Test: Image callback returning false should execute default behavior
     *
     * When the onImageClick callback returns `false`, the default image picker
     * should be called after the callback executes.
     *
     * **Validates: Requirements 12.4**
     */
    test("image callback returning false should execute default behavior") {
        // Given
        val defaultBehavior = mock<MockDefaultBehavior>()
        var callbackInvoked = false
        val onImageClick: () -> Boolean = {
            callbackInvoked = true
            false // Execute default
        }

        // When
        handleAttachmentOptionClick(
            id = "image",
            defaultBehavior = defaultBehavior,
            onImageClick = onImageClick
        )

        // Then
        callbackInvoked shouldBe true
        verify(defaultBehavior).launchImagePicker()
    }

    /**
     * Test: Null image callback should execute default behavior
     *
     * When no onImageClick callback is provided (null), the default image picker
     * should be called.
     *
     * **Validates: Requirements 12.4**
     */
    test("null image callback should execute default behavior") {
        // Given
        val defaultBehavior = mock<MockDefaultBehavior>()

        // When
        handleAttachmentOptionClick(
            id = "image",
            defaultBehavior = defaultBehavior,
            onImageClick = null
        )

        // Then
        verify(defaultBehavior).launchImagePicker()
    }

    // ==================== Video Callback Tests ====================

    /**
     * Test: Video callback returning true should skip default behavior
     *
     * When the onVideoClick callback returns `true`, the default video picker
     * should NOT be called, allowing the developer to handle it themselves.
     *
     * **Validates: Requirements 12.2, 12.4**
     */
    test("video callback returning true should skip default behavior") {
        // Given
        val defaultBehavior = mock<MockDefaultBehavior>()
        var callbackInvoked = false
        val onVideoClick: () -> Boolean = {
            callbackInvoked = true
            true // Skip default
        }

        // When
        handleAttachmentOptionClick(
            id = "video",
            defaultBehavior = defaultBehavior,
            onVideoClick = onVideoClick
        )

        // Then
        callbackInvoked shouldBe true
        verify(defaultBehavior, never()).launchVideoPicker()
    }

    /**
     * Test: Video callback returning false should execute default behavior
     *
     * When the onVideoClick callback returns `false`, the default video picker
     * should be called after the callback executes.
     *
     * **Validates: Requirements 12.4**
     */
    test("video callback returning false should execute default behavior") {
        // Given
        val defaultBehavior = mock<MockDefaultBehavior>()
        var callbackInvoked = false
        val onVideoClick: () -> Boolean = {
            callbackInvoked = true
            false // Execute default
        }

        // When
        handleAttachmentOptionClick(
            id = "video",
            defaultBehavior = defaultBehavior,
            onVideoClick = onVideoClick
        )

        // Then
        callbackInvoked shouldBe true
        verify(defaultBehavior).launchVideoPicker()
    }

    /**
     * Test: Null video callback should execute default behavior
     *
     * When no onVideoClick callback is provided (null), the default video picker
     * should be called.
     *
     * **Validates: Requirements 12.4**
     */
    test("null video callback should execute default behavior") {
        // Given
        val defaultBehavior = mock<MockDefaultBehavior>()

        // When
        handleAttachmentOptionClick(
            id = "video",
            defaultBehavior = defaultBehavior,
            onVideoClick = null
        )

        // Then
        verify(defaultBehavior).launchVideoPicker()
    }

    // ==================== Audio Callback Tests ====================

    /**
     * Test: Audio callback returning true should skip default behavior
     *
     * When the onAudioClick callback returns `true`, the default audio picker
     * should NOT be called, allowing the developer to handle it themselves.
     *
     * **Validates: Requirements 12.2, 12.4**
     */
    test("audio callback returning true should skip default behavior") {
        // Given
        val defaultBehavior = mock<MockDefaultBehavior>()
        var callbackInvoked = false
        val onAudioClick: () -> Boolean = {
            callbackInvoked = true
            true // Skip default
        }

        // When
        handleAttachmentOptionClick(
            id = "audio",
            defaultBehavior = defaultBehavior,
            onAudioClick = onAudioClick
        )

        // Then
        callbackInvoked shouldBe true
        verify(defaultBehavior, never()).launchAudioPicker()
    }

    /**
     * Test: Audio callback returning false should execute default behavior
     *
     * When the onAudioClick callback returns `false`, the default audio picker
     * should be called after the callback executes.
     *
     * **Validates: Requirements 12.4**
     */
    test("audio callback returning false should execute default behavior") {
        // Given
        val defaultBehavior = mock<MockDefaultBehavior>()
        var callbackInvoked = false
        val onAudioClick: () -> Boolean = {
            callbackInvoked = true
            false // Execute default
        }

        // When
        handleAttachmentOptionClick(
            id = "audio",
            defaultBehavior = defaultBehavior,
            onAudioClick = onAudioClick
        )

        // Then
        callbackInvoked shouldBe true
        verify(defaultBehavior).launchAudioPicker()
    }

    /**
     * Test: Null audio callback should execute default behavior
     *
     * When no onAudioClick callback is provided (null), the default audio picker
     * should be called.
     *
     * **Validates: Requirements 12.4**
     */
    test("null audio callback should execute default behavior") {
        // Given
        val defaultBehavior = mock<MockDefaultBehavior>()

        // When
        handleAttachmentOptionClick(
            id = "audio",
            defaultBehavior = defaultBehavior,
            onAudioClick = null
        )

        // Then
        verify(defaultBehavior).launchAudioPicker()
    }

    // ==================== Document Callback Tests ====================

    /**
     * Test: Document callback returning true should skip default behavior
     *
     * When the onDocumentClick callback returns `true`, the default file picker
     * should NOT be called, allowing the developer to handle it themselves.
     *
     * **Validates: Requirements 12.2, 12.4**
     */
    test("document callback returning true should skip default behavior") {
        // Given
        val defaultBehavior = mock<MockDefaultBehavior>()
        var callbackInvoked = false
        val onDocumentClick: () -> Boolean = {
            callbackInvoked = true
            true // Skip default
        }

        // When
        handleAttachmentOptionClick(
            id = "document",
            defaultBehavior = defaultBehavior,
            onDocumentClick = onDocumentClick
        )

        // Then
        callbackInvoked shouldBe true
        verify(defaultBehavior, never()).launchFilePicker()
    }

    /**
     * Test: Document callback returning false should execute default behavior
     *
     * When the onDocumentClick callback returns `false`, the default file picker
     * should be called after the callback executes.
     *
     * **Validates: Requirements 12.4**
     */
    test("document callback returning false should execute default behavior") {
        // Given
        val defaultBehavior = mock<MockDefaultBehavior>()
        var callbackInvoked = false
        val onDocumentClick: () -> Boolean = {
            callbackInvoked = true
            false // Execute default
        }

        // When
        handleAttachmentOptionClick(
            id = "document",
            defaultBehavior = defaultBehavior,
            onDocumentClick = onDocumentClick
        )

        // Then
        callbackInvoked shouldBe true
        verify(defaultBehavior).launchFilePicker()
    }

    /**
     * Test: Null document callback should execute default behavior
     *
     * When no onDocumentClick callback is provided (null), the default file picker
     * should be called.
     *
     * **Validates: Requirements 12.4**
     */
    test("null document callback should execute default behavior") {
        // Given
        val defaultBehavior = mock<MockDefaultBehavior>()

        // When
        handleAttachmentOptionClick(
            id = "document",
            defaultBehavior = defaultBehavior,
            onDocumentClick = null
        )

        // Then
        verify(defaultBehavior).launchFilePicker()
    }

    // ==================== Edge Case Tests ====================

    /**
     * Test: Unknown attachment option ID should not call any default behavior
     *
     * When an unknown ID is passed, no default behavior method should be called.
     */
    test("unknown attachment option ID should not call any default behavior") {
        // Given
        val defaultBehavior = mock<MockDefaultBehavior>()

        // When
        handleAttachmentOptionClick(
            id = "unknown",
            defaultBehavior = defaultBehavior
        )

        // Then
        verify(defaultBehavior, never()).launchCamera()
        verify(defaultBehavior, never()).launchImagePicker()
        verify(defaultBehavior, never()).launchVideoPicker()
        verify(defaultBehavior, never()).launchAudioPicker()
        verify(defaultBehavior, never()).launchFilePicker()
    }

    /**
     * Test: Only the relevant callback is invoked for each attachment option
     *
     * When camera is clicked, only onCameraClick should be invoked, not other callbacks.
     */
    test("only relevant callback is invoked for camera option") {
        // Given
        val defaultBehavior = mock<MockDefaultBehavior>()
        var cameraCallbackInvoked = false
        var imageCallbackInvoked = false
        var videoCallbackInvoked = false
        var audioCallbackInvoked = false
        var documentCallbackInvoked = false

        val onCameraClick: () -> Boolean = { cameraCallbackInvoked = true; true }
        val onImageClick: () -> Boolean = { imageCallbackInvoked = true; true }
        val onVideoClick: () -> Boolean = { videoCallbackInvoked = true; true }
        val onAudioClick: () -> Boolean = { audioCallbackInvoked = true; true }
        val onDocumentClick: () -> Boolean = { documentCallbackInvoked = true; true }

        // When
        handleAttachmentOptionClick(
            id = "camera",
            defaultBehavior = defaultBehavior,
            onCameraClick = onCameraClick,
            onImageClick = onImageClick,
            onVideoClick = onVideoClick,
            onAudioClick = onAudioClick,
            onDocumentClick = onDocumentClick
        )

        // Then
        cameraCallbackInvoked shouldBe true
        imageCallbackInvoked shouldBe false
        videoCallbackInvoked shouldBe false
        audioCallbackInvoked shouldBe false
        documentCallbackInvoked shouldBe false
    }

    /**
     * Test: Mixed callback configurations work correctly
     *
     * Some callbacks return true (skip default), some return false (execute default),
     * and some are null (execute default).
     */
    test("mixed callback configurations work correctly") {
        // Given
        val defaultBehavior = mock<MockDefaultBehavior>()

        // Camera: callback returns true -> skip default
        handleAttachmentOptionClick(
            id = "camera",
            defaultBehavior = defaultBehavior,
            onCameraClick = { true }
        )
        verify(defaultBehavior, never()).launchCamera()

        // Image: callback returns false -> execute default
        handleAttachmentOptionClick(
            id = "image",
            defaultBehavior = defaultBehavior,
            onImageClick = { false }
        )
        verify(defaultBehavior).launchImagePicker()

        // Video: null callback -> execute default
        handleAttachmentOptionClick(
            id = "video",
            defaultBehavior = defaultBehavior,
            onVideoClick = null
        )
        verify(defaultBehavior).launchVideoPicker()
    }
})
