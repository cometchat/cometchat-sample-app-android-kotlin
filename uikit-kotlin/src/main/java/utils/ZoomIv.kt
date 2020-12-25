package utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.widget.ImageView

public class ZoomIv(context: Context, attr: AttributeSet?) : androidx.appcompat.widget.AppCompatImageView(context, attr) {
    var matrixs = Matrix()
    var mode = NONE
    var last = PointF()
    var start = PointF()
    var minScale = 1f
    var maxScale = 4f
    var m: FloatArray
    var redundantXSpace = 0f
    var redundantYSpace = 0f
    var width = 0f
    var height = 0f
    var saveScale = 1f
    var right = 0f
    var bottom = 0f
    var origWidth = 0f
    var origHeight = 0f
    var bmWidth = 0f
    var bmHeight = 0f
    var mScaleDetector: ScaleGestureDetector
    var c: Context
    override fun setImageBitmap(bm: Bitmap) {
        super.setImageBitmap(bm)
        bmWidth = bm.width.toFloat()
        bmHeight = bm.height.toFloat()
    }

    fun setMaxZoom(x: Float) {
        maxScale = x
    }

    private inner class ScaleListener : SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            mode = ZOOM
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            var mScaleFactor = detector.scaleFactor
            val origScale = saveScale
            saveScale *= mScaleFactor
            if (saveScale > maxScale) {
                saveScale = maxScale
                mScaleFactor = maxScale / origScale
            } else if (saveScale < minScale) {
                saveScale = minScale
                mScaleFactor = minScale / origScale
            }
            right = width * saveScale - width - 2 * redundantXSpace * saveScale
            bottom = height * saveScale - height - 2 * redundantYSpace * saveScale
            if (origWidth * saveScale <= width || origHeight * saveScale <= height) {
                matrixs.postScale(mScaleFactor, mScaleFactor, width / 2, height / 2)
                if (mScaleFactor < 1) {
                    matrixs.getValues(m)
                    val x = m[Matrix.MTRANS_X]
                    val y = m[Matrix.MTRANS_Y]
                    if (mScaleFactor < 1) {
                        if (Math.round(origWidth * saveScale) < width) {
                            if (y < -bottom) matrixs.postTranslate(0f, -(y + bottom)) else if (y > 0) matrixs.postTranslate(0f, -y)
                        } else {
                            if (x < -right) matrixs.postTranslate(-(x + right), 0f) else if (x > 0) matrixs.postTranslate(-x, 0f)
                        }
                    }
                }
            } else {
                matrixs.postScale(mScaleFactor, mScaleFactor, detector.focusX, detector.focusY)
                matrixs.getValues(m)
                val x = m[Matrix.MTRANS_X]
                val y = m[Matrix.MTRANS_Y]
                if (mScaleFactor < 1) {
                    if (x < -right) matrixs.postTranslate(-(x + right), 0f) else if (x > 0) matrixs.postTranslate(-x, 0f)
                    if (y < -bottom) matrixs.postTranslate(0f, -(y + bottom)) else if (y > 0) matrixs.postTranslate(0f, -y)
                }
            }
            return true
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        width = MeasureSpec.getSize(widthMeasureSpec).toFloat()
        height = MeasureSpec.getSize(heightMeasureSpec).toFloat()
        //Fit to screen.
        val scale: Float
        val scaleX = width / bmWidth
        val scaleY = height / bmHeight
        scale = Math.min(scaleX, scaleY)
        matrixs.setScale(scale, scale)
        imageMatrix = matrixs
        saveScale = 1f

        // Center the image
        redundantYSpace = height - scale * bmHeight
        redundantXSpace = width - scale * bmWidth
        redundantYSpace /= 2f
        redundantXSpace /= 2f
        matrixs.postTranslate(redundantXSpace, redundantYSpace)
        origWidth = width - 2 * redundantXSpace
        origHeight = height - 2 * redundantYSpace
        right = width * saveScale - width - 2 * redundantXSpace * saveScale
        bottom = height * saveScale - height - 2 * redundantYSpace * saveScale
        imageMatrix = matrixs
    }

    companion object {
        const val NONE = 0
        const val DRAG = 1
        const val ZOOM = 2
        const val CLICK = 3
    }

    init {
        super.setClickable(true)
        this.c = context
        mScaleDetector = ScaleGestureDetector(context, ScaleListener())
        matrixs.setTranslate(1f, 1f)
        m = FloatArray(9)
        imageMatrix = matrixs
        scaleType = ScaleType.MATRIX
        setOnTouchListener { v, event ->
            mScaleDetector.onTouchEvent(event)
            matrixs.getValues(m)
            val x = m[Matrix.MTRANS_X]
            val y = m[Matrix.MTRANS_Y]
            val curr = PointF(event.x, event.y)
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    last[event.x] = event.y
                    start.set(last)
                    mode = DRAG
                }
                MotionEvent.ACTION_POINTER_DOWN -> {
                    last[event.x] = event.y
                    start.set(last)
                    mode = ZOOM
                }
                MotionEvent.ACTION_MOVE ->                         //if the mode is ZOOM or
                    //if the mode is DRAG and already zoomed
                    if (mode == ZOOM || mode == DRAG && saveScale > minScale) {
                        var deltaX = curr.x - last.x // x difference
                        var deltaY = curr.y - last.y // y difference
                        val scaleWidth = Math.round(origWidth * saveScale).toFloat() // width after applying current scale
                        val scaleHeight = Math.round(origHeight * saveScale).toFloat() // height after applying current scale
                        //if scaleWidth is smaller than the views width
                        //in other words if the image width fits in the view
                        //limit left and right movement
                        if (scaleWidth < width) {
                            deltaX = 0f
                            if (y + deltaY > 0) deltaY = -y else if (y + deltaY < -bottom) deltaY = -(y + bottom)
                        } else if (scaleHeight < height) {
                            deltaY = 0f
                            if (x + deltaX > 0) deltaX = -x else if (x + deltaX < -right) deltaX = -(x + right)
                        } else {
                            if (x + deltaX > 0) deltaX = -x else if (x + deltaX < -right) deltaX = -(x + right)
                            if (y + deltaY > 0) deltaY = -y else if (y + deltaY < -bottom) deltaY = -(y + bottom)
                        }
                        //move the image with the matrix
                        matrixs.postTranslate(deltaX, deltaY)
                        //set the last touch location to the current
                        last[curr.x] = curr.y
                    }
                MotionEvent.ACTION_UP -> {
                    mode = NONE
                    val xDiff = Math.abs(curr.x - start.x).toInt()
                    val yDiff = Math.abs(curr.y - start.y).toInt()
                    if (xDiff < CLICK && yDiff < CLICK) performClick()
                }
                MotionEvent.ACTION_POINTER_UP -> mode = NONE
            }
            imageMatrix = matrixs
            invalidate()
            true
        }
    }
}