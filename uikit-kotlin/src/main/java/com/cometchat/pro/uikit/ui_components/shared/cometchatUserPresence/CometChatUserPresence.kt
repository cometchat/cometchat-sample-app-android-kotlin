package com.cometchat.pro.uikit.ui_components.shared.cometchatUserPresence

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import com.cometchat.pro.uikit.R

/**
 * Purpose - StatusIndicator is a subclass of View and it is used as component to display status
 * indicator of user. If helps to know whether user is online or offline.
 *
 * Created on - 20th December 2019
 *
 * Modified on  - 16th January 2020
 *
 */
@BindingMethods(value = [BindingMethod(type = CometChatUserPresence::class, attribute = "app:user_status", method = "setUserStatus")])
class CometChatUserPresence : View {
    private var paint: Paint? = null
    var rectF: RectF? = null
    var status = 0

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        getAttributes(attrs)
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        getAttributes(attrs)
        init()
    }

    fun setUserStatus(userStatus: String?) {
        status = if ("online".equals(userStatus, ignoreCase = true)) {
            ONLINE
        } else {
            OFFLINE
        }
        setValues()
    }

    private fun setValues() {
        if (status == ONLINE) paint!!.color = Color.parseColor("#3BDF2F") else {
            paint!!.color = Color.parseColor("#C4C4C4")
        }
        invalidate()
    }

    private fun setColor(@ColorInt color: Int) {
        paint!!.color = color
        invalidate()
    }

    private fun getAttributes(attrs: AttributeSet?) {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.StatusIndicator, 0, 0)
        val userStatus = a.getString(R.styleable.StatusIndicator_user_status)
        status = if (userStatus == null) {
            OFFLINE
        } else {
            if (context.getString(R.string.online).equals(userStatus, ignoreCase = true)) {
                ONLINE
            } else {
                OFFLINE
            }
        }
    }

    protected fun init() {
        paint = Paint()
        rectF = RectF()
        if (status == ONLINE) paint!!.color = Color.parseColor("#3BDF2F") else paint!!.color = Color.parseColor("#C4C4C4")
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val screenWidth = MeasureSpec.getSize(widthMeasureSpec)
        val screenHeight = MeasureSpec.getSize(heightMeasureSpec)
        rectF!![0f, 0f, screenWidth.toFloat()] = screenHeight.toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawCircle(rectF!!.centerX(), rectF!!.centerY(), rectF!!.height() / 2, paint!!)
    }

    companion object {
        /*
     * Constants to define shape
     * */
        protected const val OFFLINE = 0
        protected const val ONLINE = 1
    }
}