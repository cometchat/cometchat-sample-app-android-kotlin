package com.cometchat.uikit.kotlin.presentation.shared.shimmer

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Shader
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * A [AppCompatTextView] that displays a shimmer animation effect over its text.
 * Used to indicate loading/thinking states in the AI assistant stream bubble.
 */
class ShimmerTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var linearGradient: LinearGradient? = null
    private var gradientMatrix: Matrix? = null
    private var translateX = 0f
    private var paintObj: android.graphics.Paint? = null
    private var viewWidth = 0
    private var isShimmerEnabled = true
    private val shimmerSpeed = 6f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0) {
            viewWidth = w
            paintObj = paint
            linearGradient = LinearGradient(
                (-viewWidth * 3).toFloat(), 0f,
                0f, 0f,
                intArrayOf(
                    currentTextColor,
                    CometChatTheme.getColorWhite(context),
                    CometChatTheme.getColorWhite(context),
                    currentTextColor
                ),
                floatArrayOf(0f, 0.4f, 0.5f, 1f),
                Shader.TileMode.MIRROR
            )
            paintObj?.shader = linearGradient
            gradientMatrix = Matrix()
        }
    }

    fun startShimmer() {
        isShimmerEnabled = true
        if (linearGradient != null && paintObj != null) {
            paintObj?.shader = linearGradient
        }
        invalidate()
    }

    fun isShimmerEnabled(): Boolean = isShimmerEnabled

    fun stopShimmer() {
        isShimmerEnabled = false
        paintObj?.shader = null
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val matrix = gradientMatrix ?: return
        translateX += shimmerSpeed
        if (translateX > 2 * viewWidth) {
            translateX = -viewWidth.toFloat()
        }
        matrix.setTranslate(translateX, 0f)
        linearGradient?.setLocalMatrix(matrix)
        invalidate()
    }
}
