package com.inscripts.cometchatpulse.CustomView

import android.content.Context
import android.support.v7.content.res.AppCompatResources
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.inscripts.cometchatpulse.Helpers.OnRecordClickListener
import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.Utils.AnimUtil


class RecordMicButton : android.support.v7.widget.AppCompatImageView, View.OnTouchListener, View.OnClickListener {

    private var scaleView: View? = null

    var isRecord = true


    private var onRecordClickListener: OnRecordClickListener? = null

    private var recordAudio: RecordAudio? = null

    fun setRecordAudio(recordAudio: RecordAudio?) {
        this.recordAudio = recordAudio

    }

    constructor(context: Context) : super(context) {
        initViewComponent(context, null)

    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initViewComponent(context, attrs)

    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initViewComponent(context, attrs)

    }


    private fun initViewComponent(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RecordMic)

            val imageResource = typedArray.getResourceId(R.styleable.RecordMic_mic_icon, -1)


            if (imageResource != -1) {
                setImage(imageResource)
            }

            scaleView = this


            typedArray.recycle()
        }

        this.setOnTouchListener(this)
        this.setOnClickListener(this)

    }


    private fun setImage(imageResource: Int) {
        val image = AppCompatResources.getDrawable(getContext(), imageResource)
        setImageDrawable(image)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setClip(this)
    }

    fun setClip(v: View) {
        if (v.parent == null) {
            return
        }

        if (v is ViewGroup) {
            v.clipChildren = false
            v.clipToPadding = false
        }

        if (v.parent is View) {
            setClip(v.parent as View)
        }
    }

    fun startScale() {
        scaleView?.let { AnimUtil.start(it) }
    }

    fun stopScale() {
        scaleView?.let { AnimUtil.stop(it) }
    }

    fun setListenForRecord(listenForRecord: Boolean) {
        this.isRecord = listenForRecord

    }

    fun setOnRecordClickListener(onRecordClickListener: OnRecordClickListener) {
        this.onRecordClickListener = onRecordClickListener
    }


    override fun onClick(view: View) {
        if (onRecordClickListener != null)
            onRecordClickListener!!.onRecordClick(view)

    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        if (isRecord) {
            when (motionEvent.action) {

                MotionEvent.ACTION_DOWN ->{ recordAudio!!.onActionDown(view as RecordMicButton, context)}

                MotionEvent.ACTION_MOVE -> {recordAudio!!.onActionMove(view as RecordMicButton, motionEvent)}

                MotionEvent.ACTION_UP -> {recordAudio!!.onActionUp(view as RecordMicButton)}
            }

        }
        return isRecord
    }
}
