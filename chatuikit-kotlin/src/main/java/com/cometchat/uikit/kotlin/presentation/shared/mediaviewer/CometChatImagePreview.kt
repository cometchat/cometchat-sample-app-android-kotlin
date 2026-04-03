package com.cometchat.uikit.kotlin.presentation.shared.mediaviewer

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.widget.ImageView
import android.widget.OverScroller
import androidx.core.view.ViewCompat
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import java.lang.ref.WeakReference
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Gesture handler for image preview that provides pinch-to-zoom, double-tap-to-zoom,
 * pan, drag-to-dismiss, and fling-to-dismiss behavior.
 *
 * Uses [WeakReference] for ImageView and container to prevent memory leaks.
 * Attach to an ImageView and its parent container via [create] factory method.
 */
class CometChatImagePreview(
    imageView: ImageView,
    container: ViewGroup
) : View.OnTouchListener, View.OnLayoutChangeListener {

    /**
     * Listener for view translate events during drag-to-dismiss gestures.
     */
    interface OnViewTranslateListener {
        fun onStart(view: ImageView)
        fun onViewTranslate(view: ImageView, amount: Float)
        fun onDismiss(view: ImageView)
        fun onRestore(view: ImageView)
    }

    /**
     * Listener for scale change events during pinch-to-zoom gestures.
     */
    interface OnScaleChangedListener {
        fun onScaleChange(scaleFactor: Float, focusX: Float, focusY: Float)
    }

    companion object {
        const val DEFAULT_MAX_ZOOM = 5.0f
        const val DEFAULT_ANIM_DURATION = 250L
        const val DEFAULT_ANIM_DURATION_LONG = 375L
        const val DEFAULT_VIEW_DRAG_FRICTION = 1f
        const val DEFAULT_DRAG_DISMISS_DISTANCE_IN_VIEW_HEIGHT_RATIO = 0.5f
        const val DEFAULT_DRAG_DISMISS_DISTANCE_IN_DP = 96
        const val MAX_FLING_VELOCITY = 8000f
        const val MIN_FLING_VELOCITY = 1500f
        const val DEFAULT_DOUBLE_TAP_ZOOM_SCALE = 0.5f
        val DEFAULT_INTERPOLATOR = DecelerateInterpolator()

        fun create(imageView: ImageView, container: ViewGroup): CometChatImagePreview {
            return CometChatImagePreview(imageView, container)
        }
    }

    // max zoom (> 1f)
    private val maxZoom: Float = DEFAULT_MAX_ZOOM
    // use fling gesture for dismiss
    private val useFlingToDismissGesture: Boolean = true
    // flag to enable or disable drag to dismiss
    private val useDragToDismiss: Boolean = true
    // duration millis for dismiss animation
    private val dismissAnimationDuration: Long = DEFAULT_ANIM_DURATION
    // duration millis for restore animation
    private val restoreAnimationDuration: Long = DEFAULT_ANIM_DURATION
    // duration millis for image animation
    private val flingAnimationDuration: Long = DEFAULT_ANIM_DURATION
    // duration millis for double tap scale animation
    private val scaleAnimationDuration: Long = DEFAULT_ANIM_DURATION_LONG
    // duration millis for over scale animation
    private val overScaleAnimationDuration: Long = DEFAULT_ANIM_DURATION_LONG
    // duration millis for over scrolling animation
    private val overScrollAnimationDuration: Long = DEFAULT_ANIM_DURATION
    // view drag friction for swipe to dismiss
    private val viewDragFriction: Float = DEFAULT_VIEW_DRAG_FRICTION
    // drag distance threshold in dp for swipe to dismiss
    private val dragDismissDistanceInDp: Int = DEFAULT_DRAG_DISMISS_DISTANCE_IN_DP
    private val dismissAnimationInterpolator: Interpolator = DEFAULT_INTERPOLATOR
    private val restoreAnimationInterpolator: Interpolator = DEFAULT_INTERPOLATOR
    private val flingAnimationInterpolator: Interpolator = DEFAULT_INTERPOLATOR
    private val doubleTapScaleAnimationInterpolator: Interpolator = AccelerateDecelerateInterpolator()
    private val overScaleAnimationInterpolator: Interpolator = DEFAULT_INTERPOLATOR
    private val overScrollAnimationInterpolator: Interpolator = DEFAULT_INTERPOLATOR
    private val doubleTapZoomScale: Float = DEFAULT_DOUBLE_TAP_ZOOM_SCALE // 0f~1f
    private val minimumFlingVelocity: Float = MIN_FLING_VELOCITY
    private val transfrom = Matrix()
    private val originalViewBounds = Rect()
    private val scroller: OverScroller
    private val imageViewRef: WeakReference<ImageView>
    private val containerRef: WeakReference<ViewGroup>

    // view rect - padding (recalculated on size changed)
    private var canvasBounds = RectF()
    // bitmap drawing rect (move on scroll, recalculated on scale changed)
    private var bitmapBounds = RectF()
    // displaying bitmap rect (does not move, recalculated on scale changed)
    private var viewport = RectF()
    // minimum scale of bitmap
    private var minScale = 1f
    // maximum scale of bitmap
    private var maxScale = 1f
    private var dragToDismissThreshold = 0f
    private var isVerticalScrollEnabled = true
    private var isHorizontalScrollEnabled = true
    private var isBitmapScaleAnimationRunning = false
    private var initialY = 0f
    // is ready for drawing bitmap
    private var isReadyToDraw = false
    // bitmap (decoded) width
    private var imageWidth = 0f
    // bitmap (decoded) height
    private var imageHeight = 0f
    // bitmap scale
    private var scale = 1f
    private var flingAnimator: Animator = ValueAnimator()
    private var isBitmapTranslateAnimationRunning = false
    private var isViewTranslateAnimationRunning = false
    private var dragDismissDistanceInPx = 0f
    // scaling helper
    private var scaleGestureDetector: ScaleGestureDetector? = null
    // translating helper
    private var gestureDetector: GestureDetector? = null
    // on view translate listener
    private var onViewTranslateListener: OnViewTranslateListener? = null
    // on scale changed listener
    private var onScaleChangedListener: OnScaleChangedListener? = null
    private var activity: android.app.Activity? = null
    private var lastDistY: Float = Float.NaN

    private val onScaleGestureListener = object : ScaleGestureDetector.OnScaleGestureListener {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            if (isDragging() || isBitmapTranslateAnimationRunning || isBitmapScaleAnimationRunning) {
                return false
            }

            val scaleFactor = detector.scaleFactor
            val focalX = detector.focusX
            val focalY = detector.focusY

            if (scaleFactor == 1.0f) {
                // scale is not changing
                return true
            }

            zoomToTargetScale(calcNewScale(scaleFactor), focalX, focalY)
            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean = true

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            // Do nothing
        }
    }

    private val onGestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            if (e2.pointerCount != 1) {
                return true
            }

            if (scale > minScale) {
                processScroll(distanceX, distanceY)
            } else if (useDragToDismiss && scale == minScale) {
                processDrag(distanceY)
            }
            return true
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (e1 == null) {
                return true
            }

            if (scale > minScale) {
                processFlingBitmap(velocityX, velocityY)
            } else {
                processFlingToDismiss(velocityY)
            }
            return true
        }

        override fun onDown(e: MotionEvent): Boolean = true

        override fun onDoubleTap(e: MotionEvent): Boolean {
            if (isBitmapScaleAnimationRunning) {
                return true
            }

            if (scale > minScale) {
                zoomOutToMinimumScale(false)
            } else {
                zoomInToTargetScale(e)
            }
            return true
        }
    }

    init {
        imageViewRef = WeakReference(imageView)
        containerRef = WeakReference(container)
        activity = Utils.getActivity(container.context)
        if (Utils.isActivityUsable(activity)) {
            activity!!.window.decorView.setBackgroundColor(Color.BLACK)
        }
        container.setOnTouchListener(this)
        container.addOnLayoutChangeListener(this)

        scaleGestureDetector = ScaleGestureDetector(container.context, onScaleGestureListener)
        gestureDetector = GestureDetector(container.context, onGestureListener)
        scroller = OverScroller(container.context)

        dragDismissDistanceInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dragDismissDistanceInDp.toFloat(),
            container.context.resources.displayMetrics
        )

        // Initialize imageView
        imageView.imageMatrix = null
        imageView.y = 0f
        imageView.animate().cancel()
        imageView.scaleType = ImageView.ScaleType.MATRIX
    }

    fun setOnScaleChangedListener(listener: OnScaleChangedListener?) {
        this.onScaleChangedListener = listener
    }

    fun setOnViewTranslateListener(listener: OnViewTranslateListener?) {
        this.onViewTranslateListener = listener
    }

    override fun onLayoutChange(
        v: View,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        oldLeft: Int,
        oldTop: Int,
        oldRight: Int,
        oldBottom: Int
    ) {
        val imageView = imageViewRef.get() ?: return
        val container = containerRef.get() ?: return

        // Set up layout for the ImageView
        setupLayout(left, top, right, bottom)

        // Save initial Y position
        initialY = imageView.y

        if (useFlingToDismissGesture) {
            setDragToDismissDistance(DEFAULT_DRAG_DISMISS_DISTANCE_IN_VIEW_HEIGHT_RATIO)
        } else {
            setDragToDismissDistance(DEFAULT_DRAG_DISMISS_DISTANCE_IN_DP)
        }

        // Apply transform and invalidate the view
        setTransform()
        imageView.postInvalidate()
    }

    fun setupLayout(left: Int, top: Int, right: Int, bottom: Int) {
        val imageView = imageViewRef.get() ?: return

        originalViewBounds.set(left, top, right, bottom)
        val drawable = imageView.drawable
        val bitmap = (drawable as? BitmapDrawable)?.bitmap
        if (imageView.width == 0 || imageView.height == 0 || drawable == null) {
            return
        }

        imageWidth = bitmap?.width?.toFloat() ?: drawable.intrinsicWidth.toFloat()
        imageHeight = bitmap?.height?.toFloat() ?: drawable.intrinsicHeight.toFloat()
        val canvasWidth = (imageView.width - imageView.paddingLeft - imageView.paddingRight).toFloat()
        val canvasHeight = (imageView.height - imageView.paddingTop - imageView.paddingBottom).toFloat()

        calcScaleRange(canvasWidth, canvasHeight, imageWidth, imageHeight)
        calcBounds()
        constrainBitmapBounds(false)
        isReadyToDraw = true
        imageView.invalidate()
    }

    fun setDragToDismissDistance(heightRatio: Float) {
        val imageView = imageViewRef.get() ?: return
        dragToDismissThreshold = imageView.height * heightRatio
    }

    fun setDragToDismissDistance(distance: Int) {
        val imageView = imageViewRef.get() ?: return
        dragToDismissThreshold = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            distance.toFloat(),
            imageView.context.resources.displayMetrics
        )
    }

    fun setTransform() {
        val imageView = imageViewRef.get() ?: return

        val transform = Matrix()
        transform.reset()
        transform.postTranslate(-imageWidth / 2, -imageHeight / 2)
        transform.postScale(scale, scale)
        transform.postTranslate(bitmapBounds.centerX(), bitmapBounds.centerY())

        imageView.imageMatrix = transform
    }

    fun calcScaleRange(canvasWidth: Float, canvasHeight: Float, bitmapWidth: Float, bitmapHeight: Float) {
        val canvasRatio = canvasHeight / canvasWidth
        val bitmapRatio = bitmapHeight / bitmapWidth
        minScale = if (canvasRatio > bitmapRatio) canvasWidth / bitmapWidth else canvasHeight / bitmapHeight
        scale = minScale
        maxScale = minScale * maxZoom
    }

    fun calcBounds() {
        val imageView = imageViewRef.get() ?: return

        // Calculate canvas bounds
        canvasBounds = RectF(
            imageView.paddingLeft.toFloat(),
            imageView.paddingTop.toFloat(),
            (imageView.width - imageView.paddingRight).toFloat(),
            (imageView.height - imageView.paddingBottom).toFloat()
        )

        // Calculate bitmap bounds
        bitmapBounds = RectF(
            canvasBounds.centerX() - imageWidth * scale * 0.5f,
            canvasBounds.centerY() - imageHeight * scale * 0.5f,
            canvasBounds.centerX() + imageWidth * scale * 0.5f,
            canvasBounds.centerY() + imageHeight * scale * 0.5f
        )

        // Calculate viewport
        viewport = RectF(
            max(canvasBounds.left, bitmapBounds.left),
            max(canvasBounds.top, bitmapBounds.top),
            min(canvasBounds.right, bitmapBounds.right),
            min(canvasBounds.bottom, bitmapBounds.bottom)
        )

        // Check scroll availability
        isHorizontalScrollEnabled = true
        isVerticalScrollEnabled = true

        if (bitmapBounds.width() < canvasBounds.width()) {
            isHorizontalScrollEnabled = false
        }

        if (bitmapBounds.height() < canvasBounds.height()) {
            isVerticalScrollEnabled = false
        }
    }

    fun constrainBitmapBounds(animate: Boolean) {
        val imageView = imageViewRef.get() ?: return

        if (isBitmapTranslateAnimationRunning || isBitmapScaleAnimationRunning) {
            return
        }

        val offset = PointF()

        // constrain viewport inside bitmap bounds
        if (viewport.left < bitmapBounds.left) {
            offset.x += viewport.left - bitmapBounds.left
        }

        if (viewport.top < bitmapBounds.top) {
            offset.y += viewport.top - bitmapBounds.top
        }

        if (viewport.right > bitmapBounds.right) {
            offset.x += viewport.right - bitmapBounds.right
        }

        if (viewport.bottom > bitmapBounds.bottom) {
            offset.y += viewport.bottom - bitmapBounds.bottom
        }

        if (offset.equals(0f, 0f)) {
            return
        }

        if (animate) {
            if (!isVerticalScrollEnabled) {
                bitmapBounds.offset(0f, offset.y)
                offset.y = 0f
            }

            if (!isHorizontalScrollEnabled) {
                bitmapBounds.offset(offset.x, 0f)
                offset.x = 0f
            }

            val start = RectF(bitmapBounds)
            val end = RectF(bitmapBounds)
            end.offset(offset.x, offset.y)

            val animator = ValueAnimator.ofFloat(0f, 1f)
            animator.duration = overScrollAnimationDuration
            animator.interpolator = overScrollAnimationInterpolator
            animator.addUpdateListener { valueAnimator ->
                val amount = valueAnimator.animatedValue as Float
                val newLeft = lerp(amount, start.left, end.left)
                val newTop = lerp(amount, start.top, end.top)
                bitmapBounds.offsetTo(newLeft, newTop)
                ViewCompat.postInvalidateOnAnimation(imageView)
                setTransform()
            }
            animator.start()
        } else {
            bitmapBounds.offset(offset.x, offset.y)
        }
    }

    fun lerp(amt: Float, start: Float, stop: Float): Float {
        return start + (stop - start) * amt
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        val imageView = imageViewRef.get() ?: return false
        val container = containerRef.get() ?: return false

        // Request to disallow intercepting touch events based on scale
        container.parent.requestDisallowInterceptTouchEvent(scale != minScale)

        if (!imageView.isEnabled) {
            return false
        }

        if (isViewTranslateAnimationRunning) {
            return false
        }

        val scaleEvent = scaleGestureDetector?.onTouchEvent(event) ?: false
        val isScaleAnimationRunning = scale < minScale

        if (scaleEvent != scaleGestureDetector?.isInProgress && !isScaleAnimationRunning) {
            // Handle single touch gesture when scaling process is not running
            gestureDetector?.onTouchEvent(event)
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                flingAnimator.cancel()
            }
            MotionEvent.ACTION_UP -> {
                if (scale == minScale) {
                    if (!isViewTranslateAnimationRunning) {
                        dismissOrRestoreIfNeeded()
                    }
                } else if (scale > minScale) {
                    constrainBitmapBounds(true)
                } else {
                    zoomOutToMinimumScale(true)
                }
            }
        }

        setTransform()
        imageView.postInvalidate()
        return true
    }

    fun dismissOrRestoreIfNeeded() {
        if (!isDragging() || isViewTranslateAnimationRunning) {
            return
        }
        dismissOrRestore()
    }

    fun zoomOutToMinimumScale(isOverScaling: Boolean) {
        val imageView = imageViewRef.get() ?: return

        val startScale = scale
        val endScale = minScale
        val startLeft = bitmapBounds.left
        val startTop = bitmapBounds.top
        val endLeft = canvasBounds.centerX() - imageWidth * minScale * 0.5f
        val endTop = canvasBounds.centerY() - imageHeight * minScale * 0.5f

        val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
        valueAnimator.duration = if (isOverScaling) overScaleAnimationDuration else scaleAnimationDuration
        valueAnimator.interpolator = if (isOverScaling) overScaleAnimationInterpolator else doubleTapScaleAnimationInterpolator

        valueAnimator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            scale = lerp(value, startScale, endScale)
            val newLeft = lerp(value, startLeft, endLeft)
            val newTop = lerp(value, startTop, endTop)
            calcBounds()
            bitmapBounds.offsetTo(newLeft, newTop)
            constrainBitmapBounds(false)
            ViewCompat.postInvalidateOnAnimation(imageView)
            setTransform()
        }

        valueAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {
                isBitmapScaleAnimationRunning = true
            }

            override fun onAnimationEnd(animator: Animator) {
                isBitmapScaleAnimationRunning = false
                if (endScale == minScale) {
                    scale = minScale
                    calcBounds()
                    constrainBitmapBounds(false)
                    imageView.postInvalidate()
                }
            }

            override fun onAnimationCancel(animator: Animator) {
                isBitmapScaleAnimationRunning = false
            }

            override fun onAnimationRepeat(animator: Animator) {
                // No-op
            }
        })

        valueAnimator.start()
    }

    private fun dismissOrRestore() {
        val imageView = imageViewRef.get() ?: return

        if (shouldTriggerDragToDismissAnimation()) {
            if (useFlingToDismissGesture) {
                startDragToDismissAnimation()
            } else {
                onViewTranslateListener?.onDismiss(imageView)
                cleanup()
            }
        } else {
            restoreViewTransform()
        }
    }

    fun shouldTriggerDragToDismissAnimation(): Boolean {
        return dragDistance() > dragToDismissThreshold
    }

    fun startDragToDismissAnimation() {
        val imageView = imageViewRef.get() ?: return

        // Calculate translationY based on the difference in Y positions
        val translationY = if (imageView.y - initialY > 0) {
            (originalViewBounds.top + imageView.height - imageView.top).toFloat()
        } else {
            (originalViewBounds.top - imageView.height - imageView.top).toFloat()
        }

        imageView.animate()
            .setDuration(dismissAnimationDuration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .translationY(translationY)
            .setUpdateListener {
                val amount = calcTranslationAmount()
                changeBackgroundAlpha(amount)
                onViewTranslateListener?.onViewTranslate(imageView, amount)
            }
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                    isViewTranslateAnimationRunning = true
                }

                override fun onAnimationEnd(animation: Animator) {
                    isViewTranslateAnimationRunning = false
                    onViewTranslateListener?.onDismiss(imageView)
                    cleanup()
                }

                override fun onAnimationCancel(animation: Animator) {
                    isViewTranslateAnimationRunning = false
                }

                override fun onAnimationRepeat(animation: Animator) {
                    // No-op
                }
            })
    }

    private fun isDragging(): Boolean {
        return dragDistance() > 0f
    }

    // Process the fling to dismiss gesture based on velocityY
    fun processFlingToDismiss(velocityY: Float) {
        if (useFlingToDismissGesture && !isViewTranslateAnimationRunning) {
            if (abs(velocityY) < minimumFlingVelocity) {
                return
            }
            startVerticalTranslateAnimation(velocityY)
        }
    }

    fun startVerticalTranslateAnimation(velY: Float) {
        val imageView = imageViewRef.get() ?: return

        isViewTranslateAnimationRunning = true

        // Calculate the translationY based on velY
        val translationY = if (velY > 0) {
            (originalViewBounds.top + imageView.height - imageView.top).toFloat()
        } else {
            (originalViewBounds.top - imageView.height - imageView.top).toFloat()
        }

        imageView.animate()
            .setDuration(dismissAnimationDuration)
            .setInterpolator(dismissAnimationInterpolator)
            .translationY(translationY)
            .setUpdateListener {
                // Call update listener and pass translation amount
                val amount = calcTranslationAmount()
                changeBackgroundAlpha(amount)
                onViewTranslateListener?.onViewTranslate(imageView, amount)
            }
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                    // No operation needed here
                }

                override fun onAnimationEnd(animation: Animator) {
                    isViewTranslateAnimationRunning = false
                    onViewTranslateListener?.onDismiss(imageView)
                    cleanup()
                }

                override fun onAnimationCancel(animation: Animator) {
                    isViewTranslateAnimationRunning = false
                }

                override fun onAnimationRepeat(animation: Animator) {
                    // No operation needed here
                }
            })
    }

    // Calculate the translation amount for dragging
    fun calcTranslationAmount(): Float {
        return constrain(
            0f,
            norm(dragDistance(), 0f, originalViewBounds.height().toFloat()),
            1f
        )
    }

    fun changeBackgroundAlpha(amount: Float) {
        val container = containerRef.get() ?: return
        if (Utils.isActivityUsable(activity)) {
            if (amount == 0.0f) {
                activity!!.window.decorView.setBackgroundColor(Color.BLACK)
            } else {
                activity!!.window.decorView.setBackgroundColor(Color.TRANSPARENT)
            }
        }
    }

    fun cleanup() {
        val container = containerRef.get()
        if (container != null) {
            container.setOnTouchListener(null)
            container.removeOnLayoutChangeListener(null)
        }

        imageViewRef.clear()
        containerRef.clear()
    }

    fun constrain(min: Float, value: Float, max: Float): Float {
        return max(min(value, max), min)
    }

    fun norm(value: Float, start: Float, stop: Float): Float {
        return value / (stop - start)
    }

    // Calculate the drag distance
    fun dragDistance(): Float {
        return abs(viewOffsetY())
    }

    // Method to get the Y offset for the view
    fun viewOffsetY(): Float {
        val imageView = imageViewRef.get()
        return if (imageView != null) imageView.y else 0f - initialY
    }

    fun restoreViewTransform() {
        val imageView = imageViewRef.get() ?: return

        imageView.animate()
            .setDuration(restoreAnimationDuration)
            .setInterpolator(restoreAnimationInterpolator)
            .translationY((originalViewBounds.top - imageView.top).toFloat())
            .setUpdateListener {
                val amount = calcTranslationAmount()
                changeBackgroundAlpha(amount)
                onViewTranslateListener?.onViewTranslate(imageView, amount)
            }
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                    // No-op
                }

                override fun onAnimationEnd(animation: Animator) {
                    onViewTranslateListener?.onRestore(imageView)
                }

                override fun onAnimationCancel(animation: Animator) {
                    // No-op
                }

                override fun onAnimationRepeat(animation: Animator) {
                    // No-op
                }
            })
    }

    fun processFlingBitmap(velocityX: Float, velocityY: Float) {
        val imageView = imageViewRef.get() ?: return

        var velX = velocityX / scale
        var velY = velocityY / scale

        if (velX == 0f && velY == 0f) {
            return
        }

        if (velX > MAX_FLING_VELOCITY) {
            velX = MAX_FLING_VELOCITY
        }

        if (velY > MAX_FLING_VELOCITY) {
            velY = MAX_FLING_VELOCITY
        }

        val fromX = bitmapBounds.left
        val fromY = bitmapBounds.top

        scroller.forceFinished(true)
        scroller.fling(
            fromX.roundToInt(),
            fromY.roundToInt(),
            velX.roundToInt(),
            velY.roundToInt(),
            (viewport.right - bitmapBounds.width()).roundToInt(),
            viewport.left.roundToInt(),
            (viewport.bottom - bitmapBounds.height()).roundToInt(),
            viewport.top.roundToInt()
        )

        ViewCompat.postInvalidateOnAnimation(imageView)

        val toX = scroller.finalX.toFloat()
        val toY = scroller.finalY.toFloat()

        flingAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = flingAnimationDuration
            interpolator = flingAnimationInterpolator

            addUpdateListener { animation ->
                val amount = animation.animatedValue as Float
                val newLeft = lerp(amount, fromX, toX)
                val newTop = lerp(amount, fromY, toY)
                bitmapBounds.offsetTo(newLeft, newTop)
                ViewCompat.postInvalidateOnAnimation(imageView)
                setTransform()
            }

            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animator: Animator) {
                    isBitmapTranslateAnimationRunning = true
                }

                override fun onAnimationEnd(animator: Animator) {
                    isBitmapTranslateAnimationRunning = false
                    constrainBitmapBounds(false)
                }

                override fun onAnimationCancel(animator: Animator) {
                    isBitmapTranslateAnimationRunning = false
                }

                override fun onAnimationRepeat(animator: Animator) {
                    // No-op
                }
            })
        }
        flingAnimator.start()
    }

    fun processScroll(distanceX: Float, distanceY: Float) {
        val distX = if (isHorizontalScrollEnabled) -distanceX else 0f
        val distY = if (isVerticalScrollEnabled) -distanceY else 0f

        offsetBitmap(distX, distY)
        setTransform()
    }

    fun offsetBitmap(offsetX: Float, offsetY: Float) {
        bitmapBounds.offset(offsetX, offsetY)
    }

    fun zoomInToTargetScale(e: MotionEvent) {
        val imageView = imageViewRef.get() ?: return

        val startScale = scale
        val endScale = minScale * maxZoom * doubleTapZoomScale
        val focalX = e.x
        val focalY = e.y

        val valueAnimator = ValueAnimator.ofFloat(startScale, endScale)
        valueAnimator.duration = scaleAnimationDuration
        valueAnimator.interpolator = doubleTapScaleAnimationInterpolator
        valueAnimator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Float
            zoomToTargetScale(animatedValue, focalX, focalY)
            ViewCompat.postInvalidateOnAnimation(imageView)
            setTransform()
        }
        valueAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                isBitmapScaleAnimationRunning = true
            }

            override fun onAnimationEnd(animation: Animator) {
                isBitmapScaleAnimationRunning = false
                if (endScale == minScale) {
                    zoomToTargetScale(minScale, focalX, focalY)
                    imageView.postInvalidate()
                }
            }

            override fun onAnimationCancel(animation: Animator) {
                isBitmapScaleAnimationRunning = false
            }

            override fun onAnimationRepeat(animation: Animator) {
                // No-op
            }
        })

        valueAnimator.start()
    }

    fun zoomToTargetScale(targetScale: Float, focalX: Float, focalY: Float) {
        scale = targetScale
        val lastBounds = RectF(bitmapBounds)
        // scale has changed, recalculate bitmap bounds
        calcBounds()
        // offset to focalPoint
        offsetToZoomFocalPoint(focalX, focalY, lastBounds, bitmapBounds)
        onScaleChangedListener?.onScaleChange(targetScale, focalX, focalY)
    }

    fun offsetToZoomFocalPoint(focalX: Float, focalY: Float, oldBounds: RectF, newBounds: RectF) {
        val oldX = constrain(viewport.left, focalX, viewport.right)
        val oldY = constrain(viewport.top, focalY, viewport.bottom)
        val newX = map(oldX, oldBounds.left, oldBounds.right, newBounds.left, newBounds.right)
        val newY = map(oldY, oldBounds.top, oldBounds.bottom, newBounds.top, newBounds.bottom)
        offsetBitmap(oldX - newX, oldY - newY)
    }

    fun map(value: Float, srcStart: Float, srcStop: Float, dstStart: Float, dstStop: Float): Float {
        if (srcStop - srcStart == 0f) {
            return 0f
        }
        return ((value - srcStart) * (dstStop - dstStart) / (srcStop - srcStart)) + dstStart
    }

    fun processDrag(distanceY: Float) {
        val imageView = imageViewRef.get() ?: return

        if (lastDistY.isNaN()) {
            lastDistY = distanceY
            return
        }

        if (imageView.y == initialY) {
            onViewTranslateListener?.onStart(imageView)
        }

        // Adjust the Y position of the image based on the drag friction
        imageView.y = imageView.y - distanceY * viewDragFriction

        // Calculate translation amount and update background alpha
        val amount = calcTranslationAmount()
        changeBackgroundAlpha(amount)

        // Notify the listener about the view translation
        onViewTranslateListener?.onViewTranslate(imageView, amount)
    }

    fun calcNewScale(newScale: Float): Float {
        return min(maxScale, newScale * scale)
    }

    fun dismiss() {
        // Animate down offscreen (the finish listener will call the cleanup method)
        startVerticalTranslateAnimation(MIN_FLING_VELOCITY)
    }
}
