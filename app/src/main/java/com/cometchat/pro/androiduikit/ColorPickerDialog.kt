package com.cometchat.pro.androiduikit

import android.app.Dialog
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.view.MotionEvent
import android.view.View

class ColorPickerDialog : Dialog {



    interface OnColorChangedListener {
        fun colorChanged(key: String?, color: Int)
    }

    private var mListener: OnColorChangedListener? = null
    private var mInitialColor = 0
    private  var mDefaultColor:Int = 0
    private var mKey: String? = null

    constructor(context: Context?, listener: OnColorChangedListener,
                key: String, initialColor: Int, defaultColor: Int) :super(context!!){

        mListener = listener
        mKey = key
        mInitialColor = initialColor
        mDefaultColor = defaultColor
    }

    private class ColorPickerView : View{
        private var mPaint: Paint? = null
        private var mCurrentHue = 0f
        private var mCurrentX = 0
        private  var mCurrentY:Int = 0
        private var mCurrentColor = 0
        private  var mDefaultColor:Int = 0
        private val mHueBarColors = IntArray(258)
        private val mMainColors = IntArray(65536)
        private var mListener: OnColorChangedListener? = null

        constructor(c: Context?, l: OnColorChangedListener?, color: Int,
                    defaultColor: Int):super(c){
            mListener = l
            mDefaultColor = defaultColor

            // Get the current hue from the current color and update the main
            // color field

            // Get the current hue from the current color and update the main
            // color field
            val hsv = FloatArray(3)
            Color.colorToHSV(color, hsv)
            mCurrentHue = hsv[0]
            updateMainColors()

            mCurrentColor = color

            // Initialize the colors of the hue slider bar

            // Initialize the colors of the hue slider bar
            var index = 0
            for (i in 0 until 256 step 256/42) // Red (#f00) to pink
            {
                mHueBarColors[index] = Color.rgb(255, 0, i)
                index++
            }
            for (i in 0 until 256 step 256/42) // Pink (#f0f) to blue
            // (#00f)
            {
                mHueBarColors[index] = Color.rgb(255 - i, 0, 255)
                index++
            }
            for (i in 0 until 256 step 256/42) // Blue (#00f) to light
            // blue (#0ff)
            {
                mHueBarColors[index] = Color.rgb(0, i, 255);
                index++;
            }
            for (i in 0 until 256 step 256/42) // Light blue (#0ff) to
            // green (#0f0)
            {
                mHueBarColors[index] = Color.rgb(0, 255, 255 - i);
                index++;
            }
            for (i in 0 until 256 step 256/42) // Green (#0f0) to yellow
            // (#ff0)
            {
                mHueBarColors[index] = Color.rgb(i, 255, 0);
                index++;
            }
            for (i in 0 until 256 step 256/42) // Yellow (#ff0) to red
            // (#f00)
            {
                mHueBarColors[index] = Color.rgb(255, 255 - i, 0);
                index++;
            }

            // Initializes the Paint that will draw the View
            mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            mPaint!!.textAlign = Paint.Align.CENTER
            mPaint!!.textSize = 16f
        }

        // Get the current selected color from the hue bar
        private fun getCurrentMainColor(): Int{
            val translatedHue = 255 - (mCurrentHue * 255 / 360).toInt()
            var index = 0
            for (i in 0 until 256 step 256/42){
                if (index == translatedHue) return Color.rgb(255, 0, i)
                index++
            }
            for (i in 0 until 256 step 256/42){
                if (index == translatedHue) return Color.rgb(255 - i, 0, 255)
                index++
            }
            for (i in 0 until 256 step 256/42){
                if (index == translatedHue) return Color.rgb(0, i, 255)
                index++
            }
            for (i in 0 until 256 step 256/42){
                if (index == translatedHue) return Color.rgb(0, 255, 255 - i)
                index++
            }
            for (i in 0 until 256 step 256/42){
                if (index == translatedHue) return Color.rgb(i, 255, 0)
                index++
            }
            for (i in 0 until 256 step 256/42){
                if (index == translatedHue) return Color.rgb(255, 255 - i, 0)
                index++
            }
            return Color.RED
        }

        // Update the main field colors depending on the current selected hue
        private fun updateMainColors(){
            val mainColor = getCurrentMainColor()
            var index = 0
            val topColors = IntArray(256)
            for (i in 0 until 256){
                for (j in 0 until 256){
                    if (i == 0) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            mMainColors[index] = Color.rgb(
                                    255 - (255 - Color.red(mainColor)) * x / 255,
                                    255 - (255 - Color.green(mainColor)) * x / 255,
                                    255 - (255 - Color.blue(mainColor)) * x / 255)
                        }
                        topColors[x.toInt()] = mMainColors[index]
                    } else mMainColors[index] = Color.rgb(
                            (255 - i) * Color.red(topColors[x.toInt()]) / 255,
                            (255 - i) * Color.green(topColors[x.toInt()]) / 255,
                            (255 - i) * Color.blue(topColors[x.toInt()]) / 255)
                    index++
                }
            }
        }

        override fun onDraw(canvas: Canvas?) {
            super.onDraw(canvas)
            val translatedHue = 255 - (mCurrentHue * 255 / 360).toInt()
            // Display all the colors of the hue bar with lines
            for (j in 0 until 256){
                // If this is not the current selected hue, display the actual
                // color
                if (translatedHue != j) {
                    mPaint!!.color = mHueBarColors[x.toInt()]
                    mPaint!!.strokeWidth = 1f
                } else // else display a slightly larger black line
                {
                    mPaint!!.color = Color.BLACK
                    mPaint!!.strokeWidth = 3f
                }
                canvas!!.drawLine(x + 10.toFloat(), 0f, x + 10.toFloat(), 40f, mPaint!!)
            }
            // Display the main field colors using LinearGradient
            for (j in 0 until 256){
                val colors = IntArray(2)
                colors[0] = mMainColors[x.toInt()]
                colors[1] = Color.BLACK
                val shader: Shader = LinearGradient(0F, 50F, 0F, 306F, colors, null,
                        Shader.TileMode.REPEAT)
                mPaint!!.shader = shader
                canvas!!.drawLine(x + 10.toFloat(), 50f, x + 10.toFloat(), 306f, mPaint!!)
            }
            mPaint!!.shader = null

            // Display the circle around the currently selected color in the
            // main field
            if (mCurrentX != 0 && mCurrentY != 0) {
                mPaint!!.style = Paint.Style.STROKE
                mPaint!!.color = Color.BLACK
                canvas!!.drawCircle(mCurrentX.toFloat(), mCurrentY.toFloat(), 10f, mPaint!!)
            }


            // Draw a 'button' with the currently selected color
            mPaint!!.style = Paint.Style.FILL
            mPaint!!.color = mCurrentColor
            canvas!!.drawRect(10f, 316f, 138f, 356f, mPaint!!)

            // Set the text color according to the brightness of the color
            if ((Color.red(mCurrentColor) + Color.green(mCurrentColor)
                            + Color.blue(mCurrentColor)) < 384) mPaint!!.color = Color.WHITE else mPaint!!.color = Color.BLACK
            canvas.drawText(
                    resources
                            .getString(R.string.settings_bg_color_confirm), 74f, 340f, mPaint!!)

            // Draw a 'button' with the default color
            mPaint!!.style = Paint.Style.FILL
            mPaint!!.color = mDefaultColor
            canvas.drawRect(138f, 316f, 466f, 556f, mPaint!!)


            // Set the text color according to the brightness of the color
            if ((Color.red(mDefaultColor) + Color.green(mDefaultColor)
                            + Color.blue(mDefaultColor)) < 384) mPaint!!.color = Color.WHITE else mPaint!!.color = Color.BLACK
            canvas.drawText(
                    resources.getString(
                            R.string.settings_default_color_confirm), 202f, 340f,
                    mPaint!!)
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            setMeasuredDimension(566, 566)
        }

        override fun onTouchEvent(event: MotionEvent?): Boolean {
            if (event!!.action != MotionEvent.ACTION_DOWN) return true
            val x = event.x
            val y = event.y


            // If the touch event is located in the hue bar
            if (x > 10 && x < 266 && y > 0 && y < 40) {
                // Update the main field colors
                mCurrentHue = (255 - x) * 360 / 255
                updateMainColors()

                // Update the current selected color
                val transX = mCurrentX - 10
                val transY = mCurrentY - 60
                val index = 256 * (transY - 1) + transX
                if (index > 0 && index < mMainColors.size) mCurrentColor = mMainColors[256 * (transY - 1) + transX]

                // Force the redraw of the dialog
                invalidate()
            }

            // If the touch event is located in the main field
            if (x > 10 && x < 266 && y > 50 && y < 306) {
                mCurrentX = x.toInt()
                mCurrentY = y.toInt()
                val transX = mCurrentX - 10
                val transY = mCurrentY - 60
                val index = 256 * (transY - 1) + transX
                if (index > 0 && index < mMainColors.size) {
                    // Update the current color
                    mCurrentColor = mMainColors[index]
                    // Force the redraw of the dialog
                    invalidate()
                }
            }

            // If the touch event is located in the left button, notify the
            // listener with the current color
            if (x > 10 && x < 138 && y > 316 && y < 356) mListener!!.colorChanged("", mCurrentColor)


            // If the touch event is located in the right button, notify the
            // listener with the default color
            if (x > 138 && x < 266 && y > 316 && y < 356) mListener!!.colorChanged("", mDefaultColor)

            return true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val l = object : OnColorChangedListener {
            override fun colorChanged(key: String?, color: Int) {
                mListener!!.colorChanged(mKey, color)
                dismiss()
            }
        }
        setContentView(ColorPickerView(context, l, mInitialColor, mDefaultColor))
        setTitle(R.string.settings_bg_color_dialog)
    }

}