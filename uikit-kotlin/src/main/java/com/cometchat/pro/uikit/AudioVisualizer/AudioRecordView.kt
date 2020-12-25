package com.cometchat.pro.uikit.AudioVisualizer

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.audiofx.Visualizer
import android.media.audiofx.Visualizer.OnDataCaptureListener
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.cometchat.pro.uikit.R
import utils.Utils
import java.util.*
import kotlin.jvm.internal.Intrinsics

class AudioRecordView : View {
    // Represents the minimum number of elements in the FFT array to skip for each capture.
    private val MIN_FFT_BUCKET_SIZE = 2

    // Represents the maximum number of elements in the FFT array to skip for each capture.
    private val MAX_FFT_BUCKET_SIZE = 10

    // The maximum decibel value we expect to support.
    // The Visualizer class will provide a real and imaginary component restricted to
    // a maximum value of 256.
    private val MAX_DB = (10 * Math.log10(256 * 256 + 256 * 256.toDouble())).toFloat()

    constructor(context: Context?) : super(context) {
        init()
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs!!)
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs!!)
    }

    internal enum class AlignTo {
        CENTER, BOTTOM
    }

    private var dataBytes: ByteArray? = null
    private var mVisualizer: Visualizer? = null
    private val maxReportableAmp = 22760f
    private val uninitialized = 0f
    private var chunkAlignTo = AlignTo.CENTER
    private val chunkPaint = Paint()
    private var lastUpdateTime = 0L
    private var usageWidth = 0f
    private val chunkHeights = ArrayList<Float>()
    private val chunkWidths = ArrayList<Float>()
    private val topBottomPadding = Utils.dpToPixel(6f, getResources())
    private var chunkSoftTransition = false
    private var chunkColor = Color.RED
    private var chunkWidth = Utils.dpToPixel(2f, getResources())
    private var chunkSpace = Utils.dpToPixel(1f, getResources())
    private var chunkMaxHeight = uninitialized
    private var chunkMinHeight = Utils.dpToPixel(3f, getResources())
    private var chunkRoundedCorners = false


//    fun AudioRecordView(context: Context) {
//        super(context)
//        init()
//    }
//
//    fun AudioRecordView(context: Context, attrs: AttributeSet) {
//        super(context, attrs)
//        init(attrs)
//    }
//
//    fun AudioRecordView(context: Context, attrs: AttributeSet, defStyleAttr: Int) {
//        super(context, attrs, defStyleAttr)
//        init(attrs)
//    }

    fun recreate() {
        usageWidth = 0.0f
        chunkWidths.clear()
        chunkHeights.clear()
        invalidate()
    }

    fun update(fft: Int) {
        handleNewFFT(fft)
        invalidate()
        lastUpdateTime = System.currentTimeMillis()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawChunks(canvas)
    }

    private fun init() {
        chunkPaint.strokeWidth = chunkWidth
        chunkPaint.color = chunkColor
    }

    private fun init(attrs: AttributeSet) {
        val a: TypedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.AudioRecordView, 0, 0)
        try {
            chunkSpace = a.getDimension(R.styleable.AudioRecordView_chunkSpace, chunkSpace)
            chunkMaxHeight = a.getDimension(R.styleable.AudioRecordView_chunkMaxHeight, chunkMaxHeight)
            chunkMinHeight = a.getDimension(R.styleable.AudioRecordView_chunkMinHeight, chunkMinHeight)
            setChunkRoundedCorners(a.getBoolean(R.styleable.AudioRecordView_chunkRoundedCorners, chunkRoundedCorners))
            setChunkWidth(a.getDimension(R.styleable.AudioRecordView_chunkWidth, chunkWidth))
            setChunkColor(a.getColor(R.styleable.AudioRecordView_chunkColor, chunkColor))
            val var5 = a.getInt(R.styleable.AudioRecordView_chunkAlignTo, chunkAlignTo.ordinal)
            chunkAlignTo = AlignTo.BOTTOM
            chunkSoftTransition = a.getBoolean(R.styleable.AudioRecordView_chunkSoftTransition, chunkSoftTransition)
            setWillNotDraw(false)
            chunkPaint.isAntiAlias = true
        } finally {
            a.recycle()
        }
    }

    fun setAudioSessionId(audioSessionId: Int) {
        if (mVisualizer != null) {
            mVisualizer!!.release()
            mVisualizer = null
        }
        mVisualizer = Visualizer(audioSessionId)
        mVisualizer!!.captureSize = Visualizer.getCaptureSizeRange()[1]
        mVisualizer!!.setDataCaptureListener(object : OnDataCaptureListener {
            override fun onWaveFormDataCapture(visualizer: Visualizer, bytes: ByteArray,
                                               samplingRate: Int) {
            }

            override fun onFftDataCapture(visualizer: Visualizer, bytes: ByteArray,
                                          samplingRate: Int) {
                dataBytes = bytes
            }
        }, Visualizer.getMaxCaptureRate() / 2, false, true)
        mVisualizer!!.enabled = true
    }

    fun updateVisualizer() {
        update(calculateRMSLevel())
    }

    fun calculateRMSLevel(): Int {
        var amplitude = 0
        //System.out.println("::::: audioData :::::"+audioData);
        if (dataBytes != null) {
            val dataSize = dataBytes!!.size / 2 - 1
            var allAmps: FloatArray? = null
            if (allAmps == null || allAmps.size != dataSize) allAmps = FloatArray(dataSize)
            for (i in 0 until dataSize) {
                val re = dataBytes!![2 * i].toFloat()
                val im = dataBytes!![2 * i + 1].toFloat()
                val sqMag = re * re + im * im
                var k = 1f
                if (i == 0 || i == dataSize - 1) k = 2f
                allAmps[i] = (k * Math.sqrt(sqMag.toDouble())).toFloat()
                //                double y = (dataBytes[i*2] << 8| dataBytes[i*2+1]) / 32768.0;
                // depending on your endianness:
                // double y = (audioData[i*2]<<8 | audioData[i*2+1]) / 32768.0
//                amplitude += Math.abs(y);
                amplitude += (allAmps[i] * 1024 / dataSize).toInt()
                Log.e("loopRMSLevel: ", allAmps[i].toString() + "")
            }
            Log.e("calculateRMSLevel: ", "$amplitude dataSize:$dataSize")
            return amplitude
            //            amplitude = amplitude * chunkMaxHeight / MAX_DB;
        }
        //Add this data to buffer for display
        Log.e("calculateRMSLevel: ", amplitude.toString() + "=" + amplitude)
        return amplitude
    }

    private fun handleNewFFT(fft: Int) {
        if (fft == 0) {
            return
        }
        val chunkHorizontalScale = chunkWidth + chunkSpace
        val maxChunkCount: Float = getWidth() / chunkHorizontalScale
        if (!chunkHeights.isEmpty() && chunkHeights.size >= maxChunkCount) {
            chunkHeights.removeAt(0)
        } else {
            usageWidth += chunkHorizontalScale
            chunkWidths.add(chunkWidths.size, usageWidth)
        }
        if (chunkMaxHeight == uninitialized) {
            chunkMaxHeight = getHeight() - topBottomPadding * 2
        } else if (chunkMaxHeight > getHeight() - topBottomPadding * 2) {
            chunkMaxHeight = getHeight() - topBottomPadding * 2
        }
        val verticalDrawScale = chunkMaxHeight - chunkMinHeight
        if (verticalDrawScale == 0.0f) {
            return
        }
        val point = maxReportableAmp / verticalDrawScale
        if (point == 0.0f) {
            return
        }
        var fftPoint = fft / point
        if (chunkSoftTransition && !chunkHeights.isEmpty()) {
            val updateTimeInterval = System.currentTimeMillis() - lastUpdateTime
            val scaleFactor = calculateScaleFactor(updateTimeInterval)
            val prevFftWithoutAdditionalSize = chunkHeights[chunkHeights.size - 1]!! - chunkMinHeight
            fftPoint = Utils.softTransition(fftPoint, prevFftWithoutAdditionalSize, 2.2f, scaleFactor)
        }
        fftPoint += chunkMinHeight
        if (fftPoint > chunkMaxHeight) {
            fftPoint = chunkMaxHeight
        } else if (fftPoint < chunkMinHeight) {
            fftPoint = chunkMinHeight
        }
        chunkHeights.add(chunkHeights.size, fftPoint)
    }

    private fun calculateScaleFactor(updateTimeInterval: Long): Float {
        var range = 50L
        if (0L <= updateTimeInterval && range >= updateTimeInterval) {
            return 1.6f
        }
        range = 100L
        if (50L <= updateTimeInterval && range >= updateTimeInterval) {
            return 2.2f
        }
        range = 150L
        if (100L <= updateTimeInterval && range >= updateTimeInterval) {
            return 2.8f
        }
        range = 200L
        if (150L <= updateTimeInterval && range >= updateTimeInterval) {
            return 3.4f
        }
        range = 250L
        if (200L <= updateTimeInterval && range >= updateTimeInterval) {
            return 4.2f
        }
        range = 500L
        return if (200L <= updateTimeInterval && range >= updateTimeInterval) {
            4.8f
        } else 5.4f
    }

    private fun drawChunks(canvas: Canvas) {
        drawAlignCenter(canvas)
    }

    private fun drawAlignCenter(canvas: Canvas) {
        val verticalCenter: Int = this.getHeight() / 2
        var i = 0
        val var4 = chunkHeights.size - 1
        while (i < var4) {
            val var10000: Any = chunkWidths[i]
            Intrinsics.checkExpressionValueIsNotNull(var10000, "chunkWidths[i]")
            val chunkX = (var10000 as Number).toFloat()
            val startY = verticalCenter.toFloat() - (chunkHeights[i] as Number?)!!.toFloat() / 2.toFloat()
            val stopY = verticalCenter.toFloat() + (chunkHeights[i] as Number?)!!.toFloat() / 2.toFloat()
            canvas.drawLine(chunkX, startY, chunkX, stopY, chunkPaint)
            ++i
        }
    }

    private fun drawAlignBottom(canvas: Canvas) {
        for (i in 0 until chunkHeights.size - 1) {
            val chunkX = chunkWidths[i]
            val startY = getHeight() as Float - topBottomPadding
            val stopY = startY - chunkHeights[i]!!
            canvas.drawLine(chunkX, startY, chunkX, stopY, chunkPaint)
        }
    }


    fun getChunkSoftTransition(): Boolean {
        return chunkSoftTransition
    }

    fun setChunkSoftTransition(var1: Boolean) {
        chunkSoftTransition = var1
    }

    fun getChunkColor(): Int {
        return chunkColor
    }

    fun setChunkColor(value: Int) {
        chunkPaint.color = value
        chunkColor = value
    }

    fun getChunkWidth(): Float {
        return chunkWidth
    }

    fun setChunkWidth(value: Float) {
        chunkPaint.strokeWidth = value
        chunkWidth = value
    }

    fun getChunkSpace(): Float {
        return chunkSpace
    }

    fun setChunkSpace(var1: Float) {
        chunkSpace = var1
    }

    fun getChunkMaxHeight(): Float {
        return chunkMaxHeight
    }

    fun setChunkMaxHeight(var1: Float) {
        chunkMaxHeight = var1
    }

    fun getChunkMinHeight(): Float {
        return chunkMinHeight
    }

    fun setChunkMinHeight(var1: Float) {
        chunkMinHeight = var1
    }

    fun getChunkRoundedCorners(): Boolean {
        return chunkRoundedCorners
    }

    fun setChunkRoundedCorners(value: Boolean) {
        if (value) {
            chunkPaint.strokeCap = Paint.Cap.ROUND
        } else {
            chunkPaint.strokeCap = Paint.Cap.BUTT
        }
        chunkRoundedCorners = value
    }
}