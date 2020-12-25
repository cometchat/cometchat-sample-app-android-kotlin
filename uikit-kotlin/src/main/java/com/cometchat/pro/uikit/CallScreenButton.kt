package com.cometchat.pro.uikit

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.google.android.material.floatingactionbutton.FloatingActionButton
import utils.MediaUtils
import utils.Utils

class CallScreenButton : LinearLayout, OnTouchListener {
    private var ivRedArrowOne: ImageView? = null
    private var ivRedArrowTwo: ImageView? = null
    private var ivRedArrowThree: ImageView? = null
    private var ivGreenArrowOne: ImageView? = null
    private var ivGreenArrowTwo: ImageView? = null
    private var ivGreenArrowThree: ImageView? = null
    private var fab: FloatingActionButton? = null
    private var lastY = 0f
    private var animating = false
    private var complete = false
    private var animatorSet: AnimatorSet? = null
    private var listener: AnswerDeclineListener? = null
    private var isVideo = false
    private val swipeUpText: TextView? = null
    private val swipeDownText: TextView? = null
    private val isIncoming = false

    constructor(context: Context?) : super(context) {
        intiComponentView()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        intiComponentView()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        intiComponentView()
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        intiComponentView()
    }

    @SuppressLint("RestrictedApi")
    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                resetAnimation()
                lastY = motionEvent.rawY
            }
            MotionEvent.ACTION_CANCEL -> {
            }
            MotionEvent.ACTION_UP -> {
                fab!!.rotation = 0f
                if (Build.VERSION.SDK_INT >= 21) {
                    fab!!.drawable.setTint(resources.getColor(R.color.green_600))
                    fab!!.background.setTint(Color.WHITE)
                }
                animating = true
                animateElement(0)
            }
            MotionEvent.ACTION_MOVE -> {
                val diff = motionEvent.rawY - lastY
                val diffThreshold: Float
                val percentageToThreshold: Float
                val backgroundColor: Int
                val foregroundColor: Int
                if (diff < 0) {
                    diffThreshold = Utils.dpToPx(context, ANSWER_THRESHOLD.toFloat())
                    percentageToThreshold = Math.min(1f, diff * -1 / diffThreshold)
                    backgroundColor = ArgbEvaluator().evaluate(percentageToThreshold,
                            resources.getColor(R.color.green_100), resources.getColor(R.color.green_600)) as Int
                    foregroundColor = if (percentageToThreshold > 0.5) {
                        Color.WHITE
                    } else {
                        resources.getColor(R.color.green_600)
                    }
                    fab!!.translationY = diff
                    if (percentageToThreshold == 1f && listener != null) {
                        fab!!.visibility = View.INVISIBLE
                        lastY = motionEvent.rawY
                        if (!complete) {
                            complete = true
                            MediaUtils.vibrate(context)
                            listener!!.onAnswered()
                        }
                    }
                } else {
                    diffThreshold = Utils.dpToPx(context, DECLINE_THRESHOLD.toFloat())
                    percentageToThreshold = Math.min(1f, diff / diffThreshold)
                    backgroundColor = ArgbEvaluator().evaluate(percentageToThreshold,
                            resources.getColor(R.color.red_100), resources.getColor(R.color.red_600)) as Int
                    foregroundColor = if (percentageToThreshold > 0.5) {
                        Color.WHITE
                    } else {
                        resources.getColor(R.color.green_600)
                    }
                    if (!isVideo) {
                        fab!!.rotation = 135 * percentageToThreshold
                    }
                    if (percentageToThreshold == 1f && listener != null) {
                        fab!!.visibility = View.INVISIBLE
                        lastY = motionEvent.rawY
                        if (!complete) {
                            complete = true
                            MediaUtils.vibrate(context)
                            listener!!.onDeclined()
                        }
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    fab!!.background.setTint(backgroundColor)
                    fab!!.drawable.setTint(foregroundColor)
                }
            }
        }
        return true
    }

    fun setAnswerDeclineListener(listener: AnswerDeclineListener?, isVideo: Boolean) {
        this.listener = listener
        this.isVideo = isVideo
        if (isVideo) {
            fab!!.setImageDrawable(resources.getDrawable(R.drawable.ic_videocam_green_24dp))
        } else {
            fab!!.background = resources.getDrawable(R.drawable.ic_call_green24dp)
        }
    }

    private fun intiComponentView() {
        orientation = VERTICAL
        layoutParams = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        val view = View.inflate(context, R.layout.cometchat_call_screen_button, this)
        ivGreenArrowOne = view.findViewById(R.id.green_arrow_one)
        ivGreenArrowTwo = view.findViewById(R.id.green_arrow_two)
        ivGreenArrowThree = view.findViewById(R.id.green_arrow_three)
        ivRedArrowOne = view.findViewById(R.id.red_arrow_one)
        ivRedArrowTwo = view.findViewById(R.id.red_arrow_two)
        ivRedArrowThree = view.findViewById(R.id.red_arrow_three)
        fab = view.findViewById(R.id.fab)
        fab!!.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.colorPrimary)))
        fab!!.setOnTouchListener(this)
    }

    fun startAnimation() {
        if (!animating) {
            animating = true
            animateElement(0)
        }
    }

    fun stopAnimation() {
        if (animating) {
            animating = false
            resetAnimation()
        }
    }

    private fun animateElement(delay: Int) {
        val fabUp = getUpAnimation(fab)
        val fabDown = getDownAnimation(fab)
        val fabShake = getShakeAnimation(fab)
        animatorSet = AnimatorSet()
        animatorSet!!.play(fabShake).after(fabUp)
        animatorSet!!.play(fabDown).after(fabShake)
        animatorSet!!.play(getShimmer(ivGreenArrowOne!!, ivGreenArrowTwo!!, ivGreenArrowThree!!,
                ivRedArrowOne!!, ivRedArrowTwo!!, ivRedArrowThree!!))
        animatorSet!!.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                if (animating) animateElement(1000)
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        animatorSet!!.startDelay = delay.toLong()
        animatorSet!!.start()
    }

    private fun getShimmer(vararg views: View): Animator {
        val animatorSet = AnimatorSet()
        val evenDuration = SHIMMER_TOTAL / views.size
        val interval = 75
        for (i in 0 until views.size) {
            animatorSet.play(getShimmer(views[i], evenDuration + (evenDuration - interval)))
                    .after(interval * i.toLong())
        }
        return animatorSet
    }

    private fun getShakeAnimation(target: View?): ObjectAnimator {
        val animator = ObjectAnimator.ofFloat(target, "translationX", 0f, 15f, -15f, 15f, -15f, 10f, -10f, 5f, -5f, 0f)
        animator.duration = SHAKE_TIME.toLong()
        return animator
    }

    private fun getUpAnimation(target: View?): ObjectAnimator {
        val animator = ObjectAnimator.ofFloat(target, "translationY", 0f, -1 * Utils.dpToPx(context, 16f))
        animator.interpolator = AccelerateInterpolator()
        animator.duration = UP_TIME.toLong()
        return animator
    }

    private fun getShimmer(target: View, duration: Int): ObjectAnimator {
        val shimmer = ObjectAnimator.ofFloat(target, "alpha", 0f, 0.5f, 1f, 0f, 1f, 0.5f)
        shimmer.duration = duration.toLong()
        return shimmer
    }

    private fun getDownAnimation(target: View?): ObjectAnimator {
        val animator = ObjectAnimator.ofFloat(target, "translationY", 0f)
        animator.interpolator = DecelerateInterpolator()
        animator.duration = DOWN_TIME.toLong()
        return animator
    }

    private fun resetAnimation() {
        animating = false
        complete = false
        if (animatorSet != null) animatorSet!!.cancel()
        fab!!.translationY = 0f
    }

    interface AnswerDeclineListener {
        fun onAnswered()
        fun onDeclined()
    }

    companion object {
        private const val TOTAL_TIME = 1000
        private const val SHAKE_TIME = 200
        private const val UP_TIME = (TOTAL_TIME - SHAKE_TIME) / 2
        private const val DOWN_TIME = (TOTAL_TIME - SHAKE_TIME) / 2
        private const val SHIMMER_TOTAL = UP_TIME + SHAKE_TIME
        private const val ANSWER_THRESHOLD = 40
        private const val DECLINE_THRESHOLD = 56
    }
}