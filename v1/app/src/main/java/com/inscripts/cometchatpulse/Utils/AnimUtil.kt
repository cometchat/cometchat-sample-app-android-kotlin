package com.inscripts.cometchatpulse.Utils

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.util.Pair
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.FrameLayout
import android.widget.ImageView
import com.inscripts.cometchatpulse.CustomView.RecordMicButton

class AnimUtil {

    companion object {

        private lateinit var alphaAnimation: AlphaAnimation

        private var isStartRecorded: Boolean = false

        fun start(view: View) {
            val set = AnimatorSet()
            val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 2.0f)
            val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 2.0f)
            set.duration = 150
            set.interpolator = AccelerateDecelerateInterpolator()
            set.playTogether(scaleY, scaleX)
            set.start()
        }

        fun stop(view: View) {
            val set = AnimatorSet()
            val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f)

            val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f)
            set.duration = 150
            set.interpolator = AccelerateDecelerateInterpolator()
            set.playTogether(scaleY, scaleX)
            set.start()
        }

        fun getClickOrigin(anchor: View?, contentView: View): Pair<Int, Int> {
            if (anchor == null) return Pair(0, 0)

            val anchorCoordinates = IntArray(2)
            anchor.getLocationOnScreen(anchorCoordinates)
            anchorCoordinates[0] += anchor.width / 2
            anchorCoordinates[1] += anchor.height / 2

            val contentCoordinates = IntArray(2)
            contentView.getLocationOnScreen(contentCoordinates)

            val x = anchorCoordinates[0] - contentCoordinates[0]
            val y = anchorCoordinates[1] - contentCoordinates[1]

            return Pair(x, y)
        }


        fun animateMic(recordMic: ImageView) {
            alphaAnimation = AlphaAnimation(0.0f, 1.0f)
            alphaAnimation.setDuration(500)
            alphaAnimation.setRepeatMode(Animation.REVERSE)
            alphaAnimation.setRepeatCount(Animation.INFINITE)
            recordMic.startAnimation(alphaAnimation)
        }

        fun resetSmallMic(recordMic: ImageView) {
            recordMic.alpha = 1.0f
            recordMic.scaleX = 1.0f
            recordMic.scaleY = 1.0f
        }


        fun getShakeAnimation(target: View) {
//            val animator = ObjectAnimator.ofFloat(target, target.translationX, 0, 25, -25, 25, -25, 15, -15, 6, -6, 0)
//            animator.setDuration(500)
//            animator.start()

        }

        fun moveSlideToCancel(recordMicButton: RecordMicButton, layout: FrameLayout, initialX: Float, difX: Float) {

            val positionAnimator = ValueAnimator.ofFloat(recordMicButton.getX(), initialX)

            positionAnimator.interpolator = AccelerateDecelerateInterpolator()
            positionAnimator.addUpdateListener { animation ->
                val x = animation.animatedValue as Float
                recordMicButton.setX(x)
            }

            recordMicButton.stopScale()
            positionAnimator.duration = 0
            positionAnimator.start()


            // if the move event was not called ,then the difX will still 0 and there is no need to move it back
            if (difX != 0f) {
                val x = initialX - difX
                layout.animate()
                        .x(x)
                        .setDuration(0)
                        .start()
            }


        }

        fun clearAlphaAnimation(hideView: Boolean, recordMic: ImageView) {
            alphaAnimation.cancel()
            alphaAnimation.reset()
            recordMic.clearAnimation()
            if (hideView) {
                recordMic.visibility = View.GONE
            }
        }


        fun setStartRecorded(startRecorded: Boolean) {
            isStartRecorded = startRecorded
        }

    }
}