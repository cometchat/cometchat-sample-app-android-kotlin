package com.inscripts.cometchatpulse.CustomView

import android.app.Activity
import android.content.Context
import android.graphics.PorterDuff
import android.media.MediaPlayer
import android.os.SystemClock
import androidx.appcompat.content.res.AppCompatResources
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.inscripts.cometchatpulse.Helpers.CCPermissionHelper
import com.inscripts.cometchatpulse.Helpers.RecordListener
import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.StringContract
import com.inscripts.cometchatpulse.Utils.AnimUtil
import com.inscripts.cometchatpulse.Utils.CommonUtil
import java.io.IOException

class RecordAudio : RelativeLayout {

    private var recordMic: ImageView? = null

    private var counterTime: Chronometer? = null

    private var slideToCancel: TextView? = null

    private var arrow: ImageView? = null

    private var initialX: Float = 0.toFloat()
    private var difX = 0f

    private var cancelOffset = DEFAULT_CANCEL_OFFSET.toFloat()

    private var startTime: Long = 0
    private var elapsedTime: Long = 0

    private var recordListener: RecordListener? = null

    private var isSwiped: Boolean = false
    private var isLessThanSecondAllowed = false

    private var isSoundEnabled = true

    private var RECORD_START = R.raw.record_start

    private var RECORD_FINISHED = R.raw.record_finished

    private var RECORD_ERROR = R.raw.record_error

    private var player: MediaPlayer? = null

    private val animUtil: AnimUtil? = null

    private var slideToCancelLayout: FrameLayout? = null


    constructor(context: Context) : super(context) {
        initViewComponent(context, null, -1, -1)
    }


    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {

        initViewComponent(context, attrs, -1, -1)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

        initViewComponent(context, attrs, defStyleAttr, -1)
    }

    private fun initViewComponent(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        val view = View.inflate(context, R.layout.record_audio, null)
        addView(view)

        val viewGroup = view.parent as ViewGroup
        viewGroup.clipChildren = false

        arrow = view.findViewById(R.id.left_arrow)
        slideToCancel = view.findViewById(R.id.slide_to_cancel_text)
        counterTime = view.findViewById(R.id.record_time)
        recordMic = view.findViewById(R.id.record_mic)
        slideToCancelLayout = view.findViewById(R.id.slide_to_cancel_Layout)

        recordMic?.drawable?.setColorFilter(StringContract.Color.primaryColor,PorterDuff.Mode.SRC_ATOP)
        arrow?.drawable?.setColorFilter(StringContract.Color.primaryColor,PorterDuff.Mode.SRC_ATOP)


        hideViews(true)


        if (attrs != null && defStyleAttr == -1 && defStyleRes == -1) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RecordAudio,
                    defStyleAttr, defStyleRes)

            val slideArrowResource = typedArray.getResourceId(R.styleable.RecordAudio_slide_to_cancel_arrow, -1)
            val slideToCancelText = typedArray.getString(R.styleable.RecordAudio_slide_to_cancel_text)
            val slideMarginRight = typedArray.getDimension(R.styleable.RecordAudio_slide_to_cancel_margin_right, 30f).toInt()
            val counterTimeColor = typedArray.getColor(R.styleable.RecordAudio_counter_time_color, -1)
            val arrowColor = typedArray.getColor(R.styleable.RecordAudio_slide_to_cancel_arrow_color, -1)


            val cancelBounds = typedArray.getDimensionPixelSize(R.styleable.RecordAudio_slide_to_cancel_bounds, -1)

            if (cancelBounds != -1)
                setCancelOffset(cancelBounds.toFloat(), false)


            if (slideArrowResource != -1) {
                val slideArrow = AppCompatResources.getDrawable(getContext(), slideArrowResource)
                arrow!!.setImageDrawable(slideArrow)
            }

            if (slideToCancelText != null)
                slideToCancel!!.text = slideToCancelText

            if (counterTimeColor != -1)
                setCounterTimeColor(counterTimeColor)


            if (arrowColor != -1)
                setSlideToCancelArrowColor(arrowColor)



            setMarginRight(slideMarginRight, true)

            typedArray.recycle()
        }

    }


    private fun hideViews(hideSmallMic: Boolean) {
        slideToCancelLayout!!.visibility = View.GONE
        counterTime!!.visibility = View.GONE
        if (hideSmallMic)
            recordMic!!.visibility = View.GONE
    }

    private fun showViews() {
        slideToCancelLayout!!.visibility = View.VISIBLE
        recordMic!!.visibility = View.VISIBLE
        counterTime!!.visibility = View.VISIBLE
    }


    private fun isLessThanOneSecond(time: Long): Boolean {
        return time <= 1000
    }


    private fun playSound(soundRes: Int) {

        if (isSoundEnabled) {
            if (soundRes == 0)
                return

            try {
                player = MediaPlayer()
                val afd = context!!.resources.openRawResourceFd(soundRes) ?: return
                player!!.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                player!!.prepare()
                player!!.start()
                player!!.setOnCompletionListener { mp -> mp.release() }
                player!!.isLooping = false
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }


    }


    fun onActionDown(recordMicButton: RecordMicButton, context: Context) {

        if (recordListener != null) {
            if (CCPermissionHelper.hasPermissions(context, *StringContract.RequestPermission.RECORD_PERMISSION)) {
                recordListener!!.onRecordStart()
            } else {
                CCPermissionHelper.requestPermissions(context as Activity, StringContract.RequestPermission.RECORD_PERMISSION,
                        StringContract.RequestCode.RECORD_CODE)
            }
        }

        AnimUtil.setStartRecorded(true)
        AnimUtil.resetSmallMic(recordMic!!)


        recordMicButton.startScale()

        initialX = recordMicButton.getX()

        playSound(RECORD_START)

        showViews()

        AnimUtil.animateMic(recordMic!!)

        counterTime!!.base = SystemClock.elapsedRealtime()

        startTime = System.currentTimeMillis()

        counterTime!!.start()
        isSwiped = false

    }


     fun onActionMove(recordMicButton: RecordMicButton, motionEvent: MotionEvent) {


        val time = System.currentTimeMillis() - startTime

        if (!isSwiped) {

            //Swipe To Cancel
            val slideX = slideToCancelLayout!!.x
            val right = counterTime!!.right

            if (slideX != 0f && slideX <= right + cancelOffset) {

                //if the time was less than one second then do not start basket animation
                if (isLessThanOneSecond(time)) {
                    hideViews(true)
                    recordMic?.let { AnimUtil.clearAlphaAnimation(false, it) }

                } else {
                    hideViews(true)
                    recordMic?.let { AnimUtil.clearAlphaAnimation(true, it) }
                }

                AnimUtil.moveSlideToCancel(recordMicButton, slideToCancelLayout!!, initialX, difX)
                counterTime!!.stop()
                isSwiped = true

                AnimUtil.setStartRecorded(false)
                if (recordListener != null) {

                    recordListener!!.onRecordCancel()
                }


            } else {

                if (motionEvent.getRawX() < initialX) {
                    recordMicButton.animate()
                            .x(motionEvent.getRawX())
                            .setDuration(0)
                            .start()

                    if (difX == 0f)
                        difX = initialX - slideToCancelLayout!!.x


                    slideToCancelLayout!!.animate()
                            .x(motionEvent.getRawX() - difX)
                            .setDuration(0)
                            .start()

                }


            }

        }
    }

     fun onActionUp(recordMicButton: RecordMicButton) {

        elapsedTime = System.currentTimeMillis() - startTime

        if (!isLessThanSecondAllowed && isLessThanOneSecond(elapsedTime) && !isSwiped) {
            if (recordListener != null)
                recordListener!!.onRecordLessTime()
            AnimUtil.setStartRecorded(false)
            playSound(RECORD_ERROR)


        } else {
            if (recordListener != null && !isSwiped)
                recordListener!!.onRecordFinish(elapsedTime)

            AnimUtil.setStartRecorded(false)

            if (!isSwiped)
                playSound(RECORD_FINISHED)

        }


        //if user has swiped then do not hide SmallMic since it will be hidden after swipe Animation
        hideViews(!isSwiped)


        if (!isSwiped)
            recordMic?.let { AnimUtil.clearAlphaAnimation(true, it) }

         slideToCancelLayout?.let { AnimUtil.moveSlideToCancel(recordMicButton, it, initialX, difX) }
        counterTime!!.stop()


    }


    private fun setMarginRight(marginRight: Int, convertToDp: Boolean) {
        val layoutParams = slideToCancelLayout!!.layoutParams as RelativeLayout.LayoutParams
        if (convertToDp) {
            layoutParams.rightMargin = CommonUtil.dpToPx(context, marginRight.toFloat()).toInt()
        } else
            layoutParams.rightMargin = marginRight

        slideToCancelLayout!!.layoutParams = layoutParams
    }


    fun setOnRecordListener(recrodListener: RecordListener) {
        this.recordListener = recrodListener
    }


    fun setSoundEnabled(isEnabled: Boolean) {
        isSoundEnabled = isEnabled
    }

    fun setLessThanSecondAllowed(isAllowed: Boolean) {
        isLessThanSecondAllowed = isAllowed
    }

    fun setSlideToCancelText(text: String) {
        slideToCancel!!.text = text
    }

    fun setSlideToCancelTextColor(color: Int) {
        slideToCancel!!.setTextColor(color)
    }

    fun setSmallMicColor(color: Int) {
        recordMic!!.setColorFilter(color)
    }

    fun setSmallMicIcon(icon: Int) {
        recordMic!!.setImageResource(icon)
    }

    fun setSlideMarginRight(marginRight: Int) {
        setMarginRight(marginRight, true)
    }


    fun setCustomSounds(startSound: Int, finishedSound: Int, errorSound: Int) {
        //0 means do not play sound
        RECORD_START = startSound
        RECORD_FINISHED = finishedSound
        RECORD_ERROR = errorSound
    }

    fun getCancelOffset(): Float {
        return cancelOffset
    }

    fun setCancelOffset(cancelBounds: Float) {
        setCancelOffset(cancelBounds, true)
    }

    //set Chronometer color
    fun setCounterTimeColor(color: Int) {
        counterTime!!.setTextColor(color)
    }

    fun setSlideToCancelArrowColor(color: Int) {
        arrow!!.setColorFilter(color)
    }


    private fun setCancelOffset(cancelBounds: Float, DpToPixel: Boolean) {
        val bounds = if (DpToPixel) CommonUtil.dpToPx(context, cancelBounds) else cancelBounds
        this.cancelOffset = bounds
    }

    companion object {

        val DEFAULT_CANCEL_OFFSET = 8
    }


}