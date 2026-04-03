package com.cometchat.uikit.kotlin.presentation.shared.shimmer

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.RectF
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.IntDef
import androidx.annotation.Px
import com.cometchat.uikit.kotlin.R
import kotlin.math.max
import kotlin.math.min

/**
 * CometChatShimmer is a configuration class for shimmer effects.
 * It defines the appearance and animation properties of the shimmer effect.
 */
class CometChatShimmer internal constructor() {

    companion object {
        private val TAG = CometChatShimmer::class.java.simpleName
        private const val COMPONENT_COUNT = 4
    }

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(Shape.LINEAR, Shape.RADIAL)
    annotation class Shape {
        companion object {
            const val LINEAR = 0
            const val RADIAL = 1
        }
    }

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(Direction.LEFT_TO_RIGHT, Direction.TOP_TO_BOTTOM, Direction.RIGHT_TO_LEFT, Direction.BOTTOM_TO_TOP)
    annotation class Direction {
        companion object {
            const val LEFT_TO_RIGHT = 0
            const val TOP_TO_BOTTOM = 1
            const val RIGHT_TO_LEFT = 2
            const val BOTTOM_TO_TOP = 3
        }
    }

    val positions = FloatArray(COMPONENT_COUNT)
    val colors = IntArray(COMPONENT_COUNT)
    val bounds = RectF()

    @Direction
    var direction: Int = Direction.LEFT_TO_RIGHT

    @ColorInt
    var highlightColor: Int = Color.WHITE

    @ColorInt
    var baseColor: Int = 0x4cffffff

    @Shape
    var shape: Int = Shape.LINEAR

    var fixedWidth: Int = 0
    var fixedHeight: Int = 0

    var widthRatio: Float = 1f
    var heightRatio: Float = 1f
    var intensity: Float = 0f
    var dropOff: Float = 0.5f
    var tilt: Float = 20f

    var clipToChildren: Boolean = true
    var autoStart: Boolean = true
    var alphaShimmer: Boolean = true

    var repeatCount: Int = ValueAnimator.INFINITE
    var repeatMode: Int = ValueAnimator.RESTART
    var animationDuration: Long = 1000L
    var repeatDelay: Long = 0L
    var startDelay: Long = 0L

    fun width(width: Int): Int {
        return if (fixedWidth > 0) fixedWidth else (widthRatio * width).toInt()
    }

    fun height(height: Int): Int {
        return if (fixedHeight > 0) fixedHeight else (heightRatio * height).toInt()
    }

    fun updateColors() {
        when (shape) {
            Shape.RADIAL -> {
                colors[0] = highlightColor
                colors[1] = highlightColor
                colors[2] = baseColor
                colors[3] = baseColor
            }
            else -> { // LINEAR
                colors[0] = baseColor
                colors[1] = highlightColor
                colors[2] = highlightColor
                colors[3] = baseColor
            }
        }
    }

    fun updatePositions() {
        when (shape) {
            Shape.RADIAL -> {
                positions[0] = 0f
                positions[1] = min(intensity, 1f)
                positions[2] = min(intensity + dropOff, 1f)
                positions[3] = 1f
            }
            else -> { // LINEAR
                positions[0] = max((1f - intensity - dropOff) / 2f, 0f)
                positions[1] = max((1f - intensity - 0.001f) / 2f, 0f)
                positions[2] = min((1f + intensity + 0.001f) / 2f, 1f)
                positions[3] = min((1f + intensity + dropOff) / 2f, 1f)
            }
        }
    }

    fun updateBounds(viewWidth: Int, viewHeight: Int) {
        val magnitude = max(viewWidth, viewHeight)
        val rad = Math.PI / 2f - Math.toRadians((tilt % 90f).toDouble())
        val hyp = magnitude / kotlin.math.sin(rad)
        val padding = 3 * ((hyp - magnitude) / 2f).toInt()
        bounds.set(
            -padding.toFloat(),
            -padding.toFloat(),
            (width(viewWidth) + padding).toFloat(),
            (height(viewHeight) + padding).toFloat()
        )
    }

    /**
     * Abstract builder class for creating CometChatShimmer instances.
     */
    abstract class Builder<T : Builder<T>> {
        protected val shimmer = CometChatShimmer()

        protected abstract fun getThis(): T

        fun consumeAttributes(context: Context, attrs: AttributeSet?): T {
            val a = context.obtainStyledAttributes(attrs, R.styleable.CometChatShimmerFrameLayout, 0, 0)
            return consumeAttributes(a)
        }

        open fun consumeAttributes(a: TypedArray): T {
            if (a.hasValue(R.styleable.CometChatShimmerFrameLayout_cometchatShimmerClipToChildren)) {
                setClipToChildren(a.getBoolean(R.styleable.CometChatShimmerFrameLayout_cometchatShimmerClipToChildren, shimmer.clipToChildren))
            }
            if (a.hasValue(R.styleable.CometChatShimmerFrameLayout_cometchatShimmerAutoStart)) {
                setAutoStart(a.getBoolean(R.styleable.CometChatShimmerFrameLayout_cometchatShimmerAutoStart, shimmer.autoStart))
            }
            if (a.hasValue(R.styleable.CometChatShimmerFrameLayout_cometchatShimmerBaseAlpha)) {
                setBaseAlpha(a.getFloat(R.styleable.CometChatShimmerFrameLayout_cometchatShimmerBaseAlpha, 0.3f))
            }
            if (a.hasValue(R.styleable.CometChatShimmerFrameLayout_cometchatShimmerHighlightAlpha)) {
                setHighlightAlpha(a.getFloat(R.styleable.CometChatShimmerFrameLayout_cometchatShimmerHighlightAlpha, 1f))
            }
            if (a.hasValue(R.styleable.CometChatShimmerFrameLayout_cometchatShimmerDuration)) {
                setDuration(a.getInt(R.styleable.CometChatShimmerFrameLayout_cometchatShimmerDuration, shimmer.animationDuration.toInt()).toLong())
            }
            if (a.hasValue(R.styleable.CometChatShimmerFrameLayout_cometchatShimmerRepeatCount)) {
                setRepeatCount(a.getInt(R.styleable.CometChatShimmerFrameLayout_cometchatShimmerRepeatCount, shimmer.repeatCount))
            }
            if (a.hasValue(R.styleable.CometChatShimmerFrameLayout_cometchatShimmerRepeatDelay)) {
                setRepeatDelay(a.getInt(R.styleable.CometChatShimmerFrameLayout_cometchatShimmerRepeatDelay, shimmer.repeatDelay.toInt()).toLong())
            }
            if (a.hasValue(R.styleable.CometChatShimmerFrameLayout_cometchatShimmerRepeatMode)) {
                setRepeatMode(a.getInt(R.styleable.CometChatShimmerFrameLayout_cometchatShimmerRepeatMode, shimmer.repeatMode))
            }
            if (a.hasValue(R.styleable.CometChatShimmerFrameLayout_cometchatShimmerStartDelay)) {
                setStartDelay(a.getInt(R.styleable.CometChatShimmerFrameLayout_cometchatShimmerStartDelay, shimmer.startDelay.toInt()).toLong())
            }
            if (a.hasValue(R.styleable.CometChatShimmerFrameLayout_cometchatShimmerDirection)) {
                when (a.getInt(R.styleable.CometChatShimmerFrameLayout_cometchatShimmerDirection, shimmer.direction)) {
                    Direction.TOP_TO_BOTTOM -> setDirection(Direction.TOP_TO_BOTTOM)
                    Direction.RIGHT_TO_LEFT -> setDirection(Direction.RIGHT_TO_LEFT)
                    Direction.BOTTOM_TO_TOP -> setDirection(Direction.BOTTOM_TO_TOP)
                    else -> setDirection(Direction.LEFT_TO_RIGHT)
                }
            }
            if (a.hasValue(R.styleable.CometChatShimmerFrameLayout_cometchatShimmerShape)) {
                when (a.getInt(R.styleable.CometChatShimmerFrameLayout_cometchatShimmerShape, shimmer.shape)) {
                    Shape.RADIAL -> setShape(Shape.RADIAL)
                    else -> setShape(Shape.LINEAR)
                }
            }
            if (a.hasValue(R.styleable.CometChatShimmerFrameLayout_cometchatShimmerDropOff)) {
                setDropOff(a.getFloat(R.styleable.CometChatShimmerFrameLayout_cometchatShimmerDropOff, shimmer.dropOff))
            }
            if (a.hasValue(R.styleable.CometChatShimmerFrameLayout_cometchatShimmerFixedWidth)) {
                setFixedWidth(a.getDimensionPixelSize(R.styleable.CometChatShimmerFrameLayout_cometchatShimmerFixedWidth, shimmer.fixedWidth))
            }
            if (a.hasValue(R.styleable.CometChatShimmerFrameLayout_cometchatShimmerFixedHeight)) {
                setFixedHeight(a.getDimensionPixelSize(R.styleable.CometChatShimmerFrameLayout_cometchatShimmerFixedHeight, shimmer.fixedHeight))
            }
            if (a.hasValue(R.styleable.CometChatShimmerFrameLayout_cometchatShimmerIntensity)) {
                setIntensity(a.getFloat(R.styleable.CometChatShimmerFrameLayout_cometchatShimmerIntensity, shimmer.intensity))
            }
            if (a.hasValue(R.styleable.CometChatShimmerFrameLayout_cometchatShimmerWidthRatio)) {
                setWidthRatio(a.getFloat(R.styleable.CometChatShimmerFrameLayout_cometchatShimmerWidthRatio, shimmer.widthRatio))
            }
            if (a.hasValue(R.styleable.CometChatShimmerFrameLayout_cometchatShimmerHeightRatio)) {
                setHeightRatio(a.getFloat(R.styleable.CometChatShimmerFrameLayout_cometchatShimmerHeightRatio, shimmer.heightRatio))
            }
            if (a.hasValue(R.styleable.CometChatShimmerFrameLayout_cometchatShimmerTilt)) {
                setTilt(a.getFloat(R.styleable.CometChatShimmerFrameLayout_cometchatShimmerTilt, shimmer.tilt))
            }
            return getThis()
        }

        fun copyFrom(other: CometChatShimmer): T {
            setDirection(other.direction)
            setShape(other.shape)
            setFixedWidth(other.fixedWidth)
            setFixedHeight(other.fixedHeight)
            setWidthRatio(other.widthRatio)
            setHeightRatio(other.heightRatio)
            setIntensity(other.intensity)
            setDropOff(other.dropOff)
            setTilt(other.tilt)
            setClipToChildren(other.clipToChildren)
            setAutoStart(other.autoStart)
            setRepeatCount(other.repeatCount)
            setRepeatMode(other.repeatMode)
            setRepeatDelay(other.repeatDelay)
            setStartDelay(other.startDelay)
            setDuration(other.animationDuration)
            shimmer.baseColor = other.baseColor
            shimmer.highlightColor = other.highlightColor
            return getThis()
        }

        fun setDirection(@Direction direction: Int): T {
            shimmer.direction = direction
            return getThis()
        }

        fun setShape(@Shape shape: Int): T {
            shimmer.shape = shape
            return getThis()
        }

        fun setFixedWidth(@Px fixedWidth: Int): T {
            require(fixedWidth >= 0) { "Given invalid width: $fixedWidth" }
            shimmer.fixedWidth = fixedWidth
            return getThis()
        }

        fun setFixedHeight(@Px fixedHeight: Int): T {
            require(fixedHeight >= 0) { "Given invalid height: $fixedHeight" }
            shimmer.fixedHeight = fixedHeight
            return getThis()
        }

        fun setWidthRatio(widthRatio: Float): T {
            require(widthRatio >= 0f) { "Given invalid width ratio: $widthRatio" }
            shimmer.widthRatio = widthRatio
            return getThis()
        }

        fun setHeightRatio(heightRatio: Float): T {
            require(heightRatio >= 0f) { "Given invalid height ratio: $heightRatio" }
            shimmer.heightRatio = heightRatio
            return getThis()
        }

        fun setIntensity(intensity: Float): T {
            require(intensity >= 0f) { "Given invalid intensity value: $intensity" }
            shimmer.intensity = intensity
            return getThis()
        }

        fun setDropOff(dropOff: Float): T {
            require(dropOff >= 0f) { "Given invalid dropOff value: $dropOff" }
            shimmer.dropOff = dropOff
            return getThis()
        }

        fun setTilt(tilt: Float): T {
            shimmer.tilt = tilt
            return getThis()
        }

        fun setBaseAlpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float): T {
            val intAlpha = (clamp(0f, 1f, alpha) * 255f).toInt()
            shimmer.baseColor = (intAlpha shl 24) or (shimmer.baseColor and 0x00FFFFFF)
            return getThis()
        }

        fun setHighlightAlpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float): T {
            val intAlpha = (clamp(0f, 1f, alpha) * 255f).toInt()
            shimmer.highlightColor = (intAlpha shl 24) or (shimmer.highlightColor and 0x00FFFFFF)
            return getThis()
        }

        fun setClipToChildren(status: Boolean): T {
            shimmer.clipToChildren = status
            return getThis()
        }

        fun setAutoStart(status: Boolean): T {
            shimmer.autoStart = status
            return getThis()
        }

        fun setRepeatCount(repeatCount: Int): T {
            shimmer.repeatCount = repeatCount
            return getThis()
        }

        fun setRepeatMode(mode: Int): T {
            shimmer.repeatMode = mode
            return getThis()
        }

        fun setRepeatDelay(millis: Long): T {
            require(millis >= 0) { "Given a negative repeat delay: $millis" }
            shimmer.repeatDelay = millis
            return getThis()
        }

        fun setStartDelay(millis: Long): T {
            require(millis >= 0) { "Given a negative start delay: $millis" }
            shimmer.startDelay = millis
            return getThis()
        }

        fun setDuration(millis: Long): T {
            require(millis >= 0) { "Given a negative duration: $millis" }
            shimmer.animationDuration = millis
            return getThis()
        }

        fun build(): CometChatShimmer {
            shimmer.updateColors()
            shimmer.updatePositions()
            return shimmer
        }

        private fun clamp(min: Float, max: Float, value: Float): Float {
            return min(max, max(min, value))
        }
    }

    /**
     * Builder for alpha-based shimmer effects.
     */
    class AlphaHighlightBuilder : Builder<AlphaHighlightBuilder>() {
        init {
            shimmer.alphaShimmer = true
        }

        override fun getThis(): AlphaHighlightBuilder = this
    }

    /**
     * Builder for color-based shimmer effects.
     */
    class ColorHighlightBuilder : Builder<ColorHighlightBuilder>() {
        init {
            shimmer.alphaShimmer = false
        }

        fun setHighlightColor(@ColorInt color: Int): ColorHighlightBuilder {
            shimmer.highlightColor = color
            return getThis()
        }

        fun setBaseColor(@ColorInt color: Int): ColorHighlightBuilder {
            shimmer.baseColor = (shimmer.baseColor and 0xFF000000.toInt()) or (color and 0x00FFFFFF)
            return getThis()
        }

        override fun consumeAttributes(a: TypedArray): ColorHighlightBuilder {
            super.consumeAttributes(a)
            if (a.hasValue(R.styleable.CometChatShimmerFrameLayout_cometchatShimmerBaseColor)) {
                setBaseColor(a.getColor(R.styleable.CometChatShimmerFrameLayout_cometchatShimmerBaseColor, shimmer.baseColor))
            }
            if (a.hasValue(R.styleable.CometChatShimmerFrameLayout_cometchatShimmerHighlightColor)) {
                setHighlightColor(a.getColor(R.styleable.CometChatShimmerFrameLayout_cometchatShimmerHighlightColor, shimmer.highlightColor))
            }
            return getThis()
        }

        override fun getThis(): ColorHighlightBuilder = this
    }
}
