package utils

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView

public class AnimUtil {
    companion object {

        private var alphaAnimation: AlphaAnimation? = null

        private var isStartRecorded = false


        fun stopBlinkAnimation(view: View) {
            view.clearAnimation()
            view.alpha = 1.0f
        }

        fun blinkAnimation(view: View) {
            val anim: Animation = AlphaAnimation(0.0f, 1.0f)
            anim.duration = 700 //You can manage the time of the blink with this parameter
            anim.startOffset = 20
            anim.repeatMode = Animation.REVERSE
            anim.repeatCount = Animation.INFINITE
            view.startAnimation(anim)
        }

        fun start(view: View?) {
            val set = AnimatorSet()
            val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 2.0f)
            val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 2.0f)
            set.duration = 150
            set.interpolator = AccelerateDecelerateInterpolator()
            set.playTogether(scaleY, scaleX)
            set.start()
        }

        fun stop(view: View?) {
            val set = AnimatorSet()
            val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f)
            val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f)
            set.duration = 150
            set.interpolator = AccelerateDecelerateInterpolator()
            set.playTogether(scaleY, scaleX)
            set.start()
        }

        fun setStartRecorded(startRecorded: Boolean) {
            AnimUtil.isStartRecorded = startRecorded
        }

        fun clearAlphaAnimation(hideView: Boolean, recordMic: ImageView) {
            alphaAnimation!!.cancel()
            alphaAnimation!!.reset()
            recordMic.clearAnimation()
            if (hideView) {
                recordMic.visibility = View.GONE
            }
        }

        fun animateMic(recordMic: ImageView) {
            alphaAnimation = AlphaAnimation(0.0f, 1.0f)
            alphaAnimation!!.setDuration(500)
            alphaAnimation!!.setRepeatMode(Animation.REVERSE)
            alphaAnimation!!.setRepeatCount(Animation.INFINITE)
            recordMic.startAnimation(alphaAnimation)
        }

        fun resetSmallMic(recordMic: ImageView) {
            recordMic.alpha = 1.0f
            recordMic.scaleX = 1.0f
            recordMic.scaleY = 1.0f
        }

    }
}