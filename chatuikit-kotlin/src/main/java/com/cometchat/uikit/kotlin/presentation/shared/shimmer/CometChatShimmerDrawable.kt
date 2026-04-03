package com.cometchat.uikit.kotlin.presentation.shared.shimmer

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.view.animation.LinearInterpolator
import kotlin.math.max

/**
 * CometChatShimmerDrawable is a drawable that renders the shimmer effect.
 * It handles the animation and drawing of the shimmer gradient.
 */
class CometChatShimmerDrawable : Drawable() {

    companion object {
        private val TAG = CometChatShimmerDrawable::class.java.simpleName
    }

    private val updateListener = ValueAnimator.AnimatorUpdateListener { invalidateSelf() }

    private val shimmerPaint = Paint().apply { isAntiAlias = true }
    private val drawRect = Rect()
    private val shaderMatrix = Matrix()

    private var valueAnimator: ValueAnimator? = null
    private var staticAnimationProgress = -1f

    var shimmer: CometChatShimmer? = null
        private set

    fun setShimmer(shimmer: CometChatShimmer?) {
        this.shimmer = shimmer
        shimmer?.let {
            shimmerPaint.xfermode = PorterDuffXfermode(
                if (it.alphaShimmer) PorterDuff.Mode.DST_IN else PorterDuff.Mode.SRC_IN
            )
        }
        updateShader()
        updateValueAnimator()
        invalidateSelf()
    }

    fun startShimmer() {
        valueAnimator?.let {
            if (!isShimmerStarted() && callback != null) {
                it.start()
            }
        }
    }

    fun stopShimmer() {
        valueAnimator?.let {
            if (isShimmerStarted()) {
                it.cancel()
            }
        }
    }

    fun isShimmerStarted(): Boolean = valueAnimator?.isStarted == true

    fun isShimmerRunning(): Boolean = valueAnimator?.isRunning == true

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        drawRect.set(bounds)
        updateShader()
        maybeStartShimmer()
    }

    fun setStaticAnimationProgress(value: Float) {
        if (value.compareTo(staticAnimationProgress) == 0 || (value < 0f && staticAnimationProgress < 0f)) {
            return
        }
        staticAnimationProgress = value.coerceAtMost(1f)
        invalidateSelf()
    }

    fun clearStaticAnimationProgress() {
        setStaticAnimationProgress(-1f)
    }

    override fun draw(canvas: Canvas) {
        val currentShimmer = shimmer ?: return
        if (shimmerPaint.shader == null) return

        val tiltTan = kotlin.math.tan(Math.toRadians(currentShimmer.tilt.toDouble())).toFloat()
        val translateHeight = drawRect.height() + tiltTan * drawRect.width()
        val translateWidth = drawRect.width() + tiltTan * drawRect.height()

        val animatedValue = if (staticAnimationProgress < 0f) {
            valueAnimator?.animatedValue as? Float ?: 0f
        } else {
            staticAnimationProgress
        }

        val (dx, dy) = when (currentShimmer.direction) {
            CometChatShimmer.Direction.RIGHT_TO_LEFT -> offset(translateWidth, -translateWidth, animatedValue) to 0f
            CometChatShimmer.Direction.TOP_TO_BOTTOM -> 0f to offset(-translateHeight, translateHeight, animatedValue)
            CometChatShimmer.Direction.BOTTOM_TO_TOP -> 0f to offset(translateHeight, -translateHeight, animatedValue)
            else -> offset(-translateWidth, translateWidth, animatedValue) to 0f // LEFT_TO_RIGHT
        }

        shaderMatrix.reset()
        shaderMatrix.setRotate(currentShimmer.tilt, drawRect.width() / 2f, drawRect.height() / 2f)
        shaderMatrix.preTranslate(dx, dy)
        shimmerPaint.shader?.setLocalMatrix(shaderMatrix)
        canvas.drawRect(drawRect, shimmerPaint)
    }

    override fun setAlpha(alpha: Int) {}

    override fun setColorFilter(colorFilter: ColorFilter?) {}

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int {
        return shimmer?.let {
            if (it.clipToChildren || it.alphaShimmer) PixelFormat.TRANSLUCENT else PixelFormat.OPAQUE
        } ?: PixelFormat.OPAQUE
    }

    private fun offset(start: Float, end: Float, percent: Float): Float {
        return start + (end - start) * percent
    }

    private fun updateValueAnimator() {
        val currentShimmer = shimmer ?: return

        val started = valueAnimator?.isStarted == true
        valueAnimator?.cancel()
        valueAnimator?.removeAllUpdateListeners()

        valueAnimator = ValueAnimator.ofFloat(0f, 1f + (currentShimmer.repeatDelay.toFloat() / currentShimmer.animationDuration)).apply {
            interpolator = LinearInterpolator()
            repeatMode = currentShimmer.repeatMode
            startDelay = currentShimmer.startDelay
            repeatCount = currentShimmer.repeatCount
            duration = currentShimmer.animationDuration + currentShimmer.repeatDelay
            addUpdateListener(updateListener)
        }

        if (started) {
            valueAnimator?.start()
        }
    }

    fun maybeStartShimmer() {
        valueAnimator?.let { animator ->
            shimmer?.let { shimmerConfig ->
                if (!animator.isStarted && shimmerConfig.autoStart && callback != null) {
                    animator.start()
                }
            }
        }
    }

    private fun updateShader() {
        val bounds = bounds
        val boundsWidth = bounds.width()
        val boundsHeight = bounds.height()
        val currentShimmer = shimmer

        if (boundsWidth == 0 || boundsHeight == 0 || currentShimmer == null) {
            return
        }

        val width = currentShimmer.width(boundsWidth)
        val height = currentShimmer.height(boundsHeight)

        val shader: Shader = when (currentShimmer.shape) {
            CometChatShimmer.Shape.RADIAL -> {
                RadialGradient(
                    width / 2f,
                    height / 2f,
                    (max(width, height) / kotlin.math.sqrt(2.0)).toFloat(),
                    currentShimmer.colors,
                    currentShimmer.positions,
                    Shader.TileMode.CLAMP
                )
            }
            else -> { // LINEAR
                val vertical = currentShimmer.direction == CometChatShimmer.Direction.TOP_TO_BOTTOM ||
                        currentShimmer.direction == CometChatShimmer.Direction.BOTTOM_TO_TOP
                val endX = if (vertical) 0 else width
                val endY = if (vertical) height else 0
                LinearGradient(
                    0f, 0f, endX.toFloat(), endY.toFloat(),
                    currentShimmer.colors,
                    currentShimmer.positions,
                    Shader.TileMode.CLAMP
                )
            }
        }

        shimmerPaint.shader = shader
    }
}
