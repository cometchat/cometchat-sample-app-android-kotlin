package com.cometchat.uikit.compose.presentation.imageviewer.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.cometchat.uikit.compose.presentation.imageviewer.style.CometChatImageViewerStyle
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign

/**
 * Core image preview composable with pinch-to-zoom, double-tap-to-zoom,
 * pan, drag-to-dismiss, and fling-to-dismiss gesture handling.
 *
 * Loads the image via Coil [AsyncImage] and applies all visual transforms
 * through [Modifier.graphicsLayer] to avoid recomposition overhead.
 *
 * @param imageUrl Remote URL of the image to display
 * @param modifier Modifier for the composable
 * @param style Style configuration for visual properties
 * @param onDragStart Called when a vertical drag gesture starts (hides toolbar)
 * @param onDragEnd Called when a drag gesture is restored (shows toolbar)
 * @param onDismiss Called when the viewer should be dismissed
 * @param onDragProgress Called with the drag fraction (0..1) for background alpha
 * @param onLoadingStateChange Called when the image loading state changes
 */
@Composable
internal fun CometChatImagePreview(
    imageUrl: String,
    modifier: Modifier = Modifier,
    style: CometChatImageViewerStyle,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onDismiss: () -> Unit,
    onDragProgress: (fraction: Float) -> Unit,
    onLoadingStateChange: (isLoading: Boolean) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val screenHeightPx = with(density) {
        LocalConfiguration.current.screenHeightDp.dp.toPx()
    }

    // --- Internal state (Task 4.1) ---
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    // Scale range — updated when image intrinsic size is known
    var minScale by remember { mutableFloatStateOf(1f) }
    var maxScale by remember { mutableFloatStateOf(5f) }

    // Image intrinsic size — captured from the painter state
    var imageIntrinsicSize by remember { mutableStateOf(IntSize.Zero) }

    // Animatables for smooth zoom and drag animations
    val scaleAnimatable = remember { Animatable(1f) }
    val offsetAnimatable = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val dragAnimatable = remember { Animatable(0f) }

    // Container size — captured via onSizeChanged
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { containerSize = it },
        contentAlignment = Alignment.Center
    ) {
        val containerWidth = containerSize.width.toFloat()
        val containerHeight = containerSize.height.toFloat()

        AsyncImage(
            model = imageUrl,
            contentDescription = "Image preview",
            contentScale = ContentScale.Fit,
            onState = { state ->
                when (state) {
                    is AsyncImagePainter.State.Loading -> {
                        onLoadingStateChange(true)
                    }
                    is AsyncImagePainter.State.Success -> {
                        onLoadingStateChange(false)
                        val painter = state.painter
                        val imgWidth = painter.intrinsicSize.width
                        val imgHeight = painter.intrinsicSize.height
                        if (imgWidth > 0f && imgHeight > 0f) {
                            imageIntrinsicSize = IntSize(imgWidth.toInt(), imgHeight.toInt())
                            val (newMin, newMax) = ImageViewerUtils.calcScaleRange(
                                containerWidth, containerHeight, imgWidth, imgHeight
                            )
                            minScale = newMin
                            maxScale = newMax
                            scale = newMin
                        }
                    }
                    is AsyncImagePainter.State.Error -> {
                        onLoadingStateChange(false)
                    }
                    else -> { /* Empty / initial state */ }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y + dragOffsetY
                }
                // --- Task 4.2: Pinch-to-zoom gesture handling ---
                .pointerInput(minScale, maxScale) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val newScale = (scale * zoom).coerceIn(minScale, maxScale)
                        val newOffset = if (newScale > minScale) {
                            ImageViewerUtils.constrainOffset(
                                offset = offset + pan,
                                scale = newScale,
                                imageSize = imageIntrinsicSize,
                                containerSize = containerSize
                            )
                        } else {
                            Offset.Zero
                        }
                        scale = newScale
                        offset = newOffset
                    }
                }
                // --- Task 4.3: Double-tap-to-zoom gesture handling ---
                .pointerInput(minScale, maxScale) {
                    detectTapGestures(
                        onDoubleTap = { tapOffset ->
                            coroutineScope.launch {
                                val targetScale = ImageViewerUtils.calcDoubleTapTargetScale(
                                    currentScale = scale,
                                    minScale = minScale,
                                    maxScale = maxScale
                                )
                                if (scale <= minScale) {
                                    // Zoom in centered on tap point
                                    val centroidX = tapOffset.x - containerWidth / 2f
                                    val centroidY = tapOffset.y - containerHeight / 2f
                                    val scaleFactor = targetScale / scale
                                    val targetOffset = ImageViewerUtils.constrainOffset(
                                        offset = Offset(
                                            x = centroidX * (1f - scaleFactor),
                                            y = centroidY * (1f - scaleFactor)
                                        ),
                                        scale = targetScale,
                                        imageSize = imageIntrinsicSize,
                                        containerSize = containerSize
                                    )
                                    // Animate scale
                                    scaleAnimatable.snapTo(scale)
                                    offsetAnimatable.snapTo(offset)
                                    launch {
                                        scaleAnimatable.animateTo(
                                            targetValue = targetScale,
                                            animationSpec = spring()
                                        ) { scale = value }
                                    }
                                    launch {
                                        offsetAnimatable.animateTo(
                                            targetValue = targetOffset,
                                            animationSpec = spring()
                                        ) { offset = value }
                                    }
                                } else {
                                    // Zoom out to minScale
                                    scaleAnimatable.snapTo(scale)
                                    offsetAnimatable.snapTo(offset)
                                    launch {
                                        scaleAnimatable.animateTo(
                                            targetValue = minScale,
                                            animationSpec = spring()
                                        ) { scale = value }
                                    }
                                    launch {
                                        offsetAnimatable.animateTo(
                                            targetValue = Offset.Zero,
                                            animationSpec = spring()
                                        ) { offset = value }
                                    }
                                }
                            }
                        }
                    )
                }
                // --- Task 4.4: Drag-to-dismiss and fling-to-dismiss gesture handling ---
                .pointerInput(minScale) {
                    val velocityTracker = VelocityTracker()
                    awaitPointerEventScope {
                        while (true) {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            // Only handle drag-to-dismiss at minimum scale
                            if (scale > minScale) continue

                            velocityTracker.resetTracking()
                            velocityTracker.addPosition(
                                down.uptimeMillis,
                                down.position
                            )

                            var dragStarted = false
                            var totalDragY = 0f

                            try {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull() ?: break

                                    if (!change.pressed) {
                                        // Pointer released — handle end/fling
                                        val velocity = velocityTracker.calculateVelocity()
                                        val velocityY = velocity.y

                                        if (dragStarted) {
                                            if (ImageViewerUtils.shouldFlingDismiss(velocityY)) {
                                                // Fling dismiss
                                                coroutineScope.launch {
                                                    val direction = sign(velocityY)
                                                    dragAnimatable.snapTo(dragOffsetY)
                                                    dragAnimatable.animateTo(
                                                        targetValue = direction * screenHeightPx,
                                                        animationSpec = spring()
                                                    ) { dragOffsetY = value }
                                                    onDismiss()
                                                }
                                            } else if (ImageViewerUtils.shouldDismiss(
                                                    dragOffsetY, screenHeightPx
                                                )
                                            ) {
                                                // Drag distance exceeds threshold — dismiss
                                                coroutineScope.launch {
                                                    val direction = sign(dragOffsetY)
                                                    dragAnimatable.snapTo(dragOffsetY)
                                                    dragAnimatable.animateTo(
                                                        targetValue = direction * screenHeightPx,
                                                        animationSpec = spring()
                                                    ) { dragOffsetY = value }
                                                    onDismiss()
                                                }
                                            } else {
                                                // Restore — animate back to center
                                                coroutineScope.launch {
                                                    dragAnimatable.snapTo(dragOffsetY)
                                                    dragAnimatable.animateTo(
                                                        targetValue = 0f,
                                                        animationSpec = spring()
                                                    ) { dragOffsetY = value }
                                                    onDragProgress(0f)
                                                    onDragEnd()
                                                }
                                            }
                                            isDragging = false
                                        }
                                        break
                                    }

                                    velocityTracker.addPosition(
                                        change.uptimeMillis,
                                        change.position
                                    )

                                    val dragDelta = change.position - change.previousPosition
                                    totalDragY += dragDelta.y

                                    // Start drag after a small threshold to avoid accidental drags
                                    if (!dragStarted && abs(totalDragY) > 8f) {
                                        dragStarted = true
                                        isDragging = true
                                        onDragStart()
                                    }

                                    if (dragStarted) {
                                        dragOffsetY += dragDelta.y
                                        val fraction = (abs(dragOffsetY) / screenHeightPx)
                                            .coerceIn(0f, 1f)
                                        onDragProgress(fraction)
                                        change.consume()
                                    }
                                }
                            } catch (_: Exception) {
                                // Gesture cancelled
                                if (dragStarted) {
                                    isDragging = false
                                    coroutineScope.launch {
                                        dragAnimatable.snapTo(dragOffsetY)
                                        dragAnimatable.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring()
                                        ) { dragOffsetY = value }
                                        onDragProgress(0f)
                                        onDragEnd()
                                    }
                                }
                            }
                        }
                    }
                }
        )
    }
}
