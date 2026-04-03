package com.cometchat.uikit.compose.presentation.shared.messagebubble.ui

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import kotlin.math.abs
import kotlin.math.min

/**
 * Property-based tests for Sticker Aspect Ratio Preservation.
 *
 * Feature: remaining-message-bubbles
 * Properties tested:
 * - Property 6: Sticker Aspect Ratio Preservation
 *
 * **Validates: Requirements 3.2**
 *
 * Tests the sticker aspect ratio preservation logic:
 * - For all sticker images with dimensions (width, height), the displayed aspect ratio
 *   width/height is preserved within a tolerance of 0.01 (1%)
 * - Maximum display size is constrained to 200dp x 200dp
 *
 * The aspect ratio preservation follows these rules:
 * 1. Calculate the original aspect ratio (width / height)
 * 2. Scale down to fit within max dimensions while preserving aspect ratio
 * 3. The displayed aspect ratio should match the original within 1% tolerance
 */
class StickerBubbleAspectRatioPropertyTest : StringSpec({

    /**
     * Property 6: Sticker Aspect Ratio Preservation - Square images
     *
     * *For any* square sticker image (width == height), the displayed dimensions
     * SHALL maintain a 1:1 aspect ratio.
     *
     * **Validates: Requirements 3.2**
     */
    "Property 6: Square images should maintain 1:1 aspect ratio" {
        val sizeArb = Arb.int(1..2000)

        checkAll(200, sizeArb) { size ->
            val originalWidth = size
            val originalHeight = size
            val originalAspectRatio = originalWidth.toDouble() / originalHeight.toDouble()

            val (displayWidth, displayHeight) = calculateDisplayDimensions(
                originalWidth = originalWidth,
                originalHeight = originalHeight,
                maxWidth = MAX_STICKER_SIZE,
                maxHeight = MAX_STICKER_SIZE
            )

            val displayAspectRatio = displayWidth / displayHeight
            val aspectRatioDifference = abs(originalAspectRatio - displayAspectRatio)

            // Aspect ratio should be preserved within 1% tolerance
            aspectRatioDifference shouldBeLessThanOrEqual ASPECT_RATIO_TOLERANCE

            // For square images, aspect ratio should be exactly 1.0
            displayAspectRatio shouldBe 1.0
        }
    }

    /**
     * Property 6: Sticker Aspect Ratio Preservation - Landscape images
     *
     * *For any* landscape sticker image (width > height), the displayed aspect ratio
     * SHALL be preserved within 1% tolerance.
     *
     * **Validates: Requirements 3.2**
     */
    "Property 6: Landscape images should preserve aspect ratio within tolerance" {
        val widthArb = Arb.int(100..2000)
        val heightArb = Arb.int(50..999)

        checkAll(200, widthArb, heightArb) { width, height ->
            // Ensure landscape orientation (width > height)
            val originalWidth = maxOf(width, height + 1)
            val originalHeight = minOf(width, height)
            val originalAspectRatio = originalWidth.toDouble() / originalHeight.toDouble()

            val (displayWidth, displayHeight) = calculateDisplayDimensions(
                originalWidth = originalWidth,
                originalHeight = originalHeight,
                maxWidth = MAX_STICKER_SIZE,
                maxHeight = MAX_STICKER_SIZE
            )

            val displayAspectRatio = displayWidth / displayHeight
            val aspectRatioDifference = abs(originalAspectRatio - displayAspectRatio)

            // Aspect ratio should be preserved within 1% tolerance
            aspectRatioDifference shouldBeLessThanOrEqual ASPECT_RATIO_TOLERANCE
        }
    }

    /**
     * Property 6: Sticker Aspect Ratio Preservation - Portrait images
     *
     * *For any* portrait sticker image (height > width), the displayed aspect ratio
     * SHALL be preserved within 1% tolerance.
     *
     * **Validates: Requirements 3.2**
     */
    "Property 6: Portrait images should preserve aspect ratio within tolerance" {
        val widthArb = Arb.int(50..999)
        val heightArb = Arb.int(100..2000)

        checkAll(200, widthArb, heightArb) { width, height ->
            // Ensure portrait orientation (height > width)
            val originalWidth = minOf(width, height - 1)
            val originalHeight = maxOf(width, height)
            val originalAspectRatio = originalWidth.toDouble() / originalHeight.toDouble()

            val (displayWidth, displayHeight) = calculateDisplayDimensions(
                originalWidth = originalWidth,
                originalHeight = originalHeight,
                maxWidth = MAX_STICKER_SIZE,
                maxHeight = MAX_STICKER_SIZE
            )

            val displayAspectRatio = displayWidth / displayHeight
            val aspectRatioDifference = abs(originalAspectRatio - displayAspectRatio)

            // Aspect ratio should be preserved within 1% tolerance
            aspectRatioDifference shouldBeLessThanOrEqual ASPECT_RATIO_TOLERANCE
        }
    }

    /**
     * Property 6: Sticker Aspect Ratio Preservation - Random dimensions
     *
     * *For any* sticker image with random dimensions (width, height),
     * the displayed aspect ratio SHALL be preserved within 1% tolerance.
     *
     * **Validates: Requirements 3.2**
     */
    "Property 6: Random dimensions should preserve aspect ratio within tolerance" {
        val dimensionArb = Arb.int(1..3000)

        checkAll(500, dimensionArb, dimensionArb) { originalWidth, originalHeight ->
            val originalAspectRatio = originalWidth.toDouble() / originalHeight.toDouble()

            val (displayWidth, displayHeight) = calculateDisplayDimensions(
                originalWidth = originalWidth,
                originalHeight = originalHeight,
                maxWidth = MAX_STICKER_SIZE,
                maxHeight = MAX_STICKER_SIZE
            )

            val displayAspectRatio = displayWidth / displayHeight
            val aspectRatioDifference = abs(originalAspectRatio - displayAspectRatio)

            // Aspect ratio should be preserved within 1% tolerance
            aspectRatioDifference shouldBeLessThanOrEqual ASPECT_RATIO_TOLERANCE
        }
    }

    /**
     * Property 6: Sticker Aspect Ratio Preservation - Max size constraint
     *
     * *For any* sticker image dimensions, the displayed dimensions
     * SHALL NOT exceed the maximum size (200dp x 200dp).
     *
     * **Validates: Requirements 3.2**
     */
    "Property 6: Display dimensions should not exceed max size" {
        val dimensionArb = Arb.int(1..5000)

        checkAll(300, dimensionArb, dimensionArb) { originalWidth, originalHeight ->
            val (displayWidth, displayHeight) = calculateDisplayDimensions(
                originalWidth = originalWidth,
                originalHeight = originalHeight,
                maxWidth = MAX_STICKER_SIZE,
                maxHeight = MAX_STICKER_SIZE
            )

            // Display dimensions should not exceed max size
            displayWidth shouldBeLessThanOrEqual MAX_STICKER_SIZE.toDouble()
            displayHeight shouldBeLessThanOrEqual MAX_STICKER_SIZE.toDouble()
        }
    }

    /**
     * Property 6: Sticker Aspect Ratio Preservation - Small images not upscaled
     *
     * *For any* sticker image smaller than max size, the displayed dimensions
     * SHALL NOT be larger than the original dimensions.
     *
     * **Validates: Requirements 3.2**
     */
    "Property 6: Small images should not be upscaled" {
        val smallDimensionArb = Arb.int(1..MAX_STICKER_SIZE)

        checkAll(200, smallDimensionArb, smallDimensionArb) { originalWidth, originalHeight ->
            val (displayWidth, displayHeight) = calculateDisplayDimensions(
                originalWidth = originalWidth,
                originalHeight = originalHeight,
                maxWidth = MAX_STICKER_SIZE,
                maxHeight = MAX_STICKER_SIZE
            )

            // Display dimensions should not exceed original dimensions
            displayWidth shouldBeLessThanOrEqual originalWidth.toDouble()
            displayHeight shouldBeLessThanOrEqual originalHeight.toDouble()
        }
    }

    /**
     * Property 6: Sticker Aspect Ratio Preservation - Large images scaled down
     *
     * *For any* sticker image larger than max size, the displayed dimensions
     * SHALL be scaled down to fit within max size while preserving aspect ratio.
     *
     * **Validates: Requirements 3.2**
     */
    "Property 6: Large images should be scaled down to fit max size" {
        val largeDimensionArb = Arb.int(MAX_STICKER_SIZE + 1..5000)

        checkAll(200, largeDimensionArb, largeDimensionArb) { originalWidth, originalHeight ->
            val originalAspectRatio = originalWidth.toDouble() / originalHeight.toDouble()

            val (displayWidth, displayHeight) = calculateDisplayDimensions(
                originalWidth = originalWidth,
                originalHeight = originalHeight,
                maxWidth = MAX_STICKER_SIZE,
                maxHeight = MAX_STICKER_SIZE
            )

            // At least one dimension should be at max size (fully utilizing available space)
            val atMaxSize = displayWidth == MAX_STICKER_SIZE.toDouble() || 
                           displayHeight == MAX_STICKER_SIZE.toDouble()
            atMaxSize shouldBe true

            // Aspect ratio should still be preserved
            val displayAspectRatio = displayWidth / displayHeight
            val aspectRatioDifference = abs(originalAspectRatio - displayAspectRatio)
            aspectRatioDifference shouldBeLessThanOrEqual ASPECT_RATIO_TOLERANCE
        }
    }

    /**
     * Property 6: Sticker Aspect Ratio Preservation - Extreme aspect ratios
     *
     * *For any* sticker image with extreme aspect ratios (very wide or very tall),
     * the displayed aspect ratio SHALL still be preserved within tolerance.
     *
     * **Validates: Requirements 3.2**
     */
    "Property 6: Extreme aspect ratios should be preserved" {
        // Very wide images (width >> height)
        val wideWidthArb = Arb.int(1000..5000)
        val wideHeightArb = Arb.int(10..100)

        checkAll(100, wideWidthArb, wideHeightArb) { originalWidth, originalHeight ->
            val originalAspectRatio = originalWidth.toDouble() / originalHeight.toDouble()

            val (displayWidth, displayHeight) = calculateDisplayDimensions(
                originalWidth = originalWidth,
                originalHeight = originalHeight,
                maxWidth = MAX_STICKER_SIZE,
                maxHeight = MAX_STICKER_SIZE
            )

            val displayAspectRatio = displayWidth / displayHeight
            val aspectRatioDifference = abs(originalAspectRatio - displayAspectRatio)

            aspectRatioDifference shouldBeLessThanOrEqual ASPECT_RATIO_TOLERANCE
        }

        // Very tall images (height >> width)
        val tallWidthArb = Arb.int(10..100)
        val tallHeightArb = Arb.int(1000..5000)

        checkAll(100, tallWidthArb, tallHeightArb) { originalWidth, originalHeight ->
            val originalAspectRatio = originalWidth.toDouble() / originalHeight.toDouble()

            val (displayWidth, displayHeight) = calculateDisplayDimensions(
                originalWidth = originalWidth,
                originalHeight = originalHeight,
                maxWidth = MAX_STICKER_SIZE,
                maxHeight = MAX_STICKER_SIZE
            )

            val displayAspectRatio = displayWidth / displayHeight
            val aspectRatioDifference = abs(originalAspectRatio - displayAspectRatio)

            aspectRatioDifference shouldBeLessThanOrEqual ASPECT_RATIO_TOLERANCE
        }
    }

    /**
     * Property 6: Sticker Aspect Ratio Preservation - Calculation is deterministic
     *
     * *For any* sticker image dimensions, calling calculateDisplayDimensions
     * multiple times SHALL return the same result.
     *
     * **Validates: Requirements 3.2**
     */
    "Property 6: Aspect ratio calculation should be deterministic" {
        val dimensionArb = Arb.int(1..3000)

        checkAll(200, dimensionArb, dimensionArb) { originalWidth, originalHeight ->
            val result1 = calculateDisplayDimensions(
                originalWidth = originalWidth,
                originalHeight = originalHeight,
                maxWidth = MAX_STICKER_SIZE,
                maxHeight = MAX_STICKER_SIZE
            )
            val result2 = calculateDisplayDimensions(
                originalWidth = originalWidth,
                originalHeight = originalHeight,
                maxWidth = MAX_STICKER_SIZE,
                maxHeight = MAX_STICKER_SIZE
            )
            val result3 = calculateDisplayDimensions(
                originalWidth = originalWidth,
                originalHeight = originalHeight,
                maxWidth = MAX_STICKER_SIZE,
                maxHeight = MAX_STICKER_SIZE
            )

            result1 shouldBe result2
            result2 shouldBe result3
        }
    }

    /**
     * Property 6: Sticker Aspect Ratio Preservation - Positive dimensions
     *
     * *For any* valid input dimensions, the output dimensions SHALL always be positive.
     *
     * **Validates: Requirements 3.2**
     */
    "Property 6: Output dimensions should always be positive" {
        val dimensionArb = Arb.int(1..5000)

        checkAll(300, dimensionArb, dimensionArb) { originalWidth, originalHeight ->
            val (displayWidth, displayHeight) = calculateDisplayDimensions(
                originalWidth = originalWidth,
                originalHeight = originalHeight,
                maxWidth = MAX_STICKER_SIZE,
                maxHeight = MAX_STICKER_SIZE
            )

            // Display dimensions should always be positive
            (displayWidth > 0) shouldBe true
            (displayHeight > 0) shouldBe true
        }
    }

    /**
     * Property 6: Sticker Aspect Ratio Preservation - Floating point precision
     *
     * *For any* sticker image dimensions with various aspect ratios,
     * the aspect ratio calculation SHALL handle floating point precision correctly.
     *
     * **Validates: Requirements 3.2**
     */
    "Property 6: Floating point precision should be handled correctly" {
        // Test with dimensions that produce repeating decimals
        val problematicDimensions = listOf(
            Pair(100, 3),   // 33.333...
            Pair(100, 7),   // 14.285714...
            Pair(100, 9),   // 11.111...
            Pair(100, 11),  // 9.0909...
            Pair(100, 13),  // 7.6923...
            Pair(1000, 3),  // 333.333...
            Pair(1000, 7),  // 142.857...
            Pair(1, 3),     // 0.333...
            Pair(2, 3),     // 0.666...
            Pair(1, 7),     // 0.142857...
        )

        problematicDimensions.forEach { (originalWidth, originalHeight) ->
            val originalAspectRatio = originalWidth.toDouble() / originalHeight.toDouble()

            val (displayWidth, displayHeight) = calculateDisplayDimensions(
                originalWidth = originalWidth,
                originalHeight = originalHeight,
                maxWidth = MAX_STICKER_SIZE,
                maxHeight = MAX_STICKER_SIZE
            )

            val displayAspectRatio = displayWidth / displayHeight
            val aspectRatioDifference = abs(originalAspectRatio - displayAspectRatio)

            // Aspect ratio should be preserved within tolerance even with floating point issues
            aspectRatioDifference shouldBeLessThanOrEqual ASPECT_RATIO_TOLERANCE
        }
    }

    /**
     * Property 6: Sticker Aspect Ratio Preservation - Common sticker sizes
     *
     * *For any* common sticker image sizes (128x128, 256x256, 512x512, etc.),
     * the aspect ratio SHALL be preserved correctly.
     *
     * **Validates: Requirements 3.2**
     */
    "Property 6: Common sticker sizes should preserve aspect ratio" {
        val commonSizes = listOf(
            Pair(64, 64),
            Pair(128, 128),
            Pair(256, 256),
            Pair(512, 512),
            Pair(1024, 1024),
            Pair(128, 96),   // 4:3
            Pair(96, 128),   // 3:4
            Pair(160, 90),   // 16:9
            Pair(90, 160),   // 9:16
            Pair(200, 200),  // Exactly max size
            Pair(150, 150),  // Current implementation size
        )

        commonSizes.forEach { (originalWidth, originalHeight) ->
            val originalAspectRatio = originalWidth.toDouble() / originalHeight.toDouble()

            val (displayWidth, displayHeight) = calculateDisplayDimensions(
                originalWidth = originalWidth,
                originalHeight = originalHeight,
                maxWidth = MAX_STICKER_SIZE,
                maxHeight = MAX_STICKER_SIZE
            )

            val displayAspectRatio = displayWidth / displayHeight
            val aspectRatioDifference = abs(originalAspectRatio - displayAspectRatio)

            aspectRatioDifference shouldBeLessThanOrEqual ASPECT_RATIO_TOLERANCE
        }
    }

    /**
     * Property 6: Sticker Aspect Ratio Preservation - Scale factor consistency
     *
     * *For any* sticker image that needs scaling, the scale factor applied to
     * width and height SHALL be the same (uniform scaling).
     *
     * **Validates: Requirements 3.2**
     */
    "Property 6: Scale factor should be uniform for width and height" {
        val dimensionArb = Arb.int(1..5000)

        checkAll(300, dimensionArb, dimensionArb) { originalWidth, originalHeight ->
            val (displayWidth, displayHeight) = calculateDisplayDimensions(
                originalWidth = originalWidth,
                originalHeight = originalHeight,
                maxWidth = MAX_STICKER_SIZE,
                maxHeight = MAX_STICKER_SIZE
            )

            // Calculate scale factors
            val widthScaleFactor = displayWidth / originalWidth.toDouble()
            val heightScaleFactor = displayHeight / originalHeight.toDouble()

            // Scale factors should be equal (uniform scaling)
            val scaleFactorDifference = abs(widthScaleFactor - heightScaleFactor)
            scaleFactorDifference shouldBeLessThanOrEqual 0.0001 // Very small tolerance for floating point
        }
    }
})

/**
 * Maximum sticker display size in dp.
 * Based on design document: Maximum display size is constrained to 200dp x 200dp
 */
private const val MAX_STICKER_SIZE = 200

/**
 * Tolerance for aspect ratio comparison (1% = 0.01).
 * Based on design document: aspect ratio preserved within 1% tolerance
 */
private const val ASPECT_RATIO_TOLERANCE = 0.01

/**
 * Calculates the display dimensions for a sticker image while preserving aspect ratio.
 *
 * This function implements the aspect ratio preservation logic that should be used
 * by the CometChatStickerBubble component. It scales down images that exceed the
 * maximum size while maintaining the original aspect ratio.
 *
 * @param originalWidth The original width of the sticker image in pixels
 * @param originalHeight The original height of the sticker image in pixels
 * @param maxWidth The maximum allowed display width in dp
 * @param maxHeight The maximum allowed display height in dp
 * @return A pair of (displayWidth, displayHeight) in dp
 */
private fun calculateDisplayDimensions(
    originalWidth: Int,
    originalHeight: Int,
    maxWidth: Int,
    maxHeight: Int
): Pair<Double, Double> {
    require(originalWidth > 0) { "Original width must be positive" }
    require(originalHeight > 0) { "Original height must be positive" }
    require(maxWidth > 0) { "Max width must be positive" }
    require(maxHeight > 0) { "Max height must be positive" }

    val aspectRatio = originalWidth.toDouble() / originalHeight.toDouble()

    // Calculate scale factor to fit within max dimensions
    val widthScale = maxWidth.toDouble() / originalWidth.toDouble()
    val heightScale = maxHeight.toDouble() / originalHeight.toDouble()

    // Use the smaller scale factor to ensure both dimensions fit
    // But don't upscale (scale factor should not exceed 1.0)
    val scaleFactor = min(min(widthScale, heightScale), 1.0)

    val displayWidth = originalWidth * scaleFactor
    val displayHeight = originalHeight * scaleFactor

    return Pair(displayWidth, displayHeight)
}
