package com.cometchat.uikit.compose.presentation.imageviewer.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import kotlin.math.abs

/**
 * Pure calculation functions used by the image viewer components.
 * All functions are stateless and side-effect free.
 */
internal object ImageViewerUtils {

    /**
     * Calculates the min and max scale for an image within a container.
     * minScale fits the image to the screen; maxScale is 5× minScale.
     *
     * @return Pair of (minScale, maxScale). Falls back to (1f, 5f) for zero/negative dimensions.
     */
    fun calcScaleRange(
        containerWidth: Float,
        containerHeight: Float,
        imageWidth: Float,
        imageHeight: Float
    ): Pair<Float, Float> {
        if (containerWidth <= 0f || containerHeight <= 0f || imageWidth <= 0f || imageHeight <= 0f) {
            return 1f to 5f
        }
        val containerRatio = containerHeight / containerWidth
        val imageRatio = imageHeight / imageWidth
        val minScale = if (containerRatio > imageRatio) {
            containerWidth / imageWidth
        } else {
            containerHeight / imageHeight
        }
        val maxScale = minScale * 5f
        return minScale to maxScale
    }

    /**
     * Clamps pan offset within image bounds at the given scale.
     */
    fun constrainOffset(
        offset: Offset,
        scale: Float,
        imageSize: IntSize,
        containerSize: IntSize
    ): Offset {
        val scaledWidth = imageSize.width * scale
        val scaledHeight = imageSize.height * scale
        val maxX = ((scaledWidth - containerSize.width) / 2f).coerceAtLeast(0f)
        val maxY = ((scaledHeight - containerSize.height) / 2f).coerceAtLeast(0f)
        return Offset(
            x = offset.x.coerceIn(-maxX, maxX),
            y = offset.y.coerceIn(-maxY, maxY)
        )
    }

    /**
     * Calculates background alpha based on vertical drag offset.
     * Alpha decreases as the drag distance increases relative to screen height.
     *
     * @return Alpha value in [0f, 1f]. Returns 1f if screenHeight is zero.
     */
    fun calcBackgroundAlpha(dragOffsetY: Float, screenHeight: Float): Float {
        if (screenHeight <= 0f) return 1f
        return 1f - (abs(dragOffsetY) / screenHeight).coerceIn(0f, 1f)
    }

    /**
     * Determines whether the viewer should dismiss based on drag distance.
     *
     * @return true iff |dragOffsetY| / screenHeight > 0.5. Returns false if screenHeight is zero.
     */
    fun shouldDismiss(dragOffsetY: Float, screenHeight: Float): Boolean {
        if (screenHeight <= 0f) return false
        return abs(dragOffsetY) / screenHeight > 0.5f
    }

    /**
     * Determines whether a fling gesture should dismiss the viewer.
     *
     * @return true iff |velocity| > 1500f
     */
    fun shouldFlingDismiss(velocity: Float): Boolean {
        return abs(velocity) > 1500f
    }

    /**
     * Calculates the target scale for a double-tap zoom gesture.
     * If at minScale, zooms to 50% between min and max. Otherwise, zooms back to minScale.
     */
    fun calcDoubleTapTargetScale(
        currentScale: Float,
        minScale: Float,
        maxScale: Float
    ): Float {
        return if (currentScale <= minScale) {
            minScale + (maxScale - minScale) * 0.5f
        } else {
            minScale
        }
    }

    /**
     * Validates that all share parameters are non-empty.
     *
     * @return true iff url, fileName, and mimeType are all non-empty.
     */
    fun isShareValid(url: String, fileName: String, mimeType: String): Boolean {
        return url.isNotEmpty() && fileName.isNotEmpty() && mimeType.isNotEmpty()
    }
}
